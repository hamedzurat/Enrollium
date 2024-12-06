import enrollium.server.TestHelper;
import enrollium.server.db.DB;
import enrollium.server.db.entity.*;
import enrollium.server.db.entity.types.NotificationCategory;
import enrollium.server.db.entity.types.NotificationScope;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Notification Entity Tests")
class NotificationTest {
    private static Faculty defaultSender;

    @BeforeAll
    static void setupDatabase() {
        try (var session = DB.getSessionFactory().openSession()) {
            assertTrue(session.isConnected(), "Database should be connected");
        }
        TestHelper.cleanupAllTestData();
        defaultSender = TestHelper.saveEntity(TestHelper.createValidFaculty());
    }

    @AfterAll
    static void finalCleanup() {
        // Clean notifications first due to foreign key constraints
        TestHelper.cleanupTestData(Notification.class);
        TestHelper.cleanupAllTestData();
    }

    @Nested
    @DisplayName("Notification Creation Tests")
    class CreationTests {
        @Test
        @DisplayName("Should create global notification")
        void createGlobalNotification() {
            Notification notification = TestHelper.createGlobalNotification(defaultSender);
            Notification saved        = TestHelper.saveEntity(notification);

            assertNotNull(saved.getId(), "Should generate ID");
            assertNotNull(saved.getCreatedAt(), "Should set creation timestamp");
            assertEquals(NotificationScope.GLOBAL, saved.getScope(), "Should be GLOBAL scope");
            assertNull(saved.getTrimester(), "Global notification should not have trimester");
            assertNull(saved.getSection(), "Global notification should not have section");
            assertNull(saved.getTargetUser(), "Global notification should not have target user");
        }

        @Test
        @DisplayName("Should create trimester notification")
        void createTrimesterNotification() {
            Trimester    trimester    = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Notification notification = TestHelper.createTrimesterNotification(defaultSender, trimester);
            Notification saved        = TestHelper.saveEntity(notification);

            assertEquals(NotificationScope.TRIMESTER, saved.getScope(), "Should be TRIMESTER scope");
            assertNotNull(saved.getTrimester(), "Trimester notification should have trimester");
            assertNull(saved.getSection(), "Trimester notification should not have section");
            assertNull(saved.getTargetUser(), "Trimester notification should not have target user");
        }

        @Test
        @DisplayName("Should create section notification")
        void createSectionNotification() {
            Section      section      = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Notification notification = TestHelper.createSectionNotification(defaultSender, section);
            Notification saved        = TestHelper.saveEntity(notification);

            assertEquals(NotificationScope.SECTION, saved.getScope(), "Should be SECTION scope");
            assertNull(saved.getTrimester(), "Section notification should not have trimester");
            assertNotNull(saved.getSection(), "Section notification should have section");
            assertNull(saved.getTargetUser(), "Section notification should not have target user");
        }

        @Test
        @DisplayName("Should create user notification")
        void createUserNotification() {
            Student      targetUser   = TestHelper.saveEntity(TestHelper.createValidStudent());
            Notification notification = TestHelper.createUserNotification(defaultSender, targetUser);
            Notification saved        = TestHelper.saveEntity(notification);

            assertEquals(NotificationScope.USER, saved.getScope(), "Should be USER scope");
            assertNull(saved.getTrimester(), "User notification should not have trimester");
            assertNull(saved.getSection(), "User notification should not have section");
            assertNotNull(saved.getTargetUser(), "User notification should have target user");
        }
    }


    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {
        @Test
        @DisplayName("Should validate title length")
        void validateTitleLength() {
            // Test null title
            Notification nullTitle = TestHelper.createGlobalNotification(defaultSender);
            nullTitle.setTitle(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(nullTitle));

            // Test blank title
            Notification blankTitle = TestHelper.createGlobalNotification(defaultSender);
            blankTitle.setTitle("   ");
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(blankTitle));

            // Test oversized title
            Notification oversizedTitle = TestHelper.createGlobalNotification(defaultSender);
            oversizedTitle.setTitle("A".repeat(129));
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(oversizedTitle));

