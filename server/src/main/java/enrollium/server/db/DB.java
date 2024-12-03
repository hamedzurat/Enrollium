package enrollium.server.db;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

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
    private static final    Logger         log = LoggerFactory.getLogger(DB.class);
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
}
