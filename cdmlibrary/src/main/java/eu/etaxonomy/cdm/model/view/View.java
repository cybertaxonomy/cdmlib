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
 * use ARCHIVE view/dataset to maintain an archive. All members of that view will
 * never be changed
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:19
 */
@Entity
public class View {
	static Logger logger = Logger.getLogger(View.class);

	private String description;
	private String name;
	private java.util.ArrayList superView;
	private ArrayList users;

	public String getDescription(){
		return description;
	}

	public String getName(){
		return name;
	}

	public java.util.ArrayList getSuperView(){
		return superView;
	}

	public ArrayList getUsers(){
		return users;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setDescription(String newVal){
		description = newVal;
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
	public void setSuperView(java.util.ArrayList newVal){
		superView = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUsers(ArrayList newVal){
		users = newVal;
	}

}