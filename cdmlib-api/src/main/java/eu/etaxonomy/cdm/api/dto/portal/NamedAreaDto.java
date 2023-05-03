/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.SetMap;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;

/**
 * @author a.mueller
 * @date 09.02.2023
 */
public class NamedAreaDto extends CdmBaseDto {

    private String label;
    private LabeledEntityDto level;
    private NamedAreaDto parent;
    private Set<Marker> markers;

//    public class NamedAreaLevelDTO extends CdmBaseDto {
//        private String label;
//        public NamedAreaLevelDTO(NamedAreaLevel level) {
//            super(level.getUuid(), level.getId(), null);
//            this.label = level.getLabel();
//        }
//        public String getLabel() {
//            return label;
//        }
//    }

    public NamedAreaDto(UUID uuid, int id, String label, NamedAreaLevel level, NamedAreaDto parent, Set<Marker> markers) {
        super(uuid, id, null);
        setUuid(uuid);
        this.label = label;
        if (level != null) {
            this.level = new LabeledEntityDto(level.getUuid(), level.getId(), level.getLabel());
        }
        this.parent = parent;
        this.markers = markers;
    }

    //TODO should not exist
    public NamedAreaDto(NamedArea area, SetMap<NamedArea, NamedArea> parentAreaMap) {
        super(area.getUuid(), area.getId(), null);
        this.label = area.getLabel();   //TODO i18n
        if (area.getLevel() != null) {
            NamedAreaLevel aLevel = area.getLevel();
            //TODO i18n
            level = new LabeledEntityDto(aLevel.getUuid(), aLevel.getId(), aLevel.getLabel());
        }
        if (parentAreaMap != null) {
            NamedArea parent = parentAreaMap.getFirstValue(area);  //TODO handle >1 parents
            if (parent != null) {
                this.parent = new NamedAreaDto(parent, parentAreaMap);
            }
        }
        this.markers = area.getMarkers();
    }

//    @Override
    public String getLabel() {
        return label;
    }

//    @Override
    public LabeledEntityDto getLevel() {
        return level;
    }

//    @Override
    public NamedAreaDto getParent() {
        return parent;
    }

    public boolean hasMarker(MarkerType markerType, boolean value) {
        for (Marker marker : markers) {
            if (marker.getMarkerType().equals(markerType) && marker.getFlag() == value) {
               return true;
            }
        }
        return false;
    }

//    @Override
    public int compareTo(NamedAreaDto area) {
        // TODO Auto-generated method stub
        return 0;
    }

//************************ toString() *********************************/

    @Override
    public String toString() {
        return "NamedAreaDto [label=" + label + ", level=" + level + "]";
    }
}