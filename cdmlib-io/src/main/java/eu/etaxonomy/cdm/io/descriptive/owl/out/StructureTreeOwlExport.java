/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.term.FeatureTree;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component("structureTreeOwlExport")
public class StructureTreeOwlExport extends CdmExportBase<StructureTreeOwlExportConfigurator, StructureTreeOwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    @Override
    protected boolean doCheck(StructureTreeOwlExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(StructureTreeOwlExportState state) {
        TransactionStatus txStatus = startTransaction(true);

        // export feature trees
        List<UUID> featureTreeUuids = state.getConfig().getFeatureTreeUuids();
        for (UUID uuid : featureTreeUuids) {
            if(state.getConfig().getProgressMonitor().isCanceled()){
                break;
            }
            state.getConfig().getProgressMonitor().worked(1);
            FeatureTree featureTree = getFeatureTreeService().load(uuid);
            Resource featureTreeResource = OwlExportUtil.createFeatureTreeResource(featureTree, this, state);
        }

        // write export data to file
        exportStream = new ByteArrayOutputStream();
        state.getModel().write(exportStream);
        state.getResult().addExportData(getByteArray());

        commitTransaction(txStatus);
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
