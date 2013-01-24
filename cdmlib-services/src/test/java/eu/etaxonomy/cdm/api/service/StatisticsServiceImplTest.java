/**
 * 
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.sun.org.apache.bcel.internal.generic.NEW;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;

/**
 * @author s.buers
 * 
 */
public class StatisticsServiceImplTest extends CdmTransactionalIntegrationTest {

	
	// ************constants to set up the expected results for all: ********************

	// here is the list of the types that will be test counted in the particular
	// parts (ALL, CLASSIFICATION)
	private static final List<StatisticsTypeEnum> types2Count = Arrays
			.asList(new StatisticsTypeEnum[] { 
					StatisticsTypeEnum.CLASSIFICATION, 
					StatisticsTypeEnum.ACCEPTED_TAXA, 
					StatisticsTypeEnum.ALL_TAXA,
					StatisticsTypeEnum.ALL_REFERENCES});

	// here is the number of items that will be created for the test count:
	// please only change the numbers
	// but do not replace or add a number to any constants on the right.
	
	// ................ALL............................
	
	private static final int NO_OF_CLASSIFICATIONS = 3;

	private static final int NO_OF_ACCEPTED_TAXA = 10;

	private static final int NO_OF_SYNONYMS = 0;
	
	private static final int NO_OF_SHARED_TAXA= 4;

	private static final int NO_OF_ALLTAXA = NO_OF_ACCEPTED_TAXA
			+ NO_OF_SYNONYMS;

	private static final int NO_OF_TAXON_NAMES = NO_OF_ACCEPTED_TAXA;

	private static final int NO_OF_DESCRIPTIVE_SOURCE_REFERENCES = 0;

	private static final int NO_OF_ALL_REFERENCES = NO_OF_ACCEPTED_TAXA + 0; // +....

	private static final int NO_OF_NOMECLATURAL_REFERENCES = 0;
	
	// this is for the "all DB" count test
	private static final List<IdentifiableEntity> partsAll = Arrays
			.asList(new IdentifiableEntity[] { null });

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

	//------------------ variables for CLASSIFICATIONS -----------------------
	
	//int[] anArray = new int[NO_OF_CLASSIFICATIONS];
	private static List<Integer> no_of_all_taxa = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_accepted_taxa = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_synonyms = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_taxon_names = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_descriptive_source_references = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_all_references = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
	private static List<Integer> no_of_nomenclatural_references = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));
