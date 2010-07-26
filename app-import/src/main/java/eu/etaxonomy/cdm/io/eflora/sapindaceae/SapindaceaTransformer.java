// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.sapindaceae;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class SapindaceaTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(SapindaceaTransformer.class);
	
	//feature uuids

	public static final UUID uuidHabitat = UUID.fromString("fb16929f-bc9c-456f-9d40-dec987b36438");
	public static final UUID uuidHabitatEcology = UUID.fromString("9fdc4663-4d56-47d0-90b5-c0bf251bafbb");
	
	public static final UUID uuidChromosomes = UUID.fromString("c4a60319-4978-4692-9545-58d60cf8379e");
	
	public static final UUID uuidLeaflets = UUID.fromString("0efcfbb5-7f7a-454f-985e-50cea6523fef");
	public static final UUID uuidLeaves = UUID.fromString("378c6d5f-4f8a-4769-b054-50ddaff6f080");
	public static final UUID uuidBranchlets = UUID.fromString("e63af3b4-aaff-4b4d-a8fe-3b13b79974c8");
	public static final UUID uuidLifeform = UUID.fromString("db9228d3-8bbf-4460-abfe-0b1326c82f8e");
	public static final UUID uuidInflorescences = UUID.fromString("c476f5fb-dc06-4408-af36-f48e625f5767");
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
	public static final UUID uuidFigure = UUID.fromString("5165cd6a-9b31-4a1f-8b30-04ab740c502c");
	public static final UUID uuidFigures = UUID.fromString("6dfb4e78-c67e-410c-8989-c1fb1295abf6");
	public static final UUID uuidPistil = UUID.fromString("51df329b-2b2b-4f45-960c-bf4430be5910");
	public static final UUID uuidOvary = UUID.fromString("0757d8bc-d91c-4482-bde0-d239b4122934");
	public static final UUID uuidTwigs = UUID.fromString("e1eb9d5e-1397-4a4e-84e7-483e77822c6b");
	public static final UUID uuidBranches = UUID.fromString("7c515e4a-9a6f-4d4d-9af7-c0c4039dcf27");
	public static final UUID uuidInfructescences = UUID.fromString("e60fbb4f-cf4e-4331-9dcd-d65f640eb669");
	public static final UUID uuidPistillode = UUID.fromString("7c91c9ae-ad30-4aca-96b8-249c154fb296");
	public static final UUID uuidFlower = UUID.fromString("27a04dae-3a46-41ec-a36f-866561a0f8db");
	public static final UUID uuidOvules = UUID.fromString("e118915a-0d6c-41b9-9385-9f18d852e0bc");
	public static final UUID uuidFemale = UUID.fromString("fe708a69-150d-41fb-b391-dc8d9c1b8d1a");
	public static final UUID uuidStyle = UUID.fromString("6b5ae8fb-72e4-4c60-9bbe-0abc9edb09c3");
	public static final UUID uuidArillode = UUID.fromString("d113362e-06cb-42c8-96c7-4df6bef9cb29");
	public static final UUID uuidFruit = UUID.fromString("05442d43-045d-4632-9a1e-d2eada227490");
	public static final UUID uuidBranch = UUID.fromString("71b7507c-9d04-49c9-b155-398b957b4aea");
	public static final UUID uuidInflorescence = UUID.fromString("736cd249-f2dc-4fe3-a127-2c7582e330f6");
	public static final UUID uuidCalyx = UUID.fromString("48a7fa54-1aef-4209-8df0-26a8148156af");
	public static final UUID uuidSeedling = UUID.fromString("7d977209-1579-44c9-a996-9eca1fb93cfc");
	public static final UUID uuidStaminodes = UUID.fromString("4c135e5d-805b-4591-b21f-bbc34e275ef6");
	public static final UUID uuidFilaments = UUID.fromString("5d61bc65-4621-488a-8ea9-11f6e4cd2c66");

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
	
	
	
	

	//extension type uuids

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.equalsIgnoreCase("distribution")){return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("habitatecology")){return Feature.ECOLOGY();
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
		}else if (key.equalsIgnoreCase("Female")){return uuidFemale;
		}else if (key.equalsIgnoreCase("Style")){return uuidStyle;
		}else if (key.equalsIgnoreCase("Arillode")){return uuidArillode;
		}else if (key.equalsIgnoreCase("Fruit")){return uuidFruit;
		}else if (key.equalsIgnoreCase("Branch")){return uuidBranch;
		}else if (key.equalsIgnoreCase("Inflorescence")){return uuidInflorescence;
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
		}else if (key.equalsIgnoreCase("Chromosomes")){return uuidChromosomes;
		}else if (key.equalsIgnoreCase("Axillary")){return uuidAxillary;
		}else if (key.equalsIgnoreCase("Petiolules")){return uuidPetiolules;
		}else if (key.equalsIgnoreCase("Male flowers")){return uuidMaleFlowers;
		}else if (key.equalsIgnoreCase("Young inflorescences")){return uuidYoungInflorescences;
		}else if (key.equalsIgnoreCase("Sepal")){return uuidSepal;
		}else if (key.equalsIgnoreCase("Thyrses")){return uuidThyrses;
		}else if (key.equalsIgnoreCase("Thyrsus")){return uuidThyrsus;
		}else if (key.equalsIgnoreCase("Bark")){return uuidBark;
		
		
		}else{
			return null;
		}
		
	}
	
	
	
}
