/* just for testing */
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.DeleteResult;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.exception.DataChangeNoRollbackException;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.fetch.CdmFetch;



/**
 * <h2>NOTE</h2>
 * This is a test for sole development purposes, it is not
 * touched by mvn test since it is not matching the "\/**\/*Test" pattern,
 * but it should be annotate with @Ignore when running the project a s junit suite in eclipse
 *
 *
 */
@Ignore
public class TestService {
	static Logger logger = Logger.getLogger(TestService.class);

	private static final UUID TEST_TAXON_UUID = UUID.fromString("b3084573-343d-4279-ba92-4ab01bb47db5");
	private static CdmApplicationController appCtr;
	
	public void testAppController() {
		logger.info("Create name objects...");
		NonViralName<?> nvn = NonViralName.NewInstance(Rank.SPECIES());

		BotanicalName bn = BotanicalName.NewInstance(Rank.SUBSPECIES());
		ZoologicalName zn = ZoologicalName.NewInstance(Rank.FAMILY());

		logger.info("Create reference objects...");

		Reference<?> sec = ReferenceFactory.newJournal();
		sec.setTitleCache("TestJournal", true);

		logger.info("Create taxon objects...");
		Taxon childTaxon = Taxon.NewInstance(nvn, sec);
		Synonym syn = Synonym.NewInstance(bn, sec);
		childTaxon.addSynonym(syn, SynonymRelationshipType.SYNONYM_OF());
 		TransactionStatus txStatus = appCtr.startTransaction();
		appCtr.getTaxonService().save(childTaxon);
		appCtr.commitTransaction(txStatus);


 		Taxon parentTaxon = Taxon.NewInstance(zn, sec);
		parentTaxon.setUuid(TEST_TAXON_UUID);
		parentTaxon.addTaxonomicChild(childTaxon, sec, null);


		// test
		nvn.setGenusOrUninomial("Nonvirala");
		bn.setGenusOrUninomial("Abies");

		logger.info("Create new Author agent...");
		Person team= Person.NewInstance();
		team.setTitleCache("AuthorAgent1", true);
		nvn.setCombinationAuthorship(team);

		logger.info("Save objects ...");
 		appCtr.getTaxonService().save(parentTaxon);

		// load Name list
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().list(null,1000, 0,null,null);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}
	}

	public void testRootTaxa(){
		// load Name list
		logger.info("Load existing names from db...");
		List<TaxonNameBase> tnList = appCtr.getNameService().list(null,1000, 0,null,null);
		for (TaxonNameBase tn2: tnList){
			logger.info("Title: "+ tn2.getTitleCache() + " UUID: " + tn2.getUuid()+";");
		}

		// load Root taxa
		logger.info("Load taxon from db...");
		List<Taxon> taxa = appCtr.getTaxonService().getRootTaxa(null, CdmFetch.NO_FETCH(), false);
		for (Taxon rt: taxa){
			logger.info("Root taxon: "+ rt.toString());
			for (Taxon child: rt.getTaxonomicChildren()){
				logger.info("Child: "+ child.toString());
				logger.info("  Child.higherTaxon: "+ child.getTaxonomicParent().toString());
				for (Synonym synonym: child.getSynonyms()){
					logger.info("  Child synonyms: "+ synonym.toString());
				}
			}
		}
	}

	public void testTermApi(){
		ITermService ts = (ITermService)appCtr.getTermService();
		//DefinedTermBase dt = ts.getTermByUri("e9f8cdb7-6819-44e8-95d3-e2d0690c3523");
		//logger.warn(dt.toString());
		//TODO: fix ts.listTerms(0,100)
//		List<DefinedTermBase> dts = ts.listTerms(0,100);
//		int i = 0;
//		for (DefinedTermBase d: dts){
//			i++;
//			if (i > 10) break;
//			logger.info(d.toString());
//		}
	}

	public void testDeleteTaxa(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(Rank.SPECIES());
		Reference<?> ref = ReferenceFactory.newJournal();
		Taxon taxon1 = Taxon.NewInstance(taxonName, ref);
		Taxon taxon2 = Taxon.NewInstance(taxonName, null);
		logger.info("Save taxon ...");
		UUID uuidTaxon1 = taxonService.save(taxon1);
		logger.info("  UUID: " + uuidTaxon1);
		UUID uuidTaxon2 = taxonService.save(taxon2);
		logger.info("  UUID: " + uuidTaxon2);
		logger.info("Remove taxon ...");
		UUID uuid = null;
		
		DeleteResult result = taxonService.deleteTaxon(taxon1.getUuid(), null, null);
		
		if(!result.isOk()){ 
         	Assert.fail();
       	}
		logger.info("  UUID: " + uuid);
	}


