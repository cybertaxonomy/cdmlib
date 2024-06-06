/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal.tmp;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.LabeledEntityDto;

/**
 * @author muellera
 * @since 29.02.2024
 */
//TODO we do not really need a label here
public class TermNodeDto extends LabeledEntityDto {

    private TermDto term;
    private List<TermNodeDto> children;
    private Set<UUID> positiveMarkers = new HashSet<>();
    private TermNodeDto parent;

    public TermNodeDto(UUID uuid, Integer id, String label, TermNodeDto parent) {
        super(uuid, id, label);
        this.parent = parent;
    }


    public TermDto getTerm() {
        return term;
    }
    public void setTerm(TermDto term) {
        this.term = term;
    }

    public List<TermNodeDto> getChildren() {
        return children;
    }
    /**
     * Also sets the parent of the child to <code>this</code>
     */
    public void addChild(TermNodeDto child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.parent = this;
    }

    //only needed transient TODO
    @Transient
    public Set<UUID> getMarkers() {
        return positiveMarkers;
    }
    public boolean hasMarker(UUID markerTypeUuid) {
        return positiveMarkers.contains(markerTypeUuid);
    }
    public boolean addMarker(UUID markerTypeUuid) {
        return positiveMarkers.add(markerTypeUuid);
    }

    public TermNodeDto getParent() {
        return parent;
    }
}