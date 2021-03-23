/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.Set;
import java.util.stream.Collectors;

import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.ref.TaggedEntityReference;

/**
 * @author a.kohlbecker
 * @since Mar 23, 2021
 */
public class DeterminationEventDTO extends EventDTO<DeterminationEvent> {

    private static final long serialVersionUID = -716568268770456996L;

    private TaggedEntityReference<?> determination;

    private boolean isPreferred = false;

    private DefinedTerm modifier = null;

    private Set<ReferenceDTO> references = null;

    public DeterminationEventDTO(DeterminationEvent entity) {
        super(DeterminationEvent.class, entity.getUuid());
    }

    public static DeterminationEventDTO from(DeterminationEvent entity) {
        if(entity == null) {
            return null;
        }
        DeterminationEventDTO dto = new DeterminationEventDTO(entity);
        if(entity.getTaxon() != null) {
            dto.setDetermination(new TaggedEntityReference(entity.getTaxon().getClass(), entity.getTaxon().getUuid(), entity.getTaxon().getTaggedTitle()));
        } else if(entity.getTaxonName() != null) {
            dto.setDetermination(new TaggedEntityReference(entity.getTaxonName().getClass(), entity.getTaxonName().getUuid(), entity.getTaxonName().getTaggedName()));
        }
        dto.modifier = entity.getModifier();
        dto.isPreferred = entity.getPreferredFlag();
        if(!entity.getReferences().isEmpty()) {
            dto.references = entity.getReferences().stream().map(r -> ReferenceDTO.fromReference(r)).collect(Collectors.toSet());
        }
        return dto;
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

    public DefinedTerm getModifier() {
        return modifier;
    }

    public void setModifier(DefinedTerm modifier) {
        this.modifier = modifier;
    }

    public Set<ReferenceDTO> getReferences() {
        return references;
    }

    public void setReferences(Set<ReferenceDTO> references) {
        this.references = references;
    }
}
