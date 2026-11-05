/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityConstraintViolationDao;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmSpringIntegrationTest;

/**
 * @author a.holleman
 * @since 13.05.2014
 */
@CdmDataSet("EntityValidationDaoHibernateImplTest.xml")
public class EntityConstraintViolationDaoHibernateImplTest extends CdmSpringIntegrationTest {

	private static final String MEDIA = "eu.etaxonomy.cdm.model.media.Media";
	private static final String SYNONYM = "eu.etaxonomy.cdm.model.taxon.Synonym";
	private static final String GATHERING_EVENT = "eu.etaxonomy.cdm.model.occurrence.GatheringEvent";

	@Autowired
	IEntityConstraintViolationDao dao;

	@Test
	public void init(){
		assertNotNull(dao, "Expecting an instance of IEntityConstraintViolationDao");
	}


	@Test
	public void testGetConstraintViolations_String(){
		List<EntityConstraintViolation> results;

		results = dao.getConstraintViolations(MEDIA);
		assertEquals(1, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations(SYNONYM);
		assertEquals(2, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations(GATHERING_EVENT);
		assertEquals(4, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations("foo.bar");
		assertEquals(0, results.size(), "Unexpected number of validation results");
	}


	@Test
	public void testGetConstraintViolations_String_Severity(){
		List<EntityConstraintViolation> results;

		results = dao.getConstraintViolations(MEDIA, Severity.NOTICE);
		assertEquals(0, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(MEDIA, Severity.WARNING);
		assertEquals(0, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(MEDIA, Severity.ERROR);
		assertEquals(1, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations(SYNONYM, Severity.NOTICE);
		assertEquals(0, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(SYNONYM, Severity.WARNING);
		assertEquals(1, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(SYNONYM, Severity.ERROR);
		assertEquals(1, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations(GATHERING_EVENT, Severity.NOTICE);
		assertEquals(1, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(GATHERING_EVENT, Severity.WARNING);
		assertEquals(1, results.size(), "Unexpected number of validation results");
		results = dao.getConstraintViolations(GATHERING_EVENT, Severity.ERROR);
		assertEquals(2, results.size(), "Unexpected number of validation results");

		results = dao.getConstraintViolations("foo.bar", Severity.WARNING);
		assertEquals(0, results.size(), "Unexpected number of validation results");
	}

	@Override
	public void createTestDataSet() throws FileNotFoundException {
		// TODO Auto-generated method stub
	}
}
