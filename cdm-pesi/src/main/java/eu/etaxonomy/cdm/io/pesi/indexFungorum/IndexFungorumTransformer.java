// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.indexFungorum;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class IndexFungorumTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(IndexFungorumTransformer.class);
	
	public static final String LSID_PREFIX = "urn:lsid:indexfungorum.org:names:";

	public static NomenclaturalCode kingdomId2NomCode(Integer kingdomId){
		switch (kingdomId){
			case 1: return null;
			case 2: return NomenclaturalCode.ICZN;  //Animalia
			case 3: return NomenclaturalCode.ICNAFP;  //Plantae
			case 4: return NomenclaturalCode.ICNAFP;  //Fungi
			case 5: return NomenclaturalCode.ICZN ;  //Protozoa
			case 6: return NomenclaturalCode.ICNB ;  //Bacteria
			case 7: return NomenclaturalCode.ICNAFP;  //Chromista
			case 147415: return NomenclaturalCode.ICNB;  //Monera
			default: return null;
	
		}
	}



	@Override
	public Rank getRankByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){
			return null;
		}
		Integer rankFk = Integer.valueOf(key);
		switch (rankFk){
			case 30: return Rank.DIVISION();
			case 40: return Rank.SUBDIVISION();
			case 60: return Rank.CLASS();
			case 70: return Rank.SUBCLASS();
			case 100: return Rank.ORDER();
			case 110: return Rank.SUBORDER();  //not needed
			case 140: return Rank.FAMILY();
			default:
				logger.warn("Unhandled rank: " + rankFk);
				return null;
		}
		
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNamedAreaByKey(java.lang.String)
	 */
	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){
			return null;
		}else if (key.equalsIgnoreCase("AT")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("AUT-AU");
		}else if (key.equalsIgnoreCase("LU")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BGM-LU");
		}else if (key.equalsIgnoreCase("BA")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-BH");
		}else if (key.equalsIgnoreCase("BG")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BUL-OO");
		}else if (key.equalsIgnoreCase("EE")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-ES");
		}else if (key.equalsIgnoreCase("FR")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("FRA");
		}else if (key.equalsIgnoreCase("DE")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("GER-OO");
		}else if (key.equalsIgnoreCase("IE")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("IRE-IR");
		}else if (key.equalsIgnoreCase("CH")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SWI-OO");
		}else if (key.equalsIgnoreCase("NL")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("NET-OO");
		}else if (key.equalsIgnoreCase("HU")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("HUN-OO");
		}else if (key.equalsIgnoreCase("IT")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("ITA");
		}else if (key.equalsIgnoreCase("LV")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-LA");
		}else if (key.equalsIgnoreCase("LT")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BLT-LI");
		}else if (key.equalsIgnoreCase("NO")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("NOR-OO");
		}else if (key.equalsIgnoreCase("PL")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("POL-OO");
		}else if (key.equalsIgnoreCase("RO")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("ROM-OO");
		}else if (key.equalsIgnoreCase("MT")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SIC-MA");
		}else if (key.equalsIgnoreCase("SK")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("CZE-SK");
		}else if (key.equalsIgnoreCase("RS")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("YUG-SE");
		}else if (key.equalsIgnoreCase("SE")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SWE-OO");
		}else if (key.equalsIgnoreCase("AM")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("TCS-AR");
		}else if (key.equalsIgnoreCase("IL")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("PAL-IS");
		
		}else if (key.equalsIgnoreCase("UK")){return Country.UNITEDKINGDOMOFGREATBRITAINANDNORTHERNIRELAND();
		}else if (key.equalsIgnoreCase("DK")){return Country.DENMARKKINGDOMOF();
		}else if (key.equalsIgnoreCase("GR")){return Country.GREECEHELLENICREPUBLIC();
		}else if (key.equalsIgnoreCase("ES")){return Country.SPAINSPANISHSTATE();
		}else if (key.equalsIgnoreCase("PT")){return Country.PORTUGALPORTUGUESEREPUBLIC();
		}else if (key.equalsIgnoreCase("RU")){return Country.RUSSIANFEDERATION();
		}else if (key.equalsIgnoreCase("UA")){return Country.UKRAINE();
		}else if (key.equalsIgnoreCase("GE")){return Country.GEORGIA();
		}else if (key.equalsIgnoreCase("TR")){return Country.TURKEYREPUBLICOF();
		
		
		} else {
			logger.warn("Area not yet mapped: " +  key);
			return null;
		}
	}
}
