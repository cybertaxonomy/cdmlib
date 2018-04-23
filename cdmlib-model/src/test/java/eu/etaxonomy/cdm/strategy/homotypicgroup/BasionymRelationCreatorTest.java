// $Id$
/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.homotypicgroup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * @author a.mueller
 \* @since 22.04.2017
 *
 */
public class BasionymRelationCreatorTest {

    /**
     *
     */
    private static final String SPECIUS = "specius";
    private Taxon taxon1;
    private Synonym synonym1;
    private Synonym synonym2;
    private Synonym synonym3;
    private TaxonName accName1;
    private TaxonName synName1;
    private TaxonName synName2;
    private TaxonName synName3;
    private Person person1;
    private Person person2;
    private Person person3;
    private BasionymRelationCreator guesser;


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (NameRelationshipType.BASIONYM() == null){
            new DefaultTermInitializer().initialize();
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        guesser = new BasionymRelationCreator();

        person1 = Person.NewInstance();
        person2 = Person.NewInstance();
        person3 = Person.NewInstance();
        person1.setNomenclaturalTitle("Pers1");
        person2.setNomenclaturalTitle("Pers2");
        person3.setNomenclaturalTitle("Pers3");

        accName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        synName1 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        synName2 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        synName3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        accName1.setGenusOrUninomial("Accepted");
        synName1.setGenusOrUninomial("Genus1");
        synName2.setGenusOrUninomial("Genus2");
        synName3.setGenusOrUninomial("Genus3");
        accName1.setSpecificEpithet(SPECIUS);
        synName1.setSpecificEpithet(SPECIUS);
        synName2.setSpecificEpithet(SPECIUS);
        synName3.setSpecificEpithet(SPECIUS);

        taxon1 = Taxon.NewInstance(accName1, null);
        synonym1 = Synonym.NewInstance(synName1, null);
        synonym2 = Synonym.NewInstance(synName2, null);
        synonym3 = Synonym.NewInstance(synName3, null);

        taxon1.addSynonym(synonym1, SynonymType.SYNONYM_OF());
        taxon1.addSynonym(synonym2, SynonymType.SYNONYM_OF());
        taxon1.addSynonym(synonym3, SynonymType.SYNONYM_OF());
    }

    @Test
    public void testMatchingSimple() {
        accName1.setCombinationAuthorship(person1);
        synName1.setBasionymAuthorship(person1);
        synName2.setBasionymAuthorship(person1);
        synName2.setInfraSpecificEpithet(synName2.getSpecificEpithet());
        synName2.setSpecificEpithet("xyz");
        synName2.setRank(Rank.VARIETY());
        synName3.setBasionymAuthorship(person3);
        HomotypicalGroup accNameGroup = accName1.getHomotypicalGroup();
        guesser.invoke(taxon1);
        Assert.assertEquals("Accepted and synonym1 should have same homotypic group", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertEquals("Accepted and synonym2 should have same homotypic group", accName1.getHomotypicalGroup(), synName2.getHomotypicalGroup());
        Assert.assertEquals("Homotypical group shoul be taken from basionym", accNameGroup, accName1.getHomotypicalGroup());
        Assert.assertNotEquals("Accepted and synonym3 should not have same homotypic group due to different author", accName1.getHomotypicalGroup(), synName3.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym1.getType());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym2.getType());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym3.getType());
    }

    @Test
    public void testMatchingSynonym() {
        accName1.setBasionymAuthorship(person1);
        synName1.setCombinationAuthorship(person1);
        synName2.setBasionymAuthorship(person1);
        synName2.setInfraSpecificEpithet(synName2.getSpecificEpithet());
        synName2.setSpecificEpithet("xyz");
        synName2.setRank(Rank.VARIETY());
        synName3.setBasionymAuthorship(person3);
        HomotypicalGroup accNameGroup = accName1.getHomotypicalGroup();
        guesser.invoke(taxon1);
        Assert.assertEquals("Accepted and synonym1 should have same homotypic group", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertEquals("Synonym1 and synonym2 should have same homotypic group", accName1.getHomotypicalGroup(), synName2.getHomotypicalGroup());
        Assert.assertEquals("Accepted and synonym2 should have same homotypic group", accName1.getHomotypicalGroup(), synName2.getHomotypicalGroup());
        Assert.assertEquals("Homotypical group shoul be taken from basionym", accNameGroup, accName1.getHomotypicalGroup());
        Assert.assertNotEquals("Accepted and synonym3 should not have same homotypic group due to different author", accName1.getHomotypicalGroup(), synName3.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym1.getType());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym2.getType());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym3.getType());
    }

    @Test
    public void testNonMatchingSimple() {
        accName1.setCombinationAuthorship(person1);
        synName1.setBasionymAuthorship(person1);
        synName1.setSpecificEpithet("spefides");
        synName2.setBasionymAuthorship(person2);
        HomotypicalGroup accNameGroup = accName1.getHomotypicalGroup();
        guesser.invoke(taxon1);
        Assert.assertEquals("Homotypical group shoul be taken from basionym", accNameGroup, accName1.getHomotypicalGroup());
        Assert.assertNotEquals("Different last epithets should not match", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertNotEquals("Different authors should not match", accName1.getHomotypicalGroup(), synName2.getHomotypicalGroup());
        Assert.assertNotEquals("Missing basionym author should not match", accName1.getHomotypicalGroup(), synName3.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym1.getType());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym2.getType());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym3.getType());
    }

    @Test
    public void testMatchingNormalization() {
        accName1.setCombinationAuthorship(person1);
        synName1.setBasionymAuthorship(person1);
        synName1.setSpecificEpithet("specia");
        synName2.setBasionymAuthorship(person1);
        synName2.setInfraSpecificEpithet("specios");
        synName2.setSpecificEpithet("xyz");
        synName2.setRank(Rank.VARIETY());
        synName3.setBasionymAuthorship(person1);
        synName1.setSpecificEpithet("specium");

        guesser.invoke(taxon1);
        Assert.assertEquals("Accepted and synonym1 should have same homotypic group", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertEquals("Accepted and synonym2 should have same homotypic group", accName1.getHomotypicalGroup(), synName2.getHomotypicalGroup());
        Assert.assertEquals("Accepted and synonym3 should have same homotypic group", accName1.getHomotypicalGroup(), synName3.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym1.getType());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym2.getType());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym3.getType());
    }

    @Test
    public void testMatchingNomTitle() {
        accName1.setCombinationAuthorship(person1);
        synName1.setBasionymAuthorship(person2);
        guesser.invoke(taxon1);
        Assert.assertNotEquals("Accepted and synonym1 should NOT have same homotypic group", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.SYNONYM_OF(), synonym1.getType());

        person2.setNomenclaturalTitle(person1.getNomenclaturalTitle());
        guesser.invoke(taxon1);
        Assert.assertEquals("Accepted and synonym1 should have same homotypic group", accName1.getHomotypicalGroup(), synName1.getHomotypicalGroup());
        Assert.assertEquals(SynonymType.HOMOTYPIC_SYNONYM_OF(), synonym1.getType());
    }

}
