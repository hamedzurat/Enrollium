import enrollium.server.db.DB;
import enrollium.server.db.entity.Student;
import enrollium.server.db.entity.types.UserType;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Comprehensive test suite for Student entity.
 * Tests all aspects including:
 * - Basic CRUD operations
 * - Field validations
 * - Constraints
 * - Edge cases
 * - Base entity functionality
 * - Student-specific business rules
 */
public class StudentTest {
    private static final int BASE_UNIVERSITY_ID = 1000;

    /**
     * Helper method to create a valid student for testing
     */
    private Student createValidStudent(int index) {
        Student student = new Student();
        student.setName("Test Student " + (index < 10 ? "0" + index : index));
        student.setEmail("test" + System.currentTimeMillis() + "_" + index + "@uiu.ac.bd");
        student.setType(UserType.STUDENT);
        student.setUniversityId(BASE_UNIVERSITY_ID + index + (int) (System.currentTimeMillis() % 10000));
        student.setInfo("Test info about student " + index);
        student.setCreatedBy("test-suite");
        student.setUpdatedBy("test-suite");
        return student;
    }

    /**
     * Test database connectivity before running any tests
     */
    @Test
    void testDatabaseConnection() {
        try (Session session = DB.getSessionFactory().openSession()) {
            assertNotNull(session, "Session should not be null");
            assertTrue(session.isConnected(), "Session should be connected");
        }
    }

    /**
     * Test successful student creation with all valid fields
     */
    @Test
    void testSuccessfulStudentCreation() {
        Student student      = createValidStudent(1);
        Student savedStudent = DB.save(student).blockingGet();

        assertNotNull(savedStudent.getId(), "Student should have an ID after saving");
        assertEquals(student.getName(), savedStudent.getName(), "Names should match");
        assertEquals(student.getEmail(), savedStudent.getEmail(), "Emails should match");
        assertEquals(student.getUniversityId(), savedStudent.getUniversityId(), "University IDs should match");

        DB.delete(Student.class, savedStudent.getId()).blockingAwait();
    }

    /**
     * Test UserType validation
     */
    @Test
    void testUserTypeValidation() {
        Student student = createValidStudent(1);

        // Test null type
        student.setType(null);
        assertThrows(ConstraintViolationException.class, () -> DB.save(student)
                                                                 .blockingGet(), "Should not save student with null user type");

        // Test non-student type
        student.setType(UserType.ADMIN);
        assertThrows(RuntimeException.class, () -> DB.save(student)
                                                     .blockingGet(), "Should not save student with non-STUDENT user type");
    }

    /**
     * Test that the courses collection is properly initialized
     */
    @Test
    void testCoursesCollection() {
        Student student = DB.save(createValidStudent(1)).blockingGet();

        assertNotNull(student.getCourses(), "Courses set should be initialized");
        assertTrue(student.getCourses().isEmpty(), "New student should have no courses");

        DB.delete(Student.class, student.getId()).blockingAwait();
    }

    /**
     * Test email format validation
     */
    @ParameterizedTest
    @ValueSource(strings = {"invalid-email", "@uiu.ac.bd", "test@", "test@uiu.", " @uiu.ac.bd", "test@uiu.ac.bd "})
    void testInvalidEmailValidation(String invalidEmail) {
        Student student = createValidStudent(1);
        student.setEmail(invalidEmail);
        assertThrows(ConstraintViolationException.class, () -> DB.save(student)
                                                                 .blockingGet(), "Should not save student with invalid email: " + invalidEmail);
    }

