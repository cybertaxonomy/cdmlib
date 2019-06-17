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
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

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
            exportVocabulary(uuid, state);
        }

        commitTransaction(txStatus);
    }

    private void exportVocabulary(UUID vocUuid, StructureTreeOwlExportState state){

        TermVocabulary vocabulary = getVocabularyService().load(vocUuid);

        Resource vocabularyResource = OwlExportUtil.createVocabularyResource(vocabulary, state);

        getVocabularyService().getTopLevelTerms(vocUuid).forEach(termDto->addTerm(termDto, vocabularyResource, state));

    }

    private Resource addTerm(TermDto termDto, Resource vocabularyResource, StructureTreeOwlExportState state) {
        DefinedTermBase term = getTermService().load(termDto.getUuid());
        return addTerm(term, vocabularyResource, state);
    }

    private Resource addTerm(DefinedTermBase term, Resource vocabularyResource, StructureTreeOwlExportState state) {
        Resource termResource = OwlExportUtil.createTermResource(term, state);

        vocabularyResource.addProperty(OwlUtil.propHasTerm, termResource);
        termResource.addProperty(OwlUtil.propHasVocabulary, vocabularyResource);

        // export media
        Set<Media> media = term.getMedia();
        for (Media medium : media) {
            Resource mediaResource = OwlExportUtil.createMediaResource(medium, state);
            termResource.addProperty(OwlUtil.propTermHasMedia, mediaResource);
        }

        // export includes and generalizationOf
        Set<DefinedTermBase> generalizationOf = term.getGeneralizationOf();
        for (DefinedTermBase kindOf : generalizationOf) {
            Resource kindOfResource = addTerm(kindOf, vocabularyResource, state);
            termResource.addProperty(OwlUtil.propTermIsGeneralizationOf, kindOfResource);
        }
        Set<DefinedTermBase> includes = term.getIncludes();
        for (DefinedTermBase partOf : includes) {
            Resource partOfResource = addTerm(partOf, vocabularyResource, state);
            termResource.addProperty(OwlUtil.propTermIncludes, partOfResource);
        }

        return termResource;
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
