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
import java.util.List;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.descriptive.owl.OwlConstants;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.FeatureNode;
import eu.etaxonomy.cdm.model.term.FeatureTree;
import eu.etaxonomy.cdm.model.term.TermType;

/**
 * @author pplitzner
 * @since Apr 24, 2019
 *
 */
@Component("owlImport")
public class OwlImport extends CdmImportBase<OwlImportConfigurator, OwlImportState> {

    private static final long serialVersionUID = -3659780404413458511L;

    private Property propertyHasRootNode;
    private Property propHasSubStructure;
    private Property propUuid;
    private Property propLabel;
    private Property propIsA;
    private Property propType;
    private Property propDescription;

    @Override
    protected boolean doCheck(OwlImportState state) {
        return false;
    }

    @Override
    public void doInvoke(OwlImportState state) {
        URI source = state.getConfig().getSource();

        Model model = ModelFactory.createDefaultModel();
        propertyHasRootNode = model.createProperty(OwlConstants.PROPERTY_HAS_ROOT_NODE);
        propHasSubStructure = model.createProperty(OwlConstants.PROPERTY_HAS_SUBSTRUCTURE);
        propUuid = model.createProperty(OwlConstants.PROPERTY_UUID);
        propLabel = model.createProperty(OwlConstants.PROPERTY_LABEL);
        propIsA = model.createProperty(OwlConstants.PROPERTY_IS_A);
        propType = model.createProperty(OwlConstants.PROPERTY_TYPE);
        propDescription = model.createProperty(OwlConstants.PROPERTY_DESCRIPTION);

        model.read(source.toString());

        List<FeatureTree> featureTrees = new ArrayList<>();
        //get all trees
        ResIterator iterator = model.listResourcesWithProperty(propertyHasRootNode);
        while(iterator.hasNext()){
            Resource tree = iterator.next();
            String type = tree.getProperty(propType).getString();
            FeatureTree featureTree = FeatureTree.NewInstance(TermType.getByKey(type));
            featureTree.setTitleCache(tree.getProperty(propLabel).getString(), true);

            Resource rootNode = tree.getProperty(propertyHasRootNode).getResource();
            rootNode.listProperties(propHasSubStructure).forEachRemaining(prop->createNode(featureTree.getRoot(), prop, model));

            featureTrees.add(featureTree);
        }
        state.setFeatureTrees(featureTrees);
    }

    private void createNode(FeatureNode parent, Statement nodeStatement, Model model) {
        Resource nodeResource = model.createResource(OwlConstants.RESOURCE_NODE+nodeStatement.getProperty(propUuid).getString());

        String termLabel = nodeResource.getProperty(propLabel).getString();
        String termDescription = nodeResource.hasProperty(propDescription)?nodeResource.getProperty(propDescription).getString():null;
        String termType = nodeResource.getProperty(propType).getString();
        DefinedTermBase term;
        if(termType.equals(TermType.Feature)){
            term = Feature.NewInstance();
        }
        else{
            term = DefinedTerm.NewInstance(TermType.getByKey(termType), termDescription, termLabel, null);
        }

        FeatureNode<?> childNode = FeatureNode.NewInstance(term);
        parent.addChild(childNode);

        nodeResource.listProperties(propHasSubStructure).forEachRemaining(prop->createNode(childNode, prop, model));
    }

    @Override
    protected boolean isIgnore(OwlImportState state) {
        return false;
    }

}
