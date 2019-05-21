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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
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

        // create vocabulary resource
        Resource vocabularyResource = state.getModel().createResource(OwlConstants.RESOURCE_TERM_VOCABULARY+vocabulary.getUuid())
                .addProperty(StructureTreeOwlExportState.propUuid, vocabulary.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propType, vocabulary.getTermType().getKey())
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.VOCABULARY)
                ;
        if(vocabulary.getUri()!=null){
            vocabularyResource.addProperty(StructureTreeOwlExportState.propUri, vocabulary.getUri().toString());
        }
        // add vocabulary representations
        List<Resource> vocabularyRepresentationResources = createRepresentationResources(vocabulary, state);
        vocabularyRepresentationResources.forEach(rep->vocabularyResource.addProperty(StructureTreeOwlExportState.propHasRepresentation, rep));

        getVocabularyService().getTopLevelTerms(vocUuid).forEach(termDto->addTerm(termDto, vocabularyResource, state));

    }

    private void addTerm(TermDto termDto, Resource vocabularyResource, StructureTreeOwlExportState state) {
        DefinedTerm term = HibernateProxyHelper.deproxy(getTermService().load(termDto.getUuid()), DefinedTerm.class);
        addTerm(term, vocabularyResource, state);
    }

    private void addTerm(DefinedTerm term, Resource vocabularyResource, StructureTreeOwlExportState state) {
        // create term resource
        Resource termResource = createTermResource(term, state);

        vocabularyResource.addProperty(StructureTreeOwlExportState.propHasTerm, termResource);
        Set<DefinedTerm> generalizationOf = term.getGeneralizationOf();
        for (DefinedTerm kindOf : generalizationOf) {
            termResource.addProperty(StructureTreeOwlExportState.propTermIsGeneralizationOf, createTermResource(kindOf, state));
        }
        Set<DefinedTerm> includes = term.getIncludes();
        for (DefinedTerm partOf : includes) {
            termResource.addProperty(StructureTreeOwlExportState.propTermIncludes, createTermResource(partOf, state));
        }
    }

    /**
     * @param term
     * @param state
     * @return
     */
    protected Resource createTermResource(DefinedTerm term, StructureTreeOwlExportState state) {
        Resource termResource = state.getModel().createResource(OwlConstants.RESOURCE_TERM+term.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propUuid, term.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.TERM)
                .addProperty(StructureTreeOwlExportState.propType, term.getTermType().getKey())
                ;
        if(term.getUri()!=null){
            termResource.addProperty(StructureTreeOwlExportState.propUri, term.getUri().toString());
        }
        // add term representations
        List<Resource> termRepresentationResources = createRepresentationResources(term, state);
        termRepresentationResources.forEach(rep->termResource.addProperty(StructureTreeOwlExportState.propHasRepresentation, rep));
        return termResource;
    }


    private List<Resource> createRepresentationResources(TermBase termBase, StructureTreeOwlExportState state){
        List<Resource> representations = new ArrayList<>();
        for (Representation representation : termBase.getRepresentations()) {
            Resource representationResource = state.getModel().createResource(OwlConstants.RESOURCE_REPRESENTATION+representation.getUuid())
            .addProperty(StructureTreeOwlExportState.propLabel, representation.getLabel())
            .addProperty(StructureTreeOwlExportState.propLanguage, representation.getLanguage().getTitleCache())
            .addProperty(StructureTreeOwlExportState.propLanguageUuid, representation.getLanguage().getUuid().toString())
            ;
            if(representation.getDescription()!=null){
                representationResource.addProperty(StructureTreeOwlExportState.propDescription, representation.getDescription());
            }
            if(representation.getAbbreviatedLabel()!=null){
                representationResource.addProperty(StructureTreeOwlExportState.propLabelAbbrev, representation.getAbbreviatedLabel());
            }
            representations.add(representationResource);
        }
        return representations;
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
