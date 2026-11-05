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
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.DefinedTerm;

public class SpecimenTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/SpecimenTest.xml";

    @Test
    public void testUnmarshalSpecimen() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());
//	        List<SpecimenOrObservationBase> occurrences = dataSet.getOccurrences();

		DerivedUnit specimen = (DerivedUnit)dataSet.getOccurrences().get(0);
		assertNotNull(specimen, "Specimen must not be null");

		Institution institution = (Institution)dataSet.getAgents().get(0);
		assertNotNull(institution, "Institution must not be null");
		Person person = (Person)dataSet.getAgents().get(1);
		assertNotNull(person, "Person must not be null");
		Taxon taxon = (Taxon)dataSet.getTaxonBases().get(0);
		assertNotNull(taxon, "Taxon must not be null");
		TaxonName name = dataSet.getTaxonomicNames().get(0);
		assertNotNull(name, "TaxonName must not be null");
		DefinedTerm sex = (DefinedTerm)dataSet.getTerms().get(1);

		Collection collection = dataSet.getCollections().get(0);
		assertNotNull(collection, "Collection must not be null");

		FieldUnit fieldUnit = (FieldUnit)dataSet.getOccurrences().get(1);
		assertNotNull(fieldUnit, "FieldUnit must not be null");
		assertEquals(collection, specimen.getCollection(), "Specimen.collection must equal Collection");
		assertEquals(institution, collection.getInstitute(), "Collection.institute must equal Institution");

		assertEquals(sex, specimen.getSex(), "Sex must equal Specimen.sex");

		assertNotNull(specimen.getDeterminations(), "Specimen.determinations must not be null");
		assertFalse(specimen.getDeterminations().isEmpty(), "Specimen.determinations must not be empty");
		DeterminationEvent determination = specimen.getDeterminations().iterator().next();
		assertEquals(person, determination.getActor(), "Person must equal Determination.actor");

		GatheringEvent gatheringEvent = (GatheringEvent)dataSet.getEventBases().get(0);
		assertNotNull(gatheringEvent, "GatheringEvent must not be null");

		DerivationEvent derivationEvent = (DerivationEvent)dataSet.getEventBases().get(1);
		assertNotNull(derivationEvent, "DerivationEvent must not be null");

		assertEquals(gatheringEvent, fieldUnit.getGatheringEvent(), "GatheringEvent must be equal to FieldUnit.getGatheringEvent()");
		assertTrue(derivationEvent.getDerivatives().contains(specimen), "DerivationEvent.derivatives must contain Specimen");
		assertEquals(derivationEvent, specimen.getDerivedFrom(), "DerivationEvent must equal Specimen.derivedFrom");
    }
}
