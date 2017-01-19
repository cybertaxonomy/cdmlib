/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.molecular;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

public class AmplificationTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AmplificationTest.class);

	private UUID uuid = UUID.fromString("11e6b2d5-3eb5-4434-9c56-5bb4c1102147");
	
	@Test
	public void testUpdateCache() {
		Amplification amplification = Amplification.NewInstance();
		amplification.setUuid(uuid);
		
		amplification.updateCache();
		Assert.assertEquals("<Amplification:11e6b2d5-3eb5-4434-9c56-5bb4c1102147>", amplification.getLabelCache());
		
		Person author = Person.NewTitledInstance("Person");
		Institution institution = Institution.NewInstance();
		institution.setName("My institute");
		DefinedTerm marker = DefinedTerm.NewDnaMarkerInstance("marker", "marker", "dm");
		
		amplification.setActor(author);
		amplification.setTimeperiod(TimePeriodParser.parseString("2008"));
		amplification.setDnaMarker(marker);
		amplification.setInstitution(institution);
		
		amplification.updateCache();
		Assert.assertEquals("My institute_Person_marker_2008", amplification.getLabelCache());
	}

}
