/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
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
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author s.buers
 * 
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class StatisticsServiceImplTest extends CdmTransactionalIntegrationTest {

	// constant if you want to printout the database content to console:
	// only recommended for a small probe
	private static final boolean PRINTOUT = false;

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
					StatisticsTypeEnum.ALL_REFERENCES, // this functionality
					// for
					// classifications is still missing for in the Statistics
					// Service
					StatisticsTypeEnum.SYNONYMS, 
					StatisticsTypeEnum.TAXON_NAMES,
					StatisticsTypeEnum.NOMECLATURAL_REFERENCES,
//					StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES
					});

	// private static final String[] TYPES = { "CLASSIFICATION",
	// "ACCEPTED_TAXA",
	// "ALL_TAXA", "ALL_REFERENCES" };

	// ................parts ..............................

	private static final List<String> PARTS = Arrays.asList(new String[] {
			"ALL", "CLASSIFICATION" });
	// .........................................................

	// part= null means search all DB
	private static final IdentifiableEntity PARTS_ALL = null;

	// here is the number of items that will be created for the test count:
	// please only change the numbers
	// but do not replace or add a number to any constants on the right.


	// choose a number
	private static final int NO_OF_ACCEPTED_TAXA = 10;
	
	// choose a number (less than NO_OF_ACCEPTED_TAXA)
	private static final int NO_OF_CLASSIFICATIONS = 3;


	// must be less or equal to NO_OF_ACCEPTED_TAXA
	private static final int NO_OF_SYNONYMS = 7;

	// taxa that occure in several classifications:
	// must NOT be more than NO_OF_ACCEPTED_TAXA
	private static final int NO_OF_SHARED_TAXA = 4; 

	// must be NO_OF_ACCEPTED_TAXA + NO_OF_SYNONYMS
	private static final int NO_OF_ALLTAXA = NO_OF_ACCEPTED_TAXA
			+ NO_OF_SYNONYMS;

	// must be NO_OF_ACCEPTED_TAXA+NO_OF_SYNONYMS 
	private static final int NO_OF_TAXON_NAMES = NO_OF_ACCEPTED_TAXA+NO_OF_SYNONYMS;

	private static final int NO_OF_DESCRIPTIVE_SOURCE_REFERENCES = 0;

	// private static final int NO_OF_ALL_REFERENCES = NO_OF_ACCEPTED_TAXA + 0;

	// must not be more than NO_OF_ACCEPTED_TAXA+NO_OF_SYNONYMS 
	private static final int NO_OF_NOMECLATURAL_REFERENCES = NO_OF_ACCEPTED_TAXA+NO_OF_SYNONYMS - 4;

	// --------------------variables for all ------------------

	private Long no_of_all_references = new Long(0);

	// ............................................

	// log the type enum to an int constant:
	private Map<String, Long> typeMap_ALL;

	// ------------------ variables for CLASSIFICATIONS -----------------------

	// int[] anArray = new int[NO_OF_CLASSIFICATIONS];
	private static List<Long> no_of_all_taxa_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_accepted_taxa_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_synonyms_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_taxon_names_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_descriptive_source_references_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_all_references_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	private static List<Long> no_of_nomenclatural_references_c = new ArrayList<Long>(
			Collections.nCopies(NO_OF_CLASSIFICATIONS, new Long(0)));
	// we do not count classifications in classifications

	// ........................... constant map ..........................

	private static final Map<String, List<Long>> typeCountMap_CLASSIFICATION = new HashMap<String, List<Long>>() {
		{
			put(StatisticsTypeEnum.CLASSIFICATION.getLabel(),
					new ArrayList<Long>(Arrays.asList((Long) null, null, null)));
			put(StatisticsTypeEnum.ALL_TAXA.getLabel(), no_of_all_taxa_c);
			put(StatisticsTypeEnum.ACCEPTED_TAXA.getLabel(),
					no_of_accepted_taxa_c);
			put(StatisticsTypeEnum.SYNONYMS.getLabel(), no_of_synonyms_c);
			put(StatisticsTypeEnum.TAXON_NAMES.getLabel(), no_of_taxon_names_c);
			put(StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES.getLabel(),
					no_of_descriptive_source_references_c);
			put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
					no_of_all_references_c);
			put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES.getLabel(),
					no_of_nomenclatural_references_c);
		}
	};

	private static final Logger logger = Logger.getLogger(StatisticsServiceImplTest.class);
	
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

		// missing in this example data:
		// synonyms, that are attached to several taxa (same or different
		// classification)

		// create NO_OF_CLASSIFICATIONS classifications
		classifications = new ArrayList<Classification>();

		for (int i = 1; i <= NO_OF_CLASSIFICATIONS; i++) {
			Classification classification = Classification
					.NewInstance("European Abies" + i);
			classifications.add(classification);
			classificationService.save(classification);
		}
		// create all taxa, references and synonyms and attach them to one or
		// more classifications

		// 1. variables
		int remainder = NO_OF_ACCEPTED_TAXA;
		Reference sec = ReferenceFactory.newBook();
		boolean secondClassificationForTaxon = false;
		boolean t_nomRef = false;
		boolean s_nomRef= false;

		// iterate over classifications and add taxa
		for (int taxaInClass, classiCounter = 0, sharedClassification = 0, synonymCounter = 0, nomRefCounter =0; remainder > 0
				&& classiCounter < NO_OF_CLASSIFICATIONS; classiCounter++, remainder -= taxaInClass) {

			if (classiCounter >= NO_OF_CLASSIFICATIONS - 1) { // last
																// classification
																// gets all left
																// taxs
				taxaInClass = remainder;
			} else { // take half of left taxa for this class:
				taxaInClass = remainder / 2;
			}

			// iterate over amount of taxa meant to be in this classification
			for (int taxonCounter = 1; taxonCounter <= taxaInClass; taxonCounter++) {

				// create a Name
				RandomStringUtils.randomAlphabetic(10);
				String randomName = RandomStringUtils.randomAlphabetic(5) + " "
						+ RandomStringUtils.randomAlphabetic(10);
				
				// create a name for the taxon
				BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
				name.setNameCache(randomName, true);
				increment(no_of_taxon_names_c, classiCounter);

				// create nomenclatural reference for taxon name (if left)
				if(nomRefCounter<NO_OF_NOMECLATURAL_REFERENCES){
					// we remenber this taxon has a nomenclatural reference:
					t_nomRef=true;
					Reference nomRef = ReferenceFactory.newBook();
					name.setNomenclaturalReference(nomRef);
					referenceService.save(nomRef);
					increment(no_of_nomenclatural_references_c, classiCounter);
//					if(secondClassificationForTaxon){
//						increment(no_of_nomenclatural_references_c, classiCounter+1);
//					}
					nomRefCounter++;
				}
				
				// create a new sec for every other taxon
				if (taxonCounter % 2 != 0) {
					sec = createSecReference(classiCounter, taxonCounter);
				}

				// create the taxon
				Taxon taxon = Taxon.NewInstance(name, sec);

				// add taxon to classification
				classifications.get(classiCounter).addChildTaxon(taxon, null,
						null, null);

				increment(no_of_accepted_taxa_c, classiCounter);
				// increment(no_of_all_taxa_c, k);

				// if this is not the last classification and there are
				// taxa left that should be in more than one classification
				// we add the taxon to the next class in the list too.
				if (classiCounter < NO_OF_CLASSIFICATIONS
						&& sharedClassification < NO_OF_SHARED_TAXA) {
					classifications.get(classiCounter + 1).addChildTaxon(taxon,
							null, null, null);
					increment(no_of_accepted_taxa_c, classiCounter + 1);
					// increment(no_of_all_taxa_c, k + 1);
					increment(no_of_taxon_names_c, classiCounter + 1);
					if(t_nomRef){
						increment(no_of_nomenclatural_references_c, classiCounter+1);
					}
					
					if (taxonCounter % 2 != 0) {
						increment(no_of_all_references_c, classiCounter + 1);
					}
					// we remember that this taxon is attached to 2
					// classifications:
					secondClassificationForTaxon = true;
					sharedClassification++;
					classificationService.saveOrUpdate(classifications
							.get(classiCounter + 1));
				}

				// now if there are any left, we create a synonym for the taxon
				if (synonymCounter < NO_OF_SYNONYMS) {

					randomName = RandomStringUtils.randomAlphabetic(5) + " "
							+ RandomStringUtils.randomAlphabetic(10);
					// name for synonym
					name = BotanicalName.NewInstance(Rank.SPECIES());
					name.setNameCache(randomName, true);
					
					// create nomenclatural reference for synonym name (if left)
					if(nomRefCounter<NO_OF_NOMECLATURAL_REFERENCES){
						s_nomRef = true;
						Reference nomRef = ReferenceFactory.newBook();
						name.setNomenclaturalReference(nomRef);
						referenceService.save(nomRef);
						increment(no_of_nomenclatural_references_c, classiCounter);
						nomRefCounter++;
					}
					
					// create a new reference for every other synonym:
					if (taxonCounter % 2 != 0) {
						sec = createSecReference(classiCounter, taxonCounter);
					}
					Synonym synonym = Synonym.NewInstance(name, sec);
					taxonService.save(synonym);
					taxon.addSynonym(synonym,
							SynonymRelationshipType.SYNONYM_OF());
					increment(no_of_synonyms_c, classiCounter);
					increment(no_of_taxon_names_c, classiCounter);
					
					// if the taxon is added to a second class,
					// the synonym and its countable elements are as well
					if (secondClassificationForTaxon) {
						increment(no_of_synonyms_c, classiCounter + 1);
						increment(no_of_taxon_names_c, classiCounter + 1);
						increment(no_of_nomenclatural_references_c, classiCounter+1);	
						if(s_nomRef){
							increment(no_of_nomenclatural_references_c, classiCounter+1);
						}
						
					}
					synonymCounter++;
				}

				taxonService.save(taxon);
				classificationService.saveOrUpdate(classifications
						.get(classiCounter));
				secondClassificationForTaxon = false;
				t_nomRef=false;
				s_nomRef=false;

			}

		}

		// the amount of all taxa can be computed from the number of taxa and
		// synonyms
		no_of_all_taxa_c = merge(no_of_accepted_taxa_c, no_of_synonyms_c);
	}

	/**
	 * create and count a new sec Reference
	 * @param classiCounter
	 * @param taxonCounter
	 * @return
	 */
	private Reference createSecReference(int classiCounter, int taxonCounter) {
		Reference sec;
		sec = ReferenceFactory.newBook();
		sec.setTitle("book " + classiCounter + "." + taxonCounter);
		referenceService.save(sec);
		increment(no_of_all_references_c, classiCounter);
		no_of_all_references++;
		return sec;
	}

	private List<Long> merge(List<Long> no_of_sth1, List<Long> no_of_sth2) {
		for (int i = 0; i < NO_OF_CLASSIFICATIONS; i++) {
			Long sum = no_of_sth1.get(i) + no_of_sth2.get(i);
			no_of_all_taxa_c.set(i, sum);

		}
		return null;
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
		if (PARTS.contains("ALL")) {
			expectedCountmapList.add(createExpectedCountMap_ALL());
		}
		if (PARTS.contains("CLASSIFICATION")) {
			expectedCountmapList
					.addAll(createExpectedCountMaps_CLASSIFICATION());
		}

		// create configurator needed to call
		// StatisticsService.getCountStatistics:
		List<StatisticsConfigurator> configuratorList = createConfiguratorList(
				(String[]) PARTS.toArray(), TYPES);

		// run method of StatisticsService
		List<Statistics> statisticsList = service
				.getCountStatistics(configuratorList);

		// print out the: expected and the result:
		
		logger.info("expected: ");

		for (Map<String,Number> map : expectedCountmapList) {
			logger.info( map.toString());
		}
		logger.info("statistics: ");
		for (Statistics statistics : statisticsList) {
			logger.info(statistics.getCountMap().toString());
			
		}
			
			// check the result with the expected values:
			// as we cannot be sure the order of the statisticsList
			// matches the order of the expectedCountmapList
			// we have to work arround a little:
		for (Statistics statistics : statisticsList) {
			Boolean orCompare = false;
			for (Map<String, Number> map : expectedCountmapList) {
				orCompare = orCompare || statistics.getCountMap().equals(map);
			}
			// assertTrue(orCompare);
		}
		if (PRINTOUT) {
			print();
		}

	}

	/**
	 * 
	 */
	private void print() {
		for (Classification classification : classifications) {
			System.out.println("Classification:" + classification.toString());
			for (TaxonNode node : classification.getAllNodes()) {
				System.out.println("\tTaxon: " + node.getTaxon().toString());
				for (Synonym synonym : node.getTaxon().getSynonyms()) {
					System.out.println("\t\tSynonym: " + synonym.toString());
					System.out.println();
				}
			}

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
		createMap_ALL();
		for (StatisticsTypeEnum type : TYPES) {
//			System.out.println(""+typeMap_ALL.get(type.getLabel()));
			countMap.put(type.getLabel(), typeMap_ALL.get(type.getLabel()));
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

	/**
	 * 
	 */
	private void createMap_ALL() {
		typeMap_ALL = new HashMap<String, Long>() {
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
				// put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
				// Long.valueOf(NO_OF_ALL_REFERENCES));
				put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
						no_of_all_references);
				put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES.getLabel(),
						Long.valueOf(NO_OF_NOMECLATURAL_REFERENCES));
			}
		};
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
