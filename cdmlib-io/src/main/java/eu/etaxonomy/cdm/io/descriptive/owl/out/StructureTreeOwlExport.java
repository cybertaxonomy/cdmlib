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
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.term.FeatureNode;
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
            exportTree(uuid, state);
        }

        // write export data to file
        exportStream = new ByteArrayOutputStream();
        state.getModel().write(exportStream);
        state.getResult().addExportData(getByteArray());

        commitTransaction(txStatus);
    }

    private void exportTree(UUID featureTreeUuid, StructureTreeOwlExportState state){
        FeatureTree featureTree = getFeatureTreeService().load(featureTreeUuid);

        FeatureNode rootNode = featureTree.getRoot();

        Resource featureTreeResource = OwlExportUtil.createFeatureTreeResource(featureTree, state);

        Resource resourceRootNode = OwlExportUtil.createNodeResource(state, rootNode);
        featureTreeResource.addProperty(OwlUtil.propHasRootNode, resourceRootNode);

        addChildNode(rootNode, resourceRootNode, state);
    }

    private void addChildNode(FeatureNode parentNode, Resource parentResourceNode, StructureTreeOwlExportState state){
        List<FeatureNode> childNodes = parentNode.getChildNodes();
        for (FeatureNode child : childNodes) {
            // create node resource with term
            Resource nodeResource = OwlExportUtil.createNodeResource(state, child);
            // add term to node
            Resource termResource = OwlExportUtil.createTermResource(child.getTerm(), state);
            nodeResource.addProperty(OwlUtil.propHasTerm, termResource);

            // export media
            Set<Media> media = child.getTerm().getMedia();
            for (Media medium : media) {
                Resource mediaResource = OwlExportUtil.createMediaResource(medium, state);
                termResource.addProperty(OwlUtil.propTermHasMedia, mediaResource);
            }

            // add node to parent node
            parentResourceNode.addProperty(OwlUtil.propHasSubStructure, nodeResource);

            // create vocabulary resource
            Resource vocabularyResource = OwlExportUtil.createVocabularyResource(child.getTerm().getVocabulary(), state);
            // add vocabulary to term
            termResource.addProperty(OwlUtil.propHasVocabulary, vocabularyResource);
            // add term to vocabulary
            vocabularyResource.addProperty(OwlUtil.propHasTerm, termResource);

            addChildNode(child, nodeResource, state);
        }
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlExportState state) {
        return false;
    }

}
