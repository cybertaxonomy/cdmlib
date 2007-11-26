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

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:01
 */
@MappedSuperclass
public abstract class VersionableEntity<T extends VersionableEntity> extends CdmBase {
	public VersionableEntity() {
		super();
		this.uuid = UUID.randomUUID().toString();
		this.created = Calendar.getInstance();
	}

	static Logger logger = Logger.getLogger(VersionableEntity.class);
	//the globally unique identifier
	private String uuid;
	private Calendar created;
	private Person createdBy;
	//time of last update for this object
	private Calendar updated;
	private Person updatedBy;
	private T nextVersion;
	private T previousVersion;

	//@OneToOne(mappedBy="previousVersion")
	@Transient
	public T getNextVersion(){
		return this.nextVersion;
	}
	public void setNextVersion(T nextVersion){
		this.nextVersion = nextVersion;
	}

	//@OneToOne
	@Transient
	public T getPreviousVersion(){
		return this.previousVersion;
	}
	public void setPreviousVersion(T previousVersion){
		this.previousVersion = previousVersion;
	}


	@ManyToOne
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

	@ManyToOne
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

	public String getUuid(){
		return this.uuid;
	}
	/**
	 * 
	 * @param uuid    uuid
	 */
	protected void setUuid(String uuid){
		this.uuid = uuid;
	}

	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getCreated(){
		return this.created;
	}
	/**
	 * 
	 * @param created    created
	 */
	public void setCreated(Calendar created){
		this.created = created;
	}

	@Temporal(TemporalType.TIMESTAMP)
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