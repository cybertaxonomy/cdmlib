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

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component
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
        state.getConfig().getFeatureTrees().forEach(tree->exportTree(tree, state));

        // write export data to file
        exportStream = new ByteArrayOutputStream();
        state.getModel().write(exportStream);
        state.getResult().addExportData(getByteArray());

        commitTransaction(txStatus);
    }

    private void exportTree(FeatureTree featureTree, StructureTreeOwlExportState state){
        FeatureNode rootNode = featureTree.getRoot();

        Resource resourceRootNode = state.getModel().createResource(OwlConstants.RESOURCE_NODE + rootNode.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.NODE)
                .addProperty(StructureTreeOwlExportState.propUuid, rootNode.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.NODE)
                ;

        state.getModel().createResource(OwlConstants.RESOURCE_FEATURE_TREE+featureTree.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propUuid, featureTree.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propLabel, featureTree.getTitleCache())
                .addProperty(StructureTreeOwlExportState.propHasRootNode, resourceRootNode)
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.TREE)
                .addProperty(StructureTreeOwlExportState.propType, featureTree.getTermType().getKey())
                ;

        addChildNode(rootNode, resourceRootNode, state);
    }

    private void addChildNode(FeatureNode node, Resource resourceNode, StructureTreeOwlExportState state){
        List<FeatureNode> childNodes = node.getChildNodes();
        for (FeatureNode child : childNodes) {
            // create node resource with term
            Resource nodeResource = state.getModel().createResource(OwlConstants.RESOURCE_NODE+child.getUuid().toString())
                    .addProperty(StructureTreeOwlExportState.propUuid, child.getUuid().toString())
                    .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.NODE)
                    ;
            // add term to node
            Resource termResource = TermVocabularyOwlExport.createTermResource(child.getTerm(), state);
            resourceNode.addProperty(StructureTreeOwlExportState.propHasTerm, termResource);
            // add node to parent node
            resourceNode.addProperty(StructureTreeOwlExportState.propHasSubStructure, nodeResource);

            // create vocabulary resource
            Resource vocabularyResource = TermVocabularyOwlExport.createVocabularyResource(child.getTerm().getVocabulary(), state);
            // add vocabulary to term
            nodeResource.addProperty(StructureTreeOwlExportState.propHasVocabulary, vocabularyResource);

            addChildNode(child, nodeResource, state);
        }
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