//	private List<Integer> no_of_classifications = new ArrayList<Integer>(Collections.nCopies(NO_OF_CLASSIFICATIONS, 0));

	//........................... constant map ..........................
	
	private static final Map<String, List<Integer>> typeCountMap_CLASSIFICATION = new HashMap<String, List<Integer>>() {
		{
			put(StatisticsTypeEnum.CLASSIFICATION.getLabel(),
						new ArrayList<Integer>(Arrays.asList((Integer)null, null, null)));
			put(StatisticsTypeEnum.ALL_TAXA.getLabel(),
					no_of_all_taxa);
			put(StatisticsTypeEnum.ACCEPTED_TAXA.getLabel(),
					no_of_accepted_taxa);
			put(StatisticsTypeEnum.SYNONYMS.getLabel(),
					no_of_synonyms);
			put(StatisticsTypeEnum.TAXON_NAMES.getLabel(),
					no_of_taxon_names);
			put(StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES.getLabel(),
					no_of_descriptive_source_references);
			put(StatisticsTypeEnum.ALL_REFERENCES.getLabel(),
					no_of_all_references);
			put(StatisticsTypeEnum.NOMECLATURAL_REFERENCES.getLabel(),
					no_of_nomenclatural_references);
		}
	};
	
	private List<Classification> classifications;
	
	
	//****************** services: ************************
	@SpringBeanByType
	private IStatisticsService service;
	@SpringBeanByType
	private IClassificationService classificationService;
	@SpringBeanByType
	private ITaxonService taxonService;
	@SpringBeanByType
	private IReferenceService referenceService;


	
	//*************** more members: *****************+
	


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
//		// taxa
		int remainder = NO_OF_ACCEPTED_TAXA;// -(NO_OF_ACCEPTED_TAXA / 2);
		int taxa = 0;
		for (int amount, k = 0; remainder > 0 && k < NO_OF_CLASSIFICATIONS; k++, remainder-=amount) {
			
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
				String radomCommonName = RandomStringUtils.randomAlphabetic(10);
				Reference sec = ReferenceFactory.newBook();
				sec.setTitle("book" + i);
				referenceService.save(sec);

				BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
				name.setNameCache(radomName, true);
				Taxon taxon = Taxon.NewInstance(name, sec);
				taxonService.save(taxon);
				classifications.get(k).addChildTaxon(taxon, null, null, null);
		        
		        classificationService.saveOrUpdate(classifications.get(k));
		        increment(no_of_accepted_taxa, k);
		        increment(no_of_all_taxa, k);
		    
				taxa++;
			}
		    
			// remainder-=amount;
			// k++;
			System.out.println("number of taxa: " + taxa);
		}

		// Reference sec = ReferenceFactory.newBook();
		// referenceService.save(sec);

		// BotanicalName n_abies = BotanicalName.NewInstance(Rank.GENUS());
		// n_abies.setNameCache("Abies", true);
		// Taxon t_abies = Taxon.NewInstance(n_abies, sec);
		// taxonService.save(t_abies);
		//
		// BotanicalName n_abies_alba =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_alba.setNameCache("Abies alba", true);
		// Taxon t_abies_alba = Taxon.NewInstance(n_abies_alba, sec);
		// taxonService.save(t_abies_alba);
		//
		// BotanicalName n_abies_balsamea =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_balsamea.setNameCache("Abies balsamea", true);
		// Taxon t_abies_balsamea = Taxon.NewInstance(n_abies_balsamea, sec);
		// t_abies_balsamea.setUuid(UUID.fromString(ABIES_BALSAMEA_UUID));
		// taxonService.save(t_abies_balsamea);
		//
		// BotanicalName n_abies_grandis =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_grandis.setNameCache("Abies grandis", true);
		// Taxon t_abies_grandis = Taxon.NewInstance(n_abies_grandis, sec);
		// taxonService.save(t_abies_grandis);
		//
		// BotanicalName n_abies_kawakamii =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_kawakamii.setNameCache("Abies kawakamii", true);
		// Taxon t_abies_kawakamii = Taxon.NewInstance(n_abies_kawakamii, sec);
		// taxonService.save(t_abies_kawakamii);
		//
		// BotanicalName n_abies_subalpina =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_subalpina.setNameCache("Abies subalpina", true);
		// Synonym s_abies_subalpina = Synonym.NewInstance(n_abies_subalpina,
		// sec);
		// taxonService.save(s_abies_subalpina);
		//
		// BotanicalName n_abies_lasiocarpa =
		// BotanicalName.NewInstance(Rank.SPECIES());
		// n_abies_lasiocarpa.setNameCache("Abies lasiocarpa", true);
		// Taxon t_abies_lasiocarpa = Taxon.NewInstance(n_abies_lasiocarpa,
		// sec);
		// t_abies_lasiocarpa.addSynonym(s_abies_subalpina,
		// SynonymRelationshipType.SYNONYM_OF());
		// taxonService.save(t_abies_lasiocarpa);
		//
		// // add taxa to classifications
		// classification.addChildTaxon(t_abies_balsamea, null, null, null);
		// alternativeClassification.addChildTaxon(t_abies_lasiocarpa, null,
		// null, null);
		// classificationService.saveOrUpdate(classification);
		// classificationService.saveOrUpdate(alternativeClassification);

		// // t_abies_balsamea.se
		//
		// //
		// // Description
		// //
		// TaxonDescription d_abies_alba =
		// TaxonDescription.NewInstance(t_abies_alba);
		//
		// d_abies_alba.setUuid(UUID.fromString(D_ABIES_BALSAMEA_UUID));
		// // CommonTaxonName
		// d_abies_alba.addElement(CommonTaxonName.NewInstance("Weißtanne",
		// Language.GERMAN()));
		// d_abies_alba.addElement(CommonTaxonName.NewInstance("silver fir",
		// Language.ENGLISH()));
		// // TextData
		// TaxonDescription d_abies_balsamea =
		// TaxonDescription.NewInstance(t_abies_balsamea);
		// d_abies_balsamea
		// .addElement(TextData
		// .NewInstance(
		// "Die Balsam-Tanne (Abies balsamea) ist eine Pflanzenart aus der Gattung der Tannen (Abies). Sie wächst im nordöstlichen Nordamerika, wo sie sowohl Tief- als auch Bergland besiedelt. Sie gilt als relativ anspruchslos gegenüber dem Standort und ist frosthart. In vielen Teilen des natürlichen Verbreitungsgebietes stellt sie die Klimaxbaumart dar.",
		// Language.GERMAN(), null));
		// d_abies_balsamea
		// .addElement(TextData
		// .NewInstance(
		// "Бальзам ньыв (лат. Abies balsamea) – быдмассэзлӧн пожум котырись ньыв увтырын торья вид. Ньывпуыс быдмӧ 14–20 метра вылына да овлӧ 10–60 см кыза диаметрын. Ньывпу пантасьӧ Ойвыв Америкаын.",
		// Language.RUSSIAN(), null));
		// setComplete();
		// endTransaction();
		//
		// printDataSet(new FileOutputStream("TaxonServiceSearchTest.xml"), new
		// String[] {
		// "TAXONBASE", "TAXONNAMEBASE", "SYNONYMRELATIONSHIP",
		// "REFERENCE", "DESCRIPTIONELEMENTBASE", "DESCRIPTIONBASE",
		// "AGENTBASE", "HOMOTYPICALGROUP", "CLASSIFICATION",
		// "CLASSIFICATION_TAXONNODE", "TAXONNODE",
		// "LANGUAGESTRING", "DESCRIPTIONELEMENTBASE_LANGUAGESTRING" });
		//
		//
