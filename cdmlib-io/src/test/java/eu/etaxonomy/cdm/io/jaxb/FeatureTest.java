/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermNode;
import eu.etaxonomy.cdm.model.term.TermTree;

@Disabled  //does not work anymore since TermNode + Tree was moved to term package
//#8407
public class FeatureTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TermTreeTest.xml";

    @Test
    public void testUnmarshalFeatureTree() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		TermTree<?> termTree = dataSet.getTermTrees().get(0);
		DefinedTermBase<?> term = dataSet.getTerms().get(1);

		assertNotNull(termTree, "TermTree must not be null");
		assertNotNull(term, "Term must not be null");

		assertNotNull(termTree.getRoot(), "TermTree.root must not be null");
		TermNode<?> root = termTree.getRoot();
		assertNotNull(root.getTerm(), "TermNode.term must not be null");
		assertEquals(term, root.getTerm(), "TermNode.feature must equal Feature");

		assertNotNull(root.getChildNodes(), "TermNode.children must not be null");
		assertFalse(root.getChildNodes().isEmpty(), "TermNode.children must not be empty");
		assertEquals(4, root.getChildNodes().size(), "TermNode.children must have 4 child nodes");

    }
}
