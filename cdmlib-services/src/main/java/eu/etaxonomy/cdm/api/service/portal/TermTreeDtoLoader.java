/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.dto.portal.NamedAreaDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermNodeDto;
import eu.etaxonomy.cdm.api.dto.portal.tmp.TermTreeDto;
import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermTree;

/**
 * @author muellera
 * @since 29.02.2024
 */
public class TermTreeDtoLoader {

    private static final Logger logger = LogManager.getLogger();

    public static TermTreeDtoLoader INSTANCE(){
        return new TermTreeDtoLoader();
    }

    public <T extends DefinedTermBase<T>> TermTreeDto fromEntity(TermTree<T> entity) {
        if (entity == null) {
            return null;
        }
        entity = CdmBase.deproxy(entity);
        //TODO i18n
        TermTreeDto dto = new TermTreeDto(entity.getUuid(), entity.getId(), entity.getLabel());
        load(dto, entity);
        return dto;
    }

    private <T extends DefinedTermBase<T>> void load(TermTreeDto dto, TermTree<T> entity) {
        dto.setRoot(TermNodeDtoLoader.INSTANCE().fromEntity(entity.getRoot(), null));
    }

    //******************** static method for computation on term trees **************/
    //NOTE: we handle them here to avoid cluttring the DTO, but maybe there is a better place

    //TODO maybe not needed anymore since TermDtos do have parent field now.
    //     Use #getTerm2NodeMap instead
    public static <T extends TermDto> SetMap<T,T> getTerm2ParentMap(TermTreeDto termTree, Class<T> clazz) {
        if (termTree == null) {
            return null;
        }
        SetMap<T,T> result = new SetMap<>();
        fillTerm2ParentTermMap(termTree.getRoot(), result, null, clazz);
        return result;
    }

    private static <T extends TermDto> void fillTerm2ParentTermMap(TermNodeDto nodeDto, SetMap<T,T> map,
            TermNodeDto parent, Class<T> clazz) {  //as long as node has no parent attribute

        if (nodeDto.getTerm() != null) {
            T parentTerm = parent == null? null : (T)parent.getTerm();
            if (parentTerm != null) {
                map.putItem((T)nodeDto.getTerm(), parentTerm);
            }
        }
        if (nodeDto.getChildren() != null) {
            for (TermNodeDto child: nodeDto.getChildren()){
                fillTerm2ParentTermMap(child, map, nodeDto, clazz);
            }
        }
        return;
    }

    public static <T extends TermDto> SetMap<T,TermNodeDto> getTerm2NodeMap(TermTreeDto termTree, Class<T> clazz) {
        if (termTree == null) {
            return null;
        }
        SetMap<T,TermNodeDto> result = new SetMap<>();
        fillTerm2NodeMap(termTree.getRoot(), result, clazz);
        return result;
    }

    private static <T extends TermDto> void fillTerm2NodeMap(TermNodeDto nodeDto, SetMap<T,TermNodeDto> map, Class<T> clazz) {
        if (nodeDto.getTerm() != null) {
            map.putItem((T)nodeDto.getTerm(), nodeDto);
        }
        if (nodeDto.getChildren() != null) {
            for (TermNodeDto child: nodeDto.getChildren()){
                fillTerm2NodeMap(child, map, clazz);
            }
        }
        return;
    }

    public static <T extends TermDto> List<T> toList(TermTreeDto termTree, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        toListRecursive(termTree.getRoot(), list, clazz);
        return list;
    }

    private static <T extends TermDto> void toListRecursive(TermNodeDto node, List<T> list, Class<T> clazz) {
        if (node.getTerm() != null) {
            list.add((T)node.getTerm());
        }
        if (node.getChildren() != null) {
            for (TermNodeDto child : node.getChildren()){
                toListRecursive(child, list, clazz);
            }
        }
    }

    public static <T extends DefinedTermBase<T>> TermTreeDto loadVolatileFromVocabulary(ICommonService service,
            Set<UUID> vocabularyUuids) {

        //TODO i18n
        //TODO since adding he marker type stuff, maybe an N+1 issue?, need to check
        String hql = " SELECT new map (t.uuid as termUuid, t.id as termId, t.titleCache as termLabel, "
                +    "    l.uuid as levelUuid, "
                +    "    t.partOf.id as parentId, m.markerType.uuid as markerUuid) "
                   + " FROM DefinedTermBase t LEFT OUTER JOIN t.level l LEFT OUTER JOIN t.markers m with m.flag = true "
                   + "      LEFT OUTER JOIN m.markerType mt "
                   + " WHERE t.vocabulary.uuid in ?1 "
                   + " ORDER BY t.id "
                   ;

        //This is interesting as it works for known marker types, but as we also have alternativeRootAreaMarkerTypes we can't use it here (or it becomes even more complicated)
//        String hql = " SELECT new map (t.uuid as termUuid, t.id as termId, t.titleCache as termLabel, "
//                   +    "    t.partOf.id as parentId, ("
//                   +    "             SELECT count(m) "
//                   +    "             FROM DefinedTermBase t2 INNER JOIN t2.markers m "
//                   +    "                 INNER JOIN m.markerType mt "
//                   +    "             WHERE t.id = t2.id AND mt.uuid in ?2 ) as markerCount )"
//                   + " FROM DefinedTermBase t  "
//                   + " WHERE t.vocabulary.uuid in ?1"
//                   ;

        Object[] params = new Object[] {vocabularyUuids};

//        @SuppressWarnings({ "unchecked", "rawtypes" })
        List<Map<String, Object>> queryResult = null;
        try {
            queryResult = (List)service.getHqlResult(hql, params, Map.class);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Error in hql while retrieving termtree");
        }

        TermTreeDto result = new TermTreeDto(null, null, "volatile area tree", true);

        Map<Integer,TermNodeDto> termId2NodeMap = new HashMap<>();
        Integer lastTermId = null;
        NamedAreaDto lastAreaDto = null;
        //create term nodes
        for (Map<String,Object> e : queryResult  ){
            Integer termId = (Integer)e.get("termId");
            UUID markerUuid = (UUID)e.get("markerUuid");
            if (lastAreaDto != null && termId.equals(lastTermId)) {
                //"workaround" for handling multiple markers
                lastAreaDto.addMarker(markerUuid);
            }else {
                UUID termUuid = (UUID)e.get("termUuid");
                String termLabel = (String)e.get("termLabel");
                NamedAreaDto areaDto = new NamedAreaDto(termUuid, termId, termLabel);
                TermNodeDto nodeDto = new TermNodeDto(UUID.randomUUID(), null, termLabel, null);
                //TODO or should we directly add the marker to the node, instead of adding it to the area?
                areaDto.addMarker(markerUuid);
                areaDto.setLevelUuid((UUID)e.get("levelUuid"));
                nodeDto.setTerm(areaDto);
                termId2NodeMap.put(termId, nodeDto);
                lastTermId = termId;
                lastAreaDto = areaDto;
            }
        }

        //add parents
        queryResult.stream().forEach(e->{
            Integer parentId = (Integer)e.get("parentId");
            Integer termId = (Integer)e.get("termId");
            TermNodeDto childNode = termId2NodeMap.get(termId);
            if (parentId == null) {
                result.getRoot().addChild(childNode);
            } else {
                TermNodeDto parentNode = termId2NodeMap.get(parentId);
                if (parentNode == null) {
                    logger.warn("Can't find parent in vocabulary. Data seems to be inconsistent");
                }else {
                    parentNode.addChild(childNode);
                }
            }
        });

        return result;
    }
}