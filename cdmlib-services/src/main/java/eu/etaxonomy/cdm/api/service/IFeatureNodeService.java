/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import eu.etaxonomy.cdm.api.service.config.FeatureNodeDeletionConfigurator;
import eu.etaxonomy.cdm.model.description.FeatureNode;

/**
 * @author n.hoffmann
 * @created Aug 5, 2010
 * @version 1.0
 */
public interface IFeatureNodeService extends IVersionableService<FeatureNode>{


    /**
     * @param node
     * @param config
     * @return
     */
    DeleteResult isDeletable(FeatureNode node, FeatureNodeDeletionConfigurator config);

    /**
     * @param nodeUuid
     * @param config
     * @return
     */
    DeleteResult deleteFeatureNode(UUID nodeUuid, FeatureNodeDeletionConfigurator config);

}
