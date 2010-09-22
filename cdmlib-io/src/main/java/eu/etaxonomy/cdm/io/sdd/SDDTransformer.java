// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.sdd;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * @author a.mueller
 * @created 01.03.2010
 * @version 1.0
 */
public class SDDTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SDDTransformer.class);
	
	
	
	//feature uuids
	public static final UUID uuidNote = UUID.fromString("b9af1489-6b68-497f-8d4b-260a9f886827");
	
	//extension type uuids
	public static final UUID uuidTitle = UUID.fromString("5d9ca987-81f1-4d6c-b06a-eaa8311ca249");
	
	//marker type uuid
	public static final UUID uuidExcludedTaxon = UUID.fromString("e729a22d-8c94-4859-9f91-3e3ae212c91d");
	

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
	public UUID getFeatureUuid(String key) 	throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;

		}else if (key.equalsIgnoreCase("lifeform")){return uuidNote;
		
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
		}else if (key.equalsIgnoreCase("excluded")){return uuidExcludedTaxon;
		}else if (key.equalsIgnoreCase("EXCLUDED SPECIES, OF UNCERTAIN AFHNITIES PTELEOCARPA")){return uuidExcludedTaxon;
		}else if (key.equalsIgnoreCase("EXCLUDED GENUS, OF UNCERTAIN AFHNITIES PTELEOCARPA")){return uuidExcludedTaxon;
		}else if (key.equalsIgnoreCase("INCOMPLETELY KNOWN SPECIES")){return uuidNote;
		}else{
			return null;
		}
		
	}
	
	
	
	
	
}
