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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
    public static final     String         DEMOPASS = "demopass";
    // volatile to make it only run once across all the threads
    // https://www.geeksforgeeks.org/volatile-keyword-in-java/
    private static final    Faker          faker    = new Faker();
    private static final    Logger         log      = LoggerFactory.getLogger(DB.class);
    private static final    Random         random   = new Random();
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
                        String[] tables = {"Course", "Notification", "Section", "Prerequisite", "SpaceTime", "Faculty", "Subject", "Trimester", "Student", "User"};

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

                    record FacultyInfo(String name, String email, String shortcode) {}
                    record StudentInfo(String name, int universityId, String email) {}
                    record SubjectInfo(String code, String name, int credits) {}
                    record PrerequisiteChain(String subjectCode, String prerequisiteCode) {}
                    record RoomInfo(String roomNumber, String name) {}
                    record TrimesterInfo(Integer year, Season season, TrimesterStatus status, LocalDateTime courseSelectionStart, LocalDateTime courseSelectionEnd, LocalDateTime sectionRegistrationStart, LocalDateTime sectionRegistrationEnd) {}
                    record NotificationInfo(User sender, String title, String content, NotificationCategory category, NotificationScope scope, Trimester trimester, Section section, User targetUser) {}
                    record CourseInfo(Student student, Subject subject, Trimester trimester, Section section, CourseStatus status, Double grade) {}

                    Faculty demoAdmin = new Faculty();
                    demoAdmin.setEmail("admin@uiu.ac.bd");
                    demoAdmin.setName("Demo Admin");
                    demoAdmin.setPassword("demoAdm1nPa$$");
                    demoAdmin.setType(UserType.ADMIN);
                    demoAdmin.setShortcode("ADMIN");
                    demoAdmin.setInfo("demoAdm1nPa$$");
                    admins.add(demoAdmin);

                    Student demoStudent = new Student();
                    demoStudent.setEmail("demo.student@uiu.ac.bd");
                    demoStudent.setName("Demo Student");
                    demoStudent.setPassword("demo$tudentP4ss");
                    demoStudent.setUniversityId(111111111);
                    demoStudent.setType(UserType.STUDENT);
                    demoStudent.setInfo("demo$tudentP4ss");
                    students.add(demoStudent);

                    List<FacultyInfo> adminInfos = List.of(new FacultyInfo("Mir Mohammad Monir", "mir.mohammad.monir@uiu.ac.bd", "MMM"), new FacultyInfo("Fahad Rahman", "fahad.rahman@uiu.ac.bd", "FR"));

                    for (FacultyInfo adminInfo : adminInfos) {
                        Faculty admin = new Faculty();
                        admin.setCreatedBy("demo");

                        admin.setName(adminInfo.name);
                        admin.setShortcode(adminInfo.shortcode);
                        admin.setEmail(adminInfo.email);
                        admin.setPassword(DEMOPASS);
                        admin.setInfo(DEMOPASS);
                        admin.setType(UserType.ADMIN);

                        admins.add(admin);
                    }

                    List<FacultyInfo> teacherInfos = List.of(new FacultyInfo("Dr. Md. Abul Kashem Mia", "kashem@uiu.ac.bd", "AKM"), new FacultyInfo("Dr. Hasan Sarwar", "hsarwar@cse.uiu.ac.bd", "HS"), new FacultyInfo("Dr. Mohammad Nurul Huda", "mnh@cse.uiu.ac.bd", "MNH"), new FacultyInfo("Dr. Khondaker Abdullah-Al-Mamun", "mamun@cse.uiu.ac.bd", "KAM"), new FacultyInfo("Dr. A.K.M. Muzahidul Islam", "muzahid@cse.uiu.ac.bd", "AMI"), new FacultyInfo("Dr. Md. Motaharul Islam", "motaharul@cse.uiu.ac.bd", "MMI"), new FacultyInfo("Dr. Dewan Md. Farid", "dewanfarid@cse.uiu.ac.bd", "DMF"), new FacultyInfo("Dr. Al-Sakib Khan Pathan", "sakib@cse.uiu.ac.bd", "AKP"), new FacultyInfo("Dr. Mohammad Shahriar Rahman", "mshahriar@cse.uiu.ac.bd", "MSR"), new FacultyInfo("Dr. Md. Shohrab Hossain", "shohrab@cse.uiu.ac.bd", "MSH"), new FacultyInfo("Dr. Muhammad Nomani Kabir", "kabir@cse.uiu.ac.bd", "MNK"), new FacultyInfo("Dr. Suman Ahmmed", "suman@cse.uiu.ac.bd", "SAA"), new FacultyInfo("Dr. Jannatun Noor Mukta", "jannatun@cse.uiu.ac.bd", "JNM"), new FacultyInfo("Dr. Riasat Azim", "riasat@cse.uiu.ac.bd", "RA"), new FacultyInfo("Dr. Ohidujjaman", "ohidujjaman@cse.uiu.ac.bd", "OJ"), new FacultyInfo("Mohammad Mamun Elahi", "mmelahi@cse.uiu.ac.bd", "MME"), new FacultyInfo("Rubaiya Rahtin Khan", "rubaiya@cse.uiu.ac.bd", "RRK"), new FacultyInfo("Md. Benzir Ahmed", "benzir@cse.uiu.ac.bd", "MBA"), new FacultyInfo("Nahid Hossain", "nahid@cse.uiu.ac.bd", "NH"), new FacultyInfo("Sadia Islam", "sadia@cse.uiu.ac.bd", "SI"), new FacultyInfo("Mir Moynuddin Ahmed Shibly", "moynuddin@cse.uiu.ac.bd", "MMAS"), new FacultyInfo("Khushnur Binte Jahangir", "khushnur@cse.uiu.ac.bd", "KBJ"), new FacultyInfo("Minhajul Bashir", "minhajul@cse.uiu.ac.bd", "MB"), new FacultyInfo("Shoib Ahmed Shourav", "shoib@cse.uiu.ac.bd", "SAS"), new FacultyInfo("Nabila Sabrin Sworna", "nabila@cse.uiu.ac.bd", "NSS"), new FacultyInfo("Farhan Anan Himu", "himu@cse.uiu.ac.bd", "FAH"), new FacultyInfo("Anika Tasnim Rodela", "anika@cse.uiu.ac.bd", "ATR"), new FacultyInfo("Md. Mohaiminul Islam", "mohaiminul@cse.uiu.ac.bd", "MMI2"), new FacultyInfo("Fahim Hafiz", "fahimhafiz@cse.uiu.ac.bd", "FH"), new FacultyInfo("Md. Romizul Islam", "romizul@cse.uiu.ac.bd", "MRI"), new FacultyInfo("Md. Tarek Hasan", "tarek@cse.uiu.ac.bd", "TH"), new FacultyInfo("Samin Sharaf Somik", "samin@cse.uiu.ac.bd", "SSS"), new FacultyInfo("Farhan Tanvir Utshaw", "farhan@cse.uiu.ac.bd", "FTU"), new FacultyInfo("Rahad Khan", "rahad@cse.uiu.ac.bd", "RK"), new FacultyInfo("Iftekharul Abedeen", "iftekharul@cse.uiu.ac.bd", "IA"), new FacultyInfo("Kazi Abdun Noor", "abdunnoor@cse.uiu.ac.bd", "KAN"), new FacultyInfo("Md. Muhyminul Haque", "muhyminul@cse.uiu.ac.bd", "MMH"), new FacultyInfo("Md. Shadman Aadeeb", "shadman@cse.uiu.ac.bd", "MSA"), new FacultyInfo("Nusrat Jahan Tithi", "nusrat@cse.uiu.ac.bd", "NJT"), new FacultyInfo("Umama Rahman", "umama@cse.uiu.ac.bd", "UR"), new FacultyInfo("Abdullah Al Jobair", "jobair@cse.uiu.ac.bd", "AAJ"), new FacultyInfo("Raiyan Rahman", "raiyan@cse.uiu.ac.bd", "RR"), new FacultyInfo("Fahmid Al Rifat", "fahmid@cse.uiu.ac.bd", "FAR"), new FacultyInfo("Sk. Md. Tauseef Tajwar", "tauseef@cse.uiu.ac.bd", "SMT"), new FacultyInfo("Charles Aunkan Gomes", "charles@cse.uiu.ac.bd", "CAG"), new FacultyInfo("Md. Shafqat Talukder", "shafqat@cse.uiu.ac.bd", "MST"), new FacultyInfo("Md. Tamzid Hossain", "tamzid@cse.uiu.ac.bd", "MTH"), new FacultyInfo("Asif Ahmed Utsa", "asif@cse.uiu.ac.bd", "AAU"), new FacultyInfo("Md. Tanvir Raihan", "tanvir@cse.uiu.ac.bd", "MTR"), new FacultyInfo("Sidratul Muntaha", "sidratul@cse.uiu.ac.bd", "SM"), new FacultyInfo("Taki Yashir", "taki@cse.uiu.ac.bd", "TY"), new FacultyInfo("Md. Nafis Tahmid Akhand", "tahmid@cse.uiu.ac.bd", "MNT"), new FacultyInfo("Md. Abid Hossain", "abid@cse.uiu.ac.bd", "MAH"), new FacultyInfo("Asnuva Tanvin", "tanvin@cse.uiu.ac.bd", "AT"), new FacultyInfo("Tahmid Mosaddeque", "mosaddeque@cse.uiu.ac.bd", "TM"), new FacultyInfo("Tasmin Sanjida", "sanjida@cse.uiu.ac.bd", "TS"), new FacultyInfo("Rabeya Hossain", "rabeya@cse.uiu.ac.bd", "RH"), new FacultyInfo("Azizur Rahman Anik", "azizur@cse.uiu.ac.bd", "ARA"), new FacultyInfo("A.H.M. Osama Haque", "osama@cse.uiu.ac.bd", "AHM"), new FacultyInfo("Abu Humayed Azim Fahmid", "humayed@cse.uiu.ac.bd", "AHAF"), new FacultyInfo("Khandokar Md. Rahat Hossain", "rahat@cse.uiu.ac.bd", "KRH"), new FacultyInfo("Noman Asif Aditya", "aditya@cse.uiu.ac.bd", "NAA"), new FacultyInfo("Nabila Tasfiha Rahman", "tasfiha@cse.uiu.ac.bd", "NTR"), new FacultyInfo("Md. Mushfiqul Haque Omi", "mushfiqul@cse.uiu.ac.bd", "MMO"), new FacultyInfo("Tanmoy Bipro Das", "tanmoy@cse.uiu.ac.bd", "TBD"), new FacultyInfo("Humaira Anzum Neha", "humaira@cse.uiu.ac.bd", "HAN"), new FacultyInfo("M. Fahmin Rahman", "fahmin@cse.uiu.ac.bd", "MFR"), new FacultyInfo("Redwanul Mahbub Talukder", "redwanul@cse.uiu.ac.bd", "RMT"), new FacultyInfo("Md. Irfanur Rahman Rafio", "irfanur@cse.uiu.ac.bd", "IRR"), new FacultyInfo("Shekh Md. Saifur Rahman", "saifur@cse.uiu.ac.bd", "SMSR"), new FacultyInfo("Shihab Ahmed", "shihab@cse.uiu.ac.bd", "SA"), new FacultyInfo("Sidratul Tanzila Tasmi", "tanzila@cse.uiu.ac.bd", "STT"), new FacultyInfo("Sherajul Arifin", "sherajul@cse.uiu.ac.bd", "SA2"), new FacultyInfo("Mobaswirul Islam", "mobaswirul@cse.uiu.ac.bd", "MI"), new FacultyInfo("Abdullah Ibne Masud Mahi", "ibnemasud@cse.uiu.ac.bd", "AIM"), new FacultyInfo("Mahmudul Hasan", "mahmudul@cse.uiu.ac.bd", "MH"));

                    for (FacultyInfo teacherInfo : teacherInfos) {
                        Faculty teacher = new Faculty();
                        teacher.setCreatedBy("demo");

                        teacher.setName(teacherInfo.name);
                        teacher.setShortcode(teacherInfo.shortcode);
                        teacher.setEmail(teacherInfo.email);
                        teacher.setPassword(DEMOPASS);
                        teacher.setInfo(DEMOPASS);
                        teacher.setType(UserType.TEACHER);

                        teachers.add(teacher);
                    }

                    AtomicInteger     uniId        = new AtomicInteger(112330000);
                    List<StudentInfo> studentInfos = new ArrayList<>();

                    for (int i = 0; i < 10; i++) {
                        int    universityId = uniId.addAndGet(random.nextInt(1, 10));
                        String firstName    = faker.name().firstName();
                        String lastName     = faker.name().lastName();
                        String name         = firstName + " " + lastName;
                        String email        = String.format("%s.%d@bscse.uiu.ac.bd", (firstName.charAt(0) + lastName).toLowerCase(), universityId);
                        studentInfos.add(new StudentInfo(name, universityId, email));
                    }

                    for (StudentInfo studentInfo : studentInfos) {
                        Student student = new Student();
                        student.setCreatedBy("demo");

                        student.setName(studentInfo.name);
                        student.setUniversityId(studentInfo.universityId);
                        student.setEmail(studentInfo.email);
                        student.setPassword(DEMOPASS);
                        student.setInfo(DEMOPASS);

                        students.add(student);
                    }

                    List<SubjectInfo> theoryCourses = List.of(new SubjectInfo("ENG1011", "English–I", 3), new SubjectInfo("BDS1201", "History of the Emergence of Bangladesh", 3), new SubjectInfo("CSE2213", "Discrete Mathematics", 3), new SubjectInfo("ENG1013", "English–II", 3), new SubjectInfo("CSE1111", "Structured Programming Language", 3), new SubjectInfo("MATH1151", "Fundamental Calculus", 3), new SubjectInfo("MATH2183", "Calculus and Linear Algebra", 3), new SubjectInfo("CSE1325", "Digital Logic Design", 3), new SubjectInfo("CSE1115", "Object Oriented Programming", 3), new SubjectInfo("MATH2201", "Coordinate Geometry and Vector Analysis", 3), new SubjectInfo("PHY2105", "Physics", 3), new SubjectInfo("EEE2113", "Electrical Circuits", 3));
                    List<SubjectInfo> labCourses    = List.of(new SubjectInfo("CSE1110", "Introduction to Computer Systems", 1), new SubjectInfo("CSE1112", "Structured Programming Language Laboratory", 1), new SubjectInfo("CSE1326", "Digital Logic Design Laboratory", 1), new SubjectInfo("CSE1116", "Object Oriented Programming Laboratory", 1), new SubjectInfo("PHY2106", "Physics Laboratory", 1), new SubjectInfo("CSE2118", "Advanced Object Oriented Programming Laboratory", 1));

                    for (SubjectInfo subjectInfo : theoryCourses) {
                        Subject subject = new Subject();
                        subject.setCreatedBy("demo");

                        subject.setName(subjectInfo.name);
                        subject.setCodeName(subjectInfo.code);
                        subject.setCredits(subjectInfo.credits);
                        subject.setType(SubjectType.THEORY);

                        theorySubjects.add(subject);
                    }

                    for (SubjectInfo subjectInfo : labCourses) {
                        Subject labSubject = new Subject();
                        labSubject.setCreatedBy("demo");

                        labSubject.setName(subjectInfo.name);
                        labSubject.setCodeName(subjectInfo.code);
                        labSubject.setCredits(subjectInfo.credits);
                        labSubject.setType(SubjectType.LAB);

                        labSubjects.add(labSubject);
                    }

                    List<Subject> allSubjects = new ArrayList<>(Stream.concat(theorySubjects.stream(), labSubjects.stream())
                                                                      .toList());
                    List<PrerequisiteChain> prerequisiteChains = List.of(new PrerequisiteChain("ENG1013", "ENG1011"), new PrerequisiteChain("CSE1111", "CSE1110"), new PrerequisiteChain("CSE1112", "CSE1110"), new PrerequisiteChain("MATH2183", "MATH1151"), new PrerequisiteChain("CSE1115", "CSE1111"), new PrerequisiteChain("CSE1116", "CSE1112"), new PrerequisiteChain("MATH2201", "MATH1151"), new PrerequisiteChain("CSE2118", "CSE1116"));

                    for (PrerequisiteChain chain : prerequisiteChains) {
                        Subject subject = allSubjects.stream()
                                                     .filter(s -> s.getCodeName().equals(chain.subjectCode))
                                                     .findFirst()
                                                     .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + chain.subjectCode));

                        Subject prerequisiteSubject = allSubjects.stream()
                                                                 .filter(s -> s.getCodeName()
                                                                               .equals(chain.prerequisiteCode))
                                                                 .findFirst()
                                                                 .orElseThrow(() -> new IllegalArgumentException("Prerequisite not found: " + chain.prerequisiteCode));

                        Prerequisite prerequisite = new Prerequisite();
                        prerequisite.setCreatedBy("demo");

                        prerequisite.setSubject(subject);
                        prerequisite.setPrerequisite(prerequisiteSubject);
                        prerequisite.setMinimumGrade(1.0);

                        prerequisites.add(prerequisite);
                    }

                    Collections.shuffle(allSubjects);
                    for (Faculty teacher : teachers) {
                        teacher.setTeachableSubjects(new HashSet<Subject>(allSubjects.subList(0, random.nextInt(3) + 1)));
                    }

                    List<RoomInfo> theoryRooms = List.of(new RoomInfo("201", "Class room 201"), new RoomInfo("202", "Class room 202"), new RoomInfo("203", "Class room 203"), new RoomInfo("204", "Class room 204"), new RoomInfo("205", "Class room 205"), new RoomInfo("206", "Class room 206"), new RoomInfo("207", "Class room 207"));

                    List<RoomInfo> labRooms = List.of(new RoomInfo("Lab1", "Lab 1"), new RoomInfo("Lab2", "Lab 2"), new RoomInfo("Lab3", "Lab 3"), new RoomInfo("Lab4", "Lab 4"), new RoomInfo("Lab5", "Lab 5"), new RoomInfo("Lab6", "Lab 6"));

                    List<List<SpaceTime>> theoryPairs = new ArrayList<>();
                    List<SpaceTime>       labSlots    = new ArrayList<>();

                    for (int i = 0; i < theoryRooms.size(); i++) {
                        for (int slot = 1; slot <= 6; slot++) {
                            // Create SAT-TUE pairs
                            SpaceTime satSlot = new SpaceTime();
                            satSlot.setCreatedBy("demo");
                            satSlot.setName(theoryRooms.get(i).name);
                            satSlot.setRoomNumber(theoryRooms.get(i).roomNumber);
                            satSlot.setRoomType(SubjectType.THEORY);
                            satSlot.setDayOfWeek(DayOfWeek.SATURDAY);
                            satSlot.setTimeSlot(slot);

                            SpaceTime tueSlot = new SpaceTime();
                            tueSlot.setCreatedBy("demo");
                            tueSlot.setName(theoryRooms.get(i).name);
                            tueSlot.setRoomNumber(theoryRooms.get(i).roomNumber);
                            tueSlot.setRoomType(SubjectType.THEORY);
                            tueSlot.setDayOfWeek(DayOfWeek.TUESDAY);
                            tueSlot.setTimeSlot(slot);

                            theoryPairs.add(List.of(satSlot, tueSlot));

                            // Create SUN-WED pairs
                            SpaceTime sunSlot = new SpaceTime();
                            sunSlot.setCreatedBy("demo");
                            sunSlot.setName(theoryRooms.get(i).name);
                            sunSlot.setRoomNumber(theoryRooms.get(i).roomNumber);
                            sunSlot.setRoomType(SubjectType.THEORY);
                            sunSlot.setDayOfWeek(DayOfWeek.SUNDAY);
                            sunSlot.setTimeSlot(slot);

                            SpaceTime wedSlot = new SpaceTime();
                            wedSlot.setCreatedBy("demo");
                            wedSlot.setName(theoryRooms.get(i).name);
                            wedSlot.setRoomNumber(theoryRooms.get(i).roomNumber);
                            wedSlot.setRoomType(SubjectType.THEORY);
                            wedSlot.setDayOfWeek(DayOfWeek.WEDNESDAY);
                            wedSlot.setTimeSlot(slot);

                            theoryPairs.add(List.of(sunSlot, wedSlot));

                            spaceTimeSlots.addAll(List.of(satSlot, sunSlot, tueSlot, wedSlot));
                        }
                    }

                    for (RoomInfo roomInfo : labRooms) {
                        for (int slot = 1; slot <= 3; slot++) {
                            for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)) {
                                SpaceTime spaceTime = new SpaceTime();
                                spaceTime.setCreatedBy("demo");
                                spaceTime.setName(roomInfo.name);
                                spaceTime.setRoomNumber(roomInfo.roomNumber);
                                spaceTime.setRoomType(SubjectType.LAB);
                                spaceTime.setDayOfWeek(day);
                                spaceTime.setTimeSlot(slot);

                                labSlots.add(spaceTime);
                                spaceTimeSlots.add(spaceTime);
                            }
                        }
                    }

                    List<TrimesterInfo> trimesterInfos = List.of( //

                            // Completed Trimesters
                            new TrimesterInfo(2022, Season.SPRING, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2021, 12, 5, 9, 0), //
                                    LocalDateTime.of(2021, 12, 15, 9, 0), //
                                    LocalDateTime.of(2021, 12, 22, 9, 0), //
                                    LocalDateTime.of(2021, 12, 25, 9, 0)), //

                            new TrimesterInfo(2022, Season.SUMMER, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2022, 4, 5, 9, 0), //
                                    LocalDateTime.of(2022, 4, 15, 9, 0), //
                                    LocalDateTime.of(2022, 4, 22, 9, 0), //
                                    LocalDateTime.of(2022, 4, 25, 9, 0)), //

                            new TrimesterInfo(2022, Season.FALL, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2022, 8, 5, 9, 0), //
                                    LocalDateTime.of(2022, 8, 15, 9, 0), //
                                    LocalDateTime.of(2022, 8, 22, 9, 0), //
                                    LocalDateTime.of(2022, 8, 25, 9, 0)), //

                            new TrimesterInfo(2023, Season.SPRING, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2022, 12, 5, 9, 0), //
                                    LocalDateTime.of(2022, 12, 15, 9, 0), //
                                    LocalDateTime.of(2022, 12, 22, 9, 0), //
                                    LocalDateTime.of(2022, 12, 25, 9, 0)), //

                            new TrimesterInfo(2023, Season.SUMMER, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2023, 4, 5, 9, 0), //
                                    LocalDateTime.of(2023, 4, 15, 9, 0), //
                                    LocalDateTime.of(2023, 4, 22, 9, 0), //
                                    LocalDateTime.of(2023, 4, 25, 9, 0)), //

                            new TrimesterInfo(2023, Season.FALL, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2023, 8, 5, 9, 0), //
                                    LocalDateTime.of(2023, 8, 15, 9, 0), //
                                    LocalDateTime.of(2023, 8, 22, 9, 0), //
                                    LocalDateTime.of(2023, 8, 25, 9, 0)), //

                            new TrimesterInfo(2024, Season.SPRING, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2023, 12, 5, 9, 0), //
                                    LocalDateTime.of(2023, 12, 15, 9, 0), //
                                    LocalDateTime.of(2023, 12, 22, 9, 0), //
                                    LocalDateTime.of(2023, 12, 25, 9, 0)), //

                            new TrimesterInfo(2024, Season.SUMMER, TrimesterStatus.COMPLETED, //
                                    LocalDateTime.of(2024, 4, 5, 9, 0), //
                                    LocalDateTime.of(2024, 4, 15, 9, 0), //
                                    LocalDateTime.of(2024, 4, 22, 9, 0), //
                                    LocalDateTime.of(2024, 4, 25, 9, 0)), //

                            // Ongoing trimester
                            new TrimesterInfo(2024, Season.FALL, TrimesterStatus.ONGOING, //
                                    LocalDateTime.of(2024, 8, 5, 9, 0), //
                                    LocalDateTime.of(2024, 8, 15, 9, 0), //
                                    LocalDateTime.of(2024, 8, 22, 9, 0), //
                                    LocalDateTime.of(2024, 8, 25, 9, 0)), //

                            // Section Selection trimester
                            new TrimesterInfo(2025, Season.SPRING, TrimesterStatus.SECTION_SELECTION, //
                                    LocalDateTime.of(2024, 12, 5, 9, 0), //
                                    LocalDateTime.of(2024, 12, 15, 9, 0), //
                                    LocalDateTime.of(2024, 12, 22, 9, 0), //
                                    LocalDateTime.of(2024, 12, 25, 9, 0)), //

                            // Upcoming Trimesters
                            new TrimesterInfo(2025, Season.SUMMER, TrimesterStatus.UPCOMING, //
                                    LocalDateTime.of(2025, 4, 1, 9, 0), null, null, null),

                            new TrimesterInfo(2025, Season.FALL, TrimesterStatus.UPCOMING, //
                                    LocalDateTime.of(2025, 8, 1, 9, 0), null, null, null),

                            new TrimesterInfo(2026, Season.SPRING, TrimesterStatus.UPCOMING, //
                                    LocalDateTime.of(2025, 12, 1, 9, 0), null, null, null) //
                    );

                    for (TrimesterInfo trimesterInfo : trimesterInfos) {
                        Trimester trimester = new Trimester();
                        trimester.setCreatedBy("demo");

                        int lastTwoDigitsOfYear = trimesterInfo.year % 100;
                        int trimesterNumber = switch (trimesterInfo.season) {
                            case SPRING -> 1;
                            case SUMMER -> 2;
                            case FALL -> 3;
                        };
                        Integer code = lastTwoDigitsOfYear * 10 + trimesterNumber;

                        trimester.setCode(code);
                        trimester.setYear(trimesterInfo.year);
                        trimester.setSeason(trimesterInfo.season);
                        trimester.setStatus(trimesterInfo.status);
                        trimester.setCourseSelectionStart(trimesterInfo.courseSelectionStart);
                        trimester.setCourseSelectionEnd(trimesterInfo.courseSelectionEnd);
                        trimester.setSectionRegistrationStart(trimesterInfo.sectionRegistrationStart);
                        trimester.setSectionRegistrationEnd(trimesterInfo.sectionRegistrationEnd);

                        trimesters.add(trimester);
                    }

                    AtomicInteger theoryIndex = new AtomicInteger(0);
                    AtomicInteger labIndex    = new AtomicInteger(0);

                    Collections.shuffle(theoryPairs);
                    Collections.shuffle(labSlots);

                    for (Trimester trimester : trimesters) {
                        if (trimester.getStatus() == TrimesterStatus.ONGOING || trimester.getStatus() == TrimesterStatus.SECTION_SELECTION) {

                            for (Subject subject : theorySubjects) {
                                char sectionName = 'A';
                                for (int i = 0; i < random.nextInt(6, 11); i++) {
                                    Section section = new Section();
                                    section.setCreatedBy("demo");

                                    section.setSection(String.valueOf(sectionName++));
                                    section.setName(subject.getName() + " - " + section.getSection() + " - " + trimester.getCode());
                                    section.setSubject(subject);
                                    section.setTrimester(trimester);
                                    section.setMaxCapacity(10);
                                    section.getTeachers().add(teachers.get(random.nextInt(teachers.size())));

                                    if (theoryPairs.size() > theoryIndex.get()) {
                                        section.getSpaceTimeSlots()
                                               .addAll(theoryPairs.get(theoryIndex.getAndIncrement()));
                                    } else {
                                        log.warn("No more theory sections left for {} - {}", subject.getName(), trimester.getCode());
                                        break;
                                    }

                                    sections.add(section);
                                }
                            }

                            for (Subject subject : labSubjects) {
                                char sectionName = 'A';
                                for (int i = 0; i < random.nextInt(6, 11); i++) {
                                    Section section = new Section();
                                    section.setCreatedBy("demo");

                                    section.setSection(String.valueOf(sectionName++));
                                    section.setName(subject.getName() + " - " + section.getSection() + " - " + trimester.getCode());
                                    section.setSubject(subject);
                                    section.setTrimester(trimester);
                                    section.setMaxCapacity(7);
                                    section.getTeachers().add(teachers.get(random.nextInt(teachers.size())));

                                    if (labSlots.size() > labIndex.get()) {
                                        section.getSpaceTimeSlots().add(labSlots.get(labIndex.getAndIncrement()));
                                    } else {
                                        log.warn("No more lab sections left for {} - {}", subject.getName(), trimester.getCode());
                                        break;
                                    }

                                    sections.add(section);
                                }
                            }
                        }
                    }

                    List<NotificationInfo> notificationInfos = List.of(new NotificationInfo(demoAdmin, "Global Announcement", "This is a global announcement.", NotificationCategory.GENERAL, NotificationScope.GLOBAL, null, null, null), new NotificationInfo(demoAdmin, "Trimester Update", "This is a trimester-specific update.", NotificationCategory.ACADEMIC, NotificationScope.TRIMESTER, trimesters.get(random.nextInt(trimesters.size())), null, null), new NotificationInfo(demoAdmin, "Section Notice", "This is a section-specific notice.", NotificationCategory.ADMINISTRATIVE, NotificationScope.SECTION, null, sections.get(random.nextInt(sections.size())), null), new NotificationInfo(demoAdmin, "User Alert", "This is a user-specific alert.", NotificationCategory.URGENT, NotificationScope.USER, null, null, demoStudent), new NotificationInfo(demoAdmin, "Global Reminder", "This is a global reminder.", NotificationCategory.GENERAL, NotificationScope.GLOBAL, null, null, null), new NotificationInfo(demoAdmin, "Trimester Reminder", "This is a trimester-specific reminder.", NotificationCategory.ACADEMIC, NotificationScope.TRIMESTER, trimesters.get(random.nextInt(trimesters.size())), null, null), new NotificationInfo(demoAdmin, "Section Reminder", "This is a section-specific reminder.", NotificationCategory.ADMINISTRATIVE, NotificationScope.SECTION, null, sections.get(random.nextInt(sections.size())), null), new NotificationInfo(demoAdmin, "User Reminder", "This is a user-specific reminder.", NotificationCategory.GENERAL, NotificationScope.USER, null, null, demoStudent), new NotificationInfo(demoAdmin, "Global Update", "This is a global update.", NotificationCategory.GENERAL, NotificationScope.GLOBAL, null, null, null), new NotificationInfo(demoAdmin, "Trimester Notice", "This is a trimester-specific notice.", NotificationCategory.ACADEMIC, NotificationScope.TRIMESTER, trimesters.get(random.nextInt(trimesters.size())), null, null), new NotificationInfo(demoAdmin, "Section Alert", "This is a section-specific alert.", NotificationCategory.URGENT, NotificationScope.SECTION, null, sections.get(random.nextInt(sections.size())), null), new NotificationInfo(demoAdmin, "User Notice", "This is a user-specific notice.", NotificationCategory.GENERAL, NotificationScope.USER, null, null, demoStudent));

                    for (NotificationInfo notificationInfo : notificationInfos) {
                        Notification notification = new Notification();
                        notification.setCreatedBy("demo");

                        notification.setSender(notificationInfo.sender);
                        notification.setTitle(notificationInfo.title);
                        notification.setContent(notificationInfo.content);
                        notification.setCategory(notificationInfo.category);
                        notification.setScope(notificationInfo.scope);
                        notification.setTrimester(notificationInfo.trimester);
                        notification.setSection(notificationInfo.section);
                        notification.setTargetUser(notificationInfo.targetUser);

                        notifications.add(notification);
                    }

                    List<CourseInfo> courseInfos = new ArrayList<>();
