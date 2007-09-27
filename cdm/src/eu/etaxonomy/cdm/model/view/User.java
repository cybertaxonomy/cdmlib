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
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:18
 */
@Entity
public class User {
	static Logger logger = Logger.getLogger(User.class);

	private int created;
	private String email;
	private String name;
	private String openID;
	private ArrayList views;

	public int getCreated(){
		return created;
	}

	public String getEmail(){
		return email;
	}

	public String getName(){
		return name;
	}

	public String getOpenID(){
		return openID;
	}

	public ArrayList getViews(){
		return views;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCreated(int newVal){
		created = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEmail(String newVal){
		email = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOpenID(String newVal){
		openID = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setViews(ArrayList newVal){
		views = newVal;
	}

}