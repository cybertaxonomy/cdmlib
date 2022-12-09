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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureState;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author pplitzner
 * @since May 22, 2019
 */
public class OwlExportUtil {

    static Resource createVocabularyResource(TermVocabulary<?> vocabulary, ICdmRepository repo, StructureTreeOwlExportState state) {
        String vocabularyResourceUri = getVocabularyResourceUri(vocabulary, state);
        //check if vocabulary exists
        if(state.getModel().containsResource(ResourceFactory.createResource(vocabularyResourceUri))){
            return state.getModel().createResource(vocabularyResourceUri);
        }
        // create vocabulary resource
        Resource vocabularyResource = state.getModel().createResource(vocabularyResourceUri)
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

        // add terms
        Collection<TermDto> topLevelTerms = repo.getVocabularyService().getTopLevelTerms(vocabulary.getUuid());
        for (TermDto termDto : topLevelTerms) {
            if(state.getConfig().getProgressMonitor().isCanceled()){
                break;
            }
            addTopLevelTerm(termDto, vocabularyResource, repo, state);
        }
        return vocabularyResource;
    }

    private static Resource addTopLevelTerm(TermDto termDto, Resource vocabularyResource, ICdmRepository repo, StructureTreeOwlExportState state) {
        DefinedTermBase<?> term = repo.getTermService().load(termDto.getUuid());
        Resource termResource = addTerm(term, vocabularyResource, repo, state);
        vocabularyResource.addProperty(OwlUtil.propVocTopLevelTerm, termResource);
        termResource.addLiteral(OwlUtil.propTermIsTopLevel, true);
        return termResource;
    }

    private static Resource addTerm(DefinedTermBase term, Resource vocabularyResource, ICdmRepository repo, StructureTreeOwlExportState state) {
        Resource termResource = OwlExportUtil.createTermResource(term, false, repo, state);

        // add vocabulary to term
        termResource.addProperty(OwlUtil.propHasVocabulary, vocabularyResource);
        // add term to vocabulary
        vocabularyResource.addProperty(OwlUtil.propHasTerm, termResource);

        // export includes and generalizationOf
        Set<DefinedTermBase> generalizationOf = term.getGeneralizationOf();
        for (DefinedTermBase<?> kindOf : generalizationOf) {
            Resource kindOfResource = addTerm(kindOf, vocabularyResource, repo, state);
            termResource.addProperty(OwlUtil.propTermIsGeneralizationOf, kindOfResource);
        }
        Set<DefinedTermBase> includes = term.getIncludes();
        for (DefinedTermBase<?> partOf : includes) {
            Resource partOfResource = addTerm(partOf, vocabularyResource, repo, state);
            termResource.addProperty(OwlUtil.propTermIncludes, partOfResource);
        }

        return termResource;
    }

    static List<Resource> createSourceResources(TermBase termBase, StructureTreeOwlExportState state){
        List<Resource> sources = new ArrayList<>();
        for (IdentifiableSource source : termBase.getSources()) {
            Resource sourceResource = getSourceResource(source, state)
                    .addProperty(OwlUtil.propSourceType, source.getType().getKey())
                    ;
            if(source.getIdInSource()!=null){
                sourceResource.addProperty(OwlUtil.propSourceIdInSource, source.getIdInSource());
            }
            if(source.getCitation()!=null){
                sourceResource.addProperty(OwlUtil.propSourceHasCitation, createReferenceResource(source.getCitation(), state));
            }
            sources.add(sourceResource);
        }
        return sources;
    }

    static Resource createReferenceResource(Reference reference, StructureTreeOwlExportState state) {
        Resource referenceResource = getReferenceResource(reference, state);
        if(reference.getTitle()!=null){
            referenceResource.addProperty(OwlUtil.propUuid, reference.getUuid().toString());
            referenceResource.addProperty(OwlUtil.propReferenceTitle, reference.getTitle());
        }
        return referenceResource;
    }

    static List<Resource> createRepresentationResources(TermBase termBase, StructureTreeOwlExportState state){
        List<Resource> representations = new ArrayList<>();
        for (Representation representation : termBase.getRepresentations()) {
            Resource representationResource = getRepresentationResource(representation, state)
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
            if(representation.getPlural()!=null){
                representationResource.addProperty(OwlUtil.propLabelPlural, representation.getPlural());
            }
            representations.add(representationResource);
        }
        return representations;
    }

    private static Resource addCharacterResource(Character character, Resource termResource, ICdmRepository repo, StructureTreeOwlExportState state) {
        addFeatureResource(character, termResource, repo, state);

        Resource structureNodeResource = createNodeResource(character.getStructure(), true, repo, state);
        termResource.addProperty(OwlUtil.propCharacterHasStructure, structureNodeResource);
        Resource propertyNodeResource = createNodeResource(character.getProperty(), true, repo, state);
        termResource.addProperty(OwlUtil.propCharacterHasProperty, propertyNodeResource);
        if(character.getStructureModifier()!=null){
            Resource structureModifierResource = createTermResource(character.getStructureModifier(), true, repo, state);
            termResource.addProperty(OwlUtil.propCharacterHasStructureModfier, structureModifierResource);
        }
        return termResource;
    }

