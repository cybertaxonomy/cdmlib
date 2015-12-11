// $Id$
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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * @author a.mueller
 * @date 23.08.2011
 *
 */
@Ignore
public class PolytomousKeyServiceImplTest extends CdmTransactionalIntegrationTest {
	private static final Logger logger = Logger.getLogger(PolytomousKeyServiceImplTest.class);

	@SpringBeanByType
	private IPolytomousKeyService service;
	
	@SpringBeanByType
	private IPolytomousKeyNodeService nodeService;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	
//************************* TESTS ********************************************/	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.ServiceBase#count(java.lang.Class)}.
	 */
	@Test
	public void testCount() {
		PolytomousKey key = PolytomousKey.NewTitledInstance("My test key");
		service.save(key);
		Taxon taxon = Taxon.NewInstance(null, null);
		
		
		key.addTaxonomicScope(taxon);
		
//		Annotation annotation = Annotation.NewInstance("Any annotation", Language.DEFAULT());
//		key.addAnnotation(annotation);
		
		PolytomousKeyNode child = PolytomousKeyNode.NewInstance();
		Taxon taxon2 = Taxon.NewInstance(null, null);
		
		child.setTaxon(taxon2);
		key.getRoot().addChild(child);
		
		service.save(key);
		setComplete(); 
		endTransaction();
		System.out.println("Count");
		printDataSet(System.out, new String[]{"PolytomousKey", "POLYTOMOUSKEYNODE", "POLYTOMOUSKEYNODE_LANGUAGESTRING", 
				"POLYTOMOUSKEY_ANNOTATION","POLYTOMOUSKEY_CREDIT",
				"POLYTOMOUSKEY_EXTENSION", "POLYTOMOUSKEY_MARKER", "POLYTOMOUSKEY_NAMEDAREA",
				"POLYTOMOUSKEY_ORIGINALSOURCEBASE", "POLYTOMOUSKEY_RIGHTS", "POLYTOMOUSKEY_SCOPE",
				"POLYTOMOUSKEY_TAXON", "POLYTOMOUSKEY_TAXONBASE",
				"ANNOTATION","TAXONBASE"});
//		printDataSet(System.out);
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.ServiceBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	@DataSet
	public void testDelete() {
		System.out.println("Delete start");
		printDataSet(System.out, new String[]{"PolytomousKey", "POLYTOMOUSKEYNODE", "POLYTOMOUSKEYNODE_LANGUAGESTRING", 
				"POLYTOMOUSKEY_ANNOTATION","POLYTOMOUSKEY_CREDIT",
				"POLYTOMOUSKEY_EXTENSION", "POLYTOMOUSKEY_MARKER", "POLYTOMOUSKEY_NAMEDAREA",
				"POLYTOMOUSKEY_ORIGINALSOURCEBASE", "POLYTOMOUSKEY_RIGHTS", "POLYTOMOUSKEY_SCOPE",
				"POLYTOMOUSKEY_TAXON", "POLYTOMOUSKEY_TAXONBASE",
				"ANNOTATION","TAXONBASE"});
		
		UUID uuid = UUID.fromString("0a709940-4f2e-43c1-8db1-f4745f2a4889");
		PolytomousKey key = service.find(uuid);
		PolytomousKeyNode someChild = key.getRoot().getChildren().iterator().next();
//		service.delete(key);
		key.getRoot().removeChild(someChild);
		nodeService.delete(someChild);
		
		
		setComplete(); 
		endTransaction();
		System.out.println("Delete End");
		
		printDataSet(System.out, new String[]{"PolytomousKey", "POLYTOMOUSKEYNODE", "POLYTOMOUSKEYNODE_LANGUAGESTRING", 
				"POLYTOMOUSKEY_ANNOTATION","POLYTOMOUSKEY_CREDIT",
				"POLYTOMOUSKEY_EXTENSION", "POLYTOMOUSKEY_MARKER", "POLYTOMOUSKEY_NAMEDAREA",
				"POLYTOMOUSKEY_ORIGINALSOURCEBASE", "POLYTOMOUSKEY_RIGHTS", "POLYTOMOUSKEY_SCOPE",
				"POLYTOMOUSKEY_TAXON", "POLYTOMOUSKEY_TAXONBASE"});
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.ServiceBase#exists(java.util.UUID)}.
	 */
	@Test
	public void testExists() {
		logger.warn("testExists not yet implemented");
	}

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
        
    }

}
