/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.reference;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;
import org.joda.time.Partial;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.format.common.TimePeriodPartialFormatter;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.hibernate.search.DateTimeBridge;
import eu.etaxonomy.cdm.hibernate.search.DoiBridge;
import eu.etaxonomy.cdm.hibernate.search.UriBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.ExternallyManaged;
import eu.etaxonomy.cdm.model.common.IIntextReferenceTarget;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.strategy.cache.reference.IReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.ReferenceDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.merge.Merge;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
import eu.etaxonomy.cdm.validation.Level2;
import eu.etaxonomy.cdm.validation.Level3;
import eu.etaxonomy.cdm.validation.annotation.InReference;
import eu.etaxonomy.cdm.validation.annotation.NoRecursiveInReference;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;
import eu.etaxonomy.cdm.validation.annotation.ReferenceCheck;

/**
 * The class for references (information sources). Originally
 * an abstract class with many subclasses. Now it is only
 * one class implementing many interfaces for safe use of different
 * types of references. E.g. if you want to edit a journal
 * you create a journal with {@link ReferenceFactory#newJournal()}
 * which returns an IJournal. Though this instance is an ordinary instance
 * of {@link Reference} by using IJournal you may not use attributes
 * not allowed for journals.<p>
 * References can be created via {@link ReferenceFactory} methods.
 * <P>
 * This class corresponds to: <ul>
 * <li> PublicationCitation according to the TDWG ontology
 * <li> Publication according to the TCS
 * <li> Reference according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:47
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Reference", propOrder = {
	"type",
	"uri",
    "abbrevTitleCache",
    "protectedAbbrevTitleCache",
	"nomenclaturallyRelevant",
    "authorship",
    "referenceAbstract",
    "title",
    "abbrevTitle",
    "editor",
	"volume",
	"pages",
	"edition",
    "isbn",
    "issn",
    "doi",
    "seriesPart",
    "datePublished",
    "publisher",
    "publisher2",
    "placePublished",
    "placePublished2",
    "institution",
    "school",
    "organization",
    "inReference",
    "accessed",
    "externallyManaged",
})
@XmlRootElement(name = "Reference")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Audited
@Table(name="Reference", indexes = { @javax.persistence.Index(name = "referenceTitleCacheIndex", columnList = "titleCache") })
//@InReference(groups=Level3.class)
@ReferenceCheck(groups=Level2.class)
@InReference(groups=Level3.class)
@NoRecursiveInReference(groups=Level3.class)  //may become Level1 in future  #
public class Reference
        extends IdentifiableMediaEntity<IReferenceCacheStrategy>
        implements IArticle, IBook, IPatent, IDatabase, IJournal, IBookSection,ICdDvd,
                   IGeneric,IInProceedings, IProceedings, IPrintSeries, IReport,
                   IThesis,IWebPage, IPersonalCommunication,
                   IIntextReferenceTarget {

    private static final long serialVersionUID = -2034764545042691295L;
	private static final Logger logger = LogManager.getLogger();

//  from E+M import (still needed?)
//	@Column(length=255)
//  private String refAuthorString;
//	public String getRefAuthorString() {return refAuthorString;}
//  public void setRefAuthorString(String refAuthorString) {this.refAuthorString = refAuthorString;}

    @XmlAttribute(name ="type")
	@Column(name="refType")
	@NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
    	parameters = {@Parameter(name  = "enumClass",
    	value = "eu.etaxonomy.cdm.model.reference.ReferenceType")}
    )
	@Audited
	private ReferenceType type;

	//Title of the reference
	@XmlElement(name ="Title" )
	@Column(length=4096, name="title")
	@Lob
	@Field
	@Match(MatchMode.EQUAL_REQUIRED) //TODO correct? was EQUAL_REQUIRED before, but with abbrevTitle this is not realistic anymore, see also #6427
    //TODO Val #3379
//	@NullOrNotEmpty
	private String title;

	//Title of the reference
	@XmlElement(name ="AbbrevTitle" )
	@Field
	@Match(MatchMode.EQUAL)  //TODO check if this is correct
	@NullOrNotEmpty
	@Column(length=255)
	private String abbrevTitle;

	//Title of the reference
	@XmlElement(name ="AbbrevTitleCache" )
	@Field
	@Match(MatchMode.CACHE)
    //TODO Val #3379
//	@NotNull
	@Column(length=1024)
	private String abbrevTitleCache;

	@XmlElement(name = "protectedAbbrevTitleCache")
	@Merge(MergeMode.OR)
	private boolean protectedAbbrevTitleCache;

//********************************************************/

    @XmlElement(name = "Editor")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String editor;

    @XmlElement(name = "Volume")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String volume;

    @XmlElement(name = "Pages")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String pages;

    @XmlElement(name = "Edition")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String edition;

    @XmlElement(name = "ISBN")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	@Pattern(regexp = "(?=.{13}$)\\d{1,5}([- ])\\d{1,7}\\1\\d{1,6}\\1(\\d|X)$", groups = Level2.class, message = "{eu.etaxonomy.cdm.model.reference.Reference.isbn.message}")
    private String isbn;

    @XmlElement(name = "Doi")
    @Field
    @FieldBridge(impl = DoiBridge.class)
    @Type(type="doiUserType")
    @Column(length=DOI.MAX_LENGTH)
    private DOI doi;

	@XmlElement(name = "ISSN")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	@Pattern(regexp = "(?=.{9}$)\\d{4}([- ])\\d{4} (\\d|X)$", groups = Level2.class, message = "{eu.etaxonomy.cdm.model.reference.Reference.issn.message}")
	private String issn;

    @XmlElement(name = "SeriesPart")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
    private String seriesPart;

	@XmlElement(name = "Organization")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	private String organization;

	@XmlElement(name = "Publisher")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	private String publisher;

    @XmlElement(name = "Publisher2")
    @Field
    @Column(length=255)
    private String publisher2;

	@XmlElement(name = "PlacePublished")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	private String placePublished;

    @XmlElement(name = "PlacePublished2")
    @Field
    @Column(length=255)
    private String placePublished2;

	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Institution institution;

	@XmlElement(name = "School")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private Institution school;

    @XmlElement(name = "InReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
//  @InReference(groups=Level2.class)
    private Reference inReference;

//********************************************************/

	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	@XmlElement(name ="DatePublished" )
	@Embedded
	@IndexedEmbedded
	private VerbatimTimePeriod datePublished = VerbatimTimePeriod.NewVerbatimInstance();

    //#5258
    @XmlElement(name = "Accessed", type= String.class)
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type="dateTimeUserType")
    @Basic(fetch = FetchType.LAZY)
    @Match(MatchMode.EQUAL)
    @FieldBridge(impl = DateTimeBridge.class)
    private DateTime accessed;

    @XmlElement(name ="Abstract" )
	@Column(length=CLOB_LENGTH, name="referenceAbstract")
	@Lob
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
	private String referenceAbstract;  //abstract is a reserved term in Java


	//URIs like DOIs, LSIDs or Handles for this reference
	@XmlElement(name = "URI")
	@Field(analyze = Analyze.NO)
    @FieldBridge(impl = UriBridge.class)
	@Type(type="uriUserType")
	private URI uri;

	//flag to subselect only references that could be useful for nomenclatural citations.
	//If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	@XmlElement(name = "IsNomenclaturallyRelevant")
	@Merge(MergeMode.OR)
	@Match(MatchMode.IGNORE)
	private boolean nomenclaturallyRelevant;

	@XmlElement(name = "Authorship")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TeamOrPersonBase<?> authorship;

    private ExternallyManaged externallyManaged;

	@XmlAttribute
    @Match(MatchMode.IGNORE)
	private int parsingProblem = 0;

	@XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemStarts = -1;

    @XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemEnds = -1;


