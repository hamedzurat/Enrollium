package enrollium.server.db;

import enrollium.server.db.entity.*;
import enrollium.server.db.entity.types.*;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import net.datafaker.Faker;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* DOCS:
 *
 * https://docs.jboss.org/hibernate/orm/6.6/introduction/html_single/Hibernate_Introduction.html#java-code
 * https://medium.com/@ramanamuttana/connect-hibernate-with-postgres-d8f29249db0c
 * https://www.geeksforgeeks.org/hibernate-tutorial/
 * https://www.javatpoint.com/hibernate-tutorial
 */

/* RxJava stream types:
 *
 * Single: Emits one item or an error
 * Observable: Emits multiple items or an error
 * Maybe: Emits zero or one item, or an error
 * Completable: Emits only completion or error
 *
 * `.subscribeOn(Schedulers.io())` in every stream to run it io operation optimized thread
 * https://reactivex.io/documentation/operators/subscribeon.html
 */


// Singleton - only one instance of this will exist
public class DB {
    // volatile to make it only run once across all the threads
    // https://www.geeksforgeeks.org/volatile-keyword-in-java/
    private static final    Faker          faker = new Faker();
    private static final    Logger         log   = LoggerFactory.getLogger(DB.class);
    private static volatile SessionFactory sessionFactory;

