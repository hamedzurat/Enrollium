import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.SpaceTime;
import enrollium.server.db.entity.types.SubjectType;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.PropertyValueException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("SpaceTime Entity Tests")
class SpaceTimeTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(SpaceTime.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(SpaceTime.class);
    }

    @Nested
    @DisplayName("SpaceTime Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create theory room with valid data")
        void createValidTheoryRoom() {
            SpaceTime spaceTime = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());

            assertNotNull(spaceTime.getId(), "Should generate ID");
            assertNotNull(spaceTime.getCreatedAt(), "Should set creation timestamp");
            assertEquals(SubjectType.THEORY, spaceTime.getRoomType(), "Should be THEORY type");
            assertTrue(spaceTime.getTimeSlot() >= 1 && spaceTime.getTimeSlot() <= 6, "Theory room time slot should be between 1 and 6");
        }

        @Test
        @DisplayName("Should create lab room with valid data")
        void createValidLabRoom() {
            SpaceTime spaceTime = TestHelper.saveEntity(TestHelper.createValidLabSpaceTime());

            assertNotNull(spaceTime.getId(), "Should generate ID");
            assertNotNull(spaceTime.getCreatedAt(), "Should set creation timestamp");
            assertEquals(SubjectType.LAB, spaceTime.getRoomType(), "Should be LAB type");
            assertTrue(spaceTime.getTimeSlot() >= 1 && spaceTime.getTimeSlot() <= 3, "Lab room time slot should be between 1 and 3");
        }
    }


    @Nested
    @DisplayName("Time Slot Validation Tests")
    class TimeSlotTests {
        @Test
        @DisplayName("Should validate theory room time slots")
        void validateTheoryTimeSlots() {
            // Valid slots
            for (int slot = 1; slot <= 6; slot++) {
                final int timeSlot = slot;
                assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.THEORY, timeSlot)), "Should accept valid theory time slot: " + slot);
            }

            // Invalid slots
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.THEORY, 0)), "Should reject time slot below 1");
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.THEORY, 7)), "Should reject time slot above 6");
        }

        @Test
        @DisplayName("Should validate lab room time slots")
        void validateLabTimeSlots() {
            // Valid slots
            for (int slot = 1; slot <= 3; slot++) {
                final int timeSlot = slot;
                assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.LAB, timeSlot)), "Should accept valid lab time slot: " + slot);
            }

            // Invalid slots
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.LAB, 0)), "Should reject time slot below 1");
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(TestHelper.createSpaceTimeWithSlot(SubjectType.LAB, 4)), "Should reject time slot above 3");
        }
    }


    @Nested
    @DisplayName("Uniqueness Constraint Tests")
    class UniquenessTests {
        @Test
        @DisplayName("Should enforce room schedule uniqueness")
        void enforceScheduleUniqueness() {
            // First space-time
            SpaceTime first = TestHelper.createValidTheorySpaceTime();
            first.setRoomNumber("101");
            first.setDayOfWeek(DayOfWeek.MONDAY);
            first.setTimeSlot(1);
            TestHelper.saveEntity(first);

            // Try to create another space-time with same room number, day, and time slot
            SpaceTime duplicate = TestHelper.createValidTheorySpaceTime();
            duplicate.setRoomNumber("101");  // Same room number
            duplicate.setDayOfWeek(DayOfWeek.MONDAY);  // Same day
            duplicate.setTimeSlot(1);  // Same time slot

            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(duplicate), "Should reject duplicate room schedule");
        }

        @Test
        @DisplayName("Should allow same room at different times")
        void allowSameRoomDifferentTime() {
            SpaceTime first = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());

            // Same room number, different time slot
            SpaceTime sameRoomDifferentTime = TestHelper.createValidTheorySpaceTime();
            sameRoomDifferentTime.setRoomNumber(first.getRoomNumber());
            sameRoomDifferentTime.setDayOfWeek(first.getDayOfWeek());
            sameRoomDifferentTime.setTimeSlot(first.getTimeSlot() + 1);

            assertDoesNotThrow(() -> TestHelper.saveEntity(sameRoomDifferentTime), "Should allow same room at different time slots");
        }

        @Test
        @DisplayName("Should allow different rooms at same time")
        void allowDifferentRoomSameTime() {
            SpaceTime first = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());

            // Different room number, same time slot
            SpaceTime differentRoomSameTime = TestHelper.createValidTheorySpaceTime();
            // Room number will be different by default from TestHelper
            differentRoomSameTime.setDayOfWeek(first.getDayOfWeek());
            differentRoomSameTime.setTimeSlot(first.getTimeSlot());

            assertDoesNotThrow(() -> TestHelper.saveEntity(differentRoomSameTime), "Should allow different rooms at same time");
        }
    }


    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {
        @Test
        @DisplayName("Should validate room name")
        void validateRoomName() {
            // Test null name
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setName(null), ConstraintViolationException.class);

            // Test blank name
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setName("   "), ConstraintViolationException.class);

            // Test oversized name
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setName("A".repeat(101)), ConstraintViolationException.class);
        }

        @Test
        @DisplayName("Should validate room number")
        void validateRoomNumber() {
            // Test null room number
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setRoomNumber(null), ConstraintViolationException.class);

            // Test blank room number
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setRoomNumber("   "), ConstraintViolationException.class);

            // Test oversized room number
            TestHelper.assertValidationFails(TestHelper.createValidTheorySpaceTime(), st -> st.setRoomNumber("R".repeat(9)), ConstraintViolationException.class);
        }

        @ParameterizedTest
        @EnumSource(DayOfWeek.class)
        @DisplayName("Should accept all days of week")
        void validateDayOfWeek(DayOfWeek day) {
            SpaceTime spaceTime = TestHelper.createValidTheorySpaceTime();
            spaceTime.setDayOfWeek(day);
            assertDoesNotThrow(() -> TestHelper.saveEntity(spaceTime), "Should accept " + day + " as valid day");
        }

        @Test
        @DisplayName("Should validate time slot")
        void validateTimeSlot() {
            // Test null time slot - catches our custom validation first
            SpaceTime spaceTime = TestHelper.createValidTheorySpaceTime();
            assertThrows(IllegalArgumentException.class, () -> {
                spaceTime.setTimeSlot(null);
                TestHelper.saveEntity(spaceTime);
            }, "Should reject null time slot");

            // Test invalid time slots for THEORY rooms - @Min/@Max validation
            SpaceTime theoryRoom = TestHelper.createValidTheorySpaceTime();
            assertThrows(IllegalArgumentException.class, () -> {
                theoryRoom.setTimeSlot(7);
                TestHelper.saveEntity(theoryRoom);
            }, "Should reject time slot above 6 for theory rooms");

            assertThrows(PropertyValueException.class, () -> {
                theoryRoom.setTimeSlot(0);
                TestHelper.saveEntity(theoryRoom);
            }, "Should reject time slot below 1 for theory rooms");

            // Test valid time slots for THEORY rooms
            for (int slot = 1; slot <= 6; slot++) {
                final int timeSlot    = slot;
                SpaceTime validTheory = TestHelper.createValidTheorySpaceTime();
                assertDoesNotThrow(() -> {
                    validTheory.setTimeSlot(timeSlot);
                    TestHelper.saveEntity(validTheory);
                }, "Should accept valid theory time slot: " + slot);
            }

            // Test invalid time slots for LAB rooms - our custom validation
            SpaceTime labRoom = TestHelper.createValidLabSpaceTime();
            assertThrows(IllegalArgumentException.class, () -> {
                labRoom.setTimeSlot(4);
                TestHelper.saveEntity(labRoom);
            }, "Should reject time slot above 3 for lab rooms");

            assertThrows(PropertyValueException.class, () -> {
                labRoom.setTimeSlot(0);
                TestHelper.saveEntity(labRoom);
            }, "Should reject time slot below 1 for lab rooms");

            // Test valid time slots for LAB rooms
            for (int slot = 1; slot <= 3; slot++) {
                final int timeSlot = slot;
                SpaceTime validLab = TestHelper.createValidLabSpaceTime();
                assertDoesNotThrow(() -> {
                    validLab.setTimeSlot(timeSlot);
                    TestHelper.saveEntity(validLab);
                }, "Should accept valid lab time slot: " + slot);
            }
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            SpaceTime spaceTime       = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());
            Long      originalVersion = spaceTime.getVersion();

            spaceTime.setName("Updated Room");
            spaceTime.setInfo("Updated Info");
            spaceTime.setTimeSlot(2);

            SpaceTime updated = DB.update(spaceTime).blockingGet();
            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Room", updated.getName(), "Should update name");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
            assertEquals(2, updated.getTimeSlot(), "Should update time slot");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            SpaceTime     spaceTime = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());
            LocalDateTime createdAt = spaceTime.getCreatedAt();
            String        createdBy = spaceTime.getCreatedBy();

            spaceTime.setName("Updated Room");
            SpaceTime updated = DB.update(spaceTime).blockingGet();

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
            SpaceTime saved = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());
            SpaceTime found = DB.findById(SpaceTime.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing space-time");
            assertEquals(saved.getId(), found.getId(), "Should find correct space-time");
            assertEquals(saved.getName(), found.getName(), "Should maintain name");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            SpaceTime notFound = DB.findById(SpaceTime.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<SpaceTime> spaceTimes = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                spaceTimes.add(TestHelper.saveEntity(TestHelper.createValidSpaceTime()));
            }

            List<SpaceTime> page = DB.read(SpaceTime.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing space-time")
        void deleteExisting() {
            SpaceTime saved = TestHelper.saveEntity(TestHelper.createValidTheorySpaceTime());
            UUID      id    = saved.getId();

            DB.delete(SpaceTime.class, id).blockingAwait();
            SpaceTime deleted = DB.findById(SpaceTime.class, id).blockingGet();

            assertNull(deleted, "Should delete space-time");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(SpaceTime.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent space-time");
        }
    }
}
