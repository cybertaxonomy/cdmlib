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
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.VerbatimTimePeriod;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 *
 */
public class BotanicNameCacheStrategyTest extends NameCacheStrategyTestBase{
	private static final Logger logger = Logger.getLogger(BotanicNameCacheStrategyTest.class);

	private static final String familyNameString = "Familia";
	private static final String genusNameString = "Genus";
	private static final String speciesNameString = "Abies alba";
	private static final String subSpeciesNameString = "Abies alba subsp. beta";
	private static final String appendedPhraseString = "app phrase";

	private static final String authorString = "L.";
	private static final String exAuthorString = "Exaut.";
	private static final String basAuthorString = "Basio, A.";
	private static final String exBasAuthorString = "ExBas. N.";

	private TaxonNameDefaultCacheStrategy strategy;
	private TaxonName familyName;
	private TaxonName genusName;
	private TaxonName subGenusName;
	private TaxonName speciesName;
	private TaxonName subSpeciesName;
	private TeamOrPersonBase<?> author;
	private TeamOrPersonBase<?> exAuthor;
	private TeamOrPersonBase<?> basAuthor;
	private TeamOrPersonBase<?> exBasAuthor;
	private Reference citationRef;
	ReferenceFactory refFactory;

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
		strategy = TaxonNameDefaultCacheStrategy.NewInstance();
		familyName = TaxonNameFactory.PARSED_BOTANICAL(familyNameString, Rank.FAMILY());
		genusName = TaxonNameFactory.PARSED_BOTANICAL(genusNameString, Rank.GENUS());

		subGenusName = TaxonNameFactory.NewBotanicalInstance(Rank.SUBGENUS());
		subGenusName.setGenusOrUninomial("Genus");
		subGenusName.setInfraGenericEpithet("InfraGenericPart");

		speciesName = TaxonNameFactory.PARSED_BOTANICAL(speciesNameString);
		subSpeciesName = TaxonNameFactory.PARSED_BOTANICAL(subSpeciesNameString);

		author = Person.NewInstance();
		author.setNomenclaturalTitle(authorString);
		exAuthor = Person.NewInstance();
		exAuthor.setNomenclaturalTitle(exAuthorString);
		basAuthor = Person.NewInstance();
		basAuthor.setNomenclaturalTitle(basAuthorString);
		exBasAuthor = Person.NewInstance();
		exBasAuthor.setNomenclaturalTitle(exBasAuthorString);

		citationRef = refFactory.newBook();
		// Gard. Dict. ed. 8, no. 1. 1768.
		citationRef.setTitle("Gard. Dict.");
		//citationRef.setPlacePublished("");
		citationRef.setVolume("1");
		citationRef.setEdition("ed. 8");
		GregorianCalendar testDate = new GregorianCalendar();
		testDate.set(1968, 3, 2);

		VerbatimTimePeriod period = VerbatimTimePeriod.NewVerbatimInstance(testDate);

		citationRef.setDatePublished(period);

		//speciesName.setNomenclaturalMicroReference("89");
		NomenclaturalStatus nomStatus = NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE());
		speciesName.addStatus(nomStatus);

		speciesName.setNomenclaturalReference(citationRef);
		speciesName.setAppendedPhrase("app phrase");

//		subSpeciesName.setNomenclaturalReference(citationRef);
//		subSpeciesName.setAppendedPhrase("app phrase");
	}



