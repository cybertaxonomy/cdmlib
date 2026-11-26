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

import java.io.InputStreamReader;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;

public class AgentsTest {

    private String resource = "/eu/etaxonomy/cdm/io/jaxb/AgentsTest.xml";

    @Test
    public void testUnmarshalAgents() throws Exception {
        CdmDocumentBuilder cdmDocumentBuilder = new CdmDocumentBuilder();
        URI uri = new URI(URIEncoder.encode(this.getClass().getResource(resource).toString()));
        DataSet dataSet = cdmDocumentBuilder.unmarshal(DataSet.class, new InputStreamReader(this.getClass().getResourceAsStream(resource)),uri.toString());

		Person person = (Person)dataSet.getAgents().get(0);
		assertNotNull(person, "Persion should not be null");
		assertEquals("Mill.", person.getNomenclaturalTitle(), "Person.getNomenclaturalTitle should return \'Mill.\'");
		assertEquals("Philip", person.getGivenName(), "Person.getGivenName should return \'Philip\'");
		assertEquals("Miller", person.getFamilyName(), "Person.getFamilyName should return \'Miller\'");
		assertNotNull(person.getLifespan(), "Person.getLifespan should not be null");
		assertNotNull(person.getLifespan().getStart(), "Person.getLifespan.getStart should not be null");
		assertEquals(person.getLifespan().getStart().getYear(), 1691, "Person.getLifespan.getStart should equal 1691");
        assertNotNull(person.getInstitutionalMemberships(), "Person.getInstitutionalMemberships should not be null");
        assertEquals(1, person.getInstitutionalMemberships().size(), "There should be one institutional membership");
        InstitutionalMembership institutionalMembership = person.getInstitutionalMemberships().iterator().next();
        assertEquals(institutionalMembership.getInstitute(), dataSet.getAgents().get(1), "institutionalMembership.getInstitute should return Chelsea Physic Garden");
        assertEquals(institutionalMembership.getPerson(), dataSet.getAgents().get(0), "institutionalMembership.getPerson should return Philip Miller");
    }
}