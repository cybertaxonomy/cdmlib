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
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public class MarkupTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(MarkupTransformer.class);
	
	
	//extension type uuids
	public static final UUID uuidTaxonTitle = UUID.fromString("5d9ca987-81f1-4d6c-b06a-eaa8311ca249");
	public static final UUID uuidWriter = UUID.fromString("43f988cb-bc53-4324-a702-c8f920656975");
	
	//annotation type uuids
	public static final UUID uuidFootnote = UUID.fromString("b91fab29-7d26-4277-b549-262da0d901b1");
	
	
	//marker type uuid
//	public static final UUID uuidExcludedTaxon = UUID.fromString("e729a22d-8c94-4859-9f91-3e3ae212c91d");
	public static final UUID uuidIncompleteTaxon = UUID.fromString("cb34d525-de64-4569-b277-3429ec49a09f");

	
	
	//feature uuids
	
	public static final UUID uuidFigure = UUID.fromString("5165cd6a-9b31-4a1f-8b30-04ab740c502c");
	public static final UUID uuidFigures = UUID.fromString("6dfb4e78-c67e-410c-8989-c1fb1295abf6");
	

	public static final UUID uuidHabit = UUID.fromString("03487108-173a-4335-92be-05076af29155");
	public static final UUID uuidHabitat = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
	public static final UUID uuidHabitatEcology = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
	
	public static final UUID uuidChromosomes = UUID.fromString("c4a60319-4978-4692-9545-58d60cf8379e");
	
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
	

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		
		}else if (key.equalsIgnoreCase("ecology")){return Feature.ECOLOGY();
		}else if (key.equalsIgnoreCase("uses")){return Feature.USES();
		}else if (key.equalsIgnoreCase("anatomy")){return Feature.ANATOMY();
		}else if (key.equalsIgnoreCase("description")){return Feature.DESCRIPTION();
		}else if (key.equalsIgnoreCase("distribution")){return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.HABITAT_ECOLOGY();
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

		}else if (key.equalsIgnoreCase("Chromosomes")){return uuidChromosomes;
		}else if (key.equalsIgnoreCase("habit")){return uuidHabit;
		}else if (key.equalsIgnoreCase("Habitat")){return uuidHabitat;
		}else if (key.equalsIgnoreCase("Habitat & Ecology")){return uuidHabitatEcology;
		}else if (key.equalsIgnoreCase("Leaflets")){return uuidLeaflets;
		}else if (key.equalsIgnoreCase("Leaves")){return uuidLeaves;
		}else if (key.equalsIgnoreCase("Branchlets")){return uuidBranchlets;
		}else if (key.equalsIgnoreCase("lifeform")){return uuidLifeform;
		}else if (key.equalsIgnoreCase("Inflorescences")){return uuidInflorescences;
		}else if (key.equalsIgnoreCase("Flowers")){return uuidFlowers;
		}else if (key.equalsIgnoreCase("Sepals")){return uuidSepals;
		}else if (key.equalsIgnoreCase("Outer Sepals")){return uuidOuterSepals;
		}else if (key.equalsIgnoreCase("Anthers")){return uuidAnthers;
		}else if (key.equalsIgnoreCase("Petals")){return uuidPetals;
		}else if (key.equalsIgnoreCase("Petal")){return uuidPetal;
		}else if (key.equalsIgnoreCase("Disc")){return uuidDisc;
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
		}else if (key.equalsIgnoreCase("Ovules")){return uuidOvules;
		}else if (key.equalsIgnoreCase("Female")){return uuidFemaleFlowers;
		}else if (key.equalsIgnoreCase("Style")){return uuidStyle;
		}else if (key.equalsIgnoreCase("Arillode")){return uuidArillode;
		}else if (key.equalsIgnoreCase("Fruit")){return uuidFruit;
		}else if (key.equalsIgnoreCase("Branch")){return uuidBranch;
		}else if (key.equalsIgnoreCase("Inflorescence")){return uuidInflorescence;
		}else if (key.equalsIgnoreCase("male inflorescences")){return uuidMaleInflorescences;
		}else if (key.equalsIgnoreCase("female inflorescences")){return uuidFemaleInflorescences;
		
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
		}else if (key.equalsIgnoreCase("Pollen morphology")){return uuidPollenMorphology;
		}else if (key.equalsIgnoreCase("Vegetative morphology and anatomy")){return uuidVegetativeMorphologyAndAnatomy;
		}else if (key.equalsIgnoreCase("Flower morphology")){return uuidFlowerMorphology;
		}else if (key.equalsIgnoreCase("Pollination")){return uuidPollination;
		}else if (key.equalsIgnoreCase("Life cycle")){return uuidLifeCycle;
		}else if (key.equalsIgnoreCase("Fruits and embryology")){return uuidFruitsAndEmbryology;
		}else if (key.equalsIgnoreCase("Dispersal")){return uuidDispersal;
		}else if (key.equalsIgnoreCase("Phytochemistry")){return uuidPhytochemistry;
		}else if (key.equalsIgnoreCase("phytochemo")){return uuidPhytochemistry;
		}else if (key.equalsIgnoreCase("Fossils")){return uuidFossils;
		}else if (key.equalsIgnoreCase("Morphology and anatomy")){return uuidMorphologyAndAnatomy;
		}else if (key.equalsIgnoreCase("embryology")){return uuidEmbryology;
		}else if (key.equalsIgnoreCase("cytology")){return uuidCytology;
		
		
		
		
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
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("country")){return NamedAreaLevel.COUNTRY();
//		}else if (key.equalsIgnoreCase("continent")){return NamedAreaLevel.C();
		}else if (key.equalsIgnoreCase("province")){return NamedAreaLevel.PROVINCE();
//		}else if (key.equalsIgnoreCase("region")){return NamedAreaLevel.REGION;
//		}else if (key.equalsIgnoreCase("county")){return NamedAreaLevel.COUNTRY();
		}else if (key.equalsIgnoreCase("state")){return NamedAreaLevel.STATE();

//		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
		}else{
			return null;
		}
	}
	
	
	
}
