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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.Reference;

public class ReferenceTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/ReferenceTest.xml";

    @Test
    public void testUnmarshalReference() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

        assertNotSame(0, dataSet.getReferences().size(), "Reference list should not be empty");

        IArticle article = dataSet.getReferences().get(0);
        assertNotNull(article, "Article must not be null");

        IJournal journal = dataSet.getReferences().get(1);
        assertNotNull(journal, "Journal must not be null");
        assertEquals(journal, article.getInJournal(), "Journal must equal Article.inJournal");
    }

    @Test
    public void testCastReferences() throws Exception{
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

        IArticle article = dataSet.getReferences().get(0);
        assertNotNull(article, "Article must not be null");

        IJournal journal = ((Reference)article).castReferenceToJournal();
        assertEquals("Journal", journal.getType().name());

        IGeneric generic = ((Reference)journal).castReferenceToGeneric();
        assertEquals("Generic", generic.getType().name());
    }
}
