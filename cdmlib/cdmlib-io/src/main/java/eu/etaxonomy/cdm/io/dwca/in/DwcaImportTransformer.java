// $Id$
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

/**
 * @author a.mueller
 * @created 05.05.2011
 */
public final class DwcaImportTransformer extends InputTransformerBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaImportTransformer.class);

	private static final UUID uuidFeatureBiology = UUID.fromString("daf92173-a262-49f1-9a45-923941d9c557");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureByKey(java.lang.String)
	 */
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
			return Feature.DISTRIBUTION();
		}else if (key.equalsIgnoreCase("habitat")){
			return Feature.HABITAT();
		}else if (key.equalsIgnoreCase("uses")){
			return Feature.USES();
		}
		


		
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase#getFeatureUuid(java.lang.String)
	 */
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
	
	

	
	
	
}
