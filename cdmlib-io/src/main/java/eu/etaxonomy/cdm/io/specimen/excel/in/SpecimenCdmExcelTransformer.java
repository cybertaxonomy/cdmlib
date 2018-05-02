/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.specimen.excel.in;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @since 05.05.2011
 * @version 1.0
 */
public final class SpecimenCdmExcelTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenCdmExcelTransformer.class);
	
	public static final UUID uuidRefSysEstimated = UUID.fromString("3b625520-e5cf-4d9c-9599-0cb048e0e8d2");
	public static final UUID uuidRefSysLabel = UUID.fromString("c72335ed-c9aa-4d1c-b6fc-9f307d207862");
	public static final UUID uuidRefSysGps = UUID.fromString("b3c36751-b2ac-47f7-8ac1-3dc5c129e0b2");
	public static final UUID uuidRefSysUtm = UUID.fromString("bc64f335-a80a-432a-82ff-5c08a308324f");
	public static final UUID uuidRefSysDescription = UUID.fromString("a9c42b52-c8cf-4658-86ce-271dca569f2c");
	public static final UUID uuidRefSysMapBR = UUID.fromString("64b8dc38-e532-45bf-bea7-86b9467973a1");
	public static final UUID uuidRefSysUnsure = UUID.fromString("91781b31-29e9-4807-8172-e7e25433ea06");
	
	
	
	
	//Languages
	
	@Override
	public SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		return super.getSpecimenTypeDesignationStatusByKey(key);
	}
	
	@Override
	public ReferenceSystem getReferenceSystemByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
//		}else if (key.matches("(?i)(wgs84)")){return ReferenceSystem.WGS84();
		}else{
			ReferenceSystem result = null;
			try {
				result = super.getReferenceSystemByKey(key);
			} catch (UndefinedTransformerMethodException e) {
				//do nothing
			}
			return result;
		}
	}
	
	@Override
	public UUID getReferenceSystemUuid(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
		}else if (key.matches("(?i)(estimated)")){return uuidRefSysEstimated;
		}else if (key.matches("(?i)(label)")){return uuidRefSysLabel;
		}else if (key.matches("(?i)(gps)")){return uuidRefSysLabel;
		}else if (key.matches("(?i)(utm)")){return uuidRefSysUtm;
		}else if (key.matches("(?i)(description)")){return uuidRefSysDescription;
		}else if (key.matches("(?i)(mapBR)")){return uuidRefSysMapBR;
		}else if (key.matches("(?i)(Unsure)")){return uuidRefSysUnsure;
		}
		return null;
	}
	
}
