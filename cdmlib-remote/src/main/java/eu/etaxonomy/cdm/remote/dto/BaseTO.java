/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.dto;

import java.util.Calendar;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Data Transfer Object representing derived from the domain object {@link CdmBase}. 
 * 
 * @author a.kohlbecker
 * @author m.doering
 * @version 1.0
 * @created 11.12.2007 11:14:44
 *
 */
public abstract class BaseTO extends BaseSTO{
	
	private DateTime created;
	private String createdBy;
	private Calendar updated;
	private String updatedBy;
	
	public DateTime getCreated() {
		return created;
	}
	public void setCreated(DateTime created) {
		this.created = created;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public void setCreatedBy(Person createdBy) {
		if(createdBy != null){
			this.createdBy = createdBy.getTitleCache();			
		}
	}
	public Calendar getUpdated() {
		return updated;
	}
	public void setUpdated(Calendar updated) {
		this.updated = updated;
	}
	public String getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	
	

}
