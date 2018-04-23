/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IDescriptionService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.TaxonServiceImplTest;
import eu.etaxonomy.cdm.datagenerator.TaxonGenerator;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * test for the {@link FirstDataInserter}
 *
 * @author a.kohlbecker
 * @since Oct 12, 2012
 *
 */
@DataSet
public class FirstDataInserterTest extends CdmTransactionalIntegrationTest {

    private final String[] tableNames = new String[]{"USERACCOUNT", "USERACCOUNT_GRANTEDAUTHORITYIMPL", "GRANTEDAUTHORITYIMPL", "CDMMETADATA"};

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @Test
    @DataSet(value="FirstDataInserterTest.testBlankDB.xml")
    @ExpectedDataSet(value="FirstDataInserterTest.testBlankDB-result.xml")
    public void testBlankDB(){

        commitAndStartNewTransaction(null);
//        printDataSet(System.err, tableNames);
    }

    @Test
    @DataSet(value="FirstDataInserterTest.testBlankDB.xml")
    @Ignore
    public void testInsertData(){
    	commitAndStartNewTransaction(null);

    	Taxon newTaxon = TaxonGenerator.getTestTaxon();
    	UUID taxonUUID = taxonService.save(newTaxon).getUuid();

    	TaxonDescription description = TaxonGenerator.getTestDescription(1);
    	newTaxon.addDescription(description);
    	Set<TaxonDescription> descriptions = newTaxon.getDescriptions();
    	descriptionService.save(description);

    	newTaxon = null;
    	newTaxon = (Taxon)taxonService.find(taxonUUID);
    	descriptions = newTaxon.getDescriptions();
    	assertEquals(2, descriptions.size());
    }


    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub
    }

}
