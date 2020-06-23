/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmClass;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Jun 11, 2020
 */
public class FeatureDto extends TermDto {

    private static final long serialVersionUID = 5123011385890020838L;

    boolean isAvailableForTaxon = true;
    boolean isAvailableForTaxonName = true;
    boolean isAvailableForOccurrence = true;

    public FeatureDto(UUID uuid, Set<Representation> representations, UUID partOfUuid, UUID kindOfUuid,
            UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, Set<Representation> vocRepresentations, boolean isAvailableForTaxon, boolean isAvailableForTaxonName, boolean isAvailableForOccurrence){
        super(uuid, representations, TermType.Feature, partOfUuid, kindOfUuid,
                vocabularyUuid, orderIndex, idInVocabulary, vocRepresentations);
        this.isAvailableForOccurrence = isAvailableForOccurrence;
        this.isAvailableForTaxon = isAvailableForTaxon;
        this.isAvailableForTaxonName = isAvailableForTaxonName;

    }

    static public FeatureDto fromTerm(Feature term) {
        FeatureDto result = (FeatureDto) fromTerm(term, null, false);
        result.isAvailableForOccurrence = term.isAvailableForOccurrence();
        result.isAvailableForTaxon = term.isAvailableForTaxon();
        result.isAvailableForTaxonName = term.isAvailableForTaxonName();
        return result;
    }

    /**
     * @return the isAvailableForTaxon
     */
    public boolean isAvailableForTaxon() {
        return isAvailableForTaxon;
    }

    /**
     * @param isAvailableForTaxon the isAvailableForTaxon to set
     */
    public void setAvailableForTaxon(boolean isAvailableForTaxon) {
        this.isAvailableForTaxon = isAvailableForTaxon;
    }

    /**
     * @return the isAvailableForTaxonName
     */
    public boolean isAvailableForTaxonName() {
        return isAvailableForTaxonName;
    }

    /**
     * @param isAvailableForTaxonName the isAvailableForTaxonName to set
     */
    public void setAvailableForTaxonName(boolean isAvailableForTaxonName) {
        this.isAvailableForTaxonName = isAvailableForTaxonName;
    }

    /**
     * @return the isAvailableForOccurrence
     */
    public boolean isAvailableForOccurrence() {
        return isAvailableForOccurrence;
    }

    /**
     * @param isAvailableForOccurrence the isAvailableForOccurrence to set
     */
    public void setAvailableForOccurrence(boolean isAvailableForOccurrence) {
        this.isAvailableForOccurrence = isAvailableForOccurrence;
    }

    public static String getTermDtoSelect(){
        String[] result = createSqlParts("DefinedTermBase");

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
                + "voc_rep,  "
                + "a.termType,  "
                + "a.uri,  "
                + "m,  "
                + "a.availableFor ";

        String sqlFromString =   "from "+fromTable+" as a ";

        String sqlJoinString =  "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.media as m "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "
                + "LEFT JOIN v.representations as voc_rep "
                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }


    public static List<TermDto> termDtoListFrom(List<Object[]> results) {
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
                if(elements[7]!=null){
                    dtoMap.get(uuid).addVocRepresentation((Representation)elements[7]);
                }
                if(elements[10]!=null){
                    dtoMap.get(uuid).addMedia(((Media) elements[10]).getUuid());
                }
            } else {
                // term representation
                Set<Representation> representations = new HashSet<>();
                if(elements[1] instanceof Representation) {
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                }
                // term media
                Set<UUID> mediaUuids = new HashSet<>();
                if(elements[10] instanceof Media) {
                    mediaUuids.add(((Media) elements[10]).getUuid());
                }
                // voc representation
                Set<Representation> vocRepresentations = new HashSet<>();
                if(elements[7] instanceof Representation) {
                    vocRepresentations = new HashSet<Representation>(7);
                    vocRepresentations.add((Representation)elements[7]);
                }
                boolean isAvailableForTaxon = false;
                boolean isAvailableForTaxonName = false;
                boolean isAvailableForOccurrence = false;

                EnumSet<CdmClass> availableForString = (EnumSet<CdmClass>)elements[11];

                    if (availableForString.contains(CdmClass.TAXON)){
                        isAvailableForTaxon = true;
                    }
                    if (availableForString.contains(CdmClass.TAXON_NAME)){
                        isAvailableForTaxonName = true;
                    }
                    if (availableForString.contains(CdmClass.OCCURRENCE)){
                        isAvailableForOccurrence = true;
                    }

                TermDto termDto = new FeatureDto(
                        uuid,
                        representations,
                        (UUID)elements[2],
                        (UUID)elements[3],
                        (UUID)elements[4],
                        (Integer)elements[5],
                        (String)elements[6],
                        vocRepresentations,
                        isAvailableForTaxon,
                        isAvailableForTaxonName,
                        isAvailableForOccurrence)
                        ;
                termDto.setUri((URI)elements[9]);
                termDto.setMedia(mediaUuids);


                dtoMap.put(uuid, termDto);
                dtos.add(termDto);
            }
        }
        return dtos;
    }


}
