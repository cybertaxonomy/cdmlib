/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:01
 */
@Entity
public class User {
	static Logger logger = Logger.getLogger(User.class);
	private String name;
	private String email;
	private String openID;
	private Calendar created;
	private ArrayList views;

	public ArrayList getViews(){
		return this.views;
	}

	/**
	 * 
	 * @param views    views
	 */
	public void setViews(ArrayList views){
		this.views = views;
	}

	public String getName(){
		return this.name;
	}

	/**
	 * 
	 * @param name    name
	 */
	public void setName(String name){
		this.name = name;
	}

	public String getEmail(){
		return this.email;
	}

	/**
	 * 
	 * @param email    email
	 */
	public void setEmail(String email){
		this.email = email;
	}

	public String getOpenID(){
		return this.openID;
	}

	/**
	 * 
	 * @param openID    openID
	 */
	public void setOpenID(String openID){
		this.openID = openID;
	}

	public Calendar getCreated(){
		return this.created;
	}

	/**
	 * 
	 * @param created    created
	 */
	public void setCreated(Calendar created){
		this.created = created;
	}

}