// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author a.mueller
 * @created 2013-07-03
 *
 */
public class CdmPreferencesTest {

	private String subject;
	private String predicate;
	private String value;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		subject = "database";
		predicate = "eu.etaxonomy.cdm.model.name.NomenclaturalCode";
		value = "ICZN";
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmPreference#CdmPreferences(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testCdmPreferences() {
		CdmPreference prefs = new CdmPreference(subject, predicate, value);
		Assert.assertEquals(subject, prefs.getSubject());
		Assert.assertEquals(predicate, prefs.getPredicate());
		Assert.assertEquals(value, prefs.getValue());
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
			Assert.fail("Subject must not be null");
		} catch (Exception e) {
			//ok
		}
		try {
			prefs = new CdmPreference(veryLongText400, predicate, value);
			Assert.fail("Subject must not be longer then 255");
		} catch (Exception e) {
			//ok
		}
		
		try {
			prefs = new CdmPreference(subject, null, value);
			Assert.fail("Predicate must not be null");
		} catch (Exception e) {
			//ok
		}
		try {
			prefs = new CdmPreference(subject, veryLongText400, value);
			Assert.fail("Predicate must not be longer then 255");
		} catch (Exception e) {
			//ok
		}
		
		try {
			prefs = new CdmPreference(subject, predicate, null);
		} catch (Exception e) {
			Assert.fail("Currently null values are allowed in preferences");
			Assert.assertEquals(subject, prefs.getSubject());
			Assert.assertEquals(predicate, prefs.getPredicate());
			Assert.assertNull(value, null);
		}
		try {
			prefs = new CdmPreference(subject, predicate, veryLongText1200);
			Assert.fail("Value must not be longer then 1023");
		} catch (Exception e) {
			//ok
		}
		
		

	}

}