    private static Resource addFeatureResource(Feature feature, Resource termResource, ICdmRepository repo, StructureTreeOwlExportState state) {
        if(feature.isSupportsCategoricalData()){
            termResource.addLiteral(OwlUtil.propFeatureIsCategorical, true);
        }
        if(feature.isSupportsQuantitativeData()){
            termResource.addLiteral(OwlUtil.propFeatureIsQuantitative, true);
        }
        Set<MeasurementUnit> recommendedMeasurementUnits = feature.getRecommendedMeasurementUnits();
        for (MeasurementUnit measurementUnit : recommendedMeasurementUnits) {
            Resource measurementUnitResource = createTermResource(measurementUnit, true, repo, state);
            termResource.addProperty(OwlUtil.propFeatureHasRecommendedMeasurementUnit, measurementUnitResource);
        }
        Set<TermVocabulary<DefinedTerm>> recommendedModifierEnumerations = feature.getRecommendedModifierEnumeration();
        for (TermVocabulary<DefinedTerm> modifierVocabulary : recommendedModifierEnumerations) {
            Resource modifierEnumerationResource = createVocabularyResource(modifierVocabulary, repo, state);
            termResource.addProperty(OwlUtil.propFeatureHasRecommendedModifierEnumeration, modifierEnumerationResource);
        }
        Set<StatisticalMeasure> recommendedStatisticalMeasures = feature.getRecommendedStatisticalMeasures();
        for (StatisticalMeasure statisticalMeasure : recommendedStatisticalMeasures) {
            Resource statisticalMeasureResource = createTermResource(statisticalMeasure, true, repo, state);
            termResource.addProperty(OwlUtil.propFeatureHasRecommendedStatisticalMeasure, statisticalMeasureResource);
        }
        Set<TermVocabulary<State>> supportedCategoricalEnumerations = feature.getSupportedCategoricalEnumerations();
        for (TermVocabulary<State> stateVocabulary : supportedCategoricalEnumerations) {
            Resource supportedCategoricalEnumerationResource = createVocabularyResource(stateVocabulary, repo, state);
            termResource.addProperty(OwlUtil.propFeatureHasSupportedCategoricalEnumeration, supportedCategoricalEnumerationResource);
        }
        return termResource;
    }

    static Resource createTermResource(DefinedTermBase term, boolean initVocabulary, ICdmRepository repo, StructureTreeOwlExportState state) {
        if(initVocabulary){
            createVocabularyResource(term.getVocabulary(), repo, state);
            return getTermResource(term, state);
        }
        Resource termResource = getTermResource(term, state)
                .addProperty(OwlUtil.propUuid, term.getUuid().toString())
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

        // add media
        Set<Media> media = term.getMedia();
        for (Media medium : media) {
            Resource mediaResource = OwlExportUtil.createMediaResource(medium, state);
            termResource.addProperty(OwlUtil.propTermHasMedia, mediaResource);
        }

        // add term sources
        List<Resource> termSourceResources = createSourceResources(term, state);
        termSourceResources.forEach(source->termResource.addProperty(OwlUtil.propTermHasSource, source));

        // add term sub class properties
        if(term.isInstanceOf(eu.etaxonomy.cdm.model.description.Character.class)){
            termResource.addProperty(OwlUtil.propIsA, OwlUtil.CHARACTER);
            addCharacterResource(HibernateProxyHelper.deproxy(term, Character.class), termResource, repo, state);
        }
        else if(term.isInstanceOf(Feature.class)){
            termResource.addProperty(OwlUtil.propIsA, OwlUtil.FEATURE);
            addFeatureResource(HibernateProxyHelper.deproxy(term, Feature.class), termResource, repo, state);
        }
        else {
            termResource.addProperty(OwlUtil.propIsA, OwlUtil.TERM);
        }

        return termResource;
    }

    static Resource createMediaResource(Media media, StructureTreeOwlExportState state) {
        Resource mediaResource = getMediaResource(media, state)
                .addProperty(OwlUtil.propUuid, media.getUuid().toString())
                .addProperty(OwlUtil.propIsA, OwlUtil.MEDIA)
                ;
        // TODO: support for multiple languages
        if(media.getTitle()!=null){
            mediaResource.addProperty(OwlUtil.propMediaTitle, media.getTitle(Language.DEFAULT()).getText());
        }

        // TODO: support for multiple media representations
        MediaRepresentationPart part = MediaUtils.getFirstMediaRepresentationPart(media);
        if(part!=null){
            mediaResource.addProperty(OwlUtil.propMediaUri, part.getUri().toString());
        }
        return mediaResource;
    }

