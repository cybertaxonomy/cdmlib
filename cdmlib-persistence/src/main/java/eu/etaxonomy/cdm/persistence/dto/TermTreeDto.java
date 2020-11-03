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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;;

/**
 * @author k.luther
 * @since 07.01.2020
 */
public class TermTreeDto extends TermCollectionDto {
    private static final long serialVersionUID = -7223363599985320531L;

    TermNodeDto root;

    public static TermTreeDto fromTree(TermTree tree){
        TermTreeDto dto = new TermTreeDto(tree.getUuid(), tree.getRepresentations(), tree.getTermType(), tree.getRoot(), tree.getTitleCache(), tree.isAllowDuplicates(), tree.isOrderRelevant(), tree.isFlat() );
        return dto;
        }


    /**
     * @param uuid
     * @param representations
     * @param termType
     */
    public TermTreeDto(UUID uuid, Set<Representation> representations, TermType termType, String titleCache, boolean isAllowDuplicates, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, termType, titleCache, isAllowDuplicates, isOrderRelevant, isFlat);

    }

    public TermTreeDto(UUID uuid, Set<Representation> representations, TermType termType, TermNode root, String titleCache, boolean isAllowDuplicates, boolean isOrderRelevant, boolean isFlat) {
        super(uuid, representations, termType, titleCache, isAllowDuplicates, isOrderRelevant, isFlat);
        this.root = new TermNodeDto(null, null, 0, this, root.getUuid(), root.treeIndex(), root.getPath());

    }

    public TermNodeDto getRoot() {
       return root;
    }

    public void setRoot(TermNodeDto root) {
        this.root = root;
    }

    public boolean removeChild(TermNodeDto nodeDto){
        return this.root.removeChild(nodeDto);
    }


    public static String getTermTreeDtoSelect(){
        String[] result = createSqlParts();

        return result[0]+result[1]+result[2];
    }

    /**
     * @param fromTable
     * @return
     */
    private static String[] createSqlParts() {
        String sqlSelectString = ""
                + "select a.uuid, "
                + "r, "
                + "a.termType,  "
                + "a.uri,  "
                + "root,  "
                + "a.titleCache, "
                + "a.allowDuplicates, "
                + "a.orderRelevant, "
                + "a.isFlat ";
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

    public static List<TermTreeDto> termTreeDtoListFrom(List<Object[]> results) {
        List<TermTreeDto> dtos = new ArrayList<>(); // list to ensure order
        // map to handle multiple representations/media/vocRepresentation because of LEFT JOIN
        Map<UUID, TermTreeDto> dtoMap = new HashMap<>(results.size());
        for (Object[] elements : results) {
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
                    representations = new HashSet<Representation>(1);
                    representations.add((Representation)elements[1]);
                }

                TermTreeDto termTreeDto = new TermTreeDto(
                        uuid,
                        representations,
                        (TermType)elements[2],
                        (String)elements[5],
                        (boolean)elements[6],
                        (boolean)elements[7],
                        (boolean)elements[8]);
                termTreeDto.setUri((URI)elements[3]);
                if (termTreeDto.getTermType().equals(TermType.Character)){
                    termTreeDto.setRoot(CharacterNodeDto.fromTermNode((TermNode<Character>) elements[4], termTreeDto));
                }else {
                    termTreeDto.setRoot(TermNodeDto.fromNode((TermNode)elements[4], termTreeDto));
                }



                dtoMap.put(uuid, termTreeDto);
                dtos.add(termTreeDto);
            }
        }
        return dtos;
    }
}
