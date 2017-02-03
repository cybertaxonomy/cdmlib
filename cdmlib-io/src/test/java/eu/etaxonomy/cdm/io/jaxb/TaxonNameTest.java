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
			assertNotNull("BotanicalName must not be null",botanicalName);
			DerivedUnit specimen = (DerivedUnit)dataSet.getOccurrences().get(0);

			assertNotNull("BotanicalName.typeDesignations must not be null",botanicalName.getTypeDesignations());
			assertFalse("BotanicalName.typeDesignations must contain TypeDesignation entities", botanicalName.getTypeDesignations().isEmpty());

			SpecimenTypeDesignation specimenTypeDesignation = (SpecimenTypeDesignation)botanicalName.getTypeDesignations().iterator().next();
			assertNotNull("SpecimenTypeDesignation.typifiedNames must not be null",specimenTypeDesignation.getTypifiedNames());
			assertFalse("SpecimenTypeDesignation.typifiedNames must not be empty",specimenTypeDesignation.getTypifiedNames().isEmpty());
			assertTrue("SpecimenTypeDesignation.typifiedNames must contain BotanicalName",specimenTypeDesignation.getTypifiedNames().contains(botanicalName));

			assertNotNull("SpecimenTypeDesignation.typeSpecimen must not be null",specimenTypeDesignation.getTypeSpecimen());
			assertEquals("SpecimenTypeDesignation.typeSpecimen must equal Specimen",specimen,specimenTypeDesignation.getTypeSpecimen());

			IZoologicalName zoologicalName = dataSet.getTaxonomicNames().get(5);
			assertNotNull("ZoologicalName must not be null",zoologicalName);

			NameTypeDesignation nameTypeDesignation = (NameTypeDesignation)zoologicalName.getTypeDesignations().iterator().next();
			assertNotNull("NameTypeDesignation.typeName must not be null",nameTypeDesignation.getTypeName());
			assertEquals("NameTypeDesignation.typeName must equal ZoologicalName",dataSet.getTaxonomicNames().get(6),nameTypeDesignation.getTypeName());

	    }
}
