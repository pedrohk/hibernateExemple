package dao;

import entity.Person;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import util.JPAUtil;


public class PersonJpaDao {
    public Person save(Person person) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(person);

        tx.commit();
        em.close();
        return person;
    }

    public Person find(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        Person p = em.find(Person.class, id);

        em.close();
        return p;
    }

    public Person updateDetached(Person detached) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Person merged = em.merge(detached);
        em.flush();

        tx.commit();
        em.close();
        return merged;
    }


    public void delete(Person person) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Person attached = em.contains(person) ? person : em.merge(person);
        em.remove(attached);

        tx.commit();
        em.close();
    }
}
