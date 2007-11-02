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
 * use ARCHIVE view/dataset to maintain an archive. All members of that view will
 * never be changed
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:40
 */
@Entity
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
	 * @param superView
	 */
	public void setSuperView(java.util.ArrayList superView){
		;
	}

	public ArrayList getUsers(){
		return users;
	}

	/**
	 * 
	 * @param users
	 */
	public void setUsers(ArrayList users){
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

	public String getDescription(){
		return description;
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description){
		;
	}

}