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
 * Bibtex bibliography entries are split by types
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:12
 */
@Entity
public class BibtexEntryType extends DefinedTermBase {
	private static final Logger logger = Logger.getLogger(BibtexEntryType.class);


	public static BibtexEntryType NewInstance(){
		return new BibtexEntryType();
	}
	

	public static BibtexEntryType NewInstance(String term, String label, String labelAbbrev){
		return new BibtexEntryType(term, label, labelAbbrev);
	}
	
	protected BibtexEntryType() {
		super();
	}

	protected BibtexEntryType(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/**
	 * An article from a journal or magazine. Required fields: author, title, journal,
	 * year Optional fields: volume, number, pages, month, note
	 */
	public static final BibtexEntryType ARTICLE(){
		return null;
	}

	/**
	 * A book with an explicit publisher. Required fields: author/editor, title,
	 * publisher, year Optional fields: volume, series, address, edition, month, note
	 */
	public static final BibtexEntryType BOOK(){
		return null;
	}

	/**
	 * A work that is printed and bound, but without a named publisher or sponsoring
	 * institution. Required fields: title Optional fields: author, howpublished,
	 * address, month, year, note
	 */
	public static final BibtexEntryType BOOKLET(){
		return null;
	}

	/**
	 * A part of a book, which may be a chapter (or section or whatever) and/or a
	 * range of pages. Required fields: author/editor, title, chapter/pages, publisher,
	 * year Optional fields: volume, series, address, edition, month, note
	 */
	public static final BibtexEntryType INBOOK(){
		return null;
	}

	/**
	 * A part of a book having its own title. Required fields: author, title,
	 * booktitle, year Optional fields: editor, pages, organization, publisher,
	 * address, month, note
	 */
	public static final BibtexEntryType INCOLLECTION(){
		return null;
	}

	/**
	 * The proceedings of a conference. Required fields: title, year Optional fields:
	 * editor, publisher, organization, address, month, note
	 */
	public static final BibtexEntryType PROCEEDINGS(){
		return null;
	}

	/**
	 * An article in a conference proceedings. Required fields: author, title,
	 * booktitle, year Optional fields: editor, pages, organization, publisher,
	 * address, month, note
	 */
	public static final BibtexEntryType INPROCEEDINGS(){
		return null;
	}

	/**
	 * The same as inproceedings. Required fields: author, title, booktitle, year
	 * Optional fields: editor, pages, organization, publisher, address, month, note
	 */
	public static final BibtexEntryType CONFERENCE(){
		return null;
	}

	/**
	 * Technical documentation. Required fields: title Optional fields: author,
	 * organization, address, edition, month, year, note
	 */
	public static final BibtexEntryType MANUAL(){
		return null;
	}

	/**
	 * A Master's thesis. Required fields: author, title, school, year Optional fields:
	 * address, month, note
	 */
	public static final BibtexEntryType MASTERTHESIS(){
		return null;
	}

	/**
	 * A Ph.D. thesis. Required fields: author, title, school, year Optional fields:
	 * address, month, note
	 */
	public static final BibtexEntryType PHDTHESIS(){
		return null;
	}

	/**
	 * A report published by a school or other institution, usually numbered within a
	 * series. Required fields: author, title, institution, year Optional fields: type,
	 * number, address, month, note
	 */
	public static final BibtexEntryType TECHREPORT(){
		return null;
	}

	/**
	 * A document having an author and title, but not formally published. Required
	 * fields: author, title, note Optional fields: month, year
	 */
	public static final BibtexEntryType UNPUBLISHED(){
		return null;
	}

	/**
	 * For use when nothing else fits. Required fields: none Optional fields: author,
	 * title, howpublished, month, year, note
	 */
	public static final BibtexEntryType MISC(){
		return null;
	}

}