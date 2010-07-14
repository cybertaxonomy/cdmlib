/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 *
 */
@Transactional(TransactionMode.DISABLED)
public class IdentifiableServiceBaseTest extends CdmTransactionalIntegrationTest {
	private static final Logger logger = Logger.getLogger(IdentifiableServiceBaseTest.class);
	
	
	@SpringBeanByType
	private INameService nameService;
	
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
		nameService.updateTitleCache(clazz, stepSize, null);
//		TaxonNameBase name = nameService.find(UUID.fromString("5d74500b-9fd5-4d18-b9cd-cc1c8a372fec"));
//		setComplete();
//		endTransaction();
//		try {
//			printDataSet(new FileOutputStream("C:\\tmp\\test.xml"), new String[]{"TaxonNameBase"});
//		} catch(Exception e) { 
//			logger.warn(e);
//		} 
		
	}


	public static Logger getLogger() {
		return logger;
	}
}
