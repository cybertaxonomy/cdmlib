package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
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
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

public class StatisticsServiceImplTest2 extends CdmTransactionalIntegrationTest {

	// ************constants to set up the expected results for all:
	// ********************

	// ............................................

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
					StatisticsTypeEnum.NOMENCLATURAL_REFERENCES,
					StatisticsTypeEnum.DESCRIPTIVE_SOURCE_REFERENCES });

	// private static final String[] TYPES = { "CLASSIFICATION",
	// "ACCEPTED_TAXA",
	// "ALL_TAXA", "ALL_REFERENCES" };

	// ................parts ..............................

	private static final List<String> PARTS = Arrays.asList(new String[] {
			"ALL", "CLASSIFICATION" });
	// .........................................................

	private ArrayList<Classification> classifications;

	// ****************** services: ************************
	@SpringBeanByType
	private IStatisticsService service;
	@SpringBeanByType
	private IClassificationService classificationService;
	@SpringBeanByType
	private ITaxonService taxonService;
	@SpringBeanByType
	private IReferenceService referenceService;
	@SpringBeanByType
	private IDescriptionService descriptionService;

	// ............................................

	@Before
	// @DataSet
	public void setUp() throws Exception {
//		 createTestData(3, 10, 7, 16, 4);
		// OutputStream out= new ByteArrayOutputStream();
		// printDataSet(out);
		// System.out.println(out.toString());
	}

	@Test
	@DataSet
	public void testGetCountStatistics() {

		// create configurator needed to call
		// StatisticsService.getCountStatistics()
		List<StatisticsConfigurator> configuratorList = createConfiguratorList(
				(String[]) PARTS.toArray(), TYPES);

		// run method of StatisticsService
		List<Statistics> statisticsList = service
				.getCountStatistics(configuratorList);

		// print out result
		logger.info("statistics service result: ");
		for (Statistics statistics : statisticsList) {
			logger.info(statistics.getCountMap().toString());
		}

		assertTrue(true);
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
			helperConfigurator.addFilter(null); // part= null means search all
												// DB
			configuratorList.add(helperConfigurator);
		}
		// else parse list of parts and create configurator for each:
		else {
			for (String string : part) {
				if (string.equals(StatisticsPartEnum.ALL.toString())) {
					helperConfigurator.addFilter(null);
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

	public void createTestDataSet(int noOfClassifications, int noOfAcceptedTaxa,
			int noOfSynonyms, int noOfDescrSrcReferences, int sharedTaxa)
			throws Exception {

		// create more parameters:
		int noOfNomRefs = noOfAcceptedTaxa + noOfSynonyms - 4;

		// missing in this example data:
		// synonyms, that are attached to several taxa (same or different
		// classification)

		// --------------------variables for counting produced elements
		// ------------------

		int no_of_all_references = 0;
		int descrSrcReferencesCounter = 0;

		// create noOfClassifications classifications
		classifications = new ArrayList<Classification>();

		for (int i = 1; i <= noOfClassifications; i++) {
			Classification classification = Classification
					.NewInstance("European Abies" + i);
			classifications.add(classification);
			classificationService.save(classification);

		}
		// create all taxa, references and synonyms and attach them to one or
		// more classifications

		// variables: flags
		int remainder = noOfAcceptedTaxa;
		Reference sec = ReferenceFactory.newBook();
		boolean secondClassificationForTaxonFlag = false;
		boolean synonymFlag = false;
		boolean tNomRefFlag = false;
		boolean sNomRefFlag = false;
		boolean tDescrSourceRefFlag = false;
		boolean sDescrSourceRefFlag = false;

		// variables: counter (pre-loop)
		int descriptiveElementsPerTaxon = (noOfDescrSrcReferences / noOfAcceptedTaxa) + 1;

		int taxaInClass;
		int classiCounter = 0, sharedClassification = 0, synonymCounter = 0, nomRefCounter = 0;

		// iterate over classifications and add taxa
		for (/* see above */; remainder > 0
				&& classiCounter < noOfClassifications; /* see below */) {

			// compute no of taxa to be created in this classification
			if (classiCounter >= noOfClassifications - 1) { // last
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
				BotanicalName name = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
				name.setNameCache(randomName, true);

				// create nomenclatural reference for taxon name (if left)
				if (nomRefCounter < noOfNomRefs) {
					// we remember this taxon has a nomenclatural reference:
					tNomRefFlag = true;
					Reference nomRef = ReferenceFactory.newBook();
					name.setNomenclaturalReference(nomRef);
					referenceService.save(nomRef);
					nomRefCounter++;
				}

				// create a new sec for every other taxon
				if (taxonCounter % 2 != 0) {
					sec = createSecReference(classiCounter, taxonCounter);
				}

				// create the taxon
				Taxon taxon = Taxon.NewInstance(name, sec);

				// create descriptions, description sources and their references

				if (descrSrcReferencesCounter < noOfDescrSrcReferences) {

					tDescrSourceRefFlag = true;

					// create a description and 2 description elements with
					// references for taxon name
					TaxonNameDescription nameDescr = TaxonNameDescription
							.NewInstance();
					CommonTaxonName nameElement = CommonTaxonName.NewInstance(
							"Veilchen" + taxonCounter, Language.GERMAN());
					TextData textElement = new TextData();
					Reference nameElementRef = ReferenceFactory.newArticle();
					Reference textElementRef = ReferenceFactory
							.newBookSection();
					nameElement.addSource(
							OriginalSourceType.PrimaryTaxonomicSource, null,
							null, nameElementRef, "name: ");
					textElement.addSource(
							OriginalSourceType.PrimaryTaxonomicSource, null,
							null, textElementRef, "text: ");
					nameDescr.addElement(nameElement);
					nameDescr.addElement(textElement);
					name.addDescription(nameDescr);
					// taxon.getName().addDescription(nameDescr);
					referenceService.save(nameElementRef);
					referenceService.save(textElementRef);
					descriptionService.save(nameDescr);
					System.out.println("Descriptive Src Ref for TaxonName: "+nameElementRef.getId()+" Taxon: "+taxon.getId()+" name: "+taxon.getTitleCache());
					System.out.println("Descriptive Src Ref for TaxonName: "+textElementRef.getId()+" Taxon: "+taxon.getId()+" name: "+taxon.getTitleCache());

					// ###
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
						referenceService.save(article);
						descriptionService
								.saveDescriptionElement(descriptionElement);
						System.out.println("Descriptive Src Ref for Taxon: "+article.getId()+" Taxon: "+taxon.getId()+" name: "+taxon.getTitleCache());

					}
					descriptionService.save(taxonDescription);
					taxon.addDescription(taxonDescription);

					//TODO create Sspecimen connected to taxon via TaxonDescription->DescriptionElement=IndividualAssoziation->setAssociatedSpecimenOrObservation(SpecimenOrObservationBase)
					// TODO and NameBase->SpecimenTypeDesignation->
					// DerrivedUnit???

					// create a Specimen for taxon with description, descr.
					// element and referece
					//
//					 SpecimenOrObservationBase specimen = DerivedUnit.NewInstance(SpecimenOrObservationType.Fossil);
//					 SpecimenDescription specimenDescription =
//					 SpecimenDescription.NewInstance(specimen);
//					 DescriptionElementBase descrElement = new TextData();
//					 Reference specimenRef = ReferenceFactory.newArticle();
////					 descrElement.add;
//
//
//					 descriptionService.save(specimenDescription);
//					 taxon.addSource(
//								OriginalSourceType.PrimaryTaxonomicSource,
//								null, null, nameElementRef, " ");
//					 taxon.add(specimen);

					descrSrcReferencesCounter += descriptiveElementsPerTaxon + 2 + 1;

				}

				// add taxon to classification
				classifications.get(classiCounter).addChildTaxon(taxon, null,
						null);

				// now if there are any left, we create a synonym for the taxon
				if (synonymCounter < noOfSynonyms) {
					synonymFlag = true;
					randomName = RandomStringUtils.randomAlphabetic(5) + " "
							+ RandomStringUtils.randomAlphabetic(10);
					// name for synonym
					name = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
					name.setNameCache(randomName, true);

					// create nomenclatural reference for synonym name (if left)
					if (nomRefCounter < noOfNomRefs) {
						sNomRefFlag = true;
						Reference nomRef = ReferenceFactory.newBook();
						name.setNomenclaturalReference(nomRef);
						referenceService.save(nomRef);
						nomRefCounter++;
					}

					if (descrSrcReferencesCounter < noOfDescrSrcReferences) {
						sDescrSourceRefFlag = true;

						// create a description and 2 description elements with
						// references for synonym name
						TaxonNameDescription nameDescr = TaxonNameDescription
								.NewInstance();
						CommonTaxonName nameElement = CommonTaxonName
								.NewInstance("anderes Veilchen" + taxonCounter,
										Language.GERMAN());
						TextData textElement = new TextData();
						Reference nameElementRef = ReferenceFactory
								.newArticle();
						Reference textElementRef = ReferenceFactory
								.newBookSection();
						nameElement.addSource(
								OriginalSourceType.PrimaryTaxonomicSource,
								null, null, nameElementRef, "name: ");
						textElement.addSource(
								OriginalSourceType.PrimaryTaxonomicSource,
								null, null, textElementRef, "text: ");
						nameDescr.addElement(nameElement);
						nameDescr.addElement(textElement);
						name.addDescription(nameDescr);
						// taxon.getName().addDescription(nameDescr);
						referenceService.save(nameElementRef);
						referenceService.save(textElementRef);
						descriptionService.save(nameDescr);
						descrSrcReferencesCounter += 2;
						System.out.println("Descriptive Src Ref for Synonym: "+nameElementRef.getId()+" Taxon: "+taxon.getId()+" name: "+taxon.getTitleCache());
						System.out.println("Descriptive Src Ref for Synonym: "+textElementRef.getId()+" Taxon: "+taxon.getId()+" name: "+taxon.getTitleCache());
					}

					// create a new reference for every other synonym:
					if (taxonCounter % 2 != 0) {
						sec = createSecReference(classiCounter, taxonCounter);
					}
					Synonym synonym = Synonym.NewInstance(name, sec);
					taxonService.save(synonym);
					taxon.addSynonym(synonym,
							SynonymType.SYNONYM_OF());

					synonymCounter++;
				}

				// if this is not the last classification and there are
				// taxa left that should be in more than one classification
				// we add the taxon to the next class in the list too.

				if (classiCounter < noOfClassifications
						&& sharedClassification < sharedTaxa) {
					classifications.get(classiCounter + 1).addChildTaxon(taxon,
							null, null);

					// we remember that this taxon is attached to 2
					// classifications:
					secondClassificationForTaxonFlag = true;
					sharedClassification++;
					classificationService.saveOrUpdate(classifications
							.get(classiCounter + 1));

				}

				taxonService.save(taxon);
				classificationService.saveOrUpdate(classifications
						.get(classiCounter));

				// count the data created with this taxon:
				int c = classiCounter;

				if (secondClassificationForTaxonFlag) {
					c++;
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

		commit();

		writeDbUnitDataSetFile(new String[] { "TAXONBASE", "TAXONNAMEBASE",
				"TAXONRELATIONSHIP", "REFERENCE",
				"DESCRIPTIONELEMENTBASE",
				"DESCRIPTIONELEMENTBASE_ORIGINALSOURCEBASE",
				"ORIGINALSOURCEBASE", "DESCRIPTIONBASE", "REFERENCE_ORIGINALSOURCEBASE","LANGUAGESTRING",
				"CLASSIFICATION",  "TAXONNODE",
				"HIBERNATE_SEQUENCES" });

		// "AGENTBASE","HOMOTYPICALGROUP","LANGUAGESTRING",
		// "DESCRIPTIONELEMENTBASE_LANGUAGESTRING", "HIBERNATE_SEQUENCES"
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
		referenceService.save(sec);
		return sec;
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
