/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.term.Representation;
/**
 * @author k.luther
 * @since Oct 6, 2020
 */
public class CharacterDto extends FeatureDto {
    private static final long serialVersionUID = 1743178749047550590L;

    private TermNodeDto structure;
    private TermDto structureModifier;
    private TermNodeDto property;
    private TermDto propertyModifier;


    /**
     * @param uuid
     * @param representations
     * @param partOfUuid
     * @param kindOfUuid
     * @param vocabularyUuid
     * @param orderIndex
     * @param idInVocabulary
     * @param vocRepresentations
     * @param isAvailableForTaxon
     * @param isAvailableForTaxonName
     * @param isAvailableForOccurrence
     * @param titleCache
     * @param structure
     * @param structureModifier
     * @param property
     * @param propertyModifier
     */
    public CharacterDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, Set<Representation> vocRepresentations,
            boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence,
            String titleCache, TermNodeDto structure, TermDto structureModifier, TermNodeDto property,
            TermDto propertyModifier) {
        super(uuid, representations, partOfUuid, kindOfUuid, vocabularyUuid, orderIndex, idInVocabulary,
                vocRepresentations, isAvailableForTaxon, isAvailableForTaxonName, isAvailableForOccurrence, titleCache);
        this.structure = structure;
        this.structureModifier = structureModifier;
        this.property = property;
        this.propertyModifier = propertyModifier;
    }

    /**
     * @param character
     */
    public CharacterDto(Character character) {
       super(character.getUuid(), character.getRepresentations(), character.getPartOf().getUuid(), character.getKindOf().getUuid(), character.getVocabulary().getUuid(), null, character.getIdInVocabulary(), character.getVocabulary().getRepresentations(),
                character.isAvailableForTaxon(), character.isAvailableForTaxonName(), character.isAvailableForOccurrence(), character.getTitleCache());
       this.property = new TermNodeDto(character.getProperty());
       this.propertyModifier = TermDto.fromTerm(character.getPropertyModifier());
       this.structure = new TermNodeDto(character.getStructure());
       this.structureModifier = TermDto.fromTerm(character.getStructureModifier());
    }



    public TermNodeDto getStructure() {
        return structure;
    }

    public void setStructure(TermNodeDto structure) {
        this.structure = structure;
    }

    public TermDto getStructureModifier() {
        return structureModifier;
    }

    public void setStructureModifier(TermDto structureModifier) {
        this.structureModifier = structureModifier;
    }

    public TermNodeDto getProperty() {
        return property;
    }

    public void setProperty(TermNodeDto property) {
        this.property = property;
    }

    public TermDto getPropertyModifier() {
        return propertyModifier;
    }

    public void setPropertyModifier(TermDto propertyModifier) {
        this.propertyModifier = propertyModifier;
    }


}
