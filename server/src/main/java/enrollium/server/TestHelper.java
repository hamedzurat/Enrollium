package enrollium.server;

import enrollium.server.db.DB;
import enrollium.server.db.entity.*;
import enrollium.server.db.entity.types.*;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class TestHelper {
    public static final  String        TEST_CREATOR = "test-creator";
    private static final Random        RANDOM       = new Random();
    private static final AtomicInteger ID_COUNTER   = new AtomicInteger(1000);

    // Common cleanup methods
    public static void cleanupTestData(Class<?> entityClass) {
        DB.read(entityClass, 1_000_000, 0)
          .filter(entity -> TEST_CREATOR.equals(((BaseEntity) entity).getCreatedBy()))
          .toList()
          .blockingGet()
          .forEach(entity -> DB.delete(entityClass, ((BaseEntity) entity).getId()).blockingAwait());
    }

    // Student helpers
    public static Student createValidStudent() {
        int     id      = ID_COUNTER.getAndIncrement();
        Student student = new Student();
        student.setName("Test Student " + id);
        student.setEmail("student" + id + "@uiu.ac.bd");
        student.setUniversityId(id);
        student.setPassword("password" + RANDOM.nextInt(1_000_000));
        student.setInfo("Test student " + id);
        setCreator(student);
        return student;
    }

    // Faculty helpers
    public static Faculty createValidFaculty() {
        int     id      = ID_COUNTER.getAndIncrement();
        Faculty faculty = new Faculty();
        faculty.setName("Test Faculty " + id);
        faculty.setEmail("faculty" + id + "@uiu.ac.bd");
        faculty.setShortcode("FAC" + id);
        faculty.setPassword("password" + RANDOM.nextInt(1_000_000));
        faculty.setType(UserType.TEACHER);
        faculty.setInfo("Test faculty " + id);
        setCreator(faculty);
        return faculty;
    }

    // Subject helpers
    public static Subject createValidSubject() {
        int     id      = ID_COUNTER.getAndIncrement();
        Subject subject = new Subject();
        subject.setName("Test Subject " + id);
        subject.setCodeName("SUB" + id);
        subject.setCredits(3);
        subject.setType(SubjectType.THEORY);
        subject.setInfo("Test subject " + id);
        setCreator(subject);
        return subject;
    }

    // Section helpers
    public static Section createValidSection(Subject subject, Trimester trimester) {
        if (trimester == null) {
            trimester = saveEntity(createTrimesterForOngoing());
        }

        int     id      = ID_COUNTER.getAndIncrement();
        Section section = new Section();
        section.setName("Section " + id);
        section.setSection(String.format("S%03d", id % 1000));
        section.setSubject(subject);
        section.setTrimester(trimester);
        section.setMaxCapacity(40);
        section.setCurrentCapacity(0);

        SpaceTime spaceTime = saveEntity(createValidSpaceTime());
        section.setSpaceTimeSlots(new HashSet<>(Collections.singleton(spaceTime)));

        section.setInfo("Test section " + id);
        setCreator(section);
        return section;
    }

    // Course helpers
    public static Course createValidCourse(Student student, Subject subject, Trimester trimester) {
        // If no trimester provided, create an ongoing one
        if (trimester == null) {
            trimester = saveEntity(createOngoingTrimester());
        }

        Course course = new Course();
        course.setStudent(student);
        course.setSubject(subject);
        course.setTrimester(trimester);
        course.setStatus(CourseStatus.SELECTED);
        course.setInfo("Test course registration");
        setCreator(course);
        return course;
    }

    // Prerequisite helpers
    public static Prerequisite createValidPrerequisite(Subject subject, Subject prereq) {
        Prerequisite prerequisite = new Prerequisite();
        prerequisite.setSubject(subject);
        prerequisite.setPrerequisite(prereq);
        prerequisite.setMinimumGrade(2.0);
        prerequisite.setInfo("Test prerequisite");
        setCreator(prerequisite);
        return prerequisite;
    }

    // Notification helpers
    public static Notification createValidNotification(User sender) {
        int          id           = ID_COUNTER.getAndIncrement();
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setTitle("Test Notification " + id);
        notification.setContent("Test content " + id);
        notification.setCategory(NotificationCategory.GENERAL);
        notification.setScope(NotificationScope.GLOBAL);
        notification.setInfo("Test notification");
        setCreator(notification);
        return notification;
    }

    // Common save helper
    public static <T extends BaseEntity> T saveEntity(T entity) {
        setCreator(entity);
        return DB.save(entity).blockingGet();
    }

    // Validation helper
    public static <T extends BaseEntity> void assertValidationFails(T entity, Consumer<T> invalidator, Class<? extends Exception> exceptionClass) {
        invalidator.accept(entity);
        try {
            saveEntity(entity);
            throw new AssertionError("Expected " + exceptionClass.getSimpleName() + " was not thrown");
        } catch (Exception e) {
            if (!exceptionClass.isInstance(e)) {
                throw e;
            }
        }
    }

    // Random data generators
    public static String randomEmail() {
        return "test" + RANDOM.nextInt(1_000_000) + "@uiu.ac.bd";
    }

    public static String randomPassword() {
        return "password" + RANDOM.nextInt(1_000_000);
    }

    public static String randomString(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }

    // Private helpers
    private static void setCreator(BaseEntity entity) {
        entity.setCreatedBy(TEST_CREATOR);
        entity.setUpdatedBy(TEST_CREATOR);
    }

    // Clean up all test data in correct order
    public static void cleanupAllTestData() {
        // Clean in reverse dependency order
        cleanupTestData(Course.class);
        cleanupTestData(Section.class);
        cleanupTestData(Prerequisite.class);
        cleanupTestData(SpaceTime.class);
        cleanupTestData(Student.class);
        cleanupTestData(Faculty.class);
        cleanupTestData(Subject.class);
        cleanupTestData(Trimester.class);
        cleanupTestData(Notification.class);
    }

    // Entity relationship helpers
    public static Course createValidCourseWithRelations() {
        Student   student   = saveEntity(createValidStudent());
        Subject   subject   = saveEntity(createValidSubject());
        Trimester trimester = saveEntity(createTrimesterForOngoing());
        return createValidCourse(student, subject, trimester);
    }

    public static Section createValidSectionWithRelations() {
        Subject   subject   = saveEntity(createValidSubject());
        SpaceTime spaceTime = saveEntity(createValidSpaceTime());
        Faculty   faculty   = saveEntity(createValidFaculty());
        Trimester trimester = saveEntity(createTrimesterForOngoing());

        Section section = createValidSection(subject, trimester);
        section.getSpaceTimeSlots().add(spaceTime);
        section.getTeachers().add(faculty);

        return section;
    }

    public static Prerequisite createValidPrerequisiteWithRelations() {
        Subject subject = saveEntity(createValidSubject());
        Subject prereq  = saveEntity(createValidSubject());
        return createValidPrerequisite(subject, prereq);
    }

    // Additional date helpers for Trimester testing
    public static LocalDateTime futureDate(long plusDays) {
        return LocalDateTime.now().plusDays(plusDays);
    }

    public static LocalDateTime validStartDate() {
        return futureDate(1);
    }

    public static LocalDateTime validEndDate() {
        return futureDate(7);
    }

    // Reflection utility for setting fields
    private static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    public static Course createSelectedCourse() {
        Student   student   = saveEntity(createValidStudent());
        Subject   subject   = saveEntity(createValidSubject());
        Trimester trimester = saveEntity(createValidTrimester());

        Course course = new Course();
        course.setStudent(student);
        course.setSubject(subject);
        course.setTrimester(trimester);
        course.setStatus(CourseStatus.SELECTED);
        course.setSection(null);
        course.setGrade(null);
        setCreator(course);
        return course;
    }

    public static Course createRegisteredCourse() {
        Course  course  = createSelectedCourse();
        Section section = saveEntity(createValidSection(course.getSubject(), course.getTrimester()));
        course.setStatus(CourseStatus.REGISTERED);
        course.setSection(section);
        return course;
    }

    public static Course createCompletedCourse() {
        Course course = createRegisteredCourse();
        course.setStatus(CourseStatus.COMPLETED);
        course.setGrade(3.0); // Default passing grade
        return course;
    }

    public static Course createDroppedCourse() {
        Course course = createRegisteredCourse();
        course.setStatus(CourseStatus.DROPPED);
        return course;
    }

    // Helper for creating courses with specific grades
    public static Course createCompletedCourseWithGrade(Double grade) {
        Course course = createRegisteredCourse();
        course.setStatus(CourseStatus.COMPLETED);
        course.setGrade(grade);
        return course;
    }

    // Helper for creating invalid state combinations
    public static Course createCourseWithStatus(CourseStatus status) {
        Course course = createSelectedCourse();
        course.setStatus(status);
        return course;
    }

    public static SpaceTime createValidTheorySpaceTime() {
        int       id        = ID_COUNTER.getAndIncrement();
        SpaceTime spaceTime = new SpaceTime();
        spaceTime.setName("Theory Room " + id);
        spaceTime.setRoomNumber("TR" + id);
        spaceTime.setRoomType(SubjectType.THEORY);
        spaceTime.setDayOfWeek(DayOfWeek.MONDAY);
        spaceTime.setTimeSlot(1);  // Ensure this is always set
        spaceTime.setInfo("Test theory space-time " + id);
        setCreator(spaceTime);
        return spaceTime;
    }

    public static SpaceTime createValidLabSpaceTime() {
        int       id        = ID_COUNTER.getAndIncrement();
        SpaceTime spaceTime = new SpaceTime();
        spaceTime.setName("Lab Room " + id);
        spaceTime.setRoomNumber("LR" + id);
        spaceTime.setRoomType(SubjectType.LAB);
        spaceTime.setDayOfWeek(DayOfWeek.MONDAY);
        spaceTime.setTimeSlot(1);  // Ensure this is always set
        spaceTime.setInfo("Test lab space-time " + id);
        setCreator(spaceTime);
        return spaceTime;
    }

    // SpaceTime helpers
    public static SpaceTime createValidSpaceTime() {
        return RANDOM.nextBoolean() ? createValidTheorySpaceTime() : createValidLabSpaceTime();
    }

    // Helper for creating space-times with specific time slots
    public static SpaceTime createSpaceTimeWithSlot(SubjectType type, Integer timeSlot) {
        SpaceTime spaceTime = type == SubjectType.THEORY ? createValidTheorySpaceTime() : createValidLabSpaceTime();
        spaceTime.setTimeSlot(timeSlot);
        return spaceTime;
    }

    // Helper for creating space-times with specific day and slot
    public static SpaceTime createSpaceTimeWithDayAndSlot(DayOfWeek day, Integer timeSlot) {
        SpaceTime spaceTime = createValidTheorySpaceTime();
        spaceTime.setDayOfWeek(day);
        spaceTime.setTimeSlot(timeSlot);
        return spaceTime;
    }

    // Add these to TestHelper.java
    public static Notification createGlobalNotification(User sender) {
        Notification notification = createValidNotification(sender);
        notification.setScope(NotificationScope.GLOBAL);
        notification.setTrimester(null);
        notification.setSection(null);
        notification.setTargetUser(null);
        return notification;
    }

    public static Notification createTrimesterNotification(User sender, Trimester trimester) {
        Notification notification = createValidNotification(sender);
        notification.setScope(NotificationScope.TRIMESTER);
        notification.setTrimester(trimester);
        notification.setSection(null);
        notification.setTargetUser(null);
        return notification;
    }

    public static Notification createSectionNotification(User sender, Section section) {
        Notification notification = createValidNotification(sender);
        notification.setScope(NotificationScope.SECTION);
        notification.setTrimester(null);
        notification.setSection(section);
        notification.setTargetUser(null);
        return notification;
    }

    public static Notification createUserNotification(User sender, User targetUser) {
        Notification notification = createValidNotification(sender);
        notification.setScope(NotificationScope.USER);
        notification.setTrimester(null);
        notification.setSection(null);
        notification.setTargetUser(targetUser);
        return notification;
    }

    // For different categories
    public static Notification createNotificationWithCategory(User sender, NotificationCategory category) {
        Notification notification = createValidNotification(sender);
        notification.setCategory(category);
        return notification;
    }

    public static Prerequisite createPrerequisite(Subject subject, Subject prereq, Double grade) {
        Prerequisite prerequisite = new Prerequisite();
        prerequisite.setSubject(subject);
        prerequisite.setPrerequisite(prereq);
        prerequisite.setMinimumGrade(grade);
        prerequisite.setInfo("Test prerequisite");
        setCreator(prerequisite);
        return prerequisite;
    }

    // Create a chain of prerequisites (A -> B -> C)
    public static List<Prerequisite> createPrerequisiteChain(int chainLength) {
        List<Subject> subjects = new ArrayList<>();
        for (int i = 0; i < chainLength; i++) {
            subjects.add(saveEntity(createValidSubject()));
        }

        List<Prerequisite> chain = new ArrayList<>();
        for (int i = 0; i < chainLength - 1; i++) {
            chain.add(saveEntity(createPrerequisite(subjects.get(i), subjects.get(i + 1), 2.0)));
        }
        return chain;
    }

    // Create a circular prerequisite scenario (A -> B -> C -> A)
    public static List<Prerequisite> createCircularPrerequisites(int chainLength) {
        List<Subject> subjects = new ArrayList<>();
        for (int i = 0; i < chainLength; i++) {
            subjects.add(saveEntity(createValidSubject()));
        }

        List<Prerequisite> circle = new ArrayList<>();
        for (int i = 0; i < chainLength; i++) {
            Subject current = subjects.get(i);
            Subject next    = subjects.get((i + 1) % chainLength);
            circle.add(saveEntity(createPrerequisite(current, next, 2.0)));
        }
        return circle;
    }

    // Utility method to check if prerequisites form a cycle
    public static boolean hasPrerequisiteCycle(Subject subject, Set<UUID> visited) {
        if (visited.contains(subject.getId())) {
            return true;
        }
        visited.add(subject.getId());
        for (Prerequisite prereq : subject.getPrerequisites()) {
            if (hasPrerequisiteCycle(prereq.getPrerequisite(), visited)) {
                return true;
            }
        }
        visited.remove(subject.getId());
        return false;
    }

    // Helper for creating trimester with specific dates
    public static Trimester createTrimesterWithDates(LocalDateTime courseSelectionStart, LocalDateTime courseSelectionEnd, LocalDateTime sectionRegistrationStart, LocalDateTime sectionRegistrationEnd) {
        Trimester trimester = createValidTrimester();
        trimester.setCourseSelectionStart(courseSelectionStart);
        trimester.setCourseSelectionEnd(courseSelectionEnd);
        trimester.setSectionRegistrationStart(sectionRegistrationStart);
        trimester.setSectionRegistrationEnd(sectionRegistrationEnd);
        return trimester;
    }

    // Helper for creating trimester with specific code
    public static Trimester createTrimesterWithCode(int code) {
        Trimester trimester = createValidTrimester();
        trimester.setCode(code);
        return trimester;
    }

    // Helper for creating trimester with specific year
    public static Trimester createTrimesterWithYear(int year) {
        Trimester trimester = createValidTrimester();
        trimester.setYear(year);

        // Get season number from current season
        int seasonNumber = switch (trimester.getSeason()) {
            case SPRING -> 1;
            case SUMMER -> 2;
            case FALL -> 3;
        };

        // Create new code in YY[1|2|3] format
        trimester.setCode((year % 100) * 10 + seasonNumber);

        return trimester;
    }

    // Helper for creating trimester with specific season
    public static Trimester createTrimesterWithSeason(Season season) {
        Trimester trimester = createValidTrimester();
        trimester.setSeason(season);
        return trimester;
    }

    // Helper to advance trimester to next status
    public static Trimester advanceTrimesterStatus(Trimester trimester) {
        TrimesterStatus nextStatus = switch (trimester.getStatus()) {
            case UPCOMING -> TrimesterStatus.COURSE_SELECTION;
            case COURSE_SELECTION -> TrimesterStatus.SECTION_CREATION;
            case SECTION_CREATION -> TrimesterStatus.SECTION_SELECTION;
            case SECTION_SELECTION -> TrimesterStatus.ONGOING;
            case ONGOING -> TrimesterStatus.COMPLETED;
            case COMPLETED -> throw new IllegalStateException("Cannot advance COMPLETED status");
        };

        trimester.setStatus(nextStatus);
        return DB.update(trimester).blockingGet();
    }

    public static Trimester createTrimesterWithCode(Integer code, Integer year, Season season) {
        Trimester trimester = createValidTrimester();
        trimester.setCode(code);
        trimester.setYear(year);
        trimester.setSeason(season);
        return trimester;
    }

    public static LocalDateTime getFutureDateTime() {
        return LocalDateTime.now().plusYears(1);
    }

    public static LocalDateTime getFutureDateTime(int plusDays) {
        return getFutureDateTime().plusDays(plusDays);
    }

    public static Trimester createValidTrimester() {
        // Start with year 2003 (which will give code 031, 032, 033)
        int uniqueYear = 2003 + (ID_COUNTER.get() / 3);
        int season     = (ID_COUNTER.getAndIncrement() % 3) + 1;

        Trimester trimester = new Trimester();
        trimester.setYear(uniqueYear);
        // Code format YY[1|2|3]: For 2003 -> 03[1|2|3]
        trimester.setCode((uniqueYear % 100) * 10 + season); // For 2003 this gives 031, 032, or 033
        trimester.setSeason(switch (season) {
            case 1 -> Season.SPRING;
            case 2 -> Season.SUMMER;
            case 3 -> Season.FALL;
            default -> throw new IllegalStateException("Unexpected season: " + season);
        });

        // Set UPCOMING as default with proper future dates
        LocalDateTime future = LocalDateTime.now().plusDays(10);
        trimester.setStatus(TrimesterStatus.UPCOMING);
        trimester.setCourseSelectionStart(future);
        trimester.setCourseSelectionEnd(future.plusDays(1));
        trimester.setSectionRegistrationStart(future.plusDays(2));
        trimester.setSectionRegistrationEnd(future.plusDays(3));

        trimester.setInfo("Test trimester " + ID_COUNTER.get());
        setCreator(trimester);
        return trimester;
    }

    private static Trimester createTrimesterForOngoing() {
        // Start with year 2003 (which will give code 031, 032, 033)
        int uniqueYear = 2003 + (ID_COUNTER.get() / 3);
        int season     = (ID_COUNTER.getAndIncrement() % 3) + 1;

        Trimester trimester = new Trimester();
        trimester.setYear(uniqueYear);
        // Code format YY[1|2|3]: For 2003 -> 03[1|2|3]
        trimester.setCode((uniqueYear % 100) * 10 + season); // For 2003 this gives 031, 032, or 033
        trimester.setSeason(switch (season) {
            case 1 -> Season.SPRING;
            case 2 -> Season.SUMMER;
            case 3 -> Season.FALL;
            default -> throw new IllegalStateException("Unexpected season: " + season);
        });

        // Set all dates in past for ONGOING
        LocalDateTime now  = LocalDateTime.now();
        LocalDateTime past = now.minusDays(10);
        trimester.setCourseSelectionStart(past);
        trimester.setCourseSelectionEnd(past.plusDays(1));
        trimester.setSectionRegistrationStart(past.plusDays(2));
        trimester.setSectionRegistrationEnd(past.plusDays(3));
        trimester.setStatus(TrimesterStatus.ONGOING);

        trimester.setInfo("Test trimester " + ID_COUNTER.get());
        setCreator(trimester);
        return trimester;
    }

    public static Trimester createOngoingTrimester() {
        Trimester trimester = new Trimester();

        // Set up basic info
        int uniqueYear = 2030 + (ID_COUNTER.get() / 3);
        int season     = (ID_COUNTER.getAndIncrement() % 3) + 1;

        trimester.setYear(uniqueYear);
        trimester.setCode((uniqueYear % 100) * 10 + season);
        trimester.setSeason(switch (season) {
            case 1 -> Season.SPRING;
            case 2 -> Season.SUMMER;
            case 3 -> Season.FALL;
            default -> throw new IllegalStateException("Unexpected season: " + season);
        });

        // Set past dates for ONGOING status
        LocalDateTime baseTime = LocalDateTime.now().minusMonths(1);
        trimester.setCourseSelectionStart(baseTime.minusWeeks(3));
        trimester.setCourseSelectionEnd(baseTime.minusWeeks(2));
        trimester.setSectionRegistrationStart(baseTime.minusWeeks(1));
        trimester.setSectionRegistrationEnd(baseTime.minusDays(1));

        trimester.setStatus(TrimesterStatus.ONGOING);
        trimester.setInfo("Test ongoing trimester " + ID_COUNTER.get());
        setCreator(trimester);
        return trimester;
    }

    public static Trimester createCompletedTrimester() {
        Trimester trimester = new Trimester();

        // Set up basic info
        int uniqueYear = 2030 + (ID_COUNTER.get() / 3);
        int season     = (ID_COUNTER.getAndIncrement() % 3) + 1;

        trimester.setYear(uniqueYear);
        trimester.setCode((uniqueYear % 100) * 10 + season);
        trimester.setSeason(switch (season) {
            case 1 -> Season.SPRING;
            case 2 -> Season.SUMMER;
            case 3 -> Season.FALL;
            default -> throw new IllegalStateException("Unexpected season: " + season);
        });

        // Set dates well in the past for COMPLETED status
        LocalDateTime baseTime = LocalDateTime.now().minusMonths(2);
        trimester.setCourseSelectionStart(baseTime.minusWeeks(3));
        trimester.setCourseSelectionEnd(baseTime.minusWeeks(2));
        trimester.setSectionRegistrationStart(baseTime.minusWeeks(1));
        trimester.setSectionRegistrationEnd(baseTime.minusDays(1));

        trimester.setStatus(TrimesterStatus.COMPLETED);
        trimester.setInfo("Test completed trimester " + ID_COUNTER.get());
        setCreator(trimester);
        return trimester;
    }

    // Add this utility method
    public static LocalDateTime[] getValidDateSequence() {
        LocalDateTime baseTime = LocalDateTime.now().plusMonths(1);
        return new LocalDateTime[]{
                baseTime,
                baseTime.plusWeeks(1),
                baseTime.plusWeeks(2),
                baseTime.plusWeeks(3)
        };
    }

    public static Trimester createTrimesterWithSpec(int year, Season season) {
        Trimester trimester = new Trimester();
        int seasonNumber = switch (season) {
            case SPRING -> 1;
            case SUMMER -> 2;
            case FALL -> 3;
        };

        trimester.setYear(year);
        trimester.setCode((year % 100) * 10 + seasonNumber);
        trimester.setSeason(season);

        // Set UPCOMING status with future dates by default
        LocalDateTime futureBase = LocalDateTime.now().plusMonths(1);
        trimester.setStatus(TrimesterStatus.UPCOMING);
        trimester.setCourseSelectionStart(futureBase);
        trimester.setCourseSelectionEnd(futureBase.plusWeeks(1));
        trimester.setSectionRegistrationStart(futureBase.plusWeeks(2));
        trimester.setSectionRegistrationEnd(futureBase.plusWeeks(3));

        trimester.setInfo("Test trimester " + ID_COUNTER.getAndIncrement());
        setCreator(trimester);
        return trimester;
    }

    public static Trimester createTrimesterWithStatus(TrimesterStatus status) {
        Trimester     trimester = createValidTrimester();
        LocalDateTime now       = LocalDateTime.now();

        switch (status) {
            case UPCOMING -> {
                LocalDateTime futureStart = now.plusDays(1);
                trimester.setCourseSelectionStart(futureStart);
                trimester.setCourseSelectionEnd(futureStart.plusDays(7));
                trimester.setSectionRegistrationStart(futureStart.plusDays(14));
                trimester.setSectionRegistrationEnd(futureStart.plusDays(21));
            }
            case COURSE_SELECTION -> {
                trimester.setCourseSelectionStart(now.minusHours(1));
                trimester.setCourseSelectionEnd(now.plusDays(7));
                trimester.setSectionRegistrationStart(now.plusDays(14));
                trimester.setSectionRegistrationEnd(now.plusDays(21));
            }
            case SECTION_CREATION -> {
                trimester.setCourseSelectionStart(now.minusDays(7));
                trimester.setCourseSelectionEnd(now.minusHours(1));
                trimester.setSectionRegistrationStart(now.plusHours(1));
                trimester.setSectionRegistrationEnd(now.plusDays(7));
            }
            case SECTION_SELECTION -> {
                trimester.setCourseSelectionStart(now.minusDays(14));
                trimester.setCourseSelectionEnd(now.minusDays(7));
                trimester.setSectionRegistrationStart(now.minusHours(1));
                trimester.setSectionRegistrationEnd(now.plusDays(7));
            }
            case ONGOING, COMPLETED -> {
                trimester.setCourseSelectionStart(now.minusDays(21));
                trimester.setCourseSelectionEnd(now.minusDays(14));
                trimester.setSectionRegistrationStart(now.minusDays(7));
                trimester.setSectionRegistrationEnd(now.minusHours(1));
            }
        }

        trimester.setStatus(status);
        return trimester;
    }

    public static void setTrimesterDates(Trimester trimester, LocalDateTime... dates) {
        if (dates.length > 0) trimester.setCourseSelectionStart(dates[0]);
        if (dates.length > 1) trimester.setCourseSelectionEnd(dates[1]);
        if (dates.length > 2) trimester.setSectionRegistrationStart(dates[2]);
        if (dates.length > 3) trimester.setSectionRegistrationEnd(dates[3]);
    }

    public static Section createSectionWithCapacity(Subject subject, Trimester trimester, int maxCapacity, int currentCapacity) {
        Section section = createValidSection(subject, trimester);
        section.setMaxCapacity(maxCapacity);
        section.setCurrentCapacity(currentCapacity);
        return section;
    }

    public static Section createSectionWithMultipleSpaceTimeSlots(Subject subject, Trimester trimester, int slotCount) {
        Section        section = createValidSection(subject, trimester);
        Set<SpaceTime> slots   = new HashSet<>();
        for (int i = 0; i < slotCount; i++) {
            slots.add(saveEntity(createValidSpaceTime()));
        }
        section.setSpaceTimeSlots(slots);
        return section;
    }

    public static Section createSectionWithMultipleTeachers(Subject subject, Trimester trimester, int teacherCount) {
        Section      section  = createValidSection(subject, trimester);
        Set<Faculty> teachers = new HashSet<>();
        for (int i = 0; i < teacherCount; i++) {
            teachers.add(saveEntity(createValidFaculty()));
        }
        section.setTeachers(teachers);
        return section;
    }
}
