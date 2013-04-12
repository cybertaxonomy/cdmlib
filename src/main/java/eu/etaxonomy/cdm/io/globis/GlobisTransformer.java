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

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class GlobisTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(GlobisTransformer.class);
	

	//extension types
//	public static final UUID uuidEditor = UUID.fromString("07752659-3018-4880-bf26-41bb396fbf37");
	
	
	//language uuids
	
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNameTypeDesignationStatusByKey(java.lang.String)
	 */
	@Override
	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		if (key == null){
			return null;
		}
		Integer intDesignationId = Integer.valueOf(key);
		switch (intDesignationId){
			case 1: return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
			case 2: return NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION();
			case 3: return NameTypeDesignationStatus.MONOTYPY();
			default: 
				String warning = "Unknown name type designation status id " + key;
				logger.warn(warning);
				return null;
		}
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNameTypeDesignationStatusUuid(java.lang.String)
	 */
	@Override
	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		//nott needed
		return super.getNameTypeDesignationStatusUuid(key);
	}


	public NamedArea getNamedAreaByKey(String area)  {
		Set<String> unhandledCountries = new HashSet<String>();
		
		if (StringUtils.isBlank(area)){return null;
		}else if (area.equals("Argentina")){return WaterbodyOrCountry.ARGENTINAARGENTINEREPUBLIC();
		}else if (area.equals("Bolivia")){return WaterbodyOrCountry.BOLIVIAREPUBLICOF();
		}else if (area.equals("Ghana")){return WaterbodyOrCountry.GHANAREPUBLICOF();
		}else if (area.equals("Angola")){return WaterbodyOrCountry.ANGOLAREPUBLICOF();
		}else if (area.equals("Tanzania")){return WaterbodyOrCountry.TANZANIAUNITEDREPUBLICOF();
		}else if (area.equals("China")){return WaterbodyOrCountry.CHINAPEOPLESREPUBLICOF();
		}else if (area.equals("Brunei")){return WaterbodyOrCountry.BRUNEIDARUSSALAM();
		}else if (area.equals("Australia")){return WaterbodyOrCountry.AUSTRALIACOMMONWEALTHOF();
		}else if (area.equals("Indonesia")){return WaterbodyOrCountry.INDONESIAREPUBLICOF();
		}else if (area.equals("Philippines")){return WaterbodyOrCountry.PHILIPPINESREPUBLICOFTHE();
		}else if (area.equals("Mongolia")){return WaterbodyOrCountry.MONGOLIAMONGOLIANPEOPLESREPUBLIC();
		}else if (area.equals("Russia")){return WaterbodyOrCountry.RUSSIANFEDERATION();
		}else if (area.equals("France")){return WaterbodyOrCountry.FRANCEFRENCHREPUBLIC();
		}else if (area.equals("Poland")){return WaterbodyOrCountry.POLANDPOLISHPEOPLESREPUBLIC();
		}else if (area.equals("Brazil")){return WaterbodyOrCountry.BRAZILFEDERATIVEREPUBLICOF();
		
		}else if (area.equals("Cuba")){return WaterbodyOrCountry.BRAZILFEDERATIVEREPUBLICOF();
		}else if (area.equals("Guatemala")){return WaterbodyOrCountry.GUATEMALAREPUBLICOF();
		}else if (area.equals("Colombia")){return WaterbodyOrCountry.COLOMBIAREPUBLICOF();
		}else if (area.equals("India")){return WaterbodyOrCountry.INDIAREPUBLICOF();
		
		}else if (area.equals("Mexico")){return WaterbodyOrCountry.MEXICOUNITEDMEXICANSTATES();
		}else if (area.equals("Peru")){return WaterbodyOrCountry.PERUREPUBLICOF();
		}else if (area.equals("Ecuador")){return WaterbodyOrCountry.ECUADORREPUBLICOF();
		}else if (area.equals("Venezuela")){return WaterbodyOrCountry.VENEZUELABOLIVARIANREPUBLICOF();
		}else if (area.equals("Guyana")){return WaterbodyOrCountry.GUYANAREPUBLICOF();
		}else if (area.equals("Panama")){return WaterbodyOrCountry.PANAMAREPUBLICOF();

		}else if (area.equals("Paraguay")){return WaterbodyOrCountry.PARAGUAYREPUBLICOF();
		}else if (area.equals("Suriname")){return WaterbodyOrCountry.SURINAMEREPUBLICOF();
		}else if (area.equals("Costa Rica")){return WaterbodyOrCountry.COSTARICAREPUBLICOF();
		}else if (area.equals("Ivory Coast")){return WaterbodyOrCountry.COTEDIVOIREIVORYCOASTREPUBLICOFTHE();

		}else if (area.equals("Benin")){return WaterbodyOrCountry.BENINPEOPLESREPUBLICOF();
		}else if (area.equalsIgnoreCase("Kenya")){return WaterbodyOrCountry.KENYAREPUBLICOF();
		}else if (area.equals("Uganda")){return WaterbodyOrCountry.UGANDAREPUBLICOF();
		}else if (area.equals("Zambia")){return WaterbodyOrCountry.ZAMBIAREPUBLICOF();
		}else if (area.equals("Rwanda")){return WaterbodyOrCountry.RWANDARWANDESEREPUBLIC();
		}else if (area.equals("South Africa")){return WaterbodyOrCountry.SOUTHAFRICAREPUBLICOF();
		}else if (area.equals("Botswana")){return WaterbodyOrCountry.BOTSWANAREPUBLICOF();
		}else if (area.equals("Burundi")){return WaterbodyOrCountry.BURUNDIREPUBLICOF();
		}else if (area.equals("Cameroon")){return WaterbodyOrCountry.CAMEROONUNITEDREPUBLICOF();
		
		}else if (area.equals("Congo")){return WaterbodyOrCountry.CONGOPEOPLESREPUBLICOF();
		}else if (area.equals("Zaire")){return WaterbodyOrCountry.CONGODEMOCRATICREPUBLICOF();
		}else if (area.equals("Equatorial Guinea")){return WaterbodyOrCountry.EQUATORIALGUINEAREPUBLICOF();
		}else if (area.equals("Gabon")){return WaterbodyOrCountry.GABONGABONESEREPUBLIC();
		}else if (area.equals("Liberia")){return WaterbodyOrCountry.LIBERIAREPUBLICOF();
		
		}else if (area.equals("Togo")){return WaterbodyOrCountry.TOGOTOGOLESEREPUBLIC();
		}else if (area.equals("Guinea")){return WaterbodyOrCountry.GUINEAREVOLUTIONARYPEOPLESREPCOF();
		}else if (area.equals("Guinea-Bissau")){return WaterbodyOrCountry.GUINEABISSAUREPUBLICOF();
		
		}else if (area.equals("Malawi")){return WaterbodyOrCountry.MALAWIREPUBLICOF();
		}else if (area.equals("Mozambique")){return WaterbodyOrCountry.MOZAMBIQUEPEOPLESREPUBLICOF();
		}else if (area.equals("Nigeria")){return WaterbodyOrCountry.NIGERIAFEDERALREPUBLICOF();
		}else if (area.equals("Senegal")){return WaterbodyOrCountry.SENEGALREPUBLICOF();
		}else if (area.equals("Sierra Leone")){return WaterbodyOrCountry.SIERRALEONEREPUBLICOF();
		}else if (area.equals("Sudan")){return WaterbodyOrCountry.SUDANDEMOCRATICREPUBLICOFTHE();
		}else if (area.equals("Madagascar")){return WaterbodyOrCountry.MADAGASCARREPUBLICOF();
		}else if (area.equals("Comoros")){return WaterbodyOrCountry.COMOROSUNIONOFTHE();
		
		}else if (area.equals("Vietnam")){return WaterbodyOrCountry.VIETNAMSOCIALISTREPUBLICOF();
		}else if (area.equals("Thailand")){return WaterbodyOrCountry.THAILANDKINGDOMOF();
		}else if (area.equals("Bhutan")){return WaterbodyOrCountry.BHUTANKINGDOMOF();
		}else if (area.equals("Laos")){return WaterbodyOrCountry.LAOPEOPLESDEMOCRATICREPUBLIC();
		}else if (area.equals("Myanmar (Burma)")){return WaterbodyOrCountry.MYANMAR();
		}else if (area.equals("Nepal")){return WaterbodyOrCountry.NEPALKINGDOMOF();
		}else if (area.equals("Pakistan")){return WaterbodyOrCountry.PAKISTANISLAMICREPUBLICOF();
		}else if (area.equals("Singapore")){return WaterbodyOrCountry.SINGAPOREREPUBLICOF();
		
		}else if (area.equals("Honduras")){return WaterbodyOrCountry.HONDURASREPUBLICOF();
		}else if (area.equals("Nicaragua")){return WaterbodyOrCountry.NICARAGUAREPUBLICOF();
		}else if (area.equals("Trinidad and Tobago")){return WaterbodyOrCountry.TRINIDADANDTOBAGOREPUBLICOF();
		}else if (area.equals("United States")){return WaterbodyOrCountry.UNITEDSTATESOFAMERICA();
		}else if (area.equals("Uruguay")){return WaterbodyOrCountry.URUGUAYEASTERNREPUBLICOF();
		}else if (area.equals("Haiti")){return WaterbodyOrCountry.HAITIREPUBLICOF();
		}else if (area.equals("North Korea")){return WaterbodyOrCountry.KOREADEMOCRATICPEOPLESREPUBLICOF();
		}else if (area.equals("South Korea")){return WaterbodyOrCountry.KOREAREPUBLICOF();
		}else if (area.equals("Taiwan")){return WaterbodyOrCountry.TAIWANPROVINCEOFCHINA();
		
		}else if (area.equals("Somalia")){return WaterbodyOrCountry.SOMALIASOMALIREPUBLIC();
		}else if (area.equals("Albania")){return WaterbodyOrCountry.ALBANIAPEOPLESSOCIALISTREPUBLICOF();
		}else if (area.equals("Algeria")){return WaterbodyOrCountry.ALGERIAPEOPLESDEMOCRATICREPUBLICOF();
		
		}else if (area.equals("Andorra")){return WaterbodyOrCountry.ANDORRAPRINCIPALITYOF();
		}else if (area.equals("Austria")){return WaterbodyOrCountry.AUSTRIAREPUBLICOF();
		}else if (area.equals("Azerbaijan")){return WaterbodyOrCountry.AZERBAIJANREPUBLICOF();
		}else if (area.equals("Bulgaria")){return WaterbodyOrCountry.BULGARIAPEOPLESREPUBLICOF();
		}else if (area.equals("Croatia")){return WaterbodyOrCountry.HRVATSKA();
		}else if (area.equals("Greece")){return WaterbodyOrCountry.GREECEHELLENICREPUBLIC();
		}else if (area.equals("Hungary")){return WaterbodyOrCountry.HUNGARYHUNGARIANPEOPLESREPUBLIC();
		
		}else if (area.equals("Iran")){return WaterbodyOrCountry.IRANISLAMICREPUBLICOF();
		}else if (area.equals("Iraq")){return WaterbodyOrCountry.IRAQREPUBLICOF();
		}else if (area.equals("Israel")){return WaterbodyOrCountry.ISRAELSTATEOF();
		}else if (area.equals("Italy")){return WaterbodyOrCountry.ITALYITALIANREPUBLIC();
		}else if (area.equals("Kazakhstan")){return WaterbodyOrCountry.KAZAKHSTANREPUBLICOF();
		}else if (area.equals("Kyrgyzstan")){return WaterbodyOrCountry.KYRGYZREPUBLIC();
		}else if (area.equals("Lebanon")){return WaterbodyOrCountry.LEBANONLEBANESEREPUBLIC();
		
		}else if (area.equals("Luxembourg")){return WaterbodyOrCountry.LUXEMBOURGGRANDDUCHYOF();
		}else if (area.equals("Macedonia")){return WaterbodyOrCountry.MACEDONIATHEFORMERYUGOSLAVREPUBLICOF();
		}else if (area.equals("Moldova")){return WaterbodyOrCountry.MOLDOVAREPUBLICOF();
		}else if (area.equals("Morocco")){return WaterbodyOrCountry.MOROCCOKINGDOMOF();
		}else if (area.equals("Romania")){return WaterbodyOrCountry.ROMANIASOCIALISTREPUBLICOF();
		}else if (area.equals("Serbia")){return WaterbodyOrCountry.SERBIAANDMONTENEGRO();
		}else if (area.equals("Slovakia")){return WaterbodyOrCountry.SLOVAKIA();
		}else if (area.equals("Spain")){return WaterbodyOrCountry.SPAINSPANISHSTATE();
		
		}else if (area.equals("Switzerland")){return WaterbodyOrCountry.SWITZERLANDSWISSCONFEDERATION();
		}else if (area.equals("Syria")){return WaterbodyOrCountry.SYRIANARABREPUBLIC();
		}else if (area.equals("Turkey")){return WaterbodyOrCountry.TURKEYREPUBLICOF();
		}else if (area.equals("Cambodia")){return WaterbodyOrCountry.CAMBODIAKINGDOMOF();
		}else if (area.equals("Bangladesh")){return WaterbodyOrCountry.BANGLADESHPEOPLESREPUBLICOF();
		
		}else if (area.equals("Sri Lanka")){return WaterbodyOrCountry.SRILANKADEMOCRATICSOCIALISTREPUBLICOF();
		}else if (area.equals("Bahamas")){return WaterbodyOrCountry.BAHAMASCOMMONWEALTHOFTHE();
		}else if (area.equals("Western Samoa")){return WaterbodyOrCountry.SAMOAINDEPENDENTSTATEOF();
		}else if (area.equals("Finland")){return WaterbodyOrCountry.FINLANDREPUBLICOF();
		}else if (area.equals("Norway")){return WaterbodyOrCountry.NORWAYKINGDOMOF();
		}else if (area.equals("El Salvador")){return WaterbodyOrCountry.ELSALVADORREPUBLICOF();
		}else if (area.equals("Cyprus")){return WaterbodyOrCountry.CYPRUSREPUBLICOF();
		}else if (area.equals("Sweden")){return WaterbodyOrCountry.SWEDENKINGDOMOF();
		}else if (area.equals("Denmark")){return WaterbodyOrCountry.DENMARKKINGDOMOF();
		}else if (area.equals("Trinidad & Tobago")){return WaterbodyOrCountry.TRINIDADANDTOBAGOREPUBLICOF();
		}else if (area.equals("Chile")){return WaterbodyOrCountry.CHILEREPUBLICOF();
		}else if (area.equals("Jordan")){return WaterbodyOrCountry.JORDANHASHEMITEKINGDOMOF();
		}else if (area.equals("Montenegro")){
			logger.warn("Montenegro is currently mapped to 'Serbia & Montenegro'");
			return WaterbodyOrCountry.SERBIAANDMONTENEGRO();
		}else if (area.equals("Portugal")){return WaterbodyOrCountry.PORTUGALPORTUGUESEREPUBLIC();
		}else if (area.equals("Tunisia")){return WaterbodyOrCountry.TUNISIAREPUBLICOF();
		}else if (area.equals("Seychelles")){return WaterbodyOrCountry.SEYCHELLESREPUBLICOF();
		}else if (area.equals("Fiji")){return WaterbodyOrCountry.FIJIREPUBLICOFTHEFIJIISLANDS();
		}else if (area.equals("Belgium")){return WaterbodyOrCountry.BELGIUMKINGDOMOF();
		}else if (area.equals("Virgin Islands")){return WaterbodyOrCountry.USVIRGINISLANDS();
		}else if (area.equals("Gambia")){return WaterbodyOrCountry.GAMBIAREPUBLICOFTHE();
		}else if (area.equals("Dominica")){return WaterbodyOrCountry.DOMINICACOMMONWEALTHOF();
		}else if (area.equals("Liechtenstein")){return WaterbodyOrCountry.LIECHTENSTEINPRINCIPALITYOF();
		}else if (area.matches("B(y)?elarus")){return WaterbodyOrCountry.BELARUS();
		
		
		
		
		
		}else{	
			if (unhandledCountries.contains(area)){
//				logger.warn("Unhandled country '" + area + "' replaced by null" );
				return null;
			}
//			String warning = "New language abbreviation " + area;
//			logger.warn(warning);
			return null;
//			throw new IllegalArgumentException(warning);
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
//		}else if (key.equalsIgnoreCase("recent only")){return uuidRecentOnly;
//		}else if (key.equalsIgnoreCase("recent + fossil")){return uuidRecentAndFossil;
//		}else if (key.equalsIgnoreCase("fossil only")){return uuidFossilOnly;
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
