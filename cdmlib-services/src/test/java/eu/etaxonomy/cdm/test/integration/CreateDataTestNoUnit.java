/**
* Copyright (C) 2007 EDIT
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

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 28.05.2008
 * @version 1.0
 */
public class CreateDataTestNoUnit {
	private static Logger logger = Logger.getLogger(CreateDataTestNoUnit.class);
	
	private CdmApplicationController app;
	
	
	public void test(){
		boolean isCreated = true;
		DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;
		if (isCreated == false){
			 dbSchemaValidation = DbSchemaValidation.CREATE;
		}
		ICdmDataSource dataSource = CreateDataTest.cdm_test();
		app  = CdmApplicationController.NewInstance(dataSource, dbSchemaValidation);

		TaxonNameBase<?,?> genusName2 = (TaxonNameBase<?,?>)app.getNameService().find(UUID.fromString(CreateDataTest.genusNameUuid));
		Set<TaxonBase> set = (Set<TaxonBase>)genusName2.getTaxonBases();
		System.out.println("Size:" + set.size());
		for (TaxonBase tb : set){
			System.out.println(tb.getName());
		}
		
		//taxon
		Taxon genusTaxon = (Taxon)app.getTaxonService().find(UUID.fromString(CreateDataTest.genusUuid));
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
		assertEquals(2, descriptions.size());
		TaxonDescription description = descriptions.iterator().next();
		
		
		Set<DescriptionElementBase> descriptionElements = description.getElements();
		
		Language language = Language.DEFAULT(); 
		for (DescriptionElementBase descriptionElement : descriptionElements){
			if (descriptionElement instanceof TextData){
				TextData textData = (TextData)descriptionElement;
				textData.getText(language);
			}else if(descriptionElement instanceof CommonTaxonName){
				CommonTaxonName commonTaxonName = (CommonTaxonName)descriptionElement;
				commonTaxonName.getName();
				commonTaxonName.getLanguage();
			}else{
				fail();
			}
		}

	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CreateDataTestNoUnit test = new CreateDataTestNoUnit();
		test.test();
	}
}