System.out.println(no_of_accepted_taxa);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	//****************** tests *****************
	/**
	 * Test method for
	 * {@link eu.etaxonomy.cdm.api.service.StatisticsServiceImpl#getCountStatistics(java.util.List)}
	 * .
	 */
	@Test
	public void testGetCountStatistics_ALL() {

		Map<String, Number> expectedCountMap = createExpectedCountMap_ALL();

		List<StatisticsConfigurator> configuratorList = new ArrayList<StatisticsConfigurator>();
		configuratorList.add(new StatisticsConfigurator(partsAll, types2Count));

		List<Statistics> statisticsList = service
				.getCountStatistics(configuratorList);

		assertEquals(expectedCountMap, statisticsList.get(0).getCountMap());

	}

	@Test
	public void testGetCountStatistics_CLASSIFICATION() {
		List<Map<String, Number>> ExpectedCountMaps = createExpectedCountMaps_CLASSIFICATION();
		
		fail("Not yet implemented");
	}

	//************************** private methods ****************************+
	
	/**
	 * @param no_of_sth 
	 * @param inClassification
	 */
	private void increment(List<Integer> no_of_sth, int inClassification) {
		no_of_sth.set(inClassification, (no_of_sth.get(inClassification))+1);
	}

	private Map<String, Number> createExpectedCountMap_ALL() {
		Map<String, Number> countMap = new HashMap<String, Number>();
	
		for (StatisticsTypeEnum type : types2Count) {
			countMap.put(type.getLabel(), typeCountMap_ALL.get(type.getLabel()));
		}
		return countMap;
	}

	private List<Map<String, Number>> createExpectedCountMaps_CLASSIFICATION() {
		
		List<Map<String, Number>> mapList = new ArrayList<Map<String,Number>>();
		
		for (int i=0; i<NO_OF_CLASSIFICATIONS; i++) {
			
			Map<String, Number> countMap = new HashMap<String, Number>();
			
			for (StatisticsTypeEnum type : types2Count) {
				countMap.put(type.getLabel(), (typeCountMap_CLASSIFICATION.get(type.getLabel())).get(i));
				
			}
			mapList.add(countMap);
		}

		return mapList;
	}
}
