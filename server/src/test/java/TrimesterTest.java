import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.Trimester;
import enrollium.server.db.entity.types.Season;
import enrollium.server.db.entity.types.TrimesterStatus;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Trimester Entity Tests")
class TrimesterTest {
    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupTestData(Trimester.class);
    }

    @AfterAll
    static void finalCleanup() {
        TestHelper.cleanupTestData(Trimester.class);
    }

    @AfterEach
    void cleanup() {
        TestHelper.cleanupTestData(Trimester.class);
    }

    @Nested
    @DisplayName("Trimester Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create trimester with valid data")
        void createValidTrimester() {
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());

            assertNotNull(trimester.getId(), "Should generate ID");
            assertNotNull(trimester.getCreatedAt(), "Should set creation timestamp");
            assertTrue(trimester.getYear() >= 2003, "Year should be 2003 or later");
            assertNotNull(trimester.getSeason(), "Should have season");
            assertNotNull(trimester.getStatus(), "Should have status");
        }

        @Test
        @DisplayName("Should enforce code uniqueness")
        void enforceCodeUniqueness() {
            Trimester first = TestHelper.saveEntity(TestHelper.createTrimesterWithSpec(2030, Season.SPRING));

            Trimester duplicate = TestHelper.createTrimesterWithSpec(2030, Season.SPRING);
            assertThrows(RuntimeException.class, () -> TestHelper.saveEntity(duplicate), "Should reject duplicate code");
        }
    }


    @Nested
    @DisplayName("Code Validation Tests")
    class CodeValidationTests {
        @Test
        @DisplayName("Should validate code format")
        void validateCodeFormat() {
            // Test invalid last digit (4 is not valid)
            Trimester trimester = TestHelper.createTrimesterWithSpec(2030, Season.SPRING);
            trimester.setCode(304);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(trimester), "Should reject code not ending in 1, 2, or 3");

            // Test mismatched year
            Trimester mismatchYear = TestHelper.createTrimesterWithSpec(2030, Season.SPRING);
            mismatchYear.setCode(291);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(mismatchYear), "Should reject code not matching year");

            // Test mismatched season
            Trimester mismatchSeason = TestHelper.createTrimesterWithSpec(2030, Season.SPRING);
            mismatchSeason.setCode(302);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(mismatchSeason), "Should reject code not matching season");
        }

        @Test
        @DisplayName("Should accept valid code combinations")
        void acceptValidCodes() {
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithSpec(2030, Season.SPRING)));

            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithSpec(2031, Season.SUMMER)));

            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithSpec(2032, Season.FALL)));
        }
    }


    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {
        @Test
        @DisplayName("Should validate date ranges")
        void validateDateRanges() {
            // Create trimester with UPCOMING status and future dates
            Trimester     trimester = TestHelper.createValidTrimester();
            LocalDateTime future    = LocalDateTime.now().plusDays(10);

            trimester.setCourseSelectionStart(future);
            trimester.setCourseSelectionEnd(future.plusDays(1));
            trimester.setSectionRegistrationStart(future.plusDays(2));
            trimester.setSectionRegistrationEnd(future.plusDays(3));
            trimester.setStatus(TrimesterStatus.UPCOMING);

            assertDoesNotThrow(() -> TestHelper.saveEntity(trimester));
        }
    }


    @Nested
    @DisplayName("Status Validation Tests")
    class StatusValidationTests {
        private final LocalDateTime now = LocalDateTime.now();

        @Test
        @DisplayName("Should validate UPCOMING status")
        void validateUpcomingStatus() {
            // Valid case - course selection starts in future
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.UPCOMING)));

            // Invalid case - course selection started in past
            Trimester invalid = TestHelper.createTrimesterWithStatus(TrimesterStatus.UPCOMING);
            invalid.setCourseSelectionStart(LocalDateTime.now().minusDays(1));
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(invalid), "Should reject UPCOMING status when course selection has started");
        }

        @Test
        @DisplayName("Should validate COURSE_SELECTION status")
        void validateCourseSelectionStatus() {
            // Valid case - current time within course selection period
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.COURSE_SELECTION)), "Should allow COURSE_SELECTION when within selection period");

            // Invalid case - missing dates
            Trimester missingDates = TestHelper.createTrimesterWithStatus(TrimesterStatus.COURSE_SELECTION);
            missingDates.setCourseSelectionStart(null);
            missingDates.setCourseSelectionEnd(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(missingDates), "Should reject COURSE_SELECTION without selection dates");

            // Invalid case - current time before start
            Trimester beforeStart = TestHelper.createValidTrimester();
            beforeStart.setStatus(TrimesterStatus.COURSE_SELECTION);
            TestHelper.setTrimesterDates(beforeStart, now.plusDays(1),  // start in future
                    now.plusDays(2), now.plusDays(3), now.plusDays(4));
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(beforeStart), "Should reject COURSE_SELECTION when before start date");
        }

        @Test
        @DisplayName("Should validate SECTION_CREATION status")
        void validateSectionCreationStatus() {
            // Valid case
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.SECTION_CREATION)), "Should allow SECTION_CREATION when between course selection and registration");

            // Invalid case - missing dates
            Trimester missingDates = TestHelper.createTrimesterWithStatus(TrimesterStatus.SECTION_CREATION);
            missingDates.setCourseSelectionEnd(null);
            missingDates.setSectionRegistrationStart(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(missingDates), "Should reject SECTION_CREATION without required dates");
        }

        @Test
        @DisplayName("Should validate SECTION_SELECTION status")
        void validateSectionSelectionStatus() {
            // Valid case
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.SECTION_SELECTION)), "Should allow SECTION_SELECTION when within registration period");

            // Invalid case - missing dates
            Trimester missingDates = TestHelper.createTrimesterWithStatus(TrimesterStatus.SECTION_SELECTION);
            missingDates.setSectionRegistrationStart(null);
            missingDates.setSectionRegistrationEnd(null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(missingDates), "Should reject SECTION_SELECTION without registration dates");
        }

        @Test
        @DisplayName("Should validate ONGOING and COMPLETED status")
        void validateFinalStatuses() {
            // Valid cases
            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.ONGOING)), "Should allow ONGOING when registration has ended");

            assertDoesNotThrow(() -> TestHelper.saveEntity(TestHelper.createTrimesterWithStatus(TrimesterStatus.COMPLETED)), "Should allow COMPLETED when registration has ended");

            // Invalid case - registration hasn't ended
            Trimester notEnded = TestHelper.createValidTrimester();
            notEnded.setStatus(TrimesterStatus.ONGOING);
            TestHelper.setTrimesterDates(notEnded, now.minusDays(3), now.minusDays(2), now.minusDays(1), now.plusDays(1)  // registration ends in future
            );
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(notEnded), "Should reject ONGOING when registration hasn't ended");
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Trimester trimester       = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Long      originalVersion = trimester.getVersion();

            // Keep UPCOMING status but update dates properly
            LocalDateTime future = LocalDateTime.now().plusDays(20);
            trimester.setCourseSelectionStart(future);
            trimester.setCourseSelectionEnd(future.plusDays(1));
            trimester.setSectionRegistrationStart(future.plusDays(2));
            trimester.setSectionRegistrationEnd(future.plusDays(3));
            trimester.setInfo("Updated Info");

            Trimester updated = DB.update(trimester).blockingGet();
            assertTrue(updated.getVersion() > originalVersion);
            assertEquals("Updated Info", updated.getInfo());
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Trimester     trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());
            LocalDateTime createdAt = trimester.getCreatedAt();
            String        createdBy = trimester.getCreatedBy();

            trimester.setInfo("Updated Info");
            Trimester updated = DB.update(trimester).blockingGet();

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
            Trimester saved = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Trimester found = DB.findById(Trimester.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing trimester");
            assertEquals(saved.getId(), found.getId(), "Should find correct trimester");
            assertEquals(saved.getCode(), found.getCode(), "Should maintain code");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Trimester> trimesters = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                trimesters.add(TestHelper.saveEntity(TestHelper.createValidTrimester()));
            }

            List<Trimester> page = DB.read(Trimester.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing trimester")
        void deleteExisting() {
            Trimester saved = TestHelper.saveEntity(TestHelper.createValidTrimester());
            UUID      id    = saved.getId();

            DB.delete(Trimester.class, id).blockingAwait();
            Trimester deleted = DB.findById(Trimester.class, id).blockingGet();

            assertNull(deleted, "Should delete trimester");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Trimester.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent trimester");
        }
    }
}
