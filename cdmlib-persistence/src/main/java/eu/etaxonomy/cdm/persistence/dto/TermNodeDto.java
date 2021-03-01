/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Mar 18, 2020
 */
public class TermNodeDto implements Serializable{

    private static final long serialVersionUID = 7568459208397248126L;

    private UUID parentUuid;
    private String treeIndex;
    private List<TermNodeDto> children;
    private Set<FeatureState> onlyApplicableIf = new HashSet<>();
    private Set<FeatureState> inapplicableIf = new HashSet<>();
    private UUID uuid;
    private TermDto term;
    private TermType type;
    private TermTreeDto tree;
    private String path;

    public static TermNodeDto fromNode(TermNode node, TermTreeDto treeDto){
        Assert.notNull(node, "Node should not be null");
        TermDto term = node.getTerm() != null?TermDto.fromTerm(node.getTerm()): null;
        TermNodeDto dto = new TermNodeDto(term, node.getParent() != null? node.getParent().getIndex(node): 0, treeDto != null? treeDto: TermTreeDto.fromTree((TermTree)node.getGraph()), node.getUuid(), node.treeIndex(), node.getPath());
        if (node.getParent() != null){
            dto.setParentUuid(node.getParent().getUuid());
        }

        List<TermNodeDto> children = new ArrayList<>();
        for (Object o: node.getChildNodes()){
            if (o instanceof TermNode){
                TermNode<?> child = (TermNode<?>)o;
                if (child != null){
                    if(child.getTerm() != null && child.getTerm().getTermType().equals(TermType.Character)){
                        children.add(CharacterNodeDto.fromTermNode((TermNode)child, treeDto));
                    }else{
                        children.add(TermNodeDto.fromNode(child, treeDto));
                    }

                }
            }
        }
        if (!treeDto.isOrderRelevant()){
            Collections.sort(children, new AlphabeticalNodeTermDtoComparator());
        }
        dto.setChildren(children);
        dto.setOnlyApplicableIf(node.getOnlyApplicableIf());
        dto.setInapplicableIf(node.getInapplicableIf());
        dto.setTermType(node.getTermType());
        return dto;
    }

    public TermNodeDto(TermDto termDto, TermNodeDto parent, int position, TermTreeDto treeDto, UUID uuid, String treeIndex, String path){
        this.uuid = uuid;
        if (parent != null){
            parentUuid = parent.getUuid();
        }
        this.treeIndex = treeIndex;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();
        if (parent != null){
            parent.getChildren().add(position, this);
        }
        tree = treeDto;
        if (tree != null){
            tree.addTerm(termDto);
        }
        this.path = path;
    }

    public TermNodeDto(TermDto termDto, UUID parentUuid, int position, TermTreeDto treeDto, UUID uuid, String treeIndex, String path){
        this.uuid = uuid;
        this.parentUuid = parentUuid;

        this.treeIndex = treeIndex;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();

        tree = treeDto;
        this.path = path;

    }

    public TermNodeDto(TermDto termDto, int position, TermTreeDto treeDto, UUID uuid, String treeIndex, String path){
        this.uuid = uuid;
        this.treeIndex = treeIndex;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();

        tree = treeDto;
        this.path = path;
    }

/*--------Getter and Setter ---------------*/

    public UUID getParentUuid() {
        return parentUuid;
    }

    public void setParentUuid(UUID parentUuid) {
        this.parentUuid = parentUuid;
    }

    public String getTreeIndex() {
        return treeIndex;
    }

    public void setTreeIndex(String treeIndex) {
        this.treeIndex = treeIndex;
    }

    public TermTreeDto getTree() {
        return tree;
    }

    public void setTree(TermTreeDto tree) {
        this.tree = tree;
    }

    public List<TermNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<TermNodeDto> children) {
        this.children = children;
    }

    public Set<FeatureState> getOnlyApplicableIf() {
        return onlyApplicableIf;
    }

    public void setOnlyApplicableIf(Set<FeatureState> onlyApplicableIf) {
        this.onlyApplicableIf = onlyApplicableIf;
    }

    public Set<FeatureState> getInapplicableIf() {
        return inapplicableIf;
    }

    public void setInapplicableIf(Set<FeatureState> inapplicableIf) {
        this.inapplicableIf = inapplicableIf;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public TermDto getTerm(){
        return term;
    }

    public void setTerm(TermDto termDto){
        term = termDto;
    }

    public TermType getType(){
        return type;
    }

    public void setTermType(TermType termType){
        type = termType;
    }

    public int getIndex(TermNodeDto nodeDto) {
        int index = 0;
        for (TermNodeDto child: children){
            if (child != null && child.getUuid() != null && nodeDto.getUuid() != null){
                if (child.getUuid().equals(nodeDto.getUuid())){
                    return index;
                }else if(child != null &&(child.getUuid() == null && nodeDto.getUuid() == null) && child.getTerm().getUuid().equals(nodeDto.getTerm().getUuid())){
                    return index;
                }
            }
            index++;
        }
        return -1;
   }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean removeChild(TermNodeDto nodeDto, boolean doRecursive){
       int index = this.getIndex(nodeDto);
       if (index > -1){
           this.getChildren().remove(index);
           return true;
       }else if (doRecursive && this.getChildren() != null && !this.getChildren().isEmpty()){
           for (TermNodeDto child: children){
               boolean result = child.removeChild(nodeDto, doRecursive);
               if (result){
                   return true;
               }
           }
       }
       return false;
   }


   public static String getTermNodeDtoSelect(){
       String[] result = createSqlParts();

       return result[0]+result[1]+result[2];
   }

   private static String[] createSqlParts() {
       String sqlSelectString = ""
               + " SELECT a.uuid, "
               + " r, "
               + " a.termType,  "
               + " a.uri,  "
               + " root,  "
               + " a.titleCache, "
               + " a.allowDuplicates, "
               + " a.orderRelevant, "
               + " a.isFlat ";
       String sqlFromString = " FROM TermNode as a ";

       String sqlJoinString = " LEFT JOIN a.tree "
              + " LEFT JOIN a.representations AS r "
               ;

       String[] result = new String[3];
       result[0] = sqlSelectString;
       result[1] = sqlFromString;
       result[2] = sqlJoinString;
       return result;
   }

   private static String[] createSqlPartsWithTerm() {
       String sqlSelectString = ""
               + " SELECT a.uuid, "
               + " r, "
               + " a.term"
               + " a.termType,  "
               + " a.uri,  "
               + " root,  "
               + " a.titleCache, "
               + " a.allowDuplicates, "
               + " a.orderRelevant, "
               + " a.isFlat ";
       String sqlFromString = " FROM TermNode as a ";

       String sqlJoinString = " LEFT JOIN a.tree "
              + " LEFT JOIN a.representations AS r "
               ;

       String[] result = new String[3];
       result[0] = sqlSelectString;
       result[1] = sqlFromString;
       result[2] = sqlJoinString;
       return result;
   }
}