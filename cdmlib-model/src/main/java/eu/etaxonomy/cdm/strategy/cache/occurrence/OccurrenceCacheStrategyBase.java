/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.occurrence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.strategy.StrategyBase;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;

/**
 * @author a.mueller
 * @since 09.01.2021
 */
public abstract class OccurrenceCacheStrategyBase<T extends SpecimenOrObservationBase>
        extends StrategyBase
        implements IIdentifiableEntityCacheStrategy<T>{

    private static final long serialVersionUID = -6044178882022038807L;

    @Override
    public String getTitleCache(T specimen) {
        return getTitleCache(specimen, false);
    }

    public String getTitleCache(T specimen, boolean emptyIfBlank) {
        if (specimen == null){
            return null;
        }
        String result = doGetTitleCache(specimen);
        if (isBlank(result)){
            if (emptyIfBlank){
                result = "";
            }else{
                result = specimen.toString();
            }
        }
        return result;
    }

    protected abstract String doGetTitleCache(T occurrence);

    protected String getCollectionAndAccession(T occurrence){
        if (occurrence.isInstanceOf(FieldUnit.class)){
            return null;   //does not exist for field units
        }else{
            DerivedUnit specimen = CdmBase.deproxy(occurrence, DerivedUnit.class);
            String result = null;
            if (specimen.getCollection() != null){
                Collection collection = specimen.getCollection();
                if (isNotBlank(collection.getCode())){
                    result = collection.getCode();
                }
            }
            result = CdmUtils.concat(" ", result, CdmUtils.Ne(specimen.getMostSignificantIdentifier()));
            return result;
        }
    }

    protected String truncate(String str, int length){
        if (str == null){
            return null;
        }else{
            if (str.length() > length){
                if (length <3){
                    return str.substring(0, str.length());
                }else{
                    return str.substring(0, str.length()-3)+"...";
                }
            }else{
                return str;
            }
        }
    }

    protected String addPlantDescription(String result, SpecimenOrObservationBase<?> occurrence) {
        //plant description
        result = CdmUtils.concat("; ", result, getPlantDescription(occurrence, null));
        return result;
    }

    private String getPlantDescription(SpecimenOrObservationBase<?> occurrence, Language language) {
        if (language == null) {
            language = Language.DEFAULT();
        }
        Feature feature = Feature.DESCRIPTION();
        LanguageString languageString = getTextDataAll(occurrence, feature).get(language);
        return (languageString == null ? null : languageString.getText());
    }

    protected Map<Language, LanguageString> getTextDataAll(SpecimenOrObservationBase<?> occurrence, Feature feature) {
        TextData result;
        try {
            result = getTextData(occurrence, feature, false);
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Multiple text data for the same feature not yet supported", e);
        }
        if (result == null) {
            return new HashMap<>();
        }

        return result.getMultilanguageText();
    }

    private TextData getTextData(SpecimenOrObservationBase<?> occurrence,
            Feature feature, boolean isImageGallery) {
        if (feature == null) {
            return null;
        }
        TextData textData = null;

        Set<SpecimenDescription> descriptions;
        if (isImageGallery) {
            descriptions = occurrence.getSpecimenDescriptionImageGallery();
        } else {
            descriptions = occurrence.getSpecimenDescriptions(false);
        }
        // no description exists yet for this specimen
        if (descriptions.size() == 0) {
            return null;
        }
        // description already exists
        Set<DescriptionElementBase> existingTextData = new HashSet<>();
        for (SpecimenDescription description : descriptions) {
            // collect all existing text data
            for (DescriptionElementBase element : description.getElements()) {
                if (element.isInstanceOf(TextData.class)
                        && (feature.equals(element.getFeature()) || isImageGallery)) {
                    existingTextData.add(element);
                }
            }
        }
        // use existing text data if exactly one exists
        if (existingTextData.size() > 1) {
            throw new IllegalStateException(
                    "Specimen facade does not support more than one description text data of type "
                            + feature.getLabel());

        } else if (existingTextData.size() == 1) {
            return CdmBase.deproxy(existingTextData.iterator().next(),
                    TextData.class);
        } else {
            return textData;
        }
    }

}
