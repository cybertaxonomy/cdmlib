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

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
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
//import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IParsable;
import eu.etaxonomy.cdm.model.common.TimePeriod;
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
@XmlType(name = "Reference", propOrder = {
	"uri",
	"nomenclaturallyRelevant",
    "authorTeam",
    "referenceAbstract",
    "title",
    "editor",
	"volume",
	"pages",
	"series",
    "edition",
    "isbn",
    "issn",
    "seriesPart",
    "datePublished",
    "publisher",
    "placePublished",
    "institution",
    "school",
    "organization",
    "inReference"
})
@XmlRootElement(name = "RelationshipBase")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Audited
@javax.persistence.Table(name="Reference")
@Table(appliesTo="Reference", indexes = { @org.hibernate.annotations.Index(name = "ReferenceTitleCacheIndex", columnNames = { "titleCache" }) })
public abstract class ReferenceBase<S extends IReferenceBaseCacheStrategy> extends IdentifiableMediaEntity<S> implements IParsable, IMergable, IMatchable {
	private static final long serialVersionUID = -2034764545042691295L;
	private static final Logger logger = Logger.getLogger(ReferenceBase.class);
	
	@XmlAttribute(name ="type")
	@Column(name="refType")  
	ReferenceType type;
	
	//Title of the reference
	@XmlElement(name ="Title" )
	@Column(length=4096, name="title")
	@Lob
	@Field(index=Index.TOKENIZED)
	@Match(MatchMode.EQUAL_REQUIRED)
	private String title;
	
//********************************************************/    

	
    @XmlElement(name = "Editor")
    @Field(index=Index.TOKENIZED)
	protected String editor;
	
    @XmlElement(name = "Series")
    @Field(index=Index.TOKENIZED)
	protected String series;
	
    @XmlElement(name = "Volume")
    @Field(index=Index.TOKENIZED)
	protected String volume;
	
    @XmlElement(name = "Pages")
    @Field(index=Index.TOKENIZED)
	protected String pages;
	
    @XmlElement(name = "Edition")
    @Field(index=Index.TOKENIZED)
	protected String edition;

    @XmlElement(name = "ISBN")
    @Field(index=Index.TOKENIZED)
	protected String isbn;
    
	@XmlElement(name = "ISSN")
	@Field(index=Index.TOKENIZED)
	protected String issn;
	
    @XmlElement(name = "SeriesPart")
    @Field(index=Index.TOKENIZED)
	protected String seriesPart;
    
	@XmlElement(name = "Organization")
	@Field(index=Index.TOKENIZED)
	protected String organization;
	
	@XmlElement(name = "Publisher")
	protected String publisher;
	
	@XmlElement(name = "PlacePublished")
	protected String placePublished;
    
	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	protected Institution institution;
	
	@XmlElement(name = "School")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade(CascadeType.SAVE_UPDATE)
	protected Institution school;
	
