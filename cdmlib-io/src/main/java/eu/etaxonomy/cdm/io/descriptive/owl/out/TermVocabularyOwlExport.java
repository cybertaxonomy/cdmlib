/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component("termVocabularyOwlExport")
public class TermVocabularyOwlExport extends CdmExportBase<StructureTreeOwlExportConfigurator, StructureTreeOwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    @Override
    protected boolean doCheck(StructureTreeOwlExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(StructureTreeOwlExportState state) {
        TransactionStatus txStatus = startTransaction(true);

        IProgressMonitor progressMonitor = state.getConfig().getProgressMonitor();
        List<UUID> vocabularyUuids = state.getConfig().getVocabularyUuids();
        int totalWork = vocabularyUuids.size() + state.getConfig().getFeatureTreeUuids().size();
        progressMonitor.beginTask("Exporting terms", totalWork);

        // export term vocabularies
        for (UUID uuid : vocabularyUuids) {
            if(progressMonitor.isCanceled()){
                break;
            }
            progressMonitor.worked(1);
            TermVocabulary vocabulary = getVocabularyService().load(uuid);
            OwlExportUtil.createVocabularyResource(vocabulary, this, state);
        }

        commitTransaction(txStatus);
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
