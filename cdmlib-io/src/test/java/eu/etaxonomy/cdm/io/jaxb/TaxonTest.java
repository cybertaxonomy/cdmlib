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
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
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
			assertNotNull("Taxon must not be null",parent);
			Taxon child = (Taxon)dataSet.getTaxonBases().get(1);
			assertNotNull("Taxon must not be null", child);
			
			Synonym synonym = (Synonym)dataSet.getTaxonBases().get(2);
			assertNotNull("Synonym must not be null", synonym);
			
			assertNotNull("Taxon.relationsFromThisTaxon should not be null", parent.getRelationsFromThisTaxon());
			assertFalse("Taxon.relationsFromThisTaxon should not be empty",parent.getRelationsFromThisTaxon().isEmpty());
			TaxonRelationship taxonRelationship = parent.getRelationsFromThisTaxon().iterator().next();
			assertNotNull("TaxonRelationship.fromTaxon must not be null",taxonRelationship.getFromTaxon());
			assertEquals("parent Taxon should equal TaxonRelationship.fromTaxon",parent,taxonRelationship.getFromTaxon());
			assertNotNull("TaxonRelationship.toTaxon must not be null", taxonRelationship.getToTaxon());
			assertEquals("TaxonRelationship.toTaxon should equal child Taxon",child,taxonRelationship.getToTaxon());
			assertNotNull("Taxon.relationsToThisTaxon must not be null",child.getRelationsToThisTaxon());
			assertTrue("child Taxon.relationsToThisTaxon should contain TaxonRelationship",child.getRelationsToThisTaxon().contains(taxonRelationship));
			
			assertNotNull("Taxon.synonymRelations should not be null", parent.getSynonymRelations());
			assertFalse("Taxon.synonymRelations should not be empty",parent.getSynonymRelations().isEmpty());
			SynonymRelationship synonymRelationship = parent.getSynonymRelations().iterator().next();
			assertNotNull("SynonymRelationship.acceptedTaxon must not be null",synonymRelationship.getAcceptedTaxon());
			assertEquals("parent Taxon should equal SynonymRelationship.acceptedTaxon",parent,synonymRelationship.getAcceptedTaxon());
			assertNotNull("SynonymRelationship.synonym must not be null", synonymRelationship.getSynonym());
			assertEquals("SynonymRelationship.synonym should equal synonym",synonym,synonymRelationship.getSynonym());
			assertNotNull("Synonym.synonymRelations must not be null",synonym.getSynonymRelations());
			assertTrue("Synonym.synonymRelations should contain SynonymRelationship",synonym.getSynonymRelations().contains(synonymRelationship));
			
			assertNotNull("Taxon.lsid should not be null",parent.getLsid());
			assertEquals("Taxon.lsid should equal urn:lsid:example.org:taxonconcept:1",new LSID("urn:lsid:example.org:taxonconcept:1"),parent.getLsid());
	    }
}