//

                    // First create sections for each subject-trimester combination
                    for (Trimester trimester : trimesters) {
                        if (trimester.getStatus() == TrimesterStatus.COMPLETED ||
                            trimester.getStatus() == TrimesterStatus.ONGOING) {

                            // Create one section per subject in this trimester
                            for (Subject subject : theorySubjects) {
                                // Create a section only if it doesn't exist
                                boolean sectionExists = sections.stream()
                                                                .anyMatch(s -> s.getSubject().equals(subject) && s.getTrimester().equals(trimester));

                                if (!sectionExists) {
                                    Section section = new Section();
                                    section.setCreatedBy("demo");
                                    section.setSection(String.valueOf((char) ('A' + sections.size() % 26)));
                                    section.setName(subject.getName() + " - " + section.getSection() + " - " + trimester.getCode());
                                    section.setSubject(subject);
                                    section.setTrimester(trimester);
                                    section.setMaxCapacity(40);

                                    // Assign random teacher
                                    section.getTeachers().add(teachers.get(random.nextInt(teachers.size())));

                                    // Create two class slots per week
                                    int slot = random.nextInt(1, 7);

                                    // First class of the week
                                    SpaceTime spt1 = new SpaceTime();
                                    spt1.setCreatedBy("demo");
                                    spt1.setRoomNumber(String.format("T%d%d", random.nextInt(1, 7), random.nextInt(1, 10)));
                                    spt1.setName("Theory Room " + spt1.getRoomNumber());
                                    spt1.setRoomType(SubjectType.THEORY);
                                    spt1.setDayOfWeek(DayOfWeek.SATURDAY);
                                    spt1.setTimeSlot(slot);

                                    // Second class of the week
                                    SpaceTime spt2 = new SpaceTime();
                                    spt2.setCreatedBy("demo");
                                    spt2.setRoomNumber(spt1.getRoomNumber());
                                    spt2.setName(spt1.getName());
                                    spt2.setRoomType(SubjectType.THEORY);
                                    spt2.setDayOfWeek(DayOfWeek.TUESDAY);
                                    spt2.setTimeSlot(slot);

                                    spaceTimeSlots.add(spt1);
                                    spaceTimeSlots.add(spt2);

                                    section.getSpaceTimeSlots().addAll(List.of(spt1, spt2));
                                    sections.add(section);
                                }
                            }
                        }
                    }

