/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotationDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

@CdmDataSet
public class AnnotationDaoImplTest extends CdmSpringIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

	@Autowired
	private IAnnotationDao annotationDao;

	@Test
	public void testCount() {
	    long numberOfAnnotations = annotationDao.count((User)null);
		assertEquals(5, numberOfAnnotations, "countAnnotations should return 5");
	}

	@Test
	public void testListAnnotations() {
	    Person person = null;

        List<Annotation> annotations = annotationDao.list(person, null, null, null, null);
        assertEquals(5, annotations.size(), "getAnnotations should return 5");

        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new OrderHint("commentator", SortOrder.ASCENDING));
        orderHints.add(OrderHint.ORDER_BY_ID_DESC);
        annotations = annotationDao.list(person, null, null, orderHints, null);
        assertEquals(5, annotations.size(), "getAnnotations should still return 5");
        assertEquals(UUID.fromString("76ff6174-a650-4cd8-88a0-cc140a1cbff2"), annotations.get(0).getUuid(),
                "First result should have highest ID (id=5)");
	}
}
