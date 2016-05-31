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
import java.net.URI;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Table;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.hibernate.search.DoiBridge;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.strategy.cache.reference.DefaultReferenceCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.reference.INomenclaturalReferenceCacheStrategy;
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
 * an abstract class with many subclasses. Not it is only
 * one class implementing many interfaces for safe use.
 * <P>
 * This class corresponds to: <ul>
 * <li> PublicationCitation according to the TDWG ontology
 * <li> Publication according to the TCS
 * <li> Reference according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:47
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
    "placePublished",
    "institution",
    "school",
    "organization",
    "inReference"
})
@XmlRootElement(name = "Reference")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@Audited
@Table(appliesTo="Reference", indexes = { @org.hibernate.annotations.Index(name = "ReferenceTitleCacheIndex", columnNames = { "titleCache" }) })
//@InReference(groups=Level3.class)
@ReferenceCheck(groups=Level2.class)
@InReference(groups=Level3.class)
@NoRecursiveInReference(groups=Level3.class)  //may become Level1 in future  #
public class Reference
        extends IdentifiableMediaEntity<INomenclaturalReferenceCacheStrategy>
        implements IArticle, IBook, IPatent, IDatabase, IJournal, IBookSection,ICdDvd,
                   IGeneric,IInProceedings, IProceedings, IPrintSeries, IReport,
                   IThesis,IWebPage, IPersonalCommunication,
                   INomenclaturalReference, IReference,
                   Cloneable {

    private static final long serialVersionUID = -2034764545042691295L;
	private static final Logger logger = Logger.getLogger(Reference.class);

	@XmlAttribute(name ="type")
	@Column(name="refType")
	@NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
    	parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.reference.ReferenceType")}
    )
	@Audited
	protected ReferenceType type;

	//Title of the reference
	@XmlElement(name ="Title" )
	@Column(length=4096, name="title")
	@Lob
	@Field
	@Match(MatchMode.EQUAL_REQUIRED)
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
	protected String editor;

    @XmlElement(name = "Volume")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	protected String volume;

    @XmlElement(name = "Pages")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	protected String pages;

    @XmlElement(name = "Edition")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	protected String edition;

    @XmlElement(name = "ISBN")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	@Pattern(regexp = "(?=.{13}$)\\d{1,5}([- ])\\d{1,7}\\1\\d{1,6}\\1(\\d|X)$", groups = Level2.class, message = "{eu.etaxonomy.cdm.model.reference.Reference.isbn.message}")
	protected String isbn;

    @XmlElement(name = "Doi")
    @Field
    @FieldBridge(impl = DoiBridge.class)
    @Type(type="doiUserType")
    @Column(length=DOI.MAX_LENGTH)
    protected DOI doi;


	@XmlElement(name = "ISSN")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	@Pattern(regexp = "(?=.{9}$)\\d{4}([- ])\\d{4} (\\d|X)$", groups = Level2.class, message = "{eu.etaxonomy.cdm.model.reference.Reference.issn.message}")
	protected String issn;

    @XmlElement(name = "SeriesPart")
    @Field
    //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	protected String seriesPart;

	@XmlElement(name = "Organization")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	protected String organization;

	@XmlElement(name = "Publisher")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	protected String publisher;


	@XmlElement(name = "PlacePublished")
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
    @Column(length=255)
	protected String placePublished;

	@XmlElement(name = "Institution")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	protected Institution institution;

	@XmlElement(name = "School")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	protected Institution school;

    @XmlElement(name = "InReference")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
//    @IndexedEmbedded
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
   // @InReference(groups=Level2.class)
   	protected Reference inReference;

//********************************************************/

	//The date range assigned to the reference. ISO Date range like. Flexible, year can be left out, etc
	@XmlElement(name ="DatePublished" )
	@Embedded
	@IndexedEmbedded
	private TimePeriod datePublished = TimePeriod.NewInstance();

	@XmlElement(name ="Abstract" )
	@Column(length=65536, name="referenceAbstract")
	@Lob
    @Field
    //TODO Val #3379
