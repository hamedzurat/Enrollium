package enrollium.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.JsonUtils;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Student;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;
import server.ServerRPC;

import java.util.UUID;


@Slf4j
public class Main {
    private static final ServerRPC server = new ServerRPC();

    public static void main(String[] args) {
        // Create shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down server...");
            server.close();
            log.info("Server shutdown complete.");
        }));

        try (ServerRPC server = new ServerRPC()) {
            // Register methods
            server.registerMethod("createStudent", (params, sessionToken) -> {
                // Create and save a student
                Student student = TestHelper.createValidStudent();
                Student saved   = DB.save(student).blockingGet();

                // Convert to JSON response
                ObjectNode result = JsonUtils.createObject()
                                             .put("id", saved.getId().toString())
                                             .put("name", saved.getName())
                                             .put("universityId", saved.getUniversityId())
                                             .put("email", saved.getEmail());

                return Single.just(result);
            });

            server.registerMethod("getStudent", (params, sessionToken) -> {
                String studentId = JsonUtils.getString(params, "studentId");
                UUID   id        = UUID.fromString(studentId);

                return DB.findById(Student.class, id).map(student -> {
                    ObjectNode result = JsonUtils.createObject()
                                                 .put("id", student.getId().toString())
                                                 .put("name", student.getName())
                                                 .put("universityId", student.getUniversityId())
                                                 .put("email", student.getEmail());

                    return (JsonNode) result;
                }).toSingle();
            });

            // Start server
            server.start();
            System.out.println("server started...");
            log.info("Server is running. Press Ctrl+C to exit.");

            // Keep main thread alive
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
}
