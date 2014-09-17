/**
 * 
 */
package eu.etaxonomy.cdm.io.algaterra;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @created 13.09.2012
 *
 */
public class AlgaTerraImportTransformer extends BerlinModelTransformer {
	private static final Logger logger = Logger.getLogger(AlgaTerraImportTransformer.class);

	// feature - parameter
	public static final UUID uuidFeaturePH = UUID.fromString("99354dac-0695-44b3-a0e6-c4d4f5684006");
	public static final UUID uuidFeatureConductivity = UUID.fromString("7ddb72ef-dd88-4320-848b-1c36de548459");
	public static final UUID uuidFeatureWaterTemperature = UUID.fromString("4b45fb58-c6d0-486d-82b7-e1355d177859");
	public static final UUID uuidFeatureSilica = UUID.fromString("435ff3a7-b117-4eda-bdc7-b68b1e2403d9");
	public static final UUID uuidFeatureNitrate = UUID.fromString("7d527547-2d2f-405a-bc07-f69884acc3dd");
	public static final UUID uuidFeatureAmmonium = UUID.fromString("1245d380-ab78-4aef-bcdc-c23bd207bb52");
	public static final UUID uuidFeatureNitrite = UUID.fromString("0012a3eb-abc5-4022-abbe-286658d6a6dc");
	public static final UUID uuidFeaturePhosphate = UUID.fromString("f60b5fa6-6600-47e4-9c6d-ca341e3a440c");
	public static final UUID uuidFeatureDIN = UUID.fromString("1f54453b-91ab-4e15-9664-f21aed90bc8c");
	public static final UUID uuidFeatureNPRation = UUID.fromString("5ee7741b-da0e-4e0e-8362-1d277350f6ad");
	public static final UUID uuidFeatureSRP = UUID.fromString("94977e8d-8cad-41d3-bb24-1252474fa884");
	public static final UUID uuidFeatureOxygenSaturation = UUID.fromString("23c0ca33-ad6b-4cd0-96da-98ab3573deb8");
	public static final UUID uuidFeatureCommunity = UUID.fromString("3ff5b1ab-3999-4b5a-b8f7-01fd2f6c12c7");
	public static final UUID uuidFeatureCl = UUID.fromString("0eacde9f-d245-4452-aade-28eb849155e8");
	public static final UUID uuidFeatureOrthoPhosphate = UUID.fromString("00281ea0-5cb4-4b2f-93f1-c59d6e11cb41");
	public static final UUID uuidFeatureSecchiDepth = UUID.fromString("4282d63f-107a-4818-abfc-0c23b3d0da83");

	public static final UUID uuidFeatureChloroplastPosition = UUID.fromString("e48f97a5-b066-41fb-87bd-4d69e18acf1b");
	public static final UUID uuidFeatureChloroplastShape = UUID.fromString("89e4aed5-c4ea-4830-a6bc-227d2e7d9a01");
	public static final UUID uuidFeatureChloroplastStructure = UUID.fromString("a04ca566-2020-4992-8d16-f218932c58c3");
	public static final UUID uuidFeatureGrowthForm = UUID.fromString("8e55bcaa-3b7e-4d21-80d0-41c2a2c4f8ab");
	public static final UUID uuidFeatureOrganisationLevel = UUID.fromString("ae958c09-e528-4183-9b9c-3b53012b12e7");
	public static final UUID uuidFeatureRaphe = UUID.fromString("b6743a16-23d4-4645-a63d-3b2f711971c1");
	public static final UUID uuidFeatureShape = UUID.fromString("a8f9b43f-53cb-4f0c-bb5e-8f9f8748882b");
	public static final UUID uuidFeatureSymmetrie = UUID.fromString("cc5e4ce9-c259-4744-9c99-f16144ca7a55");
	
	public static final UUID uuidFeatureApices = UUID.fromString("34d3dd86-cfce-4b65-9866-71a9be020edf");
	public static final UUID uuidFeatureChloroplastNumber = UUID.fromString("d1ef5be9-9c67-4ed0-84d4-d3bb57adddd9");
	public static final UUID uuidFeaturePyrenoid = UUID.fromString("6a42b427-69be-4cc1-a2de-fd813474d522");
	public static final UUID uuidFeatureCellWall = UUID.fromString("676f01de-416b-4cdb-b7bb-f806f7142449");
	public static final UUID uuidFeatureReproductiveStages = UUID.fromString("281de060-64aa-42d8-8c3b-7a764d207fdb");

