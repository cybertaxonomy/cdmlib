/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import org.apache.log4j.Logger;

/**
 * Nomenclatural status are also included.
 * 
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * 
 * 
 * Subgroups are:
 * ================
 * 
 * Typification:
 * -------------------
 * Type
 * ConservedType
 * RejectedType
 * SameType
 * LectoType
 * NeoType
 * 
 * 
 * Hyprid:
 * -----------------------
 * FirstParent
 * SecondParent
 * FemaleParent
 * MaleParent
 * 
 * 
 * Illegitimate:
 * ------------------------
 * Illegitimate,
 * Superfluous,
 * LaterHomonym,
 * TreatedAsLaterHomonym,
 * RejectedInFavour,
 * Rejected,
 * UtiqueRejected,
 * ConservedProp,
 * OrthographyConservedProp
 * 
 * Legitimate:
 * ------------------------
 * Legitimate,
 * Basionym,
 * ReplacedSynonym,
 * Novum,
 * AlternativeName,
 * Alternativ,
 * ConservedAgainst,
 * Conserved,
 * OrthographyConserved,
 * RejectedProp,
 * UtiqueRejectedProp
 * 
 * Invalid:
 * -----------------------------------
 * Invalid,
 * Nudum,
 * Provisional,
 * CombinationInvalid,
 * ValidatedByName,
 * LaterValidatedByName
 * @author Andreas Mueller
 * @version 1.0
 * @created 15-Aug-2007 18:36:09
 */
public enum NameRelationshipType {
	ORTHOGRAPHIC_VARIANT,
	AMBIGUOUS,
	DOUBTFUL,
	CONFUSUM,
	TYPE,
	CONSERVED_TYPE,
	REJECTED_TYPE,
	SAME_TYPE,
	LECTOTYPE,
	NEOTYPE,
	FIRST_PARENT,
	SECOND_PARENT,
	FEMALE_PARENT,
	MALE_PARENT,
	ILLEGITIMATE,
	LATER_HOMONYM,
	SUPERFLUOUS,
	REJECTED_IN_FAVOR,
	TREATED_AS_LATER_HOMONYM,
	REJECTED,
	UTIQUE_REJECTED,
	CONSERVED_PROP,
	ORTHOGRAPHY_CONSERVED_PROP,
	LEGITIMATE,
	ALTERNATIVE,
	UTIQUE_REJECTED_PROP,
	ORTHOGRAPHY_CONSERVED,
	REJECTED_PROP,
	CONSERVED,
	CONSERVED_AGAINST,
	ALTERNATIVE_NAME,
	BASIONYM,
	NOVUM,
	REPLACED_SYNONYM,
	SANCTIONED,
	INVALID,
	NUDUM,
	COMBINATION_INVALID,
	PROVISIONAL,
	VALIDATED_BY_NAME,
	LATER_VALIDATED_BY_NAME;

	public boolean isHybridType(){
		return false;
	}

	public boolean isInvalidType(){
		return false;
	}

	public boolean isLegitimateType(){
		return false;
	}

	public boolean isTypificationType(){
		return false;
	}

	public boolean isIllegitimateType(){
		return false;
	}
}