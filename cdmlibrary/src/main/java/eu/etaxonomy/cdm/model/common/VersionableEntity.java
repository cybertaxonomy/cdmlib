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
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:07:01
 */
@MappedSuperclass
public abstract class VersionableEntity<T extends VersionableEntity> extends CdmBase {
	static Logger logger = Logger.getLogger(VersionableEntity.class);
	//time of last update for this object
	private Calendar updated;
	private Person updatedBy;
	private CdmBase nextVersion;
	private CdmBase previousVersion;

	public VersionableEntity() {
		super();
	}

	
	//@OneToOne(mappedBy="previousVersion")
	@Transient
	public CdmBase getNextVersion(){
		return this.nextVersion;
	}
	public void setNextVersion(CdmBase nextVersion){
		this.nextVersion = nextVersion;
	}

	//@OneToOne
	@Transient
	public CdmBase getPreviousVersion(){
		return this.previousVersion;
	}
	public void setPreviousVersion(CdmBase previousVersion){
		this.previousVersion = previousVersion;
	}


	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
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