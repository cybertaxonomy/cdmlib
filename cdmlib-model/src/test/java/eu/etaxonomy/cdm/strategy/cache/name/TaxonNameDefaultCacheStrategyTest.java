/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
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
        assertEquals(speciesNameString, speciesName.getTitleCache());
        //TODO not yet completed
    }

    @Test
    public void testGetFullTitleCache() {
        subSpeciesName.setNomenclaturalReference(citationRef);
        assertEquals(subSpeciesNameString + ", " +  referenceTitle, subSpeciesName.getFullTitleCache());
        subSpeciesName.setNomenclaturalMicroReference("25");
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        //TODO not yet completed
    }

    @Test
    public void testNomStatus() {
        //no status
        subSpeciesName.setNomenclaturalReference(citationRef);
        subSpeciesName.setNomenclaturalMicroReference("25");
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        //1 status
        subSpeciesName.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        subSpeciesName.setFullTitleCache(null, false);
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25, nom. illeg.", subSpeciesName.getFullTitleCache());
        //2 status (ordered) - ordering see #10478#note-9
        subSpeciesName.addStatus(NomenclaturalStatusType.PRO_HYBRID(), null, null);
        subSpeciesName.setFullTitleCache(null, false);
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25, pro hybr., nom. illeg.", subSpeciesName.getFullTitleCache());
    }

    @Test
    public void testGattungsAutonyme() {
    	IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.SECTION_BOTANY());
		String strTaraxacum = "Traxacum";
		botName.setGenusOrUninomial(strTaraxacum);
		botName.setInfraGenericEpithet(strTaraxacum);
		botName.setAuthorshipCache("Author");
		assertFalse(botName.getFullTitleCache().contains("bot."));
		//TODO #4288
