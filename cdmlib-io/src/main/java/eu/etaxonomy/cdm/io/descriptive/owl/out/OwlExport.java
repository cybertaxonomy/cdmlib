/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.descriptive.owl.out;

import java.io.File;
import java.util.List;

import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import eu.etaxonomy.cdm.io.common.CdmExportBase;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author pplitzner
 * @since Jul 3, 2017
 *
 */
@Component
public class OwlExport extends CdmExportBase<OwlExportConfigurator, OwlExportState, IExportTransformer, File> {

    private static final long serialVersionUID = 3197379920692366008L;

    private static final String BASE_URI = "http://cybertaxonomy.eu/";
    private static final String RESOURCE_URI = BASE_URI+"resource/";
    private static final String PROPERTY_BASE_URI = BASE_URI+"property/";
    private static final String NODE_BASE_URI = RESOURCE_URI+"node/";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(OwlExportState state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInvoke(OwlExportState state) {

        FeatureTree featureTree = state.getConfig().getFeatureTree();
        FeatureNode rootNode = featureTree.getRoot();

        Model model = ModelFactory.createDefaultModel();
        Property propHasSubStructure = model.createProperty(PROPERTY_BASE_URI+"hasSubStructure");
        Property propUuid = model.createProperty(PROPERTY_BASE_URI+"uuid");
        Property propLabel = model.createProperty(PROPERTY_BASE_URI+"label");

        Resource resourceRootNode = model.createResource(NODE_BASE_URI + rootNode.getUuid().toString());

        model.createResource(RESOURCE_URI+"featureTree/"+featureTree.getUuid().toString())
                .addProperty(propUuid, featureTree.getUuid().toString())
                .addProperty(propLabel, featureTree.getTitleCache())
                .addProperty(model.createProperty(PROPERTY_BASE_URI + "hasRootNode"),
                        resourceRootNode
                        .addProperty(propUuid, rootNode.getUuid().toString()));

        addChildNode(rootNode, resourceRootNode, propHasSubStructure, propUuid, propLabel, model);
//                        .addProperty(propHasSubStructure, model.createResource(resourceURI + "child1")
//                                .addProperty(propHasSubStructure, model.createResource(resourceURI + "child1.1")))
//                        .addProperty(propHasSubStructure, model.createResource(resourceURI + "child2")
//                                .addProperty(propHasSubStructure, model.createResource(resourceURI + "child2.1"))
//                                .addProperty(propHasSubStructure, model.createResource(resourceURI + "child2.2"))));

        // FileOutputStream fout = new
        // FileOutputStream("C:\\Java\\jdk1.6.0_03\\bin\\amitKumar.xml");
        model.write(System.out);
    }

    private void addChildNode(FeatureNode node, Resource resourceNode, final Property propHasSubStructure, final Property propUuid, Property propLabel, Model model){
        List<FeatureNode> childNodes = node.getChildNodes();
        for (FeatureNode child : childNodes) {
            Resource childResourceNode = model.createResource(NODE_BASE_URI+child.getUuid().toString());
            resourceNode.addProperty(propHasSubStructure, childResourceNode
                    .addProperty(propUuid, child.getUuid().toString())
                    .addProperty(propLabel, child.getFeature().getLabel())
                    );
            addChildNode(child, childResourceNode, propHasSubStructure, propUuid, propLabel, model);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(OwlExportState state) {
        return false;
    }

}
