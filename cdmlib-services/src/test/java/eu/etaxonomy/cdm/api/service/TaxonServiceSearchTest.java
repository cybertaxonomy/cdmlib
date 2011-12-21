/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.search.ICdmMassIndexer;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.babadshanjan
 * @created 04.02.2009
 * @version 1.0
 */
public class TaxonServiceSearchTest extends CdmIntegrationTest {
    private static Logger logger = Logger.getLogger(TaxonServiceSearchTest.class);

    @SpringBeanByType
    private ITaxonService taxonService;
    @SpringBeanByType
    private INameService nameService;
    @SpringBeanByType
    private ICdmMassIndexer indexer;


    @Test
    public void testDbUnitUsageTest() throws Exception {
        assertNotNull("taxonService should exist", taxonService);
        assertNotNull("nameService should exist", nameService);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#findTaxaAndNames(eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator)}.
     */
    //@Ignore // TaxonServiceSearchTest.xml cannot be inserted - structure changed?
    @Test
    @DataSet
    public final void testFindTaxaAndNames() {

        ITaxonServiceConfigurator configurator = new TaxonServiceConfiguratorImpl();
        configurator.setTitleSearchString("Abies*");
        configurator.setMatchMode(MatchMode.BEGINNING);
        configurator.setDoTaxa(true);
        configurator.setDoSynonyms(true);
        configurator.setDoNamesWithoutTaxa(true);
        configurator.setDoTaxaByCommonNames(true);
        Pager<IdentifiableEntity> pager = taxonService.findTaxaAndNames(configurator);
        List<IdentifiableEntity> list = pager.getRecords();
        if (logger.isDebugEnabled()) {
            for (int i = 0; i < list.size(); i++) {
                String nameCache = "";
                if (list.get(i) instanceof NonViralName) {
                    nameCache = ((NonViralName<?>)list.get(i)).getNameCache();
                } else if (list.get(i) instanceof TaxonBase) {
                    TaxonNameBase taxonNameBase= ((TaxonBase)list.get(i)).getName();
                    nameCache = ((NonViralName)taxonNameBase).getNameCache();
                } else {}
                logger.debug(list.get(i).getClass() + "(" + i +")" +
                        ": Name Cache = " + nameCache + ", Title Cache = " + list.get(i).getTitleCache());
            }
        }
        System.err.println("number of taxa: "+list.size());
        assertTrue(list.size()==8);
    }

    /**
     * Test method for
     * {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#searchTaxaByName(java.lang.String, eu.etaxonomy.cdm.model.reference.Reference)}
     * .
     */
    @Test
//    @DataSet
    public final void testSearchTaxaByName() {
        logger.warn("testSearchTaxaByName not yet implemented"); // TODO
    }

    @Test
    @DataSet
    public final void testFindByDescriptionElementFullText() throws CorruptIndexException, IOException, ParseException {
        indexer.reindex();
        Pager<TaxonBase> pager;
        pager = taxonService.findByDescriptionElementFullText("abies", null, null, null, null);
        Assert.assertTrue("Expecting at least one enitity", pager.getCount() > 0);

    }

//	@Test
//	@DataSet
//	public final void testPrintDataSet() {
//		printDataSet(System.out);
//	}

}
