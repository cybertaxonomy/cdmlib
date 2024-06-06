/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author muellera
 * @since 31.05.2024
 */
public class IdentifierDto extends CdmBaseDto {

    private String type;

    private String identifier;

    private String link;

    private UUID typeUuid;

// ************ CONSTRUCTOR ***********************************/

    public IdentifierDto() {
        super(null, null, null);
    }

    public IdentifierDto(UUID uuid, Integer id, LocalDateTime lastUpdated) {
        super(uuid, id, lastUpdated);
    }

// ****************** GETTER / SETTER ************************/

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    public UUID getTypeUuid() {
        return typeUuid;
    }
    public void setTypeUuid(UUID typeUuid) {
        this.typeUuid = typeUuid;
    }
}