/********* TEST *******************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#NewInstance()}.
	 */
	@Test
	public final void testNewInstance() {
	    TaxonNameDefaultCacheStrategy cacheStrategy = TaxonNameDefaultCacheStrategy.NewInstance();
		assertNotNull(cacheStrategy);
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
	 * Test method for {@link eu.etaxonomy.cdm.strategy.cache.name.BotanicNameDefaultCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)}.
	 */
	@Test
	public final void testGetTitleCache() {
		assertNull(subSpeciesNameString, strategy.getTitleCache(null));
		subSpeciesName.setCombinationAuthorship(author);
		subSpeciesName.setExCombinationAuthorship(exAuthor);
		subSpeciesName.setBasionymAuthorship(basAuthor);
		subSpeciesName.setExBasionymAuthorship(exBasAuthor);
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
		assertEquals(subSpeciesNameString + " (" + exBasAuthorString + " ex " + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getTitleCache(subSpeciesName));

		subSpeciesName.setExCombinationAuthorship(null);
		assertEquals(subSpeciesNameString + " (" + exBasAuthorString + " ex " + basAuthorString + ")" +  " " + authorString , strategy.getTitleCache(subSpeciesName));

		subSpeciesName.setExBasionymAuthorship(null);
		assertEquals(subSpeciesNameString + " (" + basAuthorString + ")" +  " " + authorString , strategy.getTitleCache(subSpeciesName));


		//Autonym
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorship(author);
		subSpeciesName.setBasionymAuthorship(null);
		subSpeciesName.setExCombinationAuthorship(null);
		subSpeciesName.setExBasionymAuthorship(null);
		//changed 2009-09-04
		assertEquals("Abies alba subsp. alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba L. subsp. alba", strategy.getTitleCache(subSpeciesName));



		}

	@Test
	public final void testGetFullTitleCache() {
		assertNull("Full title cache of null object should return null", strategy.getFullTitleCache(null));
		//NOTE: Unclear if the date part should contain the full date or only the year. Undefined Behaviour.
		assertEquals("Abies alba app phrase, Gard. Dict., ed. 8, 1. 2.4.1968, nom. illeg.", strategy.getFullTitleCache(speciesName));

//		assertNull(subSpeciesNameString, strategy.getFullTitleCache(null));
//		assertEquals("Abies alba app phrase L. Gard. Dict. ed. 8, 1. 1768, nom. illeg.", strategy.getFullTitleCache(speciesName));
	}

	@Test
	public final void testGetAuthorshipCache() {
		subSpeciesName.setCombinationAuthorship(author);
		assertEquals(authorString, strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExCombinationAuthorship(exAuthor);
		assertEquals(exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setBasionymAuthorship(basAuthor);
		assertEquals("(" + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));

		subSpeciesName.setExBasionymAuthorship(exBasAuthor);
		assertEquals("(" + exBasAuthorString + " ex " + basAuthorString + ")" +  " " + exAuthorString + " ex " + authorString  , strategy.getAuthorshipCache(subSpeciesName));

		assertNull(subSpeciesNameString, strategy.getAuthorshipCache(null));
	}

	@Test
	public final void testGetGenusOrUninomialNameCache() {
		assertEquals(familyNameString, strategy.getNameCache(familyName));
	}

	@Test
	public final void testGetInfraGenusTaggedNameCache() {
		String methodName = "getInfraGenusTaggedNameCache";
		Method method = getMethod(TaxonNameDefaultCacheStrategy.class, methodName, INonViralName.class);
		this.getStringValue(method, strategy, subGenusName);
		assertEquals("Genus subg. InfraGenericPart", strategy.getNameCache(subGenusName));
	}

	@Test
	public final void testGetSpeciesNameCache() {
		String nameString = speciesNameString + " " + appendedPhraseString;
		assertEquals(nameString, strategy.getNameCache(speciesName));
	}

	@Test
	public final void testGetInfraSpeciesNameCache() {
		assertEquals(subSpeciesNameString, strategy.getNameCache(subSpeciesName));
	}

	@Test
	public final void testAutonyms() {
		subSpeciesName.setInfraSpecificEpithet("alba");
		subSpeciesName.setCombinationAuthorship(author);
		//changed 2009-09-04
		assertEquals("Abies alba subsp. alba", strategy.getNameCache(subSpeciesName));
		assertEquals("Abies alba L. subsp. alba", strategy.getTitleCache(subSpeciesName));
	}

	@Override
    protected Method getMethod(Class clazz, String methodName, Class paramClazzes){
		Method method;
		try {
			method = clazz.getDeclaredMethod(methodName, paramClazzes);
		} catch (SecurityException e) {
			logger.error("SecurityException " + e.getMessage());
			return null;
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException " + e.getMessage());
			return null;
		}
		return method;
	}



}
