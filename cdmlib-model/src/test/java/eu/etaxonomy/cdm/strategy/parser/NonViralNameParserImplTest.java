/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;

/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImplTest {
	private static final Logger logger = Logger.getLogger(NonViralNameParserImplTest.class);
	
	final private String strNameFamily = "Asteraceae";
	final private String strNameGenus = "Abies Mueller";
	final private String strNameGenusUnicode = "Abies M�ller";
	final private String strNameAbies1 = "Abies alba";
	final private String strNameAbiesSub1 = "Abies alba subsp. beta";
	final private String strNameAbiesAuthor1 = "Abies alba Mueller";
	final private String strNameAbiesAuthor1Unicode = "Abies alba M�ller";
	final private String strNameAbiesBasionymAuthor1 = "Abies alba (Ciardelli) D'Mueller";
	final private String strNameAbiesBasionymAuthor1Unicode = "Abies alba (Ciardelli) D'Müller";
	final private String strNameAbiesBasionymExAuthor1 ="Abies alba (Ciardelli ex Doering) D'Mueller ex. de Greuther"; 
	final private String strNameAbiesBasionymExAuthor1Unicode ="Abies alba (Ciardelli ex D�ring) D'�ller ex. de Greuther"; 
	final private String strNameTeam1 = "Abies alba Mueller & L."; 
	final private String strNameZoo1 = "Abies alba Mueller & L., 1822";
	final private String strNameZoo2 = "Abies alba (Mueller, 1822) Ciardelli, 2002";
	
	final private String strNameEmpty = "";
	final private String strNameNull = null;
	
	private NonViralNameParserImpl parser ;
	private NomenclaturalCode botanicCode; 
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer termInitializer = new DefaultTermInitializer();
		termInitializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		parser = NonViralNameParserImpl.NewInstance();
		botanicCode = NomenclaturalCode.ICBN;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

/*************** TEST *********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#NEW_INSTANCE()}.
	 */
	@Test
	public final void testNEW_INSTANCE() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#TaxonNameParserBotanicalNameImpl()}.
	 */
	@Test
	public final void testTaxonNameParserBotanicalNameImpl() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSubGenericFullName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericFullName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSubGenericSimpleName(java.lang.String)}.
	 */
	@Test
	public final void testParseSubGenericSimpleName() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	@Ignore //TODO Character encoding in svn
	public final void testParseFullNameUnicode() {

		NonViralName nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("M�ller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'M�ller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		BotanicalName basionym = (BotanicalName)nameBasionymAuthor.getBasionym();
		assertEquals("Ciardelli", basionym.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'M�ller", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		BotanicalName basionym2 = (BotanicalName)nameBasionymExAuthor.getBasionym();
		assertEquals("Ciardelli", basionym2.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("D�ring", basionym2.getExCombinationAuthorTeam().getNomenclaturalTitle());
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseFullName() {
		try {
			Method parseMethod = parser.getClass().getDeclaredMethod("parseFullName", String.class, NomenclaturalCode.class, Rank.class);
			testName_StringNomcodeRank(parseMethod);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		//Team
		NonViralName nameTeam1 = parser.parseFullName(strNameTeam1);
		assertEquals( "Abies", nameTeam1.getGenusOrUninomial());
		assertEquals( "alba", nameTeam1.getSpecificEpithet());
		assertEquals("Mueller & L.",  nameTeam1.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertTrue(nameTeam1.getCombinationAuthorTeam() instanceof Team);
		Team team = (Team)nameTeam1.getCombinationAuthorTeam();
		assertEquals("Mueller", team.getTeamMembers().get(0).getNomenclaturalTitle());
		assertEquals("L.", team.getTeamMembers().get(1).getNomenclaturalTitle());

		//ZooName
		ZoologicalName nameZoo1 = (ZoologicalName)parser.parseFullName(strNameZoo1);
		assertEquals( "Abies", nameZoo1.getGenusOrUninomial());
		assertEquals( "alba", nameZoo1.getSpecificEpithet());
		assertEquals("Mueller & L.",  nameZoo1.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals(NomenclaturalCode.ICZN, nameZoo1.getNomenclaturalCode() );
		assertEquals(Integer.valueOf(1822), nameZoo1.getPublicationYear());
		assertTrue(nameZoo1.getCombinationAuthorTeam() instanceof Team);
		Team teamZoo = (Team)nameZoo1.getCombinationAuthorTeam();
		assertEquals("Mueller", teamZoo.getTeamMembers().get(0).getNomenclaturalTitle());
		assertEquals("L.", teamZoo.getTeamMembers().get(1).getNomenclaturalTitle());

		ZoologicalName nameZoo2 = (ZoologicalName)parser.parseFullName(strNameZoo2);
		assertEquals(Integer.valueOf(2002), nameZoo2.getPublicationYear());
		assertEquals(Integer.valueOf(1822), nameZoo2.getOriginalPublicationYear());
		assertEquals("Mueller",  nameZoo2.getBasionymAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli",  nameZoo2.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		
		
		//empty
		NonViralName nameEmpty = parser.parseFullName(strNameEmpty);
		assertNotNull(nameEmpty);
		assertEquals("", nameEmpty.getTitleCache());
		
		//null
		NonViralName nameNull = parser.parseFullName(strNameNull);
		assertNull(nameNull);
	}
	
	private void testName_StringNomcodeRank(Method parseMethod) 
			throws InvocationTargetException, IllegalAccessException  {
		NonViralName name1 = (NonViralName)parseMethod.invoke(parser, strNameAbies1, null, Rank.SPECIES());
		//parser.parseFullName(strNameAbies1, null, Rank.SPECIES());
		assertEquals("Abies", name1.getGenusOrUninomial());
		assertEquals("alba", name1.getSpecificEpithet());
		
		NonViralName nameAuthor = (NonViralName)parseMethod.invoke(parser, strNameAbiesAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("Mueller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymAuthor = (NonViralName)parseMethod.invoke(parser, strNameAbiesBasionymAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName nameBasionymExAuthor = (NonViralName)parseMethod.invoke(parser, strNameAbiesBasionymExAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymExAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		assertEquals("Doering", nameBasionymExAuthor.getExBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName name2 = (NonViralName)parseMethod.invoke(parser, strNameAbiesSub1, null, Rank.SPECIES());
		assertEquals("Abies", name2.getGenusOrUninomial());
		assertEquals("alba", name2.getSpecificEpithet());
		assertEquals("beta", name2.getInfraSpecificEpithet());
		assertEquals(Rank.SUBSPECIES(), name2.getRank());

		
		// unparseable *********
		String problemString = "sdfjlös wer eer wer";
		NonViralName<?> nameProblem = (NonViralName<?>)parseMethod.invoke(parser, problemString, null, Rank.SPECIES());
		assertTrue(nameProblem.getHasProblem());
		assertEquals(problemString, nameProblem.getTitleCache());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseReferencedName(NonViralName, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, boolean)(, )}.
	 */
	@Test
	public final void testParseReferencedName() {
		try {
			Method parseMethod = parser.getClass().getDeclaredMethod("parseReferencedName", String.class, NomenclaturalCode.class, Rank.class);
			testName_StringNomcodeRank(parseMethod);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}

		
		//null
		String strNull = null;
		NonViralName<?> nameNull = parser.parseReferencedName(strNull, null, Rank.SPECIES());
		assertNull(nameNull);
				
		//Empty
		String strEmpty = "";
		NonViralName<?> nameEmpty = parser.parseReferencedName(strEmpty, null, Rank.SPECIES());
		assertFalse(nameEmpty.hasProblem());
		assertEquals(strEmpty, nameEmpty.getFullTitleCache());
		assertNull(nameEmpty.getNomenclaturalMicroReference());
		
		
		//Whitespaces
		String strFullWhiteSpcaceAndDot = "Abies alba Mill.,  Sp.   Pl.  4:  455 .  1987 .";
		NonViralName<?> namefullWhiteSpcaceAndDot = parser.parseReferencedName(strFullWhiteSpcaceAndDot, null, Rank.SPECIES());
		assertFullRefStandard(namefullWhiteSpcaceAndDot);
		assertTrue(namefullWhiteSpcaceAndDot.getNomenclaturalReference() instanceof Book);
		assertEquals( "Abies alba Mill., Sp. Pl. 4: 455. 1987", namefullWhiteSpcaceAndDot.getFullTitleCache());

		//Book
		String fullReference = "Abies alba Mill., Sp. Pl. 4: 455. 1987";
		NonViralName<?> name1 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFullRefStandard(name1);
		assertTrue(name1.getNomenclaturalReference() instanceof Book);
		assertEquals(fullReference, name1.getFullTitleCache());
		assertTrue("Name author and reference author should be the same", name1.getCombinationAuthorTeam() == name1.getNomenclaturalReference().getAuthorTeam());
		
		//Book Section
		fullReference = "Abies alba Mill. in Otto, Sp. Pl. 4(6): 455. 1987";
		NonViralName<?> name2 = parser.parseReferencedName(fullReference + ".", null, Rank.SPECIES());
		assertFullRefNameStandard(name2);
		assertEquals(fullReference, name2.getFullTitleCache());
		assertFalse(name2.hasProblem());
		INomenclaturalReference ref = (INomenclaturalReference)name2.getNomenclaturalReference();
		assertEquals(BookSection.class, ref.getClass());
		BookSection bookSection = (BookSection)ref;
		Book inBook = bookSection.getInBook();
		assertNotNull(inBook);
		assertNotNull(inBook.getAuthorTeam());
		assertEquals("Otto", inBook.getAuthorTeam().getTitleCache());
		assertEquals("Otto, Sp. Pl. 4(6)", inBook.getTitleCache());
		assertEquals("Sp. Pl.", inBook.getTitle());
		assertEquals("4(6)", inBook.getVolume());
		assertTrue("Name author and reference author should be the same", name2.getCombinationAuthorTeam() == name2.getNomenclaturalReference().getAuthorTeam());
		
		//Article
		fullReference = "Abies alba Mill. in Sp. Pl. 4(6): 455. 1987";
		NonViralName<?> name3 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFullRefNameStandard(name3);
		assertEquals(fullReference, name3.getFullTitleCache());
		assertFalse(name3.hasProblem());
		ref = (INomenclaturalReference)name3.getNomenclaturalReference();
		assertEquals(Article.class, ref.getClass());
		Article article = (Article)ref;
		Journal journal = article.getInJournal();
		assertNotNull(journal);
		//assertEquals("Sp. Pl. 4(6)", inBook.getTitleCache());
		assertEquals("Sp. Pl.", journal.getTitleCache());
		assertEquals("Sp. Pl.", journal.getTitle());
		assertEquals("4(6)", article.getVolume());
		assertTrue("Name author and reference author should be the same", name3.getCombinationAuthorTeam() == name3.getNomenclaturalReference().getAuthorTeam());
		
		//SoftArticle - having "," on position > 4
		String journalTitle = "Bull. Soc. Bot.France. Louis., Roi";
		String yearPart = " 1987 - 1989";
		String parsedYear = "1987-1989";
		String fullReferenceWithoutYear = "Abies alba Mill. in " + journalTitle + " 4(6): 455.";
		fullReference = fullReferenceWithoutYear + yearPart;
		String fullReferenceWithEnd = fullReference + ".";
		NonViralName<?> name4 = parser.parseReferencedName(fullReferenceWithEnd, null, Rank.SPECIES());
		assertFalse(name4.hasProblem());
		assertFullRefNameStandard(name4);
		assertEquals(fullReferenceWithoutYear + " " + parsedYear, name4.getFullTitleCache());
		ref = (INomenclaturalReference)name4.getNomenclaturalReference();
		assertEquals(Article.class, ref.getClass());
		article = (Article)ref;
		assertEquals(parsedYear, ref.getYear());
		journal = article.getInJournal();
		assertNotNull(journal);
		assertEquals(journalTitle, journal.getTitleCache());
		assertEquals(journalTitle, journal.getTitle());
		assertEquals("4(6)", article.getVolume());
		
		//Zoo name
		String strNotParsableZoo = "Abies alba M., 1923, Sp. P. xxwer4352, nom. inval.";
		ZoologicalName nameZooRefNotParsabel = (ZoologicalName)parser.parseReferencedName(strNotParsableZoo, null, null);
		assertTrue(nameZooRefNotParsabel.hasProblem());
		assertEquals(21, nameZooRefNotParsabel.getProblemStarts());
		assertEquals(37, nameZooRefNotParsabel.getProblemEnds());
		assertTrue(nameZooRefNotParsabel.getNomenclaturalReference().hasProblem());
		assertEquals(NomenclaturalCode.ICZN, nameZooRefNotParsabel.getNomenclaturalCode());
		assertEquals(Integer.valueOf(1923), nameZooRefNotParsabel.getPublicationYear());
		assertEquals(1, nameZooRefNotParsabel.getStatus().size());

		//Special MicroRefs
		String strSpecDetail1 = "Abies alba Mill. in Sp. Pl. 4(6): [455]. 1987";
		NonViralName<?> nameSpecDet1 = parser.parseReferencedName(strSpecDetail1 + ".", null, Rank.SPECIES());
		assertFalse(nameSpecDet1.hasProblem());
		assertEquals(strSpecDetail1, nameSpecDet1.getFullTitleCache());
		assertEquals("[455]", nameSpecDet1.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail2 = "Abies alba Mill. in Sp. Pl. 4(6): couv. 2. 1987";
		NonViralName<?> nameSpecDet2 = parser.parseReferencedName(strSpecDetail2 + ".", null, Rank.SPECIES());
		assertFalse(nameSpecDet2.hasProblem());
		assertEquals(strSpecDetail2, nameSpecDet2.getFullTitleCache());
		assertEquals("couv. 2", nameSpecDet2.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail3 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455. 1987";
		NonViralName<?> nameSpecDet3 = parser.parseReferencedName(strSpecDetail3 + ".", null, Rank.SPECIES());
		assertFalse(nameSpecDet3.hasProblem());
		assertEquals(strSpecDetail3, nameSpecDet3.getFullTitleCache());
		assertEquals("fig. 455", nameSpecDet3.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail4 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455-567. 1987";
		fullReference = strSpecDetail4 + ".";
		NonViralName<?> nameSpecDet4 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFalse(nameSpecDet4.hasProblem());
		assertEquals(strSpecDetail4, nameSpecDet4.getFullTitleCache());
		assertEquals("fig. 455-567", nameSpecDet4.getNomenclaturalMicroReference());
		
		
		//Special MicroRefs
		String strSpecDetail5 = "Abies alba Mill. in Sp. Pl. 4(6): Gard n� 4. 1987";
		fullReference = strSpecDetail5 + ".";
		NonViralName<?> nameSpecDet5 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFalse(nameSpecDet5.hasProblem());
		assertEquals(strSpecDetail5, nameSpecDet5.getFullTitleCache());
		assertEquals("Gard n� 4", nameSpecDet5.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail6 = "Abies alba Mill. in Sp. Pl. 4(6): 455a. 1987";
		fullReference = strSpecDetail6 + ".";
		NonViralName<?> nameSpecDet6 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFalse(nameSpecDet6.hasProblem());
		assertEquals(strSpecDetail6, nameSpecDet6.getFullTitleCache());
		assertEquals("455a", nameSpecDet6.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail7 = "Abies alba Mill. in Sp. Pl. 4(6): pp.455-457. 1987";
		fullReference = strSpecDetail7 + ".";
		NonViralName<?> nameSpecDet7 = parser.parseReferencedName(fullReference, null, Rank.SPECIES());
		assertFalse(nameSpecDet7.hasProblem());
		assertEquals(strSpecDetail7, nameSpecDet7.getFullTitleCache());
		assertEquals("pp.455-457", nameSpecDet7.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail8 = "Abies alba Mill. in Sp. Pl. 4(6): ppp.455-457. 1987";
		NonViralName<?> nameSpecDet8 = parser.parseReferencedName(strSpecDetail8, null, Rank.SPECIES());
		assertTrue(nameSpecDet8.hasProblem());
		assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
		assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after -457
		

		//Special MicroRefs
		String strSpecDetail9 = "Abies alba Mill. in Sp. Pl. 4(6): pp. 455 - 457. 1987";
		NonViralName<?> nameSpecDet9 = parser.parseReferencedName(strSpecDetail9, null, Rank.SPECIES());
		assertFalse(nameSpecDet9.hasProblem());
		assertEquals(strSpecDetail9, nameSpecDet9.getFullTitleCache());
		assertEquals("pp. 455 - 457", nameSpecDet9.getNomenclaturalMicroReference());

		//Special MicroRefs
		String strSpecDetail10 = "Abies alba Mill. in Sp. Pl. 4(6): p 455. 1987";
		NonViralName<?> nameSpecDet10 = parser.parseReferencedName(strSpecDetail10, null, Rank.SPECIES());
		assertFalse(nameSpecDet10.hasProblem());
		assertEquals(strSpecDetail10, nameSpecDet10.getFullTitleCache());
		assertEquals("p 455", nameSpecDet10.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail11 = "Abies alba Mill. in Sp. Pl. 4(6): p. 455 - 457. 1987";
		NonViralName<?> nameSpecDet11 = parser.parseReferencedName(strSpecDetail11, null, Rank.SPECIES());
		assertTrue(nameSpecDet11.hasProblem());
		assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
		assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after - 457
		
		
		//no volume, no edition
		String strNoVolume = "Abies alba Mill., Sp. Pl.: 455. 1987";
		NonViralName<?> nameNoVolume = parser.parseReferencedName(strNoVolume, null, Rank.SPECIES());
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals(null, ((Book)nameNoVolume.getNomenclaturalReference()).getVolume());
		assertEquals(null, ((Book)nameNoVolume.getNomenclaturalReference()).getEdition());

		//volume, no edition
		strNoVolume = "Abies alba Mill., Sp. Pl. 2: 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, Rank.SPECIES());
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals("2", ((Book)nameNoVolume.getNomenclaturalReference()).getVolume());
		assertEquals(null, ((Book)nameNoVolume.getNomenclaturalReference()).getEdition());

		//no volume, edition
		strNoVolume = "Abies alba Mill., Sp. Pl. ed. 3: 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, Rank.SPECIES());
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals(null, ((Book)nameNoVolume.getNomenclaturalReference()).getVolume());
		assertEquals("3", ((Book)nameNoVolume.getNomenclaturalReference()).getEdition());
		
		//volume, edition
		strNoVolume = "Abies alba Mill., Sp. Pl. ed. 3, 4(5): 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, Rank.SPECIES());
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals("4(5)", ((Book)nameNoVolume.getNomenclaturalReference()).getVolume());
		assertEquals("3", ((Book)nameNoVolume.getNomenclaturalReference()).getEdition());
		
		String strUnparsableInRef = "Abies alba Mill. in -er46: 455. 1987";
		NonViralName<?> nameUnparsableInRef = parser.parseReferencedName(strUnparsableInRef, null, Rank.SPECIES());
		assertTrue(nameUnparsableInRef.hasProblem());
		assertEquals(strUnparsableInRef, nameUnparsableInRef.getFullTitleCache());
		assertEquals(20, nameUnparsableInRef.getProblemStarts()); 
		assertEquals(25, nameUnparsableInRef.getProblemEnds());   
		
		
		//volume, edition
		String strNoSeparator = "Abies alba Mill. Sp. Pl. ed. 3, 4(5): 455. 1987";
		NonViralName<?> nameNoSeparator = parser.parseReferencedName(strNoSeparator, NomenclaturalCode.ICBN, Rank.SPECIES());
		assertTrue(nameNoSeparator.hasProblem());
		assertEquals(strNoSeparator, nameNoSeparator.getFullTitleCache());
		assertEquals(10, nameNoSeparator.getProblemStarts()); //TODO better start behind Mill. (?)
		assertEquals(47, nameNoSeparator.getProblemEnds());   //TODO better stop before :
		
		String strUnparsableInRef2 = "Hieracium pepsicum L., My Bookkkk 1. 1903";
		NonViralName<?> nameUnparsableInRef2 = parser.parseReferencedName(strUnparsableInRef2, null, Rank.SPECIES());
		assertTrue(nameUnparsableInRef2.hasProblem());
		assertEquals(strUnparsableInRef2, nameUnparsableInRef2.getFullTitleCache());
		assertEquals(23, nameUnparsableInRef2.getProblemStarts()); 
		assertEquals(41, nameUnparsableInRef2.getProblemEnds());   
	
		
		String strUnparsableInRef3 = "Hieracium pespcim N., My Bookkkk 1. 1902";
		NonViralName<?> nameUnparsableInRef3 = parser.parseReferencedName(strUnparsableInRef3, null, null);
		assertTrue(nameUnparsableInRef3.hasProblem());
		assertEquals(strUnparsableInRef3, nameUnparsableInRef3.getFullTitleCache());
		assertEquals(22, nameUnparsableInRef3.getProblemStarts()); 
		assertEquals(40, nameUnparsableInRef3.getProblemEnds());   
	
		String strUnparsableInRef4 = "Hieracium pepsicum (Hsllreterto) L., My Bookkkk 1. 1903";
		NonViralName<?> nameUnparsableInRef4 = parser.parseReferencedName(strUnparsableInRef4, null, null);
		assertTrue(nameUnparsableInRef4.hasProblem());
		assertEquals(strUnparsableInRef4, nameUnparsableInRef4.getFullTitleCache());
		assertEquals(37, nameUnparsableInRef4.getProblemStarts()); 
		assertEquals(55, nameUnparsableInRef4.getProblemEnds());   
		
		String strSameName = "Hieracium pepcum (Hsllreterto) L., My Bokkk 1. 1903";
		NonViralName<?> nameSameName = nameUnparsableInRef4;
		parser.parseReferencedName(nameSameName, strSameName, null, true);
		assertTrue(nameSameName.hasProblem());
		assertEquals(strSameName, nameSameName.getFullTitleCache());
		assertEquals(35, nameSameName.getProblemStarts()); 
		assertEquals(51, nameSameName.getProblemEnds());   
		
		String strGenusUnparse = "Hieracium L., jlklk";
		NonViralName<?> nameGenusUnparse = 
			parser.parseReferencedName(strGenusUnparse, null, null);
		assertTrue(nameGenusUnparse.hasProblem());
		assertEquals(strGenusUnparse, nameGenusUnparse.getFullTitleCache());
		assertEquals(0, nameGenusUnparse.getProblemStarts()); 
		assertEquals(19, nameGenusUnparse.getProblemEnds());   
		
		String strGenusUnparse2 = "Hieracium L., Per Luigi: 44. 1987";
		NonViralName<?> nameGenusUnparse2 = 
			parser.parseReferencedName(strGenusUnparse2, null, Rank.FAMILY());
		assertFalse(nameGenusUnparse2.hasProblem());
		assertEquals(strGenusUnparse2, nameGenusUnparse2.getFullTitleCache());
		assertEquals(-1, nameGenusUnparse2.getProblemStarts()); 
		assertEquals(-1, nameGenusUnparse2.getProblemEnds());

		String strBookSection2 = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz, ed. 2, 2: 288. 1905-1907";
		NonViralName<?> nameBookSection2 = 
			parser.parseReferencedName(strBookSection2, null, null);
		assertFalse(nameBookSection2.hasProblem());
		assertEquals(strBookSection2, nameBookSection2.getFullTitleCache());
		assertEquals(-1, nameBookSection2.getProblemStarts()); 
		assertEquals(-1, nameBookSection2.getProblemEnds());
		assertNull(((BookSection)nameBookSection2.getNomenclaturalReference()).getDatePublished().getStart());
		assertEquals("1905-1907", ((BookSection)nameBookSection2.getNomenclaturalReference()).getInBook().getDatePublished().getYear());

		
		String strBookSection = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz, ed. 2, 2: 288. 1905";
		NonViralName<?> nameBookSection = 
			parser.parseReferencedName(strBookSection, null, null);
		assertFalse(nameBookSection.hasProblem());
		assertEquals(strBookSection, nameBookSection.getFullTitleCache());
		assertEquals(-1, nameBookSection.getProblemStarts()); 
		assertEquals(-1, nameBookSection.getProblemEnds());
		assertNull(((BookSection)nameBookSection.getNomenclaturalReference()).getInBook().getDatePublished().getStart());
		assertEquals("1905", ((BookSection)nameBookSection.getNomenclaturalReference()).getDatePublished().getYear());

	}

	private void assertFullRefNameStandard(NonViralName<?> name){
		assertEquals("Abies", name.getGenusOrUninomial());
		assertEquals("alba", name.getSpecificEpithet());
		assertEquals("Mill.", name.getAuthorshipCache());
		assertEquals("455", name.getNomenclaturalMicroReference());
		assertNotNull(name.getNomenclaturalReference());
	}
	
	private void assertFullRefStandard(NonViralName<?> name){
		assertEquals("Abies", name.getGenusOrUninomial());
		assertEquals("alba", name.getSpecificEpithet());
		assertEquals("Mill.", name.getAuthorshipCache());
		assertEquals("455", name.getNomenclaturalMicroReference());
		assertNotNull(name.getNomenclaturalReference());
		INomenclaturalReference ref = (INomenclaturalReference)name.getNomenclaturalReference();
		assertEquals("1987", ref.getYear());
		StrictReferenceBase refBase = (StrictReferenceBase)ref;
		assertEquals("Sp. Pl.", refBase.getTitle());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#fullTeams(java.lang.String)}.
	 */
	@Test
	public final void testFullTeams() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#authorTeamAndEx(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeamAndEx() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#authorTeam(java.lang.String)}.
	 */
	@Test
	public final void testAuthorTeam() {
		logger.warn("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseCultivar(java.lang.String)}.
	 */
	@Test
	public final void testParseCultivar() {
		logger.warn("Not yet implemented"); // TODO
	}

}
