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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents terms describing different types of presence
 * (like "native" or "introduced") of a {@link Taxon taxon} in a {@link NamedArea particular area}.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PresenceTerm")
@XmlRootElement(name = "PresenceTerm")
@Entity
public class PresenceTerm extends PresenceAbsenceTermBase<PresenceTerm> {
	private static final Logger logger = Logger.getLogger(PresenceTerm.class);

	// ************* CONSTRUCTORS *************/	
	/** 
	 * Class constructor: creates a new empty presence term.
	 * 
	 * @see #PresenceTerm(String, String, String)
	 */
	protected PresenceTerm() {
		super();
	}

	/** 
	 * Class constructor: creates a new presence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created 
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #PresenceTerm()
	 */
	protected PresenceTerm(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	//********* METHODS **************************************/
	/** 
	 * Creates a new empty presence term.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static PresenceTerm NewInstance(){
		return new PresenceTerm();
	}

	/** 
	 * Creates a new presence term with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence term to be created 
	 * @param	label  		 the string identifying the new presence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence term to be created
	 * @see 				 #NewInstance()
	 */
	public static PresenceTerm NewInstance(String term, String label, String labelAbbrev){
		return new PresenceTerm(term, label, labelAbbrev);
	}
	
}