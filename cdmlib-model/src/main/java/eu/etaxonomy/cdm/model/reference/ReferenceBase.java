/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceBaseCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;

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
	"nomenclaturallyRelevant",
    "authorTeam"
})
@XmlRootElement(name = "RelationshipBase")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Audited
@Table(appliesTo="ReferenceBase", indexes = { @Index(name = "ReferenceBaseTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class ReferenceBase<S extends IReferenceBaseCacheStrategy> extends IdentifiableMediaEntity<S> implements IParsable, IMergable, IMatchable {
	private static final long serialVersionUID = -2034764545042691295L;
	private static final Logger logger = Logger.getLogger(ReferenceBase.class);
	
	//URIs like DOIs, LSIDs or Handles for this reference
	@XmlElement(name = "URI")
	@Field(index=org.hibernate.search.annotations.Index.TOKENIZED)
	private String uri;
	
	//flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	@XmlElement(name = "IsNomenclaturallyRelevant")
	@Merge(MergeMode.OR)
	private boolean nomenclaturallyRelevant;
	
	@XmlElement(name = "AuthorTeam")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	private TeamOrPersonBase authorTeam;

//	@XmlElement(name = "ReferenceIdentity")
//	@XmlIDREF
//	@XmlSchemaType(name = "IDREF")
//	@ManyToOne(fetch = FetchType.LAZY)
//	//@IndexedEmbedded
//	@Cascade(CascadeType.SAVE_UPDATE)
//	@Transient
//	private ReferenceIdentity referenceIdentity;
	
	@XmlAttribute
    @Match(MatchMode.IGNORE)
	private int parsingProblem = 0;
	
	@XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemStarts = -1;
    
    @XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemEnds = -1;
	
	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} who created the
	 * content of <i>this</i> reference.
	 * 
	 * @return  the author (team) of <i>this</i> reference
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase
	 */
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
		return this.nomenclaturallyRelevant;
	}

	/**
	 * @see #isNomenclaturallyRelevant()
	 */
	public void setNomenclaturallyRelevant(boolean nomenclaturallyRelevant){
		this.nomenclaturallyRelevant = nomenclaturallyRelevant;
	}

	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> reference.
	 * 
	 * @see  #generateTitle()
	 */
	// TODO implement
	public String getCitation(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getTitleCache(this);
		}
	}
	
	/**
	 * Returns a string containing the date (mostly only the year) of
	 * publication / creation of <i>this</i> reference.
	 */
	@Transient
	public abstract String getYear();

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#getHasProblem()
	 */
	public int getParsingProblem(){
		return this.parsingProblem;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#setHasProblem(boolean)
	 */
	public void setParsingProblem(int parsingProblem){
		this.parsingProblem = parsingProblem;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#hasProblem()
	 */
	public boolean hasProblem(){
		return parsingProblem != 0;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#hasProblem(eu.etaxonomy.cdm.strategy.parser.ParserProblem)
	 */
	public boolean hasProblem(ParserProblem problem) {
		return getParsingProblems().contains(problem);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#problemStarts()
	 */
	public int getProblemStarts(){
		return this.problemStarts;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#setProblemStarts(int)
	 */
	public void setProblemStarts(int start) {
		this.problemStarts = start;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#problemEnds()
	 */
	public int getProblemEnds(){
		return this.problemEnds;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#setProblemEnds(int)
	 */
	public void setProblemEnds(int end) {
		this.problemEnds = end;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#addProblem(eu.etaxonomy.cdm.strategy.parser.NameParserWarning)
	 */
	public void addParsingProblem(ParserProblem warning){
		parsingProblem = ParserProblem.addProblem(parsingProblem, warning);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#removeParsingProblem(eu.etaxonomy.cdm.strategy.parser.ParserProblem)
	 */
	public void removeParsingProblem(ParserProblem problem) {
		parsingProblem = ParserProblem.removeProblem(parsingProblem, problem);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IParsable#getParsingProblems()
	 */
	public List<ParserProblem> getParsingProblems() {
		return ParserProblem.warningList(this.parsingProblem);
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
//	@Override
//	public String generateTitle(){
//		if (cacheStrategy == null){
//			logger.warn("No CacheStrategy defined for ReferenceBase: " + this.getUuid());
//			return null;
//		}else{
//			return cacheStrategy.getTitleCache(this);
//		}
//	}
	
//**************************** CLONE *********************************/


//	/**
//	 * Returns the reference identity object
//	 * @return the referenceIdentity
//	 */
//	public ReferenceIdentity getReferenceIdentity() {
//		return referenceIdentity;
//	}
//
//	/**
//	 * For bidirectional use only
//	 * @param referenceIdentity the referenceIdentity to set
//	 */
//	protected void setReferenceIdentity(ReferenceIdentity referenceIdentity) {
//		this.referenceIdentity = referenceIdentity;
//	}
//	
//	/**
//	 * Returns the set of all identical references. Same as getReferenceIdentity().getReferences()
//	 * @return
//	 */
//	public Set<ReferenceBase> identicalReferences(){
//		return referenceIdentity.getReferences();
//	}
	/** 
	 * Clones <i>this</i> reference. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> reference by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException{
		ReferenceBase result = (ReferenceBase)super.clone();
		//no changes to: authorTeam, hasProblem, nomenclaturallyRelevant, uri
		return result;
	}


}