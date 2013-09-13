/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;

import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.database.annotations.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
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


}
