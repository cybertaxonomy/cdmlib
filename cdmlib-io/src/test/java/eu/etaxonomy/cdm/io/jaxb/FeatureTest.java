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

import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

@Ignore  //does not work anymore since TermNode + Tree was moved to term package
//#8407
public class FeatureTest {

	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TermTreeTest.xml";

	    @Test
	    public void testUnmarshalFeatureTree() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

			TermTree<DefinedTermBase> termTree = dataSet.getTermTrees().get(0);
			DefinedTermBase term = dataSet.getTerms().get(1);

			assertNotNull("TermTree must not be null", termTree);
			assertNotNull("Term must not be null", term);

			assertNotNull("TermTree.root must not be null", termTree.getRoot());
			TermNode<DefinedTermBase> root = termTree.getRoot();
			assertNotNull("TermNode.term must not be null", root.getTerm());
			assertEquals("TermNode.feature must equal Feature", term, root.getTerm());

			assertNotNull("TermNode.children must not be null", root.getChildNodes());
			assertFalse("TermNode.children must not be empty", root.getChildNodes().isEmpty());
			assertEquals("TermNode.children must have 4 child nodes", 4, root.getChildNodes().size());

	    }
}
