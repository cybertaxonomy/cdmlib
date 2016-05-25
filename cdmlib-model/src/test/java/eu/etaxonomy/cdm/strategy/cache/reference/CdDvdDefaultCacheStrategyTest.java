/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

public class CdDvdDefaultCacheStrategyTest {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdDvdDefaultCacheStrategyTest.class);

	Reference cdDvd;
	String title;
	String publisher;
	String place;
	TimePeriod publicationDate;
	CdDvdDefaultCacheStrategy instance;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.cdDvd = ReferenceFactory.newCdDvd();
		title = "A nice CD title";
		cdDvd.setTitle(title);
		publisher = "An ugly publisher";
		place = "A beutiful place";
		publicationDate = TimePeriod.NewInstance(1999, 2001);
		cdDvd.setDatePublished(publicationDate);
		this.instance = CdDvdDefaultCacheStrategy.NewInstance();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNewInstance() {
		assertNotNull(instance);
	}

	@Test
	public void testGetTitleWithoutYearAndAuthor() {
		String result = instance.getTitleWithoutYearAndAuthor(cdDvd, false);
		assertEquals(title, result);
	}

	//TODO missing publicationPlace and publisher has to be discussed
	@Test
	public void testGetTitleCache() {
		String result = instance.getTitleCache(cdDvd);
		assertEquals(title + ". 1999-2001", result);
	}


}
