/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.NameAlias;
import org.apache.log4j.Logger;
import java.util.*;
import javax.persistence.*;

/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:10
 */
@Entity
public class Person extends VersionableEntity {
	static Logger logger = Logger.getLogger(Person.class);

	private String forenames;
	private String lifespan;
	private String subjectScope;
	private String surname;
	private ArrayList standardForms;

	public String getForenames(){
		return forenames;
	}

	public String getLifespan(){
		return lifespan;
	}

	public ArrayList getStandardForms(){
		return standardForms;
	}

	public String getSubjectScope(){
		return subjectScope;
	}

	public String getSurname(){
		return surname;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setForenames(String newVal){
		forenames = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLifespan(String newVal){
		lifespan = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setStandardForms(ArrayList newVal){
		standardForms = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSubjectScope(String newVal){
		subjectScope = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSurname(String newVal){
		surname = newVal;
	}

}