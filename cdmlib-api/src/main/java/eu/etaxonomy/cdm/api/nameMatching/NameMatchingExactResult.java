/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

import java.util.UUID;

/**
 * @author muellera
 * @since 03.04.2024
 */
public class NameMatchingExactResult {

    private Integer taxonNameId;

    private UUID taxonNameUuid;

    private String nameWithAuthor;

    private String authorship;

    private String pureName;


    public Integer getTaxonNameId() {
        return taxonNameId;
    }
    public void setTaxonNameId(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }

    public UUID getTaxonNameUuid() {
        return taxonNameUuid;
    }
    public void setTaxonNameUuid(UUID taxonNameUuid) {
        this.taxonNameUuid = taxonNameUuid;
    }

    public String getNameWithAuthor() {
        return nameWithAuthor;
    }
    public void setNameWithAuthor(String nameWithAuthor) {
        this.nameWithAuthor = nameWithAuthor;
    }

    public String getAuthorship() {
        return authorship;
    }
    public void setAuthorship(String authorship) {
        this.authorship = authorship;
    }

    public String getPureName() {
        return pureName;
    }
    public void setPureName(String pureName) {
        this.pureName = pureName;
    }

}