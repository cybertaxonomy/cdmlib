/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Dummy class. Original class has been moved to model.strategy.
 * This empty class needs to stay here to avoid test error with
 * transient terms.
 * Can be removed once this issue is fixed in the tests
 *
 * @author muellera
 * @since 16.03.2013
 */
@Disabled
public class IdentificationKeyGeneratorTTT {

//*************************** TESTS *********************** /

	@Test
	public void testInvoke() {
		assertNotNull("Key should exist.", "");
	}


}
