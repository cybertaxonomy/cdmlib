/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * @author n.hoffmann
 * @created Mar 9, 2011
 */
public class GrantedAuthorityImplTest {

	private GrantedAuthorityImpl grantedAuthority;
	private String authority;

	@Before
	public void setUp() throws Exception {
		grantedAuthority = GrantedAuthorityImpl.NewInstance(null);
		authority = "";
	}

	@Test
	public final void testNewInstance() {
		assertNotNull(grantedAuthority);
	}

	@Test
	public final void testGetAuthority() {
		grantedAuthority.setAuthority(authority);

		assertEquals(authority, grantedAuthority.getAuthority());
	}

	@Test
	public final void testCompareTo() {
		// not implemented yet
	}

}
