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

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:46
 */
public class User {
	static Logger logger = Logger.getLogger(User.class);

	@Description("")
	private String name;
	@Description("")
	private String email;
	@Description("")
	private String openID;
	@Description("")
	private Calendar created;
	private ArrayList views;

	public ArrayList getViews(){
		return views;
	}

	/**
	 * 
	 * @param views
	 */
	public void setViews(ArrayList views){
		;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		;
	}

	public String getEmail(){
		return email;
	}

	/**
	 * 
	 * @param email
	 */
	public void setEmail(String email){
		;
	}

	public String getOpenID(){
		return openID;
	}

	/**
	 * 
	 * @param openID
	 */
	public void setOpenID(String openID){
		;
	}

	public Calendar getCreated(){
		return created;
	}

	/**
	 * 
	 * @param created
	 */
	public void setCreated(Calendar created){
		;
	}

}