// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.AbsenceTerm;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public class MarkupTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupTransformer.class);
	
	//preliminary
	public static final UUID uuidKalimantan = UUID.fromString("05c6bce3-2e55-48cb-a414-707e936066d1");
	public static final UUID uuidBorneo = UUID.fromString("e615e7e6-5b64-4903-b794-816baab689b3");
//	public static final UUID uuidCelebes = UUID.fromString("");
	public static final UUID uuidMoluccas = UUID.fromString("0ac8fc74-a081-45e5-b96d-aad367b5f48b");
	public static final UUID uuidPeninsularMalaysia = UUID.fromString("9f95af33-ae9e-4031-92f7-9f79d22eecf2");
	public static final UUID uuidJava = UUID.fromString("36b93871-0bee-4380-b47e-56a843ce6aa3");
	public static final UUID uuidBismarckArchipelago = UUID.fromString("d9a11144-447c-4e72-b75f-9daeb254e7c4");
//	public static final UUID uuidNewIreland = UUID.fromString("");
	public static final UUID uuidSumatra = UUID.fromString("4395d5e0-64e2-4309-a2ec-b1240919c34d");
//	public static final UUID uuidBangka = UUID.fromString("");
	public static final UUID uuidSabah = UUID.fromString("8d34b675-7de1-4623-a16a-1e0ca989df0c");
	public static final UUID uuidBali = UUID.fromString("3cc15dc7-bc55-4a9b-85af-e1eb733ad845");
	
	public static final UUID uuidPhilippines  = UUID.fromString("9dee4b0d-d864-4b6f-bd41-39a1ea7c56c3");
	
	
	//languages
	public static final UUID uuidLanguageApindji = UUID.fromString("203fe5b4-0ebb-459e-9252-521cdf0c9654");
	public static final UUID uuidLanguageBaduma = UUID.fromString("3452dea0-8a68-4640-a786-bb786fd4fc27");
	public static final UUID uuidLanguageBakele = UUID.fromString("8216e12a-8e44-490f-bb61-f63059468ba8");
	public static final UUID uuidLanguageBakota = UUID.fromString("b43aff9a-88e5-4173-8c33-e122afeff80f");
	public static final UUID uuidLanguageBalengi = UUID.fromString("14352978-94ac-4a7a-b990-dde926d12892");
	public static final UUID uuidLanguageBalumbu = UUID.fromString("aee0952a-be89-4607-ac75-29f2c2f300bc");
	public static final UUID uuidLanguageBanzabi = UUID.fromString("ff13d80c-ea0e-4ebe-bc81-7829bb1a7c94");
	public static final UUID uuidLanguageBapunu = UUID.fromString("c6c33a49-fc8c-4599-80b7-3f9332c3e005");
	public static final UUID uuidLanguageBatanga = UUID.fromString("ee277e78-1135-4823-b4ee-63c4b93f04a2");
	public static final UUID uuidLanguageBateke = UUID.fromString("03a09cbc-37e0-46c7-9787-749c18b76edb");
	public static final UUID uuidLanguageBavangu = UUID.fromString("de81dd63-ef37-49f4-9e1e-c6f810fa7d6b");
	public static final UUID uuidLanguageBavarama = UUID.fromString("e9ec9ef0-14a1-46f0-a534-ab7afd4c1891");
	public static final UUID uuidLanguageBavili = UUID.fromString("d1e64087-3c72-4442-bb62-a931d0ab150b");
	public static final UUID uuidLanguageBavove = UUID.fromString("6a3e8938-3b48-410e-9b26-20894bfa7ae4");
	public static final UUID uuidLanguageBavungu = UUID.fromString("c337bb7c-7acc-4d82-ba5b-8da9bc4c16b9");
	public static final UUID uuidLanguageBekesi = UUID.fromString("add114f9-e81d-4f13-8b35-5008800e1e78");
	public static final UUID uuidLanguageBenga = UUID.fromString("6901da32-5d39-412e-988f-667964c4bcca");
	public static final UUID uuidLanguageBeseki = UUID.fromString("af24114e-0f95-465c-b62e-e4346468560c");
	public static final UUID uuidLanguageEchira = UUID.fromString("4c3fafce-8095-4243-b961-ad325b310b4a");
	public static final UUID uuidLanguageEschira = UUID.fromString("1dde7682-532b-4277-825d-611a6bd0d0a4");
	public static final UUID uuidLanguageEshira = UUID.fromString("34ef2f91-afa5-47e4-8098-68a4d4861a4d");
	public static final UUID uuidLanguageEshiraTandu = UUID.fromString("d3bbf124-75ad-42ff-b8f5-f74bf0243883");
	public static final UUID uuidLanguageFangDuFernanVaz = UUID.fromString("6fe6c7a9-8c51-46e3-9dbc-8010f39417f3");
	public static final UUID uuidLanguageFangDuRioMuni = UUID.fromString("6ca1f212-53fe-4314-9d75-6be89d945619");
	public static final UUID uuidLanguageGaloa = UUID.fromString("38084e32-f68f-47ef-8bf0-6741116140b4");
	public static final UUID uuidLanguageIvea = UUID.fromString("6b15c613-d10b-4c5f-aaca-cec4aed418db");
	public static final UUID uuidLanguageLoango = UUID.fromString("2f563753-77fc-4a7d-9b6b-2f524999487e");
	public static final UUID uuidLanguageMasangu = UUID.fromString("e9c6c5ab-9b27-4020-a2e6-8f257bca3c8e");
	public static final UUID uuidLanguageMindumu = UUID.fromString("0c6171ad-bfa6-42fe-a664-d40905151b6d");
	public static final UUID uuidLanguageMisogo = UUID.fromString("b6d7b47c-c020-4411-a6ae-3cc69ae43825");
	public static final UUID uuidLanguageMitsogo = UUID.fromString("83fdf45d-9f75-472f-a30e-cfd0b1473284");
	public static final UUID uuidLanguageMitsogho = UUID.fromString("5cf51081-a934-4a45-bd0a-875ad2f3a785");
	public static final UUID uuidLanguageMpongwe = UUID.fromString("1d53b7f1-46f5-4286-8c79-da8b824adce6");
	public static final UUID uuidLanguageMpongw\u00E8 = UUID.fromString("c97ba35c-6ea0-47d0-88a7-bec5df77f921");
	public static final UUID uuidLanguageNgowe = UUID.fromString("ef9fbc40-26c8-4ab3-be3e-83caa14b32ee");
	public static final UUID uuidLanguageNkomi = UUID.fromString("bbf8413a-9bb9-4acc-8be6-e22f9c305efb");
	public static final UUID uuidLanguageOrungu = UUID.fromString("787e62e6-8d10-457d-82f8-e0a30a409f88");
	public static final UUID uuidLanguageOwimo = UUID.fromString("1764092c-7826-4b79-bacc-435a9af1320e");
	public static final UUID uuidLanguageSimba = UUID.fromString("ad9071e2-8ced-49e3-a76f-0d58f959a8b1");
	
	public static final UUID uuidLanguageNomPilot = UUID.fromString("6aeb1a09-cb06-479d-9688-4574f2b80238");
	
	

	{
//		Languag
	}
	
	
	
	
	//extension type uuids
	public static final UUID uuidTaxonTitle = UUID.fromString("5d9ca987-81f1-4d6c-b06a-eaa8311ca249");
	public static final UUID uuidWriterExtension = UUID.fromString("43f988cb-bc53-4324-a702-c8f920656975");
	
	//annotation type uuids
	public static final UUID uuidFootnote = UUID.fromString("b91fab29-7d26-4277-b549-262da0d901b1");
	public static final UUID uuidWriterAnnotation = UUID.fromString("df73df4a-93a1-4d95-a552-1cebe26db51b");
	
	
	//marker type uuid
//	public static final UUID uuidExcludedTaxon = UUID.fromString("e729a22d-8c94-4859-9f91-3e3ae212c91d");
	public static final UUID uuidIncompleteTaxon = UUID.fromString("cb34d525-de64-4569-b277-3429ec49a09f");
	public static final UUID uuidFeaturePlaceholder = UUID.fromString("89568794-375e-4a02-b75d-bd65601fb877");
	
	
	//named ared level uuids
	public static final UUID uuidRegion = UUID.fromString("06d3a74d-cf2e-4842-8c89-87722b9486a4");
	public static final UUID uuidWorld = UUID.fromString("69520b33-6381-454e-bb8f-0df11c3b3a67");
	public static final UUID uuidCounty = UUID.fromString("c2882e13-88dc-42ac-b89c-2ee404e22d81");
	public static final UUID uuidContinent = UUID.fromString("1051c9bb-d0ab-4a71-ab15-afdab82c2bdc");
	public static final UUID uuidDistrict = UUID.fromString("1b778ba8-ba5f-47a9-ad67-222826f96863");
	public static final UUID uuidLocality = UUID.fromString("224a4140-da1f-4046-91bb-fb948916d797");
	public static final UUID uuidLevelOther = UUID.fromString("4b483cc8-b42d-40ba-9cc7-a656faf629e2");
	
	public static final UUID uuidContinentalRegion = UUID.fromString("06d3a74d-cf2e-4842-8c89-87722b9486a4");
	
	
	
	//feature uuids
	
	public static final UUID uuidFigure = UUID.fromString("5165cd6a-9b31-4a1f-8b30-04ab740c502c");
	public static final UUID uuidFigures = UUID.fromString("6dfb4e78-c67e-410c-8989-c1fb1295abf6");
	
	
	
	
	public static final UUID uuidExtractedHabitat = UUID.fromString("d80511d2-a76c-48b8-b3aa-5fbd4a58fe5c");