    static Resource createFeatureTreeResource(TermTree featureTree, ICdmRepository repo, StructureTreeOwlExportState state) {
        String featureTreeResourceUri = getFeatureTreeResourceUri(featureTree, state);
        //check if tree exists
        if(state.getModel().containsResource(ResourceFactory.createResource(featureTreeResourceUri))){
            return state.getModel().createResource(featureTreeResourceUri);
        }
        Resource featureTreeResource = state.getModel().createResource(featureTreeResourceUri)
                .addProperty(OwlUtil.propUuid, featureTree.getUuid().toString())
                .addProperty(OwlUtil.propLabel, featureTree.getTitleCache())
                .addProperty(OwlUtil.propIsA, OwlUtil.TREE)
                .addProperty(OwlUtil.propType, featureTree.getTermType().getKey())
                ;

        TermNode rootNode = featureTree.getRoot();

        Resource resourceRootNode = OwlExportUtil.createNodeResource(rootNode, false, repo, state);
        featureTreeResource.addProperty(OwlUtil.propHasRootNode, resourceRootNode);

        addChildNode(rootNode, resourceRootNode, repo, state);

        return featureTreeResource;
    }

    private static void addChildNode(TermNode parentNode, Resource parentResourceNode, ICdmRepository repo, StructureTreeOwlExportState state){
        List<TermNode> childNodes = parentNode.getChildNodes();
        for (TermNode child : childNodes) {
            // create node resource with term
            Resource nodeResource = OwlExportUtil.createNodeResource(child, false, repo, state);

            // add node to parent node
            parentResourceNode.addProperty(OwlUtil.propHasSubStructure, nodeResource);

            addChildNode(child, nodeResource, repo, state);
        }
    }

    static Resource createNodeResource(TermNode<?> node, boolean initFeatureTree, ICdmRepository repo, StructureTreeOwlExportState state) {
        if(initFeatureTree){
            createFeatureTreeResource(node.getGraph(), repo, state);
            return getNodeResource(node, state);
        }
        Resource nodeResource = getNodeResource(node, state)
                .addProperty(OwlUtil.propIsA, OwlUtil.NODE)
                .addProperty(OwlUtil.propUuid, node.getUuid().toString())
                ;
        //inapplicable if
        Set<FeatureState> inapplicableIf = node.getInapplicableIf();
        for (FeatureState featureState : inapplicableIf) {
            Resource featureStateResource = state.getModel().createResource(OwlUtil.RESOURCE_SOURCE+featureState.getUuid())
                    .addProperty(OwlUtil.propIsA, OwlUtil.FEATURE_STATE)
                    .addProperty(OwlUtil.propUuid, featureState.getUuid().toString())
                    .addProperty(OwlUtil.propFeatureStateHasFeature, createTermResource(featureState.getFeature(), false, repo, state))
                    .addProperty(OwlUtil.propFeatureStateHasState, createTermResource(CdmBase.deproxy(featureState.getState(), DefinedTermBase.class), false, repo, state))
                    ;
            nodeResource.addProperty(OwlUtil.propNodeIsInapplicableIf, featureStateResource);
        }
        //only applicable if
        Set<FeatureState> onlyApplicableIf = node.getOnlyApplicableIf();
        for (FeatureState featureState : onlyApplicableIf) {
            Resource featureStateResource = state.getModel().createResource(OwlUtil.RESOURCE_SOURCE+featureState.getUuid())
                    .addProperty(OwlUtil.propIsA, OwlUtil.FEATURE_STATE)
                    .addProperty(OwlUtil.propUuid, featureState.getUuid().toString())
                    .addProperty(OwlUtil.propFeatureStateHasFeature, createTermResource(featureState.getFeature(), false, repo, state))
                    .addProperty(OwlUtil.propFeatureStateHasState, createTermResource(CdmBase.deproxy(featureState.getState(), DefinedTermBase.class), false, repo, state))
                    ;
            nodeResource.addProperty(OwlUtil.propNodeIsOnlyApplicableIf, featureStateResource);
        }
        if(node.getTerm()!=null){
            // add term to node
            createVocabularyResource(node.getTerm().getVocabulary(), repo, state);
            Resource termResource = OwlExportUtil.getTermResource(node.getTerm(), state);
            nodeResource.addProperty(OwlUtil.propHasTerm, termResource);
        }
        return nodeResource;
    }

    private static String getVocabularyResourceUri(TermVocabulary vocabulary, StructureTreeOwlExportState state) {
        return OwlUtil.RESOURCE_TERM_VOCABULARY+vocabulary.getUuid();
    }

    private static Resource getSourceResource(IdentifiableSource source, StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_SOURCE+source.getUuid());
    }

    private static Resource getReferenceResource(Reference reference, StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_REFERENCE+reference.getUuid());
    }

    private static Resource getRepresentationResource(Representation representation,
            StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_REPRESENTATION+representation.getUuid());
    }

    private static Resource getTermResource(DefinedTermBase term, StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_TERM+term.getUuid().toString());
    }

    private static Resource getMediaResource(Media media, StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_MEDIA+media.getUuid().toString());
    }

    private static Resource getNodeResource(TermNode node, StructureTreeOwlExportState state) {
        return state.getModel().createResource(OwlUtil.RESOURCE_NODE + node.getUuid().toString());
    }

    private static String getFeatureTreeResourceUri(TermTree featureTree, StructureTreeOwlExportState state) {
        return OwlUtil.RESOURCE_FEATURE_TREE+featureTree.getUuid().toString();
    }

}
