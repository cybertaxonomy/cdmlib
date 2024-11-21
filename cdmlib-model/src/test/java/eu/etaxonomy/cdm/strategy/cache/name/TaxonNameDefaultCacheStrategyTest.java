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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.strategy.cache.HTMLTagRules;
import eu.etaxonomy.cdm.strategy.cache.TagEnum;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.TaggedTextFormatter;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author a.mueller
 * @since 26.11.2008
 */
public class TaxonNameDefaultCacheStrategyTest extends NameCacheStrategyTestBase {

    @SuppressWarnings("unused")
    private static final Logger logger = LogManager.getLogger();

    private TaxonNameDefaultCacheStrategy strategy;

    private static final String familyNameString = "Familia";
    private static final String genusNameString = "Genus";
    private static final String speciesNameString = "Abies alba";
    private static final String subSpeciesNameString = "Abies alba subsp. beta";

    private static final String authorString = "L.";
    private static final String exAuthorString = "Exaut.";
    private static final String inAuthorString = "Inaut. B.";
    private static final String basAuthorString = "Basio, A.";
    private static final String exBasAuthorString = "ExBas. N.";
    private static final String inBasAuthorString = "Inbas., C.";

    private static final String referenceTitle = "My Reference";

    private IBotanicalName familyName;
    private IBotanicalName genusName;
    private IBotanicalName subGenusName;
    private TaxonName speciesName;
    private TaxonName subSpeciesName;
    private TeamOrPersonBase<?> author;
    private TeamOrPersonBase<?> exAuthor;
    private TeamOrPersonBase<?> inAuthor;
    private TeamOrPersonBase<?> basAuthor;
    private TeamOrPersonBase<?> exBasAuthor;
    private TeamOrPersonBase<?> inBasAuthor;
    private Reference citationRef;

    @Before
    public void setUp() throws Exception {
        strategy = TaxonNameDefaultCacheStrategy.NewInstance();
        familyName = TaxonNameFactory.PARSED_BOTANICAL(familyNameString, Rank.FAMILY());
        genusName = TaxonNameFactory.PARSED_BOTANICAL(genusNameString, Rank.GENUS());

        subGenusName = TaxonNameFactory.NewBotanicalInstance(Rank.SUBGENUS());
        subGenusName.setGenusOrUninomial("Genus");
        subGenusName.setInfraGenericEpithet("InfraGenericPart");

        speciesName = TaxonNameFactory.PARSED_BOTANICAL(speciesNameString);
        subSpeciesName = TaxonNameFactory.PARSED_BOTANICAL(subSpeciesNameString);

        author = Person.NewInstance();
        author.setNomenclaturalTitleCache(authorString, true);
        exAuthor = Person.NewInstance();
        exAuthor.setNomenclaturalTitleCache(inAuthorString, true);
        inAuthor = Person.NewInstance();
        inAuthor.setNomenclaturalTitleCache(inAuthorString, true);
        basAuthor = Person.NewInstance();
        basAuthor.setNomenclaturalTitleCache(basAuthorString, true);
        exBasAuthor = Person.NewInstance();
        exBasAuthor.setNomenclaturalTitleCache(exBasAuthorString, true);
        inBasAuthor = Person.NewInstance();
        inBasAuthor.setNomenclaturalTitleCache(inBasAuthorString, true);

        citationRef = ReferenceFactory.newGeneric();
        citationRef.setTitle(referenceTitle);
    }

//**************************** TESTS **************************************************

    @Test
    public void testGetTitleCache() {
        Assert.assertEquals(speciesNameString, speciesName.getTitleCache());
        //TODO not yet completed
    }

