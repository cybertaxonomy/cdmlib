/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author k.luther
 * @since Aug 19, 2021
 */
public class StateDataDto implements Serializable {

    private TermDto state;
    private Integer count;
    private UUID uuid;

    public StateDataDto(TermDto state, Integer count, UUID uuid){
        this.state = state;
        this.count = count;
        setUuid(uuid);
    }

    public StateDataDto(State state){
        this.state = TermDto.fromTerm(state);
        //new StateData
        setUuid(null);
    }



    public StateDataDto(TermDto state){
        this.state = state;
        //new StateData
        setUuid(null);
    }

    public Integer getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public TermDto getState() {
        return state;
    }
    public void setState(TermDto state) {
        this.state = state;
    }
    public UUID getUuid() {
        return uuid;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public static String getStateDataDtoSelect(String fromTable){
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
//                + "voc_rep,  "
                + "a.termType,  "
                + "a.uri,  "
                + "m,  "
                + "a.titleCache ";
        String sqlFromString =   " FROM "+fromTable+" as a ";

        String sqlJoinString =  "LEFT JOIN a.partOf as p "
                + "LEFT JOIN a.kindOf as k "
                + "LEFT JOIN a.media as m "
                + "LEFT JOIN a.representations AS r "
                + "LEFT JOIN a.vocabulary as v "
//                + "LEFT JOIN v.representations as voc_rep "
                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }

}
