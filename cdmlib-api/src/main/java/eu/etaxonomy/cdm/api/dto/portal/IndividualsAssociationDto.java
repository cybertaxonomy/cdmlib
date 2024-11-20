/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

/**
 * @author a.mueller
 * @date 13.02.2023
 */
public class IndividualsAssociationDto extends FactDtoBase {

    private String description;
    private String modifyingText; //Note: unclear, if modifying text makes sense IndividualsAssociations. Use description instead?

    private String occurrence;
    private UUID occurrenceUuid;

    // ****************** GETTER / SETTER *****************************/

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getModifyingText() {
        return modifyingText;
    }
    public void setModifyingText(String modifyingText) {
        this.modifyingText = modifyingText;
    }

    public String getOccurrence() {
        return occurrence;
    }
    public void setOccurrence(String occurrence) {
        this.occurrence = occurrence;
    }

    public UUID getOccurrenceUuid() {
        return occurrenceUuid;
    }
    public void setOccurrenceUuid(UUID occurrenceUuid) {
        this.occurrenceUuid = occurrenceUuid;
    }
}