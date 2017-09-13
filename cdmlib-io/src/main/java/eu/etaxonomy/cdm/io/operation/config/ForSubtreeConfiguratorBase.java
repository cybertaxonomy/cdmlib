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
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @date 06.01.2017
 *
 */
public abstract class ForSubtreeConfiguratorBase<CONFIG extends ImportConfiguratorBase>
        extends ImportConfiguratorBase<DefaultImportState<CONFIG>, Object>
        implements IImportConfigurator{

    private static final long serialVersionUID = 2756961021157678305L;

    private UUID subtreeUuid;
    private boolean includeAcceptedTaxa = true;
    private boolean includeSynonyms = true;
    private boolean includeSharedTaxa = true;
    private IProgressMonitor monitor;

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public ForSubtreeConfiguratorBase(UUID subtreeUuid, IProgressMonitor monitor) {
        super(null);
        this.subtreeUuid = subtreeUuid;
        this.monitor = monitor;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public ForSubtreeConfiguratorBase(UUID subtreeUuid) {
        super(null);
        this.subtreeUuid = subtreeUuid;
    }

    public UUID getSubtreeUuid() {
        return subtreeUuid;
    }
    public void setSubtreeUuid(UUID subtreeUuid) {
        this.subtreeUuid = subtreeUuid;
    }


    public boolean isIncludeSynonyms() {
        return includeSynonyms;
    }
    public void setIncludeSynonyms(boolean includeSynonyms) {
        this.includeSynonyms = includeSynonyms;
    }

    public boolean isIncludeAcceptedTaxa() {
        return includeAcceptedTaxa;
    }
    public void setIncludeAcceptedTaxa(boolean includeAcceptedTaxa) {
        this.includeAcceptedTaxa = includeAcceptedTaxa;
    }

    public boolean isIncludeSharedTaxa() {
        return includeSharedTaxa;
    }
    public void setIncludeSharedTaxa(boolean includeSharedTaxa) {
        this.includeSharedTaxa = includeSharedTaxa;
    }

    /**
     * @return
     */
    public IProgressMonitor getMonitor() {

        return monitor;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("rawtypes")
    @Override
    public DefaultImportState getNewState() {
      return new DefaultImportState(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getSourceReference() {
        return null;
    }

    @Override
    public boolean isValid(){
        return true;
    }

}
