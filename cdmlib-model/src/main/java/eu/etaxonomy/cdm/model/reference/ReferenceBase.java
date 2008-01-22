/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.IMediaDocumented;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.IdentifyableMediaEntity;
import eu.etaxonomy.cdm.model.common.Media;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:47
 */
@Entity
public abstract class ReferenceBase extends IdentifyableMediaEntity{
	static Logger logger = Logger.getLogger(ReferenceBase.class);
	//URIs like DOIs, LSIDs or Handles for this reference
	private String uri;
	//flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	private boolean isNomenclaturallyRelevant;
	private Agent authorTeam;
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getAuthorTeam(){
		return this.authorTeam;
	}

	public void setAuthorTeam(Agent authorTeam){
		this.authorTeam = authorTeam;
	}

	public String getUri(){
		return this.uri;
	}
	public void setUri(String uri){
		this.uri = uri;
	}

	public boolean isNomenclaturallyRelevant(){
		return this.isNomenclaturallyRelevant;
	}

	/**
	 * 
	 * @param isNomenclaturallyRelevant    isNomenclaturallyRelevant
	 */
	public void setNomenclaturallyRelevant(boolean isNomenclaturallyRelevant){
		this.isNomenclaturallyRelevant = isNomenclaturallyRelevant;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}
	
	@Transient
	public abstract String getYear();

}