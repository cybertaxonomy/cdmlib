// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.erms;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class ErmsTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(ErmsTransformer.class);
	
	public static final int SOURCE_USE_ORIGINAL_DESCRIPTION = 1;
	public static final int SOURCE_USE_BASIS_OF_RECORD = 2;
	public static final int SOURCE_USE_ADDITIONAL_SOURCE = 3;
	public static final int SOURCE_USE_SOURCE_OF_SYNONYMY = 4;
	public static final int SOURCE_USE_REDESCRIPTION = 5;
	public static final int SOURCE_USE_NEW_COMBINATION_REFERENCE = 6;
	public static final int SOURCE_USE_STATUS_SOURCE = 7;
	public static final int SOURCE_USE_EMENDATION = 8;
	
	//language uuids
	public static final UUID uuidEuropeanMarineWaters = UUID.fromString("47389e42-3b3c-4873-bded-ac030db86462");
	public static final UUID uuidMediterraneanSea = UUID.fromString("bde8a624-23c4-4ac3-b381-11287f5d656a");
	public static final UUID uuidWhiteSea = UUID.fromString("bf14bfb6-8925-4696-911c-56d3e90d4491");
	public static final UUID uuidNorthSea = UUID.fromString("d5ea2d46-ed97-4996-8702-0619231626b6");
	public static final UUID uuidBalticSea = UUID.fromString("0031cda2-4b27-49de-afa3-fdec75ee5060");
	public static final UUID uuidBlackSea = UUID.fromString("1f110909-7462-4ee8-a7ff-9f976701dd1d");
	public static final UUID uuidBarentsSea = UUID.fromString("b6fd9a4d-0ad0-4481-a0b4-5dd71c8fda8b");
	public static final UUID uuidCaspianSea = UUID.fromString("0d3c3850-0cec-48d0-ac0d-9cbcc2c60552");
	public static final UUID uuidPortugueseExclusiveEconomicZone = UUID.fromString("642336f3-41cb-4546-9a1c-ffeccbad2ef5");
	public static final UUID uuidBelgianExclusiveEconomicZone = UUID.fromString("02138b0f-cee1-4c56-ae12-72a5b36839af");
	public static final UUID uuidFrenchExclusiveEconomicZone = UUID.fromString("9f6a61c0-c329-4a61-a47a-f5f383737c36");
	public static final UUID uuidEnglishChannel = UUID.fromString("3ed96112-bb4a-47df-b489-2c198d6f0fd2");
	public static final UUID uuidAdriaticSea = UUID.fromString("da376165-f970-4f0c-99db-773686d66591");
	public static final UUID uuidBiscayBay = UUID.fromString("1461cf85-e0b4-4ac3-bdf5-efe6053af174");
	public static final UUID uuidDutchExclusiveEconomicZone = UUID.fromString("a1bd019e-e2af-41c8-a5e4-c7245b575759");
	public static final UUID uuidUnitedKingdomExclusiveEconomicZone = UUID.fromString("18ab29c0-3104-4102-ada8-6711fcdbdbb8");
	public static final UUID uuidSpanishExclusiveEconomicZone = UUID.fromString("68c2823a-2173-4c31-89e8-bc1439abf448");
	public static final UUID uuidEgyptianExclusiveEconomicZone = UUID.fromString("e542bcfd-0ff1-49ac-a6ae-c0b3db39e560");
	public static final UUID uuidGrecianExclusiveEconomicZone = UUID.fromString("1d14857c-30be-4d3a-bae9-8e79da0d9d1d");
	public static final UUID uuidTirrenoSea = UUID.fromString("6e4f8a9d-ca6e-4b23-9211-446fac35a052");
	public static final UUID uuidIcelandicExclusiveEconomicZone = UUID.fromString("a121a8fb-6287-4661-9228-0816affdf3f5");
	public static final UUID uuidIrishExclusiveeconomicZone = UUID.fromString("c8fe2626-53d2-4eaa-962b-99662470b96e");
	public static final UUID uuidIrishSea = UUID.fromString("9e972ad5-b153-419e-ab7e-935b93ff881b");
	public static final UUID uuidItalianExclusiveEconomicZone = UUID.fromString("10557c6f-a33f-443a-ad8b-cd31c105bddd");
	public static final UUID uuidNorwegianSea = UUID.fromString("c6c44372-a963-41b2-8c12-a0b46425c523");
	public static final UUID uuidMoroccanExclusiveEconomicZone = UUID.fromString("e62e5cc2-922f-4807-abd6-1b4bffbced49");
	public static final UUID uuidNorwegianExclusiveEconomicZone = UUID.fromString("bd317f3e-9719-4943-ae3e-19ff0c9761be");
	public static final UUID uuidSkagerrak = UUID.fromString("5f63ece2-d112-4b39-80a0-bffb6c78654c");
	public static final UUID uuidTunisianExclusiveEconomicZone = UUID.fromString("b5972b59-6a36-45ea-88f7-0c520c99b99d");
	public static final UUID uuidWaddenSea = UUID.fromString("ae0c4555-8e19-479d-8a4f-e1b62939c09b");
	public static final UUID uuidBeltSea = UUID.fromString("780f4144-f157-45e8-ae42-cacb3ec369ba");
	public static final UUID uuidMarmaraSea = UUID.fromString("3db5d470-3265-4187-ba5a-01ecfb94ce6e");
	public static final UUID uuidSeaofAzov = UUID.fromString("5b02cb7e-8a83-446c-af47-936a2ea31a8a");
	public static final UUID uuidAegeanSea = UUID.fromString("65d6c443-225f-4ac0-9c86-da51502b46df");
	public static final UUID uuidBulgarianExclusiveEconomicZone = UUID.fromString("13e5aa21-3971-4d06-bc34-ed75a31c2f66");
	public static final UUID uuidSouthBalticproper = UUID.fromString("1c2a672d-4948-455d-9877-42a8da1ff1d0");
	public static final UUID uuidBalticProper = UUID.fromString("12ddfcad-bf8f-43d8-a772-15ae69d37b20");
	public static final UUID uuidNorthBalticproper = UUID.fromString("183ec305-1e9e-4cb1-93cc-703bd64de28f");
	public static final UUID uuidArchipelagoSea = UUID.fromString("d9ea9d63-ec4d-4b01-967d-13f28b09a715");
	public static final UUID uuidBothnianSea = UUID.fromString("926f7fa3-b0a4-4763-85eb-4c3804a72333");
	public static final UUID uuidGermanExclusiveEconomicZone = UUID.fromString("a6dbea03-090f-4f5f-bf5e-27a00ab4cc1d");
	public static final UUID uuidSwedishExclusiveEconomicZone = UUID.fromString("94b0e605-d241-44e1-a301-d8911c34fdef");
	public static final UUID uuidUkrainianExclusiveEconomicZone = UUID.fromString("b7335968-e34f-412c-91a5-5dc0b73310e7");
	public static final UUID uuidMadeiranExclusiveEconomicZone = UUID.fromString("c00f442a-4c08-4452-b979-825fa3ff97b2");
	public static final UUID uuidLebaneseExclusiveEconomicZone = UUID.fromString("d9f7dc8b-9041-4206-bf5f-5226c42a5978");
	public static final UUID uuidSpanishExclusiveEconomicZoneMediterraneanpart = UUID.fromString("94ccf304-9687-41b6-a14b-019509adb723");
	public static final UUID uuidEstonianExclusiveEconomicZone = UUID.fromString("ed17f07b-357f-4b4a-9653-3a564fdd32e5");
	public static final UUID uuidCroatianExclusiveEconomicZone = UUID.fromString("028b045a-b1bd-4a72-a4c2-a3d0473b8257");
	public static final UUID uuidBalearSea = UUID.fromString("478f30f0-01b1-4772-9d01-3a0a571f41c3");
	public static final UUID uuidTurkishExclusiveEconomicZone = UUID.fromString("3d552e73-2bf5-4f36-8a91-94fbead970e5");
	public static final UUID uuidDanishExclusiveEconomicZone = UUID.fromString("53d5a8bd-804b-4cbb-b5ad-f47ff6433db0");


	//feature uuids
	public static final UUID uuidRemark = UUID.fromString("648eab77-8469-4139-bbf4-3fb26ec15864");
	public static final UUID uuidAdditionalinformation = UUID.fromString("ef00c304-ce33-45ef-9543-0b9336a2b6eb");
	public static final UUID uuidSpelling = UUID.fromString("536594a1-21a5-4d99-aa46-132bc7b31316");
	public static final UUID uuidPublicationdate = UUID.fromString("b996b34f-1313-4575-bf46-732676674290");
	public static final UUID uuidSystematics = UUID.fromString("caac0f7f-f43e-4b7c-b296-ec2d930c4d05");
	public static final UUID uuidClassification = UUID.fromString("aa9bffd3-1fa8-4bd7-9e25-e2d162177b3d");
	public static final UUID uuidEnvironment = UUID.fromString("4f8ea10d-2242-443f-9d7d-4ecccdee4953");
	public static final UUID uuidHabitat = UUID.fromString("b7387877-51e3-4192-b9e4-025a359f4b59");
	public static final UUID uuidAuthority = UUID.fromString("9c7f8908-2530-4900-8da9-d328f7ac9031");
	public static final UUID uuidMorphology = UUID.fromString("5be1f948-d85f-497f-a0d5-4e5f3b227274");
	public static final UUID uuidTaxonomicRemarks = UUID.fromString("cc863aee-8da9-448b-82cd-47e3af942998");
	public static final UUID uuidNote = UUID.fromString("2c66d35f-c76e-40e0-951b-f2c340e5973f");
	public static final UUID uuidTaxonomy = UUID.fromString("d5734631-c86b-4212-9b8d-cb62f813e0a0");
	public static final UUID uuidTaxonomicstatus = UUID.fromString("ffbadab5-a8bc-4fb6-a6b3-d1f2593187ff");
	public static final UUID uuidStatus = UUID.fromString("fcc50853-bcff-4d0f-bc9a-123d7f175490");
	public static final UUID uuidRank = UUID.fromString("cabada57-a098-47fc-929f-31c8c910f6cf");
	public static final UUID uuidHomonymy = UUID.fromString("2791a14f-49b2-417f-a248-84c3d022d75f");
	public static final UUID uuidNomenclature = UUID.fromString("15fe184f-4aab-4076-8bbb-3415d6f1f27f");
	public static final UUID uuidTypespecies = UUID.fromString("cf674b0d-76e2-4628-952c-2cd06e209c6e");
	public static final UUID uuidTaxonomicRemark = UUID.fromString("044e7c4e-aab8-4f44-bfa5-0339e7576c74");
	public static final UUID uuidDateofPublication = UUID.fromString("2a416574-69db-4f80-b9a7-b912d5ed1816");
	public static final UUID uuidAcknowledgments = UUID.fromString("3b2fd495-3f9a-480e-986a-7643741177da");
	public static final UUID uuidOriginalpublication = UUID.fromString("ea9b7e53-0487-499f-a281-3d82d10e76dd");
	public static final UUID uuidTypelocality = UUID.fromString("7c1c5779-2b4b-467b-b2ca-5ca2e029e116");
	public static final UUID uuidValidity = UUID.fromString("bd066f25-935b-4b4e-a2eb-3fbfcd5e608f");
	public static final UUID uuidIdentification = UUID.fromString("dec3cd5b-0690-4035-825d-bda9aee96bc1");
	public static final UUID uuidSynonymy = UUID.fromString("f5c8be5f-8d33-47df-838e-55fc7999fc81");

	//extension type uuids
	public static final UUID GAZETTEER_UUID = UUID.fromString("dcfa124a-1028-49cd-aea5-fdf9bd396c1a");
	public static final UUID IMIS_UUID = UUID.fromString("ee2ac2ca-b60c-4e6f-9cad-720fcdb0a6ae");
	public static final UUID uuidFossilStatus = UUID.fromString("ec3dffbe-a0c8-4d76-845f-5fc166a33d5b");
	public static final UUID uuidTsn = UUID.fromString("6b0df02b-7278-4ce0-8fc9-0e6523832eb5");
	public static final UUID uuidDisplayName = UUID.fromString("cd72225d-32c7-4b2d-a973-a95184392690");
	public static final UUID uuidFuzzyName = UUID.fromString("8870dc69-d3a4-425f-a5a8-093a79f527a8");
	public static final UUID uuidCredibility = UUID.fromString("909a3886-8744-49dc-b9cc-277378b81b42");
	public static final UUID uuidCompleteness = UUID.fromString("141f4816-78c0-4da1-8a79-5c9031e6b149");
	public static final UUID uuidUnacceptReason = UUID.fromString("3883fb79-374d-4120-964b-9666307e3567");
	public static final UUID uuidQualityStatus = UUID.fromString("4de84c6e-41bd-4a0e-894d-77e9ec3103d2");
	
	
	public static NomenclaturalCode kingdomId2NomCode(Integer kingdomId){
		switch (kingdomId){
			case 1: return null;
			case 2: return NomenclaturalCode.ICZN;  //Animalia
			case 3: return NomenclaturalCode.ICBN;  //Plantae
			case 4: return NomenclaturalCode.ICBN;  //Fungi
			case 5: return NomenclaturalCode.ICZN ;  //Protozoa
			case 6: return NomenclaturalCode.ICNB ;  //Bacteria
			case 7: return NomenclaturalCode.ICBN;  //Chromista
			case 147415: return NomenclaturalCode.ICNB;  //Monera
			default: return null;
	
		}
	
	}
	
	
	
	
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


	public Language getLanguageByKey(String ermsAbbrev) throws IllegalArgumentException {
		Set<String> unhandledLanguages = new HashSet<String>();
		if (CdmUtils.isEmpty(ermsAbbrev)){return null;
		}else if (ermsAbbrev.equals("af")){return Language.AFRIKAANS();
		}else if (ermsAbbrev.equals("al")){return Language.ALEUT();
		}else if (ermsAbbrev.equals("ar")){return Language.ARABIC();
		}else if (ermsAbbrev.equals("as")){return Language.ASSAMESE();
//		}else if (ermsAbbrev.equals("au")){return Language.AUNS();  //??
		}else if (ermsAbbrev.equals("az")){return Language.AZERBAIJANI();
		}else if (ermsAbbrev.equals("ba")){return Language.BASQUE();
		}else if (ermsAbbrev.equals("be")){return Language.BELORUSSIAN();
		}else if (ermsAbbrev.equals("bg")){return Language.BULGARIAN();
		}else if (ermsAbbrev.equals("bn")){return Language.BENGALI();
		}else if (ermsAbbrev.equals("br")){return Language.BRETON();
		}else if (ermsAbbrev.equals("bu")){return Language.BURMESE();
		}else if (ermsAbbrev.equals("ca")){return Language.CATALAN_VALENCIAN();  //??? (Catalan)
		}else if (ermsAbbrev.equals("ce")){return Language.CEBUANO();
		}else if (ermsAbbrev.equals("ch")){return Language.CHINESE();
//		}else if (ermsAbbrev.equals("cl")){return Language.CHUKCHI(); // (LOURAVETLANY)(); //iso639-3: ckt //also known as Luoravetlan, Chukot and Chukcha is a Palaeosiberian language spoken by Chukchi people in the easternmost extremity of Siberia, mainly in Chukotka Autonomous Okrug.
		}else if (ermsAbbrev.equals("cr")){return Language.CROATIAN();
		}else if (ermsAbbrev.equals("cs")){return Language.CZECH();
		}else if (ermsAbbrev.equals("da")){return Language.DANISH();
		}else if (ermsAbbrev.equals("de")){return Language.GERMAN();
//		}else if (ermsAbbrev.equals("ec")){return Language.ENGLISH-CANADIAN();  //no iso
		}else if (ermsAbbrev.equals("ee")){return Language.ESTONIAN();
//		}else if (ermsAbbrev.equals("ek")){return Language.EVEN-KAMCHATKA(); //iso639-3: eve    Lamut, Ewen, Eben, Orich, Ilqan; Russian: ???´????? ???´?, earlier also ????????? ???´?) is a Tungusic language spoken by the Evens in Siberia
		}else if (ermsAbbrev.equals("en")){return Language.ENGLISH();
		}else if (ermsAbbrev.equals("ep")){return Language.ESPERANTO();
		}else if (ermsAbbrev.equals("es")){return Language.SPANISH_CATALAN();
//		}else if (ermsAbbrev.equals("eu")){return Language.ENGLISH-UNITED STATES();  no iso //ENGLISH();
//		}else if (ermsAbbrev.equals("ev")){return Language.EVENKI();   iso: evn  //languages of Tungusic family 
		}else if (ermsAbbrev.equals("fa")){return Language.PERSIAN(); 
//		}else if (ermsAbbrev.equals("fc")){return Language.FRENCH-CANADIAN();   no iso  //FRENCH();
		}else if (ermsAbbrev.equals("fi")){return Language.FINNISH();
		}else if (ermsAbbrev.equals("fj")){return Language.FIJIAN();
		}else if (ermsAbbrev.equals("fl")){return Language.DUTCH_FLEMISH();
		}else if (ermsAbbrev.equals("fo")){return Language.FAROESE();
		}else if (ermsAbbrev.equals("fr")){return Language.FRENCH();
		}else if (ermsAbbrev.equals("ga")){return Language.GAELIC_SCOTTISH_GAELIC();  //??
		}else if (ermsAbbrev.equals("ge")){return Language.KALAALLISUT_GREENLANDIC(); // GREENLANDIC
		}else if (ermsAbbrev.equals("gl")){return Language.GALICIAN();
		}else if (ermsAbbrev.equals("gr")){return Language.GREEK_MODERN(); //(Greek)
//		}else if (ermsAbbrev.equals("gu")){return Language.GUARAYO();     //GUARANI() ??
//		}else if (ermsAbbrev.equals("ha")){return Language.HASSANYA(); Hassaniyya Arabic  ios 639-3: mey
		}else if (ermsAbbrev.equals("he")){return Language.HEBREW();
		}else if (ermsAbbrev.equals("hi")){return Language.HINDI();
		}else if (ermsAbbrev.equals("hu")){return Language.HUNGARIAN();
		}else if (ermsAbbrev.equals("hw")){return Language.HAWAIIAN();
		}else if (ermsAbbrev.equals("hy")){return Language.ARMENIAN();
		}else if (ermsAbbrev.equals("in")){return Language.INDONESIAN();
		}else if (ermsAbbrev.equals("iq")){return Language.INUPIAQ();
		}else if (ermsAbbrev.equals("ir")){return Language.IRISH();
		}else if (ermsAbbrev.equals("is")){return Language.ICELANDIC();
		}else if (ermsAbbrev.equals("it")){return Language.ITALIAN();
		}else if (ermsAbbrev.equals("ja")){return Language.JAPANESE();
//		}else if (ermsAbbrev.equals("ji")){return Language.JIVARA();   		//??
//		}else if (ermsAbbrev.equals("ka")){return Language.KAMCHADAL();   iso 639-3:itl //Itelmen, formerly also known as Kamchadal, is a language belonging to the Chukotko-Kamchatkan family traditionally spoken in the Kamchatka Peninsula.    
		}else if (ermsAbbrev.equals("ko")){return Language.KOREAN();
//		}else if (ermsAbbrev.equals("kr")){return Language.KORYAK();    //iso639-3: kpy
		}else if (ermsAbbrev.equals("la")){return Language.LATIN();
		}else if (ermsAbbrev.equals("li")){return Language.LITHUANIAN();
//		}else if (ermsAbbrev.equals("lp")){return Language.LAPP();      //??
		}else if (ermsAbbrev.equals("lv")){return Language.LATVIAN();
		}else if (ermsAbbrev.equals("ma")){return Language.MACEDONIAN();
//		}else if (ermsAbbrev.equals("mh")){return Language.MAHR();   //Marathi ; Mari ??
//		}else if (ermsAbbrev.equals("mk")){return Language.MAKAH (QWIQWIDICCIAT)();  //iso639-3: myh
		}else if (ermsAbbrev.equals("ml")){return Language.MALAY();
//		}else if (ermsAbbrev.equals("ne")){return Language.NENETS();   iso639-3 yrk; iso639-2: mis
		}else if (ermsAbbrev.equals("nl")){return Language.DUTCH_FLEMISH();
		}else if (ermsAbbrev.equals("no")){return Language.NORWEGIAN();
		}else if (ermsAbbrev.equals("np")){return Language.NEPALI();
//		}else if (ermsAbbrev.equals("os")){return Language.OSTYAK();   //Ostyak on its own or in combination, can refer, especially in older literature, to several Siberian peoples and languages:
		//																Khanty language (kca; 639-2: fiu); Ket language(ket); Selkup language(sel; 639-2: sel)
//		}else if (ermsAbbrev.equals("pi")){return Language.PIRAYAGUARA();  //??
		}else if (ermsAbbrev.equals("pl")){return Language.POLISH();
		}else if (ermsAbbrev.equals("pt")){return Language.PORTUGUESE();
		}else if (ermsAbbrev.equals("ro")){return Language.ROMANIAN();
		}else if (ermsAbbrev.equals("ru")){return Language.RUSSIAN();
		}else if (ermsAbbrev.equals("sc")){return Language.SCOTS();
		}else if (ermsAbbrev.equals("sd")){return Language.SINDHI();
//		}else if (ermsAbbrev.equals("sh")){return Language.SERBO_CROATIAN();  //hbs
		}else if (ermsAbbrev.equals("si")){return Language.SINHALA_SINHALESE();
		}else if (ermsAbbrev.equals("sk")){return Language.SLOVAK();
		}else if (ermsAbbrev.equals("sn")){return Language.SLOVENIAN();
		}else if (ermsAbbrev.equals("sr")){return Language.SERBIAN();
		}else if (ermsAbbrev.equals("st")){return Language.SRANAN_TONGO();
		}else if (ermsAbbrev.equals("sv")){return Language.SWEDISH();
		}else if (ermsAbbrev.equals("sw")){return Language.SWAHILI();
		}else if (ermsAbbrev.equals("ta")){return Language.TAMIL();
		}else if (ermsAbbrev.equals("te")){return Language.TELUGU();
		}else if (ermsAbbrev.equals("tg")){return Language.TAGALOG();
		}else if (ermsAbbrev.equals("th")){return Language.THAI();
//		}else if (ermsAbbrev.equals("tm")){return Language.TAMUL();			//??
		}else if (ermsAbbrev.equals("tr")){return Language.TURKISH();
		}else if (ermsAbbrev.equals("tu")){return Language.TUPIS();
		}else if (ermsAbbrev.equals("uk")){return Language.UKRAINIAN();
		}else if (ermsAbbrev.equals("ur")){return Language.URDU();
		}else if (ermsAbbrev.equals("vi")){return Language.VIETNAMESE();
		}else if (ermsAbbrev.equals("we")){return Language.WELSH();
		}else if (ermsAbbrev.equals("wo")){return Language.WOLOF();
		}else if (ermsAbbrev.equals("ya")){return Language.YAKUT();
		}else if (ermsAbbrev.equals("yp")){return Language.YUPIKS();
//		}else if (ermsAbbrev.equals("yu")){return Language.YUKAGIR();  639-2: mis;  639-3 yux (Southern Yukaghir)- ykg(Tundra Yukaghir)
		}else{
			unhandledLanguages.add("au");
			unhandledLanguages.add("cl");
			unhandledLanguages.add("ec");
			unhandledLanguages.add("ek");
			unhandledLanguages.add("eu");
			unhandledLanguages.add("ev");
			unhandledLanguages.add("fc");
			unhandledLanguages.add("gu");
			unhandledLanguages.add("ha");
			unhandledLanguages.add("ji");
			unhandledLanguages.add("ka");
			unhandledLanguages.add("kr");
			unhandledLanguages.add("lp");
			unhandledLanguages.add("mh");
			unhandledLanguages.add("mk");
			unhandledLanguages.add("ne");
			unhandledLanguages.add("os");
			unhandledLanguages.add("pi");
			unhandledLanguages.add("sh");
			unhandledLanguages.add("tm");
			unhandledLanguages.add("sh");
			unhandledLanguages.add("yu");
			
			if (unhandledLanguages.contains(ermsAbbrev)){
				logger.warn("Unhandled language '" + ermsAbbrev + "' replaced by 'UNDETERMINED'" );
				return Language.UNDETERMINED();
			}
			String warning = "New language abbreviation " + ermsAbbrev;
			logger.warn(warning);
			throw new IllegalArgumentException(warning);
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

	public static UUID uuidFromGuName(String guName){
		if (CdmUtils.isEmpty(guName)){return null;
		}else if (guName.equalsIgnoreCase("European Marine Waters")){ return uuidEuropeanMarineWaters;
		}else if (guName.equalsIgnoreCase("Mediterranean Sea")){ return uuidMediterraneanSea;
		}else if (guName.equalsIgnoreCase("White Sea")){ return uuidWhiteSea;
		}else if (guName.equalsIgnoreCase("North Sea")){ return uuidNorthSea;
		}else if (guName.equalsIgnoreCase("Baltic Sea")){ return uuidBalticSea;
		}else if (guName.equalsIgnoreCase("Black Sea")){ return uuidBlackSea;
		}else if (guName.equalsIgnoreCase("Barents Sea")){ return uuidBarentsSea;
		}else if (guName.equalsIgnoreCase("Caspian Sea")){ return uuidCaspianSea;
		}else if (guName.equalsIgnoreCase("Portuguese Exclusive Economic Zone")){ return uuidPortugueseExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Belgian Exclusive Economic Zone")){ return uuidBelgianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("French Exclusive Economic Zone")){ return uuidFrenchExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("English Channel")){ return uuidEnglishChannel;
		}else if (guName.equalsIgnoreCase("Adriatic Sea")){ return uuidAdriaticSea;
		}else if (guName.equalsIgnoreCase("Biscay Bay")){ return uuidBiscayBay;
		}else if (guName.equalsIgnoreCase("Dutch Exclusive Economic Zone")){ return uuidDutchExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("United Kingdom Exclusive Economic Zone")){ return uuidUnitedKingdomExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Spanish Exclusive Economic Zone")){ return uuidSpanishExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Egyptian Exclusive Economic Zone")){ return uuidEgyptianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Grecian Exclusive Economic Zone")){ return uuidGrecianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Tirreno Sea")){ return uuidTirrenoSea;
		}else if (guName.equalsIgnoreCase("Icelandic Exclusive Economic Zone")){ return uuidIcelandicExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Irish Exclusive economic Zone")){ return uuidIrishExclusiveeconomicZone;
		}else if (guName.equalsIgnoreCase("Irish Sea")){ return uuidIrishSea;
		}else if (guName.equalsIgnoreCase("Italian Exclusive Economic Zone")){ return uuidItalianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Norwegian Sea")){ return uuidNorwegianSea;
		}else if (guName.equalsIgnoreCase("Moroccan Exclusive Economic Zone")){ return uuidMoroccanExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Norwegian Exclusive Economic Zone")){ return uuidNorwegianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Skagerrak")){ return uuidSkagerrak;
		}else if (guName.equalsIgnoreCase("Tunisian Exclusive Economic Zone")){ return uuidTunisianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Wadden Sea")){ return uuidWaddenSea;
		}else if (guName.equalsIgnoreCase("Belt Sea")){ return uuidBeltSea;
		}else if (guName.equalsIgnoreCase("Marmara Sea")){ return uuidMarmaraSea;
		}else if (guName.equalsIgnoreCase("Sea of Azov")){ return uuidSeaofAzov;
		}else if (guName.equalsIgnoreCase("Aegean Sea")){ return uuidAegeanSea;
		}else if (guName.equalsIgnoreCase("Bulgarian Exclusive Economic Zone")){ return uuidBulgarianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("South Baltic proper")){ return uuidSouthBalticproper;
		}else if (guName.equalsIgnoreCase("Baltic Proper")){ return uuidBalticProper;
		}else if (guName.equalsIgnoreCase("North Baltic proper")){ return uuidNorthBalticproper;
		}else if (guName.equalsIgnoreCase("Archipelago Sea")){ return uuidArchipelagoSea;
		}else if (guName.equalsIgnoreCase("Bothnian Sea")){ return uuidBothnianSea;
		}else if (guName.equalsIgnoreCase("German Exclusive Economic Zone")){ return uuidGermanExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Swedish Exclusive Economic Zone")){ return uuidSwedishExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Ukrainian Exclusive Economic Zone")){ return uuidUkrainianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Madeiran Exclusive Economic Zone")){ return uuidMadeiranExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Lebanese Exclusive Economic Zone")){ return uuidLebaneseExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Spanish Exclusive Economic Zone [Mediterranean part]")){ return uuidSpanishExclusiveEconomicZoneMediterraneanpart;
		}else if (guName.equalsIgnoreCase("Estonian Exclusive Economic Zone")){ return uuidEstonianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Croatian Exclusive Economic Zone")){ return uuidCroatianExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Balear Sea")){ return uuidBalearSea;
		}else if (guName.equalsIgnoreCase("Turkish Exclusive Economic Zone")){ return uuidTurkishExclusiveEconomicZone;
		}else if (guName.equalsIgnoreCase("Danish Exclusive Economic Zone")){ return uuidDanishExclusiveEconomicZone;
		}else{
			throw new IllegalArgumentException("Unknown area " + guName);
		}
		
	}

	
	public static UUID uuidFromGuId(Integer guId){
		if (guId == null){return null;
		}else if (guId == 7788){ return uuidEuropeanMarineWaters;
		}else if (guId == 7789){ return uuidMediterraneanSea;
		}else if (guId == 7791){ return uuidWhiteSea;
		}else if (guId == 7792){ return uuidNorthSea;
		}else if (guId == 7793){ return uuidBalticSea;
		}else if (guId == 7794){ return uuidBlackSea;
		}else if (guId == 7795){ return uuidBarentsSea;
		}else if (guId == 7796){ return uuidCaspianSea;
		}else if (guId == 7799){ return uuidPortugueseExclusiveEconomicZone;
		}else if (guId == 7802){ return uuidBelgianExclusiveEconomicZone;
		}else if (guId == 7805){ return uuidFrenchExclusiveEconomicZone;
		}else if (guId == 7818){ return uuidEnglishChannel;
		}else if (guId == 7821){ return uuidAdriaticSea;
		}else if (guId == 7831){ return uuidBiscayBay;
		}else if (guId == 7839){ return uuidDutchExclusiveEconomicZone;
		}else if (guId == 7862){ return uuidUnitedKingdomExclusiveEconomicZone;
		}else if (guId == 7869){ return uuidSpanishExclusiveEconomicZone;
		}else if (guId == 7902){ return uuidEgyptianExclusiveEconomicZone;
		}else if (guId == 7939){ return uuidGrecianExclusiveEconomicZone;
		}else if (guId == 7946){ return uuidTirrenoSea;
		}else if (guId == 7964){ return uuidIcelandicExclusiveEconomicZone;
		}else if (guId == 7974){ return uuidIrishExclusiveeconomicZone;
		}else if (guId == 7975){ return uuidIrishSea;
		}else if (guId == 7978){ return uuidItalianExclusiveEconomicZone;
		}else if (guId == 7980){ return uuidNorwegianSea;
		}else if (guId == 8027){ return uuidMoroccanExclusiveEconomicZone;
		}else if (guId == 8050){ return uuidNorwegianExclusiveEconomicZone;
		}else if (guId == 8072){ return uuidSkagerrak;
		}else if (guId == 8143){ return uuidTunisianExclusiveEconomicZone;
		}else if (guId == 8155){ return uuidWaddenSea;
		}else if (guId == 8203){ return uuidBeltSea;
		}else if (guId == 8205){ return uuidMarmaraSea;
		}else if (guId == 8837){ return uuidSeaofAzov;
		}else if (guId == 9146){ return uuidAegeanSea;
		}else if (guId == 9178){ return uuidBulgarianExclusiveEconomicZone;
		}else if (guId == 9903){ return uuidSouthBalticproper;
		}else if (guId == 9904){ return uuidBalticProper;
		}else if (guId == 9905){ return uuidNorthBalticproper;
		}else if (guId == 9908){ return uuidArchipelagoSea;
		}else if (guId == 9909){ return uuidBothnianSea;
		}else if (guId == 10515){ return uuidGermanExclusiveEconomicZone;
		}else if (guId == 10528){ return uuidSwedishExclusiveEconomicZone;
		}else if (guId == 10529){ return uuidUkrainianExclusiveEconomicZone;
		}else if (guId == 10564){ return uuidMadeiranExclusiveEconomicZone;
		}else if (guId == 10574){ return uuidLebaneseExclusiveEconomicZone;
		}else if (guId == 10659){ return uuidSpanishExclusiveEconomicZoneMediterraneanpart;
		}else if (guId == 10708){ return uuidEstonianExclusiveEconomicZone;
		}else if (guId == 10778){ return uuidCroatianExclusiveEconomicZone;
		}else if (guId == 10779){ return uuidBalearSea;
		}else if (guId == 10782){ return uuidTurkishExclusiveEconomicZone;
		}else if (guId == 11039){ return uuidDanishExclusiveEconomicZone;
		
		}else{
			throw new IllegalArgumentException("Unknown area id " + guId);
		}

	}
	
	public static Feature noteType2Feature(String type){
		if (CdmUtils.isEmpty(type)){return null;
		}else if (type.equals("Remark")){return Feature.UNKNOWN();
		}else if (type.equals("Additional information")){return Feature.UNKNOWN();
		}else if (type.equals("Spelling")){return Feature.UNKNOWN();
		}else if (type.equals("Publication date")){return Feature.UNKNOWN();
		}else if (type.equals("Systematics")){return Feature.UNKNOWN();
		}else if (type.equals("Classification")){return Feature.UNKNOWN();
		}else if (type.equals("Environment")){return Feature.UNKNOWN();
		}else if (type.equals("Habitat")){return Feature.UNKNOWN();
		}else if (type.equals("Authority")){return Feature.UNKNOWN();
		}else if (type.equals("Ecology")){return Feature.UNKNOWN();
		}else if (type.equals("Morphology")){return Feature.UNKNOWN();
		}else if (type.equals("Taxonomic Remarks")){return Feature.UNKNOWN();
		}else if (type.equals("NULL")){return Feature.UNKNOWN();
		}else if (type.equals("Distribution")){return Feature.UNKNOWN();
		}else if (type.equals("Note")){return Feature.UNKNOWN();
		}else if (type.equals("Taxonomy")){return Feature.UNKNOWN();
		}else if (type.equals("Taxonomic status")){return Feature.UNKNOWN();
		}else if (type.equals("Status")){return Feature.UNKNOWN();
		}else if (type.equals("Rank")){return Feature.UNKNOWN();
		}else if (type.equals("Homonymy")){return Feature.UNKNOWN();
		}else if (type.equals("Nomenclature")){return Feature.UNKNOWN();
		}else if (type.equals("Type species")){return Feature.UNKNOWN();
		}else if (type.equals("Taxonomic Remark")){return Feature.UNKNOWN();
		}else if (type.equals("Diagnosis")){return Feature.UNKNOWN();
		}else if (type.equals("Date of Publication")){return Feature.UNKNOWN();
		}else if (type.equals("Acknowledgments")){return Feature.UNKNOWN();
		}else if (type.equals("Biology")){return Feature.UNKNOWN();
		}else if (type.equals("Original publication")){return Feature.UNKNOWN();
		}else if (type.equals("Type locality")){return Feature.UNKNOWN();
		}else if (type.equals("Host")){return Feature.UNKNOWN();
		}else if (type.equals("Validity")){return Feature.UNKNOWN();
		}else if (type.equals("Identification")){return Feature.UNKNOWN();
		}else if (type.equals("Synonymy")){return Feature.UNKNOWN();
		}else{
			throw new IllegalArgumentException("Unknown note type " + type);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("Distribution")){return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("Ecology")){return Feature.ECOLOGY();
		}else if (key.equalsIgnoreCase("Diagnosis")){return Feature.DIAGNOSIS();
		}else if (key.equalsIgnoreCase("Biology")){return Feature.BIOLOGY_ECOLOGY();
		}else if (key.equalsIgnoreCase("Host")){return Feature.HOSTPLANT();
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureUuid(java.lang.String)
	 */
	@Override
	public UUID getFeatureUuid(String key)
			throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("Remark")){return uuidRemark;
		}else if (key.equalsIgnoreCase("Additional information")){return uuidAdditionalinformation;
		}else if (key.equalsIgnoreCase("Spelling")){return uuidSpelling;
		}else if (key.equalsIgnoreCase("Publication date")){return uuidPublicationdate;
		}else if (key.equalsIgnoreCase("Systematics")){return uuidSystematics;
		}else if (key.equalsIgnoreCase("Classification")){return uuidClassification;
		}else if (key.equalsIgnoreCase("Environment")){return uuidEnvironment;
		}else if (key.equalsIgnoreCase("Habitat")){return uuidHabitat;
		}else if (key.equalsIgnoreCase("Authority")){return uuidAuthority;
		}else if (key.equalsIgnoreCase("Morphology")){return uuidMorphology;
		}else if (key.equalsIgnoreCase("Taxonomic Remarks")){return uuidTaxonomicRemarks;
		}else if (key.equalsIgnoreCase("Note")){return uuidNote;
		}else if (key.equalsIgnoreCase("Taxonomy")){return uuidTaxonomy;
		}else if (key.equalsIgnoreCase("Taxonomic status")){return uuidTaxonomicstatus;
		}else if (key.equalsIgnoreCase("Status")){return uuidStatus;
		}else if (key.equalsIgnoreCase("Rank")){return uuidRank;
		}else if (key.equalsIgnoreCase("Homonymy")){return uuidHomonymy;
		}else if (key.equalsIgnoreCase("Nomenclature")){return uuidNomenclature;
		}else if (key.equalsIgnoreCase("Type species")){return uuidTypespecies;
		}else if (key.equalsIgnoreCase("Taxonomic Remark")){return uuidTaxonomicRemark;
		}else if (key.equalsIgnoreCase("Date of Publication")){return uuidDateofPublication;
		}else if (key.equalsIgnoreCase("Acknowledgments")){return uuidAcknowledgments;
		}else if (key.equalsIgnoreCase("Original publication")){return uuidOriginalpublication;
		}else if (key.equalsIgnoreCase("Type locality")){return uuidTypelocality;
		}else if (key.equalsIgnoreCase("Validity")){return uuidValidity;
		}else if (key.equalsIgnoreCase("Identification")){return uuidIdentification;
		}else if (key.equalsIgnoreCase("Synonymy")){return uuidSynonymy;
		}else{
			return null;
		}
	}
	
	
	
}
