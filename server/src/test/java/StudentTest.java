import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.types.UserType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Student Entity Tests")
class StudentTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(Student.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(Student.class);
    }

    @Nested
    @DisplayName("Student Type Enforcement Tests")
    class StudentTypeTests {
        @Test
        @DisplayName("Should set STUDENT type by default")
        void defaultStudentType() {
            Student student = new Student();
            assertEquals(UserType.STUDENT, student.getType(), "New student should have STUDENT type");
        }

        @Test
        @DisplayName("Should prevent changing to non-STUDENT type")
        void preventTypeChange() {
            Student student = TestHelper.createValidStudent();
            student.setType(UserType.TEACHER);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(student), "Should reject non-STUDENT type");
        }

        @Test
        @DisplayName("Should maintain STUDENT type after update")
        void maintainTypeAfterUpdate() {
            Student student = TestHelper.saveEntity(TestHelper.createValidStudent());
            student.setName("Updated Name");

            Student updated = DB.update(student).blockingGet();
            assertEquals(UserType.STUDENT, updated.getType(), "Type should remain STUDENT after update");
        }
    }


    @Nested
    @DisplayName("Student Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create student with valid data")
        void createValidStudent() {
            Student student = TestHelper.saveEntity(TestHelper.createValidStudent());

            assertNotNull(student.getId(), "Should generate ID");
            assertNotNull(student.getCreatedAt(), "Should set creation timestamp");
            assertEquals(UserType.STUDENT, student.getType(), "Should be STUDENT type");
            assertTrue(student.getUniversityId() >= 1000, "Should have valid university ID");
            assertTrue(student.getEmail()
                              .contains(student.getUniversityId().toString()), "Email should contain university ID");
        }

        @Test
        @DisplayName("Should initialize empty courses")
        void initializeEmptyCourses() {
            Student student = TestHelper.saveEntity(TestHelper.createValidStudent());
            assertNotNull(student.getCourses(), "Should initialize courses collection");
            assertTrue(student.getCourses().isEmpty(), "Courses should be empty initially");
        }
    }


    @Nested
    @DisplayName("University ID Validation Tests")
    class UniversityIdTests {
        @Test
        @DisplayName("Should reject null university ID")
        void rejectNullId() {
            TestHelper.assertValidationFails(TestHelper.createValidStudent(), student -> student.setUniversityId(null), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should reject negative university ID")
        void rejectNegativeId() {
            TestHelper.assertValidationFails(TestHelper.createValidStudent(), student -> student.setUniversityId(-1), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should enforce university ID uniqueness")
        void enforceIdUniqueness() {
            Student student1 = TestHelper.saveEntity(TestHelper.createValidStudent());
            Student student2 = TestHelper.createValidStudent();
            student2.setUniversityId(student1.getUniversityId());

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(student2), "Should reject duplicate university ID");
        }
    }


    @Nested
    @DisplayName("Email Validation Tests")
    class EmailTests {
        @ParameterizedTest
        @DisplayName("Should reject invalid email formats")
        @ValueSource(strings = {"invalid-email", "@uiu.ac.bd", "test@", "test@uiu.", " @uiu.ac.bd", "test@uiu.ac.bd "})
        void rejectInvalidEmails(String invalidEmail) {
            TestHelper.assertValidationFails(TestHelper.createValidStudent(), student -> student.setEmail(invalidEmail), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should enforce email uniqueness")
        void enforceEmailUniqueness() {
            Student student1 = TestHelper.saveEntity(TestHelper.createValidStudent());
            Student student2 = TestHelper.createValidStudent();
            student2.setEmail(student1.getEmail());

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(student2), "Should reject duplicate email");
        }

        @Test
        @DisplayName("Should reject oversized email")
        void rejectOversizedEmail() {
            TestHelper.assertValidationFails(TestHelper.createValidStudent(), student -> student.setEmail("a".repeat(60) + "@uiu.ac.bd"), ConstraintViolationException.class);
        }
    }


    @Nested
    @DisplayName("Password Handling Tests")
    class PasswordTests {
        @Test
        @DisplayName("Should hash password")
        void hashPassword() {
            Student student       = TestHelper.createValidStudent();
            String  plainPassword = TestHelper.randomPassword();
            student.setPassword(plainPassword);

            assertNotEquals(plainPassword, student.getPassword(), "Should hash password");
            assertTrue(student.verifyPassword(plainPassword), "Should verify correct password");
        }

        @Test
        @DisplayName("Should reject short password")
        void rejectShortPassword() {
            Student student = TestHelper.createValidStudent();
            assertThrows(IllegalArgumentException.class, () -> student.setPassword("short"), "Should reject password shorter than 8 characters");
        }

        @Test
        @DisplayName("Should reject null password")
        void rejectNullPassword() {
            Student student = TestHelper.createValidStudent();
            assertThrows(IllegalArgumentException.class, () -> student.setPassword(null), "Should reject null password");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Student student         = TestHelper.saveEntity(TestHelper.createValidStudent());
            Long    originalVersion = student.getVersion();

            student.setName("Updated Name");
            student.setInfo("Updated Info");
            Student updated = DB.update(student).blockingGet();

            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Name", updated.getName(), "Should update name");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(UserType.STUDENT, updated.getType(), "Should maintain STUDENT type");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Student       student      = TestHelper.saveEntity(TestHelper.createValidStudent());
            LocalDateTime createdAt    = student.getCreatedAt();
            String        createdBy    = student.getCreatedBy();
            Integer       universityId = student.getUniversityId();

            student.setName("Updated Name");
            Student updated = DB.update(student).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
            assertEquals(universityId, updated.getUniversityId(), "Should not change university ID");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Student saved = TestHelper.saveEntity(TestHelper.createValidStudent());
            Student found = DB.findById(Student.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing student");
            assertEquals(saved.getId(), found.getId(), "Should find correct student");
            assertEquals(UserType.STUDENT, found.getType(), "Should maintain STUDENT type");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            Student notFound = DB.findById(Student.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            // Create test students
            List<Student> students = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                students.add(TestHelper.saveEntity(TestHelper.createValidStudent()));
            }

            List<Student> page = DB.read(Student.class, 3, 0).toList().blockingGet();

            assertEquals(3, page.size(), "Should return requested page size");
            page.forEach(student -> assertEquals(UserType.STUDENT, student.getType(), "All students should be STUDENT type"));
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing student")
        void deleteExisting() {
            Student saved = TestHelper.saveEntity(TestHelper.createValidStudent());
            UUID    id    = saved.getId();

            DB.delete(Student.class, id).blockingAwait();
            Student deleted = DB.findById(Student.class, id).blockingGet();

            assertNull(deleted, "Should delete student");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Student.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent student");
        }
    }
}

