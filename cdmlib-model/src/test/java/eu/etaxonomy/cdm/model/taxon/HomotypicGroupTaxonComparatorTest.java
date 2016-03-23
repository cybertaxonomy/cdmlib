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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 04.03.2016
 */
public class HomotypicGroupTaxonComparatorTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(HomotypicGroupTaxonComparatorTest.class);

    private Reference<?> sec;
    private Reference<?> ref1;
    private Reference<?> ref2;
    private Reference<?> ref3;
    private Calendar cal1;
    private Calendar cal2;
    private Calendar cal3;
    private BotanicalName botName1;
    private BotanicalName botName2;
    private BotanicalName botName3;
    private BotanicalName botName4;
    private BotanicalName botName5;

    private List<TaxonBase<?>> list;

    private Taxon taxon1;
    private Synonym synonym2;
    private Synonym synonym3;

    @BeforeClass
    public static void setUpBeforeClass() {
        DefaultTermInitializer vocabularyStore = new DefaultTermInitializer();
        vocabularyStore.initialize();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        sec = ReferenceFactory.newBook();

        ref1 = ReferenceFactory.newBook();
        ref2 = ReferenceFactory.newBook();
        ref3 = ReferenceFactory.newBook();
        cal1 = Calendar.getInstance();
        cal2 = Calendar.getInstance();
        cal3 = Calendar.getInstance();
        cal1.set(1856, 3, 2);
        cal2.set(1943, 3, 2);
        cal3.set(1945, 3, 2);

        ref1.setDatePublished(TimePeriod.NewInstance(cal1));
        ref2.setDatePublished(TimePeriod.NewInstance(cal2));
        ref3.setDatePublished(TimePeriod.NewInstance(cal3));

        botName1 =  BotanicalName.NewInstance(Rank.SPECIES());
        botName2 =  BotanicalName.NewInstance(Rank.SPECIES());
        botName3 =  BotanicalName.NewInstance(Rank.SPECIES());
        botName4 =  BotanicalName.NewInstance(Rank.SPECIES());
        botName5 =  BotanicalName.NewInstance(Rank.SPECIES());

        setNameParts(botName1, "Aaa", "aaa");
        setNameParts(botName2, "Bbb", "bbb");
        setNameParts(botName3, "Ccc", "ccc");
        setNameParts(botName4, "Ddd", "ddd");
        setNameParts(botName5, "Eee", "eee");

//        zooName1.setPublicationYear(1823);

        list = new ArrayList<TaxonBase<?>>();

        taxon1 = Taxon.NewInstance(botName1, sec);


    }


    /**
     *
     */
    private void setNameParts(NonViralName<?> name, String genus, String speciesEpi) {
        name.setGenusOrUninomial(genus);
        name.setSpecificEpithet(speciesEpi);
        name.getTitleCache();
    }


