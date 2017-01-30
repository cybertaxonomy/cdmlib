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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 11.06.2008
 */
public class TaxonComparatorTest {
    private static final Logger logger = Logger.getLogger(TaxonComparatorTest.class);

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }


/******************** TESTS *****************************************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public void testCompare() {

        Reference sec = ReferenceFactory.newBook();

        Reference ref1 = ReferenceFactory.newBook();
        Reference ref2 = ReferenceFactory.newBook();
        Reference ref3 = ReferenceFactory.newBook();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        Calendar cal3 = Calendar.getInstance();
        cal1.set(1945, 3, 2);
        cal2.set(1856, 3, 2);
        cal3.set(1943, 3, 2);

        ref1.setDatePublished(TimePeriod.NewInstance(cal1));
//		ref2.setDatePublished(TimePeriod.NewInstance(cal2));
        ref3.setDatePublished(TimePeriod.NewInstance(cal3));

        BotanicalName botName1 =  TaxonNameBase.NewBotanicalInstance(null);
        BotanicalName botName2 =  TaxonNameBase.NewBotanicalInstance(null);
        BotanicalName botName3 =  TaxonNameBase.NewBotanicalInstance(null);
        ZoologicalName zooName1 = TaxonNameFactory.NewZoologicalInstance(null);

        botName1.setNomenclaturalReference(ref1);
        botName2.setNomenclaturalReference(ref2);
        botName3.setNomenclaturalReference(ref3);
        zooName1.setPublicationYear(1823);

        List<TaxonBase<?>> list = new ArrayList<TaxonBase<?>>();

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

        //Order should be
//        4: 1823
//        3: 1943
//        1: 1945
//        2:
        Assert.assertEquals(list.get(0).getId(), 4);
        Assert.assertEquals(getYear(list.get(0)), "1823");
        Assert.assertEquals(list.get(1).getId(), 3);
        Assert.assertEquals(getYear(list.get(1)), "1943");
        Assert.assertEquals(list.get(2).getId(), 1);
        Assert.assertEquals(getYear(list.get(2)), "1945");
        Assert.assertEquals(list.get(3).getId(), 2);
        Assert.assertEquals(getYear(list.get(3)), "");


    }


    /**
     * @param taxonBase
     * @return
     */
    private String getYear(TaxonBase<?> taxon) {
        String year = "";
        TaxonNameBase<?,?> tnb = taxon.getName();
        if (tnb instanceof ZoologicalName){
            year = String.valueOf(((ZoologicalName)tnb).getPublicationYear());
        }else{
            year = tnb.getNomenclaturalReference().getYear();
        }
        return year;
    }
}
