/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.UpdateResult;

/**
 * @author k.luther
 * @date 14.03.2017
 *
 */
@Component
public class SecundumUpdater extends CdmImportBase<SetSecundumForSubtreeConfigurator, DefaultImportState<SetSecundumForSubtreeConfigurator>>  {
    /**
     *
     */
    private static final long serialVersionUID = 6788425152444747546L;

    private static final Logger logger = Logger.getLogger(SecundumUpdater.class);


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
    protected void doInvoke(DefaultImportState<SetSecundumForSubtreeConfigurator> state) {
        SetSecundumForSubtreeConfigurator config = state.getConfig();
        state.getConfig().getProgressMonitor().beginTask("Update Secundum References ", 100);

        UpdateResult result = getTaxonNodeService().setSecundumForSubtree(config.getSubtreeUuid(),  config.getNewSecundum(), config.isIncludeAcceptedTaxa(), config.isIncludeSynonyms(), config.isOverwriteExistingAccepted(), config.isOverwriteExistingSynonyms(), config.isIncludeSharedTaxa(), config.isEmptySecundumDetail(), config.getProgressMonitor());

        return;

    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(DefaultImportState<SetSecundumForSubtreeConfigurator> state) {
        // TODO Auto-generated method stub
        return true;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(DefaultImportState<SetSecundumForSubtreeConfigurator> state) {
        // TODO Auto-generated method stub
        return false;
    }



}