    /**
     * Test special characters in email
     */
    @ParameterizedTest
    @ValueSource(strings = {"test.name+label@uiu.ac.bd", "test.name-label@uiu.ac.bd", "test_name@uiu.ac.bd", "test.name1234@uiu.ac.bd", "TEST.NAME@uiu.ac.bd"})
    void testSpecialCharactersInEmail(String email) {
        Student student = createValidStudent(1);
        student.setEmail(email);

        Student saved = DB.save(student).blockingGet();
        assertNotNull(saved.getId(), "Should save student with valid special characters in email");
        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test email length validation
     */
    @Test
    void testEmailLengthValidation() {
        Student student      = createValidStudent(1);
        String  tooLongEmail = "a".repeat(60) + "@uiu.ac.bd"; // 65+ characters
        student.setEmail(tooLongEmail);

        assertThrows(ConstraintViolationException.class, () -> DB.save(student)
                                                                 .blockingGet(), "Should not save student with email longer than 64 characters");
    }

    /**
     * Test whitespace handling in fields
     */
    @Test
    void testWhitespaceHandling() {
        // Test name with multiple spaces
        Student studentSpaceName = createValidStudent(1);
        studentSpaceName.setName("   Test    Student    ");
        Student saved = DB.save(studentSpaceName).blockingGet();
        assertEquals("   Test    Student    ", saved.getName(), "Spaces in name should be preserved");

        // Test email with spaces
        Student studentSpaceEmail = createValidStudent(2);
        studentSpaceEmail.setEmail(" test@uiu.ac.bd ");
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentSpaceEmail)
                                                                 .blockingGet(), "Should not save student with spaces in email");

        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test name field validation
     */
    @Test
    void testNameValidation() {
        // Test null name
        Student studentNullName = createValidStudent(1);
        studentNullName.setName(null);
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentNullName)
                                                                 .blockingGet(), "Should not save student with null name");

        // Test empty name
        Student studentEmptyName = createValidStudent(2);
        studentEmptyName.setName("");
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentEmptyName)
                                                                 .blockingGet(), "Should not save student with empty name");

        // Test too long name
        Student studentLongName = createValidStudent(3);
        studentLongName.setName("a".repeat(101));
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentLongName)
                                                                 .blockingGet(), "Should not save student with name longer than 100 characters");
    }

    /**
     * Test universityId validation
     */
    @Test
    void testUniversityIdValidation() {
        // Test null university ID
        Student studentNullId = createValidStudent(1);
        studentNullId.setUniversityId(null);
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentNullId)
                                                                 .blockingGet(), "Should not save student with null university ID");

        // Test negative university ID
        Student studentNegativeId = createValidStudent(2);
        studentNegativeId.setUniversityId(-1);
        assertThrows(ConstraintViolationException.class, () -> DB.save(studentNegativeId)
                                                                 .blockingGet(), "Should not save student with negative university ID");

        // Test zero university ID (should be valid)
        Student studentZeroId = createValidStudent(3);
        studentZeroId.setUniversityId(0);
        Student saved = DB.save(studentZeroId).blockingGet();
        assertNotNull(saved.getId(), "Should save student with university ID of 0");
        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test email uniqueness constraint
     */
    @Test
    void testEmailUniqueness() {
        Student student1 = DB.save(createValidStudent(1)).blockingGet();

        Student student2 = createValidStudent(2);
        student2.setEmail(student1.getEmail());

        assertThrows(RuntimeException.class, () -> DB.save(student2)
                                                     .blockingGet(), "Should not save student with duplicate email");

        DB.delete(Student.class, student1.getId()).blockingAwait();
    }

    /**
     * Test universityId uniqueness constraint
     */
    @Test
    void testUniversityIdUniqueness() {
        Student student1 = DB.save(createValidStudent(1)).blockingGet();

        Student student2 = createValidStudent(2);
        student2.setUniversityId(student1.getUniversityId());

        assertThrows(RuntimeException.class, () -> DB.save(student2)
                                                     .blockingGet(), "Should not save student with duplicate university ID");

        DB.delete(Student.class, student1.getId()).blockingAwait();
    }

    /**
     * Test info field handling
     */
    @Test
    void testInfoFieldHandling() {
        // Test null info
        Student studentNullInfo = createValidStudent(1);
        studentNullInfo.setInfo(null);
        Student saved1 = DB.save(studentNullInfo).blockingGet();
        assertNull(saved1.getInfo(), "Should allow null info");
        DB.delete(Student.class, saved1.getId()).blockingAwait();

        // Test long info
        Student studentLongInfo = createValidStudent(2);
        studentLongInfo.setInfo("a".repeat(1000));
        Student saved2 = DB.save(studentLongInfo).blockingGet();
        assertEquals(1000, saved2.getInfo().length(), "Should allow long info text");
        DB.delete(Student.class, saved2.getId()).blockingAwait();
    }

    /**
     * Test update operations and version control
     */
    @Test
    void testUpdateAndVersioning() {
        Student original        = DB.save(createValidStudent(1)).blockingGet();
        Long    originalVersion = original.getVersion();

        original.setName("Updated Name");
        Student updated = DB.update(original).blockingGet();

        assertTrue(updated.getVersion() > originalVersion, "Version should increment after update");
        assertEquals("Updated Name", updated.getName(), "Name should be updated");
        assertEquals(original.getEmail(), updated.getEmail(), "Unmodified fields should remain the same");

        DB.delete(Student.class, updated.getId()).blockingAwait();
    }

    /**
     * Test base entity fields
     */
    @Test
    void testBaseEntityFields() {
        Student student = createValidStudent(1);
        student.setCreatedBy(null); // Test nullable audit fields
        student.setUpdatedBy(null);

        Student saved = DB.save(student).blockingGet();
        assertNotNull(saved.getCreatedAt(), "CreatedAt should be auto-set");
        assertNotNull(saved.getUpdatedAt(), "UpdatedAt should be auto-set");
        assertNotNull(saved.getVersion(), "Version should be auto-set");

        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test audit trail functionality
     */
    @Test
    void testAuditTrail() {
        Student student = createValidStudent(1);
        Student saved   = DB.save(student).blockingGet();

        assertNotNull(saved.getCreatedAt(), "Created timestamp should be set");
        assertEquals("test-suite", saved.getCreatedBy(), "Created by should be set correctly");

        LocalDateTime originalCreatedAt = saved.getCreatedAt();
        String        originalCreatedBy = saved.getCreatedBy();

        saved.setName("Updated Name");
        saved.setUpdatedBy("test-suite-updated");
        Student updated = DB.update(saved).blockingGet();

        assertEquals(originalCreatedAt, updated.getCreatedAt(), "Created timestamp should not change");
        assertEquals(originalCreatedBy, updated.getCreatedBy(), "Created by should not change");
        assertTrue(updated.getUpdatedAt()
                          .isAfter(updated.getCreatedAt()), "Updated timestamp should be after created timestamp");
        assertEquals("test-suite-updated", updated.getUpdatedBy(), "Updated by should be changed");

        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test pagination and sorting
     */
    @Test
    void testPaginationAndSorting() {
        List<Student> testStudents = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            testStudents.add(DB.save(createValidStudent(i)).blockingGet());
        }

        // Test pagination
        List<Student> firstPage  = DB.read(Student.class, 5, 0).toList().blockingGet();
        List<Student> secondPage = DB.read(Student.class, 5, 5).toList().blockingGet();

        assertEquals(5, firstPage.size(), "First page should have 5 students");
        assertEquals(5, secondPage.size(), "Second page should have 5 students");
        assertNotEquals(firstPage.getFirst().getId(), secondPage.getFirst()
                                                                .getId(), "Pages should contain different students");

        // Test sorting
        List<Student> sortedByName = DB.read(Student.class, "name", true, 10, 0).toList().blockingGet();

        assertTrue(sortedByName.size() >= 2, "Should have at least 2 students for sorting test");
        assertTrue(sortedByName.get(0)
                               .getName()
                               .compareTo(sortedByName.get(1).getName()) <= 0, "Students should be sorted by name");

        // Cleanup
        testStudents.forEach(s -> DB.delete(Student.class, s.getId()).blockingAwait());
    }

    /**
     * Test find by ID functionality
     */
    @Test
    void testFindById() {
        Student saved = DB.save(createValidStudent(1)).blockingGet();

        Student found = DB.findById(Student.class, saved.getId()).blockingGet();
        assertNotNull(found, "Should find existing student");
        assertEquals(saved.getId(), found.getId(), "Should find correct student");

        Student notFound = DB.findById(Student.class, UUID.randomUUID()).blockingGet();
        assertNull(notFound, "Should return null for non-existent ID");

        DB.delete(Student.class, saved.getId()).blockingAwait();
    }

    /**
     * Test exists functionality
     */
    @Test
    void testExists() {
        Student student = DB.save(createValidStudent(1)).blockingGet();

        assertTrue(DB.exists(Student.class, student.getId()).blockingGet(), "Should return true for existing student");
        assertFalse(DB.exists(Student.class, UUID.randomUUID())
                      .blockingGet(), "Should return false for non-existent student");

        DB.delete(Student.class, student.getId()).blockingAwait();
    }

    /**
     * Test student count functionality
     */
    @Test
    void testCount() {
        long initialCount = DB.count(Student.class).blockingGet();

        List<Student> testStudents = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testStudents.add(DB.save(createValidStudent(i)).blockingGet());
        }

        long newCount = DB.count(Student.class).blockingGet();
        assertEquals(initialCount + 5, newCount, "Count should increase by number of added students");

        // Cleanup
        testStudents.forEach(s -> DB.delete(Student.class, s.getId()).blockingAwait());

        long finalCount = DB.count(Student.class).blockingGet();
        assertEquals(initialCount, finalCount, "Count should return to initial value after deletion");
    }

    /**
     * Test delete functionality
     */
    @Test
    void testDelete() {
        Student saved = DB.save(createValidStudent(1)).blockingGet();
        UUID    id    = saved.getId();

        // Verify student exists before deletion
        assertNotNull(DB.findById(Student.class, id).blockingGet(), "Student should exist before deletion");

        // Delete the student
        DB.delete(Student.class, id).blockingAwait();

        // Verify student no longer exists
        assertNull(DB.findById(Student.class, id).blockingGet(), "Student should not exist after deletion");

        // Test deleting non-existent student
        assertThrows(RuntimeException.class, () -> DB.delete(Student.class, UUID.randomUUID())
                                                     .blockingAwait(), "Should throw exception when deleting non-existent student");
    }

    /**
     * Test handling of null entity operations
     */
    @Test
    void testNullEntityOperations() {
        // Test null save
        assertThrows(NullPointerException.class, () -> DB.save(null)
                                                         .blockingGet(), "Should throw exception when saving null entity");

        // Test null update
        assertThrows(NullPointerException.class, () -> DB.update(null)
                                                         .blockingGet(), "Should throw exception when updating null entity");

        // Test findById with null
        assertThrows(IllegalArgumentException.class, () -> DB.findById(Student.class, null)
                                                             .blockingGet(), "Should throw exception when finding by null ID");

        // Test delete with null
        assertThrows(IllegalArgumentException.class, () -> DB.delete(Student.class, null)
                                                             .blockingAwait(), "Should throw exception when deleting with null ID");
    }

    /**
     * Test handling of long string values within valid limits
     */
    @Test
    void testLongStringValues() {
        Student student = createValidStudent(1);

        // Test name at exactly 100 characters
        String maxLengthName = "a".repeat(100);
        student.setName(maxLengthName);
        Student saved1 = DB.save(student).blockingGet();
        assertEquals(100, saved1.getName().length(), "Should save name with maximum length");
        DB.delete(Student.class, saved1.getId()).blockingAwait();

        // Test email at exactly 64 characters
        student = createValidStudent(2);
        String maxLengthEmail = "a".repeat(54) + "@uiu.ac.bd"; // 64 characters total
        student.setEmail(maxLengthEmail);
        Student saved2 = DB.save(student).blockingGet();
        assertEquals(64, saved2.getEmail().length(), "Should save email with maximum length");
        DB.delete(Student.class, saved2.getId()).blockingAwait();
    }

    /**
     * Test concurrent read operations
     */
    @Test
    void testConcurrentReads() {
        List<Student> testStudents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            testStudents.add(DB.save(createValidStudent(i)).blockingGet());
        }

        // Perform multiple reads concurrently
        List<Student> results1 = DB.read(Student.class, "name", true, 5, 0).toList().blockingGet();
        List<Student> results2 = DB.read(Student.class, "email", true, 5, 0).toList().blockingGet();
        List<Student> results3 = DB.read(Student.class, "universityId", true, 5, 0).toList().blockingGet();

        assertEquals(5, results1.size(), "First concurrent read should return 5 students");
        assertEquals(5, results2.size(), "Second concurrent read should return 5 students");
        assertEquals(5, results3.size(), "Third concurrent read should return 5 students");

        // Cleanup
        testStudents.forEach(s -> DB.delete(Student.class, s.getId()).blockingAwait());
    }

    /**
     * Test batch operations with larger datasets
     */
    @Test
    void testBatchOperations() {
        List<Student> testStudents = new ArrayList<>();

        // Create a larger batch of students
        for (int i = 0; i < 50; i++) {
            testStudents.add(DB.save(createValidStudent(i)).blockingGet());
        }

        // Test reading with different page sizes
        List<Student> smallPage = DB.read(Student.class, 10, 0).toList().blockingGet();
        assertEquals(10, smallPage.size(), "Should read correct small page size");

        List<Student> largePage = DB.read(Student.class, 30, 10).toList().blockingGet();
        assertEquals(30, largePage.size(), "Should read correct large page size");

        // Test different sorting with large dataset
        List<Student> sortedByUniId = DB.read(Student.class, "universityId", true, 50, 0).toList().blockingGet();
        assertTrue(sortedByUniId.get(0).getUniversityId() < sortedByUniId.get(1)
                                                                         .getUniversityId(), "Should correctly sort large dataset");

        // Cleanup
        testStudents.forEach(s -> DB.delete(Student.class, s.getId()).blockingAwait());
    }
}
