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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@DataSet
public class AnnotationDaoImplTest extends CdmIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@SpringBeanByType
	private IAnnotationDao annotationDao;

	@Test
	public void testCount() {

	    MarkerType markerType = MarkerType.TO_BE_CHECKED();
		assert markerType != null : "markerType must exist";

		long numberOfAnnotations = annotationDao.count((User)null, markerType);
		assertEquals("countAnnotations should return 2", 2, numberOfAnnotations);
	}

	@Test
	public void testListAnnotations() {
	    MarkerType markerType = null;
	    User user = null;
	    Person person = null;

        List<Annotation> annotations = annotationDao.list(person, markerType, null, null, null, null);
        assertEquals("getAnnotations should return 5", 5, annotations.size());

        List<OrderHint> orderHints = new ArrayList<>();
//        orderHints.add(new OrderHint("commentator.titleCache", SortOrder.ASCENDING));  //requires to join the commentator first, see according comments in base method
        orderHints.add(new OrderHint("commentator", SortOrder.ASCENDING));
        orderHints.add(OrderHint.ORDER_BY_ID_DESC);
        annotations = annotationDao.list(person, markerType, null, null, orderHints, null);
        assertEquals("getAnnotations should still return 5", 5, annotations.size());
        assertEquals("First result should have highest ID (id=5)", UUID.fromString("76ff6174-a650-4cd8-88a0-cc140a1cbff2"), annotations.get(0).getUuid());


	    //with status
		markerType = MarkerType.TO_BE_CHECKED();
		assert markerType != null : "markerType must exist";

		annotations = annotationDao.list(user, markerType, null, null, null, null);
		assertEquals("getAnnotations should return 2", 2, annotations.size());
	}

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
