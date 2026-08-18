/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Assert;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.persistence.dao.reference.IReferenceDao;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;

/**
 * Test for class ReferenceFilters from cdmlib-cache. Can't be tested in cdmlib-cache
 * because it needs access to the database.
 *
 * @author muellera
 * @since 17.08.2026
 */
public class ReferenceFiltersTest extends CdmTransactionalIntegrationTest {

    @SpringBeanByType
    private IReferenceDao refDao;

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.filter.ReferenceFilters#isBeforeDatePublished(org.joda.time.DateTime)}.
     */
    @Test
    public void testIsBeforeDatePublished() {
        final String title = "abc";

        //create data
        //.. earlier reference
        Reference ref1 = ReferenceFactory.newBook();
        ref1.setDatePublished(TimePeriodParser.parseStringVerbatim("1972"));
        ref1.setTitle(title);
        refDao.save(ref1);
        //.. later reference
        Reference ref2 = ReferenceFactory.newBook();
        ref2.setTitle(title);
        ref2.setDatePublished(TimePeriodParser.parseStringVerbatim("2055"));
        refDao.save(ref2);
        //.. without year
        Reference ref3 = ReferenceFactory.newBook();
        ref3.setTitle(title);
        ref3.getDatePublished().setStart(new Partial()
                .with(DateTimeFieldType.monthOfYear(), 5));
        refDao.save(ref3);
        //.. without date
        Reference ref4 = ReferenceFactory.newBook();
        refDao.save(ref4);


        //validate
        List<EntityFilter<Reference>> filters = new ArrayList<>();
        DateTime compareDate = DateTime.parse("2026-06-24");
        filters.add(ReferenceFilters.isBeforeDatePublished(compareDate));
        long result = refDao.countByTitle("abc", MatchMode.ANYWHERE, filters);
        Assert.assertEquals("Only the earlier references 1972 should be returned", 1, result);

        List<Reference> resultList = refDao.findByTitle(Reference.class, title, MatchMode.ANYWHERE, filters, null, null, null, null);
        Assert.assertEquals("1972", resultList.get(0).getDatePublishedString());

        //validate against future date
        filters = new ArrayList<>();
        compareDate = DateTime.parse("3026-06-24");
        filters.add(ReferenceFilters.isBeforeDatePublished(compareDate));
        result = refDao.countByTitle("abc", MatchMode.ANYWHERE, filters);
        Assert.assertEquals("All references with defined date before 3026 should be returned", 2, result);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
