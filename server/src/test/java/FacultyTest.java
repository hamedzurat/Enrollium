import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Faculty;
import enrollium.server.db.entity.Subject;
import enrollium.server.db.entity.types.UserType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Faculty Entity Tests")
class FacultyTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(Faculty.class);
        TestHelper.cleanupTestData(Subject.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(Faculty.class);
        TestHelper.cleanupTestData(Subject.class);
    }

    @Nested
    @DisplayName("Faculty Type Enforcement Tests")
    class FacultyTypeTests {
        @Test
        @DisplayName("Should allow TEACHER type")
        void allowTeacherType() {
            Faculty faculty = TestHelper.createValidFaculty();
            faculty.setType(UserType.TEACHER);

            Faculty saved = TestHelper.saveEntity(faculty);
            assertEquals(UserType.TEACHER, saved.getType(), "Should accept TEACHER type");
        }

        @Test
        @DisplayName("Should allow ADMIN type")
        void allowAdminType() {
            Faculty faculty = TestHelper.createValidFaculty();
            faculty.setType(UserType.ADMIN);

            Faculty saved = TestHelper.saveEntity(faculty);
            assertEquals(UserType.ADMIN, saved.getType(), "Should accept ADMIN type");
        }

        @Test
        @DisplayName("Should prevent STUDENT type")
        void preventStudentType() {
            Faculty faculty = TestHelper.createValidFaculty();
            faculty.setType(UserType.STUDENT);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(faculty), "Should reject STUDENT type");
        }
    }


    @Nested
    @DisplayName("Faculty Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create faculty with valid data")
        void createValidFaculty() {
            Faculty faculty = TestHelper.saveEntity(TestHelper.createValidFaculty());

            assertNotNull(faculty.getId(), "Should generate ID");
            assertNotNull(faculty.getCreatedAt(), "Should set creation timestamp");
            assertEquals(UserType.TEACHER, faculty.getType(), "Should be TEACHER type by default");
            assertNotNull(faculty.getShortcode(), "Should have shortcode");
            assertNotNull(faculty.getTeachableSubjects(), "Should initialize teachable subjects");
        }

        @Test
        @DisplayName("Should initialize empty teachable subjects")
        void initializeEmptyTeachableSubjects() {
            Faculty faculty = TestHelper.saveEntity(TestHelper.createValidFaculty());
            assertNotNull(faculty.getTeachableSubjects(), "Should initialize teachable subjects collection");
            assertTrue(faculty.getTeachableSubjects().isEmpty(), "Teachable subjects should be empty initially");
        }
    }


    @Nested
    @DisplayName("Shortcode Validation Tests")
    class ShortcodeTests {
        @Test
        @DisplayName("Should reject null shortcode")
        void rejectNullShortcode() {
            TestHelper.assertValidationFails(TestHelper.createValidFaculty(), faculty -> faculty.setShortcode(null), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should reject blank shortcode")
        void rejectBlankShortcode() {
            TestHelper.assertValidationFails(TestHelper.createValidFaculty(), faculty -> faculty.setShortcode("   "), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should reject oversized shortcode")
        void rejectOversizedShortcode() {
            TestHelper.assertValidationFails(TestHelper.createValidFaculty(), faculty -> faculty.setShortcode("A".repeat(11)), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should enforce shortcode uniqueness")
        void enforceShortcodeUniqueness() {
            Faculty faculty1 = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Faculty faculty2 = TestHelper.createValidFaculty();
            faculty2.setShortcode(faculty1.getShortcode());

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(faculty2), "Should reject duplicate shortcode");
        }
    }


    @Nested
    @DisplayName("Email Validation Tests")
    class EmailTests {
        @ParameterizedTest
        @DisplayName("Should reject invalid email formats")
        @ValueSource(strings = {"invalid-email", "@uiu.ac.bd", "test@", "test@uiu.", " @uiu.ac.bd", "test@uiu.ac.bd "})
        void rejectInvalidEmails(String invalidEmail) {
            TestHelper.assertValidationFails(TestHelper.createValidFaculty(), faculty -> faculty.setEmail(invalidEmail), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should enforce email uniqueness")
        void enforceEmailUniqueness() {
            Faculty faculty1 = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Faculty faculty2 = TestHelper.createValidFaculty();
            faculty2.setEmail(faculty1.getEmail());

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(faculty2), "Should reject duplicate email");
        }
    }


    @Nested
    @DisplayName("Teachable Subjects Tests")
    class TeachableSubjectsTests {
        @Test
        @DisplayName("Should add teachable subject")
        void addTeachableSubject() {
            Faculty faculty = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());

            faculty.setTeachableSubjects(new HashSet<>());
            faculty.getTeachableSubjects().add(subject);

            Faculty updated = DB.update(faculty).blockingGet();
            assertEquals(1, updated.getTeachableSubjects().size(), "Should have one teachable subject");
            assertTrue(updated.getTeachableSubjects().contains(subject), "Should contain added subject");
        }

        @Test
        @DisplayName("Should remove teachable subject")
        void removeTeachableSubject() {
            Faculty faculty = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());

            faculty.setTeachableSubjects(new HashSet<>());
            faculty.getTeachableSubjects().add(subject);
            Faculty withSubject = DB.update(faculty).blockingGet();

            withSubject.getTeachableSubjects().remove(subject);
            Faculty updated = DB.update(withSubject).blockingGet();

            assertTrue(updated.getTeachableSubjects().isEmpty(), "Should have no teachable subjects");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Faculty faculty         = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Long    originalVersion = faculty.getVersion();

            faculty.setName("Updated Name");
            faculty.setInfo("Updated Info");
            faculty.setType(UserType.ADMIN);

            Faculty updated = DB.update(faculty).blockingGet();

            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Name", updated.getName(), "Should update name");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(UserType.ADMIN, updated.getType(), "Should update type");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Faculty       faculty   = TestHelper.saveEntity(TestHelper.createValidFaculty());
            LocalDateTime createdAt = faculty.getCreatedAt();
            String        createdBy = faculty.getCreatedBy();

            faculty.setName("Updated Name");
            Faculty updated = DB.update(faculty).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Faculty saved = TestHelper.saveEntity(TestHelper.createValidFaculty());
            Faculty found = DB.findById(Faculty.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing faculty");
            assertEquals(saved.getId(), found.getId(), "Should find correct faculty");
            assertEquals(saved.getShortcode(), found.getShortcode(), "Should maintain shortcode");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            Faculty notFound = DB.findById(Faculty.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Faculty> faculties = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                faculties.add(TestHelper.saveEntity(TestHelper.createValidFaculty()));
            }

            List<Faculty> page = DB.read(Faculty.class, 3, 0).toList().blockingGet();

            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing faculty")
        void deleteExisting() {
            Faculty saved = TestHelper.saveEntity(TestHelper.createValidFaculty());
            UUID    id    = saved.getId();

            DB.delete(Faculty.class, id).blockingAwait();
            Faculty deleted = DB.findById(Faculty.class, id).blockingGet();

            assertNull(deleted, "Should delete faculty");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Faculty.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent faculty");
        }
    }
}
