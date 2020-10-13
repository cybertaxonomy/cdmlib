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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Mar 18, 2020
 */
public class TermNodeDto<T extends DefinedTermBase> implements Serializable{

    private static final long serialVersionUID = 7568459208397248126L;

    UUID parentUuid;
    String treeIndex;
    List<TermNodeDto<T>> children;
    Set<FeatureState> onlyApplicableIf = new HashSet<>();
    Set<FeatureState> inapplicableIf = new HashSet<>();
    UUID uuid;
    TermDto term;
    TermType type;
    TermTreeDto tree;

    public TermNodeDto(TermNode<T> node){
        uuid = node.getUuid();
        if (node.getParent() != null){
            parentUuid = node.getParent().getUuid();
        }
        TermTree termTree = HibernateProxyHelper.deproxy(node.getGraph(), TermTree.class);
        tree = new TermTreeDto(termTree);
        treeIndex = node.treeIndex();
        children = new ArrayList();
        for (TermNode<T> child: node.getChildNodes()){

            children.add(new TermNodeDto(child));
        }
        onlyApplicableIf = node.getOnlyApplicableIf();
        inapplicableIf = node.getInapplicableIf();
        if (node.getTerm() != null){
            term = TermDto.fromTerm(node.getTerm());
        }
        type = node.getTermType();
    }


    public TermNodeDto(TermDto termDto, TermNodeDto parent, int position, TermTreeDto treeDto){
        uuid = null;
        if (parent != null){
            parentUuid = parent.getUuid();
        }
        treeIndex = null;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();
        if (parent != null){
            parent.getChildren().add(position, this);
        }
        tree = treeDto;
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

    public List<TermNodeDto<T>> getChildren() {
        return children;
    }

    public void setChildren(List<TermNodeDto<T>> children) {
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
            if (child.getUuid().equals(nodeDto.getUuid())){
                return index;
            }
            index++;
        }
        return -1;
   }

   public boolean removeChild(TermNodeDto nodeDto){
       int index = this.getIndex(nodeDto);
       if (index > -1){
           this.getChildren().remove(index);
           return true;
       }else if (this.getChildren() != null && !this.getChildren().isEmpty()){
           for (TermNodeDto child: children){
               boolean result = child.removeChild(nodeDto);
               if (result){
                   return true;
               }
           }
       }
       return false;
   }




}
