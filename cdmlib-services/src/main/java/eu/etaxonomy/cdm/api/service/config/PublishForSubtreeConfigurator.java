/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * Configurator for the setPublishForSubtree operation.
 *
 * @author a.mueller
 * @date 13.09.2017
 *
 */
public class PublishForSubtreeConfigurator
            extends ForSubtreeConfiguratorBase{

    private static final long serialVersionUID = 1202667588493272030L;

    private boolean publish = false;

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public PublishForSubtreeConfigurator(UUID subtreeUuid, boolean publish, IProgressMonitor monitor) {
        super(subtreeUuid, monitor);
        this.publish = publish;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public PublishForSubtreeConfigurator(UUID subtreeUuid) {
        super(subtreeUuid);
        // this.newSecundum = newSecundum;
    }

    public boolean isPublish() {
        return publish;
    }
    public void setPublish(boolean publish) {
        this.publish = publish;
    }



}
