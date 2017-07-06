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

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class SpecimenTest {

	    private String resource = "/eu/etaxonomy/cdm/io/jaxb/SpecimenTest.xml";

	    @Test
	    public void testUnmarshalSpecimen() throws Exception {
	        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
	        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
	        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
//	        List<SpecimenOrObservationBase> occurrences = dataSet.getOccurrences();

			DerivedUnit specimen = (DerivedUnit)dataSet.getOccurrences().get(0);
			assertNotNull("Specimen must not be null",specimen);

			Institution institution = (Institution)dataSet.getAgents().get(0);
			assertNotNull("Institution must not be null",institution);
			Person person = (Person)dataSet.getAgents().get(1);
			assertNotNull("Person must not be null", person);
			Taxon taxon = (Taxon)dataSet.getTaxonBases().get(0);
			assertNotNull("Taxon must not be null",taxon);
			TaxonName name = dataSet.getTaxonomicNames().get(0);
			assertNotNull("TaxonName must not be null",name);
			DefinedTerm sex = (DefinedTerm)dataSet.getTerms().get(1);

			Collection collection = dataSet.getCollections().get(0);
			assertNotNull("Collection must not be null", collection);

			FieldUnit fieldUnit = (FieldUnit)dataSet.getOccurrences().get(1);
			assertNotNull("FieldUnit must not be null", fieldUnit);
			assertEquals("Specimen.collection must equal Collection",collection, specimen.getCollection());
			assertEquals("Collection.institute must equal Institution",institution,collection.getInstitute());

			assertEquals("TaxonName must equal Specimen.storedUnder",name,specimen.getStoredUnder());
			assertEquals("Sex must equal Specimen.sex",sex,specimen.getSex());

			assertNotNull("Specimen.determinations must not be null",specimen.getDeterminations());
			assertFalse("Specimen.determinations must not be empty",specimen.getDeterminations().isEmpty());
			DeterminationEvent determination = specimen.getDeterminations().iterator().next();
			assertEquals("Person must equal Determination.actor",person,determination.getActor());

			GatheringEvent gatheringEvent = (GatheringEvent)dataSet.getEventBases().get(0);
			assertNotNull("GatheringEvent must not be null",gatheringEvent);

			DerivationEvent derivationEvent = (DerivationEvent)dataSet.getEventBases().get(1);
			assertNotNull("DerivationEvent must not be null",derivationEvent);

			assertEquals("GatheringEvent must be equal to FieldUnit.getGatheringEvent()",gatheringEvent, fieldUnit.getGatheringEvent());
			assertTrue("DerivationEvent.derivatives must contain Specimen",derivationEvent.getDerivatives().contains(specimen));
			assertEquals("DerivationEvent must equal Specimen.derivedFrom",derivationEvent,specimen.getDerivedFrom());
	    }
}
