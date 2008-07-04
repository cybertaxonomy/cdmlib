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
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * A year() method is required to get the year of publication out of the
 * datePublished field
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReferenceBase", propOrder = {
	"uri",
	"isNomenclaturallyRelevant",
    "authorTeam"
})
@XmlRootElement(name = "RelationshipBase")
@Entity
@Table(appliesTo="ReferenceBase", indexes = { @Index(name = "ReferenceBaseTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class ReferenceBase extends IdentifyableMediaEntity implements IParsable{
	
	static Logger logger = Logger.getLogger(ReferenceBase.class);
	
	//URIs like DOIs, LSIDs or Handles for this reference
	@XmlElement(name = "URI")
	private String uri;
	
	//flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	@XmlElement(name = "IsNomenclaturallyRelevant")
	private boolean isNomenclaturallyRelevant;
	
	@XmlElement(name = "AuthorTeam")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private TeamOrPersonBase authorTeam;

	//this flag will be set to true if the parseName method was unable to successfully parse the name
	@XmlAttribute
	private boolean hasProblem = false;
	
	@XmlTransient
	protected IReferenceBaseCacheStrategy<ReferenceBase> cacheStrategy;
	
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TeamOrPersonBase getAuthorTeam(){
		return this.authorTeam;
	}

	public void setAuthorTeam(TeamOrPersonBase authorTeam){
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

	public boolean getHasProblem(){
		return this.hasProblem;
	}
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
	}
	
	@Override
	public String generateTitle(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for ReferenceBase: " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getTitleCache(this);
		}
	}
	
//**************************** CLONE *********************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 */
	public Object clone() throws CloneNotSupportedException{
		ReferenceBase result = (ReferenceBase)super.clone();
		//no changes to: authorTeam, hasProblem, nomenclaturallyRelevant, uri
		return result;
	}
	
}