// *********************** CONSTRUCTOR ************************/

    //for hibernate use only, *packet* private required by bytebuddy
    //TODO currenctly still protected as OpenUrlReference inherits from Reference
    //     this should be fixed
    protected Reference(){
		this(ReferenceType.Generic);  //just in case someone uses constructor
	}

	protected Reference(ReferenceType type) {
		super();
	    if (type == null){
			this.type = ReferenceType.Generic;
		} else{
			this.type = type;
		}
	}

// *********************** LISTENER ************************/


	@Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
            	if (!ev.getPropertyName().equals("titleCache") && !ev.getPropertyName().equals("abbrevTitleCache") && !ev.getPropertyName().equals("cacheStrategy")){
            		if (! isProtectedTitleCache()){
            			titleCache = null;
            		}
            		if (! isProtectedAbbrevTitleCache()){
            			abbrevTitleCache = null;
            		}
            	}
            }
        };
        addPropertyChangeListener(listener);
    }

//*************************** GETTER / SETTER ******************************************/

    // @Transient - must not be transient, since this property needs to be included in all serializations produced by the remote layer
    @Override
    public String getTitleCache(){
        String result = super.getTitleCache();
        if (isBlank(result) && !isProtectedTitleCache()){
            this.titleCache = this.getAbbrevTitleCache(true);
        }
        return titleCache;
    }

	@Override
	public String getAbbrevTitleCache() {
		return getAbbrevTitleCache(false);
	}

	/**
	 * Implements {@link #getAbbrevTitleCache()} but allows to
	 * avoid never ending recursions if both caches are empty
	 * avoidRecursion should only be <code>true</code> if called
	 * by {@link #getTitleCache()}
	 * @param avoidRecursion
	 * @return
	 */
	private String getAbbrevTitleCache(boolean avoidRecursion) {
        if (protectedAbbrevTitleCache){
            return this.abbrevTitleCache;
        }
        // is reference dirty, i.e. equal NULL?
        if (abbrevTitleCache == null){
            this.abbrevTitleCache = generateAbbrevTitle();
            this.abbrevTitleCache = getTruncatedCache(this.abbrevTitleCache) ;
        }
        if (isBlank(abbrevTitleCache) && !avoidRecursion){
            this.abbrevTitleCache = this.getTitleCache();
        }
        return abbrevTitleCache;
    }

    /**
     * Sets the {@link #getAbbrevTitleCache() abbreviated title cache}
     *
     * @param abbrevTitleCache
     * @deprecated this method exists only for compliance with the java bean standard.
     * It usually has little effect as it will not protect the cache.
     * Use {@link #setAbbrevTitleCache(String, boolean)} instead use
     * {@link #setAbbrevTitleCache(String, boolean)} to protect the cache.
     */
	@Deprecated
	public void setAbbrevTitleCache(String abbrevTitleCache) {
		this.abbrevTitleCache = abbrevTitleCache;
	}

	@Override
	public void setAbbrevTitleCache(String abbrevTitleCache, boolean isProtected) {
		this.protectedAbbrevTitleCache = isProtected;
		setAbbrevTitleCache(abbrevTitleCache);
	}

	@Override
	public boolean isProtectedAbbrevTitleCache() {
		return protectedAbbrevTitleCache;
	}

	@Override
	public void setProtectedAbbrevTitleCache(boolean protectedAbbrevTitleCache) {
		this.protectedAbbrevTitleCache = protectedAbbrevTitleCache;
	}

	@Override
	public String getAbbrevTitle() {
		return abbrevTitle;
	}

	@Override
	public void setAbbrevTitle(String abbrevTitle) {
		this.abbrevTitle = isBlank(abbrevTitle) ? null : abbrevTitle;
	}


	@Override
    public String getEditor() {
		return editor;
	}


	@Override
    public void setEditor(String editor) {
		this.editor = isBlank(editor)? null : editor;
	}

	@Override
    public String getVolume() {
		return volume;
	}

	@Override
    public void setVolume(String volume) {
		this.volume = isBlank(volume)? null : volume;
	}

	@Override
    public String getPages() {
		return pages;
	}

	@Override
    public void setPages(String pages) {
		this.pages = isBlank(pages)? null : pages;
	}

	@Override
    public String getEdition() {
		return edition;
	}

	@Override
    public void setEdition(String edition) {
		this.edition = isBlank(edition)? null : edition;
	}

	@Override
    public String getIsbn() {
		return isbn;
	}

	@Override
    public void setIsbn(String isbn) {
		this.isbn = isBlank(isbn)? null : isbn;
	}

	@Override
    public String getIssn() {
		return issn;
	}

	@Override
    public void setIssn(String issn) {
		this.issn = isBlank(issn)? null : issn;
	}

    @Override
	public DOI getDoi() {
		return doi;
	}
    @Override
	public void setDoi(DOI doi) {
		this.doi = doi;
	}
    /**
     * Convenience method to retrieve doi as string
     */
    @Transient @XmlTransient @java.beans.Transient
    public String getDoiString() {
        return doi == null? null : doi.toString();
    }
    /**
     * Convenience method to retrieve doi as uri string
     */
    @Transient @XmlTransient @java.beans.Transient
    public String getDoiUriString() {
        return doi == null? null : doi.asURI();
    }

	@Override
    public String getSeriesPart() {
		return seriesPart;
	}
	@Override
    public void setSeriesPart(String seriesPart) {
		this.seriesPart = isBlank(seriesPart)? null : seriesPart;
	}

	@Override
    public String getOrganization() {
		return organization;
	}

	@Override
    public void setOrganization(String organization) {
		this.organization = isBlank(organization)? null : organization;
	}

	@Override
    public String getPublisher() {
		return publisher;
	}
	@Override
    public void setPublisher(String publisher) {
		this.publisher = StringUtils.truncate(isBlank(publisher)? null : publisher, 255);
	}
    @Override
    public void setPublisher(String publisher, String placePublished){
        this.publisher = publisher;
        this.placePublished = placePublished;
    }

    @Override
    public String getPublisher2() {
        return publisher2;
    }
    @Override
    public void setPublisher2(String publisher2) {
        this.publisher2 = StringUtils.truncate(isBlank(publisher2)? null : publisher2, 255);
    }
    @Override
    public void setPublisher2(String publisher2, String placePublished2){
        this.publisher2 = publisher2;
        this.placePublished2 = placePublished2;
    }

	@Override
    public String getPlacePublished() {
		return placePublished;
	}
	@Override
    public void setPlacePublished(String placePublished) {
		this.placePublished = isBlank(placePublished)? null: placePublished;
	}

    @Override
    public String getPlacePublished2() {
        return placePublished2;
    }
    @Override
    public void setPlacePublished2(String placePublished2) {
        this.placePublished2 = isBlank(placePublished2)? null: placePublished2;
    }

	@Override
    public Institution getInstitution() {
		return institution;
	}
	@Override
    public void setInstitution(Institution institution) {
		this.institution = institution;
	}

	@Override
    public Institution getSchool() {
		return school;
	}
	@Override
    public void setSchool(Institution school) {
		this.school = school;
	}

	@Override
    public Reference getInReference() {
		return inReference;
	}
	@Override
    public void setInReference(Reference inReference) {
		this.inReference = inReference;
	}

	@Override
    public void setType(ReferenceType type) {
		if (type == null){
			this.type = ReferenceType.Generic;
		} else{
			this.type = type;
		}
	}
	@Override
    public ReferenceType getType() {
		return type;
	}

	/**
	 * Whether this reference is of the given type
	 */
	@Override
    public boolean isOfType(ReferenceType type){
		return type == getType();
	}

	/**
	 * Returns a string representing the title of <i>this</i> reference. If a
	 * reference has different titles (for instance abbreviated and not
	 * abbreviated) then for each title a new instance must be created.
	 *
	 * @return  the title string of <i>this</i> reference
	 * @see 	#getCitation()
	 */
	@Override
    public String getTitle(){
		return this.title;
	}
	/**
	 * @see 	#getTitle()
	 */
	@Override
    public void setTitle(String title){
		this.title = isBlank(title)? null : title;
	}

	/**
	 * Returns the date (mostly only the year) of publication / creation of
	 * <i>this</i> reference.
	 */
	@Override
    public VerbatimTimePeriod getDatePublished(){
		return this.datePublished;
	}
	/**
	 * @see 	#getDatePublished()
	 */
	@Override
    public void setDatePublished(VerbatimTimePeriod datePublished){
		this.datePublished = datePublished;
	}
    @Override
    @Transient
    @Deprecated
    public VerbatimTimePeriod setDatePublished(TimePeriod datePublished){
        VerbatimTimePeriod newTimePeriod = VerbatimTimePeriod.toVerbatim(datePublished);
        setDatePublished(newTimePeriod);
        return newTimePeriod;
    }

	public boolean hasDatePublished(){
		boolean result = !((this.datePublished == null) || isBlank(datePublished.toString()));
		return result;
	}


	@Override
    public DateTime getAccessed() {
        return accessed;
    }

	@Override
    public void setAccessed(DateTime accessed) {
        this.accessed = accessed;
    }

	/**
	 * Returns the {@link eu.etaxonomy.cdm.model.agent.TeamOrPersonBase author (team)} who created the
	 * content of <i>this</i> reference.
	 *
	 * @return  the author (team) of <i>this</i> reference
	 * @see 	eu.etaxonomy.cdm.model.agent.TeamOrPersonBase
	 */
	@Override
    public TeamOrPersonBase getAuthorship(){
		return this.authorship;
	}

	/**
	 * @see #getAuthorship()
	 */
	@Override
    public void setAuthorship(TeamOrPersonBase authorship){
		this.authorship = authorship;
	}

	/**
	 * Returns the Uniform Resource Identifier (URI) corresponding to <i>this</i>
	 * reference. An URI is a string of characters used to identify a resource
	 * on the Internet.
	 *
	 * @return  the URI of <i>this</i> reference
	 */
	@Override
    public URI getUri(){
		return this.uri;
	}
	/**
	 * @see #getUri()
	 */
	@Override
    public void setUri(URI uri){
		this.uri = uri;
	}

	/**
	 * @return the referenceAbstract
	 */
	@Override
    public String getReferenceAbstract() {
		return referenceAbstract;
	}

	/**
	 * @param referenceAbstract the referenceAbstract to set
	 */
	@Override
    public void setReferenceAbstract(String referenceAbstract) {
		this.referenceAbstract = isBlank(referenceAbstract)? null : referenceAbstract;
	}


	/**
	 * Returns "true" if the isNomenclaturallyRelevant flag is set. This
	 * indicates that a {@link TaxonName taxon name} has been originally
	 * published in <i>this</i> reference following the rules of a
	 * {@link eu.etaxonomy.cdm.model.name.NomenclaturalCode nomenclature code} and is therefore used for
	 * nomenclatural citations. This flag will be set as soon as <i>this</i>
	 * reference is used as a nomenclatural reference for any taxon name.<BR>
	 * FIXME what happens if the only taxon name referencing this reference is not
	 * any longer using this reference as a nomenclatural reference. How does the
	 * reference get informed about the fact that it is not nomenclaturally relevant
	 * anymore?
	 * @deprecated currently not supported and not in use, may be removed in future
	 */
	@Deprecated
    public boolean isNomenclaturallyRelevant(){
		return this.nomenclaturallyRelevant;
	}

	/**
	 * @see #isNomenclaturallyRelevant()
	 * @deprecated currently not supported and not in use, may be removed in future
	 */
	@Deprecated
	public void setNomenclaturallyRelevant(boolean nomenclaturallyRelevant){
		this.nomenclaturallyRelevant = nomenclaturallyRelevant;
	}


