package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class StatisticsServiceImplTest extends CdmTransactionalIntegrationTest {// UnitilsJUnit4

	/**
	 * this is the place to put the values in. only the types listed here are
	 * tested:
	 */
	private static Map<StatisticsTypeEnum, Number> prototype = new HashMap<StatisticsTypeEnum, Number>() {
		{
			put(StatisticsTypeEnum.ALL_TAXA, 0);
			put(StatisticsTypeEnum.ACCEPTED_TAXA, 0);
			put(StatisticsTypeEnum.CLASSIFICATION, 0);
			put(StatisticsTypeEnum.ALL_REFERENCES, 0);
			put(StatisticsTypeEnum.SYNONYMS, 0);
			put(StatisticsTypeEnum.TAXON_NAMES, 0);
			put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES, 0);
//			put(StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES, 0);

		}
	};

	private static final Logger logger = Logger
			.getLogger(StatisticsServiceImplTest.class);

	StatisticsConfigurator configurator;

	@SpringBeanByType
	private IStatisticsService service;

	// @Autowired
	// public void setService(IStatisticsService service) {
	// this.service = service;
	// }

	// ---------- test--------------

	@Test
	public void testGetCountStatistics_partAll() {

		createConfigurator();
		boolean check = true;
		Statistics resultStat = service.getCountStatistics(configurator);
		Map<String, Number> resultCountMap = resultStat.getCountMap();

		for (Map.Entry<StatisticsTypeEnum, Number> protoEntry : prototype
				.entrySet()) {

			logger.info(protoEntry.getKey().getLabel()+":\texpected count: "
					+ "\t\t" + protoEntry.getValue() + "\tactual count: "
					+ resultCountMap.get(protoEntry.getKey().getLabel()));

			if (!((resultCountMap.get(protoEntry.getKey().getLabel()))
					.equals((Number) 0))) {
				check = false;
			}
		}
//		Assert.assertFalse("everything was counted right!!!", check);
		Assert.assertTrue("some count did not match!!!", check);
//		fail("Not yet implemented");
	}

	@Test
	public void testGetCountStatistics_partCLASSIFICATION() {
		fail("Not yet implemented");
	}

	// ------------------------------------------------------------------

	private void createConfigurator() {
		configurator = new StatisticsConfigurator();
		configurator.addPart(StatisticsPartEnum.ALL);
		for (StatisticsTypeEnum type : StatisticsTypeEnum.values()) {
			configurator.addType(type);
		}

	}

}
