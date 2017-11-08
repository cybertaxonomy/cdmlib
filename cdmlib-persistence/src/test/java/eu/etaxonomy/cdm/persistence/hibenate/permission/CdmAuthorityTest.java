/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibenate.permission;

import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;

/**
 * @author c.mathew
 * @created Mar 26, 2013
 */
public class CdmAuthorityTest {

	private GrantedAuthorityImpl grantedAuthority;
	private String authority;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		grantedAuthority = GrantedAuthorityImpl.NewInstance(null);
		authority = "";
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.hibernate.permission#getAuthority()}.
	 * @throws CdmAuthorityParsingException
	 */
	@Test
	public final void testGetAuthority() throws CdmAuthorityParsingException {
		// create CdmAuthority object manually
		CdmPermissionClass tnClass = CdmPermissionClass.TAXONBASE;
		String property = "Taxon";
		EnumSet<CRUD> operation = EnumSet.noneOf(CRUD.class);
		operation.add(CRUD.READ);
		operation.add(CRUD.UPDATE);
		UUID uuid = UUID.fromString("e0358c98-4222-4d17-811c-7ce18bd565ee");
		CdmAuthority cdmAuth = new CdmAuthority(tnClass, property, operation, uuid);
		String expectedAuthority = "TAXONBASE(Taxon).[READ,UPDATE]{e0358c98-4222-4d17-811c-7ce18bd565ee}";
//		System.out.println(cdmAuth.getAuthority());
//		System.out.println(expectedAuthority);
		// check object getAuthority with expectedAuthority
		assertEquals(expectedAuthority, cdmAuth.getAuthority());
		//check programmatic generation of CdmAuthority by parsing authority string
		cdmAuth = CdmAuthority.fromGrantedAuthority(cdmAuth);
		assertEquals(expectedAuthority, cdmAuth.getAuthority());
	}

	@Test
	public final void testparse_1() throws CdmAuthorityParsingException {
	    TestCdmAuthority auth = new TestCdmAuthority();
	    String[] tokens = auth.parseForTest("TAXONBASE(Taxon).[READ,UPDATE]{e0358c98-4222-4d17-811c-7ce18bd565ee}");
	    assertEquals("TAXONBASE", tokens[0]);
	    assertEquals("Taxon", tokens[1]);
        assertEquals("READ,UPDATE", tokens[2]);
        assertEquals("e0358c98-4222-4d17-811c-7ce18bd565ee", tokens[3]);
	}

	@Test
    public final void testparse_2() throws CdmAuthorityParsingException {
        TestCdmAuthority auth = new TestCdmAuthority();
        String[] tokens = auth.parseForTest("TAXONBASE(Foo,Bar).[READ,UPDATE]{e0358c98-4222-4d17-811c-7ce18bd565ee}");
        assertEquals("TAXONBASE", tokens[0]);
        assertEquals("Foo,Bar", tokens[1]);
        assertEquals("READ,UPDATE", tokens[2]);
        assertEquals("e0358c98-4222-4d17-811c-7ce18bd565ee", tokens[3]);
    }

	@Test
    public final void testparse_3() throws CdmAuthorityParsingException {
        TestCdmAuthority auth = new TestCdmAuthority();
        String[] tokens = auth.parseForTest("REGISTRATION(PREPARATION,READY).[UPDATE]{e0358c98-4222-4d17-811c-7ce18bd565ee}");
        assertEquals("REGISTRATION", tokens[0]);
        assertEquals("PREPARATION,READY", tokens[1]);
        assertEquals("UPDATE", tokens[2]);
        assertEquals("e0358c98-4222-4d17-811c-7ce18bd565ee", tokens[3]);
    }

	/**
     * Without whitespace in "[UPDATE,DELETE]"
     *
     * see https://dev.e-taxonomy.eu/redmine/issues/7027
     *
     * @throws CdmAuthorityParsingException
     */
    @Test
	public final void testFromString_issue7027_A() throws CdmAuthorityParsingException {
        TestCdmAuthority auth = new TestCdmAuthority("REGISTRATION(PREPARATION,READY).[UPDATE, DELETE]{e0358c98-4222-4d17-811c-7ce18bd565ee}");
        assertEquals(CdmPermissionClass.REGISTRATION, auth.getPermissionClass());
        assertEquals("PREPARATION,READY", auth.getProperty());
        assertEquals(EnumSet.of(CRUD.UPDATE, CRUD.DELETE), auth.getOperation());
        assertEquals("e0358c98-4222-4d17-811c-7ce18bd565ee", auth.getTargetUUID().toString());
    }

	/**
	 * With whitespace in "[UPDATE, DELETE]"
	 *
     * see https://dev.e-taxonomy.eu/redmine/issues/7027
	 *
	 * @throws CdmAuthorityParsingException
	 */
	@Test
	public final void testFromString_issue7027_B() throws CdmAuthorityParsingException {
        TestCdmAuthority auth = new TestCdmAuthority("REGISTRATION(PREPARATION,READY).[UPDATE,DELETE]{e0358c98-4222-4d17-811c-7ce18bd565ee}");
        assertEquals(CdmPermissionClass.REGISTRATION, auth.getPermissionClass());
        assertEquals("PREPARATION,READY", auth.getProperty());
        assertEquals(EnumSet.of(CRUD.UPDATE, CRUD.DELETE), auth.getOperation());
        assertEquals("e0358c98-4222-4d17-811c-7ce18bd565ee", auth.getTargetUUID().toString());
    }


	@SuppressWarnings("serial")
    class TestCdmAuthority extends CdmAuthority {

	    public TestCdmAuthority() {
	        // just create a dummy instance
	        super(CdmPermissionClass.REGISTRATION, Operation.UPDATE);
	    }

	    public TestCdmAuthority(String string) throws CdmAuthorityParsingException {
	        super(string);
	    }

	    public String[] parseForTest(String authority) throws CdmAuthorityParsingException {
	        return super.parse(authority);
	    }

	}



}