    @XmlElement(name = "InReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded
    @Cascade(CascadeType.SAVE_UPDATE)
	protected ReferenceBase inReference;
    
//********************************************************/    
    
	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	@XmlElement(name ="DatePublished" )
	@Embedded
	@IndexedEmbedded
	private TimePeriod datePublished = TimePeriod.NewInstance();
	
	@XmlElement(name ="Abstract" )
	@Column(length=65536, name="referenceAbstract")
	@Lob
	private String referenceAbstract;  //abstract is a reserved term in Java
	
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
	
//*************************** GETTER / SETTER ******************************************/    
	
	/**
	 * Returns a string representing the title of <i>this</i> reference. If a
	 * reference has different titles (for instance abbreviated and not
	 * abbreviated) then for each title a new instance must be created.
	 * 
	 * @return  the title string of <i>this</i> reference
	 * @see 	#getCitation()
	 */
	public String getTitle(){
		return this.title;
	}
	/**
	 * @see 	#getTitle()
	 */
	public void setTitle(String title){
		this.title = title;
	}
    

	/**
	 * Returns the date (mostly only the year) of publication / creation of
	 * <i>this</i> reference.
	 */
	public TimePeriod getDatePublished(){
		return this.datePublished;
	}
	/**
	 * @see 	#getDatePublished()
	 */
	public void setDatePublished(TimePeriod datePublished){
		this.datePublished = datePublished;
	}
	
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
	 * @return the referenceAbstract
	 */
	public String getReferenceAbstract() {
		return referenceAbstract;
	}

	/**
	 * @param referenceAbstract the referenceAbstract to set
	 */
	public void setReferenceAbstract(String referenceAbstract) {
		this.referenceAbstract = referenceAbstract;
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
	
//****************************************************  /	
	
//	/**
//	 * Returns the string representing the name of the editor of <i>this</i>
//	 * generic reference. An editor is mostly a person (team) who assumed the
//	 * responsibility for the content of the publication as a whole without
//	 * being the author of this content.<BR>
//	 * If there is an editor then the generic reference must be some
//	 * kind of {@link PrintedUnitBase physical printed unit}.
//	 * 
//	 * @return  the string identifying the editor of <i>this</i>
//	 * 			generic reference
//	 * @see 	#getPublisher()
//	 */
//	protected String getEditor(){
//		return this.editor;
//	}
//
//	/**
//	 * @see #getEditor()
//	 */
//	protected void setEditor(String editor){
//		this.editor = editor;
//	}
//
//	/**
//	 * Returns the string representing the series (for instance for books or
//	 * within journals) - and series part - in which <i>this</i> generic reference
//	 * was published.<BR>
//	 * If there is a series then the generic reference must be some
//	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
//	 * 
//	 * @return  the string identifying the series for <i>this</i>
//	 * 			generic reference
//	 */
//	protected String getSeries(){
//		return this.series;
//	}
//
//	/**
//	 * @see #getSeries()
//	 */
//	protected void setSeries(String series){
//		this.series = series;
//	}
//
//	/**
//	 * Returns the string representing the volume (for instance for books or
//	 * within journals) in which <i>this</i> generic reference was published.<BR>
//	 * If there is a volume then the generic reference must be some
//	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
//	 * 
//	 * @return  the string identifying the volume for <i>this</i>
//	 * 			generic reference
//	 */
//	protected String getVolume(){
//		return this.volume;
//	}
//
//	/**
//	 * @see #getVolume()
//	 */
//	protected void setVolume(String volume){
//		this.volume = volume;
//	}
//
//	/**
//	 * Returns the string representing the page(s) where the content of
//	 * <i>this</i> generic reference is located.<BR>
//	 * If there is a pages information then the generic reference must be some
//	 * kind of {@link PrintedUnitBase physical printed unit} or an {@link Article article}.
//	 * 
//	 * @return  the string containing the pages corresponding to <i>this</i>
//	 * 			generic reference
//	 */
//	protected String getPages(){
//		return this.pages;
//	}
//
//	/**
//	 * @see #getPages()
//	 */
//	protected void setPages(String pages){
//		this.pages = pages;
//	}

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
	 * Returns a string representation for the year of publication / creation
	 * of <i>this</i> reference. The string is obtained by transformation of
	 * the {@link #getDatePublished() datePublished} attribute.
	 */
	@Transient
	public String getYear(){
		if (this.getDatePublished() != null && this.getDatePublished().getStart() != null){
			return getDatePublished().getYear();
		}else{
			return null;
		}
	}
	
	

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
	
//*********************** CLONE ********************************************************/
	
	/** 
	 * Clones <i>this</i> reference. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> reference by
	 * modifying only some of the attributes.
	 * 
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			ReferenceBase result = (ReferenceBase)super.clone();
			result.setDatePublished(datePublished != null? (TimePeriod)datePublished.clone(): null);
			//no changes to: title, authorTeam, hasProblem, nomenclaturallyRelevant, uri
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
	
}