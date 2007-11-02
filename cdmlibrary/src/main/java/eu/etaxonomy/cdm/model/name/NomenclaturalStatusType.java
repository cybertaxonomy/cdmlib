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

/**
 * The list should be extensible at runtime through configuration. This needs to
 * be investigated.
 * 
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#NomenclaturalNoteTypeTerm
 * 
 * Subgroups are:
 * ================
 * 
 * Illegitimate:
 * ------------------------
 * Illegitimate,
 * Superfluous,
 * Rejected,
 * UtiqueRejected,
 * ConservedProp,
 * OrthographyConservedProp
 * 
 * Legitimate:
 * ------------------------
 * Legitimate,
 * Novum,
 * Alternativ,
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
 * CombinationInvalid
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:27
 */
public class NomenclaturalStatusType extends EnumeratedTermBase {
	static Logger logger = Logger.getLogger(NomenclaturalStatusType.class);

	@Description("")
	private static final int ontologyClassUri = http://rs.tdwg.org/ontology/voc/TaxonName.rdf#PublicationStatus;

	public getOntologyClassUri(){
		return ontologyClassUri;
	}

	/**
	 * 
	 * @param ontologyClassUri
	 */
	public void setOntologyClassUri(ontologyClassUri){
		;
	}

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

	public static final NomenclaturalStatusType AMBIGUOUS(){
		return null;
	}

	public static final NomenclaturalStatusType DOUBTFUL(){
		return null;
	}

	public static final NomenclaturalStatusType CONFUSUM(){
		return null;
	}

	public static final NomenclaturalStatusType ILLEGITIMATE(){
		return null;
	}

	public static final NomenclaturalStatusType SUPERFLUOUS(){
		return null;
	}

	public static final NomenclaturalStatusType REJECTED(){
		return null;
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED(){
		return null;
	}

	public static final NomenclaturalStatusType CONSERVED_PROP(){
		return null;
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED_PROP(){
		return null;
	}

	public static final NomenclaturalStatusType LEGITIMATE(){
		return null;
	}

	public static final NomenclaturalStatusType ALTERNATIVE(){
		return null;
	}

	public static final NomenclaturalStatusType NOVUM(){
		return null;
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED_PROP(){
		return null;
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED(){
		return null;
	}

	public static final NomenclaturalStatusType REJECTED_PROP(){
		return null;
	}

	public static final NomenclaturalStatusType CONSERVED(){
		return null;
	}

	public static final NomenclaturalStatusType SANCTIONED(){
		return null;
	}

	public static final NomenclaturalStatusType INVALID(){
		return null;
	}

	public static final NomenclaturalStatusType NUDUM(){
		return null;
	}

	public static final NomenclaturalStatusType COMBINATION_INVALID(){
		return null;
	}

	public static final NomenclaturalStatusType PROVISIONAL(){
		return null;
	}

}