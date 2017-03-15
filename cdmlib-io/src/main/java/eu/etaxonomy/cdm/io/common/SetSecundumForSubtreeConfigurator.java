/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @date 06.01.2017
 *
 */
public class SetSecundumForSubtreeConfigurator extends ImportConfiguratorBase<DefaultImportState<SetSecundumForSubtreeConfigurator>, Object> implements IImportConfigurator{
    private UUID subtreeUuid;
    private Reference newSecundum;
    private boolean includeAcceptedTaxa = true;
    private boolean includeSynonyms = true;
    private boolean overwriteExistingAccepted = true;
    private boolean overwriteExistingSynonyms = true;
    private boolean emptySecundumDetail = true;
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
    public SetSecundumForSubtreeConfigurator(UUID subtreeUuid, Reference newSecundum, IProgressMonitor monitor) {
        super(null);
        this.subtreeUuid = subtreeUuid;
        this.newSecundum = newSecundum;
        this.monitor = monitor;
    }

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    public SetSecundumForSubtreeConfigurator(UUID subtreeUuid) {
        super(null);
        this.subtreeUuid = subtreeUuid;
        // this.newSecundum = newSecundum;
    }

    /**
     * @return the subtreeUuid
     */
    public UUID getSubtreeUuid() {
        return subtreeUuid;
    }

    /**
     * @param subtreeUuid
     *            the subtreeUuid to set
     */
    public void setSubtreeUuid(UUID subtreeUuid) {
        this.subtreeUuid = subtreeUuid;
    }

    /**
     * @return the newSecundum
     */
    public Reference getNewSecundum() {
        return newSecundum;
    }

    /**
     * @param newSecundum
     *            the newSecundum to set
     */
    public void setNewSecundum(Reference newSecundum) {
        this.newSecundum = newSecundum;
    }

    /**
     * @return the overrideExisting
     */
    public boolean isOverwriteExistingAccepted() {
        return overwriteExistingAccepted;
    }

    /**
     * @param overrideExisting
     *            the overrideExisting to set
     */
    public void setOverwriteExistingAccepted(boolean overwriteExistingAccepted) {
        this.overwriteExistingAccepted = overwriteExistingAccepted;
    }

    /**
     * @return the overrideExisting
     */
    public boolean isOverwriteExistingSynonyms() {
        return overwriteExistingSynonyms;
    }

    /**
     * @param overrideExisting
     *            the overrideExisting to set
     */
    public void setOverwriteExistingSynonyms(boolean overwriteExistingSynonyms) {
        this.overwriteExistingSynonyms = overwriteExistingSynonyms;
    }

    /**
     * @return the emptySecundumDetail
     */
    public boolean isEmptySecundumDetail() {
        return emptySecundumDetail;
    }

    /**
     * @param emptySecundumDetail
     *            the emptySecundumDetail to set
     */
    public void setEmptySecundumDetail(boolean emptySecundumDetail) {
        this.emptySecundumDetail = emptySecundumDetail;
    }

    /**
     * @return the includeSynonyms
     */
    public boolean isIncludeSynonyms() {
        return includeSynonyms;
    }

    /**
     * @param includeSynonyms
     *            the includeSynonyms to set
     */
    public void setIncludeSynonyms(boolean includeSynonyms) {
        this.includeSynonyms = includeSynonyms;
    }

    /**
     * @return the includeAcceptedTaxa
     */
    public boolean isIncludeAcceptedTaxa() {
        return includeAcceptedTaxa;
    }

    /**
     * @param includeAcceptedTaxa
     *            the includeAcceptedTaxa to set
     */
    public void setIncludeAcceptedTaxa(boolean includeAcceptedTaxa) {
        this.includeAcceptedTaxa = includeAcceptedTaxa;
    }

    /**
     * @return the includeSharedTaxa
     */
    public boolean isIncludeSharedTaxa() {
        return includeSharedTaxa;
    }

    /**
     * @param includeSharedTaxa
     *            the includeSharedTaxa to set
     */
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
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                SecundumUpdater.class
                };

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference getSourceReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isValid(){
        return true;
    }

}
