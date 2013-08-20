/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.redlist.bfnXml;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.ResultWrapper;
import eu.etaxonomy.cdm.model.common.RelationshipTermBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
/*import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.PersonalCommunication;
import eu.etaxonomy.cdm.model.reference.PrintSeries;*/
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
//import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

public final class BfnXmlTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BfnXmlTransformer.class);

	//redList state Vocabularies
	public static final UUID vocStateRLKat =  UUID.fromString("66bbb391-af8a-423b-9506-a235b61af922");
	public static final UUID vocStateRlKatDiff =  UUID.fromString("abe3702e-ddd4-420c-a985-08a0f8138215");
	public static final UUID vocStateRlAkt =  UUID.fromString("a0bb10de-35c1-47f5-b700-02ceb0a6b50c");
	public static final UUID vocStateRLLang =  UUID.fromString("c48d99db-50b6-469f-879d-8bb781842382");
	public static final UUID vocStateRLKurz =  UUID.fromString("46549c3b-d9d0-4d34-9135-4789d5ed6fff");
	public static final UUID vocStateRLRisk =  UUID.fromString("ec38db54-07dd-4e48-8976-bfa4813ffa44");
	public static final UUID vocStateRLResp =  UUID.fromString("c4763d33-75ea-4387-991f-b767650b4899");
	public static final UUID vocStateRLKatOld =  UUID.fromString("e9be0626-e14e-4556-a8af-9d49e6279669");
	public static final UUID vocStateRLNeo =  UUID.fromString("6c55ae1d-046d-4b67-89aa-b24c4888df6a");
	public static final UUID vocStateRLKatId =  UUID.fromString("c54481b3-bf07-43ce-b1cb-09759e4d2a70");

	//redlist feature
	public static final UUID featureRLKat =  UUID.fromString("744f8833-619a-4d83-b330-1997c3b2c2f9");
	public static final UUID featureRlKatDiff =  UUID.fromString("bf93361d-0c8c-4961-9f60-20bcb1d3dbaf");
	public static final UUID featureRlAkt =  UUID.fromString("39b6962b-05ba-4cd6-a1a9-337d5d156e2f");
	public static final UUID featureRLLang =  UUID.fromString("f6027318-b17d-49e6-b8eb-7464304044c8");
	public static final UUID featureRLKurz =  UUID.fromString("9ecc65b5-7760-4ce7-add0-950bdcc2c792");
	public static final UUID featureRLRisk =  UUID.fromString("2c8f8ffa-c604-4385-b428-4485f5650735");
	public static final UUID featureRLResp =  UUID.fromString("02d8010f-7d1b-46a3-8c01-b5e6760bfd14");
	public static final UUID featureRLKatOld =  UUID.fromString("bbdff68d-4fa0-438d-afb5-cff89791c93f");
	public static final UUID featureRLNeo =  UUID.fromString("153c7173-6d3d-4bee-b8f2-cf8e63e0bc25");
	public static final UUID featureRLKatId =  UUID.fromString("dc9f5dd2-302c-4a32-bd70-278bbd9abd16");
	public static final UUID featureRLTaxComment =  UUID.fromString("b7c05d78-16a4-4b6e-a03b-fa6bb2ed74ae");
	public static final UUID featureRLHazardComment =  UUID.fromString("5beb1ebf-8643-4d5f-9849-8087c35455bb");
	

	//rl kat state list
	public static final UUID stateTermRlKat0 = UUID.fromString("05ff7c0f-2fb2-4c10-9527-a2e0c68d68af");
	public static final UUID stateTermRlKat1 = UUID.fromString("76a6defc-41d0-43bf-a15a-997caeefbbce");
	public static final UUID stateTermRlKat2 = UUID.fromString("ee6b79b6-8306-42d1-a80a-2963ded7c952");
	public static final UUID stateTermRlKat3 = UUID.fromString("309bf199-c0a3-4f01-829a-b10aafda4547");
	public static final UUID stateTermRlKatG = UUID.fromString("fdf9c84e-1b76-4aa8-b676-a614591ad320");
	public static final UUID stateTermRlKatR = UUID.fromString("a694e7bd-87a4-4d3c-8333-aed5092bcb0e");
	public static final UUID stateTermRlKatV = UUID.fromString("b1a6695d-65f9-4c53-9765-fd7b54e1674c");
	public static final UUID stateTermRlKatStar = UUID.fromString("1cda0ef4-cace-42e9-8061-4ada41d03974");
	public static final UUID stateTermRlKatStar2 = UUID.fromString("539cffb0-29b9-48fd-af6e-abf9c466199c");
	public static final UUID stateTermRlKatD = UUID.fromString("4d61cadd-b27e-41da-9c91-f29e96adaf89");
	public static final UUID stateTermRlKatDiamond = UUID.fromString("aedc4006-4097-41cd-bab9-f8607ff84519");
	public static final UUID stateTermRlKatKN = UUID.fromString("49dc7656-4cef-4b0e-81dd-8422a3d0d06b");
	
	//rl kat diff state list
	public static final UUID stateTermRLKatDiffPlus = UUID.fromString("6bc7ddc2-6f25-4076-a392-2626cb7a4b35");
	public static final UUID stateTermRLKatDiffMinus = UUID.fromString("8f6a8c16-195c-4084-a201-8d702f9636e7");
	public static final UUID stateTermRLKatDiffEqual = UUID.fromString("5215fd8a-7e70-43a6-abde-4e14966a0e0e");
	
	//rl kat neo state list
	public static final UUID stateTermRLKatNeo = UUID.fromString("d9ae3dc2-99c9-40aa-b724-9810ed52ca15");
	
	//rl kat akt state list
	public static final UUID stateTermRLKatAktEx = UUID.fromString("a36d4251-0ca1-4818-bbf7-4089a9362a7e");
	public static final UUID stateTermRLKatAktEs = UUID.fromString("9d2426a2-d845-47df-9607-01addc4e3253");
	public static final UUID stateTermRLKatAktSs = UUID.fromString("bde09fdd-459a-4f8e-a83c-ee562e220f52");
	public static final UUID stateTermRLKatAktS = UUID.fromString("3f44fbd3-6d02-4cef-a2c7-c29684b4eb20");
	public static final UUID stateTermRLKatAktMh = UUID.fromString("88e3cfff-623a-43b0-a708-e4d7125a504c");
	public static final UUID stateTermRLKatAktH = UUID.fromString("ec96d3f0-0f32-4121-9636-41c44079c9ea");
	public static final UUID stateTermRLKatAktSh = UUID.fromString("1126e1ad-5c06-43b2-bfd5-8327257a41eb");
	public static final UUID stateTermRLKatAktQuest = UUID.fromString("2ee9820e-c98d-4d5a-8621-5d7b73be66c2");
	public static final UUID stateTermRLKatAktNb = UUID.fromString("5da81f91-0089-4360-b07c-b3b833f8fc8e");
	public static final UUID stateTermRLKatAktKn = UUID.fromString("f10865f9-aa13-4cf0-9e6c-cc657103bd13");
	
	//rl kat lang state list
	public static final UUID stateTermRLKatLangLT3 = UUID.fromString("6d23b5f9-ac18-4ecb-9be8-2c6e5e7db736");
	public static final UUID stateTermRLKatLangLT2 = UUID.fromString("1bd75728-79ed-427d-b96e-858ddca6103d");
	public static final UUID stateTermRLKatLangLT1 = UUID.fromString("b7c592a4-72cd-4914-87f0-05a6b324af43");
	public static final UUID stateTermRLKatLangLT = UUID.fromString("5f202b93-6f20-4bae-ba3c-e2792b5451b4");
	public static final UUID stateTermRLKatLangEqual = UUID.fromString("d66be068-4a0c-4f95-aa6e-9e5804ceb1f1");
	public static final UUID stateTermRLKatLangGT = UUID.fromString("528185c6-3c12-41bd-a1e5-6ee3d729776c");
	public static final UUID stateTermRLKatLangQuest = UUID.fromString("53076429-d4ac-427f-a9dc-2c8a15901999");
	
	//rl kat kurz state list
	public static final UUID stateTermRLKatKurzDown3 = UUID.fromString("dd97697c-004a-4860-a553-67695d32a992");
	public static final UUID stateTermRLKatKurzDown2 = UUID.fromString("311a531b-8263-4c72-af79-662ffbc26fbe");
	public static final UUID stateTermRLKatKurzDown1 = UUID.fromString("d2a2a51f-5c8f-4cef-809a-58162beae5c2");
	public static final UUID stateTermRLKatKurzEqual = UUID.fromString("0a8cf4c0-8b7c-49d5-9195-0999a0f202ad");
	public static final UUID stateTermRLKatKurzUp = UUID.fromString("13e7c95c-3ca3-435d-b7b6-4889e594bf2a");
	public static final UUID stateTermRLKatKurzQuest = UUID.fromString("4eb11517-a874-484d-8390-dbb8c6bda47c");
	
	//rl kat risk state list
	public static final UUID stateTermRLKatRiskMinus = UUID.fromString("4e96c671-e1f7-4273-83e7-6650207b57e0");
	public static final UUID stateTermRLKatRiskEqual = UUID.fromString("d3f00d31-26a4-40c0-99d4-55ea3672ff5d");
	
	//rl kat resp state list
	public static final UUID stateTermRLKatRespBang2 = UUID.fromString("d1e6b6cd-bb19-40a3-9d02-33099295e7f7");
	public static final UUID stateTermRLKatRespBang1 = UUID.fromString("01856904-aced-4889-b955-d16872bcd0e8");
	public static final UUID stateTermRLKatRespBang = UUID.fromString("c432e39e-ec09-41b7-be9b-28e0d76a4cf9");
	public static final UUID stateTermRLKatRespQuest = UUID.fromString("4116fbda-a392-417c-be1c-08f5e72f762b");
	public static final UUID stateTermRLKatRespNb = UUID.fromString("800328ce-f618-4de1-9237-243f16fbe9f9");
	
	//rl Kat old state list
	public static final UUID stateTermRLKatOld0 = UUID.fromString("1977b29c-8b63-407a-a11b-ada8726ac653");
	public static final UUID stateTermRLKatOld1 = UUID.fromString("4f3d3255-7e06-4a4e-83d1-5841beee85d4");
	public static final UUID stateTermRLKatOld2 = UUID.fromString("65163104-76db-4c20-a00f-5c7531f42b3b");
	public static final UUID stateTermRLKatOld3 = UUID.fromString("3c0fbb9e-b3fa-4bab-ada2-6efb9b6b9155");
	public static final UUID stateTermRLKatOldG = UUID.fromString("a3d2daf2-a570-40d7-b08d-d105a30bb5e2");
	public static final UUID stateTermRLKatOldR = UUID.fromString("d6e511cb-40aa-48ab-8a0d-2365c984d698");
	public static final UUID stateTermRLKatOldV = UUID.fromString("92a6f1ec-6d61-4879-826a-255c34492507");
	public static final UUID stateTermRLKatOldStar = UUID.fromString("196310bc-3b8b-43c9-b317-e2b02bff5c8a");
	public static final UUID stateTermRLKatOldStar2 = UUID.fromString("7fbc42ed-43b6-4198-a9dc-bb97bacb3b9b");
	public static final UUID stateTermRLKatOldD = UUID.fromString("075a456c-2291-436c-9b9c-b06d95bf6fc6");
	public static final UUID stateTermRLKatOldNb = UUID.fromString("72faec78-6db9-4471-9a65-c6d2337bd324");
	public static final UUID stateTermRLKatOldKn = UUID.fromString("92276f3e-3c09-4761-ba5b-b49697c6d5ce");
	
	public static final UUID stateTermEmpty = UUID.fromString("1d357340-5329-4f43-a454-7f99625a1d71");
	
	
	/** Creates an cdm-Rank by the tcs rank
	 */
	public static Rank rankCode2Rank (String strRank) throws UnknownCdmTypeException{
		if (strRank == null){return null;
		//genus group
		}else if (strRank.equals("infragen")){return Rank.INFRAGENUS();
		}else if (strRank.equals("subgen")){return Rank.SUBGENUS();
		}else if (strRank.equals("gen")){return Rank.GENUS();
		//genus subdivision
		//TODO 
		}else if (strRank.equals("aggr")){return Rank.SPECIESAGGREGATE();
		}else if (strRank.equals("agg.")){return Rank.SPECIESAGGREGATE();
		}else if (strRank.equals("taxinfragen")){return Rank.INFRAGENERICTAXON();
		}else if (strRank.equals("subser")){return Rank.SUBSERIES();
		}else if (strRank.equals("ser")){return Rank.SERIES();
		}else if (strRank.equals("subsect")){return Rank.SUBSECTION_BOTANY();
		}else if (strRank.equals("sect")){return Rank.SECTION_BOTANY();
		//species group
		}else if (strRank.equals("subsp_aggr")){return Rank.SUBSPECIFICAGGREGATE();
		}else if (strRank.equals("ssp")){return Rank.SUBSPECIES();
		}else if (strRank.equals("sp")){return Rank.SPECIES();
		}else if (strRank.equals("spezies")){return Rank.SPECIES();
		//below subspecies
		}else if (strRank.equals("cand")){return Rank.CANDIDATE();
		}else if (strRank.equals("taxinfrasp")){return Rank.INFRASPECIFICTAXON();
		}else if (strRank.equals("fsp")){return Rank.SPECIALFORM();
		}else if (strRank.equals("subsubfm")){return Rank.SUBSUBFORM();
		}else if (strRank.equals("subfm")){return Rank.SUBFORM();
		}else if (strRank.equals("fm")){return Rank.FORM();
		}else if (strRank.equals("subsubvar")){return Rank.SUBSUBVARIETY();
		}else if (strRank.equals("subvar")){return Rank.SUBVARIETY();
		}else if (strRank.equals("var")){return Rank.VARIETY();
		}else if (strRank.equals("var.")){return Rank.VARIETY();
		//TODO -> see documentation, Bacteria status
//		}else if (strRank.equals("pv")){return Rank;
//		}else if (strRank.equals("bv")){return Rank.;
		}else if (strRank.equals("infrasp")){return Rank.INFRASPECIES();
		//above superfamily
		}else if (strRank.equals("infraord")){return Rank.INFRAORDER();
		}else if (strRank.equals("ord")){return Rank.ORDER();
		}else if (strRank.equals("superord")){return Rank.SUPERORDER();
		}else if (strRank.equals("infracl")){return Rank.INFRACLASS();
		}else if (strRank.equals("subcl")){return Rank.SUBCLASS();
		}else if (strRank.equals("cl")){return Rank.CLASS();
		}else if (strRank.equals("supercl")){return Rank.SUPERCLASS();
		}else if (strRank.equals("infraphyl_div")){return Rank.INFRAPHYLUM();
		}else if (strRank.equals("subphyl_div")){return Rank.SUBPHYLUM();
		}else if (strRank.equals("phyl_div")){return Rank.PHYLUM();
		}else if (strRank.equals("superphyl_div")){return Rank.SUPERPHYLUM();
		}else if (strRank.equals("infrareg")){return Rank.INFRAKINGDOM();
		}else if (strRank.equals("subreg")){return Rank.SUBKINGDOM();
		}else if (strRank.equals("reg")){return Rank.KINGDOM();
		}else if (strRank.equals("superreg")){return Rank.SUPERKINGDOM();
		}else if (strRank.equals("dom")){return Rank.DOMAIN();
		}else if (strRank.equals("taxsupragen")){return Rank.SUPRAGENERICTAXON();
		//family group
		}else if (strRank.equals("infrafam")){return Rank.FAMILY();
		}else if (strRank.equals("subfam")){return Rank.FAMILY();
		}else if (strRank.equals("fam")){return Rank.FAMILY();
		}else if (strRank.equals("superfam")){return Rank.FAMILY();
		//family subdivision
		}else if (strRank.equals("intratrib")){return Rank.FAMILY();
		}else if (strRank.equals("subtrib")){return Rank.FAMILY();
		}else if (strRank.equals("trib")){return Rank.FAMILY();
		}else if (strRank.equals("supertrib")){return Rank.FAMILY();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Rank " + strRank);
		}
	}

	public static String redListString2RedListCode (String redListCode) throws UnknownCdmTypeException{
		if (redListCode == null){ return null;
		}else if (redListCode.equals("#dtpl_RLKat_NULL#")){return "0";
		}else if (redListCode.equals("#dtpl_RLKat_EINS#")){return "1";
		}else if (redListCode.equals("#dtpl_RLKat_ZWEI#")){return "2"; 
		}else if (redListCode.equals("#dtpl_RLKat_DREI#")){return "3"; 
		}else if (redListCode.equals("#dtpl_RLKat_VIER#")){return "4"; 
		}else if (redListCode.equals("#dtpl_RLKat_G#")){return "G"; 
		}else if (redListCode.equals("#dtpl_RLKat_R#")){return "R"; 
		}else if (redListCode.equals("#dtpl_RLKat_V#")){return "V"; 
		}else if (redListCode.equals("#dtpl_RLKat_STERN#")){return "*";
		}else if (redListCode.equals("#dtpl_RLKat_STERN##dtpl_RLKat_STERN#")){return "**";
		}else if (redListCode.equals("#dtpl_RLKat_D#")){return "D";
		}else if (redListCode.equals("#dtpl_RLKat_RAUTE#")){
			char c = 0x2666;
			return String.valueOf(c);
		}else if (redListCode.equals("kN")){return "kN";
		}else if (redListCode.equals("+")){return "+";
		}else if (redListCode.equals("-")){return "-";
		}else if (redListCode.equals("=")){return "=";
		}else if (redListCode.equals("N")){return "N";
		}else if (redListCode.equals("#dtpl_KurzfBest_RUNTER##dtpl_KurzfBest_RUNTER##dtpl_KurzfBest_RUNTER#")){
			char c = 0x2193;
			return String.valueOf(c)+String.valueOf(c)+String.valueOf(c);
		}else if (redListCode.equals("#dtpl_KurzfBest_RUNTER##dtpl_KurzfBest_RUNTER#")){
			char c = 0x2193;
			return String.valueOf(c)+String.valueOf(c);
		}else if (redListCode.equals("(#dtpl_KurzfBest_RUNTER#)")){
			char c = 0x2193;
			return "("+String.valueOf(c)+")";
		}else if (redListCode.equals("#dtpl_KurzfBest_HOCH#")){
			char c = 0x2191;
			return String.valueOf(c);
		}else if (redListCode.equals("#dtpl_Risiko_MINUS#")){return "-";
		}else if (redListCode.equals("#dtpl_VERANTW_NB#")){return "nb";
		}else if (redListCode.equals("#dtpl_TaxBez_GLEICH#")){return "=";
		}else if (redListCode.equals("#dtpl_TaxBez_KLEINER#")){return "<";
		}else if (redListCode.equals("#dtpl_TaxBez_GROESSER#")){return ">";
		}else if (redListCode.equals("#dtpl_TaxBez_UNGLEICH#")){
			char c = 0x2260;
			return String.valueOf(c);
		}else if (redListCode.equals("#dtpl_AlteRLKat_STERN#")){return "*";
		}else if (redListCode.equals("#dtpl_AlteRLKat_ZWEISTERN#")){return "**";
		}else if (redListCode.equals("#dtpl_AlteRLKat_NB#")){return "nb";
		}else if (redListCode.equals("#dtpl_AlteRLKat_KN#")){return "kN";
		}else if (redListCode.equals("#dtpl_TaxBez_UNGLEICH#")){return "-";
		}else if (StringUtils.isBlank(redListCode)){return "keine Angabe";
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Redlist Code " + redListCode);
		}
	}
	
	public static NomenclaturalCode nomCodeString2NomCode (String nomCode) throws UnknownCdmTypeException{
		if (nomCode != null){
			nomCode = nomCode.trim();
		}
		if (nomCode == null){ return null;
		}else if (nomCode.equals("Botanical")){return NomenclaturalCode.ICBN;
		}else if (nomCode.equals("Zoological")){return NomenclaturalCode.ICZN;
		}else if (nomCode.equals("Viral")){return NomenclaturalCode.ICVCN;
		}else if (nomCode.equals("Bacteriological")){return NomenclaturalCode.ICNB;
		}else if (nomCode.equals("CultivatedPlant")){return NomenclaturalCode.ICNCP;
		//TODO code Indeterminate
//		}else if (nomCode.equals("Indeterminate")){return NomenclaturalCode.XXX();
		}	
		else {
			throw new UnknownCdmTypeException("Unknown Nomenclatural Code " + nomCode);
		}
	}
	public static UUID getRedlistVocabularyUUID(String redListVocabulary) throws UnknownCdmTypeException {

		if(redListVocabulary.equalsIgnoreCase("RL Kat.")) return vocStateRLKat;
		if(redListVocabulary.equalsIgnoreCase("Kat. +/-")) return vocStateRlKatDiff;
		if(redListVocabulary.equalsIgnoreCase("aktuelle Bestandsstituation")) return vocStateRlAkt;
		if(redListVocabulary.equalsIgnoreCase("langfristiger Bestandstrend")) return vocStateRLLang;
		if(redListVocabulary.equalsIgnoreCase("kurzfristiger Bestandstrend")) return vocStateRLKurz;
		if(redListVocabulary.equalsIgnoreCase("Risikofaktoren")) return vocStateRLRisk;
		if(redListVocabulary.equalsIgnoreCase("Verantwortlichkeit")) return vocStateRLResp;
		if(redListVocabulary.equalsIgnoreCase("alte RL- Kat.")) return vocStateRLKatOld;
		if(redListVocabulary.equalsIgnoreCase("Neobiota")) return vocStateRLNeo;
		if(redListVocabulary.equalsIgnoreCase("Eindeutiger Code")) return vocStateRLKatId;
		else{
			throw new UnknownCdmTypeException("Unknown feature, could not match: " + redListVocabulary);
		}

	}


	public static UUID getRedlistFeatureUUID(String redListFeature) throws UnknownCdmTypeException {

		if(redListFeature.equalsIgnoreCase("RL Kat.")) return featureRLKat;
		if(redListFeature.equalsIgnoreCase("Kat. +/-")) return featureRlKatDiff;
		if(redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return featureRlAkt;
		if(redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return featureRLLang;
		if(redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return featureRLKurz;
		if(redListFeature.equalsIgnoreCase("Risikofaktoren")) return featureRLRisk;
		if(redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return featureRLResp;
		if(redListFeature.equalsIgnoreCase("alte RL- Kat.")) return featureRLKatOld;
		if(redListFeature.equalsIgnoreCase("Neobiota")) return featureRLNeo;
		if(redListFeature.equalsIgnoreCase("Eindeutiger Code")) return featureRLKatId;
		if(redListFeature.equalsIgnoreCase("Kommentar zur Taxonomie")) return featureRLTaxComment;
		if(redListFeature.equalsIgnoreCase("Kommentar zur Gefährdung")) return featureRLHazardComment;
		else{
			throw new UnknownCdmTypeException("Unknown feature, could not match: " + redListFeature);
		}

	}

	public static UUID getRedlistStateTermUUID(String redListStateTerm, String redListFeature) throws UnknownCdmTypeException {
		//RL Kat
		char a = 0x2666;
		if(redListStateTerm.equalsIgnoreCase("0") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKat0;
		if(redListStateTerm.equalsIgnoreCase("1") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKat1;
		if(redListStateTerm.equalsIgnoreCase("2") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKat2;
		if(redListStateTerm.equalsIgnoreCase("3") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKat3;
		if(redListStateTerm.equalsIgnoreCase("G") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatG;
		if(redListStateTerm.equalsIgnoreCase("R") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatR;
		if(redListStateTerm.equalsIgnoreCase("V") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatV;
		if(redListStateTerm.equalsIgnoreCase("*") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatStar;
		if(redListStateTerm.equalsIgnoreCase("**") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatStar2;
		if(redListStateTerm.equalsIgnoreCase("D") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatD;
		if(redListStateTerm.equalsIgnoreCase(String.valueOf(a)) && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatDiamond;
		if(redListStateTerm.equalsIgnoreCase("kN") && redListFeature.equalsIgnoreCase("RL Kat.")) return stateTermRlKatKN;
		
		//RL Diff
		if(redListStateTerm.equalsIgnoreCase("+") && redListFeature.equalsIgnoreCase("Kat. +/-")) return stateTermRLKatDiffPlus;
		if(redListStateTerm.equalsIgnoreCase("-") && redListFeature.equalsIgnoreCase("Kat. +/-")) return stateTermRLKatDiffMinus;
		if(redListStateTerm.equalsIgnoreCase("=") && redListFeature.equalsIgnoreCase("Kat. +/-")) return stateTermRLKatDiffEqual;
		
		//Rl Akt
		if(redListStateTerm.equalsIgnoreCase("ex") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktEx;
		if(redListStateTerm.equalsIgnoreCase("es") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktEs;
		if(redListStateTerm.equalsIgnoreCase("ss") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktSs;
		if(redListStateTerm.equalsIgnoreCase("s") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktS;
		if(redListStateTerm.equalsIgnoreCase("mh") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktMh;
		if(redListStateTerm.equalsIgnoreCase("h") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktH;
		if(redListStateTerm.equalsIgnoreCase("sh") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktSh;
		if(redListStateTerm.equalsIgnoreCase("?") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktQuest;
		if(redListStateTerm.equalsIgnoreCase("nb") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktNb;
		if(redListStateTerm.equalsIgnoreCase("kN") && redListFeature.equalsIgnoreCase("aktuelle Bestandsstituation")) return stateTermRLKatAktKn;
		
		//RL Lang
		if(redListStateTerm.equalsIgnoreCase("<<<") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangLT3;
		if(redListStateTerm.equalsIgnoreCase("<<") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangLT2;
		if(redListStateTerm.equalsIgnoreCase("<") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangLT1;
		if(redListStateTerm.equalsIgnoreCase("(<)") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangLT;
		if(redListStateTerm.equalsIgnoreCase("=") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangEqual;
		if(redListStateTerm.equalsIgnoreCase(">") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangGT;
		if(redListStateTerm.equalsIgnoreCase("?") && redListFeature.equalsIgnoreCase("langfristiger Bestandstrend")) return stateTermRLKatLangQuest;
		
		//RL Kurz
		char c = 0x2193;
		char b = 0x2191;
		if(redListStateTerm.equalsIgnoreCase(String.valueOf(c)+String.valueOf(c)+String.valueOf(c)) && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzDown3;
		if(redListStateTerm.equalsIgnoreCase(String.valueOf(c)+String.valueOf(c)) && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzDown2;
		if(redListStateTerm.equalsIgnoreCase("("+String.valueOf(c)+")") && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzDown1;
		if(redListStateTerm.equalsIgnoreCase("=") && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzEqual;
		if(redListStateTerm.equalsIgnoreCase(String.valueOf(b)) && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzUp;
		if(redListStateTerm.equalsIgnoreCase("?") && redListFeature.equalsIgnoreCase("kurzfristiger Bestandstrend")) return stateTermRLKatKurzQuest;
		
		//RL Risk
		if(redListStateTerm.equalsIgnoreCase("-") && redListFeature.equalsIgnoreCase("Risikofaktoren")) return stateTermRLKatRiskMinus;
		if(redListStateTerm.equalsIgnoreCase("=") && redListFeature.equalsIgnoreCase("Risikofaktoren")) return stateTermRLKatRiskEqual;
		
		//RL Resp
		if(redListStateTerm.equalsIgnoreCase("!!") && redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return stateTermRLKatRespBang2;
		if(redListStateTerm.equalsIgnoreCase("!") && redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return stateTermRLKatRespBang1;
		if(redListStateTerm.equalsIgnoreCase("(!)") && redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return stateTermRLKatRespBang;
		if(redListStateTerm.equalsIgnoreCase("?") && redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return stateTermRLKatRespQuest;
		if(redListStateTerm.equalsIgnoreCase("nb") && redListFeature.equalsIgnoreCase("Verantwortlichkeit")) return stateTermRLKatRespNb;
		
		//RL Kat Old
		if(redListStateTerm.equalsIgnoreCase("0") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOld0;
		if(redListStateTerm.equalsIgnoreCase("1") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOld1;
		if(redListStateTerm.equalsIgnoreCase("2") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOld2;
		if(redListStateTerm.equalsIgnoreCase("3") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOld3;
		if(redListStateTerm.equalsIgnoreCase("G") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldG;
		if(redListStateTerm.equalsIgnoreCase("R") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldR;
		if(redListStateTerm.equalsIgnoreCase("V") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldV;
		if(redListStateTerm.equalsIgnoreCase("*") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldStar;
		if(redListStateTerm.equalsIgnoreCase("**") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldStar2;
		if(redListStateTerm.equalsIgnoreCase("D") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldD;
		if(redListStateTerm.equalsIgnoreCase("nb") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldNb;
		if(redListStateTerm.equalsIgnoreCase("kN") && redListFeature.equalsIgnoreCase("alte RL- Kat.")) return stateTermRLKatOldKn;
		
		//RL Neo
		if(redListStateTerm.equalsIgnoreCase("N") && redListFeature.equalsIgnoreCase("Neobiota")) return stateTermRLKatNeo;
		
		
		//RL Empty
		if(StringUtils.isBlank(redListStateTerm) || redListStateTerm.equalsIgnoreCase("keine Angabe")) return stateTermEmpty; 
		
		else
			throw new UnknownCdmTypeException("Unknown State, could not match: " + redListStateTerm +"\n In Category: " + redListFeature);
	
	}
	
}