	public static final UUID uuidFeatureSize = UUID.fromString("b12ca3be-2a49-46ec-96fe-12059de361db");
	public static final UUID uuidFeatureLength = UUID.fromString("492e24e1-40a1-47db-824a-180cc1f9e205");
	public static final UUID uuidFeatureWidth = UUID.fromString("50632b76-82ae-403f-b65f-a681b6788e62");
	public static final UUID uuidFeatureHeigth = UUID.fromString("cd48ed93-7eef-4387-9002-4849d2dbc040");

	public static final UUID uuidFeatureValve1 = UUID.fromString("50df186b-2efb-4e9e-b58b-35f8f2959f29");
	public static final UUID uuidFeatureStriaeFrequencyValve1 = UUID.fromString("e6c14666-0fd2-4361-a6f5-f6cf15b1734d");
	public static final UUID uuidFeatureStriaeOrientationMidValve1 = UUID.fromString("596ba634-ef39-4073-a5f1-4e3b7c0031f5");
	public static final UUID uuidFeatureStriaeOrientationApicesValve1 = UUID.fromString("8d95ea61-4bbc-4885-b7ab-dd4ad506133b");
	public static final UUID uuidFeatureCentralAreaValve1 = UUID.fromString("cacd7b76-080c-4de2-a19d-c3f382729a04");
	public static final UUID uuidFeatureAxialAreaValve1 = UUID.fromString("450578cd-0a53-4fe0-9d43-aecfc1d7fdd9");
	public static final UUID uuidFeatureHasRapheValve1 = UUID.fromString("69acb582-7a3e-4e12-9a9e-ab1871e0a0c9");
	
	public static final UUID uuidFeatureValve2 = UUID.fromString("5e34f17f-9628-4a3d-a974-df4e709d68fb");
	public static final UUID uuidFeatureStriaeFrequencyValve2 = UUID.fromString("db8f1200-5c17-4410-a58d-d93abcb3059b");
	public static final UUID uuidFeatureStriaeOrientationMidValve2 = UUID.fromString("87d50361-25f6-4d10-a830-ce17c9392825");
	public static final UUID uuidFeatureStriaeOrientationApicesValve2 = UUID.fromString("b21a9bf7-987b-49ed-b4ef-905fdfa89057");
	public static final UUID uuidFeatureCentralAreaValve2 = UUID.fromString("ac7a95c0-e388-4ab1-bf87-5bd1c17a1ad0");
	public static final UUID uuidFeatureAxialAreaValve2 = UUID.fromString("0d84c46b-789f-430a-ab55-81e2b280d8d4");
	public static final UUID uuidFeatureHasRapheValve2 = UUID.fromString("b0d99b12-8de2-43e6-83d0-2e94ca71a53e");
	public static final UUID uuidFeatureSalinity = UUID.fromString("2c9377d6-73d9-4401-a113-799eb3752ef3");
	
	public static final UUID uuidFeatureLivingSpecimen = UUID.fromString("0f167e2e-6494-4cf6-93a5-d92266626ec9");
	

