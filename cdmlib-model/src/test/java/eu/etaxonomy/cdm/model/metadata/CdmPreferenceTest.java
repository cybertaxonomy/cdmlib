/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author a.mueller
 * @since 2013-07-03
 */
public class CdmPreferenceTest {

	private String subject;
	private String predicate;
	private String value;

	@BeforeEach
	public void setUp() throws Exception {
		subject = "/";
		predicate = "eu.etaxonomy.cdm.model.name.NomenclaturalCode";
		value = "ICZN";
	}

	@Test
	public void testCdmPreferences() {
		CdmPreference prefs = new CdmPreference(subject, predicate, value);
		assertEquals(subject, prefs.getSubjectString());
		assertEquals(predicate, prefs.getPredicate());
		assertEquals(value, prefs.getValue());
	}

	@Test
	public void testConstraints() {
		String veryLongText100 =
"This is a very long text it is even longer then any other long text which is already long. size =100";
		String veryLongText200 = veryLongText100 + veryLongText100;
		String veryLongText400 = veryLongText200 + veryLongText200;
		String veryLongText1200 = veryLongText400 + veryLongText400 + veryLongText400;

		CdmPreference prefs = null;
		try {
			prefs = new CdmPreference(null, predicate, value);
			fail("Subject must not be null");
		} catch (Exception e) {
			//ok
		}
		try {
			prefs = new CdmPreference(veryLongText400, predicate, value);
			fail("Subject must not be longer then 255");
		} catch (Exception e) {
			//ok
		}

		try {
			prefs = new CdmPreference(subject, null, value);
			fail("Predicate must not be null");
		} catch (Exception e) {
			//ok
		}
		try {
			prefs = new CdmPreference(subject, veryLongText400, value);
			fail("Predicate must not be longer then 255");
		} catch (Exception e) {
			//ok
		}

		try {
			prefs = new CdmPreference(subject, predicate, null);
	         assertEquals(subject, prefs.getSubjectString());
	         assertEquals(predicate, prefs.getPredicate());
	         assertNull(null, value);
		} catch (Exception e) {
			fail("Currently null values are allowed in preferences");

		}
		//commented as we allow >65k length now
//		try {
//			prefs = new CdmPreference(subject, predicate, veryLongText1200);
//			fail("Value must not be longer then 1023");
//		} catch (Exception e) {
//			//ok
//		}
	}

    @Test
    public void testSubjectSyntax() {
        CdmPreference prefs = null;
        String subject2 = null;
        try {
            subject2 = "/TaxonNode[#t1#18681#6392#5358#9#2#]/";
            prefs = new CdmPreference(subject2, predicate, value);
            assertEquals(subject2, prefs.getSubjectString());
            assertEquals(predicate, prefs.getPredicate());
            assertEquals(value, prefs.getValue());
        } catch (Exception e) {
            fail("Syntax for " + subject2 + " should be ok");
        }

        try {
            subject2 = "/TaxonNode[#t1#18681#6392#5358#9#2#]x";
            prefs = new CdmPreference(subject2, predicate, value);
            fail("Syntax for " + subject2 + " should fail");
        } catch (Exception e) {
            //ok
        }

        try {
            subject2 = "/NamedArea[111bdf38-7a32-440a-9808-8af1c9e54b51]/";
            prefs = new CdmPreference(subject2, predicate, value);
            assertEquals(subject2, prefs.getSubjectString());
            assertEquals(predicate, prefs.getPredicate());
            assertEquals(value, prefs.getValue());
        } catch (Exception e) {
            fail();
        }

        try {
            subject2 = "/Taxon3Node[#t1#18681#6392#5358#9#2#]";
            prefs = new CdmPreference(subject2, predicate, value);
            fail("Syntax for " + subject2 + " should fail");
        } catch (Exception e) {
           //ok
        }

        try {
            prefs = CdmPreference.NewVaadinInstance(PreferencePredicate.AvailableDistributionStatus, value);
        } catch (Exception e) {
            fail("Syntax for /Vaadin/ should not fail");
        }

    }

    @Test
    public void testGetValueUuidList() {
        //null
        CdmPreference prefs = new CdmPreference(subject, predicate, null);
        List<UUID> list = prefs.getValueUuidList();
        assertTrue(list.isEmpty());
        //empty
        prefs = new CdmPreference(subject, predicate, " ");
        list = prefs.getValueUuidList();
        assertTrue(list.isEmpty());
        //singleUuid
        prefs = new CdmPreference(subject, predicate, "1ef35078-fb4a-451d-a15a-127235245358");
        list = prefs.getValueUuidList();
        assertEquals(1, list.size());
        assertEquals(UUID.fromString("1ef35078-fb4a-451d-a15a-127235245358"), list.get(0));
        //mulitple uuids and varying separators
        prefs = new CdmPreference(subject, predicate, "1ef35078-fb4a-451d-a15a-127235245358,a1184db8-e476-4410-b085-d8844ada47e8;43b2cd7d-401b-4565-853f-88ae1c43c55a");
        list = prefs.getValueUuidList();
        assertEquals(3, list.size());
        assertEquals(UUID.fromString("1ef35078-fb4a-451d-a15a-127235245358"), list.get(0));
        assertEquals(UUID.fromString("43b2cd7d-401b-4565-853f-88ae1c43c55a"), list.get(2));
        //trailing and preceding separators and whitespaces
        prefs = new CdmPreference(subject, predicate, " ; 1ef35078-fb4a-451d-a15a-127235245358 , a1184db8-e476-4410-b085-d8844ada47e8;43b2cd7d-401b-4565-853f-88ae1c43c55a , ");
        list = prefs.getValueUuidList();
        assertEquals(3, list.size());
        assertEquals(UUID.fromString("1ef35078-fb4a-451d-a15a-127235245358"), list.get(0));
        assertEquals(UUID.fromString("43b2cd7d-401b-4565-853f-88ae1c43c55a"), list.get(2));
        //non uuids
        prefs = new CdmPreference(subject, predicate, "xxx 1ef35078-fb4a-451d-a15a-127235245358");
        try {
            list = prefs.getValueUuidList();
            fail("Parsing non UUIDs must throw exception");
        } catch (IllegalArgumentException e) {
            //correct
        }
    }


}
