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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Character;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author pplitzner
 * @since May 26, 2019
 *
 */
public class OwlImportUtil {

    static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(OwlImportUtil.class);

    static Feature createFeature(Resource termResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        Feature feature = Feature.NewInstance();
        if(termResource.hasProperty(OwlUtil.propFeatureIsCategorical)){
            feature.setSupportsCategoricalData(termResource.getProperty(OwlUtil.propFeatureIsCategorical).getBoolean());
        }
        if(termResource.hasProperty(OwlUtil.propFeatureIsQuantitative)){
            feature.setSupportsQuantitativeData(termResource.getProperty(OwlUtil.propFeatureIsQuantitative).getBoolean());
        }
//        // import measurement units
//        Set<DefinedTermBase> measurementUnits = new HashSet<>();
//        List<Statement> measurementUnitStatements = termResource.listProperties(OwlUtil.propFeatureHasRecommendedMeasurementUnit).toList();
//        for (Statement statement : measurementUnitStatements) {
//            Resource measurementUnitResource = model.createResource(statement.getObject().toString());
//            measurementUnits.add(OwlImportUtil.createTerm(measurementUnitResource, termService, model, state));
//        }
//        measurementUnits.forEach(unit->feature.addRecommendedMeasurementUnit(unit));

        return feature;
    }

    static Character createCharacter(Resource termResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        Character character = Character.NewInstance();
        return character;
    }

    static DefinedTermBase createTerm(Resource termResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        TermType termType = TermType.getByKey(termResource.getProperty(OwlUtil.propType).getString());
        DefinedTermBase term;
        // create new term
        if(termType.equals(TermType.Feature)){
            term = createFeature(termResource, termService, model, state);
        }
        else if(termType.equals(TermType.Character)){
            term = createCharacter(termResource, termService, model, state);
        }
        else{
            term = DefinedTerm.NewInstance(termType);
        }
        term.setUuid(UUID.fromString(termResource.getProperty(OwlUtil.propUuid).getString()));

        // term URI
        String uriString = termResource.hasProperty(OwlUtil.propUri)?termResource.getProperty(OwlUtil.propUri).getString():null;
        if(CdmUtils.isNotBlank(uriString)){
            term.setUri(URI.create(uriString));
        }
        // symbol
        String symbolString = termResource.hasProperty(OwlUtil.propTermSymbol)?termResource.getProperty(OwlUtil.propTermSymbol).getString():null;
        if(CdmUtils.isNotBlank(symbolString)){
            term.setSymbol(symbolString);
        }
        // symbol2
        String symbol2String = termResource.hasProperty(OwlUtil.propTermSymbol2)?termResource.getProperty(OwlUtil.propTermSymbol2).getString():null;
        if(CdmUtils.isNotBlank(symbol2String)){
            term.setSymbol2(symbol2String);
        }
        // idInVocabulary
        String idInVocabularyString = termResource.hasProperty(OwlUtil.propTermIdInVocabulary)?termResource.getProperty(OwlUtil.propTermIdInVocabulary).getString():null;
        if(CdmUtils.isNotBlank(idInVocabularyString)){
            term.setIdInVocabulary(idInVocabularyString);
        }

        // import representations
        Set<Representation> representations = new HashSet<>();
        termResource.listProperties(OwlUtil.propHasRepresentation).forEachRemaining(r->representations.add(OwlImportUtil.createRepresentation(termService, r, model)));
        if(representations.isEmpty()){
            logger.error("No representations found for term: "+termResource.getProperty(OwlUtil.propUuid));
        }
        representations.forEach(rep->term.addRepresentation(rep));

        // import sources
        Set<IdentifiableSource> sources = new HashSet<>();
        termResource.listProperties(OwlUtil.propTermHasSource).forEachRemaining(sourceStatement->sources.add(OwlImportUtil.createSource(sourceStatement, model)));
        sources.forEach(source->term.addSource(source));

        // add import source
        IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(termResource.getURI());
        importSource.setCitation(state.getConfig().getSourceReference());
        term.addSource(importSource);

        return term;
    }

