package dao;

import entity.Person;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class PersonHibernateDao {
    public Person save(Person person) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        session.persist(person);

        tx.commit();
        session.close();
        return person;
    }

    public Person find(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Person p = session.get(Person.class, id);
        session.close();
        return p;
    }

    public Person updateDetached(Person detached) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        Person managed = (Person) session.merge(detached);
        tx.commit();
        session.close();
        return managed;
    }

    public void delete(Person person) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.remove(person);
        tx.commit();
        session.close();
    }
}
