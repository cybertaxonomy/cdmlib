/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.cache.taxon;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
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
	private static final Logger logger = Logger.getLogger(TaxonBaseDefaultCacheStrategyTest.class);

	private final String expectedNameTitleCache = "Abies alba (L.) Mill.";
	private final String expectedNameCache = "Abies alba";
	private IBotanicalName name;
	private Reference sec;

	@Before
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
		assertEquals("Namecache should be Abies alba", expectedNameCache, name.getNameCache());
		assertEquals("Titlecache should be Abies alba (Mill.) L.", expectedNameTitleCache, name.getTitleCache());
		sec = ReferenceFactory.newBook();
		sec.setTitle("Sp.Pl.");
	}

//******************************* TESTS ********************************************************

	@Test
	public void testGetTitleCache() {
		Taxon taxon = Taxon.NewInstance(name, sec);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. Sp.Pl.", taxon.getTitleCache());
		//without sec.
		taxon.setSec(null);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. ???", taxon.getTitleCache());
		//appended phrase without sec.
		String appendedPhrase = "aff. 'schippii'";
		taxon.setAppendedPhrase(appendedPhrase);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " aff. 'schippii'", taxon.getTitleCache());
		//appended phrase with sec.
		taxon.setSec(sec);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache());
		//use name cache
		taxon.setUseNameCache(true);
		assertEquals("Taxon titlecache is wrong", expectedNameCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache());
		taxon.setDoubtful(true);
        assertEquals("Taxon titlecache is wrong", "?" + expectedNameCache + " aff. 'schippii' sec. Sp.Pl.", taxon.getTitleCache());
        //with nom status
        taxon.setAppendedPhrase(null);
        taxon.setUseNameCache(false);
        taxon.setDoubtful(false);
        name.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + ", nom. illeg., sec. Sp.Pl.", taxon.getTitleCache());
	}

	//same as for accepted taxa but with syn. sec. instead of sec.
    @Test
    public void testSynSec() {
        Synonym syn = Synonym.NewInstance(name, sec);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " syn. sec. Sp.Pl.", syn.getTitleCache());
        //without sec.
        syn.setSec(null);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " syn. sec. ???", syn.getTitleCache());
        //appended phrase without sec.
        String appendedPhrase = "aff. 'schippii'";
        syn.setAppendedPhrase(appendedPhrase);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " aff. 'schippii'", syn.getTitleCache());
        //appended phrase with sec.
        syn.setSec(sec);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache());
        //use name cache
        syn.setUseNameCache(true);
        assertEquals("Taxon titlecache is wrong", expectedNameCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache());
        syn.setDoubtful(true);
        assertEquals("Taxon titlecache is wrong", "?" + expectedNameCache + " aff. 'schippii' syn. sec. Sp.Pl.", syn.getTitleCache());
        //with nom status
        syn.setAppendedPhrase(null);
        syn.setUseNameCache(false);
        syn.setDoubtful(false);
        name.addStatus(NomenclaturalStatusType.ILLEGITIMATE(), null, null);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + ", nom. illeg., syn. sec. Sp.Pl.", syn.getTitleCache());
    }

   @Test
    public void testGetTitleCacheWithoutName() {
        Taxon taxon = Taxon.NewInstance(null, sec);
        assertEquals("Taxon titlecache is wrong", "??? sec. Sp.Pl.", taxon.getTitleCache());
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
        Assert.assertEquals("abc", taxon.getTitleCache());
	}

    @Test
    public void testProtectedSecTitleCache(){
        Taxon taxon = Taxon.NewInstance(name, sec);
        sec.setTitleCache("My protected sec ref", true);
        taxon.setDoubtful(true);
        taxon.setSecMicroReference("123");
        Assert.assertEquals("?Abies alba (L.) Mill. sec. My protected sec ref: 123", taxon.getTitleCache());
    }

    @Test
    public void testMicroReference(){
        Taxon taxon = Taxon.NewInstance(name, sec);
        String secMicroRef = "p. 553";

        //not atomized
        taxon.setSecMicroReference(secMicroRef);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. Sp.Pl.: p. 553",
                taxon.getTitleCache());

        //atomized
        sec.setAuthorship(Team.NewTitledInstance("Team", "T."));
        sec.setDatePublished(TimePeriodParser.parseStringVerbatim("1798"));
        taxon.setTitleCache(null, false);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. Team 1798: p. 553",
                taxon.getTitleCache());
    }

    @Test
    public void testWebPageSec(){
        Reference sec = ReferenceFactory.newWebPage();
        sec.setTitle("My long webpage");
        sec.setAbbrevTitle("MLW");
        sec.setUri(URI.create("https://abc.de"));
        sec.setDatePublished(TimePeriodParser.parseStringVerbatim("2 Jan 1982"));
        TaxonBase<?> taxonBase = Taxon.NewInstance(name, sec);
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW 1982", taxonBase.getTitleCache());

        sec.setDatePublished(null);
        taxonBase.setTitleCache(null, false);
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW", taxonBase.getTitleCache());

        sec.setAccessed(DateTime.parse("1983-06-30"));
        taxonBase.setTitleCache(null, false);
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW 1983", taxonBase.getTitleCache());

        sec.setAbbrevTitle(null);
        taxonBase.setTitleCache(null, false);
        Assert.assertEquals("Abies alba (L.) Mill. sec. My long webpage 1983", taxonBase.getTitleCache());

        sec.setAbbrevTitle("MLW");
        taxonBase.setSecMicroReference("table 1");
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW 1983: table 1", taxonBase.getTitleCache());

        sec.setType(ReferenceType.Database);
        taxonBase.setSecMicroReference(null);
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW 1983", taxonBase.getTitleCache());

        sec.setType(ReferenceType.Map);
        taxonBase.setTitleCache(null, false);
        Assert.assertEquals("Abies alba (L.) Mill. sec. MLW 1983", taxonBase.getTitleCache());

    }

    @Test
    public void testMisapplication(){
        //assert default (taxon without relation)
        Taxon man = Taxon.NewInstance(name, sec);
        ITaxonCacheStrategy<Taxon> cacheStrategy = man.cacheStrategy();
        assertEquals("Taxon titlecache must use sec", expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man));

        //make it a MAN only
        Taxon mainTaxon = Taxon.NewInstance(TaxonNameFactory.NewBacterialInstance(Rank.SPECIES()), ReferenceFactory.newBook() );
        mainTaxon.addMisappliedName(man, null, null);
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man));
        man.setSec(null);
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " auct., non (L.) Mill.", cacheStrategy.getTitleCache(man));
        man.setAppendedPhrase("aucts.");
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " aucts., non (L.) Mill.", cacheStrategy.getTitleCache(man));
        //reset sec + appendedPhrase
        man.setSec(sec);
        man.setAppendedPhrase(null);

        //add another from relation
        Taxon relatedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBacterialInstance(Rank.SPECIES()), ReferenceFactory.newBook() );
        TaxonRelationship rel = man.addTaxonRelation(relatedTaxon, TaxonRelationshipType.CONGRUENT_TO(), null, null);
        assertEquals("Taxon titlecache must use sec", expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man));
        man.removeTaxonRelation(rel);
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man));

        //add another to relation
        rel = relatedTaxon.addTaxonRelation(man, TaxonRelationshipType.CONGRUENT_TO(), null, null);
        assertEquals("Taxon titlecache must use sec", expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man));
        man.removeTaxonRelation(rel);
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man));

        //add taxon node
        Classification c = Classification.NewInstance("Test");
        TaxonNode tn = c.addChildTaxon(man, null);
        assertEquals("Taxon titlecache must use sec", expectedNameTitleCache + " sec. Sp.Pl.", cacheStrategy.getTitleCache(man));
        man.removeTaxonNode(tn);
        assertEquals("Taxon titlecache must use sensu", expectedNameCache + " sensu Sp.Pl., non (L.) Mill.", cacheStrategy.getTitleCache(man));
    }
}