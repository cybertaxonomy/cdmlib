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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.strategy.cache.reference.BibtexDefaultCacheStrategy;

/**
 * This class represents references which are structured according to the BibTeX
 * format. The flat BibTeX format is an usual alternative to handle references 
 * (see "http://en.wikipedia.org/wiki/BibTeX"). Therefore this class might be
 * used instead of {@link StrictReferenceBase StrictReferenceBase} depending on the data
 * to be imported in the CDM.
 * 
 * publisher for Report is "institution" in BibTex ???
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:12
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class BibtexReference extends ReferenceBase implements INomenclaturalReference {
	private static final Logger logger = Logger.getLogger(BibtexReference.class);
	
	private BibtexEntryType type;
	private String journal;
	private String booktitle;
	private String chapter;
	private String title;
	private String series;
	private String edition;
	private String volume;
	private String number;
	private String pages;
	private String annote;
	private String editor;
	//The institution that was involved in the publishing, but not necessarily the publisher
	private String institution;
	//The school where the thesis was written
	private String school;
	//The conference sponsor
	private String organization;
	//The publisher's name
	private String publisher;
	//Publisher's address (usually just the city, but can be the full address for lesser-known publishers)
	private String address;
	//How it was published, if the publishing method is nonstandard
	private String howpublished;
	//The type of tech-report, for example, "Research Note"
	private String reportType;
	//The month of publication (or, if unpublished, the month of creation)
	private String month;
	//The year of publication (or, if unpublished, the year of creation)
	private String year;
	//A specification of an electronic publication, often a preprint or a technical report
	private String eprint;
	//Miscellaneous extra information
	private String note;
	private BibtexReference crossref;

	private NomenclaturalReferenceHelper nomRefBase = NomenclaturalReferenceHelper.NewInstance(this);

	
	/** 
	 * Class constructor: creates a new empty BibTeX reference instance only
	 * containing the {@link strategy.cache.reference.BibtexDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.BibtexDefaultCacheStrategy
	 */
	protected BibtexReference(){
		super();
		this.cacheStrategy = BibtexDefaultCacheStrategy.NewInstance();
	}

	/** 
	 * Creates a new empty BibTeX reference instance only containing the
	 * {@link strategy.cache.reference.BibtexDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see strategy.cache.reference.BibtexDefaultCacheStrategy
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
	 * and {@link InProceedings#getInProceedings() "inProceedings"} attributes of BookSection and of
	 * InProccedings.
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
	 * attribute of Article.
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
	 * attribute of BookSection.
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
	 * attribute of BookSection.
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
	 * attribute of StrictReferenceBase.
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
	 * attribute of Book.
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
	 * attribute of Book.
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
	 * attribute of StrictReferenceBase.
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
	 * of PrintedUnitBase.
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

	public String getInstitution(){
		return this.institution;
	}

	/**
	 * @see #getInstitution()
	 */
	public void setInstitution(String institution){
		this.institution = institution;
	}

	public String getSchool(){
		return this.school;
	}

	/**
	 * @see #getSchool()
	 */
	public void setSchool(String school){
		this.school = school;
	}

	public String getOrganization(){
		return this.organization;
	}

	/**
	 * @see #getOrganization()
	 */
	public void setOrganization(String organization){
		this.organization = organization;
	}

	public String getPublisher(){
		return this.publisher;
	}

	/**
	 * @see #getPublisher()
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}

	public String getAddress(){
		return this.address;
	}

	/**
	 * @see #getAddress()
	 */
	public void setAddress(String address){
		this.address = address;
	}

	public String getHowpublished(){
		return this.howpublished;
	}

	/**
	 * @see #getHowpublished()
	 */
	public void setHowpublished(String howpublished){
		this.howpublished = howpublished;
	}

	public String getReportType(){
		return this.reportType;
	}

	/**
	 * @see #getReportType()
	 */
	public void setReportType(String type){
		this.reportType = type;
	}

	public String getMonth(){
		return this.month;
	}

	/**
	 * @see #getMonth()
	 */
	public void setMonth(String month){
		this.month = month;
	}

	public String getYear(){
		return this.year;
	}

	/**
	 * @see #getYear()
	 */
	public void setYear(String year){
		this.year = year;
	}

	public String getEprint(){
		return this.eprint;
	}

	/**
	 * @see #getEprint()
	 */
	public void setEprint(String eprint){
		this.eprint = eprint;
	}

	public String getNote(){
		return this.note;
	}

	/**
	 * @see #getNote()
	 */
	public void setNote(String note){
		this.note = note;
	}

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
	 * @see eu.etaxonomy.cdm.model.reference.StrictReferenceBase#getCitation()
	 */
	@Transient
	public String getCitation(){
		return nomRefBase.getCitation();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.INomenclaturalReference#getNomenclaturalCitation(java.lang.String)
	 */
	@Transient
	public String getNomenclaturalCitation(String microReference) {
		return nomRefBase.getNomenclaturalCitation(microReference);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.reference.ReferenceBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		return nomRefBase.generateTitle();
	}

}