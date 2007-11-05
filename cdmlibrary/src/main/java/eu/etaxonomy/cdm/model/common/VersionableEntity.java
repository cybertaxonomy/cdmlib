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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:40
 */
@MappedSuperclass
public abstract class VersionableEntity extends CdmBase{
	static Logger logger = Logger.getLogger(VersionableEntity.class);

	@Description("")
	private int id;
	//the globally unique identifier
	@Description("the globally unique identifier")
	private String uuid;
	@Description("")
	private Calendar created;
	private Person createdBy;
	//time of last update for this object
	@Description("time of last update for this object")
	private Calendar updated;
	private Person updatedBy;
	private VersionableEntity nextVersion;
	private VersionableEntity previousVersion;


	public VersionableEntity getNextVersion(){
		return nextVersion;
	}

	/**
	 * 
	 * @param nextVersion
	 */
	public void setNextVersion(VersionableEntity nextVersion){
		;
	}

	public VersionableEntity getPreviousVersion(){
		return previousVersion;
	}

	/**
	 * 
	 * @param previousVersion
	 */
	public void setPreviousVersion(VersionableEntity previousVersion){
		;
	}

	public Person getUpdatedBy(){
		return updatedBy;
	}

	/**
	 * 
	 * @param updatedBy
	 */
	public void setUpdatedBy(Person updatedBy){
		;
	}

	public Person getCreatedBy(){
		return createdBy;
	}

	/**
	 * 
	 * @param createdBy
	 */
	public void setCreatedBy(Person createdBy){
		;
	}

	public int getId(){
		return id;
	}

	/**
	 * 
	 * @param id
	 */
	public void setId(int id){
		;
	}

	public String getUuid(){
		return uuid;
	}

	/**
	 * 
	 * @param uuid
	 */
	public void setUuid(String uuid){
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

	public Calendar getUpdated(){
		return updated;
	}

	/**
	 * 
	 * @param updated
	 */
	public void setUpdated(Calendar updated){
		;
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