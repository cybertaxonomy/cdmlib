// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class CentralAfricaFernsTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsTransformer.class);
	
	//Extensions
	public static UUID uuidFascicle = UUID.fromString("718bb2d6-dbcd-4e60-9cfa-154a59461ef4");
	public static UUID uuidNomenclaturalRemarks = UUID.fromString("ea7ceb5c-1b31-47b2-8544-89c90aed3a44");
	public static UUID uuidNamePublishedAs = UUID.fromString("8eb258aa-6342-49cd-ad7b-185a3e879031");
	public static UUID uuidIllustrationsNonOriginal = UUID.fromString("b0beb324-76a6-4f76-9e0c-413383782329");
		
	
	//NamedAreas
	public static UUID uuidZanzibar = UUID.fromString("8d7dc5e2-43a6-4e7d-af29-a43cf62ca212");
	public static UUID uuidAnnobon = UUID.fromString("a90c534e-d079-4525-82be-6a35c111f0ab");
	public static UUID uuidDiegoGarcia = UUID.fromString("62341ff2-516e-4e4f-951e-53d9a7acaf17");
	public static UUID uuidPemba = UUID.fromString("21230ed0-1a06-4a37-ae6e-55d67bd946c3");
	public static UUID uuidStoAntao = UUID.fromString("9bd9cb1f-43e3-4b90-8cf8-fa3439b51fe2");
	public static UUID uuidSaoVicente = UUID.fromString("77cd4a69-c876-49e8-8227-a12972bf1184");
	public static UUID uuidSaoNicolau = UUID.fromString("634e5d6a-11b3-4da6-b73a-bf9de90ea589");
	public static UUID uuidFogo = UUID.fromString("6f04c3fe-a226-4071-b945-ac87c96e1c4b");
	public static UUID uuidBrava = UUID.fromString("19430d50-c2ee-4a78-a7b3-a7c643741475");
	public static UUID uuidGrandeComore = UUID.fromString("20777d55-2370-4741-a6f9-077a3a5ddb1e");
	public static UUID uuidGough = UUID.fromString("e139d74f-27dd-4e0a-a927-fcb8c5308325");
	public static UUID uuidMiddleIslandTristanDaCunha = UUID.fromString("2d228eda-d1ad-405b-8f3a-a7c471cbd791");
	public static UUID uuidCerfs = UUID.fromString("267cf857-ef09-47d9-9779-a8b71166aa7f");
	public static UUID uuidSoqotra = UUID.fromString("3734f10e-c2f1-426e-9c6b-488b468240b9");
	public static UUID uuidJohannaIsl = UUID.fromString("852a369f-c501-4451-80ed-360ff8253184");
	public static UUID uuidTenerife = UUID.fromString("73a9a469-1e5a-4781-9121-d307e1379c10");
	public static UUID uuidGranCanaria = UUID.fromString("8f1e7b58-c459-47d4-9899-dff9eac8edc1");
	public static UUID uuidLanzarote = UUID.fromString("5490408a-e32e-4f4e-b1d9-62851b218e8e");
	public static UUID uuidFuerteventura = UUID.fromString("da4ad5f0-646f-4052-b313-a0d9dc8c46ea");
	public static UUID uuidHierro = UUID.fromString("b6cbd29d-f3b7-46b3-a30f-e9f1adfe0ea7");
	public static UUID uuidGomera = UUID.fromString("65ef14b2-859e-4b69-bccb-c42c6b494854");
	public static UUID uuidLaPalma = UUID.fromString("3f4eb88f-52f4-40b9-a5bb-068081ef0f1f");
	public static UUID uuidFlores = UUID.fromString("2a601fcd-abf2-498d-ada8-ee474f3a16b1");
	public static UUID uuidFaial = UUID.fromString("a94fa265-ec5e-4a48-a18e-8644e3a1cc3c");
	public static UUID uuidAnjouan = UUID.fromString("cf601ab3-acdf-4d31-92de-953fd304705b");
	public static UUID uuidSilhouette = UUID.fromString("4e1b5b36-1d35-4e30-a396-12a227b235e2");
	public static UUID uuidLongIslandSeychelles = UUID.fromString("26450454-cae5-4767-a8ca-f6e6e82fc1e3");
	public static UUID uuidInaccessible = UUID.fromString("cfe55681-1fe0-4ca7-81af-ea684d74db53");
	public static UUID uuidBoavista = UUID.fromString("034f8734-1d90-4f92-8e6d-11a90e696820");
	public static UUID uuidMaio = UUID.fromString("034f8734-1d90-4f92-8e6d-11a90e696820");
	public static UUID uuidStLuzia = UUID.fromString("d31471eb-7c39-43a1-b571-a3fb11a2f104");
	public static UUID uuidNightingaleIs = UUID.fromString("54105a68-e01b-41af-ba98-1b99fc8ccd03");
	public static UUID uuidMohely = UUID.fromString("06e57bcd-b598-4176-aa69-0aa71ce1e2a1");
	public static UUID uuidSantaMaria = UUID.fromString("c5b56013-ed2d-4756-aa8d-d3be55a5b6b2");
	public static UUID uuidSaoMiguel = UUID.fromString("94121376-3e77-47ad-8015-4c6c2ae35b36");
	public static UUID uuidTerceira = UUID.fromString("193d3300-d41d-4c89-9159-7318d679ef6b");
	public static UUID uuidDesertas = UUID.fromString("83107c67-038f-4d1e-b13c-6ec050e4f498");
	public static UUID uuidPortoSanto = UUID.fromString("403cd0d8-af72-4f7e-ad2f-a01350e07b28");
	public static UUID uuidMahe = UUID.fromString("3130d493-cc71-463e-bdc0-dad9eb909459");
	public static UUID uuidParslin = UUID.fromString("af3dfce1-10fb-471d-83f5-a92ab18987b8");
	public static UUID uuidStoltenhoff = UUID.fromString("6ff68f76-0bb9-45e7-900c-ea78ee28e415");
	public static UUID uuidOmoroIs = UUID.fromString("715d3872-1693-4217-a45e-b36cdeba51be");
	public static UUID uuidMoorea = UUID.fromString("40a49403-d178-4789-86d6-13aad498ba4c");
	
	

	
