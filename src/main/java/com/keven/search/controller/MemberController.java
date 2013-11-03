package com.keven.search.controller;

import com.keven.search.model.Member;
import com.keven.search.service.MemberRegistration;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

// The @Model stereotype is a convenience mechanism to make this a request-scoped bean that has an
// EL name
// Read more about the @Model stereotype in this FAQ:
// http://sfwk.org/Documentation/WhatIsThePurposeOfTheModelAnnotation
@Model
public class MemberController {

    @Inject
    private FacesContext facesContext;

    @Inject
    private MemberRegistration memberRegistration;

    @Inject
    private Logger log;


    private Member newMember;
    private Member searchMember;
    private List<Member> searchResult;

    @Produces
    @Named
    public Member getNewMember() {
        return newMember;
    }

    @Produces
    @Named
    public Member getSearchMember() {
        return searchMember;
    }


    @Produces
    @Named
    public List<Member> getSearchResult() {
        return searchResult;
    }

    public void register() {
        try {
            memberRegistration.register(newMember);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Registered!", "Registration successful"));
            initNewMember();
            memberRegistration.buildIndex();
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Not Registered due to error in data entered!", "Registration unsuccessful"));
        }
    }


    public void search() {
        log.info("start search: " + searchMember.getName());
        try {
//            memberRegistration.buildIndex();
            searchResult = memberRegistration.search(searchMember.getName());
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Search failed", "Search unsuccessful"));
        }
    }

    @PostConstruct
    public void initNewMember() {
        newMember = new Member();
        searchMember = new Member();
        searchResult = new ArrayList<Member>();
    }
}