//	public static final UUID uuidHabit = UUID.fromString("03487108-173a-4335-92be-05076af29155");
	public static final UUID uuidHabitat = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
	public static final UUID uuidHabitatEcology = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
	
	public static final UUID uuidChromosomes = UUID.fromString("c4a60319-4978-4692-9545-58d60cf8379e");
	public static final UUID uuidPhylogeny = UUID.fromString("8bcffbeb-a849-4222-83f9-bfcbbc3baef9");
	public static final UUID uuidHistory = UUID.fromString("6f9f0316-1c27-4e17-b96a-51332521f74e");
	public static final UUID uuidCultivation = UUID.fromString("f10f34fb-53b9-43c2-bfd6-05ea475e8e0f");
	
	public static final UUID uuidNote = UUID.fromString("b9af1489-6b68-497f-8d4b-260a9f886827");
	public static final UUID uuidNotes = UUID.fromString("e31bb420-f39e-493d-b452-dd5e63dda443");
	public static final UUID uuidTaxonomy = UUID.fromString("0c80c395-038b-4bd6-9ff4-48f4511754b6");
	public static final UUID uuidMorphology = UUID.fromString("1b5bfe4a-d075-4e38-ab63-3c6b6bb5846a");
	public static final UUID uuidPalynology = UUID.fromString("50ddb15e-aa25-4933-8449-c321dccad4e7");
	public static final UUID uuidWoodAnatomy = UUID.fromString("b2ff70bc-f7b9-4aa8-8a4c-8f41ad6f8ada");
	public static final UUID uuidLeafAnatomy = UUID.fromString("3633debe-1c00-4f43-98f7-38b950b3880d");
	public static final UUID uuidChromosomeNumbers = UUID.fromString("6f677e98-d8d5-4bc5-80bf-affdb7e3945a");
	public static final UUID uuidPhytochemistryAndChemotaxonomy = UUID.fromString("ea76e235-a845-4f25-af07-1eee91547ef5");
	public static final UUID uuidPollenMorphology = UUID.fromString("4a00d8b2-60d7-4891-b5e7-3244278d849d");
	public static final UUID uuidVegetativeMorphologyAndAnatomy = UUID.fromString("282d1d8e-47cf-4c34-86ff-772e78b71058");
	public static final UUID uuidFlowerMorphology = UUID.fromString("cbe3ca08-0407-4a67-bf35-665e6fb3efdb");
	public static final UUID uuidPollination = UUID.fromString("0d601a3d-c444-4a7c-940b-be0a9902673f");
	public static final UUID uuidLifeCycle = UUID.fromString("fcb5d9a7-ad56-401c-b179-5f017342f3b3");
	public static final UUID uuidFruitsAndEmbryology = UUID.fromString("f22ff5ff-8cf6-4fcc-8fd2-bfdc07cb7952");
	public static final UUID uuidDispersal = UUID.fromString("1349d543-929a-4048-89dd-5006880a4cb2");
	public static final UUID uuidPhytochemistry = UUID.fromString("3466fdb9-360f-467e-9bd2-be8d997d1361");
	public static final UUID uuidFossils = UUID.fromString("ccbf72ff-ab72-4f41-8c60-77100e14b6b0");
	public static final UUID uuidMorphologyAndAnatomy = UUID.fromString("e18a82c2-8961-409f-8b8e-0502225ea43f");
	public static final UUID uuidEmbryology = UUID.fromString("09b89b41-c993-45a6-b461-799a90e283f8");
	public static final UUID uuidCytology = UUID.fromString("cc28bedb-8d9f-457c-ac5f-5f019edb214e");
	
	
	public static final UUID uuidLeaflets = UUID.fromString("0efcfbb5-7f7a-454f-985e-50cea6523fef");
	public static final UUID uuidLeaves = UUID.fromString("378c6d5f-4f8a-4769-b054-50ddaff6f080");
	public static final UUID uuidBranchlets = UUID.fromString("e63af3b4-aaff-4b4d-a8fe-3b13b79974c8");
	public static final UUID uuidLifeform = UUID.fromString("db9228d3-8bbf-4460-abfe-0b1326c82f8e");
	public static final UUID uuidInflorescences = UUID.fromString("c476f5fb-dc06-4408-af36-f48e625f5767");
	public static final UUID uuidMaleInflorescences = UUID.fromString("374fa3fe-70c2-4ec9-a611-97c62288aeba");
	public static final UUID uuidFemaleInflorescences = UUID.fromString("179af784-850c-4187-ba1f-cdc9f68970ef");
	public static final UUID uuidFlowers = UUID.fromString("7fd80f15-9abf-44e7-b55a-be264b9dd7ac");
	public static final UUID uuidHermaphroditeFlowers = UUID.fromString("e2faea4c-49d8-4e65-b76f-0cfff5add113");
	
	public static final UUID uuidSepals = UUID.fromString("d6867f7c-68c9-4b7c-9094-862bdfe1e064");
	public static final UUID uuidOuterSepals = UUID.fromString("e95b65f8-656f-4770-b716-2824cb4b54b6");
	public static final UUID uuidAnthers = UUID.fromString("3f40ea65-320b-4875-86e6-f499017b4bc6");
	public static final UUID uuidPetals = UUID.fromString("ddcba956-a461-4e66-a996-a4db808d1d9f");
	public static final UUID uuidPetal = UUID.fromString("271c2e09-5965-4c03-9ab5-6ed31a0e7e17");
	public static final UUID uuidDisc = UUID.fromString("a98875f5-fdb8-4432-98dd-3840552bf701");
	public static final UUID uuidStamens = UUID.fromString("88a6e1ff-aba2-49a1-82c5-b6a20c44d825");
	public static final UUID uuidFruits = UUID.fromString("e19b1b3c-e3fe-4496-a254-46f01ab514b3");
	public static final UUID uuidIndumentum = UUID.fromString("5737a803-397e-43e9-a278-b195941b824b");
	public static final UUID uuidSeeds = UUID.fromString("c7bb0c58-5817-4fcf-9bea-e67224e8cd8f");
	public static final UUID uuidFlowering = UUID.fromString("e6f274b9-25ca-4d49-b264-50346350df0d");
	public static final UUID uuidBracts = UUID.fromString("7e1f2b3e-caa5-4e12-af4c-3fc379cea89f");
	public static final UUID uuidPedicels = UUID.fromString("fdb43c85-c3dd-4d13-b5e7-51cca60d25b1");
	public static final UUID uuidPistil = UUID.fromString("51df329b-2b2b-4f45-960c-bf4430be5910");
	public static final UUID uuidOvary = UUID.fromString("0757d8bc-d91c-4482-bde0-d239b4122934");
	public static final UUID uuidTwigs = UUID.fromString("e1eb9d5e-1397-4a4e-84e7-483e77822c6b");
	public static final UUID uuidBranches = UUID.fromString("7c515e4a-9a6f-4d4d-9af7-c0c4039dcf27");
	public static final UUID uuidInfructescences = UUID.fromString("e60fbb4f-cf4e-4331-9dcd-d65f640eb669");
	public static final UUID uuidPistillode = UUID.fromString("7c91c9ae-ad30-4aca-96b8-249c154fb296");
	public static final UUID uuidFlower = UUID.fromString("27a04dae-3a46-41ec-a36f-866561a0f8db");
	public static final UUID uuidOvules = UUID.fromString("e118915a-0d6c-41b9-9385-9f18d852e0bc");
	//= female Flowers
	//	public static final UUID uuidFemale = UUID.fromString("fe708a69-150d-41fb-b391-dc8d9c1b8d1a");
	public static final UUID uuidStyle = UUID.fromString("6b5ae8fb-72e4-4c60-9bbe-0abc9edb09c3");
	public static final UUID uuidArillode = UUID.fromString("d113362e-06cb-42c8-96c7-4df6bef9cb29");
	public static final UUID uuidFruit = UUID.fromString("05442d43-045d-4632-9a1e-d2eada227490");
	public static final UUID uuidBranch = UUID.fromString("71b7507c-9d04-49c9-b155-398b957b4aea");
	public static final UUID uuidInflorescence = UUID.fromString("736cd249-f2dc-4fe3-a127-2c7582e330f6");
	public static final UUID uuidCalyx = UUID.fromString("48a7fa54-1aef-4209-8df0-26a8148156af");
	public static final UUID uuidSeedling = UUID.fromString("7d977209-1579-44c9-a996-9eca1fb93cfc");
	public static final UUID uuidStaminodes = UUID.fromString("4c135e5d-805b-4591-b21f-bbc34e275ef6");
	public static final UUID uuidFilaments = UUID.fromString("5d61bc65-4621-488a-8ea9-11f6e4cd2c66");

	
	//key
	public static final UUID uuidPseudoStipules = UUID.fromString("a8e3002a-5a3a-4098-9439-90dff56deeed");
	public static final UUID uuidWallOfFruitInside = UUID.fromString("c9424f3a-91fd-4696-8207-c07b2cdd5902");

	public static final UUID uuidBuds = UUID.fromString("a2f1861d-50ba-4216-80f6-7889e4785cd5");
	public static final UUID uuidStems = UUID.fromString("80b542d6-c2ec-4bc9-95c1-a1b9429691a7");
	public static final UUID uuidTrees = UUID.fromString("03a2f775-e7c7-4487-a725-51b290084e14");
	public static final UUID uuidStigma = UUID.fromString("e68292cb-3711-4129-9b1a-992fb17059e3");
	public static final UUID uuidPetiole = UUID.fromString("7e926909-5983-490e-aebe-532a329fb21f");
	public static final UUID uuidAxillary = UUID.fromString("aa1eee6e-dd2f-464e-95ed-79cc5313e8d1");
	public static final UUID uuidPetiolules = UUID.fromString("9db0732d-35f9-476b-8824-727840faabb9");
	public static final UUID uuidMaleFlowers = UUID.fromString("036ed3cb-4598-4ccd-ae77-e66dff4274f8");
	public static final UUID uuidYoungInflorescences = UUID.fromString("c92baa8b-b20e-44ec-bbc4-5990d548431c");
	public static final UUID uuidSepal = UUID.fromString("7af26081-17c8-4966-9d58-affe26b8dc34");
	public static final UUID uuidThyrses = UUID.fromString("74af0814-5f11-4c1d-82c2-06ed97471fc5");
	public static final UUID uuidThyrsus = UUID.fromString("ff0ddbd3-6049-416e-91b5-fa8fe42621dd");
	public static final UUID uuidBark = UUID.fromString("a8cd7ed7-0e55-4aa6-8a6f-52bf497e1602");
	public static final UUID uuidEndophyticBody = UUID.fromString("5b6c3525-bc9a-4ae9-b16b-814ea0ff3ffc");
	public static final UUID uuidFloweringBuds = UUID.fromString("b3c8cd80-0eea-4747-83f9-32f2418c34ff");
	
	//VOL 13.1
	public static final UUID uuidPerianth = UUID.fromString("bd1480bb-ce44-495f-a462-98db4ac80530");
	public static final UUID uuidScales = UUID.fromString("bd1480bb-ce44-495f-a462-98db4ac80530");
	public static final UUID uuidPerigoneTube = UUID.fromString("d1799423-31ce-4525-b0ba-8d7cc9240abf");
	public static final UUID uuidPerigoneLobes = UUID.fromString("e309e1e3-8f60-4478-9b89-ca5069bc1622");
	public static final UUID uuidPerigone = UUID.fromString("f026fc87-5fc6-4559-a7e5-e8832c20033d");
	public static final UUID uuidCorolla = UUID.fromString("9ff17ff9-cb59-4ad7-bfa1-1d67935e567f");
	public static final UUID uuidAnnulus = UUID.fromString("d9b93acb-9b49-45ef-8661-09e00081931f");
	public static final UUID uuidFemaleFlowers = UUID.fromString("2e06cea2-5993-417c-8d0d-81cb571aa17a");
	public static final UUID uuidCymes = UUID.fromString("ab13622f-fc90-49de-b51c-c1b00ed98728");
	public static final UUID uuidNutlets = UUID.fromString("d9675d0c-0af7-4378-aeb7-7216cdff7289");
	public static final UUID uuidStem = UUID.fromString("39f35516-045a-4f10-a3a0-c25f47e30b7d");
	public static final UUID uuidPollen = UUID.fromString("7c0df742-d1b3-4174-976a-fa04a2664aba");
	public static final UUID uuidSecondaryXylem = UUID.fromString("f2e07699-edfc-404b-9504-52a8be014131");
	public static final UUID uuidChromosomeNumber = UUID.fromString("0000feeb-ca15-4207-954b-9e3aa1112950");
	public static final UUID uuidStemLeaves = UUID.fromString("b5ef43c8-e98b-4e06-b322-c214100370ad");
	public static final UUID uuidSeed = UUID.fromString("18c725f5-6ffc-4c57-a209-3393b6c28a18");
	public static final UUID uuidDrupes = UUID.fromString("ec664134-144b-425a-9f7b-ffccd0a4bf1a");
	public static final UUID uuidFruitingAxes = UUID.fromString("a246d13e-b809-4180-ab01-f6ac1b2d2b46");
	public static final UUID uuidAndroecium = UUID.fromString("ad600c18-b04c-4c61-b71e-ce6e48de508e");
	public static final UUID uuidGynoecium = UUID.fromString("02c81ac2-3fd7-4daa-9f62-ffb2d6776f44");
	public static final UUID uuidFlowerTube = UUID.fromString("4328c13c-f80c-4f16-8c53-b0b3d8ec3cb7");
	public static final UUID uuidAnther = UUID.fromString("4c7cf621-11f7-4102-b49a-caa493364707");
	public static final UUID uuidFlowerBearingStems = UUID.fromString("86ee899f-af71-4b51-aa1a-2666bab79cf0");
	public static final UUID uuidFloweringBranchlets = UUID.fromString("e0364345-764c-4b39-943a-fad1eac0fe9e");
	public static final UUID uuidWood = UUID.fromString("e44b3268-ca49-4400-90f7-98e17412fe92");
	public static final UUID uuidGermination = UUID.fromString("747707f4-27f8-4f07-b7d1-8959f549212f");
	public static final UUID uuidFoliage = UUID.fromString("5f0ddeb5-bc12-4097-9373-b4921a87f51f");
	public static final UUID uuidCapsule = UUID.fromString("28177f44-cff5-4f9a-ba5e-ce48decd7691");
	public static final UUID uuidBioGeography = UUID.fromString("257590ed-f9c7-4253-9918-2e8440435385");
	public static final UUID uuidJuvenileParts = UUID.fromString("f4524744-920e-40c1-a37c-ba1a47044037");
	public static final UUID uuidCrown = UUID.fromString("7a234ac5-d6f4-479c-ba63-cf486307f950");
	public static final UUID uuidButtresses = UUID.fromString("bc79f058-cc3a-44bb-b800-7f24836d9175");
	public static final UUID uuidRacemes = UUID.fromString("aac5b7e7-bac2-41fa-b79b-78cd2ad7b55f");
	public static final UUID uuidSpikes = UUID.fromString("4bdf3464-a12d-4bfd-ab94-428e306ab62c");
	public static final UUID uuidParasitism = UUID.fromString("22de1689-2d54-44e0-9ebb-72b8ca84a90d");
	public static final UUID uuidBracteoles = UUID.fromString("3b17d38b-5df0-4767-919e-ee822dff4011");
	public static final UUID uuidTesta = UUID.fromString("c72ef615-8c2b-4b1e-95d3-f3a282760c02");
	public static final UUID uuidLatex = UUID.fromString("0fa07ab6-375f-4b18-8d90-8c38ecefa9c6");
	public static final UUID uuidshoots = UUID.fromString("d88360a2-59cd-4cb5-91f9-e109ae873d5e");
	public static final UUID uuidCostae = UUID.fromString("d88360a2-59cd-4cb5-91f9-e109ae873d5e");
	
	
	
	//PHYTOCHEMISTRY AND CHEMOTAXONOMY
	public static final UUID uuidLeafPhenolics = UUID.fromString("4ae0580e-601e-4961-8220-b98876cb7fbf");
	public static final UUID uuidAlkaloids = UUID.fromString("a727f4f4-b2c3-4cac-9edd-138201470396");
	public static final UUID uuidIridoidGlucosides = UUID.fromString("d858c9d7-e870-4e4e-a52a-f6533599c9d7");
	public static final UUID uuidAluminium = UUID.fromString("5c6cdbf0-c7a9-4223-8bbb-d33d41e1b9d3");
	public static final UUID uuidChemotaxonomy = UUID.fromString("53ff9430-0154-48a4-a2bb-99f183757c96");
	public static final UUID uuidStorageProductsOfSeeds = UUID.fromString("5e569333-a5b6-42f9-bc5d-e010bdfef89c");
	
	
	//VOL 12
	public static final UUID uuidCotyledons = UUID.fromString("f8087a67-ed2b-45fb-b447-3c677087fdba");
	public static final UUID uuidGrowthForm = UUID.fromString("11b5c813-a85a-4dd6-bf42-0d5f1336710b");
	public static final UUID uuidPinnaLobes = UUID.fromString("ec7c81af-1f5c-40d7-9c08-0610cf96cfb7");
	public static final UUID uuidPinnules = UUID.fromString("89a49ee5-a3da-4a3b-a00e-8f6d90e90c1b");
	public static final UUID uuidPinnatifidPinnules = UUID.fromString("a8acf6af-2a9f-4be8-ad08-19a0e87b43f0");
