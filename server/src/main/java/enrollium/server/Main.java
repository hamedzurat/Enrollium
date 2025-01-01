package enrollium.server;

import com.fasterxml.jackson.databind.node.ArrayNode;
import core.JsonUtils;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.types.UserType;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.ServerRPC;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Main {
    private static final Random random = new Random();

    public static void main(String[] args) {
        try (ServerRPC server = new ServerRPC()) {
            registerMethods(server);

            server.start();
            log.info("Server is running. Press Ctrl+C to exit.");

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Server error", e);
        }
    }

    private static void registerMethods(ServerRPC server) {
        // Create student
        server.registerMethod("createStudent", (params, sessionToken) -> {
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

            Student saved = DB.save(student).blockingGet();
            log.info("Created new student: {}", saved.getName());

            return Single.just(JsonUtils.createObject()
                                        .put("id", saved.getId().toString())
                                        .put("name", saved.getName())
                                        .put("email", saved.getEmail())
                                        .put("universityId", saved.getUniversityId().toString()));
        });

        // Get all students
        server.registerMethod("getStudents", (params, sessionToken) -> {
            log.info("Fetching all students");
            try {
                List<Student> studentList = DB.read(Student.class, 100, 0)
                                              .timeout(20, TimeUnit.SECONDS)  // Add timeout to DB operation
                                              .toList()
                                              .blockingGet();

                log.info("Found {} students", studentList.size());

                ArrayNode students = JsonUtils.createArray();
                for (Student student : studentList) {
                    try {
                        students.add(JsonUtils.createObject()
                                              .put("id", student.getId().toString())
                                              .put("name", student.getName())
                                              .put("email", student.getEmail())
                                              .put("universityId", student.getUniversityId().toString()));
                    } catch (Exception e) {
                        log.error("Error processing student {}: {}", student.getId(), e.getMessage());
                        // Continue with next student
                    }
                }

                return Single.just(JsonUtils.createObject().set("students", students));
            } catch (Exception e) {
                log.error("Error fetching students", e);
                return Single.error(new RuntimeException("Failed to fetch students: " + e.getMessage()));
            }
        });

        // Update student
        server.registerMethod("updateStudent", (params, sessionToken) -> {
            String id    = JsonUtils.getString(params, "id");
            String name  = JsonUtils.getString(params, "name");
            String email = JsonUtils.getString(params, "email");

            Student student = DB.findById(Student.class, UUID.fromString(id)).blockingGet();

            if (student == null) {
                return Single.error(new RuntimeException("Student not found"));
            }

            student.setName(name);
            student.setEmail(email);

            Student updated = DB.update(student).blockingGet();
            log.info("Updated student: {}", updated.getName());

            return Single.just(JsonUtils.createObject()
                                        .put("id", updated.getId().toString())
                                        .put("name", updated.getName())
                                        .put("email", updated.getEmail())
                                        .put("universityId", updated.getUniversityId().toString()));
        });

        // Delete student
        server.registerMethod("deleteStudent", (params, sessionToken) -> {
            String id = JsonUtils.getString(params, "id");
            DB.delete(Student.class, UUID.fromString(id)).blockingAwait();
            log.info("Deleted student with ID: {}", id);
            return Single.just(JsonUtils.createObject().put("success", true));
        });
    }
}