    // setup db connection or return existing connection
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            // synchronized to make sure it runs one at a time across multiple threads
            // https://www.geeksforgeeks.org/synchronization-in-java/
            synchronized (DB.class) {
                if (sessionFactory == null) { // create only if it isn't created yet
                    try {
                        log.info("Initializing SessionFactory");
                        Instant start = Instant.now();

                        Configuration conf = new Configuration();

                        // adding all the entity files
                        conf.addAnnotatedClass(enrollium.server.db.entity.Student.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.User.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Course.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Faculty.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Notification.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Prerequisite.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Section.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.SpaceTime.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Subject.class);
                        conf.addAnnotatedClass(enrollium.server.db.entity.Trimester.class);

                        conf.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                        conf.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                        conf.setProperty("hibernate.show_sql", "true");
                        conf.setProperty("hibernate.hbm2ddl.auto", "update");
                        conf.setProperty("jakarta.persistence.validation.mode", "auto");

                        conf.setProperty("hibernate.connection.url", //
                                System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/enrollium"));
                        conf.setProperty("hibernate.connection.username", //
                                System.getenv().getOrDefault("DB_USERNAME", "enrollium"));
                        conf.setProperty("hibernate.connection.password", //
                                System.getenv().getOrDefault("DB_PASSWORD", "enrollium"));

                        // connection pool
                        conf.setProperty("hibernate.hikari.maximumPoolSize", "10");
                        conf.setProperty("hibernate.hikari.minimumIdle", "5");
                        conf.setProperty("hibernate.hikari.idleTimeout", "300000");

                        sessionFactory = conf.buildSessionFactory();

                        Duration duration = Duration.between(start, Instant.now());
                        log.info("SessionFactory initialized in {} ms", duration.toMillis());
                    } catch (Exception e) {
                        log.error("Failed to create SessionFactory: {}", e.getMessage(), e);
                        throw new RuntimeException("Failed to create SessionFactory: " + e.getMessage(), e);
                    }
                }
            }
        }
        return sessionFactory;
    }

    // do the operation in a transaction with logs
    // takes a lambda functions (function pointer) to execute under transaction...
    // https://stackoverflow.com/a/58508080
    // https://www.geeksforgeeks.org/function-interface-in-java-with-examples/
    private static <T> T exec(Function<Session, T> operation, String name) {
        Instant start = Instant.now();

        Session     session = getSessionFactory().openSession();
        Transaction tx      = null;

        try {
            tx = session.beginTransaction();

            // run the lambda using the current session
            T result = operation.apply(session);

            // only commit if db connection is still alive
            if (tx.isActive()) {
                tx.commit();
            }

            Duration duration = Duration.between(start, Instant.now());
            log.info("{} completed in {} ms", name, duration.toMillis());

            return result;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    // when fails
                    tx.rollback();
                    log.error("{} failed and rolled back: {}", name, e.getMessage());
                } catch (Exception rollbackEx) {
                    log.error("Error during rollback", rollbackEx);
                }
            }

            throw e;
        } finally {
            // always try to close
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    // creates an entry.
    // single bc returns only one object
    public static <T> Single<T> save(T entity) {
        String OpName = "Save " + entity.getClass().getSimpleName();

        return Single.<T>create(emitter -> {
            try {
                emitter.onSuccess(exec(session -> {
                    session.persist(entity);
                    return entity;
                }, OpName));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // reads entry
    // basic observable bc can read multiple
    public static <T> Observable<T> read(Class<T> type, int limit, int offset) {
        String OpName = "Read " + type.getSimpleName();

        return Observable.<T>create(emitter -> {
            try {
                exec(session -> {
                    session.createQuery("FROM " + type.getSimpleName(), type)
                           .setMaxResults(limit)
                           .setFirstResult(offset)
                           .stream()// convert the Query in to a java stream and emit for each item
                           .forEach(emitter::onNext);
                    return null;
                }, OpName);

                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // sorted read
    public static <T> Observable<T> read(Class<T> type, String fieldToBy, boolean ascending, int limit, int offset) {
        String OpName = "Read Sorted " + type.getSimpleName();
        String order  = ascending ? "ASC" : "DESC";

        return Observable.<T>create(emitter -> {
            try {
                exec(session -> {
                    session.createQuery("FROM " + type.getSimpleName() + " e ORDER BY e." + fieldToBy + " " + order, type)
                           .setMaxResults(limit)
                           .setFirstResult(offset)
                           .stream()
                           .forEach(emitter::onNext);
                    return null;
                }, OpName);

                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // updates entry
    // single bc returns only one obj
    public static <T> Single<T> update(T entity) {
        String OpName = "Update " + entity.getClass().getSimpleName();

        return Single.<T>create(emitter -> {
            try {
                emitter.onSuccess(exec(session -> session.merge(entity), OpName));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // finds entry using UUID and deletes it
    // completable bc it will emit completion or error
    public static <T> Completable delete(Class<T> type, UUID id) {
        String OpName = "Delete " + type.getSimpleName();

        return Completable.create(emitter -> {
            try {
                exec(session -> {
                    T entity = session.get(type, id);

                    if (entity != null) session.remove(entity);
                    else throw new RuntimeException("Entity not found");

                    return null;
                }, OpName);

                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // finds by UUID
    // maybe bc can be null or obj
    public static <T> Maybe<T> findById(Class<T> type, UUID id) {
        String OpName = "FindById " + type.getSimpleName();

        return Maybe.<T>create(emitter -> {
            try {
                T result = exec(session -> session.get(type, id), OpName);

                if (result != null) emitter.onSuccess(result);
                else emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // returns number of entries
    // single bc returns only one int
    public static <T> Single<Long> count(Class<T> type) {
        String OpName = "Count " + type.getSimpleName();

        return Single.<Long>create(emitter -> {
            try {
                emitter.onSuccess(exec(session -> session.createSelectionQuery("SELECT COUNT(e) FROM " + type.getSimpleName() + " e", Long.class)
                                                         .getSingleResult(), OpName));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    // checks if exists
    // maybe bc it also can be null
    public static <T> Maybe<Boolean> exists(Class<T> type, UUID id) {
        String OpName = "Exists " + type.getSimpleName();

        return Maybe.<Boolean>create(emitter -> {
            try {
                emitter.onSuccess(exec(session -> session.createSelectionQuery("SELECT COUNT(e) > 0 FROM " + type.getSimpleName() + " e WHERE e.id = :id", Boolean.class)
                                                         .setParameter("id", id)
                                                         .getSingleResult(), OpName));
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Completable resetAndSeed() {
        return Completable.defer(() -> {
            log.info("Starting database reset and seed");
            try {
                return Completable.fromCallable(() -> {
                    // First delete all data
                    exec(session -> {
                        log.info("Starting to clear all tables...");

                        // List of tables in reverse dependency order
                        String[] tables = {"Course", "Notification", "Section", "Prerequisite", "SpaceTime", "Subject", "Trimester", "Student", "Faculty", "User"};

                        // Loop through tables and clear them
                        for (String table : tables) {
                            session.createMutationQuery("DELETE FROM " + table).executeUpdate();
                            log.info("Table {} cleared successfully.", table);
                        }

                        log.info("All tables have been cleared successfully.");
                        return null;
                    }, "Reset Database Schema");

                    // Create all objects first
                    List<Faculty>      admins         = new ArrayList<>();
                    List<Faculty>      teachers       = new ArrayList<>();
                    List<Student>      students       = new ArrayList<>();
                    List<Subject>      theorySubjects = new ArrayList<>();
                    List<Subject>      labSubjects    = new ArrayList<>();
                    List<Prerequisite> prerequisites  = new ArrayList<>();
                    List<SpaceTime>    spaceTimeSlots = new ArrayList<>();
                    List<Trimester>    trimesters     = new ArrayList<>();
                    List<Section>      sections       = new ArrayList<>();
                    List<Course>       courses        = new ArrayList<>();
                    List<Notification> notifications  = new ArrayList<>();

                    // Create admins
                    Set<String> usedEmails = new HashSet<>();

                    for (int i = 0; i < 2; i++) {
                        Faculty admin = new Faculty();
                        admin.setName(faker.name().fullName());
                        admin.setShortcode("ADMIN" + (i + 1));

                        String email;
                        do {
                            email = String.format("%s%d@uiu.ac.bd", admin.getShortcode().toLowerCase(), faker.number()
                                                                                                             .numberBetween(1, 1000));
                        } while (!usedEmails.add(email));  // Ensure uniqueness

                        String pass = "admin" + (i + 1) + "pass";

                        admin.setCreatedBy("demo");
                        admin.setEmail(email);
                        admin.setPassword(pass);
                        admin.setInfo(pass);
                        admin.setType(UserType.ADMIN);
                        admins.add(admin);
                    }

                    Set<String> usedFacultyEmails = new HashSet<>();

                    for (int i = 0; i < 6; i++) {
                        Faculty teacher = new Faculty();
                        teacher.setCreatedBy("demo");

                        String name = faker.name().fullName();
                        teacher.setName(name);

                        String shortcode;
                        String email;

                        // Generate a unique shortcode and email
                        do {
                            shortcode = Arrays.stream(name.split(" "))
                                              .map(s -> s.substring(0, 1).toUpperCase())
                                              .collect(Collectors.joining()) + faker.number().numberBetween(1, 1000);
                            email     = String.format("%s@uiu.ac.bd", shortcode.toLowerCase());
                        } while (!usedFacultyEmails.add(email));  // Ensure uniqueness

                        String pass = "teacher" + (i + 1) + "pass";

                        teacher.setShortcode(shortcode);
                        teacher.setEmail(email);
                        teacher.setPassword(pass);
                        teacher.setInfo(pass);
                        teacher.setType(UserType.TEACHER);
                        teachers.add(teacher);
                    }

                    Set<String> usedStudentEmails = new HashSet<>();

                    for (int i = 0; i < 12; i++) {
                        Student student = new Student();
                        student.setCreatedBy("demo");

                        student.setName(faker.name().fullName());

                        int    universityId;
                        String email;

                        // Generate a unique university ID and email
                        do {
                            universityId = faker.number().numberBetween(1001, 9999);
                            email        = String.format("%d@student.uiu.ac.bd", universityId);
                        } while (!usedStudentEmails.add(email));  // Ensure uniqueness

                        String pass = "student" + universityId;

                        student.setUniversityId(universityId);
                        student.setEmail(email);
                        student.setPassword(pass);
                        student.setInfo(pass);
                        students.add(student);
                    }

                    List<String> theorySubjectNames = Arrays.asList("Programming 1", "Programming 2", "Advanced Programming", "Basic Electronics", "Digital Logic", "Computer Architecture", "Data Structures", "Algorithms");
                    List<String> labSubjectNames    = Arrays.asList("Programming Lab 1", "Programming Lab 2", "Electronics Lab", "Digital Lab");

                    // Create theory subjects
                    for (int i = 0; i < theorySubjectNames.size(); i++) {
                        Subject subject = new Subject();
                        subject.setCreatedBy("demo");

                        subject.setName(theorySubjectNames.get(i));
                        subject.setCodeName("CSE" + (101 + i * 2));
                        subject.setCredits(i % 2 == 0 ? 1 : 3);
                        subject.setType(SubjectType.THEORY);
                        theorySubjects.add(subject);
                    }

                    // Create lab subjects
                    for (int i = 0; i < labSubjectNames.size(); i++) {
                        Subject subject = new Subject();
                        subject.setCreatedBy("demo");

                        subject.setName(labSubjectNames.get(i));
                        subject.setCodeName("CSE" + (102 + i * 2));
                        subject.setCredits(1);
                        subject.setType(SubjectType.LAB);
                        labSubjects.add(subject);
                    }

                    // Create prerequisites
                    Map<String, String[]> prerequisiteChains = Map.of("Programming 2", new String[]{"Programming 1"}, "Advanced Programming", new String[]{"Programming 2"}, "Digital Logic", new String[]{"Basic Electronics"}, "Computer Architecture", new String[]{"Digital Logic"}, "Algorithms", new String[]{"Data Structures"}, "Programming Lab 2", new String[]{"Programming Lab 1"}, "Digital Lab", new String[]{"Electronics Lab"});

                    List<Subject> allSubjects = Stream.concat(theorySubjects.stream(), labSubjects.stream()).toList();

                    // Create prerequisites based on the chains
                    for (Subject subject : allSubjects) {
                        String[] prereqs = prerequisiteChains.get(subject.getName());
                        if (prereqs != null) {
                            for (String prereqName : prereqs) {
                                Subject prereqSubject = allSubjects.stream()
                                                                   .filter(s -> s.getName().equals(prereqName))
                                                                   .findFirst()
                                                                   .orElseThrow();

                                Prerequisite prerequisite = new Prerequisite();
                                prerequisite.setCreatedBy("demo");

                                prerequisite.setSubject(subject);
                                prerequisite.setPrerequisite(prereqSubject);
                                prerequisite.setMinimumGrade(0.0);
                                prerequisites.add(prerequisite);
                            }
                        }
                    }

                    // Create SpaceTime slots
                    String[] theoryRooms = {"201", "202", "203", "204", "205", "206", "207"};
                    String[] labRooms    = {"Lab1", "Lab2", "Lab3", "Lab4", "Lab5", "Lab6"};

                    // Create theory room slots
                    for (String roomNumber : theoryRooms) {
                        String roomName = "Theory Room " + roomNumber;
                        for (DayOfWeek day : DayOfWeek.values()) {
                            if (day != DayOfWeek.FRIDAY) {
                                for (int slot = 1; slot <= 6; slot++) {
                                    SpaceTime spaceTime = new SpaceTime();
                                    spaceTime.setCreatedBy("demo");

                                    spaceTime.setName(roomName);
                                    spaceTime.setRoomNumber(roomNumber);
                                    spaceTime.setRoomType(SubjectType.THEORY);
                                    spaceTime.setDayOfWeek(day);
                                    spaceTime.setTimeSlot(slot);
                                    spaceTimeSlots.add(spaceTime);
                                }
                            }
                        }
                    }

                    // Create lab room slots
                    for (String roomNumber : labRooms) {
                        String roomName = "Lab Room " + roomNumber;
                        for (DayOfWeek day : DayOfWeek.values()) {
                            if (day != DayOfWeek.FRIDAY) {
                                for (int slot = 1; slot <= 3; slot++) {
                                    SpaceTime spaceTime = new SpaceTime();
                                    spaceTime.setCreatedBy("demo");
                                    spaceTime.setName(roomName);
                                    spaceTime.setRoomNumber(roomNumber);
                                    spaceTime.setRoomType(SubjectType.LAB);
                                    spaceTime.setDayOfWeek(day);
                                    spaceTime.setTimeSlot(slot);
                                    spaceTimeSlots.add(spaceTime);
                                }
                            }
                        }
                    }

                    LocalDateTime now = LocalDateTime.now();
                    for (int i = 0; i < 12; i++) {
                        Trimester trimester = new Trimester();
                        trimester.setCreatedBy("demo");

                        int    yearOffset = i / 3;
                        int    yearNum    = now.getYear() - 2 + yearOffset;
                        Season season     = Season.values()[i % 3];

                        trimester.setYear(yearNum);
                        trimester.setSeason(season);
                        trimester.setCode(Integer.parseInt((yearNum % 100) + "" + (season.ordinal() + 1)));

                        LocalDateTime baseDate = now.minusMonths((11 - i) * 4L);
                        trimester.setCourseSelectionStart(baseDate.minusDays(14));
                        trimester.setCourseSelectionEnd(baseDate.minusDays(7));
                        trimester.setSectionRegistrationStart(baseDate.minusDays(6));
                        trimester.setSectionRegistrationEnd(baseDate.minusDays(1));

                        if (i < 2) trimester.setStatus(TrimesterStatus.COMPLETED);
                        else if (i == 2) trimester.setStatus(TrimesterStatus.ONGOING);
                        else if (i < 5) trimester.setStatus(TrimesterStatus.SECTION_SELECTION);
                        else if (i < 7) trimester.setStatus(TrimesterStatus.SECTION_CREATION);
                        else if (i < 9) trimester.setStatus(TrimesterStatus.COURSE_SELECTION);
                        else trimester.setStatus(TrimesterStatus.UPCOMING);

                        trimesters.add(trimester);
                    }

                    // Create sections and track used slots
                    Map<UUID, Set<UUID>> trimesterUsedSlots = new HashMap<>();

                    for (Trimester trimester : trimesters) {
                        trimesterUsedSlots.put(trimester.getId(), new HashSet<>());

                        for (Subject subject : allSubjects) {
                            // Filter available SpaceTime slots that haven't been used in this trimester
                            List<SpaceTime> availableSlots = new ArrayList<>(spaceTimeSlots.stream()
                                                                                           .filter(st -> st.getRoomType() == subject.getType()) // Match room type
                                                                                           .filter(st -> !trimesterUsedSlots.get(trimester.getId())
                                                                                                                            .contains(st.getId())) // Exclude used slots
                                                                                           .toList());

                            Collections.shuffle(availableSlots);

                            if (!availableSlots.isEmpty()) {
                                Section section = new Section();
                                section.setCreatedBy("demo");

                                section.setName(subject.getName() + " - " + trimester.getCode());
                                section.setSection("A");
                                section.setSubject(subject);
                                section.setTrimester(trimester);
                                section.setMaxCapacity(5);

                                Faculty teacher = teachers.get(faker.number().numberBetween(0, teachers.size()));
                                section.setTeachers(new HashSet<>(Collections.singletonList(teacher)));

                                // Select and reserve a unique SpaceTime slot
                                SpaceTime selectedSlot = availableSlots.get(faker.number()
                                                                                 .numberBetween(0, availableSlots.size()));

                                // Mark the slot as used for this trimester
                                trimesterUsedSlots.get(trimester.getId()).add(selectedSlot.getId());

                                section.setSpaceTimeSlots(new HashSet<>(Collections.singleton(selectedSlot)));
                                sections.add(section);
                            } else {
                                log.warn("No available SpaceTime slots for subject {} in trimester {}", subject.getName(), trimester.getCode());
                            }
                        }
                    }

                    // Create courses
                    Map<UUID, Integer> sectionRegistrations = new HashMap<>(); // Keep track of registrations per section

                    for (Student student : students) {
                        for (Trimester trimester : trimesters) {
                            int courseCount = faker.number().numberBetween(3, 5);
                            List<Section> availableSections = new ArrayList<>(sections.stream()
                                                                                      .filter(s -> s.getTrimester()
                                                                                                    .equals(trimester))
                                                                                      .filter(s -> sectionRegistrations.getOrDefault(s.getId(), 0) < s.getMaxCapacity())
                                                                                      .toList());

                            Collections.shuffle(availableSections);

                            for (int i = 0; i < Math.min(courseCount, availableSections.size()); i++) {
                                Section section = availableSections.get(i);

                                Course course = new Course();
                                course.setCreatedBy("demo");

                                course.setStudent(student);
                                course.setSubject(section.getSubject());
                                course.setTrimester(trimester);

                                if (trimester.getStatus() == TrimesterStatus.COMPLETED) {
                                    course.setStatus(CourseStatus.COMPLETED);
                                    course.setSection(section);
                                    course.setGrade(faker.number().randomDouble(2, 2, 4));
                                    sectionRegistrations.merge(section.getId(), 1, Integer::sum);
                                } else if (trimester.getStatus() == TrimesterStatus.ONGOING) {
                                    course.setStatus(CourseStatus.REGISTERED);
                                    course.setSection(section);
                                    sectionRegistrations.merge(section.getId(), 1, Integer::sum);
                                } else if (faker.random().nextBoolean()) {
                                    course.setStatus(CourseStatus.SELECTED);
                                } else {
                                    course.setStatus(CourseStatus.DROPPED);
                                    course.setSection(section);
                                }

                                courses.add(course);
                            }
                        }
                    }

                    // Create notifications
                    for (NotificationCategory category : NotificationCategory.values()) {
                        for (NotificationScope scope : NotificationScope.values()) {
                            Notification notification = new Notification();
                            notification.setCreatedBy("demo");

                            notification.setSender(admins.get(faker.number().numberBetween(0, admins.size())));
                            notification.setTitle(faker.lorem().sentence());
                            notification.setContent(faker.lorem().paragraph());
                            notification.setCategory(category);
                            notification.setScope(scope);

                            switch (scope) {
                                case GLOBAL -> { /* No additional fields needed */ }
                                case TRIMESTER -> notification.setTrimester(trimesters.get(faker.number()
                                                                                                .numberBetween(0, trimesters.size())));
                                case SECTION -> notification.setSection(sections.get(faker.number()
                                                                                          .numberBetween(0, sections.size())));
                                case USER -> notification.setTargetUser(students.get(faker.number()
                                                                                          .numberBetween(0, students.size())));
                            }

                            notifications.add(notification);
                        }
                    }

                    // Now persist everything in the correct order
                    exec(session -> {
                        log.info("Persisting Admins...");
                        admins.forEach(session::persist);

                        log.info("Persisting Teachers...");
                        teachers.forEach(session::persist);

                        log.info("Persisting Students...");
                        students.forEach(session::persist);

                        log.info("Persisting Subjects...");
                        theorySubjects.forEach(session::persist);
                        labSubjects.forEach(session::persist);

                        log.info("Persisting Prerequisites...");
                        prerequisites.forEach(session::persist);

                        log.info("Persisting SpaceTime slots...");
                        spaceTimeSlots.forEach(session::persist);

                        log.info("Persisting Trimesters...");
                        trimesters.forEach(session::persist);

                        log.info("Persisting Sections...");
                        sections.forEach(session::persist);

                        log.info("Persisting Courses...");
                        courses.forEach(session::persist);

                        log.info("Persisting Notifications...");
                        notifications.forEach(session::persist);

                        session.flush();
                        return true;
                    }, "Persist All Entities");

                    log.info("Database reset and seed completed successfully");
                    return true;
                });
            } catch (Exception e) {
                log.error("Failed to reset and seed database: {}", e.getMessage(), e);
                return Completable.error(e);
            }
        }).subscribeOn(Schedulers.io());
    }
}
