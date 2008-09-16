/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Taxon;

import org.apache.log4j.Logger;
import javax.persistence.*;

/**
 * This (abstract) class represents terms describing the {@link AbsenceTerm absence}
 * (like "extinct") or the {@link PresenceTerm presence} (like "cultivated") of a {@link Taxon taxon}
 * in a {@link NamedArea named area}. Splitting the terms in two subclasses allows to
 * assign them automatically to absent or present status. These terms are only
 * used for {@link Distribution distributions}. 

 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:44
 */
@Entity
public abstract class PresenceAbsenceTermBase<T extends PresenceAbsenceTermBase> extends OrderedTermBase<PresenceAbsenceTermBase> {
	static Logger logger = Logger.getLogger(PresenceAbsenceTermBase.class);

	/** 
	 * Class constructor: creates a new empty presence or absence term.
	 * 
	 * @see #PresenceAbsenceTermBase(String, String, String)
	 */
	protected PresenceAbsenceTermBase() {
		super();
	}

	/** 
	 * Class constructor: creates a new presence or absence term with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new presence or absence term to be created 
	 * @param	label  		 the string identifying the new presence or absence term to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new presence or absence term to be created
	 * @see 				 #PresenceAbsenceTermBase()
	 */
	protected PresenceAbsenceTermBase(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

}