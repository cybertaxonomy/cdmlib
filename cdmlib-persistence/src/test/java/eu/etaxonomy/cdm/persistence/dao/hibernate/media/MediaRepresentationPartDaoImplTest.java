/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.media;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.hibernate.TypeMismatchException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.persistence.dao.media.IMediaRepresentationPartDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

@DataSet
public class MediaRepresentationPartDaoImplTest extends CdmTransactionalIntegrationTest {

	@SpringBeanByType
	IMediaRepresentationPartDao dao;

	@SpringBeanByType
	IDefinedTermDao definedTermDao;


	@Before
	public void setUp() {
	}

	@Test
	public void testCount() {
		long numberOfMediaRepresentationParts = dao.count();
		assertEquals("count should return 3",3,numberOfMediaRepresentationParts);
	}

	@Test
	public void testFindByUuid() {
		MediaRepresentationPart part = dao.findByUuid(UUID.fromString("e06a0f29-ef82-4ce3-8a94-dd98feae8f9e"));
		Assert.assertNotNull("part should not be null", part);
		try {
			Assert.assertEquals("part URI should be http://www.test.de", new URI("http://www.test.de"), part.getUri());
		} catch (URISyntaxException e) {
			Assert.fail("No URISyntaxException should occurr");
		}

		MediaRepresentationPart part2 = dao.findByUuid(UUID.fromString("a07a0f29-ef82-3ce3-8a94-dd98feae8f9e"));
		Assert.assertNotNull("part2 should not be null", part2);
		try {
			Assert.assertEquals("part URI should be http://www.test2.de", new URI("http://www.test2.de"), part2.getUri());
		} catch (URISyntaxException e) {
			Assert.fail("No URISyntaxException should occurr");
		}

		try {
			MediaRepresentationPart part3 = dao.findByUuid(UUID.fromString("b48a0f26-2f42-3ce3-8a04-dd98feaa8e98"));
			Assert.assertEquals("part URI should be http://www.test2.de", new URI("http://www.test2.de"), part3.getUri());
			Assert.fail("A TypeMismatchException should occurr for http://www.fail.de");
		} catch (Exception e) {
			if (e instanceof TypeMismatchException){
				//OK
			}else{
				Assert.fail("Only an URISyntaxException should occurr but exception was of type " + e.getClass().getName());
			}
		}

	}

	@Test
	@ExpectedDataSet
	public void testSave() {
		try {
			URI uri = new URI("http://www.test3.de");

			MediaRepresentationPart part1 = MediaRepresentationPart.NewInstance(uri, 22);
			//a representation is needed otherwise a not nullable exception is thrown (due to lucene index (?))
			MediaRepresentation representation = MediaRepresentation.NewInstance();
			representation.addRepresentationPart(part1);
			dao.save(part1);
			commit();

		} catch (URISyntaxException e) {
			Assert.fail("URI should be ok");
		}

	}

	@Test
	public void testLongUri() {
		try {
			URI uri = new URI("http://www.test3.de/woieoriuwoirwuwouroiwuowiuoiuwoieuroiuewroiououi/hdsfiuwhuehfhiuhiuhwihiuhew"+
					"wereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+
					"weeeeeeeeeeeeeeeeeeeeeee/eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"+
					"uizweiuzriuzewiurziuewui/weeeeeeeeeeeeeeeeeuiziuzwueziuzweiurziuwzriufsfdds");

			MediaRepresentationPart part1 = MediaRepresentationPart.NewInstance(uri, 22);
			//a representation is needed otherwise a not nullable exception is thrown (due to lucene index (?))
			MediaRepresentation representation = MediaRepresentation.NewInstance();
			representation.addRepresentationPart(part1);
			dao.save(part1);
			commit();

		} catch (URISyntaxException e) {
			Assert.fail("URI should be ok");
		}

	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
