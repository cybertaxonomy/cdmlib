/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedCacheHelper;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @created 26.11.2008
 */
public class NonViralNameDefaultCacheStrategyTest extends NameCacheStrategyTestBase{
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategyTest.class);

    private NonViralNameDefaultCacheStrategy<NonViralName> strategy;

    private static final String familyNameString = "Familia";
    private static final String genusNameString = "Genus";
    private static final String speciesNameString = "Abies alba";
    private static final String subSpeciesNameString = "Abies alba subsp. beta";
    private static final String appendedPhraseString = "app phrase";

    private static final String authorString = "L.";
    private static final String exAuthorString = "Exaut.";
    private static final String basAuthorString = "Basio, A.";
    private static final String exBasAuthorString = "ExBas. N.";

    private static final String referenceTitle = "My Reference";

    private BotanicalName familyName;
    private BotanicalName genusName;
    private BotanicalName subGenusName;
    private BotanicalName speciesName;
    private BotanicalName subSpeciesName;
    private TeamOrPersonBase<?> author;
    private TeamOrPersonBase<?> exAuthor;
    private TeamOrPersonBase<?> basAuthor;
    private TeamOrPersonBase<?> exBasAuthor;
    private Reference citationRef;

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
        strategy = NonViralNameDefaultCacheStrategy.NewInstance();
        familyName = TaxonNameBase.PARSED_BOTANICAL(familyNameString, Rank.FAMILY());
        genusName = TaxonNameBase.PARSED_BOTANICAL(genusNameString, Rank.GENUS());

        subGenusName = TaxonNameBase.NewBotanicalInstance(Rank.SUBGENUS());
        subGenusName.setGenusOrUninomial("Genus");
        subGenusName.setInfraGenericEpithet("InfraGenericPart");

        speciesName = TaxonNameBase.PARSED_BOTANICAL(speciesNameString);
        subSpeciesName = TaxonNameBase.PARSED_BOTANICAL(subSpeciesNameString);

        author = Person.NewInstance();
        author.setNomenclaturalTitle(authorString);
        exAuthor = Person.NewInstance();
        exAuthor.setNomenclaturalTitle(exAuthorString);
        basAuthor = Person.NewInstance();
        basAuthor.setNomenclaturalTitle(basAuthorString);
        exBasAuthor = Person.NewInstance();
        exBasAuthor.setNomenclaturalTitle(exBasAuthorString);

        citationRef = ReferenceFactory.newGeneric();
        citationRef.setTitleCache(referenceTitle, true);

    }

