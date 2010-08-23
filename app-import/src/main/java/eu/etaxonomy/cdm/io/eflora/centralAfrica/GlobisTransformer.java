// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public final class GlobisTransformer extends InputTransformerBase {
	private static final Logger logger = Logger.getLogger(GlobisTransformer.class);
	
//	public static final int SOURCE_USE_ORIGINAL_DESCRIPTION = 1;
//	public static final int SOURCE_USE_BASIS_OF_RECORD = 2;
//	public static final int SOURCE_USE_ADDITIONAL_SOURCE = 3;
//	public static final int SOURCE_USE_SOURCE_OF_SYNONYMY = 4;
//	public static final int SOURCE_USE_REDESCRIPTION = 5;
//	public static final int SOURCE_USE_NEW_COMBINATION_REFERENCE = 6;
//	public static final int SOURCE_USE_STATUS_SOURCE = 7;
//	public static final int SOURCE_USE_EMENDATION = 8;
	
	//extension types
	public static final UUID uuidEdition = UUID.fromString("c42dfb85-abbe-49b3-8a2b-56cc1b8eb6d0");
	public static final UUID uuidEditor = UUID.fromString("07752659-3018-4880-bf26-41bb396fbf37");
	public static final UUID uuidGeneralKeywords = UUID.fromString("aaa67b2a-c45b-42ed-b4fa-1028ffe41e44");
	public static final UUID uuidGeoKeywords = UUID.fromString("a1afb697-d37b-4a8c-84d8-63f8f01ae10a");
	public static final UUID uuidLibrary = UUID.fromString("71a3e44d-4ed2-44f9-be6a-76fa26a294bd");

//	public static final UUID uuidEditor = UUID.fromString("07752659-3018-4880-bf26-41bb396fbf37");
//	public static final UUID uuidEditor = UUID.fromString("07752659-3018-4880-bf26-41bb396fbf37");
	
	
	//language uuids
	
	
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

	
	
	
}
