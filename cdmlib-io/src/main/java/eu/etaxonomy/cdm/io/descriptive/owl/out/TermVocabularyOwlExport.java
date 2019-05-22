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
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component
public class TermVocabularyOwlExport extends CdmExportBase<StructureTreeOwlExportConfigurator, StructureTreeOwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    @Override
    protected boolean doCheck(StructureTreeOwlExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(StructureTreeOwlExportState state) {
        TransactionStatus txStatus = startTransaction(true);

        // export term vocabularies
        state.getConfig().getVocabularyUuids().forEach(vocUuid->exportVocabulary(vocUuid, state));

        commitTransaction(txStatus);
    }

    private void exportVocabulary(UUID vocUuid, StructureTreeOwlExportState state){

        TermVocabulary vocabulary = getVocabularyService().load(vocUuid);

        Resource vocabularyResource = OwlExportUtil.createVocabularyResource(vocabulary, state);

        getVocabularyService().getTopLevelTerms(vocUuid).forEach(termDto->addTerm(termDto, vocabularyResource, state));

    }

    private void addTerm(TermDto termDto, Resource vocabularyResource, StructureTreeOwlExportState state) {
        DefinedTerm term = HibernateProxyHelper.deproxy(getTermService().load(termDto.getUuid()), DefinedTerm.class);
        addTerm(term, vocabularyResource, state);
    }

    private void addTerm(DefinedTerm term, Resource vocabularyResource, StructureTreeOwlExportState state) {
        Resource termResource = OwlExportUtil.createTermResource(term, state);

        vocabularyResource.addProperty(StructureTreeOwlExportState.propHasTerm, termResource);
        Set<DefinedTerm> generalizationOf = term.getGeneralizationOf();
        for (DefinedTerm kindOf : generalizationOf) {
            termResource.addProperty(StructureTreeOwlExportState.propTermIsGeneralizationOf, OwlExportUtil.createTermResource(kindOf, state));
        }
        Set<DefinedTerm> includes = term.getIncludes();
        for (DefinedTerm partOf : includes) {
            termResource.addProperty(StructureTreeOwlExportState.propTermIncludes, OwlExportUtil.createTermResource(partOf, state));
        }
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
