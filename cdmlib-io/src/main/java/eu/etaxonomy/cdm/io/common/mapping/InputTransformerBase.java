/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;

/**
 * @author a.mueller
 * @since 15.03.2010
 */
public class InputTransformerBase implements IInputTransformer, Serializable {
    private static final long serialVersionUID = 1824180329524647957L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InputTransformerBase.class);

	@Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}


	@Override
	public State getStateByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getStateByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getStateUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getStateByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	@Override
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	@Override
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public MarkerType getMarkerTypeByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getMarkerTypeByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getMarkerTypeUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public SpecimenTypeDesignationStatus getSpecimenTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		if (StringUtils.isBlank(key)){return null;
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

	@Override
	public UUID getSpecimenTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getSpecimenTypeDesignationStatusUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}


	@Override
	public PresenceAbsenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getPresenceTermByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getPresenceTermUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	@Override
	public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public NamedAreaLevel getNamedAreaLevelByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaLevelByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getNamedAreaLevelUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNamedAreaLevelUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public ReferenceSystem getReferenceSystemByKey(String key) throws UndefinedTransformerMethodException {
		if (CdmUtils.isBlank(key)){return null;
		}else if (key.matches("(?i)(wgs84)")){return ReferenceSystem.WGS84();
		}else if (key.matches("(?i)(googleearth)")){return ReferenceSystem.GOOGLE_EARTH();
		}else if (key.matches("(?i)(gazetteer)")){return ReferenceSystem.GAZETTEER();
		}else if (key.matches("(?i)(map)")){return ReferenceSystem.MAP();
		}else{
			String warning = "getReferenceSystemByKey is not implemented in implementing transformer class";
			throw new UndefinedTransformerMethodException(warning);
		}
	}

	@Override
	public UUID getReferenceSystemUuid(String key) throws UndefinedTransformerMethodException {

		String warning = "getReferenceSystemUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public Rank getRankByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getRankByKey is not yet implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public NomenclaturalStatusType getNomenclaturalStatusByKey(String key) throws UndefinedTransformerMethodException {
	    String warning = "getNomenclaturalStatusByKey is not yet implemented in implementing transformer class";
	    throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public UUID getRankUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getRankUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

    @Override
    public DefinedTerm getIdentifierTypeByKey(String key) throws UndefinedTransformerMethodException {
        String warning = "getIdentifierTypeByKey is not implemented in implementing transformer class";
        throw new UndefinedTransformerMethodException(warning);
    }

    @Override
    public UUID getIdentifierTypeUuid(String key) throws UndefinedTransformerMethodException {
        String warning = "getIdentifierTypeUuid is not implemented in implementing transformer class";
        throw new UndefinedTransformerMethodException(warning);
    }


}
