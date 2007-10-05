/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.event.CdmEventListenerRegistrationSupport;
import java.util.*;
import javax.persistence.*;


/**
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:18
 */
@MappedSuperclass
public abstract class VersionableEntity extends CdmEventListenerRegistrationSupport{
	static Logger logger = Logger.getLogger(VersionableEntity.class);
	
	
	/*Default creator*/
	public static String createdWhoDefault = "test";
	
	private Calendar createdWhen;
	private String createdWho;
	private int id;
	private String notes;
	private Calendar updatedWhen;
	private String updatedWho;
	//the globally unique identifier
	private String uuid;
	private ArrayList m_View;

	public Calendar getCreatedWhen(){
		if (createdWhen == null){
			createdWhen = Calendar.getInstance();
		}
		return createdWhen;
	}

	public String getCreatedWho(){
		if (createdWho == null){
			createdWho = createdWhoDefault;
		}
		return createdWho;
	}

	@Id @GeneratedValue(generator="system-increment")
	public int getId(){
		return id;
	}

	public ArrayList getM_View(){
		return m_View;
	}

	public String getNotes(){
		return notes;
	}

	public Calendar getUpdatedWhen(){
		return updatedWhen;
	}

	public String getUpdatedWho(){
		return updatedWho;
	}

	public String getUuid(){
		if (uuid == null){
			uuid = UUID.randomUUID().toString();
		}
		return uuid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCreatedWhen(Calendar newVal){
		createdWhen = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCreatedWho(String newVal){
		createdWho = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setId(int newVal){
		id = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setM_View(ArrayList newVal){
		m_View = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNotes(String newVal){
		notes = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUpdatedWhen(Calendar newVal){
		updatedWhen = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUpdatedWho(String newVal){
		updatedWho = newVal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUuid(String newVal){
		uuid = newVal;
	}
	

}