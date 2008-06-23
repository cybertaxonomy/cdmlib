package eu.etaxonomy.cdm.io.tcs;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public final class TcsTransformer {
	private static final Logger logger = Logger.getLogger(TcsTransformer.class);
 
	//REFERENCES
	public static int REF_ARTICLE = 1;
	public static int REF_PART_OF_OTHER_TITLE = 2;
	public static int REF_BOOK = 3;
	public static int REF_DATABASE = 4;
	public static int REF_INFORMAL = 5;
	public static int REF_NOT_APPLICABLE = 6;
	public static int REF_WEBSITE = 7;
	public static int REF_CD = 8;
	public static int REF_JOURNAL = 9;
	public static int REF_UNKNOWN = 10;

	
	//NameStatus
	public static int NAME_ST_NOM_INVAL = 1;
	public static int NAME_ST_NOM_ILLEG = 2;
	public static int NAME_ST_NOM_NUD = 3;
	public static int NAME_ST_NOM_REJ = 4;
	public static int NAME_ST_NOM_REJ_PROP = 5;
	public static int NAME_ST_NOM_UTIQUE_REJ = 6;
	public static int NAME_ST_NOM_UTIQUE_REJ_PROP = 7;
	public static int NAME_ST_NOM_CONS = 8;
	public static int NAME_ST_NOM_CONS_PROP = 9;
	public static int NAME_ST_ORTH_CONS = 10;
	public static int NAME_ST_ORTH_CONS_PROP = 11;
	public static int NAME_ST_NOM_SUPERFL = 12;
	public static int NAME_ST_NOM_AMBIG = 13;
	public static int NAME_ST_NOM_PROVIS = 14;
	public static int NAME_ST_NOM_DUB = 15;
	public static int NAME_ST_NOM_NOV = 16;
	public static int NAME_ST_NOM_CONFUS = 17;
	public static int NAME_ST_NOM_ALTERN = 18;
	public static int NAME_ST_COMB_INVAL = 19;
	
	
	//NameRelationShip
	public static int NAME_REL_IS_BASIONYM_FOR = 1;
	public static int NAME_REL_IS_LATER_HOMONYM_OF = 2;
	public static int NAME_REL_IS_REPLACED_SYNONYM_FOR = 3;
	public static int NAME_REL_IS_VALIDATION_OF = 4;
	public static int NAME_REL_IS_LATER_VALIDATION_OF = 5;
	public static int NAME_REL_IS_TYPE_OF = 6;
	public static int NAME_REL_IS_CONSERVED_TYPE_OF =7;
	public static int NAME_REL_IS_REJECTED_TYPE_OF = 8;
	public static int NAME_REL_IS_FIRST_PARENT_OF = 9;
	public static int NAME_REL_IS_SECOND_PARENT_OF = 10;
	public static int NAME_REL_IS_FEMALE_PARENT_OF = 11;
	public static int NAME_REL_IS_MALE_PARENT_OF = 12;
	public static int NAME_REL_IS_CONSERVED_AGAINST =13;
	public static int NAME_REL_IS_REJECTED_IN_FAVOUR_OF = 14;
	public static int NAME_REL_IS_TREATED_AS_LATER_HOMONYM_OF = 15;
	public static int NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF = 16;
	public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
	public static int NAME_REL_IS_LECTOTYPE_OF = 61;
	public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;
	
	//NameFacts
	public static String NAME_FACT_PROTOLOGUE = "Protologue";
	public static String NAME_FACT_ALSO_PUBLISHED_IN = "Also published in";
	
	//TaxonRelationShip
	public static int TAX_REL_IS_INCLUDED_IN = 1;
	public static int TAX_REL_IS_SYNONYM_OF = 2;
	public static int TAX_REL_IS_MISAPPLIED_NAME_OF = 3;
	public static int TAX_REL_IS_HETEROTYPIC_SYNONYM_OF = 6;
	public static int TAX_REL_IS_HOMOTYPIC_SYNONYM_OF = 7;
	

	//TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_SYNONYM = 2;
	public static int T_STATUS_PARTIAL_SYN = 3;
	public static int T_STATUS_PRO_PARTE_SYN = 4;
	public static int T_STATUS_UNRESOLVED = 5;
	public static int T_STATUS_ORPHANED = 6;
	
	
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
	public static TypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
		switch (typeStatusId){
			case 1: return TypeDesignationStatus.HOLOTYPE();
			case 2: return TypeDesignationStatus.LECTOTYPE();
			case 3: return TypeDesignationStatus.NEOTYPE();
			case 4: return TypeDesignationStatus.EPITYPE();
			case 5: return TypeDesignationStatus.ISOLECTOTYPE();
			case 6: return TypeDesignationStatus.ISONEOTYPE();
			case 7: return TypeDesignationStatus.ISOTYPE();
			case 8: return TypeDesignationStatus.PARANEOTYPE();
			case 9: return TypeDesignationStatus.PARATYPE();
			case 10: return TypeDesignationStatus.SECOND_STEP_LECTOTYPE();
			case 11: return TypeDesignationStatus.SECOND_STEP_NEOTYPE();
			case 12: return TypeDesignationStatus.SYNTYPE();
			case 21: return TypeDesignationStatus.ICONOTYPE();
			case 22: return TypeDesignationStatus.PHOTOTYPE();
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
	public static NomenclaturalCode nomCodeString2NomCode (String nomCode) throws UnknownCdmTypeException{
		
		String tcsRoot = "http://rs.tdwg.org/ontology/voc/TaxonName#";
		String tcsICBN = tcsRoot + "ICBN";
		String tcsICZN = tcsRoot + "ICZN";
		String tcsICNCP = tcsRoot + "ICNCP";
		String tcsBacteriological = tcsRoot + "BACTERIOLOGICAL";
		String tcsViral = tcsRoot + "VIRAL";
		
		if (nomCode == null){ return null;
		}else if (tcsICBN.equals(nomCode)){return NomenclaturalCode.ICBN();
		}else if (tcsICZN.equals(nomCode)){return NomenclaturalCode.ICZN();
		}else if (tcsICNCP.equals(nomCode)){return NomenclaturalCode.ICNCP();
		}else if (tcsBacteriological.equals(nomCode)){return NomenclaturalCode.BACTERIOLOGICAL();
		}else if (tcsViral.equals(nomCode)){return NomenclaturalCode.VIRAL();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Nomenclatural Code " + nomCode);
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
		}else if (isSynonymFor.equals(tcsRelationshipCategory)){return SynonymRelationshipType.SYNONYM_OF(); 
		}else if (hasSynonym.equals(tcsRelationshipCategory)){/*isReverse = true; */ return SynonymRelationshipType.SYNONYM_OF(); 
		
		//Taxon relationships
		}else if (isChildTaxonOf.equals(tcsRelationshipCategory)){return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		}else if (isParentTaxonOf.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN(); 
		
		//concept relationships
		}else if (doesNotOverlap.equals(tcsRelationshipCategory)){return TaxonRelationshipType.DOESNOTOVERLAP(); 
		}else if (excludes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.EXCLUDES(); 
		}else if (includes.equals(tcsRelationshipCategory)){return TaxonRelationshipType.INCLUDES(); 
		}else if (isCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.CONGRUENTTO(); 
		}else if (isNotCongruentTo.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOTCONGRUENTTO(); 
		}else if (isNotIncludedIn.equals(tcsRelationshipCategory)){return TaxonRelationshipType.NOTINCLUDEDIN(); 
		}else if (overlaps.equals(tcsRelationshipCategory)){return TaxonRelationshipType.OVERLAPS(); 
		//reverse concept relationships
		}else if (isIncludedIn.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.INCLUDES();
		}else if (doesNotInclude.equals(tcsRelationshipCategory)){/*isReverse = true; */ return TaxonRelationshipType.NOTINCLUDEDIN(); 
		
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
