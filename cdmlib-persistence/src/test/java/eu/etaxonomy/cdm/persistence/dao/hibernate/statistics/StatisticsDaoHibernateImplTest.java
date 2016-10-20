package eu.etaxonomy.cdm.persistence.dao.hibernate.statistics;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.view.context.AuditEventContextHolder;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionDao;
import eu.etaxonomy.cdm.persistence.dao.description.IDescriptionElementDao;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.persistence.dao.statistics.IStatisticsDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonNodeDao;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class StatisticsDaoHibernateImplTest
        extends CdmTransactionalIntegrationTest {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(StatisticsDaoHibernateImplTest.class);

	private static final boolean PRINTOUT = true;

	@SpringBeanByType
	private IStatisticsDao statisticsDao;

	private UUID nodeUuid;

	private List<Classification> classifications;



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
	private static final int NO_OF_TAXON_NAMES = NO_OF_ACCEPTED_TAXA
			+ NO_OF_SYNONYMS;

	// this represents an approx. no of the amount that will actually generated!
	private static final int NO_OF_DESCRIPTIVE_SOURCE_REFERENCES = 16;

	// private static final int NO_OF_ALL_REFERENCES = NO_OF_ACCEPTED_TAXA + 0;

	// must not be more than NO_OF_ACCEPTED_TAXA+NO_OF_SYNONYMS
	private static final int NO_OF_NOMENCLATURAL_REFERENCES = NO_OF_ACCEPTED_TAXA
			+ NO_OF_SYNONYMS - 4;

	// --------------------variables for all ------------------

	private Long no_of_all_references = new Long(0);
	private Long no_of_descriptive_source_references = new Long(0);

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
			put("CLASSIFICATION",
					new ArrayList<Long>(Arrays.asList((Long) null, null, null)));
			put("ALL_TAXA", no_of_all_taxa_c);
			put("ACCEPTED_TAXA", no_of_accepted_taxa_c);
			put("SYNONYMS", no_of_synonyms_c);
			put("TAXON_NAMES", no_of_taxon_names_c);
			put("DESCRIPTIVE_SOURCE_REFERENCES",
					no_of_descriptive_source_references_c);
			put("ALL_REFERENCES", no_of_all_references_c);
			put("NOMENCLATURAL_REFERENCES", no_of_nomenclatural_references_c);
		}
	};

	// ****************** services: ************************
	@SpringBeanByType
	private IStatisticsDao service;
	@SpringBeanByType
	private IClassificationDao classificationDao;
	@SpringBeanByType
	private ITaxonDao taxonDao;
	@SpringBeanByType
	private IReferenceDao referenceDao;
	@SpringBeanByType
	private IDescriptionDao descriptionDao;
	@SpringBeanByType
	private IDescriptionElementDao descriptionElementDao;
	@SpringBeanByType
	private ITaxonNodeDao taxonNodeDao;


	@Before
	public void setUp() {
		// nodeUuid =UUID.fromString("46cd7e78-f7d5-4c31-937b-2bc5074618c4");
		nodeUuid = UUID.fromString("0b5846e5-b8d2-4ca9-ac51-099286ea4adc");

		AuditEventContextHolder.clearContext();

	}

	@After
	public void tearDown() {
		AuditEventContextHolder.clearContext();
	}

	@Test
	// @DataSet
	public void testGetAllChildNodes() {
		List<UUID> result;
		createDataSet();
		for (Classification classification : classifications) {
			TaxonNode root;
			root= createTaxTree(classification);

			result=statisticsDao.getAllChildNodeIds(root.getUuid());
			System.out.println("classification "+ classification.getName()+": ");
			System.out.println("result: "+result.toString());
			System.out.println("");
		}
		if (PRINTOUT) {
			print();
		}
		// result=statisticsDao.getAllTaxonIds(nodeUuid);
		// statisticsDao.getAllTaxonIds();
		assertTrue(true);
		// fail("Not yet implemented");
	}

	private void createDataSet() {
		// create NO_OF_CLASSIFICATIONS classifications
		classifications = new ArrayList<Classification>();

		for (int i = 1; i <= NO_OF_CLASSIFICATIONS; i++) {
			Classification classification = Classification
					.NewInstance("European Abies" + i);
			classifications.add(classification);
			classificationDao.save(classification);
		}
		// create all taxa, references and synonyms and attach them to one or
		// more classifications

		// variables: flags
		int remainder = NO_OF_ACCEPTED_TAXA;
		Reference sec = ReferenceFactory.newBook();
		boolean secondClassificationForTaxonFlag = false;
		boolean synonymFlag = false;
		boolean tNomRefFlag = false;
		boolean sNomRefFlag = false;
		boolean tDescrSourceRefFlag = false;
		boolean sDescrSourceRefFlag = false;

		// variables: counter (pre-loop)
		int descriptiveElementsPerTaxon = (NO_OF_DESCRIPTIVE_SOURCE_REFERENCES / NO_OF_ACCEPTED_TAXA) + 1;

		int taxaInClass;
		int classiCounter = 0, sharedClassification = 0, synonymCounter = 0, nomRefCounter = 0;

		// iterate over classifications and add taxa
		for (/* see above */; remainder > 0
				&& classiCounter < NO_OF_CLASSIFICATIONS; /* see below */) {

			// compute no of taxa to be created in this classification
			if (classiCounter >= NO_OF_CLASSIFICATIONS - 1) { // last
																// classification
																// gets all left
																// taxa
				taxaInClass = remainder;
			} else { // take half of left taxa for this class:
				taxaInClass = remainder / 2;
			}

			// iterate over amount of taxa meant to be in this classification
			for (int taxonCounter = 1; taxonCounter <= taxaInClass; taxonCounter++) {

				// create a String for the Name
				RandomStringUtils.randomAlphabetic(10);
				String randomName = RandomStringUtils.randomAlphabetic(5) + " "
						+ RandomStringUtils.randomAlphabetic(10);

				// create a name for the taxon
				BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
				name.setNameCache(randomName, true);

				// create nomenclatural reference for taxon name (if left)
				if (nomRefCounter < NO_OF_NOMENCLATURAL_REFERENCES) {
					// we remember this taxon has a nomenclatural reference:
					tNomRefFlag = true;
					Reference nomRef = ReferenceFactory.newBook();
					name.setNomenclaturalReference(nomRef);
					referenceDao.save(nomRef);
					nomRefCounter++;
				}

				// create a new sec for every other taxon
				if (taxonCounter % 2 != 0) {
					sec = createSecReference(classiCounter, taxonCounter);
				}

				// create the taxon
				Taxon taxon = Taxon.NewInstance(name, sec);

				// create descriptions, description sources and their references

				if (no_of_descriptive_source_references < NO_OF_DESCRIPTIVE_SOURCE_REFERENCES) {

					tDescrSourceRefFlag = true;

					// create a description and 2 description elements with
					// references for taxon name
					TaxonNameDescription nameDescr = TaxonNameDescription.NewInstance();
					CommonTaxonName nameElement = CommonTaxonName.NewInstance(
							"Veilchen" + taxonCounter, Language.GERMAN());
					TextData textElement = new TextData();
					Reference nameElementRef = ReferenceFactory.newArticle();
					Reference textElementRef = ReferenceFactory
							.newBookSection();
					nameElement.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, nameElementRef, "name: ");
					textElement.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, textElementRef, "text: ");
					nameDescr.addElement(nameElement);
					nameDescr.addElement(textElement);
					name.addDescription(nameDescr);
					// taxon.getName().addDescription(nameDescr);
					referenceDao.save(nameElementRef);
					referenceDao.save(textElementRef);
					descriptionDao.save(nameDescr);

					// create descriptions, description sources and their
					// references
					// for taxon
					TaxonDescription taxonDescription = new TaxonDescription();
					for (int i = 0; i < descriptiveElementsPerTaxon; i++) {
						DescriptionElementBase descriptionElement = new TextData();
						DescriptionElementSource descriptionElementSource = DescriptionElementSource
								.NewInstance(OriginalSourceType.PrimaryTaxonomicSource);
						Reference article = ReferenceFactory.newArticle();

						descriptionElementSource.setCitation(article);
						descriptionElement.addSource(descriptionElementSource);
						taxonDescription.addElement(descriptionElement);
						referenceDao.save(article);
						descriptionElementDao.save(descriptionElement);

					}
					descriptionDao.save(taxonDescription);
					taxon.addDescription(taxonDescription);

					// create a Specimen for taxon with description, descr.
					// element and referece
					//
					// Specimen specimen = Specimen.NewInstance();
					// SpecimenDescription specimenDescription =
					// SpecimenDescription.NewInstance(specimen);
					// DescriptionElementBase descrElement = new TextData();
					// Reference specimenRef = ReferenceFactory.newArticle();
					// descrElement.addSource(null, null, specimenRef, null);
					//
					//
					// descriptionService.save(specimenDescription);
					// taxon.add(specimen);

					no_of_descriptive_source_references += descriptiveElementsPerTaxon + 2 + 1;

				}

				// add taxon to classification
				classifications.get(classiCounter).addChildTaxon(taxon, null, null);

				// now if there are any left, we create a synonym for the taxon
				if (synonymCounter < NO_OF_SYNONYMS) {
					synonymFlag = true;
					randomName = RandomStringUtils.randomAlphabetic(5) + " "
							+ RandomStringUtils.randomAlphabetic(10);
					// name for synonym
					name = BotanicalName.NewInstance(Rank.SPECIES());
					name.setNameCache(randomName, true);

					// create nomenclatural reference for synonym name (if left)
					if (nomRefCounter < NO_OF_NOMENCLATURAL_REFERENCES) {
						sNomRefFlag = true;
						Reference nomRef = ReferenceFactory.newBook();
						name.setNomenclaturalReference(nomRef);
						referenceDao.save(nomRef);
						nomRefCounter++;
					}

					if (no_of_descriptive_source_references < NO_OF_DESCRIPTIVE_SOURCE_REFERENCES) {
						sDescrSourceRefFlag = true;

						// create a description and 2 description elements with
						// references for synonym name
						TaxonNameDescription nameDescr = TaxonNameDescription.NewInstance();
						CommonTaxonName nameElement = CommonTaxonName
								.NewInstance("anderes Veilchen" + taxonCounter,
										Language.GERMAN());
						TextData textElement = new TextData();
						Reference nameElementRef = ReferenceFactory.newArticle();
						Reference textElementRef = ReferenceFactory.newBookSection();
						nameElement.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, nameElementRef,"name: ");
						textElement.addSource(OriginalSourceType.PrimaryTaxonomicSource, null, null, textElementRef,"text: ");
						nameDescr.addElement(nameElement);
						nameDescr.addElement(textElement);
						name.addDescription(nameDescr);
						// taxon.getName().addDescription(nameDescr);
						referenceDao.save(nameElementRef);
						referenceDao.save(textElementRef);
						descriptionDao.save(nameDescr);
						no_of_descriptive_source_references += 2;
					}

					// create a new reference for every other synonym:
					if (taxonCounter % 2 != 0) {
						sec = createSecReference(classiCounter, taxonCounter);
					}
					Synonym synonym = Synonym.NewInstance(name, sec);
					taxonDao.save(synonym);
					taxon.addSynonym(synonym,
							SynonymType.SYNONYM_OF());

					synonymCounter++;
				}

				// if this is not the last classification and there are
				// taxa left that should be in more than one classification
				// we add the taxon to the next class in the list too.
				if (classiCounter < NO_OF_CLASSIFICATIONS
						&& sharedClassification < NO_OF_SHARED_TAXA) {
					classifications.get(classiCounter + 1).addChildTaxon(taxon, null, null);

					// we remember that this taxon is attached to 2
					// classifications:
					secondClassificationForTaxonFlag = true;
					sharedClassification++;
					classificationDao.saveOrUpdate(classifications
							.get(classiCounter + 1));
				}

				taxonDao.save(taxon);
				classificationDao.saveOrUpdate(classifications
						.get(classiCounter));

				// count the data created with this taxon:
				int c = classiCounter;

				if (secondClassificationForTaxonFlag) {
					c++;
				}

				// run the following loop once, if this taxon only belongs to
				// one
				// classification.
				// twice, if it is attached to 2 classifications
				for (int i = classiCounter; i <= c; i++) {

					// count everything just created for this taxon:
					increment(no_of_accepted_taxa_c, i);
					increment(no_of_taxon_names_c, i);
					if (tNomRefFlag) {
						increment(no_of_nomenclatural_references_c, i);
					}
					if (sNomRefFlag) {
						increment(no_of_nomenclatural_references_c, i);
					}
					if (synonymFlag) {
						increment(no_of_synonyms_c, i);
						increment(no_of_taxon_names_c, i);
					}
					if (taxonCounter % 2 != 0) {
						increment(no_of_all_references_c, i);
						if (synonymFlag) {
							increment(no_of_all_references_c, i);
						}
					}
					if (tDescrSourceRefFlag) {
						increment(no_of_descriptive_source_references_c, i,
								descriptiveElementsPerTaxon + 2);
					}

					if (sDescrSourceRefFlag) {
						increment(no_of_descriptive_source_references_c, i, 2);
					}
				}
				// put flags back:
				secondClassificationForTaxonFlag = false;
				tNomRefFlag = false;
				sNomRefFlag = false;
				synonymFlag = false;
				tDescrSourceRefFlag = false;
				sDescrSourceRefFlag = false;
			}

			// modify variables (post-loop)
			classiCounter++;
			remainder -= taxaInClass;

		}
		merge(no_of_accepted_taxa_c, no_of_synonyms_c, no_of_all_taxa_c);
		merge(no_of_all_references_c, no_of_nomenclatural_references_c,
				no_of_all_references_c);

		// TODO Auto-generated method stub

	}

	/**
	 * create and count a new sec Reference
	 *
	 * @param classiCounter
	 * @param taxonCounter
	 * @return
	 */
	private Reference createSecReference(int classiCounter, int taxonCounter) {
		Reference sec;
		sec = ReferenceFactory.newBook();
		sec.setTitle("book " + classiCounter + "." + taxonCounter);
		referenceDao.save(sec);
		no_of_all_references++;
		return sec;
	}

	/**
	 * @param no_of_sth
	 * @param inClassification
	 * @param increase
	 */
	private void increment(List<Long> no_of_sth, int inClassification,
			int increase) {
		no_of_sth.set(inClassification, (no_of_sth.get(inClassification))
				+ increase);
	}

	private void increment(List<Long> no_of_sth, int inClassification) {
		increment(no_of_sth, inClassification, 1);
	}

	private void merge(List<Long> no_of_sth1, List<Long> no_of_sth2,
			List<Long> no_of_sum) {

		for (int i = 0; i < NO_OF_CLASSIFICATIONS; i++) {
			Long sum = no_of_sth1.get(i) + no_of_sth2.get(i);
			no_of_sum.set(i, sum);

		}
	}

	private TaxonNode createTaxTree(Classification classification) {
		Random rand = new Random();

			Set<TaxonNode> nodes = classification.getAllNodes();
			ArrayList<TaxonNode> children = new ArrayList<>();
			TaxonNode parent = nodes.iterator().next();

			TaxonNode root = parent;
			nodes.remove(parent);
			while (!nodes.isEmpty()) {
				int n = rand.nextInt(2) + 1;
				for (int i = 1; i <= n && !(nodes.isEmpty()); i++) {
					TaxonNode nextNode = nodes.iterator().next();
					nextNode = parent.addChildNode(nextNode, null, null);
					children.add(nextNode);
					nodes.remove(nextNode);
				}

				parent = children.get(0);
				children.remove(0);
			}

		return root;
	}

	/**
	 *
	 */
	private void print() {
		for (Classification classification : classifications) {
			System.out.println("Classification:" + classification.toString());
			for (TaxonNode node : classification.getAllNodes()) {
				System.out.println("\tTaxon: " + node.getTaxon().toString()+" node UUID: "+ node.getUuid());
				System.out.println(" \t(Name: "
						+ node.getTaxon().getName().toString() + ")");
				System.out.print("\tChildren: ");
				for (TaxonNode childNode : node.getChildNodes()) {
					System.out.print(/*childNode.getTaxon().getName() + */" node UUID: "+ node.getUuid()+"   ");
				}
				System.out.println();

				if (node.getTaxon().getName().getNomenclaturalReference() != null) {
					System.out.println(" \t(Nomencl. Ref.: "
							+ node.getTaxon().getName()
									.getNomenclaturalReference().getId() + ")");
				}
				for (Synonym synonym : node.getTaxon().getSynonyms()) {
					System.out.println("\t\tSynonym: " + synonym.toString());
					System.out.println(" \t\t(Name: "
							+ synonym.getName().toString() + ")");
					if (synonym.getName().getNomenclaturalReference() != null) {
						System.out.println(" \t\t(Nomencl. Ref.: "
								+ synonym.getName().getNomenclaturalReference()
										.getId() + ")");
					}
					System.out.println();
				}
			}

		}
		System.out.println();
		System.out.println("end!");

	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
