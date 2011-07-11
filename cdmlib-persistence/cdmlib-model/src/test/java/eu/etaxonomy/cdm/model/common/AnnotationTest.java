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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 *
 */
public class AnnotationTest extends EntityTestBase {
	private static final Logger logger = Logger.getLogger(AnnotationTest.class);
	
	private static Annotation annotation1; 
	private static Person commentator;
	private static URL linkbackUrl;
	private static AnnotatableEntity annotatedObject;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		commentator = Person.NewInstance();
		commentator.setTitleCache("automatic importer", true);
		annotation1 = Annotation.NewInstance("anno1", Language.DEFAULT());
		annotation1.setCommentator(commentator);
		annotatedObject = BotanicalName.NewInstance(Rank.SPECIES());
		annotatedObject.addAnnotation(annotation1);
		try {
			linkbackUrl = new URL("http:\\www.abc.de");
			annotation1.setLinkbackUrl(linkbackUrl);
		} catch (MalformedURLException e) {
			logger.warn("MalformedURLException");
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/* ****************** TESTS *************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#Annotation(java.lang.String, eu.etaxonomy.cdm.model.common.Language)}.
	 */
	@Test
	public void testNewInstanceStringLanguage() {
		assertNotNull(annotation1);
		assertSame(commentator, annotation1.getCommentator());
		assertSame(Language.DEFAULT(), annotation1.getLanguage());
		assertSame(linkbackUrl, annotation1.getLinkbackUrl());
		assertSame(annotatedObject.getAnnotations().iterator().next(), annotation1);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#getAnnotatedObj()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#setAnnotatedObj(eu.etaxonomy.cdm.model.common.AnnotatableEntity)}.
	 */
	@Test
	public void testGetSetAnnotatedObj() {
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		AnnotatableEntity database = refFactory.newDatabase();
		annotation1.setAnnotatedObj(database);
		assertSame(database, annotation1.getAnnotatedObj());
		annotation1.setAnnotatedObj(null);
		assertNull(annotation1.getAnnotatedObj());
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#getCommentator()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#setCommentator(eu.etaxonomy.cdm.model.agent.Person)}.
	 */
	@Test
	public void testGetSetCommentator() {
		Person person = Person.NewInstance();
		annotation1.setCommentator(person);
		assertSame(person, annotation1.getCommentator());
		annotation1.setCommentator(null);
		assertNull(annotation1.getCommentator());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#getLinkbackUrl()}.
	 * Test method for {@link eu.etaxonomy.cdm.model.common.Annotation#setLinkbackUrl(java.net.URL)}.
	 */
	@Test
	public void testGetSetLinkbackUrl() {
		URL url = null;
		try {
			url = new URL("http:\\test.abc.de");
		} catch (MalformedURLException e) {
			fail();
		}
		annotation1.setLinkbackUrl(url);
		assertSame(url, annotation1.getLinkbackUrl());
		annotation1.setLinkbackUrl(null);
		assertNull(annotation1.getLinkbackUrl());
	}

}
