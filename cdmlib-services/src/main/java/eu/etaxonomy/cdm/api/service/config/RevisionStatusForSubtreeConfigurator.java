/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.common.RevisionStatusInfo;

/**
 * Configurator for the setRevisionStatusForSubtree operation.
 *
 * Synonyms are not supported because {@link RevisionStatusInfo} exists only on
 * {@link eu.etaxonomy.cdm.model.taxon.Taxon}.
 *
 * See #11040
 *
 * @author muellera
 * @since 07.10.2026
 */
public class RevisionStatusForSubtreeConfigurator
            extends ForSubtreeConfiguratorBase{

    private static final long serialVersionUID = -3184167284910628451L;

    private RevisionStatusInfo revisionStatus;
    private boolean overwriteExisting = true;
    private boolean includeHybrids = true;

    public static RevisionStatusForSubtreeConfigurator NewInstance(UUID subtreeUuid,
            RevisionStatusInfo revisionStatus, IProgressMonitor monitor) {
        return new RevisionStatusForSubtreeConfigurator(subtreeUuid, revisionStatus, monitor);
    }

    public static RevisionStatusForSubtreeConfigurator NewInstance(UUID subtreeUuid,
            RevisionStatusInfo revisionStatus, boolean includeAcceptedTaxa,
            boolean includeSharedTaxa, IProgressMonitor monitor) {
        return new RevisionStatusForSubtreeConfigurator(subtreeUuid, revisionStatus,
                includeAcceptedTaxa, includeSharedTaxa, monitor);
    }

// ****************************** CONSTRUCTOR ******************************/

    private RevisionStatusForSubtreeConfigurator(UUID subtreeUuid,
            RevisionStatusInfo revisionStatus, IProgressMonitor monitor) {
        super(subtreeUuid, monitor);
        this.revisionStatus = revisionStatus;
        setIncludeSynonyms(false);
        setIncludeMisapplications(false);
        setIncludeProParteSynonyms(false);
    }

    private RevisionStatusForSubtreeConfigurator(UUID subtreeUuid,
            RevisionStatusInfo revisionStatus, boolean includeAcceptedTaxa,
            boolean includeSharedTaxa, IProgressMonitor monitor) {
        super(subtreeUuid, includeAcceptedTaxa, false, includeSharedTaxa, monitor);
        this.revisionStatus = revisionStatus;
        setIncludeMisapplications(false);
        setIncludeProParteSynonyms(false);
    }

// ******************************* GETTER / SETTER  **************************/

    public RevisionStatusInfo getRevisionStatus() {
        return revisionStatus;
    }
    public void setRevisionStatus(RevisionStatusInfo revisionStatus) {
        this.revisionStatus = revisionStatus;
    }

    public boolean isOverwriteExisting() {
        return overwriteExisting;
    }
    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    public boolean isIncludeHybrids() {
        return includeHybrids;
    }
    public void setIncludeHybrids(boolean includeHybrids) {
        this.includeHybrids = includeHybrids;
    }
}
