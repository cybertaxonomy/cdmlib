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
 * @since 03.06.2010
 *
 * @deprecated with #9678 a similar cache strategy (DerivedUnitCacheStrategy)
 *      was implemented in cdmlib-model. This class may be removed in future.
 */
@Deprecated
public class DerivedUnitFacadeCacheStrategy
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<DerivedUnit> {

    private static final long serialVersionUID = 1578628591216605619L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DerivedUnitFacadeCacheStrategy.class);

	private static final UUID uuid = UUID.fromString("df4672c1-ce5c-4724-af6d-91e2b326d4a4");

	@Override
	protected UUID getUuid() {return uuid;}

	private boolean includeEmptySeconds = false;
	private boolean includeReferenceSystem = true;

	@Override
    public String getTitleCache(DerivedUnit derivedUnit) {
	    return getTitleCache(derivedUnit, false, true);
	}

	public String getTitleCache(DerivedUnit derivedUnit, boolean skipFieldUnit, boolean addTrailingDot) {

	    String result = "";
	    DerivedUnitFacadeFieldUnitCacheStrategy fieldStrategy = new DerivedUnitFacadeFieldUnitCacheStrategy();

		DerivedUnitFacade facade;
		// NOTE: regarding the string representations of MediaTypes, see https://dev.e-taxonomy.eu/redmine/issues/7608
		try {
			DerivedUnitFacadeConfigurator config = DerivedUnitFacadeConfigurator.NewInstance();
			config.setFirePropertyChangeEvents(false);
			facade = DerivedUnitFacade.NewInstance(derivedUnit, config);

	        if(!skipFieldUnit){
	            result += fieldStrategy.getFieldData(facade);
	        }

			//exsiccatum
			String exsiccatum = null;
			try {
				exsiccatum = facade.getExsiccatum();
			} catch (MethodNotSupportedByDerivedUnitTypeException e) {
				//NO exsiccatum if this facade doe not represent a specimen
			}
			result = CdmUtils.concat("; ", result, exsiccatum);

			//Herbarium & identifier
			String barcode = getSpecimenLabel(facade);
	        if (StringUtils.isNotBlank(barcode)) {
	            result = (result + " (" +  barcode + ")").trim();
	        }
			//result
			result = fieldStrategy.addPlantDescription(result, facade);

			if (addTrailingDot){
			    result = CdmUtils.addTrailingDotIfNotExists(result);
			}

		} catch (DerivedUnitFacadeNotSupportedException e) {
			e.printStackTrace();
		}

		return result;
	}

    /**
     * Produces the collection barcode which is the combination of the collection code and
     * accession number.
     *
     * @param result
     * @param facade
     * @return
     */
    public String getSpecimenLabel(DerivedUnitFacade facade) {
        String code = getCode(facade);
        String identifier = getUnitNumber(facade /*, code*/);
        String collectionData = CdmUtils.concat(" ", code, identifier);
        return collectionData;
    }


    /**
     * Computes the unit number which might be an accession number, barcode, catalogue number, ...
     * In future if the unit number starts with the same string as the barcode
     * it might be replaced.
     * @param facade the derived unit facade
     */
    private String getUnitNumber(DerivedUnitFacade facade) {
        String result;
        if (isNotBlank(facade.getAccessionNumber())){
            result = facade.getAccessionNumber();
        }else if (isNotBlank(facade.getBarcode())){
            result = facade.getBarcode();
        }else{
            result = facade.getCatalogNumber();
        }
        String code = getCode(facade);
        if(result != null){
            result = result.trim();
            if(isNotBlank(code) && result.startsWith(code + " ")){
                result = result.replaceAll("^" + code + "\\s", "");
            }
        }
        return result;
    }

	private String getCode(DerivedUnitFacade facade) {
		String code = "";
		if(facade.getCollection() != null){
			code = facade.getCollection().getCode();
			if (StringUtils.isBlank(code)){
			    code = facade.getCollection().getName();
			}
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