//	69e62cb6-e0ea-4886-8c52-5aa6de6bee74
//	54715820-f15e-4ad1-b7a4-2a0e5967c931
//	baaec166-6df9-4a55-b3c7-465982e481bb

	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("DRC")){return TdwgArea.getAreaByTdwgAbbreviation("CON-OO");
		}else if (key.matches("C[ôo]t[eéê] d'Ivoire")){return TdwgArea.getAreaByTdwgAbbreviation("IVO-OO");
		}else if (key.equalsIgnoreCase("Gambia")){return TdwgArea.getAreaByTdwgAbbreviation("GAM-OO");
		}else if (key.matches("Sout?h Af?rica")){return WaterbodyOrCountry.getWaterbodyOrCountryByLabel("South Africa, Republic of");
		}else if (key.matches("Equ[ai]torial? Guinea")){return TdwgArea.getAreaByTdwgAbbreviation("EQG-OO");
		}else if (key.equalsIgnoreCase("Spanish Moroco")){return TdwgArea.getAreaByTdwgAbbreviation("MOR-SP");
		}else if (key.matches("Co?[mn]o[rl][oe] [iI]sl?\\.")|| key.equalsIgnoreCase("Comores")){return TdwgArea.getAreaByTdwgAbbreviation("COM-CO");
		}else if (key.matches("(La )?R[ée]un?ion\\.?")){return TdwgArea.getAreaByTdwgAbbreviation("REU-OO");
		}else if (key.equalsIgnoreCase("Lybia")){return TdwgArea.getAreaByTdwgAbbreviation("LBY-OO");
		}else if (key.matches("St He[lr]ena")){return TdwgArea.getAreaByTdwgAbbreviation("STH-OO");
		}else if (key.equalsIgnoreCase("Amsterdam Isl.")){return TdwgArea.getAreaByTdwgAbbreviation("ASP-OO");
		}else if (key.equalsIgnoreCase("Crozet isl.")){return TdwgArea.getAreaByTdwgAbbreviation("CRZ-OO");
		}else if (key.matches("Pr?[ií]nci[pl]e")){return TdwgArea.getAreaByTdwgAbbreviation("GGI-PR");
		}else if (key.equalsIgnoreCase("Marion Isl.") || key.matches("Prince Ed[uw]ard Isl.") ){return TdwgArea.getAreaByTdwgAbbreviation("MPE-OO");
		}else if (key.matches("Tan?z?an(ia|ai)")){return TdwgArea.getAreaByTdwgAbbreviation("TAN-OO");
		}else if (key.matches("Cana[rt]y [iI]sl.")){return TdwgArea.getAreaByTdwgAbbreviation("CNY-OO");
		}else if (key.equalsIgnoreCase("Azores")){return TdwgArea.getAreaByTdwgAbbreviation("AZO-OO");
		}else if (key.matches("Rodri[gq]ue[sz]( Isl?(and)?\\.?)?")){return TdwgArea.getAreaByTdwgAbbreviation("ROD-OO");
		}else if (key.equalsIgnoreCase("Ascension Island")){return TdwgArea.getAreaByTdwgAbbreviation("ASC-OO");
		}else if (key.matches("S[aâ]o Tom[ée]")){return TdwgArea.getAreaByTdwgAbbreviation("GGI-ST");
		}else if (key.matches("Marquesas Is.")){return TdwgArea.getAreaByTdwgAbbreviation("MRQ-OO");
		}else if (key.matches("Equador")){return TdwgArea.getAreaByTdwgAbbreviation("ECU-OO");
		}else if (key.matches("Norfolk Isl.")){return TdwgArea.getAreaByTdwgAbbreviation("NFK");
		}else if (key.matches("[cC]ape Ver[db]e Isl\\.?")){return TdwgArea.getAreaByTdwgAbbreviation("CVI-OO");
		}else if (key.matches("Sey(h|cl)elles")){return TdwgArea.getAreaByTdwgAbbreviation("SEY-OO");
		}else if (key.matches("Ma?dagas?c?ar")){return TdwgArea.getAreaByTdwgAbbreviation("MDG-OO");
		}else if (key.matches("Malay Peninsula")){return TdwgArea.getAreaByTdwgAbbreviation("MLY-PM");
		}else if (key.matches("Si?erra [lL]eone")){return TdwgArea.getAreaByTdwgAbbreviation("SIE-OO");
		}else if (key.matches("[mM]auri(tius|ce)")){return TdwgArea.getAreaByTdwgAbbreviation("MAU-OO");
		}else if (key.matches("Kena")){return TdwgArea.getAreaByTdwgAbbreviation("KEN-OO");
		}else if (key.matches("Ma[yj]otte")){return TdwgArea.getAreaByTdwgAbbreviation("COM-MA");
		}else if (key.matches("Cent?ral Africa?n? Republic")){return TdwgArea.getAreaByTdwgAbbreviation("CAF-OO");
		}else if (key.matches("Guiea")){return TdwgArea.getAreaByTdwgAbbreviation("GUI-OO");
		}else if (key.matches("Swazialnd")){return TdwgArea.getAreaByTdwgAbbreviation("SWZ-OO");
		}else if (key.matches("Guinea Bissau")){return TdwgArea.getAreaByTdwgAbbreviation("GNB-OO");
		}else if (key.matches("Za[nm]bia")){return TdwgArea.getAreaByTdwgAbbreviation("ZAM-OO");
		}else if (key.matches("Western Cape")){return TdwgArea.getAreaByTdwgAbbreviation("CPP-WC");
		}else if (key.matches("U?gan[gd]a")){return TdwgArea.getAreaByTdwgAbbreviation("UGA-OO");
		}else if (key.matches("Mo[zx]?ambique")){return TdwgArea.getAreaByTdwgAbbreviation("MOZ-OO");
		}else if (key.matches("Tchad")){return TdwgArea.getAreaByTdwgAbbreviation("CHA-OO");
		}else if (key.matches("Tri?[sa]t?an da Cunha")){return TdwgArea.getAreaByTdwgAbbreviation("TDC-OO");
		}else if (key.matches("Camero?on")){return TdwgArea.getAreaByTdwgAbbreviation("CMN-OO");
		}else if (key.matches("also in China")){return TdwgArea.getAreaByTdwgAbbreviation("36");
		}else if (key.matches("Java")){return TdwgArea.getAreaByTdwgAbbreviation("JAW-OO");
		}else if (key.matches("Burma")){return TdwgArea.getAreaByTdwgAbbreviation("MYA-OO");
		}else if (key.matches("French Guinea")){return TdwgArea.getAreaByTdwgAbbreviation("GUI-OO");
		}else if (key.matches("Bourbon")){return TdwgArea.getAreaByTdwgAbbreviation("REU-OO");
		}else if (key.matches("Sumatra")){return TdwgArea.getAreaByTdwgAbbreviation("SUM-OO");
		}else if (key.matches("Gana")){return TdwgArea.getAreaByTdwgAbbreviation("GHA-OO");
		}else if (key.matches("Lestho")){return TdwgArea.getAreaByTdwgAbbreviation("LES-OO");
		
		
		}else{
			return null;
		}
	}
	
	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		//Teilstaat des Unionsstaates Tansania
		}else if (key.equalsIgnoreCase("Zanzibar")){return uuidZanzibar;
		//Annobón (port. Ano Bom) ist eine Insel im Golf von Guinea und gleichzeitig eine der sieben Provinzen Äquatorialguineas mit der Hauptstadt San Antonio de Palé.
		}else if (key.matches("Anno?[bn]on")){return uuidAnnobon;
		//Diego Garcia ist nach der Landfläche das größte Atoll des Chagos-Archipels,
		}else if (key.equalsIgnoreCase("Diego Garcia")){return uuidDiegoGarcia;
		//Pemba ist die zweitgrößte Insel des ostafrikanischen Sansibar-Archipels. Gemeinsam mit der 50 km südlich gelegenen Insel Unguja und zahlreichen kleinen Nebeninseln bildet Pemba den halbautonomen Teilstaat „Sansibar“ in Tansania.
		}else if (key.matches("Pemba( Isl.)?")){return uuidPemba;
		//Santo Antão (port. für Heiliger Antonius) ist mit 779 km² die zweitgrößte der Kapverdischen Inseln im Atlantik. 
		}else if (key.matches("S(an)?(to?)?\\.? Ant[ãa]o") ){return uuidStoAntao;
		//São Vicente  ist eine der kleineren Kapverdischen Inseln im Atlantik
		}else if (key.matches("S(ão)?\\.? Vicente")){return uuidSaoVicente;
		//São Nicolau (dt.: „Sankt Nikolaus“) ist eine der kleineren (388 km²) und gebirgigen Kapverdischen Inseln im Norden des Archipels.
		}else if (key.matches("S(\\.|ão) Nicolau")){return uuidSaoNicolau;
		//Fogo - Kap Verde
		}else if (key.equalsIgnoreCase("Fogo")){return uuidFogo;
		//Brava ist die kleinste der bewohnten Kapverdischen Inseln im Atlantik.
		}else if (key.matches("Br[ao]v[ao]")){return uuidBrava;
		//Grande Comore - Comores
		}else if (key.matches("Grande Comor[eo]")){return uuidGrandeComore;
		//Gough ist eine hohe vulkanische Insel im Atlantischen Ozean, die zur Inselgruppe Tristan da Cunha gehört.
		}else if (key.matches("Gough Isl(\\.|and)?")){return uuidGough;
		//Middle Island ist eine Insel im Südatlantik, gelegen zwischen Nightingale Island und Stoltenhoff Island. Sie ist Teil der Inselgruppe Tristan da Cunha und gehört somit zum Britischen Überseegebiet St. Helena.
		}else if (key.matches("Middle Isl(\\.|and)?")){return uuidMiddleIslandTristanDaCunha;
		//Die Île aux Cerfs (wörtlich übersetzt „Hirscheninsel“) ist eine kleine Insel östlich von Mauritius im Indischen Ozean.
		}else if (key.equalsIgnoreCase("Ile aux Cerfs")){return uuidCerfs;
		//Sokotra ist eine Insel im nordwestlichen Indischen Ozean.
		}else if (key.equalsIgnoreCase("Soqotra")){return uuidSoqotra;
		//Johanna Isl.
		}else if (key.matches("Johanna( Isl\\.?)?")){return uuidJohannaIsl;
		//Tenerife
		}else if (key.equalsIgnoreCase("Tenerife")){return uuidTenerife;
		//Gran Canaria
		}else if (key.matches("Gran Canar(ia|y)")){return uuidGranCanaria;
		//Lanzarote
		}else if (key.matches("Lanzarote")){return uuidLanzarote;
		//Fuerteventura
		}else if (key.matches("Fuerteventura")){return uuidFuerteventura;
		//Gomera
		}else if (key.matches("Gomera")){return uuidGomera;
		//Hierro
		}else if (key.matches("Hierro")){return uuidHierro;
		//La Palma
		}else if (key.matches("(La )?Palma")){return uuidLaPalma;
		//Flores
		}else if (key.matches("Flores")){return uuidFlores;
		//Faial ist eine Gemeinde an der Nordküste Madeiras im Kreis Santan
		}else if (key.equalsIgnoreCase("Faial")){return uuidFaial;
		//Anjouan (komorisch Ndzouani) ist eine der vier Inseln der Komoren.
		}else if (key.equalsIgnoreCase("Anjouan")){return uuidAnjouan;
		//Silhouette ist eine Insel der Seychellen in der Inselgruppe um Mahé.
		}else if (key.matches("Silhouette( Isl?(and)?\\.)?")){return uuidSilhouette;
		//Long Island : Seychellen (es gibt auch andere Long Islands)
		}else if (key.matches("Long Island")){return uuidLongIslandSeychelles;
		//Inaccessible Islands sind eine Gruppe kleiner, unbewohnter Inseln im Archipel der Südlichen Orkneyinseln im Südpolarmeer.
		}else if (key.matches("Inaccessibl[ea] Isl(and)?\\.?")){return uuidInaccessible;
		//Boa Vista (dt.: „Schöner Anblick“), auch Boavista geschrieben, ist die drittgrößte der Kapverdischen Inseln im Zentralatlantik.
		}else if (key.equalsIgnoreCase("Boavista")){return uuidBoavista;
		//Maio ist eine der neun bewohnten Kapverdischen Inseln im Atlantik.
		}else if (key.equalsIgnoreCase("Maio")){return uuidMaio;
		//Sta. Luzia
		}else if (key.equalsIgnoreCase("Sta. Luzia")){return uuidStLuzia;
		//Nightingale Islands
		}else if (key.matches("Nightingale Isl(and)?\\.?")){return uuidNightingaleIs;
		//Mohély ??
		}else if (key.matches("Moh[ée]ll?[yi]")){return uuidMohely;
		//Santa Maria ist die geologisch älteste Insel der Azoren im Atlantischen Ozean
		}else if (key.matches("Santa Maria")){return uuidSantaMaria;
		//São Miguel ist die größte Insel der Azoren. 
		}else if (key.matches("São Miguel")){return uuidSaoMiguel;
		//Terceira gehört zur Zentralgruppe der Azoren.
		}else if (key.matches("Terceira")){return uuidTerceira;
		//Desertas - Madeira
		}else if (key.matches("Desertas")){return uuidDesertas;
		//Porto Santo - Madeira
		}else if (key.matches("Porto Santo")){return uuidPortoSanto;
		//Mahé - Seychelles
		}else if (key.matches("Mah[ée]")){return uuidMahe;
		//Parslin - Seychelles
		}else if (key.matches("Parslin")){return uuidParslin;
		//Stoltenhoff Island ist eine unbewohnte Insel im Südatlantik. Stoltenhoff gehört, wie die gesamte Inselgruppe Tristan da Cunha, zum Britischen Überseegebiet St. Helena.
		}else if (key.matches("Stoltenhoff Isl(and)?\\.?")){return uuidStoltenhoff;
		//Comoro Is.??
		}else if (key.matches("Omoro Isl.")){return uuidOmoroIs;
		//Moorea ältere Namen Aimeho oder Eimeo (Cook), Santo Domingo (Boenechea) und York Island (Wallis), ist eine Insel im Süd-Pazifik, die politisch zu Französisch-Polynesien gehört.
		}else if (key.matches("Moorea")){return uuidMoorea;
		
		}else{
			return null;
		}

	}
	
	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getExtensionTypeByKey(java.lang.String)
	 */
	@Override
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException {
		if (key == null){return null;
//		}else if (key.equalsIgnoreCase("fascicle")){return getExt;
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
		}else if (key.equalsIgnoreCase("fascicle")){return uuidFascicle;
		}
		return null;
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("distribution")){return Feature.DISTRIBUTION();
//		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureUuid(java.lang.String)
	 */
	@Override
	public UUID getFeatureUuid(String key) 	throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("Chromosomes")){return uuidChromosomes;
//		}else if (key.equalsIgnoreCase("Inflorescence")){return uuidInflorescence;

		
		
		}else{
			return null;
		}
		
	}

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getMarkerTypeByKey(java.lang.String)
	 */
	@Override
	public MarkerType getMarkerTypeByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("distribution")){return MarkerType.;
//		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
		}else{
			return null;
		}
	}

	@Override
	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("IMPERFECTLY KNOWN SPECIES")){return uuidIncompleteTaxon;
		}else{
			return null;
		}

	}

	
	@Override
	public PresenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("introduced")){return PresenceTerm.INTRODUCED();
		}else if (key.equalsIgnoreCase("endemic")){return PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
		}else if (key.equalsIgnoreCase("naturalised")){return PresenceTerm.NATURALISED();
		}else if (key.equalsIgnoreCase("introduced?")){return PresenceTerm.INTRODUCED_DOUBTFULLY_INTRODUCED();
		}else{
			return null;
		}
	}

	@Override
	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
//		}else if (key.equalsIgnoreCase("IN")){return indigenousUuid;
//		}else if (key.equalsIgnoreCase("CA")){return casualUuid;
//		}else if (key.equalsIgnoreCase("NN")){return nonInvasiveUuid;
//		}else if (key.equalsIgnoreCase("NA")){return invasiveUuid;
//		}else if (key.equalsIgnoreCase("Q")){return questionableUuid;
//		}else if (key.equalsIgnoreCase("IN?")){return indigenousDoubtfulUuid;
//		}else if (key.equalsIgnoreCase("CA?")){return casualDoubtfulUuid;
//		}else if (key.equalsIgnoreCase("NN?")){return nonInvasiveDoubtfulUuid;
//		}else if (key.equalsIgnoreCase("NA?")){return invasiveDoubtfulUuid;
//		}else if (key.equalsIgnoreCase("Q?")){return questionableDoubtfulUuid;
//		}else if (key.equalsIgnoreCase("CU?")){return cultivatedDoubtfulUuid;
		}else{
			return null;
		}

	}
	
}
