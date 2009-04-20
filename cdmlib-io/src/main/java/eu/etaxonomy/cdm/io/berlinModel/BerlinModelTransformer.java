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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public final class BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(BerlinModelTransformer.class);
 
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
	
	//TypeDesignation
	public static PresenceAbsenceTermBase<?> occStatus2PresenceAbsence (int occStatusId)  throws UnknownCdmTypeException{
		switch (occStatusId){
			case 0: return null;
			case 110: return PresenceTerm.CULTIVATED_REPORTED_IN_ERROR();
			case 120: return PresenceTerm.CULTIVATED();
			case 210: return PresenceTerm.INTRODUCED_REPORTED_IN_ERROR();
			case 220: return PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
			case 230: return PresenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
			case 240: return PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
			case 250: return PresenceTerm.INTRODUCED();
			case 260: return PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
			case 270: return PresenceTerm.INTRODUCED_ADVENTITIOUS();
			case 280: return PresenceTerm.INTRODUCED_NATURALIZED();
			case 310: return PresenceTerm.NATIVE_REPORTED_IN_ERROR();
			case 320: return PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
			case 330: return PresenceTerm.NATIVE_FORMERLY_NATIVE();
			case 340: return PresenceTerm.NATIVE_DOUBTFULLY_NATIVE();
			case 350: return PresenceTerm.NATIVE();
			case 999: {
					logger.warn("endemic for EM can not be transformed in legal status");
					//TODO preliminary
					return PresenceTerm.PRESENT();
				}
			default: {
				throw new UnknownCdmTypeException("Unknown occurrence status  (id=" + Integer.valueOf(occStatusId).toString() + ")");
			}
		}
	}
	
	
	//TypeDesignation
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
	
	
	
	public static Rank rankId2Rank (ResultSet rs, boolean useUnknown) throws UnknownCdmTypeException{
		Rank result;
		try {
			int rankId = rs.getInt("rankFk");
			
			String abbrev = rs.getString("rankAbbrev");
			String rankName = rs.getString("rank");
			if (logger.isDebugEnabled()){logger.debug(rankId);}
			if (logger.isDebugEnabled()){logger.debug(abbrev);}
			if (logger.isDebugEnabled()){logger.debug(rankName);}
			
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
						case 45: return Rank.SECTION();
						case 47: return Rank.SUBSECTION();
						case 50: return Rank.SERIES();
						case 52: return Rank.SUBSERIES();
						case 58: return Rank.SPECIESAGGREGATE();
						//FIXME
						//case 59: return Rank.SPECIESAGGREGATE();
						case 60: return Rank.SPECIES();
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
							if (useUnknown){
								logger.error("Rank unknown. Created UNKNOWN_RANK");
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
		
	
}
