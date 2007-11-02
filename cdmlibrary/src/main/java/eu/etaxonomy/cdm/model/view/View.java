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
 * use ARCHIVE view/dataset to maintain an archive. All members of that view will
 * never be changed
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:53
 */
public class View {
	static Logger logger = Logger.getLogger(View.class);

	@Description("")
	private String name;
	@Description("")
	private String description;
	private ArrayList users;
	private java.util.ArrayList superView;

	public java.util.ArrayList getSuperView(){
		return superView;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSuperView(java.util.ArrayList newVal){
		superView = newVal;
	}

	public ArrayList getUsers(){
		return users;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUsers(ArrayList newVal){
		users = newVal;
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

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
	}

}