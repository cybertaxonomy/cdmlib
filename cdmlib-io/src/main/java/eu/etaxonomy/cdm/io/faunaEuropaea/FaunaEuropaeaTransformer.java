/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */

public final class FaunaEuropaeaTransformer {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTransformer.class);
	
	//TaxonStatus
	public static int T_STATUS_ACCEPTED = 1;
	public static int T_STATUS_NOT_ACCEPTED = 0;
	
	private static Map<String, String> tdwgAreas = null;

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
		
	
	public static Rank rankId2Rank (ResultSet rs, boolean useUnknown) throws UnknownCdmTypeException {
		Rank result;
		try {
			int rankId = rs.getInt("rnk_id");
			int parentRankId = rs.getInt("rnk_rnk_id");
			String rankName = rs.getString("rnk_name");
			String rankLatinName = rs.getString("rnk_latinname");
			int rankCategory = rs.getInt("rnk_category");

			if (logger.isDebugEnabled()) {
				logger.debug(rankId + ", " + parentRankId + ", " + rankName + ", " + rankLatinName + ", " + rankCategory);
			}

			try {
				result = Rank.getRankByNameOrAbbreviation(rankName);
			} catch (UnknownCdmTypeException e1) {

				switch (rankId) {
				case 0: return null;
				case 1: return Rank.KINGDOM();
				case 2: return Rank.SUBKINGDOM();
				case 3: return Rank.SUPERPHYLUM();
				case 4: return Rank.PHYLUM();
				case 5: return Rank.SUBPHYLUM();
				case 6: return Rank.INFRAPHYLUM();
				case 7: return Rank.CLASS();
				case 8: return Rank.SUBCLASS();
				case 9: return Rank.INFRACLASS();
				case 10: return Rank.SUPERORDER();
				case 11: return Rank.ORDER();
				case 12: return Rank.SUBORDER();
				case 13: return Rank.INFRAORDER();
				case 14: return Rank.SUPERFAMILY();
				case 15: return Rank.FAMILY();
				case 16: return Rank.SUBFAMILY();
				case 17: return Rank.TRIBE();
				case 18: return Rank.SUBTRIBE();
				case 19: return Rank.GENUS();
				case 20: return Rank.SUBGENUS();
				case 21: return Rank.SPECIES();
				case 22: return Rank.SUBSPECIES();

				default: {
					if (useUnknown){
						logger.error("Rank unknown. Created UNKNOWN_RANK");
						return Rank.UNKNOWN_RANK();
					}
					throw new UnknownCdmTypeException("Unknown Rank id" + Integer.valueOf(rankId).toString());
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
	
	
	public static NamedArea areaId2TdwgArea (ResultSet rs, boolean useUnknown) throws UnknownCdmTypeException {
		
		NamedArea tdwgArea = null;
		
		try {
			int areaId = rs.getInt("ara_id");
			String areaName = rs.getString("ara_name");
			String areaCode = rs.getString("ara_code");
			int extraLimital = rs.getInt("ara_extralimital");
			
			//TODO: Verify mappings with comments. Are those the best matches?
			
			if (areaCode.equals("AD")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SPA-AN");
			else if (areaCode.equals("AFR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("24"); // Afro-tropical region - Northeast Tropical Africa
			else if (areaCode.equals("AL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ALB-OO");
			else if (areaCode.equals("AT")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("AUT-AU");
			else if (areaCode.equals("AUS")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("50"); // Australian region - Australia
			else if (areaCode.equals("BA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("YUG-BH"); 
			else if (areaCode.equals("BE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BGM-BE");
			else if (areaCode.equals("BG")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BUL-OO");
			else if (areaCode.equals("BY")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLR-OO");
			else if (areaCode.equals("CH")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SWI-OO");
			else if (areaCode.equals("CY")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("CYP-OO");
			else if (areaCode.equals("CZ")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("CZE-CZ");
			else if (areaCode.equals("DE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("GER-OO");
			else if (areaCode.equals("DK-DEN")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("DEN-OO");
			else if (areaCode.equals("DK-FOR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("FOR-OO");
			else if (areaCode.equals("EE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLT-ES");
			else if (areaCode.equals("EPA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("Palaearctic");
			else if (areaCode.equals("ES-BAL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLT-ES");
			else if (areaCode.equals("ES-CNY")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("CNY-OO");
			else if (areaCode.equals("ES-SPA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SPA-SP");
			else if (areaCode.equals("FI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("FIN-OO");
			else if (areaCode.equals("FR-COR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("COR-OO");
			else if (areaCode.equals("FR-FRA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("FRA-FR");
			else if (areaCode.equals("GB-CI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("FRA-CI");
			else if (areaCode.equals("GB-GI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SPA-GI");
			else if (areaCode.equals("GB-GRB")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("GRB-OO");
			else if (areaCode.equals("GB-NI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("IRE-NI");
			else if (areaCode.equals("GR-AEG")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("EAI-OO"); // North Aegean Is. - East Aegean Is.
			else if (areaCode.equals("GR-CYC")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("GRC-OO"); // Cyclades Is. - Greece
			else if (areaCode.equals("GR-DOD")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("EAI-OO"); // Dodecanese Is. - East Aegean Is.
			else if (areaCode.equals("GR-GRC")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("GRC-OO");
			else if (areaCode.equals("GR-KRI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("KRI-OO");
			else if (areaCode.equals("HR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("YUG-CR");
			else if (areaCode.equals("HU")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("HUN-OO");
			else if (areaCode.equals("IE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("IRE-IR");
			else if (areaCode.equals("IS")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ICE-OO");
			else if (areaCode.equals("IT-ITA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ITA-IT");
			else if (areaCode.equals("IT-SAR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SAR-OO");
			else if (areaCode.equals("IT-SI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SIC-SI");
			else if (areaCode.equals("LI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("AUT-LI");
			else if (areaCode.equals("LT")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLT-LI");
			else if (areaCode.equals("LU")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BGM-LU");
			else if (areaCode.equals("LV")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLT-LA");
			else if (areaCode.equals("MC")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("FRA-MO");
			else if (areaCode.equals("MD")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("UKR-MO");
			else if (areaCode.equals("MK")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("YUG-MA");
			else if (areaCode.equals("MT")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SIC-MA");
			else if (areaCode.equals("NAF")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("20"); // North Africa - Northern Africa
			else if (areaCode.equals("NEA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("7");	// Nearctic region - Northern America
			else if (areaCode.equals("NEO")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("8"); // Neotropical region - Southern America
else if (areaCode.equals("NL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("NET-OO");
			else if (areaCode.equals("NO-NOR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("NOR-OO");
			else if (areaCode.equals("NO-SVA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SVA-OO");
			else if (areaCode.equals("NRE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("34"); // Near East - Western Asia
			else if (areaCode.equals("ORR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("35");	// Oriental region - Arabian Peninsula
			else if (areaCode.equals("PL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("POL-OO");
			else if (areaCode.equals("PT-AZO")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("AZO-OO");
			else if (areaCode.equals("PT-MDR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("MDR-OO");
			else if (areaCode.equals("PT-POR")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("POR-OO");
			else if (areaCode.equals("PT-SEL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SEL-OO");
			else if (areaCode.equals("RO")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ROM-OO");
			else if (areaCode.equals("RU-FJL")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("WSB-OO");
			else if (areaCode.equals("RU-KGD")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("BLT-KA");
			else if (areaCode.equals("RU-NOZ")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("WSB-OO");
			else if (areaCode.equals("RU-RUC")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("RUC-OO");
			else if (areaCode.equals("RU-RUE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("RUE-OO");
			else if (areaCode.equals("RU-RUN")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("RUN-OO");
			else if (areaCode.equals("RU-RUS")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("RUS-OO");
			else if (areaCode.equals("RU-RUW")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("RUW-OO");
			else if (areaCode.equals("SE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("SWE-OO");
			else if (areaCode.equals("SI")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("YUG-SL");
			else if (areaCode.equals("SK")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("CZE-SL");
			else if (areaCode.equals("SM")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ITA-SM");
			else if (areaCode.equals("TR-TUE")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("TUE-OO");
			else if (areaCode.equals("UA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("UKR-UK");
			else if (areaCode.equals("VA")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("ITA-VC");
			else if (areaCode.equals("YU")) tdwgArea = TdwgArea.getAreaByTdwgAbbreviation("YUG");
			else 					
				throw new UnknownCdmTypeException("Unknown Area " + areaCode);

				
			if (logger.isDebugEnabled()) {
				logger.debug(areaId + ", " + areaName + ", " + areaCode + ", " + extraLimital);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warn("Exception occurred. Area could not be mapped.");
			return null;
		}	
		
		return tdwgArea;
	}
	
}