/******************** TESTS *****************************************************/

    @Test
    public void testCompare_BasionymGroupsTogether() {
        //2 basionym groups

        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5, homotypicalGroup, null, null);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2).getSynonym();
        taxon1.addHeterotypicSynonymName(botName4, homotypicalGroup, null, null);
        botName2.addBasionym(botName4);


        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for first group should come first", botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals(botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName4.addReplacedSynonym(botName5, null, null, null);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));
        Assert.assertEquals("basionym of second group should come first now as it is the replaced synonym",
                            botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals("replacement name should come after replaced synonym but first in basionym group",
                            botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //set a name as first name
        Collections.sort(list, new HomotypicGroupTaxonComparator(synonym2));

        Assert.assertEquals("name2 should come first now",
                            botName2, list.get(0).getName());
        Assert.assertEquals("name2 should be followed by its basionym", botName4, list.get(1).getName());
        Assert.assertEquals("other basionym group should come later but with basionym first",
                            botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());


    }

    @Test
    public void testCompare_BasionymGroupsSeparated() {

        //2 basionym groups in 2 homotypic groups
        synonym3 = taxon1.addHeterotypicSynonymName(botName3).getSynonym();
        taxon1.addHeterotypicSynonymName(botName5);
        botName3.addBasionym(botName5);  //also merges homotypic groups

        taxon1.addHeterotypicSynonymName(botName2).getSynonym();
        taxon1.addHeterotypicSynonymName(botName4);
        botName2.addBasionym(botName4);

        list.addAll(taxon1.getSynonyms());

        UUID uuidFirst = UUID.fromString("000000972-d164-4cb5-9a1e-d6216cc858f6");
        UUID uuidSecond = UUID.fromString("ffffff972-d164-4cb5-9a1e-d6216cc858f6");
        Assert.assertTrue(uuidFirst.toString().compareTo(uuidSecond.toString())<-1);

        botName4.getHomotypicalGroup().setUuid(uuidFirst);
        botName5.getHomotypicalGroup().setUuid(uuidSecond);


        //start test
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for first group should come first", botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals(botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());

        //set a name as first name
        //TODO
        //first taxon in group should have an impact only for the homotypic group to which it belongs
        //but this is still under discussion and may change in future
        Collections.sort(list, new HomotypicGroupTaxonComparator(synonym3));

        Assert.assertEquals(botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals(botName3, list.get(2).getName());
        Assert.assertEquals(botName5, list.get(3).getName());
    }

    @Test
    public void testCompare_BasionymGroupsSomeWithYears() {

        //2 basionym groups, 1 new combination with year (botName2) and 1 basionym with year (botName5)
        //The later should come first according to the rules, though alphabetically being
        //basionym botName4

        botName2.setNomenclaturalReference(ref2);
        botName5.setNomenclaturalReference(ref3);

        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5, homotypicalGroup, null, null);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2).getSynonym();
        taxon1.addHeterotypicSynonymName(botName4, homotypicalGroup, null, null);
        botName2.addBasionym(botName4);


        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym with date should comes first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName5.addReplacedSynonym(botName4, null, null, null);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));
        Assert.assertEquals("basionym of second group should come first now as it is the replaced synonym",
                            botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals("replacement name should come after replaced synonym but first in basionym group",
                            botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());
    }

    @Test
    public void testCompare_BasionymGroupsWithRanks1() {

        botName2.setRank(Rank.VARIETY());
        botName2.setInfraSpecificEpithet("varbbbb");

        botName3.setRank(Rank.VARIETY());
        botName3.setInfraSpecificEpithet("subspccc");

        botName4.setRank(Rank.SUBSPECIES());
        botName4.setInfraSpecificEpithet("subspddd");


        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5, homotypicalGroup, null, null);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2).getSynonym();
        taxon1.addHeterotypicSynonymName(botName4, homotypicalGroup, null, null);
        botName2.addBasionym(botName4);


        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym with rank species should comes first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName5.addReplacedSynonym(botName4, null, null, null);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));
        Assert.assertEquals("basionym of second group should come first now as it is the replaced synonym",
                            botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals("replacement name should come after replaced synonym but first in basionym group",
                            botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());
    }

    @Test
    public void testCompare_BasionymGroupsWithRanks2() {


        botName2.setRank(Rank.VARIETY());
        botName2.setInfraSpecificEpithet("varbbbb");

        botName3.setRank(Rank.VARIETY());
        botName3.setInfraSpecificEpithet("subspccc");

        botName4.setRank(Rank.SUBSPECIES());
        botName4.setInfraSpecificEpithet("subspddd");

        botName5.setRank(Rank.VARIETY());
        botName5.setInfraSpecificEpithet("vareee");


        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5, homotypicalGroup, null, null);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2).getSynonym();
        taxon1.addHeterotypicSynonymName(botName4, homotypicalGroup, null, null);
        botName2.addBasionym(botName5);
        botName4.addBasionym(botName5);


        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym should comes first", botName5, list.get(0).getName());
        Assert.assertEquals("subspecies should come next", botName4, list.get(1).getName());
        Assert.assertEquals("variety with b should come next", botName2, list.get(2).getName());
        Assert.assertEquals("variety with c should come last", botName3, list.get(3).getName());

    }

    @Test
    public void testCompare_NoCircularProblems() {

        taxon1.addHomotypicSynonymName(botName3, null, null);
        taxon1.addHomotypicSynonymName(botName5, null, null);
        botName3.addBasionym(botName5);

//        taxon1.addHomotypicSynonymName(botName2, null, null);
//        taxon1.addHomotypicSynonymName(botName4, null, null);
//        botName2.addBasionym(botName4);

        Assert.assertEquals(botName1.getHomotypicalGroup(), botName5.getHomotypicalGroup());
        botName5.addBasionym(botName1);
        botName1.addBasionym(botName3);

        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

    }

}
