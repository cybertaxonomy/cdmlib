/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.test.unit;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author m.doering
 * Basic CDM unit testing class that incorporates the Spring Context for DI
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/eu/etaxonomy/cdm/applicationContext.xml"})
@TransactionConfiguration(defaultRollback=true)
@Transactional
public abstract class CdmUnitTestBase{
	
}
