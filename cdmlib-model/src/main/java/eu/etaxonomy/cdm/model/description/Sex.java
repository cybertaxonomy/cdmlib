/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.UUID;

import javax.persistence.Entity;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * The class representing the restriction concerning the sex for
 * the applicability of {@link TaxonDescription taxon descriptions}. The sex of a
 * {@link SpecimenOrObservationBase specimen or observation}
 * does not belong to a {@link SpecimenDescription specimen description} but is an attribute of
 * the specimen itself.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:52
 */
@Entity
public class Sex extends Scope {
	private static final Logger logger = Logger.getLogger(Sex.class);

	private static final UUID uuidMale = UUID.fromString("600a5212-cc02-431d-8a80-2bf595bd1eab");
	private static final UUID uuidFemale = UUID.fromString("b4cfe0cb-b35c-4f97-9b6b-2b3c096ea2c0");

	
	public static final Sex getByUuid(UUID uuid){
		return (Sex) findByUuid(uuid);
	}
	
	
	/** 
	 * Class constructor: creates a new empty sex instance.
	 * 
	 * @see #Sex(String, String, String)
	 */
	public Sex() {
		super();
	}

	/** 
	 * Class constructor: creates a new sex instance with a description
	 * (in the {@link Language#DEFAULT() default language}), a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new sex to be created 
	 * @param	label  		 the string identifying the new sex to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new sex to be created
	 * @see 				 #Sex()
	 */
	public Sex(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty sex instance.
	 * 
	 * @see #Sex(String, String, String)
	 */
	public static Sex NewInstance(){
		return new Sex();
	}
	
	public static Sex MALE(){
		return getByUuid(uuidMale);
	}

	public static Sex FEMALE(){
		return getByUuid(uuidFemale);
	}

	
}