//	public static final UUID uuidPinnaLobes = UUID.fromString("dccb5464-f871-44aa-aed1-cb76e50efa6c");
	public static final UUID uuidSinusTeeth = UUID.fromString("5926f238-16a8-4343-a690-b958f1e9025e");
	public static final UUID uuidHypanthium = UUID.fromString("aa0fd079-3bb1-4aa7-abb4-36fd0e8ecf63");
	public static final UUID uuidPods = UUID.fromString("5d389a28-0ab7-461a-aaf8-466eff858f18");
	public static final UUID uuidStipules = UUID.fromString("63338260-014b-49a2-9714-682a8c18652f");

	
	//Ser2 VOL 2
	public static final UUID uuidGlands = UUID.fromString("28b5a141-3127-4a3e-8c4a-03a90fbe3e66");
	public static final UUID uuidSori = UUID.fromString("6e35cd29-86c9-43df-a65d-037db4d4407b");
	public static final UUID uuidVeins = UUID.fromString("7a6e2c58-504f-40fb-a97a-c928b1c519d8");
	public static final UUID uuidIndusia = UUID.fromString("196e6a5b-8612-4d3e-946b-be93e5ef66f1");
	public static final UUID uuidUpperSurfaces = UUID.fromString("90764e2b-419b-4687-93e4-832ae6fdb05e");
	public static final UUID uuidLowerSurfaces = UUID.fromString("51646c3f-5404-4423-ac44-bf7b3b258fac");
	public static final UUID uuidStipes = UUID.fromString("acf322bf-4d57-44d1-ae7b-e7525f63749c");
	public static final UUID uuidLobes = UUID.fromString("6d5f4617-96a5-4435-ad37-3d05d3718eac");
	public static final UUID uuidRachises = UUID.fromString("87365769-8257-447b-a918-51837a94487c");
	public static final UUID uuidSporangia = UUID.fromString("922e2263-ee8a-4674-9d52-188ca6e14089");
	public static final UUID uuidSpores = UUID.fromString("c8520d08-6ff3-43de-b027-e64289fa164a");
	public static final UUID uuidPinnae = UUID.fromString("5554cc7c-441f-417b-9b68-2a7643d23837");
	public static final UUID uuidBasalPinnae = UUID.fromString("ebe46c21-46f6-41b1-b269-672a08daca77");
	public static final UUID uuidpinnaLobes = UUID.fromString("26ea1b16-d008-4c63-86e6-89091f193615");
	public static final UUID uuidUpperPinnae = UUID.fromString("8563eac6-6286-4860-ac57-6cc23aa8b831");
	
	public static final UUID uuidCostules = UUID.fromString("f546e66f-a99f-4284-add3-459aff906c9f");
	public static final UUID uuidAreoles = UUID.fromString("fbf7cfb9-28bd-4443-a791-9e7ebd3eb9b6");
	public static final UUID uuidCaudex = UUID.fromString("486882de-435c-4119-94a5-213daced4c26");
	public static final UUID uuidHairs = UUID.fromString("ea2a7cc7-44be-41cb-8688-c31b085d2aa7");
	public static final UUID uuidSupraBasalPinnae = UUID.fromString("484f578e-6807-45d4-8a13-8a03b3ad1a60");
	public static final UUID uuidFreePinnae = UUID.fromString("0b8254bc-cf2d-4f95-92b9-4cc3fe450f32");
	public static final UUID uuidSecondPairOfPinnae = UUID.fromString("c25b67a8-b7b8-4caa-b112-85360d76aa26");
	public static final UUID uuidMiddlePinnae = UUID.fromString("2d90013c-cbc3-4ff7-bc6c-88b616d407a7");
	public static final UUID uuidFertilePinnae = UUID.fromString("4071035d-4a2e-4793-b6c8-178ad664c31d");
	public static final UUID uuidBasalScales = UUID.fromString("35cc8192-b6c0-4ee0-aebe-dfd15f87014c");
	public static final UUID uuidLamina = UUID.fromString("bac94906-ab83-4fa5-9670-f750a4cdd105");
	public static final UUID uuidApicalLamina = UUID.fromString("e7dff187-8704-42f0-85d0-b3cbfa18cfd0");
	public static final UUID uuidFronds = UUID.fromString("20306313-69aa-4cd7-86b7-82065ea90a07");
	public static final UUID uuidBasalPinnules = UUID.fromString("b72cba24-f61f-4d26-b34b-09f61456a419");
	public static final UUID uuidAcroscopicPinnules = UUID.fromString("c6799ad0-cc4a-4b61-98e2-b7330405a9d5");
	public static final UUID uuidVascularStrands = UUID.fromString("15d7ae67-c220-45c1-8cfa-a52f520bbe45");
	
	
	//gabon
	public static final UUID uuidEndosperm = UUID.fromString("0077b7ab-1987-4879-8dfc-e2def5377410");
	public static final UUID uuidVeinlets = UUID.fromString("21412ceb-767d-4c4f-b7f0-953acdb75a96");
	public static final UUID uuidAxillaryFlowers = UUID.fromString("61ec9cd7-bb44-47fe-abca-8ef817aba605");
	public static final UUID uuidLateralVeins = UUID.fromString("aa6aa22a-1b6f-4bc4-99bd-93281fa01a98");
	public static final UUID uuidIntegument = UUID.fromString("baaba14e-31bc-45b4-9f4a-f534073c9565");
	public static final UUID uuidJuvenileLeaves = UUID.fromString("a21bd0a7-3808-4158-8499-d49a9b3cef28");
	public static final UUID uuidPeduncle = UUID.fromString("e7ef4187-4522-4297-8945-9aeb94d0bec5");
	public static final UUID uuidJuvenileFruits = UUID.fromString("96d6c3ee-d393-4759-bbe6-d34e8282e59e");
	public static final UUID uuuidPollenSacs = UUID.fromString("3e42eb8f-5b14-427b-83be-da69d250242e");
	public static final UUID uuidConnective = UUID.fromString("1602a923-96a0-4450-8388-3467692bf244");
	public static final UUID uuidCarpels = UUID.fromString("55cbf691-b347-4399-b14f-f054a872d3e4");
	public static final UUID uuidOvule = UUID.fromString("5871858c-144f-4209-81e2-5d0fc0a61cc3");
	public static final UUID uuidSeedNumber = UUID.fromString("405762c0-0733-4eef-af74-ac85d6301810");
	public static final UUID uuidAndrophore = UUID.fromString("06757528-7a62-4068-8982-fa5900de796a");
	public static final UUID uuidRadicle = UUID.fromString("e2f7895f-1fd5-41ef-90d8-6a53e28bb7c7");
	public static final UUID uuidCentralFolioles = UUID.fromString("0587bf46-2f4c-474d-93d6-2866455e8286");
	public static final UUID uuidLateralFolioles = UUID.fromString("c1c98bc3-3e45-47c2-8ee8-915ad942066c");
	public static final UUID uuidOvarianFollicles = UUID.fromString("362d7509-1cce-44a3-b2e3-f68b4b6fb174");
	public static final UUID uuidFolioles = UUID.fromString("2f1f3b07-508d-44ca-aff1-814f985a39ce");
	public static final UUID uuidMainFolioles = UUID.fromString("85102fd1-1974-4963-a068-edeb9c0ced39");
	public static final UUID uuidTerminalFolioles = UUID.fromString("a09122d0-97ff-444c-9be4-c31df05dc4df");
	public static final UUID uuidInferiorFolioles = UUID.fromString("f5737453-a326-4931-8b79-6ba81dbd67c8");
	
	public static final UUID uuidLeafShape = UUID.fromString("dc7dd8aa-39ee-4a82-8e67-e03574a224f0");
	public static final UUID uuidJuvenileLamina = UUID.fromString("490e70c8-6777-4baa-9895-d1f22dbe5e4c");
	public static final UUID uuidApicalBuds = UUID.fromString("07e85d1a-2051-4b29-ae4d-86cf076a9fa8");
	public static final UUID uuidJuvenileCarpels = UUID.fromString("c7527c25-a599-4c85-be94-0076003b46ce");
	public static final UUID uuidAndrogynophore = UUID.fromString("a5404cfa-cff2-4b20-b300-3d10667426a7");
	public static final UUID uuidEmbryo = UUID.fromString("fe9eee1a-ba98-4cf5-8f7f-561877e4aa79");
	public static final UUID uuidMesocarp = UUID.fromString("df0fc7bf-b63f-4e7c-91b6-60f79a0d2e07");
	public static final UUID uuidEndocarp = UUID.fromString("37dfd3e5-0a1b-4e72-a7de-72961351c118");
	public static final UUID uuidSurfaces = UUID.fromString("57598ba1-3ba6-4358-ae17-45571f99cf3c");
	public static final UUID uuidRoots = UUID.fromString("77363ffb-5683-4801-a71a-f8e3b1342edc");
	public static final UUID uuidPanicles = UUID.fromString("8ec7ee47-ae72-42de-9b37-2e991b117e62");
	public static final UUID uuidFruiting = UUID.fromString("5dbdd977-0928-4479-93ea-00f303616fcd");
	public static final UUID uuidLabellum = UUID.fromString("b869ac9f-ac0a-43ee-8d22-e20dfc25c73a");
	public static final UUID uuidFlowerColor = UUID.fromString("2c94b502-13fc-40f6-a8ae-69a20b5fe9ca");
	public static final UUID uuidReceptacle = UUID.fromString("1264dae6-e72e-4de5-b1ea-604417f71987");
	public static final UUID uuidSpines = UUID.fromString("2ac1f9a4-1a36-4337-9a57-444576cc91ab");
	public static final UUID uuidTeeth = UUID.fromString("9275dcaa-5106-4bd7-8c93-9b46b1db9d29");
	public static final UUID uuidSecondaryVeins = UUID.fromString("24776a0b-cf0f-4045-b3b1-c0e541a88cf7");
	public static final UUID uuidIntersecondaryVeins = UUID.fromString("294812c5-d26d-409b-9e14-2929da1af189");
	public static final UUID uuidSyncarps= UUID.fromString("e2f5d304-7c8e-4249-9462-56b4aedfbe68");
	public static final UUID uuidEpicarp= UUID.fromString("43e81346-f731-4375-86ac-2fd69c1db2f3");
	public static final UUID uuidSuperiorMesocarps= UUID.fromString("26c269d4-7c88-4914-b4b0-0c6ec3f308f2");
	public static final UUID uuidInferiorMesocarps= UUID.fromString("2770b41b-4613-4b49-b928-3144a91c220f");
	public static final UUID uuidMaleSpikes= UUID.fromString("73c4ae7d-c0b6-4f0d-bbf9-bcde4da6a7b4");
	public static final UUID uuidCarpellodes= UUID.fromString("9862aa51-65e1-49f6-98ee-b665f24d27b9");
	public static final UUID uuidJuvenileShoots= UUID.fromString("f1c25e9e-90ce-4c2b-b568-e3e7abbd61fe");
	public static final UUID uuidSamaras= UUID.fromString("e74a9445-a17c-46d7-8053-7eae424e3a9c");
	public static final UUID uuidCapsules= UUID.fromString("6c4b042c-37f8-4d40-8298-81ce4b2e38b4");
	public static final UUID uuidLacuna= UUID.fromString("cd14eee6-71af-4eb5-ba23-2df2034c4e35");
	public static final UUID uuidDomatia= UUID.fromString("b53a6d9f-3a3f-4e93-ac27-94101b740a51");
	public static final UUID uuidNuts= UUID.fromString("94d3f3d8-3ebc-4cb7-86bd-5b0574fc965e");
	public static final UUID uuidJuvenileStems= UUID.fromString("37f447a2-2f34-4336-9ea6-a95bfb274d0c");
	public static final UUID uuidMatureStems= UUID.fromString("fdae6cf1-5335-4e50-a800-ea11adb83d6e");
	public static final UUID uuidFloralBuds= UUID.fromString("1df8faa0-46a0-48d0-bdb2-f20da41e6a85");
	public static final UUID uuidStones= UUID.fromString("f20f248c-7648-423c-8eb8-c20d5cf99854");
	public static final UUID uuidTrunk= UUID.fromString("02f48662-1f5f-4ba2-94bc-9d3f0c5027e2");
	public static final UUID uuidAkenes= UUID.fromString("c084801f-20ca-49c8-9973-8805e3b3f2c1");
	public static final UUID uuidTube= UUID.fromString("79c654de-a166-49ea-9dfa-eb1a4de9e800");
	public static final UUID uuidTepals= UUID.fromString("2f3781c3-ddc0-4946-9c41-e46a4fcf63ef");
	public static final UUID uuidPlantSexuality= UUID.fromString("dadee79f-9091-4056-b37c-cc531406fbd1");
	public static final UUID uuidAril= UUID.fromString("2da22f6e-69a7-4ba2-9595-e364f450ad0e");
	public static final UUID uuidLemma= UUID.fromString("69ae5cac-d0f0-4194-96c9-2912d1747db6");
	public static final UUID uuidValves= UUID.fromString("4e829b8b-b1b7-41ad-9835-7b4095230d58");
	public static final UUID uuidRhytidome= UUID.fromString("5a55c7c0-7e71-4e02-893f-ac218e0d9517");
	public static final UUID uuidSeedlings= UUID.fromString("48ef31e5-26c5-464b-adf2-cc350661783a");
	public static final UUID uuidHypocotyles= UUID.fromString("12758284-7fb2-441b-a38d-6b19d2865cfa");
	public static final UUID uuidFloralArrangement= UUID.fromString("4cec081c-0c6f-4246-ab89-12d2b4e18038");
	public static final UUID uuidLocules= UUID.fromString("9a005e6f-f142-4aab-b713-8994aedd758e");
	public static final UUID uuidMainStems= UUID.fromString("a87fd09e-2e38-488a-af1b-ad34d6532b10");
	public static final UUID uuidLenticels= UUID.fromString("f63740ae-9822-4e0f-8d8c-a7d81aa399e0");
	public static final UUID uuidMedianVeins= UUID.fromString("1ef11217-29e6-4dfc-b794-d9c8e822eff0");
	public static final UUID uuidColumella= UUID.fromString("40a04dad-f245-4ff6-8597-74a0c078554a");
	public static final UUID uuidKapok= UUID.fromString("8b1c7663-1c36-48d3-a4db-513dad26b124");
	public static final UUID uuidOtherSecretions= UUID.fromString("0f112884-f638-4d42-8d5b-9790d329152f");
	public static final UUID uuidPlankButtresses= UUID.fromString("a2a4adfc-fa60-4729-9b3b-1135124e95f1");
	public static final UUID uuidBranching= UUID.fromString("b543f142-7098-4433-b318-20bdaafcf9b6");
	public static final UUID uuidPetiolule= UUID.fromString("911cb51d-e65c-4d2e-a7f4-6ea02430a1d9");
	public static final UUID uuidBudsAndJuvenileShoots= UUID.fromString("e3efca5b-7ebc-48d9-9d48-ff1d15c05bce");
	public static final UUID uuidCalyxRemains= UUID.fromString("73956b0f-ae2c-4461-a2dd-fe004dcf523e");
	public static final UUID uuidFirstLeaves= UUID.fromString("a05cfbcc-3a2d-4a91-8800-d06bfad65d13");
	public static final UUID uuidColumn= UUID.fromString("73fd4fa7-fac7-4bda-9a39-c58ae110289c");
	public static final UUID uuidPrimaryVeins= UUID.fromString("16d6ea7b-6821-4193-9cc1-d252fff3347e");
	public static final UUID uuidPseudoInvolucres= UUID.fromString("d72bb8c3-ad33-4615-8f7b-926b0c780d8a");
	public static final UUID uuidSmallPlants= UUID.fromString("202c1530-4883-4c42-9912-1a6631f1d5a9");
	public static final UUID uuidLargePlants= UUID.fromString("01b901fa-87b2-4d95-9393-12a5a352bb8f");
	public static final UUID uuidAxes= UUID.fromString("5ea30f5d-d1ac-45f4-97b4-c5f233a227b0");
	public static final UUID uuidMainAxes= UUID.fromString("4b64494c-96fe-4d62-af05-3ac57e4a9ad7");
	public static final UUID uuidMainInflorescences= UUID.fromString("e2f2328e-8e99-4f1f-8e63-cff062c6e65d");
	public static final UUID uuidFertileStamens= UUID.fromString("0ad64656-3e30-4b55-a5b5-619aedc6ec82");
	public static final UUID uuidPseudostipules= UUID.fromString("0a001fc5-acdf-4338-b33e-9f97848ce288");
	public static final UUID uuidPaniculatedInflorescences= UUID.fromString("17e60c37-155a-4b87-905e-659b72908238");
	public static final UUID uuidFemaleSpikes= UUID.fromString("3e9c5c83-d437-4c4f-bbd6-4c6bf962ba65");
	public static final UUID uuidFlowerAnatomy= UUID.fromString("db7717ab-5d1a-4665-b7a1-733fc7cabe9d");
	public static final UUID uuidPileus= UUID.fromString("800c08ec-7cc0-4039-bedf-a66f702074df");
	public static final UUID uuidHypodermalCells= UUID.fromString("a0a880d1-b466-414c-8f81-bec51923895c");
	public static final UUID uuidAlbumen= UUID.fromString("410577fb-31de-4121-b439-abe4b04d9e1e");
	public static final UUID uuidMaleParts= UUID.fromString("a760a101-340c-419e-a057-674e81a50737");
	public static final UUID uuidFloralScapes= UUID.fromString("e67c18d6-8ba8-48be-b26d-bf306311f1c8");
	public static final UUID uuidIntroducedspecies= UUID.fromString("9938807e-2dfd-4eea-ab9f-c8697d2781fd");
	public static final UUID uuidBerries= UUID.fromString("73437384-8303-42ab-877a-13b7780627bd");
	public static final UUID uuidBase= UUID.fromString("d359a4fd-9c1e-467f-8522-32fb83a8d192");
	public static final UUID uuidFlowerShape= UUID.fromString("02a7b63d-7278-4e5d-8751-9b5f2ec7237e");
	public static final UUID uuidCaruncle= UUID.fromString("c7e1778c-56da-4372-ae5f-f43ec9c3c100");
	public static final UUID uuidRhizome= UUID.fromString("8fa1ab2e-beb3-4fd1-8393-696265937eae");
	public static final UUID uuidAppendages= UUID.fromString("5be4eff7-c0a6-430c-bdbd-4cd7fb886e59");
	public static final UUID uuidGynophore= UUID.fromString("918f4470-1e46-4dcc-b7a1-3d51bd3d142d");
	public static final UUID uuidInflorescenceAxes= UUID.fromString("0fab63e7-eaea-4352-b016-9f770937aac1");
	public static final UUID uuidReplum= UUID.fromString("1c10678f-9134-4693-9360-1ae1fe1ce96c");
	public static final UUID uuidSheaths= UUID.fromString("1630254b-b83f-4fd3-8818-618770d503a0");
	public static final UUID uuidSpikelets= UUID.fromString("727d4d58-352c-4704-ae1f-81f5cdc7a602");
	public static final UUID uuidMesocarps= UUID.fromString("a4303a70-83b1-4df5-86b3-a7506442486b");
	public static final UUID uuidFalseFlowers= UUID.fromString("76d706d3-bd0f-4e8b-a19c-e76aee4bbb49");
	public static final UUID uuidLodicules= UUID.fromString("60696081-77e4-4e82-aca6-1c4144070878");
	public static final UUID uuidCaryopses= UUID.fromString("88199ee4-cb10-45e8-9263-c15c6c07ed50");
	public static final UUID uuidBisexualSpikelets= UUID.fromString("a872cca3-aa84-4987-ba58-d86f41d44128");
	public static final UUID uuidUnisexualSpikelets= UUID.fromString("55a5ac26-f83a-4e77-aad7-8f0bbca77681");
	public static final UUID uuidMatureSpikelets= UUID.fromString("0db7b72e-3963-4436-8ee7-697bfd84a9eb");
	public static final UUID uuidLemmaInsertionPoint= UUID.fromString("8fd51345-c760-40bc-9639-65487f888efd");
	public static final UUID uuidGlumes= UUID.fromString("26dbf0bc-3c54-479f-be85-8132959eda4c");
	public static final UUID uuidInferiorFlowers= UUID.fromString("7f103c37-abdb-4d87-b6e0-e566137bb045");
	public static final UUID uuidSuperiorFlowers= UUID.fromString("0b95903b-c855-4a9e-aa3d-5d1bb8c331a8");
	public static final UUID uuidLeafSheath= UUID.fromString("83ffd776-2c2b-46ea-bf24-d07490a840c4");
	public static final UUID uuidInferiorLemma= UUID.fromString("f0df8083-4ef2-4a7e-bbde-cf0a6354923a");
	public static final UUID uuidInferiorGlumes= UUID.fromString("a2f3ed1a-f2d0-476d-b5df-63f12871f05b");
	public static final UUID uuidFirstInternode= UUID.fromString("cb15ee92-df1b-4ce9-a4a5-c72736b46991");
	public static final UUID uuidExtravaginalInnovations= UUID.fromString("b4a5471b-6461-4fd6-bcab-3e1544860923");
	public static final UUID uuidInnovations= UUID.fromString("14705f64-8edc-482d-a296-c690d84c2e67");
	public static final UUID uuidCataphylls= UUID.fromString("4832a8cc-d61c-47c2-b679-851c53b889b3");
	public static final UUID uuidLigula= UUID.fromString("c87125e9-b53f-4f36-86a9-fa2f9bb34909");
	public static final UUID uuidSuperiorGlumes= UUID.fromString("61ca6f10-52de-4f77-9616-83306d4cdf6f");
	public static final UUID uuidFalseSpikes= UUID.fromString("65b3c5bb-ad6b-4b2d-83c1-f73358acac30");
	public static final UUID uuidPalea= UUID.fromString("ec237f14-5884-468e-93e2-f83bdf5fdf9e");
	public static final UUID uuidSecondaryRacemes= UUID.fromString("808f514e-1d8f-4e06-a1a1-1b3839e89213");
	public static final UUID uuidSuperiorLemma= UUID.fromString("7defd3c7-474f-4949-b9a9-0c0f521483c0");
	public static final UUID uuidSterileLemma= UUID.fromString("64a49e5e-daa3-4e23-a14d-3f7891309b29");
	public static final UUID uuidReproduction= UUID.fromString("118a5351-cfe5-4b57-b12e-6e770e5f58ee");
	public static final UUID uuidFertileLemma= UUID.fromString("5d640c4e-c0a8-4dff-a812-ad897fa7b683");
	public static final UUID uuidDivision= UUID.fromString("986febfa-7da3-4a7c-a672-403ee015d1df");
	public static final UUID uuidSessileSpikelets= UUID.fromString("2d69fd0a-f280-408e-9965-d5810756421b");
	public static final UUID uuidInvolucralBracts= UUID.fromString("42c2bd31-c291-4d22-a89e-0c8775cffc24");
	public static final UUID uuidSuperiorFlowerLemma= UUID.fromString("1727c080-a251-4483-997d-43ca13735677");
	public static final UUID uuidAroma= UUID.fromString("e321d29a-2f85-4204-bc78-e3ed8124935a");
	public static final UUID uuidGlumellules= UUID.fromString("7c6cedb0-1cc8-4e29-a5b5-67bf8f196ee9");
	public static final UUID uuidRachisArticles= UUID.fromString("c915493d-d3d5-44eb-ba51-5d5937b3f629");
	public static final UUID uuidPedicelSpikelets= UUID.fromString("f91c7b10-9df3-4ca5-9c50-5747e74162e8");
	public static final UUID uuidCarina= UUID.fromString("4de2680f-ab6f-47db-97a9-3099f923058b");
	public static final UUID uuidFalseFruits= UUID.fromString("0ecf41dc-9d86-46ce-a337-866250be7394");
	public static final UUID uuidArticles= UUID.fromString("4ee7e50e-5ce8-4f2f-8c63-105fa1f9ef57");
	public static final UUID uuidInferiorFlowerLemma= UUID.fromString("330ef0f0-8bf8-469f-ad58-b6d6e935ea4c");
	public static final UUID uuidHyalineLemma= UUID.fromString("9e0dc7b2-40bf-4c97-8c89-0a058187d052");
	public static final UUID uuidCallus= UUID.fromString("f258ab47-3acc-4f27-86b2-8000e7094e81");
	public static final UUID uuidRidges= UUID.fromString("e24b5618-581a-47ba-8ba0-315c2d235952");
	public static final UUID uuidJoints= UUID.fromString("fb211597-eb1e-4524-bec3-ce3f218352cf");
	public static final UUID uuidInferiorFlowerPalea= UUID.fromString("7c167543-7b5d-4484-9f35-a33cd97b53e4");
	public static final UUID uuidInferiorGlumeShape= UUID.fromString("53867ffe-5731-4852-9950-270a89268f92");
	public static final UUID uuidSuperiorGlumeShape= UUID.fromString("38df60a2-1c01-42c8-87fb-4249b70a535c");
	public static final UUID uuidFertileSpikelets= UUID.fromString("7d03ed4c-459c-48a5-ac17-3d1c7cd1bfd0");
	public static final UUID uuidInternodes= UUID.fromString("39a06d2d-7c48-4f56-ae8d-e96066079de8");
	public static final UUID uuidSpathe= UUID.fromString("a5ab6564-464a-416f-8bb0-2fe7d5d2587e");
	public static final UUID uuidLastLeaf= UUID.fromString("b35a4f4e-d2de-46f2-95c0-5cab078a0b3d");
	public static final UUID uuidSpatheoles= UUID.fromString("e5c66ce0-4fe5-44d2-accd-3c7395c3c7a4");
	public static final UUID uuidInferiorRacemeBase= UUID.fromString("35d38f2d-5986-4aeb-ac29-ccec6f49f2f8");
	public static final UUID uuidLemmaRidges= UUID.fromString("f65b59c1-d910-444a-92d0-95c8c84a4dad");
	public static final UUID uuidRacemeBase= UUID.fromString("3fe5418e-6485-4cb7-b061-e3eb92f9b15b");
	public static final UUID uuidStemTufts= UUID.fromString("945029bb-387c-44cd-88e6-a78c1e203ee3");
	public static final UUID uuidSpikeletPairs= UUID.fromString("481b91be-8a51-40a9-9db0-ce78f48054a5");
	public static final UUID uuidGlumeInsertionPoint= UUID.fromString("df6598a8-36cc-46f4-ac7e-866a36d660a9");
	public static final UUID uuidDehiscence= UUID.fromString("51dde955-c13b-4772-a3d7-8e6036447f88");
	public static final UUID uuidGlumesAndLemmas= UUID.fromString("7b4a8fa7-5a62-41e8-8562-9ec68f2c1048");
	public static final UUID uuidPericarp= UUID.fromString("d6bd6f97-c015-4068-b438-3b8ca81ac1e0");
	public static final UUID uuidStemBase= UUID.fromString("c05c17d3-eff3-4359-8700-76536e786a1f");
	public static final UUID uuidMaleSpikelets= UUID.fromString("65bdfd7a-aab6-4d4b-a62f-00be551159aa");
	public static final UUID uuidFemaleSpikelets= UUID.fromString("01677fcd-a8b7-474b-822c-6dbee235c798");
	public static final UUID uuidLaminaMargins= UUID.fromString("b59284e3-39cd-47d2-a699-2e83527323f0");
	public static final UUID uuidJuvenileTrunk= UUID.fromString("dd22b989-a5f9-4da5-81bc-fd4bf82e513c");
	public static final UUID uuidFloriferousShoots= UUID.fromString("b02b350f-c483-46bf-915f-57b2575f1566");
	public static final UUID uuidNectaries= UUID.fromString("868bb2ed-3363-4990-acbb-faa05b4a6028");
	public static final UUID uuidPerianthDivisions= UUID.fromString("54e288ee-e04b-4805-a18f-5d5d1e17f19d");
	public static final UUID uuidSterileFlowers= UUID.fromString("e24c9d63-98dc-4cf0-8123-4bce6ff35fb0");
	public static final UUID uuidMaleFlowerTepals= UUID.fromString("6030083c-722c-4da0-a7d7-c89c33ab22bf");
	public static final UUID uuidElementaryInflorescences= UUID.fromString("a0897c1b-f582-4cda-9187-4abb406b2669");
	public static final UUID uuidDryLeaves= UUID.fromString("42b1ef0d-cf9d-40d6-9fad-e76cbbd3df95");
	public static final UUID uuidInferiorPartOfThePerianth= UUID.fromString("7c2d439e-ff61-4365-8840-54c44465826b");
	public static final UUID uuidAnthocarps= UUID.fromString("23d5443a-aca1-43c5-b74b-5cfc323392d0");
	public static final UUID uuidGlossary= UUID.fromString("b2569461-4512-45b9-b993-616062b4bff1");
	public static final UUID uuidGeography= UUID.fromString("4ef0173e-0db8-4332-8b2d-456552fffcb9");
	public static final UUID uuidFertileLeaves= UUID.fromString("f94df64e-a3d2-4a00-a38d-52d03ef5b1d1");
	public static final UUID uuidStrobili= UUID.fromString("0c24cb5d-d622-4763-a68b-1a9b00ee433d");
	public static final UUID uuidSporophylls= UUID.fromString("868c55d6-331a-42be-ae0a-915c0073685e");
	public static final UUID uuidStemMorphology= UUID.fromString("2233f648-d673-4c07-b703-0de662efad35");
	public static final UUID uuidPrimaryStems= UUID.fromString("2362976e-bce7-4a06-b125-9309f668cb69");
	public static final UUID uuidVentralRhizophores= UUID.fromString("5fd099b7-0a96-4ecc-8179-1bf89fa91e80");
	public static final UUID uuidRhizophores= UUID.fromString("5a7ec111-0207-4e14-8587-942c1809f493");
	public static final UUID uuidLateralBranchingOutline= UUID.fromString("6aa3af21-05f7-4212-a19d-f8a306f0aef5");
	public static final UUID uuidPrimaryStemLeaves= UUID.fromString("73bfe17b-b4cb-408b-b2dd-33baccf4f16d");
	public static final UUID uuidMainBranchesLateralLeaves= UUID.fromString("ad560be1-a04a-45f9-9b23-692b9c6aebc3");
	public static final UUID uuidUltimateBranchingLateralLeaves= UUID.fromString("4955fcb6-dd59-4e9d-aa23-87c607db6e94");
	public static final UUID uuidMargins= UUID.fromString("e9bc594a-3b12-4b9b-8b65-d07117c84b62");
	public static final UUID uuidAxillaryLeaves= UUID.fromString("2e77ab97-2acc-416e-8184-f2400199fe14");
	public static final UUID uuidMedianLeaves= UUID.fromString("ec0b4052-5dd3-493f-a1c1-87b72d349cc1");
	public static final UUID uuidMegaspore= UUID.fromString("0fbd52e4-5c83-4326-8c7c-8aa41f377d0d");
	public static final UUID uuidMicrospores= UUID.fromString("89cec73f-f816-4b88-9eae-d7c16eae1959");
	public static final UUID uuidUltimateRamules= UUID.fromString("4917d063-b934-4769-8404-556036e7c9f7");
	public static final UUID uuidMegasporangia= UUID.fromString("83c62bfc-69ae-4f68-b1c0-0934af422a55");
	public static final UUID uuidBranchedParts= UUID.fromString("e9f1e121-585b-4c6d-b327-13949e7ec16d");
	public static final UUID uuidLateralLeaves= UUID.fromString("d465dca6-cf04-4328-9569-2ac8eaab7cba");
	public static final UUID uuidSoboles= UUID.fromString("045ee9b3-5375-4fe1-9c37-b04eb7ed814f");
	public static final UUID uuidFalseVeins= UUID.fromString("7ae26de9-d989-48a1-9268-3af052f5c531");
	public static final UUID uuidMarks= UUID.fromString("2d70942c-0d56-4d9b-89b1-7dd23e822f70");
	public static final UUID uuidLowerHalfOfLeaves= UUID.fromString("e7d8f4c8-8840-407a-89df-c04ea34558d5");
	public static final UUID uuidVentralSporophylls= UUID.fromString("c4834494-0d9f-4225-a728-489a665e5fc5");
	public static final UUID uuidSporophytes= UUID.fromString("de3c6a33-4186-4805-927f-f2e1f7e496cf");
	public static final UUID uuidGametophytes= UUID.fromString("c4b8294a-d371-46fa-88b9-e590076bee15");
	public static final UUID uuidSegments= UUID.fromString("d837fd50-7785-4862-9e5a-3911a357c463");
	public static final UUID uuidProthallium= UUID.fromString("b1ce0535-2031-4203-b657-b409ba08629e");
	public static final UUID uuidVegetativeFronds= UUID.fromString("83a596f9-7177-479a-888d-4dd909e2f2e9");
	public static final UUID uuidFertileFronds= UUID.fromString("d42d6f25-26ac-4f59-9ce9-8a0e16f5ede5");
	public static final UUID uuidFreeVeins= UUID.fromString("9c3cb986-f432-42f1-9d7f-e734153da826");
	public static final UUID uuidTexture= UUID.fromString("613e1db2-d4c8-42b4-9df7-a672d4a68868");
	public static final UUID uuidColour= UUID.fromString("a2e173ea-51e2-45b9-a800-b05204663e99");
	public static final UUID uuidMicrosporangia= UUID.fromString("d338049c-313b-4f95-b33b-f925ace939ea");
	public static final UUID uuidMacrosporangia= UUID.fromString("c4b60645-104a-4a78-a2e1-a775020dc8b2");
	public static final UUID uuidMacrospores= UUID.fromString("cab9b36d-e592-43a8-878b-9ff113ab9c5b");
	public static final UUID uuidSterilePinnules= UUID.fromString("8917d4f8-c05e-4774-be1d-75eb4e216a97");
	public static final UUID uuidFertilePinnules= UUID.fromString("0618f5bf-396d-4ed8-aec9-92117bdc82b2");
	public static final UUID uuidFinalBifurcations= UUID.fromString("281d1d82-5c63-4b1b-9a0f-d358b1b32410");
	public static final UUID uuidPennateVeins= UUID.fromString("821c4288-42a8-46d8-a399-bb4c3f857ac1");
	public static final UUID uuidFrondDistance= UUID.fromString("5b195b5c-144a-4aa3-91d9-1586d3306253");
	public static final UUID uuidLowerPinnae= UUID.fromString("cfc703c1-c732-4e76-855c-7fde09aba033");
	public static final UUID uuidCells= UUID.fromString("46ee7b82-964b-4468-8fb9-f7cbea6b5961");
	public static final UUID uuidMarginalFalseVeins= UUID.fromString("6b31fcbb-e570-47f5-b19d-7c3356bd65f5");
	public static final UUID uuidFrondApex= UUID.fromString("5eef4649-c6fa-4a0e-a8a5-855b5081f7c7");
	public static final UUID uuidParaphyses= UUID.fromString("1384005e-43d8-4305-9cc5-be20a90d978a");
	public static final UUID uuidLateralPinnae= UUID.fromString("9515a0b1-fabf-450c-8ca7-710e9e395e31");
	public static final UUID uuidStolons= UUID.fromString("19e1f9e4-5a92-4df7-8ca6-b1c6d36b4cb9");
	public static final UUID uuidHydathodes= UUID.fromString("11682b66-0500-4a7a-9f28-6d8f5d8621ed");
	public static final UUID uuidSterilePinnae= UUID.fromString("7acbb962-b456-4905-9c3e-9387b821ef5a");
	public static final UUID uuidLaminaApex= UUID.fromString("991a6d54-cba6-4c72-8219-1e1e10345197");
	public static final UUID uuidJuvenileFronds= UUID.fromString("6e8380bf-f05d-4290-934f-2184786efcf2");
	public static final UUID uuidMatureFronds= UUID.fromString("a1aae5da-2e83-4566-982c-4a56999c7564");
	public static final UUID uuidFertileLamina= UUID.fromString("79d72420-bdea-41e7-94be-4f6194945219");
	public static final UUID uuidPinnaeApex= UUID.fromString("ac337da2-6729-4073-9de8-5041dab6d43d");
	public static final UUID uuidLaminaInferiorSurfaces= UUID.fromString("6bf624a9-610b-46cc-9fe4-0c2901552ec5");
	public static final UUID uuidTerminalPinnae= UUID.fromString("9c8fa89c-ccd3-4712-84c8-3002e2f70a8e");
	public static final UUID uuidLaminaShape= UUID.fromString("92848bc0-8ddf-4b46-87c5-7bb92d3f6ced");
	public static final UUID uuidFurrows= UUID.fromString("a0dea3ce-7c8e-4d06-95df-57e1471a097e");
	public static final UUID uuidFirstPairOfPinnae= UUID.fromString("7219f86f-fb20-4ded-ac32-ab1f16c39764");
	public static final UUID uuidCostalAreoles= UUID.fromString("99f730ba-854d-48a2-858f-2c55957f3a2b");
	public static final UUID uuidHumusCollectingFronds= UUID.fromString("5910ba4b-5c89-4170-ad4d-08c6d56e3a45");
	public static final UUID uuidAssimilatingFronds= UUID.fromString("67b6ca6d-39b1-4d30-bdf6-49171d365b06");
	public static final UUID uuidFertileParts= UUID.fromString("34bc2b46-09cc-4285-b4ff-98a47b88c370");
	public static final UUID uuidInternalCycle= UUID.fromString("036dcaf3-8c19-49a5-8902-32dd17b21748");
	public static final UUID uuidOuterParts= UUID.fromString("f31be71c-4780-4cfa-ac95-42f26b70f069");
	public static final UUID uuidLeafyStems= UUID.fromString("f63cec9b-a011-46fb-84f7-59ad1e6bf26e");
	public static final UUID uuidOuterCycle= UUID.fromString("169e4c8a-b09f-46a5-b720-d3bc6ce19eb8");
	public static final UUID uuidInnerCycle= UUID.fromString("0361a040-19de-41b1-bee5-09829cc1804a");
	public static final UUID uuidLateralStimanodes= UUID.fromString("a0f44d49-379e-402d-88d4-140b21fd5b2d");
	public static final UUID uuidFloriferousStems= UUID.fromString("15cf3f2d-4da1-4cc2-bf24-d0e4cc3f4cec");
	public static final UUID uuidInferiorBracts= UUID.fromString("6d48d403-4fad-42b8-aea7-3d382aff45c2");
	public static final UUID uuidSecondaryInflorescences= UUID.fromString("76497c6d-b958-4b33-b0cf-e6f8a8e0de1b");
	public static final UUID uuidMedianLobes= UUID.fromString("f725f99f-93bf-44f9-aaa9-b9fd1fd81f69");
	public static final UUID uuidSterileBracts= UUID.fromString("30cb3102-bcf8-4f93-be54-8ab9f0a8daa3");
	public static final UUID uuidFertileBracts= UUID.fromString("b3ac91bf-e502-4e90-8217-25faf21845f1");
	public static final UUID uuidCentralLobes= UUID.fromString("e773f6ff-fb52-4225-abc4-6edd66ca65ed");
	public static final UUID uuidCorollaLobes= UUID.fromString("ade8654a-199b-47c8-b3ff-5b92d319ebd8");
	public static final UUID uuidOuterBracts= UUID.fromString("61a6d27f-74f9-48d3-ba5e-4a41ce2676bb");
	public static final UUID uuidInnerBracts= UUID.fromString("147b7174-0761-4c8b-8c63-0b625b1952eb");
	public static final UUID uuidAerialParts= UUID.fromString("9abb931c-d06f-4b2e-9c7f-46e7aeaec162");
	public static final UUID uuidOuterStaminodes= UUID.fromString("4cbddecc-6b54-4841-ba7f-f9790fb7cac5");
	public static final UUID uuidSheathAndLigula= UUID.fromString("88d48002-7a99-4807-b664-e2378701ec4c");
	public static final UUID uuidSheathLigulaSeparation= UUID.fromString("ce8e645b-71bc-466f-8381-50aca0bcefe1");
	public static final UUID uuidAxils= UUID.fromString("ddae6f41-9bc9-45b7-ad68-f9fe33485797");
	public static final UUID uuidSixthStamen= UUID.fromString("53210dc7-2986-46ee-a561-62de9303375d");
	public static final UUID uuidLeafSymmetry= UUID.fromString("f61e9273-4bbb-4fd4-b0af-aefea2207902");
	public static final UUID uuidAbaxialBracts= UUID.fromString("c104740b-a9b4-4215-9d26-cb75e72ceaaf");
	public static final UUID uuidCallousPartOfThePetiole= UUID.fromString("dfb080b2-efe0-4465-9961-a992b10c743c");
	public static final UUID uuidInnerStaminodes= UUID.fromString("c59f4625-7f85-442e-abe5-062b28c8856b");
	public static final UUID uuidNonCallousPartOfThePetiole= UUID.fromString("2a993694-e94f-401e-a838-d48a42e6f89f");
	public static final UUID uuidPerispermaticCanal= UUID.fromString("1b46eaf4-8ffb-4cf9-8de7-42451ea663c1");
	public static final UUID uuidAdaxialBracts= UUID.fromString("b575bd61-87ee-4c04-aed0-f870bcfa6c17");
	public static final UUID uuidCallousAndNonCallousPartsOfThePetiole= UUID.fromString("f119a8b1-e3f3-4b1e-ac25-f6abd7063fd6");
	public static final UUID uuidTransitionToMedianVeins= UUID.fromString("06b8be56-602e-4bba-8dd0-2cbb5f5daa95");
	public static final UUID uuidLeafDimensions= UUID.fromString("9bfdc050-679c-4c71-80ce-4bd82102a6e8");
	public static final UUID uuidNodes= UUID.fromString("d7c6f764-47f5-4963-99e4-09b7ce4ee3eb");
	public static final UUID uuidDimensions= UUID.fromString("8db5baeb-953c-45c8-8fea-d27eff172e2b");
	public static final UUID uuidInnerStaminodialCycle= UUID.fromString("94c78e6e-91d7-4879-b284-858e654c84d5");
	public static final UUID uuidEnvelope= UUID.fromString("f1f343a8-514f-4e87-b6b6-612fed832487");
	public static final UUID uuidMedianTransversalSection= UUID.fromString("ff7a0c14-9d41-4fc5-9c13-c4f024deefb7");
	public static final UUID uuidAdaxialSpatheBracts= UUID.fromString("4827d13b-a0fa-4ccd-96f9-6df3e26fe4cb");
	public static final UUID uuidTransitionToLamina= UUID.fromString("9a4ab714-f59e-4317-851c-2b1d025878ff");
	public static final UUID uuidFlowerArrangement= UUID.fromString("63f54a01-3060-4d53-a98b-920751c1f331");
	public static final UUID uuidSecondOuterStaminode= UUID.fromString("23fd6bfa-cbf8-408a-a4ca-78adbc584c6f");
	public static final UUID uuidSecretoryStructures= UUID.fromString("ced12bb6-5b36-499f-947b-b0af00e37b14");
	public static final UUID uuidMedianTepals= UUID.fromString("764b7f4c-a418-4ad7-b17a-110fb53bd7a3");
	public static final UUID uuidThirdCycle= UUID.fromString("a29f755b-56b0-4b9b-b21d-400b02d9a164");
	public static final UUID uuidMatureLeaves= UUID.fromString("ac3e5310-e9ad-467e-9b82-8c408d265efa");
	public static final UUID uuidExocarp= UUID.fromString("dfd9629c-2bde-40db-af45-1d2b7495dd9a");
	public static final UUID uuidOuterStamens= UUID.fromString("7fdf9e58-4b32-4385-9526-c1efdd0d2783");
	public static final UUID uuidTrunkDiameter= UUID.fromString("c5cd9029-848f-41d6-a7e2-b2fbe9a20175");
	public static final UUID uuidFruitColour= UUID.fromString("403d57a2-7c7b-4b1c-a8c9-5abd0acc581c");
	public static final UUID uuidFruitPulp= UUID.fromString("1dfa6455-9864-4a90-b1b6-72c76ea8d4c1");
	public static final UUID uuidMarginalVeins= UUID.fromString("9f70c8eb-c9fb-4649-912e-c85606280b1b");
	public static final UUID uuidOperculum= UUID.fromString("ad83c4ba-6dcf-4421-9801-d8b65f5ce6d0");
	public static final UUID uuidFibres= UUID.fromString("0ac0069a-1e86-4ac4-a2ad-9087a8ee34fb");
	public static final UUID uuidJuvenileFlowerBuds= UUID.fromString("fbd3d819-b4ff-4b88-9497-e46093468d5a");
	public static final UUID uuidPlurilocularOvary= UUID.fromString("2f29a1bc-9d86-4eac-afc1-3d07308832c8");
	public static final UUID uuidExternalParts= UUID.fromString("9d6b9d3a-0b29-44c9-a847-fdd73e27619e");
	public static final UUID uuidLaminaColour= UUID.fromString("ed14883e-052a-47c1-a8fc-7181d0fabaff");
	public static final UUID uuidFlowerPosition= UUID.fromString("54ab5bbb-dfc5-4e85-b6d4-c5f5a0319ac5");
	public static final UUID uuidTertiaryVeins= UUID.fromString("00a9c985-de6b-4bc7-9400-d864ff76efa8");
	public static final UUID uuidFruitAnatomy= UUID.fromString("9b984b37-8c37-4ce7-af4b-3fa068bbe511");
	public static final UUID uuidJuvenilePlants= UUID.fromString("56227883-c393-4a62-933f-c087a970c7aa");
	public static final UUID uuidLargeFlowers= UUID.fromString("d828a5ac-5c18-4871-b6ec-88066814382a");
	public static final UUID uuidOpenFlowers= UUID.fromString("50714ecf-ef48-48c0-a2ff-74f122aaa82d");
	public static final UUID uuidCupules= UUID.fromString("44d1f59d-5cb7-4cc1-a5f3-62182bc6e966");
	public static final UUID uuidPrickles= UUID.fromString("7caf0f41-f73a-4f7d-8b50-4ea622a06c34");
	public static final UUID uuidMedianFolioles= UUID.fromString("189b8d3a-32a4-4e86-a3ee-1576bc15e18d");
	public static final UUID uuidStaminodialAppendix= UUID.fromString("6c091ba5-8a82-44b5-b1d2-290353dcd7a5");
	public static final UUID uuidAerialStems= UUID.fromString("46c62d21-0631-4b26-8372-0c55069c45f2");
	public static final UUID uuidSuperiorSepals= UUID.fromString("bbdc4114-d3da-4021-a12c-79cb5fb82e6f");
	public static final UUID uuidSuperiorPetals= UUID.fromString("6c917307-a637-4fc5-9194-a83b418e5bf9");
	public static final UUID uuidLeafArrangement= UUID.fromString("65484a83-060a-465e-b8e4-feeca3e022e1");
	public static final UUID uuidUnifoliateLeaves= UUID.fromString("c64e2efa-eba8-4e8f-b96b-5160bf7e5c85");
	public static final UUID uuidTrifoliateLeaves= UUID.fromString("818fffd8-2b00-444e-8fc5-9266c629e980");
	public static final UUID uuid5FoliateLeaves= UUID.fromString("e2c274f6-c195-4c98-8af8-695fadef85b5");
	public static final UUID uuidFloralBractStipules= UUID.fromString("4ed3b96b-b8d6-4d1f-946c-5f455a17f318");
	public static final UUID uuid4FoliateLeaves= UUID.fromString("9c6155c1-d8e2-404b-bc48-4dbc1373cf41");
	public static final UUID uuidCompoundLeafPetiolules= UUID.fromString("c255e9f1-77db-4979-97f2-22df26b3812d");
	public static final UUID uuidBractStipules= UUID.fromString("12d2566b-308f-427a-b01a-7228bb6aebfc");
	public static final UUID uuidLeafDivision= UUID.fromString("1184d7f0-6896-40c2-be82-e1537e7da10e");
	public static final UUID uuidSimpleLeaves= UUID.fromString("566f5d9e-3dfd-40b3-a864-7d8491c916ea");
	public static final UUID uuidCompoundLeaves= UUID.fromString("4744711c-5edc-4912-a33e-1f899496f1e5");
	public static final UUID uuidFolioleTexture= UUID.fromString("66e7bcfe-75ef-49eb-91c5-cfe199dbc5e8");
	public static final UUID uuidFruitWall= UUID.fromString("ea9cd484-dc65-4a1b-ad36-b1daf0b99dbf");


	
