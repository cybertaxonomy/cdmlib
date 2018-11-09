// $Id$
/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.net.URI;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermType;

/**
 * @author pplitzner
 * @date 05.11.2018
 *
 */
public class AbstractTermDto implements Serializable {

    private static final long serialVersionUID = -7160319884811828125L;

    private final UUID uuid;
    private URI uri;
    private TermType termType;
    private final Set<Representation> representations;
    private String representation_L10n = null;
    private String representation_L10n_abbreviatedLabel = null;

    public AbstractTermDto(UUID uuid, Set<Representation> representations) {
        this.representations = representations;
        this.uuid = uuid;
    }

    /**
     *
     * @param representation_L10n a blank instance of ITermRepresentation_L10n
     *   created by the  default constructor
     */
    public void localize(ITermRepresentation_L10n representation_L10n) {

        representation_L10n.localize(representations);
        if (representation_L10n.getLabel() != null) {
            setRepresentation_L10n(representation_L10n.getLabel());
        }
        if (representation_L10n.getAbbreviatedLabel() != null) {
            setRepresentation_L10n_abbreviatedLabel(representation_L10n.getAbbreviatedLabel());
        }
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public String getRepresentation_L10n() {
        return representation_L10n;
    }

    public void setRepresentation_L10n(String representation_L10n) {
        this.representation_L10n = representation_L10n;
    }

    public void addRepresentation(Representation representation) {
        representations.add(representation);

    }

    public String getRepresentation_L10n_abbreviatedLabel() {
        return representation_L10n_abbreviatedLabel;
    }

    public void setRepresentation_L10n_abbreviatedLabel(String representation_L10n_abbreviatedLabel) {
        this.representation_L10n_abbreviatedLabel = representation_L10n_abbreviatedLabel;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Set<Representation> getRepresentations() {
        return representations;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractTermDto other = (AbstractTermDto) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }

}
