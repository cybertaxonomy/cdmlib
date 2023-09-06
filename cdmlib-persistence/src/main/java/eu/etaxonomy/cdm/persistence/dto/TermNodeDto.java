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

import org.springframework.util.Assert;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author k.luther
 * @since Mar 18, 2020
 */
public class TermNodeDto extends CdmBaseDto implements Serializable, IAnnotatableDto{

    private static final long serialVersionUID = 7568459208397248126L;

    private UUID parentUuid;
    private String treeIndex;
    private List<TermNodeDto> children;
    private Set<FeatureStateDto> onlyApplicableIf = new HashSet<>();
    private Set<FeatureStateDto> inapplicableIf = new HashSet<>();

    private TermDto term;
    private TermType type;
    private UUID treeUuid;
    private String path;
    private Set<MarkerDto> markers;
    private Set<AnnotationDto> annotations;
    private boolean externallyManaged = false;
    private boolean allowsDuplicats = false;
    private boolean isFlat = false;
    private boolean isOrderRelevant = false;


    public static TermNodeDto fromNode(TermNode node, TermTreeDto treeDto){
        Assert.notNull(node, "Node should not be null");
        TermDto term = node.getTerm() != null?TermDto.fromTerm(node.getTerm()): null;
        if (treeDto != null){
            treeDto.addTerm(term);
        }

        TermNodeDto dto = new TermNodeDto(term, node.getParent() != null? node.getParent().getIndex(node): 0, treeDto != null? treeDto: TermTreeDto.fromTree((TermTree)node.getGraph()), node.getUuid(), node.getId(), node.treeIndex(), node.getPath());

        if (node.getParent() != null){
            dto.setParentUuid(node.getParent().getUuid());
        }
        dto.setExternallyManaged(treeDto!= null? treeDto.isManaged():false);
        dto.setAllowsDuplicats(treeDto!= null? treeDto.isAllowDuplicate():false);
        dto.setFlat(treeDto!= null? treeDto.isFlat():false);
        dto.setFlat(treeDto!= null? treeDto.isOrderRelevant():false);

        List<TermNodeDto> children = new ArrayList<>();
        for (Object o: node.getChildNodes()){
            if (o instanceof TermNode){
                TermNode<?> child = (TermNode<?>)o;
                if(child.getTerm() != null && child.getTerm().getTermType().equals(TermType.Character)){
                    children.add(CharacterNodeDto.fromTermNode((TermNode)child, treeDto));
                }else{
                    children.add(TermNodeDto.fromNode(child, treeDto));
                }
            }
        }

        dto.setChildren(children);

        dto.setOnlyApplicableIf(node.getOnlyApplicableIf());
        dto.setInapplicableIf(node.getInapplicableIf());
        dto.setTermType(node.getTermType());

        //annotations
        if (dto.annotations == null) {
            dto.annotations = new HashSet<>();
        }
        for (Annotation an: node.getAnnotations()) {
            AnnotationDto anDto = new AnnotationDto(an.getUuid(), an.getId(), an.getAnnotationType() == null ? null : an.getAnnotationType().getUuid(),
                    an.getAnnotationType() == null ? null : an.getAnnotationType().getLabel(),
                            an.getText(), an.getCreated(), an.getCreatedBy() != null? CdmUtils.Nz(an.getCreatedBy().getUsername()):null,
                                    an.getUpdated(), an.getUpdatedBy() != null? CdmUtils.Nz(an.getUpdatedBy().getUsername()):null);

            dto.annotations.add(anDto);

        }


        //markers
        if (dto.markers == null) {
            dto.markers = new HashSet<>();
        }
        for (Marker marker: node.getMarkers()) {


            MarkerDto maDto = new MarkerDto(marker.getUuid(), marker.getId(),
                    marker.getMarkerType().getUuid(), marker.getMarkerType().getLabel(),
                    marker.getFlag(), marker.getCreated(), marker.getCreatedBy()!= null?CdmUtils.Nz(marker.getCreatedBy().getUsername()):null,
                            marker.getUpdated(), marker.getUpdatedBy()!= null? CdmUtils.Nz(marker.getUpdatedBy().getUsername()):null);

            dto.markers.add(maDto);
        }
        dto.setCreated(node.getCreated());
        dto.setCreatedBy(node.getCreatedBy()!= null? CdmUtils.Nz(node.getCreatedBy().getUsername()): null);
        dto.setUpdated(node.getUpdated());
        dto.setUpdatedBy(node.getUpdatedBy()!= null? CdmUtils.Nz(node.getUpdatedBy().getUsername()): null);

        return dto;
    }

    public TermNodeDto(TermDto termDto, TermNodeDto parent, int position, TermTreeDto treeDto, UUID uuid, int id, String treeIndex, String path){
        super(uuid, id);

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

        if (treeDto != null){
            treeUuid = treeDto.getUuid();
            treeDto.addTerm(termDto);
            this.externallyManaged = treeDto.isManaged();
            this.allowsDuplicats = treeDto.isAllowDuplicate();
            this.isFlat = treeDto.isFlat();
            this.isOrderRelevant = treeDto.isOrderRelevant();
        }else {
            treeUuid = parent.getTreeUuid();
            this.externallyManaged = parent.externallyManaged;
            this.allowsDuplicats = parent.allowsDuplicats;
            this.isFlat = parent.isFlat;
            this.isOrderRelevant = parent.isOrderRelevant;
        }
        this.path = path;
    }

