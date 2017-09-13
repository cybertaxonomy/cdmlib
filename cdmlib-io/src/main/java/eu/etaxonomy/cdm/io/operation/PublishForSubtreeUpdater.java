/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.operation.config.PublishForSubtreeConfigurator;

/**
 * @author a.mueller
 * @date 13.09.2017
 *
 */
@Component
public class PublishForSubtreeUpdater
        extends CdmImportBase<PublishForSubtreeConfigurator, DefaultImportState<PublishForSubtreeConfigurator>>  {

    private static final long serialVersionUID = -8472720162647029724L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PublishForSubtreeUpdater.class);


    /**
     * {@inheritDoc}
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(DefaultImportState<PublishForSubtreeConfigurator> state) {
        PublishForSubtreeConfigurator config = state.getConfig();
        state.getConfig().getProgressMonitor().beginTask("Update pulish flag References ", 100);

        UpdateResult result = getTaxonNodeService().setSecundumForSubtree(config.getSubtreeUuid(),  config.getNewSecundum(), config.isIncludeAcceptedTaxa(), config.isIncludeSynonyms(), config.isOverwriteExistingAccepted(), config.isOverwriteExistingSynonyms(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail(), config.getProgressMonitor());

        return;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(DefaultImportState<PublishForSubtreeConfigurator> state) {
        return true;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(DefaultImportState<PublishForSubtreeConfigurator> state) {
        return false;
    }

}


