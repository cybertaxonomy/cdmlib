/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.reference;


import org.apache.log4j.Logger;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:36
 */
public class BibtexReference extends ReferenceBase implements INomenclaturalReference {
	static Logger logger = Logger.getLogger(BibtexReference.class);

	//The name(s) of the author(s) (in the case of more than one author, separated by and)
	@Description("The name(s) of the author(s) (in the case of more than one author, separated by and)")
	private String author;
	//The journal or magazine the work was published in

	@Description("The journal or magazine the work was published in 
	")
	private String journal;
	//The title of the book, if only part of it is being cited
	@Description("The title of the book, if only part of it is being cited")
	private String booktitle;
	//The chapter number
	@Description("The chapter number")
	private String chapter;
	//The title of the work

	@Description("The title of the work 
	")
	private String title;
	//The series of books the book was published in (e.g. "The Hardy Boys")

	@Description("The series of books the book was published in (e.g. "The Hardy Boys") 
	")
	private String series;
	//The edition of a book, long form (such as "first" or "second")
	@Description("The edition of a book, long form (such as "first" or "second")")
	private String edition;
	//The volume of a journal or multi-volume book

	@Description("The volume of a journal or multi-volume book 
	")
	private String volume;
	//The "number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number"
	//field.)
	//

	@Description("The "number" of a journal, magazine, or tech-report, if applicable. (Most publications have a "volume", but no "number" field.) 
	
	")
	private String number;
	//Page numbers, separated either by commas or double-hyphens

	@Description("Page numbers, separated either by commas or double-hyphens 
	")
	private String pages;
	//An annotation for annotated bibliography styles (not typical)
	@Description("An annotation for annotated bibliography styles (not typical)")
	private String annote;
	//The name(s) of the editor(s)

	@Description("The name(s) of the editor(s) 
	")
	private String editor;
	//The institution that was involved in the publishing, but not necessarily the publisher

	@Description("The institution that was involved in the publishing, but not necessarily the publisher 
	")
	private String institution;
	//The school where the thesis was written 
	@Description("The school where the thesis was written ")
	private String school;
	//The conference sponsor

	@Description("The conference sponsor 
	")
	private String organization;
	//The publisher's name
	//

	@Description("The publisher's name 
	
	")
	private String publisher;
	//Publisher's address (usually just the city, but can be the full address for lesser-known publishers)
	@Description("Publisher's address (usually just the city, but can be the full address for lesser-known publishers)")
	private String address;
	//How it was published, if the publishing method is nonstandard

	@Description("How it was published, if the publishing method is nonstandard 
	")
	private String howpublished;
	//The type of tech-report, for example, "Research Note"
	//

	@Description("The type of tech-report, for example, "Research Note" 
	
	")
	private String type;
	//The month of publication (or, if unpublished, the month of creation)
	//

	@Description("The month of publication (or, if unpublished, the month of creation) 
	
	")
	private String month;
	//The year of publication (or, if unpublished, the year of creation)

	@Description("The year of publication (or, if unpublished, the year of creation) 
	")
	private String year;
	//A specification of an electronic publication, often a preprint or a technical report

	@Description("A specification of an electronic publication, often a preprint or a technical report 
	")
	private String eprint;
	//Miscellaneous extra information
	//

	@Description("Miscellaneous extra information 
	
	")
	private String note;
	private BibtexReference crossref;

	public BibtexReference getCrossref(){
		return crossref;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCrossref(BibtexReference newVal){
		crossref = newVal;
	}

	public String getAuthor(){
		return author;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAuthor(String newVal){
		author = newVal;
	}

	public String getJournal(){
		return journal;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setJournal(String newVal){
		journal = newVal;
	}

	public String getBooktitle(){
		return booktitle;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setBooktitle(String newVal){
		booktitle = newVal;
	}

	public String getChapter(){
		return chapter;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setChapter(String newVal){
		chapter = newVal;
	}

	public String getTitle(){
		return title;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setTitle(String newVal){
		title = newVal;
	}

	public String getSeries(){
		return series;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSeries(String newVal){
		series = newVal;
	}

	public String getEdition(){
		return edition;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEdition(String newVal){
		edition = newVal;
	}

	public String getVolume(){
		return volume;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setVolume(String newVal){
		volume = newVal;
	}

	public String getNumber(){
		return number;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNumber(String newVal){
		number = newVal;
	}

	public String getPages(){
		return pages;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPages(String newVal){
		pages = newVal;
	}

	public String getAnnote(){
		return annote;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAnnote(String newVal){
		annote = newVal;
	}

	public String getEditor(){
		return editor;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEditor(String newVal){
		editor = newVal;
	}

	public String getInstitution(){
		return institution;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInstitution(String newVal){
		institution = newVal;
	}

	public String getSchool(){
		return school;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSchool(String newVal){
		school = newVal;
	}

	public String getOrganization(){
		return organization;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setOrganization(String newVal){
		organization = newVal;
	}

	public String getPublisher(){
		return publisher;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPublisher(String newVal){
		publisher = newVal;
	}

	public String getAddress(){
		return address;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setAddress(String newVal){
		address = newVal;
	}

	public String getHowpublished(){
		return howpublished;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setHowpublished(String newVal){
		howpublished = newVal;
	}

	public String getType(){
		return type;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setType(String newVal){
		type = newVal;
	}

	public String getMonth(){
		return month;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setMonth(String newVal){
		month = newVal;
	}

	public String getYear(){
		return year;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setYear(String newVal){
		year = newVal;
	}

	public String getEprint(){
		return eprint;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setEprint(String newVal){
		eprint = newVal;
	}

	public String getNote(){
		return note;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setNote(String newVal){
		note = newVal;
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

	@Transient
	public int getYear(){
		return 0;
	}

	@Transient
	public String getYear(){
		return "";
	}

}