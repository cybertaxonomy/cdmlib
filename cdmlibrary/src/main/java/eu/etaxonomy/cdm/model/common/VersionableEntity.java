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
 * @created 08-Nov-2007 13:07:01
 */
@MappedSuperclass
public abstract class VersionableEntity extends CdmBase {
	static Logger logger = Logger.getLogger(VersionableEntity.class);
	private int id;
	//the globally unique identifier
	private String uuid;
	private Calendar created;
	private Person createdBy;
	//time of last update for this object
	private Calendar updated;
	private Person updatedBy;
	private VersionableEntity nextVersion;
	private VersionableEntity previousVersion;

	public VersionableEntity getNextVersion(){
		return this.nextVersion;
	}

	/**
	 * 
	 * @param nextVersion    nextVersion
	 */
	public void setNextVersion(VersionableEntity nextVersion){
		this.nextVersion = nextVersion;
	}

	public VersionableEntity getPreviousVersion(){
		return this.previousVersion;
	}

	/**
	 * 
	 * @param previousVersion    previousVersion
	 */
	public void setPreviousVersion(VersionableEntity previousVersion){
		this.previousVersion = previousVersion;
	}

	public Person getUpdatedBy(){
		return this.updatedBy;
	}

	/**
	 * 
	 * @param updatedBy    updatedBy
	 */
	public void setUpdatedBy(Person updatedBy){
		this.updatedBy = updatedBy;
	}

	public Person getCreatedBy(){
		return this.createdBy;
	}

	/**
	 * 
	 * @param createdBy    createdBy
	 */
	public void setCreatedBy(Person createdBy){
		this.createdBy = createdBy;
	}

	@Id @GeneratedValue(generator="system-increment")
	public int getId(){
		return this.id;
	}
	/**
	 * 
	 * @param id    id
	 */
	public void setId(int id){
		this.id = id;
	}

	public String getUuid(){
		if (this.uuid == null){
			this.uuid = UUID.randomUUID().toString();
		}
		return this.uuid;
	}
	/**
	 * 
	 * @param uuid    uuid
	 */
	public void setUuid(String uuid){
		this.uuid = uuid;
	}

	public Calendar getCreated(){
		if (this.created == null){
			this.created = Calendar.getInstance();
		}
		return this.created;
	}
	/**
	 * 
	 * @param created    created
	 */
	public void setCreated(Calendar created){
		this.created = created;
	}

	public Calendar getUpdated(){
		return this.updated;
	}

	/**
	 * 
	 * @param updated    updated
	 */
	public void setUpdated(Calendar updated){
		this.updated = updated;
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