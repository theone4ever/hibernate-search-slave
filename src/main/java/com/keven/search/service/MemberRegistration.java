package com.keven.search.service;

import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import com.keven.search.model.Member;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class MemberRegistration {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;


    @Inject
    private Event<Member> memberEventSrc;

    public void register(Member member) throws Exception {
        log.info("Registering " + member.getName());
        // em.persist(member);

        // using Hibernate session(Native API) and JPA entitymanager
        Session session = (Session) em.getDelegate();
        session.persist(member);
        memberEventSrc.fire(member);

    }


    public void buildIndex() throws Exception {
        log.log(Level.INFO, "Start build index");
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        fullTextEntityManager.createIndexer().startAndWait();
        log.log(Level.INFO, "Index build ok");
    }

    public List<Member> search(String searchName) {
        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
        //em.getTransaction().begin();
        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Member.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onFields("name")
                .matching(searchName+"*")
                .createQuery();
        // wrap Lucene query in a javax.persistence.Query
        javax.persistence.Query persistenceQuery =
                fullTextEntityManager.createFullTextQuery(query, Member.class);
        log.info("input query:"+ persistenceQuery.toString());
        // execute search
        List result = persistenceQuery.getResultList();
        log.info("Size of result:"+ result.size());
        //em.getTransaction().commit();
        //em.close();
        return result;
    }
}
