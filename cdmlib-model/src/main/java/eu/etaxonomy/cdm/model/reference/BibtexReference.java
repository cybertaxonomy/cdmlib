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
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy;

/**
 * This class represents references which are structured according to the BibTeX
 * format. The flat BibTeX format is an usual alternative to handle references 
 * (see <a href="http://en.wikipedia.org/wiki/BibTeX">BibTeX</a>)
 * Therefore this class might be
 * used instead of {@link StrictReferenceBase StrictReferenceBase} depending on the data
 * to be imported in the CDM.
 * 
 * @see BibtexEntryType
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BibtexReference", propOrder = {
    "type",
    "journal",
    "booktitle",
    "chapter",
    "title",
    "series",
    "edition",
    "volume",
    "number",
    "pages",
    "annote",
    "editor",
    "institution",
    "school",
    "organization",
    "publisher",
    "address",
    "howpublished",
    "reportType",
    "month",
    "year",
    "eprint",
    "note",
    "crossref",
    "nomRefBase"
})
@XmlRootElement(name = "BibtexReference")
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class BibtexReference extends ReferenceBase implements INomenclaturalReference, Cloneable {
	private static final Logger logger = Logger.getLogger(BibtexReference.class);
	
	@XmlElement(name = "BibtexEntryType")
	private BibtexEntryType type;
	
	@XmlElement(name = "Journal")
	private String journal;
	
	@XmlElement(name = "Booktitle")
	private String booktitle;
	
	@XmlElement(name = "Chapter")
	private String chapter;
	
	@XmlElement(name = "Title")
	private String title;
	
	@XmlElement(name = "Series")
	private String series;
	
	@XmlElement(name = "Edition")
	private String edition;
	
	@XmlElement(name = "Volume")
	private String volume;
	
	@XmlElement(name = "Number")
	private String number;
	
	@XmlElement(name = "Pages")
	private String pages;
	
	@XmlElement(name = "Annote")
	private String annote;
	
	@XmlElement(name = "Editor")
	private String editor;
	
	@XmlElement(name = "Institution")
	private String institution;
	
	@XmlElement(name = "School")
	private String school;
	
	@XmlElement(name = "Organization")
	private String organization;
	
	@XmlElement(name = "Publisher")
	private String publisher;
	
	@XmlElement(name = "Address")
	private String address;
	
	@XmlElement(name = "HowPublished")
	private String howpublished;
	
	@XmlElement(name = "ReportType")
	private String reportType;
	
	@XmlElement(name = "Month")
	private String month;
	
	@XmlElement(name = "Year")
	private String year;
	
	@XmlElement(name = "Eprint")
	private String eprint;
	
	@XmlElement(name = "Note")
	private String note;
	
	@XmlElement(name = "Crossref")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
	private BibtexReference crossref;

    @XmlElementRef(name = "NomenclaturalReferenceBase")
	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	/** 
	 * Class constructor: creates a new empty BibTeX reference instance only
	 * containing the {@link eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy
	 */
	protected BibtexReference(){
		super();
		this.cacheStrategy = BibtexDefaultCacheStrategy.NewInstance();
	}

	/** 
	 * Creates a new empty BibTeX reference instance only containing the
	 * {@link eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy
	 */
	public static BibtexReference NewInstance(){
		BibtexReference result = new BibtexReference();
		return result;
	}

	/**
	 * Returns the BibTeX reference <i>this</i> BibTeX reference belongs to (for
	 * instance a BibTeX reference with the {@link BibtexEntryType entry type} "INBOOK" belongs
	 * to another BibTeX reference with the entry type "BOOK").<BR>
	 * The returned "crossref" attribute corresponds to the {@link BookSection#getInBook() "inBook"}
	 * and {@link InProceedings#getInProceedings() "inProceedings"} attributes of {@link BookSection BookSection} and of
	 * {@link InProceedings InProceedings}.
	 * 
	 * @return  the BibTeX reference containing <i>this</i> BibTeX reference
	 * @see 	BibtexEntryType
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public BibtexReference getCrossref(){
		return this.crossref;
	}
	/**
	 * @see #getCrossref()
	 */
	public void setCrossref(BibtexReference crossref){
		this.crossref = crossref;
	}

	/**
	 * Returns the string representing the journal or magazine title <i>this</i>
	 * BibTeX reference with the {@link BibtexEntryType entry type} "ARTICLE" was
	 * published in.<BR>
	 * The returned "journal" attribute corresponds to the {@link Article#getInJournal() "inJournal"}
	 * attribute of {@link Article Article}.
	 * 
	 * @return  the string identifying the journal where <i>this</i> BibTeX
	 * 			reference (article) was published
	 */
	public String getJournal(){
		return this.journal;
	}

	/**
	 * @see #getJournal()
	 */
	public void setJournal(String journal){
		this.journal = journal;
	}

	/**
	 * Returns the string representing the book title <i>this</i>
	 * BibTeX reference, with the {@link BibtexEntryType entry type} "INBOOK", "INCOLLECTION"
	 * "INPROCEEDINGS" or "CONFERENCE", was published in.<BR>
	 * The returned "booktitle" attribute corresponds to the {@link BookSection#getInBook() "inBook"}
	 * attribute of {@link BookSection BookSection}.
	 * 
	 * @return  the string identifying the book where <i>this</i> BibTeX
	 * 			reference (part of a book) was published
	 */
	public String getBooktitle(){
		return this.booktitle;
	}

	/**
	 * @see #getBooktitle()
	 */
	public void setBooktitle(String booktitle){
		this.booktitle = booktitle;
	}

	/**
	 * Returns the string representing the chapter number of <i>this</i>
	 * BibTeX reference with the {@link BibtexEntryType entry type} "INBOOK" or "INCOLLECTION"
	 * if this part of a book is a chapter.<BR>
	 * In this case the returned string is included in the inherited {@link BookSection#getTitle() "title"}
	 * attribute of {@link BookSection BookSection}.
	 * 
	 * @return  the string with the chapter number corresponding to <i>this</i>
	 * 			BibTeX reference
	 */
	public String getChapter(){
		return this.chapter;
	}

	/**
	 * @see #getChapter()
	 */
	public void setChapter(String chapter){
		this.chapter = chapter;
	}

	/**
	 * Returns the string representing the title of <i>this</i> BibTeX reference.<BR>
	 * The returned "title" attribute corresponds to the {@link StrictReferenceBase#getTitle() "title"}
	 * attribute of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string with the title of <i>this</i> BibTeX reference
	 */
	public String getTitle(){
		return this.title;
	}

	/**
	 * @see #getTitle()
	 */
	public void setTitle(String title){
		this.title = title;
	}

	/**
	 * Returns the string representing the series of books <i>this</i>
	 * BibTeX reference with the {@link BibtexEntryType entry type} "BOOK" or "INBOOK"
	 * was published in.<BR>
	 * The returned "series" attribute corresponds to the inherited {@link PrintedUnitBase#getInSeries() "inSeries"}
	 * attribute of {@link Book Book}.
	 * 
	 * @return  the string identifying the book series <i>this</i> BibTeX
	 * 			reference (book) was published in
	 */
	public String getSeries(){
		return this.series;
	}

	/**
	 * @see #getSeries()
	 */
	public void setSeries(String series){
		this.series = series;
	}

	/**
	 * Returns the string representing the edition of <i>this</i>
	 * BibTeX reference with the {@link BibtexEntryType entry type} "BOOK", "INBOOK"
	 * or "MANUAL".<BR>
	 * The returned "edition" attribute corresponds to the {@link Book#getEdition() "edition"}
	 * attribute of {@link Book Book}.
	 * 
	 * @return  the string identifying the edition of <i>this</i> BibTeX
	 * 			reference (book)
	 */
	public String getEdition(){
		return this.edition;
	}

	/**
	 * @see #getEdition()
	 */
	public void setEdition(String edition){
		this.edition = edition;
	}

	/**
	 * Returns the string representing either the volume of <i>this</i>
	 * BibTeX reference if it is a {@link BibtexEntryType#BOOK() "BOOK"} or of the journal
	 * in which <i>this</i> BibTeX reference was published if it is an {@link BibtexEntryType#ARTICLE() "ARTICLE"}.<BR>
	 * The returned "volume" attribute corresponds to the "volume" attributes
	 * of {@link PrintedUnitBase#getVolume() PrintedUnitBase} and {@link Article#getVolume() Article}.
	 * 
	 * @return  the string identifying the volume of <i>this</i> BibTeX
	 * 			reference (article or book)
	 */
	public String getVolume(){
		return this.volume;
	}

	/**
	 * @see #getVolume()
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	/**
	 * Returns the string representing, if applicable, either the number of <i>this</i>
	 * BibTeX reference if it is a {@link BibtexEntryType#TECHREPORT() "TECHREPORT" (report)}
	 * or of the journal in which <i>this</i> BibTeX reference was published if
	 * it is an {@link BibtexEntryType#ARTICLE() "ARTICLE"}. Most publications have a "volume", but no "number".<BR>
	 * In this case the returned string is included in the {@link StrictReferenceBase#getTitle() "title"}
	 * attribute of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string identifying the number for <i>this</i> BibTeX
	 * 			reference (article or technical report)
	 */
	public String getNumber(){
		return this.number;
	}

	/**
	 * @see #getNumber()
	 */
	public void setNumber(String number){
		this.number = number;
	}

	/**
	 * Returns the string representing the pages range (separated either by
	 * commas or double-hyphens) of <i>this</i> BibTeX reference.
	 * 
	 * @return  the string with the pages corresponding to <i>this</i> BibTeX
	 * 			reference
	 */
	public String getPages(){
		return this.pages;
	}

	/**
	 * @see #getPages()
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

	/**
	 * Returns the string representing a (not typical) annotation for annotated
	 * bibliography styles to be applied to <i>this</i> BibTeX reference.
	 * 
	 * @return  the string with the annotation for annotated bibliography styles
	 */
	public String getAnnote(){
		return this.annote;
	}

	/**
	 * @see #getAnnote()
	 */
	public void setAnnote(String annote){
		this.annote = annote;
	}

	/**
	 * Returns the string representing the name(s) of the editor(s) of <i>this</i>
	 * BibTeX reference.<BR>
	 * The returned "editor" attribute corresponds to the {@link PrintedUnitBase#getEditor() "editor"} attribute
	 * of {@link PrintedUnitBase PrintedUnitBase}.
	 * 
	 * @return  the string identifying the editor of <i>this</i>
	 * 			BibTeX reference
	 */
	public String getEditor(){
		return this.editor;
	}

	/**
	 * @see #getEditor()
	 */
	public void setEditor(String editor){
		this.editor = editor;
	}

	/**
	 * Returns the string representing the name of the institution that was
	 * involved in the publishing of <i>this</i> BibTeX reference or the name of
	 * its publisher, if it is a {@link BibtexEntryType#TECHREPORT() "TECHREPORT" (report)}.<BR>
	 * The returned "institution" attribute corresponds to the {@link Report#getInstitution() "institution"}
	 * attribute of {@link Report Report} or to its inherited {@link PublicationBase#getPublisher() "publisher"} attribute.
	 * 
	 * @return  the string identifying the institution assigned to <i>this</i>
	 * 			BibTeX reference
	 */
	public String getInstitution(){
		return this.institution;
	}

	/**
	 * @see #getInstitution()
	 */
	public void setInstitution(String institution){
		this.institution = institution;
	}

	/**
	 * Returns the string representing the name of the school where <i>this</i> BibTeX
	 * reference (a {@link BibtexEntryType#MASTERTHESIS() "MASTERTHESIS"} or a
	 * {@link BibtexEntryType#PHDTHESIS() "PHDTHESIS"}) was written.<BR>
	 * The returned "school" attribute corresponds to the {@link Thesis#getSchool() "school"}
	 * attribute of {@link Thesis Thesis}.
	 * 
	 * @return  the string identifying the school where <i>this</i> BibTeX 
	 * 			reference was written
	 */
	public String getSchool(){
		return this.school;
	}

	/**
	 * @see #getSchool()
	 */
	public void setSchool(String school){
		this.school = school;
	}

	/**
	 * Returns the string representing the name of the organization responsible for the
	 * conference in the context of which <i>this</i> BibTeX reference ({@link BibtexEntryType#PROCEEDINGS() "PROCEEDINGS"},
	 * {@link BibtexEntryType#INPROCEEDINGS() "INPROCEEDINGS"} or a {@link BibtexEntryType#CONFERENCE() "CONFERENCE"})
	 * has been printed.<BR>
	 * The returned "organization" attribute corresponds to the {@link Proceedings#getOrganization() "organization"}
	 * attribute of {@link Proceedings Proceedings}.
	 * 
	 * @return  the string with the responsible organization of the conference
	 */
	public String getOrganization(){
		return this.organization;
	}

	/**
	 * @see #getOrganization()
	 */
	public void setOrganization(String organization){
		this.organization = organization;
	}

	/**
	 * Returns the string representing the name of the publisher of <i>this</i>
	 * BibTeX reference.<BR>
	 * The returned "publisher" attribute corresponds to the {@link PublicationBase#getPublisher() "publisher"} attribute
	 * of {@link PublicationBase PublicationBase}.
	 * 
	 * @return  the string identifying the publisher of <i>this</i>
	 * 			BibTeX reference
	 */
	public String getPublisher(){
		return this.publisher;
	}

	/**
	 * @see #getPublisher()
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}

	/**
	 * Returns the string representing the address of the publisher of <i>this</i>
	 * BibTeX reference (usually just the city, but can be the full address for
	 * lesser-known publishers).<BR>
	 * The returned "address" attribute corresponds to the {@link PublicationBase#getPlacePublished() "placePublished"} attribute
	 * of {@link PublicationBase PublicationBase}.
	 * 
	 * @return  the string with the publisher's address
	 */
	public String getAddress(){
		return this.address;
	}

	/**
	 * @see #getAddress()
	 */
	public void setAddress(String address){
		this.address = address;
	}

	/**
	 * Returns the string describing how <i>this</i> BibTeX reference was
	 * published, if the publishing method was nonstandard. This can apply to
	 * the entry types {@link BibtexEntryType#BOOKLET() "BOOKLET"} or {@link BibtexEntryType#MISC() "MISC"}.<BR>
	 * The returned "howpublished" attribute might correspond to one of the
	 * {@link eu.etaxonomy.cdm.model.common.AnnotatableEntity#getAnnotations() annotations}
	 * of {@link PublicationBase PublicationBase}.
	 * 
	 * @return  the string describing the publishing method
	 */
	public String getHowpublished(){
		return this.howpublished;
	}

	/**
	 * @see #getHowpublished()
	 */
	public void setHowpublished(String howpublished){
		this.howpublished = howpublished;
	}

	/**
	 * Returns the string describing the kind of technical report (for instance
	 * "Research Note") <i>this</i> BibTeX reference ({@link BibtexEntryType#TECHREPORT() "TECHREPORT"}) is.<BR>
	 * The returned "reportType" attribute might correspond to one of the
	 * {@link common.AnnotatableEntity#getAnnotations()() annotations}
	 * of {@link Report Report}.
	 * 
	 * @return  the string describing the kind of technical report
	 */
	public String getReportType(){
		return this.reportType;
	}

	/**
	 * @see #getReportType()
	 */
	public void setReportType(String type){
		this.reportType = type;
	}

	/**
	 * Returns the string with the month of publication (or, if unpublished,
	 * the month of creation) of <i>this</i> BibTeX reference.<BR>
	 * The returned "month" attribute corresponds partially to the {@link StrictReferenceBase#getDatePublished() "datePublished"}
	 * attribute of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string with the month of publication
	 */
	public String getMonth(){
		return this.month;
	}

	/**
	 * @see #getMonth()
	 */
	public void setMonth(String month){
		this.month = month;
	}

	/**
	 * Returns the string with the year of publication (or, if unpublished,
	 * the year of creation) of <i>this</i> BibTeX reference.<BR>
	 * The returned "year" attribute corresponds partially to the {@link StrictReferenceBase#getDatePublished() "datePublished"}
	 * attribute of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string with the year of publication
	 */
	@Override
	public String getYear(){
		return this.year;
	}

	/**
	 * @see #getYear()
	 */
	public void setYear(String year){
		this.year = year;
	}

	//A specification of an electronic publication, often a preprint or a technical report
	/**
	 * Returns the string specifying <i>this</i> BibTeX reference as an electronic
	 * publication (often a preprint or a technical report).<BR>
	 * The returned "eprint" attribute might correspond to one of the
	 * {@link eu.etaxonomy.cdm.model.common.AnnotatableEntity#getAnnotations() annotations}
	 * of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string specifying <i>this</i> electronic BibTeX reference
	 */
	public String getEprint(){
		return this.eprint;
	}

	/**
	 * @see #getEprint()
	 */
	public void setEprint(String eprint){
		this.eprint = eprint;
	}

	/**
	 * Returns the string with miscellaneous extra information for <i>this</i> BibTeX
	 * reference.<BR>
	 * The returned "note" attribute corresponds to one of the {@link eu.etaxonomy.cdm.model.common.AnnotatableEntity#getAnnotations() annotations}
	 * of {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the string with extra information for <i>this</i> BibTeX reference
	 */
	public String getNote(){
		return this.note;
	}

	/**
	 * @see #getNote()
	 */
	public void setNote(String note){
		this.note = note;
	}

	/**
	 * Returns the {@link BibtexEntryType entry type} of <i>this</i> BibTeX reference.
	 * BibTeX references are split by types which correspond to subclasses of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @return  the BibTeX entry type of <i>this</i> BibTeX reference
	 */
	@ManyToOne
	public BibtexEntryType getType() {
		return type;
	}

	/**
	 * @see #getType()
	 */
	public void setType(BibtexEntryType type) {
		this.type = type;
	}
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#setDatePublished(eu.etaxonomy.cdm.model.common.TimePeriod)
	 */
	public void setDatePublished(TimePeriod datePublished) {
		//TODO
		logger.warn("Not yet implemented");
	}

	/**
	 * Returns a formatted string containing the entire reference citation,
	 * including authors and other elements corresponding to <i>this</i> BibTeX
	 * reference.
	 * 
	 * @see  #getNomenclaturalCitation(String)
	 * @see  ReferenceBase#getCitation()
	 */
	@Override
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/**
	 * Returns a formatted string containing the entire citation used for
	 * nomenclatural purposes based on <i>this</i> BibTeX reference - including
	 * (abbreviated) title  but not authors - and on the given details.
	 * 
	 * @param  microReference	the string with the details (generally pages)
	 * 							within <i>this</i> BibTeX reference
	 * @return					the formatted string representing the
	 * 							nomenclatural citation
	 * @see  					getCitation()
	 * @see  					INomenclaturalReference#getNomenclaturalCitation(String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/**
	 * Generates, according to the {@link strategy.cache.reference.BibtexDefaultCacheStrategy default cache strategy}
	 * assigned to <i>this</i> BibTeX reference, a string that identifies <i>this</i>
	 * BibTeX reference and returns it. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.<BR>
	 * This method overrides the generic and inherited generateTitle method
	 * from {@link ReferenceBase ReferenceBase}.
	 *
	 * @return  the string identifying <i>this</i> BibTeX reference
	 * @see  	#getCitation()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache()
	 * @see  	eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}
	
	
//****************** clone ********************** //

	
	/** 
	 * Clones <i>this</i> bibtex reference. This is a shortcut that enables to
	 * create a new instance that differs only slightly from <i>this</i> bibtext
	 * by modifying only some of the attributes.<BR>
	 * This method overrides the clone method from {@link ReferenceBase ReferenceBase}.
	 * 
	 * @see ReferenceBase#clone()
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public BibtexReference clone(){
		try{
			BibtexReference result = (BibtexReference)super.clone();
			result.nomRefBase = NomenclaturalReferenceHelper.NewInstance(result);
			result.setCrossref(this.getCrossref());
			//no changes to: crossref, type
			return result;
		} catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

}