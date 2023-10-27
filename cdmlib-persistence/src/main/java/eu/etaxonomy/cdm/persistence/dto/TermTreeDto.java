/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.util.ArrayList;
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
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since 07.01.2020
 */
public class TermTreeDto extends TermCollectionDto {
    private static final long serialVersionUID = -7223363599985320531L;

    private TermNodeDto root;
    private Map<UUID, Set<FeatureStateDto>> inapplicableMap = new HashMap<>(); //a map <uuid of the parent feature, uuid of child feature, state> shows for a parent feature which features are inapplicable for specific state
    private Map<UUID, Set<FeatureStateDto>> onlyApplicableMap = new HashMap<>();


    public static TermTreeDto fromTree(TermTree tree){
        TermTreeDto dto = new TermTreeDto(tree.getUuid(), tree.getRepresentations(), tree.getTermType(), tree.getRoot(), tree.getTitleCache(), tree.isAllowDuplicates(), tree.isOrderRelevant(), tree.isFlat() );
        return dto;
    }

    public TermTreeDto(UUID uuid, Set<Representation> representations, TermType termType, String titleCache, boolean isAllowDuplicates, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, termType, titleCache, isAllowDuplicates, isOrderRelevant, isFlat);
    }

    public TermTreeDto(UUID uuid, Set<Representation> representations, TermType termType, TermNode root, String titleCache, boolean isAllowDuplicates, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, termType, titleCache, isAllowDuplicates, isOrderRelevant, isFlat);
        root = HibernateProxyHelper.deproxy(root);
        this.root = TermNodeDto.fromNode(root, this);
    }

    public TermNodeDto getRoot() {
       return root;
    }

    public void setRoot(TermNodeDto root) {
        this.root = root;
    }

    public boolean removeChild(TermNodeDto nodeDto){
        return this.root.removeChild(nodeDto, true);
    }

    public static String getTermTreeDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2];
    }

    public static String getTermTreeDtoSelectForDescriptiveDataSet(){
        String[] result = createSqlPartsForDescriptiveDataSet();

        return result[0]+result[1]+result[2];
    }

    private static String[] createSqlParts() {
        String sqlSelectString = ""
                + "select "
                + "a.uuid, "
                + "r, "
                + "a.termType,  "
                + "a.uri,  "
                + "a.titleCache, "
                + "a.allowDuplicates, "
                + "a.orderRelevant, "
                + "a.isFlat, "
                + "a.externallyManaged, "
                + "root ";
        String sqlFromString =   "from TermTree as a ";

        String sqlJoinString =  "LEFT JOIN a.root as root "
               + "LEFT JOIN a.representations AS r "
                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }


    private static String[] createSqlPartsForDescriptiveDataSet() {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "r, "
                + "a.termType,  "
                + "a.uri,  "
                + "a.titleCache, "
                + "a.allowDuplicates, "
                + "a.orderRelevant, "
                + "a.isFlat, "
                + "a.externallyManaged,"
                + "root ";
        String sqlFromString =   "from DescriptiveDataSet as d ";

        String sqlJoinString =  "JOIN d.descriptiveSystem as a "
               + "LEFT JOIN a.root as root "
               + "LEFT JOIN a.representations AS r "
                ;

        String[] result = new String[3];
        result[0] = sqlSelectString;
        result[1] = sqlFromString;
        result[2] = sqlJoinString;
        return result;
    }


    public static List<TermCollectionDto> termTreeDtoListFrom(List<Object[]> results) {
        List<TermCollectionDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations/media/vocRepresentation because of LEFT JOIN
        Map<UUID, TermCollectionDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
            extracted(dtos, dtoMap, elements);
        }
        return dtos;
    }

    /**
     * @param dtos
     * @param dtoMap
     * @param elements
     */
    protected static void extracted(List<TermCollectionDto> dtos, Map<UUID, TermCollectionDto> dtoMap, Object[] elements) {
        UUID uuid = (UUID)elements[0];
        if(dtoMap.containsKey(uuid)){
            // multiple results for one term -> multiple (voc) representation/media
            if(elements[1]!=null){
                dtoMap.get(uuid).addRepresentation((Representation)elements[1]);
            }
        } else {
            // term representation
            Set<Representation> representations = new HashSet<>();
            if(elements[1] instanceof Representation) {
                representations = new HashSet<>(1);
                representations.add((Representation)elements[1]);
            }

            TermTreeDto termTreeDto = new TermTreeDto(
                    uuid,
                    representations,
                    (TermType)elements[2],
                    (String)elements[3],
                    (boolean)elements[4],
                    (boolean)elements[5],
                    (boolean)elements[6]);
            termTreeDto.setUri((URI)elements[8]);
            ExternallyManaged extManaged = (ExternallyManaged) elements[7];
            if (extManaged != null) {
                termTreeDto.setManaged(extManaged != null && extManaged.getAuthorityType() == AuthorityType.EXTERN);
            }


            if (termTreeDto.getTermType().equals(TermType.Character)){
                termTreeDto.setRoot(CharacterNodeDto.fromTermNode((TermNode<Character>) elements[9], termTreeDto));
            }else {
                termTreeDto.setRoot(TermNodeDto.fromNode((TermNode)elements[9], termTreeDto));
            }

            dtoMap.put(uuid, termTreeDto);
            dtos.add(termTreeDto);
        }
    }

    public boolean containsSubtrees(){
        boolean result = false;
        for (TermNodeDto child: root.getChildren()){
            if (child.getChildren() != null && !child.getChildren().isEmpty()){
                result = true;
                break;
            }
        }
        return result;
    }

    public TermNodeDto getNodeWithTerm(TermDto term, TermNodeDto node){
        TermNodeDto result = null;
        if (node.getTerm() != null && node.getTerm().getUuid().equals(term.getUuid())) {
            return node;
        }
        for (TermNodeDto child: node.getChildren()){
            result = getNodeWithTerm(term, child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * @return the inapplicableMap
     */
    public Map<UUID, Set<FeatureStateDto>> getInapplicableMap() {
        return inapplicableMap;
    }

    /**
     * @param inapplicableMap the inapplicableMap to set
     */
    public void setInapplicableMap(Map<UUID, Set<FeatureStateDto>> inapplicableMap) {
        this.inapplicableMap = inapplicableMap;
    }

    /**
     * @return the onlyApplicable
     */
    public Map<UUID, Set<FeatureStateDto>> getOnlyApplicable() {
        return onlyApplicableMap;
    }

    /**
     * @param onlyApplicable the onlyApplicable to set
     */
    public void setOnlyApplicable(Map<UUID, Set<FeatureStateDto>> onlyApplicable) {
        this.onlyApplicableMap = onlyApplicable;
    }
}