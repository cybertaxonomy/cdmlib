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
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component
public class StructureTreeOwlExport extends CdmExportBase<StructureTreeOwlExportConfigurator, StructureTreeOwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    private Property propHasSubStructure;
    private Property propHasRepresentation;
    private Property propHasVocabulary;
    private Property propHasRootNode;
    private Property propUuid;
    private Property propUri;
    private Property propLabel;
    private Property propLabelAbbrev;
    private Property propLanguage;
    private Property propLanguageUuid;
    private Property propIsA;
    private Property propType;
    private Property propDescription;

    @Override
    protected boolean doCheck(StructureTreeOwlExportState state) {
        return false;
    }

    @Override
    protected void doInvoke(StructureTreeOwlExportState state) {
        TransactionStatus txStatus = startTransaction(true);

        // create model properties
        Model model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRepresentation = model.createProperty(OwlConstants.PROPERTY_HAS_REPRESENTATION);
        propHasVocabulary = model.createProperty(OwlConstants.PROPERTY_HAS_VOCABULARY);
        propHasRootNode = model.createProperty(OwlConstants.PROPERTY_HAS_ROOT_NODE);
        propUuid = model.createProperty(OwlConstants.PROPERTY_UUID);
        propUri = model.createProperty(OwlConstants.PROPERTY_URI);
        propLabel = model.createProperty(OwlConstants.PROPERTY_LABEL);
        propLabelAbbrev = model.createProperty(OwlConstants.PROPERTY_LABEL_ABBREV);
        propLanguage = model.createProperty(OwlConstants.PROPERTY_LANGUAGE);
        propLanguageUuid = model.createProperty(OwlConstants.PROPERTY_LANGUAGE_UUID);
        propIsA = model.createProperty(OwlConstants.PROPERTY_IS_A);
        propType = model.createProperty(OwlConstants.PROPERTY_TYPE);
        propDescription = model.createProperty(OwlConstants.PROPERTY_DESCRIPTION);

        // export feature trees
        state.getConfig().getFeatureTrees().forEach(tree->exportTree(tree, model));

        // write export data to file
        exportStream = new ByteArrayOutputStream();
        model.write(exportStream);
        state.getResult().addExportData(getByteArray());

        commitTransaction(txStatus);
    }

    private void exportTree(FeatureTree featureTree, Model model){

        FeatureNode rootNode = featureTree.getRoot();

        Resource resourceRootNode = model.createResource(OwlConstants.RESOURCE_NODE + rootNode.getUuid().toString())
                .addProperty(propIsA, OwlConstants.NODE)
                .addProperty(propUuid, rootNode.getUuid().toString())
                .addProperty(propIsA, OwlConstants.NODE)
                ;

        model.createResource(OwlConstants.RESOURCE_FEATURE_TREE+featureTree.getUuid().toString())
                .addProperty(propUuid, featureTree.getUuid().toString())
                .addProperty(propLabel, featureTree.getTitleCache())
                .addProperty(propHasRootNode, resourceRootNode)
                .addProperty(propIsA, OwlConstants.TREE)
                .addProperty(propType, featureTree.getTermType().getKey())
                ;

        addChildNode(rootNode, resourceRootNode, model);
    }

    private void addChildNode(FeatureNode node, Resource resourceNode, Model model){
        List<FeatureNode> childNodes = node.getChildNodes();
        for (FeatureNode child : childNodes) {
            DefinedTermBase term = child.getTerm();
            // create node resource with term
            Resource termResource = model.createResource(OwlConstants.RESOURCE_NODE+term.getUuid().toString())
                    .addProperty(propUuid, term.getUuid().toString())
                    .addProperty(propIsA, OwlConstants.NODE)
                    .addProperty(propType, term.getTermType().getKey())
                    ;
            if(term.getUri()!=null){
                termResource.addProperty(propUri, term.getUri().toString());
            }
            // add to parent node
            resourceNode.addProperty(propHasSubStructure, termResource);

            // add term representations
            List<Resource> termRepresentationResources = createRepresentationResources(term, model);
            termRepresentationResources.forEach(rep->termResource.addProperty(propHasRepresentation, rep));

            // create vocabulary resource
            TermVocabulary vocabulary = term.getVocabulary();
            Resource vocabularyResource = model.createResource(OwlConstants.RESOURCE_TERM_VOCABULARY+vocabulary.getUuid())
                    .addProperty(propUuid, vocabulary.getUuid().toString())
                    .addProperty(propType, vocabulary.getTermType().getKey())
                    ;
            if(vocabulary.getUri()!=null){
                vocabularyResource.addProperty(propUri, vocabulary.getUri().toString());
            }
            // add term representations
            List<Resource> vocabularyRepresentationResources = createRepresentationResources(vocabulary, model);
            vocabularyRepresentationResources.forEach(rep->vocabularyResource.addProperty(propHasRepresentation, rep));
            // add vocabulary to term
            termResource.addProperty(propHasVocabulary, vocabularyResource);

            addChildNode(child, termResource, model);
        }
    }

    private List<Resource> createRepresentationResources(TermBase termBase, Model model){
        List<Resource> representations = new ArrayList<>();
        for (Representation representation : termBase.getRepresentations()) {
            Resource representationResource = model.createResource(OwlConstants.RESOURCE_REPRESENTATION+representation.getUuid())
            .addProperty(propLabel, representation.getLabel())
            .addProperty(propLanguage, representation.getLanguage().getTitleCache())
            .addProperty(propLanguageUuid, representation.getLanguage().getUuid().toString())
            ;
            if(representation.getDescription()!=null){
                representationResource.addProperty(propDescription, representation.getDescription());
            }
            if(representation.getAbbreviatedLabel()!=null){
                representationResource.addProperty(propLabelAbbrev, representation.getAbbreviatedLabel());
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
