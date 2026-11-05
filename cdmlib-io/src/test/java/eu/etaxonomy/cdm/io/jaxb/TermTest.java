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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.location.NamedArea;

public class TermTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TermTest.xml";

    @Test
    public void testUnmarshalTerm() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		NamedArea africa = (NamedArea)dataSet.getTerms().get(2);
		assertNotNull(africa, "DefinedTermBase must not be null");
		assertNotNull(africa.getIncludes(), "DefinedTermBase.includes must not be null");
		assertFalse(africa.getIncludes().isEmpty(), "DefinedTermBase.includes must contain NamedArea instances");
		assertEquals(8, africa.getIncludes().size(), "DefinedTermBase.includes must contain eight NamedArea instances");
		NamedArea northAfrica = (NamedArea)dataSet.getTerms().get(4);
		assertNotNull(northAfrica, "DefinedTermBase must not be null");
		assertTrue(africa.getIncludes().contains(northAfrica), "Africa must include North Africa");
		assertNotNull(northAfrica.getPartOf(), "DefinedTermBase.partOf must not be null");
		assertEquals(africa, northAfrica.getPartOf(), "North Africa must be part of Africa");

		Feature discussion = (Feature)dataSet.getTerms().get(13);
		assertNotNull(discussion, "DefinedTermBase must not be null");
		assertNotNull(discussion.getGeneralizationOf(), "DefinedTermBase.generalizationOf must not be null");
		assertFalse(discussion.getGeneralizationOf().isEmpty(), "DefinedTermBase.generalizationOf must contain Feature instances");
		assertEquals(2, discussion.getGeneralizationOf().size(), "DefinedTermBase.generalizationOf must contain two Feature instances");
		Feature taxonomicDiscussion = (Feature)dataSet.getTerms().get(14);
		assertNotNull(taxonomicDiscussion, "DefinedTermBase must not be null");
		assertTrue(discussion.getGeneralizationOf().contains(taxonomicDiscussion), "Discussion must be the generalization of Taxonomic Discussion");
		assertNotNull(taxonomicDiscussion.getKindOf(), "DefinedTermBase.kindOf must not be null");
		assertEquals(discussion, taxonomicDiscussion.getKindOf(), "Taxonomic Discussion must be a kind of Discussion");
    }
}