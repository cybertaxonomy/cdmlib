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
public class CommonNameDto extends SourcedDto implements IFactDto {

    private String language;
    private UUID languageUuid;

    private String area;
    private UUID areaUUID;

    private String name;

 // ****************** GETTER / SETTER *****************************/

    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    public UUID getLanguageUuid() {
        return languageUuid;
    }
    public void setLanguageUuid(UUID languageUuid) {
        this.languageUuid = languageUuid;
    }

    public String getArea() {
        return area;
    }
    public void setArea(String area) {
        this.area = area;
    }

    public UUID getAreaUUID() {
        return areaUUID;
    }
    public void setAreaUUID(UUID areaUUID) {
        this.areaUUID = areaUUID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getClazz() {
        return this.getClass().getSimpleName();
    }
}