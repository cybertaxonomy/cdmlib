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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;

/**
 * The class representing types (categories) of {@link BibtexReference BibTeX references}
 * (like "article" or "book").
 * <P>
 * A standard set of BibTeX entry type instances
 * (see "http://en.wikipedia.org/wiki/BibTeX") will be automatically created
 * as the project starts. But this class allows to extend this standard set
 * by creating new instances of additional BibTeX entry types if needed. 
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:12
 */
@Entity
public class BibtexEntryType extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(BibtexEntryType.class);

	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty BibTeX entry type instance.
	 * 
	 * @see 	#BibtexEntryType(String, String, String)
	 */
	protected BibtexEntryType() {
		super();
	}

	/** 
	 * Class constructor: creates an additional BibTeX entry type instance with
	 * a description (in the {@link common.Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new BibTeX entry type to be created 
	 * @param	label  		 the string identifying the new BibTeX entry
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new BibTeX entry type to be created
	 * @see 				 #BibtexEntryType()
	 */
	protected BibtexEntryType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/

	/** 
	 * Creates a new empty BibTeX entry type instance.
	 * 
	 * @see 	#NewInstance(String, String, String)
	 */
	public static BibtexEntryType NewInstance(){
		return new BibtexEntryType();
	}
	

	/** 
	 * Creates an additional BibTeX entry type instance with
	 * a description (in the {@link common.Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new BibTeX entry type to be created 
	 * @param	label  		 the string identifying the new BibTeX entry
	 * 						 type to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new BibTeX entry type to be created
	 * @see 				 #NewInstance()
	 */
	public static BibtexEntryType NewInstance(String term, String label, String labelAbbrev){
		return new BibtexEntryType(term, label, labelAbbrev);
	}

	/**
	 * Returns the BibTeX entry "article": an article from a journal or
	 * magazine. Required attributes in {@link BibtexReference BibTeX reference} are:
	 * author, title, journal and year. Optional attributes are: volume, number,
	 * pages, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Article Article} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType ARTICLE(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "book" (with an explicit publisher). Required
	 * attributes in {@link BibtexReference BibTeX reference} are: author/editor, title,
	 * publisher and year. Optional attributes are: volume, series, address,
	 * edition, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Book Book} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType BOOK(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "booklet": a work that is printed and bound, but
	 * without a named publisher or sponsoring institution.
	 * Required attribute in {@link BibtexReference BibTeX reference} is: title. Optional
	 * attributes are: author, howpublished, address, month, year and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Book Book} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType BOOKLET(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "inbook": a part of a book, which may be a
	 * chapter (or section or whatever) and/or a range of pages. Required
	 * attributes in {@link BibtexReference BibTeX reference} are: author/editor, title,
	 * chapter/pages, publisher and year. Optional attributes are: volume,
	 * series, address, edition, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link BookSection BookSection} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType INBOOK(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "incollection": a part of a book having its own
	 * title. Required attributes in {@link BibtexReference BibTeX reference} are: author, title,
	 * booktitle and year. Optional attributes are: editor, pages, organization,
	 * publisher, address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link BookSection BookSection} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType INCOLLECTION(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "proceedings" of a conference. Required
	 * attributes in {@link BibtexReference BibTeX reference} are: title and year. Optional
	 * attributes are: editor, publisher, organization, address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Proceedings Proceedings} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType PROCEEDINGS(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "inproceedings": an article in a conference
	 * proceedings. Required attributes in {@link BibtexReference BibTeX reference} are:
	 * author, title, booktitle and year. Optional attributes are: editor,
	 * pages, organization, publisher, address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link InProceedings InProceedings} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see	#CONFERENCE()
	 */
	public static final BibtexEntryType INPROCEEDINGS(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "conference": the same meaning as
	 * "inproceedings". This type was introduced in BibTex format only for
	 * compatibility with previous bibliographical standards.<BR>
	 * This BibTeX entry type corresponds to the {@link InProceedings InProceedings} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 * 
	 * @see	#INPROCEEDINGS()
	 */
	public static final BibtexEntryType CONFERENCE(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "manual" (a technical documentation). Required
	 * attribute in {@link BibtexReference BibTeX reference} is: title. Optional attributes are:
	 * author, organization, address, edition, month, year and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Generic Generic} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType MANUAL(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "masterthesis" (a master's thesis). Required
	 * attributes in {@link BibtexReference BibTeX reference} are: author, title, school and year.
	 * Optional attributes are: address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Thesis Thesis} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType MASTERTHESIS(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "phdthesis" (a Ph.D. thesis). Required
	 * attributes in {@link BibtexReference BibTeX reference} are: author, title, school and year.
	 * Optional attributes are: address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Thesis Thesis} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType PHDTHESIS(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "techreport": a report published by a school or
	 * other institution, usually numbered within a series. Required attributes
	 * in {@link BibtexReference BibTeX reference} are: author, title, institution and year.
	 * Optional attributes are: type, number, address, month and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Report Report} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType TECHREPORT(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "unpublished": a document having an author and
	 * title, but not formally published. Required attributes in
	 * {@link BibtexReference BibTeX reference} are: author, title and note. Optional attributes are:
	 * month and year.<BR>
	 * This BibTeX entry type corresponds to the {@link Generic Generic} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType UNPUBLISHED(){
		return null;
	}

	/**
	 * Returns the BibTeX entry "misc" used when nothing else fits. There are no
	 * required attributes in {@link BibtexReference BibTeX reference}. Optional  attributes are:
	 * author, title, howpublished, month, year and note.<BR>
	 * This BibTeX entry type corresponds to the {@link Generic Generic} subclass of
	 * {@link StrictReferenceBase StrictReferenceBase}.
	 */
	public static final BibtexEntryType MISC(){
		return null;
	}

}