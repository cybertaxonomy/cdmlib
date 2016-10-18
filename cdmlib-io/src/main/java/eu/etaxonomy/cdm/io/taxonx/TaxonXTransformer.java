/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.taxonx;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public final class TaxonXTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonXTransformer.class);
 
	//Facts
	public static int FACT_DESCRIPTION = 1;
	public static int FACT_GROWTH_FORM = 2;
	public static int FACT_HARDINESS = 3;
	public static int FACT_ECOLOGY = 4;
	public static int FACT_PHENOLOGY = 5;
	public static int FACT_KARYOLOGY = 6;
	public static int FACT_ILLUSTRATION = 7;
	public static int FACT_IDENTIFICATION = 8;
	public static int FACT_OBSERVATION = 9;
	public static int FACT_DISTIRBUTION_EM = 10;
	public static int FACT_DISTIRBUTION_WORLD = 11;
	
	//TypeDesignation
	public static SpecimenTypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
		switch (typeStatusId){
			case 1: return SpecimenTypeDesignationStatus.HOLOTYPE();
			case 2: return SpecimenTypeDesignationStatus.LECTOTYPE();
			case 3: return SpecimenTypeDesignationStatus.NEOTYPE();
			case 4: return SpecimenTypeDesignationStatus.EPITYPE();
			case 5: return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
			case 6: return SpecimenTypeDesignationStatus.ISONEOTYPE();
			case 7: return SpecimenTypeDesignationStatus.ISOTYPE();
			case 8: return SpecimenTypeDesignationStatus.PARANEOTYPE();
			case 9: return SpecimenTypeDesignationStatus.PARATYPE();
			case 10: return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
			case 11: return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
			case 12: return SpecimenTypeDesignationStatus.SYNTYPE();
			case 21: return SpecimenTypeDesignationStatus.ICONOTYPE();
			case 22: return SpecimenTypeDesignationStatus.PHOTOTYPE();
			default: {
				throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(typeStatusId).toString() + ")");
			}
		}
	}
	
	
	
	
	/** Creates an cdm-Rank by the tcs rank
	 */
	public static Rank rankString2Rank (String strRank) throws UnknownCdmTypeException{
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonRank#";
		String tcsGenus = tcsRoot + "Genus";
		String tcsSpecies = tcsRoot + "Species";
//		String tcsGenus = tcsRoot + "Genus";
		
		if (strRank == null){return null;
		}else if (tcsGenus.equals(strRank)){return Rank.GENUS();
		}else if (tcsSpecies.equals(strRank)){return Rank.SPECIES();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Rank " + strRank);
		}
	}
	
	/** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
	 */
	public static Feature descriptionType2feature (String descriptionType) throws UnknownCdmTypeException{
		if (descriptionType == null){ return null;
//		}else if ("abstract".equals(descriptionType)){return Feature.XX();
//		}else if ("acknowledgments".equals(descriptionType)){return Feature.ICZN();
		}else if ("biology_ecology".equals(descriptionType)){return Feature.BIOLOGY_ECOLOGY();
		}else if ("description".equals(descriptionType)){return Feature.DESCRIPTION();
		}else if ("cultivation".equals(descriptionType)){return Feature.CULTIVATION();
		}else if ("conservation".equals(descriptionType)){return Feature.CONSERVATION();
		}else if ("diagnosis".equals(descriptionType)){return Feature.DIAGNOSIS();
		}else if ("Description".equals(descriptionType)){return Feature.DESCRIPTION();
		}else if ("discussion".equals(descriptionType)){return Feature.DISCUSSION();
		}else if ("distribution".equals(descriptionType)){return Feature.DISTRIBUTION();
		}else if ("etymology".equals(descriptionType)){return Feature.ETYMOLOGY();
		}else if ("key".equals(descriptionType)){return Feature.KEY();
		}else if ("introduction".equals(descriptionType)){return Feature.INTRODUCTION();
		}else if ("materials_examined".equals(descriptionType)){return Feature.MATERIALS_EXAMINED();
		}else if ("materials_methods".equals(descriptionType)){return Feature.MATERIALS_METHODS();
//		}else if ("multiple".equals(descriptionType)){return Feature.multi;
//		}else if ("synopsis".equals(descriptionType)){return Feature.synopsis;
		}else if ("uses".equals(descriptionType)){return Feature.USES();
		}else if ("vernacular".equals(descriptionType)){return Feature.COMMON_NAME();
		}else if ("anatomy".equals(descriptionType)){return Feature.ANATOMY();
		}else {
			throw new UnknownCdmTypeException("Unknown Description Type: " + descriptionType);
		}
	}
	
	public static boolean isReverseRelationshipCategory (String tcsRelationshipCategory){
		String str = tcsRelationshipCategory.replace("http://rs.tdwg.org/ontology/voc/TaxonConcept#", "");
		if ("HasSynonym".equalsIgnoreCase(str) 
				|| "IsParentTaxonOf".equalsIgnoreCase(str) 
				|| "IsIncludedIn".equalsIgnoreCase(str) 
				|| "DoesNotInclude".equalsIgnoreCase(str) 
									){
			
			return true;
		}
		return false;
	}
	
	/** Creates an cdm-RelationshipTermBase by the tcsRelationshipCategory
	 */
	public static RelationshipTermBase tcsRelationshipCategory2Relationship (String tcsRelationshipCategory) throws UnknownCdmTypeException{
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
		String doesNotInclude  = tcsRoot + "DoesNotInclude";
		String doesNotOverlap  = tcsRoot + "DoesNotOverlap";
		String excludes  = tcsRoot + "Excludes";
		String hasSynonym  = tcsRoot + "HasSynonym";
		String hasVernacular  = tcsRoot + "HasVernacular";
		String includes  = tcsRoot + "Includes";
		String isAmbiregnalOf  = tcsRoot + "IsAmbiregnalOf";
		String isAnamorphOf  = tcsRoot + "IsAnamorphOf";
		String isChildTaxonOf  = tcsRoot + "IsChildTaxonOf";
		String isCongruentTo  = tcsRoot + "IsCongruentTo";
		String isFemaleParentOf  = tcsRoot + "IsFemaleParentOf";
		String isFirstParentOf  = tcsRoot + "IsFirstParentOf";
		String isHybridChildOf  = tcsRoot + "IsHybridChildOf";
		String isHybridParentOf  = tcsRoot + "IsHybridParentOf";
		String isIncludedIn  = tcsRoot + "IsIncludedIn";
		String isMaleParentOf  = tcsRoot + "IsMaleParentOf";
		String isNotCongruentTo  = tcsRoot + "IsNotCongruentTo";
		String isNotIncludedIn  = tcsRoot + "IsNotIncludedIn";
		String isParentTaxonOf  = tcsRoot + "IsParentTaxonOf";
		String isSecondParentOf  = tcsRoot + "IsSecondParentOf";
		String isSynonymFor  = tcsRoot + "IsSynonymFor";
		String isTeleomorphOf  = tcsRoot + "IsTeleomorphOf";
		String isVernacularFor  = tcsRoot + "IsVernacularFor";
		String overlaps  = tcsRoot + "Overlaps";

		if (tcsRelationshipCategory == null){ return null;
		
		//Synonym relationships
		}else if (isSynonymFor.equals(tcsRelationshipCategory)){return SynonymType.SYNONYM_OF(); 
		}else if (hasSynonym.equals(tcsRelationshipCategory)){/*isReverse = true; */ return SynonymType.SYNONYM_OF(); 
		
		//Taxon relationships
		}else if (isChildTaxonOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		}else if (isParentTaxonOf.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		
		//concept relationships
		}else if (doesNotOverlap.equals(tcsRelationshipCategory)){return TaxonRelationshipType.DOES_NOT_OVERLAP(); 
		}else if (excludes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.EXCLUDES(); 
		}else if (includes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.INCLUDES(); 
		}else if (isCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.CONGRUENT_TO(); 
		}else if (isNotCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOT_CONGRUENT_TO(); 
		}else if (isNotIncludedIn.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		}else if (overlaps.equals(tcsRelationshipCategory)){return TaxonRelationshipType.OVERLAPS(); 
		//reverse concept relationships
		}else if (isIncludedIn.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.INCLUDES();
		}else if (doesNotInclude.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.NOT_INCLUDED_IN(); 
		
	//TODO	
//		}else if (hasVernacular.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isAmbiregnalOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isAnamorphOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isFemaleParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isFirstParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isHybridChildOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isHybridParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isMaleParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isSecondParentOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isTeleomorphOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
//		}else if (isVernacularFor.equals(tcsRelationshipCategory)){return TaxonRelationshipType.X; 
		
		}else {
			throw new UnknownCdmTypeException("Unknown RelationshipCategory " + tcsRelationshipCategory);
		}
	}
	
	
	/** Creates an cdm-NomenclaturalCode by the tcs NomenclaturalCode
	 */
	public static NomenclaturalStatusType nomStatusString2NomStatus (String nomStatus) throws UnknownCdmTypeException{
	
		if (nomStatus == null){ return null;
		}else if ("Valid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.VALID();
		
		}else if ("Alternative".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();
		}else if ("nom. altern.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ALTERNATIVE();
		
		}else if ("Ambiguous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.AMBIGUOUS();
		
		}else if ("Doubtful".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.DOUBTFUL();
		
		}else if ("Confusum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONFUSUM();
		
		}else if ("Illegitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();
		}else if ("nom. illeg.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ILLEGITIMATE();
		
		}else if ("Superfluous".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();
		}else if ("nom. superfl.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SUPERFLUOUS();
		
		}else if ("Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();
		}else if ("nom. rej.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED();
		
		}else if ("Utique Rejected".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED();
		
		}else if ("Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED_PROP();
		
		}else if ("Orthography Conserved Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP();
		
		}else if ("Legitimate".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.LEGITIMATE();
		
		}else if ("Novum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();
		}else if ("nom. nov.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NOVUM();
		
		}else if ("Utique Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.UTIQUE_REJECTED_PROP();
		
		}else if ("Orthography Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED();
		
		}else if ("Rejected Prop".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.REJECTED_PROP();
		
		}else if ("Conserved".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();
		}else if ("nom. cons.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.CONSERVED();
		
		}else if ("Sanctioned".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.SANCTIONED();
		
		}else if ("Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();
		}else if ("nom. inval.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.INVALID();
		
		}else if ("Nudum".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();
		}else if ("nom. nud.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.NUDUM();
		
		}else if ("Combination Invalid".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.COMBINATION_INVALID();
		
		}else if ("Provisional".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
		}else if ("nom. provis.".equalsIgnoreCase(nomStatus)){return NomenclaturalStatusType.PROVISIONAL();
		}
		else {
			throw new UnknownCdmTypeException("Unknown Nomenclatural status type " + nomStatus);
		}
	}
	
}
