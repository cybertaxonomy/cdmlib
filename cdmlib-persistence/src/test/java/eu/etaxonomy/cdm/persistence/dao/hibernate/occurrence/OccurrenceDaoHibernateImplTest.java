package eu.etaxonomy.cdm.persistence.dao.hibernate.occurrence;

import java.io.FileNotFoundException;

import org.h2.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.persistence.dao.occurrence.IOccurrenceDao;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class OccurrenceDaoHibernateImplTest  extends CdmIntegrationTest {

	@SpringBeanByType
	private IOccurrenceDao dao;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

//**************** TESTS ************************************************

	@Test
	public void testRebuildIndex() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testOccurrenceDaoHibernateImpl() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountDerivationEvents() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountDeterminations() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountMedia() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetDerivationEvents() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetDeterminations() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetMedia() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testCountClassOfQextendsSpecimenOrObservationBaseTaxonBase() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testListClassOfQextendsSpecimenOrObservationBaseTaxonBaseIntegerIntegerListOfOrderHintListOfString() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetDerivedUnitUuidAndTitleCache() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testGetFieldUnitUuidAndTitleCache() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testListByAnyAssociation() {
		logger.warn("Not yet implemented");
	}

	@Test
	public void testSaveOriginalLabelData(){
		DerivedUnit unit = DerivedUnit.NewInstance(SpecimenOrObservationType.DerivedUnit);
		String originalLabelInfo = StringUtils.pad("my original info", 10000, "x", false);
		Assert.assertEquals(Integer.valueOf(10000),  (Integer)originalLabelInfo.length());
		unit.setOriginalLabelInfo(originalLabelInfo);
		//test that lob is supported
		dao.save(unit);
		//assert no exception
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
