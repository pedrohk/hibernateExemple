import dao.PersonHibernateDao;
import dao.PersonJpaDao;
import entity.Person;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import util.HibernateUtil;
import util.JPAUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonStateTest {

    private PersonHibernateDao hDao;
    private PersonJpaDao jDao;

    @BeforeAll
    public static void setupAll() {

    }

    @AfterAll
    public static void tearDownAll() {
        HibernateUtil.shutdown();
        JPAUtil.close();
    }

    @BeforeEach
    public void setup() {
        hDao = new PersonHibernateDao();
        jDao = new PersonJpaDao();
    }

    @Test
    public void testHibernateEntityStates() {
        // 1. Transient
        Person p = new Person("Alice");
        assertNull(p.getId());

        // 2. Persist (becomes Persistent in a session)
        Person saved = hDao.save(p);
        assertNotNull(saved.getId());

        Long id = saved.getId();

        // 3. Detached: after session close, p is no longer managed
        // Let’s modify the detached instance
        saved.setName("Alice Updated");

        // 4. Merge back (reattach)
        Person merged = hDao.updateDetached(saved);
        assertEquals("Alice Updated", merged.getName());

        // 5. Get from DB and confirm
        Person fromDb = hDao.find(id);
        assertEquals("Alice Updated", fromDb.getName());
    }

    @Test
    public void testJpaEntityStates() {
        // 1. Transient
        Person p = new Person("Bob");
        assertNull(p.getId());

        // 2. Persist → Persistent
        Person saved = jDao.save(p);
        assertNotNull(saved.getId());
        Long id = saved.getId();

        // 3. Detached (after close)
        saved.setName("Bob Edited");

        // 4. Merge (reattach)
        Person merged = jDao.updateDetached(saved);
        assertEquals("Bob Edited", merged.getName());

        // 5. Find and verify
        Person fromDb = jDao.find(id);
        assertEquals("Bob Edited", fromDb.getName());
    }

    @Test
    public void testJpaRemoveDetached() {
        Person p = new Person("Charlie");
        Person saved = jDao.save(p);
        Long id = saved.getId();

        // Delete via JPA
        jDao.delete(saved);

        Person fromDb = jDao.find(id);
        assertNull(fromDb);
    }

    @Test
    public void testHibernateRemoveDetached() {
        Person p = new Person("Diana");
        Person saved = hDao.save(p);
        Long id = saved.getId();

        hDao.delete(saved);

        Person fromDb = hDao.find(id);
        assertNull(fromDb);
    }

    @Test
    public void testMixingHibernateAndJpa() {
        // Save via JPA
        Person p = new Person("Eve");
        Person savedJ = jDao.save(p);
        Long id = savedJ.getId();

        assertNotNull(id, "Saved person ID should not be null");

        // Now update via Hibernate with the detached instance
        savedJ.setName("Eve via Hibernate");

        Person mergedH = hDao.updateDetached(savedJ);
        assertNotNull(mergedH, "Merged person should not be null");

        // Fetch via JPA
        Person finalDb = jDao.find(id);
        assertNotNull(finalDb, "Final fetched person is null — likely not persisted or deleted");

        assertEquals("Eve via Hibernate", finalDb.getName());
    }
}
