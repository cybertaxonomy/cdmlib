/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.in;

import java.net.URI;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 *
 * @author pplitzner
 * @since May 27, 2019
 *
 */
@Component("termVocabularyOwlImport")
public class TermVocabularyOwlImport extends CdmImportBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TermVocabularyOwlImport.class);


    @Override
    protected boolean doCheck(StructureTreeOwlImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }

    @Override
    public void doInvoke(StructureTreeOwlImportState state) {
        URI source = state.getConfig().getSource();

        state.getModel().read(source.toString());

        //calculate total work
        int nodeCount = state.getModel().listResourcesWithProperty(OwlUtil.propIsA, OwlUtil.NODE).toSet().size();
        int termCount = state.getModel().listResourcesWithProperty(OwlUtil.propIsA, OwlUtil.TERM).toSet().size();
        int totalWork = nodeCount+termCount;
        IProgressMonitor progressMonitor = state.getConfig().getProgressMonitor();
        progressMonitor.beginTask("Importing terms", totalWork);

        //get all vocabularies
        ResIterator iterator = state.getModel().listResourcesWithProperty(OwlUtil.propIsA, OwlUtil.VOCABULARY);
        while(iterator.hasNext()){
            Resource voc = iterator.next();
            String type = voc.getProperty(OwlUtil.propType).getString();

            UUID vocUuid = UUID.fromString(voc.getProperty(OwlUtil.propUuid).getString());
            TermVocabulary vocabulary = getVocabularyService().load(vocUuid);
            if(vocabulary==null){
                vocabulary = OwlImportUtil.createVocabulary(voc, getTermService(), state.getModel(), state);
                getVocabularyService().save(vocabulary);
            }

            // import terms
            Set<Statement> terms = voc.listProperties(OwlUtil.propHasTerm).toSet();
            for (Statement statement : terms) {
                if(progressMonitor.isCanceled()){
                    break;
                }
                createTerm(vocabulary, statement, state.getModel(), state);
                progressMonitor.worked(1);
            }

            getVocabularyService().saveOrUpdate(vocabulary);

        }
    }

    private DefinedTermBase createTerm(TermVocabulary vocabulary, Statement termStatement, Model model, StructureTreeOwlImportState state) {
        Resource termResource = model.createResource(termStatement.getObject().toString());

        UUID termUuid = UUID.fromString(termResource.getProperty(OwlUtil.propUuid).getString());
        DefinedTermBase term = getTermService().load(termUuid);
        if(term!=null){
            return term;
        }

        term = OwlImportUtil.createTerm(termResource, getTermService(), model, state);
        getTermService().saveOrUpdate(term);
        vocabulary.addTerm(term); // only add term if it does not already exist

        //check media
        StmtIterator mediaIterator = termResource.listProperties(OwlUtil.propTermHasMedia);
        while(mediaIterator.hasNext()){
            Resource mediaResource = model.createResource(mediaIterator.next().getObject().toString());
            Media media = OwlImportUtil.createMedia(mediaResource, state);
            term.addMedia(media);
        }

        // check includes
        StmtIterator includesIterator = termResource.listProperties(OwlUtil.propTermIncludes);
        while(includesIterator.hasNext()){
            DefinedTermBase includeTerm = createTerm(vocabulary, includesIterator.next(), model, state);
            term.addIncludes(includeTerm);
        }
        // check generalization
        StmtIterator generalizationOfIterator = termResource.listProperties(OwlUtil.propTermIsGeneralizationOf);
        while(generalizationOfIterator.hasNext()){
            DefinedTermBase generalizationOfTerm = createTerm(vocabulary, generalizationOfIterator.next(), model, state);
            term.addGeneralizationOf(generalizationOfTerm);
        }
        return term;
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlImportState state) {
        return false;
    }

}
