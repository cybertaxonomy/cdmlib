/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;

/**
 * Configurator for the setPublishForSubtree operation.
 *
 * @author a.mueller
 * @since 13.09.2017
 */
public class PublishForSubtreeConfigurator
            extends ForSubtreeConfiguratorBase<PublishForSubtreeConfigurator>{

    private static final long serialVersionUID = 1202667588493272030L;

    private boolean publish = false;
    private boolean includeMisapplications = true;
    private boolean includeProParteSynonyms = true;
    //TODO move to super class
    private boolean includeHybrids = true;




    //TODO UUIDs ??
    private Set<TaxonRelationshipType> includedRelationTypes = new HashSet<>();


    public static PublishForSubtreeConfigurator NewInstance(UUID subtreeUuid, boolean publish,
            IProgressMonitor monitor) {
        PublishForSubtreeConfigurator result = new PublishForSubtreeConfigurator(subtreeUuid, publish, monitor);
        return result;
    }

    public static PublishForSubtreeConfigurator NewInstance(UUID subtreeUuid, boolean publish,
            boolean includeAcceptedTaxa, boolean includeSynonyms,
            boolean includeSharedTaxa, IProgressMonitor monitor) {
        PublishForSubtreeConfigurator result = new PublishForSubtreeConfigurator(subtreeUuid, publish, includeAcceptedTaxa,
                includeSynonyms, includeSharedTaxa, monitor);
        return result;
    }

// ****************************** CONSTRUCTOR ******************************/

    /**
     * @param subtreeUuid
     * @param newSecundum
     */
    private PublishForSubtreeConfigurator(UUID subtreeUuid, boolean publish, IProgressMonitor monitor) {
        super(subtreeUuid, monitor);
        this.publish = publish;
    }

    private PublishForSubtreeConfigurator(UUID subtreeUuid, boolean publish, boolean includeAcceptedTaxa, boolean includeSynonyms,
            boolean includeSharedTaxa, IProgressMonitor monitor) {
        super(subtreeUuid, includeAcceptedTaxa, includeSynonyms, includeSharedTaxa, monitor);
        this.publish = publish;
    }

// ******************************* GETTER / SETTER  **************************/

    public boolean isPublish() {
        return publish;
    }

    public PublishForSubtreeConfigurator setPublish(boolean publish) {
        this.publish = publish;
        return this;
    }

    public Set<TaxonRelationshipType> getIncludedRelationTypes() {
        return includedRelationTypes;
    }
    public PublishForSubtreeConfigurator setIncludedRelationTypes(Set<TaxonRelationshipType> includedRelationTypes) {
        this.includedRelationTypes = includedRelationTypes;
        return this;
    }

    public boolean isIncludeMisapplications() {
        return includeMisapplications;
    }
    public PublishForSubtreeConfigurator setIncludeMisapplications(boolean includeMisapplications) {
        this.includeMisapplications = includeMisapplications;
        return this;
    }

    public boolean isIncludeProParteSynonyms() {
        return includeProParteSynonyms;
    }
    public PublishForSubtreeConfigurator setIncludeProParteSynonyms(boolean includeProParteSynonyms) {
        this.includeProParteSynonyms = includeProParteSynonyms;
        return this;
    }

//    public boolean isIncludeHybrids() {
//        return includeHybrids;
//    }
//
//    public PublishForSubtreeConfigurator setIncludeHybrids(boolean includeHybrids) {
//        this.includeHybrids = includeHybrids;
//        return this;
//    }

}