    @Test
    public void testGetFullTitleCache() {
        subSpeciesName.setNomenclaturalReference(citationRef);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle, subSpeciesName.getFullTitleCache());
        subSpeciesName.setNomenclaturalMicroReference("25");
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        //TODO not yet completed
    }

    @Test
    public void testNomStatus() {
        //no status
        subSpeciesName.setNomenclaturalReference(citationRef);
        subSpeciesName.setNomenclaturalMicroReference("25");
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        //1 status
        subSpeciesName.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        subSpeciesName.setFullTitleCache(null, false);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25, nom. illeg.", subSpeciesName.getFullTitleCache());
        //2 status (ordered) - ordering see #10478#note-9
        subSpeciesName.addStatus(NomenclaturalStatusType.PRO_HYBRID(), null, null);
        subSpeciesName.setFullTitleCache(null, false);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25, pro hybr., nom. illeg.", subSpeciesName.getFullTitleCache());
    }

    @Test
    public void testGattungsAutonyme() {
    	IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.SECTION_BOTANY());
		String strTaraxacum = "Traxacum";
		botName.setGenusOrUninomial(strTaraxacum);
		botName.setInfraGenericEpithet(strTaraxacum);
		botName.setAuthorshipCache("Author");
		Assert.assertFalse(botName.getFullTitleCache().contains("bot."));
		//TODO #4288
