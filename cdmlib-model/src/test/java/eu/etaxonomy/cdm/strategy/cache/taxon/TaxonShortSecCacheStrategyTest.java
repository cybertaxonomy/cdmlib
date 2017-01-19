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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 09.09.2015
 * NOTE: This test is currently more or less a copy of {@link TaxonBaseDefaultCacheStrategyTest}
 * It does NOT yet test the specifics of the class under test.
 *
 */
public class TaxonShortSecCacheStrategyTest {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonShortSecCacheStrategyTest.class);

	private final String expectedNameTitleCache = "Abies alba (L.) Mill.";
	private final String expectedNameCache = "Abies alba";
	private BotanicalName name;
	private Reference sec;
	private static ITaxonCacheStrategy shortStrategy;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    shortStrategy = new TaxonBaseShortSecCacheStrategy<TaxonBase>();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		name = BotanicalName.NewInstance(Rank.SPECIES());
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

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//******************************* TESTS ********************************************************

	@Test
	public void testGetTitleCache() {
		TaxonBase taxonBase = Taxon.NewInstance(name, sec);
		taxonBase.setCacheStrategy(shortStrategy);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. Sp.Pl.", taxonBase.getTitleCache());
		String appendedPhrase = "aff. 'schippii'";
		taxonBase.setAppendedPhrase(appendedPhrase);
		assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " aff. 'schippii' sec. Sp.Pl.", taxonBase.getTitleCache());
		taxonBase.setUseNameCache(true);
		assertEquals("Taxon titlecache is wrong", expectedNameCache + " aff. 'schippii' sec. Sp.Pl.", taxonBase.getTitleCache());
	}

   @Test
    public void testGetTitleCacheWithoutName() {
        TaxonBase taxonBase = Taxon.NewInstance(null, sec);
        taxonBase.setCacheStrategy(shortStrategy);
        assertEquals("Taxon titlecache is wrong", "??? sec. Sp.Pl.", taxonBase.getTitleCache());
   }

	//test missing "&" in title cache  #3822
	@Test
	public void testAndInTitleCache() {
		TaxonBase<?> taxonBase = Taxon.NewInstance(name, sec);
		Team team = Team.NewInstance();
		team.addTeamMember((Person)name.getCombinationAuthorship());
		team.addTeamMember((Person)name.getBasionymAuthorship());
		name.setCombinationAuthorship(team);
		System.out.println(taxonBase.generateTitle());
		assertEquals("Abies alba (L.) Mill. \u0026 L. sec. Sp.Pl.", taxonBase.generateTitle());

		name = BotanicalName.NewInstance(null);
		NonViralNameParserImpl.NewInstance().parseFullName(name, "Cichorium glandulosum Boiss. \u0026 A. Huet", null, true);
		Taxon taxon = Taxon.NewInstance(name, sec);
		assertEquals("Cichorium glandulosum Boiss. \u0026 A. Huet sec. Sp.Pl.", taxon.getTitleCache());
	}

    @Test
    public void testMicroReference(){
        TaxonBase<?> taxonBase = Taxon.NewInstance(name, sec);
        String secMicroRef = "p. 553";
        taxonBase.setSecMicroReference(secMicroRef);
        assertEquals("Taxon titlecache is wrong", expectedNameTitleCache + " sec. Sp.Pl.: " + secMicroRef,
                taxonBase.getTitleCache());
    }

	@Test
	public void testProtectedTitleCache(){
	    TaxonBase<?> taxonBase = Taxon.NewInstance(name, sec);
        taxonBase.setTitleCache("abc", true);
        taxonBase.setDoubtful(true);
        Assert.assertEquals("abc", taxonBase.getTitleCache());
	}
}
