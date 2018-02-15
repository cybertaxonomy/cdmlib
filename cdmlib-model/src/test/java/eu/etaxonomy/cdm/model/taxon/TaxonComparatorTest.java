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

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 11.06.2008
 */
public class TaxonComparatorTest {
    private static final Logger logger = Logger.getLogger(TaxonComparatorTest.class);

    @BeforeClass
    public static void setUpBeforeClass() {
        DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
        vocabularyStore.initialize();
    }


/******************** TESTS *****************************************************/

    /**
     * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonComparator#compare(eu.etaxonomy.cdm.model.taxon.TaxonBase, eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public void testCompare() {

        List<TaxonBase<?>> list = createTestData();
        Collections.sort(list, new TaxonComparator());

        //Order should be
//        4: 1823
//        3: 1943
//        1: 1945
//        2:
        Assert.assertEquals(list.get(0).getId(), 3);
        Assert.assertEquals(getYear(list.get(0)), "1759");
        Assert.assertEquals(list.get(1).getId(), 1);
        Assert.assertEquals(getYear(list.get(1)), "1759");
        Assert.assertEquals(list.get(2).getId(), 5);
        Assert.assertEquals(getYear(list.get(2)), "1823");
        Assert.assertEquals(list.get(3).getId(), 4);
        Assert.assertEquals(getYear(list.get(3)), "1943");
        Assert.assertEquals(list.get(4).getId(), 2);
        Assert.assertEquals(getYear(list.get(4)), "");



    }


    /**
     * @return
     */
    private List<TaxonBase<?>> createTestData() {
        Reference sec = ReferenceFactory.newBook();

        Reference ref1 = ReferenceFactory.newArticle();
        ref1.setAbbrevTitle("Gard. Dict. ed. 7 110, 2 1759");
        Reference ref2 = ReferenceFactory.newBook();
        Reference ref3 = ReferenceFactory.newBook();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        Calendar cal3 = Calendar.getInstance();
        cal1.set(1759, 3, 2);
        cal2.set(1856, 3, 2);
        cal3.set(1943, 3, 2);

        ref1.setDatePublished(TimePeriod.NewInstance(cal1));
//		ref2.setDatePublished(TimePeriod.NewInstance(cal2));
        ref3.setDatePublished(TimePeriod.NewInstance(cal3));

        Person author1 = Person.NewTitledInstance("Rehder");
        author1.setNomenclaturalTitle("Rehder");

        Person author4 = Person.NewTitledInstance("Mill.");
        author1.setNomenclaturalTitle("Mill.");

        Person author2 = Person.NewTitledInstance("A.Murray");
        author2.setNomenclaturalTitle("A.Murray");
        Person author3 = Person.NewTitledInstance("Lemmon");
        author3.setNomenclaturalTitle("Lemmon");

        IBotanicalName botName1 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(),
                "Abies", null, "procera",null,
                author1, ref1, null, null);
        IBotanicalName botName2 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES(),
                "Abies", null, "magnifica", null,
                author2, ref2, null, null);
        IBotanicalName botName3 =   TaxonNameFactory.NewBotanicalInstance(Rank.GENUS(),
                "Abies", null, null,null,
                author4, ref1, null, null);
        IBotanicalName botName4 =   TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY(),
                "Abies", null, "magnifica","shastensis",
                author3, ref3, null, null);

        IZoologicalName zooName1 = TaxonNameFactory.NewZoologicalInstance(null);


       // botName3.setNomenclaturalReference(ref3);
        zooName1.setPublicationYear(1823);

        List<TaxonBase<?>> list = new ArrayList<TaxonBase<?>>();

        Taxon taxon1 = Taxon.NewInstance(botName1, sec);
        Taxon taxon2 = Taxon.NewInstance(botName2, sec);
        Taxon taxon3 = Taxon.NewInstance(botName3, sec);
        Taxon taxon4 = Taxon.NewInstance(botName4, sec);
        Taxon zooTaxon4 = Taxon.NewInstance(zooName1, sec);

        taxon1.setId(1);
        taxon2.setId(2);
        taxon3.setId(3);
        taxon4.setId(4);
        zooTaxon4.setId(5);

        list.add(taxon3);
        list.add(taxon2);
        list.add(taxon1);
        list.add(taxon4);
        list.add(zooTaxon4);
        return list;
    }

    private List<TaxonNode> createTestDataWithTaxonNodes(){
        List<TaxonBase<?>> taxa = createTestData();
        List<TaxonNode> result = new ArrayList();
        TaxonNode node;
        Classification classification = Classification.NewInstance("TestClassification");
        int index = 0;
        for (TaxonBase<?> taxon: taxa){
            if (taxon instanceof Taxon){
                node = classification.addChildTaxon((Taxon)taxon, null, null);
                node.setId(++index);
                result.add(node);
            }
        }
        return result;
    }


    /**
     * @param taxonBase
     * @return
     */
    private String getYear(TaxonBase<?> taxon) {
        String year = "";
        TaxonName tnb = taxon.getName();
        if (tnb.isZoological()){
            year = String.valueOf(tnb.getPublicationYear());
        }else{
            year = tnb.getNomenclaturalReference().getYear();
        }
        return year;
    }

    @Test
    public void testCompareTaxonNodeByName(){
        List<TaxonNode> list = createTestDataWithTaxonNodes();

        Collections.sort(list, new TaxonNodeByNameComparator());
        for(TaxonNode node: list){
            System.out.println(node.getTaxon().getName().getFullTitleCache());
        }

        //Order should be
//        5: without rank
//        1: Genus
//        2: Species (Abies magnifica A.Murray)
//        4: subSpecies (Abies magnifica var. shastensis Lemmon)
//        3: Species (Abies procera Rehder)
        Assert.assertEquals(list.get(0).getId(), 5);
        Assert.assertEquals(list.get(1).getId(), 1);
        Assert.assertEquals(list.get(2).getId(), 2);
        Assert.assertEquals(list.get(3).getId(), 4);
        Assert.assertEquals(list.get(4).getId(), 3);



    }
}
