/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;

public class PresenceAbsenceTermTest {
	private static final Logger logger = Logger.getLogger(PresenceAbsenceTermTest.class);

	@BeforeClass
    public static void setUpBeforeClass() {
        DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
        vocabularyStore.initialize();
    }
	@Test
	public void testPresenceAbsenceTermBase() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testPresenceAbsenceTermBaseStringStringString() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testReadCsvLineList() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetPresenceTermByAbbreviation() {
		String abbrev = "p";
		PresenceAbsenceTerm paTerm = PresenceAbsenceTerm.getPresenceAbsenceTermByAbbreviation(abbrev);
		Assert.assertEquals(PresenceAbsenceTerm.PRESENT(), paTerm);
		PresenceAbsenceTerm paTermNotExist = PresenceAbsenceTerm.getPresenceAbsenceTermByAbbreviation("slkjlsl�");
		Assert.assertNull(paTermNotExist);
	}
	
	@Test
    public void testCompareTo(){
        PresenceAbsenceTerm term1 = PresenceAbsenceTerm.CULTIVATED();
        PresenceAbsenceTerm term2 = PresenceAbsenceTerm.NATIVE();
        PresenceAbsenceTerm term3 = PresenceAbsenceTerm.ABSENT();
        PresenceAbsenceTerm term4 = PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR();

        List<PresenceAbsenceTerm> list = new ArrayList<PresenceAbsenceTerm>();
        list.add(term4);
        list.add(term2);
        list.add(term3);
        list.add(term1);

        Collections.sort(list);
        for (PresenceAbsenceTerm t: list){
            System.out.println(t.getTitleCache());
        }



    }

}
