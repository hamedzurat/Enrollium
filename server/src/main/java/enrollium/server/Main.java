package enrollium.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enrollium.design.system.memory.Volatile;
import enrollium.lib.banner.Issue;
import enrollium.lib.version.Version;
import enrollium.rpc.core.JsonUtils;
import enrollium.rpc.core.Request;
import enrollium.rpc.core.SessionInfo;
import enrollium.rpc.server.ServerRPC;
import enrollium.rpc.server.SessionManager;
import enrollium.server.db.DB;
import enrollium.server.db.entity.*;
import enrollium.server.db.entity.types.*;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.NetworkIF;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
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
    private static final String         STUDENT_EMAIL = "demo.student@uiu.ac.bd";

    public static void main(String[] args) {
        Issue.print(log);
        log.info("[VERSION]: {}", Version.getVersion());

        try {
            DB.resetAndSeed().blockingAwait();
            log.info("Database reset and seed completed");
        } catch (Exception e) {
            log.error("Failed to reset and seed database", e);
            throw new RuntimeException("Database reset failed", e);
        }

        // Create demo user if it doesn't exist
        createDemoUserIfNeeded();

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

    private static void createDemoUserIfNeeded() {
        try {
            // Check if admin already exists
            boolean adminExists = DB.read(Faculty.class, 1, 0)
                                    .filter(f -> ADMIN_EMAIL.equals(f.getEmail()))
                                    .count()
                                    .blockingGet() > 0;

            if (!adminExists) {
                // Ensure deletion is complete before creating a new admin
                String adminPassword = secureRandom.ints(16, 0, 10)
                                                   .mapToObj(String::valueOf)
                                                   .collect(Collectors.joining());
                adminPassword = "demoAdm1nPa$$";

                Faculty admin = new Faculty();
                admin.setEmail(ADMIN_EMAIL);
                admin.setName(ADMIN);
                admin.setPassword(adminPassword);
                admin.setType(UserType.ADMIN);
                admin.setShortcode(ADMIN);
                admin.setInfo(adminPassword);

                DB.save(admin).blockingGet();
                log.info("Created admin user with email: {} password: \"{}\"", ADMIN_EMAIL, adminPassword);
            }

            boolean studentExists = DB.read(Student.class, 1, 0)
                                      .filter(s -> STUDENT_EMAIL.equals(s.getEmail()))
                                      .count()
                                      .blockingGet() > 0;

            if (!studentExists) {
                // Ensure deletion is complete before creating a new demo student
                String studentPassword = secureRandom.ints(16, 0, 10)
                                                     .mapToObj(String::valueOf)
                                                     .collect(Collectors.joining());
                studentPassword = "demo$tudentP4ss";

                Student demoStudent = new Student();
                demoStudent.setEmail(STUDENT_EMAIL);
                demoStudent.setName("Demo Student");
                demoStudent.setPassword(studentPassword);
                demoStudent.setUniversityId(1001);
                demoStudent.setType(UserType.STUDENT);
                demoStudent.setInfo(studentPassword);

                DB.save(demoStudent).blockingGet();
                log.info("Created demo student with email: {} password: \"{}\"", STUDENT_EMAIL, studentPassword);
            }
        } catch (Exception e) {
            log.error("Failed to create admin or demo student user: {}", e.getMessage(), e);
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

                int size = Math.toIntExact(DB.count(User.class).blockingGet());

                return DB.read(User.class, size, 0)
                         .filter(user -> email.equals(user.getEmail()))
                         .firstElement()
                         .toSingle()
                         .flatMap(user -> {
                             if (!user.verifyPassword(password)) {
                                 return Single.error(new IllegalArgumentException("Invalid password"));
                             }

                             // Create session
                             String UUID = user.getId().toString();
                             SessionInfo session = SessionManager.getInstance()
                                                                 .createSession(UUID, request.getConnection()
                                                                                             .getSocket(), server);

                             // Return success response
                             JsonNode response = JsonUtils.createObject()
                                                          .put("sessionToken", session.getSessionToken())
                                                          .put("uuid", UUID)
                                                          .put("userType", user.getType().toString());

                             return Single.just(response);
                         })
                         .onErrorResumeNext(error -> {
                             if (error instanceof NoSuchElementException) {
                                 return Single.error(new IllegalArgumentException("User not found"));
                             }
                             return Single.error(error);
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

        server.registerMethod("Faculty.create", (params, _) -> Single.defer(() -> {
            try {
                String   name      = JsonUtils.getString(params, "name");
                String   email     = JsonUtils.getString(params, "email");
                String   password  = JsonUtils.getString(params, "password");
                String   shortcode = JsonUtils.getString(params, "shortcode");
                UserType type      = UserType.valueOf(JsonUtils.getString(params, "type"));

                Faculty faculty = new Faculty();
                faculty.setName(name);
                faculty.setEmail(email);
                faculty.setPassword(password);
                faculty.setShortcode(shortcode);
                faculty.setType(type);

                return DB.save(faculty)
                         .map(saved -> JsonUtils.createObject()
                                                .put("id", saved.getId().toString())
                                                .put("name", saved.getName())
                                                .put("email", saved.getEmail())
                                                .put("shortcode", saved.getShortcode())
                                                .put("type", saved.getType().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create faculty: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid faculty data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Faculty.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(faculty -> JsonUtils.createObject()
                                                  .put("id", faculty.getId().toString())
                                                  .put("name", faculty.getName())
                                                  .put("email", faculty.getEmail())
                                                  .put("shortcode", faculty.getShortcode())
                                                  .put("type", faculty.getType().toString()))
                         .collect(ArrayList::new, ArrayList::add)
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch faculty list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Faculty.class, UUID.fromString(id))
                         .map(faculty -> JsonUtils.createObject()
                                                  .put("id", faculty.getId().toString())
                                                  .put("name", faculty.getName())
                                                  .put("email", faculty.getEmail())
                                                  .put("shortcode", faculty.getShortcode())
                                                  .put("type", faculty.getType().toString()))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Faculty not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find faculty: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid faculty ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.update", (params, _) -> Single.defer(() -> {
            try {
                String id        = JsonUtils.getString(params, "id");
                String name      = JsonUtils.getString(params, "name");
                String email     = JsonUtils.getString(params, "email");
                String shortcode = JsonUtils.getString(params, "shortcode");

                return DB.findById(Faculty.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(faculty -> {
                             faculty.setName(name);
                             faculty.setEmail(email);
                             faculty.setShortcode(shortcode);
                             return DB.update(faculty);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("name", updated.getName())
                                                  .put("email", updated.getEmail())
                                                  .put("shortcode", updated.getShortcode())
                                                  .put("type", updated.getType().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update faculty: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Faculty.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete faculty: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid faculty ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.count", (_, _) -> Single.defer(() -> DB.count(Faculty.class)
                                                                              .map(count -> JsonUtils.createObject()
                                                                                                     .put("count", count))
                                                                              .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count faculty: " + error.getMessage())))));

        server.registerMethod("Faculty.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Faculty.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check faculty existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid faculty ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.searchByName", (params, _) -> Single.defer(() -> {
            try {
                String name   = JsonUtils.getString(params, "name");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Faculty.class, limit, offset)
                         .filter(faculty -> faculty.getName().toLowerCase().contains(name.toLowerCase()))
                         .map(faculty -> JsonUtils.createObject()
                                                  .put("id", faculty.getId().toString())
                                                  .put("name", faculty.getName())
                                                  .put("email", faculty.getEmail())
                                                  .put("shortcode", faculty.getShortcode())
                                                  .put("type", faculty.getType().toString()))
                         .collect(ArrayList::new, ArrayList::add)
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search faculty by name: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Faculty.searchByEmail", (params, _) -> Single.defer(() -> {
            try {
                String email  = JsonUtils.getString(params, "email");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Faculty.class, limit, offset)
                         .filter(faculty -> faculty.getEmail().toLowerCase().contains(email.toLowerCase()))
                         .map(faculty -> JsonUtils.createObject()
                                                  .put("id", faculty.getId().toString())
                                                  .put("name", faculty.getName())
                                                  .put("email", faculty.getEmail())
                                                  .put("shortcode", faculty.getShortcode())
                                                  .put("type", faculty.getType().toString()))
                         .collect(ArrayList::new, ArrayList::add)
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search faculty by email: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        // Student methods
        server.registerMethod("Student.create", (params, _) -> Single.defer(() -> {
            try {
                String name     = JsonUtils.getString(params, "name");
                String email    = JsonUtils.getString(params, "email");
                String password = JsonUtils.getString(params, "password");

                Student student = new Student();
                student.setName(name);
                student.setEmail(email);
                student.setPassword(password);
                // Generate a unique university ID (1000-9999)
                student.setUniversityId(1000 + random.nextInt(9000));

                return DB.save(student)
                         .map(saved -> JsonUtils.createObject()
                                                .put("id", saved.getId().toString())
                                                .put("name", saved.getName())
                                                .put("email", saved.getEmail())
                                                .put("universityId", saved.getUniversityId().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create student: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid student data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Student.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(student -> JsonUtils.createObject()
                                                  .put("id", student.getId().toString())
                                                  .put("name", student.getName())
                                                  .put("email", student.getEmail())
                                                  .put("universityId", student.getUniversityId().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch student list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Student.class, UUID.fromString(id))
                         .map(student -> JsonUtils.createObject()
                                                  .put("id", student.getId().toString())
                                                  .put("name", student.getName())
                                                  .put("email", student.getEmail())
                                                  .put("universityId", student.getUniversityId().toString()))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Student not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find student: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid student ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.update", (params, _) -> Single.defer(() -> {
            try {
                String id    = JsonUtils.getString(params, "id");
                String name  = JsonUtils.getString(params, "name");
                String email = JsonUtils.getString(params, "email");

                return DB.findById(Student.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(student -> {
                             student.setName(name);
                             student.setEmail(email);
                             return DB.update(student);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("name", updated.getName())
                                                  .put("email", updated.getEmail())
                                                  .put("universityId", updated.getUniversityId().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update student: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Student.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete student: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid student ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.count", (_, _) -> Single.defer(() -> DB.count(Student.class)
                                                                              .map(count -> JsonUtils.createObject()
                                                                                                     .put("count", count))
                                                                              .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count students: " + error.getMessage())))));

        server.registerMethod("Student.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Student.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check student existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid student ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.searchByName", (params, _) -> Single.defer(() -> {
            try {
                String name   = JsonUtils.getString(params, "name");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Student.class, limit, offset)
                         .filter(student -> student.getName().toLowerCase().contains(name.toLowerCase()))
                         .map(student -> JsonUtils.createObject()
                                                  .put("id", student.getId().toString())
                                                  .put("name", student.getName())
                                                  .put("email", student.getEmail())
                                                  .put("universityId", student.getUniversityId().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search students by name: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.searchByEmail", (params, _) -> Single.defer(() -> {
            try {
                String email  = JsonUtils.getString(params, "email");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Student.class, limit, offset)
                         .filter(student -> student.getEmail().toLowerCase().contains(email.toLowerCase()))
                         .map(student -> JsonUtils.createObject()
                                                  .put("id", student.getId().toString())
                                                  .put("name", student.getName())
                                                  .put("email", student.getEmail())
                                                  .put("universityId", student.getUniversityId().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search students by email: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Student.list", (_, _) -> Single.defer(() -> DB.read(Student.class, 1000, 0)
                                                                             .map(student -> JsonUtils.createObject()
                                                                                                      .put("id", student.getId()
                                                                                                                        .toString())
                                                                                                      .put("name", student.getName()))
                                                                             .collect(ArrayList::new, (list, item) -> list.add(item))
                                                                             .map(list -> {
                                                                                 ObjectNode response = JsonUtils.createObject();
                                                                                 ArrayNode  items    = JsonUtils.createArray();
                                                                                 list.forEach(item -> items.add((JsonNode) item));
                                                                                 response.set("items", items);
                                                                                 return response;
                                                                             })));

        // Subject methods
        server.registerMethod("Subject.create", (params, _) -> Single.defer(() -> {
            try {
                String      name     = JsonUtils.getString(params, "name");
                String      codeName = JsonUtils.getString(params, "codeName");
                int         credits  = JsonUtils.getInt(params, "credits");
                SubjectType type     = SubjectType.valueOf(JsonUtils.getString(params, "type"));

                Subject subject = new Subject();
                subject.setName(name);
                subject.setCodeName(codeName);
                subject.setCredits(credits);
                subject.setType(type);

                return DB.save(subject)
                         .map(saved -> JsonUtils.createObject()
                                                .put("id", saved.getId().toString())
                                                .put("name", saved.getName())
                                                .put("codeName", saved.getCodeName())
                                                .put("credits", saved.getCredits())
                                                .put("type", saved.getType().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create subject: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid subject data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Subject.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(subject -> JsonUtils.createObject()
                                                  .put("id", subject.getId().toString())
                                                  .put("name", subject.getName())
                                                  .put("codeName", subject.getCodeName())
                                                  .put("credits", subject.getCredits())
                                                  .put("type", subject.getType().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch subject list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Subject.class, UUID.fromString(id))
                         .map(subject -> {
                             ObjectNode subjectObj = JsonUtils.createObject()
                                                              .put("id", subject.getId().toString())
                                                              .put("name", subject.getName())
                                                              .put("codeName", subject.getCodeName())
                                                              .put("credits", subject.getCredits())
                                                              .put("type", subject.getType().toString());

                             // Add prerequisites if they exist
                             ArrayNode prereqs = JsonUtils.createArray();
                             subject.getPrerequisites().forEach(prereq -> {
                                 prereqs.add(JsonUtils.createObject()
                                                      .put("id", prereq.getPrerequisite().getId().toString())
                                                      .put("name", prereq.getPrerequisite().getName())
                                                      .put("minimumGrade", prereq.getMinimumGrade()));
                             });
                             subjectObj.set("prerequisites", prereqs);

                             return subjectObj;
                         })
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Subject not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find subject: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid subject ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.update", (params, _) -> Single.defer(() -> {
            try {
                String      id       = JsonUtils.getString(params, "id");
                String      name     = JsonUtils.getString(params, "name");
                String      codeName = JsonUtils.getString(params, "codeName");
                int         credits  = JsonUtils.getInt(params, "credits");
                SubjectType type     = SubjectType.valueOf(JsonUtils.getString(params, "type"));

                return DB.findById(Subject.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(subject -> {
                             subject.setName(name);
                             subject.setCodeName(codeName);
                             subject.setCredits(credits);
                             subject.setType(type);
                             return DB.update(subject);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("name", updated.getName())
                                                  .put("codeName", updated.getCodeName())
                                                  .put("credits", updated.getCredits())
                                                  .put("type", updated.getType().toString()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update subject: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Subject.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete subject: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid subject ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.count", (_, _) -> Single.defer(() -> DB.count(Subject.class)
                                                                              .map(count -> JsonUtils.createObject()
                                                                                                     .put("count", count))
                                                                              .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count subjects: " + error.getMessage())))));

        server.registerMethod("Subject.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Subject.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check subject existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid subject ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.searchByName", (params, _) -> Single.defer(() -> {
            try {
                String name   = JsonUtils.getString(params, "name");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Subject.class, limit, offset)
                         .filter(subject -> subject.getName().toLowerCase().contains(name.toLowerCase()))
                         .map(subject -> JsonUtils.createObject()
                                                  .put("id", subject.getId().toString())
                                                  .put("name", subject.getName())
                                                  .put("codeName", subject.getCodeName())
                                                  .put("credits", subject.getCredits())
                                                  .put("type", subject.getType().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search subjects by name: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.searchByCodeName", (params, _) -> Single.defer(() -> {
            try {
                String codeName = JsonUtils.getString(params, "codeName");
                int    limit    = JsonUtils.getInt(params, "limit");
                int    offset   = JsonUtils.getInt(params, "offset");

                return DB.read(Subject.class, limit, offset)
                         .filter(subject -> subject.getCodeName().toLowerCase().contains(codeName.toLowerCase()))
                         .map(subject -> JsonUtils.createObject()
                                                  .put("id", subject.getId().toString())
                                                  .put("name", subject.getName())
                                                  .put("codeName", subject.getCodeName())
                                                  .put("credits", subject.getCredits())
                                                  .put("type", subject.getType().toString()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search subjects by code name: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Subject.list", (_, _) -> Single.defer(() -> DB.read(Subject.class, 1000, 0)
                                                                             .map(subject -> JsonUtils.createObject()
                                                                                                      .put("id", subject.getId()
                                                                                                                        .toString())
                                                                                                      .put("name", subject.getName()))
                                                                             .collect(ArrayList::new, (list, item) -> list.add(item))
                                                                             .map(list -> {
                                                                                 ObjectNode response = JsonUtils.createObject();
                                                                                 ArrayNode  items    = JsonUtils.createArray();
                                                                                 list.forEach(item -> items.add((JsonNode) item));
                                                                                 response.set("items", items);
                                                                                 return response;
                                                                             })));

        // Prerequisite methods
        server.registerMethod("Prerequisite.create", (params, _) -> Single.defer(() -> {
            try {
                String subjectId      = JsonUtils.getString(params, "subjectId");
                String prerequisiteId = JsonUtils.getString(params, "prerequisiteId");
                double minimumGrade   = JsonUtils.getDouble(params, "minimumGrade");

                return DB.findById(Subject.class, UUID.fromString(subjectId))
                         .toSingle()
                         .flatMap(subject -> DB.findById(Subject.class, UUID.fromString(prerequisiteId))
                                               .toSingle()
                                               .map(prereq -> {
                                                   Prerequisite prerequisite = new Prerequisite();
                                                   prerequisite.setSubject(subject);
                                                   prerequisite.setPrerequisite(prereq);
                                                   prerequisite.setMinimumGrade(minimumGrade);
                                                   return prerequisite;
                                               }))
                         .flatMap(DB::save)
                         .map(saved -> JsonUtils.createObject()
                                                .put("id", saved.getId().toString())
                                                .put("subjectId", saved.getSubject().getId().toString())
                                                .put("subjectName", saved.getSubject().getName())
                                                .put("prerequisiteId", saved.getPrerequisite().getId().toString())
                                                .put("prerequisiteName", saved.getPrerequisite().getName())
                                                .put("minimumGrade", saved.getMinimumGrade()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create prerequisite: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid prerequisite data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Prerequisite.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(prereq -> JsonUtils.createObject()
                                                 .put("id", prereq.getId().toString())
                                                 .put("subjectId", prereq.getSubject().getId().toString())
                                                 .put("subjectName", prereq.getSubject().getName())
                                                 .put("prerequisiteId", prereq.getPrerequisite().getId().toString())
                                                 .put("prerequisiteName", prereq.getPrerequisite().getName())
                                                 .put("minimumGrade", prereq.getMinimumGrade()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch prerequisites list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Prerequisite.class, UUID.fromString(id))
                         .map(prereq -> JsonUtils.createObject()
                                                 .put("id", prereq.getId().toString())
                                                 .put("subjectId", prereq.getSubject().getId().toString())
                                                 .put("subjectName", prereq.getSubject().getName())
                                                 .put("prerequisiteId", prereq.getPrerequisite().getId().toString())
                                                 .put("prerequisiteName", prereq.getPrerequisite().getName())
                                                 .put("minimumGrade", prereq.getMinimumGrade()))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Prerequisite not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find prerequisite: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid prerequisite ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.update", (params, _) -> Single.defer(() -> {
            try {
                String id           = JsonUtils.getString(params, "id");
                double minimumGrade = JsonUtils.getDouble(params, "minimumGrade");

                return DB.findById(Prerequisite.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(prerequisite -> {
                             prerequisite.setMinimumGrade(minimumGrade);
                             return DB.update(prerequisite);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("subjectId", updated.getSubject().getId().toString())
                                                  .put("subjectName", updated.getSubject().getName())
                                                  .put("prerequisiteId", updated.getPrerequisite().getId().toString())
                                                  .put("prerequisiteName", updated.getPrerequisite().getName())
                                                  .put("minimumGrade", updated.getMinimumGrade()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update prerequisite: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Prerequisite.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete prerequisite: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid prerequisite ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.count", (_, _) -> Single.defer(() -> DB.count(Prerequisite.class)
                                                                                   .map(count -> JsonUtils.createObject()
                                                                                                          .put("count", count))
                                                                                   .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count prerequisites: " + error.getMessage())))));

        server.registerMethod("Prerequisite.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Prerequisite.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check prerequisite existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid prerequisite ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Prerequisite.getBySubject", (params, _) -> Single.defer(() -> {
            try {
                String subjectId = JsonUtils.getString(params, "subjectId");
                int    limit     = JsonUtils.getInt(params, "limit");
                int    offset    = JsonUtils.getInt(params, "offset");

                return DB.read(Prerequisite.class, limit, offset)
                         .filter(prereq -> prereq.getSubject().getId().equals(UUID.fromString(subjectId)))
                         .map(prereq -> JsonUtils.createObject()
                                                 .put("id", prereq.getId().toString())
                                                 .put("prerequisiteId", prereq.getPrerequisite().getId().toString())
                                                 .put("prerequisiteName", prereq.getPrerequisite().getName())
                                                 .put("minimumGrade", prereq.getMinimumGrade()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to get prerequisites for subject: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid parameters: " + e.getMessage()));
            }
        }));

        // Course methods
        server.registerMethod("Course.create", (params, _) -> Single.defer(() -> {
            try {
                String       studentId   = JsonUtils.getString(params, "studentId");
                String       subjectId   = JsonUtils.getString(params, "subjectId");
                String       trimesterId = JsonUtils.getString(params, "trimesterId");
                CourseStatus status      = CourseStatus.valueOf(JsonUtils.getString(params, "status"));

                // Get the optional section and grade
                String sectionId = JsonUtils.getStringOptional(params, "sectionId").orElse(null);
                Double grade     = params.has("grade") ? JsonUtils.getDouble(params, "grade") : null;

                // First, load all required entities
                Single<Student> studentSingle = DB.findById(Student.class, UUID.fromString(studentId)).toSingle();
                Single<Subject> subjectSingle = DB.findById(Subject.class, UUID.fromString(subjectId)).toSingle();
                Single<Trimester> trimesterSingle = DB.findById(Trimester.class, UUID.fromString(trimesterId))
                                                      .toSingle();

                // Load section if provided
                Single<Section> sectionSingle = sectionId != null
                                                ? DB.findById(Section.class, UUID.fromString(sectionId)).toSingle()
                                                : Single.just(null);

                // Combine all entities and create course
                return Single.zip(studentSingle, subjectSingle, trimesterSingle, sectionSingle, (student, subject, trimester, section) -> {
                                 Course course = new Course();
                                 course.setStudent(student);
                                 course.setSubject(subject);
                                 course.setTrimester(trimester);
                                 course.setStatus(status);

                                 // Set section based on status
                                 if (status != CourseStatus.SELECTED) {
                                     if (section == null) {
                                         throw new IllegalArgumentException("Section is required for status: " + status);
                                     }
                                     course.setSection(section);
                                 }

                                 // Set grade only for COMPLETED status
                                 if (status == CourseStatus.COMPLETED) {
                                     if (grade == null) {
                                         throw new IllegalArgumentException("Grade is required for COMPLETED status");
                                     }
                                     if (grade < 0.0 || grade > 4.0) {
                                         throw new IllegalArgumentException("Grade must be between 0.0 and 4.0");
                                     }
                                     course.setGrade(grade);
                                 }

                                 return course;
                             })
                             .flatMap(DB::save)
                             .map(saved -> {
                                 ObjectNode response = JsonUtils.createObject()
                                                                .put("id", saved.getId().toString())
                                                                .put("studentId", saved.getStudent().getId().toString())
                                                                .put("studentName", saved.getStudent().getName())
                                                                .put("subjectId", saved.getSubject().getId().toString())
                                                                .put("subjectName", saved.getSubject().getName())
                                                                .put("trimesterId", saved.getTrimester()
                                                                                         .getId()
                                                                                         .toString())
                                                                .put("trimesterCode", saved.getTrimester().getCode())
                                                                .put("status", saved.getStatus().toString());

                                 if (saved.getSection() != null) {
                                     response.put("sectionId", saved.getSection().getId().toString())
                                             .put("sectionName", saved.getSection().getName());
                                 }
                                 if (saved.getGrade() != null) {
                                     response.put("grade", saved.getGrade());
                                 }

                                 return response;
                             })
                             .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create course: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid course data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Course.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(course -> {
                             ObjectNode courseObj = JsonUtils.createObject()
                                                             .put("id", course.getId().toString())
                                                             .put("version", course.getVersion())
                                                             .put("studentId", course.getStudent().getId().toString())
                                                             .put("studentName", course.getStudent().getName())
                                                             .put("subjectId", course.getSubject().getId().toString())
                                                             .put("subjectName", course.getSubject().getName())
                                                             .put("trimesterId", course.getTrimester()
                                                                                       .getId()
                                                                                       .toString())
                                                             .put("trimesterCode", course.getTrimester().getCode())
                                                             .put("status", course.getStatus().toString());

                             if (course.getSection() != null) {
                                 courseObj.put("sectionId", course.getSection().getId().toString());
                                 courseObj.put("sectionName", course.getSection().getName());
                             }
                             if (course.getGrade() != null) {
                                 courseObj.put("grade", course.getGrade());
                             }

                             return courseObj;
                         })
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch course list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Course.class, UUID.fromString(id))
                         .map(course -> {
                             ObjectNode courseObj = JsonUtils.createObject()
                                                             .put("id", course.getId().toString())
                                                             .put("studentId", course.getStudent().getId().toString())
                                                             .put("studentName", course.getStudent().getName())
                                                             .put("subjectId", course.getSubject().getId().toString())
                                                             .put("subjectName", course.getSubject().getName())
                                                             .put("trimesterId", course.getTrimester()
                                                                                       .getId()
                                                                                       .toString())
                                                             .put("status", course.getStatus().toString());

                             if (course.getSection() != null) {
                                 courseObj.put("sectionId", course.getSection().getId().toString());
                                 courseObj.put("sectionName", course.getSection().getName());
                             }
                             if (course.getGrade() != null) {
                                 courseObj.put("grade", course.getGrade());
                             }

                             return courseObj;
                         })
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Course not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find course: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid course ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.update", (params, _) -> Single.defer(() -> {
            try {
                String       id          = JsonUtils.getString(params, "id");
                String       studentId   = JsonUtils.getString(params, "studentId");
                String       subjectId   = JsonUtils.getString(params, "subjectId");
                String       trimesterId = JsonUtils.getString(params, "trimesterId");
                CourseStatus status      = CourseStatus.valueOf(JsonUtils.getString(params, "status"));

                // Get optional fields
                String sectionId = JsonUtils.getStringOptional(params, "sectionId").orElse(null);
                Double grade     = params.has("grade") ? JsonUtils.getDouble(params, "grade") : null;

                // Load the existing course first
                return DB.findById(Course.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(course -> {
                             // Load all required entities
                             Single<Student> studentSingle = DB.findById(Student.class, UUID.fromString(studentId))
                                                               .toSingle();
                             Single<Subject> subjectSingle = DB.findById(Subject.class, UUID.fromString(subjectId))
                                                               .toSingle();
                             Single<Trimester> trimesterSingle = DB.findById(Trimester.class, UUID.fromString(trimesterId))
                                                                   .toSingle();
                             Single<Section> sectionSingle = sectionId != null
                                                             ? DB.findById(Section.class, UUID.fromString(sectionId))
                                                                 .toSingle()
                                                             : Single.just(null);

                             return Single.zip(studentSingle, subjectSingle, trimesterSingle, sectionSingle, (student, subject, trimester, section) -> {
                                 // Update all fields
                                 course.setStudent(student);
                                 course.setSubject(subject);
                                 course.setTrimester(trimester);
                                 course.setStatus(status);

                                 // Handle section based on status
                                 if (status != CourseStatus.SELECTED) {
                                     if (section == null) {
                                         throw new IllegalArgumentException("Section is required for status: " + status);
                                     }
                                     course.setSection(section);
                                 } else {
                                     course.setSection(null);
                                 }

                                 // Handle grade based on status
                                 if (status == CourseStatus.COMPLETED) {
                                     if (grade == null) {
                                         throw new IllegalArgumentException("Grade is required for COMPLETED status");
                                     }
                                     if (grade < 0.0 || grade > 4.0) {
                                         throw new IllegalArgumentException("Grade must be between 0.0 and 4.0");
                                     }
                                     course.setGrade(grade);
                                 } else {
                                     course.setGrade(null);
                                 }

                                 return course;
                             });
                         })
                         .flatMap(DB::update)
                         .map(updated -> {
                             ObjectNode response = JsonUtils.createObject()
                                                            .put("id", updated.getId().toString())
                                                            .put("studentId", updated.getStudent().getId().toString())
                                                            .put("studentName", updated.getStudent().getName())
                                                            .put("subjectId", updated.getSubject().getId().toString())
                                                            .put("subjectName", updated.getSubject().getName())
                                                            .put("trimesterId", updated.getTrimester()
                                                                                       .getId()
                                                                                       .toString())
                                                            .put("trimesterCode", updated.getTrimester().getCode())
                                                            .put("status", updated.getStatus().toString());

                             if (updated.getSection() != null) {
                                 response.put("sectionId", updated.getSection().getId().toString())
                                         .put("sectionName", updated.getSection().getName());
                             }
                             if (updated.getGrade() != null) {
                                 response.put("grade", updated.getGrade());
                             }

                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update course: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid course data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.updateStatus", (params, _) -> Single.defer(() -> {
            try {
                String       id        = JsonUtils.getString(params, "id");
                CourseStatus newStatus = CourseStatus.valueOf(JsonUtils.getString(params, "status"));
                String       sectionId = JsonUtils.getStringOptional(params, "sectionId").orElse(null);
                Double       grade     = params.has("grade") ? JsonUtils.getDouble(params, "grade") : null;

                return DB.findById(Course.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(course -> {
                             if (sectionId != null) {
                                 return DB.findById(Section.class, UUID.fromString(sectionId))
                                          .toSingle()
                                          .map(section -> {
                                              course.setSection(section);
                                              return course;
                                          });
                             }
                             return Single.just(course);
                         })
                         .flatMap(course -> {
                             course.setStatus(newStatus);
                             if (grade != null) {
                                 course.setGrade(grade);
                             }
                             return DB.update(course);
                         })
                         .map(updated -> {
                             ObjectNode courseObj = JsonUtils.createObject()
                                                             .put("id", updated.getId().toString())
                                                             .put("status", updated.getStatus().toString());

                             if (updated.getSection() != null) {
                                 courseObj.put("sectionId", updated.getSection().getId().toString());
                                 courseObj.put("sectionName", updated.getSection().getName());
                             }
                             if (updated.getGrade() != null) {
                                 courseObj.put("grade", updated.getGrade());
                             }

                             return courseObj;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update course status: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Course.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete course: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid course ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.count", (_, _) -> Single.defer(() -> DB.count(Course.class)
                                                                             .map(count -> JsonUtils.createObject()
                                                                                                    .put("count", count))
                                                                             .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count courses: " + error.getMessage())))));

        server.registerMethod("Course.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Course.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check course existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid course ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.getByStudent", (params, _) -> Single.defer(() -> {
            try {
                String studentId = JsonUtils.getString(params, "studentId");
                int    limit     = JsonUtils.getInt(params, "limit");
                int    offset    = JsonUtils.getInt(params, "offset");

                return DB.read(Course.class, limit, offset)
                         .filter(course -> course.getStudent().getId().equals(UUID.fromString(studentId)))
                         .map(course -> {
                             ObjectNode courseObj = JsonUtils.createObject()
                                                             .put("id", course.getId().toString())
                                                             .put("subjectId", course.getSubject().getId().toString())
                                                             .put("subjectName", course.getSubject().getName())
                                                             .put("trimesterId", course.getTrimester()
                                                                                       .getId()
                                                                                       .toString())
                                                             .put("status", course.getStatus().toString());

                             if (course.getSection() != null) {
                                 courseObj.put("sectionId", course.getSection().getId().toString());
                                 courseObj.put("sectionName", course.getSection().getName());
                             }
                             if (course.getGrade() != null) {
                                 courseObj.put("grade", course.getGrade());
                             }

                             return courseObj;
                         })
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to get courses for student: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid parameters: " + e.getMessage()));
            }
        }));

        // Section methods
        server.registerMethod("Section.create", (params, _) -> Single.defer(() -> {
            try {
                String    name             = JsonUtils.getString(params, "name");
                String    sectionCode      = JsonUtils.getString(params, "section");
                String    subjectId        = JsonUtils.getString(params, "subjectId");
                String    trimesterId      = JsonUtils.getString(params, "trimesterId");
                int       maxCapacity      = JsonUtils.getInt(params, "maxCapacity");
                ArrayNode spaceTimeSlotIds = JsonUtils.getArray(params, "spaceTimeSlotIds");
                ArrayNode teacherIds       = JsonUtils.getArray(params, "teacherIds");

                return Single.zip(DB.findById(Subject.class, UUID.fromString(subjectId))
                                    .toSingle(), DB.findById(Trimester.class, UUID.fromString(trimesterId))
                                                   .toSingle(), (subject, trimester) -> {
                                 Section section = new Section();
                                 section.setName(name);
                                 section.setSection(sectionCode);
                                 section.setSubject(subject);
                                 section.setTrimester(trimester);
                                 section.setMaxCapacity(maxCapacity);
                                 return section;
                             })
                             .flatMap(section -> {
                                 // Load space-time slots
                                 Set<SpaceTime> slots = new HashSet<>();
                                 for (JsonNode slotId : spaceTimeSlotIds) {
                                     SpaceTime slot = DB.findById(SpaceTime.class, UUID.fromString(slotId.asText()))
                                                        .toSingle()
                                                        .blockingGet();
                                     slots.add(slot);
                                 }
                                 section.setSpaceTimeSlots(slots);

                                 // Load teachers
                                 Set<Faculty> teachers = new HashSet<>();
                                 for (JsonNode teacherId : teacherIds) {
                                     Faculty teacher = DB.findById(Faculty.class, UUID.fromString(teacherId.asText()))
                                                         .toSingle()
                                                         .blockingGet();
                                     teachers.add(teacher);
                                 }
                                 section.setTeachers(teachers);

                                 return DB.save(section);
                             })
                             .map(saved -> {
                                 ObjectNode sectionObj = JsonUtils.createObject()
                                                                  .put("id", saved.getId().toString())
                                                                  .put("name", saved.getName())
                                                                  .put("section", saved.getSection())
                                                                  .put("subjectId", saved.getSubject()
                                                                                         .getId()
                                                                                         .toString())
                                                                  .put("subjectName", saved.getSubject().getName())
                                                                  .put("trimesterId", saved.getTrimester()
                                                                                           .getId()
                                                                                           .toString())
                                                                  .put("maxCapacity", saved.getMaxCapacity())
                                                                  .put("currentCapacity", saved.getCurrentCapacity());

                                 // Add space-time slots
                                 ArrayNode slots = JsonUtils.createArray();
                                 saved.getSpaceTimeSlots().forEach(slot -> {
                                     slots.add(JsonUtils.createObject()
                                                        .put("id", slot.getId().toString())
                                                        .put("name", slot.getName())
                                                        .put("roomNumber", slot.getRoomNumber())
                                                        .put("dayOfWeek", slot.getDayOfWeek().toString())
                                                        .put("timeSlot", slot.getTimeSlot()));
                                 });
                                 sectionObj.set("spaceTimeSlots", slots);

                                 // Add teachers
                                 ArrayNode teachers = JsonUtils.createArray();
                                 saved.getTeachers().forEach(teacher -> {
                                     teachers.add(JsonUtils.createObject()
                                                           .put("id", teacher.getId().toString())
                                                           .put("name", teacher.getName())
                                                           .put("shortcode", teacher.getShortcode()));
                                 });
                                 sectionObj.set("teachers", teachers);

                                 return sectionObj;
                             })
                             .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create section: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid section data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Section.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(section -> {
                             ObjectNode sectionObj = JsonUtils.createObject()
                                                              .put("id", section.getId().toString())
                                                              .put("name", section.getName())
                                                              .put("section", section.getSection())
                                                              .put("subjectId", section.getSubject().getId().toString())
                                                              .put("subjectName", section.getSubject().getName())
                                                              .put("trimesterId", section.getTrimester()
                                                                                         .getId()
                                                                                         .toString())
                                                              .put("maxCapacity", section.getMaxCapacity())
                                                              .put("currentCapacity", section.getCurrentCapacity());

                             // Add space-time slots
                             ArrayNode slots = JsonUtils.createArray();
                             section.getSpaceTimeSlots().forEach(slot -> {
                                 slots.add(JsonUtils.createObject()
                                                    .put("id", slot.getId().toString())
                                                    .put("name", slot.getName())
                                                    .put("roomNumber", slot.getRoomNumber())
                                                    .put("dayOfWeek", slot.getDayOfWeek().toString())
                                                    .put("timeSlot", slot.getTimeSlot()));
                             });
                             sectionObj.set("spaceTimeSlots", slots);

                             // Add teachers
                             ArrayNode teachers = JsonUtils.createArray();
                             section.getTeachers().forEach(teacher -> {
                                 teachers.add(JsonUtils.createObject()
                                                       .put("id", teacher.getId().toString())
                                                       .put("name", teacher.getName())
                                                       .put("shortcode", teacher.getShortcode()));
                             });
                             sectionObj.set("teachers", teachers);

                             return sectionObj;
                         })
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch section list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Section.class, UUID.fromString(id))
                         .map(section -> {
                             ObjectNode sectionObj = JsonUtils.createObject()
                                                              .put("id", section.getId().toString())
                                                              .put("name", section.getName())
                                                              .put("section", section.getSection())
                                                              .put("subjectId", section.getSubject().getId().toString())
                                                              .put("subjectName", section.getSubject().getName())
                                                              .put("trimesterId", section.getTrimester()
                                                                                         .getId()
                                                                                         .toString())
                                                              .put("maxCapacity", section.getMaxCapacity())
                                                              .put("currentCapacity", section.getCurrentCapacity());

                             // Add space-time slots
                             ArrayNode slots = JsonUtils.createArray();
                             section.getSpaceTimeSlots().forEach(slot -> {
                                 slots.add(JsonUtils.createObject()
                                                    .put("id", slot.getId().toString())
                                                    .put("name", slot.getName())
                                                    .put("roomNumber", slot.getRoomNumber())
                                                    .put("dayOfWeek", slot.getDayOfWeek().toString())
                                                    .put("timeSlot", slot.getTimeSlot()));
                             });
                             sectionObj.set("spaceTimeSlots", slots);

                             // Add teachers
                             ArrayNode teachers = JsonUtils.createArray();
                             section.getTeachers().forEach(teacher -> {
                                 teachers.add(JsonUtils.createObject()
                                                       .put("id", teacher.getId().toString())
                                                       .put("name", teacher.getName())
                                                       .put("shortcode", teacher.getShortcode()));
                             });
                             sectionObj.set("teachers", teachers);

                             // Add registrations
                             ArrayNode registrations = JsonUtils.createArray();
                             section.getRegistrations().forEach(course -> {
                                 registrations.add(JsonUtils.createObject()
                                                            .put("id", course.getId().toString())
                                                            .put("studentId", course.getStudent().getId().toString())
                                                            .put("studentName", course.getStudent().getName())
                                                            .put("status", course.getStatus().toString()));
                             });
                             sectionObj.set("registrations", registrations);

                             return sectionObj;
                         })
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Section not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find section: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid section ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.update", (params, _) -> Single.defer(() -> {
            try {
                String    id          = JsonUtils.getString(params, "id");
                String    name        = JsonUtils.getString(params, "name");
                String    sectionCode = JsonUtils.getString(params, "section");
                int       maxCapacity = JsonUtils.getInt(params, "maxCapacity");
                ArrayNode teacherIds  = JsonUtils.getArray(params, "teacherIds");

                return DB.findById(Section.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(section -> {
                             section.setName(name);
                             section.setSection(sectionCode);
                             section.setMaxCapacity(maxCapacity);

                             // Update teachers
                             Set<Faculty> teachers = new HashSet<>();
                             for (JsonNode teacherId : teacherIds) {
                                 Faculty teacher = DB.findById(Faculty.class, UUID.fromString(teacherId.asText()))
                                                     .toSingle()
                                                     .blockingGet();
                                 teachers.add(teacher);
                             }
                             section.setTeachers(teachers);

                             return DB.update(section);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("name", updated.getName())
                                                  .put("section", updated.getSection())
                                                  .put("maxCapacity", updated.getMaxCapacity())
                                                  .put("currentCapacity", updated.getCurrentCapacity()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update section: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Section.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete section: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid section ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.count", (_, _) -> Single.defer(() -> DB.count(Section.class)
                                                                              .map(count -> JsonUtils.createObject()
                                                                                                     .put("count", count))
                                                                              .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count sections: " + error.getMessage())))));

        server.registerMethod("Section.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Section.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check section existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid section ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.searchByName", (params, _) -> Single.defer(() -> {
            try {
                String name   = JsonUtils.getString(params, "name");
                int    limit  = JsonUtils.getInt(params, "limit");
                int    offset = JsonUtils.getInt(params, "offset");

                return DB.read(Section.class, limit, offset)
                         .filter(section -> section.getName().toLowerCase().contains(name.toLowerCase()))
                         .map(section -> JsonUtils.createObject()
                                                  .put("id", section.getId().toString())
                                                  .put("name", section.getName())
                                                  .put("section", section.getSection())
                                                  .put("subjectName", section.getSubject().getName())
                                                  .put("maxCapacity", section.getMaxCapacity())
                                                  .put("currentCapacity", section.getCurrentCapacity()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search sections: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Section.list", (params, _) -> Single.defer(() -> {
            String subjectId   = JsonUtils.getString(params, "subjectId");
            String trimesterId = JsonUtils.getString(params, "trimesterId");

            return DB.read(Section.class, 1000, 0)
                     .filter(section -> section.getSubject()
                                               .getId()
                                               .equals(UUID.fromString(subjectId)) && section.getTrimester()
                                                                                             .getId()
                                                                                             .equals(UUID.fromString(trimesterId)))
                     .map(section -> JsonUtils.createObject()
                                              .put("id", section.getId().toString())
                                              .put("name", section.getName()))
                     .collect(ArrayList::new, (list, item) -> list.add(item))
                     .map(list -> {
                         ObjectNode response = JsonUtils.createObject();
                         ArrayNode  items    = JsonUtils.createArray();
                         list.forEach(item -> items.add((JsonNode) item));
                         response.set("items", items);
                         return response;
                     });
        }));

        server.registerMethod("Section.getByTeacher", (params, _) -> Single.defer(() -> {
            try {
                String teacherId = JsonUtils.getString(params, "teacherId");

                return DB.read(Section.class, Integer.MAX_VALUE, 0)
                         .filter(section -> section.getTeachers()
                                                   .stream()
                                                   .anyMatch(teacher -> teacher.getId().toString().equals(teacherId)))
                         .map(section -> {
                             ObjectNode sectionObj = JsonUtils.createObject()
                                                              .put("id", section.getId().toString())
                                                              .put("name", section.getName())
                                                              .put("section", section.getSection())
                                                              .put("subjectName", section.getSubject().getName())
                                                              .put("trimesterCode", section.getTrimester().getCode())
                                                              .put("maxCapacity", section.getMaxCapacity())
                                                              .put("currentCapacity", section.getCurrentCapacity());
                             ArrayNode spaceTimesArray = JsonUtils.createArray();
                             for (SpaceTime spaceTime : section.getSpaceTimeSlots()) {
                                 spaceTimesArray.add(JsonUtils.createObject()
                                                              .put("roomNumber", spaceTime.getRoomNumber())
                                                              .put("timeSlot", spaceTime.getTimeSlot())
                                                              .put("dayOfWeek", spaceTime.getDayOfWeek().toString()));
                             }
                             sectionObj.set("spaceTimes", spaceTimesArray);

                             return sectionObj;
                         })
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch teacher's sections: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid parameters: " + e.getMessage()));
            }
        }));

        // SpaceTime methods
        server.registerMethod("SpaceTime.create", (params, _) -> Single.defer(() -> {
            try {
                String      name       = JsonUtils.getString(params, "name");
                String      roomNumber = JsonUtils.getString(params, "roomNumber");
                SubjectType roomType   = SubjectType.valueOf(JsonUtils.getString(params, "roomType"));
                DayOfWeek   dayOfWeek  = DayOfWeek.valueOf(JsonUtils.getString(params, "dayOfWeek"));
                int         timeSlot   = JsonUtils.getInt(params, "timeSlot");

                SpaceTime spaceTime = new SpaceTime();
                spaceTime.setName(name);
                spaceTime.setRoomNumber(roomNumber);
                spaceTime.setRoomType(roomType);
                spaceTime.setDayOfWeek(dayOfWeek);
                spaceTime.setTimeSlot(timeSlot);

                return DB.save(spaceTime)
                         .map(saved -> JsonUtils.createObject()
                                                .put("id", saved.getId().toString())
                                                .put("name", saved.getName())
                                                .put("roomNumber", saved.getRoomNumber())
                                                .put("roomType", saved.getRoomType().toString())
                                                .put("dayOfWeek", saved.getDayOfWeek().toString())
                                                .put("timeSlot", saved.getTimeSlot()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create space-time slot: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid space-time data: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(SpaceTime.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(spaceTime -> JsonUtils.createObject()
                                                    .put("id", spaceTime.getId().toString())
                                                    .put("name", spaceTime.getName())
                                                    .put("roomNumber", spaceTime.getRoomNumber())
                                                    .put("roomType", spaceTime.getRoomType().toString())
                                                    .put("dayOfWeek", spaceTime.getDayOfWeek().toString())
                                                    .put("timeSlot", spaceTime.getTimeSlot()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch space-time list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(SpaceTime.class, UUID.fromString(id))
                         .map(spaceTime -> JsonUtils.createObject()
                                                    .put("id", spaceTime.getId().toString())
                                                    .put("name", spaceTime.getName())
                                                    .put("roomNumber", spaceTime.getRoomNumber())
                                                    .put("roomType", spaceTime.getRoomType().toString())
                                                    .put("dayOfWeek", spaceTime.getDayOfWeek().toString())
                                                    .put("timeSlot", spaceTime.getTimeSlot()))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Space-time slot not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find space-time slot: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid space-time ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.update", (params, _) -> Single.defer(() -> {
            try {
                String id         = JsonUtils.getString(params, "id");
                String name       = JsonUtils.getString(params, "name");
                String roomNumber = JsonUtils.getString(params, "roomNumber");
                int    timeSlot   = JsonUtils.getInt(params, "timeSlot");

                return DB.findById(SpaceTime.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(spaceTime -> {
                             spaceTime.setName(name);
                             spaceTime.setRoomNumber(roomNumber);
                             spaceTime.setTimeSlot(timeSlot);
                             return DB.update(spaceTime);
                         })
                         .map(updated -> JsonUtils.createObject()
                                                  .put("id", updated.getId().toString())
                                                  .put("name", updated.getName())
                                                  .put("roomNumber", updated.getRoomNumber())
                                                  .put("roomType", updated.getRoomType().toString())
                                                  .put("dayOfWeek", updated.getDayOfWeek().toString())
                                                  .put("timeSlot", updated.getTimeSlot()))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update space-time slot: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(SpaceTime.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete space-time slot: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid space-time ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.count", (_, _) -> Single.defer(() -> DB.count(SpaceTime.class)
                                                                                .map(count -> JsonUtils.createObject()
                                                                                                       .put("count", count))
                                                                                .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count space-time slots: " + error.getMessage())))));

        server.registerMethod("SpaceTime.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(SpaceTime.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check space-time existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid space-time ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.searchByRoomNumber", (params, _) -> Single.defer(() -> {
            try {
                String roomNumber = JsonUtils.getString(params, "roomNumber");
                int    limit      = JsonUtils.getInt(params, "limit");
                int    offset     = JsonUtils.getInt(params, "offset");

                return DB.read(SpaceTime.class, limit, offset)
                         .filter(spaceTime -> spaceTime.getRoomNumber()
                                                       .toLowerCase()
                                                       .contains(roomNumber.toLowerCase()))
                         .map(spaceTime -> JsonUtils.createObject()
                                                    .put("id", spaceTime.getId().toString())
                                                    .put("name", spaceTime.getName())
                                                    .put("roomNumber", spaceTime.getRoomNumber())
                                                    .put("roomType", spaceTime.getRoomType().toString())
                                                    .put("dayOfWeek", spaceTime.getDayOfWeek().toString())
                                                    .put("timeSlot", spaceTime.getTimeSlot()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to search space-time slots: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("SpaceTime.findAvailable", (params, _) -> Single.defer(() -> {
            try {
                SubjectType roomType  = SubjectType.valueOf(JsonUtils.getString(params, "roomType"));
                DayOfWeek   dayOfWeek = DayOfWeek.valueOf(JsonUtils.getString(params, "dayOfWeek"));
                int         limit     = JsonUtils.getInt(params, "limit");
                int         offset    = JsonUtils.getInt(params, "offset");

                return DB.read(SpaceTime.class, limit, offset)
                         .filter(spaceTime -> spaceTime.getRoomType() == roomType && spaceTime.getDayOfWeek() == dayOfWeek)
                         .map(spaceTime -> JsonUtils.createObject()
                                                    .put("id", spaceTime.getId().toString())
                                                    .put("name", spaceTime.getName())
                                                    .put("roomNumber", spaceTime.getRoomNumber())
                                                    .put("timeSlot", spaceTime.getTimeSlot()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find available space-time slots: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        // Trimester methods
        server.registerMethod("Trimester.create", (params, _) -> Single.defer(() -> {
            try {
                Integer         code   = JsonUtils.getInt(params, "code");
                Integer         year   = JsonUtils.getInt(params, "year");
                Season          season = Season.valueOf(JsonUtils.getString(params, "season"));
                TrimesterStatus status = TrimesterStatus.valueOf(JsonUtils.getString(params, "status"));

                // Parse dates if provided
                LocalDateTime courseSelectionStart = JsonUtils.getStringOptional(params, "courseSelectionStart")
                                                              .map(str -> LocalDateTime.parse(str))
                                                              .orElse(null);
                LocalDateTime courseSelectionEnd = JsonUtils.getStringOptional(params, "courseSelectionEnd")
                                                            .map(str -> LocalDateTime.parse(str))
                                                            .orElse(null);
                LocalDateTime sectionRegistrationStart = JsonUtils.getStringOptional(params, "sectionRegistrationStart")
                                                                  .map(str -> LocalDateTime.parse(str))
                                                                  .orElse(null);
                LocalDateTime sectionRegistrationEnd = JsonUtils.getStringOptional(params, "sectionRegistrationEnd")
                                                                .map(str -> LocalDateTime.parse(str))
                                                                .orElse(null);

                Trimester trimester = new Trimester();
                trimester.setCode(code);
                trimester.setYear(year);
                trimester.setSeason(season);
                trimester.setStatus(status);
                trimester.setCourseSelectionStart(courseSelectionStart);
                trimester.setCourseSelectionEnd(courseSelectionEnd);
                trimester.setSectionRegistrationStart(sectionRegistrationStart);
                trimester.setSectionRegistrationEnd(sectionRegistrationEnd);

                return DB.save(trimester)
                         .map(saved -> buildTrimesterJson(saved))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create trimester: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid trimester data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Trimester.class, limit, offset)
                         .timeout(20, TimeUnit.SECONDS)
                         .map(trimester -> buildTrimesterJson(trimester))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch trimester list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Trimester.class, UUID.fromString(id))
                         .map(trimester -> buildTrimesterJson(trimester))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Trimester not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find trimester: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid trimester ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.updateStatus", (params, _) -> Single.defer(() -> {
            try {
                String          id        = JsonUtils.getString(params, "id");
                TrimesterStatus newStatus = TrimesterStatus.valueOf(JsonUtils.getString(params, "status"));

                return DB.findById(Trimester.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(trimester -> {
                             trimester.setStatus(newStatus);
                             return DB.update(trimester);
                         })
                         .map(updated -> buildTrimesterJson(updated))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update trimester status: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.updateDates", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");

                // Parse dates
                LocalDateTime courseSelectionStart     = LocalDateTime.parse(JsonUtils.getString(params, "courseSelectionStart"));
                LocalDateTime courseSelectionEnd       = LocalDateTime.parse(JsonUtils.getString(params, "courseSelectionEnd"));
                LocalDateTime sectionRegistrationStart = LocalDateTime.parse(JsonUtils.getString(params, "sectionRegistrationStart"));
                LocalDateTime sectionRegistrationEnd   = LocalDateTime.parse(JsonUtils.getString(params, "sectionRegistrationEnd"));

                return DB.findById(Trimester.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(trimester -> {
                             trimester.setCourseSelectionStart(courseSelectionStart);
                             trimester.setCourseSelectionEnd(courseSelectionEnd);
                             trimester.setSectionRegistrationStart(sectionRegistrationStart);
                             trimester.setSectionRegistrationEnd(sectionRegistrationEnd);
                             return DB.update(trimester);
                         })
                         .map(updated -> buildTrimesterJson(updated))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update trimester dates: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid date data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Trimester.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete trimester: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid trimester ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.count", (_, _) -> Single.defer(() -> DB.count(Trimester.class)
                                                                                .map(count -> JsonUtils.createObject()
                                                                                                       .put("count", count))
                                                                                .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count trimesters: " + error.getMessage())))));

        server.registerMethod("Trimester.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Trimester.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check trimester existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid trimester ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.getCurrent", (_, _) -> Single.defer(() -> DB.read(Trimester.class, 1, 0)
                                                                                     .filter(trimester -> trimester.getStatus() == TrimesterStatus.ONGOING)
                                                                                     .firstElement()
                                                                                     .map(trimester -> buildTrimesterJson(trimester))
                                                                                     .switchIfEmpty(Single.just(JsonUtils.createObject()
                                                                                                                         .put("error", "No ongoing trimester found")))
                                                                                     .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to get current trimester: " + error.getMessage())))));

        server.registerMethod("Trimester.getUpcoming", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Trimester.class, limit, offset)
                         .filter(trimester -> trimester.getStatus() == TrimesterStatus.UPCOMING)
                         .map(Main::buildTrimesterJson)
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to get upcoming trimesters: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trimester.list", (_, _) -> Single.defer(() -> DB.read(Trimester.class, 1000, 0)
                                                                               .map(trimester -> JsonUtils.createObject()
                                                                                                          .put("id", trimester.getId()
                                                                                                                              .toString())
                                                                                                          .put("code", trimester.getCode()))
                                                                               .collect(ArrayList::new, (list, item) -> list.add(item))
                                                                               .map(list -> {
                                                                                   ObjectNode response = JsonUtils.createObject();
                                                                                   ArrayNode  items    = JsonUtils.createArray();
                                                                                   list.forEach(item -> items.add((JsonNode) item));
                                                                                   response.set("items", items);
                                                                                   return response;
                                                                               })));

        // Notification methods
        server.registerMethod("Notification.create", (params, _) -> Single.defer(() -> {
            try {
                String               senderId = JsonUtils.getString(params, "senderId");
                String               title    = JsonUtils.getString(params, "title");
                String               content  = JsonUtils.getString(params, "content");
                NotificationCategory category = NotificationCategory.valueOf(JsonUtils.getString(params, "category"));
                NotificationScope    scope    = NotificationScope.valueOf(JsonUtils.getString(params, "scope"));

                // Optional parameters based on scope
                String trimesterId  = JsonUtils.getStringOptional(params, "trimesterId").orElse(null);
                String sectionId    = JsonUtils.getStringOptional(params, "sectionId").orElse(null);
                String targetUserId = JsonUtils.getStringOptional(params, "targetUserId").orElse(null);

                return DB.findById(Faculty.class, UUID.fromString(senderId))
                         .toSingle()
                         .flatMap(sender -> {
                             Notification notification = new Notification();
                             notification.setSender(sender);
                             notification.setTitle(title);
                             notification.setContent(content);
                             notification.setCategory(category);
                             notification.setScope(scope);

                             // Handle scope-specific relationships
                             Single<Notification> scopeSetup = Single.just(notification);
                             if (trimesterId != null) {
                                 scopeSetup = DB.findById(Trimester.class, UUID.fromString(trimesterId))
                                                .toSingle()
                                                .map(trimester -> {
                                                    notification.setTrimester(trimester);
                                                    return notification;
                                                });
                             }
                             if (sectionId != null) {
                                 scopeSetup = DB.findById(Section.class, UUID.fromString(sectionId))
                                                .toSingle()
                                                .map(section -> {
                                                    notification.setSection(section);
                                                    return notification;
                                                });
                             }
                             if (targetUserId != null) {
                                 scopeSetup = DB.findById(User.class, UUID.fromString(targetUserId))
                                                .toSingle()
                                                .map(user -> {
                                                    notification.setTargetUser(user);
                                                    return notification;
                                                });
                             }

                             return scopeSetup.flatMap(n -> DB.save(n));
                         })
                         .map(saved -> buildNotificationJson(saved))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to create notification: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid notification data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.getAll", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Notification.class, "createdAt", false, limit, offset)  // Newest first
                         .timeout(20, TimeUnit.SECONDS)
                         .map(notification -> buildNotificationJson(notification))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch notification list: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.getById", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.findById(Notification.class, UUID.fromString(id))
                         .map(notification -> buildNotificationJson(notification))
                         .switchIfEmpty(Single.just(JsonUtils.createObject().put("error", "Notification not found")))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to find notification: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid notification ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.update", (params, _) -> Single.defer(() -> {
            try {
                String               id       = JsonUtils.getString(params, "id");
                String               title    = JsonUtils.getString(params, "title");
                String               content  = JsonUtils.getString(params, "content");
                NotificationCategory category = NotificationCategory.valueOf(JsonUtils.getString(params, "category"));

                return DB.findById(Notification.class, UUID.fromString(id))
                         .toSingle()
                         .flatMap(notification -> {
                             notification.setTitle(title);
                             notification.setContent(content);
                             notification.setCategory(category);
                             return DB.update(notification);
                         })
                         .map(updated -> buildNotificationJson(updated))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to update notification: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid update data: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.delete", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.delete(Notification.class, UUID.fromString(id))
                         .toSingle(() -> JsonUtils.createObject().put("success", true))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to delete notification: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid notification ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.count", (_, _) -> Single.defer(() -> DB.count(Notification.class)
                                                                                   .map(count -> JsonUtils.createObject()
                                                                                                          .put("count", count))
                                                                                   .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to count notifications: " + error.getMessage())))));

        server.registerMethod("Notification.exists", (params, _) -> Single.defer(() -> {
            try {
                String id = JsonUtils.getString(params, "id");
                return DB.exists(Notification.class, UUID.fromString(id))
                         .toSingle()
                         .map(exists -> JsonUtils.createObject().put("exists", exists))
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to check notification existence: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid notification ID: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.getByScope", (params, _) -> Single.defer(() -> {
            try {
                NotificationScope scope    = NotificationScope.valueOf(JsonUtils.getString(params, "scope"));
                String            targetId = JsonUtils.getString(params, "targetId");  // trimesterId, sectionId, or userId based on scope
                int               limit    = JsonUtils.getInt(params, "limit");
                int               offset   = JsonUtils.getInt(params, "offset");

                return DB.read(Notification.class, "createdAt", false, limit, offset)  // Newest first
                         .filter(notification -> {
                             if (notification.getScope() != scope) return false;
                             switch (scope) {
                                 case TRIMESTER:
                                     return notification.getTrimester() != null && notification.getTrimester()
                                                                                               .getId()
                                                                                               .toString()
                                                                                               .equals(targetId);
                                 case SECTION:
                                     return notification.getSection() != null && notification.getSection()
                                                                                             .getId()
                                                                                             .toString()
                                                                                             .equals(targetId);
                                 case USER:
                                     return notification.getTargetUser() != null && notification.getTargetUser()
                                                                                                .getId()
                                                                                                .toString()
                                                                                                .equals(targetId);
                                 default:
                                     return false;
                             }
                         })
                         .map(notification -> buildNotificationJson(notification))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch notifications by scope: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid search parameters: " + e.getMessage()));
            }
        }));

        server.registerMethod("Notification.getGlobal", (params, _) -> Single.defer(() -> {
            try {
                int limit  = JsonUtils.getInt(params, "limit");
                int offset = JsonUtils.getInt(params, "offset");

                return DB.read(Notification.class, "createdAt", false, limit, offset)  // Newest first
                         .filter(notification -> notification.getScope() == NotificationScope.GLOBAL)
                         .map(Main::buildNotificationJson)
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         })
                         .onErrorResumeNext(error -> Single.error(new RuntimeException("Failed to fetch global notifications: " + error.getMessage())));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Invalid pagination parameters: " + e.getMessage()));
            }
        }));

        // ---
        // ---
        // ---
        // ---
        // ---
        // ---

        server.registerMethod("Course.getSchedule", (params, _) -> Single.defer(() -> {
            try {
                String userId = JsonUtils.getString(params, "userId");

                return Single.fromCallable(() -> {
                    try (var session = DB.getSessionFactory().openSession()) {
                        var transaction = session.beginTransaction();

                        try {
                            // 1. Get active trimester
                            var trimester = session.createQuery("FROM Trimester t WHERE t.status = :status", Trimester.class)
                                                   .setParameter("status", TrimesterStatus.SECTION_SELECTION)
                                                   .setMaxResults(1)
                                                   .uniqueResult();

                            if (trimester == null) {
                                return JsonUtils.createObject()
                                                .put("error", "No active section selection period found");
                            }

                            // 2. Get student's courses with subjects
                            var courses = session.createQuery("SELECT DISTINCT c FROM Course c " + "JOIN FETCH c.subject sub " + "LEFT JOIN FETCH c.section sec " + "WHERE c.student.id = :userId " + "AND c.trimester.id = :trimesterId " + "AND (c.status = :status1 OR c.status = :status2)", Course.class)
                                                 .setParameter("userId", UUID.fromString(userId))
                                                 .setParameter("trimesterId", trimester.getId())
                                                 .setParameter("status1", CourseStatus.SELECTED)
                                                 .setParameter("status2", CourseStatus.REGISTERED)
                                                 .getResultList();

                            if (courses.isEmpty()) {
                                return JsonUtils.createObject().put("error", "No courses found for section selection");
                            }

                            // Get unique subject IDs
                            Set<UUID> subjectIds = courses.stream()
                                                          .map(c -> c.getSubject().getId())
                                                          .collect(Collectors.toSet());

                            // 3. Get all sections for these subjects with space-times
                            var sections = session.createQuery("SELECT DISTINCT s FROM Section s " + "JOIN FETCH s.subject sub " + "JOIN FETCH s.spaceTimeSlots sts " + "LEFT JOIN FETCH s.registrations r " + "WHERE s.trimester.id = :trimesterId " + "AND s.subject.id IN (:subjectIds)", Section.class)
                                                  .setParameter("trimesterId", trimester.getId())
                                                  .setParameter("subjectIds", subjectIds)
                                                  .getResultList();

                            // Build response while session is still open
                            ObjectNode response = JsonUtils.createObject();
                            response.put("trimesterId", trimester.getId().toString())
                                    .put("trimesterCode", trimester.getCode());

                            ArrayNode subjectsArray = JsonUtils.createArray();

                            // Group sections by subject
                            Map<UUID, List<Section>> sectionsBySubject = sections.stream()
                                                                                 .collect(Collectors.groupingBy(s -> s.getSubject()
                                                                                                                      .getId()));

                            // Group student's courses by subject for registration status check
                            Map<UUID, Course> coursesBySubject = courses.stream()
                                                                        .collect(Collectors.toMap(c -> c.getSubject()
                                                                                                        .getId(), c -> c));

                            // Build subject groups
                            for (Course course : courses) {
                                Subject       subject         = course.getSubject();
                                List<Section> subjectSections = sectionsBySubject.getOrDefault(subject.getId(), new ArrayList<>());

                                ObjectNode subjectNode = JsonUtils.createObject()
                                                                  .put("subjectId", subject.getId().toString())
                                                                  .put("subjectName", subject.getName())
                                                                  .put("subjectCode", subject.getCodeName())
                                                                  .put("courseId", course.getId().toString())
                                                                  .put("subjectType", subject.getType().toString());

                                // Group sections by day
                                Map<String, List<Section>> sectionsByDay = groupSectionsByDay(subjectSections, subject.getType());
                                ArrayNode                  daysArray     = JsonUtils.createArray();

                                for (Map.Entry<String, List<Section>> entry : sectionsByDay.entrySet()) {
                                    ObjectNode dayNode       = JsonUtils.createObject().put("day", entry.getKey());
                                    ArrayNode  sectionsArray = JsonUtils.createArray();

                                    for (Section section : entry.getValue()) {
                                        boolean isRegistered = course.getStatus() == CourseStatus.REGISTERED && course.getSection() != null && course.getSection()
                                                                                                                                                     .getId()
                                                                                                                                                     .equals(section.getId());

                                        ObjectNode sectionNode = JsonUtils.createObject()
                                                                          .put("sectionId", section.getId().toString())
                                                                          .put("sectionCode", section.getSection())
                                                                          .put("currentCapacity", section.getCurrentCapacity())
                                                                          .put("maxCapacity", section.getMaxCapacity())
                                                                          .put("timeSlot", getFirstTimeSlot(section))
                                                                          .put("isRegistered", isRegistered);

                                        sectionsArray.add(sectionNode);
                                    }
                                    dayNode.set("sections", sectionsArray);
                                    daysArray.add(dayNode);
                                }

                                subjectNode.set("days", daysArray);
                                subjectsArray.add(subjectNode);
                            }

                            response.set("subjects", subjectsArray);

                            transaction.commit();
                            return response;
                        } catch (Exception e) {
                            transaction.rollback();
                            throw e;
                        }
                    }
                });
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to fetch schedule: " + e.getMessage()));
            }
        }));

        server.registerMethod("Course.updateRegistration", (params, _) -> Single.defer(() -> {
            try {
                String courseId  = JsonUtils.getString(params, "courseId");
                String sectionId = JsonUtils.getStringOptional(params, "sectionId").orElse(null);

                return Single.fromCallable(() -> {
                    try (var session = DB.getSessionFactory().openSession()) {
                        var transaction = session.beginTransaction();

                        try {
                            // Load course with existing section if any
                            var course = session.createQuery("SELECT c FROM Course c " + "LEFT JOIN FETCH c.section s " + "JOIN FETCH c.trimester t " + "WHERE c.id = :courseId", Course.class)
                                                .setParameter("courseId", UUID.fromString(courseId))
                                                .uniqueResult();

                            if (course == null) {
                                throw new IllegalArgumentException("Course not found");
                            }

                            // Verify trimester is in SECTION_SELECTION
                            if (course.getTrimester().getStatus() != TrimesterStatus.SECTION_SELECTION) {
                                throw new IllegalStateException("Section selection is not active");
                            }

                            if (sectionId != null) {
                                // Load new section with space-times
                                var section = session.createQuery("SELECT s FROM Section s " + "JOIN FETCH s.spaceTimeSlots " + "WHERE s.id = :sectionId", Section.class)
                                                     .setParameter("sectionId", UUID.fromString(sectionId))
                                                     .uniqueResult();

                                if (section == null) {
                                    throw new IllegalArgumentException("Section not found");
                                }

                                // Check capacity
                                if (section.getCurrentCapacity() >= section.getMaxCapacity()) {
                                    throw new IllegalStateException("Section is full");
                                }

                                course.setStatus(CourseStatus.REGISTERED);
                                course.setSection(section);
                            } else {
                                course.setStatus(CourseStatus.SELECTED);
                                course.setSection(null);
                            }

                            session.merge(course);
                            transaction.commit();

                            ObjectNode response = JsonUtils.createObject()
                                                           .put("success", true)
                                                           .put("courseId", course.getId().toString())
                                                           .put("status", course.getStatus().toString());

                            if (course.getSection() != null) {
                                response.put("sectionId", course.getSection().getId().toString());
                            }

                            return response;
                        } catch (Exception e) {
                            transaction.rollback();
                            throw e;
                        }
                    }
                });
            } catch (Exception e) {
                return Single.error(new RuntimeException("Registration update failed: " + e.getMessage()));
            }
        }));

        // ---
        // ---
        // ---
        // ---
        // ---
        // ---

        // Add this to Main.java's registerMethods
// Trade-related RPC methods
        server.registerMethod("Trade.offerTrade", (params, _) -> Single.defer(() -> {
            try {
                String studentId        = JsonUtils.getString(params, "studentId");
                String sectionId        = JsonUtils.getString(params, "sectionId");
                String note             = JsonUtils.getString(params, "note");
                String desiredSectionId = JsonUtils.getStringOptional(params, "desiredSectionId").orElse(null);

                // Get course info from section
                Section section = DB.findById(Section.class, UUID.fromString(sectionId)).blockingGet();

                Map<String, Object> trade = new HashMap<>();
                trade.put("id", UUID.randomUUID().toString());
                trade.put("offeredBy", studentId);
                trade.put("currentSectionId", sectionId);
                trade.put("desiredSectionId", desiredSectionId);
                trade.put("courseId", section.getSubject().getId().toString());
                trade.put("courseName", section.getSubject().getName());
                trade.put("currentSectionName", section.getName());
                trade.put("note", note);
                trade.put("status", "PENDING");
                trade.put("createdAt", System.currentTimeMillis());

                // Get trades list from Volatile or create new
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trades = (List<Map<String, Object>>) Optional.ofNullable(Volatile.getInstance()
                                                                                                           .get("trades"))
                                                                                       .orElse(new ArrayList<>());

                // Check if student already has a pending trade for this section
                boolean hasPendingTrade = trades.stream()
                                                .anyMatch(t -> t.get("offeredBy")
                                                                .equals(studentId) && t.get("currentSectionId")
                                                                                       .equals(sectionId) && t.get("status")
                                                                                                              .equals("PENDING"));

                if (hasPendingTrade) {
                    return Single.error(new IllegalStateException("Already have a pending trade for this section"));
                }

                trades.add(trade);
                Volatile.getInstance().put("trades", trades);

                return Single.just(JsonUtils.toJson(trade));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to offer trade: " + e.getMessage()));
            }
        }));

        // Update this part in the server-side Trade.listAvailableTrades method:
        server.registerMethod("Trade.listAvailableTrades", (params, _) -> Single.defer(() -> {
            try {
                String studentId = JsonUtils.getString(params, "studentId");

                // Get current trades
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trades = (List<Map<String, Object>>) Optional.ofNullable(Volatile.getInstance()
                                                                                                           .get("trades"))
                                                                                       .orElse(new ArrayList<>());

                // If no real trades, add some mock data
                if (trades.isEmpty()) {
                    // Add mock trades
                    Map<String, Object> mockTrade1 = new HashMap<>();
                    mockTrade1.put("id", UUID.randomUUID().toString());
                    mockTrade1.put("offeredBy", "mock-user-1");
                    mockTrade1.put("courseName", "Data Structures");
                    mockTrade1.put("currentSectionName", "Section A");
                    mockTrade1.put("note", "Looking for morning sections");
                    mockTrade1.put("status", "PENDING");

                    Map<String, Object> mockTrade2 = new HashMap<>();
                    mockTrade2.put("id", UUID.randomUUID().toString());
                    mockTrade2.put("offeredBy", "mock-user-2");
                    mockTrade2.put("courseName", "Algorithm Analysis");
                    mockTrade2.put("currentSectionName", "Section B");
                    mockTrade2.put("note", "Prefer afternoon slots");
                    mockTrade2.put("status", "PENDING");

                    Map<String, Object> mockTrade3 = new HashMap<>();
                    mockTrade3.put("id", UUID.randomUUID().toString());
                    mockTrade3.put("offeredBy", "mock-user-3");
                    mockTrade3.put("courseName", "Database Systems");
                    mockTrade3.put("currentSectionName", "Section C");
                    mockTrade3.put("note", "Need to switch due to timing conflict");
                    mockTrade3.put("status", "PENDING");

                    trades.add(mockTrade1);
                    trades.add(mockTrade2);
                    trades.add(mockTrade3);
                }

                // Filter out user's own trades
                List<Map<String, Object>> availableTrades = trades.stream()
                                                                  .filter(t -> !t.get("offeredBy").equals(studentId))
                                                                  .collect(Collectors.toList());

                ObjectNode response = JsonUtils.createObject();
                ArrayNode  items    = JsonUtils.createArray();
                availableTrades.forEach(trade -> items.add(JsonUtils.toJson(trade)));
                response.set("items", items);

                return Single.just(response);
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to list trades: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trade.listMyTrades", (params, _) -> Single.defer(() -> {
            try {
                String studentId = JsonUtils.getString(params, "studentId");

                // Get current trades
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trades = (List<Map<String, Object>>) Optional.ofNullable(Volatile.getInstance()
                                                                                                           .get("trades"))
                                                                                       .orElse(new ArrayList<>());

                // Filter trades offered by current student
                List<Map<String, Object>> myTrades = trades.stream()
                                                           .filter(t -> t.get("offeredBy").equals(studentId))
                                                           .collect(Collectors.toList());

                ObjectNode response = JsonUtils.createObject();
                ArrayNode  items    = JsonUtils.createArray();
                myTrades.forEach(trade -> items.add(JsonUtils.toJson(trade)));
                response.set("items", items);

                return Single.just(response);
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to list my trades: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trade.acceptTrade", (params, _) -> Single.defer(() -> {
            try {
                String tradeId       = JsonUtils.getString(params, "tradeId");
                String studentId     = JsonUtils.getString(params, "studentId");
                String swapSectionId = JsonUtils.getString(params, "swapSectionId");

                // Get current trades
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trades = (List<Map<String, Object>>) Optional.ofNullable(Volatile.getInstance()
                                                                                                           .get("trades"))
                                                                                       .orElse(new ArrayList<>());

                // Find and update the trade
                Optional<Map<String, Object>> tradeMaybe = trades.stream()
                                                                 .filter(t -> t.get("id").equals(tradeId))
                                                                 .findFirst();

                if (!tradeMaybe.isPresent()) {
                    return Single.error(new IllegalArgumentException("Trade not found"));
                }

                Map<String, Object> trade = tradeMaybe.get();
                if (!trade.get("status").equals("PENDING")) {
                    return Single.error(new IllegalStateException("Trade is no longer pending"));
                }

                // Update trade status
                trade.put("status", "ACCEPTED");
                trade.put("respondedBy", studentId);
                trade.put("swapSectionId", swapSectionId);
                trade.put("updatedAt", System.currentTimeMillis());

                // Update in volatile store
                Volatile.getInstance().put("trades", trades);

                return Single.just(JsonUtils.toJson(trade));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to accept trade: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trade.cancelTrade", (params, _) -> Single.defer(() -> {
            try {
                String tradeId   = JsonUtils.getString(params, "tradeId");
                String studentId = JsonUtils.getString(params, "studentId");

                // Get current trades
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> trades = (List<Map<String, Object>>) Optional.ofNullable(Volatile.getInstance()
                                                                                                           .get("trades"))
                                                                                       .orElse(new ArrayList<>());

                // Find and update the trade
                Optional<Map<String, Object>> tradeMaybe = trades.stream()
                                                                 .filter(t -> t.get("id").equals(tradeId))
                                                                 .findFirst();

                if (!tradeMaybe.isPresent()) {
                    return Single.error(new IllegalArgumentException("Trade not found"));
                }

                Map<String, Object> trade = tradeMaybe.get();
                if (!trade.get("offeredBy").equals(studentId)) {
                    return Single.error(new IllegalStateException("Not authorized to cancel this trade"));
                }

                // Remove trade
                trades.remove(trade);

                // Update in volatile store
                Volatile.getInstance().put("trades", trades);

                return Single.just(JsonUtils.createObject().put("success", true));
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to cancel trade: " + e.getMessage()));
            }
        }));

        server.registerMethod("Trade.getRegisteredSections", (params, _) -> Single.defer(() -> {
            try {
                String studentId = JsonUtils.getString(params, "studentId");

                return DB.read(Course.class, 1000, 0)
                         .filter(course -> course.getStudent()
                                                 .getId()
                                                 .toString()
                                                 .equals(studentId) && course.getStatus() == CourseStatus.REGISTERED)
                         .map(course -> JsonUtils.createObject()
                                                 .put("id", course.getSection().getId().toString())
                                                 .put("name", course.getSection().getName())
                                                 .put("courseName", course.getSubject().getName()))
                         .collect(ArrayList::new, (list, item) -> list.add(item))
                         .map(list -> {
                             ObjectNode response = JsonUtils.createObject();
                             ArrayNode  items    = JsonUtils.createArray();
                             list.forEach(item -> items.add((JsonNode) item));
                             response.set("items", items);
                             return response;
                         });
            } catch (Exception e) {
                return Single.error(new RuntimeException("Failed to get registered sections: " + e.getMessage()));
            }
        }));

        server.registerMethod("sendMessage", (params, request) -> {
            try {
                String recipientEmail = JsonUtils.getString(params, "recipient");
                String messageContent = JsonUtils.getString(params, "message");

                String senderToken = request.getSessionToken();

                // Find sender's email based on session token
                Optional<SessionInfo> senderSession = SessionManager.getInstance().getSession(senderToken);

                if (senderSession.isPresent()) {
                    String senderEmail = DB.read(User.class, 1, 0)
                                           .filter(user -> user.getId()
                                                               .toString()
                                                               .equals(senderSession.get().getUserId()))
                                           .firstElement()
                                           .map(User::getEmail)
                                           .blockingGet();

                    // Find recipient's session token by email
                    Optional<SessionInfo> recipientSession = SessionManager.getInstance()
                                                                           .getActiveSessions()
                                                                           .stream()
                                                                           .filter(session -> session.getUserId()
                                                                                                     .equals(recipientEmail))
                                                                           .findFirst();

                    if (recipientSession.isPresent()) {
                        String recipientToken = recipientSession.get().getSessionToken();

                        Request forwardRequest = Request.create(request.getId(), "receiveMessage", JsonUtils.createObject()
                                                                                                            .put("sender", senderEmail)  // Send sender's email instead of session token
                                                                                                            .put("message", messageContent), recipientToken);

                        return SessionManager.getInstance()
                                             .sendRequest(recipientToken, forwardRequest)
                                             .map(response -> JsonUtils.createObject().put("status", "delivered"));
                    } else {
                        return Single.error(new IllegalStateException("Recipient is not online."));
                    }
                } else {
                    return Single.error(new IllegalStateException("Sender not recognized."));
                }
            } catch (Exception e) {
                return Single.just(JsonUtils.createObject().put("error", "Invalid parameters: " + e.getMessage()));
            }
        });

        server.registerMethod("receiveMessage", (params, request) -> {
            return Single.just(JsonUtils.createObject().put("status", "received"));
        });

        server.registerMethod("getServerStats", (params, request) -> Single.defer(() -> {
            try {
                SystemInfo       systemInfo = new SystemInfo();
                CentralProcessor processor  = systemInfo.getHardware().getProcessor();
                GlobalMemory     memory     = systemInfo.getHardware().getMemory();
                List<NetworkIF>  networkIFs = systemInfo.getHardware().getNetworkIFs();

                // Measure CPU usage
                long[] prevTicks = processor.getSystemCpuLoadTicks();
                TimeUnit.SECONDS.sleep(1);
                double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

                // Measure RAM usage
                long totalMemory = memory.getTotal();
                long usedMemory  = totalMemory - memory.getAvailable();
                int  ramUsage    = (int) ((usedMemory * 100) / totalMemory);

                int diskUsage = (int) (Math.random() * 100);

                long totalSent     = 0;
                long totalReceived = 0;
                for (NetworkIF net : networkIFs) {
                    net.updateAttributes();
                    totalSent += net.getBytesSent();
                    totalReceived += net.getBytesRecv();
                }
                int networkUsage = (int) ((totalSent + totalReceived) / 1024); // Convert bytes to KB

                // Prepare response
                ObjectNode response = JsonUtils.createObject();
                response.put("cpu", (int) cpuUsage);
                response.put("ram", ramUsage);
                response.put("disk", diskUsage);
                response.put("network", networkUsage);

                log.info("Server stats fetched: {}", response);
                return Single.just(response);
            } catch (Exception e) {
                log.error("Failed to get server stats", e);
                return Single.error(new RuntimeException("Failed to get server stats: " + e.getMessage()));
            }
        }));
    }

    // Helper method to get first time slot
    private static int getFirstTimeSlot(Section section) {
        return section.getSpaceTimeSlots().stream().findFirst().map(SpaceTime::getTimeSlot).orElse(1);
    }

    // Helper method to build trimester JSON
    private static JsonNode buildTrimesterJson(Trimester trimester) {
        ObjectNode trimesterObj = JsonUtils.createObject()
                                           .put("id", trimester.getId().toString())
                                           .put("code", trimester.getCode())
                                           .put("year", trimester.getYear())
                                           .put("season", trimester.getSeason().toString())
                                           .put("status", trimester.getStatus().toString());

        if (trimester.getCourseSelectionStart() != null) trimesterObj.put("courseSelectionStart", trimester.getCourseSelectionStart()
                                                                                                           .toString());
        if (trimester.getCourseSelectionEnd() != null) trimesterObj.put("courseSelectionEnd", trimester.getCourseSelectionEnd()
                                                                                                       .toString());
        if (trimester.getSectionRegistrationStart() != null) trimesterObj.put("sectionRegistrationStart", trimester.getSectionRegistrationStart()
                                                                                                                   .toString());
        if (trimester.getSectionRegistrationEnd() != null) trimesterObj.put("sectionRegistrationEnd", trimester.getSectionRegistrationEnd()
                                                                                                               .toString());

        return trimesterObj;
    }

    // Helper method to build notification JSON
    private static JsonNode buildNotificationJson(Notification notification) {
        ObjectNode notificationObj = JsonUtils.createObject()
                                              .put("id", notification.getId().toString())
                                              .put("title", notification.getTitle())
                                              .put("content", notification.getContent())
                                              .put("category", notification.getCategory().toString())
                                              .put("scope", notification.getScope().toString())
                                              .put("createdAt", notification.getCreatedAt().toString())
                                              .put("senderId", notification.getSender().getId().toString())
                                              .put("senderName", notification.getSender().getName());

        switch (notification.getScope()) {
            case TRIMESTER:
                if (notification.getTrimester() != null) {
                    notificationObj.put("trimesterId", notification.getTrimester().getId().toString());
                    notificationObj.put("trimesterCode", notification.getTrimester().getCode().toString());
                }
                break;
            case SECTION:
                if (notification.getSection() != null) {
                    notificationObj.put("sectionId", notification.getSection().getId().toString());
                    notificationObj.put("sectionName", notification.getSection().getName());
                }
                break;
            case USER:
                if (notification.getTargetUser() != null) {
                    notificationObj.put("targetUserId", notification.getTargetUser().getId().toString());
                    notificationObj.put("targetUserName", notification.getTargetUser().getName());
                }
                break;
        }

        return notificationObj;
    }

    // Helper to group sections by day, combining Sat/Thu and Sun/Wed
    private static Map<String, List<Section>> groupSectionsByDay(List<Section> sections, SubjectType subjectType) {
        Map<String, List<Section>> grouped = new HashMap<>();

        for (Section section : sections) {
            for (SpaceTime slot : section.getSpaceTimeSlots()) {
                String day = slot.getDayOfWeek().toString();

                // Convert to proper case (e.g., "Saturday")
                day = day.charAt(0) + day.substring(1).toLowerCase();

                // Process day based on subject type
                if (subjectType == SubjectType.THEORY) {
                    // Combine days for theory
                    if (day.equals("Saturday") || day.equals("Tuesday")) {
                        day = "Sat+Tue";
                    } else if (day.equals("Sunday") || day.equals("Wednesday")) {
                        day = "Sun+Wed";
                    }
                } else {
                    // Short format for lab (first 3 letters capitalized)
                    day = day.substring(0, 3); // "Sat", "Sun", etc.
                }

                grouped.computeIfAbsent(day, k -> new ArrayList<>()).add(section);
            }
        }

        return grouped;
    }
}
