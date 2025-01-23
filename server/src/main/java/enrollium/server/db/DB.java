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
                    List<Faculty>      teachers       = new ArrayList<>(); // TODO: add some sub
                    List<Student>      students       = new ArrayList<>();
                    List<Subject>      theorySubjects = new ArrayList<>();
                    List<Subject>      labSubjects    = new ArrayList<>();
                    List<Prerequisite> prerequisites  = new ArrayList<>();
                    List<SpaceTime>    spaceTimeSlots = new ArrayList<>();
                    List<Trimester>    trimesters     = new ArrayList<>();
                    List<Section>      sections       = new ArrayList<>(); // TODO: write more sections
                    List<Course>       courses        = new ArrayList<>(); // TODO: jst do it
                    List<Notification> notifications  = new ArrayList<>(); // TODO: generate some msg

                    // Create admins
                    Set<String>  usedEmails = new HashSet<>();
                    List<String> adminNames = Arrays.asList("Mir Mohammad Monir", "Fahad Rahman");
                    List<String> adminSC    = Arrays.asList("MMM", "FR");

                    for (int i = 0; i < adminSC.size(); i++) {
                        Faculty admin = new Faculty();
                        admin.setCreatedBy("demo");

                        // Use the predefined names and shortcodes for admins
                        String name      = adminNames.get(i);
                        String shortcode = adminSC.get(i);
                        String email     = String.format("%s@admin.uiu.ac.bd", shortcode.toLowerCase());

                        // Generate a valid password
                        String pass = "admin" + (i + 1) + "pass";

                        admin.setName(name);
                        admin.setShortcode(shortcode);
                        admin.setEmail(email);
                        admin.setPassword(pass);
                        admin.setInfo(pass);
                        admin.setType(UserType.ADMIN);
                        admins.add(admin);
                    }

                    //Faculty
