/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:12
 */
@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
public class BibtexReference extends ReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(BibtexReference.class);
	private BibtexEntryType type;
	//The journal or magazine the work was published in
	private String journal;
	//The title of the book, if only part of it is being cited
	private String booktitle;
	//The chapter number
	private String chapter;
	//The title of the work
	private String title;
	//The series of books the book was published in (e.g. "The Hardy Boys")
	private String series;
	//The edition of a book, long form (such as "first" or "second")
	private String edition;
	//The volume of a journal or multi-volume book
	private String volume;
	//The "number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number"
	//field.)
	private String number;
	//Page numbers, separated either by commas or double-hyphens
	private String pages;
	//An annotation for annotated bibliography styles (not typical)
	private String annote;
	//The name(s) of the editor(s)
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

	@ManyToOne
	public BibtexReference getCrossref(){
		return this.crossref;
	}
	public void setCrossref(BibtexReference crossref){
		this.crossref = crossref;
	}

	public String getJournal(){
		return this.journal;
	}

	/**
	 * 
	 * @param journal    journal
	 */
	public void setJournal(String journal){
		this.journal = journal;
	}

	public String getBooktitle(){
		return this.booktitle;
	}

	/**
	 * 
	 * @param booktitle    booktitle
	 */
	public void setBooktitle(String booktitle){
		this.booktitle = booktitle;
	}

	public String getChapter(){
		return this.chapter;
	}

	/**
	 * 
	 * @param chapter    chapter
	 */
	public void setChapter(String chapter){
		this.chapter = chapter;
	}

	public String getTitle(){
		return this.title;
	}

	/**
	 * 
	 * @param title    title
	 */
	public void setTitle(String title){
		this.title = title;
	}

	public String getSeries(){
		return this.series;
	}

	/**
	 * 
	 * @param series    series
	 */
	public void setSeries(String series){
		this.series = series;
	}

	public String getEdition(){
		return this.edition;
	}

	/**
	 * 
	 * @param edition    edition
	 */
	public void setEdition(String edition){
		this.edition = edition;
	}

	public String getVolume(){
		return this.volume;
	}

	/**
	 * 
	 * @param volume    volume
	 */
	public void setVolume(String volume){
		this.volume = volume;
	}

	public String getNumber(){
		return this.number;
	}

	/**
	 * 
	 * @param number    number
	 */
	public void setNumber(String number){
		this.number = number;
	}

	public String getPages(){
		return this.pages;
	}

	/**
	 * 
	 * @param pages    pages
	 */
	public void setPages(String pages){
		this.pages = pages;
	}

	public String getAnnote(){
		return this.annote;
	}

	/**
	 * 
	 * @param annote    annote
	 */
	public void setAnnote(String annote){
		this.annote = annote;
	}

	public String getEditor(){
		return this.editor;
	}

	/**
	 * 
	 * @param editor    editor
	 */
	public void setEditor(String editor){
		this.editor = editor;
	}

	public String getInstitution(){
		return this.institution;
	}

	/**
	 * 
	 * @param institution    institution
	 */
	public void setInstitution(String institution){
		this.institution = institution;
	}

	public String getSchool(){
		return this.school;
	}

	/**
	 * 
	 * @param school    school
	 */
	public void setSchool(String school){
		this.school = school;
	}

	public String getOrganization(){
		return this.organization;
	}

	/**
	 * 
	 * @param organization    organization
	 */
	public void setOrganization(String organization){
		this.organization = organization;
	}

	public String getPublisher(){
		return this.publisher;
	}

	/**
	 * 
	 * @param publisher    publisher
	 */
	public void setPublisher(String publisher){
		this.publisher = publisher;
	}

	public String getAddress(){
		return this.address;
	}

	/**
	 * 
	 * @param address    address
	 */
	public void setAddress(String address){
		this.address = address;
	}

	public String getHowpublished(){
		return this.howpublished;
	}

	/**
	 * 
	 * @param howpublished    howpublished
	 */
	public void setHowpublished(String howpublished){
		this.howpublished = howpublished;
	}

	public String getReportType(){
		return this.reportType;
	}

	/**
	 * 
	 * @param type    type
	 */
	public void setReportType(String type){
		this.reportType = type;
	}

	public String getMonth(){
		return this.month;
	}

	/**
	 * 
	 * @param month    month
	 */
	public void setMonth(String month){
		this.month = month;
	}

	public String getYear(){
		return this.year;
	}

	/**
	 * 
	 * @param year    year
	 */
	public void setYear(String year){
		this.year = year;
	}

	public String getEprint(){
		return this.eprint;
	}

	/**
	 * 
	 * @param eprint    eprint
	 */
	public void setEprint(String eprint){
		this.eprint = eprint;
	}

	public String getNote(){
		return this.note;
	}

	/**
	 * 
	 * @param note    note
	 */
	public void setNote(String note){
		this.note = note;
	}

	/**
	 * returns a formatted string containing the entire reference citation including
	 * authors
	 */
	@Transient
	public String getCitation(){
		return "";
	}

	/**
	 * returns a formatted string containing the reference citation excluding authors
	 * as used in a taxon name
	 */
	@Transient
	public String getNomenclaturalCitation(){
		return "";
	}

	@Override
	public String generateTitle(){
		return "";
	}

	@ManyToOne
	public BibtexEntryType getType() {
		return type;
	}

	public void setType(BibtexEntryType type) {
		this.type = type;
	}

}