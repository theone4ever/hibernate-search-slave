package com.keven.search.service;

import org.hibernate.Session;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import com.keven.search.model.Member;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: eqqiwng
 * Date: 10/30/13
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/service")
@Singleton
@Startup
public class SearchService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    @GET
    @Path("/register/{name}")
    @Produces(MediaType.TEXT_XML)
    public Member register(@PathParam("name") String name) throws Exception {
        log.info("start register!");
        Member member = new Member();
        member.setName(name);
        member.setAddress("abcd");
        member.setPhoneNumber("123456789");
        log.info("Registering: " + member);

        // using Hibernate session(Native API) and JPA entitymanager
        Session session = (Session) em.getDelegate();
        session.persist(member);
        return member;
    }



    @GET
    @Path("/search/{name}")
    @Produces(MediaType.TEXT_XML)
    public List<Member> search(@PathParam("name") String name) {
        FullTextEntityManager fullTextEntityManager =
                org.hibernate.search.jpa.Search.getFullTextEntityManager(em);
//        em.getTransaction().begin();
        // create native Lucene query unsing the query DSL
        // alternatively you can write the Lucene query using the Lucene query parser
        // or the Lucene programmatic API. The Hibernate Search DSL is recommended though
        QueryBuilder qb = fullTextEntityManager.getSearchFactory()
                .buildQueryBuilder().forEntity(Member.class).get();
        org.apache.lucene.search.Query query = qb
                .keyword()
                .onFields("name")
                .matching(name + "*")
                .createQuery();
        // wrap Lucene query in a javax.persistence.Query
        log.info(query.toString());
        javax.persistence.Query persistenceQuery =
                fullTextEntityManager.createFullTextQuery(query, Member.class);
        log.info("input query:" + persistenceQuery);
        // execute search
        List result = persistenceQuery.getResultList();
        log.info("Size of result:" + result.size());
//        em.getTransaction().commit();
//        em.close();


        return result;
    }






}
