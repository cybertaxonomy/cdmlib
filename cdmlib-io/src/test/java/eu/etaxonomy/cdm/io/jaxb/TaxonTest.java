package eu.etaxonomy.cdm.io.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.net.URI;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

public class TaxonTest {
		
	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TaxonTest.xml";
	    
	    @Test
	    public void testUnmarshalTaxon() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = null;
	        dataSet = cdmDocumentBuilder.unmarshal(dataSet, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
			
			Taxon parent = (Taxon)dataSet.getTaxonBases().get(0);	
			assertNotNull("Taxon must not be null",parent);
			Taxon child = (Taxon)dataSet.getTaxonBases().get(1);
			assertNotNull("Taxon must not be null", child);
			
			assertNotNull("Taxon.relationsFromThisTaxon should not be null", parent.getRelationsFromThisTaxon());
			assertFalse("Taxon.relationsFromThisTaxon should not be empty",parent.getRelationsFromThisTaxon().isEmpty());
			TaxonRelationship taxonRelationship = parent.getRelationsFromThisTaxon().iterator().next();
			assertNotNull("TaxonRelationship.fromTaxon must not be null",taxonRelationship.getFromTaxon());
			assertEquals("parent Taxon should equal TaxonRelationship.fromTaxon",parent,taxonRelationship.getFromTaxon());
			assertNotNull("TaxonRelationship.toTaxon must not be null", taxonRelationship.getToTaxon());
			assertEquals("TaxonRelationship.toTaxon should equal child Taxon",child,taxonRelationship.getToTaxon());
			assertNotNull("Taxon.relationsToThisTaxon must not be null",child.getRelationsToThisTaxon());
			assertTrue("child Taxon.relationsToThisTaxon should contain TaxonRelationship",child.getRelationsToThisTaxon().contains(taxonRelationship));
			
			assertNotNull("Taxon.lsid should not be null",parent.getLsid());
			assertEquals("Taxon.lsid should equal urn:lsid:example.org:taxonconcept:1",new LSID("urn:lsid:example.org:taxonconcept:1"),parent.getLsid());
	    }
}
