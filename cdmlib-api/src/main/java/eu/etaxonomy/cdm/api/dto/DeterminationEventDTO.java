/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.ref.TaggedEntityReference;

/**
 * @author a.kohlbecker
 * @since Mar 23, 2021
 */
public class DeterminationEventDTO extends EventDTO<DeterminationEvent> {

    private static final long serialVersionUID = 5895155323153858101L;

    private TaggedEntityReference<?> determination;

    private boolean isPreferred = false;

    private DefinedTermDTO modifier = null;

    private Set<ReferenceDTO> references = null;

    public DeterminationEventDTO(Class<DeterminationEvent> clazz, UUID uuid) {
        super(clazz, uuid);
    }

    public TaggedEntityReference<?> getDetermination() {
        return determination;
    }
    public void setDetermination(TaggedEntityReference<?> determination) {
        this.determination = determination;
    }

    public boolean isPreferred() {
        return isPreferred;
    }
    public void setPreferred(boolean isPreferred) {
        this.isPreferred = isPreferred;
    }

    public DefinedTermDTO getModifier() {
        return modifier;
    }
    public void setModifier(DefinedTermDTO modifier) {
        this.modifier = modifier;
    }

    public Set<ReferenceDTO> getReferences() {
        return references;
    }
    public void setReferences(Set<ReferenceDTO> references) {
        this.references = references;
    }
}