	public static final UUID uuidVocChloroplastPosition = UUID.fromString("0ef57573-09e0-4ed6-a6bc-d80a8f927113");
	public static final UUID uuidVocChloroplastShape = UUID.fromString("7777329d-a5e5-4f3d-a85b-2cd5be82080a");
	public static final UUID uuidVocChloroplastStructure = UUID.fromString("cd41aefa-8ca8-469c-9793-78f8eb2e66f1");
	public static final UUID uuidVocGrowthForm = UUID.fromString("8ad0597d-82c5-4d96-9113-3080cd6074e7");
	public static final UUID uuidVocOrganisationLevel = UUID.fromString("0b09e2a5-cb38-45b7-a4fd-dd46817c9fab");
	public static final UUID uuidVocRaphe = UUID.fromString("93a6f107-af4c-42c9-9a04-3c715f23742d");
	public static final UUID uuidVocShape = UUID.fromString("11f9f0af-da1b-44a0-8673-99e418ad6b97");
	public static final UUID uuidVocSymmetry = UUID.fromString("3b7b921f-5d49-4cd7-b73d-29c4710f95da");

	
	public static final UUID uuidMeasurementUnitMgL = UUID.fromString("7ac302c5-3cbd-4334-964a-bf5d11eb9ead");
	public static final UUID uuidMeasurementUnitMolMol = UUID.fromString("96b78d78-3e49-448f-8100-e7779b71dd53");
	public static final UUID uuidMeasurementUnitMicroMolSiL = UUID.fromString("2cb8bc85-a4af-42f1-b80b-34c36c9f75d4");
	public static final UUID uuidMeasurementUnitMicroMolL = UUID.fromString("a631f62e-377e-405c-bd1a-76885b13a72b");
	public static final UUID uuidMeasurementUnitDegreeC = UUID.fromString("55222aec-d5be-413e-8db7-d9a48c316c6c");
	public static final UUID uuidMeasurementUnitPercent = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");
	public static final UUID uuidMeasurementUnitCm = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");
	public static final UUID uuidMeasurementUnitMicroSiCm = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");
	
	
	public static final UUID uuidNamedAreaVocAlgaTerraInformalAreas = UUID.fromString("ebce2f16-2a5d-4845-b4b8-f7841796a093");
	public static final UUID uuidNamedAreaPatagonia = UUID.fromString("e0423d0a-87c3-41aa-afa3-8f2a86aa06dd");
	public static final UUID uuidNamedAreaTierraDelFuego = UUID.fromString("53cf6dc2-b5cc-42be-9fad-7158b8f682f4");
	public static final UUID uuidNamedAreaBorneo = UUID.fromString("6bb908af-be16-402b-973b-3ea20df1d70d");
	
