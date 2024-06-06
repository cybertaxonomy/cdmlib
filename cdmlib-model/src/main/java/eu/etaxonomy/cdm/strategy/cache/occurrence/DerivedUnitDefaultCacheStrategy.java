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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
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
    private static final Logger logger = LogManager.getLogger();

    private static final UUID uuid = UUID.fromString("2746bca3-f58a-4b5d-b4ec-ece9785731fe");

    @Override
    protected UUID getUuid() {return uuid;}

    private boolean skipFieldUnit = false;
    private boolean addTrailingDot = false;
    //according to #6865 deduplication is usually not wanted
    private boolean deduplicateCollectionCodeInNumber = false;
    private CollectionAccessionSeperator collectionAccessionSeperator = CollectionAccessionSeperator.COLON;

    public enum CollectionAccessionSeperator {
        SPACE, COLON, ACCESION_NO_TYPE;

        private String getSeperator(DerivedUnit du) {
            switch (this) {
                case COLON:
                    return ": ";
                case ACCESION_NO_TYPE:
                    if (StringUtils.isNotEmpty(du.getBarcode())) {
                        return " barcode ";
                    }else if (StringUtils.isNotEmpty(du.getAccessionNumber())) {
                        return " accession no. ";
                    }else if (StringUtils.isNotEmpty(du.getCatalogNumber())) {
                        return " catalog no. ";
                    }else {
                        //TODO more types to come?
                        return " ";
                    }
                case SPACE:
                default:
                    return " ";
            }
        }
    }

    private static final FieldUnitDefaultCacheStrategy fieldUnitCacheStrategy
        = FieldUnitDefaultCacheStrategy.NewInstance(false, false);

    public static DerivedUnitDefaultCacheStrategy NewInstance(){
        return new DerivedUnitDefaultCacheStrategy();
    }

    public static DerivedUnitDefaultCacheStrategy NewInstance(boolean skipFieldUnit, boolean addTrailingDot,
            boolean deduplicateCollectionCodeInNumber){
        return new DerivedUnitDefaultCacheStrategy(skipFieldUnit, addTrailingDot, deduplicateCollectionCodeInNumber, null);
    }

    public static DerivedUnitDefaultCacheStrategy NewInstance(boolean skipFieldUnit, boolean addTrailingDot,
            boolean deduplicateCollectionCodeInNumber, CollectionAccessionSeperator collectionAccessionSeperator){
        return new DerivedUnitDefaultCacheStrategy(skipFieldUnit, addTrailingDot, deduplicateCollectionCodeInNumber,
                collectionAccessionSeperator);
    }

//******************************* CONSTRUCTOR *******************************************/

    //default value constructor
    private DerivedUnitDefaultCacheStrategy() {}


    private DerivedUnitDefaultCacheStrategy(boolean skipFieldUnit, boolean addTrailingDot,
            boolean deduplicateCollectionCodeInNumber, CollectionAccessionSeperator collectionAccessionSeperator) {
        this.skipFieldUnit = skipFieldUnit;
        this.addTrailingDot = addTrailingDot;
        this.deduplicateCollectionCodeInNumber = deduplicateCollectionCodeInNumber;
        if (collectionAccessionSeperator != null){
            this.collectionAccessionSeperator = collectionAccessionSeperator;
        }
    }

//******************************* METHODS ***************************************************/

    @Override
    protected String doGetTitleCache(DerivedUnit specimen) {
        String result = "";

        SortedSet<FieldUnit> fieldUnits = getFieldUnits(specimen);
        if(!skipFieldUnit){
            for (FieldUnit fieldUnit : fieldUnits){
                result = CdmUtils.concat("; ", fieldUnitCacheStrategy.getTitleCache(fieldUnit, true));
            }
        }

        // NOTE: regarding the string representations of MediaTypes, see https://dev.e-taxonomy.eu/redmine/issues/7608

        //exsiccatum
        String exsiccatum = null;
        exsiccatum = specimen.getExsiccatum();
        result = CdmUtils.concat("; ", result, exsiccatum);

        //Herbarium & identifier
        String collectionAndNumber = getSpecimenLabel(specimen);
        String specimenStatusStr = getSpecimenStatusStr(specimen);
        collectionAndNumber = CdmUtils.concat(", ", collectionAndNumber, specimenStatusStr);
        if (isNotBlank(collectionAndNumber)) {
            result = (result + " (" +  collectionAndNumber + ")").trim();
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

    private String getSpecimenStatusStr(DerivedUnit specimen) {
        String result = null;
        if (!specimen.getStatus().isEmpty()){
            result = specimen.getStatus()
                    .stream()
                    .map(s->s.getType())
                    .filter(t->t != null)
                    .map(t->t.getPreferredRepresentation(Language.DEFAULT()).getLabel())
                    .sorted((s1,s2)->s1.compareTo(s2))
                    .collect(Collectors.joining(", "));
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


    //NOTE still need to discuss, if the identity cache for derived units
    //should also include the identity cache for field units (see also #5951).
    //This may make sense for searching, but is less comfortable in
    //tree representations.
    //For search it might be not so urgent if showing both the identity cache
    //followed by the titleCache whcih includes field unit information.
    @Override
    protected String doGetIdentityCache(DerivedUnit derivedUnit) {
        String specimenLabel = getSpecimenLabel(derivedUnit);
        if (isBlank(specimenLabel)){
            return getTitleCache(derivedUnit);
        }else{
            //NOTE: in future we may add further information if not both data are given
            //      collection code/label AND unit number
            return specimenLabel;
        }
    }

    /**
     * Produces the collection code and number which is the combination of the collection code and
     * accession number or barcode.
     */
    public String getSpecimenLabel(DerivedUnit derivedUnit) {
        String code = getCollectionCode(derivedUnit);
        String identifier = getUnitNumber(derivedUnit /*, code*/);
        CharSequence separator = collectionAccessionSeperator.getSeperator(derivedUnit);
        String collectionData = CdmUtils.concat(separator, code, identifier);
        return collectionData;
    }

    /**
     * Computes the unit number which might be an accession number, barcode, catalogue number, ...
     * In future if the unit number starts with the same string as the barcode
     * it might be replaced.
     * @param derivedUnit the derived unit facade
     */
    //is public as it is used by SpecimenTypeDesignationSetFormatter to extract the unit number
    public String getUnitNumber(DerivedUnit derivedUnit) {
        String result;
        if (isNotBlank(derivedUnit.getAccessionNumber())){
            result = derivedUnit.getAccessionNumber();
        }else if (isNotBlank(derivedUnit.getBarcode())){
            result = derivedUnit.getBarcode();
        }else{
            result = derivedUnit.getCatalogNumber();
        }
        if(deduplicateCollectionCodeInNumber){
            String code = getCollectionCode(derivedUnit);
            if(result != null){
                result = result.trim();
                if(isNotBlank(code) && result.startsWith(code + " ")){
                    result = result.replaceAll("^" + code + "\\s", "");
                }
            }
        }
        return result;
    }

    public String getCollectionCode(DerivedUnit derivedUnit) {
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
