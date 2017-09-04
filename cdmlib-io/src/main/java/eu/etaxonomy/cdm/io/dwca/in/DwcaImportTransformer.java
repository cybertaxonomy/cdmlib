/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public final class DwcaImportTransformer extends InputTransformerBase {
    private static final long serialVersionUID = 3204045957159056826L;

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportTransformer.class);

	private static final UUID uuidFeatureBiology = UUID.fromString("daf92173-a262-49f1-9a45-923941d9c557");

	private static final UUID uuidMarkerTypeIsExtinct = UUID.fromString("e861a3f1-6f67-407d-9171-8dadbb330016");

	public static final UUID uuidExtensionTypeModified = UUID.fromString("5e3d4930-e672-42c1-813c-c3cee7aef965");


    @Override
    public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException {
        if (key == null){return null;
        }else if (key.equalsIgnoreCase("modified")){return uuidExtensionTypeModified;
        }
        return null;
    }

	@Override
    public UUID getMarkerTypeUuid(String key) throws UndefinedTransformerMethodException {
	    if (key == null){return null;
        }else if (key.equalsIgnoreCase("isExtinct")){return uuidMarkerTypeIsExtinct;
        }
	    return null;
    }

    @Override
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		if (key == null){
			return null;
		}else if (key.equalsIgnoreCase("ecology")){
			return Feature.ECOLOGY();
		}else if (key.equalsIgnoreCase("general_description")){
			return Feature.DESCRIPTION();
		}else if (key.equalsIgnoreCase("diagnostic_description")){
			return Feature.DIAGNOSIS();
		}else if (key.equalsIgnoreCase("conservation_status")){
			return Feature.CONSERVATION();
		}else if (key.equalsIgnoreCase("associations")){
			return Feature.INDIVIDUALS_ASSOCIATION();   //correct ?
		}else if (key.equalsIgnoreCase("distribution")){
			return Feature.DISTRIBUTION_GENERAL();
		}else if (key.equalsIgnoreCase("habitat")){
			return Feature.HABITAT();
		}else if (key.equalsIgnoreCase("uses")){
			return Feature.USES();
		}

		return null;
	}

	@Override
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		if (key == null){
			return null;
		}else if (key.equalsIgnoreCase("biology")){
			return uuidFeatureBiology;
		}

		//TODO from eMonocots scratchpad
//		morphology
//		behavious
//		diseases
//		dispersal
//		legislation
//		management
//		threats
//		cyclicity
//		management
//
//		evolution
//		genetics
//		growth
//		life_cycle
//		life_expectancy
//		look_alikes
//		migration
//		molecular_biology
//		physiology
//		population_biology
//		reproduction
//		risk_statement
//		size
//		taxon_biology
//		trophic_strategy


		return null;
	}

    @Override
    public NamedArea getNamedAreaByKey(String key) throws UndefinedTransformerMethodException {
        if (key == null){
            return null;
        }else if (key.equalsIgnoreCase("xyz")){
            return null;
        }
        return null;
    }

    @Override
    public UUID getNamedAreaUuid(String key) throws UndefinedTransformerMethodException {
        if (key == null){
            return null;
        }else if (key.equalsIgnoreCase("xyz")){
            return null;
        }
        return null;
    }

    @Override
    public PresenceAbsenceTerm getPresenceTermByKey(String key) throws UndefinedTransformerMethodException {
        if (key == null){
            return null;
        }else if (key.equalsIgnoreCase("xyz")){
            return null;
        }
        return null;
    }

    @Override
    public UUID getPresenceTermUuid(String key) throws UndefinedTransformerMethodException {
        if (key == null){
            return null;
        }else if (key.equalsIgnoreCase("xyz")){
            return null;
        }
        return null;
    }


}
