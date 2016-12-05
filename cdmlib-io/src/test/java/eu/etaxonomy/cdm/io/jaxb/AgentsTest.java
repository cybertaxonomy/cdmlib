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

import java.io.InputStreamReader;
import java.net.URI;

import org.joda.time.DateTimeFieldType;
import org.junit.Test;

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
		assertNotNull("Persion should not be null",person);
		assertEquals("Person.getNomenclaturalTitle should return \'Mill.\'","Mill.",person.getNomenclaturalTitle());
		assertEquals("Person.getFirstName should return \'Philip\'","Philip",person.getFirstname());
		assertEquals("Person.getLastName should return \'Miller\'","Miller",person.getLastname());
		assertNotNull("Person.getLifespan should not be null",person.getLifespan());
		assertNotNull("Person.getLifespan.getStart should not be null",person.getLifespan().getStart());
		assertEquals("Person.getLifespan.getStart should equal 1691", person.getLifespan().getStart().get(DateTimeFieldType.year()),1691);
        assertNotNull("Person.getInstitutionalMemberships should not be null",person.getInstitutionalMemberships());
        assertEquals("There should be one institutional membership",1,person.getInstitutionalMemberships().size());
        InstitutionalMembership institutionalMembership = person.getInstitutionalMemberships().iterator().next();
        assertEquals("institutionalMembership.getInstitute should return Chelsea Physic Garden",institutionalMembership.getInstitute(),dataSet.getAgents().get(1));
        assertEquals("institutionalMembership.getPerson should return Philip Miller",institutionalMembership.getPerson(),dataSet.getAgents().get(0));

    }
}
