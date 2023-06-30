/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.persistence.dto.FeatureDto;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author k.luther
 * @since Aug 18, 2021
 */
public class CategoricalDataDto extends DescriptionElementDto {

    private static final long serialVersionUID = -675109569492621389L;
    private List<StateDataDto> states = new ArrayList<>();

    public CategoricalDataDto (UUID elementUuid, FeatureDto feature, List<StateDataDto> states){
       super(elementUuid, feature);
       if (states != null){
           this.states = states;
       }
    }

    public CategoricalDataDto(FeatureDto feature){
        super(feature);

    }

    public static CategoricalDataDto fromCategoricalData(CategoricalData data){
        List<StateDataDto> stateDtos = new ArrayList<>();
        StateDataDto stateDataDto;
        Set<StateData> toRemove = new HashSet<>();
        for (StateData stateData: data.getStateData()){
            if (stateData.getState() == null){
                toRemove.add(stateData);
                continue;
            }
            Integer count = stateData.getCount();
            UUID uuid = stateData.getUuid();
            TermDto termDto = TermDto.fromTerm(CdmBase.deproxy(stateData.getState(), DefinedTermBase.class));
            List<TermDto> modifiers = new ArrayList<>();
            for (DefinedTermBase modifier: stateData.getModifiers()) {
                modifiers.add(TermDto.fromTerm(modifier));
            }
            String modifyingText = stateData.getModifyingText().get(Language.getDefaultLanguage()).getText();
            stateDataDto = new StateDataDto(termDto, count, modifiers, modifyingText, uuid);
            stateDtos.add(stateDataDto);
        }
        for (StateData stateData: toRemove){
            data.removeStateData(stateData);
        }
        CategoricalDataDto dto = new CategoricalDataDto(data.getUuid(), FeatureDto.fromFeature(data.getFeature()), stateDtos);

        return dto;
    }


    public List<StateDataDto> getStates() {
        return states;
    }

    public void addState(StateDataDto state) {
        this.states.add(state);
    }

    public List<StateDataDto> setStateDataOnly(List<TermDto> states){
        removeStateData();

        for (TermDto state : states) {
            addStateData(state);
        }
        return this.states;
    }

    /**
     * @param state
     */
     public StateDataDto addStateData(State state){
        StateDataDto stateData = new StateDataDto(state);
        addStateData(stateData);
        return stateData;
    }

     public StateDataDto addStateData(TermDto state){
         StateDataDto stateData = new StateDataDto(state);
         addStateData(stateData);
         return stateData;
     }

     public void addStateData(StateDataDto stateData){
         this.states.add(stateData);

     }

    /**
     * @param stateData
     */
    private void removeStateData() {
        states.clear();
    }

    public static String getCategoricalDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2] + result[3];
    }

    private static String[] createSqlParts() {
        //featureDto, uuid, states

        String sqlSelectString = ""
                + "select a.uuid, "//0
                + "feature.uuid, "//1
                + "stateData.uuid,  "//2
                + "stateData.count, "//3
                + "stateData.state, "//4
                + "modifier.uuid, "//5
                + "modifier.titleCache, "//6
                + "modifier.orderIndex ";//7


        String sqlFromString =   " FROM CategoricalData as a ";

        String sqlJoinString =  "LEFT JOIN a.stateData as stateData "
                + "LEFT JOIN a.feature as feature "
                + "LEFT JOIN stateData.modifiers as modifier";

        String sqlWhereString =  " WHERE a.inDescription.uuid = :uuid";


        String[] result = new String[4];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        result[3] = sqlWhereString;
        return result;
    }

    /**
     * @param result
     * @return
     */
    public static List<CategoricalDataDto> categoricalDataDtoListFrom(List<Object[]> result) {
        List<CategoricalDataDto> dtoResult = new ArrayList<>();
        CategoricalDataDto dto = null;
        StateDataDto state = null;
        for (Object[] o: result){
            UUID uuid = (UUID)o[0];
            UUID featureUuid = (UUID)o[1];
            UUID stateDataUuid = (UUID)o[2];

            if (dto == null || !dto.getElementUuid().equals(uuid)){
                dto = new CategoricalDataDto(uuid, new FeatureDto(featureUuid, null, null, null, null, null, null, true, false, true, null, true, false, null, null, null, null), null);
                dtoResult.add(dto);
            }
            if (state == null || !state.getUuid().equals(stateDataUuid)) {
                state = new StateDataDto(TermDto.fromTerm((DefinedTermBase)o[4]),(Integer) o[3], null, null, stateDataUuid);
            }
            if (o[5] != null) {
                TermDto modifier = new TermDto((UUID)o[5], null, null, null, null, null, (Integer)o[7], null, (String)o[6]);
                state.addModifier(modifier);
            }
            dto.addState(state);
        }


        return dtoResult;

    }


}
