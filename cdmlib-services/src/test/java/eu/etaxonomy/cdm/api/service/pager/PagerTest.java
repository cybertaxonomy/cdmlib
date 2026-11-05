/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.pager;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class PagerTest {

    private List<Taxon> taxa;

    @BeforeEach
    public void setUp() {
                taxa = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
                taxa.add(Taxon.NewInstance(null, null));
        }
    }

    /**
     * The default is to supply the first page (pageNumber = null == pageNumber = 0)
     * So supplying null should not be a problem.
     */
    @Test
    public void testPagerWithNullPageNumber() {
        Pager<Taxon> pager = new DefaultPagerImpl<>(null, Long.valueOf(1), 30, taxa);
        assertNotNull(pager);
    }

    /**
     * The pager should be able to cope with no results being supplied
     */
    @Test
    public void testPagerWithNoResults() {
    	Pager<Taxon> pager = new DefaultPagerImpl<>(null, Long.valueOf(0), null, new ArrayList<>());
        assertNotNull(pager);
    }

    /**
     * What happens when we supply a pagesize of 0? surely we should throw an error
     */
    @Test
    public void testPagerWithInvalidPageSize() {
    	Pager<Taxon> pager = new DefaultPagerImpl<>(null, Long.valueOf(0), 0, new ArrayList<>());
        assertNotNull(pager);
    }

    @Test
    public void testPagerWithDivisibleNumberOfPages() {
        Pager<Taxon> pager = new DefaultPagerImpl<>(0, Long.valueOf(60), 30, taxa);

        assertEquals(60l, pager.getCount().longValue());
        assertEquals(0, pager.getCurrentIndex().intValue());
        assertEquals(1, pager.getFirstRecord().intValue());
        assertEquals(30, pager.getLastRecord().intValue());
        assertArrayEquals(new Integer[] {0,1}, pager.getIndices().toArray(new Integer[pager.getIndices().size()]));
        assertNull(pager.getPrevIndex());
        assertEquals(1,pager.getNextIndex().intValue());
        assertEquals("1 - 30", pager.getPageNumber(0));
        assertEquals("31 - 60", pager.getPageNumber(1));
    }

    @Test
    public void testPagerWithNonDivisibleNumberOfPages() {
        Pager<Taxon> pager = new DefaultPagerImpl<>(0, Long.valueOf(68), 30, taxa);

        assertEquals(68l, pager.getCount().longValue());
        assertEquals(0, pager.getCurrentIndex().intValue());
        assertEquals(1, pager.getFirstRecord().intValue());
        assertEquals(30, pager.getLastRecord().intValue());
        assertArrayEquals(new Integer[] {0,1,2}, pager.getIndices().toArray(new Integer[pager.getIndices().size()]));
        assertNull(pager.getPrevIndex());
        assertEquals(1,pager.getNextIndex().intValue());
        assertEquals("1 - 30", pager.getPageNumber(0));
        assertEquals("31 - 60", pager.getPageNumber(1));
        assertEquals("61 - 68", pager.getPageNumber(2));
    }

    @Test
    public void testPagerWithLargeNumberAndNonDivisibleNumberOfPages() {
        Pager<Taxon> pager = new DefaultPagerImpl<>(4, Long.valueOf(256), 30, taxa);

        assertEquals(256l, pager.getCount().longValue());
        assertEquals(4, pager.getCurrentIndex().intValue());
        assertEquals(121, pager.getFirstRecord().intValue());
        assertEquals(150, pager.getLastRecord().intValue());
        assertArrayEquals(new Integer[] {1,2,3,4,5,6}, pager.getIndices().toArray(new Integer[pager.getIndices().size()]));
        assertEquals(3,pager.getPrevIndex().intValue());
        assertEquals(5,pager.getNextIndex().intValue());

        assertEquals("31 - 60", pager.getPageNumber(1));
        assertEquals("61 - 90", pager.getPageNumber(2));
        assertEquals("91 - 120", pager.getPageNumber(3));
        assertEquals("121 - 150", pager.getPageNumber(4));
        assertEquals("151 - 180", pager.getPageNumber(5));
        assertEquals("181 - 210", pager.getPageNumber(6));
    }
}