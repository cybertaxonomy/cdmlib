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
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

public class TaxonTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TaxonTest.xml";

    @Test
    public void testUnmarshalTaxon() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		Taxon parent = (Taxon)dataSet.getTaxonBases().get(0);
		assertNotNull(parent, "Taxon must not be null");
		Taxon child = (Taxon)dataSet.getTaxonBases().get(1);
		assertNotNull(child, "Taxon must not be null");

		Synonym synonym = (Synonym)dataSet.getTaxonBases().get(2);
		assertNotNull(synonym, "Synonym must not be null");

		assertNotNull(parent.getRelationsFromThisTaxon(), "Taxon.relationsFromThisTaxon should not be null");
		assertFalse(parent.getRelationsFromThisTaxon().isEmpty(), "Taxon.relationsFromThisTaxon should not be empty");
		TaxonRelationship taxonRelationship = parent.getRelationsFromThisTaxon().iterator().next();
		assertNotNull(taxonRelationship.getFromTaxon(), "TaxonRelationship.fromTaxon must not be null");
		assertEquals(parent, taxonRelationship.getFromTaxon(), "parent Taxon should equal TaxonRelationship.fromTaxon");
		assertNotNull(taxonRelationship.getToTaxon(), "TaxonRelationship.toTaxon must not be null");
		assertEquals(child, taxonRelationship.getToTaxon(), "TaxonRelationship.toTaxon should equal child Taxon");
		assertNotNull(child.getRelationsToThisTaxon(), "Taxon.relationsToThisTaxon must not be null");
		assertTrue(child.getRelationsToThisTaxon().contains(taxonRelationship), "child Taxon.relationsToThisTaxon should contain TaxonRelationship");

		assertNotNull(parent.getSynonyms(), "Taxon.synonyms should not be null");
		assertFalse(parent.getSynonyms().isEmpty(), "Taxon.synonyms should not be empty");
		Synonym parentSynonym = parent.getSynonyms().iterator().next();
		assertNotNull(parentSynonym, "parentSynonym must not be null");
		assertNotNull(parentSynonym.getAcceptedTaxon(), "parentSynonym.acceptedTaxon must not be null");
		assertEquals(parent, parentSynonym.getAcceptedTaxon(), "parent Taxon should equal parentSynonym.acceptedTaxon");
		assertEquals(synonym, parentSynonym, "parentSynonym should equal synonym");

		assertNotNull(parent.getLsid(), "Taxon.lsid should not be null");
		assertEquals(new LSID("urn:lsid:example.org:taxonconcept:1"), parent.getLsid(), "Taxon.lsid should equal urn:lsid:example.org:taxonconcept:1");
    }
}
