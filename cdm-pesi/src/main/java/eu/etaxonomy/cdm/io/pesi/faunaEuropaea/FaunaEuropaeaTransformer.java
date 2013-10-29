/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.babadshanjan
 * @created 12.05.2009
 * @version 1.0
 */

public final class FaunaEuropaeaTransformer {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaTransformer.class);
	
	// Query
	public static final int Q_NO_RESTRICTION = -1;
	
	// TaxonStatus
	public static final int T_STATUS_ACCEPTED = 1;
	public static final int T_STATUS_NOT_ACCEPTED = 0;
	
	// Author
	public static final int A_AUCT = 1;
	public static final String A_AUCTNAME = "auct.";
	
	// Parenthesis
	public static final int P_PARENTHESIS = 1;
	
	//new AbsencePresenceTermUUIDs
	
	public static UUID noData;
	//public static UUID doubtfull_present;
	
	// Rank
	public static final int R_KINGDOM = 1;
	public static final int R_SUBKINGDOM = 2;
	public static final int R_SUPERPHYLUM = 3;
	public static final int R_PHYLUM = 4;
	public static final int R_SUBPHYLUM = 5;
	public static final int R_INFRAPHYLUM = 6;
	public static final int R_CLASS = 7;
	public static final int R_SUBCLASS = 8;
	public static final int R_INFRACLASS = 9;
	public static final int R_SUPERORDER = 10;
	public static final int R_ORDER = 11;
	public static final int R_SUBORDER = 12;
	public static final int R_INFRAORDER = 13;
	public static final int R_SUPERFAMILY = 14;
	public static final int R_FAMILY = 15;
	public static final int R_SUBFAMILY = 16;
	public static final int R_TRIBE = 17;
	public static final int R_SUBTRIBE = 18;
	public static final int R_GENUS = 19;
	public static final int R_SUBGENUS = 20;
	public static final int R_SPECIES = 21;
	public static final int R_SUBSPECIES = 22;
	
	public static PresenceAbsenceTermBase<?> occStatus2PresenceAbsence(int occStatusId)  throws UnknownCdmTypeException{
	
		if (Integer.valueOf(occStatusId) == null) {
			return PresenceTerm.PRESENT();
		}
		switch (occStatusId){
		case 0: return PresenceTerm.PRESENT();
		case 2: return AbsenceTerm.ABSENT();
		case 1: return PresenceTerm.PRESENT_DOUBTFULLY();

		default: {

			return null;
		

		}
		
	}
	}
	public static void setUUIDs(UUID uuid){
		noData = uuid;
		//doubtfull_present = uuids.get("doubtfullPresent");
	
	}
	
	public static PresenceAbsenceTermBase<?> occStatus2PresenceAbsence_ (int occStatusId)  throws UnknownCdmTypeException{
		switch (occStatusId){
			case 0: return null;
			//case 110: return AbsenceTerm.CULTIVATED_REPORTED_IN_ERROR();
			case 120: return PresenceTerm.CULTIVATED();
		//	case 210: return AbsenceTerm.INTRODUCED_REPORTED_IN_ERROR();
			case 220: return PresenceTerm.INTRODUCED_PRESENCE_QUESTIONABLE();
			case 230: return AbsenceTerm.INTRODUCED_FORMERLY_INTRODUCED();
			case 240: return PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
			case 250: return PresenceTerm.INTRODUCED();
			case 260: return PresenceTerm.INTRODUCED_UNCERTAIN_DEGREE_OF_NATURALISATION();
			case 270: return PresenceTerm.INTRODUCED_ADVENTITIOUS();
			case 280: return PresenceTerm.INTRODUCED_NATURALIZED();
			//case 310: return AbsenceTerm.NATIVE_REPORTED_IN_ERROR();
			case 320: return PresenceTerm.NATIVE_PRESENCE_QUESTIONABLE();
			case 330: return AbsenceTerm.NATIVE_FORMERLY_NATIVE();
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
				logger.debug(rankId + ", " + parentRankId + ", " + rankName + ", " + rankCategory);
			}

			try {
				result = Rank.getRankByNameOrIdInVoc(rankName);
			} catch (UnknownCdmTypeException e1) {

				switch (rankId) {
				case 0: return null;
				case R_KINGDOM: return Rank.KINGDOM();
				case R_SUBKINGDOM: return Rank.SUBKINGDOM();
				case R_SUPERPHYLUM: return Rank.SUPERPHYLUM();
				case R_PHYLUM: return Rank.PHYLUM();
				case R_SUBPHYLUM: return Rank.SUBPHYLUM();
				case R_INFRAPHYLUM: return Rank.INFRAPHYLUM();
				case R_CLASS: return Rank.CLASS();
				case R_SUBCLASS: return Rank.SUBCLASS();
				case R_INFRACLASS: return Rank.INFRACLASS();
				case R_SUPERORDER: return Rank.SUPERORDER();
				case R_ORDER: return Rank.ORDER();
				case R_SUBORDER: return Rank.SUBORDER();
				case R_INFRAORDER: return Rank.INFRAORDER();
				case R_SUPERFAMILY: return Rank.SUPERFAMILY();
				case R_FAMILY: return Rank.FAMILY();
				case R_SUBFAMILY: return Rank.SUBFAMILY();
				case R_TRIBE: return Rank.TRIBE();
				case R_SUBTRIBE: return Rank.SUBTRIBE();
				case R_GENUS: return Rank.GENUS();
				case R_SUBGENUS: return Rank.SUBGENUS();
				case R_SPECIES: return Rank.SPECIES();
				case R_SUBSPECIES: return Rank.SUBSPECIES();

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
	
	
	public static NamedArea areaId2TdwgArea (FaunaEuropaeaDistribution fauEuDistribution) 
	throws UnknownCdmTypeException {
		
		NamedArea tdwgArea = null;
		
		try {
			int areaId = fauEuDistribution.getAreaId();
			String areaName = fauEuDistribution.getAreaName();
			String areaCode = fauEuDistribution.getAreaCode();
			int extraLimital = fauEuDistribution.getExtraLimital();
			
			//TODO: Verify mappings with comments. Those don't map to TDWG areas.
			
			if (areaCode.equals("AD")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SPA-AN");
			//else if (areaCode.equals("AFR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("24"); // Afro-tropical region - Northeast Tropical Africa
			else if (areaCode.equals("AL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ALB-OO");
			else if (areaCode.equals("AT")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("AUT-AU");
			else if (areaCode.equals("AUS")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("AUS"); // Australian region - Australia
			else if (areaCode.equals("BA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-BH"); 
			else if (areaCode.equals("BE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BGM-BE");
			else if (areaCode.equals("BG")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BUL-OO");
			else if (areaCode.equals("BY")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLR-OO");
			else if (areaCode.equals("CH")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SWI-OO");
			else if (areaCode.equals("CY")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("CYP-OO");
			else if (areaCode.equals("CZ")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("CZE-CZ");
			else if (areaCode.equals("DE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("GER-OO");
			else if (areaCode.equals("DK-DEN")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("DEN-OO");
			else if (areaCode.equals("DK-FOR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("FOR-OO");
			else if (areaCode.equals("EE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-ES");
			//else if (areaCode.equals("EPA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("3");   // Palaearctic - Asia-Temperate
			else if (areaCode.equals("ES-BAL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-ES");
			else if (areaCode.equals("ES-CNY")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("CNY-OO");
			else if (areaCode.equals("ES-SPA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SPA-SP");
			else if (areaCode.equals("FI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("FIN-OO");
			else if (areaCode.equals("FR-COR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("COR-OO");
			else if (areaCode.equals("FR-FRA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("FRA-FR");
			else if (areaCode.equals("GB-CI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("FRA-CI");
			else if (areaCode.equals("GB-GI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SPA-GI");
			else if (areaCode.equals("GB-GRB")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("GRB-OO");
			else if (areaCode.equals("GB-NI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("IRE-NI");
			//else if (areaCode.equals("GR-AEG")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("EAI-OO"); // North Aegean Is. - East Aegean Is.
			//else if (areaCode.equals("GR-CYC")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("GRC-OO"); // Cyclades Is. - Greece
			//else if (areaCode.equals("GR-DOD")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("EAI-OO"); // Dodecanese Is. - East Aegean Is.
			else if (areaCode.equals("GR-GRC")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("GRC-OO");
			//else if (areaCode.equals("GR-KRI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("KRI-OO");//only Crete
			else if (areaCode.equals("HR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-CR");
			else if (areaCode.equals("HU")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("HUN-OO");
			else if (areaCode.equals("IE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("IRE-IR");
			else if (areaCode.equals("IS")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ICE-OO");
			else if (areaCode.equals("IT-ITA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ITA-IT");
			else if (areaCode.equals("IT-SAR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SAR-OO");
			else if (areaCode.equals("IT-SI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SIC-SI");
			else if (areaCode.equals("LI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("AUT-LI");
			else if (areaCode.equals("LT")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-LI");
			else if (areaCode.equals("LU")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BGM-LU");
			else if (areaCode.equals("LV")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-LA");
			else if (areaCode.equals("MC")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("FRA-MO");
			else if (areaCode.equals("MD")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("UKR-MO");
			else if (areaCode.equals("MK")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-MA");
			else if (areaCode.equals("MT")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SIC-MA");
			//else if (areaCode.equals("NAF")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("20");   // North Africa - Northern Africa
			//else if (areaCode.equals("NEA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("7");	// Nearctic region - Northern America
			//else if (areaCode.equals("NEO")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("8");    // Neotropical region - Southern America
			else if (areaCode.equals("NL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("NET-OO");
			else if (areaCode.equals("NO-NOR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("NOR-OO");
			else if (areaCode.equals("NO-SVA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SVA-OO");
			//else if (areaCode.equals("NRE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("34");   // Near East - Western Asia
			//else if (areaCode.equals("ORR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("35");	// Oriental region - Arabian Peninsula
			else if (areaCode.equals("PL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("POL-OO");
			else if (areaCode.equals("PT-AZO")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("AZO-OO");
			else if (areaCode.equals("PT-MDR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("MDR-OO");
			else if (areaCode.equals("PT-POR")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("POR-OO");
			else if (areaCode.equals("PT-SEL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SEL-OO");
			else if (areaCode.equals("RO")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ROM-OO");
			//else if (areaCode.equals("RU-FJL")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("WSB-OO"); Franz Josef Land
			else if (areaCode.equals("RU-KGD")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-KA");
			//else if (areaCode.equals("RU-NOZ")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("WSB-OO"); Novaya Zemla
			else if (areaCode.equals("RU-RUC")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("RUC-OO");
			else if (areaCode.equals("RU-RUE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("RUE-OO");
			else if (areaCode.equals("RU-RUN")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("RUN-OO");
			else if (areaCode.equals("RU-RUS")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("RUS-OO");
			else if (areaCode.equals("RU-RUW")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("RUW-OO");
			else if (areaCode.equals("SE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("SWE-OO");
			else if (areaCode.equals("SI")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-SL");
			else if (areaCode.equals("SK")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("CZE-SK");
			else if (areaCode.equals("SM")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ITA-SM");
			else if (areaCode.equals("TR-TUE")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("TUE-OO");
			//else if (areaCode.equals("UA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("UKR-UK"); //UKraine including Crimea
			
			else if (areaCode.equals("VA")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("ITA-VC");
			else if (areaCode.equals("YU")) tdwgArea = TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG");
			else 					
				throw new UnknownCdmTypeException("Unknown Area " + areaCode);

			if (logger.isDebugEnabled()) {
				logger.debug(areaId + ", " + areaName + ", " + areaCode + ", " + extraLimital);
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			logger.debug("Exception occurred. Area could not be mapped." + fauEuDistribution.getAreaName());
			return null;
		}	
		
		return tdwgArea;
	}
	
	public static UUID uuidAreaAUS = UUID.fromString("cf979ca8-8cb6-42df-b2ce-1f432ec7c26b");
	public static UUID uuidAreaAFR = UUID.fromString("07ac5e75-9fc9-4aa0-938c-1324c9618b97");
	public static UUID uuidAreaEPA = UUID.fromString("e83446d7-7379-4beb-be05-295f8da6f5ae");
	public static UUID uuidAreaGR_AEG = UUID.fromString("6bd422aa-9911-4b80-8595-0f6d1ecd5eee");
	public static UUID uuidAreaGR_CYC = UUID.fromString("8e7d7f1e-3e4d-4f7c-96ec-93ec91e528d6");
	public static UUID uuidAreaGR_DOD = UUID.fromString("6d6f6842-924e-440b-9e7e-3df1922bf4a6");
	public static UUID uuidAreaNAF = UUID.fromString("d2c54b1e-5f9f-455d-b308-6859cb153c7b");
	public static UUID uuidAreaNEA = UUID.fromString("aa87f6b8-110b-44b5-a329-91a08f1a4cc9");
	public static UUID uuidAreaNEO = UUID.fromString("0e6e0ce9-c6ab-46bc-80b9-aee4a0620e78");
	public static UUID uuidAreaNRE = UUID.fromString("d51876c2-eaf6-4c7f-963e-337dd3e0d729");
	public static UUID uuidAreaORR = UUID.fromString("04cab4f8-b316-4e21-9bcc-236a45e4e83d");
	public static UUID uuidAreaGR_CR = UUID.fromString("2ef9fa6c-7c3f-431a-96c9-5980c55fc440");
	public static UUID uuidAreaGR_GRC = UUID.fromString("b5d33c78-91e7-467a-9c2d-6c337b78d31a");
	public static UUID uuidAreaUA = UUID.fromString("859cf055-a208-4f30-8e6e-361f165c6fa9");
	public static UUID uuidAreaRU_FJL = UUID.fromString("84671068-2f18-40cb-933a-e7e1c0e37cc8");
	public static UUID uuidAreaRU_NOZ = UUID.fromString("64cce0aa-0222-4740-8fa2-dbf0330e9266");
	
	
	public final static HashMap<String, UUID> abbrToUUID = new HashMap<String,UUID>();
	 	static
	 	{	
	 		abbrToUUID.put("AUS", uuidAreaAUS);
	 		abbrToUUID.put("AFR", uuidAreaAFR);
	 		abbrToUUID.put("EPA", uuidAreaEPA);
	 		abbrToUUID.put("GR-AEG", uuidAreaGR_AEG);
	 		abbrToUUID.put("GR-CYC", uuidAreaGR_CYC);
	 		abbrToUUID.put("GR-DOD", uuidAreaGR_DOD);
	 		abbrToUUID.put("NAF", uuidAreaNAF);
	 		abbrToUUID.put("NEA", uuidAreaNEA);
	 		abbrToUUID.put("NEO", uuidAreaNEO);
	 		abbrToUUID.put("NRE", uuidAreaNRE);
	 		abbrToUUID.put("ORR", uuidAreaORR);
	 		abbrToUUID.put("GR-KRI", uuidAreaGR_CR);
	 		abbrToUUID.put("GR-GRC", uuidAreaGR_GRC);
	 		abbrToUUID.put("UA", uuidAreaUA);
	 		abbrToUUID.put("RU-FJL", uuidAreaRU_FJL);
	 		abbrToUUID.put("RU-NOZ", uuidAreaRU_NOZ);
	 		
	 		
	 	}
	public static UUID getUUIDByAreaAbbr(String abbr){
		return abbrToUUID.get(abbr);
	}
	
	public static UUID uuidNomStatusTempNamed = UUID.fromString("aa6ada5a-ca21-4fef-b76f-9ae237e9c4ae");
	
	static NomenclaturalStatusType nomStatusTempNamed;
	
	public static NomenclaturalStatusType getNomStatusTempNamed(ITermService termService){
		if (nomStatusTempNamed == null){
			nomStatusTempNamed = (NomenclaturalStatusType)termService.find(uuidNomStatusTempNamed);
			if (nomStatusTempNamed == null){
				nomStatusTempNamed = NomenclaturalStatusType.NewInstance("temporary named", "temporary named", "temp. named", Language.ENGLISH());
				Representation repLatin = Representation.NewInstance("", "", "", Language.LATIN());
				nomStatusTempNamed.addRepresentation(repLatin);
				nomStatusTempNamed.setUuid(uuidNomStatusTempNamed);
				NomenclaturalStatusType.ALTERNATIVE().getVocabulary().addTerm(nomStatusTempNamed);
				termService.save(nomStatusTempNamed);
			}
		}
		return nomStatusTempNamed;
	}
	
}