// Now create the course registrations
                    for (Trimester trimester : trimesters) {
                        if (trimester.getStatus() == TrimesterStatus.COMPLETED ||
                            trimester.getStatus() == TrimesterStatus.ONGOING ||
                            trimester.getStatus() == TrimesterStatus.SECTION_SELECTION) {

                            for (Student student : students) {
                                for (Subject subject : theorySubjects) {
                                    Section section = null;
                                    CourseStatus status;
                                    Double grade = null;

                                    // Determine status and section based on trimester status
                                    if (trimester.getStatus() == TrimesterStatus.COMPLETED) {
                                        status = CourseStatus.COMPLETED;
                                        // Find the section for this subject in this trimester
                                        section = sections.stream()
                                                          .filter(s -> s.getSubject().equals(subject) && s.getTrimester().equals(trimester))
                                                          .findFirst()
                                                          .orElse(null);

                                        // Generate realistic grade
                                        double randomValue = random.nextDouble();
                                        if (randomValue < 0.1) {
                                            grade = random.nextDouble(0.0, 2.0); // 10% fail/poor grades
                                        } else if (randomValue < 0.7) {
                                            grade = random.nextDouble(2.0, 3.5); // 60% average grades
                                        } else {
                                            grade = random.nextDouble(3.5, 4.0); // 30% excellent grades
                                        }
                                        grade = Math.round(grade * 2) / 2.0; // Round to nearest 0.5
                                    }
                                    else if (trimester.getStatus() == TrimesterStatus.ONGOING) {
                                        status = CourseStatus.REGISTERED;
                                        // Find the section for this subject in this trimester
                                        section = sections.stream()
                                                          .filter(s -> s.getSubject().equals(subject) && s.getTrimester().equals(trimester))
                                                          .findFirst()
                                                          .orElse(null);
                                    }
                                    else {
                                        status = CourseStatus.SELECTED;
                                        // No section for SELECTED status
                                    }

                                    // Only add course if we have a section (or it's in SELECTED status)
                                    if (status == CourseStatus.SELECTED || section != null) {
                                        courseInfos.add(new CourseInfo(student, subject, trimester, section, status, grade));
                                    }
                                }
                            }
                        }
                    }


