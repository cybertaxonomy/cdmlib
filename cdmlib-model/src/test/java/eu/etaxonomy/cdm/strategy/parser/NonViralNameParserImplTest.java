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
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
/**
 * @author a.mueller
 *
 */
public class NonViralNameParserImplTest {
    private static final NomenclaturalCode ICNAFP = NomenclaturalCode.ICNAFP;
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
    final private String strNameZoo3 = "Marmota marmota normalis Ciardelli, 2002";
    final private String strNameZoo4 = "Marmota marmota subsp. normalis Ciardelli, 2002";
    final private String strNameZoo5 = "Marmota marmota var. normalis Ciardelli, 2002";

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
        botanicCode = ICNAFP;
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
        INonViralName name;

//      String strNameWith1AUthorAndCommaSepEditon = "Abies alba Mill., Sp. Pl., ed. 3: 455. 1987";
//      name = parser.parseReferencedName(strNameWith1AUthorAndCommaSepEditon, botanicCode, speciesRank);
//      Assert.assertFalse("No problems should exist", name.hasProblem());
//      Assert.assertEquals("Name should not include reference part", "Abies alba Mill.", name.getTitleCache());
//      Assert.assertEquals("Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());
//
//
//      String strNameWith2Authors = "Abies alba L. & Mill., Sp. Pl., ed. 3: 455. 1987";
//      name = parser.parseReferencedName(strNameWith2Authors, botanicCode, speciesRank);
//      Assert.assertFalse("No problems should exist", name.hasProblem());
//      Assert.assertEquals("Name should not include reference part", "Abies alba L. & Mill.", name.getTitleCache());
//      Assert.assertEquals("Name should have authorteam with 2 authors", 2, ((Team)name.getCombinationAuthorship()).getTeamMembers().size());
//      Assert.assertEquals("L. & Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());

