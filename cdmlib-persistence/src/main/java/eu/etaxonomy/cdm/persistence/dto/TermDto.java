/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.AuthorityType;
import eu.etaxonomy.cdm.model.common.ExternallyManaged;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author andreas
 * @since Mar 25, 2015
 */
public class TermDto extends AbstractTermDto{

    private static final long serialVersionUID = 5627308906985438034L;

    private UUID kindOfUuid = null;
    private UUID partOfUuid = null;
    private UUID vocabularyUuid = null;
    private TermDto kindOfDto = null;
    private TermDto partOfDto = null;
    private TermCollectionDto vocabularyDto = null;
    private Integer orderIndex = null;
    private String idInVocabulary = null;
    private Collection<TermDto> includes;
    private Collection<TermDto> generalizationOf;
    private Collection<UUID> media = null;
    private NamedAreaLevel level = null;
    private String symbol = null;
    private String symbol2 = null;

    public TermDto(UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary,  String titleCache, String symbol, String symbol2) {
        super(uuid, representations, titleCache);
        this.partOfUuid = partOfUuid;
        this.kindOfUuid = kindOfUuid;
        this.vocabularyUuid = vocabularyUuid;
        this.orderIndex = orderIndex;
        this.idInVocabulary = idInVocabulary;
        this.symbol = symbol;
        this.symbol2 = symbol2;
        setTermType(termType);
    }
    
    public TermDto(UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary,  String titleCache) {
        this(uuid, representations, termType, partOfUuid, kindOfUuid, vocabularyUuid, orderIndex, idInVocabulary, titleCache, null, null);
        
    }

    static public TermDto fromTerm(DefinedTermBase<?> term) {
        return fromTerm(term, null, false);
    }

    static public TermDto fromTerm(DefinedTermBase<?> term, boolean initializeToTop) {
        return fromTerm(term, null, initializeToTop);
    }

    static public TermDto fromTerm(DefinedTermBase<?> term, Set<Representation> representations) {
        return fromTerm(term, representations, false);
    }

    static public TermDto fromTerm(DefinedTermBase<?> term, Set<Representation> representations, boolean initializeToTop) {
        if (term == null){
            return null;
        }
        DefinedTermBase<?> partOf = term.getPartOf();
        DefinedTermBase<?> kindOf = term.getKindOf();
        TermVocabulary<?> vocabulary = term.getVocabulary();
        if (representations == null){
            term = HibernateProxyHelper.deproxy(term);
            representations = term.getRepresentations();
        }

        TermDto dto = new TermDto(
                        term.getUuid(),
                        representations!=null?representations:term.getRepresentations(),
                        term.getTermType(),
                        (partOf!=null?partOf.getUuid():null),
                        (kindOf!=null?kindOf.getUuid():null),
                        (vocabulary!=null?vocabulary.getUuid():null),
                        term.getOrderIndex(),
                        term.getIdInVocabulary(),
                        term.getTitleCache(),
                        term.getSymbol(),
                        term.getSymbol2());
        dto.setUri(term.getUri());
        if(initializeToTop){
            if(partOf!=null){
                dto.setPartOfDto(fromTerm(partOf, initializeToTop));
            }
            if(kindOf!=null){
                dto.setKindOfDto(fromTerm(kindOf, initializeToTop));
            }
        }
        if (vocabulary != null){
            dto.setVocabularyDto(new TermVocabularyDto(vocabulary.getUuid(), vocabulary.getRepresentations(), term.getTermType(), vocabulary.getTitleCache(), vocabulary.isAllowDuplicates(), vocabulary.isOrderRelevant(), vocabulary.isFlat()));
        }
        if(term.getMedia()!=null){
            Collection<UUID> mediaUuids = new HashSet<>();
            Set<Media> media = term.getMedia();

                for (Media medium : media) {
                    mediaUuids.add(medium.getUuid());
                }
                dto.setMedia(mediaUuids);

        }
        if (term instanceof NamedArea && ((NamedArea)term).getLevel() != null){
            dto.setLevel(((NamedArea)term).getLevel());
        }
        dto.setManaged(term.isManaged());
        return dto;
    }

    @Override
    public void localize(ITermRepresentation_L10n representation_L10n) {
        if(this.vocabularyDto != null && this.vocabularyDto.getRepresentations()!=null){
            this.vocabularyDto.localize(representation_L10n);
        }
        super.localize(representation_L10n);
    }


    public String getVocRepresentation_L10n() {
        return vocabularyDto == null ? null : vocabularyDto.getRepresentation_L10n();
    }