//                    for (Trimester trimester : trimesters) {
//                        // Only generate data for relevant trimester statuses
//                        if (trimester.getStatus() == TrimesterStatus.COMPLETED ||
//                            trimester.getStatus() == TrimesterStatus.ONGOING ||
//                            trimester.getStatus() == TrimesterStatus.SECTION_SELECTION) {
//
//                            // For each student-subject combination in this trimester
//                            for (Student student : students) {
//                                for (Subject subject : theorySubjects) {
//                                    Section section = null;
//                                    CourseStatus status;
//                                    Double grade = null;
//
//                                    // Determine status based on trimester status
//                                    if (trimester.getStatus() == TrimesterStatus.COMPLETED) {
//                                        status = CourseStatus.COMPLETED;
//
//                                        // Find or create section for completed courses
//                                        section = sections.stream()
//                                                          .filter(s -> s.getSubject().equals(subject) && s.getTrimester().equals(trimester))
//                                                          .findFirst()
//                                                          .orElseGet(() -> {
//                                                              Section newSection = createSection(subject, trimester, teachers, spaceTimeSlots, random);
//                                                              sections.add(newSection);
//                                                              return newSection;
//                                                          });
//
//                                        // Generate realistic grade distribution
//                                        double randomValue = random.nextDouble();
//                                        if (randomValue < 0.1) {
//                                            grade = random.nextDouble(0.0, 2.0); // 10% fail/poor grades
//                                        } else if (randomValue < 0.7) {
//                                            grade = random.nextDouble(2.0, 3.5); // 60% average grades
//                                        } else {
//                                            grade = random.nextDouble(3.5, 4.0); // 30% excellent grades
//                                        }
//                                        grade = Math.round(grade * 2) / 2.0; // Round to nearest 0.5
//                                    }
//                                    else if (trimester.getStatus() == TrimesterStatus.ONGOING) {
//                                        status = CourseStatus.REGISTERED;
//
//                                        // Find or create section for ongoing courses
//                                        section = sections.stream()
//                                                          .filter(s -> s.getSubject().equals(subject) && s.getTrimester().equals(trimester))
//                                                          .findFirst()
//                                                          .orElseGet(() -> {
//                                                              Section newSection = createSection(subject, trimester, teachers, spaceTimeSlots, random);
//                                                              sections.add(newSection);
//                                                              return newSection;
//                                                          });
//                                    }
//                                    else {
//                                        // For SECTION_SELECTION status, courses should be in SELECTED state with no section
//                                        status = CourseStatus.SELECTED;
//                                        section = null;  // Explicitly set to null for clarity
//                                    }
//
//                                    // Add the course info
//                                    courseInfos.add(new CourseInfo(student, subject, trimester, section, status, grade));
//                                }
//                            }
//                        }
//                    }

