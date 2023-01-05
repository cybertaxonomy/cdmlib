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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;

public class PresenceAbsenceTermTest extends EntityTestBase {

    private static final Logger logger = LogManager.getLogger();

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

        List<PresenceAbsenceTerm> list = new ArrayList<>();
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