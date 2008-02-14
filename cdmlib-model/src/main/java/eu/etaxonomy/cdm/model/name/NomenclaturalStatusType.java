/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;



import eu.etaxonomy.cdm.model.common.OrderedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/TaxonName.rdf#PublicationStatus
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@Entity
public class NomenclaturalStatusType extends OrderedTermBase<NomenclaturalStatusType> {
	static Logger logger = Logger.getLogger(NomenclaturalStatusType.class);

	public NomenclaturalStatusType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NomenclaturalStatusType(String term, String label) {
		super(term, label);
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