	public static final UUID uuidKindOfUnitVoc = UUID.fromString("400d3ef7-1882-4ed6-bb76-f98f636b595c");
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getFeatureByKey(java.lang.String)
	 */
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getFeatureUuid(java.lang.String)
	 */
	public static UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		if (key == null){ return null;
		}else if(key.equalsIgnoreCase("pH")){return uuidFeaturePH;
		}else if(key.equalsIgnoreCase("Conductivity")){return uuidFeatureConductivity;
		}else if(key.equalsIgnoreCase("Water temperature")){return uuidFeatureWaterTemperature;
		}else if(key.equalsIgnoreCase("Silica")){return uuidFeatureSilica;
		}else if(key.equalsIgnoreCase("Nitrate")){return uuidFeatureNitrate;
		}else if(key.equalsIgnoreCase("Nitrite")){return uuidFeatureNitrite;
		}else if(key.equalsIgnoreCase("Ammonium")){return uuidFeatureAmmonium;
		}else if(key.equalsIgnoreCase("Phosphate")){return uuidFeaturePhosphate;
		}else if(key.equalsIgnoreCase("DIN")){return uuidFeatureDIN;
		}else if(key.equalsIgnoreCase("N/P-ratio")){return uuidFeatureNPRation;
		}else if(key.equalsIgnoreCase("SRP")){return uuidFeatureSRP;
		}else if(key.equalsIgnoreCase("Oxygen saturation")){return uuidFeatureOxygenSaturation;
		}else if(key.equalsIgnoreCase("Community")){return uuidFeatureCommunity;
		}else if(key.equalsIgnoreCase("Cl")){return uuidFeatureCl;
		}else if(key.equalsIgnoreCase("Ortho-phosphate")){return uuidFeatureOrthoPhosphate;
		}else if(key.equalsIgnoreCase("Secchi depth")){return uuidFeatureSecchiDepth;

		}else if (key.equalsIgnoreCase("Chloroplast Position")){return uuidFeatureChloroplastPosition;
		}else if (key.equalsIgnoreCase("Chloroplast Shape")){return uuidFeatureChloroplastShape;
		}else if (key.equalsIgnoreCase("Chloroplast Structure")){return uuidFeatureChloroplastStructure;
		}else if (key.equalsIgnoreCase("Growth Form")){return uuidFeatureGrowthForm;
		}else if (key.equalsIgnoreCase("Organisation Level")){return uuidFeatureOrganisationLevel;
		}else if (key.equalsIgnoreCase("Raphe")){return uuidFeatureRaphe;
		}else if (key.equalsIgnoreCase("Shape")){return uuidFeatureShape;
		}else if (key.equalsIgnoreCase("Symmetry")){return uuidFeatureSymmetrie;
		
		}else if (key.equalsIgnoreCase("Apices")){return uuidFeatureApices;
		}else if (key.equalsIgnoreCase("Chloroplast Number")){return uuidFeatureChloroplastNumber;
		}else if (key.equalsIgnoreCase("Pyrenoid")){return uuidFeaturePyrenoid;
		}else if (key.equalsIgnoreCase("Cell Wall")){return uuidFeatureCellWall;
		}else if (key.equalsIgnoreCase("Reproductive Stages")){return uuidFeatureReproductiveStages;
	
		}else if (key.equalsIgnoreCase("Valve 1")){return uuidFeatureValve1;
		}else if (key.equalsIgnoreCase("Striae Frequency Valve 1")){return uuidFeatureStriaeFrequencyValve1;
		}else if (key.equalsIgnoreCase("Striae Orientation Mid Valve 1")){return uuidFeatureStriaeOrientationMidValve1;
		}else if (key.equalsIgnoreCase("Striae Orientation Apices Valve 1")){return uuidFeatureStriaeOrientationApicesValve1;
		}else if (key.equalsIgnoreCase("Central Area Valve 1")){return uuidFeatureCentralAreaValve1;
		}else if (key.equalsIgnoreCase("Axial Area Valve 1")){return uuidFeatureAxialAreaValve1;
		}else if (key.equalsIgnoreCase("has Raphe Valve 1")){return uuidFeatureHasRapheValve1;

		}else if (key.equalsIgnoreCase("Valve 2")){return uuidFeatureValve2;
		}else if (key.equalsIgnoreCase("Striae Frequency Valve 2")){return uuidFeatureStriaeFrequencyValve2;
		}else if (key.equalsIgnoreCase("Striae Orientation Mid Valve 2")){return uuidFeatureStriaeOrientationMidValve2;
		}else if (key.equalsIgnoreCase("Striae Orientation Apices Valve 2")){return uuidFeatureStriaeOrientationApicesValve2;
		}else if (key.equalsIgnoreCase("Central Area Valve 2")){return uuidFeatureCentralAreaValve2;
		}else if (key.equalsIgnoreCase("Axial Area Valve 2")){return uuidFeatureAxialAreaValve2;
		}else if (key.equalsIgnoreCase("has Raphe Valve 2")){return uuidFeatureHasRapheValve2;

		}else if (key.equalsIgnoreCase("Salinity")){return uuidFeatureSalinity;

		}else{
			logger.warn("Feature was not recognized: " + key);
			return null;
		}
	}

	
	public static UUID getMeasurementUnitUuid(String key) {
		if (key == null){ return null;	
		}else if (key.equalsIgnoreCase("mg/L")){ return uuidMeasurementUnitMgL;
		}else if (key.equalsIgnoreCase("mol/mol")){ return uuidMeasurementUnitMolMol;
		}else if (key.equalsIgnoreCase("\u00B5mol Si/L")){return uuidMeasurementUnitMicroMolSiL;   //µmol Si/L 
		}else if (key.equalsIgnoreCase("\u00B5mol/L")){	return uuidMeasurementUnitMicroMolL;	//µmol/L
		}else if (key.equalsIgnoreCase("\u00B0C")){ return uuidMeasurementUnitDegreeC;           //°C
		}else if (key.equalsIgnoreCase("%")){ return uuidMeasurementUnitPercent;
		}else if (key.equalsIgnoreCase("cm")){ return uuidMeasurementUnitCm;
		}else if (key.equalsIgnoreCase("\u00B5S/cm")){ return uuidMeasurementUnitMicroSiCm;   //µS/cm
		}else{
			logger.warn("MeasurementUnit was not recognized");
			return null;
		}
	}
}
