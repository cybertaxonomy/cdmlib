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
import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IGeneric;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 26.11.2008
 * @version 1.0
 */
public class NonViralNameDefaultCacheStrategyTest {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategyTest.class);

	private NonViralNameDefaultCacheStrategy strategy;
	
	private static final String familyNameString = "Familia";
	private static final String genusNameString = "Genus";
	private static final String speciesNameString = "Abies alba";
	private static final String subSpeciesNameString = "Abies alba subsp. beta";
	private static final String appendedPhraseString = "app phrase";

	private static final String authorString = "L.";
	private static final String exAuthorString = "Exaut.";
	private static final String basAuthorString = "Basio, A.";
	private static final String exBasAuthorString = "ExBas. N.";

	private BotanicalName familyName;
	private BotanicalName genusName;
	private BotanicalName subGenusName;
	private BotanicalName speciesName;
	private BotanicalName subSpeciesName;
	private INomenclaturalAuthor author;
	private INomenclaturalAuthor exAuthor;
	private INomenclaturalAuthor basAuthor;
	private INomenclaturalAuthor exBasAuthor;
	private ReferenceBase citationRef;
	private ReferenceFactory refFactory;
	
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
		refFactory = ReferenceFactory.newInstance();
		strategy = NonViralNameDefaultCacheStrategy.NewInstance();
		familyName = BotanicalName.PARSED_NAME(familyNameString, Rank.FAMILY());
		genusName = BotanicalName.PARSED_NAME(genusNameString, Rank.GENUS());
		
		subGenusName = BotanicalName.NewInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");
		
		speciesName = BotanicalName.PARSED_NAME(speciesNameString);
		subSpeciesName = BotanicalName.PARSED_NAME(subSpeciesNameString);

		author = Person.NewInstance();
		author.setNomenclaturalTitle(authorString);
		exAuthor = Person.NewInstance();
		exAuthor.setNomenclaturalTitle(exAuthorString);
		basAuthor = Person.NewInstance();
		basAuthor.setNomenclaturalTitle(basAuthorString);
		exBasAuthor = Person.NewInstance();
		exBasAuthor.setNomenclaturalTitle(exBasAuthorString);
		
	}

