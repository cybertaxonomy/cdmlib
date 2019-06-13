/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since May 22, 2019
 *
 */
public class OwlExportUtil {

    static Resource createVocabularyResource(TermVocabulary vocabulary, StructureTreeOwlExportState state) {
        // create vocabulary resource
        Resource vocabularyResource = state.getModel().createResource(OwlUtil.RESOURCE_TERM_VOCABULARY+vocabulary.getUuid())
                .addProperty(OwlUtil.propUuid, vocabulary.getUuid().toString())
                .addProperty(OwlUtil.propType, vocabulary.getTermType().getKey())
                .addProperty(OwlUtil.propIsA, OwlUtil.VOCABULARY)
                ;
        if(vocabulary.getUri()!=null){
            vocabularyResource.addProperty(OwlUtil.propUri, vocabulary.getUri().toString());
        }
        // add vocabulary representations
        List<Resource> vocabularyRepresentationResources = OwlExportUtil.createRepresentationResources(vocabulary, state);
        vocabularyRepresentationResources.forEach(rep->vocabularyResource.addProperty(OwlUtil.propHasRepresentation, rep));
        return vocabularyResource;
    }

    static List<Resource> createRepresentationResources(TermBase termBase, StructureTreeOwlExportState state){
        List<Resource> representations = new ArrayList<>();
        for (Representation representation : termBase.getRepresentations()) {
            Resource representationResource = state.getModel().createResource(OwlUtil.RESOURCE_REPRESENTATION+representation.getUuid())
            .addProperty(OwlUtil.propLabel, representation.getLabel())
            .addProperty(OwlUtil.propLanguage, representation.getLanguage().getTitleCache())
            .addProperty(OwlUtil.propLanguageUuid, representation.getLanguage().getUuid().toString())
            ;
            if(representation.getDescription()!=null){
                representationResource.addProperty(OwlUtil.propDescription, representation.getDescription());
            }
            if(representation.getAbbreviatedLabel()!=null){
                representationResource.addProperty(OwlUtil.propLabelAbbrev, representation.getAbbreviatedLabel());
            }
            representations.add(representationResource);
        }
        return representations;
    }

    static Resource createTermResource(DefinedTermBase term, StructureTreeOwlExportState state) {
        Resource termResource = state.getModel().createResource(OwlUtil.RESOURCE_TERM+term.getUuid().toString())
                .addProperty(OwlUtil.propUuid, term.getUuid().toString())
                .addProperty(OwlUtil.propIsA, OwlUtil.TERM)
                .addProperty(OwlUtil.propType, term.getTermType().getKey())
                ;
        if(term.getUri()!=null){
            termResource.addProperty(OwlUtil.propUri, term.getUri().toString());
        }
        if(CdmUtils.isNotBlank(term.getSymbol())){
            termResource.addProperty(OwlUtil.propTermSymbol, term.getSymbol());
        }
        if(CdmUtils.isNotBlank(term.getSymbol2())){
            termResource.addProperty(OwlUtil.propTermSymbol2, term.getSymbol2());
        }
        if(CdmUtils.isNotBlank(term.getIdInVocabulary())){
            termResource.addProperty(OwlUtil.propTermIdInVocabulary, term.getIdInVocabulary());
        }
        // add term representations
        List<Resource> termRepresentationResources = createRepresentationResources(term, state);
        termRepresentationResources.forEach(rep->termResource.addProperty(OwlUtil.propHasRepresentation, rep));
        return termResource;
    }

    static Resource createFeatureTreeResource(FeatureTree featureTree, StructureTreeOwlExportState state) {
        Resource featureTreeResource = state.getModel().createResource(OwlUtil.RESOURCE_FEATURE_TREE+featureTree.getUuid().toString())
                .addProperty(OwlUtil.propUuid, featureTree.getUuid().toString())
                .addProperty(OwlUtil.propLabel, featureTree.getTitleCache())
                .addProperty(OwlUtil.propIsA, OwlUtil.TREE)
                .addProperty(OwlUtil.propType, featureTree.getTermType().getKey())
                ;
        return featureTreeResource;
    }

    static Resource createNodeResource(StructureTreeOwlExportState state, FeatureNode node) {
        Resource resourceRootNode = state.getModel().createResource(OwlUtil.RESOURCE_NODE + node.getUuid().toString())
                .addProperty(OwlUtil.propIsA, OwlUtil.NODE)
                .addProperty(OwlUtil.propUuid, node.getUuid().toString())
                ;
        return resourceRootNode;
    }

}
