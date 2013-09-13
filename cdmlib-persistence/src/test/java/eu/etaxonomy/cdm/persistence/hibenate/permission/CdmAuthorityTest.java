/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.hibenate.permission;

import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import sun.security.provider.PolicyParser.ParsingException;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;

/**
 * @author c.mathew
 * @created Mar 26, 2013
 * @version 1.0
 */
public class CdmAuthorityTest {

	private GrantedAuthorityImpl grantedAuthority;
	private String authority;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		grantedAuthority = GrantedAuthorityImpl.NewInstance();
		authority = "";
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.persistence.hibernate.permission#getAuthority()}.
	 * @throws ParsingException 
	 */
	@Test
	public final void testGetAuthority() throws ParsingException {
		// create CdmAuthority object manually
		CdmPermissionClass tnClass = CdmPermissionClass.TAXONBASE;
		String property = "Taxon";
		EnumSet<CRUD> operation = EnumSet.noneOf(CRUD.class);
		operation.add(CRUD.READ);
		operation.add(CRUD.UPDATE);
		UUID uuid = UUID.fromString("e0358c98-4222-4d17-811c-7ce18bd565ee");
		CdmAuthority cdma = new CdmAuthority(tnClass,property, operation, uuid);
		String expectedAuthority = "TAXONBASE(Taxon).[READ, UPDATE]{e0358c98-4222-4d17-811c-7ce18bd565ee}";
		System.out.println(cdma.getAuthority());
		System.out.println(expectedAuthority);
		// check object getAuthority with expectedAuthority
		assertEquals(expectedAuthority, cdma.getAuthority());
		//check programmatic generation of CdmAuthority by parsing authority string
		cdma = CdmAuthority.fromGrantedAuthority(cdma);		
		assertEquals(expectedAuthority, cdma.getAuthority());
	}



}

