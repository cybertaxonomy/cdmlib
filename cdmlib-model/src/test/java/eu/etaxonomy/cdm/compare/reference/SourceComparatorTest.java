/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.reference;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.reference.NamedSource;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author muellera
 * @since 06.08.2024
 */
public class SourceComparatorTest {

    private static final UUID uuid1 = UUID.fromString("00e54c86-77e0-4493-b088-29c965463c57");
    private static final UUID uuid2 = UUID.fromString("ff5a4ce4-228a-4df6-8307-9f3ae1b42c9f");

    @Test
    public void test() {


        OriginalSourceBase source1 = NamedSource.NewPrimarySourceInstance(null, null);
        OriginalSourceBase source2 = NamedSource.NewPrimarySourceInstance(null, null);
        source1.setUuid(uuid1);
        source2.setUuid(uuid2);

        test(source1, source2, 1);

        //other direction
        source1 = NamedSource.NewPrimarySourceInstance(null, null);
        source2 = NamedSource.NewPrimarySourceInstance(null, null);
        source1.setUuid(uuid1);
        source2.setUuid(uuid2);

        test(source2, source1, -1);


    }

    private void test(OriginalSourceBase source1, OriginalSourceBase source2, int expected) {

        //both null
        SourceComparator comparator = SourceComparator.Instance();
        Assert.assertEquals(expected, comparator.compare(source1, source2));

        //1  has citation
        source1.setCitation(ReferenceFactory.newBook());
        Assert.assertEquals(expected, comparator.compare(source1, source2));

        //1 has date published
        source1.getCitation().setDatePublished(VerbatimTimePeriod.NewVerbatimInstance());
        Assert.assertEquals(expected, comparator.compare(source1, source2));

        //1 has year
        source1.getCitation().getDatePublished().setStartYear(1988);
        Assert.assertEquals(4, comparator.compare(source1, source2));

        //2 also has year, year is equal
        source2.setCitation(ReferenceFactory.newBook());
        source2.getCitation().setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(1988));
        Assert.assertEquals(expected, comparator.compare(source1, source2));

        //2 year is later
        source2.getCitation().getDatePublished().setStartYear(1989);
        Assert.assertEquals(-1, comparator.compare(source1, source2));

        //2 year is earlier
        source2.getCitation().getDatePublished().setStartYear(1987);
        Assert.assertEquals(1, comparator.compare(source1, source2));
    }
}
