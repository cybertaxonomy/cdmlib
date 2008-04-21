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
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

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

	private static final UUID uuidAmbiguous = UUID.fromString("90f5012b-705b-4488-b4c6-002d2bc5198e");
	private static final UUID uuidDoubtful = UUID.fromString("0ffeb39e-872e-4c0f-85ba-a4150d9f9e7d");
	private static final UUID uuidConfusum = UUID.fromString("24955174-aa5c-4e71-a2fd-3efc79e885db");
	private static final UUID uuidIllegitimate = UUID.fromString("b7c544cf-a375-4145-9d3e-4b97f3f18108");
	private static final UUID uuidSuperfluous = UUID.fromString("6890483a-c6ba-4ae1-9ab1-9fbaa5736ce9");
	private static final UUID uuidRejected = UUID.fromString("48107cc8-7a5b-482e-b438-efbba050b851");
	private static final UUID uuidUtiqueRejected = UUID.fromString("04338fdd-c12a-402f-a1ca-68b4bf0be042");
	private static final UUID uuidConservedProp = UUID.fromString("82bab006-5aed-4301-93ec-980deb30cbb1");
	private static final UUID uuidOrthographyConservedProp = UUID.fromString("02f82bc5-1066-454b-a023-11967cba9092");
	private static final UUID uuidLegitimate = UUID.fromString("51a3613c-b53b-4561-b0cd-9163d91c15aa");
	private static final UUID uuidAlternative = UUID.fromString("3b8a8519-420f-4dfa-b050-b410cc257961");
	private static final UUID uuidNovum = UUID.fromString("05fcb68f-af60-4851-b912-892512058897");
	private static final UUID uuidUtiqueRejectedProp = UUID.fromString("643ee07f-026c-426c-b838-c778c8613383");
	private static final UUID uuidOrthographyConserved = UUID.fromString("34a7d383-988b-4117-b8c0-52b947f8c711");
	private static final UUID uuidRejectedProp = UUID.fromString("248e44c2-5436-4526-a352-f7467ecebd56");
	private static final UUID uuidConserved = UUID.fromString("6330f719-e2bc-485f-892b-9f882058a966");
	private static final UUID uuidSanctioned = UUID.fromString("1afe55c4-76aa-46c0-afce-4dc07f512733");
	private static final UUID uuidInvalid = UUID.fromString("b09d4f51-8a77-442a-bbce-e7832aaf46b7");
	private static final UUID uuidNudum = UUID.fromString("e0d733a8-7777-4b27-99a3-05ab50e9f312");
	private static final UUID uuidCombinationInvalid = UUID.fromString("f858e619-7b7f-4225-913b-880a2143ec83");
	private static final UUID uuidProvisional = UUID.fromString("a277507e-ad93-4978-9419-077eb889c951");

	
	
	public NomenclaturalStatusType() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NomenclaturalStatusType(String term, String label) {
		super(term, label);
		// TODO Auto-generated constructor stub
	}

	public static final NomenclaturalStatusType getByUuid(UUID uuid){
		return (NomenclaturalStatusType) findByUuid(uuid);
	}
	

	@Transient
	public boolean isInvalidType(){
		if (this.equals(INVALID()) || 
				this.equals(NUDUM()) || 
				this.equals(PROVISIONAL()) ||
				this.equals(COMBINATION_INVALID())
			){
			return true;	
		}else{
			return false;
		}
	}

	@Transient
	public boolean isLegitimateType(){
		if (this.equals(LEGITIMATE()) || 
				this.equals(NOVUM()) || 
				this.equals(ALTERNATIVE()) ||
				this.equals(CONSERVED()) ||
				this.equals(ORTHOGRAPHY_CONSERVED()) ||
				this.equals(REJECTED_PROP()) ||
				this.equals(UTIQUE_REJECTED_PROP()) 
			){
			return true;	
		}else{
			return false;
		}
	}

	@Transient
	public boolean isIllegitimateType(){
		if (this.equals(ILLEGITIMATE()) || 
				this.equals(SUPERFLUOUS()) || 
				this.equals(REJECTED()) ||
				this.equals(UTIQUE_REJECTED()) ||
				this.equals(CONSERVED_PROP()) ||
				this.equals(ORTHOGRAPHY_CONSERVED_PROP())
			){
			return true;	
		}else{
			return false;
		}
	}
	
	public static final NomenclaturalStatusType AMBIGUOUS(){
		return getByUuid(uuidAmbiguous);
	}

	public static final NomenclaturalStatusType DOUBTFUL(){
		return getByUuid(uuidDoubtful);
	}

	public static final NomenclaturalStatusType CONFUSUM(){
		return getByUuid(uuidConfusum);
	}

	public static final NomenclaturalStatusType ILLEGITIMATE(){
		return getByUuid(uuidIllegitimate);
	}

	public static final NomenclaturalStatusType SUPERFLUOUS(){
		return getByUuid(uuidSuperfluous);
	}

	public static final NomenclaturalStatusType REJECTED(){
		return getByUuid(uuidRejected);
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED(){
		return getByUuid(uuidUtiqueRejected);
	}

	public static final NomenclaturalStatusType CONSERVED_PROP(){
		return getByUuid(uuidConservedProp);
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED_PROP(){
		return getByUuid(uuidOrthographyConservedProp);
	}

	public static final NomenclaturalStatusType LEGITIMATE(){
		return getByUuid(uuidLegitimate);
	}

	public static final NomenclaturalStatusType ALTERNATIVE(){
		return getByUuid(uuidAlternative);
	}

	public static final NomenclaturalStatusType NOVUM(){
		return getByUuid(uuidNovum);
	}

	public static final NomenclaturalStatusType UTIQUE_REJECTED_PROP(){
		return getByUuid(uuidUtiqueRejectedProp);
	}

	public static final NomenclaturalStatusType ORTHOGRAPHY_CONSERVED(){
		return getByUuid(uuidOrthographyConserved);
	}
	
	public static final NomenclaturalStatusType REJECTED_PROP(){
		return getByUuid(uuidRejectedProp);
	}

	public static final NomenclaturalStatusType CONSERVED(){
		return getByUuid(uuidConserved);
	}

	public static final NomenclaturalStatusType SANCTIONED(){
		return getByUuid(uuidSanctioned);
	}

	public static final NomenclaturalStatusType INVALID(){
		return getByUuid(uuidInvalid);
	}

	public static final NomenclaturalStatusType NUDUM(){
		return getByUuid(uuidNudum);
	}

	public static final NomenclaturalStatusType COMBINATION_INVALID(){
		return getByUuid(uuidCombinationInvalid);
	}

	public static final NomenclaturalStatusType PROVISIONAL(){
		return getByUuid(uuidProvisional);
	}
	
	
//TODO Soraya
//	nom. subnud.: nomen subnudum
//	orth. var.: orthographic variant
//	opus utique oppr.: operautique oppressa
//	pro syn.: pro synonymo
	
	/** TODO
	 * preliminary implementation for BotanicalNameParser
	 *  * not yet complete
	 */
	@Transient
	public static NomenclaturalStatusType getNomenclaturalStatusTypeByAbbreviation(String statusAbbreviation) throws UnknownCdmTypeException{
		if (statusAbbreviation == null){ throw new NullPointerException("statusAbbreviation is 'null' in getNomenclaturalStatusTypeByAbbreviation");
		}else if (statusAbbreviation.equalsIgnoreCase("nom. superfl.")){ return NomenclaturalStatusType.SUPERFLUOUS();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. nud.")){ return NomenclaturalStatusType.NUDUM();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. illeg.")){return NomenclaturalStatusType.ILLEGITIMATE();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. inval.")) { return NomenclaturalStatusType.INVALID();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. cons.")) { return NomenclaturalStatusType.CONSERVED();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. alternativ.")) { return NomenclaturalStatusType.ALTERNATIVE();
		//TODO
		//}else if (statusAbbreviation.equalsIgnoreCase("nom. subnud.")) { return NomenclaturalStatusType.su;
		}else if (statusAbbreviation.equalsIgnoreCase("nom. rej.")) { return NomenclaturalStatusType.REJECTED();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. rej. prop.")) { return NomenclaturalStatusType.REJECTED_PROP();
		}else if (statusAbbreviation.equalsIgnoreCase("nom. provis.")) { return NomenclaturalStatusType.PROVISIONAL();
		//TODO
		}else { 
			if (statusAbbreviation == null){
				statusAbbreviation = "(null)";
			}
			throw new eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException("Unknown NomenclaturalStatusType abbreviation: " + statusAbbreviation);
		}
	}

}