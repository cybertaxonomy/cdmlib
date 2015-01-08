/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.VocabularyEnum;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
@Transactional(TransactionMode.DISABLED)
public class IdentifiableServiceBaseTest extends CdmTransactionalIntegrationTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(IdentifiableServiceBaseTest.class);
	
	
	@SpringBeanByType
	private INameService nameService;
	
	@SpringBeanByType
	private ITermService termService;
	
	@SpringBeanByType
	private IVocabularyService vocService;
	
	@SpringBeanByType
	private ITaxonService taxonService;
	
	@SpringBeanByType
	private IClassificationService classificationService;
	
/****************** TESTS *****************************/
	
	@Test
	public final void voidTestSeriveExists(){
		Assert.assertNotNull("Service shoulb be initialized", nameService);
	}

	
	@Test
	@DataSet
	@ExpectedDataSet	
	public final void testUpdateTitleCache() {
		Assert.assertEquals("There should be 5 TaxonNames in the data set", 5, nameService.count(TaxonNameBase.class));
		Class clazz = TaxonNameBase.class;
		int stepSize = 2;
		nameService.updateTitleCache(clazz, stepSize, null, null);
		commit();
//		commitAndStartNewTransaction(new String[]{"TaxonNameBase","TaxonNameBase_AUD"});	
	}
	
	
	@Test
	@DataSet(value="IdentifiableServiceBaseTest.testListByIdentifier.xml")
	public final void testListByIdentifier(){
		UUID uuidIdentifierType1 = UUID.fromString("02bb62db-a229-4eeb-83e6-a9a093943d5e");
		UUID uuidIdentifierType2 = UUID.fromString("ef6e960f-5289-456c-b25c-cff7f4de2f63");
		
		DefinedTerm it1 = (DefinedTerm)termService.find(uuidIdentifierType1);
		Assert.assertNotNull("identifier type must not be null", it1);
		
		List<Taxon> taxa = taxonService.listByIdentifier(Taxon.class, "ext-1234", it1, null, null, null, null, null);
		Assert.assertTrue("Result should not be empty", taxa.size() == 1);
		Taxon taxon = taxa.get(0);
		Assert.assertEquals(UUID.fromString("888cded1-cadc-48de-8629-e32927919879"), taxon.getUuid());
		Assert.assertEquals("Taxon should have 1 identifier", 1, taxon.getIdentifiers().size());
		Identifier identifier = taxon.getIdentifiers().get(0);
		DefinedTerm type = CdmBase.deproxy(identifier.getType(), DefinedTerm.class);
		Assert.assertEquals(uuidIdentifierType1, type.getUuid());
		
		List<TaxonNameBase> names = nameService.listByIdentifier(TaxonNameBase.class, "ext-1234", null, null, null, null, null, null);
		Assert.assertTrue("Identifier does not exist for TaxonName", names.isEmpty());
		
		taxa = taxonService.listByIdentifier(null, "ext-1234", null, null, null, null, null, null);
		Assert.assertTrue("Result size for 'ext-1234' should be 1", taxa.size() == 1);
		
//		taxa = taxonService.listByIdentifier(Taxon.class, null, null, null, null, null, null, null);
//		Assert.assertTrue("Result should not be empty", taxa.size() == 2);

		
	}



    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
//	@Test
    @Override
    public void createTestDataSet() throws FileNotFoundException {
		TermVocabulary<DefinedTerm> voc = vocService.find(VocabularyEnum.IdentifierType.getUuid());
    	
		DefinedTerm identifierType1 = DefinedTerm.NewIdentifierTypeInstance(null, "identifierType1", null);
    	voc.addTerm(identifierType1);
		termService.save(identifierType1);
    	DefinedTerm identifierType2 = DefinedTerm.NewIdentifierTypeInstance(null, "identifierType2", null);
    	voc.addTerm(identifierType2);
		termService.save(identifierType2);
    	
    	
    	BotanicalName name = BotanicalName.NewInstance(Rank.SPECIES());
        Taxon tb = Taxon.NewInstance(name, null);
        Identifier.NewInstance(tb, "ext-1234", identifierType1);
        Identifier.NewInstance(name, "ext-name12", identifierType2);
        taxonService.saveOrUpdate(tb);
        
        Taxon tb2 = Taxon.NewInstance(null, null);
        tb2.setTitleCache("Cached taxon", true);
        Identifier.NewInstance(tb2, "ext-cache1", identifierType2);
        taxonService.saveOrUpdate(tb2);
        
        Classification classification = Classification.NewInstance("My classification");
        classification.addChildTaxon(tb, null, null);
        classificationService.saveOrUpdate(classification);
        
        commitAndStartNewTransaction(null);

        // this will write flat xml file to the same package in the test resources 
        // the test file is named after the test class like: TestClassName.xml
		writeDbUnitDataSetFile(new String[] {
		        "TAXONBASE", "TAXONNAMEBASE","IDENTIFIER","TAXONBASE_IDENTIFIER",
		        "TAXONNAMEBASE_IDENTIFIER",
		        "REFERENCE",
		        "CLASSIFICATION", "CLASSIFICATION_TAXONNODE", "TAXONNODE",
		        "HOMOTYPICALGROUP",
		        "TERMVOCABULARY",
		        "DEFINEDTERMBASE"
		 });
            
    }


}
