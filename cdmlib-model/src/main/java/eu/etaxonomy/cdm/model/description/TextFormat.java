/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

/**
 * The class representing kinds of formats used for structuring text
 * (like "xml schema namespace", "rdf", or any other format).
 *
 * @author m.doering
 * @created 08-Nov-2007 13:06:59
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextFormat")
@XmlRootElement(name = "TextFormat")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.common.DefinedTermBase")
@Audited
public class TextFormat extends DefinedTermBase<TextFormat> {
	private static final long serialVersionUID = 2063382669537212917L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TextFormat.class);

	protected static Map<UUID, TextFormat> termMap = null;

	/**
	 * Creates a new empty text format instance.
	 *
	 * @see 	#NewInstance(String, String, String, boolean, boolean)
	 */
	public static TextFormat NewInstance(){
		return new TextFormat();
	}
	/**
	 * Creates a new text format instance with a description, a label
	 * and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new text format to be created
	 * @param	label  		 the string identifying the new text format to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new text format to be created
	 * @see 				 #NewInstance()
	 */
	public static TextFormat NewInstance(String term, String label, String labelAbbrev){
		return new TextFormat(term, label, labelAbbrev);
	}

//********************************** Constructor *******************************************************************/

	//for hibernate use only
	@Deprecated
	protected TextFormat() {
		super(TermType.TextFormat);
	}

	/**
	 * Class constructor: creates a new text format instance with a description,
	 * a label and a label abbreviation.
	 *
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new text format to be created
	 * @param	label  		 the string identifying the new text format to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new text format to be created
	 * @see 				 #TextFormat()
	 */
	private TextFormat(String term, String label, String labelAbbrev) {
		super(TermType.TextFormat, term, label, labelAbbrev);
	}

//********* METHODS **************************************/


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.DefinedTermBase#resetTerms()
	 */
	@Override
	public void resetTerms(){
		termMap = null;
	}

	@Override
	protected void setDefaultTerms(TermVocabulary<TextFormat> termVocabulary){
		termMap = new HashMap<UUID, TextFormat>();
		for (TextFormat term : termVocabulary.getTerms()){
			termMap.put(term.getUuid(), term);
		}
	}

}