    public String getVocRepresentation_L10n_abbreviatedLabel() {
        return vocabularyDto == null ? null : vocabularyDto.getRepresentation_L10n_abbreviatedLabel();
    }

    public void setPartOfDto(TermDto partOfDto) {
        this.partOfDto = partOfDto;
    }

    public TermDto getPartOfDto() {
        return partOfDto;
    }

    /**
     * @return the kindOfDto
     */
    public TermDto getKindOfDto() {
        return kindOfDto;
    }

    /**
     * @param kindOfDto the kindOfDto to set
     */
    public void setKindOfDto(TermDto kindOfDto) {
        this.kindOfDto = kindOfDto;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public String getSymbol2() {
        return symbol2;
    }

    public void setSymbol2(String symbol) {
        this.symbol2 = symbol;
    }

    public void setVocabularyDto(TermCollectionDto vocabularyDto) {
        this.vocabularyDto = vocabularyDto;
    }

    public TermCollectionDto getVocabularyDto() {
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

    public Collection<UUID> getMedia() {
        return media;
    }

    public void setMedia(Collection<UUID> media) {
        this.media = media;
    }

    protected void addMedia(UUID mediaUuid){
        this.media.add(mediaUuid);
    }

    public void setLevel(NamedAreaLevel level) {
        this.level = level;
    }

    public NamedAreaLevel getLevel(){
        return level;
    }

    public static String getTermDtoSelect(String fromTable){
        String[] result = createSqlParts(fromTable);

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlParts(String fromTable) {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "r, "
                + "p.uuid, "
                + "k.uuid, "
                + "v.uuid, "
                + "a.orderIndex, "
                + "a.idInVocabulary, "
                + "a.termType,  "
                + "a.uri,  "
                + "m,  "
                + "a.titleCache, "
                + "a.externallyManaged, "
                + "a.symbol, "
                + "a.symbol2 ";

        String sqlFromString =   " FROM "+fromTable+" as a ";

        String sqlJoinString =  "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.media as m "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "

                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }

    public static String getTermDtoSelectNamedArea(){
        String[] result = createSqlParts("NamedArea");
        return result[0]+  ", level  " + result[1] + result[2]+ "LEFT JOIN a.level as level ";
    }

    public static List<TermDto> termDtoListFrom(List<Object[]> results){
        return termDtoListFrom(results, null);
    }

    public static List<TermDto> termDtoListFrom(List<Object[]> results, Language lang) {
        List<TermDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations/media/vocRepresentation because of LEFT JOIN
        Map<UUID, TermDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            UUID uuid = (UUID)elements[0];
            if(dtoMap.containsKey(uuid)){
                // multiple results for one term -> multiple (voc) representation/media
                if(elements[1]!=null){
                    dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
                }

                if(elements[9]!=null){
                    dtoMap.get(uuid).addMedia(((Media) elements[9]).getUuid());
                }
            } else {
                // term representation
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<>(1);
                    representations.add((Representation)elements[1]);
                }
                // term media
                Set<UUID> mediaUuids = new HashSet<>();
                if(elements[9] instanceof Media) {
                    mediaUuids.add(((Media) elements[9]).getUuid());
                }

                TermDto termDto = new TermDto(
                        uuid,
                        representations,
                        (TermType)elements[7],
                        (UUID)elements[2],
                        (UUID)elements[3],
                        (UUID)elements[4],
                        (Integer)elements[5],
                        (String)elements[6],
                        (String)elements[10],
                        (String)elements[12],
                        (String)elements[13]);
                termDto.setUri((URI)elements[8]);
                termDto.setMedia(mediaUuids);
                if (elements.length>14 && elements[14] != null){
                    termDto.setLevel((NamedAreaLevel)elements[14]);
                }
                if (elements[11] != null) {
                    ExternallyManaged extManaged = (ExternallyManaged)elements[11];
                    termDto.setManaged(extManaged!= null && extManaged.getAuthorityType()!= null? extManaged.getAuthorityType().equals(AuthorityType.EXTERN):false);
                }
                if (lang != null) {
                    termDto.setLabel(lang);
                }
                dtoMap.put(uuid, termDto);
                dtos.add(termDto);
            }
        }
        return dtos;
    }

    private void setLabel(Language lang) {
        if (lang == null) {
            return;
        }
        if (representations != null && representations.iterator().hasNext()) {
            for (Representation rep: representations) {
                if (rep.getLanguage().equals(lang)) {
                    this.setLabel(rep.getLabel());
                    break;
                }
            }
            if (this.getLabel() == null) {
                this.setLabel(representations.iterator().next().getLabel());
            }
        }

    }
}