//	guianas
	public static final UUID uuidExtraxylarySclerenchyma = UUID.fromString("cdddefbe-2f41-4d5c-89e8-4790b6d069fb");

	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		
		}else if (key.equalsIgnoreCase("ecology")){return Feature.ECOLOGY();
		}else if (key.equalsIgnoreCase("phenology")){return Feature.PHENOLOGY();
		}else if (key.equalsIgnoreCase("uses")){return Feature.USES();
		}else if (key.equalsIgnoreCase("anatomy")){return Feature.ANATOMY();
		}else if (key.equalsIgnoreCase("description")){return Feature.DESCRIPTION();
		}else if (key.equalsIgnoreCase("distribution")){return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.HABITAT_ECOLOGY();
		}else if (key.equalsIgnoreCase("vernacular")){return Feature.COMMON_NAME();
		}else if (key.equalsIgnoreCase("specimens")){return Feature.SPECIMEN();
		}else if (key.equalsIgnoreCase("materials examined")){return Feature.MATERIALS_EXAMINED();
		}else if (key.equalsIgnoreCase("material and methods")){return Feature.MATERIALS_METHODS();
		}else if (key.equalsIgnoreCase("Vegetative Anatomy")){return Feature.ANATOMY();
		
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureUuid(java.lang.String)
	 */
	@Override
	public UUID getFeatureUuid(String key) 	throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;

		}else if (key.equalsIgnoreCase("cultivation")){return uuidCultivation;
		}else if (key.equalsIgnoreCase("history")){return uuidHistory;
		}else if (key.equalsIgnoreCase("phylogeny")){return uuidPhylogeny;
		}else if (key.equalsIgnoreCase("Chromosomes")){return uuidChromosomes;
		}else if (key.equalsIgnoreCase("Habitat")){return uuidHabitat;
		}else if (key.equalsIgnoreCase("Habitat & Ecology")){return uuidHabitatEcology;
		}else if (key.equalsIgnoreCase("Leaflets")){return uuidLeaflets;
		}else if (key.equalsIgnoreCase("Leaves")){return uuidLeaves;
		}else if (key.equalsIgnoreCase("Branchlets")){return uuidBranchlets;
		}else if (key.equalsIgnoreCase("lifeform")){return uuidLifeform;
		}else if (key.equalsIgnoreCase("habit")){return uuidLifeform;
		}else if (key.equalsIgnoreCase("Inflorescences")){return uuidInflorescences;
		}else if (key.equalsIgnoreCase("Flowers")){return uuidFlowers;
		}else if (key.equalsIgnoreCase("Sepals")){return uuidSepals;
		}else if (key.equalsIgnoreCase("Outer Sepals")){return uuidOuterSepals;
		}else if (key.equalsIgnoreCase("Anthers")){return uuidAnthers;
		}else if (key.equalsIgnoreCase("Petals")){return uuidPetals;
		}else if (key.equalsIgnoreCase("Petal")){return uuidPetal;
		}else if (key.equalsIgnoreCase("Disc")){return uuidDisc;
		}else if (key.equalsIgnoreCase("Disk")){return uuidDisc;
		}else if (key.equalsIgnoreCase("Stamens")){return uuidStamens;
		}else if (key.equalsIgnoreCase("Fruits")){return uuidFruits;
		}else if (key.equalsIgnoreCase("Indumentum")){return uuidIndumentum;
		}else if (key.equalsIgnoreCase("figure")){return uuidFigure;
		}else if (key.equalsIgnoreCase("fig")){return uuidFigure;
		}else if (key.equalsIgnoreCase("figs")){return uuidFigures;
		}else if (key.equalsIgnoreCase("figures")){return uuidFigures;
		}else if (key.equalsIgnoreCase("Seeds")){return uuidSeeds;
		}else if (key.equalsIgnoreCase("Flowering")){return uuidFlowering;
		}else if (key.equalsIgnoreCase("Bracts")){return uuidBracts;
		}else if (key.equalsIgnoreCase("Pedicels")){return uuidPedicels;
		}else if (key.equalsIgnoreCase("Pistil")){return uuidPistil;
		}else if (key.equalsIgnoreCase("Ovary")){return uuidOvary;
		}else if (key.equalsIgnoreCase("Twigs")){return uuidTwigs;
		}else if (key.equalsIgnoreCase("Pedicels")){return uuidPedicels;
		}else if (key.equalsIgnoreCase("Infructescences")){return uuidInfructescences;
		}else if (key.equalsIgnoreCase("Branches")){return uuidBranches;
		}else if (key.equalsIgnoreCase("Flower")){return uuidFlower;
		}else if (key.equalsIgnoreCase("hermaphrodite flowers")){return uuidHermaphroditeFlowers;
		}else if (key.equalsIgnoreCase("Ovules")){return uuidOvules;
		}else if (key.equalsIgnoreCase("Female")){return uuidFemaleFlowers;
		}else if (key.equalsIgnoreCase("Style")){return uuidStyle;
		}else if (key.equalsIgnoreCase("Arillode")){return uuidArillode;
		}else if (key.equalsIgnoreCase("Fruit")){return uuidFruit;
		}else if (key.equalsIgnoreCase("Branch")){return uuidBranch;
		}else if (key.equalsIgnoreCase("Inflorescence")){return uuidInflorescence;
		}else if (key.equalsIgnoreCase("Male inflorescences")){return uuidMaleInflorescences;
		}else if (key.equalsIgnoreCase("Female inflorescences")){return uuidFemaleInflorescences;
		
		}else if (key.equalsIgnoreCase("Calyx")){return uuidCalyx;
		}else if (key.equalsIgnoreCase("Seedling")){return uuidSeedling;
		}else if (key.equalsIgnoreCase("Staminodes")){return uuidStaminodes;
		}else if (key.equalsIgnoreCase("Filaments")){return uuidFilaments;
		}else if (key.equalsIgnoreCase("Pistillode")){return uuidPistillode;
		}else if (key.equalsIgnoreCase("Stigma")){return uuidStigma;
		}else if (key.equalsIgnoreCase("Petiole")){return uuidPetiole;	
		}else if (key.equalsIgnoreCase("Buds")){return uuidBuds;
		}else if (key.equalsIgnoreCase("Stems")){return uuidStems;
		}else if (key.equalsIgnoreCase("Trees")){return uuidTrees;
		}else if (key.equalsIgnoreCase("Axillary")){return uuidAxillary;
		}else if (key.equalsIgnoreCase("Petiolules")){return uuidPetiolules;
		}else if (key.equalsIgnoreCase("Male flowers")){return uuidMaleFlowers;
		}else if (key.equalsIgnoreCase("Young inflorescences")){return uuidYoungInflorescences;
		}else if (key.equalsIgnoreCase("Sepal")){return uuidSepal;
		}else if (key.equalsIgnoreCase("Thyrses")){return uuidThyrses;
		}else if (key.equalsIgnoreCase("Thyrsus")){return uuidThyrsus;
		}else if (key.equalsIgnoreCase("Bark")){return uuidBark;
		}else if (key.equalsIgnoreCase("endophytic body")){return uuidEndophyticBody;
		}else if (key.equalsIgnoreCase("flowering buds")){return uuidFloweringBuds;
		//TODO the same ?
		}else if (key.equalsIgnoreCase("flower buds")){return uuidFloweringBuds;
		}else if (key.equalsIgnoreCase("perianth")){return uuidPerianth;
		}else if (key.equalsIgnoreCase("scales")){return uuidScales;
		}else if (key.equalsIgnoreCase("perigone tube")){return uuidPerigoneTube;
		}else if (key.equalsIgnoreCase("perigone")){return uuidPerigone;
		}else if (key.equalsIgnoreCase("perigone lobes")){return uuidPerigoneLobes;
		
		}else if (key.equalsIgnoreCase("corolla")){return uuidCorolla;
		}else if (key.equalsIgnoreCase("annulus")){return uuidAnnulus;
		}else if (key.equalsIgnoreCase("female flowers")){return uuidFemaleFlowers;
		}else if (key.equalsIgnoreCase("cymes")){return uuidCymes;
		}else if (key.equalsIgnoreCase("nutlets")){return uuidNutlets;
		}else if (key.equalsIgnoreCase("stem")){return uuidStem;
		}else if (key.equalsIgnoreCase("pollen")){return uuidPollen;
		}else if (key.equalsIgnoreCase("secondary xylem")){return uuidSecondaryXylem;
		}else if (key.equalsIgnoreCase("chromosome number")){return uuidChromosomeNumber;
		}else if (key.equalsIgnoreCase("stem leaves")){return uuidStemLeaves;
		}else if (key.equalsIgnoreCase("flower tube")){return uuidFlowerTube;
		
		}else if (key.equalsIgnoreCase("seed")){return uuidSeed;
		}else if (key.equalsIgnoreCase("drupes")){return uuidDrupes;
		}else if (key.equalsIgnoreCase("fruiting axes")){return uuidFruitingAxes;
		}else if (key.equalsIgnoreCase("androecium")){return uuidAndroecium;
		}else if (key.equalsIgnoreCase("gynoecium")){return uuidGynoecium;
		
		}else if (key.equalsIgnoreCase("anther")){return uuidAnther;
		}else if (key.equalsIgnoreCase("wood")){return uuidWood;
		}else if (key.equalsIgnoreCase("flower-bearing stems")){return uuidFlowerBearingStems;
		}else if (key.equalsIgnoreCase("Flowering branchlets")){return uuidFloweringBranchlets;
		
		}else if (key.equalsIgnoreCase("Pseudo-stipules")){return uuidPseudoStipules;
		}else if (key.equalsIgnoreCase("Wall of fruit inside")){return uuidWallOfFruitInside;
				
		
		}else if (key.equalsIgnoreCase("Note")){return uuidNote;
		}else if (key.equalsIgnoreCase("Notes")){return uuidNotes;
		}else if (key.equalsIgnoreCase("Taxonomy")){return uuidTaxonomy;
		}else if (key.equalsIgnoreCase("Morphology")){return uuidMorphology;
		}else if (key.equalsIgnoreCase("Palynology")){return uuidPalynology;
		}else if (key.equalsIgnoreCase("Wood anatomy")){return uuidWoodAnatomy;
		}else if (key.equalsIgnoreCase("Leaf anatomy")){return uuidLeafAnatomy;
		}else if (key.equalsIgnoreCase("Chromosome numbers")){return uuidChromosomeNumbers;
		}else if (key.equalsIgnoreCase("Phytochemistry and Chemotaxonomy")){return uuidPhytochemistryAndChemotaxonomy;
		}else if (key.equalsIgnoreCase("phytochemo")){return uuidPhytochemistryAndChemotaxonomy;
		}else if (key.equalsIgnoreCase("Pollen morphology")){return uuidPollenMorphology;
		}else if (key.equalsIgnoreCase("Vegetative morphology and anatomy")){return uuidVegetativeMorphologyAndAnatomy;
		}else if (key.equalsIgnoreCase("Flower morphology")){return uuidFlowerMorphology;
		}else if (key.equalsIgnoreCase("Pollination")){return uuidPollination;
		}else if (key.equalsIgnoreCase("Life cycle")){return uuidLifeCycle;
		}else if (key.equalsIgnoreCase("lifehistory")){return uuidLifeCycle;
		}else if (key.equalsIgnoreCase("Fruits and embryology")){return uuidFruitsAndEmbryology;
		}else if (key.equalsIgnoreCase("Dispersal")){return uuidDispersal;
		}else if (key.equalsIgnoreCase("Phytochemistry")){return uuidPhytochemistry;
		}else if (key.equalsIgnoreCase("Fossils")){return uuidFossils;
		}else if (key.equalsIgnoreCase("Morphology and anatomy")){return uuidMorphologyAndAnatomy;
		}else if (key.equalsIgnoreCase("embryology")){return uuidEmbryology;
		}else if (key.equalsIgnoreCase("cytology")){return uuidCytology;
		}else if (key.equalsIgnoreCase("germination")){return uuidGermination;
		}else if (key.equalsIgnoreCase("foliage")){return uuidFoliage;
		}else if (key.equalsIgnoreCase("capsule")){return uuidCapsule;
		}else if (key.equalsIgnoreCase("biogeography")){return uuidBioGeography;
		}else if (key.equalsIgnoreCase("juvenile parts")){return uuidJuvenileParts;
		}else if (key.equalsIgnoreCase("crown")){return uuidCrown;
		}else if (key.equalsIgnoreCase("buttresses")){return uuidButtresses;
		}else if (key.equalsIgnoreCase("racemes")){return uuidRacemes;
		}else if (key.equalsIgnoreCase("spikes")){return uuidSpikes;
		}else if (key.equalsIgnoreCase("parasitism")){return uuidParasitism;
		}else if (key.equalsIgnoreCase("bracteoles")){return uuidBracteoles;
		}else if (key.equalsIgnoreCase("testa")){return uuidTesta;
		}else if (key.equalsIgnoreCase("latex")){return uuidLatex;
		}else if (key.equalsIgnoreCase("shoots")){return uuidshoots;
		
		
		
		
		}else if (key.equalsIgnoreCase("Leaf phenolics")){return uuidLeafPhenolics;
		}else if (key.equalsIgnoreCase("Alkaloids")){return uuidAlkaloids;
		}else if (key.equalsIgnoreCase("Iridoid glucosides")){return uuidIridoidGlucosides;
		}else if (key.equalsIgnoreCase("Aluminium")){return uuidAluminium;
		}else if (key.equalsIgnoreCase("Chemotaxonomy")){return uuidChemotaxonomy;
		}else if (key.equalsIgnoreCase("Storage products of seeds")){return uuidStorageProductsOfSeeds;
		
		}else if (key.equalsIgnoreCase("cotyledons")){return uuidCotyledons;
		}else if (key.equalsIgnoreCase("Growth form")){return uuidCotyledons;
		}else if (key.equalsIgnoreCase("Hypanthium")){return uuidHypanthium;
		}else if (key.equalsIgnoreCase("pods")){return uuidPods;
		}else if (key.equalsIgnoreCase("stipules")){return uuidStipules;
		
		//2_2
		}else if (key.equalsIgnoreCase("glands")){return uuidGlands;
		}else if (key.equalsIgnoreCase("sori")){return uuidSori;
		}else if (key.equalsIgnoreCase("veins")){return uuidVeins;
		}else if (key.equalsIgnoreCase("indusia")){return uuidIndusia;
		}else if (key.equalsIgnoreCase("upper surfaces")){return uuidUpperSurfaces;
		}else if (key.equalsIgnoreCase("lower surfaces")){return uuidLowerSurfaces;
		}else if (key.equalsIgnoreCase("stipes")){return uuidStipes;
		}else if (key.equalsIgnoreCase("lobes")){return uuidLobes;
		}else if (key.equalsIgnoreCase("rachises")){return uuidRachises;
		}else if (key.equalsIgnoreCase("sporangia")){return uuidSporangia;
		}else if (key.equalsIgnoreCase("spores")){return uuidSpores;
		}else if (key.equalsIgnoreCase("pinnae")){return uuidPinnae;
		}else if (key.equalsIgnoreCase("basal pinnae")){return uuidBasalPinnae;
		}else if (key.equalsIgnoreCase("suprabasal pinnae")){return uuidSupraBasalPinnae;
		}else if (key.equalsIgnoreCase("free pinnae")){return uuidFreePinnae;
		}else if (key.equalsIgnoreCase("second pair of pinnae")){return uuidSecondPairOfPinnae;
		}else if (key.equalsIgnoreCase("middle pinnae")){return uuidMiddlePinnae;
		}else if (key.equalsIgnoreCase("fertile pinnae")){return uuidFertilePinnae;
		}else if (key.equalsIgnoreCase("pinna-lobes")){return uuidPinnaLobes;
		}else if (key.equalsIgnoreCase("upper pinnae")){return uuidUpperPinnae;
		
		
		}else if (key.equalsIgnoreCase("sinus-teeth")){return uuidSinusTeeth;
		
		
		}else if (key.equalsIgnoreCase("costules")){return uuidCostules;
		}else if (key.equalsIgnoreCase("areoles")){return uuidAreoles;
		}else if (key.equalsIgnoreCase("caudex")){return uuidCaudex;
		}else if (key.equalsIgnoreCase("hairs")){return uuidHairs;
		}else if (key.equalsIgnoreCase("basal scales")){return uuidBasalScales;
		}else if (key.equalsIgnoreCase("lamina")){return uuidLamina;
		}else if (key.equalsIgnoreCase("apical lamina")){return uuidApicalLamina;
		}else if (key.equalsIgnoreCase("fronds")){return uuidFronds;
		}else if (key.equalsIgnoreCase("pinnules")){return uuidPinnules;
		
		}else if (key.equalsIgnoreCase("basal pinnules")){return uuidBasalPinnules;
		
		}else if (key.equalsIgnoreCase("acroscopic pinnules")){return uuidAcroscopicPinnules;
		}else if (key.equalsIgnoreCase("costae")){return uuidCostae;
		}else if (key.equalsIgnoreCase("vascular strands")){return uuidVascularStrands;
		
		
		//FdG
		}else if (key.equalsIgnoreCase("endosperm")){return uuidEndosperm;
		}else if (key.equalsIgnoreCase("veinlets")){return uuidVeinlets;
		}else if (key.equalsIgnoreCase("axillary flowers")){return uuidAxillaryFlowers;
		}else if (key.equalsIgnoreCase("lateral veins")){return uuidLateralVeins;
		}else if (key.equalsIgnoreCase("secondary veins")){return uuidSecondaryVeins;
		}else if (key.equalsIgnoreCase("intersecondary veins")){return uuidIntersecondaryVeins;
		
		}else if (key.equalsIgnoreCase("integument")){return uuidIntegument;
		}else if (key.equalsIgnoreCase("juvenile leaves")){return uuidJuvenileLeaves;
		}else if (key.equalsIgnoreCase("peduncle")){return uuidPeduncle;
		}else if (key.equalsIgnoreCase("juvenile fruits")){return uuidJuvenileFruits;
		}else if (key.equalsIgnoreCase("pollen sacs")){return uuuidPollenSacs;
		}else if (key.equalsIgnoreCase("connective")){return uuidConnective;
		}else if (key.equalsIgnoreCase("carpels")){return uuidCarpels;
		}else if (key.equalsIgnoreCase("ovule")){return uuidOvule;
		}else if (key.equalsIgnoreCase("seed number")){return uuidSeedNumber;
		}else if (key.equalsIgnoreCase("androphore")){return uuidAndrophore;
		}else if (key.equalsIgnoreCase("radicle")){return uuidRadicle;
		
		
		}else if (key.equalsIgnoreCase("folioles")){return uuidFolioles;
		}else if (key.equalsIgnoreCase("central folioles")){return uuidCentralFolioles;
		}else if (key.equalsIgnoreCase("lateral folioles")){return uuidLateralFolioles;
		}else if (key.equalsIgnoreCase("main folioles")){return uuidMainFolioles;
		}else if (key.equalsIgnoreCase("terminal folioles")){return uuidTerminalFolioles;
		}else if (key.equalsIgnoreCase("inferior folioles")){return uuidInferiorFolioles;
		}else if (key.equalsIgnoreCase("ovarian follicles")){return uuidOvarianFollicles;

		}else if (key.equalsIgnoreCase("leaf shape")){return uuidLeafShape;
		}else if (key.equalsIgnoreCase("juvenile lamina")){return uuidJuvenileLamina;
		}else if (key.equalsIgnoreCase("apical buds")){return uuidApicalBuds;
		}else if (key.equalsIgnoreCase("juvenile carpels")){return uuidJuvenileCarpels;
		}else if (key.equalsIgnoreCase("androgynophore")){return uuidAndrogynophore;
		}else if (key.equalsIgnoreCase("embryo")){return uuidEmbryo;
		}else if (key.equalsIgnoreCase("mesocarp")){return uuidMesocarp;
		}else if (key.equalsIgnoreCase("endocarp")){return uuidEndocarp;
		}else if (key.equalsIgnoreCase("surfaces")){return uuidSurfaces;
		}else if (key.equalsIgnoreCase("roots")){return uuidRoots;
		}else if (key.equalsIgnoreCase("panicles")){return uuidPanicles;
		}else if (key.equalsIgnoreCase("fruiting")){return uuidFruiting;
		}else if (key.equalsIgnoreCase("labellum")){return uuidLabellum;
		}else if (key.equalsIgnoreCase("flower color")){return uuidFlowerColor;
		}else if (key.equalsIgnoreCase("receptacle")){return uuidReceptacle;
		}else if (key.equalsIgnoreCase("spines")){return uuidSpines;
		}else if (key.equalsIgnoreCase("teeth")){return uuidTeeth;
		}else if (key.equalsIgnoreCase("syncarps")){return uuidSyncarps;
		}else if (key.equalsIgnoreCase("epicarp")){return uuidEpicarp;
		}else if (key.equalsIgnoreCase("superior mesocarps")){return uuidSuperiorMesocarps;
		}else if (key.equalsIgnoreCase("inferior mesocarps")){return uuidInferiorMesocarps;
		}else if (key.equalsIgnoreCase("male spikes")){return uuidMaleSpikes;
		}else if (key.equalsIgnoreCase("carpellodes")){return uuidCarpellodes;
		}else if (key.equalsIgnoreCase("juvenile shoots")){return uuidJuvenileShoots;
		}else if (key.equalsIgnoreCase("samaras")){return uuidSamaras;
		}else if (key.equalsIgnoreCase("capsules")){return uuidCapsules;
		}else if (key.equalsIgnoreCase("lacuna")){return uuidLacuna;
		}else if (key.equalsIgnoreCase("domatia")){return uuidDomatia;
		}else if (key.equalsIgnoreCase("nuts")){return uuidNuts;
		}else if (key.equalsIgnoreCase("juvenile stems")){return uuidJuvenileStems;
		}else if (key.equalsIgnoreCase("mature stems")){return uuidMatureStems;
		}else if (key.equalsIgnoreCase("floral buds")){return uuidFloralBuds;
		}else if (key.equalsIgnoreCase("stones")){return uuidStones;
		}else if (key.equalsIgnoreCase("trunk")){return uuidTrunk;
		}else if (key.equalsIgnoreCase("akenes")){return uuidAkenes;
		}else if (key.equalsIgnoreCase("tube")){return uuidTube;
		}else if (key.equalsIgnoreCase("tepals")){return uuidTepals;
		}else if (key.equalsIgnoreCase("plant sexuality")){return uuidPlantSexuality;
		}else if (key.equalsIgnoreCase("aril")){return uuidAril;
		}else if (key.equalsIgnoreCase("lemma")){return uuidLemma;
		}else if (key.equalsIgnoreCase("valves")){return uuidValves;
		}else if (key.equalsIgnoreCase("rhytidome")){return uuidRhytidome;
		}else if (key.equalsIgnoreCase("seedlings")){return uuidSeedlings;
		}else if (key.equalsIgnoreCase("hypocotyles")){return uuidHypocotyles;
		}else if (key.equalsIgnoreCase("floral arrangement")){return uuidFloralArrangement;
		}else if (key.equalsIgnoreCase("locules")){return uuidLocules;
		}else if (key.equalsIgnoreCase("main stems")){return uuidMainStems;
		}else if (key.equalsIgnoreCase("lenticels")){return uuidLenticels;
		}else if (key.equalsIgnoreCase("median veins")){return uuidMedianVeins;
		}else if (key.equalsIgnoreCase("columella")){return uuidColumella;
		}else if (key.equalsIgnoreCase("kapok")){return uuidKapok;
		}else if (key.equalsIgnoreCase("other secretions")){return uuidOtherSecretions;
		}else if (key.equalsIgnoreCase("plank buttresses")){return uuidPlankButtresses;
		}else if (key.equalsIgnoreCase("branching")){return uuidBranching;
		}else if (key.equalsIgnoreCase("petiolule")){return uuidPetiolule;
		}else if (key.equalsIgnoreCase("buds and juvenile shoots")){return uuidBudsAndJuvenileShoots;
		}else if (key.equalsIgnoreCase("calyx remains")){return uuidCalyxRemains;
		}else if (key.equalsIgnoreCase("first leaves")){return uuidFirstLeaves;
		}else if (key.equalsIgnoreCase("column")){return uuidColumn;
		}else if (key.equalsIgnoreCase("primary veins")){return uuidPrimaryVeins;
		}else if (key.equalsIgnoreCase("pseudo-involucres")){return uuidPseudoInvolucres;
		}else if (key.equalsIgnoreCase("small plants")){return uuidSmallPlants;
		}else if (key.equalsIgnoreCase("large plants")){return uuidLargePlants;
		}else if (key.equalsIgnoreCase("axes")){return uuidAxes;
		}else if (key.equalsIgnoreCase("main axes")){return uuidMainAxes;
		}else if (key.equalsIgnoreCase("main inflorescences")){return uuidMainInflorescences;
		}else if (key.equalsIgnoreCase("fertile stamens")){return uuidFertileStamens;
		}else if (key.equalsIgnoreCase("pseudostipules")){return uuidPseudostipules;
		}else if (key.equalsIgnoreCase("paniculated inflorescences")){return uuidPaniculatedInflorescences;
		}else if (key.equalsIgnoreCase("female spikes")){return uuidFemaleSpikes;
		}else if (key.equalsIgnoreCase("flower anatomy")){return uuidFlowerAnatomy;
		}else if (key.equalsIgnoreCase("pileus")){return uuidPileus;
		}else if (key.equalsIgnoreCase("hypodermal cells")){return uuidHypodermalCells;
		}else if (key.equalsIgnoreCase("albumen")){return uuidAlbumen;
		}else if (key.equalsIgnoreCase("male parts")){return uuidMaleParts;
		}else if (key.equalsIgnoreCase("floral scapes")){return uuidFloralScapes;
		}else if (key.equalsIgnoreCase("introducedspecies")){return uuidIntroducedspecies;
		}else if (key.equalsIgnoreCase("berries")){return uuidBerries;
		}else if (key.equalsIgnoreCase("base")){return uuidBase;
		}else if (key.equalsIgnoreCase("flower shape")){return uuidFlowerShape;
		}else if (key.equalsIgnoreCase("caruncle")){return uuidCaruncle;
		}else if (key.equalsIgnoreCase("rhizome")){return uuidRhizome;
		}else if (key.equalsIgnoreCase("appendages")){return uuidAppendages;
		}else if (key.equalsIgnoreCase("gynophore")){return uuidGynophore;
		}else if (key.equalsIgnoreCase("inflorescence axes")){return uuidInflorescenceAxes;
		}else if (key.equalsIgnoreCase("replum")){return uuidReplum;
		}else if (key.equalsIgnoreCase("capsules")){return uuidCapsules;
		}else if (key.equalsIgnoreCase("sheaths")){return uuidSheaths;
		}else if (key.equalsIgnoreCase("spikelets")){return uuidSpikelets;
		}else if (key.equalsIgnoreCase("mesocarps")){return uuidMesocarps;
		}else if (key.equalsIgnoreCase("false flowers")){return uuidFalseFlowers;
		}else if (key.equalsIgnoreCase("lodicules")){return uuidLodicules;
		}else if (key.equalsIgnoreCase("caryopses")){return uuidCaryopses;
		}else if (key.equalsIgnoreCase("bisexual spikelets")){return uuidBisexualSpikelets;
		}else if (key.equalsIgnoreCase("unisexual spikelets")){return uuidUnisexualSpikelets;
		}else if (key.equalsIgnoreCase("mature spikelets")){return uuidMatureSpikelets;
		}else if (key.equalsIgnoreCase("lemma insertion point")){return uuidLemmaInsertionPoint;
		}else if (key.equalsIgnoreCase("glumes")){return uuidGlumes;
		}else if (key.equalsIgnoreCase("inferior flowers")){return uuidInferiorFlowers;
		}else if (key.equalsIgnoreCase("superior flowers")){return uuidSuperiorFlowers;
		}else if (key.equalsIgnoreCase("leaf sheath")){return uuidLeafSheath;
		}else if (key.equalsIgnoreCase("inferior lemma")){return uuidInferiorLemma;
		}else if (key.equalsIgnoreCase("inferior glumes")){return uuidInferiorGlumes;
		}else if (key.equalsIgnoreCase("first internode")){return uuidFirstInternode;
		}else if (key.equalsIgnoreCase("extravaginal innovations")){return uuidExtravaginalInnovations;
		}else if (key.equalsIgnoreCase("innovations")){return uuidInnovations;
		}else if (key.equalsIgnoreCase("cataphylls")){return uuidCataphylls;
		}else if (key.equalsIgnoreCase("ligula")){return uuidLigula;
		}else if (key.equalsIgnoreCase("superior glumes")){return uuidSuperiorGlumes;
		}else if (key.equalsIgnoreCase("false spikes")){return uuidFalseSpikes;
		}else if (key.equalsIgnoreCase("palea")){return uuidPalea;
		}else if (key.equalsIgnoreCase("secondary racemes")){return uuidSecondaryRacemes;
		}else if (key.equalsIgnoreCase("superior lemma")){return uuidSuperiorLemma;
		}else if (key.equalsIgnoreCase("sterile lemma")){return uuidSterileLemma;
		}else if (key.equalsIgnoreCase("reproduction")){return uuidReproduction;
		}else if (key.equalsIgnoreCase("fertile lemma")){return uuidFertileLemma;
		}else if (key.equalsIgnoreCase("division")){return uuidDivision;
		}else if (key.equalsIgnoreCase("sessile spikelets")){return uuidSessileSpikelets;
		}else if (key.equalsIgnoreCase("involucral bracts")){return uuidInvolucralBracts;
		}else if (key.equalsIgnoreCase("superior flower lemma")){return uuidSuperiorFlowerLemma;
		}else if (key.equalsIgnoreCase("aroma")){return uuidAroma;
		}else if (key.equalsIgnoreCase("glumellules")){return uuidGlumellules;
		}else if (key.equalsIgnoreCase("rachis articles")){return uuidRachisArticles;
		}else if (key.equalsIgnoreCase("pedicel spikelets")){return uuidPedicelSpikelets;
		}else if (key.equalsIgnoreCase("carina")){return uuidCarina;
		}else if (key.equalsIgnoreCase("false fruits")){return uuidFalseFruits;
		}else if (key.equalsIgnoreCase("articles")){return uuidArticles;
		}else if (key.equalsIgnoreCase("inferior flower lemma")){return uuidInferiorFlowerLemma;
		}else if (key.equalsIgnoreCase("hyaline lemma")){return uuidHyalineLemma;
		}else if (key.equalsIgnoreCase("callus")){return uuidCallus;
		}else if (key.equalsIgnoreCase("ridges")){return uuidRidges;
		}else if (key.equalsIgnoreCase("joints")){return uuidJoints;
		}else if (key.equalsIgnoreCase("inferior flower palea")){return uuidInferiorFlowerPalea;
		}else if (key.equalsIgnoreCase("inferior glume shape")){return uuidInferiorGlumeShape;
		}else if (key.equalsIgnoreCase("superior glume shape")){return uuidSuperiorGlumeShape;
		}else if (key.equalsIgnoreCase("fertile spikelets")){return uuidFertileSpikelets;
		}else if (key.equalsIgnoreCase("internodes")){return uuidInternodes;
		}else if (key.equalsIgnoreCase("spathe")){return uuidSpathe;
		}else if (key.equalsIgnoreCase("last leaf")){return uuidLastLeaf;
		}else if (key.equalsIgnoreCase("spatheoles")){return uuidSpatheoles;
		}else if (key.equalsIgnoreCase("inferior raceme base")){return uuidInferiorRacemeBase;
		}else if (key.equalsIgnoreCase("lemma ridges")){return uuidLemmaRidges;
		}else if (key.equalsIgnoreCase("raceme base")){return uuidRacemeBase;
		}else if (key.equalsIgnoreCase("stem tufts")){return uuidStemTufts;
		}else if (key.equalsIgnoreCase("spikelet pairs")){return uuidSpikeletPairs;
		}else if (key.equalsIgnoreCase("glume insertion point")){return uuidGlumeInsertionPoint;
		}else if (key.equalsIgnoreCase("dehiscence")){return uuidDehiscence;
		}else if (key.equalsIgnoreCase("glumes and lemmas")){return uuidGlumesAndLemmas;
		}else if (key.equalsIgnoreCase("pericarp")){return uuidPericarp;
		}else if (key.equalsIgnoreCase("stem base")){return uuidStemBase;
		}else if (key.equalsIgnoreCase("male spikelets")){return uuidMaleSpikelets;
		}else if (key.equalsIgnoreCase("female spikelets")){return uuidFemaleSpikelets;
		}else if (key.equalsIgnoreCase("lamina margins")){return uuidLaminaMargins;
		}else if (key.equalsIgnoreCase("juvenile trunk")){return uuidJuvenileTrunk;
		}else if (key.equalsIgnoreCase("floriferous shoots")){return uuidFloriferousShoots;
		}else if (key.equalsIgnoreCase("nectaries")){return uuidNectaries;
		}else if (key.equalsIgnoreCase("perianth divisions")){return uuidPerianthDivisions;
		}else if (key.equalsIgnoreCase("sterile flowers")){return uuidSterileFlowers;
		}else if (key.equalsIgnoreCase("male flower tepals")){return uuidMaleFlowerTepals;
		}else if (key.equalsIgnoreCase("elementary inflorescences")){return uuidElementaryInflorescences;
		}else if (key.equalsIgnoreCase("dry leaves")){return uuidDryLeaves;
		}else if (key.equalsIgnoreCase("inferior part of the perianth")){return uuidInferiorPartOfThePerianth;
		}else if (key.equalsIgnoreCase("anthocarps")){return uuidAnthocarps;
		}else if (key.equalsIgnoreCase("glossary")){return uuidGlossary;
		}else if (key.equalsIgnoreCase("geography")){return uuidGeography;
		}else if (key.equalsIgnoreCase("fertile leaves")){return uuidFertileLeaves;
		}else if (key.equalsIgnoreCase("strobili")){return uuidStrobili;
		}else if (key.equalsIgnoreCase("sporophylls")){return uuidSporophylls;
		}else if (key.equalsIgnoreCase("stem morphology")){return uuidStemMorphology;
		}else if (key.equalsIgnoreCase("primary stems")){return uuidPrimaryStems;
		}else if (key.equalsIgnoreCase("ventral rhizophores")){return uuidVentralRhizophores;
		}else if (key.equalsIgnoreCase("rhizophores")){return uuidRhizophores;
		}else if (key.equalsIgnoreCase("lateral branching outline")){return uuidLateralBranchingOutline;
		}else if (key.equalsIgnoreCase("primary stem leaves")){return uuidPrimaryStemLeaves;
		}else if (key.equalsIgnoreCase("main branches lateral leaves")){return uuidMainBranchesLateralLeaves;
		}else if (key.equalsIgnoreCase("ultimate branching lateral leaves")){return uuidUltimateBranchingLateralLeaves;
		}else if (key.equalsIgnoreCase("margins")){return uuidMargins;
		}else if (key.equalsIgnoreCase("axillary leaves")){return uuidAxillaryLeaves;
		}else if (key.equalsIgnoreCase("median leaves")){return uuidMedianLeaves;
		}else if (key.equalsIgnoreCase("megaspore")){return uuidMegaspore;
		}else if (key.equalsIgnoreCase("microspores")){return uuidMicrospores;
		}else if (key.equalsIgnoreCase("ultimate ramules")){return uuidUltimateRamules;
		}else if (key.equalsIgnoreCase("megasporangia")){return uuidMegasporangia;
		}else if (key.equalsIgnoreCase("branched parts")){return uuidBranchedParts;
		}else if (key.equalsIgnoreCase("lateral leaves")){return uuidLateralLeaves;
		}else if (key.equalsIgnoreCase("soboles")){return uuidSoboles;
		}else if (key.equalsIgnoreCase("false veins")){return uuidFalseVeins;
		}else if (key.equalsIgnoreCase("marks")){return uuidMarks;
		}else if (key.equalsIgnoreCase("lower half of leaves")){return uuidLowerHalfOfLeaves;
		}else if (key.equalsIgnoreCase("ventral sporophylls")){return uuidVentralSporophylls;
		}else if (key.equalsIgnoreCase("sporophytes")){return uuidSporophytes;
		}else if (key.equalsIgnoreCase("gametophytes")){return uuidGametophytes;
		}else if (key.equalsIgnoreCase("segments")){return uuidSegments;
		}else if (key.equalsIgnoreCase("prothallium")){return uuidProthallium;
		}else if (key.equalsIgnoreCase("vegetative fronds")){return uuidVegetativeFronds;
		}else if (key.equalsIgnoreCase("fertile fronds")){return uuidFertileFronds;
		}else if (key.equalsIgnoreCase("free veins")){return uuidFreeVeins;
		}else if (key.equalsIgnoreCase("texture")){return uuidTexture;
		}else if (key.equalsIgnoreCase("colour")){return uuidColour;
		}else if (key.equalsIgnoreCase("microsporangia")){return uuidMicrosporangia;
		}else if (key.equalsIgnoreCase("macrosporangia")){return uuidMacrosporangia;
		}else if (key.equalsIgnoreCase("macrospores")){return uuidMacrospores;
		}else if (key.equalsIgnoreCase("sterile pinnules")){return uuidSterilePinnules;
		}else if (key.equalsIgnoreCase("fertile pinnules")){return uuidFertilePinnules;
		}else if (key.equalsIgnoreCase("final bifurcations")){return uuidFinalBifurcations;
		}else if (key.equalsIgnoreCase("pennate veins")){return uuidPennateVeins;
		}else if (key.equalsIgnoreCase("frond distance")){return uuidFrondDistance;
		}else if (key.equalsIgnoreCase("lower pinnae")){return uuidLowerPinnae;
		}else if (key.equalsIgnoreCase("cells")){return uuidCells;
		}else if (key.equalsIgnoreCase("marginal false veins")){return uuidMarginalFalseVeins;
		}else if (key.equalsIgnoreCase("frond apex")){return uuidFrondApex;
		}else if (key.equalsIgnoreCase("paraphyses")){return uuidParaphyses;
		}else if (key.equalsIgnoreCase("lateral pinnae")){return uuidLateralPinnae;
		}else if (key.equalsIgnoreCase("stolons")){return uuidStolons;
		}else if (key.equalsIgnoreCase("hydathodes")){return uuidHydathodes;
		}else if (key.equalsIgnoreCase("sterile pinnae")){return uuidSterilePinnae;
		}else if (key.equalsIgnoreCase("lamina apex")){return uuidLaminaApex;
		}else if (key.equalsIgnoreCase("juvenile fronds")){return uuidJuvenileFronds;
		}else if (key.equalsIgnoreCase("mature fronds")){return uuidMatureFronds;
		}else if (key.equalsIgnoreCase("fertile lamina")){return uuidFertileLamina;
		}else if (key.equalsIgnoreCase("pinnae apex")){return uuidPinnaeApex;
		}else if (key.equalsIgnoreCase("lamina inferior surfaces")){return uuidLaminaInferiorSurfaces;
		}else if (key.equalsIgnoreCase("terminal pinnae")){return uuidTerminalPinnae;
		}else if (key.equalsIgnoreCase("lamina shape")){return uuidLaminaShape;
		}else if (key.equalsIgnoreCase("furrows")){return uuidFurrows;
		}else if (key.equalsIgnoreCase("first pair of pinnae")){return uuidFirstPairOfPinnae;
		}else if (key.equalsIgnoreCase("costal areoles")){return uuidCostalAreoles;
		}else if (key.equalsIgnoreCase("humus collecting fronds")){return uuidHumusCollectingFronds;
		}else if (key.equalsIgnoreCase("assimilating fronds")){return uuidAssimilatingFronds;
		}else if (key.equalsIgnoreCase("fertile parts")){return uuidFertileParts;
		}else if (key.equalsIgnoreCase("internal cycle")){return uuidInternalCycle;
		}else if (key.equalsIgnoreCase("outer parts")){return uuidOuterParts;
		}else if (key.equalsIgnoreCase("leafy stems")){return uuidLeafyStems;
		}else if (key.equalsIgnoreCase("outer cycle")){return uuidOuterCycle;
		}else if (key.equalsIgnoreCase("inner cycle")){return uuidInnerCycle;
		}else if (key.equalsIgnoreCase("lateral stimanodes")){return uuidLateralStimanodes;
		}else if (key.equalsIgnoreCase("floriferous stems")){return uuidFloriferousStems;
		}else if (key.equalsIgnoreCase("inferior bracts")){return uuidInferiorBracts;
		}else if (key.equalsIgnoreCase("secondary inflorescences")){return uuidSecondaryInflorescences;
		}else if (key.equalsIgnoreCase("median lobes")){return uuidMedianLobes;
		}else if (key.equalsIgnoreCase("sterile bracts")){return uuidSterileBracts;
		}else if (key.equalsIgnoreCase("fertile bracts")){return uuidFertileBracts;
		}else if (key.equalsIgnoreCase("central lobes")){return uuidCentralLobes;
		}else if (key.equalsIgnoreCase("corolla lobes")){return uuidCorollaLobes;
		}else if (key.equalsIgnoreCase("outer bracts")){return uuidOuterBracts;
		}else if (key.equalsIgnoreCase("inner bracts")){return uuidInnerBracts;
		}else if (key.equalsIgnoreCase("aerial parts")){return uuidAerialParts;
		}else if (key.equalsIgnoreCase("outer staminodes")){return uuidOuterStaminodes;
		}else if (key.equalsIgnoreCase("sheath and ligula")){return uuidSheathAndLigula;
		}else if (key.equalsIgnoreCase("sheath-ligula separation")){return uuidSheathLigulaSeparation;
		}else if (key.equalsIgnoreCase("axils")){return uuidAxils;
		}else if (key.equalsIgnoreCase("sixth stamen")){return uuidSixthStamen;
		}else if (key.equalsIgnoreCase("leaf symmetry")){return uuidLeafSymmetry;
		}else if (key.equalsIgnoreCase("abaxial bracts")){return uuidAbaxialBracts;
		}else if (key.equalsIgnoreCase("callous part of the petiole")){return uuidCallousPartOfThePetiole;
		}else if (key.equalsIgnoreCase("inner staminodes")){return uuidInnerStaminodes;
		}else if (key.equalsIgnoreCase("non-callous part of the petiole")){return uuidNonCallousPartOfThePetiole;
		}else if (key.equalsIgnoreCase("perispermatic canal")){return uuidPerispermaticCanal;
		}else if (key.equalsIgnoreCase("adaxial bracts")){return uuidAdaxialBracts;
		}else if (key.equalsIgnoreCase("callous and non-callous parts of the petiole")){return uuidCallousAndNonCallousPartsOfThePetiole;
		}else if (key.equalsIgnoreCase("transition to median veins")){return uuidTransitionToMedianVeins;
		}else if (key.equalsIgnoreCase("leaf dimensions")){return uuidLeafDimensions;
		}else if (key.equalsIgnoreCase("nodes")){return uuidNodes;
		}else if (key.equalsIgnoreCase("dimensions")){return uuidDimensions;
		}else if (key.equalsIgnoreCase("inner staminodial cycle")){return uuidInnerStaminodialCycle;
		}else if (key.equalsIgnoreCase("envelope")){return uuidEnvelope;
		}else if (key.equalsIgnoreCase("median transversal section")){return uuidMedianTransversalSection;
		}else if (key.equalsIgnoreCase("adaxial spathe-bracts")){return uuidAdaxialSpatheBracts;
		}else if (key.equalsIgnoreCase("transition to lamina")){return uuidTransitionToLamina;
		}else if (key.equalsIgnoreCase("flower arrangement")){return uuidFlowerArrangement;
		}else if (key.equalsIgnoreCase("second outer staminode")){return uuidSecondOuterStaminode;
		}else if (key.equalsIgnoreCase("secretory structures")){return uuidSecretoryStructures;
		}else if (key.equalsIgnoreCase("median tepals")){return uuidMedianTepals;
		}else if (key.equalsIgnoreCase("third cycle")){return uuidThirdCycle;
		}else if (key.equalsIgnoreCase("mature leaves")){return uuidMatureLeaves;
		}else if (key.equalsIgnoreCase("exocarp")){return uuidExocarp;
		}else if (key.equalsIgnoreCase("outer stamens")){return uuidOuterStamens;
		}else if (key.equalsIgnoreCase("trunk diameter")){return uuidTrunkDiameter;
		}else if (key.equalsIgnoreCase("fruit colour")){return uuidFruitColour;
		}else if (key.equalsIgnoreCase("fruit pulp")){return uuidFruitPulp;
		}else if (key.equalsIgnoreCase("marginal veins")){return uuidMarginalVeins;
		}else if (key.equalsIgnoreCase("operculum")){return uuidOperculum;
		}else if (key.equalsIgnoreCase("fibres")){return uuidFibres;
		}else if (key.equalsIgnoreCase("juvenile flower buds")){return uuidJuvenileFlowerBuds;
		}else if (key.equalsIgnoreCase("plurilocular ovary")){return uuidPlurilocularOvary;
		}else if (key.equalsIgnoreCase("external parts")){return uuidExternalParts;
		}else if (key.equalsIgnoreCase("lamina colour")){return uuidLaminaColour;
		}else if (key.equalsIgnoreCase("flower position")){return uuidFlowerPosition;
		}else if (key.equalsIgnoreCase("tertiary veins")){return uuidTertiaryVeins;
		}else if (key.equalsIgnoreCase("fruit anatomy")){return uuidFruitAnatomy;
		}else if (key.equalsIgnoreCase("juvenile plants")){return uuidJuvenilePlants;
		}else if (key.equalsIgnoreCase("large flowers")){return uuidLargeFlowers;
		}else if (key.equalsIgnoreCase("open flowers")){return uuidOpenFlowers;
		}else if (key.equalsIgnoreCase("cupules")){return uuidCupules;
		}else if (key.equalsIgnoreCase("prickles")){return uuidPrickles;
		}else if (key.equalsIgnoreCase("median folioles")){return uuidMedianFolioles;
		}else if (key.equalsIgnoreCase("staminodial appendix")){return uuidStaminodialAppendix;
		}else if (key.equalsIgnoreCase("aerial stems")){return uuidAerialStems;
		}else if (key.equalsIgnoreCase("superior sepals")){return uuidSuperiorSepals;
		}else if (key.equalsIgnoreCase("superior petals")){return uuidSuperiorPetals;
		}else if (key.equalsIgnoreCase("leaf arrangement")){return uuidLeafArrangement;
		}else if (key.equalsIgnoreCase("unifoliate leaves")){return uuidUnifoliateLeaves;
		}else if (key.equalsIgnoreCase("trifoliate leaves")){return uuidTrifoliateLeaves;
		}else if (key.equalsIgnoreCase("5-foliate leaves")){return uuid5FoliateLeaves;
		}else if (key.equalsIgnoreCase("floral bract stipules")){return uuidFloralBractStipules;
		}else if (key.equalsIgnoreCase("4-foliate leaves")){return uuid4FoliateLeaves;
		}else if (key.equalsIgnoreCase("compound leaf petiolules")){return uuidCompoundLeafPetiolules;
		}else if (key.equalsIgnoreCase("bract stipules")){return uuidBractStipules;
		}else if (key.equalsIgnoreCase("leaf division")){return uuidLeafDivision;
		}else if (key.equalsIgnoreCase("simple leaves")){return uuidSimpleLeaves;
		}else if (key.equalsIgnoreCase("compound leaves")){return uuidCompoundLeaves;
		}else if (key.equalsIgnoreCase("foliole texture")){return uuidFolioleTexture;
		}else if (key.equalsIgnoreCase("fruit wall")){return uuidFruitWall;

		
		//guianas
		}else if (key.equalsIgnoreCase("extraxylary sclerenchyma")){return uuidExtraxylarySclerenchyma;
		
		
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
		if (CdmUtils.isBlank(key)){return null;
//		}else if (key.equalsIgnoreCase("distribution")){return MarkerType.;
//		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
		}else{
			return null;
		}
	}

	@Override
	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("INCOMPLETELY KNOWN SPECIES")){return uuidIncompleteTaxon;
		}else if (key.equalsIgnoreCase("INSUFICIENTLY KNOWN")){return uuidIncompleteTaxon;
		}else if (key.equalsIgnoreCase("INSUFFICIENTLY KNOWN")){return uuidIncompleteTaxon;
		}else if (key.equalsIgnoreCase("IMPERFECTLY KNOWN SPECIES")){return uuidIncompleteTaxon;
		}else{
			return null;
		}
		
	}
	
	
	
	@Override
	public NamedAreaLevel getNamedAreaLevelByKey(String key )throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("country")){return NamedAreaLevel.COUNTRY();
		}else if (key.equalsIgnoreCase("province")){return NamedAreaLevel.PROVINCE();
		}else if (key.equalsIgnoreCase("town")){return NamedAreaLevel.TOWN();
		}else if (key.equalsIgnoreCase("state")){return NamedAreaLevel.STATE();
		}else if (key.equalsIgnoreCase("tdwg1")){return NamedAreaLevel.TDWG_LEVEL1();
		}else if (key.equalsIgnoreCase("tdwg2")){return NamedAreaLevel.TDWG_LEVEL2();
		}else if (key.equalsIgnoreCase("tdwg3")){return NamedAreaLevel.TDWG_LEVEL3();
		}else if (key.equalsIgnoreCase("tdwg4")){return NamedAreaLevel.TDWG_LEVEL4();
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNamedAreaLevelUuid(java.lang.String)
	 */
	@Override
	public UUID getNamedAreaLevelUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("region")){return uuidRegion;
		}else if (key.equalsIgnoreCase("continental region")){return uuidContinentalRegion;
		}else if (key.equalsIgnoreCase("world")){return uuidWorld;
		}else if (key.equalsIgnoreCase("county")){return uuidCounty;
		}else if (key.equalsIgnoreCase("continent")){return uuidContinent;
		}else if (key.equalsIgnoreCase("district")){return uuidDistrict;
		}else if (key.equalsIgnoreCase("locality")){return uuidLocality;
		}else if (key.equalsIgnoreCase("other")){return uuidLevelOther;
		}else{
			return null;
		}
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNamedAreaByKey(java.lang.String)
	 */
	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("Kalimantan")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BOR-KA");
		}else if (key.equalsIgnoreCase("Borneo")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BOR");
		}else if (key.equalsIgnoreCase("Peninsular Malaysia")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("MLY-PM");
			}else if (key.equalsIgnoreCase("Malay Peninsula")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("MLY-PM");
		}else if (key.equalsIgnoreCase("Java")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("JAW-OO");
		}else if (key.equalsIgnoreCase("Bismarck Archipelago")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BIS-OO");
		}else if (key.equalsIgnoreCase("Sumatra")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SUM-OO");
		}else if (key.equalsIgnoreCase("Sabah")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BOR-SB");
			}else if (key.equalsIgnoreCase("North Borneo")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("BOR-SB");
		
		}else if (key.equalsIgnoreCase("Bali")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("LSI-BA");
		}else if (key.equalsIgnoreCase("Moluccas")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("MOL-OO");
		
		}else if (key.equalsIgnoreCase("Indochina")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("41");
		}else if (key.equalsIgnoreCase("Lesser Sunda Islands")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("LSI");
		}else if (key.equalsIgnoreCase("Sulawesi")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SUL-OO");
			}else if (key.equalsIgnoreCase("Celebes")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("SUL-OO");
		
		}else if (key.equalsIgnoreCase("south-east United States")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("78");
		}else if (key.equalsIgnoreCase("West Indies")){return TdwgAreaProvider.getAreaByTdwgAbbreviation("81");
		
		}else{
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getNamedAreaUuid(java.lang.String)
	 */
	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("Kalimantan")){return uuidKalimantan;
		}else if (key.equalsIgnoreCase("Borneo")){return uuidBorneo;
		}else if (key.equalsIgnoreCase("Moluccas")){return uuidMoluccas;
		}else if (key.equalsIgnoreCase("Peninsular Malaysia")){return uuidPeninsularMalaysia;
		}else if (key.equalsIgnoreCase("Java")){return uuidJava;
		}else if (key.equalsIgnoreCase("Bismarck Archipelago")){return uuidBismarckArchipelago;
//		}else if (key.equalsIgnoreCase("New Ireland")){return uuidNewIreland;
//		}else if (key.equalsIgnoreCase("Celebes")){return uuidSulawesi;
		}else if (key.equalsIgnoreCase("Sumatra")){return uuidSumatra;
		
//		}else if (key.equalsIgnoreCase("Bangka")){return uuidBangka;
		}else if (key.equalsIgnoreCase("Sabah")){return uuidSabah;
		}else if (key.equalsIgnoreCase("Bali")){return uuidBali;
	
		//
		//Celebes (Sulawesi)
		//Moluccas (Ceram, Halmahera, Ambon)
		//Peninsular Malaysia (Kedah, Perak, Kelantan, Trengganu, Pahang, Selangor, Negri Sembilan, Malacca, Johore)
		//Borneo (Sabah, Sarawak, Kalimantan)
		//Sumatra (Aceh, Sumatera Barat, Sumatera Utara)
		//Lesser Sunda Islands (Bali, Lombok, Flores, Timor)

		
		//countries
		}else if (key.equalsIgnoreCase("Philippines")){return uuidPhilippines;
		

		
		}else{
			return null;
		}
	}

	@Override
	public PresenceAbsenceTermBase getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("endemic")){return PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
		}else if (key.equalsIgnoreCase("cultivated")){return PresenceTerm.CULTIVATED();
		}else if (key.equalsIgnoreCase("absent")){return AbsenceTerm.ABSENT();
		}else{
			return null;
		}
	}

	@Override
	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
