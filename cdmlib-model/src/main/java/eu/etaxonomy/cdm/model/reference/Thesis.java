/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.Institution;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@Entity
public class Thesis extends PublicationBase {
	private static final Logger logger = Logger.getLogger(Thesis.class);
	private Institution school;

	public static Thesis NewInstance(){
		Thesis result = new Thesis();
		return result;
	}
	
	public static Thesis NewInstance(Institution school){
		Thesis result = NewInstance();
		result.setSchool(school);
		return result;
	}
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Institution getSchool(){
		return this.school;
	}

	/**
	 * 
	 * @param school    school
	 */
	public void setSchool(Institution school){
		this.school = school;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}