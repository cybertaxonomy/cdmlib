/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import eu.etaxonomy.cdm.api.dto.DerivedUnitDTO;
import eu.etaxonomy.cdm.api.dto.DerivedUnitStatusDto;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.CdmFormatterFactory;
import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.description.DefaultCategoricalDescriptionBuilder;
import eu.etaxonomy.cdm.format.description.DefaultQuantitativeDescriptionBuilder;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatusBase;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 14.02.2024
 */
public abstract class DerivedUnitDtoLoaderBase<T extends DerivedUnit>
        extends SpecimenOrObservationBaseDtoLoader<DerivedUnitDTO>{


    public abstract DerivedUnitDTO fromEntity(T entity);

    protected void load(DerivedUnitDTO dto, DerivedUnit derivedUnit) {

        super.load(derivedUnit, dto);

        // experimental feature, not yet exposed in method signature
        boolean cleanAccessionNumber = false;
        dto.setAccessionNumber(derivedUnit.getAccessionNumber());
        dto.setPreferredStableUri(derivedUnit.getPreferredStableUri());
        if (derivedUnit.getCollection() != null){
            dto.setCollectioDTO(CollectionDtoLoader.INSTANCE().fromEntity(
                    HibernateProxyHelper.deproxy(derivedUnit.getCollection())));

            if(cleanAccessionNumber && dto.getCollection().getCode() != null) {
                dto.setAccessionNumber(dto.getAccessionNumber().replaceFirst("^" + Pattern.quote(dto.getCollection().getCode()) + "-", ""));
            }
        }
        dto.setBarcode(derivedUnit.getBarcode());
        dto.setCatalogNumber(derivedUnit.getCatalogNumber());
        dto.setDerivationEvent(DerivationEventDTO.fromEntity(derivedUnit.getDerivedFrom()));
        if (derivedUnit.getPreservation()!= null){
            dto.setPreservationMethod(derivedUnit.getPreservation().getMaterialMethodText());
        }
        dto.setRecordBase(derivedUnit.getRecordBasis());
        dto.setSources(derivedUnit.getSources());
        setSpecimenTypeDesignations(dto, derivedUnit.getSpecimenTypeDesignations());

        // -------------------------------------------------------------

        dto.setMostSignificantIdentifier(derivedUnit.getMostSignificantIdentifier());

        //specimenShortTitle
        dto.setSpecimenShortTitle(composeSpecimenShortTitle(derivedUnit));

        //preferred stable URI
        dto.setPreferredStableUri(derivedUnit.getPreferredStableUri());

        // label
        dto.setSummaryLabel(derivedUnit.getTitleCache());

        // character state data
        if(derivedUnit.characterData() != null) {
            Collection<DescriptionElementBase> characterDataForSpecimen = derivedUnit.characterData();
            for (DescriptionElementBase descriptionElementBase : characterDataForSpecimen) {
                String character = descriptionElementBase.getFeature().getLabel();
                ArrayList<Language> languages = new ArrayList<>(Collections.singleton(Language.DEFAULT()));
                if (descriptionElementBase instanceof QuantitativeData) {
                    QuantitativeData quantitativeData = (QuantitativeData) descriptionElementBase;
                    DefaultQuantitativeDescriptionBuilder builder = new DefaultQuantitativeDescriptionBuilder();
                    String state = builder.build(quantitativeData, languages).getText(Language.DEFAULT());
                    dto.addCharacterData(character, state);
                }
                else if(descriptionElementBase instanceof CategoricalData){
                    CategoricalData categoricalData = (CategoricalData) descriptionElementBase;
                    DefaultCategoricalDescriptionBuilder builder = new DefaultCategoricalDescriptionBuilder();
                    String state = null;
                    if (categoricalData.getNoDataStatus()!= null) {
                        state = categoricalData.getNoDataStatus().getLabel(Language.DEFAULT());
                    }else {
                        state = builder.build(categoricalData, languages).getText(Language.DEFAULT());
                    }

                    dto.addCharacterData(character, state);
                }
            }
        }

        // check type designations
        Collection<SpecimenTypeDesignation> specimenTypeDesignations = derivedUnit.getSpecimenTypeDesignations();
        for (SpecimenTypeDesignation specimenTypeDesignation : specimenTypeDesignations) {
            TypeDesignationStatusBase<?> typeStatus = specimenTypeDesignation.getTypeStatus();
            Set<TaxonName> typifiedNames = specimenTypeDesignation.getTypifiedNames();
            List<String> typedTaxaNames = new ArrayList<>();
            for (TaxonName taxonName : typifiedNames) {
                typedTaxaNames.add(taxonName.getTitleCache());
            }
            dto.addTypes(typeStatus!=null?typeStatus.getLabel():"", typedTaxaNames);
        }
        Collection<OccurrenceStatus> occurrenceStatus = derivedUnit.getStatus();

        if (occurrenceStatus != null && !occurrenceStatus.isEmpty()) {

            List<DerivedUnitStatusDto> status = new ArrayList<>();

            for (OccurrenceStatus specimenStatus : occurrenceStatus) {
                DerivedUnitStatusDto statusDto = new DerivedUnitStatusDto(specimenStatus.getType().getLabel());
                statusDto.setStatusSource(SourceDtoLoader.fromDescriptionElementSource(specimenStatus.getSource()) ) ;
                status.add(statusDto);
            }
            dto.setStatus(status);
        }
        dto.setDerivationTreeSummary(DerivationTreeSummaryDtoLoader.fromEntity(derivedUnit, dto.getSpecimenShortTitle()));

        if(derivedUnit.getStoredUnder() != null) {
            dto.setStoredUnder(TypedEntityReference.fromEntity(derivedUnit.getStoredUnder()));
        }
        dto.setOriginalLabelInfo(derivedUnit.getOriginalLabelInfo());
        dto.setExsiccatum(derivedUnit.getExsiccatum());
    }

    private static String composeSpecimenShortTitle(DerivedUnit derivedUnit) {
        FormatKey collectionKey = FormatKey.COLLECTION_CODE;
        String specimenShortTitle = CdmFormatterFactory.format(derivedUnit, collectionKey);
        if (CdmUtils.isBlank(specimenShortTitle)) {
            collectionKey = FormatKey.COLLECTION_NAME;
        }
        if(CdmUtils.isNotBlank(derivedUnit.getMostSignificantIdentifier())){
            specimenShortTitle = CdmFormatterFactory.format(derivedUnit, new FormatKey[] {
                    collectionKey,
                    FormatKey.SPACE,
                    FormatKey.MOST_SIGNIFICANT_IDENTIFIER
                    });
            if(!specimenShortTitle.isEmpty() && derivedUnit instanceof MediaSpecimen) {
                Media media = ((MediaSpecimen)derivedUnit).getMediaSpecimen();
                if(media != null && !CdmUtils.isBlank(media.getTitleCache()) ) {
                    if(media.getTitle() != null && !media.getTitle().getText().isEmpty()) {
                        specimenShortTitle += " (" + media.getTitle().getText() + ")";
                    }
                }
            }
        }
        if(CdmUtils.isBlank(specimenShortTitle)){
            specimenShortTitle = derivedUnit.getTitleCache();
        }
        if(CdmUtils.isBlank(specimenShortTitle)){  //should not be necessary as titleCache should never be empty
            specimenShortTitle = derivedUnit.getUuid().toString();
        }
        return specimenShortTitle;
    }
}