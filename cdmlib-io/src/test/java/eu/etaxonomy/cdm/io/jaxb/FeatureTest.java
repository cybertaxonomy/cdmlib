/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

public class FeatureTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/FeatureTest.xml";
	    
	    @Test
	    public void testUnmarshalFeatureTree() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			FeatureTree featureTree = (FeatureTree)dataSet.getFeatureTrees().get(0);
			Feature feature = (Feature)dataSet.getTerms().get(1);
			
			assertNotNull("FeatureTree must not be null",featureTree);
			assertNotNull("Feature must not be null",feature);
			
			assertNotNull("FeatureTree.root must not be null",featureTree.getRoot());
			FeatureNode featureNode = featureTree.getRoot();
			assertNotNull("FeatureNode.feature must not be null",featureNode.getFeature());
			assertEquals("FeatureNode.feature must equal Feature",feature,featureNode.getFeature());
			
			assertNotNull("FeatureNode.children must not be null",featureNode.getChildNodes());
			assertFalse("FeatureNode.children must not be empty",featureNode.getChildNodes().isEmpty());
			assertEquals("FeatureNode.children must have 4 child nodes",4,featureNode.getChildNodes().size());
			
	    }
}
