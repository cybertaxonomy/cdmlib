/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.taxon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
//import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationshipType;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 21.09.2009
 */
public class TaxonBaseDefaultCacheStrategyTest extends TermTestBase {

	@SuppressWarnings("unused")
	private static final Logger logger = LogManager.getLogger();

	private final String expectedNameTitleCache = "Abies alba (L.) Mill.";
	private final String expectedNameCache = "Abies alba";
	private IBotanicalName name;
	private Reference sec;

	@BeforeEach
	public void setUp() throws Exception {
		name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		name.setGenusOrUninomial("Abies");
		name.setSpecificEpithet("alba");
		Person combinationAuthor = Person.NewInstance();
		combinationAuthor.setNomenclaturalTitle("Mill.");
		Person basionymAuthor = Person.NewInstance();
		basionymAuthor.setNomenclaturalTitle("L.");

		name.setCombinationAuthorship(combinationAuthor);
		name.setBasionymAuthorship(basionymAuthor);
		assertEquals(expectedNameCache, name.getNameCache(), "Namecache should be Abies alba");
		assertEquals(expectedNameTitleCache, name.getTitleCache(), "Titlecache should be Abies alba (Mill.) L.");
		sec = ReferenceFactory.newBook();
		sec.setTitle("Sp.Pl.");
	}

//******************************* TESTS ********************************************************

