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

import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;

import javax.persistence.*;

/**
 * The class representing restrictions for the applicability of
 * {@link TaxonDescription taxon descriptions}. This could include not only {@link Stage life stage}
 * or {@link Sex sex} but also for instance particular morphological parts or seasons.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:50
 */
@Entity
public class Scope extends Modifier {
	static Logger logger = Logger.getLogger(Scope.class);
	
	// ************* CONSTRUCTORS *************/	

	/** 
	 * Class constructor: creates a new empty scope instance.
	 * 
	 * @see #Scope(String, String, String)
	 */
	protected Scope() {
		super();
	}

	/** 
	 * Class constructor: creates a new scope instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new scope to be created 
	 * @param	label  		 the string identifying the new scope to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new scope to be created
	 * @see 				 #Scope()
	 */
	protected Scope(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	
	//********* METHODS **************************************/

	/** 
	 * Creates a new empty scope instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static Scope NewInstance(){
		return new Scope();
	}
	
	/** 
	 * Creates a new scope instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new scope to be created 
	 * @param	label  		 the string identifying the new scope to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new scope to be created
	 * @see 				 #NewInstance()
	 */
	public static Scope NewInstance(String term, String label, String labelAbbrev){
		return new Scope(term, label, labelAbbrev);
	}
}