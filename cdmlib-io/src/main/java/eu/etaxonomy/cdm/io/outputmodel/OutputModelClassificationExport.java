/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.UUID;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.ICdmExport;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * @author k.luther
 * @date 15.03.2017
 *
 */
public class OutputModelClassificationExport
            extends CdmExportBase<OutputModelConfigurator, OutputModelExportState, IExportTransformer>
            implements ICdmExport<OutputModelConfigurator, OutputModelExportState>{


    private static final long serialVersionUID = 2518643632756927053L;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(OutputModelExportState state) {
        OutputModelConfigurator config = state.getConfig();

        if (config.getClassificationUuids().isEmpty()){
            //TODO
            state.setEmptyData();
            return;
        }
        for (UUID classificationUuid : config.getClassificationUuids()){
            Classification classification = getClassificationService().find(classificationUuid);
            if (classification == null){
                String message = String.format("Classification for given classification UUID not found. No data imported for %s", classificationUuid.toString());
                state.getResult().addWarning(message);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(OutputModelExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(OutputModelExportState state) {
        return false;
    }



}
