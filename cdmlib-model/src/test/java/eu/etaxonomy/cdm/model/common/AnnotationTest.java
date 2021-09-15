/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 */
public class AnnotationTest extends EntityTestBase {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AnnotationTest.class);

	private static final String TEST_URI_STR = "http://test.abc.de";

	private static Annotation annotation1;
	private static Person commentator;
	private static URI linkbackUri;
	private static AnnotatableEntity annotatedObject;

	@Before
	public void setUp() throws Exception {
		commentator = Person.NewInstance();
		commentator.setTitleCache("automatic importer", true);
		annotation1 = Annotation.NewInstance("anno1", Language.DEFAULT());
		annotation1.setCommentator(commentator);
		annotatedObject = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		annotatedObject.addAnnotation(annotation1);
		linkbackUri = new URI("http://www.abc.de");
        annotation1.setLinkbackUri(linkbackUri);
	}

/* ****************** TESTS *************************************/

	@Test
	public void testNewInstanceStringLanguage() {
		assertNotNull(annotation1);
		assertSame(commentator, annotation1.getCommentator());
		assertSame(Language.DEFAULT(), annotation1.getLanguage());
		assertSame(linkbackUri, annotation1.getLinkbackUri());
		assertSame(annotatedObject.getAnnotations().iterator().next(), annotation1);
	}

	@Test
	public void testGetSetCommentator() {
		Person person = Person.NewInstance();
		annotation1.setCommentator(person);
		assertSame(person, annotation1.getCommentator());
		annotation1.setCommentator(null);
		assertNull(annotation1.getCommentator());
	}

	@Test
	public void testGetSetLinkbackUri() {
		URI uri = null;
		try {
			uri = new URI(TEST_URI_STR);
		} catch (URISyntaxException e) {
		    fail();
        }
		annotation1.setLinkbackUri(uri);
		assertSame(uri, annotation1.getLinkbackUri());
		annotation1.setLinkbackUri(null);
		assertNull(annotation1.getLinkbackUri());
	}
}