    public TermNodeDto(TermDto termDto, UUID parentUuid, int position, TermTreeDto treeDto, UUID uuid, int id, String treeIndex, String path){
        super(uuid, id);

        this.parentUuid = parentUuid;

        this.treeIndex = treeIndex;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();

        if (treeDto != null){
            treeUuid = treeDto.getUuid();
            treeDto.addTerm(termDto);
            this.externallyManaged = treeDto.isManaged();
            this.allowsDuplicats = treeDto.isAllowDuplicate();
            this.setFlat(treeDto.isFlat());
            this.isOrderRelevant = treeDto.isOrderRelevant();
        }
        this.path = path;

    }

    public TermNodeDto(TermDto termDto, int position, TermTreeDto treeDto, UUID uuid, int id, String treeIndex, String path){
        super(uuid, id);

        this.treeIndex = treeIndex;
        term = termDto;
        type = termDto!= null? termDto.getTermType(): null;
        children = new ArrayList<>();

        if (treeDto != null){
            treeUuid = treeDto.getUuid();
            treeDto.addTerm(termDto);
            this.externallyManaged = treeDto.isManaged();
            this.allowsDuplicats= treeDto.isAllowDuplicate();
            this.isFlat = treeDto.isFlat();
            this.isOrderRelevant = treeDto.isOrderRelevant();
        }
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

    public UUID getTreeUuid() {
        return treeUuid;
    }

    public void setTree(UUID treeUUID) {
        this.treeUuid = treeUUID;
    }

    public List<TermNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<TermNodeDto> children) {
        this.children = children;
    }

    public Set<FeatureStateDto> getOnlyApplicableIf() {
        return onlyApplicableIf;
    }

    public void setOnlyApplicableIfDto(Set<FeatureStateDto> onlyApplicableIf) {
        this.onlyApplicableIf = onlyApplicableIf;
    }
    public void setOnlyApplicableIf(Set<FeatureState> onlyApplicableIf) {
        if (this.onlyApplicableIf == null){
            this.onlyApplicableIf = new HashSet<>();
        }
        for (FeatureState state: onlyApplicableIf){
            this.onlyApplicableIf.add(new FeatureStateDto(state.getUuid(), FeatureDto.fromFeature(state.getFeature()), TermDto.fromTerm(CdmBase.deproxy(state.getState(), DefinedTermBase.class))));
        }
    }

    public Set<FeatureStateDto> getInapplicableIf() {
        return inapplicableIf;
    }

    public void setInapplicableIfDto(Set<FeatureStateDto> inapplicableIf) {
        this.inapplicableIf = inapplicableIf;
    }

    public void setInapplicableIf(Set<FeatureState> inApplicableIf) {
        if (this.inapplicableIf == null){
            this.inapplicableIf = new HashSet<>();
        }
        for (FeatureState state: inApplicableIf){
            this.inapplicableIf.add( new FeatureStateDto(state.getUuid(),FeatureDto.fromFeature(state.getFeature()), TermDto.fromTerm(CdmBase.deproxy(state.getState(), DefinedTermBase.class))));
        }
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

    @Override
    public Set<MarkerDto> getMarkers() {
        return markers;
    }

    @Override
    public void setMarkers(Set<MarkerDto> markers) {
        this.markers = markers;
    }
    @Override
    public void addMarker(MarkerDto marker) {
        if (this.markers == null) {
            this.markers = new HashSet<>();
        }
        this.markers.add(marker);
    }

    /**
     * @return the externallyManaged
     */
    public boolean isExternallyManaged() {
        return externallyManaged;
    }

    /**
     * @return the isFlat
     */
    public boolean isFlat() {
        return isFlat;
    }

    /**
     * @param isFlat the isFlat to set
     */
    public void setFlat(boolean isFlat) {
        this.isFlat = isFlat;
    }

    /**
     * @param externallyManaged the externallyManaged to set
     */
    public void setExternallyManaged(boolean externallyManaged) {
        this.externallyManaged = externallyManaged;
    }

    @Override
    public Set<AnnotationDto> getAnnotations() {
        return annotations;
    }



    /**
     * @return the allowsDuplicats
     */
    public boolean isAllowsDuplicats() {
        return allowsDuplicats;
    }

    /**
     * @param allowsDuplicats the allowsDuplicats to set
     */
    public void setAllowsDuplicats(boolean allowsDuplicats) {
        this.allowsDuplicats = allowsDuplicats;
    }




    public void setType(TermType type) {
        this.type = type;
    }

    @Override
    public void setAnnotations(Set<AnnotationDto> annotations) {
        this.annotations = annotations;
    }

    @Override
    public void addAnnotation(AnnotationDto annotation) {
        if (this.annotations == null) {
            this.annotations = new HashSet<>();
        }
        this.annotations.add(annotation);
    }

    public boolean removeChild(TermNodeDto nodeDto, boolean doRecursive){
       int index = this.getIndex(nodeDto);
       if (index > -1){
           this.getChildren().remove(index);
           //this.tree.removeTerm(nodeDto.term);
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
               + " a.id, "
               + " r, "
               + " a.termType,  "
               + " a.uri,  "
               + " root,  "
               + " a.titleCache ";
//               + " a.allowDuplicates, "
//               + " a.orderRelevant, "
//               + " a.isFlat ";
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
               + " a.id, "
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

    @Override
    public String getLabel() {
        return this.getTerm().getTitleCache();
    }

    /**
     * @return the isOrderRelevant
     */
    public boolean isOrderRelevant() {
        return isOrderRelevant;
    }

    /**
     * @param isOrderRelevant the isOrderRelevant to set
     */
    public void setOrderRelevant(boolean isOrderRelevant) {
        this.isOrderRelevant = isOrderRelevant;
    }

    @Override
    public void removeMarker(MarkerDto marker) {
        this.getMarkers().remove(marker);
    }

    @Override
    public void removeAnnotation(AnnotationDto annotation) {
        this.getAnnotations().remove(annotation);
    }
}
