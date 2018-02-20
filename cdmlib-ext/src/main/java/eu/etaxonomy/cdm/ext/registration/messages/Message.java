/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages;

import java.util.Date;

import eu.etaxonomy.cdm.model.common.User;

/**
 * @author a.kohlbecker
 * @since Feb 15, 2018
 *
 */
public class Message {

    private String text;

    private User from;

    private Date createdOn;

    private Integer id;

    /**
     * @param text
     * @param from
     */
    public Message(Integer id, String text, User from, Date createdOn) {
        super();
        this.id = id;
        this.text = text;
        this.from = from;
        this.createdOn = createdOn;
    }

    public Integer getId() {
        return id;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the from
     */
    public User getFrom() {
        return from;
    }

    /**
     * @return the createdOn
     */
    public Date getCreatedOn() {
        return createdOn;
    }




}
