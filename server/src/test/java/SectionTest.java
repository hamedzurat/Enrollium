import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Section Entity Tests")
class SectionTest {
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
    @DisplayName("Section Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create section with valid data")
        void createValidSection() {
            Section section = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());

            assertNotNull(section.getId(), "Should generate ID");
            assertNotNull(section.getCreatedAt(), "Should set creation timestamp");
            assertNotNull(section.getSubject(), "Should have subject");
            assertNotNull(section.getTrimester(), "Should have trimester");
            assertFalse(section.getSpaceTimeSlots().isEmpty(), "Should have space-time slots");
            assertEquals(0, section.getCurrentCapacity(), "Should initialize with zero current capacity");
            assertTrue(section.getMaxCapacity() > 0, "Should have positive max capacity");
        }

        @Test
        @DisplayName("Should require at least one space-time slot")
        void requireSpaceTimeSlot() {
            Section section = TestHelper.createValidSectionWithRelations();
            section.setSpaceTimeSlots(new HashSet<>());

            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(section), "Should reject section without space-time slots");
        }

        @Test
        @DisplayName("Should reject null subject")
        void rejectNullSubject() {
            // Create base valid data
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());
            SpaceTime spaceTime = TestHelper.saveEntity(TestHelper.createValidSpaceTime());

            // Create section without subject
            Section section = new Section();
            section.setName("Test Section 1");
            section.setSection("A1");
            section.setMaxCapacity(40);
            section.setCurrentCapacity(0);
            section.setTrimester(trimester);
            section.setSubject(null);
            section.setSpaceTimeSlots(new HashSet<>(List.of(spaceTime)));

            assertThrows(ConstraintViolationException.class,
                    () -> TestHelper.saveEntity(section),
                    "Should require subject");
        }

        @Test
        @DisplayName("Should reject null trimester")
        void rejectNullTrimester() {
            // Create base valid data
            Subject   subject   = TestHelper.saveEntity(TestHelper.createValidSubject());
            SpaceTime spaceTime = TestHelper.saveEntity(TestHelper.createValidSpaceTime());

            // Create section without trimester
            Section section = new Section();
            section.setName("Test Section 2");
            section.setSection("A2");
            section.setMaxCapacity(40);
            section.setCurrentCapacity(0);
            section.setSubject(subject);
            section.setTrimester(null);
            section.setSpaceTimeSlots(new HashSet<>(List.of(spaceTime)));

            assertThrows(ConstraintViolationException.class,
                    () -> TestHelper.saveEntity(section),
                    "Should require trimester");
        }
    }


    @Nested
    @DisplayName("Course Registration Tests")
    class CourseRegistrationTests {
        @Test
        @DisplayName("Should validate section assignment based on course status")
        void validateSectionAssignment() {
            Section section = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());

            // Test SELECTED status
            Course selectedCourse = TestHelper.createSelectedCourse();
            selectedCourse.setSection(section);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(selectedCourse), "Section must be null when status is SELECTED");

            // Test REGISTERED status
            Course registeredCourse = TestHelper.createRegisteredCourse();
            registeredCourse.setSection(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(registeredCourse), "Section must not be null when status is REGISTERED");
        }
    }


    @Nested
    @DisplayName("Capacity Validation Tests")
    class CapacityTests {
        @Test
        @DisplayName("Should validate capacity constraints")
        void validateCapacityConstraints() {
            Subject   subject   = TestHelper.saveEntity(TestHelper.createValidSubject());
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());

            // Test negative current capacity
            Section negativeCurrentCapacity = TestHelper.createSectionWithCapacity(subject, trimester, 40, -1);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(negativeCurrentCapacity), "Should reject negative current capacity");

            // Test zero max capacity
            Section zeroMaxCapacity = TestHelper.createSectionWithCapacity(subject, trimester, 0, 0);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(zeroMaxCapacity), "Should reject zero max capacity");

            // Test current capacity exceeding max capacity
            Section exceededCapacity = TestHelper.createSectionWithCapacity(subject, trimester, 30, 31);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(exceededCapacity), "Should reject current capacity exceeding max capacity");

            // Test valid capacities
            Section validCapacity = TestHelper.createSectionWithCapacity(subject, trimester, 40, 20);
            assertDoesNotThrow(() -> TestHelper.saveEntity(validCapacity), "Should accept valid capacity values");
        }
    }


    @Nested
    @DisplayName("Space-Time Slot Tests")
    class SpaceTimeSlotTests {
        @Test
        @DisplayName("Should handle multiple space-time slots")
        void handleMultipleSpaceTimeSlots() {
            Subject   subject   = TestHelper.saveEntity(TestHelper.createValidSubject());
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Section   section   = TestHelper.createSectionWithMultipleSpaceTimeSlots(subject, trimester, 3);

            Section saved = TestHelper.saveEntity(section);
            assertEquals(3, saved.getSpaceTimeSlots().size(), "Should save all space-time slots");
        }

        @Test
        @DisplayName("Should prevent duplicate space-time slots in same trimester")
        void preventDuplicateSpaceTimeSlots() {
            // Create first section
            Section   section1  = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            SpaceTime spaceTime = section1.getSpaceTimeSlots().iterator().next();

            // Try to create second section with same space-time slot in same trimester
            Section section2 = TestHelper.createValidSectionWithRelations();
            section2.setTrimester(section1.getTrimester());
            section2.setSpaceTimeSlots(new HashSet<>(section1.getSpaceTimeSlots()));

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(section2), "Should reject duplicate space-time slot in same trimester");
        }
    }


    @Nested
    @DisplayName("Faculty Assignment Tests")
    class FacultyTests {
        @Test
        @DisplayName("Should handle multiple teachers")
        void handleMultipleTeachers() {
            Subject   subject   = TestHelper.saveEntity(TestHelper.createValidSubject());
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Section   section   = TestHelper.createSectionWithMultipleTeachers(subject, trimester, 3);

            Section saved = TestHelper.saveEntity(section);
            assertEquals(3, saved.getTeachers().size(), "Should save all teachers");
        }

        @Test
        @DisplayName("Should allow teacher to have multiple sections")
        void allowTeacherMultipleSections() {
            Faculty faculty = TestHelper.saveEntity(TestHelper.createValidFaculty());

            // Create first section
            Section section1 = TestHelper.createValidSectionWithRelations();
            section1.getTeachers().add(faculty);
            TestHelper.saveEntity(section1);

            // Create second section with same teacher
            Section section2 = TestHelper.createValidSectionWithRelations();
            section2.getTeachers().add(faculty);
            assertDoesNotThrow(() -> TestHelper.saveEntity(section2), "Should allow teacher to have multiple sections");
        }
    }


    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {
        @Test
        @DisplayName("Should track registrations")
        void trackRegistrations() {
            Section section = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Student student = TestHelper.saveEntity(TestHelper.createValidStudent());

            // Create and save a course registration with REGISTERED status
            Course registration = TestHelper.createRegisteredCourse();
            registration.setSubject(section.getSubject());
            registration.setTrimester(section.getTrimester());
            registration.setStudent(student);
            registration.setSection(section);
            TestHelper.saveEntity(registration);

            // Use DB.read to fetch complete section with registrations
            List<Course> registrations = DB.read(Course.class, 10, 0)
                                           .filter(course -> course.getSection() != null && course.getSection()
                                                                                                  .getId()
                                                                                                  .equals(section.getId()))
                                           .toList()
                                           .blockingGet();

            assertEquals(1, registrations.size(), "Should track registration");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Section section         = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Long    originalVersion = section.getVersion();

            section.setName("Updated Name");
            section.setInfo("Updated Info");
            section.setMaxCapacity(50);

            Section updated = DB.update(section).blockingGet();
            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Name", updated.getName(), "Should update name");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(50, updated.getMaxCapacity(), "Should update max capacity");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Section       section           = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            LocalDateTime createdAt         = section.getCreatedAt();
            String        createdBy         = section.getCreatedBy();
            Subject       originalSubject   = section.getSubject();
            Trimester     originalTrimester = section.getTrimester();

            section.setName("Updated Name");
            Section updated = DB.update(section).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
            assertEquals(originalSubject.getId(), updated.getSubject().getId(), "Should not change subject");
            assertEquals(originalTrimester.getId(), updated.getTrimester().getId(), "Should not change trimester");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Section saved = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Section found = DB.findById(Section.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing section");
            assertEquals(saved.getId(), found.getId(), "Should find correct section");
            assertEquals(saved.getName(), found.getName(), "Should maintain name");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            Section notFound = DB.findById(Section.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Section> sections = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                sections.add(TestHelper.saveEntity(TestHelper.createValidSectionWithRelations()));
            }

            List<Section> page = DB.read(Section.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing section")
        void deleteExisting() {
            Section saved = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            UUID    id    = saved.getId();

            DB.delete(Section.class, id).blockingAwait();
            Section deleted = DB.findById(Section.class, id).blockingGet();

            assertNull(deleted, "Should delete section");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Section.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent section");
        }

        @Test
        @DisplayName("Should handle deletion with registrations")
        void handleDeletionWithRegistrations() {
            Section section = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Student student = TestHelper.saveEntity(TestHelper.createValidStudent());

            // Create and save a course registration with REGISTERED status
            Course registration = TestHelper.createRegisteredCourse();
            registration.setSubject(section.getSubject());
            registration.setTrimester(section.getTrimester());
            registration.setStudent(student);
            registration.setSection(section);
            TestHelper.saveEntity(registration);

            // Try to delete section
            assertThrows(RuntimeException.class, () -> DB.delete(Section.class, section.getId())
                                                         .blockingAwait(), "Should prevent deletion of section with registrations");
        }
    }
}
