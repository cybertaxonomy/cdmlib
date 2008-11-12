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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;

/**
 * The upmost (abstract) class for references (information sources). Its two
 * direct subclasses {@link StrictReferenceBase StrictReferenceBase} and {@link BibtexReference BibtexReference}
 * allow either on the one side to handle different kind of references with their
 * peculiarities or on the other side to follow the flat BibTeX format
 * (see "http://en.wikipedia.org/wiki/BibTeX").
 * <P>
 * This class corresponds to: <ul>
 * <li> PublicationCitation according to the TDWG ontology
 * <li> Publication according to the TCS
 * <li> Reference according to the ABCD schema
 * </ul>
 * 
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
	
	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} who created the
	 * content of <i>this</i> reference.
	 * 
	 * @return  the author (team) of <i>this</i> reference
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public TeamOrPersonBase getAuthorTeam(){
		return this.authorTeam;
	}

	/**
	 * @see #getAuthorTeam()
	 */
	public void setAuthorTeam(TeamOrPersonBase authorTeam){
		this.authorTeam = authorTeam;
	}

	/**
	 * Returns the Uniform Resource Identifier (URI) corresponding to <i>this</i>
	 * reference. An URI is a string of characters used to identify a resource
	 * on the Internet.
	 * 
	 * @return  the URI of <i>this</i> reference
	 */
	public String getUri(){
		return this.uri;
	}
	/**
	 * @see #getUri()
	 */
	public void setUri(String uri){
		this.uri = uri;
	}

	/**
	 * Returns "true" if the isNomenclaturallyRelevant flag is set. This 
	 * indicates that a {@link TaxonNameBase taxon name} has been originally
	 * published in <i>this</i> reference following the rules of a
	 * {@link eu.etaxonomy.cdm.model.name.NomenclaturalCode nomenclature code} and is therefore used for
	 * nomenclatural citations. This flag will be set as soon as <i>this</i>
	 * reference is used as a nomenclatural reference for any taxon name.
	 */
	public boolean isNomenclaturallyRelevant(){
		return this.isNomenclaturallyRelevant;
	}

	/**
	 * @see #isNomenclaturallyRelevant()
	 */
	public void setNomenclaturallyRelevant(boolean isNomenclaturallyRelevant){
		this.isNomenclaturallyRelevant = isNomenclaturallyRelevant;
	}

	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> reference.
	 * 
	 * @see  #generateTitle()
	 */
	@Transient
	// TODO implement 
	public String getCitation(){
		return "";
	}
	
	/**
	 * Returns a string containing the date (mostly only the year) of
	 * publication / creation of <i>this</i> reference.
	 */
	@Transient
	public abstract String getYear();

	/**
	 * Returns the boolean value of the flag indicating whether the used {@link eu.etaxonomy.cdm.strategy.parser.INonViralNameParser parser} 
	 * method was able to parse the string designating <i>this</i> reference
	 * successfully (false) or not (true).
	 *  
	 * @return  the boolean value of the hasProblem flag
	 * @see     #getCitation()
	 */
	public boolean getHasProblem(){
		return this.hasProblem;
	}
	/**
	 * @see  #getHasProblem()
	 */
	public void setHasProblem(boolean hasProblem){
		this.hasProblem = hasProblem;
	}
	
	/**
	 * Generates, according to the {@link eu.etaxonomy.cdm.strategy.strategy.cache.reference.IReferenceBaseCacheStrategy cache strategy}
	 * assigned to <i>this</i> reference, a string that identifies <i>this</i>
	 * reference and returns it. This string may be stored in the inherited
	 * {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity IdentifiableEntity}.
	 *
	 * @return  the string identifying <i>this</i> reference
	 * @see  	#getCitation()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 * @see  	eu.etaxonomy.cdm.strategy.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache()
	 */
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


	/** 
	 * Clones <i>this</i> reference. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> reference by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.media.IdentifyableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		ReferenceBase result = (ReferenceBase)super.clone();
		//no changes to: authorTeam, hasProblem, nomenclaturallyRelevant, uri
		return result;
	}
	
}