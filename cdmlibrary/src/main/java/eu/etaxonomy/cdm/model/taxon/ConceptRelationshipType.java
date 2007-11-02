/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:01
 */
@Entity
public class ConceptRelationshipType extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(ConceptRelationshipType.class);

	@Description("")
	private static final int initializationClassUri = http://rs.tdwg.org/ontology/voc/TaxonConcept#TaxonRelationshipTerm;

	public getInitializationClassUri(){
		return initializationClassUri;
	}

	/**
	 * 
	 * @param initializationClassUri
	 */
	public void setInitializationClassUri(initializationClassUri){
		;
	}

	public static final ConceptRelationshipType TAXONOMICALLY_INCLUDED_IN(){
		return null;
	}

	public static final ConceptRelationshipType MISAPPLIED_NAME_FOR(){
		return null;
	}

	public static final ConceptRelationshipType INVALID_DESIGNATION_FOR(){
		return null;
	}

	public static final ConceptRelationshipType CONTRADICTION(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDED_IN(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_INCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDED_IN_OR_INCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDED_IN_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDES_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDES_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_INCLUDES_OR_OVERLAPS(){
		return null;
	}

	public static final ConceptRelationshipType DOES_NOT_EXCLUDE(){
		return null;
	}

	public static final ConceptRelationshipType EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDED_IN_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDES_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_INCLUDES_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_INCLUDES_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType DOES_NOT_OVERLAP(){
		return null;
	}

	public static final ConceptRelationshipType OVERLAPS_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType CONGRUENT_TO_OR_OVERLAPS_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDED_IN_OR_OVERLAPS_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType DOES_NOT_INCLUDE(){
		return null;
	}

	public static final ConceptRelationshipType INCLUDES_OR_OVERLAPS_OR_EXCLUDES(){
		return null;
	}

	public static final ConceptRelationshipType NOT_INCLUDED_IN(){
		return null;
	}

	public static final ConceptRelationshipType NOT_CONGRUENT_TO(){
		return null;
	}

	public static final ConceptRelationshipType ALL_RELATIONSHIPS(){
		return null;
	}

}