//**************************** TESTS **************************************************

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testGetTitleCache() {
        Assert.assertEquals(speciesNameString, speciesName.getTitleCache());
        //TODO not yet completed
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testGetFullTitleCache() {
        subSpeciesName.setNomenclaturalReference(citationRef);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle, subSpeciesName.getFullTitleCache());
        //TODO not yet completed
    }

    @Test
    public void testGattungsAutonyme() {
    	BotanicalName botName = TaxonNameBase.NewBotanicalInstance(Rank.SECTION_BOTANY());
		String strTaraxacum = "Traxacum";
		botName.setGenusOrUninomial(strTaraxacum);
		botName.setInfraGenericEpithet(strTaraxacum);
		botName.setAuthorshipCache("Author");
		Assert.assertFalse(botName.getFullTitleCache().contains("bot."));
		//TODO #4288
		System.out.println(botName.getFullTitleCache());
    }



    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testGetNameCache() {
        assertEquals("Species Name should be Abies alba", speciesNameString, speciesName.getNameCache());
        speciesName.setNameCache("Any species");
        assertEquals("Species Name should be Any species", "Any species", speciesName.getNameCache());
        assertEquals("Species Name should be Any species", "Any species", speciesName.getTitleCache());
        assertEquals("subSpeciesNameString should be correct", subSpeciesNameString, subSpeciesName.getNameCache());
        BotanicalName botName = TaxonNameBase.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("", "Lepidocaryum tenue var. tenue", botName.getNameCache());
        BotanicalName specName = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
        specName.setGenusOrUninomial("Genus");
        specName.setSpecificEpithet("");
        assertEquals("Empty species string must not result in trailing whitespace", "Genus", specName.getNameCache());

        //unranked taxa
        String unrankedCache;
        BotanicalName unrankedName = TaxonNameBase.NewBotanicalInstance(Rank.INFRASPECIFICTAXON());
        unrankedName.setGenusOrUninomial("Genus");
        NonViralNameDefaultCacheStrategy<BotanicalName> strategy = NonViralNameDefaultCacheStrategy.NewInstance();
            //infraspecific
        unrankedName.setInfraSpecificEpithet("infraspecific");
        unrankedName.setSpecificEpithet("species");
        unrankedCache = strategy.getNameCache(unrankedName);

        Assert.assertEquals("Correct unranked cache expected", "Genus species [infraspec.] infraspecific", unrankedCache);

            //infrageneric
        unrankedName.setRank(Rank.INFRAGENERICTAXON());
        unrankedName.setInfraSpecificEpithet(null);
        unrankedName.setSpecificEpithet(null);
        unrankedName.setInfraGenericEpithet("Infrageneric");
        unrankedCache = strategy.getNameCache(unrankedName);
        Assert.assertEquals("Correct unranked cache expected", "Genus [unranked] Infrageneric", unrankedCache);

        //bot. specific ranks
        botName = TaxonNameBase.NewBotanicalInstance(Rank.SECTION_BOTANY());
        botName.setGenusOrUninomial("Genus");
        botName.setInfraGenericEpithet("Infragenus");
        Assert.assertEquals("", "Genus sect. Infragenus", botName.getNameCache());
        botName.setRank(Rank.SUBSECTION_BOTANY());
        Assert.assertEquals("", "Genus subsect. Infragenus", botName.getNameCache());

        //zool. specific ranks (we don't have markers here therefore no problem should exist
        ZoologicalName zooName = TaxonNameBase.NewZoologicalInstance(Rank.SECTION_ZOOLOGY());
        zooName.setGenusOrUninomial("Genus");
        zooName.setInfraGenericEpithet("Infragenus");
        Assert.assertEquals("", "Genus", zooName.getNameCache());
        zooName.setRank(Rank.SUBSECTION_ZOOLOGY());
        Assert.assertEquals("", "Genus", zooName.getNameCache());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testNameCacheWithInfraGenericEpithet() {
        speciesName.setInfraGenericEpithet("Infraabies");
        assertEquals("Species Name should be Abies (Infraabies) alba", "Abies (Infraabies) alba", speciesName.getNameCache());

        BotanicalName botName = TaxonNameBase.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setInfraGenericEpithet("Infralepi");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("Name cache should be Lepidocaryum (Infralepi) tenue var. tenue", "Lepidocaryum (Infralepi) tenue var. tenue", botName.getNameCache());

        botName.setInfraGenericEpithet(" ");
        //Note: This test may fail if aspectj doesn't work correctly
        assertEquals("Empty infrageneric epithet must be neglegted", "Lepidocaryum tenue var. tenue", botName.getNameCache());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getAuthorshipCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testGetAuthorshipCache() {
        this.speciesName.setCombinationAuthorship(author);
        assertEquals(author.getNomenclaturalTitle(), speciesName.getAuthorshipCache());
        this.speciesName.setBasionymAuthorship(basAuthor);
        String expected = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+author.getNomenclaturalTitle();
        assertEquals(expected, speciesName.getAuthorshipCache());
        String authorshipcache = "authorshipcache";
        speciesName.setAuthorshipCache(authorshipcache);
        assertEquals(authorshipcache, speciesName.getAuthorshipCache());
        speciesName.setCombinationAuthorship(exAuthor);
        assertEquals(authorshipcache, speciesName.getAuthorshipCache()); //cache is protected
        assertEquals(speciesNameString + " " + authorshipcache, speciesName.getFullTitleCache());
        //unprotected
        speciesName.setProtectedAuthorshipCache(false);
        String atomizedAuthorCache = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitle();
        assertEquals(atomizedAuthorCache, speciesName.getAuthorshipCache());
        String atomizedTitleCache = speciesNameString + " "+ strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitle();
        //Note: This test may fail if aspectj doesn't work correctly
        assertEquals(atomizedTitleCache, speciesName.getTitleCache());
        assertEquals(atomizedTitleCache, speciesName.getFullTitleCache());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getAuthorshipCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testHybridNames() {
        //Note \u00D7 : hybrid sign (multiplication sign)
        this.speciesName.setCombinationAuthorship(author);
        Assert.assertEquals(author.getNomenclaturalTitle(), speciesName.getAuthorshipCache());
        Assert.assertEquals("Should be Abies alba L.", "Abies alba L.", speciesName.getTitleCache());

        speciesName.setBinomHybrid(true);
        //Note: This test may fail if aspectj doesn't work correctly
        Assert.assertEquals("Should be Abies \u00D7alba L.", "Abies \u00D7alba L.", speciesName.getTitleCache());
        speciesName.setMonomHybrid(true);
        Assert.assertEquals("Should be '\u00D7Abies \u00D7alba L.'", "\u00D7Abies \u00D7alba L.", speciesName.getTitleCache());

        Assert.assertEquals("Should be 'Genus'", "Genus", genusName.getTitleCache());
        genusName.setMonomHybrid(true);
        Assert.assertEquals("Should be '\u00D7Genus'", "\u00D7Genus", genusName.getTitleCache());

        Assert.assertEquals("Should be 'Abies alba subsp. beta'", subSpeciesNameString, subSpeciesName.getTitleCache());
        subSpeciesName.setTrinomHybrid(true);
        Assert.assertEquals("Should be 'Abies alba nothosubsp. beta or nbeta'", "Abies alba nothosubsp. beta", subSpeciesName.getTitleCache());
        subSpeciesName.setMonomHybrid(true);
        Assert.assertEquals("Should be '\u00D7Abies alba nothosubsp. beta'", "\u00D7Abies alba nothosubsp. beta", subSpeciesName.getTitleCache());

        Assert.assertEquals("Should be 'Genus subg. InfraGenericPart'", "Genus subg. InfraGenericPart", subGenusName.getTitleCache());
        subGenusName.setBinomHybrid(true);
        Assert.assertEquals("Should be 'Genus nothosubg. InfraGenericPart'", "Genus nothosubg. InfraGenericPart", subGenusName.getTitleCache());
    }

    @Test
    public void testHybridFormula(){
        this.speciesName.setCombinationAuthorship(author);
        Assert.assertEquals(author.getNomenclaturalTitle(), speciesName.getAuthorshipCache());
        Assert.assertEquals("Should be 'Abies alba L.'", "Abies alba L.", speciesName.getTitleCache());

        NonViralName<?> hybridName = TaxonNameBase.NewNonViralInstance(Rank.SPECIES());
        NonViralName<?> secondParent = TaxonNameBase.NewNonViralInstance(Rank.SPECIES());

        secondParent.setTitleCache("Second parent Mill.", true);
        hybridName.addHybridParent(speciesName, HybridRelationshipType.FIRST_PARENT(), null);
        hybridName.addHybridParent(secondParent, HybridRelationshipType.SECOND_PARENT(), null);
        hybridName.setHybridFormula(true);

        Assert.assertEquals("", "Abies alba L. \u00D7 Second parent Mill.", hybridName.getTitleCache());

    }

    //TODO add more tests when specification is clearer
    @Test
    public void testOriginalSpelling() {
    	NameRelationshipType origSpellingType = NameRelationshipType.ORIGINAL_SPELLING();
    	NonViralName<?> originalName = (NonViralName<?>)speciesName.clone();
    	originalName.setSpecificEpithet("alpa");
    	Assert.assertEquals("Preconditions are wrong", "Abies alpa", originalName.getNameCache());

    	speciesName.addRelationshipFromName(originalName, origSpellingType, null);
    	Assert.assertEquals("Abies alba 'alpa'", speciesName.getNameCache());
    	originalName.setGenusOrUninomial("Apies");

    	speciesName.setNameCache(null, false);
    	//TODO update cache of current name (species name)
    	Assert.assertEquals("Abies alba 'Apies alpa'", speciesName.getNameCache());

    	//TODO add more tests when specification of exact behaviour is clearer
    }

    @Test
    public void testCacheListener() {
        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("GenericRef",true);
        this.subSpeciesName.setNomenclaturalReference(ref);
        Assert.assertEquals("Expected full title cache has error", "Abies alba subsp. beta, GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setCombinationAuthorship(author);
        subSpeciesName.setBasionymAuthorship(basAuthor);
        Assert.assertEquals("Expected full title cache has error", "Abies alba subsp. beta (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache());
        //cascade name change to fullTitleCache
        subSpeciesName.setRank(Rank.SPECIES());
        subSpeciesName.setProtectedNameCache(true);
        Assert.assertNull("name cache should be null", subSpeciesName.getNameCache());
        subSpeciesName.setProtectedNameCache(false);
        Assert.assertNotNull("name cache should not be null", subSpeciesName.getNameCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) L.", subSpeciesName.getTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba", subSpeciesName.getNameCache());

        subSpeciesName.setProtectedNameCache(true);
        subSpeciesName.setSpecificEpithet("gamma");
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) L.", subSpeciesName.getTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba", subSpeciesName.getNameCache());
        //make original status
        subSpeciesName.setRank(Rank.SUBSPECIES());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache());

        //author change
        author.setNomenclaturalTitle("M.");
        Assert.assertEquals("Expected full title cache has error", "(Basio, A.) M.", subSpeciesName.getAuthorshipCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) M.", subSpeciesName.getTitleCache());

        //protectedTitleCache
        subSpeciesName.setProtectedTitleCache(true);
        subSpeciesName.setProtectedNameCache(false);
        subSpeciesName.setGenusOrUninomial("Pinus");
        subSpeciesName.setSpecificEpithet("alba");
        Assert.assertEquals("Expected full title cache has error", "Pinus alba subsp. beta", subSpeciesName.getNameCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());
        Assert.assertEquals("Expected full title cache has error", "Abies alba (Basio, A.) M.", subSpeciesName.getTitleCache());

        subSpeciesName.setTitleCache("Pinus beta C.", true);
        Assert.assertEquals("Expected full title cache has error", "Pinus beta C., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setProtectedTitleCache(false);

        Assert.assertEquals("Expected full title cache has error", "Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());

        //protected full title cache set
        subSpeciesName.setFullTitleCache("ABC");
        Assert.assertEquals("Expected full title cache has error", "ABC", subSpeciesName.getFullTitleCache());
        subSpeciesName.setProtectedFullTitleCache(false);
        Assert.assertEquals("Expected full title cache has error", "Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());

        //protected title cache set
        subSpeciesName.setProtectedTitleCache(false);
        Assert.assertEquals("Expected full title cache has error", "Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());

        //protectedNameCache set
        subSpeciesName.setProtectedNameCache(true);
        Assert.assertEquals("Expected full title cache has error", "Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setNameCache("P. alba subsp. beta");
        Assert.assertEquals("Expected full title cache has error", "P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());

        subSpeciesName.setGenusOrUninomial("A.");
        subSpeciesName.setProtectedNameCache(false);
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setNameCache("P. alba subsp. beta");
        Assert.assertEquals("Expected full title cache has error", "P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());

        //protected authorship
        subSpeciesName.setProtectedAuthorshipCache(true);
        Assert.assertEquals("Expected full title cache has error", "P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setAuthorshipCache("Ciard.");
        Assert.assertEquals("Expected full title cache has error", "P. alba subsp. beta Ciard., GenericRef", subSpeciesName.getFullTitleCache());

        author.setNomenclaturalTitle("X.");
        subSpeciesName.setProtectedAuthorshipCache(false);
        Assert.assertEquals("Expected full title cache has error", "P. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());

        //clear
        subSpeciesName.setProtectedNameCache(false);
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());

        //appended phrase
        subSpeciesName.setProtectedNameCache(true);
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setAppendedPhrase("app phrase");
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setProtectedNameCache(false);
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta app phrase (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());
        subSpeciesName.setAppendedPhrase("app2 phrase2");
        subSpeciesName.setProtectedNameCache(true);
        Assert.assertNull("NameCache should be null", subSpeciesName.getNameCache());
        subSpeciesName.setProtectedNameCache(false);
        subSpeciesName.setAppendedPhrase(null);


        //ref + nomRef
        Reference book = ReferenceFactory.newBook();
        book.setTitle("Booktitle");
        Assert.assertNotNull("TitleCache should not be null", subSpeciesName.getTitleCache());
        subSpeciesName.setNomenclaturalReference(book);
        subSpeciesName.setNomenclaturalMicroReference("22");
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) X., Booktitle: 22", subSpeciesName.getFullTitleCache());
        subSpeciesName.setProtectedTitleCache(true);
        Assert.assertNotNull("TitleCache should not be null", subSpeciesName.getTitleCache());

        //year
        ZoologicalName zooName = TaxonNameBase.NewZoologicalInstance(Rank.SPECIES());
        zooName.setGenusOrUninomial("Homo");
        zooName.setSpecificEpithet("sapiens");
        zooName.setBasionymAuthorship(basAuthor);
        zooName.setCombinationAuthorship(author);
        zooName.setNomenclaturalReference(book);
        zooName.setNomenclaturalMicroReference("22");
        Assert.assertEquals("Expected full title cache has error", "Homo sapiens (Basio, A.) X., Booktitle: 22", zooName.getFullTitleCache());

        zooName.setOriginalPublicationYear(1922);
        zooName.setPublicationYear(1948);
        Assert.assertEquals("Expected full title cache has error", "Homo sapiens (Basio, A., 1922) X., 1948, Booktitle: 22", zooName.getFullTitleCache());
        zooName.setOriginalPublicationYear(1923);
        zooName.setProtectedAuthorshipCache(true);
        Assert.assertNull("AuthorshipCache should be null", zooName.getAuthorshipCache());
        zooName.setProtectedAuthorshipCache(false);
        Assert.assertNotNull("AuthorshipCache should not be null", zooName.getAuthorshipCache());
        zooName.setPublicationYear(1949);
        zooName.setProtectedAuthorshipCache(true);
        Assert.assertNull("AuthorshipCache should be null", zooName.getAuthorshipCache());




    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setNameAuthorSeperator(java.lang.String)}.
     */
    @Test
    public void testGetSetNameAuthorSeperator() {
        String authorSeparator = "authorSeparator";
        strategy.setNameAuthorSeperator(authorSeparator);
        assertEquals(authorSeparator, strategy.getNameAuthorSeperator());
        strategy.setNameAuthorSeperator(null);
        assertNull(strategy.getNameAuthorSeperator());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymStart(java.lang.String)}.
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymStart()}.
     */
    @Test
    public void testGetSetBasionymStart() {
        String basStart = "start";
        strategy.setBasionymStart(basStart);
        assertEquals(basStart, strategy.getBasionymStart());
        strategy.setBasionymStart(null);
        assertNull(strategy.getBasionymStart());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymEnd(java.lang.String)}.
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymEnd()}.
    */
    @Test
    public void testGetSetBasionymEnd() {
        String basEnd = "end";
        strategy.setBasionymEnd(basEnd);
        assertEquals(basEnd, strategy.getBasionymEnd());
        strategy.setBasionymEnd(null);
        assertNull(strategy.getBasionymEnd());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setExAuthorSeperator(java.lang.String)}.
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getExAuthorSeperator()}.
     */
    @Test
    public void testGetSetExAuthorSeperator() {
        String exAuthorSeparator = "exAuthorSeparator";
        strategy.setExAuthorSeperator(exAuthorSeparator);
        assertEquals(exAuthorSeparator, strategy.getExAuthorSeperator());
        strategy.setExAuthorSeperator(null);
        assertNull(strategy.getExAuthorSeperator());
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#setBasionymAuthorCombinationAuthorSeperator(java.lang.CharSequence)}.
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getBasionymAuthorCombinationAuthorSeperator()}.
     */
    @Test
    public void testSetBasionymAuthorCombinationAuthorSeperator() {
        String basComSeparator = "basComSeparator";
        strategy.setBasionymAuthorCombinationAuthorSeperator(basComSeparator);
        assertEquals(basComSeparator, strategy.getBasionymAuthorCombinationAuthorSeperator());
        strategy.setBasionymAuthorCombinationAuthorSeperator(null);
        assertNull(strategy.getBasionymAuthorCombinationAuthorSeperator());
    }

    @Test
    public void testGetInfraGenericNames(){
        String author = "Anyauthor";
        NonViralName<?> nonViralName = TaxonNameBase.NewNonViralInstance(Rank.SUBGENUS());
        nonViralName.setGenusOrUninomial("Genus");
        nonViralName.setInfraGenericEpithet("subgenus");
        nonViralName.setAuthorshipCache(author);

        //test ordinary infrageneric
        List<TaggedText> subGenusNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName);
        String subGenusNameCache = TaggedCacheHelper.createString(subGenusNameCacheTagged);
        assertEquals("Subgenus name should be 'Genus subg. subgenus'.", "Genus subg. subgenus", subGenusNameCache);
        String subGenusTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Subgenus name should be 'Genus subg. subgenus Anyauthor'.", "Genus subg. subgenus Anyauthor", subGenusTitle);

        //test species aggregates and species groups
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet(null);
        nonViralName.setAuthorshipCache(null);

        List<TaggedText> aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName);

        String aggrNameCache = TaggedCacheHelper.createString(aggrNameCacheTagged);
        assertEquals("Species aggregate name should be 'Genus myspecies aggr.'.", "Genus myspecies aggr.", aggrNameCache);
        String aggrNameTitle = strategy.getTitleCache(nonViralName);
        Assert.assertTrue("Species aggregate should not include author information.", aggrNameTitle.indexOf(author) == -1);
        assertEquals("Species aggregate name should be 'Genus myspecies aggr.'.", "Genus myspecies aggr.", aggrNameTitle);
        nonViralName.setRank(Rank.SPECIESGROUP());
        String groupNameTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Species group name should be 'Genus myspecies species group'.", "Genus myspecies species group", groupNameTitle);

        //test species aggregates and species groups with infrageneric information
        //TODO check if groups do ever have infrageneric epithets
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet("Infragenus");


        aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName);
        aggrNameCache = TaggedCacheHelper.createString(aggrNameCacheTagged);
        assertEquals("Species aggregate name should be 'Genus (Infragenus) myspecies aggr.'.", "Genus (Infragenus) myspecies aggr.", aggrNameCache);

        aggrNameTitle = strategy.getTitleCache(nonViralName);
        Assert.assertTrue("Species aggregate should not include author information.", aggrNameTitle.indexOf(author) == -1);
        assertEquals("Species aggregate name should be 'Genus (Infragenus) myspecies aggr.'.", "Genus (Infragenus) myspecies aggr.", aggrNameTitle);

        nonViralName.setRank(Rank.SPECIESGROUP());
        groupNameTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Species group name should be 'Genus (Infragenus) myspecies species group'.", "Genus (Infragenus) myspecies species group", groupNameTitle);

        //aggregates with author and nom.ref. information #4288
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet(null);
        nonViralName.setAuthorshipCache("L.");

        aggrNameCacheTagged = strategy.getTaggedTitle(nonViralName);
        aggrNameCache = TaggedCacheHelper.createString(aggrNameCacheTagged);
        assertEquals("Species aggregate name should be 'Genus myspecies L.'.", "Genus myspecies L.", aggrNameCache);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.name.NonViralName)}.
     */
    @Test
    public void testGetTaggedNameSpeciesAggregate() {
        BotanicalName speciesAggregate = TaxonNameBase.NewBotanicalInstance(Rank.SPECIESAGGREGATE());
        speciesAggregate.setGenusOrUninomial("Mygenus");
        speciesAggregate.setSpecificEpithet("myspecies");
        List<TaggedText> taggedName = strategy.getTaggedName(speciesAggregate);
        Assert.assertEquals("1rd tag must be genus epithet", "Mygenus", taggedName.get(0).getText());
        Assert.assertEquals("2rd tag must be species epithet", "myspecies", taggedName.get(1).getText());
        Assert.assertEquals("3rd tag must be aggregate marker", "aggr.",taggedName.get(2).getText());
    }


    @Test
    public void testGetTaggedNameSubspecies(){
        List<TaggedText> taggedName = strategy.getTaggedName(subSpeciesName);
        Assert.assertEquals("First tag should be 'Abies'", "Abies", taggedName.get(0).getText());
        Assert.assertEquals("Second tag should be 'alba'", "alba", taggedName.get(1).getText());
        Assert.assertEquals("Third tag should be subspecies rank, and rank abbreviatioin should be subsp.", "subsp.", taggedName.get(2).getText());
        Assert.assertEquals("Fourth tag should be 'beta'", "beta", taggedName.get(3).getText());
        //to be continued

    }

    @Test
    public void testTitleCacheHtmlTagged(){
    	HTMLTagRules rules = new HTMLTagRules().addRule(TagEnum.name, "i");
    	Assert.assertEquals("<i>Abies alba</i>", strategy.getTitleCache(speciesName, rules));
    	rules.addRule(TagEnum.name, "b");
    	Assert.assertEquals("<b><i>Abies alba</i></b>", strategy.getTitleCache(speciesName, rules));
    	speciesName.setCombinationAuthorship(author);
    	Assert.assertEquals("<b><i>Abies alba</i></b> L.", strategy.getTitleCache(speciesName, rules));
    	rules.addRule(TagEnum.authors, "i");
    	Assert.assertEquals("<b><i>Abies alba</i></b> <i>L.</i>", strategy.getTitleCache(speciesName, rules));
    	rules = new HTMLTagRules().addRule(TagEnum.name, "i").addRule(TagEnum.name, "b").addRule(TagEnum.authors, "b");
    	Assert.assertEquals("<b><i>Abies alba</i> L.</b>", strategy.getTitleCache(speciesName, rules));

    }

    @Test //#2888
    public void testAutonymWithExAuthor(){
    	BotanicalName name = TaxonNameBase.NewBotanicalInstance(Rank.FORM());
    	name.setGenusOrUninomial("Euphorbia");
    	name.setSpecificEpithet("atropurpurea");
    	name.setInfraSpecificEpithet("atropurpurea");
    	Team combTeam = Team.NewTitledInstance("Combauthor", "Combauthor");
    	name.setCombinationAuthorship(combTeam);
    	Team exCombTeam = Team.NewTitledInstance("Excomb", "Excomb");
    	name.setExCombinationAuthorship(exCombTeam);

    	Assert.assertEquals("", "Euphorbia atropurpurea Excomb ex Combauthor f. atropurpurea", name.getTitleCache());
    }


}
