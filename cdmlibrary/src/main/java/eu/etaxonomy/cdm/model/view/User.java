/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.view;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:25
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
	 * @param newVal
	 */
	public void setViews(ArrayList newVal){
		views = newVal;
	}

	public String getName(){
		return name;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setName(String newVal){
		name = newVal;
	}

	public String getEmail(){
		return email;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEmail(String newVal){
		email = newVal;
	}

	public String getOpenID(){
		return openID;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOpenID(String newVal){
		openID = newVal;
	}

	public Calendar getCreated(){
		return created;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCreated(Calendar newVal){
		created = newVal;
	}

}