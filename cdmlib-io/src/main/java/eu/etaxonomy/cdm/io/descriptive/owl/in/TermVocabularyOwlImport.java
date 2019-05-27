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
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 *
 * @author pplitzner
 * @since May 27, 2019
 *
 */
@Component("structureTreeOwlImport")
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
            StmtIterator termIterator = voc.listProperties(OwlUtil.propHasTerm);
            while(termIterator.hasNext()){
                createTerm(vocabulary, termIterator.next(), state.getModel(), state);
            }

            getVocabularyService().saveOrUpdate(vocabulary);

        }
    }

    private DefinedTermBase createTerm(TermVocabulary vocabulary, Statement termStatement, Model model, StructureTreeOwlImportState state) {
        Resource termResource = model.createResource(termStatement.getObject().toString());

        UUID termUuid = UUID.fromString(termResource.getProperty(OwlUtil.propUuid).getString());
        DefinedTermBase term = getTermService().load(termUuid);
        if(term==null){
            term = OwlImportUtil.createTerm(termResource, getTermService(), model, state);
            getTermService().saveOrUpdate(term);
        }
        vocabulary.addTerm(term);

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
