/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.MethodNotSupportedByDerivedUnitTypeException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 * @since 18.10.2010
 *
 */
public class DerivedUnitFacadeBeanProcessor extends AbstractBeanProcessor<DerivedUnitFacade> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#processBeanSecondStep(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(DerivedUnitFacade bean,
			JSONObject json, JsonConfig jsonConfig) {
		
		List<Language> languages = LocaleContext.getLanguages();
		
		if(Hibernate.isInitialized(bean.innerGatheringEvent())){

			addJsonElement(json, jsonConfig, "country",bean.getCountry());
			addJsonElement(json, jsonConfig, "collectingAreas", bean.getCollectingAreas());
			addJsonElement(json, jsonConfig, "absoluteElevation", bean.getAbsoluteElevation());
//			addJsonElement(json, jsonConfig, "absoluteElevationError", bean.getAbsoluteElevationError());
//			addJsonElement(json, jsonConfig, "absoluteElevationMinimum", bean.getAbsoluteElevationMinimum());
			addJsonElement(json, jsonConfig, "absoluteElevationMaximum", bean.getAbsoluteElevationMaximum());
			//new
			addJsonElement(json, jsonConfig, "absoluteElevationText", bean.absoluteElevationToString());
			
			addJsonElement(json, jsonConfig, "collector", bean.getCollector());
			addJsonElement(json, jsonConfig, "collectingMethod", bean.getCollectingMethod());
			addJsonElement(json, jsonConfig, "distanceToGround", bean.getDistanceToGround());
			addJsonElement(json, jsonConfig, "distanceToWaterSurface", bean.getDistanceToWaterSurface());
			addJsonElement(json, jsonConfig, "exactLocation", bean.getExactLocation());
			addJsonElement(json, jsonConfig, "gatheringEventDescription", bean.getGatheringEventDescription());
			addJsonElement(json, jsonConfig, "getGatheringPeriod", bean.getGatheringPeriod());
			addJsonElement(json, jsonConfig, "locality", bean.getLocality());
		}
				
// FIXME: addJsonElement(json, jsonConfig, "ecology", bean.getEcology(languages));
		addJsonElement(json, jsonConfig, "ecology", bean.getEcology());
// FIXME: addJsonElement(json, jsonConfig, "plantDescription", bean.getPlantDescription(languages));
		addJsonElement(json, jsonConfig, "plantDescription", bean.getPlantDescription());
		
		if(Hibernate.isInitialized(bean.innerFieldUnit())){
			addJsonElement(json, jsonConfig, "fieldObjectMedia", bean.getFieldObjectMedia());
			addJsonElement(json, jsonConfig, "fieldNumber", bean.getFieldNumber());			
			addJsonElement(json, jsonConfig, "fieldNotes", bean.getFieldNotes());			
			addJsonElement(json, jsonConfig, "individualCount", bean.getIndividualCount());			
			addJsonElement(json, jsonConfig, "lifeStage", bean.getLifeStage());			
			addJsonElement(json, jsonConfig, "sex", bean.getSex());			
		}
		
		if(Hibernate.isInitialized(bean.innerDerivedUnit())){	
			addJsonElement(json, jsonConfig, "derivedUnitDefinitions", bean.getDerivedUnitDefinitions());			
			addJsonElement(json, jsonConfig, "determinations", bean.getDeterminations());
			addJsonElement(json, jsonConfig, "derivedUnitMedia", bean.getDerivedUnitMedia());			
			addJsonElement(json, jsonConfig, "accessionNumber", bean.getAccessionNumber());		
			addJsonElement(json, jsonConfig, "catalogNumber", bean.getCatalogNumber());			
			addJsonElement(json, jsonConfig, "barcode", bean.getBarcode());	
			try {
				addJsonElement(json, jsonConfig, "preservationMethod", bean.getPreservationMethod());
			} catch (MethodNotSupportedByDerivedUnitTypeException e) {
				/* Skip - Only supported by specimen and fossils */
			}			
			addJsonElement(json, jsonConfig, "storedUnder", bean.getStoredUnder());
//			addJsonElement(json, jsonConfig, "collectorsNumber", bean.getCollectorsNumber());
			try {
				addJsonElement(json, jsonConfig, "exsiccatum", bean.getExsiccatum());
			} catch (MethodNotSupportedByDerivedUnitTypeException e) {
				/* Skip - Only supported by specimen and fossils */
			}
			addJsonElement(json, jsonConfig, "sources", bean.getSources());
			addJsonElement(json, jsonConfig, "collection", bean.getCollection());
			if(Hibernate.isInitialized(bean.innerDerivedUnit().getDerivedFrom().getDerivatives())){
				addJsonElement(json, jsonConfig, "duplicates", bean.getDuplicates());
			}
		}
		

		
		return json;
	}
	

}
