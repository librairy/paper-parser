/*
 * Copyright (c) 2016. Universidad Politecnica de Madrid
 *
 * @author Badenes Olmedo, Carlos <cbadenes@fi.upm.es>
 *
 */

package org.librairy.paperparser.data;

import edu.upf.taln.dri.lib.model.ext.Author;

import java.util.stream.Collectors;

/**
 * Created by cbadenes on 07/01/16.
 */
public class AuthorWrapper {

    private final Author author;

    public AuthorWrapper(Author author){
        this.author = author;
    }

    public String getFirstName(){
        return this.author.getFirstName();
    }

    public String getSurName(){
        return this.author.getSurname();
    }

    public String getFullName(){
        return this.author.getFullName();
    }

    public String getAffiliation(){
        //TODO More detailed affiliation
        return this.author.getAffiliations().stream().map(affiliation -> affiliation.toString()).collect(Collectors.joining(";"));
    }

    public String getPersonalPageURL(){
        return this.author.getPersonalPageURL();
    }
}
