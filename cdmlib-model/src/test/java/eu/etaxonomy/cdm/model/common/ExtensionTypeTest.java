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

import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 08.06.2009
 */
public class ExtensionTypeTest extends EntityTestBase {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExtensionTypeTest.class);

//****************** TESTS *******************************************************/

	@Test
	public void testRDF(){
		UUID uuidRdf = UUID.fromString("f3684e25-dcad-4c1e-a5d8-16cddf1c4f5b");
		ExtensionType rdfExtension = ExtensionType.getTermByUuid(uuidRdf);
		Assert.notNull(rdfExtension, "RdfExtension must not be null");
		assertEquals("Wrong label for extension type rdf", "RDF",rdfExtension.getRepresentation(Language.ENGLISH()).getAbbreviatedLabel());
	}

	@Test
	public void testDOI(){
		ExtensionType doiExtension = ExtensionType.DOI();
		Assert.notNull(doiExtension, "DoiExtension must not be null");
		assertEquals("Wrong label for extension type rdf", "DOI",doiExtension.getLabel());
	}
}