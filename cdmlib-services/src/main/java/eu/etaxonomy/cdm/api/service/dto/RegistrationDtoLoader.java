/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Arrays;

import eu.etaxonomy.cdm.api.dto.RegistrationDTO;
import eu.etaxonomy.cdm.api.dto.RegistrationDTO.RankedNameReference;
import eu.etaxonomy.cdm.api.dto.RegistrationType;
import eu.etaxonomy.cdm.api.service.exception.TypeDesignationSetException;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainer;
import eu.etaxonomy.cdm.api.service.name.TypeDesignationGroupContainerFormatter;
import eu.etaxonomy.cdm.format.reference.NomenclaturalSourceFormatter;
import eu.etaxonomy.cdm.format.reference.OriginalSourceFormatter;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.NamedSourceBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;

/**
 * Loader for {@link RegistrationWrapperDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class RegistrationDtoLoader {

    public static RegistrationDtoLoader INSTANCE(){
        return new RegistrationDtoLoader();
    }

    public RegistrationDTO fromEntity(Registration reg){
        String label = null; //for now, we do not provide a label. Do we need it?
        RegistrationDTO dto = new RegistrationDTO(Registration.class, reg.getUuid(), label);
        load(dto, reg);
        return dto;
    }

    private void load(RegistrationDTO dto, Registration reg) {

        RegistrationType registrationType = typeFrom(reg);
        dto.setRegistrationType(registrationType);

        if(reg.getSubmitter() != null ){
            dto.setSubmitterUserName(reg.getSubmitter().getUsername());
        }

        if(reg.hasName()){
            TaxonName taxonName = reg.getName();
            RankedNameReference nameRef = new RankedNameReference(taxonName.getUuid(),
                    taxonName.getTitleCache(), taxonName.isSupraSpecific());
            dto.setNameRef(nameRef);
        }

        switch (registrationType) {
        case EMPTY:
            String summary = "BLANK REGISTRATION";
            dto.setSummary(summary);
            dto.addSummaryTaggedText(Arrays.asList(new TaggedText(TagEnum.label, summary)));
            break;
        case NAME:
            summary = reg.getName().getTitleCache();
            dto.setSummary(summary);
            dto.addSummaryTaggedText(reg.getName().getTaggedName());
            break;
        case NAME_AND_TYPIFICATION:
        case TYPIFICATION:
        default:
            try {
                TypeDesignationGroupContainer typeDesignationSetContainer = TypeDesignationGroupContainer.NewDefaultInstance(reg.getTypeDesignations());
                dto.addSummaryTaggedText(new TypeDesignationGroupContainerFormatter(false, true, true, true, false, null)
                        .toTaggedText(typeDesignationSetContainer));
                summary = TaggedTextFormatter.createString(dto.getSummaryTaggedText());
                dto.setSummary(summary);
            } catch (TypeDesignationSetException e) {
                dto.addValidationProblem("Validation errors: " + e.getMessage());
            }
            break;
        }

        Reference citation = null;
        NamedSourceBase citedSource = reg.findCitedSource();
        if(citedSource != null) {
            dto.setCitationDetail(citedSource.getCitationMicroReference());
            //TODO DTO
//            dto.setCitationPure(publishedUnit.getCitation());
            citation = citedSource.getCitation();
        }

        makeBibliographicCitationStrings(dto, citation, dto.getCitationDetail());
        makeNomenclaturalCitationString(dto, citation, dto.getCitationDetail());
    }

    private static void makeBibliographicCitationStrings(RegistrationDTO dto, Reference citation, String detail) {
        if(citation == null){
            dto.setBibliographicCitationString(null);
        } else {
            OriginalSourceFormatter formatter = OriginalSourceFormatter.INSTANCE;


            Reference bibliographicCitation;
            String bibliographicCitationDetail = detail;
            if((citation.getType() == ReferenceType.Section || citation.getType() == ReferenceType.BookSection) && citation.getInReference() != null){
                bibliographicCitation = citation.getInReference();
                bibliographicCitationDetail = null; // can possibly be known once https://dev.e-taxonomy.eu/redmine/issues/6623 is solved
            } else {
                bibliographicCitation = citation;
            }
            String bibliographicInRefCitationString = formatter.format(bibliographicCitation, bibliographicCitationDetail);
            dto.setBibliographicInRefCitationString(bibliographicInRefCitationString);

            String bibliographicCitationString = formatter.format(citation, detail);
            dto.setBibliographicCitationString(bibliographicCitationString);
        }
    }

    private static void makeNomenclaturalCitationString(RegistrationDTO dto, Reference citation, String detail) {
        if(citation == null){
            dto.setNomenclaturalCitationString(null);
        } else {
            String nomenclaturalCitationString = NomenclaturalSourceFormatter.INSTANCE().format(citation, detail);
            dto.setNomenclaturalCitationString(nomenclaturalCitationString);
        }
    }

    public static RegistrationType typeFrom(Registration reg) {

        if (reg.getName() != null && reg.getTypeDesignations() != null && reg.getTypeDesignations().size() > 0) {
            return RegistrationType.NAME_AND_TYPIFICATION;
        }
        if (reg.getName() != null) {
            return RegistrationType.NAME;
        }
        if (reg.getTypeDesignations().size() > 0) {
            return RegistrationType.TYPIFICATION;
        }
        return RegistrationType.EMPTY;
    }
}