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

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class DescriptionTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/DescriptionTest.xml";

    @Test
    public void testUnmarshalDescription() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		Taxon taxon = (Taxon)dataSet.getTaxonBases().get(0);
		assertNotNull(taxon, "Taxon must not be null");
		assertNotNull(taxon.getDescriptions(), "Taxon.descriptions must not be null");
		assertFalse(taxon.getDescriptions().isEmpty(), "Taxon.descriptions must not be empty");
		assertEquals(1, taxon.getDescriptions().size(), "Taxon.descriptions must contain one description");

		TaxonDescription taxonDescription = taxon.getDescriptions().iterator().next();

		assertNotNull(taxonDescription.getElements(), "TaxonDescription.descriptionElements must not be null");
		assertFalse(taxonDescription.getElements().isEmpty(), "TaxonDescription.descriptionElements must not be empty");
		assertEquals(1, taxonDescription.getElements().size(), "TaxonDescription.descriptionElements should contain one DescriptionElement");

		TextData textData = (TextData)taxonDescription.getElements().iterator().next();
		assertEquals(taxonDescription, textData.getInDescription(), "TaxonDescription should equal TextData.inDescription");
    }
}
