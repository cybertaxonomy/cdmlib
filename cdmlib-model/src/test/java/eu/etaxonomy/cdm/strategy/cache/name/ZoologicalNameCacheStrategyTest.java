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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;

/**
 * @author a.mueller
 *
 */
public class ZoologicalNameCacheStrategyTest extends NameCacheStrategyTestBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ZoologicalNameCacheStrategyTest.class);
	
	private ZooNameDefaultCacheStrategy strategy;
	private ZoologicalName familyName;
	private ZoologicalName genusName;
	private ZoologicalName subGenusName;
	private ZoologicalName speciesName;
	private ZoologicalName subSpeciesName;
	private TeamOrPersonBase<?> author;
	private TeamOrPersonBase<?> exAuthor;
	private TeamOrPersonBase<?> basAuthor;
	private TeamOrPersonBase<?> exBasAuthor;
	
	private final String familyNameString = "Familia";
	private final String genusNameString = "Genus";
	private final String speciesNameString = "Abies alba";
	private final String subSpeciesNameString = "Abies alba subsp. beta";

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
		strategy = ZooNameDefaultCacheStrategy.NewInstance();
		familyName = ZoologicalName.PARSED_NAME(familyNameString, Rank.FAMILY());
		genusName = ZoologicalName.PARSED_NAME(genusNameString, Rank.GENUS());
		
		subGenusName = ZoologicalName.NewInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");
		
		speciesName = ZoologicalName.PARSED_NAME(speciesNameString);
		subSpeciesName = ZoologicalName.PARSED_NAME(subSpeciesNameString);

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

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
		assertNotNull(strategy);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getNameCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
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
		
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
		assertEquals(subSpeciesNameString + " (" + exBasAuthorString + " ex " + basAuthorString  + ", " + originalPublicationYear +")" +  " " + exAuthorString + " ex " + authorString + ", " + publicationYear, strategy.getTitleCache(subSpeciesName));
		
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
		assertEquals("Abies alba subsp. alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba subsp. alba (1860) L., 1928", strategy.getTitleCache(subSpeciesName));
		
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
		assertEquals("TitleCache must be updated", "Abies alba subsp. beta " + authorString, subSpeciesName.getTitleCache());
		subSpeciesName.setProtectedAuthorshipCache(false);
		//TODO make this not needed
		subSpeciesName.setTitleCache(null, false);
		assertEquals("TitleCache must be updated", "Abies alba subsp. beta " + "(ExBas. N. ex Basio, A., 1860) Exaut. ex L., 1928", subSpeciesName.getTitleCache());
		
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
		Method method = getMethod(NonViralNameDefaultCacheStrategy.class, methodName, NonViralName.class);
		
		this.getStringValue(method, strategy, subGenusName);
		assertEquals("Genus subg. InfraGenericPart", strategy.getNameCache(subGenusName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetSpeciesNameCache() {
		assertEquals(speciesNameString, strategy.getNameCache(speciesName));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testGetInfraSpeciesNameCache() {
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getInfraSpeciesNameCache(eu.etaxonomy.cdm.model.name.BotanicalName)}.
	 */
	@Test
	public final void testAutonyms() {
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorship(author);
		//old
//		assertEquals("Abies alba alba", strategy.getNameCache(subSpeciesName));
//		assertEquals("Abies alba L. subsp. alba", strategy.getTitleCache(subSpeciesName));
		//new
		//new: we now assume that there are no autonyms in zoology (source: they do not exist in Fauna Europaea)
		assertEquals("Abies alba subsp. alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba subsp. alba L.", strategy.getTitleCache(subSpeciesName));
	}
	
	
}