            // Test maximum allowed title
            Notification maxTitle = TestHelper.createGlobalNotification(defaultSender);
            maxTitle.setTitle("A".repeat(128));
            assertDoesNotThrow(() -> TestHelper.saveEntity(maxTitle));
        }

        @Test
        @DisplayName("Should validate content length")
        void validateContentLength() {
            // Test null content
            Notification nullContent = TestHelper.createGlobalNotification(defaultSender);
            nullContent.setContent(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(nullContent));

            // Test blank content
            Notification blankContent = TestHelper.createGlobalNotification(defaultSender);
            blankContent.setContent("   ");
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(blankContent));

            // Test oversized content
            Notification oversizedContent = TestHelper.createGlobalNotification(defaultSender);
            oversizedContent.setContent("A".repeat(4001));
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(oversizedContent));

            // Test maximum allowed content
            Notification maxContent = TestHelper.createGlobalNotification(defaultSender);
            maxContent.setContent("A".repeat(4000));
            assertDoesNotThrow(() -> TestHelper.saveEntity(maxContent));
        }
    }


    @Nested
    @DisplayName("Scope Validation Tests")
    class ScopeValidationTests {
        @Test
        @DisplayName("Should validate GLOBAL scope constraints")
        void validateGlobalScope() {
            Trimester trimester  = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Section   section    = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Student   targetUser = TestHelper.saveEntity(TestHelper.createValidStudent());

            // Try to add trimester
            Notification withTrimester = TestHelper.createGlobalNotification(defaultSender);
            withTrimester.setTrimester(trimester);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withTrimester));

            // Try to add section
            Notification withSection = TestHelper.createGlobalNotification(defaultSender);
            withSection.setSection(section);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withSection));

            // Try to add target user
            Notification withUser = TestHelper.createGlobalNotification(defaultSender);
            withUser.setTargetUser(targetUser);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withUser));
        }

        @Test
        @DisplayName("Should validate TRIMESTER scope constraints")
        void validateTrimesterScope() {
            Trimester trimester  = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Section   section    = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Student   targetUser = TestHelper.saveEntity(TestHelper.createValidStudent());

            // Test missing trimester
            Notification noTrimester = TestHelper.createTrimesterNotification(defaultSender, null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noTrimester));

            // Test with section
            Notification withSection = TestHelper.createTrimesterNotification(defaultSender, trimester);
            withSection.setSection(section);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withSection));

            // Test with target user
            Notification withUser = TestHelper.createTrimesterNotification(defaultSender, trimester);
            withUser.setTargetUser(targetUser);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withUser));
        }

        @Test
        @DisplayName("Should validate SECTION scope constraints")
        void validateSectionScope() {
            Section   section    = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Trimester trimester  = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Student   targetUser = TestHelper.saveEntity(TestHelper.createValidStudent());

            // Test missing section
            Notification noSection = TestHelper.createSectionNotification(defaultSender, null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noSection));

            // Test with trimester
            Notification withTrimester = TestHelper.createSectionNotification(defaultSender, section);
            withTrimester.setTrimester(trimester);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withTrimester));

            // Test with target user
            Notification withUser = TestHelper.createSectionNotification(defaultSender, section);
            withUser.setTargetUser(targetUser);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withUser));
        }

        @Test
        @DisplayName("Should validate USER scope constraints")
        void validateUserScope() {
            Student   targetUser = TestHelper.saveEntity(TestHelper.createValidStudent());
            Trimester trimester  = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Section   section    = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());

            // Test missing target user
            Notification noUser = TestHelper.createUserNotification(defaultSender, null);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(noUser));

            // Test with trimester
            Notification withTrimester = TestHelper.createUserNotification(defaultSender, targetUser);
            withTrimester.setTrimester(trimester);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withTrimester));

            // Test with section
            Notification withSection = TestHelper.createUserNotification(defaultSender, targetUser);
            withSection.setSection(section);
            assertThrows(IllegalArgumentException.class, () -> TestHelper.saveEntity(withSection));
        }
    }


    @Nested
    @DisplayName("Category Tests")
    class CategoryTests {
        @ParameterizedTest
        @EnumSource(NotificationCategory.class)
        @DisplayName("Should create notifications with different categories")
        void createWithDifferentCategories(NotificationCategory category) {
            Notification notification = TestHelper.createNotificationWithCategory(defaultSender, category);
            Notification saved        = TestHelper.saveEntity(notification);
            assertEquals(category, saved.getCategory(), "Should save notification with category: " + category);
        }

        @Test
        @DisplayName("Should reject null category")
        void rejectNullCategory() {
            Notification notification = TestHelper.createGlobalNotification(defaultSender);
            notification.setCategory(null);
            assertThrows(ConstraintViolationException.class, () -> TestHelper.saveEntity(notification));
        }
    }


    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update modifiable fields")
        void updateModifiableFields() {
            Notification notification    = TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender));
            Long         originalVersion = notification.getVersion();

            notification.setTitle("Updated Title");
            notification.setContent("Updated Content");
            notification.setInfo("Updated Info");

            Notification updated = DB.update(notification).blockingGet();
            assertTrue(updated.getVersion() > originalVersion, "Should increment version");
            assertEquals("Updated Title", updated.getTitle(), "Should update title");
            assertEquals("Updated Content", updated.getContent(), "Should update content");
            assertEquals("Updated Info", updated.getInfo(), "Should update info");
        }

        @Test
        @DisplayName("Should maintain immutable fields")
        void maintainImmutableFields() {
            Notification      notification  = TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender));
            LocalDateTime     createdAt     = notification.getCreatedAt();
            String            createdBy     = notification.getCreatedBy();
            NotificationScope originalScope = notification.getScope();

            notification.setTitle("Updated Title");
            Notification updated = DB.update(notification).blockingGet();

            assertEquals(createdAt, updated.getCreatedAt(), "Should not change creation timestamp");
            assertEquals(createdBy, updated.getCreatedBy(), "Should not change created by");
            assertEquals(originalScope, updated.getScope(), "Should not change scope");
            assertEquals(defaultSender.getId(), updated.getSender().getId(), "Should not change sender");
        }
    }


    @Nested
    @DisplayName("Query Operation Tests")
    class QueryTests {
        @Test
        @DisplayName("Should find by ID")
        void findById() {
            Notification saved = TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender));
            Notification found = DB.findById(Notification.class, saved.getId()).blockingGet();

            assertNotNull(found, "Should find existing notification");
            assertEquals(saved.getId(), found.getId(), "Should find correct notification");
            assertEquals(saved.getTitle(), found.getTitle(), "Should maintain title");
            assertEquals(saved.getScope(), found.getScope(), "Should maintain scope");
        }

        @Test
        @DisplayName("Should handle non-existent ID")
        void handleNonExistentId() {
            Notification notFound = DB.findById(Notification.class, UUID.randomUUID()).blockingGet();
            assertNull(notFound, "Should return null for non-existent ID");
        }

        @Test
        @DisplayName("Should paginate results")
        void paginateResults() {
            List<Notification> notifications = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                notifications.add(TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender)));
            }

            List<Notification> page = DB.read(Notification.class, 3, 0).toList().blockingGet();
            assertEquals(3, page.size(), "Should return requested page size");
        }

        @Test
        @DisplayName("Should sort by creation date")
        void sortByCreationDate() {
            // Clear existing test notifications
            TestHelper.cleanupTestData(Notification.class);

            List<String> contentOrder = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                Notification notification = TestHelper.createGlobalNotification(defaultSender);
                notification.setContent("Content " + i); // Use content to verify order
                TestHelper.saveEntity(notification);
                contentOrder.add(notification.getContent());

                // Ensure next notification has a different timestamp
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // Test ascending order (oldest first)
            List<Notification> ascending = DB.read(Notification.class, "createdAt", true, 10, 0)
                                             .filter(n -> n.getCreatedBy().equals(TestHelper.TEST_CREATOR))
                                             .toList()
                                             .blockingGet();

            assertEquals(contentOrder.get(0), ascending.get(0)
                                                       .getContent(), "Should return oldest notification first when sorting ascending");
            assertEquals(contentOrder.get(2), ascending.get(2)
                                                       .getContent(), "Should return newest notification last when sorting ascending");

            // Test descending order (newest first)
            List<Notification> descending = DB.read(Notification.class, "createdAt", false, 10, 0)
                                              .filter(n -> n.getCreatedBy().equals(TestHelper.TEST_CREATOR))
                                              .toList()
                                              .blockingGet();

            assertEquals(contentOrder.get(2), descending.get(0)
                                                        .getContent(), "Should return newest notification first when sorting descending");
            assertEquals(contentOrder.get(0), descending.get(2)
                                                        .getContent(), "Should return oldest notification last when sorting descending");
        }
    }


    @Nested
    @DisplayName("Deletion Tests")
    class DeletionTests {
        @Test
        @DisplayName("Should delete existing notification")
        void deleteExisting() {
            Notification saved = TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender));
            UUID         id    = saved.getId();

            DB.delete(Notification.class, id).blockingAwait();
            Notification deleted = DB.findById(Notification.class, id).blockingGet();

            assertNull(deleted, "Should delete notification");
        }

        @Test
        @DisplayName("Should handle non-existent deletion")
        void handleNonExistentDeletion() {
            assertThrows(RuntimeException.class, () -> DB.delete(Notification.class, UUID.randomUUID())
                                                         .blockingAwait(), "Should throw exception for non-existent notification");
        }
    }


    @Nested
    @DisplayName("State Change Tests")
    class StateChangeTests {
        @Test
        @DisplayName("Should prevent adding scope-specific fields after creation")
        void preventAddingScopeFields() {
            // Create a global notification
            Notification notification = TestHelper.saveEntity(TestHelper.createGlobalNotification(defaultSender));

            // Try to add trimester to existing global notification
            Trimester trimester = TestHelper.saveEntity(TestHelper.createValidTrimester());
            notification.setTrimester(trimester);

            Notification finalNotification = notification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalNotification)
                                                                 .blockingGet(), "Should not allow adding trimester to global notification");

            // Try to add section to existing global notification
            notification = DB.findById(Notification.class, notification.getId()).blockingGet();
            Section section = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            notification.setSection(section);

            Notification finalNotification2 = notification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalNotification2)
                                                                 .blockingGet(), "Should not allow adding section to global notification");

            // Try to add target user to existing global notification
            notification = DB.findById(Notification.class, notification.getId()).blockingGet();
            Student targetUser = TestHelper.saveEntity(TestHelper.createValidStudent());
            notification.setTargetUser(targetUser);

            Notification finalNotification3 = notification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalNotification3)
                                                                 .blockingGet(), "Should not allow adding target user to global notification");
        }

        @Test
        @DisplayName("Should prevent removing required scope fields")
        void preventRemovingRequiredFields() {
            // Test TRIMESTER scope
            Trimester    trimester             = TestHelper.saveEntity(TestHelper.createValidTrimester());
            Notification trimesterNotification = TestHelper.saveEntity(TestHelper.createTrimesterNotification(defaultSender, trimester));

            trimesterNotification.setTrimester(null);
            Notification finalTrimesterNotification = trimesterNotification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalTrimesterNotification)
                                                                 .blockingGet(), "Should not allow removing required trimester");

            // Test SECTION scope
            Section      section             = TestHelper.saveEntity(TestHelper.createValidSectionWithRelations());
            Notification sectionNotification = TestHelper.saveEntity(TestHelper.createSectionNotification(defaultSender, section));

            sectionNotification.setSection(null);
            Notification finalSectionNotification = sectionNotification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalSectionNotification)
                                                                 .blockingGet(), "Should not allow removing required section");

            // Test USER scope
            Student      targetUser       = TestHelper.saveEntity(TestHelper.createValidStudent());
            Notification userNotification = TestHelper.saveEntity(TestHelper.createUserNotification(defaultSender, targetUser));

            userNotification.setTargetUser(null);
            Notification finalUserNotification = userNotification;
            assertThrows(IllegalArgumentException.class, () -> DB.update(finalUserNotification)
                                                                 .blockingGet(), "Should not allow removing required target user");
        }
    }
}
