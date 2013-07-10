/**
 * 
 */
package eu.etaxonomy.cdm.io.algaterra;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;

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

	public static final UUID uuidMeasurementUnitMgL = UUID.fromString("7ac302c5-3cbd-4334-964a-bf5d11eb9ead");
	public static final UUID uuidMeasurementUnitMolMol = UUID.fromString("96b78d78-3e49-448f-8100-e7779b71dd53");
	public static final UUID uuidMeasurementUnitMicroMolSiL = UUID.fromString("2cb8bc85-a4af-42f1-b80b-34c36c9f75d4");
	public static final UUID uuidMeasurementUnitMicroMolL = UUID.fromString("a631f62e-377e-405c-bd1a-76885b13a72b");
	public static final UUID uuidMeasurementUnitDegreeC = UUID.fromString("55222aec-d5be-413e-8db7-d9a48c316c6c");
	public static final UUID uuidMeasurementUnitPercent = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");
	public static final UUID uuidMeasurementUnitCm = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");
	public static final UUID uuidMeasurementUnitMicroSiCm = UUID.fromString("3ea3110e-f048-4bed-8bfe-33c60f63626f");

	
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
