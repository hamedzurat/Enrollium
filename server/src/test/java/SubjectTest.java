import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Subject;
import enrollium.server.db.entity.types.SubjectType;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Subject Entity Tests")
class SubjectTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(Subject.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(Subject.class);
    }

    @Nested
    @DisplayName("Subject Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create subject with valid data")
        void createValidSubject() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());

            assertNotNull(subject.getId(), "Should generate ID");
            assertNotNull(subject.getCreatedAt(), "Should set creation timestamp");
            assertEquals(SubjectType.THEORY, subject.getType(), "Should set subject type");
            assertNotNull(subject.getPrerequisites(), "Should initialize prerequisites");
        }

        @Test
        @DisplayName("Should initialize empty prerequisites")
        void initializeEmptyPrerequisites() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            assertNotNull(subject.getPrerequisites(), "Should initialize prerequisites collection");
            assertTrue(subject.getPrerequisites().isEmpty(), "Prerequisites should be empty initially");
        }
    }


    @Nested
    @DisplayName("Subject Validation Tests")
    class ValidationTests {
        @Test
        @DisplayName("Should reject null name")
        void rejectNullName() {
            TestHelper.assertValidationFails(TestHelper.createValidSubject(), subject -> subject.setName(null), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should reject blank name")
        void rejectBlankName() {
            TestHelper.assertValidationFails(TestHelper.createValidSubject(), subject -> subject.setName("   "), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should reject oversized name")
        void rejectOversizedName() {
            TestHelper.assertValidationFails(TestHelper.createValidSubject(), subject -> subject.setName("A".repeat(101)), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should enforce codeName uniqueness")
        void enforceCodeNameUniqueness() {
            Subject subject1 = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subject2 = TestHelper.createValidSubject();
            subject2.setCodeName(subject1.getCodeName());

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(subject2), "Should reject duplicate code name");
        }

        @Test
        @DisplayName("Should validate credits range")
        void validateCreditsRange() {
            // Test minimum
            Subject minSubject = TestHelper.createValidSubject();
            minSubject.setCredits(0);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(minSubject), "Should reject credits less than 1");

            // Test maximum
            Subject maxSubject = TestHelper.createValidSubject();
            maxSubject.setCredits(6);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(maxSubject), "Should reject credits more than 5");

            // Test valid values
            Subject validSubject = TestHelper.createValidSubject();
            validSubject.setCredits(3);
            assertDoesNotThrow(() -> TestHelper.saveEntity(validSubject), "Should accept credits between 1 and 5");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Subject subject         = TestHelper.saveEntity(TestHelper.createValidSubject());
            Long    originalVersion = subject.getVersion();

            subject.setName("Updated Name");
            subject.setInfo("Updated Info");
            subject.setType(SubjectType.LAB);

            Subject updated = DB.update(subject).blockingGet();

            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Name", updated.getName(), "Should update name");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(SubjectType.LAB, updated.getType(), "Should update type");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Subject       subject   = TestHelper.saveEntity(TestHelper.createValidSubject());
            LocalDateTime createdAt = subject.getCreatedAt();
            String        createdBy = subject.getCreatedBy();

            subject.setName("Updated Name");
            Subject updated = DB.update(subject).blockingGet();

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
            Subject saved = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject found = DB.findById(Subject.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing subject");
            assertEquals(saved.getId(), found.getId(), "Should find correct subject");
            assertEquals(saved.getCodeName(), found.getCodeName(), "Should maintain code name");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Subject> subjects = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                subjects.add(TestHelper.saveEntity(TestHelper.createValidSubject()));
            }

            List<Subject> page = DB.read(Subject.class, 3, 0).toList().blockingGet();

            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing subject")
        void deleteExisting() {
            Subject saved = TestHelper.saveEntity(TestHelper.createValidSubject());
            UUID    id    = saved.getId();

            DB.delete(Subject.class, id).blockingAwait();
            Subject deleted = DB.findById(Subject.class, id).blockingGet();

            assertNull(deleted, "Should delete subject");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Subject.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent subject");
        }
    }
}
