/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.search;

import java.io.FileNotFoundException;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TermQuery;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

/**
 * @author a.kohlbecker
 * @date Jan 13, 2017
 *
 */
public class QueryFactoryTest extends CdmIntegrationTest {

    @SpringBeanByType
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    @Test
    public void testNewTermQuery_textfield_simple(){

        QueryFactory qf = new QueryFactory(luceneIndexToolProvider, Taxon.class);
        Assert.assertEquals(TermQuery.class, qf.newTermQuery("titleCache", "Lactuca", true).getClass());
        Assert.assertEquals(BooleanQuery.class, qf.newTermQuery("titleCache", "Lactuca perennis", true).getClass());
        Assert.assertEquals(PrefixQuery.class, qf.newTermQuery("titleCache", "Lactu*", true).getClass());
        Assert.assertEquals(PhraseQuery.class, qf.newTermQuery("titleCache", "\"Lactuca perennis\"", true).getClass());
    }

    @Test
    public void testNewTermQuery_textfield_complex(){

        QueryFactory qf = new QueryFactory(luceneIndexToolProvider, Taxon.class);
        Assert.assertEquals("ComplexPhraseQuery", qf.newTermQuery("titleCache", "\"Lactuca per*\"", true).getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
