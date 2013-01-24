/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author s.buers
 * 
 */
@SuppressWarnings({"rawtypes","serial"})
public class StatisticsServiceImplTest extends CdmTransactionalIntegrationTest {

	// ************constants to set up the expected results for all:
	// ********************

	// ................ALL............................

	// here is the list of the types that will be test counted in the
	// parts (ALL, CLASSIFICATION)
	private static final List<StatisticsTypeEnum> TYPES = Arrays
			.asList(new StatisticsTypeEnum[] {
					StatisticsTypeEnum.CLASSIFICATION,
					StatisticsTypeEnum.ACCEPTED_TAXA,
					StatisticsTypeEnum.ALL_TAXA,
					StatisticsTypeEnum.ALL_REFERENCES });

	// private static final String[] TYPES = { "CLASSIFICATION",
	// "ACCEPTED_TAXA",
	// "ALL_TAXA", "ALL_REFERENCES" };

	// ................parts ..............................


	private static final List<String> PARTS2 = Arrays.asList(new String[] {
			"ALL", "CLASSIFICATION" });
	// .........................................................

	// part= null means search all DB

	private static final IdentifiableEntity PARTS_ALL = null;

	// here is the number of items that will be created for the test count:
	// please only change the numbers
	// but do not replace or add a number to any constants on the right.

	private static final int NO_OF_CLASSIFICATIONS = 3;

	private static final int NO_OF_ACCEPTED_TAXA = 10;

	private static final int NO_OF_SYNONYMS = 0;

	private static final int NO_OF_SHARED_TAXA = 4;

	private static final int NO_OF_ALLTAXA = NO_OF_ACCEPTED_TAXA
			+ NO_OF_SYNONYMS;

	private static final int NO_OF_TAXON_NAMES = NO_OF_ACCEPTED_TAXA;

	private static final int NO_OF_DESCRIPTIVE_SOURCE_REFERENCES = 0;

	private static final int NO_OF_ALL_REFERENCES = NO_OF_ACCEPTED_TAXA + 0; // +....

	private static final int NO_OF_NOMECLATURAL_REFERENCES = 0;

	// ............................................

