/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author muellera
 * @since 16.02.2024
 */
public class PrimerDTO extends TypedEntityReference<Primer> {

    private static final long serialVersionUID = 1818785107821492678L;

    public PrimerDTO(Class<Primer> type, UUID uuid, String label) {
        super(type, uuid, label);
    }

    private String label;
    private String sequence;

    private DefinedTermDTO dnaMarker;

    private ReferenceDTO publishedIn;

    public String getSequence() {
        return sequence;
    }
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public DefinedTermDTO getDnaMarker() {
        return dnaMarker;
    }
    public void setDnaMarker(DefinedTermDTO dnaMarker) {
        this.dnaMarker = dnaMarker;
    }

    public ReferenceDTO getPublishedIn() {
        return publishedIn;
    }
    public void setPublishedIn(ReferenceDTO publishedIn) {
        this.publishedIn = publishedIn;
    }

}
