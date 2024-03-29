/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.compare.taxon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.comparators.ReverseComparator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.test.unit.EntityTestBase;

/**
 * @author a.mueller
 * @since 04.03.2016
 */
public class HomotypicGroupTaxonComparatorTest extends EntityTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private Reference sec;
    private Reference ref1;
    private Reference ref2;
    private Reference ref3;
    private Calendar cal1;
    private Calendar cal2;
    private Calendar cal3;
    private TaxonName botName1;
    private TaxonName botName2;
    private TaxonName botName3;
    private TaxonName botName4;
    private TaxonName botName5;

    private List<TaxonBase<?>> list;

    private Taxon taxon1;
    private Synonym synonym2;
    private Synonym synonym3;

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

        ref1.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(cal1));
        ref2.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(cal2));
        ref3.setDatePublished(VerbatimTimePeriod.NewVerbatimInstance(cal3));

        botName1 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botName2 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botName3 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botName4 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botName5 =  TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());

        setNameParts(botName1, "Aaa", "aaa");
        setNameParts(botName2, "Bbb", "bbb");
        setNameParts(botName3, "Ccc", "ccc");
        setNameParts(botName4, "Ddd", "ddd");
        setNameParts(botName5, "Eee", "eee");

//        zooName1.setPublicationYear(1823);

        list = new ArrayList<>();

        taxon1 = Taxon.NewInstance(botName1, sec);
    }

    private void setNameParts(INonViralName name, String genus, String speciesEpi) {
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
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4, null, null, homotypicalGroup);
        botName2.addBasionym(botName4);


        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for first group should come first", botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals(botName5, list.get(2).getName());
        Assert.assertEquals(botName3, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName4.addReplacedSynonym(botName5, null, null, null, null);
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
        synonym3 = taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5);
        botName3.addBasionym(botName5);  //also merges homotypic groups

        taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4);
        botName2.addBasionym(botName4);

        list.addAll(taxon1.getSynonyms());
        UUID uuidFirst =  UUID.fromString("00000001-fd5f-4a91-bf28-ea432bd1dab5");
        UUID uuidSecond = UUID.fromString("ffffffff-fd5f-4a91-bf28-ea432bd1dab5");
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
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4, null, null, homotypicalGroup);
        botName2.addBasionym(botName4);

        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym with date should comes first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //even with nom. illeg. the name with date should come first
        NomenclaturalStatus illegStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
        botName5.addStatus(illegStatus);

        Assert.assertEquals("basionym with date should comes first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName5.addReplacedSynonym(botName4, null, null, null, null);
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
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4, null, null, homotypicalGroup);
        botName2.addBasionym(botName4);

        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym with rank species should comes first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName4, list.get(2).getName());
        Assert.assertEquals(botName2, list.get(3).getName());

        //add replaced synonym relation between basionyms
        botName5.addReplacedSynonym(botName4, null, null, null, null);
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
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4, null, null, homotypicalGroup);
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
    public void testCompare_BasionymGroupsWithNomIlleg() {
        NomenclaturalStatus illegStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
        botName4.addStatus(illegStatus);

        //2 basionym groups

        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName3);
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        botName3.addBasionym(botName5);

        synonym2 = taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName4, null, null, homotypicalGroup);
