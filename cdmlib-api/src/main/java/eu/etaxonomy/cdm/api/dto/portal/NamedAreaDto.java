/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.beans.Transient;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.tmp.TermDto;
import eu.etaxonomy.cdm.common.CdmUtils;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class NamedAreaDto extends TermDto {

    //for all terms
    private UUID vocabularyUuid;
    private Set<UUID> positiveMarkers = new HashSet<>();
    //specific for area
    private UUID levelUuid;
    private String areaMapping;
    //TODO quickfix, better decision needs to wait if TermDto will inherit from IdentifiableDto
    private String geoServiceMapping;

    //uuid should not be null to allow equals()
    public NamedAreaDto(UUID uuid, int id, String label) {
        super(uuid, id, label);
    }

    public UUID getLevelUuid() {
        return this.levelUuid;
    }
    public void setLevelUuid(UUID levelUuid) {
        this.levelUuid = levelUuid;
    }

    //only needed transient for computation TODO
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

    public UUID getVocabularyUuid() {
        return vocabularyUuid;
    }
    public void setVocabularyUuid(UUID vocabularyUuid) {
        this.vocabularyUuid = vocabularyUuid;
    }

    @Transient  //TODO
    public String getAreaMapping() {
        return areaMapping;
    }

    @Transient  //TODO
    public String getGeoServiceMapping() {
        return geoServiceMapping;
    }
    public void setGeoServiceMapping(String geoServiceMapping) {
        this.geoServiceMapping = geoServiceMapping;
    }

    //@Override
    public int compareTo(NamedAreaDto area) {

        Integer orderThis = this.getOrderIndex();
        Integer orderThat = area.getOrderIndex();

        if (orderThis > orderThat){
            return -1;
        }else if (orderThis < orderThat){
            return 1;
        } else {
            return CdmUtils.nullSafeCompareTo(this.vocabularyUuid, area.vocabularyUuid);
        }
    }

//************************ toString() *********************************/

    @Override
    public String toString() {
        return "NamedAreaDto [label=" + getLabel() + ", level=" + levelUuid + "]";
    }
}