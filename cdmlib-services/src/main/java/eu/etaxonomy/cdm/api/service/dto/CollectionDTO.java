/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import eu.etaxonomy.cdm.model.occurrence.Collection;

/**
 * @author k.luther
 * @since 21.06.2018
 *
 */
public class CollectionDTO {
    private String code;
    private String codeStandard;
    private String institute;
    private String townOrLocation;


    /**
     * @param code
     * @param codeStandard
     * @param institute
     * @param townOrLocation
     */
    public CollectionDTO(String code, String codeStandard, String institute, String townOrLocation) {
        this.code = code;
        this.codeStandard = codeStandard;
        this.institute = institute;
        this.townOrLocation = townOrLocation;
    }

    /**
     * @param collection
     */
    public CollectionDTO(Collection collection) {
        this(collection.getCode(),collection.getCodeStandard(), collection.getInstitute().getTitleCache(),collection.getTownOrLocation());

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

}
