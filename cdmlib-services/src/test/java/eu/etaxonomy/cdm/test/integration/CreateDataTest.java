/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.integration;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.database.CdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

public class CreateDataTest {
	private static Logger logger = Logger.getLogger(CreateDataTest.class);

	private static boolean isCreated;
	private CdmApplicationController app;
	public static final String genusUuid = "c399e245-3def-427d-8502-afa0ae87e875";
	public static final String genusNameUuid = "d399e245-3def-427d-8502-afa0ae87e875";

	private static boolean ignore = true;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (ignore){
			logger.warn("\nCreateDataTest ignored !\n");
			return;
		}
		logger.info("setUpBeforeClass");
		isCreated = false;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		if (ignore){return;}
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		if (isCreated == false){
			 dbSchemaValidation = DbSchemaValidation.CREATE;
		}
		ICdmDataSource dataSource = cdm_test();
		app  = CdmApplicationController.NewInstance(dataSource, dbSchemaValidation);
	}

	@After
	public void tearDown() throws Exception {
		if (ignore){return;}
		isCreated = true;
		app.close();
	}

	//just temporarly
	public static ICdmDataSource cdm_test(){
		//DatabaseTypeEnum dbType = DatabaseTypeEnum.MySQL;
		String cdmServer = "192.168.2.10";
		String cdmDB = "cdm_test_andreasM";
		String cdmUserName = "edit";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}


	//just temporarly
	public static ICdmDataSource paddie(){
//		DatabaseTypeEnum dbType = DatabaseTypeEnum.SqlServer2005;
		String cdmServer = "PADDIE";
		String cdmDB = "edit_test";
		String cdmUserName = "andreas";
		return makeDestination(cdmServer, cdmDB, -1, cdmUserName, null);
	}

	/**
	 * initializes source
	 * @return true, if connection establisehd
	 */
	private static ICdmDataSource makeDestination(String cdmServer, String cdmDB, int port, String cdmUserName, String pwd ){
		//establish connection
		try {
			if (pwd == null){
				pwd = CdmUtils.readInputLine("Please insert password for " + CdmUtils.Nz(cdmUserName) + ": ");
			}
			//TODO not MySQL
			ICdmDataSource destination = CdmDataSource.NewMySqlInstance(cdmServer, cdmDB, port, cdmUserName, pwd, null);
			//ICdmDataSource destination = CdmDataSource.NewSqlServer2005Instance(cdmServer, cdmDB, cdmUserName, pwd);
			return destination;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


/* ********************* TESTS *********************************/

	@Ignore
	@Test
	public void testCreateTaxon(){
		if (ignore){return;}
		//Taxon with childs, basionym, childrens synonyms, child misapplied Name
		Taxon genusTaxon = eu.etaxonomy.cdm.datagenerator.TaxonGenerator.getTestTaxon();
		genusTaxon.setUuid(UUID.fromString(genusUuid));
		genusTaxon.getName().setUuid(UUID.fromString(genusNameUuid));

//		Synonym syn2 = Synonym.NewInstance(genusTaxon.getName(), null);

		app.getTaxonService().save(genusTaxon);
		//app.getTaxonService().saveTaxon(syn2);
	}

	@Test
	public void testLoadTaxon(){
		if (ignore){return;}
		//Taxon with childs, basionym, childrens synonyms, child misapplied Name


		TaxonNameBase<?,?> genusName2 = app.getNameService().find(UUID.fromString(genusNameUuid));
		Set<TaxonBase> set = genusName2.getTaxonBases();
		System.out.println("Size:" + set.size());
		for (TaxonBase tb : set){
			System.out.println(tb.getName());
		}

		//taxon
		Taxon genusTaxon = (Taxon)app.getTaxonService().find(UUID.fromString(genusUuid));
		assertNotNull(genusTaxon);
		//name
		BotanicalName genusName = (BotanicalName)genusTaxon.getName();
		assertNotNull(genusName);
		Set<TaxonBase> taxaSet = genusName.getTaxonBases();
		for (TaxonBase tb : taxaSet){
			System.out.println(tb.getName());
		}

		//taxonBases of Name
		Set<TaxonBase> taxonBases = genusName.getTaxonBases();
		logger.warn(taxonBases.size());
		Set<Taxon> children = genusTaxon.getTaxonomicChildren();
		for (Taxon child : children){
			child.getSynonyms();
			child.getMisappliedNames();
			child.getHomotypicGroup();
			child.getHomotypicSynonymsByHomotypicGroup();
			child.getHomotypicSynonymsByHomotypicRelationship();
		}

		Set<TaxonDescription> descriptions = genusTaxon.getDescriptions();
		assertEquals(1, descriptions.size());
		TaxonDescription firstDescription = descriptions.iterator().next();


		Set<DescriptionElementBase> descriptionElements = firstDescription.getElements();
		//assertEquals(2, descriptions.size());

		Language language = Language.DEFAULT();
		for (DescriptionElementBase descriptionElement : descriptionElements){
			if (descriptionElement instanceof TextData){
				TextData textData = (TextData)descriptionElement;
				textData.getText(language);
				System.out.println(textData);
			}else if(descriptionElement instanceof CommonTaxonName){
				CommonTaxonName commonTaxonName = (CommonTaxonName)descriptionElement;
				commonTaxonName.getName();
				commonTaxonName.getLanguage();
				System.out.println(commonTaxonName);
			}else{
				fail();
			}
		}

	}


	@Ignore
	@Test
	public void testSave(){
		if (ignore){return;}
		logger.warn("testSave");
		ITaxonService taxonService = app.getTaxonService();
		Taxon genusTaxon = (Taxon)taxonService.find(UUID.fromString(genusUuid));
		BotanicalName genusName = (BotanicalName)genusTaxon.getName();
		genusName.setGenusOrUninomial("newGenusUninomial");
		genusName.setUpdated(new DateTime());
		BotanicalName newName = BotanicalName.NewInstance(Rank.SPECIES());
		Taxon newTaxon = Taxon.NewInstance(newName, genusTaxon.getSec());
		genusTaxon.addTaxonomicChild(newTaxon, null, "5677");
		UUID uuid = taxonService.save(newTaxon).getUuid();
		assertNotNull(uuid);
	}


}
