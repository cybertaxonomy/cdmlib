/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import javax.persistence.*;

/**
 * This class represents terms describing different types of absence
 * (like "extinct" or just "absent") of a {@link Taxon taxon} in a {@link NamedArea particular area}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:08
 */
@Entity
public class AbsenceTerm extends PresenceAbsenceTermBase<AbsenceTerm> {
	static Logger logger = Logger.getLogger(AbsenceTerm.class);
	
	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty absence term.
	 * 
	 * @see #AbsenceTerm(String, String, String)
	 */
	public AbsenceTerm() {
		super();
	}

	/** 
	 * Class constructor: creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new absence term to be created 
	 * @param	label  		 the string identifying the new absence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new absence term to be created
	 * @see 				 #AbsenceTerm()
	 */
	public AbsenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
	/** 
	 * Creates a new empty absence term.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static AbsenceTerm NewInstance(){
		return new AbsenceTerm();
	}
	
	/** 
	 * Creates a new absence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new absence term to be created 
	 * @param	label  		 the string identifying the new absence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new absence term to be created
	 * @see 				 #NewInstance()
	 */
	public static AbsenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new AbsenceTerm(term, label, labelAbbrev);
	}
}