/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNoteTypeTerm
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:38
 */
@Entity
public class NameRelationshipType extends RelationshipTermBase {
	static Logger logger = Logger.getLogger(NameRelationshipType.class);

	public NameRelationshipType(String term, String label,
			TermVocabulary enumeration, boolean symmetric, boolean transitive) {
		super(term, label, enumeration, symmetric, transitive);
		// TODO Auto-generated constructor stub
	}


	@Transient
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

	@Transient
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

	@Transient
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