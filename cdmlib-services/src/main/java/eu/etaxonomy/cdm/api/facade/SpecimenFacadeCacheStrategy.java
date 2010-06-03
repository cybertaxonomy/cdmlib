// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.facade;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @date 03.06.2010
 *
 */
public class SpecimenFacadeCacheStrategy extends StrategyBase implements IIdentifiableEntityCacheStrategy<Specimen> {
	private static final long serialVersionUID = 1578628591216605619L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenFacadeCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("df4672c1-ce5c-4724-af6d-91e2b326d4a4");
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
	 */
	@Override
	public String getTitleCache(Specimen specimen) {
		String ALTITUDE_PREFIX = "alt. ";
		String ALTITUDE_POSTFIX = " m";
		
		String result = "";
		
		SpecimenFacade facade;
		try {
			facade = SpecimenFacade.NewInstance(specimen);
			//country
			//TODO
			
			// FIXME hasGatheringEvent needed;
			//locality
			result = CdmUtils.concat(", ", result, facade.getLocalityText());
			//elevation
			if (facade.getAbsoluteElevation() != null){
				result = CdmUtils.concat(", " , result, ALTITUDE_PREFIX);
				result += facade.getAbsoluteElevation() + ALTITUDE_POSTFIX;
			}
			//ecology
			result = CdmUtils.concat(", ", result, facade.getEcology());
			//gathering period
			//TODO period.toString ??
			result = CdmUtils.concat(", ", result, facade.getGatheringPeriod().toString());
			
			//Herbarium & accession number
			String code = getCode(facade);
			String collectionData = CdmUtils.concat(" ", code, facade.getAccessionNumber());
			if (CdmUtils.isNotEmpty(collectionData)) {
				result = (result + " (" +  collectionData + ")").trim();
			}
			
			//plant description
			result = CdmUtils.concat("; ", result, facade.getPlantDescription());
			if (CdmUtils.isNotEmpty(result)){
				result += ".";
			}
			
		} catch (SpecimenFacadeNotSupportedException e) {
			e.printStackTrace();
		}
		
		
		return result;
	}



	/**
	 * @param facade
	 */
	private String getCode(SpecimenFacade facade) {
		String code = facade.getCollection().getCode();
		if (CdmUtils.isEmpty(code)){
			Institution institution = facade.getCollection().getInstitute();
			if (institution != null){
				code = institution.getCode();
			}
			if (CdmUtils.isEmpty(code)){
				Collection superCollection = facade.getCollection().getSuperCollection();
				if (superCollection != null){
					code = superCollection.getCode();
				}
			}
		}
		return code;
	}

	
}
