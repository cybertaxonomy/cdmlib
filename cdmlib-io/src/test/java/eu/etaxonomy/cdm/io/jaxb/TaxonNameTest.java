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
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;

public class TaxonNameTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/TaxonNameTest.xml";

    @Test
    public void testUnmarshalName() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		IBotanicalName botanicalName = dataSet.getTaxonomicNames().get(0);
		assertNotNull(botanicalName, "BotanicalName must not be null");
		DerivedUnit specimen = (DerivedUnit)dataSet.getOccurrences().get(0);

		assertNotNull(botanicalName.getTypeDesignations(), "BotanicalName.typeDesignations must not be null");
		assertFalse(botanicalName.getTypeDesignations().isEmpty(), "BotanicalName.typeDesignations must contain TypeDesignation entities");

		SpecimenTypeDesignation specimenTypeDesignation = (SpecimenTypeDesignation)botanicalName.getTypeDesignations().iterator().next();
		assertNotNull(specimenTypeDesignation.getTypifiedNames(), "SpecimenTypeDesignation.typifiedNames must not be null");
		assertFalse(specimenTypeDesignation.getTypifiedNames().isEmpty(), "SpecimenTypeDesignation.typifiedNames must not be empty");
		assertTrue(specimenTypeDesignation.getTypifiedNames().contains(botanicalName), "SpecimenTypeDesignation.typifiedNames must contain BotanicalName");

		assertNotNull(specimenTypeDesignation.getTypeSpecimen(), "SpecimenTypeDesignation.typeSpecimen must not be null");
		assertEquals(specimen, specimenTypeDesignation.getTypeSpecimen(), "SpecimenTypeDesignation.typeSpecimen must equal Specimen");

		IZoologicalName zoologicalName = dataSet.getTaxonomicNames().get(5);
		assertNotNull(zoologicalName, "ZoologicalName must not be null");

		NameTypeDesignation nameTypeDesignation = (NameTypeDesignation)zoologicalName.getTypeDesignations().iterator().next();
		assertNotNull(nameTypeDesignation.getTypeName(), "NameTypeDesignation.typeName must not be null");
		assertEquals(dataSet.getTaxonomicNames().get(6), nameTypeDesignation.getTypeName(), "NameTypeDesignation.typeName must equal ZoologicalName");

    }
}
