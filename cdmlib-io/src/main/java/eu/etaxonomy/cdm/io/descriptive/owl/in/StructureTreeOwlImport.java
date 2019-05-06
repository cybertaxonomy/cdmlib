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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.dto.TermDto;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
@Component("structureTreeOwlImport")
public class StructureTreeOwlImport extends CdmImportBase<StructureTreeOwlImportConfigurator, StructureTreeOwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StructureTreeOwlImport.class);

    private Property propHasSubStructure;
    private Property propHasRepresentation;
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
    protected boolean doCheck(StructureTreeOwlImportState state) {
        logger.warn("Checking not yet implemented for " + this.getClass().getSimpleName());
        return true;
    }

    @Override
    public void doInvoke(StructureTreeOwlImportState state) {
        URI source = state.getConfig().getSource();

        Model model = ModelFactory.createDefaultModel();
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propHasRepresentation = model.createProperty(OwlConstants.PROPERTY_HAS_REPRESENTATION);
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

        model.read(source.toString());

        List<FeatureTree> featureTrees = new ArrayList<>();
        //get all trees
        ResIterator iterator = model.listResourcesWithProperty(propHasRootNode);
        while(iterator.hasNext()){
            Resource tree = iterator.next();
            String type = tree.getProperty(propType).getString();
            FeatureTree featureTree = FeatureTree.NewInstance(TermType.getByKey(type));
            featureTree.setTitleCache(tree.getProperty(propLabel).getString(), true);

            Resource rootNode = tree.getProperty(propHasRootNode).getResource();
            rootNode.listProperties(propHasSubStructure).forEachRemaining(prop->createNode(featureTree.getRoot(), prop, featureTree.getTitleCache(), model, state));

            featureTrees.add(featureTree);

            getFeatureTreeService().save(featureTree);
        }
    }

    private void createNode(FeatureNode parent, Statement nodeStatement, String treeLabel, Model model, StructureTreeOwlImportState state) {
        Resource nodeResource = model.createResource(nodeStatement.getObject().toString());

        TermType termType = TermType.getByKey(nodeResource.getProperty(propType).getString());
        String uriString = nodeResource.hasProperty(propUri)?nodeResource.getProperty(propUri).toString():null;

        Collection<TermDto> dtos = new ArrayList<>();

        // check representations if term already exists
        Set<Representation> representations = new HashSet<>();
        nodeResource.listProperties(propHasRepresentation).forEachRemaining(r->representations.add(createRepresentation(r, model)));
        if(representations.isEmpty()){
            logger.error("No representations found for term: "+nodeResource.getProperty(propUuid));
            return;
        }

        DefinedTermBase term = null;
        String termLabel = null;
        for (Representation representation : representations) {
            termLabel = representation.getLabel();
            if(uriString!=null){
                URI uri = URI.create(uriString);
                dtos = getTermService().findByUriAsDto(uri, termLabel, termType);
            }
            else{
                dtos = getTermService().findByTitleAsDto(termLabel, termType);
            }
            if(dtos.size()>1){
                logger.warn("More than one term was found for: "+termLabel+"\nUsing the first one found");
            }
            if(dtos.size()>=1){
                term = getTermService().load(dtos.iterator().next().getUuid());
            }
            if(term!=null){
                break;
            }
        }

        if(term==null){
            // create new term
            if(termType.equals(TermType.Feature)){
                term = Feature.NewInstance();
                for (Representation representation : representations) {
                    term.addRepresentation(representation);
                }
            }
            else{
                term = DefinedTerm.NewInstance(termType);
                for (Representation representation : representations) {
                    term.addRepresentation(representation);
                }
            }
            IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance(termLabel);
            importSource.setCitation(state.getConfig().getSourceReference());
            term.addSource(importSource);
            getTermService().save(term);

            TermVocabulary vocabulary = state.getConfig().getVocabulary(termType, treeLabel);
            vocabulary.addTerm(term);
            getVocabularyService().saveOrUpdate(vocabulary);
        }

        FeatureNode<?> childNode = FeatureNode.NewInstance(term);
        parent.addChild(childNode);

        nodeResource.listProperties(propHasSubStructure).forEachRemaining(prop->createNode(childNode, prop, treeLabel, model, state));
    }

    private Representation createRepresentation(Statement repr, Model model) {
        Resource repsentationResource = model.createResource(repr.getObject().toString());

        String languageLabel = repsentationResource.getProperty(propLanguage).getString();
        UUID languageUuid = UUID.fromString(repsentationResource.getProperty(propLanguageUuid).getString());
        DefinedTermBase termBase = getTermService().load(languageUuid);
        Language language = null;
        if(termBase.isInstanceOf(Language.class)){
            language = HibernateProxyHelper.deproxy(termBase, Language.class);
        }
        if(language==null){
            language = getTermService().getLanguageByLabel(languageLabel);
        }
        if(language==null){
            language = Language.getDefaultLanguage();
        }

        String abbreviatedLabel = repsentationResource.hasProperty(propLabelAbbrev)?repsentationResource.getProperty(propLabelAbbrev).toString():null;
        String label = repsentationResource.getProperty(propLabel).getString();
        String description = repsentationResource.hasProperty(propDescription)?repsentationResource.getProperty(propDescription).getString():null;
        Representation representation = Representation.NewInstance(description, label, abbreviatedLabel, language);

        return representation;
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlImportState state) {
        return false;
    }

}
