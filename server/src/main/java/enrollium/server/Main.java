package enrollium.server;

import banner.Issue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.JsonUtils;
import core.SessionInfo;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Faculty;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.User;
import enrollium.server.db.entity.types.UserType;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.ServerRPC;
import server.SessionManager;
import version.Version;

import java.security.SecureRandom;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
public class Main {
    private static final Random         random        = new Random();
    private static final SecureRandom   secureRandom  = new SecureRandom();
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static final String         ADMIN         = "admin";
    private static final String         ADMIN_EMAIL   = "admin@uiu.ac.bd";

    public static void main(String[] args) {
        Issue.print(log);
        log.info("[VERSION]: {}", Version.getVersion());

        // Create admin user if it doesn't exist
        createAdminUserIfNeeded();

        // Add shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Initiating server shutdown...");
                shutdownLatch.countDown();
            } catch (Exception e) {
                log.warn("Exception during shutdown: {}", e.getMessage());
            } finally {
                log.info("Shutdown complete.");
            }
        }));

        ServerRPC.initialize();
        try (ServerRPC server = ServerRPC.getInstance()) {
            registerMethods(server);

            server.start();
            log.info("Server is running. Press Ctrl+C to exit.");

            // Wait for shutdown signal
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Shutdown interrupted: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Fatal error during server operation: {}", e.getMessage());
        } finally {
            log.info("Server stopped.");
        }
    }

    private static void createAdminUserIfNeeded() {
        try {
            // Check if admin already exists
            boolean adminExists = DB.read(Faculty.class, 1, 0)
                                    .filter(f -> ADMIN_EMAIL.equals(f.getEmail()))
                                    .count()
                                    .blockingGet() > 0;

            if (!adminExists) {
                // Generate random 16 digit password
                String password = secureRandom.ints(16, 0, 10).mapToObj(String::valueOf).collect(Collectors.joining());
                // password = "adminpass";

                Faculty admin = new Faculty();
                admin.setEmail(ADMIN_EMAIL);
                admin.setName(ADMIN);
                admin.setPassword(password);
                admin.setType(UserType.ADMIN);
                admin.setShortcode(ADMIN);

                DB.save(admin).blockingGet();
                log.info("Created admin user with email: {} password: {}", ADMIN_EMAIL, password);
            }
        } catch (Exception e) {
            log.error("Failed to create admin user: {}", e.getMessage());
        }
    }

    private static void registerMethods(ServerRPC server) {
        server.registerMethod("auth", (params, request) -> {
            try {
                // Validate request parameters
                if (params == null) return Single.error(new IllegalArgumentException("Missing auth parameters"));

                String email    = JsonUtils.getString(params, "email");
                String password = JsonUtils.getString(params, "password");

                if (!(email != null && password != null && !email.trim().isEmpty() && !password.trim().isEmpty()))
                    return Single.error(new IllegalArgumentException("Invalid credentials"));

                // Try to find user in both Student and Faculty tables
                return Single.defer(() -> {
                    // Try to find user in Student table
                    Single<User> studentSearch = DB.read(Student.class, 1, 0)
                                                   .filter(s -> email.equals(s.getEmail()))
                                                   .firstOrError()
                                                   .cast(User.class);

                    // Try to find user in Faculty table
                    Single<User> facultySearch = DB.read(Faculty.class, 1, 0)
                                                   .filter(f -> email.equals(f.getEmail()))
                                                   .firstOrError()
                                                   .cast(User.class);

                    // Try both tables
                    return Single.merge(studentSearch, facultySearch).firstOrError().flatMap(user1 -> {
                        if (!user1.verifyPassword(password))
                            return Single.error(new IllegalArgumentException("Invalid password"));

                        return Single.just(user1);
                    }).onErrorResumeNext(error -> {
                        if (error instanceof NoSuchElementException)
                            return Single.error(new IllegalArgumentException("User not found"));

                        return Single.error(error);
                    });
                }).flatMap(user -> {
                    // Generate user ID and create session
                    String UUID = user.getId().toString();

                    // Create new session with the authenticated connection
                    SessionInfo session = SessionManager.getInstance()
                                                        .createSession(UUID, request.getConnection()
                                                                                    .getSocket(), server);

                    // Return success response with session token and user details
                    ObjectNode response = JsonUtils.createObject()
                                                   .put("sessionToken", session.getSessionToken())
                                                   .put("uuid", UUID)
                                                   .put("userType", user.getType().toString());

                    return Single.just(response);
                });
            } catch (Exception e) {
                return Single.error(e);
            }
        });

        server.registerMethod("health", (_, _) -> {
            ObjectNode response = JsonUtils.createObject()
                                           .put("serverVersion", Version.getVersion())
                                           .put("status", "ok")
                                           .put("serverTime", System.currentTimeMillis());

            return Single.just(response);
        });

        server.registerMethod("Student.create", (params, _) -> Single.defer(() -> {
            String name     = JsonUtils.getString(params, "name");
            String email    = JsonUtils.getString(params, "email");
            String password = JsonUtils.getString(params, "password");

            // Create new student
            Student student = new Student();
            student.setName(name);
            student.setEmail(email);
            student.setPassword(password);
            student.setType(UserType.STUDENT);

            // Generate a unique university ID (1000-9999)
            int universityId = 1000 + random.nextInt(9000);
            student.setUniversityId(universityId);

            return DB.save(student).map(saved -> {
                log.info("Created new student: {}", saved.getName());
                return JsonUtils.createObject()
                                .put("id", saved.getId().toString())
                                .put("name", saved.getName())
                                .put("email", saved.getEmail())
                                .put("universityId", saved.getUniversityId().toString());
            }).doOnError(error -> log.warn("Failed to create student: {}", error.getMessage()));
        }));

        server.registerMethod("Students.getAll", (_, _) -> Single.defer(() -> {
            log.info("Fetching all students");

            return DB.read(Student.class, 100, 0).timeout(20, TimeUnit.SECONDS).toList().map(studentList -> {
                log.info("Found {} students", studentList.size());
                ArrayNode students = JsonUtils.createArray();

                for (Student student : studentList) {
                    students.add(JsonUtils.createObject()
                                          .put("id", student.getId().toString())
                                          .put("name", student.getName())
                                          .put("email", student.getEmail())
                                          .put("universityId", student.getUniversityId().toString()));
                }

                return (JsonNode) JsonUtils.createObject().set("students", students);
            }).doOnError(error -> log.warn("Failed to fetch students: {}", error.getMessage()));
        }));

        server.registerMethod("Student.update", (params, _) -> Single.defer(() -> {
            String id    = JsonUtils.getString(params, "id");
            String name  = JsonUtils.getString(params, "name");
            String email = JsonUtils.getString(params, "email");

            return DB.findById(Student.class, UUID.fromString(id)).toSingle().flatMap(student -> {
                student.setName(name);
                student.setEmail(email);

                return DB.update(student).map(updated -> {
                    log.info("Updated student: {}", updated.getName());
                    return JsonUtils.createObject()
                                    .put("id", updated.getId().toString())
                                    .put("name", updated.getName())
                                    .put("email", updated.getEmail())
                                    .put("universityId", updated.getUniversityId().toString());
                });
            }).doOnError(error -> log.warn("Failed to update student: {}", error.getMessage()));
        }));

        server.registerMethod("Student.delete", (params, _) -> Single.defer(() -> {
            String id = JsonUtils.getString(params, "id");

            return DB.delete(Student.class, UUID.fromString(id)).toSingle(() -> {
                log.info("Deleted student with ID: {}", id);
                return JsonUtils.createObject().put("success", true);
            }).doOnError(error -> log.warn("Failed to delete student: {}", error.getMessage()));
        }));
    }
}
