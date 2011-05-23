// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public class InputTransformerBase implements IInputTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InputTransformerBase.class);

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
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getLanguageByKey(java.lang.String)
	 */
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getLanguageUuid(java.lang.String)
	 */
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getExtensionTypeByKey(java.lang.String)
	 */
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getExtensionTypeUuid(java.lang.String)
	 */
	public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getMarkerTypeByKey(java.lang.String)
	 */
	public MarkerType getMarkerTypeByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getMarkerTypeByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getMarkerTypeUuid(java.lang.String)
	 */
	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getMarkerTypeUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNameTypeDesignationStatusByKey(java.lang.String)
	 */
	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNameTypeDesignationStatusUuid(java.lang.String)
	 */
	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getSpecimenTypeDesignationStatusByKey(java.lang.String)
	 */
	public SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isEmpty(key)){return null;
		}else if (key.matches("(?i)(T|Type)")){return SpecimenTypeDesignationStatus.TYPE();
		}else if (key.matches("(?i)(HT|Holotype)")){return SpecimenTypeDesignationStatus.HOLOTYPE();
		}else if (key.matches("(?i)(LT|Lectotype)")){return SpecimenTypeDesignationStatus.LECTOTYPE();
		}else if (key.matches("(?i)(NT|Neotype)")){return SpecimenTypeDesignationStatus.NEOTYPE();
		}else if (key.matches("(?i)(ST|Syntype)")){return SpecimenTypeDesignationStatus.SYNTYPE();
		}else if (key.matches("(?i)(ET|Epitype)")){return SpecimenTypeDesignationStatus.EPITYPE();
		}else if (key.matches("(?i)(IT|Isotype)")){return SpecimenTypeDesignationStatus.ISOTYPE();
		}else if (key.matches("(?i)(ILT|Isolectotype)")){return SpecimenTypeDesignationStatus.ISOLECTOTYPE();
		}else if (key.matches("(?i)(INT|Isoneotype)")){return SpecimenTypeDesignationStatus.ISONEOTYPE();
		}else if (key.matches("(?i)(IET|Isoepitype)")){return SpecimenTypeDesignationStatus.ISOEPITYPE();
		}else if (key.matches("(?i)(PT|Paratype)")){return SpecimenTypeDesignationStatus.PARATYPE();
		}else if (key.matches("(?i)(PLT|Paralectotype)")){return SpecimenTypeDesignationStatus.PARALECTOTYPE();
		}else if (key.matches("(?i)(PNT|Paraneotype)")){return SpecimenTypeDesignationStatus.PARANEOTYPE();
		}else if (key.matches("(?i)(unsp.|Unspecified)")){return SpecimenTypeDesignationStatus.UNSPECIFIC();
		}else if (key.matches("(?i)(2LT|Second Step Lectotype)")){return SpecimenTypeDesignationStatus.SECOND_STEP_LECTOTYPE();
		}else if (key.matches("(?i)(2NT|Second Step Neotype)")){return SpecimenTypeDesignationStatus.SECOND_STEP_NEOTYPE();
		}else if (key.matches("(?i)(OM|Original Material)")){return SpecimenTypeDesignationStatus.ORIGINAL_MATERIAL();
		}else if (key.matches("(?i)(IcT|Iconotype)")){return SpecimenTypeDesignationStatus.ICONOTYPE();
		}else if (key.matches("(?i)(PT|Phototype)")){return SpecimenTypeDesignationStatus.PHOTOTYPE();
		}else if (key.matches("(?i)(IST|Isosyntype)")){return SpecimenTypeDesignationStatus.ISOSYNTYPE();
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getSpecimenTypeDesignationStatusUuid(java.lang.String)
	 */
	public UUID getSpecimenTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getSpecimenTypeDesignationStatusUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getPresenceTermByKey(java.lang.String)
	 */
	public PresenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getPresenceTermByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getPresenceTermUuid(java.lang.String)
	 */
	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getPresenceTermUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNamedAreaByKey(java.lang.String)
	 */
	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNamedAreaUuid(java.lang.String)
	 */
	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	
	
}
