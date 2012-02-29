/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.reference.ISectionBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public final class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);
	
	//ranks
	public static UUID uuidRankCollSpecies = UUID.fromString("e14630ee-9446-4bb4-a7b7-4c3881bc5d94");
	public static UUID uuidRankProles = UUID.fromString("8810d1ba-6a34-4ae3-a355-919ccd1cd1a5");
	public static UUID uuidRankRace = UUID.fromString("196dee39-cfd8-4460-8bf0-88b83da27f62");
	
	//named areas
	public static UUID euroMedUuid = UUID.fromString("9fe09988-58c0-4c06-8474-f660a0c50014");
	
	public static UUID uuidDesertas = UUID.fromString("36f5e93e-34e8-45b5-a401-f0e0faad21cf");
	public static UUID uuidMadeira = UUID.fromString("086e27ee-78ff-4236-aca9-9850850cd355");
	public static UUID uuidPortoSanto = UUID.fromString("1f9ab6a0-a402-4dfe-8c5b-b1844eb4d8e5");
	public static UUID uuidEasternEuropeanRussia = UUID.fromString("3f013375-0e0a-40c3-8a14-84c0535fab40");
	public static UUID uuidSerbiaMontenegro = UUID.fromString("8926dbe6-863e-47a9-98a0-7dc9ed2c57f7");
	public static UUID uuidLebanonSyria = UUID.fromString("0c45f250-99da-4b19-aa89-c3e56cfdf103");
	public static UUID uuidUssr = UUID.fromString("a512e00a-45f3-4be5-82fa-bba8d675696f");
	public static UUID uuidSicilyMalta = UUID.fromString("424d81ee-d272-4ae8-9600-0a334049cd72");
	public static UUID uuidFlores = UUID.fromString("ef0067c2-8bbb-4e37-8462-97b03f51ba43");
	public static UUID uuidRussiaNorthern = UUID.fromString("c23bc1c9-a775-4426-b883-07d4d7d47eed");
	public static UUID uuidRussiaBaltic = UUID.fromString("579dad44-9439-4b19-8716-ab90d8f27944");
	public static UUID uuidRussiaCentral = UUID.fromString("8bbc8c6a-2ef2-4024-ad51-66fe34c70092");
	public static UUID uuidRussiaSouthWest = UUID.fromString("daa5c207-5567-4690-8742-5e4d153b6a64");
	public static UUID uuidRussiaSouthEast = UUID.fromString("e8516598-b529-489e-9ee8-63bbbd295c1b");
	public static UUID uuidEastAegeanIslands = UUID.fromString("1c429593-c493-46e6-971a-0d70be690da8");
	public static UUID uuidTurkishEastAegeanIslands = UUID.fromString("ba35dba3-ac70-41ae-81c2-2070943f44f2");
	public static UUID uuidBalticStates = UUID.fromString("bf9d64f6-3183-4fa5-8e90-73090e7a2282");
	public static final UUID uuidTurkey = UUID.fromString("d344ee2c-14c8-438d-b03d-11538edb1268");
	public static final UUID uuidCaucasia = UUID.fromString("ebfd3fd1-3859-4e5e-95c7-f66010599d7e");
	
	//language areas
	public static final UUID uuidUkraineAndCrimea = UUID.fromString("99d4d1c2-09f6-416e-86a3-bdde5cae52af");
	public static final UUID uuidAzerbaijanNakhichevan = UUID.fromString("232fbef0-9f4a-4cab-8ac1-e14c717e9de6");
	
	//Marker Types
	public static final UUID uuidMisappliedCommonName = UUID.fromString("25f5cfc3-16ab-4aba-a008-0db0f2cf7f9d");
	
	//Extension Types
	public static final UUID uuidSpeciesExpertName = UUID.fromString("2e8153d2-7412-49e4-87e1-5c38f4c5153a");
	public static final UUID uuidExpertName = UUID.fromString("24becb79-a90c-47d3-be35-efc87bb48fd3");
	
	public static final UUID DETAIL_EXT_UUID = UUID.fromString("c3959b4f-d876-4b7a-a739-9260f4cafd1c");
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");

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
	public static int REF_PRINT_SERIES = 55;
	public static int REF_CONFERENCE_PROCEEDINGS = 56;
	public static int REF_JOURNAL_VOLUME = 57;
	

	
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
	public static int NAME_REL_IS_ALTERNATIVE_NAME_FOR = 17;
	public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
	public static int NAME_REL_IS_LECTOTYPE_OF = 61;
	public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;
	
	//NameFacts
	public static String NAME_FACT_PROTOLOGUE = "Protologue";
	public static String NAME_FACT_ALSO_PUBLISHED_IN = "Also published in";
	public static String NAME_FACT_BIBLIOGRAPHY = "Bibliography";
	
	//TaxonRelationShip
	public static int TAX_REL_IS_INCLUDED_IN = 1;
	public static int TAX_REL_IS_SYNONYM_OF = 2;
	public static int TAX_REL_IS_MISAPPLIED_NAME_OF = 3;
	public static int TAX_REL_IS_PROPARTE_SYN_OF = 4;
	public static int TAX_REL_IS_PARTIAL_SYN_OF = 5;
	public static int TAX_REL_IS_HETEROTYPIC_SYNONYM_OF = 6;
	public static int TAX_REL_IS_HOMOTYPIC_SYNONYM_OF = 7;
	public static int TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF = 101;
	public static int TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF = 102;
	public static int TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF = 103;
	public static int TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF = 104;
	
	

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
	public static int FACT_DISTRIBUTION_EM = 10;
	public static int FACT_DISTRIBUTION_WORLD = 11;
	
	public static UUID uuidNomStatusCombIned = UUID.fromString("dde8a2e7-bf9e-42ec-b186-d5bde9c9c128");
	public static UUID uuidNomStatusSpNovIned = UUID.fromString("1a359ca1-9364-43bc-93e4-834bdcd52b72");
	public static UUID uuidNomStatusNomOrthCons = UUID.fromString("0f838183-ffa0-4014-928e-0e3a27eb3918");
	
	static NomenclaturalStatusType nomStatusCombIned;
	static NomenclaturalStatusType nomStatusSpNovIned;
	static NomenclaturalStatusType nomStatusNomOrthCons;
	
	public static NomenclaturalStatusType nomStatusTypeAbbrev2NewNomStatusType(String nomStatus){
		NomenclaturalStatusType result = null;
		if (nomStatus == null){
			return null;
		}else if (nomStatus.equalsIgnoreCase("comb. ined.")){
			if (nomStatusCombIned == null){
				nomStatusCombIned = new NomenclaturalStatusType();
				Representation representation = Representation.NewInstance("comb. ined.", "comb. ined.", "comb. ined.", Language.LATIN());
				nomStatusCombIned.addRepresentation(representation);
				nomStatusCombIned.setUuid(uuidNomStatusCombIned);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusCombIned);
			}
			result = nomStatusCombIned;
		}else if (nomStatus.equalsIgnoreCase("sp. nov. ined.")){
			if (nomStatusSpNovIned == null){
				nomStatusSpNovIned = new NomenclaturalStatusType();
				Representation representation = Representation.NewInstance("sp. nov. ined.", "sp. nov. ined.", "sp. nov. ined.", Language.LATIN());
				nomStatusSpNovIned.addRepresentation(representation);
				nomStatusSpNovIned.setUuid(uuidNomStatusSpNovIned);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusSpNovIned);
			}
			result = nomStatusSpNovIned;
		}else if (nomStatus.equalsIgnoreCase("nom. & orth. cons.")){
			if (nomStatusNomOrthCons == null){
				nomStatusNomOrthCons = new NomenclaturalStatusType();
				Representation representation = Representation.NewInstance("nom. & orth. cons.", "nom. & orth. cons.", "nom. & orth. cons.", Language.LATIN());
				nomStatusNomOrthCons.addRepresentation(representation);
				nomStatusNomOrthCons.setUuid(uuidNomStatusNomOrthCons);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusNomOrthCons);
			}
			result = nomStatusNomOrthCons;
		}
		return result;
	}

	
	public static NomenclaturalStatus nomStatusFkToNomStatus(int nomStatusFk, String nomStatusLabel)  throws UnknownCdmTypeException{
		if (nomStatusFk == NAME_ST_NOM_INVAL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID());
		}else if (nomStatusFk == NAME_ST_NOM_ILLEG){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
		}else if (nomStatusFk == NAME_ST_NOM_NUD){
			 return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM());
		}else if (nomStatusFk == NAME_ST_NOM_REJ){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED());
		}else if (nomStatusFk == NAME_ST_NOM_REJ_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED());
		}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_CONS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED());
		}else if (nomStatusFk == NAME_ST_NOM_CONS_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED_PROP());
		}else if (nomStatusFk == NAME_ST_ORTH_CONS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED());
		}else if (nomStatusFk == NAME_ST_ORTH_CONS_PROP){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP());
		}else if (nomStatusFk == NAME_ST_NOM_SUPERFL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS());
		}else if (nomStatusFk == NAME_ST_NOM_AMBIG){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS());
		}else if (nomStatusFk == NAME_ST_NOM_PROVIS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.PROVISIONAL());
		}else if (nomStatusFk == NAME_ST_NOM_DUB){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.DOUBTFUL());
		}else if (nomStatusFk == NAME_ST_NOM_NOV){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM());
		}else if (nomStatusFk == NAME_ST_NOM_CONFUS){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONFUSUM());
		}else if (nomStatusFk == NAME_ST_NOM_ALTERN){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE());
		}else if (nomStatusFk == NAME_ST_COMB_INVAL){
			return NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_INVALID());
		}else {
			NomenclaturalStatusType statusType = nomStatusTypeAbbrev2NewNomStatusType(nomStatusLabel);
			NomenclaturalStatus result = NomenclaturalStatus.NewInstance(statusType);
			if (result != null){
				return result;
			}
			throw new UnknownCdmTypeException("Unknown NomenclaturalStatus (id=" + Integer.valueOf(nomStatusFk).toString() + ")");
		}
	}
	
	
	//TypeDesignation
	public static SpecimenTypeDesignationStatus typeStatusId2TypeStatus (int typeStatusId)  throws UnknownCdmTypeException{
		switch (typeStatusId){
			case 0: return null;
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
	
	//TypeDesignation
	public static TaxonRelationshipType taxonRelId2TaxonRelType (int relTaxonTypeId, ResultWrapper<Boolean> isInverse)  throws UnknownCdmTypeException{
		isInverse.setValue(false);
		switch (relTaxonTypeId){
			case 0: return null;
			case 11: return TaxonRelationshipType.CONGRUENT_TO();
			case 12: isInverse.setValue(true); return TaxonRelationshipType.INCLUDES();
			case 13: isInverse.setValue(true); return TaxonRelationshipType.CONGRUENT_OR_INCLUDES();
			case 14: return TaxonRelationshipType.INCLUDES();
			case 15: return TaxonRelationshipType.CONGRUENT_OR_INCLUDES();
			case 16: return TaxonRelationshipType.INCLUDED_OR_INCLUDES();
			case 17: return TaxonRelationshipType.CONGRUENT_OR_INCLUDED_OR_INCLUDES();
			case 18: return TaxonRelationshipType.OVERLAPS();
			case 19: return TaxonRelationshipType.CONGRUENT_OR_OVERLAPS();
			case 20: isInverse.setValue(true); return TaxonRelationshipType.INCLUDES_OR_OVERLAPS();
			case 21: isInverse.setValue(true); return TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_OVERLAPS();
			case 22: return TaxonRelationshipType.INCLUDES_OR_OVERLAPS();
			case 23: return TaxonRelationshipType.CONGRUENT_OR_INCLUDES_OR_OVERLAPS();
			case 24: return TaxonRelationshipType.INCLUDED_OR_INCLUDES_OR_OVERLAPS();
			
			case 26: return TaxonRelationshipType.OVERLAPS();
			//TODO other relationshipTypes
			
			//FIXME doubtful
			case 43: return TaxonRelationshipType.CONGRUENT_TO();
			default: {
				throw new UnknownCdmTypeException("Unknown TypeDesignationStatus (id=" + Integer.valueOf(relTaxonTypeId).toString() + ")");
			}
		}
	}
	
	//TypeDesignation
	public static HybridRelationshipType relNameId2HybridRel (int relNameId)  throws UnknownCdmTypeException{
		switch (relNameId){
			case 0: return null;
			case 9: return HybridRelationshipType.FIRST_PARENT();
			case 10: return HybridRelationshipType.SECOND_PARENT();
			case 11: return HybridRelationshipType.FEMALE_PARENT();
			case 12: return HybridRelationshipType.MALE_PARENT();
			default: {
				throw new UnknownCdmTypeException("Unknown HybridRelationshipType (id=" + Integer.valueOf(relNameId).toString() + ")");
			}
		}
	}
	
	//OccStatus
	public static PresenceAbsenceTermBase<?> occStatus2PresenceAbsence (int occStatusId)  throws UnknownCdmTypeException{
		switch (occStatusId){
			case 0: return null;
			case 110: return AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
			case 120: return PresenceTerm.CULTIVATED();
			case 210: return AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
			case 220: return PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
			case 230: return AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
			case 240: return PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
			case 250: return PresenceTerm.INTRODUCED();
			case 260: return PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
			case 270: return PresenceTerm.INTRODUCED_ADVENTITIOUS();
			case 280: return PresenceTerm.INTRODUCED_NATURALIZED();
			case 310: return AbsenceTerm.NATIVE_REPORTED_IN_ERROR();
			case 320: return PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
			case 330: return AbsenceTerm.NATIVE_FORMERLY_NATIVE();
			case 340: return PresenceTerm.NATIVE_DOUBTFULLY_NATIVE();
			case 350: return PresenceTerm.NATIVE();
			case 999: {
					logger.info("endemic for EM can not be transformed in legal status. Used 'PRESENT' instead");
					//TODO preliminary
					return PresenceTerm.PRESENT();
				}
			default: {
				throw new UnknownCdmTypeException("Unknown occurrence status  (id=" + Integer.valueOf(occStatusId).toString() + ")");
			}
		}
	}
	
	
	//FactCategory
	public static Feature factCategory2Feature (int factCategoryId)  throws UnknownCdmTypeException{
		switch (factCategoryId){
			case 0: return null;
			case 1: return Feature.DESCRIPTION();
			case 4: return Feature.ECOLOGY();
			case 5: return Feature.PHENOLOGY();
			case 12: return Feature.COMMON_NAME();
			case 13: return Feature.OCCURRENCE();
			case 99: return Feature.CITATION();
			default: {
				throw new UnknownCdmTypeException("Unknown FactCategory (id=" + Integer.valueOf(factCategoryId).toString() + ")");
			}
		}
	}
	
	
	static Rank collSpeciesRank;
	/**
	 * @param i
	 * @return
	 */
	private static Rank rankId2NewRank(Integer rankId, boolean switchRank) {
		Rank result = null;
		if (rankId == null){
			return null;
		}else if (rankId == 57){
			
			if (collSpeciesRank == null){
				collSpeciesRank = new Rank();
				Representation representation = Representation.NewInstance("Collective species", "Coll. species", "coll.", Language.ENGLISH());
				collSpeciesRank.addRepresentation(representation);
				collSpeciesRank.setUuid(uuidRankCollSpecies);
				OrderedTermVocabulary<Rank> voc = (OrderedTermVocabulary<Rank>)Rank.SPECIES().getVocabulary();
				voc.addTermBelow(collSpeciesRank, Rank.SPECIESGROUP());
			}
			result = collSpeciesRank;
		}
		return result;
	}

	
	public static Rank rankId2Rank (ResultSet rs, boolean useUnknown, boolean switchSpeciesGroup) throws UnknownCdmTypeException{
		Rank result;
		try {
			int rankId = rs.getInt("rankFk");
			
			String abbrev = rs.getString("rankAbbrev");
			String rankName = rs.getString("rank");
			if (logger.isDebugEnabled()){logger.debug(rankId);}
			if (logger.isDebugEnabled()){logger.debug(abbrev);}
			if (logger.isDebugEnabled()){logger.debug(rankName);}
			
			if (switchSpeciesGroup){
				if (rankId == 59){
					rankId = 57;
				}else if (rankId == 57){
					rankId = 59;
				}
			}
			try {
				result = Rank.getRankByNameOrAbbreviation(abbrev);
			} catch (UnknownCdmTypeException e) {
				try {
					result = Rank.getRankByNameOrAbbreviation(rankName);
				} catch (UnknownCdmTypeException e1) {
					switch (rankId){
						case 0: return null;
						case 1: return Rank.KINGDOM();
						case 3: return Rank.SUBKINGDOM();
						case 5: return Rank.PHYLUM();
						case 7: return Rank.SUBPHYLUM();
						case 8: return Rank.DIVISION();
						case 9: return Rank.SUBDIVISION();
						case 10: return Rank.CLASS();
						case 13: return Rank.SUBCLASS();
						case 16: return Rank.SUPERORDER();
						case 18: return Rank.ORDER();
						case 19: return Rank.SUBORDER();
						case 20: return Rank.FAMILY();
						case 25: return Rank.SUBFAMILY();
						case 30: return Rank.TRIBE();
						case 35: return Rank.SUBTRIBE();
						case 40: return Rank.GENUS();
						case 42: return Rank.SUBGENUS();
						case 45: return Rank.SECTION_BOTANY();
						case 47: return Rank.SUBSECTION_BOTANY();
						case 50: return Rank.SERIES();
						case 52: return Rank.SUBSERIES();
						case 58: return Rank.SPECIESAGGREGATE();
						case 59: return Rank.SPECIESGROUP();
						case 60: return Rank.SPECIES();
						case 61: return Rank.GREX();
						case 65: return Rank.SUBSPECIES();
						case 68: return Rank.CONVAR();
						case 70: return Rank.VARIETY();
						case 73: return Rank.SUBVARIETY();
						case 80: return Rank.FORM();
						case 82: return Rank.SUBFORM();
						case 84: return Rank.SPECIALFORM();
						case 98: return Rank.INFRAGENERICTAXON();
						case 99: return Rank.INFRASPECIFICTAXON();
						
						case 750: return Rank.SUPERCLASS();
						case 780: return Rank.INFRACLASS();
						case 820: return Rank.INFRAORDER();
						
						case 830: return Rank.SUPERFAMILY();
						
						default: {
							Rank rank = rankId2NewRank(57, switchSpeciesGroup);
							if (rank != null){
								return rank;
							}
							if (useUnknown){
								logger.error("Rank unknown: " + rankId + ". Created UNKNOWN_RANK");
								return Rank.UNKNOWN_RANK();
							}
							throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());
						}
					}
				}
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warn("Exception occurred. Created UNKNOWN_RANK instead");
			return Rank.UNKNOWN_RANK();
		}		
	}


	public static Integer rank2RankId (Rank rank){
		if (rank == null){
			return null;
		}
		else if (rank.equals(Rank.KINGDOM())){		return 1;}
		else if (rank.equals(Rank.SUBKINGDOM())){	return 3;}
		else if (rank.equals(Rank.PHYLUM())){		return 5;}
		else if (rank.equals(Rank.SUBPHYLUM())){	return 7;}
		else if (rank.equals(Rank.DIVISION())){		return 8;}
		else if (rank.equals(Rank.SUBDIVISION())){	return 9;}
		
		else if (rank.equals(Rank.CLASS())){		return 10;}
		else if (rank.equals(Rank.SUBCLASS())){		return 13;}
		else if (rank.equals(Rank.SUPERORDER())){	return 16;}
		else if (rank.equals(Rank.ORDER())){		return 18;}
		else if (rank.equals(Rank.SUBORDER())){		return 19;}
		else if (rank.equals(Rank.FAMILY())){		return 20;}
		else if (rank.equals(Rank.SUBFAMILY())){	return 25;}
		else if (rank.equals(Rank.TRIBE())){		return 30;}
		else if (rank.equals(Rank.SUBTRIBE())){		return 35;}
		else if (rank.equals(Rank.GENUS())){		return 40;}
		else if (rank.equals(Rank.SUBGENUS())){		return 42;}
		else if (rank.equals(Rank.SECTION_BOTANY())){		return 45;}
		else if (rank.equals(Rank.SUBSECTION_BOTANY())){	return 47;}
		else if (rank.equals(Rank.SERIES())){		return 50;}
		else if (rank.equals(Rank.SUBSERIES())){	return 52;}
		else if (rank.equals(Rank.SPECIESAGGREGATE())){	return 58;}
		//TODO
		//		else if (rank.equals(Rank.XXX())){	return 59;}
		else if (rank.equals(Rank.SPECIES())){		return 60;}
		else if (rank.equals(Rank.SUBSPECIES())){	return 65;}
		else if (rank.equals(Rank.CONVAR())){		return 68;}
		else if (rank.equals(Rank.VARIETY())){		return 70;}
		else if (rank.equals(Rank.SUBVARIETY())){	return 73;}
		else if (rank.equals(Rank.FORM())){			return 80;}
		else if (rank.equals(Rank.SUBFORM())){		return 82;}
		else if (rank.equals(Rank.SPECIALFORM())){	return 84;}
		else if (rank.equals(Rank.INFRAGENERICTAXON())){	return 98;}
		else if (rank.equals(Rank.INFRASPECIFICTAXON())){	return 99;}
		
		else if (rank.equals(Rank.SUPERCLASS())){	return 750;}
		else if (rank.equals(Rank.INFRACLASS())){	return 780;}
		else if (rank.equals(Rank.INFRAORDER())){	return 820;}
		else if (rank.equals(Rank.SUPERFAMILY())){	return 830;}
		
		else {
			//TODO Exception
			logger.warn("Rank not yet supported in Berlin Model: "+ rank.getLabel());
			return null;
		}
	}
	
	public static Integer textData2FactCategoryFk (Feature feature){
		if (feature == null){return null;}
		if (feature.equals(Feature.DESCRIPTION())){
			return 1;
		}else if (feature.equals(Feature.ECOLOGY())){
			return 4;
		}else if (feature.equals(Feature.PHENOLOGY())){
			return 5;
		}else if (feature.equals(Feature.COMMON_NAME())){
			return 12;
		}else if (feature.equals(Feature.OCCURRENCE())){
			return 13;
		}else if (feature.equals(Feature.CITATION())){
			return 99;
		}else{
			logger.debug("Unknown Feature.");
			return null;
		}
	}
	
	
	public static Integer taxonBase2statusFk (TaxonBase<?> taxonBase){
		if (taxonBase == null){return null;}		
		if (taxonBase.isInstanceOf(Taxon.class)){
			return T_STATUS_ACCEPTED;
		}else if (taxonBase.isInstanceOf(Synonym.class)){
			return T_STATUS_SYNONYM;
		}else{
			logger.warn("Unknown ");
			return T_STATUS_UNRESOLVED;
		}
		//TODO 
//		public static int T_STATUS_PARTIAL_SYN = 3;
//		public static int T_STATUS_PRO_PARTE_SYN = 4;
//		public static int T_STATUS_UNRESOLVED = 5;
//		public static int T_STATUS_ORPHANED = 6;
	}
		
	public static Integer ref2refCategoryId (Reference<?> ref){
		if (ref == null){
			return null;
		}
		else if (ref.getType().equals(ReferenceType.Article)){		return REF_ARTICLE;}
		else if (ref instanceof ISectionBase){	return REF_PART_OF_OTHER_TITLE;}
		else if (ref.getType().equals(ReferenceType.Book)){	return REF_BOOK;}
		else if (ref.getType().equals(ReferenceType.Database)){	return REF_DATABASE;}
//		else if (ref instanceof SectionBas){	return REF_INFORMAL;}
//		else if (ref instanceof SectionBas){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.WebPage)){	return REF_WEBSITE;}
		else if (ref.getType().equals(ReferenceType.CdDvd)){	return REF_CD;}
		else if (ref.getType().equals(ReferenceType.Journal)){	return REF_JOURNAL;}
		else if (ref.getType().equals(ReferenceType.Generic)){	return REF_UNKNOWN;}
		else if (ref.getType().equals(ReferenceType.PrintSeries)){	
			logger.warn("Print Series is not a standard Berlin Model category");
			return REF_PRINT_SERIES;
		}
		else if (ref.getType().equals(ReferenceType.Proceedings)){	
			logger.warn("Proceedings is not a standard Berlin Model category");
			return REF_CONFERENCE_PROCEEDINGS;
		}
//		else if (ref instanceof ){	return REF_JOURNAL_VOLUME;}
		else if (ref.getType().equals(ReferenceType.Patent)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.PersonalCommunication)){	return REF_INFORMAL;}
		else if (ref.getType().equals(ReferenceType.Report)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.Thesis)){	return REF_NOT_APPLICABLE;}
		else if (ref.getType().equals(ReferenceType.Report)){	return REF_NOT_APPLICABLE;}
		
		else {
			//TODO Exception
			logger.warn("Reference type not yet supported in Berlin Model: "+ ref.getClass().getSimpleName());
			return null;
		}
	}
	
	
	public static Integer taxRelation2relPtQualifierFk (RelationshipBase<?,?,?> rel){
		if (rel == null){
			return null;
		}
//		else if (rel instanceof SynonymRelationship){		
//			return ;
//		}else if (rel instanceof TaxonRelationship){
			RelationshipTermBase<?> type = rel.getType();
			if (type.equals(TaxonRelationshipType.TAXONOMICALLY_INCLUDED_IN())) {return TAX_REL_IS_INCLUDED_IN;
			}else if (type.equals(TaxonRelationshipType.MISAPPLIED_NAME_FOR())) {return TAX_REL_IS_MISAPPLIED_NAME_OF;
			}else if (type.equals(SynonymRelationshipType.SYNONYM_OF())) {return TAX_REL_IS_SYNONYM_OF;
			}else if (type.equals(SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF())) {return TAX_REL_IS_HOMOTYPIC_SYNONYM_OF;
			}else if (type.equals(SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF())) {return TAX_REL_IS_HETEROTYPIC_SYNONYM_OF;
			}else if (type.equals(TaxonRelationshipType.CONGRUENT_TO())) {return 11;
//			public static int TAX_REL_IS_PROPARTE_SYN_OF = 4;
//			public static int TAX_REL_IS_PARTIAL_SYN_OF = 5;
//			public static int TAX_REL_IS_PROPARTE_HOMOTYPIC_SYNONYM_OF = 101;
//			public static int TAX_REL_IS_PROPARTE_HETEROTYPIC_SYNONYM_OF = 102;
//			public static int TAX_REL_IS_PARTIAL_HOMOTYPIC_SYNONYM_OF = 103;
//			public static int TAX_REL_IS_PARTIAL_HETEROTYPIC_SYNONYM_OF = 104;
			
			}else {
				//TODO Exception
				logger.warn("Relationship type not yet supported by Berlin Model export: "+ rel.getType());
				return null;
		}
	}
	
	public static Integer nomStatus2nomStatusFk (NomenclaturalStatusType status){
		if (status == null){
			return null;
		}
		if (status.equals(NomenclaturalStatusType.INVALID())) {return NAME_ST_NOM_INVAL;
		}else if (status.equals(NomenclaturalStatusType.ILLEGITIMATE())) {return NAME_ST_NOM_ILLEG;
		}else if (status.equals(NomenclaturalStatusType.NUDUM())) {return NAME_ST_NOM_NUD;
		}else if (status.equals(NomenclaturalStatusType.REJECTED())) {return NAME_ST_NOM_REJ;
		}else if (status.equals(NomenclaturalStatusType.REJECTED_PROP())) {return NAME_ST_NOM_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED())) {return NAME_ST_NOM_UTIQUE_REJ;
		}else if (status.equals(NomenclaturalStatusType.UTIQUE_REJECTED_PROP())) {return NAME_ST_NOM_UTIQUE_REJ_PROP;
		}else if (status.equals(NomenclaturalStatusType.CONSERVED())) {return NAME_ST_NOM_CONS;
		
		}else if (status.equals(NomenclaturalStatusType.CONSERVED_PROP())) {return NAME_ST_NOM_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED())) {return NAME_ST_ORTH_CONS;
		}else if (status.equals(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP())) {return NAME_ST_ORTH_CONS_PROP;
		}else if (status.equals(NomenclaturalStatusType.SUPERFLUOUS())) {return NAME_ST_NOM_SUPERFL;
		}else if (status.equals(NomenclaturalStatusType.AMBIGUOUS())) {return NAME_ST_NOM_AMBIG;
		}else if (status.equals(NomenclaturalStatusType.PROVISIONAL())) {return NAME_ST_NOM_PROVIS;
		}else if (status.equals(NomenclaturalStatusType.DOUBTFUL())) {return NAME_ST_NOM_DUB;
		}else if (status.equals(NomenclaturalStatusType.NOVUM())) {return NAME_ST_NOM_NOV;
		
		}else if (status.equals(NomenclaturalStatusType.CONFUSUM())) {return NAME_ST_NOM_CONFUS;
		}else if (status.equals(NomenclaturalStatusType.ALTERNATIVE())) {return NAME_ST_NOM_ALTERN;
		}else if (status.equals(NomenclaturalStatusType.COMBINATION_INVALID())) {return NAME_ST_COMB_INVAL;
		//TODO
		}else {
			//TODO Exception
			logger.warn("NomStatus type not yet supported by Berlin Model export: "+ status);
			return null;
		}
	}

	
	
	public static Integer nameRel2RelNameQualifierFk (RelationshipBase<?,?,?> rel){
		if (rel == null){
			return null;
		}
		RelationshipTermBase<?> type = rel.getType();
		if (type.equals(NameRelationshipType.BASIONYM())) {return NAME_REL_IS_BASIONYM_FOR;
		}else if (type.equals(NameRelationshipType.LATER_HOMONYM())) {return NAME_REL_IS_LATER_HOMONYM_OF;
		}else if (type.equals(NameRelationshipType.REPLACED_SYNONYM())) {return NAME_REL_IS_REPLACED_SYNONYM_FOR;
		//TODO
		}else if (type.equals(NameRelationshipType.VALIDATED_BY_NAME())) {return NAME_REL_IS_VALIDATION_OF;
		}else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {return NAME_REL_IS_LATER_VALIDATION_OF;
		}else if (type.equals(NameRelationshipType.CONSERVED_AGAINST())) {return NAME_REL_IS_CONSERVED_AGAINST;
		
		
		}else if (type.equals(NameRelationshipType.TREATED_AS_LATER_HOMONYM())) {return NAME_REL_IS_TREATED_AS_LATER_HOMONYM_OF;
		}else if (type.equals(NameRelationshipType.ORTHOGRAPHIC_VARIANT())) {return NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
		}else {
			//TODO Exception
			logger.warn("Relationship type not yet supported by Berlin Model export: "+ rel.getType());
			return null;
	}
			
			//NameRelationShip

//	}else if (type.equals(NameRelationshipType.())) {return NAME_REL_IS_REJECTED_IN_FAVOUR_OF;

//			public static int NAME_REL_IS_FIRST_PARENT_OF = 9;
//			public static int NAME_REL_IS_SECOND_PARENT_OF = 10;
//			public static int NAME_REL_IS_FEMALE_PARENT_OF = 11;
//			public static int NAME_REL_IS_MALE_PARENT_OF = 12;
//
//			public static int NAME_REL_IS_REJECTED_IN_FAVOUR_OF = 14;
//	}else if (type.equals(NameRelationshipType.)) {return NAME_REL_IS_REJECTED_TYPE_OF;
//			
//			public static int NAME_REL_HAS_SAME_TYPE_AS = 18;
//			public static int NAME_REL_IS_LECTOTYPE_OF = 61;
//			public static int NAME_REL_TYPE_NOT_DESIGNATED = 62;

		//	}else if (type.equals(NameRelationshipType.LATER_VALIDATED_BY_NAME())) {return NAME_REL_IS_TYPE_OF;
			
			
	}
	
	public static UUID getWebMarkerUuid (int markerCategoryId){
		if (markerCategoryId == 1){
			return UUID.fromString("d8554418-d1ae-471d-a1bd-a0cbc7ab860c");  //any as not to find in cichorieae
		}else if (markerCategoryId == 2){
			return UUID.fromString("7f189c48-8632-4870-9ec8-e4d2489f324e");
		}else if (markerCategoryId == 3){
			return UUID.fromString("9a115e6b-8210-4dd3-825a-6fed11016c63");
		}else if (markerCategoryId == 4){
			return UUID.fromString("1d287011-2054-41c5-a919-17ac1d0a9270");
		}else if (markerCategoryId == 9){
			return UUID.fromString("cc5eca5c-1ae5-4feb-9a95-507fc167b0c9");
		}else{
			logger.warn("Unknown webMarker category: " + markerCategoryId);
			return null;
		}
		
	}
	
}
