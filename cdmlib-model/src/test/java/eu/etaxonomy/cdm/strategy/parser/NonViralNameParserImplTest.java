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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImplTest {
	private static final NomenclaturalCode ICBN = NomenclaturalCode.ICNAFP;
	private static final NomenclaturalCode ICZN = NomenclaturalCode.ICZN;

	private static final Logger logger = Logger.getLogger(NonViralNameParserImplTest.class);
	
	final private String strNameFamily = "Asteraceae";
	final private String strNameGenus = "Abies Mueller";
	final private String strNameGenusUnicode = "Abies M\u00FCller";
	final private String strNameAbies1 = "Abies alba";
	final private String strNameAbiesSub1 = "Abies alba subsp. beta";
	final private String strNameAbiesAuthor1 = "Abies alba Mueller";
	final private String strNameAbiesAuthor1Unicode = "Abies alba M\u00FCller";
	final private String strNameAbiesBasionymAuthor1 = "Abies alba (Ciardelli) D'Mueller";
	final private String strNameAbiesBasionymAuthor1Unicode = "Abies alba (Ciardelli) D'M\u00FCller";
	final private String strNameAbiesBasionymExAuthor1 ="Abies alba (Ciardelli ex Doering) D'Mueller ex. de Greuther"; 
	final private String strNameAbiesBasionymExAuthor1Unicode ="Abies alba (Ciardelli ex D\u00F6ring) D'M\u00FCller ex. de Greuther"; 
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
	@Before
	public void setUp() throws Exception {
		parser = NonViralNameParserImpl.NewInstance();
		botanicCode = ICBN;
	}


/*************** TEST *********************************************/
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#NEW_INSTANCE()}.
	 */
	@Test
	public final void testNewInstance() {
		assertNotNull(parser);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#TaxonNameParserBotanicalNameImpl()}.
	 */
	@Test
	public final void testTaxonNameParserBotanicalNameImpl() {
		logger.warn("Not yet implemented"); // TODO
	}
	
	@Test
	public final void testTeamSeperation(){
		Rank speciesRank = Rank.SPECIES();
		NonViralName<?> name;
		
//		String strNameWith1AUthorAndCommaSepEditon = "Abies alba Mill., Sp. Pl., ed. 3: 455. 1987";
//		name = parser.parseReferencedName(strNameWith1AUthorAndCommaSepEditon, botanicCode, speciesRank);
//		Assert.assertFalse("No problems should exist", name.hasProblem());
//		Assert.assertEquals("Name should not include reference part", "Abies alba Mill.", name.getTitleCache());
//		Assert.assertEquals("Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());
//		
//		
//		String strNameWith2Authors = "Abies alba L. & Mill., Sp. Pl., ed. 3: 455. 1987";
//		name = parser.parseReferencedName(strNameWith2Authors, botanicCode, speciesRank);
//		Assert.assertFalse("No problems should exist", name.hasProblem());
//		Assert.assertEquals("Name should not include reference part", "Abies alba L. & Mill.", name.getTitleCache());
//		Assert.assertEquals("Name should have authorteam with 2 authors", 2, ((Team)name.getCombinationAuthorTeam()).getTeamMembers().size());
//		Assert.assertEquals("L. & Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());
		
		String strNameWith3Authors = "Abies alba Mess., L. & Mill., Sp. Pl., ed. 3: 455. 1987";
		name = parser.parseReferencedName(strNameWith3Authors, botanicCode, speciesRank);
		Assert.assertFalse("No problems should exist", name.hasProblem());
		Assert.assertEquals("Name should not include reference part", "Abies alba Mess., L. & Mill.", name.getTitleCache());
		Assert.assertEquals("Name should have authorteam with 2 authors", 3, ((Team)name.getCombinationAuthorTeam()).getTeamMembers().size());
		Assert.assertEquals("Mess., L. & Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testParseSimpleName() {
		
		//Uninomials
		ZoologicalName milichiidae = (ZoologicalName)parser.parseSimpleName("Milichiidae", NomenclaturalCode.ICZN, null);
		assertEquals("Family rank expected", Rank.FAMILY(), milichiidae.getRank());
		BotanicalName crepidinae = (BotanicalName)parser.parseSimpleName("Crepidinae", ICBN, null);
		assertEquals("Family rank expected", Rank.SUBTRIBE(), crepidinae.getRank());
		BotanicalName abies = (BotanicalName)parser.parseSimpleName("Abies", ICBN, null);
		assertEquals("Family rank expected", Rank.GENUS(), abies.getRank());
		
		abies.addParsingProblem(ParserProblem.CheckRank);
		parser.parseSimpleName(abies, "Abies", abies.getRank(), true);
		assertTrue(abies.getParsingProblems().contains(ParserProblem.CheckRank));
		
		BotanicalName rosa = (BotanicalName)parser.parseSimpleName("Rosaceae", ICBN, null);
		assertTrue("Rosaceae have rank family", rosa.getRank().equals(Rank.FAMILY()));
		assertTrue("Rosaceae must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
		parser.parseSimpleName(rosa, "Rosaceaex", abies.getRank(), true);
		assertEquals("Rosaceaex have rank genus", Rank.GENUS(), rosa.getRank());
		assertTrue("Rosaceaex must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
	
		//repeat but remove warning after first parse
		rosa = (BotanicalName)parser.parseSimpleName("Rosaceae", ICBN, null);
		assertTrue("Rosaceae have rank family", rosa.getRank().equals(Rank.FAMILY()));
		assertTrue("Rosaceae must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
		rosa.removeParsingProblem(ParserProblem.CheckRank);
		parser.parseSimpleName(rosa, "Rosaceaex", rosa.getRank(), true);
		assertEquals("Rosaceaex have rank family", Rank.FAMILY(), rosa.getRank());
		assertFalse("Rosaceaex must have no rank warning", rosa.hasProblem(ParserProblem.CheckRank));

		
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
	public final void testParseFullNameUnicode() {

		NonViralName<?> nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("M\u00FCller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName<?> nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'M\u00FCller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		INomenclaturalAuthor basionymTeam = nameBasionymAuthor.getBasionymAuthorTeam();
		assertEquals("Ciardelli", basionymTeam.getNomenclaturalTitle());
		
		NonViralName<?> nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'M\u00FCller", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		INomenclaturalAuthor basionymTeam2 = nameBasionymExAuthor.getExBasionymAuthorTeam();
		assertEquals("Ciardelli", basionymTeam2.getNomenclaturalTitle());
		INomenclaturalAuthor exBasionymTeam2 = nameBasionymExAuthor.getBasionymAuthorTeam();
		assertEquals("D\u00F6ring", exBasionymTeam2.getNomenclaturalTitle());
		
		BotanicalName nameBasionymExAuthor2 = (BotanicalName)parser.parseFullName("Washingtonia filifera (Linden ex Andre) H.Wendl. ex de Bary", null, Rank.SPECIES());
		assertEquals("Washingtonia", nameBasionymExAuthor2.getGenusOrUninomial());
		assertEquals("filifera", nameBasionymExAuthor2.getSpecificEpithet());
		assertEquals("H.Wendl.", nameBasionymExAuthor2.getExCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Bary", nameBasionymExAuthor2.getCombinationAuthorTeam().getNomenclaturalTitle());
		INomenclaturalAuthor basionymTeam3 = nameBasionymExAuthor2.getBasionymAuthorTeam();
		assertEquals("Andre", basionymTeam3.getNomenclaturalTitle());
		INomenclaturalAuthor exBasionymTeam3 = nameBasionymExAuthor2.getExBasionymAuthorTeam();
		assertEquals("Linden", exBasionymTeam3.getNomenclaturalTitle());
		String title = nameBasionymExAuthor2.generateTitle();
		assertEquals("Washingtonia filifera (Linden ex Andre) H.Wendl. ex de Bary", title);
	
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
		NonViralName<?> nameTeam1 = parser.parseFullName(strNameTeam1);
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
		
		//Autonym
		BotanicalName autonymName = (BotanicalName)parser.parseFullName("Abies alba Mill. var. alba", ICBN, null);
		assertFalse("Autonym should be parsable", autonymName.hasProblem());
		
		
		//empty
		NonViralName<?> nameEmpty = parser.parseFullName(strNameEmpty);
		assertNotNull(nameEmpty);
		assertEquals("", nameEmpty.getTitleCache());
		
		//null
		NonViralName<?> nameNull = parser.parseFullName(strNameNull);
		assertNull(nameNull);
		
		//some authors
		String fullNameString = "Abies alba (Greuther & L'Hiver & al. ex M\u00FCller & Schmidt)Clark ex Ciardelli"; 
		NonViralName<?> authorname = (BotanicalName)parser.parseFullName(fullNameString);
		assertFalse(authorname.hasProblem());
		assertEquals("Basionym author should have 3 authors", 3, ((Team)authorname.getExBasionymAuthorTeam()).getTeamMembers().size());

	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testHybrids() {
		try {
			Method parseMethod = parser.getClass().getDeclaredMethod("parseFullName", String.class, NomenclaturalCode.class, Rank.class);
			testName_StringNomcodeRank(parseMethod);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		//Species hybrid
//		NonViralName nameTeam1 = parser.parseFullName("Aegilops \u00D7insulae-cypri H. Scholz");
		NonViralName<?> name1 = parser.parseFullName("Aegilops \u00D7insulae Scholz", botanicCode, null);
		assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
		assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
		assertFalse("Name must not have trinom hybrid bit set", name1.isTrinomHybrid());
		assertEquals("Species epithet must be 'insulae'", "insulae", name1.getSpecificEpithet());
		
		//Uninomial hybrid
		name1 = parser.parseFullName("x Aegilops Scholz", botanicCode, null);
		assertTrue("Name must have monom hybrid bit set", name1.isMonomHybrid());
		assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
		assertFalse("Name must not have trinom hybrid bit set", name1.isTrinomHybrid());
		assertEquals("Uninomial must be 'Aegilops'", "Aegilops", name1.getGenusOrUninomial());

		//Species hybrid
		name1 = parser.parseFullName("Aegilops insulae subsp. X abies Scholz", botanicCode, null);
		assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
		assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
		assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
		assertEquals("Infraspecific epithet must be 'abies'", "abies", name1.getInfraSpecificEpithet());
		

	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testUnrankedNames() {
		try {
			Method parseMethod = parser.getClass().getDeclaredMethod("parseFullName", String.class, NomenclaturalCode.class, Rank.class);
			testName_StringNomcodeRank(parseMethod);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		//unranked infraspecific
		String infraspecificUnranked = "Genus species [unranked] infraspecific";
		NonViralName<?> name = parser.parseFullName(infraspecificUnranked);
		assertEquals( "Genus", name.getGenusOrUninomial());
		assertEquals( "species", name.getSpecificEpithet());
		assertEquals( "infraspecific", name.getInfraSpecificEpithet());
		assertEquals( "Unranked rank should be parsed", Rank.INFRASPECIFICTAXON(), name.getRank());

		//unranked infrageneric
		String infraGenericUnranked = "Genus [unranked] Infragen";
		NonViralName<?> name2 = parser.parseFullName(infraGenericUnranked);
		assertEquals( "Genus", name2.getGenusOrUninomial());
		assertEquals( null, name2.getSpecificEpithet());
		assertEquals( "Infragen", name2.getInfraGenericEpithet());
		assertEquals( "Unranked rank should be parsed", Rank.INFRAGENERICTAXON(), name2.getRank());

	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
	 */
	@Test
	public final void testHybridFormulars() {
		try {
			Method parseMethod = parser.getClass().getDeclaredMethod("parseFullName", String.class, NomenclaturalCode.class, Rank.class);
			testName_StringNomcodeRank(parseMethod);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		
		//Species hybrid
		String hybridCache = "Abies alba \u00D7 Pinus bus";
		NonViralName<?> name1 = parser.parseFullName(hybridCache, botanicCode, null);
		assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
		assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
		assertEquals("Title cache must be correct", hybridCache, name1.getTitleCache());
		List<HybridRelationship> orderedRels = name1.getOrderedChildRelationships();
		assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
		NonViralName<?> firstParent = orderedRels.get(0).getParentName();
		assertEquals("Name must have Abies alba as first hybrid parent", "Abies alba", firstParent.getTitleCache());
		NonViralName<?> secondParent = orderedRels.get(1).getParentName();
		assertEquals("Name must have Pinus bus as second hybrid parent", "Pinus bus", secondParent.getTitleCache());
		assertEquals("Hybrid name must have the lowest rank ('species') as rank", Rank.SPECIES(), name1.getRank());
		assertNull("Name must not have a genus eptithet", name1.getGenusOrUninomial());
		assertNull("Name must not have a specific eptithet", name1.getSpecificEpithet());

		
		//x-sign
		hybridCache = "Abies alba x Pinus bus";
		name1 = parser.parseFullName(hybridCache, botanicCode, null);
		assertFalse("Name must be parsable", name1.hasProblem());
		assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
		
		//Subspecies first hybrid
		name1 = parser.parseFullName("Abies alba subsp. beta \u00D7 Pinus bus", botanicCode, null);
		assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
		assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
		assertEquals("Title cache must be correct", "Abies alba subsp. beta \u00D7 Pinus bus", name1.getTitleCache());
		orderedRels = name1.getOrderedChildRelationships();
		assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
		firstParent = orderedRels.get(0).getParentName();
		assertEquals("Name must have Abies alba subsp. beta as first hybrid parent", "Abies alba subsp. beta", firstParent.getTitleCache());
		secondParent = orderedRels.get(1).getParentName();
		assertEquals("Name must have Pinus bus as second hybrid parent", "Pinus bus", secondParent.getTitleCache());
		assertEquals("Hybrid name must have the lower rank ('subspecies') as rank", Rank.SUBSPECIES(), name1.getRank());

		//variety second hybrid
		name1 = parser.parseFullName("Abies alba \u00D7 Pinus bus  var. beta", botanicCode, null);
		assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
		assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
		assertEquals("Title cache must be correct", "Abies alba \u00D7 Pinus bus var. beta", name1.getTitleCache());
		assertEquals("Hybrid name must have the lower rank ('variety') as rank", Rank.VARIETY(), name1.getRank());
		
		//hybrids with authors
		name1 = parser.parseFullName("Abies alba L. \u00D7 Pinus bus Mill.", botanicCode, null);
		assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
		assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
		assertEquals("Title cache must be correct", "Abies alba L. \u00D7 Pinus bus Mill.", name1.getTitleCache());
		orderedRels = name1.getOrderedChildRelationships();
		assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
		firstParent = orderedRels.get(0).getParentName();
		assertEquals("Name must have Abies alba L. as first hybrid parent", "Abies alba L.", firstParent.getTitleCache());
		secondParent = orderedRels.get(1).getParentName();
		assertEquals("Name must have Pinus bus Mill. as second hybrid parent", "Pinus bus Mill.", secondParent.getTitleCache());
		assertEquals("Hybrid name must have the lower rank ('species') as rank", Rank.SPECIES(), name1.getRank());
	    
	}

	
	private void testName_StringNomcodeRank(Method parseMethod) 
			throws InvocationTargetException, IllegalAccessException  {
		NonViralName<?> name1 = (NonViralName<?>)parseMethod.invoke(parser, strNameAbies1, null, Rank.SPECIES());
		//parser.parseFullName(strNameAbies1, null, Rank.SPECIES());
		assertEquals("Abies", name1.getGenusOrUninomial());
		assertEquals("alba", name1.getSpecificEpithet());
		
		NonViralName<?> nameAuthor = (NonViralName<?>)parseMethod.invoke(parser, strNameAbiesAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameAuthor.getGenusOrUninomial());
		assertEquals("alba", nameAuthor.getSpecificEpithet());
		assertEquals("Mueller", nameAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		
		NonViralName<?> nameBasionymAuthor = (NonViralName<?>)parseMethod.invoke(parser, strNameAbiesBasionymAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName<?> nameBasionymExAuthor = (NonViralName<?>)parseMethod.invoke(parser, strNameAbiesBasionymExAuthor1, null, Rank.SPECIES());
		assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
		assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
		assertEquals("D'Mueller", nameBasionymExAuthor.getExCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorTeam().getNomenclaturalTitle());
		assertEquals("Ciardelli", nameBasionymExAuthor.getExBasionymAuthorTeam().getNomenclaturalTitle());
		assertEquals("Doering", nameBasionymExAuthor.getBasionymAuthorTeam().getNomenclaturalTitle());
		
		NonViralName<?> name2 = (NonViralName<?>)parseMethod.invoke(parser, strNameAbiesSub1, null, Rank.SPECIES());
		assertEquals("Abies", name2.getGenusOrUninomial());
		assertEquals("alba", name2.getSpecificEpithet());
		assertEquals("beta", name2.getInfraSpecificEpithet());
		assertEquals(Rank.SUBSPECIES(), name2.getRank());

		
		// unparseable *********
		String problemString = "sdfjlös wer eer wer";
		NonViralName<?> nameProblem = (NonViralName<?>)parseMethod.invoke(parser, problemString, null, Rank.SPECIES());
		List<ParserProblem> list = nameProblem.getParsingProblems();
		assertTrue(nameProblem.getParsingProblem()!=0);
		assertEquals(problemString, nameProblem.getTitleCache());
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseReferencedName(NonViralName, java.lang.String, eu.etaxonomy.cdm.model.name.Rank, boolean)(, )}.
	 */
	@Test
	public final void testParseNomStatus() {
		//nom. ambig.
		String strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. ambig.";
		NonViralName<?> nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.AMBIGUOUS(), nameTestStatus.getStatus().iterator().next().getType());
		
		//nom. dub.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. dub.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.DOUBTFUL(), nameTestStatus.getStatus().iterator().next().getType());
		
		//nom. confus.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. confus.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.CONFUSUM(), nameTestStatus.getStatus().iterator().next().getType());
		
		//nom. illeg.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. illeg.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.ILLEGITIMATE(), nameTestStatus.getStatus().iterator().next().getType());
		
		//nom. superfl.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. superfl.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.SUPERFLUOUS(), nameTestStatus.getStatus().iterator().next().getType());
		
		//nom. rej.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. rej.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.REJECTED(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. utique rej.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. utique rej.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.UTIQUE_REJECTED(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. cons. prop.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. cons. prop.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.CONSERVED_PROP(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. orth. cons. prop.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. orth. cons. prop.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. legit.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. legit.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.LEGITIMATE(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. altern.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. altern.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.ALTERNATIVE(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. alternativ.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. alternativ.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.ALTERNATIVE(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. nov.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. nov.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.NOVUM(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. utique rej. prop.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. utique rej. prop.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.UTIQUE_REJECTED_PROP(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. orth. cons.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. orth. cons.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. rej. prop.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. rej. prop.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.REJECTED_PROP(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. cons.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. cons.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.CONSERVED(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. sanct.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. sanct.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.SANCTIONED(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. inval.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. inval.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.INVALID(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. nud.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. nud.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.NUDUM(), nameTestStatus.getStatus().iterator().next().getType());

		//comb. inval.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, comb. inval.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.COMBINATION_INVALID(), nameTestStatus.getStatus().iterator().next().getType());

		//comb. illeg.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, comb. illeg.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.COMBINATION_ILLEGITIMATE(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. provis.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. provis.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.PROVISIONAL(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. valid
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. valid";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.VALID(), nameTestStatus.getStatus().iterator().next().getType());

		//nom. subnud.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. subnud.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.SUBNUDUM(), nameTestStatus.getStatus().iterator().next().getType());

		//opus. utique oppr.
		strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, opus. utique oppr.";
		nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
		assertFullRefStandard(nameTestStatus);
		assertTrue(nameTestStatus.getStatus().size()== 1);
		assertEquals( NomenclaturalStatusType.OPUS_UTIQUE_OPPR(), nameTestStatus.getStatus().iterator().next().getType());

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
		Rank rankSpecies = Rank.SPECIES();
		NonViralName<?> nameNull = parser.parseReferencedName(strNull, null, rankSpecies);
		assertNull(nameNull);
				
		//Empty
		String strEmpty = "";
		NonViralName<?> nameEmpty = parser.parseReferencedName(strEmpty, null, rankSpecies);
		assertFalse(nameEmpty.hasProblem());
		assertEquals(strEmpty, nameEmpty.getFullTitleCache());
		assertNull(nameEmpty.getNomenclaturalMicroReference());
		
		
		//Whitespaces
		String strFullWhiteSpcaceAndDot = "Abies alba Mill.,  Sp.   Pl.  4:  455 .  1987 .";
		NonViralName<?> namefullWhiteSpcaceAndDot = parser.parseReferencedName(strFullWhiteSpcaceAndDot, null, rankSpecies);
		assertFullRefStandard(namefullWhiteSpcaceAndDot);
		assertTrue(((Reference<?>)namefullWhiteSpcaceAndDot.getNomenclaturalReference()).getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
		assertEquals( "Abies alba Mill., Sp. Pl. 4: 455. 1987", namefullWhiteSpcaceAndDot.getFullTitleCache());

		//Book
		String fullReference = "Abies alba Mill., Sp. Pl. 4: 455. 1987";
		NonViralName<?> name1 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFullRefStandard(name1);
		assertTrue(((Reference<?>)name1.getNomenclaturalReference()).getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
		assertEquals(fullReference, name1.getFullTitleCache());
		assertTrue("Name author and reference author should be the same", name1.getCombinationAuthorTeam() == ((Reference<?>)name1.getNomenclaturalReference()).getAuthorTeam());
		
		//Book Section
		fullReference = "Abies alba Mill. in Otto, Sp. Pl. 4(6): 455. 1987";
		NonViralName<?> name2 = parser.parseReferencedName(fullReference + ".", null, rankSpecies);
		assertFullRefNameStandard(name2);
		assertEquals(fullReference, name2.getFullTitleCache());
		assertFalse(name2.hasProblem());
		INomenclaturalReference ref = name2.getNomenclaturalReference();
		assertEquals(eu.etaxonomy.cdm.model.reference.ReferenceType.BookSection, ((Reference<?>)ref).getType());
		IBookSection bookSection = (IBookSection) ref;
		IBook inBook = bookSection.getInBook();
		assertNotNull(inBook);
		assertNotNull(inBook.getAuthorTeam());
		assertEquals("Otto", inBook.getAuthorTeam().getTitleCache());
		assertEquals("Otto, Sp. Pl. 4(6)", inBook.getTitleCache());
		assertEquals("Sp. Pl.", inBook.getTitle());
		assertEquals("4(6)", inBook.getVolume());
		assertTrue("Name author and reference author should be the same", name2.getCombinationAuthorTeam() == ((Reference<?>)name2.getNomenclaturalReference()).getAuthorTeam());
		
		//Article
		fullReference = "Abies alba Mill. in Sp. Pl. 4(6): 455. 1987";
		NonViralName<?> name3 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFullRefNameStandard(name3);
		name3.setTitleCache(null);
		assertEquals(fullReference, name3.getFullTitleCache());
		assertFalse(name3.hasProblem());
		ref = name3.getNomenclaturalReference();
		assertEquals(eu.etaxonomy.cdm.model.reference.ReferenceType.Article, ref.getType());
		//Article article = (Article)ref;
		IJournal journal = ((IArticle)ref).getInJournal();
		assertNotNull(journal);
		//assertEquals("Sp. Pl. 4(6)", inBook.getTitleCache());
		assertEquals("Sp. Pl.",((Reference<?>) journal).getTitleCache());
		assertEquals("Sp. Pl.", journal.getTitle());
		assertEquals("4(6)",((IArticle)ref).getVolume());
		assertTrue("Name author and reference author should be the same", name3.getCombinationAuthorTeam() == name3.getNomenclaturalReference().getAuthorTeam());
		
		//SoftArticle - having "," on position > 4
		String journalTitle = "Bull. Soc. Bot.France. Louis., Roi";
		String yearPart = " 1987 - 1989";
		String parsedYear = "1987-1989";
		String fullReferenceWithoutYear = "Abies alba Mill. in " + journalTitle + " 4(6): 455.";
		fullReference = fullReferenceWithoutYear + yearPart;
		String fullReferenceWithEnd = fullReference + ".";
		NonViralName<?> name4 = parser.parseReferencedName(fullReferenceWithEnd, null, rankSpecies);
		assertFalse(name4.hasProblem());
		assertFullRefNameStandard(name4);
		assertEquals(fullReferenceWithoutYear + " " + parsedYear, name4.getFullTitleCache());
		ref = name4.getNomenclaturalReference();
		assertEquals(ReferenceType.Article, ref.getType());
		//article = (Article)ref;
		assertEquals(parsedYear, ref.getYear());
		journal = ((IArticle)ref).getInJournal();
		assertNotNull(journal);
		assertEquals(journalTitle, ((Reference<?>) journal).getTitleCache());
		assertEquals(journalTitle, journal.getTitle());
		assertEquals("4(6)", ((IArticle)ref).getVolume());
		
		//Zoo name
		String strNotParsableZoo = "Abies alba M., 1923, Sp. P. xxwer4352, nom. inval.";
		ZoologicalName nameZooRefNotParsabel = (ZoologicalName)parser.parseReferencedName(strNotParsableZoo, null, null);
		assertTrue(nameZooRefNotParsabel.hasProblem());
		List<ParserProblem> list = nameZooRefNotParsabel.getParsingProblems();
		assertTrue("List must contain detail and year warning ", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(21, nameZooRefNotParsabel.getProblemStarts());
		assertEquals(37, nameZooRefNotParsabel.getProblemEnds());
		assertTrue(nameZooRefNotParsabel.getNomenclaturalReference().hasProblem());
		list = nameZooRefNotParsabel.getNomenclaturalReference().getParsingProblems();
		assertTrue("List must contain detail and year warning ", list.contains(ParserProblem.CheckDetailOrYear));
		
		assertEquals(NomenclaturalCode.ICZN, nameZooRefNotParsabel.getNomenclaturalCode());
		assertEquals(Integer.valueOf(1923), nameZooRefNotParsabel.getPublicationYear());
		assertEquals(1, nameZooRefNotParsabel.getStatus().size());

		String strZooNameSineYear = "Homo sapiens L., 1758, Sp. An. 3: 345";
		ZoologicalName nameZooNameSineYear = (ZoologicalName)parser.parseReferencedName(strZooNameSineYear);
		assertFalse(nameZooNameSineYear.hasProblem());
		assertEquals("Name without reference year must have year", (Integer)1758, nameZooNameSineYear.getPublicationYear());
		assertEquals("Name without reference year must have year", "1758", nameZooNameSineYear.getNomenclaturalReference().getYear());
		
		String strZooNameNewCombination = "Homo sapiens (L., 1758) Mill., 1830, Sp. An. 3: 345";
		ZoologicalName nameZooNameNewCombination = (ZoologicalName)parser.parseReferencedName(strZooNameNewCombination);
		assertTrue(nameZooNameNewCombination.hasProblem());
		list = nameZooNameNewCombination.getParsingProblems();
		assertTrue("List must contain new combination has publication warning ", list.contains(ParserProblem.NewCombinationHasPublication));
		assertEquals(35, nameZooNameNewCombination.getProblemStarts());
		assertEquals(51, nameZooNameNewCombination.getProblemEnds());
		
		
		//Special MicroRefs
		String strSpecDetail1 = "Abies alba Mill. in Sp. Pl. 4(6): [455]. 1987";
		NonViralName<?> nameSpecDet1 = parser.parseReferencedName(strSpecDetail1 + ".", null, rankSpecies);
		assertFalse(nameSpecDet1.hasProblem());
		assertEquals(strSpecDetail1, nameSpecDet1.getFullTitleCache());
		assertEquals("[455]", nameSpecDet1.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail2 = "Abies alba Mill. in Sp. Pl. 4(6): couv. 2. 1987";
		NonViralName<?> nameSpecDet2 = parser.parseReferencedName(strSpecDetail2 + ".", null, rankSpecies);
		assertFalse(nameSpecDet2.hasProblem());
		assertEquals(strSpecDetail2, nameSpecDet2.getFullTitleCache());
		assertEquals("couv. 2", nameSpecDet2.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail3 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455. 1987";
		NonViralName<?> nameSpecDet3 = parser.parseReferencedName(strSpecDetail3 + ".", null, rankSpecies);
		assertFalse(nameSpecDet3.hasProblem());
		assertEquals(strSpecDetail3, nameSpecDet3.getFullTitleCache());
		assertEquals("fig. 455", nameSpecDet3.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail4 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455-567. 1987";
		fullReference = strSpecDetail4 + ".";
		NonViralName<?> nameSpecDet4 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFalse(nameSpecDet4.hasProblem());
		assertEquals(strSpecDetail4, nameSpecDet4.getFullTitleCache());
		assertEquals("fig. 455-567", nameSpecDet4.getNomenclaturalMicroReference());
		
		
		//Special MicroRefs
		String strSpecDetail5 = "Abies alba Mill. in Sp. Pl. 4(6): Gard n\u00B0 4. 1987";
		fullReference = strSpecDetail5 + ".";
		NonViralName<?> nameSpecDet5 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFalse(nameSpecDet5.hasProblem());
		assertEquals(strSpecDetail5, nameSpecDet5.getFullTitleCache());
		assertEquals("Gard n\u00B0 4", nameSpecDet5.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail6 = "Abies alba Mill. in Sp. Pl. 4(6): 455a. 1987";
		fullReference = strSpecDetail6 + ".";
		NonViralName<?> nameSpecDet6 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFalse(nameSpecDet6.hasProblem());
		assertEquals(strSpecDetail6, nameSpecDet6.getFullTitleCache());
		assertEquals("455a", nameSpecDet6.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail7 = "Abies alba Mill. in Sp. Pl. 4(6): pp.455-457. 1987";
		fullReference = strSpecDetail7 + ".";
		NonViralName<?> nameSpecDet7 = parser.parseReferencedName(fullReference, null, rankSpecies);
		assertFalse(nameSpecDet7.hasProblem());
		assertEquals(strSpecDetail7, nameSpecDet7.getFullTitleCache());
		assertEquals("pp.455-457", nameSpecDet7.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail8 = "Abies alba Mill. in Sp. Pl. 4(6): ppp.455-457. 1987";
		NonViralName<?> nameSpecDet8 = parser.parseReferencedName(strSpecDetail8, null, rankSpecies);
		assertTrue(nameSpecDet8.hasProblem());
		assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
		assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after -457
		

		//Special MicroRefs
		String strSpecDetail9 = "Abies alba Mill. in Sp. Pl. 4(6): pp. 455 - 457. 1987";
		NonViralName<?> nameSpecDet9 = parser.parseReferencedName(strSpecDetail9, null, rankSpecies);
		assertFalse(nameSpecDet9.hasProblem());
		assertEquals(strSpecDetail9, nameSpecDet9.getFullTitleCache());
		assertEquals("pp. 455 - 457", nameSpecDet9.getNomenclaturalMicroReference());

		//Special MicroRefs
		String strSpecDetail10 = "Abies alba Mill. in Sp. Pl. 4(6): p 455. 1987";
		NonViralName<?> nameSpecDet10 = parser.parseReferencedName(strSpecDetail10, null, rankSpecies);
		assertFalse(nameSpecDet10.hasProblem());
		assertEquals(strSpecDetail10, nameSpecDet10.getFullTitleCache());
		assertEquals("p 455", nameSpecDet10.getNomenclaturalMicroReference());
		
		//Special MicroRefs
		String strSpecDetail11 = "Abies alba Mill. in Sp. Pl. 4(6): p. 455 - 457. 1987";
		NonViralName<?> nameSpecDet11 = parser.parseReferencedName(strSpecDetail11, null, rankSpecies);
		assertTrue(nameSpecDet11.hasProblem());
		list = nameSpecDet11.getParsingProblems();
		assertTrue("Problem is Detail. Must be pp.", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
		assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after - 457
		
		
		//no volume, no edition
		String strNoVolume = "Abies alba Mill., Sp. Pl.: 455. 1987";
		NonViralName<?> nameNoVolume = parser.parseReferencedName(strNoVolume, null, rankSpecies);
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals(null, ((IVolumeReference)(nameNoVolume.getNomenclaturalReference())).getVolume());
		assertEquals(null, ((IBook)nameNoVolume.getNomenclaturalReference()).getEdition());

		//volume, no edition
		strNoVolume = "Abies alba Mill., Sp. Pl. 2: 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, rankSpecies);
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals("2", ((IVolumeReference)(nameNoVolume.getNomenclaturalReference())).getVolume());
		assertEquals(null, ((IBook)(nameNoVolume.getNomenclaturalReference())).getEdition());

		//no volume, edition
		strNoVolume = "Abies alba Mill., Sp. Pl., ed. 3: 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, rankSpecies);
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume, nameNoVolume.getFullTitleCache());
		assertEquals(null, ((IVolumeReference)(nameNoVolume.getNomenclaturalReference())).getVolume());
		assertEquals("3", ((IBook)(nameNoVolume.getNomenclaturalReference())).getEdition());
		
		//volume, edition
		strNoVolume = "Abies alba Mill., Sp. Pl. ed. 3, 4(5): 455. 1987";
		nameNoVolume = parser.parseReferencedName(strNoVolume, null, rankSpecies);
		assertFalse(nameNoVolume.hasProblem());
		assertEquals(strNoVolume.replace(" ed.", ", ed."), nameNoVolume.getFullTitleCache());
		assertEquals("4(5)", ((IVolumeReference)(nameNoVolume.getNomenclaturalReference())).getVolume());
		assertEquals("3", ((IBook)(nameNoVolume.getNomenclaturalReference())).getEdition());
		
		String strUnparsableInRef = "Abies alba Mill. in -er46: 455. 1987";
		NonViralName<?> nameUnparsableInRef = parser.parseReferencedName(strUnparsableInRef, null, rankSpecies);
		assertTrue(nameUnparsableInRef.hasProblem());
		list = nameUnparsableInRef.getParsingProblems();
		assertTrue("Unparsable title", list.contains(ParserProblem.UnparsableReferenceTitle));
		assertEquals(strUnparsableInRef, nameUnparsableInRef.getFullTitleCache());
		assertEquals(20, nameUnparsableInRef.getProblemStarts()); 
		assertEquals(25, nameUnparsableInRef.getProblemEnds());   
		
		
		//volume, edition
		String strNoSeparator = "Abies alba Mill. Sp. Pl. ed. 3, 4(5): 455. 1987";
		NonViralName<?> nameNoSeparator = parser.parseReferencedName(strNoSeparator, ICBN, rankSpecies);
		assertTrue(nameNoSeparator.hasProblem());
		list = nameNoSeparator.getParsingProblems();
		assertTrue("Problem is missing name-reference separator", list.contains(ParserProblem.NameReferenceSeparation));
		assertEquals(strNoSeparator, nameNoSeparator.getFullTitleCache());
		assertEquals(10, nameNoSeparator.getProblemStarts()); //TODO better start behind Mill. (?)
		assertEquals(47, nameNoSeparator.getProblemEnds());   //TODO better stop before :
		
		String strUnparsableInRef2 = "Hieracium pepsicum L., My Bookkkk 1. 1903";
		NonViralName<?> nameUnparsableInRef2 = parser.parseReferencedName(strUnparsableInRef2, null, rankSpecies);
		assertTrue(nameUnparsableInRef2.hasProblem());
		list = nameUnparsableInRef2.getParsingProblems();
		assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(strUnparsableInRef2, nameUnparsableInRef2.getFullTitleCache());
		assertEquals(23, nameUnparsableInRef2.getProblemStarts()); 
		assertEquals(41, nameUnparsableInRef2.getProblemEnds());   
	
		
		String strUnparsableInRef3 = "Hieracium pespcim N., My Bookkkk 1. 1902";
		NonViralName<?> nameUnparsableInRef3 = parser.parseReferencedName(strUnparsableInRef3, null, null);
		assertTrue(nameUnparsableInRef3.hasProblem());
		list = nameUnparsableInRef3.getParsingProblems();
		assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(strUnparsableInRef3, nameUnparsableInRef3.getFullTitleCache());
		assertEquals(22, nameUnparsableInRef3.getProblemStarts()); 
		assertEquals(40, nameUnparsableInRef3.getProblemEnds());   
	
		String strUnparsableInRef4 = "Hieracium pepsicum (Hsllreterto) L., My Bookkkk 1. 1903";
		NonViralName<?> nameUnparsableInRef4 = parser.parseReferencedName(strUnparsableInRef4, null, null);
		assertTrue(nameUnparsableInRef4.hasProblem());
		list = nameUnparsableInRef4.getParsingProblems();
		assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(strUnparsableInRef4, nameUnparsableInRef4.getFullTitleCache());
		assertEquals(37, nameUnparsableInRef4.getProblemStarts()); 
		assertEquals(55, nameUnparsableInRef4.getProblemEnds());   
		
		String strSameName = "Hieracium pepcum (Hsllreterto) L., My Bokkk 1. 1903";
		NonViralName<?> nameSameName = nameUnparsableInRef4;
		parser.parseReferencedName(nameSameName, strSameName, null, true);
		assertTrue(nameSameName.hasProblem());
		list = nameSameName.getParsingProblems();
		assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
		assertEquals(strSameName, nameSameName.getFullTitleCache());
		assertEquals(35, nameSameName.getProblemStarts()); 
		assertEquals(51, nameSameName.getProblemEnds());   
		
		String strGenusUnparse = "Hieracium L., jlklk";
		NonViralName<?> nameGenusUnparse = 
			parser.parseReferencedName(strGenusUnparse, null, null);
		assertTrue(nameGenusUnparse.hasProblem());
		list = nameGenusUnparse.getParsingProblems();
		assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
		assertTrue("Problem uninomial", list.contains(ParserProblem.CheckRank));
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
		String strBookSection2NoComma = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz ed. 2, 2: 288. 1905-1907";
		NonViralName<?> nameBookSection2 = 
			parser.parseReferencedName(strBookSection2, null, null);
		assertFalse(nameBookSection2.hasProblem());
		nameBookSection2.setFullTitleCache(null, false);
		assertEquals(strBookSection2NoComma.replace(" ed.", ", ed."), nameBookSection2.getFullTitleCache());
		assertEquals(-1, nameBookSection2.getProblemStarts()); 
		assertEquals(-1, nameBookSection2.getProblemEnds());
		assertNull((nameBookSection2.getNomenclaturalReference()).getDatePublished().getStart());
		assertEquals("1905-1907", ((IBookSection)nameBookSection2.getNomenclaturalReference()).getInBook().getDatePublished().getYear());

		
		String strBookSection = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz ed. 2, 2: 288. 1905";
		NonViralName<?> nameBookSection = 
			parser.parseReferencedName(strBookSection, null, null);
		assertFalse(nameBookSection.hasProblem());
		assertEquals(strBookSection.replace(" ed.", ", ed."), nameBookSection.getFullTitleCache());
		assertEquals(-1, nameBookSection.getProblemStarts()); 
		assertEquals(-1, nameBookSection.getProblemEnds());
		assertNull(((IBookSection)nameBookSection.getNomenclaturalReference()).getInBook().getDatePublished().getStart());
		assertEquals("1905", ((IBookSection)nameBookSection.getNomenclaturalReference()).getDatePublished().getYear());

		String strXXXs = "Abies alba, Soer der 1987";
		NonViralName<?> problemName = parser.parseReferencedName(strXXXs, null, null);
		assertTrue(problemName.hasProblem());
		list = problemName.getParsingProblems();
		assertTrue("Problem must be name-reference separation", list.contains(ParserProblem.NameReferenceSeparation));
		parser.parseReferencedName(problemName, strBookSection, null, true);
		assertFalse(problemName.hasProblem());
		
		problemName = parser.parseFullName(strXXXs, null, null);
		assertTrue(problemName.hasProblem());
		list = problemName.getParsingProblems();
		assertTrue("Name part must be unparsable", list.contains(ParserProblem.UnparsableNamePart));
		
		
		String testParsable = "Pithecellobium macrostachyum Benth.";
		assertTrue(isParsable(testParsable, ICBN));

		testParsable = "Pithecellobium macrostachyum (Benth.)";
		assertTrue(isParsable(testParsable, ICBN));
		
		testParsable = "Pithecellobium macrostachyum (Benth., 1845)";
		assertTrue(isParsable(testParsable, NomenclaturalCode.ICZN));

		testParsable = "Pithecellobium macrostachyum L., Sp. Pl. 3: n\u00B0 123. 1753."; //00B0 is degree character
		assertTrue(isParsable(testParsable, ICBN));
		
		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.-Eur. 6: 1285. 1929";
		assertTrue("Reference title should support special characters as separators like - and &", isParsable(testParsable, ICBN));
		
		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.&Eur. 6: 1285. 1929";
		assertTrue("Reference title should support special characters as separators like - and &", isParsable(testParsable, ICBN));
		
		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.-Eur.& 6: 1285. 1929";
		assertFalse("Reference title should not support special characters like - and & at the end of the title", isParsable(testParsable, ICBN));
		assertTrue("Problem must be reference title", getProblems(testParsable, ICBN).
				contains(ParserProblem.UnparsableReferenceTitle));
		
		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.:Eur. 6: 1285. 1929";
		assertFalse("Reference title should not support detail separator", isParsable(testParsable, ICBN));
		assertTrue("Problem must be reference title", getProblems(testParsable, ICBN).
				contains(ParserProblem.UnparsableReferenceTitle));

		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
		assertTrue("Reference title should support brackets", isParsable(testParsable, ICBN));

		testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
		assertTrue("Reference title should support brackets", isParsable(testParsable, ICBN));
		
		testParsable = "Hieracium lachenalii Zahn, nom. illeg.";
		assertTrue("Reference should not be obligatory if a nom status exist", isParsable(testParsable, ICBN));
	
		testParsable = "Hieracium lachenalii, nom. illeg.";
		assertTrue("Authorship should not be obligatory if followed by nom status", isParsable(testParsable, ICBN));

		testParsable = "Hieracium lachenalii, Ill. Fl. (Mitt.) 6: 1285. 1929";
		assertFalse("Author is obligatory if followed by reference", isParsable(testParsable, ICBN));
		assertTrue("Problem must be name-reference separation", getProblems(testParsable, ICBN).
				contains(ParserProblem.NameReferenceSeparation));

		testParsable = "Hieracium lachenalii in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
		assertFalse("Author is obligatory if followed by reference", isParsable(testParsable, ICBN));
		assertTrue("Problem must be name-reference separation", getProblems(testParsable, ICBN).
				contains(ParserProblem.NameReferenceSeparation));
		
		testParsable = "Abies alba Mill. var. alba";
		assertTrue("Autonym problem", isParsable(testParsable, ICBN));
		
		testParsable = "Scleroblitum abc Ulbr. in Engler & Prantl, Nat. Pflanzenfam., ed. 2, 16c: 495. 1934.";
		assertTrue("Volume with subdivision", isParsable(testParsable, ICBN));

		
		testParsable = "Hieracium antarcticum d'Urv. in M\u00E9m. Soc. Linn. Paris 4: 608. 1826";
//		testParsable = "Hieracium antarcticum Urv. in M\u00E9m. Soc. Linn. Paris 4: 608. 1826";
		assertTrue("Name with apostrophe is not parsable", isParsable(testParsable, ICBN));

		testParsable = "Cichorium intybus subsp. glaucum (Hoffmanns. & Link) Tzvelev in Komarov, Fl. SSSR 29: 17. 1964";
		assertTrue("Reference containing a word in uppercase is not parsable", isParsable(testParsable, ICBN));

		
	}
	
	
	/**
	 * Test author with name parts van, von, de, de la, d', da, del.
	 * See also http://dev.e-taxonomy.eu/trac/ticket/3373
	 */
	@Test
	public final void  testComposedAuthorNames(){
			
		//van author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		String testParsable = "Aphelocoma unicolor subsp. griscomi van Rossem, 1928"; 
		assertTrue("Author with 'van' should be parsable", isParsable(testParsable, ICZN));

		//von author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi von Rossem, 1928"; 
		assertTrue("Author with 'von' should be parsable", isParsable(testParsable, ICZN));

		//de author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi de Rossem, 1928"; 
		assertTrue("Author with 'de' should be parsable", isParsable(testParsable, ICZN));

		//de la author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi de la Rossem, 1928"; 
		assertTrue("Author with 'de la' should be parsable", isParsable(testParsable, ICZN));

		//d' author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi d'Rossem, 1928"; 
		assertTrue("Author with \"'d'\" should be parsable", isParsable(testParsable, ICZN));

		//da author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi da Rossem, 1928"; 
		assertTrue("Author with 'da' should be parsable", isParsable(testParsable, ICZN));

		//del author (see https://dev.e-taxonomy.eu/trac/ticket/3373)
		testParsable = "Aphelocoma unicolor subsp. griscomi del Rossem, 1928"; 
		assertTrue("Author with 'del' should be parsable", isParsable(testParsable, ICZN));

	}	
	
	
	
	/**
	 * @param testParsable
	 * @param icbn
	 * @return
	 */
	private List<ParserProblem> getProblems(String string, NomenclaturalCode code) {
		List<ParserProblem> result;
		result = parser.parseReferencedName(string, code, null).getParsingProblems();
		return result;
	}

	private boolean isParsable(String string, NomenclaturalCode code){
		NonViralName<?> name = parser.parseReferencedName(string, code, null);
		return ! name.hasProblem();
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
		assertEquals("Sp. Pl.", ref.getTitle());
	}
	
	
	@Test
	public void testNeverEndingParsing(){
		//some full titles result in never ending parsing process https://dev.e-taxonomy.eu/trac/ticket/1556

		String irinaExample = "Milichiidae Sharp, 1899, Insects. Part II. Hymenopteracontinued (Tubulifera and Aculeata), Coleoptera, Strepsiptera, Lepidoptera, Diptera, Aphaniptera, Thysanoptera, Hemiptera, Anoplura 6: 504. 1899";
//		irinaExample = "Milichiidae Sharp, 1899, Insects. Part II. Uiuis Iuiui Hymenopteracontinued (Tubulifera and Aculeata), Coleoptera, Strepsiptera, Lepidoptera, Diptera, Aphaniptera, Thysanoptera, Hemiptera, Anoplura 6: 504. 1899";
		NonViralName<?> nvn = this.parser.parseReferencedName(irinaExample, NomenclaturalCode.ICZN, null);
		int parsingProblem = nvn.getParsingProblem();
		Assert.assertEquals("Name should have only rank warning", 1, parsingProblem);
		Assert.assertEquals("Titlecache", "Milichiidae Sharp, 1899", nvn.getTitleCache());
		Assert.assertEquals("If this line reached everything should be ok", "Milichiidae", nvn.getGenusOrUninomial());
		
		String anotherExample = "Scorzonera hispanica var. brevifolia Boiss. & Balansa in Boissier, Diagn. Pl. Orient., ser. 2 6: 119. 1859.";
		nvn = this.parser.parseReferencedName(anotherExample, ICBN, null);
		parsingProblem = nvn.getParsingProblem();
		Assert.assertEquals("Problem should be 0", 0, parsingProblem);
		Assert.assertEquals("Titlecache", "Scorzonera hispanica var. brevifolia Boiss. & Balansa", nvn.getTitleCache());
		Assert.assertEquals("If this line reached everything should be ok", "Scorzonera", nvn.getGenusOrUninomial());
		
		String unparsable = "Taraxacum nevskii L., Trudy Bot. Inst. Nauk S.S.S.R., Ser. 1, Fl. Sist. Vyssh. Rast. 4: 293. 1937.";
//		String unparsableA = "Taraxacum nevskii L. in Trudy Bot. Inst. Nauk: 293. 1937.";
		nvn = this.parser.parseReferencedName(unparsable, ICBN, null);
		Assert.assertEquals("Titlecache", "Taraxacum nevskii L.", nvn.getTitleCache());
		Assert.assertEquals("If this line reached everything should be ok", "Taraxacum", nvn.getGenusOrUninomial());
		parsingProblem = nvn.getParsingProblem();
		Assert.assertEquals("Name should no warnings or errors", 0, parsingProblem);
		
		String unparsable2 = "Hieracium pxxx Dahlst., Kongl. Svenska Vetensk. Acad. Handl. ser. 2, 26(3): 255. 1894";
//		String unparsable2A = "Hieracium pxxx Dahlst., Kongl Svenska Vetensk Acad Handl, 26: 255. 1894.";
		nvn = this.parser.parseReferencedName(unparsable2, ICBN, null);
		Assert.assertEquals("Titlecache", "Hieracium pxxx Dahlst.", nvn.getTitleCache());
		Assert.assertEquals("If this line reached everything should be ok", "Hieracium", nvn.getGenusOrUninomial());
		parsingProblem = nvn.getParsingProblem();
		Assert.assertEquals("Name should no warnings or errors", 0, parsingProblem);
		
		
		String again = "Adiantum emarginatum Bory ex. Willd., Species Plantarum, ed. 4,5,1: 449,450. 1810";
		nvn = this.parser.parseReferencedName(again, ICBN, null);
		Assert.assertEquals("Titlecache", "Adiantum emarginatum Bory ex Willd.", nvn.getTitleCache());
		Assert.assertEquals("If this line reached everything should be ok", "Adiantum", nvn.getGenusOrUninomial());
		
	}
	
	@Test
	public final void testSeriesPart(){
		Pattern seriesPattern = Pattern.compile(NonViralNameParserImpl.pSeriesPart);
		Matcher matcher = seriesPattern.matcher("ser. 2");
		Assert.assertTrue("", matcher.matches());
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
	
//	@Ignore // please add this test once #2750 is fixed
	@Test
	public final void testNomenclaturalStatus() {
		BotanicalName name = BotanicalName.NewInstance(Rank.FAMILY(), "Acanthopale", null, null, null, null, null, null, null);
		name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
		
		BotanicalName name2 = BotanicalName.NewInstance(Rank.FAMILY());
		
		parser.parseReferencedName(name2, name.getFullTitleCache(),	name2.getRank(), true);
		
		parser.parseReferencedName(name2, name.getFullTitleCache(),	name2.getRank(), true);
		
		Assert.assertEquals("Title cache should be same. No duplication of nom. status should take place", name.getFullTitleCache(), name2.getFullTitleCache());
		
		
	}

}