//                    Set<String> usedFacultyEmails = new HashSet<>();
                    List<String> names = Arrays.asList("Dr. Md. Abul Kashem Mia", "Dr. Hasan Sarwar", "Dr. Mohammad Nurul Huda", "Dr. Khondaker Abdullah-Al-Mamun", "Dr. A.K.M. Muzahidul Islam", "Dr. Md. Motaharul Islam", "Dr. Dewan Md. Farid", "Dr. Al-Sakib Khan Pathan", "Dr. Mohammad Shahriar Rahman", "Dr. Md. Shohrab Hossain", "Dr. Muhammad Nomani Kabir", "Dr. Suman Ahmmed", "Dr. Jannatun Noor Mukta", "Dr. Riasat Azim", "Dr. Ohidujjaman", "Mohammad Mamun Elahi", "Rubaiya Rahtin Khan", "Md. Benzir Ahmed", "Nahid Hossain", "Sadia Islam", "Mir Moynuddin Ahmed Shibly", "Khushnur Binte Jahangir", "Minhajul Bashir", "Shoib Ahmed Shourav", "Nabila Sabrin Sworna", "Farhan Anan Himu", "Anika Tasnim Rodela", "Md. Mohaiminul Islam", "Fahim Hafiz", "Md. Romizul Islam", "Md. Tarek Hasan", "Samin Sharaf Somik", "Farhan Tanvir Utshaw", "Rahad Khan", "Iftekharul Abedeen", "Kazi Abdun Noor", "Md. Muhyminul Haque", "Md. Shadman Aadeeb", "Nusrat Jahan Tithi", "Umama Rahman", "Abdullah Al Jobair", "Raiyan Rahman", "Fahmid Al Rifat", "Sk. Md. Tauseef Tajwar", "Charles Aunkan Gomes", "Md. Shafqat Talukder", "Md. Tamzid Hossain", "Asif Ahmed Utsa", "Md. Tanvir Raihan", "Sidratul Muntaha", "Taki Yashir", "Md. Nafis Tahmid Akhand", "Md. Abid Hossain", "Asnuva Tanvin", "Tahmid Mosaddeque", "Tasmin Sanjida", "Rabeya Hossain", "Azizur Rahman Anik", "A.H.M. Osama Haque", "Abu Humayed Azim Fahmid", "Khandokar Md. Rahat Hossain", "Noman Asif Aditya", "Nabila Tasfiha Rahman", "Md. Mushfiqul Haque Omi", "Tanmoy Bipro Das", "Humaira Anzum Neha", "M. Fahmin Rahman", "Redwanul Mahbub Talukder", "Md. Irfanur Rahman Rafio", "Shekh Md. Saifur Rahman", "Shihab Ahmed", "Sidratul Tanzila Tasmi", "Sherajul Arifin", "Mobaswirul Islam", "Abdullah Ibne Masud Mahi", "Mahmudul Hasan");

                    List<String> emails = Arrays.asList("kashem@uiu.ac.bd", "hsarwar@cse.uiu.ac.bd", "mnh@cse.uiu.ac.bd", "mamun@cse.uiu.ac.bd", "muzahid@cse.uiu.ac.bd", "motaharul@cse.uiu.ac.bd", "dewanfarid@cse.uiu.ac.bd", "sakib@cse.uiu.ac.bd", "mshahriar@cse.uiu.ac.bd", "shohrab@cse.uiu.ac.bd", "kabir@cse.uiu.ac.bd", "suman@cse.uiu.ac.bd", "jannatun@cse.uiu.ac.bd", "riasat@cse.uiu.ac.bd", "ohidujjaman@cse.uiu.ac.bd", "mmelahi@cse.uiu.ac.bd", "rubaiya@cse.uiu.ac.bd", "benzir@cse.uiu.ac.bd", "nahid@cse.uiu.ac.bd", "sadia@cse.uiu.ac.bd", "moynuddin@cse.uiu.ac.bd", "khushnur@cse.uiu.ac.bd", "minhajul@cse.uiu.ac.bd", "shoib@cse.uiu.ac.bd", "nabila@cse.uiu.ac.bd", "himu@cse.uiu.ac.bd", "anika@cse.uiu.ac.bd", "mohaiminul@cse.uiu.ac.bd", "fahimhafiz@cse.uiu.ac.bd", "romizul@cse.uiu.ac.bd", "tarek@cse.uiu.ac.bd", "samin@cse.uiu.ac.bd", "farhan@cse.uiu.ac.bd", "rahad@cse.uiu.ac.bd", "iftekharul@cse.uiu.ac.bd", "abdunnoor@cse.uiu.ac.bd", "muhyminul@cse.uiu.ac.bd", "shadman@cse.uiu.ac.bd", "nusrat@cse.uiu.ac.bd", "umama@cse.uiu.ac.bd", "jobair@cse.uiu.ac.bd", "raiyan@cse.uiu.ac.bd", "fahmid@cse.uiu.ac.bd", "tauseef@cse.uiu.ac.bd", "charles@cse.uiu.ac.bd", "shafqat@cse.uiu.ac.bd", "tamzid@cse.uiu.ac.bd", "asif@cse.uiu.ac.bd", "tanvir@cse.uiu.ac.bd", "sidratul@cse.uiu.ac.bd", "taki@cse.uiu.ac.bd", "tahmid@cse.uiu.ac.bd", "abid@cse.uiu.ac.bd", "tanvin@cse.uiu.ac.bd", "mosaddeque@cse.uiu.ac.bd", "sanjida@cse.uiu.ac.bd", "rabeya@cse.uiu.ac.bd", "azizur@cse.uiu.ac.bd", "osama@cse.uiu.ac.bd", "humayed@cse.uiu.ac.bd", "rahat@cse.uiu.ac.bd", "aditya@cse.uiu.ac.bd", "tasfiha@cse.uiu.ac.bd", "mushfiqul@cse.uiu.ac.bd", "tanmoy@cse.uiu.ac.bd", "humaira@cse.uiu.ac.bd", "fahmin@cse.uiu.ac.bd", "redwanul@cse.uiu.ac.bd", "irfanur@cse.uiu.ac.bd", "saifur@cse.uiu.ac.bd", "shihab@cse.uiu.ac.bd", "tanzila@cse.uiu.ac.bd", "sherajul@cse.uiu.ac.bd", "mobaswirul@cse.uiu.ac.bd", "ibnemasud@cse.uiu.ac.bd", "mahmudul@cse.uiu.ac.bd");

                    List<String> shortCode = Arrays.asList("AKM", "HS", "MNH", "KAM", "AMI", "MMI", "DMF", "AKP", "MSR", "MSH", "MNK", "SAA", "JNM", "RA", "OJ", "MME", "RRK", "MBA", "NH", "SI", "MMAS", "KBJ", "MB", "SAS", "NSS", "FAH", "ATR", "MMI2", "FH", "MRI", "TH", "SSS", "FTU", "RK", "IA", "KAN", "MMH", "MSA", "NJT", "UR", "AAJ", "RR", "FAR", "SMT", "CAG", "MST", "MTH", "AAU", "MTR", "SM", "TY", "MNT", "MAH", "AT", "TM", "TS", "RH", "ARA", "AHM", "AHAF", "KRH", "NAA", "NTR", "MMO", "TBD", "HAN", "MFR", "RMT", "IRR", "SMSR", "SA", "STT", "SA2", "MI", "AIM", "MH");

                    for (int i = 0; i < shortCode.size(); i++) {
                        Faculty teacher = new Faculty();
                        teacher.setCreatedBy("demo");

                        // Generate the name and email
                        String name      = names.get(i);
                        String shortcode = shortCode.get(i);
                        String email     = emails.get(i);

                        String pass = "teacher" + (i + 1) + "pass";

                        teacher.setName(name);
                        teacher.setShortcode(shortcode);
                        teacher.setEmail(email);
                        teacher.setPassword(pass);
                        teacher.setInfo(pass);
                        teacher.setType(UserType.TEACHER);
                        teachers.add(teacher);
                    }
                    //Faculty Subject

                    //Student
                    for (int i = 0; i < 12; i++) {
                        Student student = new Student();
                        student.setCreatedBy("demo");

                        int universityId = 1234 + i;

                        String pass = "student" + universityId;

                        student.setName(faker.name().fullName());
                        student.setUniversityId(universityId);
                        student.setEmail(String.format("%s%d@student.uiu.ac.bd", student.getName()
                                                                                        .split(" ")[0].trim(), universityId));
                        student.setPassword(pass);
                        student.setInfo(pass);
                        students.add(student);
                    }

                    //TheorySubjects
                    List<String> theoryCourseCode = Arrays.asList("ENG1011", "BDS1201", "CSE2213", "ENG1013", "CSE1111", "MATH1151", "MATH2183", "CSE1325", "CSE1115", "MATH2201", "PHY2105", "EEE2113");

                    List<String> theoryCourseName = Arrays.asList("English–I", "History of the Emergence of Bangladesh", "Discrete Mathematics", "English–II", "Structured Programming Language", "Fundamental Calculus", "Calculus and Linear Algebra", "Digital Logic Design", "Object Oriented Programming", "Coordinate Geometry and Vector Analysis", "Physics", "Electrical Circuits");

                    for (int i = 0; i < theoryCourseCode.size(); i++) {
                        Subject subject = new Subject();
                        subject.setCreatedBy("demo");
                        subject.setName(theoryCourseName.get(i));  // Human-readable name
                        subject.setCodeName(theoryCourseCode.get(i)); // Use predefined codeName
                        subject.setCredits(3); // Example: Set 3 credits for theory courses
                        subject.setType(SubjectType.THEORY); // Set type to THEORY
                        theorySubjects.add(subject);
                    }

                    // LabSubjects
                    List<String> labCourseCode = Arrays.asList("CSE1110", "CSE1112", "CSE1326", "CSE1116", "PHY2106", "CSE2118");

                    List<String> labCourseName = Arrays.asList("Introduction to Computer Systems", "Structured Programming Language Laboratory", "Digital Logic Design Laboratory", "Object Oriented Programming Laboratory", "Physics Laboratory", "Advanced Object Oriented Programming Laboratory");

                    for (int i = 0; i < labCourseCode.size(); i++) {
                        Subject labSubject = new Subject();
                        labSubject.setCreatedBy("demo");
                        labSubject.setName(labCourseName.get(i));  // Human-readable name
                        labSubject.setCodeName(labCourseCode.get(i)); // Use predefined codeName
                        labSubject.setCredits(1); // Set credits for lab courses
                        labSubject.setType(SubjectType.LAB); // Set subject type
                        labSubjects.add(labSubject);
                    }

                    //Prerequisite
                    List<String[]> prerequisiteChains = Arrays.asList(new String[]{"ENG1013", "ENG1011"},       // ENG1013 prerequisite is ENG1011
                            new String[]{"CSE1111", "CSE1110"},       // CSE1111 prerequisite is CSE1110
                            new String[]{"CSE1112", "CSE1110"},       // CSE1112 prerequisite is CSE1110
                            new String[]{"MATH2183", "MATH1151"},     // MATH2183 prerequisite is MATH1151
                            new String[]{"CSE1115", "CSE1111"},       // CSE1115 prerequisite is CSE1111
                            new String[]{"CSE1116", "CSE1112"},       // CSE1116 prerequisite is CSE1112
                            new String[]{"MATH2201", "MATH1151"},     // MATH2201 prerequisite is MATH1151
                            new String[]{"CSE2118", "CSE1116"}        // CSE2118 prerequisite is CSE1116
                    );

                    List<Subject> allSubjects = Stream.concat(theorySubjects.stream(), labSubjects.stream()).toList();

                    for (String[] chain : prerequisiteChains) {
                        String subjectCode = chain[0]; // Main subject code
                        String prereqCode  = chain[1];  // Prerequisite subject code

                        Subject subject = allSubjects.stream()
                                                     .filter(s -> s.getCodeName().equals(subjectCode))
                                                     .findFirst()
                                                     .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectCode));

                        Subject prereqSubject = allSubjects.stream()
                                                           .filter(s -> s.getCodeName().equals(prereqCode))
                                                           .findFirst()
                                                           .orElseThrow(() -> new IllegalArgumentException("Prerequisite not found: " + prereqCode));

                        Prerequisite prerequisite = new Prerequisite();
                        prerequisite.setCreatedBy("demo");
                        prerequisite.setSubject(subject);
                        prerequisite.setPrerequisite(prereqSubject);
                        prerequisite.setMinimumGrade(0.0); // Example minimum grade
                        prerequisites.add(prerequisite);
                    }

                    // Create SpaceTime slots

                    // Create theory room slots
                    String[]        theoryRooms = {"201", "202", "203", "204", "205", "206", "207"};
                    List<DayOfWeek> allowedDays = Arrays.asList(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.THURSDAY);

                    int theoryCourseCounter = 1; // Counter for theory courses
                    for (String roomNumber : theoryRooms) {
                        for (DayOfWeek day : allowedDays) {
                            for (int slot = 1; slot <= 6; slot++) { // Theory courses have 6 slots per day
                                if (theoryCourseCounter <= 10) { // Ensure only 10 theory courses
                                    SpaceTime spaceTime = new SpaceTime();
                                    spaceTime.setCreatedBy("demo");

                                    spaceTime.setName("Theory Course " + theoryCourseCounter); // Dynamically name courses
                                    spaceTime.setRoomNumber(roomNumber); // Room number from the loop
                                    spaceTime.setRoomType(SubjectType.THEORY); // Mark as THEORY type
                                    spaceTime.setDayOfWeek(day);
                                    spaceTime.setTimeSlot(slot);

                                    spaceTimeSlots.add(spaceTime);

                                    theoryCourseCounter++; // Increment the course counter
                                }
                            }
                        }
                    }

                    // Create lab room slots
                    String[] labRooms         = {"Lab1", "Lab2", "Lab3", "Lab4", "Lab5", "Lab6"};
                    int      labCourseCounter = 1; // Counter for lab courses
                    for (String roomNumber : labRooms) {
                        for (DayOfWeek day : allowedDays) {
                            for (int slot = 1; slot <= 3; slot++) { // Lab courses have 3 slots per day
                                if (labCourseCounter <= 6) { // Ensure only 6 lab courses
                                    SpaceTime spaceTime = new SpaceTime();
                                    spaceTime.setCreatedBy("demo");

                                    spaceTime.setName("Lab Course " + labCourseCounter); // Dynamically name courses
                                    spaceTime.setRoomNumber(roomNumber); // Room number from the loop
                                    spaceTime.setRoomType(SubjectType.LAB); // Mark as LAB type
                                    spaceTime.setDayOfWeek(day);
                                    spaceTime.setTimeSlot(slot);

                                    spaceTimeSlots.add(spaceTime);

                                    labCourseCounter++; // Increment the lab course counter
                                }
                            }
                        }
                    }

                    LocalDateTime now = LocalDateTime.now();
                    for (int i = 0; i < 13; i++) { // Loop through 13 trimesters
                        Trimester trimester = new Trimester();
                        trimester.setCreatedBy("demo");

                        int    yearOffset = i / 3;
                        int    yearNum    = now.getYear() - 2 + yearOffset;
                        Season season     = Season.values()[i % 3];

                        trimester.setYear(yearNum);
                        trimester.setSeason(season);
                        trimester.setCode(Integer.parseInt((yearNum % 100) + "" + (season.ordinal() + 1)));

                        LocalDateTime baseDate = now.minusMonths((12 - i) * 4L); // Ensure chronological order
                        trimester.setCourseSelectionStart(baseDate.minusDays(14));
                        trimester.setCourseSelectionEnd(baseDate.minusDays(7));
                        trimester.setSectionRegistrationStart(baseDate.minusDays(6));
                        trimester.setSectionRegistrationEnd(baseDate.minusDays(1));

                        // Distribute statuses
                        if (i < 5) {
                            trimester.setStatus(TrimesterStatus.COMPLETED); // First 5 are COMPLETED
                        } else if (i == 5) {
                            trimester.setStatus(TrimesterStatus.ONGOING); // 6th is ONGOING
                        } else if (i < 9) {
                            trimester.setStatus(TrimesterStatus.SECTION_SELECTION); // 7th to 9th are SECTION_SELECTION
                        } else if (i < 11) {
                            trimester.setStatus(TrimesterStatus.COURSE_SELECTION); // 10th and 11th are COURSE_SELECTION
                        } else {
                            trimester.setStatus(TrimesterStatus.UPCOMING); // Last 2 are UPCOMING
                        }

                        trimesters.add(trimester);
                    }


                    for (Trimester trimester : trimesters) {
                        for (String courseCode : theoryCourseCode) {
                            Section section = new Section();

                            // Set section attributes
                            section.setName("Section for " + courseCode + " in " + trimester.getYear() + " " + trimester.getSeason());
                            section.setSection(courseCode); // Use course code as section identifier
                            section.setTrimester(trimester);
                            section.setMaxCapacity(30); // Default max capacity

                            // Create or assign a subject (replace with database retrieval if needed)
                            Subject subject = new Subject();
                            subject.setCodeName(courseCode); // Set course code to subject
                            section.setSubject(subject);

                            // Assign a faculty member based on the course code
                            Faculty assignedFaculty = teachers.get(Math.abs(courseCode.hashCode()) % teachers.size()); // Ensure index is positive
                            section.getTeachers().add(assignedFaculty);

                            // Dynamically create space-time slots for the section
//                            Set<SpaceTime> spaceTimeSlots = new HashSet<>();
//                            for (int i = 0; i < 3; i++) { // Create 3 space-time slots per section
//                                SpaceTime slot = new SpaceTime();
//                                slot.setDayOfWeek(DayOfWeek.of((i % 7) + 1)); // Rotate through days of the week
//                                slot.setStartTime(LocalDateTime.of(2025, 1, 1, 9 + i, 0)); // Example fixed date with dynamic times
//                                slot.setEndTime(LocalDateTime.of(2025, 1, 1, 10 + i, 0)); // Add 1 hour for each slot
//                                slot.setRoomNumber("Room " + (i + 101)); // Assign dynamic room numbers (e.g., Room 101, Room 102)
//                                slot.setRoomType(SubjectType.THEORY); // Set as THEORY type
//                                spaceTimeSlots.add(slot);
//                            }
//                            section.setSpaceTimeSlots(spaceTimeSlots);

                            // Save or process the section (currently printing for demonstration)
                            System.out.println("Created Section: " + section.getName());
                            System.out.println("Assigned Faculty: " + assignedFaculty.getName());
                            for (SpaceTime slot : spaceTimeSlots) {
                                System.out.println("SpaceTime Slot: " + slot.getDayOfWeek() + ", Room " + slot.getRoomNumber());
                            }
                        }
                    }




                    // Create courses
                    Map<UUID, Integer> sectionRegistrations = new HashMap<>();

                    for (Student student : students) {
                        for (Trimester trimester : trimesters) {
                            int courseCount = faker.number().numberBetween(3, 5);
                            List<Section> availableSections = new ArrayList<>(sections.stream()
                                                                                      .filter(s -> s.getTrimester()
                                                                                                    .equals(trimester)) // Match trimester
                                                                                      .filter(s -> sectionRegistrations.getOrDefault(s.getId(), 0) < s.getMaxCapacity()) // Check capacity
                                                                                      .toList());

                            Collections.shuffle(availableSections);

                            for (int i = 0; i < Math.min(courseCount, availableSections.size()); i++) {
                                Section section = availableSections.get(i);

                                Course course = new Course();
                                course.setCreatedBy("demo");
                                course.setStudent(student);
                                course.setSubject(section.getSubject());
                                course.setTrimester(trimester);

                                switch (trimester.getStatus()) {
                                    case COMPLETED -> {
                                        course.setStatus(CourseStatus.COMPLETED);
                                        course.setSection(section);
                                        course.setGrade(faker.number().randomDouble(2, 2, 4));
                                        if (course.getGrade() < 0.0 || course.getGrade() > 4.0) {
                                            throw new IllegalArgumentException("Grade must be between 0.0 and 4.0 for COMPLETED courses");
                                        }
                                    }
                                    case ONGOING -> {
                                        course.setStatus(CourseStatus.REGISTERED);
                                        course.setSection(section);
                                    }
                                    default -> {
                                        if (faker.random().nextBoolean()) {
                                            course.setStatus(CourseStatus.SELECTED);
                                            course.setSection(null);
                                            course.setGrade(null);
                                        } else {
                                            course.setStatus(CourseStatus.DROPPED);
                                            course.setSection(section);
                                            course.setGrade(null);
                                        }
                                    }
                                }

                                // Update section registrations if the course is REGISTERED or COMPLETED
                                if (course.getStatus() == CourseStatus.REGISTERED || course.getStatus() == CourseStatus.COMPLETED) {
                                    sectionRegistrations.merge(section.getId(), 1, Integer::sum);
                                }

                                courses.add(course);
                            }
                        }
                    }

                    // Create notifications
                    Map<NotificationScope, String> scopeTitles = Map.of(NotificationScope.GLOBAL, "System Maintenance Scheduled", NotificationScope.TRIMESTER, "New Trimester Registration Open", NotificationScope.SECTION, "Section A Meeting Reminder", NotificationScope.USER, "Personal Reminder for Submission");

                    Map<NotificationCategory, String> categoryContent = Map.of(NotificationCategory.URGENT, "Please note that immediate action is required regarding this notification.", NotificationCategory.ACADEMIC, "Important academic update. Please review the details provided.", NotificationCategory.ADMINISTRATIVE, "Administrative update: Please review and take necessary actions.", NotificationCategory.GENERAL, "This is a general notification. Please stay informed.");

                    for (NotificationCategory category : NotificationCategory.values()) {
                        for (NotificationScope scope : NotificationScope.values()) {
                            Notification notification = new Notification();
                            notification.setCreatedBy("demo");

                            notification.setSender(admins.get(faker.number().numberBetween(0, admins.size())));
                            notification.setTitle(scopeTitles.get(scope)); // Use meaningful titles based on the scope
                            notification.setContent(categoryContent.get(category)); // Assign meaningful content based on category
                            notification.setCategory(category);
                            notification.setScope(scope);

                            // Assign scope-specific fields
                            if (scope == NotificationScope.TRIMESTER) {
                                notification.setTrimester(trimesters.get(faker.number()
                                                                              .numberBetween(0, trimesters.size())));
                            } else if (scope == NotificationScope.SECTION) {
                                notification.setSection(sections.get(faker.number().numberBetween(0, sections.size())));
                            } else if (scope == NotificationScope.USER) {
                                notification.setTargetUser(students.get(faker.number()
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
