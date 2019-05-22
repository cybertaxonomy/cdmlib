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

import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
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
        Resource vocabularyResource = state.getModel().createResource(OwlConstants.RESOURCE_TERM_VOCABULARY+vocabulary.getUuid())
                .addProperty(StructureTreeOwlExportState.propUuid, vocabulary.getUuid().toString())
                .addProperty(StructureTreeOwlExportState.propType, vocabulary.getTermType().getKey())
                .addProperty(StructureTreeOwlExportState.propIsA, OwlConstants.VOCABULARY)
                ;
        if(vocabulary.getUri()!=null){
            vocabularyResource.addProperty(StructureTreeOwlExportState.propUri, vocabulary.getUri().toString());
        }
        // add vocabulary representations
        List<Resource> vocabularyRepresentationResources = OwlExportUtil.createRepresentationResources(vocabulary, state);
        vocabularyRepresentationResources.forEach(rep->vocabularyResource.addProperty(StructureTreeOwlExportState.propHasRepresentation, rep));
        return vocabularyResource;
    }

    static List<Resource> createRepresentationResources(TermBase termBase, StructureTreeOwlExportState state){
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

    static Resource createTermResource(DefinedTermBase term, StructureTreeOwlExportState state) {
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

}