// Helper method to create a new section

                    for (CourseInfo courseInfo : courseInfos) {
                        Course course = new Course();
                        course.setCreatedBy("demo");

                        course.setStudent(courseInfo.student);
                        course.setSubject(courseInfo.subject);
                        course.setTrimester(courseInfo.trimester);
                        course.setSection(courseInfo.section);
                        course.setStatus(courseInfo.status);
                        course.setGrade(courseInfo.grade);

                        courses.add(course);
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

    private static Section createSection(Subject subject, Trimester trimester, List<Faculty> teachers,
                                         List<SpaceTime> spaceTimeSlots, Random random) {
        Section section = new Section();
        section.setCreatedBy("demo");
        section.setSection(String.valueOf((char) ('A' + random.nextInt(26))));
        section.setName(subject.getName() + " - " + section.getSection() + " - " + trimester.getCode());
        section.setSubject(subject);
        section.setTrimester(trimester);
        section.setMaxCapacity(40);

        // Assign random teacher
        section.getTeachers().add(teachers.get(random.nextInt(teachers.size())));

        // Create two class slots per week
        int slot = random.nextInt(1, 7);

        // First class of the week
        SpaceTime spt1 = new SpaceTime();
        spt1.setCreatedBy("demo");
        spt1.setRoomNumber(String.format("T%d%d", random.nextInt(1, 7), random.nextInt(1, 10)));
        spt1.setName("Theory Room " + spt1.getRoomNumber());
        spt1.setRoomType(SubjectType.THEORY);
        spt1.setDayOfWeek(DayOfWeek.SATURDAY);
        spt1.setTimeSlot(slot);

        // Second class of the week
        SpaceTime spt2 = new SpaceTime();
        spt2.setCreatedBy("demo");
        spt2.setRoomNumber(spt1.getRoomNumber());
        spt2.setName(spt1.getName());
        spt2.setRoomType(SubjectType.THEORY);
        spt2.setDayOfWeek(DayOfWeek.TUESDAY);
        spt2.setTimeSlot(slot);

        spaceTimeSlots.add(spt1);
        spaceTimeSlots.add(spt2);

        section.getSpaceTimeSlots().addAll(List.of(spt1, spt2));

        return section;
    }
}
