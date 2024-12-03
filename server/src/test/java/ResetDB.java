import enrollium.server.db.DB;
import org.hibernate.Session;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// https://stackoverflow.com/questions/3327312/how-can-i-drop-all-the-tables-in-a-postgresql-database
// https://thorben-janssen.com/mutationquery-and-selectionquery/


public class ResetDB {
    @Disabled
    @Test
    void cleanDatabase() {
        try (Session session = DB.getSessionFactory().openSession()) {
            session.beginTransaction();

            session.createNativeMutationQuery("DROP SCHEMA public CASCADE; CREATE SCHEMA public;").executeUpdate();

            session.getTransaction().commit();
        }
    }
}
