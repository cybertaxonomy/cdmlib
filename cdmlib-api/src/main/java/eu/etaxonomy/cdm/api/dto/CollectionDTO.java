/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author k.luther
 * @since 21.06.2018
 */
public class CollectionDTO extends TypedEntityReference<Collection> {

    private static final long serialVersionUID = -1840237876297997573L;

    private String code;
    private String codeStandard;
    private String institute;
    private String townOrLocation;
    private CollectionDTO superCollection;

    public CollectionDTO(Class<Collection> clazz, UUID uuid, String label) {
        super(clazz, uuid, label);
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeStandard() {
        return codeStandard;
    }
    public void setCodeStandard(String codeStandard) {
        this.codeStandard = codeStandard;
    }

    public String getInstitute() {
        return institute;
    }
    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getTownOrLocation() {
        return townOrLocation;
    }
    public void setTownOrLocation(String townOrLocation) {
        this.townOrLocation = townOrLocation;
    }

    public CollectionDTO getSuperCollection() {
        return superCollection;
    }
    public void setSuperCollection(CollectionDTO superCollection) {
        this.superCollection = superCollection;
    }
}