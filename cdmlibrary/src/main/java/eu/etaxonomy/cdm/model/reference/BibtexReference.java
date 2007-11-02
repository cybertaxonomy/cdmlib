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
 * @created 02-Nov-2007 19:35:57
 */
@Entity
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
	 * @param crossref
	 */
	public void setCrossref(BibtexReference crossref){
		;
	}

	public String getAuthor(){
		return author;
	}

	/**
	 * 
	 * @param author
	 */
	public void setAuthor(String author){
		;
	}

	public String getJournal(){
		return journal;
	}

	/**
	 * 
	 * @param journal
	 */
	public void setJournal(String journal){
		;
	}

	public String getBooktitle(){
		return booktitle;
	}

	/**
	 * 
	 * @param booktitle
	 */
	public void setBooktitle(String booktitle){
		;
	}

	public String getChapter(){
		return chapter;
	}

	/**
	 * 
	 * @param chapter
	 */
	public void setChapter(String chapter){
		;
	}

	public String getTitle(){
		return title;
	}

	/**
	 * 
	 * @param title
	 */
	public void setTitle(String title){
		;
	}

	public String getSeries(){
		return series;
	}

	/**
	 * 
	 * @param series
	 */
	public void setSeries(String series){
		;
	}

	public String getEdition(){
		return edition;
	}

	/**
	 * 
	 * @param edition
	 */
	public void setEdition(String edition){
		;
	}

	public String getVolume(){
		return volume;
	}

	/**
	 * 
	 * @param volume
	 */
	public void setVolume(String volume){
		;
	}

	public String getNumber(){
		return number;
	}

	/**
	 * 
	 * @param number
	 */
	public void setNumber(String number){
		;
	}

	public String getPages(){
		return pages;
	}

	/**
	 * 
	 * @param pages
	 */
	public void setPages(String pages){
		;
	}

	public String getAnnote(){
		return annote;
	}

	/**
	 * 
	 * @param annote
	 */
	public void setAnnote(String annote){
		;
	}

	public String getEditor(){
		return editor;
	}

	/**
	 * 
	 * @param editor
	 */
	public void setEditor(String editor){
		;
	}

	public String getInstitution(){
		return institution;
	}

	/**
	 * 
	 * @param institution
	 */
	public void setInstitution(String institution){
		;
	}

	public String getSchool(){
		return school;
	}

	/**
	 * 
	 * @param school
	 */
	public void setSchool(String school){
		;
	}

	public String getOrganization(){
		return organization;
	}

	/**
	 * 
	 * @param organization
	 */
	public void setOrganization(String organization){
		;
	}

	public String getPublisher(){
		return publisher;
	}

	/**
	 * 
	 * @param publisher
	 */
	public void setPublisher(String publisher){
		;
	}

	public String getAddress(){
		return address;
	}

	/**
	 * 
	 * @param address
	 */
	public void setAddress(String address){
		;
	}

	public String getHowpublished(){
		return howpublished;
	}

	/**
	 * 
	 * @param howpublished
	 */
	public void setHowpublished(String howpublished){
		;
	}

	public String getType(){
		return type;
	}

	/**
	 * 
	 * @param type
	 */
	public void setType(String type){
		;
	}

	public String getMonth(){
		return month;
	}

	/**
	 * 
	 * @param month
	 */
	public void setMonth(String month){
		;
	}

	public String getYear(){
		return year;
	}

	/**
	 * 
	 * @param year
	 */
	public void setYear(String year){
		;
	}

	public String getEprint(){
		return eprint;
	}

	/**
	 * 
	 * @param eprint
	 */
	public void setEprint(String eprint){
		;
	}

	public String getNote(){
		return note;
	}

	/**
	 * 
	 * @param note
	 */
	public void setNote(String note){
		;
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