//**************************** TESTS **************************************************
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetTitleCache() {
		logger.warn("Not yet implemented");
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getFullTitleCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetFullTitleCache() {
		logger.warn("Not yet implemented");
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
		BotanicalName botName = BotanicalName.NewInstance(Rank.VARIETY());
		botName.setGenusOrUninomial("Lepidocaryum");
		botName.setSpecificEpithet("tenue");
		botName.setInfraSpecificEpithet("tenue");
		assertEquals("", "Lepidocaryum tenue var. tenue", botName.getNameCache());
		BotanicalName specName = BotanicalName.NewInstance(Rank.SPECIES());
		specName.setGenusOrUninomial("Genus");
		specName.setSpecificEpithet("");
		assertEquals("Empty species string must not result in trailing whitespace", "Genus", specName.getNameCache());
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getAuthorshipCache(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetAuthorshipCache() {
		this.speciesName.setCombinationAuthorTeam(author);
		assertEquals(author.getNomenclaturalTitle(), speciesName.getAuthorshipCache());
		this.speciesName.setBasionymAuthorTeam(basAuthor);
		String expected = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+author.getNomenclaturalTitle();
		assertEquals(expected, speciesName.getAuthorshipCache());
		String authorshipcache = "authorshipcache";
		speciesName.setAuthorshipCache(authorshipcache);
		assertEquals(authorshipcache, speciesName.getAuthorshipCache());
		speciesName.setCombinationAuthorTeam(exAuthor);
		assertEquals(authorshipcache, speciesName.getAuthorshipCache()); //cache is protected
		assertEquals(speciesNameString + " " + authorshipcache, speciesName.getFullTitleCache());
		//unprotected
		speciesName.setProtectedAuthorshipCache(false);
		String atomizedAuthorCache = strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitle();
		assertEquals(atomizedAuthorCache, speciesName.getAuthorshipCache());
		String atomizedTitleCache = speciesNameString + " "+ strategy.getBasionymStart()+ basAuthor.getNomenclaturalTitle()+strategy.getBasionymEnd()+strategy.getBasionymAuthorCombinationAuthorSeperator()+exAuthor.getNomenclaturalTitle();
		assertEquals(atomizedTitleCache, speciesName.getTitleCache());
		assertEquals(atomizedTitleCache, speciesName.getFullTitleCache());	
	}
	
	@Test
	public void testCacheListener() {
		ReferenceBase ref = refFactory.newGeneric();
		ref.setTitleCache("GenericRef");
		this.subSpeciesName.setNomenclaturalReference(ref);
		Assert.assertEquals("Expected full title cache has error", "Abies alba subsp. beta, GenericRef", subSpeciesName.getFullTitleCache());
		subSpeciesName.setCombinationAuthorTeam(author);
		subSpeciesName.setBasionymAuthorTeam(basAuthor);
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

		subSpeciesName.setTitleCache("Pinus beta C.");
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
		ReferenceBase book = refFactory.newBook();
		book.setTitle("Booktitle");
		Assert.assertNotNull("TitleCache should not be null", subSpeciesName.getTitleCache());
		subSpeciesName.setNomenclaturalReference(book);
		subSpeciesName.setNomenclaturalMicroReference("22");
		Assert.assertEquals("Expected full title cache has error", "A. alba subsp. beta (Basio, A.) X., Booktitle: 22", subSpeciesName.getFullTitleCache());
		subSpeciesName.setProtectedTitleCache(true);
		Assert.assertNotNull("TitleCache should not be null", subSpeciesName.getTitleCache());
		
		//year
		ZoologicalName zooName = ZoologicalName.NewInstance(Rank.SPECIES());
		zooName.setGenusOrUninomial("Homo");
		zooName.setSpecificEpithet("sapiens");
		zooName.setBasionymAuthorTeam(basAuthor);
		zooName.setCombinationAuthorTeam(author);
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
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.name.NonViralName)}.
	 */
	@Test
	public void testGetTaggedName() {
		logger.warn("testGetTaggedName yet implemented");
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
	public void testGetInfraGericNames(){
		String author = "Anyauthor";
		NonViralName nonViralName = NonViralName.NewInstance(Rank.SUBGENUS());
		nonViralName.setGenusOrUninomial("Genus");
		nonViralName.setInfraGenericEpithet("subgenus");
		nonViralName.setAuthorshipCache(author);
		//test ordinary infrageneric
		String subGenusNameCache = strategy.getInfraGenusNameCache(nonViralName);
		assertEquals("Subgenus name should be 'Genus subg. subgenus'.", "Genus subg. subgenus", subGenusNameCache);
		String subGenusTitle = strategy.getTitleCache(nonViralName);
		assertEquals("Subgenus name should be 'Genus subg. subgenus Anyauthor'.", "Genus subg. subgenus Anyauthor", subGenusTitle);
		//test species aggregates and species groups
		nonViralName.setRank(Rank.SPECIESAGGREGATE());
		nonViralName.setSpecificEpithet("species");
		String aggrNameCache = strategy.getInfraGenusNameCache(nonViralName);
		assertEquals("Species aggregate name should be 'Genus species aggr.'.", "Genus species aggr.", aggrNameCache);
		String aggrNameTitle = strategy.getTitleCache(nonViralName);
		Assert.assertTrue("Species aggregate should not include author information.", aggrNameTitle.indexOf(author) == -1);
		assertEquals("Species aggregate name should be 'Genus species aggr.'.", "Genus species aggr.", aggrNameTitle);
		nonViralName.setRank(Rank.SPECIESGROUP());
		String groupNameTitle = strategy.getTitleCache(nonViralName);
		assertEquals("Species group name should be 'Genus species group'.", "Genus species group", groupNameTitle);
		
	}

}
