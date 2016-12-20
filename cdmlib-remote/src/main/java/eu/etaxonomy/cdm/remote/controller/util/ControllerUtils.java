/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.util.TaxonRelationshipEdge;
import eu.etaxonomy.cdm.model.common.RelationshipBase.Direction;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.remote.editor.UuidList;

/**
 * @author a.kohlbecker
 * @date Jan 24, 2013
 *
 */
public class ControllerUtils {

    /**
     * @param relationshipUuids
     * @param relationshipInversUuids
     * @param includeRelationships
     * @return
     */
    public static Set<TaxonRelationshipEdge> loadIncludeRelationships(UuidList relationshipUuids, UuidList relationshipInversUuids, ITermService termService) {
        Set<TaxonRelationshipEdge> includeRelationships = null;
        if(relationshipUuids != null || relationshipInversUuids != null){
            includeRelationships = new HashSet<TaxonRelationshipEdge>();
            if(relationshipUuids != null) {
                for (UUID uuid : relationshipUuids) {
                    if(relationshipInversUuids != null && relationshipInversUuids.contains(uuid)){
                        includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedTo, Direction.relatedFrom));
                        relationshipInversUuids.remove(uuid);
                    } else {
                        includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedTo));
                    }
                }
            }
            if(relationshipInversUuids != null) {
                for (UUID uuid : relationshipInversUuids) {
                    includeRelationships.add(new TaxonRelationshipEdge((TaxonRelationshipType) termService.find(uuid), Direction.relatedFrom));
                }
            }
        }
        return includeRelationships;
    }


}
