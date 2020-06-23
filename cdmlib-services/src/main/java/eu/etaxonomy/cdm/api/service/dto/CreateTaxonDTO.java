/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author k.luther
 * @since Apr 23, 2020
 */
public class CreateTaxonDTO implements Serializable{

    private static final long serialVersionUID = 9183429250677087219L;

    //uuid to a persisted name
    private UUID nameUuid;
    //name string if no persisted name is used
    private String taxonNameString;
    private NomenclaturalCode nomenclaturalCode;
    private Rank preferredRank;

    private UUID secUuid;
    private String secMicroReference;
    private boolean isDoubtful;
    private String appendedPhrase;
    private boolean isPublish;

    public CreateTaxonDTO(UUID nameUuid, UUID secUuid, String secMicroRef, boolean isDoubtful,
            String appendedPhrase, boolean isPublish, String taxonNameString,
            NomenclaturalCode nomenclaturalCode, Rank preferredRank){
        this.nameUuid = nameUuid;
        this.secUuid = secUuid;
        this.secMicroReference = secMicroRef;
        this.isDoubtful = isDoubtful;
        this.appendedPhrase= appendedPhrase;
        this.isPublish = isPublish;
        this.taxonNameString = taxonNameString;
        this.nomenclaturalCode = nomenclaturalCode;
        this.preferredRank = preferredRank;
    }

    public UUID getNameUuid() {
        return nameUuid;
    }
    public void setNameUuid(UUID nameUuid) {
        this.nameUuid = nameUuid;
    }

    public UUID getSecUuid() {
        return secUuid;
    }
    public void setSecUuid(UUID secUuid) {
        this.secUuid = secUuid;
    }

    public String getSecMicroReference() {
        return secMicroReference;
    }
    public void setSecMicroReference(String secMicroReference) {
        this.secMicroReference = secMicroReference;
    }

    public boolean isDoubtful() {
        return isDoubtful;
    }
    public void setDoubtful(boolean isDoubtful) {
        this.isDoubtful = isDoubtful;
    }

    public String getAppendedPhrase() {
        return appendedPhrase;
    }
    public void setAppendedPhrase(String appendedPhrase) {
        this.appendedPhrase = appendedPhrase;
    }

    public boolean isPublish() {
        return isPublish;
    }
    public void setPublish(boolean isPublish) {
        this.isPublish = isPublish;
    }

    public String getTaxonNameString() {
        return taxonNameString;
    }

    public void setTaxonNameString(String taxonNameString) {
        this.taxonNameString = taxonNameString;
    }

    public NomenclaturalCode getCode() {
        return nomenclaturalCode;
    }

    public Rank getPreferredRank() {
        return preferredRank;
    }
}