//	@NullOrNotEmpty
	private String referenceAbstract;  //abstract is a reserved term in Java


	//URIs like DOIs, LSIDs or Handles for this reference
	@XmlElement(name = "URI")
	@Field(analyze = Analyze.NO)
	@Type(type="uriUserType")
	private URI uri;

	//flag to subselect only references that could be useful for nomenclatural citations. If a reference is used as a
	//nomenclatural reference in a name this flag should be automatically set
	@XmlElement(name = "IsNomenclaturallyRelevant")
	@Merge(MergeMode.OR)
	private boolean nomenclaturallyRelevant;

	@XmlElement(name = "Authorship")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@IndexedEmbedded
	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	private TeamOrPersonBase<?> authorship;

	@XmlAttribute
    @Match(MatchMode.IGNORE)
	private int parsingProblem = 0;

	@XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemStarts = -1;

    @XmlAttribute
    @Match(MatchMode.IGNORE)
    private int problemEnds = -1;

    @Transient
    @XmlAttribute
    @Match(MatchMode.IGNORE)
	private boolean cacheStrategyRectified = false;

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
		this.setCacheStrategy(DefaultReferenceCacheStrategy.NewInstance());
	}

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


	@Override
	public String getAbbrevTitleCache() {
		if (protectedAbbrevTitleCache){
            return this.abbrevTitleCache;
        }
        // is reference dirty, i.e. equal NULL?
        if (abbrevTitleCache == null){
            this.abbrevTitleCache = generateAbbrevTitle();
            this.abbrevTitleCache = getTruncatedCache(this.abbrevTitleCache) ;
        }
        return abbrevTitleCache;
	}

	@Override
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
		this.abbrevTitle = StringUtils.isBlank(abbrevTitle) ? null : abbrevTitle;
	}


	@Override
    public String getEditor() {
		return editor;
	}


	@Override
    public void setEditor(String editor) {
		this.editor = StringUtils.isBlank(editor)? null : editor;
	}

	@Override
    public String getVolume() {
		return volume;
	}

	@Override
    public void setVolume(String volume) {
		this.volume = StringUtils.isBlank(volume)? null : volume;
	}

	@Override
    public String getPages() {
		return pages;
	}

	@Override
    public void setPages(String pages) {
		this.pages = StringUtils.isBlank(pages)? null : pages;
	}

	@Override
    public String getEdition() {
		return edition;
	}

	@Override
    public void setEdition(String edition) {
		this.edition = StringUtils.isBlank(edition)? null : edition;
	}

	@Override
    public String getIsbn() {
		return isbn;
	}

	@Override
    public void setIsbn(String isbn) {
		this.isbn = StringUtils.isBlank(isbn)? null : isbn;
	}

	@Override
    public String getIssn() {
		return issn;
	}

	@Override
    public void setIssn(String issn) {
		this.issn = StringUtils.isBlank(issn)? null : issn;
	}

    @Override
	public DOI getDoi() {
		return doi;
	}

    @Override
	public void setDoi(DOI doi) {
		this.doi = doi;
	}

	@Override
    public String getSeriesPart() {
		return seriesPart;
	}

	@Override
    public void setSeriesPart(String seriesPart) {
		this.seriesPart = StringUtils.isBlank(seriesPart)? null : seriesPart;
	}

	@Override
    public String getOrganization() {
		return organization;
	}

	@Override
    public void setOrganization(String organization) {
		this.organization = StringUtils.isBlank(organization)? null : organization;
	}

	@Override
    public String getPublisher() {
		return publisher;
	}

	@Override
    public void setPublisher(String publisher) {
		this.publisher = StringUtils.isBlank(publisher)? null : publisher;
	}

	@Override
    public void setPublisher(String publisher, String placePublished){
		this.publisher = publisher;
		this.placePublished = placePublished;
	}

	@Override
    public String getPlacePublished() {
		return placePublished;
	}

	@Override
    public void setPlacePublished(String placePublished) {
		this.placePublished = StringUtils.isBlank(placePublished)? null: placePublished;
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
	 *
	 * @param type
	 * @return
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
		this.title = StringUtils.isBlank(title)? null : title;
	}

	/**
	 * Returns the date (mostly only the year) of publication / creation of
	 * <i>this</i> reference.
	 */
	@Override
    public TimePeriod getDatePublished(){
		return this.datePublished;
	}
	/**
	 * @see 	#getDatePublished()
	 */
	@Override
    public void setDatePublished(TimePeriod datePublished){
		this.datePublished = datePublished;
	}

	public boolean hasDatePublished(){
		boolean result =  ! ( (this.datePublished == null) || StringUtils.isBlank(datePublished.toString()));
		return result;
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
		this.referenceAbstract = StringUtils.isBlank(referenceAbstract)? null : referenceAbstract;
	}


	/**
	 * Returns "true" if the isNomenclaturallyRelevant flag is set. This
	 * indicates that a {@link TaxonNameBase taxon name} has been originally
	 * published in <i>this</i> reference following the rules of a
	 * {@link eu.etaxonomy.cdm.model.name.NomenclaturalCode nomenclature code} and is therefore used for
	 * nomenclatural citations. This flag will be set as soon as <i>this</i>
	 * reference is used as a nomenclatural reference for any taxon name.<BR>
	 * FIXME what happens if the only taxon name referencing this reference is not
	 * any longer using this reference as a nomenclatural reference. How does the
	 * reference get informed about the fact that it is not nomenclaturally relevant
	 * anymore?
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


	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors, corresponding to <i>this</i> reference.
	 *
	 * @see  #generateTitle()
	 */
	// TODO implement
	@Transient
	public String getCitation(){
		if (getCacheStrategy() == null){
			logger.warn("No CacheStrategy defined for "+ this.getClass() + ": " + this.getUuid());
			return null;
		}else{
			return getCacheStrategy().getTitleCache(this);
		}
	}


	@Override
    public String generateTitle() {
		return super.generateTitle();
	}

    public String generateAbbrevTitle() {
		return getCacheStrategy().getFullAbbrevTitleString(this);
	}

	/**
	 * Returns a string representation for the year of publication / creation
	 * of <i>this</i> reference. If the {@link #getDatePublished() datePublished}
	 * of this reference contains more date information then (starting) year
	 * only the year is returned.
	 * than  attribute.
	 */
	@Override
    @Transient
	public String getYear(){
		TimePeriod datePublished = this.getDatePublished();
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
		TimePeriod datePublished = this.getDatePublished();
		if (datePublished != null ){
			return getDatePublished().toString();
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
		String typeName = this.getType()== null ? "(no type defined)" : this.getType().getMessage();
		if (getCacheStrategy() == null){
			logger.warn("No CacheStrategy defined for "+ typeName + ": " + this.getUuid());
			return null;
		}else{
		    if (getCacheStrategy() instanceof INomenclaturalReferenceCacheStrategy){
				return cacheStrategy.getNomenclaturalCitation(this, microReference);
			}else {
				logger.warn("No INomenclaturalReferenceCacheStrategy defined for "+ typeName + ": " + this.getUuid());
				return null;
			}
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

//*************************** CACHE STRATEGIES ******************************/

    @Override
    public INomenclaturalReferenceCacheStrategy getCacheStrategy() {
    	return this.cacheStrategy;
    }

	@Override
    public void setCacheStrategy(INomenclaturalReferenceCacheStrategy referenceCacheStrategy) {
		this.cacheStrategy = referenceCacheStrategy;
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
	public Object clone() {
		try {
			Reference result = (Reference)super.clone();
			result.setDatePublished(datePublished != null? (TimePeriod)datePublished.clone(): null);
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
			String result = "Reference [type=" + type + ", id= " + this.getId() + ", uuid=" + this.uuid ;
			result += title == null ? "" : ", title=" + title;
			result += abbrevTitle == null ? "" : ", abbrevTitle=" + abbrevTitle;
			result += "]";
			return result;
		}else{
			return super.toString();
		}
	}




}

