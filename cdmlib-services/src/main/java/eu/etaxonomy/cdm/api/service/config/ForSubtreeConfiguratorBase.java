/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * Configurator for the setSecundumForSubtree operation.
 *
 * @author a.mueller
 * @since 06.01.2017
 */
public abstract class ForSubtreeConfiguratorBase
            implements Serializable{

    private static final long serialVersionUID = 2756961021157678305L;

    private UUID subtreeUuid;
    private boolean includeAcceptedTaxa = true;
    private boolean includeSynonyms = true;
    private boolean includeSharedTaxa = true;
    private boolean includeMisapplications = true;  //SecundumForSubtree overrides default value
    private boolean includeProParteSynonyms = true;  //SecundumForSubtree overrides default value
    private IProgressMonitor monitor;

    protected ForSubtreeConfiguratorBase(UUID subtreeUuid, IProgressMonitor monitor) {
        this.subtreeUuid = subtreeUuid;
        this.monitor = monitor;
    }

    protected ForSubtreeConfiguratorBase(UUID subtreeUuid) {
//        super(null);
        this.subtreeUuid = subtreeUuid;
    }

    protected ForSubtreeConfiguratorBase(UUID subtreeUuid, boolean includeAcceptedTaxa, boolean includeSynonyms,
            boolean includeSharedTaxa, IProgressMonitor monitor) {
        super();
        this.subtreeUuid = subtreeUuid;
        this.includeAcceptedTaxa = includeAcceptedTaxa;
        this.includeSynonyms = includeSynonyms;
        this.includeSharedTaxa = includeSharedTaxa;
        this.monitor = monitor;
    }

// ************************** GETTER / SETTER ********************************/

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

    public boolean isIncludeMisapplications() {
        return includeMisapplications;
    }
    public void setIncludeMisapplications(boolean includeMisapplications) {
        this.includeMisapplications = includeMisapplications;
    }

    public boolean isIncludeProParteSynonyms() {
        return includeProParteSynonyms;
    }
    public void setIncludeProParteSynonyms(boolean includeProParteSynonyms) {
        this.includeProParteSynonyms = includeProParteSynonyms;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
}
