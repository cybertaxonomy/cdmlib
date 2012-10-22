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
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.TdwgArea;

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
	public static final UUID uuidLanguageFang = UUID.fromString("6f811da1-c821-48bb-8891-c30b4228430e");
	public static final UUID uuidLanguageOwimo = UUID.fromString("1764092c-7826-4b79-bacc-435a9af1320e");
	public static final UUID uuidLanguageBatanga = UUID.fromString("ee277e78-1135-4823-b4ee-63c4b93f04a2");
	
	
	
	
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
//	public static final UUID uuid = UUID.fromString("");

	
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
		}else if (key.equalsIgnoreCase("Kalimantan")){return TdwgArea.getAreaByTdwgAbbreviation("BOR-KA");
		}else if (key.equalsIgnoreCase("Borneo")){return TdwgArea.getAreaByTdwgAbbreviation("BOR");
		}else if (key.equalsIgnoreCase("Peninsular Malaysia")){return TdwgArea.getAreaByTdwgAbbreviation("MLY-PM");
			}else if (key.equalsIgnoreCase("Malay Peninsula")){return TdwgArea.getAreaByTdwgAbbreviation("MLY-PM");
		}else if (key.equalsIgnoreCase("Java")){return TdwgArea.getAreaByTdwgAbbreviation("JAW-OO");
		}else if (key.equalsIgnoreCase("Bismarck Archipelago")){return TdwgArea.getAreaByTdwgAbbreviation("BIS-OO");
		}else if (key.equalsIgnoreCase("Sumatra")){return TdwgArea.getAreaByTdwgAbbreviation("SUM-OO");
		}else if (key.equalsIgnoreCase("Sabah")){return TdwgArea.getAreaByTdwgAbbreviation("BOR-SB");
			}else if (key.equalsIgnoreCase("North Borneo")){return TdwgArea.getAreaByTdwgAbbreviation("BOR-SB");
		
		}else if (key.equalsIgnoreCase("Bali")){return TdwgArea.getAreaByTdwgAbbreviation("LSI-BA");
		}else if (key.equalsIgnoreCase("Moluccas")){return TdwgArea.getAreaByTdwgAbbreviation("MOL-OO");
		
		}else if (key.equalsIgnoreCase("Indochina")){return TdwgArea.getAreaByTdwgAbbreviation("41");
		}else if (key.equalsIgnoreCase("Lesser Sunda Islands")){return TdwgArea.getAreaByTdwgAbbreviation("LSI");
		}else if (key.equalsIgnoreCase("Sulawesi")){return TdwgArea.getAreaByTdwgAbbreviation("SUL-OO");
			}else if (key.equalsIgnoreCase("Celebes")){return TdwgArea.getAreaByTdwgAbbreviation("SUL-OO");
		
		}else if (key.equalsIgnoreCase("south-east United States")){return TdwgArea.getAreaByTdwgAbbreviation("78");
		}else if (key.equalsIgnoreCase("West Indies")){return TdwgArea.getAreaByTdwgAbbreviation("81");
		
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getPresenceTermByKey(java.lang.String)
	 */
	@Override
	public PresenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.equalsIgnoreCase("endemic")){return PresenceTerm.ENDEMIC_FOR_THE_RELEVANT_AREA();
		}else if (key.equalsIgnoreCase("cultivated")){return PresenceTerm.CULTIVATED();
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getPresenceTermUuid(java.lang.String)
	 */
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
		}else if (key.equalsIgnoreCase("fang")){return uuidLanguageFang;
		}else if (key.equalsIgnoreCase("owimo")){return uuidLanguageOwimo;
		}else if (key.equalsIgnoreCase("batanga")){return uuidLanguageBatanga;
		
		}else{
			return null;
		}
	}
	
	
	
	
}
