/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.common.DOI;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Oct 8, 2020
 */
public class ReferenceDTO extends TypedEntityReference<Reference> {

    private static final long serialVersionUID = -2619569446682158500L;

    //TODO is this needed? We have "label" in TypedEntityReference
    private String titleCache;
    private String abbrevTitleCache;
    private URI uri;
    private DOI doi;
    private VerbatimTimePeriod datePublished;

    //should only be used by loader classes
    public ReferenceDTO(Class<Reference> clazz, UUID uuid, String label) {
        super(clazz, uuid, label);
    }

    public String getTitleCache() {
        return titleCache;
    }
    public void setTitleCache(String titleCache) {
        this.titleCache = titleCache;
    }

    public String getAbbrevTitleCache() {
        return abbrevTitleCache;
    }
    public void setAbbrevTitleCache(String abbrevTitleCache) {
        this.abbrevTitleCache = abbrevTitleCache;
    }

    public URI getUri() {
        return uri;
    }
    public void setUri(URI uri) {
        this.uri = uri;
    }

    public DOI getDoi() {
        return doi;
    }
    public void setDoi(DOI doi) {
        this.doi = doi;
    }

    public VerbatimTimePeriod getDatePublished() {
        return datePublished;
    }
    public void setDatePublished(VerbatimTimePeriod datePublished) {
        this.datePublished = datePublished;
    }
}