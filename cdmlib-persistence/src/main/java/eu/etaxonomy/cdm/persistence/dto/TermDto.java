/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author andreas
 * @since Mar 25, 2015
 *
 */
public class TermDto extends AbstractTermDto{

    private static final long serialVersionUID = 5627308906985438034L;

    private UUID kindOfUuid = null;
    private UUID partOfUuid = null;
    private UUID vocabularyUuid = null;
    private TermDto kindOfDto = null;
    private TermDto partOfDto = null;
    private TermVocabularyDto vocabularyDto = null;
    private Integer orderIndex = null;
    private String idInVocabulary = null;
    private Collection<TermDto> includes;
    private Collection<TermDto> generalizationOf;
    private Set<Representation> vocRepresentations = null;
    private String vocRepresentation_L10n = null;
    private String vocRepresentation_L10n_abbreviatedLabel = null;

    private TermDto(UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary) {
        this(uuid, representations, termType, partOfUuid, kindOfUuid, vocabularyUuid, orderIndex, idInVocabulary, null);
    }

    private TermDto(UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, Set<Representation> vocRepresentations) {
        super(uuid, representations);
        this.partOfUuid = partOfUuid;
        this.kindOfUuid = kindOfUuid;
        this.vocabularyUuid = vocabularyUuid;
        this.orderIndex = orderIndex;
        this.idInVocabulary = idInVocabulary;
        this.vocRepresentations = vocRepresentations;
        setTermType(termType);
    }

    static public TermDto fromTerm(DefinedTermBase term) {
        return fromTerm(term, null, false);
    }

    static public TermDto fromTerm(DefinedTermBase term, boolean initializeToTop) {
        return fromTerm(term, null, initializeToTop);
    }

    static public TermDto fromTerm(DefinedTermBase term, Set<Representation> representations) {
        return fromTerm(term, representations, false);
    }

    static public TermDto fromTerm(DefinedTermBase term, Set<Representation> representations, boolean initializeToTop) {
        DefinedTermBase partOf = term.getPartOf();
        DefinedTermBase kindOf = term.getKindOf();
        TermVocabulary vocabulary = term.getVocabulary();
        TermDto dto = new TermDto(
                        term.getUuid(),
                        representations!=null?representations:term.getRepresentations(),
                        term.getTermType(),
                        (partOf!=null?partOf.getUuid():null),
                        (kindOf!=null?kindOf.getUuid():null),
                        vocabulary.getUuid(),
                        (term instanceof OrderedTermBase)?((OrderedTermBase) term).getOrderIndex():null,
                         term.getIdInVocabulary());
        dto.setUri(term.getUri());
        if(initializeToTop){
            if(partOf!=null){
                dto.setPartOfDto(fromTerm(partOf, initializeToTop));
            }
            if(kindOf!=null){
                dto.setKindOfDto(fromTerm(kindOf, initializeToTop));
            }
            dto.setVocabularyDto(new TermVocabularyDto(vocabulary.getUuid(), vocabulary.getRepresentations(), term.getTermType()));
        }
        return dto;
    }

    @Override
    public void localize(ITermRepresentation_L10n representation_L10n) {
        if(vocRepresentations!=null){
            representation_L10n.localize(vocRepresentations);
            if (representation_L10n.getLabel() != null) {
                setVocRepresentation_L10n(representation_L10n.getLabel());
            }
            if (representation_L10n.getAbbreviatedLabel() != null) {
                setVocRepresentation_L10n_abbreviatedLabel(representation_L10n.getAbbreviatedLabel());
            }
        }
        super.localize(representation_L10n);
    }

    public void setVocRepresentation_L10n(String vocRepresentation_L10n) {
        this.vocRepresentation_L10n = vocRepresentation_L10n;
    }

    public String getVocRepresentation_L10n() {
        return vocRepresentation_L10n;
    }

    public void setVocRepresentation_L10n_abbreviatedLabel(String vocRepresentation_L10n_abbreviatedLabel) {
        this.vocRepresentation_L10n_abbreviatedLabel = vocRepresentation_L10n_abbreviatedLabel;
    }

    public String getVocRepresentation_L10n_abbreviatedLabel() {
        return vocRepresentation_L10n_abbreviatedLabel;
    }

    public void setPartOfDto(TermDto partOfDto) {
        this.partOfDto = partOfDto;
    }

    public TermDto getPartOfDto() {
        return partOfDto;
    }

    public void setKindOfDto(TermDto kindOfDto) {
        this.kindOfDto = kindOfDto;
    }

    public TermDto getKindOfDto() {
        return kindOfDto;
    }

    public void setVocabularyDto(TermVocabularyDto vocabularyDto) {
        this.vocabularyDto = vocabularyDto;
    }

    public TermVocabularyDto getVocabularyDto() {
        return vocabularyDto;
    }

    public UUID getVocabularyUuid() {
        return vocabularyUuid;
    }

    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
    }

    public UUID getPartOfUuid() {
        return partOfUuid;
    }

    public UUID getKindOfUuid() {
        return kindOfUuid;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getIdInVocabulary() {
        return idInVocabulary;
    }

    public void setIdInVocabulary(String idInVocabulary) {
        this.idInVocabulary = idInVocabulary;
    }

    public Collection<TermDto> getIncludes() {
        return includes;
    }

    public void setIncludes(Collection<TermDto> includes) {
        this.includes = includes;
    }

    public Collection<TermDto> getGeneralizationOf() {
        return generalizationOf;
    }

    public void setGeneralizationOf(Collection<TermDto> generalizationOf) {
        this.generalizationOf = generalizationOf;
    }

    public static String getTermDtoSelect(){
        return getTermDtoSelect("DefinedTermBase");
    }

    public static String getTermDtoSelect(String fromTable){
        return ""
                + "select a.uuid, "
                + "r, "
                + "p.uuid, "
                + "k.uuid, "
                + "v.uuid, "
                + "a.orderIndex, "
                + "a.idInVocabulary, "
                + "voc_rep,  "
                + "a.termType,  "
                + "a.uri  "
                + "from "+fromTable+" as a "
                + "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "
                + "LEFT JOIN v.representations as voc_rep ";
    }

    public static List<TermDto> termDtoListFrom(List<Object[]> results) {
        List<TermDto> dtos = new ArrayList<>(); // list to ensure order
        Map<UUID, TermDto> dtoMap = new HashMap<>(results.size()); // map to handle multiple representations
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
            } else {
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                } else {
                    representations = (Set<Representation>)elements[1];
                }
                Set<Representation> vocRepresentations = new HashSet<>();
                if(elements[7] instanceof Representation) {
                    vocRepresentations = new HashSet<Representation>(7);
                    vocRepresentations.add((Representation)elements[7]);
                } else {
                    vocRepresentations = (Set<Representation>)elements[7];
                }
                TermDto termDto = new TermDto(
                        uuid,
                        representations,
                        (TermType)elements[8],
                        (UUID)elements[2],
                        (UUID)elements[3],
                        (UUID)elements[4],
                        (Integer)elements[5],
                        (String)elements[6],
                        vocRepresentations);
                termDto.setUri((URI)elements[9]);

                dtoMap.put(uuid, termDto);
                dtos.add(termDto);
            }
        }
        return dtos;
    }

}