//****************************************************  /

	@Transient
	@Override
	public void setTitleCaches(String cache){
	    this.setAbbrevTitleCache(cache, true);
	    this.setTitleCache(cache, true);
	}


	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> reference.
	 *
	 * @see  #generateTitle()
	 */
	// TODO implement
	@Transient
	public String getCitation(){
		if (cacheStrategy() == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return cacheStrategy().getTitleCache(this);
		}
	}


	@Override
    public String generateTitle() {
		return super.generateTitle();
	}

    public String generateAbbrevTitle() {
		return cacheStrategy().getNomenclaturalTitleCache(this);
	}

	/**
	 * Returns a string representation for the year of publication / creation
	 * of <i>this</i> reference. If the {@link #getDatePublished() datePublished}
	 * of this reference contains more date information than (starting) year
	 * only the year is returned.
	 */
	@Override
    @Transient
	public String getYear(){
		VerbatimTimePeriod datePublished = this.getDatePublished();
		if (datePublished != null ){
			String result = getDatePublished().getYear();
			return result;
		}else{
			return null;
		}
	}

	/**
	 * Convenience method that returns a string representation for the publication date / creation
	 * of <i>this</i> reference. The string is obtained by
	 * {@link #getDatePublished()#toString() the string representation
	 * of the date published}.
	 */
	@Transient
	public String getDatePublishedString(){
		VerbatimTimePeriod datePublished = this.getDatePublished();
		if (datePublished != null ){
			return getDatePublished().toString();
		}else{
			return null;
		}
	}

    /**
     * Returns a sortable string of the datePublished.start attribute.<BR>
     * If datePublished is null in-references are called recursively.
     * Only structured publication data is considered, no freetext or
     * verbatim date.
     */
    @Transient
    public String getSortableDateString(){
        VerbatimTimePeriod datePublished = this.getDatePublished();
        if (datePublished != null ){
            Partial partial = getDatePublished().getStart();
            if (partial == null) {
                partial = getDatePublished().getEnd();
            }
            if (partial != null ) {
                return TimePeriodPartialFormatter.INSTANCE().printSortableDateString(partial);
            }
        }
        if (this.inReference != null){
            return this.inReference.getSortableDateString();
        }else {
            return "zzzz-zz-zz";
        }
    }

	/**
     * Convenience method that returns a string representation for the publication date / creation
     * of <i>this</i> reference. The string is obtained by
     * {@link #getDatePublished()#toString() the string representation
     * of the date published}.
     */
    @Transient
    public String getTimePeriodPublishedString(){
        VerbatimTimePeriod datePublished = this.getDatePublished();
        if (datePublished != null ){
            return getDatePublished().getTimePeriod();
        }else{
            return null;
        }
    }

	@Override
    public int getParsingProblem(){
		return this.parsingProblem;
	}

	@Override
    public void setParsingProblem(int parsingProblem){
		this.parsingProblem = parsingProblem;
	}

	@Override
    public boolean hasProblem(){
		return parsingProblem != 0;
	}

	@Override
    public boolean hasProblem(ParserProblem problem) {
		return getParsingProblems().contains(problem);
	}

	@Override
    public int getProblemStarts(){
		return this.problemStarts;
	}

	@Override
    public void setProblemStarts(int start) {
		this.problemStarts = start;
	}

	@Override
    public int getProblemEnds(){
		return this.problemEnds;
	}

	@Override
    public void setProblemEnds(int end) {
		this.problemEnds = end;
	}

	@Override
    public void addParsingProblem(ParserProblem warning){
		parsingProblem = ParserProblem.addProblem(parsingProblem, warning);
	}

	@Override
    public void removeParsingProblem(ParserProblem problem) {
		parsingProblem = ParserProblem.removeProblem(parsingProblem, problem);
	}

	@Override
    @Transient
	public List<ParserProblem> getParsingProblems() {
		return ParserProblem.warningList(this.parsingProblem);
	}


	@Override
    @Transient
    public String getNomenclaturalCitation(String microReference) {
		String typeName = this.getType()== null ? "(no type defined)" : this.getType().getLabel();
		if (cacheStrategy() == null){
		    throw new IllegalStateException("No CacheStrategy defined for "+ typeName + ": " + this.getUuid());
		}else{
		    return NomenclaturalSourceFormatter.INSTANCE().format(this, microReference);
		}
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
//			logger.warn("No CacheStrategy defined for Reference: " + this.getUuid());
//			return null;
//		}else{
//			return cacheStrategy.getTitleCache(this);
//		}
//	}



//********** Casting methods ***********************************/

	/**
	 * @return
	 */
	public IArticle castReferenceToArticle(){
		setType(ReferenceType.Article);
		return this;
	}

	public IBook castReferenceToBook(){
		setType(ReferenceType.Book);
		return this;
	}

	public IBookSection castReferenceToBookSection(){
		setType(ReferenceType.BookSection);
		return this;
	}

	public ICdDvd castReferenceToCdDvd(){
		setType(ReferenceType.CdDvd);
		return this;
	}

	public IDatabase castReferenceToDatabase(){
		setType(ReferenceType.Database);
		return this;
	}

	public IGeneric castReferenceToGeneric(){
		setType(ReferenceType.Generic);
		return this;
	}

	public IInProceedings castReferenceToInProceedings(){
		setType(ReferenceType.InProceedings);
		return this;
	}

	public IJournal castReferenceToJournal(){
		setType(ReferenceType.Journal);
		return this;
	}

	public IMap castReferenceToMap(){
		setType(ReferenceType.Map);
		return (IMap) this;
	}

	public IPatent castReferenceToPatent(){
		setType(ReferenceType.Patent);
		return this;
	}

	public IPersonalCommunication castReferenceToPersonalCommunication(){
		setType(ReferenceType.PersonalCommunication);
		return this;
	}

	public IPrintSeries castReferenceToPrintSeries(){
		setType(ReferenceType.PrintSeries);
		return this;
	}

	public IWebPage castReferenceToWebPage(){
		setType(ReferenceType.WebPage);
		return this;
	}

	public IProceedings castReferenceToProceedings(){
		setType(ReferenceType.Proceedings);
		return this;
	}

	public IReport castReferenceToReport(){
		setType(ReferenceType.Report);
		return this;
	}

	public IThesis castReferenceToThesis(){
		setType(ReferenceType.Thesis);
		return this;
	}


	@Override
    @Transient // prevent from being serialized by webservice
	public IJournal getInJournal() {
		IJournal journal = this.inReference;
		return journal;
	}

	@Override
    public void setInJournal(IJournal journal) {
		setInReference((Reference)journal);  //user setter to invoke aspect #1815
	}

	@Override
    @Transient // prevent from being serialized by webservice
	public IPrintSeries getInSeries() {
		return this.inReference;
	}

	@Override
    public void setInSeries(IPrintSeries inSeries) {
	    setInReference((Reference)inSeries);  //user setter to invoke aspect  #1815
	}

	@Override
    @Transient // prevent from being serialized by webservice
	public IBook getInBook() {
		IBook book = this.inReference;
		return book;
	}

//********************** In-References *****************************************

	@Override
    public void setInBook(IBook book) {
	    setInReference((Reference)book);  //user setter to invoke aspect #1815
	}

	@Override
    @Transient // prevent from being serialized by webservice
	public IProceedings getInProceedings() {
		IProceedings proceedings = this.inReference;
		return proceedings;
	}

	@Override
    public void setInProceedings(IProceedings proceeding) {
        setInReference((Reference)proceeding);  //user setter to invoke aspect #1815
	}

//**************************** Type *****************************************/

    public boolean isArticle() {
        return this.getType().isArticle();
    }
    public boolean isBook() {
        return this.getType().isBook();
    }
    public boolean isBookSection() {
        return this.getType().isBookSection();
    }
    public boolean isWebPage() {
        return this.getType().isWebPage();
    }
    public boolean isDatabase() {
        return this.getType().isDatabase();
    }
    public boolean isMap() {
        return this.getType().isMap();
    }
    public boolean isPatent() {
        return this.getType().isPatent();
    }
    public boolean isGeneric() {
        return this.getType().isGeneric();
    }
    public boolean isCdDvd() {
        return this.getType().isCdDvd();
    }
    public boolean isProceedings() {
        return this.getType().isProceedings();
    }
    public boolean isInProceedings() {
        return this.getType().isInProceedings();
    }
    public boolean isJournal() {
        return this.getType().isJournal();
    }
    public boolean isPersonalCommunication() {
        return this.getType().isPersonalCommunication();
    }
    public boolean isThesis() {
        return this.getType().isThesis();
    }
    public boolean isPrintSeries() {
        return this.getType().isPrintSeries();
    }
    /**
     * @return <code>true</code> if this type is exactly {@link ReferenceType#Section}
     * @see ReferenceType#isSection()
     */
    public boolean isSectionOnly() {
        return this.getType().isSectionOnly();
    }
    /**
     * Returns <code>true</code> if this reference is part of another reference
     * (inheriting from {@link ISection}) and therefore may have an in-reference and pages.
     * @see ReferenceType#isSection()
     */
    public boolean isSection() {
        return this.getType().isSection();
    }
    /**
     * @see ReferenceType#isPrintedUnit()
     */
    public boolean isPrintedUnit() {
        return this.getType().isPrintedUnit();
    }
    /**
     * @return <code>true</code> if the type of this reference
     *         supports the {@link IDynamicReference} interface. Currently these are
     *         webpages, databases and maps.
     */
    public boolean isDynamic() {
        return this.getType().isDynamic();
    }

//*************************** CACHE STRATEGIES ******************************/

    @Override
    protected void initDefaultCacheStrategy() {
        this.setCacheStrategy(ReferenceDefaultCacheStrategy.NewInstance());
    }

   @Override
   public boolean updateCaches(){
       //TODO shouldn't this be moved to the cache strategy?
       if (this.equals(this.getInReference())){
           String message = "-- invalid inreference (self-referencing) --";
           String oldTitleCache = this.titleCache;
           this.titleCache = message;
           return !message.equals(oldTitleCache);
       }
       boolean result = super.updateCaches();
       if (this.protectedAbbrevTitleCache == false){
           String oldAbbrevTitleCache = this.abbrevTitleCache;

           String newAbbrevTitleCache = getTruncatedCache(cacheStrategy().getNomenclaturalTitleCache(this));
           if (newAbbrevTitleCache.equals("")){
               newAbbrevTitleCache = cacheStrategy().getTitleCache(this);
           }

           if ( oldAbbrevTitleCache == null || ! oldAbbrevTitleCache.equals(newAbbrevTitleCache) ){
                this.setAbbrevTitleCache(null, false);
                String newCache = this.getAbbrevTitleCache();

                if (newCache == null){
                    logger.warn("New abbrevCache should never be null");
                }
                if (oldAbbrevTitleCache == null){
                    logger.info("oldAbbrevTitleCache was illegaly null and has been fixed");
                }
                result = true;
            }
        }
        return result;
    }

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
	public Reference clone() {
		try {
			Reference result = (Reference)super.clone();
			result.setDatePublished(datePublished != null? (VerbatimTimePeriod)datePublished.clone(): null);
			//no changes to: title, authorship, hasProblem, nomenclaturallyRelevant, uri
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

//******************************* toString *****************************/

	@Override
	public String toString() {
		if (type != null){
			String result = "Reference [type=" + type  ;
			result += title == null ? "" : ", title=" + title;
			result += abbrevTitle == null ? "" : ", abbrevTitle=" + abbrevTitle;
			result += ", id= " + this.getId() + ", uuid=" + this.uuid;
			result += "]";
			return result;
		}else{
			return super.toString();
		}
	}
}