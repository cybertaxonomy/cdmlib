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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

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

            assertNotSame("Reference list should not be empty", 0, dataSet.getReferences().size());

            IArticle article = dataSet.getReferences().get(0);
            assertNotNull("Article must not be null",article);

            IJournal journal = dataSet.getReferences().get(1);
            assertNotNull("Journal must not be null", journal);
            assertEquals("Journal must equal Article.inJournal",journal,article.getInJournal());
        }

        @Test
        public void testCastReferences() throws Exception{
            CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
            URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
            DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

            IArticle article = dataSet.getReferences().get(0);
            assertNotNull("Article must not be null",article);

            IJournal journal = ((Reference)article).castReferenceToJournal();
            assertEquals("Journal", journal.getType().name());

            IGeneric generic = ((Reference)journal).castReferenceToGeneric();
            assertEquals("Generic", generic.getType().name());


        }
}