        String strNameWith3Authors = "Abies alba Mess., L. & Mill., Sp. Pl., ed. 3: 455. 1987";
        name = parser.parseReferencedName(strNameWith3Authors, botanicCode, speciesRank);
        Assert.assertFalse("No problems should exist", name.hasProblem());
        Assert.assertEquals("Name should not include reference part", "Abies alba Mess., L. & Mill.", name.getTitleCache());
        Assert.assertEquals("Name should have authorship with 2 authors", 3, ((Team)name.getCombinationAuthorship()).getTeamMembers().size());
        Assert.assertEquals("Mess., L. & Mill., Sp. Pl., ed. 3. 1987", name.getNomenclaturalReference().getTitleCache());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseSimpleName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
     */
    @Test
    public final void testParseSimpleName() {

        //Uninomials
        IZoologicalName milichiidae = (IZoologicalName)parser.parseSimpleName("Milichiidae", NomenclaturalCode.ICZN, null);
        assertEquals("Family rank expected", Rank.FAMILY(), milichiidae.getRank());
        BotanicalName crepidinae = (BotanicalName)parser.parseSimpleName("Crepidinae", ICNAFP, null);
        assertEquals("Family rank expected", Rank.SUBTRIBE(), crepidinae.getRank());
        BotanicalName abies = (BotanicalName)parser.parseSimpleName("Abies", ICNAFP, null);
        assertEquals("Family rank expected", Rank.GENUS(), abies.getRank());

        abies.addParsingProblem(ParserProblem.CheckRank);
        parser.parseSimpleName(abies, "Abies", abies.getRank(), true);
        assertTrue(abies.getParsingProblems().contains(ParserProblem.CheckRank));

        BotanicalName rosa = (BotanicalName)parser.parseSimpleName("Rosaceae", ICNAFP, null);
        assertTrue("Rosaceae have rank family", rosa.getRank().equals(Rank.FAMILY()));
        assertTrue("Rosaceae must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
        parser.parseSimpleName(rosa, "Rosaceaex", abies.getRank(), true);
        assertEquals("Rosaceaex have rank genus", Rank.GENUS(), rosa.getRank());
        assertTrue("Rosaceaex must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));

        //repeat but remove warning after first parse
        rosa = (BotanicalName)parser.parseSimpleName("Rosaceae", ICNAFP, null);
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
        String zooSpeciesWithSubgenus = "Bacanius (Mullerister) rombophorus (Aube, 1843)";
        //zoo as fullName
        IZoologicalName zooName = parser.parseReferencedName(zooSpeciesWithSubgenus, NomenclaturalCode.ICZN, Rank.SPECIES());
        Assert.assertTrue(zooName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", zooName.getInfraGenericEpithet());
        Assert.assertEquals(Integer.valueOf(1843), zooName.getOriginalPublicationYear());
        //zoo as referenced name
        zooName = (ZoologicalName)parser.parseFullName(zooSpeciesWithSubgenus, NomenclaturalCode.ICZN, Rank.SPECIES());
        Assert.assertTrue(zooName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", zooName.getInfraGenericEpithet());
        Assert.assertEquals(Integer.valueOf(1843), zooName.getOriginalPublicationYear());

        //bot as full Name
        String botSpeciesWithSubgenus = "Bacanius (Mullerister) rombophorus (Aube) Mill.";
        BotanicalName botName = (BotanicalName)parser.parseFullName(botSpeciesWithSubgenus, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("Aube", botName.getBasionymAuthorship().getTitleCache());

        //bot as referenced Name
        botName = (BotanicalName)parser.parseReferencedName(botSpeciesWithSubgenus, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("Aube", botName.getBasionymAuthorship().getTitleCache());

        //bot without author
        String botSpeciesWithSubgenusWithoutAuthor = "Bacanius (Mullerister) rombophorus";
        botName = (BotanicalName)parser.parseReferencedName(botSpeciesWithSubgenusWithoutAuthor, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("", botName.getAuthorshipCache());
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

        INonViralName nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameAuthor.getGenusOrUninomial());
        assertEquals("alba", nameAuthor.getSpecificEpithet());
        assertEquals("M\u00FCller", nameAuthor.getCombinationAuthorship().getNomenclaturalTitle());

        INonViralName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
        assertEquals("D'M\u00FCller", nameBasionymAuthor.getCombinationAuthorship().getNomenclaturalTitle());
        INomenclaturalAuthor basionymTeam = nameBasionymAuthor.getBasionymAuthorship();
        assertEquals("Ciardelli", basionymTeam.getNomenclaturalTitle());

        INonViralName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
        assertEquals("D'M\u00FCller", nameBasionymExAuthor.getExCombinationAuthorship().getNomenclaturalTitle());
        assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorship().getNomenclaturalTitle());
        INomenclaturalAuthor basionymTeam2 = nameBasionymExAuthor.getExBasionymAuthorship();
        assertEquals("Ciardelli", basionymTeam2.getNomenclaturalTitle());
        INomenclaturalAuthor exBasionymTeam2 = nameBasionymExAuthor.getBasionymAuthorship();
        assertEquals("D\u00F6ring", exBasionymTeam2.getNomenclaturalTitle());

        BotanicalName nameBasionymExAuthor2 = (BotanicalName)parser.parseFullName("Washingtonia filifera (Linden ex Andre) H.Wendl. ex de Bary", null, Rank.SPECIES());
        assertEquals("Washingtonia", nameBasionymExAuthor2.getGenusOrUninomial());
        assertEquals("filifera", nameBasionymExAuthor2.getSpecificEpithet());
        assertEquals("H.Wendl.", nameBasionymExAuthor2.getExCombinationAuthorship().getNomenclaturalTitle());
        assertEquals("de Bary", nameBasionymExAuthor2.getCombinationAuthorship().getNomenclaturalTitle());
        INomenclaturalAuthor basionymTeam3 = nameBasionymExAuthor2.getBasionymAuthorship();
        assertEquals("Andre", basionymTeam3.getNomenclaturalTitle());
        INomenclaturalAuthor exBasionymTeam3 = nameBasionymExAuthor2.getExBasionymAuthorship();
        assertEquals("Linden", exBasionymTeam3.getNomenclaturalTitle());
        String title = nameBasionymExAuthor2.getTitleCache();
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
        INonViralName nameTeam1 = parser.parseFullName(strNameTeam1);
        assertEquals( "Abies", nameTeam1.getGenusOrUninomial());
        assertEquals( "alba", nameTeam1.getSpecificEpithet());
        assertEquals("Mueller & L.",  nameTeam1.getCombinationAuthorship().getNomenclaturalTitle());
        assertTrue(nameTeam1.getCombinationAuthorship() instanceof Team);
        Team team = (Team)nameTeam1.getCombinationAuthorship();
        assertEquals("Mueller", team.getTeamMembers().get(0).getNomenclaturalTitle());
        assertEquals("L.", team.getTeamMembers().get(1).getNomenclaturalTitle());

        //ZooName
        IZoologicalName nameZoo1 = (IZoologicalName)parser.parseFullName(strNameZoo1);
        assertEquals( "Abies", nameZoo1.getGenusOrUninomial());
        assertEquals( "alba", nameZoo1.getSpecificEpithet());
        assertEquals("Mueller & L.",  nameZoo1.getCombinationAuthorship().getNomenclaturalTitle());
        assertEquals(NomenclaturalCode.ICZN, nameZoo1.getNomenclaturalCode() );
        assertEquals(Integer.valueOf(1822), nameZoo1.getPublicationYear());
        assertTrue(nameZoo1.getCombinationAuthorship() instanceof Team);
        Team teamZoo = (Team)nameZoo1.getCombinationAuthorship();
        assertEquals("Mueller", teamZoo.getTeamMembers().get(0).getNomenclaturalTitle());
        assertEquals("L.", teamZoo.getTeamMembers().get(1).getNomenclaturalTitle());

        IZoologicalName nameZoo2 = (IZoologicalName)parser.parseFullName(strNameZoo2);
        assertEquals(Integer.valueOf(2002), nameZoo2.getPublicationYear());
        assertEquals(Integer.valueOf(1822), nameZoo2.getOriginalPublicationYear());
        assertEquals("Mueller",  nameZoo2.getBasionymAuthorship().getNomenclaturalTitle());
        assertEquals("Ciardelli",  nameZoo2.getCombinationAuthorship().getNomenclaturalTitle());

        //subsp
        IZoologicalName nameZoo3 = (IZoologicalName)parser.parseFullName(strNameZoo3);
        assertEquals("Ciardelli",  nameZoo3.getCombinationAuthorship().getNomenclaturalTitle());
        assertFalse("Subsp. without marker should be parsable", nameZoo3.hasProblem());
        assertEquals("Variety should be recognized", Rank.SUBSPECIES(), nameZoo3.getRank());

        IZoologicalName nameZoo4 = (IZoologicalName)parser.parseFullName(strNameZoo4);
        assertEquals("Ciardelli",  nameZoo4.getCombinationAuthorship().getNomenclaturalTitle());
        assertFalse("Subsp. without marker should be parsable", nameZoo4.hasProblem());
        assertEquals("Variety should be recognized", Rank.SUBSPECIES(), nameZoo4.getRank());

        IZoologicalName nameZoo5 = (IZoologicalName)parser.parseFullName(strNameZoo5);
        assertEquals("Ciardelli",  nameZoo5.getCombinationAuthorship().getNomenclaturalTitle());
        assertFalse("Subsp. without marker should be parsable", nameZoo5.hasProblem());
        assertEquals("Variety should be recognized", Rank.VARIETY(), nameZoo5.getRank());


        //Autonym
        BotanicalName autonymName = (BotanicalName)parser.parseFullName("Abies alba Mill. var. alba", ICNAFP, null);
        assertFalse("Autonym should be parsable", autonymName.hasProblem());


        //empty
        INonViralName nameEmpty = parser.parseFullName(strNameEmpty);
        assertNotNull(nameEmpty);
        assertEquals("", nameEmpty.getTitleCache());

        //null
        INonViralName nameNull = parser.parseFullName(strNameNull);
        assertNull(nameNull);

        //some authors
        String fullNameString = "Abies alba (Greuther & L'Hiver & al. ex M\u00FCller & Schmidt)Clark ex Ciardelli";
        INonViralName authorname = parser.parseFullName(fullNameString);
        assertFalse(authorname.hasProblem());
        assertEquals("Basionym author should have 3 authors", 2, ((Team)authorname.getExBasionymAuthorship()).getTeamMembers().size());
        Assert.assertTrue("ExbasionymAuthorship must have more members'", ((Team)authorname.getExBasionymAuthorship()).isHasMoreMembers());

        //author with 2 capitals
        fullNameString = "Campanula rhodensis A. DC.";
        INonViralName name = parser.parseFullName(fullNameString);
        assertFalse(name.hasProblem());

        //author with no space  #5618
        fullNameString = "Gordonia moaensis (Vict.)H. Keng";
        name = parser.parseFullName(fullNameString);
        assertFalse(name.hasProblem());
        assertNotNull(name.getCombinationAuthorship());
        assertEquals("H. Keng", name.getCombinationAuthorship().getNomenclaturalTitle());

        //name without combination  author  , only to check if above fix for #5618 works correctly
        fullNameString = "Gordonia moaensis (Vict.)";
        name = parser.parseFullName(fullNameString);
        assertFalse(name.hasProblem());
        assertNull(name.getCombinationAuthorship());
        assertNotNull(name.getBasionymAuthorship());
        assertEquals("Vict.", name.getBasionymAuthorship().getNomenclaturalTitle());

    }

    @Test
    public final void testEtAl() throws StringNotParsableException {
        //some authors
        String fullNameString = "Abies alba Greuther, Hiver & al.";
        INonViralName authorname = parser.parseFullName(fullNameString);
        assertFalse(authorname.hasProblem());
        assertEquals("Basionym author should have 2 authors", 2, ((Team)authorname.getCombinationAuthorship()).getTeamMembers().size());
        assertTrue("Basionym author team should have more authors", ((Team)authorname.getCombinationAuthorship()).isHasMoreMembers()  );

        //et al.
        INonViralName nvn = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseAuthors(nvn, "Eckweiler, Hand et al., 2003");
        Team team = (Team)nvn.getCombinationAuthorship();
        Assert.assertNotNull("Comb. author must not be null", team);
        Assert.assertEquals("Must be team with 2 members", 2, team.getTeamMembers().size());
        Assert.assertEquals("Second member must be 'Hand'", "Hand", team.getTeamMembers().get(1).getTitleCache());
        Assert.assertTrue("Team must have more members'", team.isHasMoreMembers());
    }

    @Test
    public final void testMultipleAuthors() {
        //multiple authors for inReference
        String fullTitleString = "Abies alba L. in Mill., Gregor & Behr., Sp. Pl. 173: 384. 1982.";
        INonViralName multipleAuthorRefName = parser.parseReferencedName(fullTitleString, NomenclaturalCode.ICNAFP, Rank.SPECIES());
        assertFalse(multipleAuthorRefName.hasProblem());
        assertTrue("Combination author should be a person", multipleAuthorRefName.getCombinationAuthorship() instanceof Person);
        assertEquals("Combination author should be L.", "L.", ((Person)multipleAuthorRefName.getCombinationAuthorship()).getNomenclaturalTitle());
        IReference nomRef = multipleAuthorRefName.getNomenclaturalReference();
        Assert.assertNotNull("nomRef must have inRef", ((Reference)nomRef).getInReference());
        Reference inRef = ((Reference)nomRef).getInReference();
        String abbrevTitle = inRef.getAbbrevTitle();
        assertEquals("InRef title should be Sp. Pl.", "Sp. Pl.", abbrevTitle);
        assertTrue(inRef.getAuthorship() instanceof Team);
        Team team = (Team)inRef.getAuthorship();
        assertEquals(3, team.getTeamMembers().size());

//        multiple authors in Name
        fullTitleString = "Abies alba Mill., Aber & Schwedt";
        INonViralName multipleAuthorName = parser.parseReferencedName(fullTitleString, NomenclaturalCode.ICNAFP, Rank.SPECIES());
        assertFalse(multipleAuthorName.hasProblem());
        assertTrue("Combination author should be a team", multipleAuthorName.getCombinationAuthorship() instanceof Team);
        team = (Team)multipleAuthorName.getCombinationAuthorship();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Second team member should be Aber", "Aber", team.getTeamMembers().get(1).getTitleCache());

//      multiple authors in Name with reference
        fullTitleString = "Abies alba Mill., Aber & Schwedt in L., Sp. Pl. 173: 384. 1982.";
        multipleAuthorName = parser.parseReferencedName(fullTitleString, NomenclaturalCode.ICNAFP, Rank.SPECIES());
        assertFalse(multipleAuthorName.hasProblem());
        assertTrue("Combination author should be a team", multipleAuthorName.getCombinationAuthorship() instanceof Team);
        team = (Team)multipleAuthorName.getCombinationAuthorship();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Second team member should be Aber", "Aber", team.getTeamMembers().get(1).getTitleCache());
        nomRef = multipleAuthorName.getNomenclaturalReference();
        Assert.assertNotNull("nomRef must have inRef", ((Reference)nomRef).getInReference());
        inRef = ((Reference)nomRef).getInReference();
        abbrevTitle = inRef.getAbbrevTitle();
        assertEquals("InRef title should be Sp. Pl.", "Sp. Pl.", abbrevTitle);
        assertTrue(inRef.getAuthorship() instanceof Person);
        Person person = (Person)inRef.getAuthorship();
        assertEquals("Book author should be L.", "L.", person.getNomenclaturalTitle());


        fullTitleString = "Abies alba Mill., Aber & Schwedt, Sp. Pl. 173: 384. 1982.";
        multipleAuthorName = parser.parseReferencedName(fullTitleString, NomenclaturalCode.ICNAFP, Rank.SPECIES());
        assertFalse(multipleAuthorName.hasProblem());
        assertTrue("Combination author should be a team", multipleAuthorName.getCombinationAuthorship() instanceof Team);
        team = (Team)multipleAuthorName.getCombinationAuthorship();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Second team member should be Aber", "Aber", team.getTeamMembers().get(1).getTitleCache());
        nomRef = multipleAuthorName.getNomenclaturalReference();
        Assert.assertNull("nomRef must not have inRef as it is a book itself", ((Reference)nomRef).getInReference());
        abbrevTitle = nomRef.getAbbrevTitle();
        assertEquals("InRef title should be Sp. Pl.", "Sp. Pl.", abbrevTitle);
        assertTrue(nomRef.getAuthorship() instanceof Team);
        team = (Team)nomRef.getAuthorship();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Second team member should be Schwedt", "Schwedt", team.getTeamMembers().get(2).getTitleCache());

        //et al.
        INonViralName nvn = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseReferencedName (nvn, "Marmota marmota Eckweiler, Hand et al., 2003", Rank.SPECIES(),true);
        assertTrue("Combination author should be a team", nvn.getCombinationAuthorship() instanceof Team);
        team = (Team)nvn.getCombinationAuthorship();
        Assert.assertNotNull("Comb. author must not be null", team);
        Assert.assertEquals("Must be team with 2 members", 2, team.getTeamMembers().size());
        Assert.assertEquals("Second member must be 'Hand'", "Hand", team.getTeamMembers().get(1).getTitleCache());
        Assert.assertTrue("Team must have more members'", team.isHasMoreMembers());

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseFullName(java.lang.String, eu.etaxonomy.cdm.model.name.Rank)}.
     */
    @Test
    public final void testHybrids() {
        INonViralName name1;


        //Infrageneric hybrid
        name1 = parser.parseFullName("Aegilops nothosubg. Insulae Scholz", botanicCode, null);
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must not have trinom hybrid bit set", name1.isTrinomHybrid());
        assertEquals("Infrageneric epithet must be 'Insulae'", "Insulae", name1.getInfraGenericEpithet());

        //Species hybrid
//      INonViralName nameTeam1 = parser.parseFullName("Aegilops \u00D7insulae-cypri H. Scholz");
        name1 = parser.parseFullName("Aegilops \u00D7insulae Scholz", botanicCode, null);
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must not have trinom hybrid bit set", name1.isTrinomHybrid());
        assertEquals("Species epithet must be 'insulae'", "insulae", name1.getSpecificEpithet());

        name1 = parser.parseFullName("Aegilops \u00D7 insulae Scholz", botanicCode, null);
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

        //Subspecies hybrid with hybrid sign
        //maybe false: see http://dev.e-taxonomy.eu/trac/ticket/3868
        name1 = parser.parseFullName("Aegilops insulae subsp. X abies Scholz", botanicCode, null);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertEquals("Infraspecific epithet must be 'abies'", "abies", name1.getInfraSpecificEpithet());

        //Subspecies hybrid with notho / n
        name1 = parser.parseFullName("Aegilops insulae nothosubsp. abies Scholz", botanicCode, null);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertEquals("Infraspecific epithet must be 'abies'", "abies", name1.getInfraSpecificEpithet());

        name1 = parser.parseFullName("Aegilops insulae nsubsp. abies Scholz", botanicCode, null);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertEquals("Infraspecific epithet must be 'abies'", "abies", name1.getInfraSpecificEpithet());

        //
        String nameStr = "Dactylorhiza \u00D7incarnata nothosubsp. versicolor";
        name1 = parser.parseFullName(nameStr);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getTitleCache());  //we expect the cache strategy to create the same result

        nameStr = "Dactylorhiza \u00D7incarnata nothosubsp. versicolor";
        name1 = parser.parseFullName(nameStr);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getTitleCache());  //we expect the cache strategy to create the same result

        //nothovar.
        nameStr = "Dactylorhiza incarnata nothovar. versicolor";
        name1 = parser.parseFullName(nameStr);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertFalse("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getNameCache());  //we expect the cache strategy to create the same result

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
        INonViralName name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Unranked rank should be parsed", Rank.INFRASPECIFICTAXON(), name.getRank());

        //unranked infrageneric
        String infraGenericUnranked = "Genus [unranked] Infragen";
        INonViralName name2 = parser.parseFullName(infraGenericUnranked);
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
        String hybridCache = "Abies alba "+UTF8.HYBRID+" Pinus bus";
        INonViralName name1 = parser.parseFullName(hybridCache, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        assertEquals("Title cache must be correct", hybridCache, name1.getTitleCache());
        List<HybridRelationship> orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        TaxonNameBase<?,?> firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Abies alba as first hybrid parent", "Abies alba", firstParent.getTitleCache());
        TaxonNameBase<?,?> secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Pinus bus as second hybrid parent", "Pinus bus", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lowest rank ('species') as rank", Rank.SPECIES(), name1.getRank());
        assertNull("Name must not have a genus eptithet", name1.getGenusOrUninomial());
        assertNull("Name must not have a specific eptithet", name1.getSpecificEpithet());
        assertFalse("Name must not have parsing problems", name1.hasProblem());

        name1 = parser.parseReferencedName(hybridCache, botanicCode, null);
        assertFalse("Name must not have parsing problems", name1.hasProblem());

        //x-sign
        hybridCache = "Abies alba x Pinus bus";
        name1 = parser.parseFullName(hybridCache, botanicCode, null);
        assertFalse("Name must be parsable", name1.hasProblem());
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertFalse("Name must not have parsing problems", name1.hasProblem());

        //Genus //#6030
        hybridCache = "Orchis "+UTF8.HYBRID+" Platanthera";
        name1 = parser.parseFullName(hybridCache, botanicCode, null);
        assertFalse("Name must be parsable", name1.hasProblem());
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertFalse("Name must not have parsing problems", name1.hasProblem());
        assertEquals("Title cache must be correct", hybridCache, name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis as first hybrid parent", "Orchis", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Platanthera as second hybrid parent", "Platanthera", secondParent.getTitleCache());
        assertEquals("Hybrid name must have genus as rank", Rank.GENUS(), name1.getRank());

        name1 = parser.parseReferencedName(hybridCache, botanicCode, null);
        assertFalse("Name must not have parsing problems", name1.hasProblem());

        //Subspecies first hybrid
        name1 = parser.parseFullName("Abies alba subsp. beta "+UTF8.HYBRID+" Pinus bus", botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        assertEquals("Title cache must be correct", "Abies alba subsp. beta "+UTF8.HYBRID+" Pinus bus", name1.getTitleCache());
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

        //hybrids with authors  //happens but questionable
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

        //abbreviated genus hybrid formula #6410 / #5983
        String nameStr = "Nepenthes mirabilis \u00D7 N. alata";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Nepenthes mirabilis \u00D7 Nepenthes alata", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        //to be discussed as usually they should be ordered alphabetically
        assertEquals("Name must have Nepenthes mirabilis as first hybrid parent", "Nepenthes mirabilis", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Nepenthes alata as second hybrid parent", "Nepenthes alata", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('species') as rank", Rank.SPECIES(), name1.getRank());

        //missing genus hybrid formula #5983
        nameStr = "Nepenthes mirabilis \u00D7 alata";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Nepenthes mirabilis \u00D7 Nepenthes alata", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        //to be discussed as usually they should be ordered alphabetically
        assertEquals("Name must have Nepenthes mirabilis as first hybrid parent", "Nepenthes mirabilis", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Nepenthes alata as second hybrid parent", "Nepenthes alata", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('species') as rank", Rank.SPECIES(), name1.getRank());

        //#5983 subsp. with species and missing genus
        nameStr = "Orchis coriophora subsp. fragrans \u00D7 sancta";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis coriophora subsp. fragrans \u00D7 Orchis sancta", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis coriophora subsp. fragrans as first hybrid parent", "Orchis coriophora subsp. fragrans", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis sancta as second hybrid parent", "Orchis sancta", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('subspecies') as rank", Rank.SUBSPECIES(), name1.getRank());

        //2 subspecies with missing genus part #5983
        nameStr = "Orchis morio subsp. syriaca \u00D7 papilionacea subsp. schirvanica";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Orchis papilionacea subsp. schirvanica", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis morio subsp. syriaca as first hybrid parent", "Orchis morio subsp. syriaca", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis papilionacea subsp. schirvanica as second hybrid parent", "Orchis papilionacea subsp. schirvanica", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('subspecies') as rank", Rank.SUBSPECIES(), name1.getRank());

        //subspecies and variety with missing genus part
        nameStr = "Orchis morio subsp. syriaca \u00D7 papilionacea var. schirvanica";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Orchis papilionacea var. schirvanica", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis morio subsp. syriaca as first hybrid parent", "Orchis morio subsp. syriaca", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis papilionacea var. schirvanica as second hybrid parent", "Orchis papilionacea var. schirvanica", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('variety') as rank", Rank.VARIETY(), name1.getRank());

        //2 subspecies with missing genus and species part #5983
        nameStr = "Orchis morio subsp. syriaca \u00D7 subsp. schirvanica";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Orchis morio subsp. schirvanica", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis morio subsp. syriaca as first hybrid parent", "Orchis morio subsp. syriaca", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis morio subsp. schirvanica as second hybrid parent", "Orchis morio subsp. schirvanica", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('subspecies') as rank", Rank.SUBSPECIES(), name1.getRank());

        //subspecies and variety with missing genus and species part #5983
        nameStr = "Orchis morio subsp. syriaca \u00D7 var. schirvanica";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Orchis morio var. schirvanica", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis morio subsp. syriaca as first hybrid parent", "Orchis morio subsp. syriaca", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis morio subsp. schirvanica as second hybrid parent", "Orchis morio var. schirvanica", secondParent.getTitleCache());
        assertEquals("Hybrid name must have the lower rank ('variety') as rank", Rank.VARIETY(), name1.getRank());


    }

//    @Test
//    public final void testTemp(){
////        String nalata = "N. alata";
////        if (! nalata.matches(NonViralNameParserImplRegExBase.abbrevHybridSecondPart)){
////            throw new RuntimeException();
////        }
//
//        //abbreviated hybrid formula #6410
//        String nameStr = "Orchis morio subsp. syriaca \u00D7 papilionacea subsp. schirvanica";
//        INonViralName name1 = parser.parseFullName(nameStr, botanicCode, null);
//        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
//        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
//        //could also be N. or no genus at all, depends on formatter
//        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Orchis papilionacea subsp. schirvanica", name1.getTitleCache());
//    }


    @Test
    public final void testHybridsRemoval(){
        //if the parser input already has hybridrelationships they need to be removed
        //Create input
        String hybridCache = "Abies alba "+UTF8.HYBRID+" Pinus bus";
        INonViralName name1 = parser.parseFullName(hybridCache, botanicCode, null);
        assertFalse("Name must not have parsing problems", name1.hasProblem());
        assertTrue("", name1.getHybridChildRelations().size() == 2);

        hybridCache = "Abieta albana "+UTF8.HYBRID+" Pinuta custa";
        boolean makeEmpty = true;
        parser.parseFullName(name1, hybridCache, Rank.SPECIES(), makeEmpty);
        assertEquals("After parsing another string there should still be 2 parents, but different ones", 2, name1.getHybridChildRelations().size());
        assertFalse("Name must not have parsing problems", name1.hasProblem());


        hybridCache = "Calendula arvensis Mill.";
        makeEmpty = true;
        parser.parseFullName(name1, hybridCache, Rank.SPECIES(), makeEmpty);
        assertTrue("", name1.getHybridChildRelations().isEmpty());
        assertFalse("Name must not have parsing problems", name1.hasProblem());


        //AND the same for reference parsing
        hybridCache = "Abies alba "+UTF8.HYBRID+" Pinus bus";
        name1 = parser.parseReferencedName(hybridCache, botanicCode, null);
        assertFalse("Name must not have parsing problems", name1.hasProblem());
        assertTrue("", name1.getHybridChildRelations().size() == 2);

        hybridCache = "Abieta albana "+UTF8.HYBRID+" Pinuta custa";
        makeEmpty = true;
        parser.parseReferencedName(name1, hybridCache, Rank.SPECIES(), makeEmpty);
        assertEquals("After parsing another string there should still be 2 parents, but different ones", 2, name1.getHybridChildRelations().size());
        assertFalse("Name must not have parsing problems", name1.hasProblem());


        hybridCache = "Calendula arvensis Mill.";
        makeEmpty = true;
        parser.parseReferencedName(name1, hybridCache, Rank.SPECIES(), makeEmpty);
        assertTrue("", name1.getHybridChildRelations().isEmpty());
        assertFalse("Name must not have parsing problems", name1.hasProblem());
    }

    private void testName_StringNomcodeRank(Method parseMethod)
            throws InvocationTargetException, IllegalAccessException  {
        INonViralName name1 = (INonViralName)parseMethod.invoke(parser, strNameAbies1, null, Rank.SPECIES());
        //parser.parseFullName(strNameAbies1, null, Rank.SPECIES());
        assertEquals("Abies", name1.getGenusOrUninomial());
        assertEquals("alba", name1.getSpecificEpithet());

        INonViralName nameAuthor = (INonViralName)parseMethod.invoke(parser, strNameAbiesAuthor1, null, Rank.SPECIES());
        assertEquals("Abies", nameAuthor.getGenusOrUninomial());
        assertEquals("alba", nameAuthor.getSpecificEpithet());
        assertEquals("Mueller", nameAuthor.getCombinationAuthorship().getNomenclaturalTitle());

        INonViralName nameBasionymAuthor = (INonViralName)parseMethod.invoke(parser, strNameAbiesBasionymAuthor1, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
        assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorship().getNomenclaturalTitle());
        assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorship().getNomenclaturalTitle());

        INonViralName nameBasionymExAuthor = (INonViralName)parseMethod.invoke(parser, strNameAbiesBasionymExAuthor1, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
        assertEquals("D'Mueller", nameBasionymExAuthor.getExCombinationAuthorship().getNomenclaturalTitle());
        assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorship().getNomenclaturalTitle());
        assertEquals("Ciardelli", nameBasionymExAuthor.getExBasionymAuthorship().getNomenclaturalTitle());
        assertEquals("Doering", nameBasionymExAuthor.getBasionymAuthorship().getNomenclaturalTitle());

        INonViralName name2 = (INonViralName)parseMethod.invoke(parser, strNameAbiesSub1, null, Rank.SPECIES());
        assertEquals("Abies", name2.getGenusOrUninomial());
        assertEquals("alba", name2.getSpecificEpithet());
        assertEquals("beta", name2.getInfraSpecificEpithet());
        assertEquals(Rank.SUBSPECIES(), name2.getRank());


        // unparseable *********
        String problemString = "sdfjlös wer eer wer";
        INonViralName nameProblem = (INonViralName)parseMethod.invoke(parser, problemString, null, Rank.SPECIES());
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
        INonViralName nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertFullRefStandard(nameTestStatus);
        assertTrue(nameTestStatus.getStatus().size()== 1);
        assertEquals( NomenclaturalStatusType.AMBIGUOUS(), nameTestStatus.getStatus().iterator().next().getType());

        //nom. inval.
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. inval.";
        nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertFullRefStandard(nameTestStatus);
        assertTrue(nameTestStatus.getStatus().size()== 1);
        assertEquals( NomenclaturalStatusType.INVALID(), nameTestStatus.getStatus().iterator().next().getType());

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

        //nom. cons. prop.
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. cons. des.";
        nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertFullRefStandard(nameTestStatus);
        assertTrue(nameTestStatus.getStatus().size()== 1);
        assertEquals( NomenclaturalStatusType.CONSERVED_DESIG(), nameTestStatus.getStatus().iterator().next().getType());

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

        //comb. nov.
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, comb. nov.";
        nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertFullRefStandard(nameTestStatus);
        assertTrue(nameTestStatus.getStatus().size()== 1);
        assertEquals( NomenclaturalStatusType.COMB_NOV(), nameTestStatus.getStatus().iterator().next().getType());

        //orth. rej.
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, orth. rej.";
        nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertFullRefStandard(nameTestStatus);
        assertTrue(nameTestStatus.getStatus().size()== 1);
        assertEquals( NomenclaturalStatusType.ORTHOGRAPHY_REJECTED(), nameTestStatus.getStatus().iterator().next().getType());

        //ined.
        strTestStatus = "Houstonia macvaughii (Terrell), ined.";
        nameTestStatus = parser.parseReferencedName(strTestStatus, null, Rank.SPECIES());
        assertEquals("Houstonia", nameTestStatus.getGenusOrUninomial());
        assertEquals("macvaughii", nameTestStatus.getSpecificEpithet());
        assertEquals("(Terrell)", nameTestStatus.getAuthorshipCache());
        assertEquals(1, nameTestStatus.getStatus().size());
        assertEquals( NomenclaturalStatusType.INED(), nameTestStatus.getStatus().iterator().next().getType());

        //not yet parsed "not avail."
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
        INonViralName nameNull = parser.parseReferencedName(strNull, null, rankSpecies);
        assertNull(nameNull);

        //Empty
        String strEmpty = "";
        INonViralName nameEmpty = parser.parseReferencedName(strEmpty, null, rankSpecies);
        assertFalse(nameEmpty.hasProblem());
        assertEquals(strEmpty, nameEmpty.getFullTitleCache());
        assertNull(nameEmpty.getNomenclaturalMicroReference());


        //Whitespaces
        String strFullWhiteSpcaceAndDot = "Abies alba Mill.,  Sp.   Pl.  4:  455 .  1987 .";
        INonViralName namefullWhiteSpcaceAndDot = parser.parseReferencedName(strFullWhiteSpcaceAndDot, null, rankSpecies);
        assertFullRefStandard(namefullWhiteSpcaceAndDot);
        assertTrue(((Reference)namefullWhiteSpcaceAndDot.getNomenclaturalReference()).getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
        assertEquals( "Abies alba Mill., Sp. Pl. 4: 455. 1987", namefullWhiteSpcaceAndDot.getFullTitleCache());

        //Book
        String fullReference = "Abies alba Mill., Sp. Pl. 4: 455. 1987";
        INonViralName name1 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFullRefStandard(name1);
        assertTrue(((Reference)name1.getNomenclaturalReference()).getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
        assertEquals(fullReference, name1.getFullTitleCache());
        assertTrue("Name author and reference author should be the same", name1.getCombinationAuthorship() == ((Reference)name1.getNomenclaturalReference()).getAuthorship());

        //Book Section
        fullReference = "Abies alba Mill. in Otto, Sp. Pl. 4(6): 455. 1987";
        INonViralName name2 = parser.parseReferencedName(fullReference + ".", null, rankSpecies);
        assertFullRefNameStandard(name2);
        assertEquals(fullReference, name2.getFullTitleCache());
        assertFalse(name2.hasProblem());
        INomenclaturalReference ref = name2.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, ((Reference)ref).getType());
        IBookSection bookSection = (IBookSection) ref;
        IBook inBook = bookSection.getInBook();
        assertNotNull(inBook);
        assertNotNull(inBook.getAuthorship());
        assertEquals("Otto", inBook.getAuthorship().getTitleCache());
        assertEquals("Otto, Sp. Pl. 4(6)", inBook.getTitleCache());
        assertEquals("Sp. Pl.", inBook.getAbbrevTitle());
        assertEquals("4(6)", inBook.getVolume());
        assertTrue("Name author and reference author should be the same", name2.getCombinationAuthorship() == ((Reference)name2.getNomenclaturalReference()).getAuthorship());

        //Article
        fullReference = "Abies alba Mill. in Sp. Pl. 4(6): 455. 1987";
        INonViralName name3 = parser.parseReferencedName(fullReference, null, rankSpecies);
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
        assertEquals("Sp. Pl.",((Reference) journal).getTitleCache());
        assertEquals("Sp. Pl.", journal.getAbbrevTitle());
        assertEquals("4(6)",((IArticle)ref).getVolume());
        assertTrue("Name author and reference author should be the same", name3.getCombinationAuthorship() == name3.getNomenclaturalReference().getAuthorship());

        //Article with volume range
        fullReference = "Abies alba Mill. in Sp. Pl. 4(1-2): 455. 1987";
        INonViralName name3a = parser.parseReferencedName(fullReference, null, rankSpecies);
        name3a.setTitleCache(null);
        assertEquals(fullReference, name3a.getFullTitleCache());
        assertFalse(name3a.hasProblem());
        ref = name3a.getNomenclaturalReference();
        assertEquals(eu.etaxonomy.cdm.model.reference.ReferenceType.Article, ref.getType());
        assertEquals("4(1-2)",((IArticle)ref).getVolume());

        //SoftArticle - having "," on position > 4
        String journalTitle = "Bull. Soc. Bot.France. Louis., Roi";
        String yearPart = " 1987 - 1989";
        String parsedYear = "1987-1989";
        String fullReferenceWithoutYear = "Abies alba Mill. in " + journalTitle + " 4(6): 455.";
        fullReference = fullReferenceWithoutYear + yearPart;
        String fullReferenceWithEnd = fullReference + ".";
        INonViralName name4 = parser.parseReferencedName(fullReferenceWithEnd, null, rankSpecies);
        assertFalse(name4.hasProblem());
        assertFullRefNameStandard(name4);
        assertEquals(fullReferenceWithoutYear + " " + parsedYear, name4.getFullTitleCache());
        ref = name4.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, ref.getType());
        //article = (Article)ref;
        assertEquals(parsedYear, ref.getYear());
        journal = ((IArticle)ref).getInJournal();
        assertNotNull(journal);
        assertEquals(journalTitle, ((Reference) journal).getTitleCache());
        assertEquals(journalTitle, journal.getAbbrevTitle());
        assertEquals("4(6)", ((IArticle)ref).getVolume());

        //Zoo name
        String strNotParsableZoo = "Abies alba M., 1923, Sp. P. xxwer4352, nom. inval.";
        IZoologicalName nameZooRefNotParsabel = parser.parseReferencedName(strNotParsableZoo, null, null);
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
        IZoologicalName nameZooNameSineYear = parser.parseReferencedName(strZooNameSineYear);
        assertFalse(nameZooNameSineYear.hasProblem());
        assertEquals("Name without reference year must have year", (Integer)1758, nameZooNameSineYear.getPublicationYear());
        assertEquals("Name without reference year must have year", "1758", nameZooNameSineYear.getNomenclaturalReference().getYear());

        String strZooNameNewCombination = "Homo sapiens (L., 1758) Mill., 1830, Sp. An. 3: 345";
        IZoologicalName nameZooNameNewCombination = parser.parseReferencedName(strZooNameNewCombination);
        assertTrue(nameZooNameNewCombination.hasProblem());
        list = nameZooNameNewCombination.getParsingProblems();
        assertTrue("List must contain new combination has publication warning ", list.contains(ParserProblem.NewCombinationHasPublication));
        assertEquals(35, nameZooNameNewCombination.getProblemStarts());
        assertEquals(51, nameZooNameNewCombination.getProblemEnds());


        //Special MicroRefs
        String strSpecDetail1 = "Abies alba Mill. in Sp. Pl. 4(6): [455]. 1987";
        INonViralName nameSpecDet1 = parser.parseReferencedName(strSpecDetail1 + ".", null, rankSpecies);
        assertFalse(nameSpecDet1.hasProblem());
        assertEquals(strSpecDetail1, nameSpecDet1.getFullTitleCache());
        assertEquals("[455]", nameSpecDet1.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail2 = "Abies alba Mill. in Sp. Pl. 4(6): couv. 2. 1987";
        INonViralName nameSpecDet2 = parser.parseReferencedName(strSpecDetail2 + ".", null, rankSpecies);
        assertFalse(nameSpecDet2.hasProblem());
        assertEquals(strSpecDetail2, nameSpecDet2.getFullTitleCache());
        assertEquals("couv. 2", nameSpecDet2.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail3 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455. 1987";
        INonViralName nameSpecDet3 = parser.parseReferencedName(strSpecDetail3 + ".", null, rankSpecies);
        assertFalse(nameSpecDet3.hasProblem());
        assertEquals(strSpecDetail3, nameSpecDet3.getFullTitleCache());
        assertEquals("fig. 455", nameSpecDet3.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail4 = "Abies alba Mill. in Sp. Pl. 4(6): fig. 455-567. 1987";
        fullReference = strSpecDetail4 + ".";
        INonViralName nameSpecDet4 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFalse(nameSpecDet4.hasProblem());
        assertEquals(strSpecDetail4, nameSpecDet4.getFullTitleCache());
        assertEquals("fig. 455-567", nameSpecDet4.getNomenclaturalMicroReference());


        //Special MicroRefs
        String strSpecDetail5 = "Abies alba Mill. in Sp. Pl. 4(6): Gard n\u00B0 4. 1987";
        fullReference = strSpecDetail5 + ".";
        INonViralName nameSpecDet5 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFalse(nameSpecDet5.hasProblem());
        assertEquals(strSpecDetail5, nameSpecDet5.getFullTitleCache());
        assertEquals("Gard n\u00B0 4", nameSpecDet5.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail6 = "Abies alba Mill. in Sp. Pl. 4(6): 455a. 1987";
        fullReference = strSpecDetail6 + ".";
        INonViralName nameSpecDet6 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFalse(nameSpecDet6.hasProblem());
        assertEquals(strSpecDetail6, nameSpecDet6.getFullTitleCache());
        assertEquals("455a", nameSpecDet6.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail7 = "Abies alba Mill. in Sp. Pl. 4(6): pp.455-457. 1987";
        fullReference = strSpecDetail7 + ".";
        INonViralName nameSpecDet7 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFalse(nameSpecDet7.hasProblem());
        assertEquals(strSpecDetail7, nameSpecDet7.getFullTitleCache());
        assertEquals("pp.455-457", nameSpecDet7.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail8 = "Abies alba Mill. in Sp. Pl. 4(6): ppp.455-457. 1987";
        INonViralName nameSpecDet8 = parser.parseReferencedName(strSpecDetail8, null, rankSpecies);
        assertTrue(nameSpecDet8.hasProblem());
        assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
        assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after -457


        //Special MicroRefs
        String strSpecDetail9 = "Abies alba Mill. in Sp. Pl. 4(6): pp. 455 - 457. 1987";
        INonViralName nameSpecDet9 = parser.parseReferencedName(strSpecDetail9, null, rankSpecies);
        assertFalse(nameSpecDet9.hasProblem());
        assertEquals(strSpecDetail9, nameSpecDet9.getFullTitleCache());
        assertEquals("pp. 455 - 457", nameSpecDet9.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail10 = "Abies alba Mill. in Sp. Pl. 4(6): p 455. 1987";
        INonViralName nameSpecDet10 = parser.parseReferencedName(strSpecDetail10, null, rankSpecies);
        assertFalse(nameSpecDet10.hasProblem());
        assertEquals(strSpecDetail10, nameSpecDet10.getFullTitleCache());
        assertEquals("p 455", nameSpecDet10.getNomenclaturalMicroReference());

        //Special MicroRefs
        String strSpecDetail11 = "Abies alba Mill. in Sp. Pl. 4(6): p. 455 - 457. 1987";
        INonViralName nameSpecDet11 = parser.parseReferencedName(strSpecDetail11, null, rankSpecies);
        assertTrue(nameSpecDet11.hasProblem());
        list = nameSpecDet11.getParsingProblems();
        assertTrue("Problem is Detail. Must be pp.", list.contains(ParserProblem.CheckDetailOrYear));
        assertEquals(20, nameSpecDet8.getProblemStarts()); //TODO better start behind :
        assertEquals(51, nameSpecDet8.getProblemEnds());   //TODO better stop after - 457


        //no volume, no edition
        String strNoVolume = "Abies alba Mill., Sp. Pl.: 455. 1987";
        INonViralName nameNoVolume = parser.parseReferencedName(strNoVolume, null, rankSpecies);
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
        INonViralName nameUnparsableInRef = parser.parseReferencedName(strUnparsableInRef, null, rankSpecies);
        assertTrue(nameUnparsableInRef.hasProblem());
        list = nameUnparsableInRef.getParsingProblems();
        assertTrue("Unparsable title", list.contains(ParserProblem.UnparsableReferenceTitle));
        assertEquals(strUnparsableInRef, nameUnparsableInRef.getFullTitleCache());
        assertEquals(20, nameUnparsableInRef.getProblemStarts());
        assertEquals(25, nameUnparsableInRef.getProblemEnds());


        //volume, edition
        String strNoSeparator = "Abies alba Mill. Sp. Pl. ed. 3, 4(5): 455. 1987";
        INonViralName nameNoSeparator = parser.parseReferencedName(strNoSeparator, ICNAFP, rankSpecies);
        assertTrue(nameNoSeparator.hasProblem());
        list = nameNoSeparator.getParsingProblems();
        assertTrue("Problem is missing name-reference separator", list.contains(ParserProblem.NameReferenceSeparation));
        assertEquals(strNoSeparator, nameNoSeparator.getFullTitleCache());
        assertEquals(10, nameNoSeparator.getProblemStarts()); //TODO better start behind Mill. (?)
        assertEquals(47, nameNoSeparator.getProblemEnds());   //TODO better stop before :

        String strUnparsableInRef2 = "Hieracium pepsicum L., My Bookkkk 1. 1903";
        INonViralName nameUnparsableInRef2 = parser.parseReferencedName(strUnparsableInRef2, null, rankSpecies);
        assertTrue(nameUnparsableInRef2.hasProblem());
        list = nameUnparsableInRef2.getParsingProblems();
        assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
        assertEquals(strUnparsableInRef2, nameUnparsableInRef2.getFullTitleCache());
        assertEquals(23, nameUnparsableInRef2.getProblemStarts());
        assertEquals(41, nameUnparsableInRef2.getProblemEnds());


        String strUnparsableInRef3 = "Hieracium pespcim N., My Bookkkk 1. 1902";
        INonViralName nameUnparsableInRef3 = parser.parseReferencedName(strUnparsableInRef3, null, null);
        assertTrue(nameUnparsableInRef3.hasProblem());
        list = nameUnparsableInRef3.getParsingProblems();
        assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
        assertEquals(strUnparsableInRef3, nameUnparsableInRef3.getFullTitleCache());
        assertEquals(22, nameUnparsableInRef3.getProblemStarts());
        assertEquals(40, nameUnparsableInRef3.getProblemEnds());

        String strUnparsableInRef4 = "Hieracium pepsicum (Hsllreterto) L., My Bookkkk 1. 1903";
        INonViralName nameUnparsableInRef4 = parser.parseReferencedName(strUnparsableInRef4, null, null);
        assertTrue(nameUnparsableInRef4.hasProblem());
        list = nameUnparsableInRef4.getParsingProblems();
        assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
        assertEquals(strUnparsableInRef4, nameUnparsableInRef4.getFullTitleCache());
        assertEquals(37, nameUnparsableInRef4.getProblemStarts());
        assertEquals(55, nameUnparsableInRef4.getProblemEnds());

        String strSameName = "Hieracium pepcum (Hsllreterto) L., My Bokkk 1. 1903";
        INonViralName nameSameName = nameUnparsableInRef4;
        parser.parseReferencedName(nameSameName, strSameName, null, true);
        assertTrue(nameSameName.hasProblem());
        list = nameSameName.getParsingProblems();
        assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
        assertEquals(strSameName, nameSameName.getFullTitleCache());
        assertEquals(35, nameSameName.getProblemStarts());
        assertEquals(51, nameSameName.getProblemEnds());

        String strGenusUnparse = "Hieracium L., jlklk";
        INonViralName nameGenusUnparse =
            parser.parseReferencedName(strGenusUnparse, null, null);
        assertTrue(nameGenusUnparse.hasProblem());
        list = nameGenusUnparse.getParsingProblems();
        assertTrue("Problem detail", list.contains(ParserProblem.CheckDetailOrYear));
        assertTrue("Problem uninomial", list.contains(ParserProblem.CheckRank));
        assertEquals(strGenusUnparse, nameGenusUnparse.getFullTitleCache());
        assertEquals(0, nameGenusUnparse.getProblemStarts());
        assertEquals(19, nameGenusUnparse.getProblemEnds());

        String strGenusUnparse2 = "Hieracium L., Per Luigi: 44. 1987";
        INonViralName nameGenusUnparse2 =
            parser.parseReferencedName(strGenusUnparse2, null, Rank.FAMILY());
        assertFalse(nameGenusUnparse2.hasProblem());
        assertEquals(strGenusUnparse2, nameGenusUnparse2.getFullTitleCache());
        assertEquals(-1, nameGenusUnparse2.getProblemStarts());
        assertEquals(-1, nameGenusUnparse2.getProblemEnds());

        String strBookSection2 = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz, ed. 2, 2: 288. 1905-1907";
        String strBookSection2NoComma = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz ed. 2, 2: 288. 1905-1907";
        INonViralName nameBookSection2 =
            parser.parseReferencedName(strBookSection2, null, null);
        assertFalse(nameBookSection2.hasProblem());
        nameBookSection2.setFullTitleCache(null, false);
        assertEquals(strBookSection2NoComma.replace(" ed.", ", ed."), nameBookSection2.getFullTitleCache());
        assertEquals(-1, nameBookSection2.getProblemStarts());
        assertEquals(-1, nameBookSection2.getProblemEnds());
        assertNull((nameBookSection2.getNomenclaturalReference()).getDatePublished().getStart());
        assertEquals("1905-1907", ((IBookSection)nameBookSection2.getNomenclaturalReference()).getInBook().getDatePublished().getYear());


        String strBookSection = "Hieracium vulgatum subsp. acuminatum (Jord.) Zahn in Schinz & Keller, Fl. Schweiz ed. 2, 2: 288. 1905";
        INonViralName nameBookSection =
            parser.parseReferencedName(strBookSection, null, null);
        assertFalse(nameBookSection.hasProblem());
        assertEquals(strBookSection.replace(" ed.", ", ed."), nameBookSection.getFullTitleCache());
        assertEquals(-1, nameBookSection.getProblemStarts());
        assertEquals(-1, nameBookSection.getProblemEnds());
        assertNull(((IBookSection)nameBookSection.getNomenclaturalReference()).getInBook().getDatePublished().getStart());
        assertEquals("1905", ((IBookSection)nameBookSection.getNomenclaturalReference()).getDatePublished().getYear());

        String strXXXs = "Abies alba, Soer der 1987";
        INonViralName problemName = parser.parseReferencedName(strXXXs, null, null);
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
        assertTrue(isParsable(testParsable, ICNAFP));

        testParsable = "Pithecellobium macrostachyum (Benth.)";
        assertTrue(isParsable(testParsable, ICNAFP));

        testParsable = "Pithecellobium macrostachyum (Benth., 1845)";
        assertTrue(isParsable(testParsable, NomenclaturalCode.ICZN));

        testParsable = "Pithecellobium macrostachyum L., Sp. Pl. 3: n\u00B0 123. 1753."; //00B0 is degree character
        assertTrue(isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.-Eur. 6: 1285. 1929";
        assertTrue("Reference title should support special characters as separators like - and &", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.&Eur. 6: 1285. 1929";
        assertTrue("Reference title should support special characters as separators like - and &", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.-Eur.& 6: 1285. 1929";
        assertFalse("Reference title should not support special characters like - and & at the end of the title", isParsable(testParsable, ICNAFP));
        assertTrue("Problem must be reference title", getProblems(testParsable, ICNAFP).
                contains(ParserProblem.UnparsableReferenceTitle));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. Mitt.:Eur. 6: 1285. 1929";
        assertFalse("Reference title should not support detail separator", isParsable(testParsable, ICNAFP));
        assertTrue("Problem must be reference title", getProblems(testParsable, ICNAFP).
                contains(ParserProblem.UnparsableReferenceTitle));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
        assertTrue("Reference title should support brackets", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii subsp. acuminatum (Jord.) Zahn in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
        assertTrue("Reference title should support brackets", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii Zahn, nom. illeg.";
        assertTrue("Reference should not be obligatory if a nom status exist", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii, nom. illeg.";
        assertTrue("Authorship should not be obligatory if followed by nom status", isParsable(testParsable, ICNAFP));

        testParsable = "Hieracium lachenalii, Ill. Fl. (Mitt.) 6: 1285. 1929";
        assertFalse("Author is obligatory if followed by reference", isParsable(testParsable, ICNAFP));
        assertTrue("Problem must be name-reference separation", getProblems(testParsable, ICNAFP).
                contains(ParserProblem.NameReferenceSeparation));

        testParsable = "Hieracium lachenalii in Hegi, Ill. Fl. (Mitt.) 6: 1285. 1929";
        assertFalse("Author is obligatory if followed by reference", isParsable(testParsable, ICNAFP));
        assertTrue("Problem must be name-reference separation", getProblems(testParsable, ICNAFP).
                contains(ParserProblem.NameReferenceSeparation));

        testParsable = "Abies alba Mill. var. alba";
        assertTrue("Autonym problem", isParsable(testParsable, ICNAFP));

        testParsable = "Scleroblitum abc Ulbr. in Engler & Prantl, Nat. Pflanzenfam., ed. 2, 16c: 495. 1934.";
        assertTrue("Volume with subdivision", isParsable(testParsable, ICNAFP));


        testParsable = "Hieracium antarcticum d'Urv. in M\u00E9m. Soc. Linn. Paris 4: 608. 1826";
//      testParsable = "Hieracium antarcticum Urv. in M\u00E9m. Soc. Linn. Paris 4: 608. 1826";
        assertTrue("Name with apostrophe is not parsable", isParsable(testParsable, ICNAFP));

        testParsable = "Cichorium intybus subsp. glaucum (Hoffmanns. & Link) Tzvelev in Komarov, Fl. SSSR 29: 17. 1964";
        assertTrue("Reference containing a word in uppercase is not parsable", isParsable(testParsable, ICNAFP));


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

        //O' author (see https://dev.e-taxonomy.eu/trac/ticket/4759)
        testParsable = "Aphelocoma unicolor subsp. griscomi O'Connor, 1928";
        assertTrue("Author with 'O'' should be parsable", isParsable(testParsable, ICZN));

        //del author (see https://dev.e-taxonomy.eu/trac/ticket/4759)
        testParsable = "Aphelocoma unicolor subsp. griscomi zur Strassen, 1928";
        assertTrue("Author with 'zur' should be parsable", isParsable(testParsable, ICZN));

    }



    /**
     * @param testParsable
     * @param icbn
     * @return
     */
    private List<ParserProblem> getProblems(String string, NomenclaturalCode code) {
        List<ParserProblem> result = parser.parseReferencedName(string, code, null).getParsingProblems();
        return result;
    }

    private boolean isParsable(String string, NomenclaturalCode code){
        INonViralName name = parser.parseReferencedName(string, code, null);
        return ! name.hasProblem();
    }

    private void assertFullRefNameStandard(INonViralName name){
        assertEquals("Abies", name.getGenusOrUninomial());
        assertEquals("alba", name.getSpecificEpithet());
        assertEquals("Mill.", name.getAuthorshipCache());
        assertEquals("455", name.getNomenclaturalMicroReference());
        assertNotNull(name.getNomenclaturalReference());
    }

    private void assertFullRefStandard(INonViralName name){
        assertEquals("Abies", name.getGenusOrUninomial());
        assertEquals("alba", name.getSpecificEpithet());
        assertEquals("Mill.", name.getAuthorshipCache());
        assertEquals("455", name.getNomenclaturalMicroReference());
        assertNotNull(name.getNomenclaturalReference());
        INomenclaturalReference ref = name.getNomenclaturalReference();
        assertEquals("1987", ref.getYear());
        assertEquals("Sp. Pl.", ref.getAbbrevTitle());
    }


    @Test
    public void testNeverEndingParsing(){
        //some full titles result in never ending parsing process https://dev.e-taxonomy.eu/trac/ticket/1556

        String irinaExample = "Milichiidae Sharp, 1899, Insects. Part II. Hymenopteracontinued (Tubulifera and Aculeata), Coleoptera, Strepsiptera, Lepidoptera, Diptera, Aphaniptera, Thysanoptera, Hemiptera, Anoplura 6: 504. 1899";
//      irinaExample = "Milichiidae Sharp, 1899, Insects. Part II. Uiuis Iuiui Hymenopteracontinued (Tubulifera and Aculeata), Coleoptera, Strepsiptera, Lepidoptera, Diptera, Aphaniptera, Thysanoptera, Hemiptera, Anoplura 6: 504. 1899";
        INonViralName nvn = this.parser.parseReferencedName(irinaExample, NomenclaturalCode.ICZN, null);
        int parsingProblem = nvn.getParsingProblem();
        Assert.assertEquals("Name should have only rank warning", 1, parsingProblem);
        Assert.assertEquals("Titlecache", "Milichiidae Sharp, 1899", nvn.getTitleCache());
        Assert.assertEquals("If this line reached everything should be ok", "Milichiidae", nvn.getGenusOrUninomial());

        String anotherExample = "Scorzonera hispanica var. brevifolia Boiss. & Balansa in Boissier, Diagn. Pl. Orient., ser. 2 6: 119. 1859.";
        nvn = this.parser.parseReferencedName(anotherExample, ICNAFP, null);
        parsingProblem = nvn.getParsingProblem();
        Assert.assertEquals("Problem should be 0", 0, parsingProblem);
        Assert.assertEquals("Titlecache", "Scorzonera hispanica var. brevifolia Boiss. & Balansa", nvn.getTitleCache());
        Assert.assertEquals("If this line reached everything should be ok", "Scorzonera", nvn.getGenusOrUninomial());

        String unparsable = "Taraxacum nevskii L., Trudy Bot. Inst. Nauk S.S.S.R., Ser. 1, Fl. Sist. Vyssh. Rast. 4: 293. 1937.";
//      String unparsableA = "Taraxacum nevskii L. in Trudy Bot. Inst. Nauk: 293. 1937.";
        nvn = this.parser.parseReferencedName(unparsable, ICNAFP, null);
        Assert.assertEquals("Titlecache", "Taraxacum nevskii L.", nvn.getTitleCache());
        Assert.assertEquals("If this line reached everything should be ok", "Taraxacum", nvn.getGenusOrUninomial());
        parsingProblem = nvn.getParsingProblem();
        Assert.assertEquals("Name should no warnings or errors", 0, parsingProblem);

        String unparsable2 = "Hieracium pxxx Dahlst., Kongl. Svenska Vetensk. Acad. Handl. ser. 2, 26(3): 255. 1894";
//      String unparsable2A = "Hieracium pxxx Dahlst., Kongl Svenska Vetensk Acad Handl, 26: 255. 1894.";
        nvn = this.parser.parseReferencedName(unparsable2, ICNAFP, null);
        Assert.assertEquals("Titlecache", "Hieracium pxxx Dahlst.", nvn.getTitleCache());
        Assert.assertEquals("If this line reached everything should be ok", "Hieracium", nvn.getGenusOrUninomial());
        parsingProblem = nvn.getParsingProblem();
        Assert.assertEquals("Name should no warnings or errors", 0, parsingProblem);


        String again = "Adiantum emarginatum Bory ex. Willd., Species Plantarum, ed. 4,5,1: 449,450. 1810";
        nvn = this.parser.parseReferencedName(again, ICNAFP, null);
        Assert.assertEquals("Titlecache", "Adiantum emarginatum Bory ex Willd.", nvn.getTitleCache());
        Assert.assertEquals("If this line reached everything should be ok", "Adiantum", nvn.getGenusOrUninomial());

    }

    @Test
    public final void testSeriesPart(){
        Pattern seriesPattern = Pattern.compile(NonViralNameParserImplRegExBase.pSeriesPart);
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
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#AuthorshipAndEx(java.lang.String)}.
     * @throws StringNotParsableException
     */
    @Test
    public final void testParseAuthorsTaxonNameString() throws StringNotParsableException {
        INonViralName nvn = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseAuthors(nvn, "Eckweiler & ten Hagen, 2003");
        Team team = (Team)nvn.getCombinationAuthorship();
        Assert.assertNotNull("Comb. author must not be null", team);
        Assert.assertEquals("Must be team with 2 members", 2, team.getTeamMembers().size());
        Assert.assertEquals("Second member must be 'ten Hagen'", "ten Hagen", team.getTeamMembers().get(1).getTitleCache());

        //Crosson du Cormier, 1964
        IZoologicalName zooName = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseAuthors(zooName, "Crosson du Cormier, 1964");
        Person person = (Person)zooName.getCombinationAuthorship();
        Assert.assertNotNull("Comb. author must not be null", person);
        Assert.assertEquals("Persons title must be 'Crosson du Cormier'", "Crosson du Cormier", person.getTitleCache());
        Assert.assertEquals("Year must be 1964", Integer.valueOf(1964), zooName.getPublicationYear() );

        //(van der Hoeven, 1839)
        zooName = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseAuthors(zooName, "(van der Hoeven, 1839)");
        Assert.assertNull("Combination author must be null", zooName.getCombinationAuthorship());
        person = (Person)zooName.getBasionymAuthorship();
        Assert.assertNotNull("Basionym author must not be null", person);
        Assert.assertEquals("Persons title must be 'van der Hoeven'", "van der Hoeven", person.getTitleCache());
        Assert.assertEquals("Year must be 1839", Integer.valueOf(1839), zooName.getOriginalPublicationYear() );

        //le Doux, 1931
        zooName = TaxonNameFactory.NewZoologicalInstance(null);
        parser.parseAuthors(zooName, "le Doux, 1931");
        person = (Person)zooName.getCombinationAuthorship();
        Assert.assertNotNull("Comb. author must not be null", person);
        Assert.assertEquals("Persons title must be 'le Doux'", "le Doux", person.getTitleCache());
        Assert.assertEquals("Year must be 1931", Integer.valueOf(1931), zooName.getPublicationYear() );


    }

    @Test  //#4764
    public void testParseSection(){
        //this test does not really test problematic cases where sect.idInVoc = "sect." instead of "sect.(bot.)"
        //however, by changing the csv file entry to sect. just for testing it can be used as a functional test
        String sectionNameStr = "Taraxacum sect. Testtaxa M\u00fcller, Incredible Taxa: 12. 2016";
        INonViralName sectionName = parser.parseReferencedName(sectionNameStr, NomenclaturalCode.ICNAFP, null);
        int parsingProblem = sectionName.getParsingProblem();
        Assert.assertEquals("Problem should be 0", 0, parsingProblem);
        Rank rank = sectionName.getRank();
        Assert.assertEquals("", Rank.SECTION_BOTANY(), rank  );

    }

    @Test  //#5072
    public final void testLongRunningParsingCapitals(){
        DateTime start = DateTime.now();
        String nameStr = "Nazeris fujianensis JIAYAO HU, LIZHEN LI, MEIJUN ZHAO,2010";  //name from CoL that created problems
        INonViralName name = parser.parseReferencedName(nameStr, NomenclaturalCode.ICZN, null);
        DateTime end = DateTime.now();
        Duration duration = new Duration(start, end);
        long seconds = duration.getStandardSeconds();
        //this is the critical part of the test that must not be changed
        Assert.assertTrue("Parsing of name should take less then 3 seconds but took " + seconds, seconds < 3);

    }

    @Test  //#5072
    //http://www.regular-expressions.info/catastrophic.html
    public final void testLongRunningParsing(){

        //name only
        String nameStr = "Dictyocoela berillonum R.S. Terry, J.E. Sm., R.G. Sharpe, T. Rigaud, D.T.J. Littlewood, J.E. Ironside, D. Rollinson & D. Bou";
        DateTime start = DateTime.now();
        INonViralName name = parser.parseReferencedName(nameStr, NomenclaturalCode.ICNAFP, null);
        DateTime end = DateTime.now();
        Duration duration = new Duration(start, end);
        long seconds = duration.getStandardSeconds();
        //this is the critical part of the test that must not be changed
        Assert.assertTrue("Parsing of name should take less then 3 seconds but took " + seconds, seconds < 3);
        //the following may be discussed
        Assert.assertFalse("Name should parse without problems",name.hasProblem());


        //with reference
        nameStr = "Dictyocoela berillonum R.S. Terry, J.E. Sm., R.G. Sharpe, T. Rigaud, D.T.J. Littlewood, J.E. Ironside, D. Rollinson & D. Bou in Species Fauna Atlantica Of Blues Animals 3: p.345. 1758.";
        start = DateTime.now();
        name = parser.parseReferencedName(nameStr, NomenclaturalCode.ICNAFP, null);
        end = DateTime.now();
        duration = new Duration(start, end);
        seconds = duration.getStandardSeconds();
        //this is the critical part of the test that must not be changed
        Assert.assertTrue("Parsing of name should take less then 3 seconds but took " + seconds, seconds < 3);
        //the following may be discussed
        Assert.assertFalse("Name should parse without problems",name.hasProblem());
    }

    @Test  //#5072
    public final void testLongRunningParsingAuthors(){
        //http://www.regular-expressions.info/catastrophic.html
        //
        //Länge des Nachnamens macht keinen Unterschied
        //Anzahl der "AuthorParts scheint entscheidend
        // & am Ende macht es langsamger (16s), als nur ","(6s))

        String authorStr = "R.S. Terry J.E. Sm. R.G. Sharpe T. Rigaud T.H. Rigseaud D.T. Li, R.G. Sharpe, T. Rigaud, D.T.J. Littlewood & D. Bou";
        TeamOrPersonBase[] authorArray = new TeamOrPersonBase[4];
        try {
            DateTime start = DateTime.now();
            parser.fullAuthors(authorStr, authorArray, new Integer[]{1800, null, null, null}, BotanicalName.class);
            DateTime end = DateTime.now();
            Duration duration = new Duration(start, end);
            long seconds = duration.getStandardSeconds();
//            System.out.println(seconds);
            //this is the critical part of the test that must not be changed
            Assert.assertTrue("Parsing of name should take less then 3 seconds but took " + seconds, seconds < 3);
        } catch (StringNotParsableException e) {
            e.printStackTrace();
            Assert.fail("Authors should be parsable");
        }

    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#AuthorshipAndEx(java.lang.String)}.
     */
    @Test
    public final void testAuthorshipAndEx() {
        logger.warn("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#Authorship(java.lang.String)}.
     */
    @Test
    public final void testAuthorship() {
        logger.warn("Not yet implemented"); // TODO
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl#parseCultivar(java.lang.String)}.
     */
    @Test
    public final void testParseCultivar() {
        logger.warn("Not yet implemented"); // TODO
    }

    @Test
    public final void testNomenclaturalStatus() {
        BotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY(), "Acanthopale", null, null, null, null, null, null, null);
        name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
        BotanicalName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY());
        parser.parseReferencedName(name2, name.getFullTitleCache(), name2.getRank(), true);
        parser.parseReferencedName(name2, name.getFullTitleCache(), name2.getRank(), true);
        Assert.assertEquals("Title cache should be same. No duplication of nom. status should take place", name.getFullTitleCache(), name2.getFullTitleCache());
    }

    @Test
    public final void testSpecificAuthors(){
        //McVaugh
        INonViralName name = parser.parseFullName("Psidium longipes var. orbiculare (O.Berg) McVaugh");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "McVaugh", combinationAuthor.getNomenclaturalTitle());
        TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
        assertEquals( "O.Berg", basionymAuthor.getNomenclaturalTitle());

//      Campanula rhodensis A. DC.

    }

    @Test
    public final void testExistingProblems(){
        //Canabio, issue with space
        INonViralName name = parser.parseReferencedName("Machaonia erythrocarpa var. hondurensis (Standl.) Borhidi"
                + " in Acta Bot. Hung. 46 (1-2): 30. 2004");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi", combinationAuthor.getNomenclaturalTitle());
        Reference nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("46 (1-2)", nomRef.getVolume());

        //Canabio, detail with fig.
        name = parser.parseReferencedName("Didymaea floribunda Rzed."
                + " in Bol. Soc. Bot. Mex. 44: 72, fig. 1. 1983");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Rzed.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("44", nomRef.getVolume());
        assertEquals("72, fig. 1", name.getNomenclaturalMicroReference());

        //fig with a-c and without dot
        name = parser.parseReferencedName("Deppea guerrerensis Dwyer & Lorence"
                + " in Allertonia 4: 428. fig 4a-c. 1988");  //
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Dwyer & Lorence", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("428. fig 4a-c", name.getNomenclaturalMicroReference());

        //issue with EN_DASH (3–4)
        name = parser.parseReferencedName("Arachnothryx tacanensis (Lundell) Borhidi"
              + " in Acta Bot. Hung. 33 (3" + UTF8.EN_DASH + "4): 303. 1987");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("33 (3" + UTF8.EN_DASH + "4)", nomRef.getVolume());
        assertEquals("303", name.getNomenclaturalMicroReference());

        //fig with f.
        name = parser.parseReferencedName("Stenotis Terrell"
                + " in Sida 19(4): 901" + UTF8.EN_DASH + "911, f. 1" + UTF8.EN_DASH + "2. 2001");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Terrell", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("19(4)", nomRef.getVolume());
        assertEquals("901" + UTF8.EN_DASH + "911, f. 1" + UTF8.EN_DASH + "2", name.getNomenclaturalMicroReference());

        //detail with figs
        name = parser.parseReferencedName("Randia sonorensis Wiggins"
                + " in Contr. Dudley Herb. 3: 75, figs 4-6. 1940");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wiggins", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("75, figs 4-6", name.getNomenclaturalMicroReference());

        //detail with pl. and figs
        name = parser.parseReferencedName("Randia sonorensis Wiggins"
                + " in Contr. Dudley Herb. 3: 75, pl. 19, figs 4-6. 1940");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wiggins", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("75, pl. 19, figs 4-6", name.getNomenclaturalMicroReference());


        //pl
        name = parser.parseReferencedName("Carapichea  Aubl."
                + " in Hist. Pl. Guiane 1: 167, pl. 64. 1775");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Aubl.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("1", nomRef.getVolume());
        assertEquals("167, pl. 64", name.getNomenclaturalMicroReference());

        //fig with ,
        name = parser.parseReferencedName("Hoffmannia ixtlanensis Lorence"
                + " in Novon 4: 121. fig. 2a, b. 1994");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Lorence", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("121. fig. 2a, b", name.getNomenclaturalMicroReference());

        //detail with , to number
        name = parser.parseReferencedName("Deppea martinez-calderonii Lorence"
                + " in Allertonia 4: 399. figs 1e, 2. 1988");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Lorence", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("399. figs 1e, 2", name.getNomenclaturalMicroReference());

        //(Suppl.)
        name = parser.parseReferencedName("Manettia costaricensis  Wernham"
                + " in J. Bot. 57(Suppl.): 38. 1919");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wernham", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("57(Suppl.)", nomRef.getVolume());
        assertEquals("38", name.getNomenclaturalMicroReference());

        //NY.
        name = parser.parseReferencedName("Crusea psyllioides (Kunth) W.R. Anderson"
                + " in Mem. NY. Bot. Gard. 22: 75. 1972");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "W.R. Anderson", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("22", nomRef.getVolume());
        assertEquals("75", name.getNomenclaturalMicroReference());

        //apostroph word in title
        name = parser.parseReferencedName("Sabicea glabrescens Benth."
                + " in Hooker's J. Bot. Kew Gard. Misc. 3: 219. 1841");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Benth.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("219", name.getNomenclaturalMicroReference());

        // place published e.g. (Hannover)
        name = parser.parseReferencedName("Pittoniotis trichantha Griseb."
                  + " in Bonplandia (Hannover) 6 (1): 8. 1858");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Griseb.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("6 (1)", nomRef.getVolume());
        assertEquals("8", name.getNomenclaturalMicroReference());

        //komplex / incorrect year without quotation marks
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1\u20134): 16, f. 1\u20132, t. 1-8. 1983 [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1\u20134)", nomRef.getVolume());
        assertEquals("16, f. 1\u20132, t. 1-8", name.getNomenclaturalMicroReference());
        assertEquals("1983 [1984]", nomRef.getDatePublishedString());
        assertEquals("1984", nomRef.getYear());

        //incorrect year with \u201e \u201f  (s. eu.etaxonomy.cdm.common.UTF8.ENGLISH_QUOT_START
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1-4): 16, f. 1-2. \u201e1983\u201f [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1-4)", nomRef.getVolume());
        assertEquals("16, f. 1-2", name.getNomenclaturalMicroReference());
        assertEquals("\u201e1983\u201f [1984]", nomRef.getDatePublishedString());
        assertEquals("1984", nomRef.getYear());

        //incorrect year with "
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1-4): 16, f. 1-2. \"1983\" [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1-4)", nomRef.getVolume());
        assertEquals("16, f. 1-2", name.getNomenclaturalMicroReference());
        assertEquals("\"1983\" [1984]", nomRef.getDatePublishedString());
        assertEquals("1984", nomRef.getYear());

        //fig. a
        name = parser.parseReferencedName("Psychotria capitata  Ruiz & Pav."
                + " in Fl. Peruv. 2: 59, pl. 206, fig. a. 1799");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Ruiz & Pav.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("2", nomRef.getVolume());
        assertEquals("59, pl. 206, fig. a", name.getNomenclaturalMicroReference());

        //442A.
        name = parser.parseReferencedName("Rogiera elegans Planch."
                + " in Fl. Serres Jard. Eur. 5: 442A. 1849");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Planch.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("5", nomRef.getVolume());
        assertEquals("442A", name.getNomenclaturalMicroReference());

        //f
        name = parser.parseReferencedName("Coussarea imitans L.O. Williams"
                + " in Phytologia 26 (6): 488-489, f. 1973");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "L.O. Williams", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("26 (6)", nomRef.getVolume());
        assertEquals("488-489, f", name.getNomenclaturalMicroReference());

        //Phys.-Med.
        name = parser.parseReferencedName("Coccocypselum cordifolium Nees & Mart."
                + " in Nova Acta Phys.-Med. Acad. Caes.\u2013Leop. Nat. Cur. 12: 14. 1824");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Nees & Mart.", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Nova Acta Phys.-Med. Acad. Caes.\u2013Leop. Nat. Cur.", nomRef.getInReference().getAbbrevTitle());
        assertEquals("12", nomRef.getVolume());
        assertEquals("14", name.getNomenclaturalMicroReference());
        assertEquals("1824", nomRef.getYear());

        //(ed. 10)  wanted?
//        Syst. Nat. (ed. 10) 2: 930. 1759
//        name = parser.parseReferencedName("Erithalis fruticosa L."
//                + ", Syst. Nat. ed. 10, 2: 930. 1759");
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
//        combinationAuthor = name.getCombinationAuthorship();
//        assertEquals( "L.", combinationAuthor.getNomenclaturalTitle());
//        nomRef = (Reference)name.getNomenclaturalReference();
//        assertEquals(ReferenceType.Book, nomRef.getType());
//        assertEquals("2", nomRef.getVolume());
//        assertEquals("10", nomRef.getEdition());
//        assertEquals("930", name.getNomenclaturalMicroReference());
//        assertEquals("1759", nomRef.getYear());

        //issue with letter "(1a)"
        name = parser.parseReferencedName("Arthraerua (Kuntze) Schinz,"
                + " Nat. Pflanzenfam. 3(1a): 109. 1893");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Schinz", combinationAuthor.getNomenclaturalTitle());
        nomRef = (Reference)name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("Nat. Pflanzenfam.", nomRef.getAbbrevTitle());
        assertEquals("3(1a)", nomRef.getVolume());
        assertEquals("109", name.getNomenclaturalMicroReference());
        assertEquals("1893", nomRef.getYear());

        //Accent graph in author name #6057
        name = parser.parseReferencedName("Sedum plicatum O`Brian");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "O`Brian", name.getCombinationAuthorship().getNomenclaturalTitle());

        //-e-  #6060
        name = parser.parseReferencedName("Thamniopsis stenodictyon (Sehnem) Oliveira-e-Silva & O.Yano");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Team team = (Team)name.getCombinationAuthorship();
        assertEquals( "Oliveira-e-Silva", team.getTeamMembers().get(0).getNomenclaturalTitle());

        //Vorabdr.
        name = parser.parseReferencedName("Ophrys hystera  Kreutz & Ruedi Peter in J. Eur. Orchideen 30(Vorabdr.): 128. 1997");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "30(Vorabdr.)", ((Reference)name.getNomenclaturalReference()).getVolume());

        //test case disabled, would fail! Is due to '´t'
        // ´t
//        name = parser.parseReferencedName("Sempervivum globiferum subsp. allionii (Jord. & Fourr.) ´t Hart & Bleij");
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());


    }

}
