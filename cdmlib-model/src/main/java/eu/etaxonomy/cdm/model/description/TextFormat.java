/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


import org.apache.log4j.Logger;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The class representing kinds of formats used for structuring text
 * (like "xml schema namespace", "rdf", or any other format).
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextFormat")
@XmlRootElement(name = "TextFormat")
@Entity
public class TextFormat extends DefinedTermBase {
	static Logger logger = Logger.getLogger(TextFormat.class);
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty text format instance.
	 * 
	 * @see 	#TextFormat(String, String, String, boolean, boolean)
	 */
	protected TextFormat() {
		super();
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
	public TextFormat(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
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
}