/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class AnnotationDaoTest extends CdmIntegrationTest {

	@SpringBeanByType
	private IAnnotationDao annotationDao;

	private UUID uuid;

	@Before
	public void setUp() {
		uuid = UUID.fromString("97097410-a112-4dde-a2c6-0096754076b5");
	}

	@Test
	public void testCountAnnotations() {
		Annotation annotatedObj = annotationDao.findByUuid(uuid);
		assert annotatedObj != null : "annotatedObj must exist";

		int numberOfAnnotations = annotationDao.countAnnotations(annotatedObj, null);
		assertEquals("countAnnotations should return 4",4,numberOfAnnotations);
	}

	@Test
	public void testGetAnnotations() {
		Annotation annotatedObj = annotationDao.findByUuid(uuid);
		assert annotatedObj != null : "annotatedObj must exist";
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("created", SortOrder.ASCENDING));
		List<String> propertyPaths = new ArrayList<String>();
//		propertyPaths.add("annotatedObj");
		propertyPaths.add("createdBy");

		List<Annotation> annotations = annotationDao.getAnnotations(annotatedObj, null,null,null,orderHints,propertyPaths);
		assertNotNull("getAnnotations should return a List",annotations);
		assertFalse("the list should contain Annotation instances",annotations.isEmpty());
		assertEquals("getAnnotations should return 4",4,annotations.size());
	}

	@Test
	public void testCountAnnotationsWithStatus() {
		Annotation annotatedObj = annotationDao.findByUuid(uuid);
		MarkerType markerType = MarkerType.TO_BE_CHECKED();

		assert annotatedObj != null : "annotatedObj must exist";
		assert markerType != null : "markerType must exist";

		int numberOfAnnotations = annotationDao.countAnnotations(annotatedObj, markerType);
		assertEquals("countAnnotations should return 2",2,numberOfAnnotations);
	}

	@Test
	public void testGetAnnotationsWithStatus() {
		Annotation annotatedObj = annotationDao.findByUuid(uuid);
		MarkerType markerType = MarkerType.TO_BE_CHECKED();
		assert annotatedObj != null : "annotatedObj must exist";
		assert markerType != null : "markerType must exist";

		List<Annotation> annotations = annotationDao.getAnnotations(annotatedObj, markerType,null,null,null,null);
		assertNotNull("getAnnotations should return a List",annotations);
		assertFalse("the list should contain Annotation instances",annotations.isEmpty());
		assertEquals("getAnnotations should return 2",2,annotations.size());
	}

	@Test
	public void testCountAllAnnotationsWithStatus() {
		MarkerType markerType = MarkerType.TO_BE_CHECKED();

		assert markerType != null : "markerType must exist";

		int numberOfAnnotations = annotationDao.count((User)null, markerType);
		assertEquals("countAnnotations should return 2",2,numberOfAnnotations);
	}

	@Test
	public void testListAllAnnotationsWithStatus() {
		MarkerType markerType = MarkerType.TO_BE_CHECKED();
		assert markerType != null : "markerType must exist";

		List<Annotation> annotations = annotationDao.list((User)null, markerType, null, null, null, null);
		assertNotNull("getAnnotations should return a List",annotations);
		assertFalse("the list should contain Annotation instances",annotations.isEmpty());
		assertEquals("getAnnotations should return 2",2,annotations.size());
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
