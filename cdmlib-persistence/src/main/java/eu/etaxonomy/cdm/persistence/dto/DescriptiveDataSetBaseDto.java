/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.DescriptiveDataSet;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * @author k.luther
 * @since Aug 31, 2021
 */
public class DescriptiveDataSetBaseDto implements Serializable{

    private static final long serialVersionUID = 7310069387512600745L;

    private UUID uuid;
    private Integer id;
    private String titleCache;
    private TermTreeDto descriptiveSystem;
    private Set<TaxonNodeDto> subTreeFilter;
    private Set<TermDto> geoFilter;

    private Set<Representation> representations = new HashSet<>();
    private String representation_L10n = null;
    private String representation_L10n_abbreviatedLabel = null;
    private String representation_L10n_text = null;

    private TermDto maxRank;
    private TermDto minRank;



    public DescriptiveDataSetBaseDto(UUID uuid, Integer id, String titleCache){
        this.uuid = uuid;
        this.id = id;
        this.titleCache = titleCache;
    }

    public UUID getUuid() {
        return uuid;
    }


    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitleCache() {
        return titleCache;
    }

    public void setTitleCache(String titleCache) {
        this.titleCache = titleCache;
    }

    public TermTreeDto getDescriptiveSystem() {
        return descriptiveSystem;
    }

    public void setDescriptiveSystem(TermTreeDto descriptiveSystem) {
        this.descriptiveSystem = descriptiveSystem;
    }


    private void addRepresentation(Representation representation) {
        if (this.representations == null){
            this.representations = new HashSet<>();
        }
        this.representations.add(representation);

    }
    public TermDto getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(TermDto minRank) {
        this.maxRank = minRank;
    }

    public TermDto getMinRank() {
        return minRank;
    }

    public void setMinRank(TermDto minRank) {
        this.minRank = minRank;
    }


    /** sql queries to get descriptive data set dto*/

    public static String getDescriptiveDataSetDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlParts() {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "a.id, "
                + "a.titleCache, "
                + "r, "
                + "d.uuid, "
                + "minRank.uuid, "
//                + "minRank.representations, "
//                + "minRank.termType, "
//                + "minRank.orderIndex, "
//                + "minRank.idInVocabulary, "
//                + "minRank.titleCache, "
                + "maxRank.uuid ";
//                + "maxRank.representations, "
//                + "maxRank.termType, "
//                + "maxRank.orderIndex, "
//                + "maxRank.idInVocabulary, "
//                + "maxRank.titleCache ";
        //UUID uuid, Set<Representation> representations, TermType termType, UUID partOfUuid, UUID kindOfUuid,
       // UUID vocabularyUuid, Integer orderIndex, String idInVocabulary, String titleCache
        String sqlFromString =   " FROM DescriptiveDataSet as a ";

        String sqlJoinString =  " LEFT JOIN a.descriptiveSystem as d "
                + " LEFT JOIN a.representations AS r "
                + " LEFT JOIN a.minRank as minRank "
                + " LEFT JOIN a.maxRank as maxRank ";

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }

    /**
     * @param result
     * @return
     */
    public static List<DescriptiveDataSetBaseDto> descriptiveDataSetBaseDtoListFrom(List<Object[]> results) {

            List<DescriptiveDataSetBaseDto> dtos = new ArrayList<>(); // list to ensure order
            // map to handle multiple representations/media/vocRepresentation because of LEFT JOIN
            Map<UUID, DescriptiveDataSetBaseDto> dtoMap = new HashMap<>(results.size());
            for (Object[] elements : results) {
                UUID uuid = (UUID)elements[0];
                Integer id = (Integer)elements[1];

                if(dtoMap.containsKey(uuid)){
                    // multiple results for one dataset -> multiple (voc) representation/media
                    if(elements[1]!=null){
                        dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
                    }
                } else {
                    // dataset representation
                    Set<Representation> representations = new HashSet<>();
                    if(elements[1] instanceof Representation) {
                        representations = new HashSet<>(1);
                        representations.add((Representation)elements[1]);
                    }

                    DescriptiveDataSetBaseDto datasetDto = new DescriptiveDataSetBaseDto(
                            uuid,
                            id,
                            (String)elements[2]);

//                    TermDto minRank = new TermDto((UUID)elements[5], (Set<Representation>)elements[6], (TermType)elements[7], null, null, null, (Integer)elements[8], (String)elements[9], (String)elements[10]);
//                    TermDto maxRank = new TermDto((UUID)elements[11], (Set<Representation>)elements[12], (TermType)elements[13], null, null, null, (Integer)elements[14], (String)elements[15], (String)elements[16]);
//                    datasetDto.setMinRank(minRank);
//                    datasetDto.setMaxRank(maxRank);

                    dtoMap.put(uuid, datasetDto);
                    dtos.add(datasetDto);
                }
            }
            return dtos;
        }

    public static DescriptiveDataSetBaseDto fromDescriptiveDataSet(DescriptiveDataSet dataSet){
        DescriptiveDataSetBaseDto dto = new DescriptiveDataSetBaseDto(dataSet.getUuid(), dataSet.getId(), dataSet.getTitleCache());
        if (dataSet.getDescriptiveSystem() != null){
            dto.setDescriptiveSystem(TermTreeDto.fromTree(dataSet.getDescriptiveSystem()));
        }
        return dto;
    }

    public Set<TaxonNodeDto> getSubTreeFilter() {
        return subTreeFilter;
    }

    public void setSubTreeFilter(Set<TaxonNodeDto> subTreeFilter) {
        this.subTreeFilter = subTreeFilter;
    }

    public Set<TermDto> getGeoFilter() {
        return geoFilter;
    }

    public void setGeoFilter(Set<TermDto> geoFilter) {
        this.geoFilter = geoFilter;
    }


}