//		System.out.println(botName.getFullTitleCache());
    }

    @Test
    public void testGetNameCache() {
        assertEquals("Species Name should be Abies alba", speciesNameString, speciesName.getNameCache());
        speciesName.setNameCache("Any species");
        assertEquals("Species Name should be Any species", "Any species", speciesName.getNameCache());
        assertEquals("Species Name should be Any species", "Any species", speciesName.getTitleCache());
        assertEquals("subSpeciesNameString should be correct", subSpeciesNameString, subSpeciesName.getNameCache());
        IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("", "Lepidocaryum tenue var. tenue", botName.getNameCache());
        IBotanicalName specName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        specName.setGenusOrUninomial("Genus");
        specName.setSpecificEpithet("");
        assertEquals("Empty species string must not result in trailing whitespace", "Genus", specName.getNameCache());

        //unranked taxa
        String unrankedCache;
        TaxonName unrankedName = TaxonNameFactory.NewBotanicalInstance(Rank.INFRASPECIFICTAXON());
        unrankedName.setGenusOrUninomial("Genus");
        TaxonNameDefaultCacheStrategy strategy = TaxonNameDefaultCacheStrategy.NewInstance();
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
        botName = TaxonNameFactory.NewBotanicalInstance(Rank.SECTION_BOTANY());
        botName.setGenusOrUninomial("Genus");
        botName.setInfraGenericEpithet("Infragenus");
        Assert.assertEquals("", "Genus sect. Infragenus", botName.getNameCache());
        botName.setRank(Rank.SUBSECTION_BOTANY());
        botName.setNameCache(null, false);
        Assert.assertEquals("", "Genus subsect. Infragenus", botName.getNameCache());

        //zool. specific ranks (we don't have markers here therefore no problem should exist
        IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.SECTION_ZOOLOGY());
        zooName.setGenusOrUninomial("Genus");
        zooName.setInfraGenericEpithet("Infragenus");
        Assert.assertEquals("", "Genus", zooName.getNameCache());
        zooName.setRank(Rank.SUBSECTION_ZOOLOGY());
        Assert.assertEquals("", "Genus", zooName.getNameCache());
    }

    @Test
    public void testNameCacheWithInfraGenericEpithet() {
        speciesName.setInfraGenericEpithet("Infraabies");
        assertEquals("Species Name should be Abies (Infraabies) alba", "Abies (Infraabies) alba", speciesName.getNameCache());

        IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setInfraGenericEpithet("Infralepi");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("Name cache should be Lepidocaryum (Infralepi) tenue var. tenue", "Lepidocaryum (Infralepi) tenue var. tenue", botName.getNameCache());

        botName.setInfraGenericEpithet(" ");
        botName.setNameCache(null, false);
        assertEquals("Empty infrageneric epithet must be neglegted", "Lepidocaryum tenue var. tenue", botName.getNameCache());
    }

    @Test
    public void testGetAuthorshipCache() {
        this.speciesName.setCombinationAuthorship(author);
        assertEquals(author.getNomenclaturalTitleCache(), speciesName.getAuthorshipCache());
        this.speciesName.setBasionymAuthorship(basAuthor);
        String expected = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitleCache()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+author.getNomenclaturalTitleCache();
        assertEquals(expected, speciesName.getAuthorshipCache());
        String authorshipcache = "authorshipcache";
        speciesName.setAuthorshipCache(authorshipcache);
        assertEquals(authorshipcache, speciesName.getAuthorshipCache());
        speciesName.setCombinationAuthorship(exAuthor);
        assertEquals(authorshipcache, speciesName.getAuthorshipCache()); //cache is protected
        assertEquals(speciesNameString + " " + authorshipcache, speciesName.getFullTitleCache());
        //unprotected
        speciesName.setProtectedAuthorshipCache(false);
        String atomizedAuthorCache = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitleCache()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitleCache();
        assertEquals(atomizedAuthorCache, speciesName.getAuthorshipCache());
        String atomizedTitleCache = speciesNameString + " "+ strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitleCache()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitleCache();
        //Note: This test may fail if aspectj doesn't work correctly
        assertEquals(atomizedTitleCache, speciesName.getTitleCache());
        speciesName.setFullTitleCache(null, false);
        assertEquals(atomizedTitleCache, speciesName.getFullTitleCache());
    }

    @Test
    public void testHybridNames() {
        //Note \u00D7 : hybrid sign (multiplication sign)
        this.speciesName.setCombinationAuthorship(author);
        Assert.assertEquals(author.getNomenclaturalTitleCache(), speciesName.getAuthorshipCache());
        Assert.assertEquals("Should be Abies alba L.", "Abies alba L.", speciesName.getTitleCache());

        speciesName.setBinomHybrid(true);
        speciesName.setTitleCache(null, false);
        Assert.assertEquals("Should be Abies \u00D7\u202Falba L.", "Abies \u00D7\u202Falba L.", speciesName.getTitleCache());
        speciesName.setMonomHybrid(true);
        speciesName.setTitleCache(null, false);
        Assert.assertEquals("Should be '\u00D7\u202FAbies \u00D7\u202Falba L.'", "\u00D7\u202FAbies \u00D7\u202Falba L.", speciesName.getTitleCache());

        Assert.assertEquals("Should be 'Genus'", "Genus", genusName.getTitleCache());
        genusName.setMonomHybrid(true);
        genusName.setTitleCache(null, false);
        Assert.assertEquals("Should be '\u00D7\u202FGenus'", "\u00D7\u202FGenus", genusName.getTitleCache());

        Assert.assertEquals("Should be 'Abies alba subsp. beta'", subSpeciesNameString, subSpeciesName.getTitleCache());
        subSpeciesName.setTrinomHybrid(true);
        subSpeciesName.setTitleCache(null, false);
        Assert.assertEquals("Should be 'Abies alba nothosubsp. beta or nbeta'", "Abies alba nothosubsp. beta", subSpeciesName.getTitleCache());
        subSpeciesName.setMonomHybrid(true);
        subSpeciesName.setTitleCache(null, false);
        Assert.assertEquals("Should be '\u00D7\u202FAbies alba nothosubsp. beta'", "\u00D7\u202FAbies alba nothosubsp. beta", subSpeciesName.getTitleCache());

        Assert.assertEquals("Should be 'Genus subg. InfraGenericPart'", "Genus subg. InfraGenericPart", subGenusName.getTitleCache());
        subGenusName.setBinomHybrid(true);
        subGenusName.setTitleCache(null, false);
        Assert.assertEquals("Should be 'Genus nothosubg. InfraGenericPart'", "Genus nothosubg. InfraGenericPart", subGenusName.getTitleCache());
    }

    @Test
    public void testHybridFormula(){
        this.speciesName.setCombinationAuthorship(author);
        Assert.assertEquals(author.getNomenclaturalTitleCache(), speciesName.getAuthorshipCache());
        Assert.assertEquals("Should be 'Abies alba L.'", "Abies alba L.", speciesName.getTitleCache());

        INonViralName hybridName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        INonViralName secondParent = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());

        secondParent.setTitleCache("Second parent Mill.", true);
        hybridName.addHybridParent(speciesName, HybridRelationshipType.FIRST_PARENT(), null);
        hybridName.addHybridParent(secondParent, HybridRelationshipType.SECOND_PARENT(), null);
        hybridName.setHybridFormula(true);

        Assert.assertEquals("", "Abies alba L. \u00D7 Second parent Mill.", hybridName.getTitleCache());
        //Note: handling of empty nameCache of parents may change in future
        Assert.assertEquals("", "Abies alba \u00D7", hybridName.getNameCache());
        secondParent.setGenusOrUninomial("Second");
        secondParent.setSpecificEpithet("parent");
        hybridName.setNameCache(null, false);
        Assert.assertEquals("", "Abies alba \u00D7 Second parent", hybridName.getNameCache());
    }

    //#9778
    @Test
    public void testOldRanks(){

        //grex
        subSpeciesName.setRank(Rank.GREX_INFRASPEC());
        Assert.assertEquals("Abies alba grex beta", strategy.getTitleCache(subSpeciesName));
        //subgrex
        subSpeciesName.setRank(Rank.SUBGREX());
        Assert.assertEquals("Abies alba subgrex beta", strategy.getTitleCache(subSpeciesName));
        //proles
        subSpeciesName.setRank(Rank.PROLES());
        Assert.assertEquals("Abies alba proles beta", strategy.getTitleCache(subSpeciesName));
        //proles
        subSpeciesName.setRank(Rank.SUBPROLES());
        Assert.assertEquals("Abies alba subproles beta", strategy.getTitleCache(subSpeciesName));
        //lusus
        subSpeciesName.setRank(Rank.LUSUS());
        Assert.assertEquals("Abies alba lusus beta", strategy.getTitleCache(subSpeciesName));
        //sublusus
        subSpeciesName.setRank(Rank.SUBLUSUS());
        Assert.assertEquals("Abies alba sublusus beta", strategy.getTitleCache(subSpeciesName));
    }

    @Test
    public void testWithoutRank() {
        subSpeciesName.setRank(null);
        Assert.assertEquals("Abies alba beta", strategy.getTitleCache(subSpeciesName));

        subSpeciesName.setTrinomHybrid(true);
        //Not sure if selfstanding notho is correct here, for now only to indicate that there is the hybridflag set
        //TODO still misses for binom, and monom Hybridflag.
        Assert.assertEquals("Abies alba notho beta", strategy.getTitleCache(subSpeciesName));
    }

    //#9754
    @Test
    public void testCultivar(){

        //cultivar
        speciesName.setRank(Rank.CULTIVAR());
        speciesName.setCultivarEpithet("Cultus");
        Assert.assertEquals("Abies alba 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setBinomHybrid(true);
        Assert.assertEquals("Abies \u00D7\u202Falba 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setBinomHybrid(false);
        speciesName.setSpecificEpithet(null);
        Assert.assertEquals("Abies 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setCombinationAuthorship(author);
        Assert.assertEquals("Abies 'Cultus' L.", strategy.getTitleCache(speciesName));
        speciesName.setBasionymAuthorship(basAuthor);
        speciesName.setExCombinationAuthorship(exAuthor);
        speciesName.setExBasionymAuthorship(exBasAuthor);
        Assert.assertEquals("Basionym and ex-authors should not be considered for cultivar names"
                , "Abies 'Cultus' L.", strategy.getTitleCache(speciesName));
        speciesName.setNomenclaturalReference(citationRef);
        Assert.assertEquals("Abies 'Cultus' L., My Reference", strategy.getFullTitleCache(speciesName));
        speciesName.setCombinationAuthorship(null);
        speciesName.setBasionymAuthorship(null);
        speciesName.setExCombinationAuthorship(null);
        speciesName.setExBasionymAuthorship(null);
        speciesName.setNomenclaturalReference(null);

        speciesName.setCultivarEpithet(null);
        Assert.assertEquals("Correct formatting for incorrect name needs to be discussed", "Abies ''", strategy.getTitleCache(speciesName));

        //cultivar group
        speciesName.setRank(Rank.CULTIVARGROUP());
        Assert.assertEquals("Abies Group", strategy.getTitleCache(speciesName)); //not sure if this is correct for an empty group field
        speciesName.setCultivarGroupEpithet("Cultus Group");
        Assert.assertEquals("Abies Cultus Group", strategy.getTitleCache(speciesName));

        speciesName.setCultivarGroupEpithet("Cultus Gruppe");
        Assert.assertEquals("Abies Cultus Gruppe", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Cultus Gp");
        Assert.assertEquals("Abies Cultus Gp", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Gruppo Cultus");
        Assert.assertEquals("Abies Gruppo Cultus", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Druppo Cultus");
        Assert.assertEquals("Abies Druppo Cultus Group", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet(null);
        Assert.assertEquals("Correct formatting for missing epithet needs to be discussed", "Abies Group", strategy.getTitleCache(speciesName));

        //grex
        speciesName.setRank(Rank.GREX_ICNCP());
        speciesName.setCultivarGroupEpithet("Lovely");
        Assert.assertEquals("Abies Lovely grex", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet(null);
        Assert.assertEquals("Correct formatting for missing epithet needs to be discussed", "Abies grex", strategy.getTitleCache(speciesName));

        //subspecies name
        subSpeciesName.setRank(Rank.CULTIVAR());
        subSpeciesName.setCultivarEpithet("Cultus");
        Assert.assertEquals("Infraspecific epithet in cultivars can not be handled correctly yet", "Abies alba beta 'Cultus'", strategy.getTitleCache(subSpeciesName));
        subSpeciesName.setInfraSpecificEpithet("var. beta");
        Assert.assertEquals("Abies alba var. beta 'Cultus'", strategy.getTitleCache(subSpeciesName));


        //graft chimaera
        //https://en.wikipedia.org/wiki/Graft-chimaera
        //either formula (like hybrids) concatenated by ' + ' (Art. 24.2)
        speciesName.setRank(Rank.GRAFTCHIMAERA());
        speciesName.setGenusOrUninomial("Laburnocytisus");
        speciesName.setCultivarEpithet("Adamii");
        Assert.assertEquals("+ Laburnocytisus 'Adamii'", strategy.getTitleCache(speciesName));
        //tbc

        //denomination class (only dummy implementation, may change in future)
        speciesName.setRank(Rank.DENOMINATIONCLASS());
        speciesName.setGenusOrUninomial("Laburnocytisus");
        speciesName.setCultivarEpithet("Adamii");
        Assert.assertEquals("Laburnocytisus 'Adamii'", strategy.getTitleCache(speciesName));

        //appended phrase
        speciesName.setRank(Rank.CULTIVAR());
        speciesName.setGenusOrUninomial("Abies");
        speciesName.setSpecificEpithet("alba");
        speciesName.setCultivarEpithet("Cultus");
        speciesName.setAppendedPhrase("appended");
        Assert.assertEquals("Abies alba 'Cultus' appended", strategy.getTitleCache(speciesName));
    }

    //10299
    @Test
    public void testVerbatimDate() {

        subSpeciesName.setNomenclaturalReference(citationRef);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle, subSpeciesName.getFullTitleCache());
        subSpeciesName.setNomenclaturalMicroReference("25");
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        VerbatimTimePeriod datePublished = TimePeriodParser.parseStringVerbatim("1988");
        datePublished.setVerbatimDate("1989");
        citationRef.setDatePublished(datePublished);
        subSpeciesName.setFullTitleCache(null, false);
        Assert.assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25. 1988 [\"1989\"]", subSpeciesName.getFullTitleCache());
    }

    //3665
    @Test
    public void testOriginalSpelling() {

        TaxonName originalName = speciesName.clone();
    	originalName.setSpecificEpithet("alpa");
    	Assert.assertEquals("Preconditions are wrong", "Abies alpa", originalName.getTitleCache());
        Assert.assertEquals("Name cache should not show original spelling", "Abies alpa", originalName.getNameCache());

    	speciesName.setOriginalSpelling(originalName);
    	Assert.assertEquals("Abies alba [as \"alpa\"]", speciesName.getFullTitleCache());
        Assert.assertEquals("Abies alba", speciesName.getTitleCache());
        Assert.assertEquals("Name cache should not show original spelling", "Abies alba", speciesName.getNameCache());

    	originalName.setGenusOrUninomial("Apies");
    	speciesName.setFullTitleCache(null, false);
    	originalName.setNameCache(null, false);
    	//TODO update cache of current name (species name)
    	Assert.assertEquals("Abies alba [as \"Apies alpa\"]", speciesName.getFullTitleCache());
        Assert.assertEquals("Abies alba", speciesName.getTitleCache());
        Assert.assertEquals("Name cache should not show original spelling", "Abies alba", speciesName.getNameCache());
        originalName.setSpecificEpithet("alba");
        originalName.setNameCache(null, false);
        speciesName.setFullTitleCache(null, false);
        //not fully sure if it is wanted that here only the genus name is given and not the specific epithet too, may change if required by users
        Assert.assertEquals("Abies alba [as \"Apies\"]", speciesName.getFullTitleCache());

        //subspecies
        originalName = subSpeciesName.clone();
        originalName.setInfraSpecificEpithet("peta");
        Assert.assertEquals("Preconditions are wrong", "Abies alba subsp. peta", originalName.getTitleCache());
        subSpeciesName.setOriginalSpelling(originalName);
        Assert.assertEquals("Abies alba subsp. beta [as \"peta\"]", subSpeciesName.getFullTitleCache());
        Assert.assertEquals("Abies alba subsp. beta", subSpeciesName.getTitleCache());
        originalName.setSpecificEpithet("alpa");
        originalName.setNameCache(null, false);
        subSpeciesName.setFullTitleCache(null, false);
        Assert.assertEquals("Abies alba subsp. beta [as \"alpa subsp. peta\"]", subSpeciesName.getFullTitleCache());

        originalName.setInfraSpecificEpithet("beta");
        originalName.setNameCache(null, false);
        subSpeciesName.setFullTitleCache(null, false);
        //not fully sure if it is wanted that here only the specific epithet is given and not the infra specific epithet too, may change if required by users
        Assert.assertEquals("Abies alba subsp. beta [as \"alpa\"]", subSpeciesName.getFullTitleCache());

    	INonViralName correctName = NonViralNameParserImpl.NewInstance().parseFullName("Nepenthes glabrata J.R.Turnbull & A.T.Middleton");
    	TaxonName originalSpelling = (TaxonName)NonViralNameParserImpl.NewInstance().parseFullName("Nepenthes glabratus");
    	correctName.setOriginalSpelling(originalSpelling);
    	Assert.assertEquals("Nepenthes glabrata", correctName.getNameCache());
    	Assert.assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton", correctName.getTitleCache());
    	Assert.assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton [as \"glabratus\"]", correctName.getFullTitleCache());

    	correctName.setNomenclaturalReference(citationRef);
        Assert.assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, My Reference [as \"glabratus\"]", correctName.getFullTitleCache());
        citationRef.setProtectedTitleCache(false);
        citationRef.setTitle("Sp. Pl.");
        citationRef.setDatePublished(TimePeriodParser.parseStringVerbatim("1988"));
        correctName.setFullTitleCache(null, false);
        Assert.assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, Sp. Pl. 1988 [as \"glabratus\"]", correctName.getFullTitleCache());
        correctName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
        correctName.setFullTitleCache(null, false);
        Assert.assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, Sp. Pl. 1988 [as \"glabratus\"], nom. illeg.", correctName.getFullTitleCache());
    }

    //3667
    @Test
    public void testOriginalSpellingItalics() {

        TaxonName originalName = subSpeciesName.clone();
        originalName.setSpecificEpithet("alpa");
        Assert.assertEquals("Preconditions are wrong", "Abies alpa subsp. beta", originalName.getTitleCache());

        subSpeciesName.setOriginalSpelling(originalName);

        List<TaggedText> taggedFullTitle = subSpeciesName.cacheStrategy().getTaggedFullTitle(subSpeciesName);
        Assert.assertEquals(7, taggedFullTitle.size());
        Assert.assertEquals(new TaggedText(TagEnum.name, "Abies"), taggedFullTitle.get(0));
        Assert.assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        Assert.assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(2));
        Assert.assertEquals(new TaggedText(TagEnum.name, "beta"), taggedFullTitle.get(3));
        Assert.assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, " [as \""), taggedFullTitle.get(4));
        Assert.assertEquals(new TaggedText(TagEnum.name, "alpa"), taggedFullTitle.get(5));
        Assert.assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, "\"]"), taggedFullTitle.get(6));

        originalName.setInfraSpecificEpithet("peta");
        originalName.setNameCache(null, false);
        taggedFullTitle = subSpeciesName.cacheStrategy().getTaggedFullTitle(subSpeciesName);
        Assert.assertEquals(9, taggedFullTitle.size());
        Assert.assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        Assert.assertEquals(new TaggedText(TagEnum.name, "Abies"), taggedFullTitle.get(0));
        Assert.assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        Assert.assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(2));
        Assert.assertEquals(new TaggedText(TagEnum.name, "beta"), taggedFullTitle.get(3));
        Assert.assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, " [as \""), taggedFullTitle.get(4));
        Assert.assertEquals(new TaggedText(TagEnum.name, "alpa"), taggedFullTitle.get(5));
        Assert.assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(6));
        Assert.assertEquals(new TaggedText(TagEnum.name, "peta"), taggedFullTitle.get(7));
        Assert.assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, "\"]"), taggedFullTitle.get(8));
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
        author.setNomenclaturalTitleCache("M.", true);
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

        author.setNomenclaturalTitleCache("X.", true);
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
        subSpeciesName.setAppendedPhrase("[\"app phrase\"]");
        Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta [\"app phrase\"] (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache());

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
        IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
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

    @Test
    public void testGetSetNameAuthorSeperator() {
        String authorSeparator = "authorSeparator";
        strategy.setNameAuthorSeperator(authorSeparator);
        assertEquals(authorSeparator, strategy.getNameAuthorSeperator());
        strategy.setNameAuthorSeperator(null);
        assertNull(strategy.getNameAuthorSeperator());
    }

    @Test
    public void testGetSetBasionymStart() {
        String basStart = "start";
        strategy.setBasionymStart(basStart);
        assertEquals(basStart, strategy.getBasionymStart());
        strategy.setBasionymStart(null);
        assertNull(strategy.getBasionymStart());
    }

    @Test
    public void testGetSetBasionymEnd() {
        String basEnd = "end";
        strategy.setBasionymEnd(basEnd);
        assertEquals(basEnd, strategy.getBasionymEnd());
        strategy.setBasionymEnd(null);
        assertNull(strategy.getBasionymEnd());
    }

    @Test
    public void testGetSetExAuthorSeperator() {
        String exAuthorSeparator = "exAuthorSeparator";
        strategy.setExAuthorSeperator(exAuthorSeparator);
        assertEquals(exAuthorSeparator, strategy.getExAuthorSeperator());
        strategy.setExAuthorSeperator(null);
        assertNull(strategy.getExAuthorSeperator());
    }

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
        TaxonName nonViralName = TaxonNameFactory.NewNonViralInstance(Rank.SUBGENUS());
        nonViralName.setGenusOrUninomial("Genus");
        nonViralName.setInfraGenericEpithet("subgenus");
        nonViralName.setAuthorshipCache(author);

        //test ordinary infrageneric
        List<TaggedText> subGenusNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName, false);
        String subGenusNameCache = TaggedTextFormatter.createString(subGenusNameCacheTagged);
        assertEquals("Subgenus name should be 'Genus subg. subgenus'.", "Genus subg. subgenus", subGenusNameCache);
        String subGenusTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Subgenus name should be 'Genus subg. subgenus Anyauthor'.", "Genus subg. subgenus Anyauthor", subGenusTitle);

        //test species aggregates and species groups
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet(null);
        nonViralName.setAuthorshipCache(null);

        List<TaggedText> aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName, false);

        String aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
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

        aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName, false);
        aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
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
        aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
        assertEquals("Species aggregate name should be 'Genus myspecies L.'.", "Genus myspecies L.", aggrNameCache);

    }

    @Test
    public void testGetTaggedNameSpeciesAggregate() {
        TaxonName speciesAggregate = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIESAGGREGATE());
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
    	IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.FORM());
    	name.setGenusOrUninomial("Euphorbia");
    	name.setSpecificEpithet("atropurpurea");
    	name.setInfraSpecificEpithet("atropurpurea");
    	Team combTeam = Team.NewTitledInstance("Combauthor", "Combauthor");
    	name.setCombinationAuthorship(combTeam);
    	Team exCombTeam = Team.NewTitledInstance("Excomb", "Excomb");
    	name.setExCombinationAuthorship(exCombTeam);

    	Assert.assertEquals("", "Euphorbia atropurpurea Excomb ex Combauthor f. atropurpurea", name.getTitleCache());
    }

    @Test //#6656
    public void testAutonymHybrids(){
        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
        name.setGenusOrUninomial("Ophrys");
        name.setSpecificEpithet("kastelli");
        name.setInfraSpecificEpithet("kastelli");
        Team combTeam = Team.NewTitledInstance("E. Klein", "E. Klein");
        name.setCombinationAuthorship(combTeam);
        name.setBinomHybrid(true);
        name.setTrinomHybrid(true);

        String expected = String.format("Ophrys %skastelli E. Klein nothosubsp. kastelli", UTF8.HYBRID_SPACE.toString());
        Assert.assertEquals("", expected, name.getTitleCache());
    }

    @Test
    public void testEtAlAuthors() {
        TaxonName name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setGenusOrUninomial("Ophrys");
        name.setSpecificEpithet("kastelli");
        Team combTeam = Team.NewInstance();
        combTeam.addTeamMember(Person.NewInstance("Mill.", "Miller", "A.", null));
        combTeam.addTeamMember(Person.NewInstance("Ball.", "Baller", "B.", null));
        combTeam.addTeamMember(Person.NewInstance("Cill.", "Ciller", "C.", null));
        name.setCombinationAuthorship(combTeam);

        INameCacheStrategy formatter = name.cacheStrategy();
        Assert.assertEquals("", "Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name));
        formatter.setEtAlPosition(3);
        Assert.assertEquals("", "Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name));
        formatter.setEtAlPosition(2);
        Assert.assertEquals("", "Ophrys kastelli Mill. & al.", formatter.getTitleCache(name));
        //null and <2 are handled as "no position defined"
        formatter.setEtAlPosition(1);
        Assert.assertEquals("", "Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name));
        formatter.setEtAlPosition(null);
        Assert.assertEquals("", "Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name));

        name.setBasionymAuthorship(combTeam);
        formatter.setEtAlPosition(2);
        Assert.assertEquals("", "Ophrys kastelli (Mill. & al.) Mill. & al.", formatter.getTitleCache(name));

    }

    @Test  //#7443
    public void testInAuthors() {
        //base configuration
        speciesName.setCombinationAuthorship(author);
        speciesName.setBasionymAuthorship(basAuthor);
        String expectedWithoutInAuthor = speciesNameString + " (" + basAuthorString + ") " + authorString;
        Assert.assertEquals(expectedWithoutInAuthor, speciesName.getTitleCache());

        //with in-authors
        speciesName.setNameType(NomenclaturalCode.Fungi);
        speciesName.setInBasionymAuthorship(inBasAuthor);
        speciesName.setInCombinationAuthorship(inAuthor);
        String expectedWithInAuthor = speciesNameString + " (" + basAuthorString + " in "+inBasAuthorString+") " + authorString + " in "+inAuthorString;
        speciesName.setTitleCache(null, false);
        Assert.assertEquals(expectedWithInAuthor, speciesName.getTitleCache());

        //... for zoo-names
        speciesName.setNameType(NomenclaturalCode.ICZN);
        speciesName.setTitleCache(null, false);
        Assert.assertEquals(expectedWithInAuthor, speciesName.getTitleCache());

        //... for botanical names
        speciesName.setNameType(NomenclaturalCode.ICNAFP);
        speciesName.setTitleCache(null, false);
        Assert.assertEquals("For now we do not allow in-authors for botanical names (except for fungi)",
                expectedWithoutInAuthor, speciesName.getTitleCache());
    }
}