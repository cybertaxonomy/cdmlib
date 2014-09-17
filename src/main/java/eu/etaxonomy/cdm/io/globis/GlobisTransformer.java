// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.globis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Country;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class GlobisTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(GlobisTransformer.class);
	

	//marker types
	public static final UUID uuidCheckedMarkerType = UUID.fromString("f2a7926f-def1-49a6-b642-9b81e6b1e35c");
	public static final UUID uuidOldRecordMarkerType = UUID.fromString("8616edc5-00d4-40ca-aca4-d48ec32231e9");
	public static final UUID uuidNotAvailableMarkerType = UUID.fromString("6931e584-6fc2-44ab-9084-e6452f8cd5d1");
	
	//extension types
	public static final UUID uuidExtTypeNotAvailableReason = UUID.fromString("d7dd5632-8c65-4058-b804-d1291560ac4c");
	
	public NamedArea getNamedAreaByKey(String area)  {
		Set<String> unhandledCountries = new HashSet<String>();
		
		if (StringUtils.isBlank(area)){return null;
		}else if (area.equals("Argentina")){return Country.ARGENTINAARGENTINEREPUBLIC();
		}else if (area.equals("Bolivia")){return Country.BOLIVIAREPUBLICOF();
		}else if (area.equals("Ghana")){return Country.GHANAREPUBLICOF();
		}else if (area.equals("Angola")){return Country.ANGOLAREPUBLICOF();
		}else if (area.equals("Tanzania")){return Country.TANZANIAUNITEDREPUBLICOF();
		}else if (area.equals("China")){return Country.CHINAPEOPLESREPUBLICOF();
		}else if (area.equals("Brunei")){return Country.BRUNEIDARUSSALAM();
		}else if (area.equals("Australia")){return Country.AUSTRALIACOMMONWEALTHOF();
		}else if (area.equals("Indonesia")){return Country.INDONESIAREPUBLICOF();
		}else if (area.equals("Philippines")){return Country.PHILIPPINESREPUBLICOFTHE();
		}else if (area.equals("Mongolia")){return Country.MONGOLIAMONGOLIANPEOPLESREPUBLIC();
		}else if (area.equals("Russia")){return Country.RUSSIANFEDERATION();
		}else if (area.equals("France")){return Country.FRANCEFRENCHREPUBLIC();
		}else if (area.equals("Poland")){return Country.POLANDPOLISHPEOPLESREPUBLIC();
		}else if (area.equals("Brazil")){return Country.BRAZILFEDERATIVEREPUBLICOF();
		
		}else if (area.equals("Cuba")){return Country.BRAZILFEDERATIVEREPUBLICOF();
		}else if (area.equals("Guatemala")){return Country.GUATEMALAREPUBLICOF();
		}else if (area.equals("Colombia")){return Country.COLOMBIAREPUBLICOF();
		}else if (area.equals("India")){return Country.INDIAREPUBLICOF();
		
		}else if (area.equals("Mexico")){return Country.MEXICOUNITEDMEXICANSTATES();
		}else if (area.equals("Peru")){return Country.PERUREPUBLICOF();
		}else if (area.equals("Ecuador")){return Country.ECUADORREPUBLICOF();
		}else if (area.equals("Venezuela")){return Country.VENEZUELABOLIVARIANREPUBLICOF();
		}else if (area.equals("Guyana")){return Country.GUYANAREPUBLICOF();
		}else if (area.equals("Panama")){return Country.PANAMAREPUBLICOF();

		}else if (area.equals("Paraguay")){return Country.PARAGUAYREPUBLICOF();
		}else if (area.equals("Suriname")){return Country.SURINAMEREPUBLICOF();
		}else if (area.equals("Costa Rica")){return Country.COSTARICAREPUBLICOF();
		}else if (area.equals("Ivory Coast")){return Country.COTEDIVOIREIVORYCOASTREPUBLICOFTHE();

		}else if (area.equals("Benin")){return Country.BENINPEOPLESREPUBLICOF();
		}else if (area.equalsIgnoreCase("Kenya")){return Country.KENYAREPUBLICOF();
		}else if (area.equals("Uganda")){return Country.UGANDAREPUBLICOF();
		}else if (area.equals("Zambia")){return Country.ZAMBIAREPUBLICOF();
		}else if (area.equals("Rwanda")){return Country.RWANDARWANDESEREPUBLIC();
		}else if (area.equals("South Africa")){return Country.SOUTHAFRICAREPUBLICOF();
		}else if (area.equals("Botswana")){return Country.BOTSWANAREPUBLICOF();
		}else if (area.equals("Burundi")){return Country.BURUNDIREPUBLICOF();
		}else if (area.equals("Cameroon")){return Country.CAMEROONUNITEDREPUBLICOF();
		
		}else if (area.equals("Congo")){return Country.CONGOPEOPLESREPUBLICOF();
		}else if (area.equals("Zaire")){return Country.CONGODEMOCRATICREPUBLICOF();
		}else if (area.equals("Equatorial Guinea")){return Country.EQUATORIALGUINEAREPUBLICOF();
		}else if (area.equals("Gabon")){return Country.GABONGABONESEREPUBLIC();
		}else if (area.equals("Liberia")){return Country.LIBERIAREPUBLICOF();
		
		}else if (area.equals("Togo")){return Country.TOGOTOGOLESEREPUBLIC();
		}else if (area.equals("Guinea")){return Country.GUINEAREVOLUTIONARYPEOPLESREPCOF();
		}else if (area.equals("Guinea-Bissau")){return Country.GUINEABISSAUREPUBLICOF();
		
		}else if (area.equals("Malawi")){return Country.MALAWIREPUBLICOF();
		}else if (area.equals("Mozambique")){return Country.MOZAMBIQUEPEOPLESREPUBLICOF();
		}else if (area.equals("Nigeria")){return Country.NIGERIAFEDERALREPUBLICOF();
		}else if (area.equals("Senegal")){return Country.SENEGALREPUBLICOF();
		}else if (area.equals("Sierra Leone")){return Country.SIERRALEONEREPUBLICOF();
		}else if (area.equals("Sudan")){return Country.SUDANDEMOCRATICREPUBLICOFTHE();
		}else if (area.equals("Madagascar")){return Country.MADAGASCARREPUBLICOF();
		}else if (area.equals("Comoros")){return Country.COMOROSUNIONOFTHE();
		
		}else if (area.equals("Vietnam")){return Country.VIETNAMSOCIALISTREPUBLICOF();
		}else if (area.equals("Thailand")){return Country.THAILANDKINGDOMOF();
		}else if (area.equals("Bhutan")){return Country.BHUTANKINGDOMOF();
		}else if (area.equals("Laos")){return Country.LAOPEOPLESDEMOCRATICREPUBLIC();
		}else if (area.equals("Myanmar (Burma)")){return Country.MYANMAR();
		}else if (area.equals("Nepal")){return Country.NEPALKINGDOMOF();
		}else if (area.equals("Pakistan")){return Country.PAKISTANISLAMICREPUBLICOF();
		}else if (area.equals("Singapore")){return Country.SINGAPOREREPUBLICOF();
		
		}else if (area.equals("Honduras")){return Country.HONDURASREPUBLICOF();
		}else if (area.equals("Nicaragua")){return Country.NICARAGUAREPUBLICOF();
		}else if (area.equals("Trinidad and Tobago")){return Country.TRINIDADANDTOBAGOREPUBLICOF();
		}else if (area.equals("United States")){return Country.UNITEDSTATESOFAMERICA();
		}else if (area.equals("Uruguay")){return Country.URUGUAYEASTERNREPUBLICOF();
		}else if (area.equals("Haiti")){return Country.HAITIREPUBLICOF();
		}else if (area.equals("North Korea")){return Country.KOREADEMOCRATICPEOPLESREPUBLICOF();
		}else if (area.equals("South Korea")){return Country.KOREAREPUBLICOF();
		}else if (area.equals("Taiwan")){return Country.TAIWANPROVINCEOFCHINA();
		
		}else if (area.equals("Somalia")){return Country.SOMALIASOMALIREPUBLIC();
		}else if (area.equals("Albania")){return Country.ALBANIAPEOPLESSOCIALISTREPUBLICOF();
		}else if (area.equals("Algeria")){return Country.ALGERIAPEOPLESDEMOCRATICREPUBLICOF();
		
		}else if (area.equals("Andorra")){return Country.ANDORRAPRINCIPALITYOF();
		}else if (area.equals("Austria")){return Country.AUSTRIAREPUBLICOF();
		}else if (area.equals("Azerbaijan")){return Country.AZERBAIJANREPUBLICOF();
		}else if (area.equals("Bulgaria")){return Country.BULGARIAPEOPLESREPUBLICOF();
		}else if (area.equals("Croatia")){return Country.HRVATSKA();
		}else if (area.equals("Greece")){return Country.GREECEHELLENICREPUBLIC();
		}else if (area.equals("Hungary")){return Country.HUNGARYHUNGARIANPEOPLESREPUBLIC();
		
		}else if (area.equals("Iran")){return Country.IRANISLAMICREPUBLICOF();
		}else if (area.equals("Iraq")){return Country.IRAQREPUBLICOF();
		}else if (area.equals("Israel")){return Country.ISRAELSTATEOF();
		}else if (area.equals("Italy")){return Country.ITALYITALIANREPUBLIC();
		}else if (area.equals("Kazakhstan")){return Country.KAZAKHSTANREPUBLICOF();
		}else if (area.equals("Kyrgyzstan")){return Country.KYRGYZREPUBLIC();
		}else if (area.equals("Lebanon")){return Country.LEBANONLEBANESEREPUBLIC();
		
		}else if (area.equals("Luxembourg")){return Country.LUXEMBOURGGRANDDUCHYOF();
		}else if (area.equals("Macedonia")){return Country.MACEDONIATHEFORMERYUGOSLAVREPUBLICOF();
		}else if (area.equals("Moldova")){return Country.MOLDOVAREPUBLICOF();
		}else if (area.equals("Morocco")){return Country.MOROCCOKINGDOMOF();
		}else if (area.equals("Romania")){return Country.ROMANIASOCIALISTREPUBLICOF();
		}else if (area.equals("Serbia")){return Country.SERBIAANDMONTENEGRO();
		}else if (area.equals("Slovakia")){return Country.SLOVAKIA();
		}else if (area.equals("Spain")){return Country.SPAINSPANISHSTATE();
		
		}else if (area.equals("Switzerland")){return Country.SWITZERLANDSWISSCONFEDERATION();
		}else if (area.equals("Syria")){return Country.SYRIANARABREPUBLIC();
		}else if (area.equals("Turkey")){return Country.TURKEYREPUBLICOF();
		}else if (area.equals("Cambodia")){return Country.CAMBODIAKINGDOMOF();
		}else if (area.equals("Bangladesh")){return Country.BANGLADESHPEOPLESREPUBLICOF();
		
		}else if (area.equals("Sri Lanka")){return Country.SRILANKADEMOCRATICSOCIALISTREPUBLICOF();
		}else if (area.equals("Bahamas")){return Country.BAHAMASCOMMONWEALTHOFTHE();
		}else if (area.equals("Western Samoa")){return Country.SAMOAINDEPENDENTSTATEOF();
		}else if (area.equals("Finland")){return Country.FINLANDREPUBLICOF();
		}else if (area.equals("Norway")){return Country.NORWAYKINGDOMOF();
		}else if (area.equals("El Salvador")){return Country.ELSALVADORREPUBLICOF();
		}else if (area.equals("Cyprus")){return Country.CYPRUSREPUBLICOF();
		}else if (area.equals("Sweden")){return Country.SWEDENKINGDOMOF();
		}else if (area.equals("Denmark")){return Country.DENMARKKINGDOMOF();
		}else if (area.equals("Trinidad & Tobago")){return Country.TRINIDADANDTOBAGOREPUBLICOF();
		}else if (area.equals("Chile")){return Country.CHILEREPUBLICOF();
		}else if (area.equals("Jordan")){return Country.JORDANHASHEMITEKINGDOMOF();
		}else if (area.equals("Montenegro")){
			logger.warn("Montenegro is currently mapped to 'Serbia & Montenegro'");
			return Country.SERBIAANDMONTENEGRO();
		}else if (area.equals("Portugal")){return Country.PORTUGALPORTUGUESEREPUBLIC();
		}else if (area.equals("Tunisia")){return Country.TUNISIAREPUBLICOF();
		}else if (area.equals("Seychelles")){return Country.SEYCHELLESREPUBLICOF();
		}else if (area.equals("Fiji")){return Country.FIJIREPUBLICOFTHEFIJIISLANDS();
		}else if (area.equals("Belgium")){return Country.BELGIUMKINGDOMOF();
		}else if (area.equals("Virgin Islands")){return Country.USVIRGINISLANDS();
		}else if (area.equals("Gambia")){return Country.GAMBIAREPUBLICOFTHE();
		}else if (area.equals("Dominica")){return Country.DOMINICACOMMONWEALTHOF();
		}else if (area.equals("Liechtenstein")){return Country.LIECHTENSTEINPRINCIPALITYOF();
		}else if (area.matches("B(y)?elarus")){return Country.BELARUS();
		}else if (area.equals("Turkey")){return Country.TURKEYREPUBLICOF();
		}else if (area.equals("Turkmenistan")){return Country.TURKMENISTAN();
		
		}else if (area.matches("United States: Alaska")){ return TdwgAreaProvider.getAreaByTdwgAbbreviation("ASK");
		
		
		}else{	
			if (unhandledCountries.contains(area)){
				logger.warn("Unhandled country '" + area + "' replaced by null" );
				return null;
			}
			return null;

		}

	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getLanguageUuid(java.lang.String)
	 */
	@Override
	public UUID getLanguageUuid(String key)
			throws UndefinedTransformerMethodException {
		return super.getLanguageUuid(key);
	}
	
	
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getExtensionTypeByKey(java.lang.String)
	 */
	@Override
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException {
		if (key == null){return null;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getExtensionTypeUuid(java.lang.String)
	 */
	@Override
	public UUID getExtensionTypeUuid(String key)
			throws UndefinedTransformerMethodException {
		if (key == null){return null;
		}else if (key.equalsIgnoreCase("not available reason")){return uuidExtTypeNotAvailableReason;
//		}else if (key.equalsIgnoreCase("recent + fossil")){return uuidRecentAndFossil;
//		}else if (key.equalsIgnoreCase("fossil only")){return uuidFossilOnly;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getExtensionTypeUuid(java.lang.String)
	 */
	@Override
	public UUID getMarkerTypeUuid(String key)
			throws UndefinedTransformerMethodException {
		if (key == null){return null;
		}else if (key.equalsIgnoreCase("old record")){return uuidOldRecordMarkerType;
		}else if (key.equalsIgnoreCase("checked")){return uuidCheckedMarkerType;
		}else if (key.equalsIgnoreCase("not available")){return uuidNotAvailableMarkerType;
		
		}
		return null;
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("Distribution")){return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("Ecology")){return Feature.ECOLOGY();
		}else if (key.equalsIgnoreCase("Diagnosis")){return Feature.DIAGNOSIS();
		}else if (key.equalsIgnoreCase("Biology")){return Feature.BIOLOGY_ECOLOGY();
		}else if (key.equalsIgnoreCase("Host")){return Feature.HOSTPLANT();
		}else{
			return null;
		}
	}

	
	
	
}
