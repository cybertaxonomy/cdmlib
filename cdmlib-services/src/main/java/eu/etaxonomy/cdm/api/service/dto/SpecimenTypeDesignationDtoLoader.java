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
import java.util.List;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.api.dto.SpecimenTypeDesignationDTO;
import eu.etaxonomy.cdm.api.service.l10n.TermRepresentation_L10n;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.ref.EntityReference;
import eu.etaxonomy.cdm.ref.TypedEntityReferenceFactory;

/**
 * Loader for {@link SpecimenTypeDesignationDTO}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class SpecimenTypeDesignationDtoLoader {

   public static SpecimenTypeDesignationDTO fromEntity(SpecimenTypeDesignation typeDesignation)  {

       String label = null;
       SpecimenTypeDesignationDTO dto = new SpecimenTypeDesignationDTO(
               SpecimenTypeDesignation.class, typeDesignation.getUuid(), label);

       if (typeDesignation.getTypeStatus() != null){
           TermRepresentation_L10n term_L10n = new TermRepresentation_L10n(typeDesignation.getTypeStatus(), false);
           dto.setTypeStatus_L10n(term_L10n.getText());

       }
       List<EntityReference> names = new ArrayList<>();
       for (TaxonName name:typeDesignation.getTypifiedNames()){
           names.add(new EntityReference(name.getUuid(), name.getTitleCache()));
       }
       dto.setNames(names);
       dto.setDesignationSource(SourceDtoLoader.fromEntity(typeDesignation.getDesignationSource()));
       dto.setTypeSpecimen(TypedEntityReferenceFactory.fromEntity(typeDesignation.getTypeSpecimen()));
       dto.setRegistrations(typeDesignation.getRegistrations().stream()
               .map(reg -> RegistrationDtoLoader.INSTANCE().fromEntity(reg))
               .collect(Collectors.toList()));
       return dto;
   }
}