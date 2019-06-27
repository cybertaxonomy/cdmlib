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

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
@Component("structureTreeOwlImport")
public class StructureTreeOwlImport extends CdmImportBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StructureTreeOwlImport.class);


    @Override
    protected boolean doCheck(StructureTreeOwlImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }

    @Override
    public void doInvoke(StructureTreeOwlImportState state) {
        URI source = state.getConfig().getSource();

        state.getModel().read(source.toString());

        //get all trees
        ResIterator iterator = state.getModel().listResourcesWithProperty(OwlUtil.propHasRootNode);
        while(iterator.hasNext()){
            Resource tree = iterator.next();
            String type = tree.getProperty(OwlUtil.propType).getString();
            FeatureTree featureTree = FeatureTree.NewInstance(TermType.getByKey(type));
            featureTree.setTitleCache(tree.getProperty(OwlUtil.propLabel).getString(), true);

            Resource rootNode = tree.getProperty(OwlUtil.propHasRootNode).getResource();
            rootNode.listProperties(OwlUtil.propHasSubStructure).forEachRemaining(prop->createNode(featureTree.getRoot(), prop, featureTree.getTitleCache(), state.getModel(), state));

            getFeatureTreeService().save(featureTree);
        }
    }

    private void createNode(FeatureNode parent, Statement nodeStatement, String treeLabel, Model model, StructureTreeOwlImportState state) {
        if(state.getConfig().getProgressMonitor().isCanceled()){
            return;
        }
        Resource nodeResource = model.createResource(nodeStatement.getObject().toString());

        Resource termResource = nodeResource.getPropertyResourceValue(OwlUtil.propHasTerm);

        //import term vocabulary
        Resource vocabularyResource = termResource.getPropertyResourceValue(OwlUtil.propHasVocabulary);
        UUID vocUuid = UUID.fromString(vocabularyResource.getProperty(OwlUtil.propUuid).getString());
        TermVocabulary vocabulary = getVocabularyService().load(vocUuid);
        if(vocabulary==null){
            vocabulary = OwlImportUtil.createVocabulary(vocabularyResource, getTermService(), model, state);
            vocabulary = getVocabularyService().save(vocabulary);
        }

        // import term
        UUID termUuid = UUID.fromString(termResource.getProperty(OwlUtil.propUuid).getString());
        DefinedTermBase term = getTermService().find(termUuid);
        if(term==null){
            term = OwlImportUtil.createTerm(termResource, getTermService(), model, state);
            getTermService().save(term);
            vocabulary.addTerm(term); // only add term if it does not already exist
        }

        getVocabularyService().saveOrUpdate(vocabulary);

        FeatureNode<?> childNode = parent.addChild(term);

        state.getConfig().getProgressMonitor().worked(1);

        nodeResource.listProperties(OwlUtil.propHasSubStructure).forEachRemaining(prop->createNode(childNode, prop, treeLabel, model, state));
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlImportState state) {
        return false;
    }

}
