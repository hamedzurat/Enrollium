import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Prerequisite;
import enrollium.server.db.entity.Subject;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Prerequisite Entity Tests")
class PrerequisiteTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(Prerequisite.class);
        TestHelper.cleanupTestData(Subject.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(Prerequisite.class);
        TestHelper.cleanupTestData(Subject.class);
    }

    @Nested
    @DisplayName("Prerequisite Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create prerequisite with valid data")
        void createValidPrerequisite() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            Prerequisite prerequisite = new Prerequisite();
            prerequisite.setSubject(subject);
            prerequisite.setPrerequisite(prereq);
            prerequisite.setMinimumGrade(2.0);
            prerequisite.setInfo("Test prerequisite");

            Prerequisite saved = TestHelper.saveEntity(prerequisite);

            assertNotNull(saved.getId(), "Should generate ID");
            assertNotNull(saved.getCreatedAt(), "Should set creation timestamp");
            assertEquals(2.0, saved.getMinimumGrade(), "Should set minimum grade");
            assertEquals(subject.getId(), saved.getSubject().getId(), "Should set correct subject");
            assertEquals(prereq.getId(), saved.getPrerequisite().getId(), "Should set correct prerequisite");
        }

        @Test
        @DisplayName("Should not allow prerequisite with same subject")
        void preventSelfPrerequisite() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());

            Prerequisite prerequisite = new Prerequisite();
            prerequisite.setSubject(subject);
            prerequisite.setPrerequisite(subject);
            prerequisite.setMinimumGrade(2.0);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(prerequisite), "Should not allow subject to be its own prerequisite");
        }

        @Test
        @DisplayName("Should prevent duplicate prerequisites")
        void preventDuplicatePrerequisites() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            // Create first prerequisite
            Prerequisite first = new Prerequisite();
            first.setSubject(subject);
            first.setPrerequisite(prereq);
            first.setMinimumGrade(2.0);
            TestHelper.saveEntity(first);

            // Try to create duplicate
            Prerequisite duplicate = new Prerequisite();
            duplicate.setSubject(subject);
            duplicate.setPrerequisite(prereq);
            duplicate.setMinimumGrade(3.0);

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(duplicate), "Should not allow duplicate prerequisites for the same subject");
        }
    }


    @Nested
    @DisplayName("Grade Validation Tests")
    class GradeValidationTests {
        @Test
        @DisplayName("Should validate minimum grade range")
        void validateGradeRange() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            // Test below minimum (0.0)
            Prerequisite belowMin = new Prerequisite();
            belowMin.setSubject(subject);
            belowMin.setPrerequisite(prereq);
            belowMin.setMinimumGrade(-0.1);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(belowMin), "Should reject grade below 0.0");

            // Test above maximum (4.0)
            Prerequisite aboveMax = new Prerequisite();
            aboveMax.setSubject(subject);
            aboveMax.setPrerequisite(prereq);
            aboveMax.setMinimumGrade(4.1);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(aboveMax), "Should reject grade above 4.0");

            // Test valid range
            Prerequisite valid = new Prerequisite();
            valid.setSubject(subject);
            valid.setPrerequisite(prereq);
            valid.setMinimumGrade(2.0);
            assertDoesNotThrow(() -> TestHelper.saveEntity(valid), "Should accept grade within valid range");
        }

        @Test
        @DisplayName("Should reject null minimum grade")
        void rejectNullGrade() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            Prerequisite prerequisite = new Prerequisite();
            prerequisite.setSubject(subject);
            prerequisite.setPrerequisite(prereq);
            prerequisite.setMinimumGrade(null);

            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(prerequisite), "Should reject null minimum grade");
        }
    }


    @Nested
    @DisplayName("Circular Dependency Tests")
    class CircularDependencyTests {
        @Test
        @DisplayName("Should detect direct circular dependency")
        void detectDirectCircular() {
            Subject subjectA = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subjectB = TestHelper.saveEntity(TestHelper.createValidSubject());

            // Create A -> B
            Prerequisite prereqAB = new Prerequisite();
            prereqAB.setSubject(subjectA);
            prereqAB.setPrerequisite(subjectB);
            prereqAB.setMinimumGrade(2.0);
            TestHelper.saveEntity(prereqAB);

            // Try to create B -> A
            Prerequisite prereqBA = new Prerequisite();
            prereqBA.setSubject(subjectB);
            prereqBA.setPrerequisite(subjectA);
            prereqBA.setMinimumGrade(2.0);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(prereqBA), "Should detect and prevent direct circular dependency");
        }

        @Test
        @DisplayName("Should detect indirect circular dependency")
        void detectIndirectCircular() {
            Subject subjectA = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subjectB = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subjectC = TestHelper.saveEntity(TestHelper.createValidSubject());

            // Create A -> B
            Prerequisite prereqAB = new Prerequisite();
            prereqAB.setSubject(subjectA);
            prereqAB.setPrerequisite(subjectB);
            prereqAB.setMinimumGrade(2.0);
            TestHelper.saveEntity(prereqAB);

            // Create B -> C
            Prerequisite prereqBC = new Prerequisite();
            prereqBC.setSubject(subjectB);
            prereqBC.setPrerequisite(subjectC);
            prereqBC.setMinimumGrade(2.0);
            TestHelper.saveEntity(prereqBC);

            // Try to create C -> A
            Prerequisite prereqCA = new Prerequisite();
            prereqCA.setSubject(subjectC);
            prereqCA.setPrerequisite(subjectA);
            prereqCA.setMinimumGrade(2.0);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(prereqCA), "Should detect and prevent indirect circular dependency");
        }

        @Test
        @DisplayName("Should allow valid prerequisite chains")
        void allowValidChains() {
            Subject subjectA = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subjectB = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject subjectC = TestHelper.saveEntity(TestHelper.createValidSubject());

            // Create chain A -> B -> C
            Prerequisite prereqAB = new Prerequisite();
            prereqAB.setSubject(subjectA);
            prereqAB.setPrerequisite(subjectB);
            prereqAB.setMinimumGrade(2.0);
            TestHelper.saveEntity(prereqAB);

            Prerequisite prereqBC = new Prerequisite();
            prereqBC.setSubject(subjectB);
            prereqBC.setPrerequisite(subjectC);
            prereqBC.setMinimumGrade(2.0);

            assertDoesNotThrow(() -> TestHelper.saveEntity(prereqBC), "Should allow valid prerequisite chain");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            Prerequisite prerequisite = new Prerequisite();
            prerequisite.setSubject(subject);
            prerequisite.setPrerequisite(prereq);
            prerequisite.setMinimumGrade(2.0);

            Prerequisite saved           = TestHelper.saveEntity(prerequisite);
            Long         originalVersion = saved.getVersion();

            saved.setMinimumGrade(3.0);
            saved.setInfo("Updated Info");

            Prerequisite updated = DB.update(saved).blockingGet();
            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals(3.0, updated.getMinimumGrade(), "Should update minimum grade");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Prerequisite  prerequisite = TestHelper.saveEntity(TestHelper.createValidPrerequisiteWithRelations());
            LocalDateTime createdAt    = prerequisite.getCreatedAt();
            String        createdBy    = prerequisite.getCreatedBy();
            UUID          subjectId    = prerequisite.getSubject().getId();
            UUID          prereqId     = prerequisite.getPrerequisite().getId();

            prerequisite.setMinimumGrade(3.0);
            Prerequisite updated = DB.update(prerequisite).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
            assertEquals(subjectId, updated.getSubject().getId(), "Should not change subject");
            assertEquals(prereqId, updated.getPrerequisite().getId(), "Should not change prerequisite subject");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Prerequisite saved = TestHelper.saveEntity(TestHelper.createValidPrerequisiteWithRelations());
            Prerequisite found = DB.findById(Prerequisite.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing prerequisite");
            assertEquals(saved.getId(), found.getId(), "Should find correct prerequisite");
            assertEquals(saved.getMinimumGrade(), found.getMinimumGrade(), "Should maintain minimum grade");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            Prerequisite notFound = DB.findById(Prerequisite.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Prerequisite> prerequisites = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                prerequisites.add(TestHelper.saveEntity(TestHelper.createValidPrerequisiteWithRelations()));
            }

            List<Prerequisite> page = DB.read(Prerequisite.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing prerequisite")
        void deleteExisting() {
            Prerequisite saved = TestHelper.saveEntity(TestHelper.createValidPrerequisiteWithRelations());
            UUID         id    = saved.getId();

            DB.delete(Prerequisite.class, id).blockingAwait();
            Prerequisite deleted = DB.findById(Prerequisite.class, id).blockingGet();

            assertNull(deleted, "Should delete prerequisite");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Prerequisite.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent prerequisite");
        }

        @Test
        @DisplayName("Should maintain data integrity after deletion")
        void maintainDataIntegrity() {
            Subject subject = TestHelper.saveEntity(TestHelper.createValidSubject());
            Subject prereq  = TestHelper.saveEntity(TestHelper.createValidSubject());

            Prerequisite prerequisite = new Prerequisite();
            prerequisite.setSubject(subject);
            prerequisite.setPrerequisite(prereq);
            prerequisite.setMinimumGrade(2.0);

            Prerequisite saved = TestHelper.saveEntity(prerequisite);
            DB.delete(Prerequisite.class, saved.getId()).blockingAwait();

            // Verify subjects still exist
            Subject foundSubject = DB.findById(Subject.class, subject.getId()).blockingGet();
            Subject foundPrereq  = DB.findById(Subject.class, prereq.getId()).blockingGet();

            assertNotNull(foundSubject, "Subject should still exist after prerequisite deletion");
            assertNotNull(foundPrereq, "Prerequisite subject should still exist after prerequisite deletion");
        }
    }
}
