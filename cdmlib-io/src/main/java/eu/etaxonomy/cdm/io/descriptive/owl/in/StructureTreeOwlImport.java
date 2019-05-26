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
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlUtil;
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
        Resource nodeResource = model.createResource(nodeStatement.getObject().toString());

        Collection<TermDto> dtos = new ArrayList<>();

        // import representations
        Set<Representation> representations = new HashSet<>();
        nodeResource.listProperties(OwlUtil.propHasRepresentation).forEachRemaining(r->representations.add(createRepresentation(r, model)));
        if(representations.isEmpty()){
            logger.error("No representations found for term: "+nodeResource.getProperty(OwlUtil.propUuid));
            return;
        }

        DefinedTermBase term = getTermService().load(dtos.iterator().next().getUuid());
        if(term==null){
            TermType termType = TermType.getByKey(nodeResource.getProperty(OwlUtil.propType).getString());
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
            String uriString = nodeResource.hasProperty(OwlUtil.propUri)?nodeResource.getProperty(OwlUtil.propUri).toString():null;
            if(CdmUtils.isNotBlank(uriString)){
                term.setUri(URI.create(uriString));
            }

            IdentifiableSource importSource = IdentifiableSource.NewDataImportInstance("Import of term tree "+treeLabel);
            importSource.setCitation(state.getConfig().getSourceReference());
            term.addSource(importSource);
            getTermService().save(term);

            TermVocabulary vocabulary = state.getConfig().getVocabulary(termType, treeLabel);
            vocabulary.addTerm(term);
            getVocabularyService().saveOrUpdate(vocabulary);
        }

        FeatureNode<?> childNode = parent.addChild(term);

        nodeResource.listProperties(OwlUtil.propHasSubStructure).forEachRemaining(prop->createNode(childNode, prop, treeLabel, model, state));
    }

    private Representation createRepresentation(Statement repr, Model model) {
        Resource repsentationResource = model.createResource(repr.getObject().toString());

        String languageLabel = repsentationResource.getProperty(OwlUtil.propLanguage).getString();
        UUID languageUuid = UUID.fromString(repsentationResource.getProperty(OwlUtil.propLanguageUuid).getString());
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

        String abbreviatedLabel = repsentationResource.hasProperty(OwlUtil.propLabelAbbrev)?repsentationResource.getProperty(OwlUtil.propLabelAbbrev).toString():null;
        String label = repsentationResource.getProperty(OwlUtil.propLabel).getString();
        String description = repsentationResource.hasProperty(OwlUtil.propDescription)?repsentationResource.getProperty(OwlUtil.propDescription).getString():null;
        Representation representation = Representation.NewInstance(description, label, abbreviatedLabel, language);

        return representation;
    }

    @Override
    protected boolean isIgnore(StructureTreeOwlImportState state) {
        return false;
    }

}
