/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.EnumeratedTermBase;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * 
 * 
 * Subgroups are:
 * ================
 * 
 * 
 * Illegitimate:
 * ------------------------
 * LaterHomonym,
 * TreatedAsLaterHomonym,
 * RejectedInFavour
 * 
 * Legitimate:
 * ------------------------
 * ReplacedSynonym,
 * AlternativeName,
 * ConservedAgainst,
 * OrthographyConserved
 * 
 * Invalid:
 * -----------------------------------
 * ValidatedByName,
 * LaterValidatedByName
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:19
 */
@Entity
public class NameRelationshipType extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	@Description("")
	private static String initializationClassUri = "http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNoteTypeTerm";


	public boolean isInvalidType(){
		//True, if enum is of type:
		//Invalid,
		//Nudum,
		//Provisional,
		//CombinationInvalid,
		//ValidatedByName,
		//LaterValidatedByName

		return false;
	}

	public boolean isLegitimateType(){
		//True, if enum is of type:
		//Legitimate,
		//Basionym,
		//ReplacedSynonym,
		//Novum,
		//AlternativeName,
		//Alternativ,
		//ConservedAgainst,
		//Conserved,
		//OrthographyConserved,
		//RejectedProp,
		//UtiqueRejectedProp

		return false;
	}

	public boolean isIllegitimateType(){
		//True, if enum is of type:
		//Illegitimate,
		//Superfluous,
		//LaterHomonym,
		//TreatedAsLaterHomonym,
		//RejectedInFavour,
		//Rejected,
		//UtiqueRejected,
		//ConservedProp,
		//OrthographyConservedProp

		return false;
	}

	public static final NameRelationshipType ORTHOGRAPHIC_VARIANT(){
		return null;
	}

	public static final NameRelationshipType REJECTED_IN_FAVOUR(){
		return null;
	}

	public static final NameRelationshipType LATER_HOMONYM(){
		return null;
	}

	public static final NameRelationshipType TREATED_AS_LATER_HOMONYM(){
		return null;
	}

	public static final NameRelationshipType ALTERNATIVE_NAME(){
		return null;
	}

	public static final NameRelationshipType BASIONYM(){
		return null;
	}

	public static final NameRelationshipType REPLACED_SYNONYM(){
		return null;
	}

	public static final NameRelationshipType CONSERVED_AGAINST(){
		return null;
	}

	public static final NameRelationshipType VALIDATED_BY_NAME(){
		return null;
	}

	public static final NameRelationshipType LATER_VALIDATED_BY_NAME(){
		return null;
	}

}