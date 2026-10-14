/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * @author muellera
 * @since 17.02.2024
 */
public class DtoUtil {


    public static LocalDateTime fromDateTime(ZonedDateTime dateToAdd) {

        LocalDateTime result = dateToAdd == null ? null:
            LocalDateTime.of(dateToAdd.getYear(), dateToAdd.getMonth(),
                    dateToAdd.getDayOfMonth(), dateToAdd.getHour(),
                    dateToAdd.getMinute(), dateToAdd.getSecond());
        return result;
    }
}
