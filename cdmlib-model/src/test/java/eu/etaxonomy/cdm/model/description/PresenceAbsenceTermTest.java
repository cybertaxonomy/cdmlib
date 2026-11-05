/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Iterator;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

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
		assertEquals(PresenceAbsenceTerm.PRESENT(), paTerm);
		PresenceAbsenceTerm paTermNotExist = PresenceAbsenceTerm.getPresenceAbsenceTermByAbbreviation("slkjlsl�");
		assertNull(paTermNotExist);
	}

	@Test
    public void testCompareTo(){
        PresenceAbsenceTerm term1 = PresenceAbsenceTerm.CULTIVATED();
        PresenceAbsenceTerm term2 = PresenceAbsenceTerm.NATIVE();
        PresenceAbsenceTerm term3 = PresenceAbsenceTerm.ABSENT();
        PresenceAbsenceTerm term4 = PresenceAbsenceTerm.NATIVE_REPORTED_IN_ERROR();

        TreeSet<PresenceAbsenceTerm> list = new TreeSet<>();
        list.add(term1);
        list.add(term3);
        list.add(term2);
        list.add(term4);

        Iterator<PresenceAbsenceTerm> it = list.iterator();
        assertEquals(term4, it.next());
        assertEquals(term3, it.next());
        assertEquals(term1, it.next());
        assertEquals(term2, it.next());
    }
}