//        botName2.addBasionym(botName4);

        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for non nom. illeg. group should come first", botName2, list.get(0).getName());
        Assert.assertEquals(botName5, list.get(1).getName());
        Assert.assertEquals(botName3, list.get(2).getName());
        Assert.assertEquals("Nom illeg should come last", botName4, list.get(3).getName());

        //name having a nom. illeg. as basionym should stay in its basionym group
        //NOTE: this is dirty data as nom. illeg. is not allowed as basionym by the code
        botName2.addBasionym(botName4);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for non nom. illeg. group should come first", botName5, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals("Nom illeg basionym group should come last", botName4, list.get(2).getName());
        Assert.assertEquals("Names with nom. illeg. as basionym should stay in basionym group", botName2, list.get(3).getName());

        //non basionym nom. illeg. should not change the order
        botName4.removeStatus(illegStatus);
        botName2.addStatus(illegStatus);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("basionym for non nom. illeg. group should come first", botName4, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals("Nom illeg basionym group should come last", botName5, list.get(2).getName());
        Assert.assertEquals("Names with nom. illeg. as basionym should stay in basionym group", botName3, list.get(3).getName());
    }

    @Test  //failing selenium test
    public void testCompare_NomIllegWithDate() {
        NomenclaturalStatus illegStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
        botName3.addStatus(illegStatus);
        botName3.setNomenclaturalReference(ref1);
        botName2.setNomenclaturalReference(ref2);

        taxon1.addHomotypicSynonymName(botName3);
        taxon1.addHomotypicSynonymName(botName2);

        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(taxon1));

        Assert.assertEquals("Earlier nom. illeg. should come next", botName3, list.get(0).getName());
        Assert.assertEquals("Later name should come last", botName2, list.get(1).getName());
    }

    @Test
    public void testCompare_NoCircularProblemsWithBasionyms() {

        taxon1.addHomotypicSynonymName(botName3);
        taxon1.addHomotypicSynonymName(botName5);
        botName3.addBasionym(botName5);

        Assert.assertEquals(botName1.getHomotypicalGroup(), botName5.getHomotypicalGroup());
        botName5.addBasionym(botName1);
        botName1.addBasionym(botName3);

        list.addAll(taxon1.getSynonyms());
        list.add(taxon1);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals(botName1, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName5, list.get(2).getName());

        //additional basionym for botName3
        taxon1.addHomotypicSynonymName(botName2);
        Assert.assertEquals(botName1.getHomotypicalGroup(), botName2.getHomotypicalGroup());
        botName3.addBasionym(botName2);
        list.clear();
        list.addAll(taxon1.getSynonyms());
        list.add(taxon1);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals(botName1, list.get(0).getName());
        Assert.assertEquals(botName2, list.get(1).getName());
        Assert.assertEquals(botName3, list.get(2).getName());
        Assert.assertEquals(botName5, list.get(3).getName());
    }

    @Test
    public void testCompare_NoCircularProblemsWithReplacedSynonyms() {

        taxon1.addHomotypicSynonymName(botName3);
        taxon1.addHomotypicSynonymName(botName5);
        botName3.addReplacedSynonym(botName5, null, null, null, null);

        Assert.assertEquals(botName1.getHomotypicalGroup(), botName5.getHomotypicalGroup());
        botName5.addReplacedSynonym(botName1, null, null, null, null);
        botName1.addReplacedSynonym(botName3, null, null, null, null);

        list.addAll(taxon1.getSynonyms());
        list.add(taxon1);
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals(botName1, list.get(0).getName());
        Assert.assertEquals(botName3, list.get(1).getName());
        Assert.assertEquals(botName5, list.get(2).getName());
    }

    @Test
    public void testCompare_NomenclaturalStanding() {

        //default behavior without nomenclatural standing
        HomotypicalGroup homotypicalGroup = botName2.getHomotypicalGroup();
        taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName5, null, null, homotypicalGroup);
        list.addAll(taxon1.getSynonyms());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("Bbb should come before Eee in same homotypic group", botName2, list.get(0).getName());
        Assert.assertEquals("Bbb should come before Eee in same homotypic group", botName5, list.get(1).getName());

        //with nomenclatural standing
        botName2.addRelationshipToName(botName3, NameRelationshipType.MISSPELLING());
        Collections.sort(list, new HomotypicGroupTaxonComparator(null));

        Assert.assertEquals("Invalid designation should come after valid name", botName5, list.get(0).getName());
        Assert.assertEquals("Invalid designation should come after valid name", botName2, list.get(1).getName());

    }

    @Test
    public void testCompare_hybrids() {

        String name2 = "Opuntia "+UTF8.HYBRID+"rubiflora Davidson";
        String name3 = "Opuntia rubiflora Davidson";
        List<String> strList = Arrays.asList(new String[]{name2, name3});
        Collections.sort(strList);
        Assert.assertEquals("Non hybrid name should come first in alphabetical order", name3, strList.get(0));

        botName2 = TaxonNameFactory.PARSED_BOTANICAL(name2);
        botName3 = TaxonNameFactory.PARSED_BOTANICAL(name3);
        taxon1.addHeterotypicSynonymName(botName2);
        taxon1.addHeterotypicSynonymName(botName3, null, null, botName2.getHomotypicalGroup());
        list.addAll(taxon1.getSynonyms());
        HomotypicGroupTaxonComparator comparator = new HomotypicGroupTaxonComparator(null);
        Collections.sort(list, comparator);

        Assert.assertEquals("Hybrid should come after non-hybrid", botName3, list.get(0).getName());
        Assert.assertEquals("Hybrid should come after non-hybrid", botName2, list.get(1).getName());

        ReverseComparator<TaxonBase<?>> reverseComparator = new ReverseComparator<>(comparator);
        Collections.sort(list, reverseComparator);
        Assert.assertEquals("Hybrid should come before non-hybrid in reverse order", botName2, list.get(0).getName());
        Assert.assertEquals("Hybrid should come before non-hybrid in reverse order", botName3, list.get(1).getName());
    }
}