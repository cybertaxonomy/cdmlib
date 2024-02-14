/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.api.dto.DerivedUnitStatusDto;
import eu.etaxonomy.cdm.model.occurrence.OccurrenceStatus;

/**
 * Loader for {@link DerivedUnitStatusDto}s. Extracted from DTO class.
 *
 * @author muellera
 * @since 13.02.2024
 */
public class DerivedUnitStatusDtoLoader {

    public static DerivedUnitStatusDto fromStatus(OccurrenceStatus status) {
        DerivedUnitStatusDto dto = new DerivedUnitStatusDto(status.getType().getLabel());
        dto.setStatusSource(SourceDtoLoader.fromDescriptionElementSource(status.getSource()));
        return dto;
    }
}
