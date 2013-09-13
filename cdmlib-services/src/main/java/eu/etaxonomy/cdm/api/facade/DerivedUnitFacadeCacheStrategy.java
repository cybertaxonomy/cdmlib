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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @date 03.06.2010
 *
 */
public class DerivedUnitFacadeCacheStrategy extends StrategyBase implements IIdentifiableEntityCacheStrategy<DerivedUnit> {
	private static final long serialVersionUID = 1578628591216605619L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("df4672c1-ce5c-4724-af6d-91e2b326d4a4");
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	private boolean includeEmptySeconds = false;
	private boolean includeReferenceSystem = true;
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.IdentifiableEntity)
	 */
	@Override
	public String getTitleCache(DerivedUnit derivedUnit) {
		DerivedUnitFacadeFieldUnitCacheStrategy fieldStrategy = new DerivedUnitFacadeFieldUnitCacheStrategy();

		String result = "";
		
		DerivedUnitFacade facade;
		try {
			DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
			config.setFirePropertyChangeEvents(false);
			facade = DerivedUnitFacade.NewInstance(derivedUnit, config);
			result += fieldStrategy.getFieldData(facade);
			
			//Exsiccatum
			String exsiccatum = null;
			try {
				exsiccatum = facade.getExsiccatum();
			} catch (MethodNotSupportedByDerivedUnitTypeException e) {
				//NO exsiccatum if this facade doe not represent a specimen
			}
			result = CdmUtils.concat("; ", result, exsiccatum);
			
			//Herbarium & accession number
			String code = getCode(facade);
			String collectionData = CdmUtils.concat(" ", code, facade.getAccessionNumber());
			if (StringUtils.isNotBlank(collectionData)) {
				result = (result + " (" +  collectionData + ")").trim();
			}
			
			//result
			result = fieldStrategy.addPlantDescription(result, facade);

			
		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
		}
		
		
		return result;
	}


	/**
	 * @param facade
	 */
	private String getCode(DerivedUnitFacade facade) {
		String code = "";
		if(facade.getCollection() != null){			
			code = facade.getCollection().getCode();
			if (StringUtils.isBlank(code)){
				Institution institution = facade.getCollection().getInstitute();
				if (institution != null){
					code = institution.getCode();
				}
				if (StringUtils.isBlank(code)){
					Collection superCollection = facade.getCollection().getSuperCollection();
					if (superCollection != null){
						code = superCollection.getCode();
					}
				}
			}
		} 
		return code;
	}
	
// ************************** GETTER / SETTER ******************************************************
	
	public boolean isIncludeSeconds() {
		return includeEmptySeconds;
	}



	public void setIncludeSeconds(boolean includeSeconds) {
		this.includeEmptySeconds = includeSeconds;
	}



	public void setIncludeReferenceSystem(boolean includeReferenceSystem) {
		this.includeReferenceSystem = includeReferenceSystem;
	}



	public boolean isIncludeReferenceSystem() {
		return includeReferenceSystem;
	}

	
}
