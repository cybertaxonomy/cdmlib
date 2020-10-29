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
import java.util.List;
import java.util.UUID;

import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Oct 9, 2020
 */
public class CharacterNodeDto extends TermNodeDto {

    private static final long serialVersionUID = 7635704848569122836L;

    /**
     * @param termDto
     * @param parent
     * @param position
     */
    public CharacterNodeDto(CharacterDto characterDto, TermNodeDto parent, int position, TermTreeDto treeDto, UUID uuid, String treeIndex, String path) {
        super(characterDto, parent, position, treeDto, uuid, treeIndex, path);
    }

    public static CharacterNodeDto fromTermNode(TermNode<Character> child) {
        Assert.notNull(child, "Node should not be null");
        CharacterNodeDto dto = new CharacterNodeDto(child.getTerm() != null?CharacterDto.fromCharacter(child.getTerm()): null, null, child.getParent() != null?child.getParent().getIndex(child): 0, TermTreeDto.fromTree(child.getGraph()), child.getUuid(), child.treeIndex(), child.getPath());

        if (child.getParent() != null){
            dto.setParentUuid(child.getParent().getUuid());
        }

        List<TermNodeDto> children = new ArrayList();
        for (Object o: child.getChildNodes()){
            if (o instanceof TermNode){
                TermNode childNode = (TermNode)o;

                if (childNode != null){
                    if(childNode.getTermType().equals(TermType.Character)){
                        children.add(CharacterNodeDto.fromTermNode(childNode));
                    }else{
                        children.add(TermNodeDto.fromNode(childNode));
                    }
                }
            }
        }
        dto.setChildren(children);
        dto.setOnlyApplicableIf(child.getOnlyApplicableIf());
        dto.setInapplicableIf(child.getInapplicableIf());

        dto.setTermType(child.getTermType());

        return dto;


    }



}
