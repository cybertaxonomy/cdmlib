/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;

/**
 * Formatting class for DerivedUnits.
 *
 * Note: this class is mostly a copy from the orignal class DerivedUnitFacadeCacheStrategy
 *       in cdmlib-service. (#9678)
 *
 * @author a.mueller
 * @since 18.06.2021
 */
public class DerivedUnitDefaultCacheStrategy
        extends OccurrenceCacheStrategyBase<DerivedUnit>{

    private static final long serialVersionUID = -3905309456296895952L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DerivedUnitDefaultCacheStrategy.class);

    private static final UUID uuid = UUID.fromString("2746bca3-f58a-4b5d-b4ec-ece9785731fe");

    @Override
    protected UUID getUuid() {return uuid;}

    private boolean skipFieldUnit = false;
    private boolean addTrailingDot = true;

    private static final FieldUnitDefaultCacheStrategy fieldUnitCacheStrategy
        = FieldUnitDefaultCacheStrategy.NewInstance(false, false);

    @Override
    protected String doGetTitleCache(DerivedUnit specimen) {
        String result = "";

        SortedSet<FieldUnit> fieldUnits = getFieldUnits(specimen);
        if(!skipFieldUnit){
            for (FieldUnit fieldUnit : fieldUnits){
                result = CdmUtils.concat("; ", fieldUnitCacheStrategy.getTitleCache(fieldUnit));
            }
        }

        // NOTE: regarding the string representations of MediaTypes, see https://dev.e-taxonomy.eu/redmine/issues/7608

        //exsiccatum
        String exsiccatum = null;
        exsiccatum = specimen.getExsiccatum();
        result = CdmUtils.concat("; ", result, exsiccatum);

        //Herbarium & identifier
        String barcode = getSpecimenLabel(specimen);
        if (isNotBlank(barcode)) {
            result = (result + " (" +  barcode + ")").trim();
        }

        //result
        if(!skipFieldUnit){
            for (FieldUnit fieldUnit : fieldUnits){
                result = addPlantDescription(result, fieldUnit);
            }
        }


        if (addTrailingDot){
            result = CdmUtils.addTrailingDotIfNotExists(result);
        }

        return result;
    }

    private SortedSet<FieldUnit> getFieldUnits(DerivedUnit specimen) {
        SortedSet<FieldUnit> result = new TreeSet<>(getFieldUnitComparator());
        if (specimen.getOriginals() != null){
            for (SpecimenOrObservationBase<?> sob : specimen.getOriginals()){
                if (sob.isInstanceOf(FieldUnit.class)){
                    result.add(CdmBase.deproxy(sob, FieldUnit.class));
                }else{
                    result.addAll(getFieldUnits(CdmBase.deproxy(sob, DerivedUnit.class)));
                }
            }
        }
        return result;
    }

    //very preliminary comparator to guarantee a defined order
    private Comparator<FieldUnit> getFieldUnitComparator() {
        return new Comparator<FieldUnit>() {

            @Override
            public int compare(FieldUnit o1, FieldUnit o2) {
                return o1.getUuid().compareTo(o2.getUuid());
            }
        };
    }

    /**
     * Produces the collection barcode which is the combination of the collection code and
     * accession number.
     *
     * @param result
     * @param derivedUnit
     * @return
     */
    public String getSpecimenLabel(DerivedUnit derivedUnit) {
        String code = getCode(derivedUnit);
        String identifier = getUnitNumber(derivedUnit /*, code*/);
        String collectionData = CdmUtils.concat(" ", code, identifier);
        return collectionData;
    }


    /**
     * Computes the unit number which might be an accession number, barcode, catalogue number, ...
     * In future if the unit number starts with the same string as the barcode
     * it might be replaced.
     * @param derivedUnit the derived unit facade
     */
    private String getUnitNumber(DerivedUnit derivedUnit) {
        String result;
        if (isNotBlank(derivedUnit.getAccessionNumber())){
            result = derivedUnit.getAccessionNumber();
        }else if (isNotBlank(derivedUnit.getBarcode())){
            result = derivedUnit.getBarcode();
        }else{
            result = derivedUnit.getCatalogNumber();
        }
        String code = getCode(derivedUnit);
        if(result != null){
            result = result.trim();
            if(isNotBlank(code) && result.startsWith(code + " ")){
                result = result.replaceAll("^" + code + "\\s", "");
            }
        }
        return result;
    }

    private String getCode(DerivedUnit derivedUnit) {
        String code = "";
        if(derivedUnit.getCollection() != null){
            code = derivedUnit.getCollection().getCode();
            if (isBlank(code)){
                code = derivedUnit.getCollection().getName();
            }
            if (isBlank(code)){
                Institution institution = derivedUnit.getCollection().getInstitute();
                if (institution != null){
                    code = institution.getCode();
                }
                if (isBlank(code)){
                    Collection superCollection = derivedUnit.getCollection().getSuperCollection();
                    if (superCollection != null){
                        code = superCollection.getCode();
                    }
                }
            }
        }
        return code;
    }
}
