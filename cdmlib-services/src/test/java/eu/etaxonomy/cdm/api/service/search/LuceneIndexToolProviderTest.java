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

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
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
public class LuceneIndexToolProviderTest extends CdmIntegrationTest {

    @SpringBeanByType
    private ILuceneIndexToolProvider luceneIndexToolProvider;

    @Test
    public void testgetQueryParserFor_1(){
        Assert.assertEquals(QueryParser.class, luceneIndexToolProvider.getQueryParserFor(Taxon.class, false).getClass());
        Assert.assertEquals(ComplexPhraseQueryParser.class, luceneIndexToolProvider.getQueryParserFor(Taxon.class, true).getClass());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
