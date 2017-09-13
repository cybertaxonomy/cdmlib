/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation.config;

import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.operation.SecundumForSubtreeUpdater;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @date 06.01.2017
 *
 */
public class SecundumForSubtreeConfigurator
            extends ForSubtreeConfiguratorBase<SecundumForSubtreeConfigurator>
            implements IImportConfigurator{

    private static final long serialVersionUID = 1202667588493272030L;

    private Reference newSecundum;
    private boolean emptySecundumDetail = true;
    private IProgressMonitor monitor;

    /**
     * @param monitor the monitor to set
     */
    @Override
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public SecundumForSubtreeConfigurator(UUID subtreeUuid, Reference newSecundum, IProgressMonitor monitor) {
        super(subtreeUuid, monitor);
        this.newSecundum = newSecundum;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public SecundumForSubtreeConfigurator(UUID subtreeUuid) {
        super(subtreeUuid);
        // this.newSecundum = newSecundum;
    }

    public Reference getNewSecundum() {
        return newSecundum;
    }
    public void setNewSecundum(Reference newSecundum) {
        this.newSecundum = newSecundum;
    }

    public boolean isEmptySecundumDetail() {
        return emptySecundumDetail;
    }
    public void setEmptySecundumDetail(boolean emptySecundumDetail) {
        this.emptySecundumDetail = emptySecundumDetail;
    }

    /**
     * @return
     */
    @Override
    public IProgressMonitor getMonitor() {
        return monitor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                SecundumForSubtreeUpdater.class
                };

    }

}
