/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 11.06.2008
 * @version 1.0
 */
public class TaxonComparatorTest {
	private static final Logger logger = Logger.getLogger(TaxonComparatorTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/******************** TESTS *****************************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public void testCompare() {
		logger.debug("start testCompare");
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		
		ReferenceBase sec = refFactory.newBook();
		
		ReferenceBase ref1 = refFactory.newBook();
		ReferenceBase ref2 = refFactory.newBook();
		ReferenceBase ref3 = refFactory.newBook();
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		Calendar cal3 = Calendar.getInstance();
		cal1.set(1945, 3, 2);
		cal2.set(1856, 3, 2);
		cal3.set(1943, 3, 2);

		ref1.setDatePublished(TimePeriod.NewInstance(cal1));
//		ref2.setDatePublished(TimePeriod.NewInstance(cal2));
		ref3.setDatePublished(TimePeriod.NewInstance(cal3));
		
		BotanicalName botName1 =  BotanicalName.NewInstance(null);
		BotanicalName botName2 =  BotanicalName.NewInstance(null);
		BotanicalName botName3 =  BotanicalName.NewInstance(null);
		ZoologicalName zooName1 = ZoologicalName.NewInstance(null);
		
		botName1.setNomenclaturalReference(ref1);
		botName2.setNomenclaturalReference(ref2);
		botName3.setNomenclaturalReference(ref3);
		zooName1.setPublicationYear(1823);
		
		List<TaxonBase> list = new ArrayList<TaxonBase>();
		
		Taxon taxon1 = Taxon.NewInstance(botName1, sec);
		Taxon taxon2 = Taxon.NewInstance(botName2, sec);
		Taxon taxon3 = Taxon.NewInstance(botName3, sec);
		Taxon zooTaxon4 = Taxon.NewInstance(zooName1, sec);
		
		taxon1.setId(1);
		taxon2.setId(2);
		taxon3.setId(3);
		zooTaxon4.setId(4);
		
		
		list.add(taxon3);
		list.add(taxon2);
		list.add(taxon1);
		list.add(zooTaxon4);
		Collections.sort(list, new TaxonComparator());
		
		for (TaxonBase taxon : list){
			String year = "";
			TaxonNameBase<?,?> tnb = taxon.getName();
			if (tnb instanceof ZoologicalName){
				year = String.valueOf(((ZoologicalName)tnb).getPublicationYear());
			}else{
				year = tnb.getNomenclaturalReference().getYear();
			}
			System.out.println(taxon.getId() + ": " + year);
		}
	}
}
