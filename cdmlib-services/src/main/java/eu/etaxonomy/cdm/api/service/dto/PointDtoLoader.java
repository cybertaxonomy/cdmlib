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

import eu.etaxonomy.cdm.api.dto.PointDTO;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.Point.Sexagesimal;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class PointDtoLoader {

    public static PointDTO fromEntity(Point point) {

        if (point == null) {
            return null;
        }
        PointDTO dto = new PointDTO();
        load(dto, point);
        return dto;
    }

    private static void load(PointDTO dto, Point point) {

        //TODO
        List<Language> languages = new ArrayList<>();

        boolean includeEmptySeconds = false;
        boolean removeTertiers = false;

        dto.setLabel(point.toSexagesimalString(includeEmptySeconds, true));

        dto.setLatitude(point.getLatitude());
        dto.setLatitudeSexagesimal(nullSafeSexagesimal(point.getLatitudeSexagesimal(),
                includeEmptySeconds, removeTertiers));

        dto.setLongitude(point.getLongitude());
        dto.setLongitudeSexagesimal(nullSafeSexagesimal(point.getLongitudeSexagesimal(),
                includeEmptySeconds, removeTertiers));

        dto.setErrorRadius(point.getErrorRadius());

        if (point.getReferenceSystem() != null) {
            ReferenceSystem refSys = point.getReferenceSystem();

            String refSysLabel = refSys.getPreferredLabel(languages);
            dto.setReferenceSystemLabel(refSysLabel);

            String refSysAbbrevLabel = refSys.getPreferredAbbreviation(languages, false, true);
            dto.setReferenceSystemAbbrev(refSysAbbrevLabel);
        }
    }

    private static String nullSafeSexagesimal(Sexagesimal sexagesimal,
            boolean includeEmptySeconds, boolean removeTertiers) {
        if (sexagesimal == null) {
            return null;
        }else {
            return sexagesimal.toString(includeEmptySeconds, removeTertiers);
        }
    }
}