	private static final Map<String, Long> typeCountMap_ALL = new HashMap<String, Long>() {
		{
			put(StatisticsTypeEnum.CLASSIFICATION.getLabel(),
					Long.valueOf(NO_OF_CLASSIFICATIONS));
			put(StatisticsTypeEnum.ALL_TAXA.getLabel(),
					Long.valueOf(NO_OF_ALLTAXA));
			put(StatisticsTypeEnum.ACCEPTED_TAXA.getLabel(),
					Long.valueOf(NO_OF_ACCEPTED_TAXA));
			put(StatisticsTypeEnum.SYNONYMS.getLabel(),
					Long.valueOf(NO_OF_SYNONYMS));
			put(StatisticsTypeEnum.TAXON_NAMES.getLabel(),
					Long.valueOf(NO_OF_TAXON_NAMES));
			put(StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES.getLabel(),
					Long.valueOf(NO_OF_DESCRIPTIVE_SOURCE_REFERENCES));
			put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
					Long.valueOf(NO_OF_ALL_REFERENCES));
			put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES.getLabel(),
					Long.valueOf(NO_OF_NOMECLATURAL_REFERENCES));
		}
	};

	// ------------------ variables for CLASSIFICATIONS -----------------------

	// int[] anArray = new int[NO_OF_CLASSIFICATIONS];
	private static List<Long> no_of_all_taxa = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_accepted_taxa = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_synonyms = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_taxon_names = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_descriptive_source_references = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_all_references = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_nomenclatural_references = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	// we do not count classifications in classifications

	// ........................... constant map ..........................


	private static final Map<String, List<Long>> typeCountMap_CLASSIFICATION = new HashMap<String, List<Long>>() {
		{
			put(StatisticsTypeEnum.CLASSIFICATION.getLabel(),
					new ArrayList<Long>(Arrays.asList((Long) null, null, null)));
			put(StatisticsTypeEnum.ALL_TAXA.getLabel(), no_of_all_taxa);
			put(StatisticsTypeEnum.ACCEPTED_TAXA.getLabel(),
					no_of_accepted_taxa);
			put(StatisticsTypeEnum.SYNONYMS.getLabel(), no_of_synonyms);
			put(StatisticsTypeEnum.TAXON_NAMES.getLabel(), no_of_taxon_names);
			put(StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES.getLabel(),
					no_of_descriptive_source_references);
			put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
					no_of_all_references);
			put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES.getLabel(),
					no_of_nomenclatural_references);
		}
	};

	private List<Classification> classifications;

	// ****************** services: ************************
	@SpringBeanByType
	private IStatisticsService service;
	@SpringBeanByType
	private IClassificationService classificationService;
	@SpringBeanByType
	private ITaxonService taxonService;
	@SpringBeanByType
	private IReferenceService referenceService;

	// *************** more members: *****************+

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
	 * create some testdata
	 * 
	 * @throws java.lang.Exception
	 */

	@Before
	@DataSet
	public void setUp() throws Exception {

		classifications = new ArrayList<Classification>();

		for (int i = 1; i <= NO_OF_CLASSIFICATIONS; i++) {
			Classification classification = Classification
					.NewInstance("European Abies" + i);
			classifications.add(classification);
			classificationService.save(classification);
		}
		// // taxa
		int remainder = NO_OF_ACCEPTED_TAXA;

		for (int amount, k = 0; remainder > 0 && k < NO_OF_CLASSIFICATIONS; k++, remainder -= amount) {

			if (k >= NO_OF_CLASSIFICATIONS - 1) {
				amount = remainder;
			} else {
				amount = remainder / 2;
			}

			System.out.println("amount: " + amount);
			for (int i = 1; i <= amount; i++) {
				RandomStringUtils.randomAlphabetic(10);
				String radomName = RandomStringUtils.randomAlphabetic(5) + " "
						+ RandomStringUtils.randomAlphabetic(10);
				//TODO
				String radomCommonName = RandomStringUtils.randomAlphabetic(10);
				Reference sec = ReferenceFactory.newBook();
				sec.setTitle("book" + i);
				referenceService.save(sec);
				increment(no_of_all_references, k);
				BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
				name.setNameCache(radomName, true);
				Taxon taxon = Taxon.NewInstance(name, sec);
				// TODO count name
				taxonService.save(taxon);
				classifications.get(k).addChildTaxon(taxon, null, null, null);

				classificationService.saveOrUpdate(classifications.get(k));
				increment(no_of_accepted_taxa, k);
				increment(no_of_all_taxa, k);
			}

		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	// ****************** tests *****************
	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.StatisticsServiceImpl#getCountStatistics(java.util.List)}
	 * .
	 */
	@Test
	public void testGetCountStatistics() {

		// create maps to compare the testresults with:
		List<Map<String, Number>> expectedCountmapList = new ArrayList<Map<String, Number>>();
		if (PARTS2.contains("ALL")) {
			expectedCountmapList.add(createExpectedCountMap_ALL());
		}
		if (PARTS2.contains("CLASSIFICATION")) {
			expectedCountmapList
					.addAll(createExpectedCountMaps_CLASSIFICATION());
		}

		// create configurator needed to call
		// StatisticsService.getCountStatistics:
		List<StatisticsConfigurator> configuratorList = createConfiguratorList(
				(String[]) PARTS2.toArray(), TYPES);

		// run method of StatisticsService
		List<Statistics> statisticsList = service
				.getCountStatistics(configuratorList);

		// check the result with the expected values:
		// as we cannot be sure the order of the statisticsList
		// matches the order of the expectedCountmapList
		// we have to work arround a little:

		for (Statistics statistics : statisticsList) {
			Boolean orCompare = false;
			for (Map<String, Number> map : expectedCountmapList) {
				orCompare = orCompare || statistics.getCountMap().equals(map);
			}
			assertTrue(orCompare);
		}
	}


	// ************************** private methods ****************************+

	/**
	 * @param no_of_sth
	 * @param inClassification
	 */
	private void increment(List<Long> no_of_sth, int inClassification) {
		no_of_sth.set(inClassification, (no_of_sth.get(inClassification)) + 1);
	}

	private Map<String, Number> createExpectedCountMap_ALL() {
		Map<String, Number> countMap = new HashMap<String, Number>();

		for (StatisticsTypeEnum type : TYPES) {
			countMap.put(type.getLabel(), typeCountMap_ALL.get(type.getLabel()));
		}
		return countMap;
	}

	private List<Map<String, Number>> createExpectedCountMaps_CLASSIFICATION() {

		List<Map<String, Number>> mapList = new ArrayList<Map<String, Number>>();

		for (int i = 0; i < NO_OF_CLASSIFICATIONS; i++) {

			Map<String, Number> countMap = new HashMap<String, Number>();

			for (StatisticsTypeEnum type : TYPES) {
				countMap.put(type.getLabel(),
						typeCountMap_CLASSIFICATION.get(type.getLabel()).get(i));

			}
			mapList.add(countMap);
		}

		return mapList;
	}

	private List<StatisticsConfigurator> createConfiguratorList(String[] part,
			List<StatisticsTypeEnum> types) {

		ArrayList<StatisticsConfigurator> configuratorList = new ArrayList<StatisticsConfigurator>();

		// 1. get types for configurators:
		// in our case all the configurators will have the same types
		// so we calculate the types once and save them in a helperConfigurator
		StatisticsConfigurator helperConfigurator = new StatisticsConfigurator();

		if (types != null) {
			for (StatisticsTypeEnum type : types) {
				helperConfigurator.addType(type);
			}
		} else {
			for (StatisticsTypeEnum enumValue : StatisticsTypeEnum.values()) {
				helperConfigurator.addType(enumValue);
			}
		}

		// 2. determine the entities and put each of them in a configurator:

		// if no part was given:
		if (part == null) {
			helperConfigurator.addFilter(PARTS_ALL);
			configuratorList.add(helperConfigurator);
		}
		// else parse list of parts and create configurator for each:
		else {
			for (String string : part) {
				// System.out.println(StatisticsPartEnum.ALL.toString());
				if (string.equals(StatisticsPartEnum.ALL.toString())) {
					helperConfigurator.addFilter(PARTS_ALL);
					configuratorList.add(helperConfigurator);
				} else if (string.equals(StatisticsPartEnum.CLASSIFICATION
						.toString())) {
					List<Classification> classificationsList = classificationService
							.listClassifications(null, 0, null, null);
					for (Classification classification : classificationsList) {

						StatisticsConfigurator newConfigurator = new StatisticsConfigurator();
						newConfigurator.setType(helperConfigurator.getType());
						newConfigurator.getFilter().addAll(
								helperConfigurator.getFilter());
						newConfigurator.addFilter(classification);
						configuratorList.add(newConfigurator);
					}
				}
			}

		}

		return configuratorList;
	}


}
