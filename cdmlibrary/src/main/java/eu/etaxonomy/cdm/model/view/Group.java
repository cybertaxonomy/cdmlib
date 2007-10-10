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
 * @created 15-Aug-2007 18:36:04
 */
@Entity
public class Group {
	static Logger logger = Logger.getLogger(Group.class);

	private String description;
	private String location;

	public String getDescription(){
		return description;
	}

	public String getLocation(){
		return location;
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
	public void setLocation(String newVal){
		location = newVal;
	}

}