//		System.out.println(botName.getFullTitleCache());
    }

    @Test
    public void testGetNameCache() {
        assertEquals(speciesNameString, speciesName.getNameCache(), "Species Name should be Abies alba");
        speciesName.setNameCache("Any species");
        assertEquals("Any species", speciesName.getNameCache(), "Species Name should be Any species");
        assertEquals("Any species", speciesName.getTitleCache(), "Species Name should be Any species");
        assertEquals(subSpeciesNameString, subSpeciesName.getNameCache(), "subSpeciesNameString should be correct");
        IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("Lepidocaryum tenue var. tenue", botName.getNameCache(), "");
        IBotanicalName specName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        specName.setGenusOrUninomial("Genus");
        specName.setSpecificEpithet("");
        assertEquals("Genus", specName.getNameCache(), "Empty species string must not result in trailing whitespace");

        //unranked taxa
        String unrankedCache;
        TaxonName unrankedName = TaxonNameFactory.NewBotanicalInstance(Rank.INFRASPECIFICTAXON());
        unrankedName.setGenusOrUninomial("Genus");
        TaxonNameDefaultCacheStrategy strategy = TaxonNameDefaultCacheStrategy.NewInstance();
            //infraspecific
        unrankedName.setInfraSpecificEpithet("infraspecific");
        unrankedName.setSpecificEpithet("species");
        unrankedCache = strategy.getNameCache(unrankedName);

        assertEquals("Genus species [infraspec.] infraspecific", unrankedCache, "Correct unranked cache expected");

            //infrageneric
        unrankedName.setRank(Rank.INFRAGENERICTAXON());
        unrankedName.setInfraSpecificEpithet(null);
        unrankedName.setSpecificEpithet(null);
        unrankedName.setInfraGenericEpithet("Infrageneric");
        unrankedCache = strategy.getNameCache(unrankedName);
        assertEquals("Genus [unranked] Infrageneric", unrankedCache, "Correct unranked cache expected");

        //bot. specific ranks
        botName = TaxonNameFactory.NewBotanicalInstance(Rank.SECTION_BOTANY());
        botName.setGenusOrUninomial("Genus");
        botName.setInfraGenericEpithet("Infragenus");
        assertEquals("Genus sect. Infragenus", botName.getNameCache(), "");
        botName.setRank(Rank.SUBSECTION_BOTANY());
        botName.setNameCache(null, false);
        assertEquals("Genus subsect. Infragenus", botName.getNameCache(), "");

        //zool. specific ranks (we don't have markers here therefore no problem should exist
        IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.SECTION_ZOOLOGY());
        zooName.setGenusOrUninomial("Genus");
        zooName.setInfraGenericEpithet("Infragenus");
        assertEquals("Genus", zooName.getNameCache(), "");
        zooName.setRank(Rank.SUBSECTION_ZOOLOGY());
        assertEquals("Genus", zooName.getNameCache(), "");
    }

    @Test
    public void testNameCacheWithInfraGenericEpithet() {
        speciesName.setInfraGenericEpithet("Infraabies");
        assertEquals("Abies (Infraabies) alba", speciesName.getNameCache(), "Species Name should be Abies (Infraabies) alba");

        IBotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
        botName.setGenusOrUninomial("Lepidocaryum");
        botName.setInfraGenericEpithet("Infralepi");
        botName.setSpecificEpithet("tenue");
        botName.setInfraSpecificEpithet("tenue");
        assertEquals("Lepidocaryum (Infralepi) tenue var. tenue", botName.getNameCache(), "Name cache should be Lepidocaryum (Infralepi) tenue var. tenue");

        botName.setInfraGenericEpithet(" ");
        botName.setNameCache(null, false);
        assertEquals("Lepidocaryum tenue var. tenue", botName.getNameCache(), "Empty infrageneric epithet must be neglegted");
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
        assertEquals(author.getNomenclaturalTitleCache(), speciesName.getAuthorshipCache());
        assertEquals("Abies alba L.", speciesName.getTitleCache(), "Should be Abies alba L.");

        speciesName.setBinomHybrid(true);
        speciesName.setTitleCache(null, false);
        assertEquals("Abies \u00D7alba L.", speciesName.getTitleCache(), "Should be Abies \u00D7alba L.");
        speciesName.setMonomHybrid(true);
        speciesName.setTitleCache(null, false);
        assertEquals("\u00D7Abies \u00D7alba L.", speciesName.getTitleCache(), "Should be '\u00D7Abies \u00D7alba L.'");

        assertEquals("Genus", genusName.getTitleCache(), "Should be 'Genus'");
        genusName.setMonomHybrid(true);
        genusName.setTitleCache(null, false);
        assertEquals("\u00D7Genus", genusName.getTitleCache(), "Should be '\u00D7Genus'");

        assertEquals(subSpeciesNameString, subSpeciesName.getTitleCache(), "Should be 'Abies alba subsp. beta'");
        subSpeciesName.setTrinomHybrid(true);
        subSpeciesName.setTitleCache(null, false);
        assertEquals("Abies alba nothosubsp. beta", subSpeciesName.getTitleCache(), "Should be 'Abies alba nothosubsp. beta or nbeta'");
        subSpeciesName.setMonomHybrid(true);
        subSpeciesName.setTitleCache(null, false);
        assertEquals("\u00D7Abies alba nothosubsp. beta", subSpeciesName.getTitleCache(), "Should be '\u00D7Abies alba nothosubsp. beta'");

        assertEquals("Genus subg. InfraGenericPart", subGenusName.getTitleCache(), "Should be 'Genus subg. InfraGenericPart'");
        subGenusName.setBinomHybrid(true);
        subGenusName.setTitleCache(null, false);
        assertEquals("Genus nothosubg. InfraGenericPart", subGenusName.getTitleCache(), "Should be 'Genus nothosubg. InfraGenericPart'");
    }

    @Test
    public void testHybridFormula(){
        this.speciesName.setCombinationAuthorship(author);
        assertEquals(author.getNomenclaturalTitleCache(), speciesName.getAuthorshipCache());
        assertEquals("Abies alba L.", speciesName.getTitleCache(), "Should be 'Abies alba L.'");

        INonViralName hybridName = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());
        INonViralName secondParent = TaxonNameFactory.NewNonViralInstance(Rank.SPECIES());

        secondParent.setTitleCache("Second parent Mill.", true);
        hybridName.addHybridParent(speciesName, HybridRelationshipType.FIRST_PARENT(), null);
        hybridName.addHybridParent(secondParent, HybridRelationshipType.SECOND_PARENT(), null);
        hybridName.setHybridFormula(true);

        assertEquals("Abies alba L. \u00D7 Second parent Mill.", hybridName.getTitleCache(), "");
        //Note: handling of empty nameCache of parents may change in future
        assertEquals("Abies alba \u00D7", hybridName.getNameCache(), "");
        secondParent.setGenusOrUninomial("Second");
        secondParent.setSpecificEpithet("parent");
        hybridName.setNameCache(null, false);
        assertEquals("Abies alba \u00D7 Second parent", hybridName.getNameCache(), "");
    }

    //#9778
    @Test
    public void testOldRanks(){

        //grex
        subSpeciesName.setRank(Rank.GREX_INFRASPEC());
        assertEquals("Abies alba grex beta", strategy.getTitleCache(subSpeciesName));
        //subgrex
        subSpeciesName.setRank(Rank.SUBGREX());
        assertEquals("Abies alba subgrex beta", strategy.getTitleCache(subSpeciesName));
        //proles
        subSpeciesName.setRank(Rank.PROLES());
        assertEquals("Abies alba proles beta", strategy.getTitleCache(subSpeciesName));
        //proles
        subSpeciesName.setRank(Rank.SUBPROLES());
        assertEquals("Abies alba subproles beta", strategy.getTitleCache(subSpeciesName));
        //lusus
        subSpeciesName.setRank(Rank.LUSUS());
        assertEquals("Abies alba lusus beta", strategy.getTitleCache(subSpeciesName));
        //sublusus
        subSpeciesName.setRank(Rank.SUBLUSUS());
        assertEquals("Abies alba sublusus beta", strategy.getTitleCache(subSpeciesName));
    }

    @Test
    public void testWithoutRank() {
        subSpeciesName.setRank(null);
        assertEquals("Abies alba beta", strategy.getTitleCache(subSpeciesName));

        subSpeciesName.setTrinomHybrid(true);
        //Not sure if selfstanding notho is correct here, for now only to indicate that there is the hybridflag set
        //TODO still misses for binom, and monom Hybridflag.
        assertEquals("Abies alba notho beta", strategy.getTitleCache(subSpeciesName));
    }

    //#9754
    @Test
    public void testCultivar(){

        //cultivar
        speciesName.setRank(Rank.CULTIVAR());
        speciesName.setCultivarEpithet("Cultus");
        assertEquals("Abies alba 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setBinomHybrid(true);
        assertEquals("Abies \u00D7alba 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setBinomHybrid(false);
        speciesName.setSpecificEpithet(null);
        assertEquals("Abies 'Cultus'", strategy.getTitleCache(speciesName));

        speciesName.setCombinationAuthorship(author);
        assertEquals("Abies 'Cultus' L.", strategy.getTitleCache(speciesName));
        speciesName.setBasionymAuthorship(basAuthor);
        speciesName.setExCombinationAuthorship(exAuthor);
        speciesName.setExBasionymAuthorship(exBasAuthor);
        assertEquals("Abies 'Cultus' L.", strategy.getTitleCache(speciesName), "Basionym and ex-authors should not be considered for cultivar names");
        speciesName.setNomenclaturalReference(citationRef);
        assertEquals("Abies 'Cultus' L., My Reference", strategy.getFullTitleCache(speciesName));
        speciesName.setCombinationAuthorship(null);
        speciesName.setBasionymAuthorship(null);
        speciesName.setExCombinationAuthorship(null);
        speciesName.setExBasionymAuthorship(null);
        speciesName.setNomenclaturalReference(null);

        speciesName.setCultivarEpithet(null);
        assertEquals("Abies ''", strategy.getTitleCache(speciesName), "Correct formatting for incorrect name needs to be discussed");

        //cultivar group
        speciesName.setRank(Rank.CULTIVARGROUP());
        assertEquals("Abies Group", strategy.getTitleCache(speciesName)); //not sure if this is correct for an empty group field
        speciesName.setCultivarGroupEpithet("Cultus Group");
        assertEquals("Abies Cultus Group", strategy.getTitleCache(speciesName));

        speciesName.setCultivarGroupEpithet("Cultus Gruppe");
        assertEquals("Abies Cultus Gruppe", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Cultus Gp");
        assertEquals("Abies Cultus Gp", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Gruppo Cultus");
        assertEquals("Abies Gruppo Cultus", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet("Druppo Cultus");
        assertEquals("Abies Druppo Cultus Group", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet(null);
        assertEquals("Abies Group", strategy.getTitleCache(speciesName), "Correct formatting for missing epithet needs to be discussed");

        //grex
        speciesName.setRank(Rank.GREX_ICNCP());
        speciesName.setCultivarGroupEpithet("Lovely");
        assertEquals("Abies Lovely grex", strategy.getTitleCache(speciesName));
        speciesName.setCultivarGroupEpithet(null);
        assertEquals("Abies grex", strategy.getTitleCache(speciesName), "Correct formatting for missing epithet needs to be discussed");

        //subspecies name
        subSpeciesName.setRank(Rank.CULTIVAR());
        subSpeciesName.setCultivarEpithet("Cultus");
        assertEquals("Abies alba beta 'Cultus'", strategy.getTitleCache(subSpeciesName), "Infraspecific epithet in cultivars can not be handled correctly yet");
        subSpeciesName.setInfraSpecificEpithet("var. beta");
        assertEquals("Abies alba var. beta 'Cultus'", strategy.getTitleCache(subSpeciesName));


        //graft chimaera
        //https://en.wikipedia.org/wiki/Graft-chimaera
        //either formula (like hybrids) concatenated by ' + ' (Art. 24.2)
        speciesName.setRank(Rank.GRAFTCHIMAERA());
        speciesName.setGenusOrUninomial("Laburnocytisus");
        speciesName.setCultivarEpithet("Adamii");
        assertEquals("+ Laburnocytisus 'Adamii'", strategy.getTitleCache(speciesName));
        //tbc

        //denomination class (only dummy implementation, may change in future)
        speciesName.setRank(Rank.DENOMINATIONCLASS());
        speciesName.setGenusOrUninomial("Laburnocytisus");
        speciesName.setCultivarEpithet("Adamii");
        assertEquals("Laburnocytisus 'Adamii'", strategy.getTitleCache(speciesName));

        //appended phrase
        speciesName.setRank(Rank.CULTIVAR());
        speciesName.setGenusOrUninomial("Abies");
        speciesName.setSpecificEpithet("alba");
        speciesName.setCultivarEpithet("Cultus");
        speciesName.setAppendedPhrase("appended");
        assertEquals("Abies alba 'Cultus' appended", strategy.getTitleCache(speciesName));
    }

    //10299
    @Test
    public void testVerbatimDate() {

        subSpeciesName.setNomenclaturalReference(citationRef);
        assertEquals(subSpeciesNameString + ", " +  referenceTitle, subSpeciesName.getFullTitleCache());
        subSpeciesName.setNomenclaturalMicroReference("25");
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25", subSpeciesName.getFullTitleCache());
        VerbatimTimePeriod datePublished = TimePeriodParser.parseStringVerbatim("1988");
        datePublished.setVerbatimDate("1989");
        citationRef.setDatePublished(datePublished);
        subSpeciesName.setFullTitleCache(null, false);
        assertEquals(subSpeciesNameString + ", " +  referenceTitle + ": 25. 1988 [\"1989\"]", subSpeciesName.getFullTitleCache());
    }

    //3665
    @Test
    public void testOriginalSpelling() {

        TaxonName originalName = speciesName.clone();
    	originalName.setSpecificEpithet("alpa");
    	assertEquals("Abies alpa", originalName.getTitleCache(), "Preconditions are wrong");
        assertEquals("Abies alpa", originalName.getNameCache(), "Name cache should not show original spelling");

    	speciesName.setOriginalSpelling(originalName);
    	assertEquals("Abies alba [as \"alpa\"]", speciesName.getFullTitleCache());
        assertEquals("Abies alba", speciesName.getTitleCache());
        assertEquals("Abies alba", speciesName.getNameCache(), "Name cache should not show original spelling");

    	originalName.setGenusOrUninomial("Apies");
    	speciesName.setFullTitleCache(null, false);
    	originalName.setNameCache(null, false);
    	//TODO update cache of current name (species name)
    	assertEquals("Abies alba [as \"Apies alpa\"]", speciesName.getFullTitleCache());
        assertEquals("Abies alba", speciesName.getTitleCache());
        assertEquals("Abies alba", speciesName.getNameCache(), "Name cache should not show original spelling");
        originalName.setSpecificEpithet("alba");
        originalName.setNameCache(null, false);
        speciesName.setFullTitleCache(null, false);
        //not fully sure if it is wanted that here only the genus name is given and not the specific epithet too, may change if required by users
        assertEquals("Abies alba [as \"Apies\"]", speciesName.getFullTitleCache());

        //subspecies
        originalName = subSpeciesName.clone();
        originalName.setInfraSpecificEpithet("peta");
        assertEquals("Abies alba subsp. peta", originalName.getTitleCache(), "Preconditions are wrong");
        subSpeciesName.setOriginalSpelling(originalName);
        assertEquals("Abies alba subsp. beta [as \"peta\"]", subSpeciesName.getFullTitleCache());
        assertEquals("Abies alba subsp. beta", subSpeciesName.getTitleCache());
        originalName.setSpecificEpithet("alpa");
        originalName.setNameCache(null, false);
        subSpeciesName.setFullTitleCache(null, false);
        assertEquals("Abies alba subsp. beta [as \"alpa subsp. peta\"]", subSpeciesName.getFullTitleCache());

        originalName.setInfraSpecificEpithet("beta");
        originalName.setNameCache(null, false);
        subSpeciesName.setFullTitleCache(null, false);
        //not fully sure if it is wanted that here only the specific epithet is given and not the infra specific epithet too, may change if required by users
        assertEquals("Abies alba subsp. beta [as \"alpa\"]", subSpeciesName.getFullTitleCache());

    	INonViralName correctName = NonViralNameParserImpl.NewInstance().parseFullName("Nepenthes glabrata J.R.Turnbull & A.T.Middleton");
    	TaxonName originalSpelling = (TaxonName)NonViralNameParserImpl.NewInstance().parseFullName("Nepenthes glabratus");
    	correctName.setOriginalSpelling(originalSpelling);
    	assertEquals("Nepenthes glabrata", correctName.getNameCache());
    	assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton", correctName.getTitleCache());
    	assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton [as \"glabratus\"]", correctName.getFullTitleCache());

    	correctName.setNomenclaturalReference(citationRef);
        assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, My Reference [as \"glabratus\"]", correctName.getFullTitleCache());
        citationRef.setProtectedTitleCache(false);
        citationRef.setTitle("Sp. Pl.");
        citationRef.setDatePublished(TimePeriodParser.parseStringVerbatim("1988"));
        correctName.setFullTitleCache(null, false);
        assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, Sp. Pl. 1988 [as \"glabratus\"]", correctName.getFullTitleCache());
        correctName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
        correctName.setFullTitleCache(null, false);
        assertEquals("Nepenthes glabrata J.R.Turnbull & A.T.Middleton, Sp. Pl. 1988 [as \"glabratus\"], nom. illeg.", correctName.getFullTitleCache());
    }

    //3667
    @Test
    public void testOriginalSpellingItalics() {

        TaxonName originalName = subSpeciesName.clone();
        originalName.setSpecificEpithet("alpa");
        assertEquals("Abies alpa subsp. beta", originalName.getTitleCache(), "Preconditions are wrong");

        subSpeciesName.setOriginalSpelling(originalName);

        List<TaggedText> taggedFullTitle = subSpeciesName.cacheStrategy().getTaggedFullTitle(subSpeciesName);
        assertEquals(7, taggedFullTitle.size());
        assertEquals(new TaggedText(TagEnum.name, "Abies"), taggedFullTitle.get(0));
        assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(2));
        assertEquals(new TaggedText(TagEnum.name, "beta"), taggedFullTitle.get(3));
        assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, " [as \""), taggedFullTitle.get(4));
        assertEquals(new TaggedText(TagEnum.name, "alpa"), taggedFullTitle.get(5));
        assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, "\"]"), taggedFullTitle.get(6));

        originalName.setInfraSpecificEpithet("peta");
        originalName.setNameCache(null, false);
        taggedFullTitle = subSpeciesName.cacheStrategy().getTaggedFullTitle(subSpeciesName);
        assertEquals(9, taggedFullTitle.size());
        assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        assertEquals(new TaggedText(TagEnum.name, "Abies"), taggedFullTitle.get(0));
        assertEquals(new TaggedText(TagEnum.name, "alba"), taggedFullTitle.get(1));
        assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(2));
        assertEquals(new TaggedText(TagEnum.name, "beta"), taggedFullTitle.get(3));
        assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, " [as \""), taggedFullTitle.get(4));
        assertEquals(new TaggedText(TagEnum.name, "alpa"), taggedFullTitle.get(5));
        assertEquals(new TaggedText(TagEnum.rank, "subsp."), taggedFullTitle.get(6));
        assertEquals(new TaggedText(TagEnum.name, "peta"), taggedFullTitle.get(7));
        assertEquals(new TaggedText(TagEnum.nameInSourceSeparator, "\"]"), taggedFullTitle.get(8));
    }

    @Test
    public void testCacheListener() {
        Reference ref = ReferenceFactory.newGeneric();
        ref.setTitleCache("GenericRef",true);
        this.subSpeciesName.setNomenclaturalReference(ref);
        assertEquals("Abies alba subsp. beta, GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setCombinationAuthorship(author);
        subSpeciesName.setBasionymAuthorship(basAuthor);
        assertEquals("Abies alba subsp. beta (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        //cascade name change to fullTitleCache
        subSpeciesName.setRank(Rank.SPECIES());
        subSpeciesName.setProtectedNameCache(true);
        assertNull(subSpeciesName.getNameCache(), "name cache should be null");
        subSpeciesName.setProtectedNameCache(false);
        assertNotNull(subSpeciesName.getNameCache(), "name cache should not be null");
        assertEquals("Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) L.", subSpeciesName.getTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba", subSpeciesName.getNameCache(), "Expected full title cache has error");

        subSpeciesName.setProtectedNameCache(true);
        subSpeciesName.setSpecificEpithet("gamma");
        assertEquals("Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) L.", subSpeciesName.getTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba", subSpeciesName.getNameCache(), "Expected full title cache has error");
        //make original status
        subSpeciesName.setRank(Rank.SUBSPECIES());
        assertEquals("Abies alba (Basio, A.) L., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //author change
        author.setNomenclaturalTitleCache("M.", true);
        assertEquals("(Basio, A.) M.", subSpeciesName.getAuthorshipCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) M.", subSpeciesName.getTitleCache(), "Expected full title cache has error");

        //protectedTitleCache
        subSpeciesName.setProtectedTitleCache(true);
        subSpeciesName.setProtectedNameCache(false);
        subSpeciesName.setGenusOrUninomial("Pinus");
        subSpeciesName.setSpecificEpithet("alba");
        assertEquals("Pinus alba subsp. beta", subSpeciesName.getNameCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        assertEquals("Abies alba (Basio, A.) M.", subSpeciesName.getTitleCache(), "Expected full title cache has error");

        subSpeciesName.setTitleCache("Pinus beta C.", true);
        assertEquals("Pinus beta C., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setProtectedTitleCache(false);

        assertEquals("Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //protected full title cache set
        subSpeciesName.setFullTitleCache("ABC");
        assertEquals("ABC", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setProtectedFullTitleCache(false);
        assertEquals("Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //protected title cache set
        subSpeciesName.setProtectedTitleCache(false);
        assertEquals("Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //protectedNameCache set
        subSpeciesName.setProtectedNameCache(true);
        assertEquals("Pinus alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setNameCache("P. alba subsp. beta");
        assertEquals("P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        subSpeciesName.setGenusOrUninomial("A.");
        subSpeciesName.setProtectedNameCache(false);
        assertEquals("A. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setNameCache("P. alba subsp. beta");
        assertEquals("P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //protected authorship
        subSpeciesName.setProtectedAuthorshipCache(true);
        assertEquals("P. alba subsp. beta (Basio, A.) M., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setAuthorshipCache("Ciard.");
        assertEquals("P. alba subsp. beta Ciard., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        author.setNomenclaturalTitleCache("X.", true);
        subSpeciesName.setProtectedAuthorshipCache(false);
        assertEquals("P. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //clear
        subSpeciesName.setProtectedNameCache(false);
        assertEquals("A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        //appended phrase
        subSpeciesName.setProtectedNameCache(true);
        assertEquals("A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setAppendedPhrase("app phrase");
        assertEquals("A. alba subsp. beta (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setProtectedNameCache(false);
        assertEquals("A. alba subsp. beta app phrase (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setAppendedPhrase("[\"app phrase\"]");
        assertEquals("A. alba subsp. beta [\"app phrase\"] (Basio, A.) X., GenericRef", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");

        subSpeciesName.setAppendedPhrase("app2 phrase2");
        subSpeciesName.setProtectedNameCache(true);
        assertNull(subSpeciesName.getNameCache(), "NameCache should be null");
        subSpeciesName.setProtectedNameCache(false);
        subSpeciesName.setAppendedPhrase(null);

        //ref + nomRef
        Reference book = ReferenceFactory.newBook();
        book.setTitle("Booktitle");
        assertNotNull(subSpeciesName.getTitleCache(), "TitleCache should not be null");
        subSpeciesName.setNomenclaturalReference(book);
        subSpeciesName.setNomenclaturalMicroReference("22");
        assertEquals("A. alba subsp. beta (Basio, A.) X., Booktitle: 22", subSpeciesName.getFullTitleCache(), "Expected full title cache has error");
        subSpeciesName.setProtectedTitleCache(true);
        assertNotNull(subSpeciesName.getTitleCache(), "TitleCache should not be null");

        //year
        IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
        zooName.setGenusOrUninomial("Homo");
        zooName.setSpecificEpithet("sapiens");
        zooName.setBasionymAuthorship(basAuthor);
        zooName.setCombinationAuthorship(author);
        zooName.setNomenclaturalReference(book);
        zooName.setNomenclaturalMicroReference("22");
        assertEquals("Homo sapiens (Basio, A.) X., Booktitle: 22", zooName.getFullTitleCache(), "Expected full title cache has error");

        zooName.setOriginalPublicationYear(1922);
        zooName.setPublicationYear(1948);
        assertEquals("Homo sapiens (Basio, A., 1922) X., 1948, Booktitle: 22", zooName.getFullTitleCache(), "Expected full title cache has error");
        zooName.setOriginalPublicationYear(1923);
        zooName.setProtectedAuthorshipCache(true);
        assertNull(zooName.getAuthorshipCache(), "AuthorshipCache should be null");
        zooName.setProtectedAuthorshipCache(false);
        assertNotNull(zooName.getAuthorshipCache(), "AuthorshipCache should not be null");
        zooName.setPublicationYear(1949);
        zooName.setProtectedAuthorshipCache(true);
        assertNull(zooName.getAuthorshipCache(), "AuthorshipCache should be null");

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
        assertEquals("Genus subg. subgenus", subGenusNameCache, "Subgenus name should be 'Genus subg. subgenus'.");
        String subGenusTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Genus subg. subgenus Anyauthor", subGenusTitle, "Subgenus name should be 'Genus subg. subgenus Anyauthor'.");

        //test species aggregates and species groups
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet(null);
        nonViralName.setAuthorshipCache(null);

        List<TaggedText> aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName, false);

        String aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
        assertEquals("Genus myspecies aggr.", aggrNameCache, "Species aggregate name should be 'Genus myspecies aggr.'.");
        String aggrNameTitle = strategy.getTitleCache(nonViralName);
        assertTrue(aggrNameTitle.indexOf(author) == -1, "Species aggregate should not include author information.");
        assertEquals("Genus myspecies aggr.", aggrNameTitle, "Species aggregate name should be 'Genus myspecies aggr.'.");
        nonViralName.setRank(Rank.SPECIESGROUP());
        String groupNameTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Genus myspecies species group", groupNameTitle, "Species group name should be 'Genus myspecies species group'.");

        //test species aggregates and species groups with infrageneric information
        //TODO check if groups do ever have infrageneric epithets
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet("Infragenus");

        aggrNameCacheTagged = strategy.getInfraGenusTaggedNameCache(nonViralName, false);
        aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
        assertEquals("Genus (Infragenus) myspecies aggr.", aggrNameCache, "Species aggregate name should be 'Genus (Infragenus) myspecies aggr.'.");

        aggrNameTitle = strategy.getTitleCache(nonViralName);
        assertTrue(aggrNameTitle.indexOf(author) == -1, "Species aggregate should not include author information.");
        assertEquals("Genus (Infragenus) myspecies aggr.", aggrNameTitle, "Species aggregate name should be 'Genus (Infragenus) myspecies aggr.'.");

        nonViralName.setRank(Rank.SPECIESGROUP());
        groupNameTitle = strategy.getTitleCache(nonViralName);
        assertEquals("Genus (Infragenus) myspecies species group", groupNameTitle, "Species group name should be 'Genus (Infragenus) myspecies species group'.");

        //aggregates with author and nom.ref. information #4288
        nonViralName.setRank(Rank.SPECIESAGGREGATE());
        nonViralName.setSpecificEpithet("myspecies");
        nonViralName.setInfraGenericEpithet(null);
        nonViralName.setAuthorshipCache("L.");

        aggrNameCacheTagged = strategy.getTaggedTitle(nonViralName);
        aggrNameCache = TaggedTextFormatter.createString(aggrNameCacheTagged);
        assertEquals("Genus myspecies L.", aggrNameCache, "Species aggregate name should be 'Genus myspecies L.'.");

    }

    @Test
    public void testGetTaggedNameSpeciesAggregate() {
        TaxonName speciesAggregate = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIESAGGREGATE());
        speciesAggregate.setGenusOrUninomial("Mygenus");
        speciesAggregate.setSpecificEpithet("myspecies");
        List<TaggedText> taggedName = strategy.getTaggedName(speciesAggregate);
        assertEquals("Mygenus", taggedName.get(0).getText(), "1rd tag must be genus epithet");
        assertEquals("myspecies", taggedName.get(1).getText(), "2rd tag must be species epithet");
        assertEquals("aggr.", taggedName.get(2).getText(), "3rd tag must be aggregate marker");
    }

    @Test
    public void testGetTaggedNameSubspecies(){
        List<TaggedText> taggedName = strategy.getTaggedName(subSpeciesName);
        assertEquals("Abies", taggedName.get(0).getText(), "First tag should be 'Abies'");
        assertEquals("alba", taggedName.get(1).getText(), "Second tag should be 'alba'");
        assertEquals("subsp.", taggedName.get(2).getText(), "Third tag should be subspecies rank, and rank abbreviatioin should be subsp.");
        assertEquals("beta", taggedName.get(3).getText(), "Fourth tag should be 'beta'");
        //to be continued
    }

    @Test
    public void testTitleCacheHtmlTagged(){
    	HTMLTagRules rules = new HTMLTagRules().addRule(TagEnum.name, "i");
    	assertEquals("<i>Abies alba</i>", strategy.getTitleCache(speciesName, rules));
    	rules.addRule(TagEnum.name, "b");
    	assertEquals("<b><i>Abies alba</i></b>", strategy.getTitleCache(speciesName, rules));
    	speciesName.setCombinationAuthorship(author);
    	assertEquals("<b><i>Abies alba</i></b> L.", strategy.getTitleCache(speciesName, rules));
    	rules.addRule(TagEnum.authors, "i");
    	assertEquals("<b><i>Abies alba</i></b> <i>L.</i>", strategy.getTitleCache(speciesName, rules));
    	rules = new HTMLTagRules().addRule(TagEnum.name, "i").addRule(TagEnum.name, "b").addRule(TagEnum.authors, "b");
    	assertEquals("<b><i>Abies alba</i> L.</b>", strategy.getTitleCache(speciesName, rules));
    }

    @Test //#10924
    public void testAutonymForVariety(){
        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.VARIETY());
        name.setGenusOrUninomial("Euphorbia");
        name.setSpecificEpithet("atropurpurea");
        name.setInfraSpecificEpithet("atropurpurea");
        Team combTeam = Team.NewTitledInstance("Combauthor", "Combauthor");
        name.setCombinationAuthorship(combTeam);
        assertEquals("Euphorbia atropurpurea Combauthor var. atropurpurea", name.getTitleCache(), "");

        name.setAutonymFlag(false);
        assertEquals("Euphorbia atropurpurea var. atropurpurea Combauthor", name.getTitleCache(), "");

        name.setRank(Rank.SUBSPECIES());
        assertEquals("Euphorbia atropurpurea Combauthor subsp. atropurpurea", name.getTitleCache(), "The autonym flag is valid only for names below rank subspecies");

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

    	assertEquals("Euphorbia atropurpurea Excomb ex Combauthor f. atropurpurea", name.getTitleCache(), "");
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

        String expected = String.format("Ophrys %skastelli E. Klein nothosubsp. kastelli", UTF8.HYBRID.toString());
        assertEquals(expected, name.getTitleCache(), "");
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
        assertEquals("Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name), "");
        formatter.setEtAlPosition(3);
        assertEquals("Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name), "");
        formatter.setEtAlPosition(2);
        assertEquals("Ophrys kastelli Mill. & al.", formatter.getTitleCache(name), "");
        //null and <2 are handled as "no position defined"
        formatter.setEtAlPosition(1);
        assertEquals("Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name), "");
        formatter.setEtAlPosition(null);
        assertEquals("Ophrys kastelli Mill., Ball. & Cill.", formatter.getTitleCache(name), "");

        name.setBasionymAuthorship(combTeam);
        formatter.setEtAlPosition(2);
        assertEquals("Ophrys kastelli (Mill. & al.) Mill. & al.", formatter.getTitleCache(name), "");

    }

    @Test  //#7443
    public void testInAuthors() {
        //base configuration
        speciesName.setCombinationAuthorship(author);
        speciesName.setBasionymAuthorship(basAuthor);
        String expectedWithoutInAuthor = speciesNameString + " (" + basAuthorString + ") " + authorString;
        assertEquals(expectedWithoutInAuthor, speciesName.getTitleCache());

        //with in-authors
        speciesName.setNameType(NomenclaturalCode.Fungi);
        speciesName.setInBasionymAuthorship(inBasAuthor);
        speciesName.setInCombinationAuthorship(inAuthor);
        String expectedWithInAuthor = speciesNameString + " (" + basAuthorString + " in "+inBasAuthorString+") " + authorString + " in "+inAuthorString;
        speciesName.setTitleCache(null, false);
        assertEquals(expectedWithInAuthor, speciesName.getTitleCache());

        //... for zoo-names
        speciesName.setNameType(NomenclaturalCode.ICZN);
        speciesName.setTitleCache(null, false);
        assertEquals(expectedWithInAuthor, speciesName.getTitleCache());

        //... for botanical names
        speciesName.setNameType(NomenclaturalCode.ICNAFP);
        speciesName.setTitleCache(null, false);
        assertEquals(expectedWithoutInAuthor, speciesName.getTitleCache(), "For now we do not allow in-authors for botanical names (except for fungi)");
    }
}