package eu.etaxonomy.cdm.persistence.dao.hibernate.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;
import eu.etaxonomy.cdm.validation.Severity;

@DataSet
public class EntityValidationResultDaoHibernateImplTest extends CdmIntegrationTest {

	//	public static void main(String[] args)
	//	{
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//		System.out.println(UUID.randomUUID());
	//	}

	@SpringBeanByType
	IEntityValidationResultDao dao;


	//@Test
	public void init()
	{
		assertNotNull("Expecting an instance of IEntityValidationResultDao", dao);
	}


	//@Test
	public void testGetEntityValidationResult()
	{
		EntityValidationResult result;

		result = dao.getValidationResult("eu.etaxonomy.cdm.model.media.Media", 100);
		assertNotNull(result);
		assertEquals("Unexpected entity id", 1, result.getId());
		assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

		result = dao.getValidationResult("eu.etaxonomy.cdm.model.taxon.SynonymRelationship", 200);
		assertNotNull(result);
		assertEquals("Unexpected entity id", 2, result.getId());
		assertEquals("Unexpected number of constraint violations", 2, result.getEntityConstraintViolations().size());

		result = dao.getValidationResult("eu.etaxonomy.cdm.model.occurrence.GatheringEvent", 300);
		assertNotNull(result);
		assertEquals("Unexpected entity id", 3, result.getId());
		assertEquals("Unexpected number of constraint violations", 3, result.getEntityConstraintViolations().size());

		result = dao.getValidationResult("eu.etaxonomy.cdm.model.occurrence.GatheringEvent", 301);
		assertNotNull(result);
		assertEquals("Unexpected entity id", 4, result.getId());
		assertEquals("Unexpected number of constraint violations", 1, result.getEntityConstraintViolations().size());

		// Test we get a null back
		result = dao.getValidationResult("Foo Bar", 100);
		assertNull(result);
	}


	@Test
	public void testGetEntityValidationResults_String()
	{
		List<EntityValidationResult> results;

		results = dao.getEntityValidationResults("eu.etaxonomy.cdm.model.media.Media");
		assertEquals("Unexpected number of validation results", 1, results.size());

		results = dao.getEntityValidationResults("eu.etaxonomy.cdm.model.taxon.SynonymRelationship");
		assertEquals("Unexpected number of validation results", 1, results.size());

		results = dao.getEntityValidationResults("eu.etaxonomy.cdm.model.occurrence.GatheringEvent");
		assertEquals("Unexpected number of validation results", 2, results.size());

		results = dao.getEntityValidationResults("foo.bar");
		assertEquals("Unexpected number of validation results", 0, results.size());
	}

	@Test
	public void testGetConstraintViolations_String()
	{
		List<EntityConstraintViolation> results;

		results = dao.getConstraintViolations("eu.etaxonomy.cdm.model.media.Media");
		assertEquals("Unexpected number of validation results", 1, results.size());

		results = dao.getConstraintViolations("eu.etaxonomy.cdm.model.taxon.SynonymRelationship");
		assertEquals("Unexpected number of validation results", 2, results.size());

		results = dao.getConstraintViolations("eu.etaxonomy.cdm.model.occurrence.GatheringEvent");
		assertEquals("Unexpected number of validation results", 4, results.size());

		results = dao.getConstraintViolations("foo.bar");
		assertEquals("Unexpected number of validation results", 0, results.size());
	}


	//@Test
	public void testGetEntityValidationResults_String_Severity()
	{
		List<EntityValidationResult> results;

		results = dao.getValidationResults("eu.etaxonomy.cdm.model.media.Media", Severity.NOTICE);
		assertEquals("Unexpected number of validation results", 0, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.media.Media", Severity.WARNING);
		assertEquals("Unexpected number of validation results", 0, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.media.Media", Severity.ERROR);
		assertEquals("Unexpected number of validation results", 1, results.size());
		assertEquals("Unexpected number of validation results", 1, results.iterator().next().getEntityConstraintViolations().size());
		assertEquals("Unexpected severity", Severity.ERROR, results.iterator().next().getEntityConstraintViolations().iterator().next().getSeverity());

		results = dao.getValidationResults("eu.etaxonomy.cdm.model.taxon.SynonymRelationship", Severity.NOTICE);
		assertEquals("Unexpected number of validation results", 0, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.taxon.SynonymRelationship", Severity.WARNING);
		assertEquals("Unexpected number of validation results", 1, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.taxon.SynonymRelationship", Severity.ERROR);
		assertEquals("Unexpected number of validation results", 1, results.size());

		results = dao.getValidationResults("eu.etaxonomy.cdm.model.occurrence.GatheringEvent", Severity.NOTICE);
		assertEquals("Unexpected number of validation results", 1, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.occurrence.GatheringEvent", Severity.WARNING);
		assertEquals("Unexpected number of validation results", 1, results.size());
		results = dao.getValidationResults("eu.etaxonomy.cdm.model.occurrence.GatheringEvent", Severity.ERROR);
		assertEquals("Unexpected number of validation results", 2, results.size());

		results = dao.getValidationResults("foo.bar", Severity.ERROR);
		assertEquals("Unexpected number of validation results", 0, results.size());
	}


	@Test
	public void testGetEntityValidationResults_Severity()
	{
		List<EntityValidationResult> results;
		results = dao.getValidationResults(Severity.NOTICE);
		assertEquals("Unexpected number of validation results", 1, results.size());
		results = dao.getValidationResults(Severity.WARNING);
		assertEquals("Unexpected number of validation results", 2, results.size());
		results = dao.getValidationResults(Severity.ERROR);
		assertEquals("Unexpected number of validation results", 4, results.size());
	}

}