	@Test
	public void testGetTitleCache() {
		Taxon taxon = Taxon.NewInstance(name, sec);
		assertEquals(expectedNameTitleCache + " sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
		//without sec.
		taxon.setSec(null);
		assertEquals(expectedNameTitleCache + " sec. ???", taxon.getTitleCache(), "Taxon titlecache is wrong");
		//appended phrase without sec.
		String appendedPhrase = "aff. 'schippii'";
		taxon.setAppendedPhrase(appendedPhrase);
		assertEquals(expectedNameTitleCache + " aff. 'schippii'", taxon.getTitleCache(), "Taxon titlecache is wrong");
		//appended phrase with sec.
		taxon.setSec(sec);
		assertEquals(expectedNameTitleCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
		//use name cache
		taxon.setUseNameCache(true);
		assertEquals(expectedNameCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
		taxon.setDoubtful(true);
        assertEquals("?" + expectedNameCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
        //with nom status
        taxon.setAppendedPhrase(null);
        taxon.setUseNameCache(false);
        taxon.setDoubtful(false);
        name.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        assertEquals(expectedNameTitleCache + ", nom. illeg., sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
	}

    @Test
    public void testGetTitleCacheWithNameUsedInSource() {
        Taxon taxon = Taxon.NewInstance(name, sec);
        TaxonName nameUsedInSource = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        nameUsedInSource.setGenusOrUninomial("Pinus");
        nameUsedInSource.setSpecificEpithet("blanca");
        taxon.getSecSource().setNameUsedInSource(nameUsedInSource);
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl. (sub Pinus blanca)", taxon.getTitleCache(), "Taxon titlecache is wrong");
    }

	//same as for accepted taxa but with syn. sec. instead of sec.
    @Test
    public void testSynSec() {
        Synonym syn = Synonym.NewInstance(name, sec);
        assertEquals(expectedNameTitleCache + " syn. sec. Sp.Pl.", syn.getTitleCache(), "Taxon titlecache is wrong");
        //without sec.
        syn.setSec(null);
        assertEquals(expectedNameTitleCache + " syn. sec. ???", syn.getTitleCache(), "Taxon titlecache is wrong");
        //appended phrase without sec.
        String appendedPhrase = "aff. 'schippii'";
        syn.setAppendedPhrase(appendedPhrase);
        assertEquals(expectedNameTitleCache + " aff. 'schippii'", syn.getTitleCache(), "Taxon titlecache is wrong");
        //appended phrase with sec.
        syn.setSec(sec);
        assertEquals(expectedNameTitleCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache(), "Taxon titlecache is wrong");
        //use name cache
        syn.setUseNameCache(true);
        assertEquals(expectedNameCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache(), "Taxon titlecache is wrong");
        syn.setDoubtful(true);
        assertEquals("?" + expectedNameCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache(), "Taxon titlecache is wrong");
        //with nom status
        syn.setAppendedPhrase(null);
        syn.setUseNameCache(false);
        syn.setDoubtful(false);
        name.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        assertEquals(expectedNameTitleCache + ", nom. illeg., syn. sec. Sp.Pl.", syn.getTitleCache(), "Taxon titlecache is wrong");
    }

   @Test
    public void testGetTitleCacheWithoutName() {
        Taxon taxon = Taxon.NewInstance(null, sec);
        assertEquals("??? sec. Sp.Pl.", taxon.getTitleCache(), "Taxon titlecache is wrong");
    }

	//test missing "&" in title cache  #3822
	@Test
	public void testAndInTitleCache() {
		Taxon taxon = Taxon.NewInstance(name, sec);
		Team team = Team.NewInstance();
		team.addTeamMember((Person)name.getCombinationAuthorship());
		team.addTeamMember((Person)name.getBasionymAuthorship());
		name.setCombinationAuthorship(team);
//		System.out.println(taxonBase.generateTitle());
		assertEquals("Abies alba (L.) Mill. \u0026 L. sec. Sp.Pl.", taxon.generateTitle());

		name = TaxonNameFactory.NewBotanicalInstance(null);
		NonViralNameParserImpl.NewInstance().parseFullName(name, "Cichorium glandulosum Boiss. \u0026 A. Huet", null, true);
		Taxon taxon2 = Taxon.NewInstance(name, sec);
		assertEquals("Cichorium glandulosum Boiss. \u0026 A. Huet sec. Sp.Pl.", taxon2.getTitleCache());
	}

    @Test
	public void testProtectedTitleCache(){
	    Taxon taxon = Taxon.NewInstance(name, sec);
        taxon.setTitleCache("abc", true);
        taxon.setDoubtful(true);
        assertEquals("abc", taxon.getTitleCache());
	}

    @Test
    public void testProtectedSecTitleCache(){
        Taxon taxon = Taxon.NewInstance(name, sec);
        sec.setTitleCache("My protected sec ref", true);
        taxon.setDoubtful(true);
        taxon.setSecMicroReference("123");
        assertEquals("?Abies alba (L.) Mill. sec. My protected sec ref: 123", taxon.getTitleCache());
    }

    @Test
    public void testMicroReference(){
        Taxon taxon = Taxon.NewInstance(name, sec);
        String secMicroRef = "p. 553";

        //not atomized
        taxon.setSecMicroReference(secMicroRef);
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl.: p. 553", taxon.getTitleCache(), "Taxon titlecache is wrong");

        //atomized
        sec.setAuthorship(Team.NewTitledInstance("Team", "T."));
        sec.setDatePublished(TimePeriodParser.parseStringVerbatim("1798"));
        taxon.setTitleCache(null, false);
        assertEquals(expectedNameTitleCache + " sec. Team 1798: p. 553", taxon.getTitleCache(), "Taxon titlecache is wrong");
    }

    @Test
    public void testWebPageSec(){
        Reference sec = ReferenceFactory.newWebPage();
        sec.setTitle("My long webpage");
        sec.setAbbrevTitle("MLW");
        sec.setUri(URI.create("https://abc.de"));
        sec.setDatePublished(TimePeriodParser.parseStringVerbatim("2 Jan 1982"));
        TaxonBase taxonBase = Taxon.NewInstance(name, sec);
        assertEquals("Abies alba (L.) Mill. sec. MLW 1982", taxonBase.getTitleCache());

        sec.setDatePublished((VerbatimTimePeriod)null);
        taxonBase.setTitleCache(null, false);
        assertEquals("Abies alba (L.) Mill. sec. MLW", taxonBase.getTitleCache());

        sec.setAbbrevTitle(null);
        taxonBase.setTitleCache(null, false);
        assertEquals("Abies alba (L.) Mill. sec. My long webpage", taxonBase.getTitleCache());

        sec.setAbbrevTitle("MLW");
        taxonBase.setSecMicroReference("table 1");
        assertEquals("Abies alba (L.) Mill. sec. MLW: table 1", taxonBase.getTitleCache());

        sec.setType(ReferenceType.Database);
        taxonBase.setSecMicroReference(null);
        assertEquals("Abies alba (L.) Mill. sec. MLW", taxonBase.getTitleCache());

        sec.setType(ReferenceType.Map);
        taxonBase.setTitleCache(null, false);
        assertEquals("Abies alba (L.) Mill. sec. MLW", taxonBase.getTitleCache());

        TimePeriod accessed = TimePeriodParser.parseString("5 Mar 2014");
        taxonBase.getSecSource().setAccessed(accessed);
        assertEquals("Abies alba (L.) Mill. sec. MLW 2014", taxonBase.getTitleCache());
    }

    @Test
    public void testMisapplication(){
        //assert default (taxon without relation)
        Taxon man = Taxon.NewInstance(name, sec);
        ITaxonCacheStrategy cacheStrategy = man.cacheStrategy();
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sec");

        //make it a MAN only
        Taxon mainTaxon = Taxon.NewInstance(TaxonNameFactory.NewBacterialInstance(Rank.SPECIES()), ReferenceFactory.newBook() );
        mainTaxon.addMisappliedName(man, null, null);
        assertEquals(expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");
        man.setSec(null);
        assertEquals(expectedNameCache + " auct., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");
        man.setAppendedPhrase("aucts.");
        assertEquals(expectedNameCache + " aucts., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");
        //reset sec + appendedPhrase
        man.setSec(sec);
        man.setAppendedPhrase(null);

        //add another from relation
        Taxon relatedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBacterialInstance(Rank.SPECIES()), ReferenceFactory.newBook() );
        TaxonRelationship rel = man.addTaxonRelation(relatedTaxon, TaxonRelationshipType.CONGRUENT_TO(), null, null);
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sec");
        man.removeTaxonRelation(rel);
        assertEquals(expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");

        //add another to relation
        rel = relatedTaxon.addTaxonRelation(man, TaxonRelationshipType.CONGRUENT_TO(), null, null);
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sec");
        man.removeTaxonRelation(rel);
        assertEquals(expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");

        //add taxon node
        Classification c = Classification.NewInstance("Test");
        TaxonNode tn = c.addChildTaxon(man, null);
        assertEquals(expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sec");
        man.removeTaxonNode(tn);
        assertEquals(expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man), "Taxon titlecache must use sensu");
    }
}