/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.dto.portal.ContainerDto;
import eu.etaxonomy.cdm.api.dto.portal.FactDto;
import eu.etaxonomy.cdm.api.dto.portal.FeatureDto;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.TypedLabel;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Loads the portal dto from a taxon instance.
 * Maybe later also supports loading from persistence.
 *
 * @author a.mueller
 * @date 09.01.2023
 */
public class PortalDtoLoader {

   public TaxonPageDto load(Taxon taxon, TaxonPageDtoConfiguration config) {
       TaxonPageDto result = new TaxonPageDto();

       TaxonName name = taxon.getName();

       result.setId(taxon.getId());
       result.setUuid(taxon.getUuid());
       result.setLastUpdated(getLastUpdated(null, taxon));
       result.setNameLabel(name != null? name.getTitleCache() : "");
       result.setTaxonLabel(CdmUtils.Nz(taxon.getTitleCache()));

       //TODO load feature tree
       Map<Feature,Set<DescriptionElementBase>> featureMap = new HashMap<>();

       for (TaxonDescription taxonDescription : taxon.getDescriptions()) {
           if (taxonDescription.isImageGallery()) {
               continue;
           }
           for (DescriptionElementBase deb : taxonDescription.getElements()) {
               Feature feature = deb.getFeature();
               if (featureMap.get(feature) == null) {
                   featureMap.put(feature, new HashSet<>());
               }
               featureMap.get(feature).add(deb);
           }
       }

       //TODO sort
        if (!featureMap.isEmpty()) {
            ContainerDto<FeatureDto> features = new ContainerDto<>();
            result.setFactualData(features);
            for (Feature feature : featureMap.keySet()) {
                FeatureDto featureDto = new FeatureDto();
                featureDto.setId(feature.getId());
                featureDto.setUuid(feature.getUuid());
                //TODO locale
                featureDto.setLabel(feature.getTitleCache());
                features.addItem(featureDto);

                //
                for (DescriptionElementBase fact : featureMap.get(feature)){
                    handleFact(featureDto, fact);
                }
            }
        }

       return result;
   }

    private void handleFact(FeatureDto featureDto, DescriptionElementBase fact) {
        if (fact.isInstanceOf(TextData.class)) {
            TextData td = CdmBase.deproxy(fact, TextData.class);
            //TODO locale
            Language lang = null;
            LanguageString ls = td.getPreferredLanguageString(lang);
            String text = ls == null ? "" : CdmUtils.Nz(ls.getText());

            FactDto factDto = new FactDto();
            featureDto.getFacts().add(factDto);
            TypedLabel typedLabel = new TypedLabel();
            typedLabel.setClassAndId(td);
            typedLabel.setLabel(text);
            factDto.getTypedLabel().add(typedLabel);
        }else {
//            TODO
        }

    }

    /**
     * Compares an existing last date and the last date of an entity
     * and returns the resulting last date.
     */
    private LocalDateTime getLastUpdated(LocalDateTime existingLastDate, VersionableEntity dateToAddEntity) {

        DateTime dateToAdd = dateToAddEntity.getUpdated() != null ? dateToAddEntity.getUpdated() : dateToAddEntity.getCreated();

        LocalDateTime javaLocalDateTimeOfEntity = dateToAdd == null ? null:
                LocalDateTime.of(dateToAdd.getYear(), dateToAdd.getMonthOfYear(),
                        dateToAdd.getDayOfMonth(), dateToAdd.getHourOfDay(),
                        dateToAdd.getMinuteOfHour(), dateToAdd.getSecondOfMinute());

       if (existingLastDate == null) {
           return javaLocalDateTimeOfEntity;
       }else if (javaLocalDateTimeOfEntity == null || javaLocalDateTimeOfEntity.compareTo(existingLastDate) < 0)  {
           return existingLastDate;
       }else {
           return javaLocalDateTimeOfEntity;
       }
    }

}
