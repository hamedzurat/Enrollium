import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Course;
import enrollium.server.db.entity.Section;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.types.CourseStatus;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Course Entity Tests")
class CourseTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupAllTestData();
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupAllTestData();
    }

    @Nested
    @DisplayName("Course Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create course with valid data")
        void createValidCourse() {
            Course course = TestHelper.saveEntity(TestHelper.createSelectedCourse());

            assertNotNull(course.getId(), "Should generate ID");
            assertNotNull(course.getCreatedAt(), "Should set creation timestamp");
            assertEquals(CourseStatus.SELECTED, course.getStatus(), "Should be SELECTED status");
            assertNull(course.getSection(), "Should have null section");
            assertNull(course.getGrade(), "Should have null grade");
        }

        @Test
        @DisplayName("Should require non-null relationships")
        void requireNonNullRelationships() {
            Course course = TestHelper.createSelectedCourse();

            // Test student requirement
            Course noStudent = TestHelper.createSelectedCourse();
            noStudent.setStudent(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(noStudent),
                    "Should require student");

            // Test subject requirement
            Course noSubject = TestHelper.createSelectedCourse();
            noSubject.setSubject(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(noSubject),
                    "Should require subject");

            // Test trimester requirement
            Course noTrimester = TestHelper.createSelectedCourse();
            noTrimester.setTrimester(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(noTrimester),
                    "Should require trimester");
        }
    }


    @Nested
    @DisplayName("Course Status Validation Tests")
    class StatusValidationTests {
        @Test
        @DisplayName("Should validate SELECTED status constraints")
        void validateSelectedStatus() {
            Course course = TestHelper.createSelectedCourse();

            // Try to add section (should fail)
            Section section = TestHelper.saveEntity(TestHelper.createValidSection(course.getSubject(), course.getTrimester()));
            course.setSection(section);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(course),
                    "SELECTED status should not allow section");

            // Try to add grade (should fail)
            Course withGrade = TestHelper.createSelectedCourse();
            withGrade.setGrade(3.0);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withGrade),
                    "SELECTED status should not allow grade");
        }

        @Test
        @DisplayName("Should validate REGISTERED status constraints")
        void validateRegisteredStatus() {
            // Create and save a course first
            Course course = TestHelper.saveEntity(TestHelper.createRegisteredCourse());

            // Now create a new course for testing section requirement
            Course testCourse = TestHelper.createRegisteredCourse();
            testCourse.setSection(null);

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(testCourse),
                    "REGISTERED status requires section");

            // Test for grade validation
            Course withGrade = TestHelper.createRegisteredCourse();
            withGrade.setGrade(3.0);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withGrade),
                    "REGISTERED status should not allow grade");
        }

        @Test
        @DisplayName("Should validate COMPLETED status constraints")
        void validateCompletedStatus() {
            // Test valid completed course
            Course course = TestHelper.createCompletedCourse();
            assertDoesNotThrow(() -> TestHelper.saveEntity(course),
                    "Should allow valid completed course");

            // Test missing section
            Course noSection = TestHelper.createCompletedCourse();
            noSection.setSection(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noSection),
                    "COMPLETED status requires section");

            // Test missing grade
            Course noGrade = TestHelper.createCompletedCourse();
            noGrade.setGrade(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noGrade),
                    "COMPLETED status requires grade");
        }

        @Test
        @DisplayName("Should validate DROPPED status constraints")
        void validateDroppedStatus() {
            // Test valid dropped course
            Course course = TestHelper.createDroppedCourse();
            assertDoesNotThrow(() -> TestHelper.saveEntity(course),
                    "Should allow valid dropped course");

            // Test missing section
            Course noSection = TestHelper.createDroppedCourse();
            noSection.setSection(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noSection),
                    "DROPPED status requires section");

            // Test with grade (should fail)
            Course withGrade = TestHelper.createDroppedCourse();
            withGrade.setGrade(3.0);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withGrade),
                    "DROPPED status should not allow grade");
        }
    }


    @Nested
    @DisplayName("Grade Validation Tests")
    class GradeValidationTests {
        @Test
        @DisplayName("Should validate grade range")
        void validateGradeRange() {
            // Test minimum grade
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createCompletedCourseWithGrade(0.0)),
                    "Should allow minimum grade of 0.0");

            // Test maximum grade
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createCompletedCourseWithGrade(4.0)),
                    "Should allow maximum grade of 4.0");

            // Test below minimum
            assertThrows(IllegalArgumentException.class,  // Changed from ConstraintViolationException
                    () -> TestHelper.saveEntity(TestHelper.createCompletedCourseWithGrade(-0.1)),
                    "Should reject grade below 0.0");

            // Test above maximum
            assertThrows(IllegalArgumentException.class,  // Changed from ConstraintViolationException
                    () -> TestHelper.saveEntity(TestHelper.createCompletedCourseWithGrade(4.1)),
                    "Should reject grade above 4.0");
        }
    }


    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {
        @Test
        @DisplayName("Should handle transition from SELECTED to REGISTERED")
        void transitionSelectedToRegistered() {
            Course  course  = TestHelper.saveEntity(TestHelper.createSelectedCourse());
            Section section = TestHelper.saveEntity(TestHelper.createValidSection(course.getSubject(), course.getTrimester()));

            course.setStatus(CourseStatus.REGISTERED);
            course.setSection(section);

            Course updated = DB.update(course).blockingGet();
            assertEquals(CourseStatus.REGISTERED, updated.getStatus());
            assertNotNull(updated.getSection());
            assertNull(updated.getGrade());
        }

        @Test
        @DisplayName("Should handle transition from REGISTERED to COMPLETED")
        void transitionRegisteredToCompleted() {
            Course course = TestHelper.saveEntity(TestHelper.createRegisteredCourse());

            course.setStatus(CourseStatus.COMPLETED);
            course.setGrade(3.5);

            Course updated = DB.update(course).blockingGet();
            assertEquals(CourseStatus.COMPLETED, updated.getStatus());
            assertNotNull(updated.getSection());
            assertEquals(3.5, updated.getGrade());
        }

        @Test
        @DisplayName("Should handle transition from REGISTERED to DROPPED")
        void transitionRegisteredToDropped() {
            Course course = TestHelper.saveEntity(TestHelper.createRegisteredCourse());

            course.setStatus(CourseStatus.DROPPED);

            Course updated = DB.update(course).blockingGet();
            assertEquals(CourseStatus.DROPPED, updated.getStatus());
            assertNotNull(updated.getSection());
            assertNull(updated.getGrade());
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Course  course          = TestHelper.saveEntity(TestHelper.createRegisteredCourse());
            Long    originalVersion = course.getVersion();
            Section newSection      = TestHelper.saveEntity(TestHelper.createValidSection(course.getSubject(), course.getTrimester()));

            course.setSection(newSection);
            course.setInfo("Updated Info");

            Course updated = DB.update(course).blockingGet();
            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(newSection.getId(), updated.getSection().getId(), "Should update section");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Course        course          = TestHelper.saveEntity(TestHelper.createRegisteredCourse());
            LocalDateTime createdAt       = course.getCreatedAt();
            String        createdBy       = course.getCreatedBy();
            Student       originalStudent = course.getStudent();

            course.setInfo("Updated Info");
            Course updated = DB.update(course).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
            assertEquals(originalStudent.getId(), updated.getStudent().getId(), "Should not change student");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Course saved = TestHelper.saveEntity(TestHelper.createSelectedCourse());
            Course found = DB.findById(Course.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing course");
            assertEquals(saved.getId(), found.getId(), "Should find correct course");
            assertEquals(saved.getStatus(), found.getStatus(), "Should maintain status");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Course> courses = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                courses.add(TestHelper.saveEntity(TestHelper.createSelectedCourse()));
            }

            List<Course> page = DB.read(Course.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing course")
        void deleteExisting() {
            Course saved = TestHelper.saveEntity(TestHelper.createSelectedCourse());
            UUID   id    = saved.getId();

            DB.delete(Course.class, id).blockingAwait();
            Course deleted = DB.findById(Course.class, id).blockingGet();

            assertNull(deleted, "Should delete course");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class,
                    () -> DB.delete(Course.class, UUID.randomUUID()).blockingAwait(),
                    "Should throw exception for non-existent course");
        }
    }
}
