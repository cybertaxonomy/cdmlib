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

import org.joda.time.DateTime;

import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto;
import eu.etaxonomy.cdm.api.dto.portal.config.TaxonPageDtoConfiguration;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
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

       result.id = taxon.getId();
       result.uuid = taxon.getUuid();
       result.lastUpdated = getLastUpdated(null, taxon);
       result.nameLabel = name != null? name.getTitleCache() : "";
       result.taxonLabel = CdmUtils.Nz(taxon.getTitleCache());

       return result;
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


       if (javaLocalDateTimeOfEntity == null || javaLocalDateTimeOfEntity.compareTo(existingLastDate) < 0)  {
           return existingLastDate;
       }else {
           return javaLocalDateTimeOfEntity;
       }
    }

}
