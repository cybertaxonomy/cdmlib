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
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;

public class PresenceAbsenceTermBaseTest {
	private static final Logger logger = Logger.getLogger(PresenceAbsenceTermBaseTest.class);

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
//		String abbrev = "p";
//		PresenceAbsenceTermBase paTerm = PresenceTerm.getPresenceAbsenceTermByAbbreviation(abbrev);
//		assertEquals(PresenceTerm.PRESENT(), paTerm);
//		PresenceAbsenceTermBase paTermNotExist = PresenceTerm.getPresenceAbsenceTermByAbbreviation("slkjlsl�");
//		assertNull(paTermNotExist);
	}
	@Test
    public void testCompareTo(){
        PresenceTerm term1 = PresenceTerm.CULTIVATED();
        PresenceTerm term2 = PresenceTerm.NATIVE();
        AbsenceTerm term3 = AbsenceTerm.ABSENT();
        AbsenceTerm term4 = AbsenceTerm.NATIVE_REPORTED_IN_ERROR();

        List<PresenceAbsenceTermBase> list = new ArrayList<PresenceAbsenceTermBase>();
        list.add(term4);
        list.add(term2);
        list.add(term3);
        list.add(term1);

        Collections.sort(list);
        for (PresenceAbsenceTermBase t: list){
            System.out.println(t.getTitleCache());
        }



    }

}
