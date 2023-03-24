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
 * @date 19.01.2023
 */
public class MarkerDto extends CdmBaseDto {

    private Boolean value;
    private UUID typeUuid;
    private String type;

    public Boolean getValue() {
        return value;
    }
    public void setValue(Boolean value) {
        this.value = value;
    }
    public UUID getTypeUuid() {
        return typeUuid;
    }
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
