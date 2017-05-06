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

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;

/**
 * @author a.mueller
 *
 */
public class ZooNameNoMarkerCacheStrategyTest extends NameCacheStrategyTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZooNameNoMarkerCacheStrategyTest.class);

	private ZooNameDefaultCacheStrategy strategy;
	private IZoologicalName familyName;
	private IZoologicalName subGenusName;
	private IZoologicalName speciesName;
	private TaxonNameBase<?,ZooNameDefaultCacheStrategy> subSpeciesName;
	private TeamOrPersonBase<?> author;
	private TeamOrPersonBase<?> exAuthor;
	private TeamOrPersonBase<?> basAuthor;
	private TeamOrPersonBase<?> exBasAuthor;

	private final String familyNameString = "Familia";
//	private final String genusNameString = "Genus";
	private final String speciesNameString = "Abies alba";
	private final String subSpeciesNameString = "Abies alba beta";
	private final String subSpeciesNameStringToParse = "Abies alba subsp. beta";

	private final String authorString = "L.";
	private final String exAuthorString = "Exaut.";
	private final String basAuthorString = "Basio, A.";
	private final String exBasAuthorString = "ExBas. N.";

	private final Integer publicationYear = 1928;
	private final Integer originalPublicationYear = 1860;



	/**
	 * @throws java.lang.Exception
	 */
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
		strategy = ZooNameNoMarkerCacheStrategy.NewInstance();
		familyName = TaxonNameFactory.PARSED_ZOOLOGICAL(familyNameString, Rank.FAMILY());

		subGenusName = TaxonNameFactory.NewZoologicalInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");

		speciesName = TaxonNameFactory.PARSED_ZOOLOGICAL(speciesNameString);
		subSpeciesName =(TaxonNameBase) TaxonNameFactory.PARSED_ZOOLOGICAL(subSpeciesNameStringToParse);
		subSpeciesName.setCacheStrategy(strategy);

		author = Person.NewInstance();
		author.setNomenclaturalTitle(authorString);
		exAuthor = Person.NewInstance();
		exAuthor.setNomenclaturalTitle(exAuthorString);
		basAuthor = Person.NewInstance();
		basAuthor.setNomenclaturalTitle(basAuthorString);
		exBasAuthor = Person.NewInstance();
		exBasAuthor.setNomenclaturalTitle(exBasAuthorString);
	}


/********* TEST *******************************************/

	@Test
	public final void testGetNameCache() {
		assertEquals(subSpeciesNameString, subSpeciesName.getNameCache());
		assertNull(subSpeciesNameString, strategy.getNameCache(null));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getFullNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetTitleCache() {
		assertNull(subSpeciesNameString, strategy.getTitleCache(null));
		subSpeciesName.setCombinationAuthorship(author);
		subSpeciesName.setExCombinationAuthorship(exAuthor);
		subSpeciesName.setBasionymAuthorship(basAuthor);
		subSpeciesName.setExBasionymAuthorship(exBasAuthor);
		subSpeciesName.setPublicationYear(publicationYear);
		subSpeciesName.setOriginalPublicationYear(originalPublicationYear);

		assertEquals("Abies alba beta", strategy.getNameCache(subSpeciesName));

		assertEquals("Abies alba beta" + " (" + exBasAuthorString + " ex " + basAuthorString  + ", " + originalPublicationYear +")" +  " " + exAuthorString + " ex " + authorString + ", " + publicationYear, strategy.getTitleCache(subSpeciesName));

		//Autonym,
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorship(author);
		subSpeciesName.setBasionymAuthorship(null);
		subSpeciesName.setExCombinationAuthorship(null);
		subSpeciesName.setExBasionymAuthorship(null);
		//old
		//assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		//assertEquals("Abies alba L. subsp. alba", strategy.getTitleCache(subSpeciesName));
		//new we now assume that there are no autonyms in zoology (source: they do not exist in Fauna Europaea)
		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba alba (1860) L., 1928", strategy.getTitleCache(subSpeciesName));

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getAuthorCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetAuthorshipCache() {
		subSpeciesName.setCombinationAuthorship(author);
		assertEquals(authorString, strategy.getAuthorshipCache(subSpeciesName));
		subSpeciesName.setPublicationYear(publicationYear);
		assertEquals(authorString + ", " + publicationYear, strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExCombinationAuthorship(exAuthor);
		assertEquals( exAuthorString + " ex " + authorString + ", " + publicationYear , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setBasionymAuthorship(basAuthor);
		assertEquals("(" + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear  , strategy.getAuthorshipCache(subSpeciesName));
		subSpeciesName.setOriginalPublicationYear(originalPublicationYear);
		assertEquals("(" + basAuthorString  + ", " + originalPublicationYear  + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear  , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExBasionymAuthorship(exBasAuthor);
		assertEquals("(" + exBasAuthorString + " ex " +  basAuthorString + ", " + originalPublicationYear  + ")" +  " " + exAuthorString + " ex " + authorString  + ", " + publicationYear   , strategy.getAuthorshipCache(subSpeciesName));

		//cache
		subSpeciesName.setAuthorshipCache(authorString);
		assertEquals("AuthorshipCache must be updated", authorString, subSpeciesName.getAuthorshipCache());
		assertEquals("TitleCache must be updated", "Abies alba beta " + authorString, subSpeciesName.getTitleCache());
		subSpeciesName.setProtectedAuthorshipCache(false);
		//TODO make this not needed
		subSpeciesName.setTitleCache(null, false);
		assertEquals("TitleCache must be updated", "Abies alba beta " + "(ExBas. N. ex Basio, A., 1860) Exaut. ex L., 1928", subSpeciesName.getTitleCache());

		assertNull("Authorship cache for null must return null", strategy.getAuthorshipCache(null));

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getUninomialNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetGenusOrUninomialNameCache() {
		assertEquals(familyNameString, strategy.getNameCache(familyName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraGenusNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraGenusTaggedNameCache() {
		String methodName = "getInfraGenusTaggedNameCache";
		Method method = getMethod(NonViralNameDefaultCacheStrategy.class, methodName, INonViralName.class);

		this.getStringValue(method, strategy, subGenusName);
		assertEquals("Genus (InfraGenericPart)", strategy.getNameCache(subGenusName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetSpeciesNameCache() {
		//test correct overriding for ZooNameDefaultCacheStrategy
		assertEquals(speciesNameString, strategy.getNameCache(speciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraSpeciesNameCache() {
		assertEquals("Abies alba beta", strategy.getNameCache(subSpeciesName));
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testAutonyms() {
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorship(author);
		//new: we now assume that there are no autonyms in zoology (source: they do not exist in Fauna Europaea)
		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba alba L.", strategy.getTitleCache(subSpeciesName));
	}


}