//	public void testVocabularyLists(){
//		TermVocabulary<NomenclaturalStatusType> voc = appCtr.getNameService().getStatusTypeVocabulary();
//		Set<NomenclaturalStatusType> set = voc.getTermsOrderedByLabels(Language.DEFAULT());
//		for (Object obj : set.toArray()){
//			NomenclaturalStatusType nomStatusType = (NomenclaturalStatusType)obj;
//			System.out.println(nomStatusType.getLabel());
//		}
//		TermVocabulary<NameRelationshipType> nameRelVoc = appCtr.getNameService().getNameRelationshipTypeVocabulary();
//		Set<NameRelationshipType> nameRelSet = nameRelVoc.getTermsOrderedByLabels(Language.DEFAULT());
//		for (Object obj : nameRelSet.toArray()){
//			NameRelationshipType naemRelType = (NameRelationshipType)obj;
//			System.out.println(naemRelType.getLabel());
//		}
//		System.out.println("=========== NAME LIST =================");
//		List<TaxonNameBase> nameList = appCtr.getNameService().getNamesByName("Abies%");
//		System.out.println("Size" + nameList.size());
//		for (TaxonNameBase name : nameList){
//			System.out.println("ABEIS: " + name.getTitleCache());
//		}
//	}

	public void testDeleteRelationship(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(Rank.SPECIES());
		Reference<?> ref = ReferenceFactory.newJournal();
		Taxon parent = Taxon.NewInstance(taxonName, ref);
		Taxon child = Taxon.NewInstance(taxonName, null);
		parent.addTaxonomicChild(child, null, null);

		logger.info("Save taxon ...");
		UUID uuidTaxon1 = taxonService.save(parent);
		logger.info("  UUID: " + uuidTaxon1);
		UUID uuidTaxon2 = taxonService.save(child);
		logger.info("  UUID: " + uuidTaxon2);


//		Set<TaxonRelationship> set = parent.getRelationsToThisTaxon();
//		for (TaxonRelationship rel : set){
//			if (rel.getType().equals(ConceptRelationshipType.TAXONOMICALLY_INCLUDED_IN())){
//				parent.removeTaxonRelation(rel);
//			}
//		}

	}

	public void testTransientRank(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		TaxonNameBase<?,?> taxonName = BotanicalName.NewInstance(transientRank);
		Reference<?> ref =  ReferenceFactory.newJournal();
		Taxon taxon = Taxon.NewInstance(taxonName, ref);

		logger.info("Save taxon ...");
		UUID uuidTaxon1 = taxonService.save(taxon);
		logger.info("  UUID: " + uuidTaxon1);

	}

	public void testFeature(){
		TransactionStatus tx = appCtr.startTransaction();
		Language lang = Language.DEFAULT();
		IDescriptionService descriptionService = appCtr.getDescriptionService();
		TermVocabulary<Feature> voc = descriptionService.getDefaultFeatureVocabulary();
		SortedSet<Feature> terms = voc.getTermsOrderedByLabels(lang);
		for (DefinedTermBase term : terms){
			logger.warn(term.getRepresentation(lang));
		}
		appCtr.commitTransaction(tx);
	}


	public void regenerateTaxonTitleCache(){
		ITaxonService taxonService = (ITaxonService)appCtr.getTaxonService();
		taxonService.updateTitleCache();
	}

	private void test(){
		System.out.println("Start ...");
//    	testAppController();
		//testRootTaxa();
		//testTermApi();
		//testDeleteTaxa();
		//testDeleteRelationship();
		//regenerateTaxonTitleCache();
		//testVocabularyLists();
		//testTransientRank();
		testFeature();
		System.out.println("\nEnd ...");
	}

	private static Rank transientRank = Rank.SPECIES();

	private void init(){
		try {
			DbSchemaValidation dbSchemaValidation = DbSchemaValidation.CREATE;
			//appCtr = CdmApplicationController.NewInstance(CdmPersistentDataSource.NewInstance("defaultMySql") , dbSchemaValidation);
			appCtr = CdmApplicationController.NewInstance(dbSchemaValidation);


			TaxonNameBase<?,?> name = NonViralName.NewInstance(null);
			name.setTitleCache("Abies alba", true);

			TaxonNameBase<?,?> name2 = NonViralName.NewInstance(null);
			name2.setTitleCache("Abies beta", true);

			//appCtr.getNameService().saveTaxonName(name);
			//appCtr.getNameService().saveTaxonName(name2);

			//appCtr = CdmApplicationController.NewInstance(CdmPersistentDataSource.NewInstance("rel1_1"));
			//appCtr = new CdmApplicationController(HBM2DDL.CREATE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestService sc = new TestService();
		sc.init();
    	sc.test();
		appCtr.close();
	}

}
