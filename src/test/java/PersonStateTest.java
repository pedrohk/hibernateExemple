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

        Person p = new Person("Alice");
        assertNull(p.getId());


        Person saved = hDao.save(p);
        assertNotNull(saved.getId());

        Long id = saved.getId();

        saved.setName("Alice Updated");

        Person merged = hDao.updateDetached(saved);
        assertEquals("Alice Updated", merged.getName());


        Person fromDb = hDao.find(id);
        assertEquals("Alice Updated", fromDb.getName());
    }

    @Test
    public void testJpaEntityStates() {

        Person p = new Person("Bob");
        assertNull(p.getId());


        Person saved = jDao.save(p);
        assertNotNull(saved.getId());
        Long id = saved.getId();


        saved.setName("Bob Edited");


        Person merged = jDao.updateDetached(saved);
        assertEquals("Bob Edited", merged.getName());


        Person fromDb = jDao.find(id);
        assertEquals("Bob Edited", fromDb.getName());
    }

    @Test
    public void testJpaRemoveDetached() {
        Person p = new Person("Charlie");
        Person saved = jDao.save(p);
        Long id = saved.getId();


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

        Person p = new Person("Eve");
        Person savedJ = jDao.save(p);

        assertNotNull(savedJ.getId(), "Saved person ID should not be null");


        savedJ.setName("Eve via Hibernate");

        Person mergedH = hDao.updateDetached(savedJ);
        assertNotNull(mergedH, "Merged person should not be null");


        Person finalDb = jDao.find(savedJ.getId());
        assertNotNull(finalDb, "Final fetched person is null — likely not persisted or deleted");
        assertEquals("Eve via Hibernate", finalDb.getName());
    }
}