//		}else if (key.equalsIgnoreCase("region")){return uuidRegion;
		}else{
			return null;
		}
	}
	

	@Override
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		
		//FdG languages
		}else if (key.equalsIgnoreCase("owimo")){return uuidLanguageOwimo;
		}else if (key.equalsIgnoreCase("batanga")){return uuidLanguageBatanga;
		}else if (key.equalsIgnoreCase("galoa")){return uuidLanguageGaloa;
		
		}else if (key.equalsIgnoreCase("apindji")){return uuidLanguageApindji;
		}else if (key.equalsIgnoreCase("baduma")){return uuidLanguageBaduma;
		}else if (key.equalsIgnoreCase("bak\u00E8l\u00E8")){return uuidLanguageBakele;
		}else if (key.equalsIgnoreCase("banzabi")){return uuidLanguageBanzabi;
		}else if (key.equalsIgnoreCase("bapunu")){return uuidLanguageBapunu;
		}else if (key.equalsIgnoreCase("bat\u00E9k\u00E9")){return uuidLanguageBateke;
		}else if (key.equalsIgnoreCase("bavangu")){return uuidLanguageBavangu;
		}else if (key.equalsIgnoreCase("bavarama")){return uuidLanguageBavarama;
		}else if (key.equalsIgnoreCase("bavili")){return uuidLanguageBavili;
		}else if (key.equalsIgnoreCase("bavov\u00E9")){return uuidLanguageBavove;
		}else if (key.equalsIgnoreCase("bavungu")){return uuidLanguageBavungu;
		}else if (key.equalsIgnoreCase("benga")){return uuidLanguageBenga;
		}else if (key.equalsIgnoreCase("b\u00E9s\u00E9ki")){return uuidLanguageBeseki;
		}else if (key.equalsIgnoreCase("eschira")){return uuidLanguageEschira;
		}else if (key.equalsIgnoreCase("\u00E9chira")){return uuidLanguageEchira;
		}else if (key.equalsIgnoreCase("\u00E9shira")){return uuidLanguageEshira;
		}else if (key.equalsIgnoreCase("fang du Fernan-Vaz")){return uuidLanguageFangDuFernanVaz;
		}else if (key.equalsIgnoreCase("fang du Rio-Muni")){return uuidLanguageFangDuRioMuni;
		}else if (key.equalsIgnoreCase("iv\u00E9a")){return uuidLanguageIvea;
		}else if (key.equalsIgnoreCase("loango")){return uuidLanguageLoango;
		}else if (key.equalsIgnoreCase("masangu")){return uuidLanguageMasangu;
		}else if (key.equalsIgnoreCase("mindumu")){return uuidLanguageMindumu;
		}else if (key.equalsIgnoreCase("mitsogho")){return uuidLanguageMitsogho;
		}else if (key.equalsIgnoreCase("mitsogo")){return uuidLanguageMitsogo;
		}else if (key.equalsIgnoreCase("misogo")){return uuidLanguageMisogo;
		}else if (key.equalsIgnoreCase("mpongw\u00E8")){return uuidLanguageMpongw\u00E8;
		}else if (key.equalsIgnoreCase("mpongwe")){return uuidLanguageMpongwe;
		}else if (key.equalsIgnoreCase("ngow\u00E9")){return uuidLanguageNgowe;
		}else if (key.equalsIgnoreCase("nkomi")){return uuidLanguageNkomi;
		}else if (key.equalsIgnoreCase("orungu")){return uuidLanguageOrungu;
		}else if (key.equalsIgnoreCase("simba")){return uuidLanguageSimba;
		
		}else if (key.equalsIgnoreCase("balumbu")){return uuidLanguageBalumbu;
		}else if (key.equalsIgnoreCase("bakota")){return uuidLanguageBakota;
		}else if (key.equalsIgnoreCase("bal\u00E8ngi")){return uuidLanguageBalengi;
		}else if (key.equalsIgnoreCase("\u00E9shira-Tandu")){return uuidLanguageEshiraTandu;
		}else if (key.equalsIgnoreCase("b\u00E9k\u00E9si")){return uuidLanguageBekesi;
		}else if (key.equalsIgnoreCase("balengi")){return uuidLanguageBalengi;
		
		}else if (key.equalsIgnoreCase("nom pilot")){return uuidLanguageNomPilot;
		
		
		}else{
			return null;
		}
	}

	@Override
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("angl.")){return Language.ENGLISH();
		}else if (key.equalsIgnoreCase("fr.")){return Language.FRENCH();
		}else if (key.equalsIgnoreCase("fang")){return Language.FANG();
		
		}else{
			return null;
		}
	}
	
	
	
	
	
	
}
