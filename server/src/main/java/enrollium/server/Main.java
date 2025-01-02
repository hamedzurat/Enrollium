package enrollium.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import core.JsonUtils;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.types.UserType;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.ServerRPC;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Main {
    private static final Random random = new Random();

    public static void main(String[] args) {
        CountDownLatch shutdownLatch = new CountDownLatch(1);

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

        try (ServerRPC server = new ServerRPC()) {
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

    private static void registerMethods(ServerRPC server) {
        // Create student
        server.registerMethod("Student.create", (params, _) -> {
            return Single.defer(() -> {
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
            });
        });

        // Get all students
        server.registerMethod("Students.getAll", (_, _) -> {
            return Single.defer(() -> {
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
            });
        });

        // Update student
        server.registerMethod("Student.update", (params, _) -> {
            return Single.defer(() -> {
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
            });
        });

        // Delete student
        server.registerMethod("Student.delete", (params, _) -> {
            return Single.defer(() -> {
                String id = JsonUtils.getString(params, "id");

                return DB.delete(Student.class, UUID.fromString(id)).toSingle(() -> {
                    log.info("Deleted student with ID: {}", id);
                    return JsonUtils.createObject().put("success", true);
                }).doOnError(error -> log.warn("Failed to delete student: {}", error.getMessage()));
            });
        });
    }
}
