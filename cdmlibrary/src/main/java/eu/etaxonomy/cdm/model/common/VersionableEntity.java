/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.view.View;
import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:53
 */
@MappedSuperclass
public abstract class VersionableEntity {
	static Logger logger = Logger.getLogger(VersionableEntity.class);

	@Description("")
	private int id;
	//the globally unique identifier
	@Description("the globally unique identifier")
	private String uuid;
	@Description("")
	private Calendar created;
	//time of last update for this object
	@Description("time of last update for this object")
	private Calendar updated;
	private ArrayList m_View;
	private VersionableEntity nextVersion;
	private VersionableEntity previousVersion;
	private Person updatedBy;
	private Person createdBy;

	public ArrayList getM_View(){
		return m_View;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setM_View(ArrayList newVal){
		m_View = newVal;
	}

	public VersionableEntity getNextVersion(){
		return nextVersion;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNextVersion(VersionableEntity newVal){
		nextVersion = newVal;
	}

	public VersionableEntity getPreviousVersion(){
		return previousVersion;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPreviousVersion(VersionableEntity newVal){
		previousVersion = newVal;
	}

	public Person getUpdatedBy(){
		return updatedBy;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUpdatedBy(Person newVal){
		updatedBy = newVal;
	}

	public Person getCreatedBy(){
		return createdBy;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCreatedBy(Person newVal){
		createdBy = newVal;
	}

	public int getId(){
		return id;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setId(int newVal){
		id = newVal;
	}

	public String getUuid(){
		return uuid;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUuid(String newVal){
		uuid = newVal;
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

	public Calendar getUpdated(){
		return updated;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUpdated(Calendar newVal){
		updated = newVal;
	}

	/**
	 * based on created
	 */
	@Transient
	public Calendar getValidFrom(){
		return null;
	}

	/**
	 * based on updated
	 */
	@Transient
	public Calendar getValidTo(){
		return null;
	}

}