    static IdentifiableSource createSource(Statement sourceStatement, Model model) {
        Resource sourceResource = model.createResource(sourceStatement.getObject().toString());

        String typeString = sourceResource.getProperty(OwlUtil.propSourceType).getString();
        IdentifiableSource source = IdentifiableSource.NewInstance(OriginalSourceType.getByKey(typeString));

        if(sourceResource.hasProperty(OwlUtil.propSourceIdInSource)){
            String idInSource = sourceResource.getProperty(OwlUtil.propSourceIdInSource).getString();
            source.setIdInSource(idInSource);
        }

        // import citation
        List<Statement> citationStatements = sourceResource.listProperties(OwlUtil.propSourceHasCitation).toList();
        if(citationStatements.size()>1){
            logger.error("More than one citations found for source. Choosing one arbitrarily. - "+sourceResource.toString());
        }
        if(!citationStatements.isEmpty()){
            Statement citationStatement = citationStatements.iterator().next();
            source.setCitation(createReference(citationStatement, model));
        }
        return source;
    }

    static Reference createReference(Statement citationStatement, Model model){
        Resource citationResource = model.createResource(citationStatement.getObject().toString());
        String titleString = citationResource.getProperty(OwlUtil.propReferenceTitle).getString();
        Reference citation = ReferenceFactory.newGeneric();
        citation.setTitle(titleString);
        return citation;
    }

    static TermVocabulary createVocabulary(Resource vocabularyResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        TermType termType = TermType.getByKey(vocabularyResource.getProperty(OwlUtil.propType).getString());
        // create new vocabulary
        TermVocabulary vocabulary = TermVocabulary.NewInstance(termType);
        vocabulary.setUuid(UUID.fromString(vocabularyResource.getProperty(OwlUtil.propUuid).getString()));

        // voc URI
        String vocUriString = vocabularyResource.hasProperty(OwlUtil.propUri)?vocabularyResource.getProperty(OwlUtil.propUri).getString():null;
        if(CdmUtils.isNotBlank(vocUriString)){
            vocabulary.setUri(URI.create(vocUriString));
        }

        // voc representations
        Set<Representation> vocRepresentations = new HashSet<>();
        vocabularyResource.listProperties(OwlUtil.propHasRepresentation).forEachRemaining(r->vocRepresentations.add(OwlImportUtil.createRepresentation(termService, r, model)));
        if(vocRepresentations.isEmpty()){
            logger.error("No representations found for vocabulary: "+vocabularyResource.getProperty(OwlUtil.propUuid));
        }
        vocRepresentations.forEach(rep->vocabulary.addRepresentation(rep));

        IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(vocabularyResource.getURI());
        importSource.setCitation(state.getConfig().getSourceReference());
        vocabulary.addSource(importSource);


        return vocabulary;
    }

    static Media createMedia(Resource mediaResource, StructureTreeOwlImportState state){
        URI mediaUri = URI.create(mediaResource.getProperty(OwlUtil.propMediaUri).getString());
        // create new media
        Media media = Media.NewInstance(mediaUri, null, null, null);
        media.setUuid(UUID.fromString(mediaResource.getProperty(OwlUtil.propUuid).getString()));

        if(mediaResource.hasProperty(OwlUtil.propMediaTitle)){
            // TODO: support multiple language titles
            media.putTitle(Language.DEFAULT(), mediaResource.getProperty(OwlUtil.propMediaTitle).getString());
        }

        IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(mediaResource.getURI());
        importSource.setCitation(state.getConfig().getSourceReference());
        media.addSource(importSource);

        return media;
    }

    static Representation createRepresentation(ITermService termService, Statement repr, Model model) {
        Resource repsentationResource = model.createResource(repr.getObject().toString());

        String languageLabel = repsentationResource.getProperty(OwlUtil.propLanguage).getString();
        UUID languageUuid = UUID.fromString(repsentationResource.getProperty(OwlUtil.propLanguageUuid).getString());
        Language language = Language.getLanguageFromUuid(languageUuid);
        if(language==null){
            language = termService.getLanguageByLabel(languageLabel);
        }
        if(language==null){
            language = Language.getDefaultLanguage();
        }

        String abbreviatedLabel = repsentationResource.hasProperty(OwlUtil.propLabelAbbrev)?repsentationResource.getProperty(OwlUtil.propLabelAbbrev).getString():null;
        String plural = repsentationResource.hasProperty(OwlUtil.propLabelPlural)?repsentationResource.getProperty(OwlUtil.propLabelPlural).getString():null;
        String label = repsentationResource.getProperty(OwlUtil.propLabel).getString();
        String description = repsentationResource.hasProperty(OwlUtil.propDescription)?repsentationResource.getProperty(OwlUtil.propDescription).getString():null;
        Representation representation = Representation.NewInstance(description, label, abbreviatedLabel, language);
        representation.setPlural(plural);

        return representation;
    }

}
