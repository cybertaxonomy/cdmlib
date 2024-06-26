/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * TODO probably not in use anymore after implementation of #10222
 *
 * @author a.kohlbecker
 * @since Aug 31, 2018
 */
public class SourceDTO implements Serializable{

    private static final long serialVersionUID = -3314135226037542122L;

    private UUID uuid;
    private String label;
    private String citationDetail;
    // can not reduce to TypedEntityReference here since the data portal requires
    // doi, uri, etc, see function cdm_reference_markup() in cdm_dataportal
    private ReferenceDTO citation;

//********************* GETTER / SETTER ***************************/

    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ReferenceDTO getCitation() {
        return citation;
    }
    public void setCitation(ReferenceDTO citation) {
        this.citation = citation;
    }

    public String getCitationDetail() {
        return citationDetail;
    }
    public void setCitationDetail(String citationDetail) {
        this.citationDetail = citationDetail;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
}