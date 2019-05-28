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
import java.util.Set;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
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

    static DefinedTermBase createTerm(Resource termResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        TermType termType = TermType.getByKey(termResource.getProperty(OwlUtil.propType).getString());
        DefinedTermBase term;
        // create new term
        if(termType.equals(TermType.Feature)){
            term = Feature.NewInstance();
        }
        else{
            term = DefinedTerm.NewInstance(termType);
        }
        term.setUuid(UUID.fromString(termResource.getProperty(OwlUtil.propUuid).getString()));

        // term URI
        String uriString = termResource.hasProperty(OwlUtil.propUri)?termResource.getProperty(OwlUtil.propUri).toString():null;
        if(CdmUtils.isNotBlank(uriString)){
            term.setUri(URI.create(uriString));
        }

        // import representations
        Set<Representation> representations = new HashSet<>();
        termResource.listProperties(OwlUtil.propHasRepresentation).forEachRemaining(r->representations.add(OwlImportUtil.createRepresentation(termService, r, model)));
        if(representations.isEmpty()){
            StructureTreeOwlImport.logger.error("No representations found for term: "+termResource.getProperty(OwlUtil.propUuid));
        }
        representations.forEach(rep->term.addRepresentation(rep));

        IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(termResource.getURI());
        importSource.setCitation(state.getConfig().getSourceReference());
        term.addSource(importSource);

        return term;
    }

    static TermVocabulary createVocabulary(Resource vocabularyResource, ITermService termService, Model model, StructureTreeOwlImportState state){
        TermType termType = TermType.getByKey(vocabularyResource.getProperty(OwlUtil.propType).getString());
        // create new vocabulary
        TermVocabulary vocabulary = TermVocabulary.NewInstance(termType);
        vocabulary.setUuid(UUID.fromString(vocabularyResource.getProperty(OwlUtil.propUuid).getString()));

        // voc URI
        String vocUriString = vocabularyResource.hasProperty(OwlUtil.propUri)?vocabularyResource.getProperty(OwlUtil.propUri).toString():null;
        if(CdmUtils.isNotBlank(vocUriString)){
            vocabulary.setUri(URI.create(vocUriString));
        }

        // voc representations
        Set<Representation> vocRepresentations = new HashSet<>();
        vocabularyResource.listProperties(OwlUtil.propHasRepresentation).forEachRemaining(r->vocRepresentations.add(OwlImportUtil.createRepresentation(termService, r, model)));
        if(vocRepresentations.isEmpty()){
            StructureTreeOwlImport.logger.error("No representations found for vocabulary: "+vocabularyResource.getProperty(OwlUtil.propUuid));
        }
        vocRepresentations.forEach(rep->vocabulary.addRepresentation(rep));

        IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(vocabularyResource.getURI());
        importSource.setCitation(state.getConfig().getSourceReference());
        vocabulary.addSource(importSource);


        return vocabulary;
    }

    static Representation createRepresentation(ITermService termService, Statement repr, Model model) {
        Resource repsentationResource = model.createResource(repr.getObject().toString());

        String languageLabel = repsentationResource.getProperty(OwlUtil.propLanguage).getString();
        UUID languageUuid = UUID.fromString(repsentationResource.getProperty(OwlUtil.propLanguageUuid).getString());
        DefinedTermBase termBase = termService.load(languageUuid);
        Language language = null;
        if(termBase.isInstanceOf(Language.class)){
            language = HibernateProxyHelper.deproxy(termBase, Language.class);
        }
        if(language==null){
            language = termService.getLanguageByLabel(languageLabel);
        }
        if(language==null){
            language = Language.getDefaultLanguage();
        }

        String abbreviatedLabel = repsentationResource.hasProperty(OwlUtil.propLabelAbbrev)?repsentationResource.getProperty(OwlUtil.propLabelAbbrev).toString():null;
        String label = repsentationResource.getProperty(OwlUtil.propLabel).getString();
        String description = repsentationResource.hasProperty(OwlUtil.propDescription)?repsentationResource.getProperty(OwlUtil.propDescription).getString():null;
        Representation representation = Representation.NewInstance(description, label, abbreviatedLabel, language);

        return representation;
    }

}
