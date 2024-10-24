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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.IBotanicalName;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.IZoologicalName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IJournal;
import eu.etaxonomy.cdm.model.reference.IVolumeReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
import eu.etaxonomy.cdm.strategy.exceptions.StringNotParsableException;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * Tests for {@link NonViralNameParserImpl}.
 *
 * @author a.mueller
 */
public class NonViralNameParserImplTest extends TermTestBase {

    private static final Logger logger = LogManager.getLogger();

    private static final NomenclaturalCode ICNAFP = NomenclaturalCode.ICNAFP;
    private static final NomenclaturalCode ICZN = NomenclaturalCode.ICZN;
    private static final NomenclaturalCode FUNGI = NomenclaturalCode.Fungi;

    private static final String SEP = TimePeriod.SEP;

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

    @Before
    public void setUp() throws Exception {
        parser = NonViralNameParserImpl.NewInstance();
        botanicCode = ICNAFP;
    }

//*************** TEST *********************************************/

    @Test
    public final void testNewInstance() {
        assertNotNull(parser);
    }

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
        Assert.assertEquals("Mess., L. & Mill. 1987: Sp. Pl., ed. 3", name.getNomenclaturalReference().getTitleCache());
    }

    @Test
    public final void testParseSimpleName() {

        //Uninomials
        IZoologicalName milichiidae = (IZoologicalName)parser.parseSimpleName("Milichiidae", NomenclaturalCode.ICZN, null);
        assertEquals("Family rank expected", Rank.FAMILY(), milichiidae.getRank());
        IBotanicalName crepidinae = (IBotanicalName)parser.parseSimpleName("Crepidinae", ICNAFP, null);
        assertEquals("Family rank expected", Rank.SUBTRIBE(), crepidinae.getRank());
        IBotanicalName abies = (IBotanicalName)parser.parseSimpleName("Abies", ICNAFP, null);
        assertEquals("Family rank expected", Rank.GENUS(), abies.getRank());

        abies.addParsingProblem(ParserProblem.CheckRank);
        parser.parseSimpleName(abies, "Abies", abies.getRank(), true);
        assertTrue(abies.getParsingProblems().contains(ParserProblem.CheckRank));

        IBotanicalName rosa = (IBotanicalName)parser.parseSimpleName("Rosaceae", ICNAFP, null);
        assertTrue("Rosaceae have rank family", rosa.getRank().equals(Rank.FAMILY()));
        assertTrue("Rosaceae must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
        parser.parseSimpleName(rosa, "Rosaceaex", abies.getRank(), true);
        assertEquals("Rosaceaex have rank genus", Rank.GENUS(), rosa.getRank());
        assertTrue("Rosaceaex must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));

        //repeat but remove warning after first parse
        rosa = (IBotanicalName)parser.parseSimpleName("Rosaceae", ICNAFP, null);
        assertTrue("Rosaceae have rank family", rosa.getRank().equals(Rank.FAMILY()));
        assertTrue("Rosaceae must have a rank warning", rosa.hasProblem(ParserProblem.CheckRank));
        rosa.removeParsingProblem(ParserProblem.CheckRank);
        parser.parseSimpleName(rosa, "Rosaceaex", rosa.getRank(), true);
        assertEquals("Rosaceaex have rank family", Rank.FAMILY(), rosa.getRank());
        assertFalse("Rosaceaex must have no rank warning", rosa.hasProblem(ParserProblem.CheckRank));
    }

    @Test
    public final void testParseSubGenericFullName() {
        String zooSpeciesWithSubgenus = "Bacanius (Mullerister) rombophorus (Aube, 1843)";
        //zoo as fullName
        IZoologicalName zooName = parser.parseReferencedName(zooSpeciesWithSubgenus, NomenclaturalCode.ICZN, Rank.SPECIES());
        Assert.assertTrue(zooName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", zooName.getInfraGenericEpithet());
        Assert.assertEquals(Integer.valueOf(1843), zooName.getOriginalPublicationYear());
        //zoo as referenced name
        zooName = (IZoologicalName)parser.parseFullName(zooSpeciesWithSubgenus, NomenclaturalCode.ICZN, Rank.SPECIES());
        Assert.assertTrue(zooName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", zooName.getInfraGenericEpithet());
        Assert.assertEquals(Integer.valueOf(1843), zooName.getOriginalPublicationYear());

        //bot as full Name
        String botSpeciesWithSubgenus = "Bacanius (Mullerister) rombophorus (Aube) Mill.";
        IBotanicalName botName = (IBotanicalName)parser.parseFullName(botSpeciesWithSubgenus, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("Aube", botName.getBasionymAuthorship().getTitleCache());

        //bot as referenced Name
        botName = parser.parseReferencedName(botSpeciesWithSubgenus, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("Aube", botName.getBasionymAuthorship().getTitleCache());

        //bot without author
        String botSpeciesWithSubgenusWithoutAuthor = "Bacanius (Mullerister) rombophorus";
        botName = parser.parseReferencedName(botSpeciesWithSubgenusWithoutAuthor, NomenclaturalCode.ICNAFP, Rank.GENUS());
        Assert.assertTrue(botName.getParsingProblems().isEmpty());
        Assert.assertEquals("Mullerister", botName.getInfraGenericEpithet());
        Assert.assertEquals("rombophorus", botName.getSpecificEpithet());
        Assert.assertEquals("", botName.getAuthorshipCache());
    }

    @Test
    public final void testParseSubGenericSimpleName() {
        logger.warn("Not yet implemented"); // TODO
    }

    @Test
    public final void testParseFullNameUnicode() {

        INonViralName nameAuthor = parser.parseFullName(strNameAbiesAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameAuthor.getGenusOrUninomial());
        assertEquals("alba", nameAuthor.getSpecificEpithet());
        assertEquals("M\u00FCller", nameAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());

        INonViralName nameBasionymAuthor = parser.parseFullName(strNameAbiesBasionymAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
        assertEquals("D'M\u00FCller", nameBasionymAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());
        TeamOrPersonBase<?> basionymTeam = nameBasionymAuthor.getBasionymAuthorship();
        assertEquals("Ciardelli", basionymTeam.getNomenclaturalTitleCache());

        INonViralName nameBasionymExAuthor = parser.parseFullName(strNameAbiesBasionymExAuthor1Unicode, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
        assertEquals("D'M\u00FCller", nameBasionymExAuthor.getExCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());
        TeamOrPersonBase<?> basionymTeam2 = nameBasionymExAuthor.getExBasionymAuthorship();
        assertEquals("Ciardelli", basionymTeam2.getNomenclaturalTitleCache());
        TeamOrPersonBase<?> exBasionymTeam2 = nameBasionymExAuthor.getBasionymAuthorship();
        assertEquals("D\u00F6ring", exBasionymTeam2.getNomenclaturalTitleCache());

        IBotanicalName nameBasionymExAuthor2 = (IBotanicalName)parser.parseFullName("Washingtonia filifera (Linden ex Andre) H.Wendl. ex de Bary", null, Rank.SPECIES());
        assertEquals("Washingtonia", nameBasionymExAuthor2.getGenusOrUninomial());
        assertEquals("filifera", nameBasionymExAuthor2.getSpecificEpithet());
        assertEquals("H.Wendl.", nameBasionymExAuthor2.getExCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals("de Bary", nameBasionymExAuthor2.getCombinationAuthorship().getNomenclaturalTitleCache());
        TeamOrPersonBase<?> basionymTeam3 = nameBasionymExAuthor2.getBasionymAuthorship();
        assertEquals("Andre", basionymTeam3.getNomenclaturalTitleCache());
        TeamOrPersonBase<?> exBasionymTeam3 = nameBasionymExAuthor2.getExBasionymAuthorship();
        assertEquals("Linden", exBasionymTeam3.getNomenclaturalTitleCache());
        String title = nameBasionymExAuthor2.getTitleCache();
        assertEquals("Washingtonia filifera (Linden ex Andre) H.Wendl. ex de Bary", title);
    }

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
        assertEquals("Mueller & L.",  nameTeam1.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertTrue(nameTeam1.getCombinationAuthorship() instanceof Team);
        Team team = (Team)nameTeam1.getCombinationAuthorship();
        assertEquals("Mueller", team.getTeamMembers().get(0).getNomenclaturalTitleCache());
        assertEquals("L.", team.getTeamMembers().get(1).getNomenclaturalTitleCache());

        //ZooName
        IZoologicalName nameZoo1 = (IZoologicalName)parser.parseFullName(strNameZoo1);
        assertEquals( "Abies", nameZoo1.getGenusOrUninomial());
        assertEquals( "alba", nameZoo1.getSpecificEpithet());
        assertEquals("Mueller & L.",  nameZoo1.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals(NomenclaturalCode.ICZN, nameZoo1.getNameType() );
        assertEquals(Integer.valueOf(1822), nameZoo1.getPublicationYear());
        assertTrue(nameZoo1.getCombinationAuthorship() instanceof Team);
        Team teamZoo = (Team)nameZoo1.getCombinationAuthorship();
        assertEquals("Mueller", teamZoo.getTeamMembers().get(0).getNomenclaturalTitleCache());
        assertEquals("L.", teamZoo.getTeamMembers().get(1).getNomenclaturalTitleCache());

        IZoologicalName nameZoo2 = (IZoologicalName)parser.parseFullName(strNameZoo2);
        assertEquals(Integer.valueOf(2002), nameZoo2.getPublicationYear());
        assertEquals(Integer.valueOf(1822), nameZoo2.getOriginalPublicationYear());
        assertEquals("Mueller",  nameZoo2.getBasionymAuthorship().getNomenclaturalTitleCache());
        assertEquals("Ciardelli",  nameZoo2.getCombinationAuthorship().getNomenclaturalTitleCache());

        //subsp
        IZoologicalName nameZoo3 = (IZoologicalName)parser.parseFullName(strNameZoo3);
        assertEquals("Ciardelli",  nameZoo3.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertFalse("Subsp. without marker should be parsable", nameZoo3.hasProblem());
        assertEquals("Variety should be recognized", Rank.SUBSPECIES(), nameZoo3.getRank());

        IZoologicalName nameZoo4 = (IZoologicalName)parser.parseFullName(strNameZoo4);
        assertEquals("Ciardelli",  nameZoo4.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertFalse("Subsp. without marker should be parsable", nameZoo4.hasProblem());
        assertEquals("Variety should be recognized", Rank.SUBSPECIES(), nameZoo4.getRank());

        IZoologicalName nameZoo5 = (IZoologicalName)parser.parseFullName(strNameZoo5);
        assertEquals("Ciardelli",  nameZoo5.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertFalse("Subsp. without marker should be parsable", nameZoo5.hasProblem());
        assertEquals("Variety should be recognized", Rank.VARIETY(), nameZoo5.getRank());

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
        assertEquals("H. Keng", name.getCombinationAuthorship().getNomenclaturalTitleCache());

        //name without combination  author  , only to check if above fix for #5618 works correctly
        fullNameString = "Gordonia moaensis (Vict.)";
        name = parser.parseFullName(fullNameString);
        assertFalse(name.hasProblem());
        assertNull(name.getCombinationAuthorship());
        assertNotNull(name.getBasionymAuthorship());
        assertEquals("Vict.", name.getBasionymAuthorship().getNomenclaturalTitleCache());

    }

    @Test
    public final void testAutonyms(){
        TaxonName autonymName;
        //infraspecific
        autonymName = (TaxonName)parser.parseFullName("Abies alba Mill. var. alba", ICNAFP, null);
        assertFalse("Autonym should be parsable", autonymName.hasProblem());
        autonymName = parser.parseReferencedName("Abies alba Mill. var. alba", ICNAFP, null);
        assertFalse("Autonym should be parsable", autonymName.hasProblem());
        //infrageneric
        autonymName = (TaxonName)parser.parseFullName("Abies Mill. sect. Abies", ICNAFP, null);
        assertFalse("Genus autonym should be parsable", autonymName.hasProblem());
        assertEquals("Rank should be section (bot.)", Rank.SECTION_BOTANY(), autonymName.getRank());
        autonymName = parser.parseReferencedName("Achnanthes Bory sect. Achnanthes", ICNAFP, null);
        assertFalse("Genus autonym should be parsable", autonymName.hasProblem());
        assertEquals("Rank should be section (bot.)", Rank.SECTION_BOTANY(), autonymName.getRank());
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
        assertEquals("Combination author should be L.", "L.", ((Person)multipleAuthorRefName.getCombinationAuthorship()).getNomenclaturalTitleCache());
        Reference nomRef = multipleAuthorRefName.getNomenclaturalReference();
        Assert.assertNotNull("nomRef must have inRef", nomRef.getInReference());
        Reference inRef = nomRef.getInReference();
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
        Assert.assertNotNull("nomRef must have inRef", nomRef.getInReference());
        inRef = nomRef.getInReference();
        abbrevTitle = inRef.getAbbrevTitle();
        assertEquals("InRef title should be Sp. Pl.", "Sp. Pl.", abbrevTitle);
        assertTrue(inRef.getAuthorship() instanceof Person);
        Person person = (Person)inRef.getAuthorship();
        assertEquals("Book author should be L.", "L.", person.getNomenclaturalTitleCache());


        fullTitleString = "Abies alba Mill., Aber & Schwedt, Sp. Pl. 173: 384. 1982.";
        multipleAuthorName = parser.parseReferencedName(fullTitleString, NomenclaturalCode.ICNAFP, Rank.SPECIES());
        assertFalse(multipleAuthorName.hasProblem());
        assertTrue("Combination author should be a team", multipleAuthorName.getCombinationAuthorship() instanceof Team);
        team = (Team)multipleAuthorName.getCombinationAuthorship();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Second team member should be Aber", "Aber", team.getTeamMembers().get(1).getTitleCache());
        nomRef = multipleAuthorName.getNomenclaturalReference();
        Assert.assertNull("nomRef must not have inRef as it is a book itself", nomRef.getInReference());
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
        //maybe false: see https://dev.e-taxonomy.eu/redmine/issues/3868
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
        assertFalse("Name must not have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getNameCache());  //we expect the cache strategy to create the same result

        //hybrid autonym #6656
        nameStr = "Ophrys \u00D7kastelli E. Klein nothosubsp. kastelli";
        name1 = parser.parseFullName(nameStr);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getTitleCache()); //we expect the cache strategy to create the same result

        name1 = parser.parseReferencedName(nameStr);
        assertFalse("Name must not have monom hybrid bit set", name1.isMonomHybrid());
        assertTrue("Name must have binom hybrid bit set", name1.isBinomHybrid());
        assertTrue("Name must have trinom hybrid bit set", name1.isTrinomHybrid());
        assertFalse("Name must not be protected", name1.isProtectedTitleCache());
        assertEquals(nameStr, name1.getTitleCache()); //we expect the cache strategy to create the same result

        //remove space since #7094
        parser.setRemoveSpaceAfterDot(true);
        name1 = parser.parseReferencedName(nameStr);
        assertEquals(nameStr.replace("E. Kl", "E.Kl"), name1.getTitleCache()); //we expect the cache strategy to create the same result
        parser.setRemoveSpaceAfterDot(false);
    }

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
        TaxonName firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Abies alba as first hybrid parent", "Abies alba", firstParent.getTitleCache());
        TaxonName secondParent = orderedRels.get(1).getParentName();
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

        //subspecies and variety with genus part
        nameStr = "Orchis morio subsp. syriaca \u00D7 Test papilionacea var. schirvanica";
        name1 = parser.parseFullName(nameStr, botanicCode, null);
        assertTrue("Name must have hybrid formula bit set", name1.isHybridFormula());
        assertEquals("Name must have 2 hybrid parents", 2, name1.getHybridChildRelations().size());
        //could also be N. or no genus at all, depends on formatter
        assertEquals("Title cache must be correct", "Orchis morio subsp. syriaca \u00D7 Test papilionacea var. schirvanica", name1.getTitleCache());
        orderedRels = name1.getOrderedChildRelationships();
        assertEquals("Name must have 2 hybrid parents in ordered list", 2, orderedRels.size());
        firstParent = orderedRels.get(0).getParentName();
        assertEquals("Name must have Orchis morio subsp. syriaca as first hybrid parent", "Orchis morio subsp. syriaca", firstParent.getTitleCache());
        secondParent = orderedRels.get(1).getParentName();
        assertEquals("Name must have Orchis papilionacea var. schirvanica as second hybrid parent", "Test papilionacea var. schirvanica", secondParent.getTitleCache());
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

    @Test
    public final void testUninomials() {
        String uninomial = "Anserineae";  //this, in reality is a tribe but the parser should recognize a suborder as the suborder ending -ineae is more specific then the tribe ending -eae
        INonViralName name = parser.parseFullName(uninomial, botanicCode, null);
        assertEquals(Rank.SUBORDER(), name.getRank());
    }

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

        //'ranglos' infraspecific
        infraspecificUnranked = "Genus species [ranglos] infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
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

        //unranked infrageneric
        infraGenericUnranked = "Genus [ranglos] Infragen";
         name2 = parser.parseFullName(infraGenericUnranked);
        assertEquals( "Genus", name2.getGenusOrUninomial());
        assertEquals( null, name2.getSpecificEpithet());
        assertEquals( "Infragen", name2.getInfraGenericEpithet());
        assertEquals( "Ranglos rank should be parsed", Rank.INFRAGENERICTAXON(), name2.getRank());

    }

    @Test
    public final void testOldRanks() {
        try {
            Method parseMethod = parser.getClass().getDeclaredMethod("parseFullName", String.class, NomenclaturalCode.class, Rank.class);
            testName_StringNomcodeRank(parseMethod);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

        //proles
        String infraspecificUnranked = "Genus species proles infraspecific";
        INonViralName name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Proles should be parsed", Rank.PROLES(), name.getRank());

        //subproles
        infraspecificUnranked = "Genus species subproles infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Subproles should be parsed", Rank.SUBPROLES(), name.getRank());

        //prol.
        infraspecificUnranked = "Genus species prol. infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Prol. should be parsed", Rank.PROLES(), name.getRank());

        //subproles
        infraspecificUnranked = "Genus species subprol. infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Subprol. should be parsed", Rank.SUBPROLES(), name.getRank());

        //lusus
        infraspecificUnranked = "Genus species lusus infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Sublusus should be parsed", Rank.LUSUS(), name.getRank());

        //sublusus
        infraspecificUnranked = "Genus species sublusus infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Sublusus should be parsed", Rank.SUBLUSUS(), name.getRank());

        //race
        infraspecificUnranked = "Genus species race infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Race should be parsed", Rank.RACE(), name.getRank());

        //grex
        infraspecificUnranked = "Genus species grex infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Grex should be parsed", Rank.GREX_INFRASPEC(), name.getRank());

        //subgrex
        infraspecificUnranked = "Genus species subgrex infraspecific";
        name = parser.parseFullName(infraspecificUnranked);
        assertEquals( "Genus", name.getGenusOrUninomial());
        assertEquals( "species", name.getSpecificEpithet());
        assertEquals( "infraspecific", name.getInfraSpecificEpithet());
        assertEquals( "Subgrex should be parsed", Rank.SUBGREX(), name.getRank());
    }

    @Test
    public final void testTemp(){

//        String nameStr = "Mentha aquatica L. x Mentha spicata L.";
//        INonViralName name = parser.parseFullName(nameStr, botanicCode, null);
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
//        assertFalse( name.getHybridChildRelations().isEmpty());
//        for (HybridRelationship rel : name.getHybridChildRelations()){
//            TaxonName parent = rel.getParentName();
//            System.out.println(parent.getTitleCache());
//        }
    }

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
        assertEquals("Mueller", nameAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());

        INonViralName nameBasionymAuthor = (INonViralName)parseMethod.invoke(parser, strNameAbiesBasionymAuthor1, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymAuthor.getSpecificEpithet());
        assertEquals("D'Mueller", nameBasionymAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals("Ciardelli", nameBasionymAuthor.getBasionymAuthorship().getNomenclaturalTitleCache());

        INonViralName nameBasionymExAuthor = (INonViralName)parseMethod.invoke(parser, strNameAbiesBasionymExAuthor1, null, Rank.SPECIES());
        assertEquals("Abies", nameBasionymExAuthor.getGenusOrUninomial());
        assertEquals("alba", nameBasionymExAuthor.getSpecificEpithet());
        assertEquals("D'Mueller", nameBasionymExAuthor.getExCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals("de Greuther", nameBasionymExAuthor.getCombinationAuthorship().getNomenclaturalTitleCache());
        assertEquals("Ciardelli", nameBasionymExAuthor.getExBasionymAuthorship().getNomenclaturalTitleCache());
        assertEquals("Doering", nameBasionymExAuthor.getBasionymAuthorship().getNomenclaturalTitleCache());

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

        //desig. inval. (same as above but with other status label
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, desig. inval.";
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
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, orth. cons. prop.";
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
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, orth. cons.";
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

        //nom. val.
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, nom. val.";
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
        strTestStatus = "Abies alba Mill., Sp. Pl. 4: 455. 1987, op. utique oppr.";
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
        assertTrue(namefullWhiteSpcaceAndDot.getNomenclaturalReference().getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
        assertEquals( "Abies alba Mill., Sp. Pl. 4: 455. 1987", namefullWhiteSpcaceAndDot.getFullTitleCache());

        //Book
        String fullReference = "Abies alba Mill., Sp. Pl. 4: 455. 1987";
        INonViralName name1 = parser.parseReferencedName(fullReference, null, rankSpecies);
        assertFullRefStandard(name1);
        assertTrue(name1.getNomenclaturalReference().getType().equals(eu.etaxonomy.cdm.model.reference.ReferenceType.Book));
        assertEquals(fullReference, name1.getFullTitleCache());
        assertTrue("Name author and reference author should be the same", name1.getCombinationAuthorship() == name1.getNomenclaturalReference().getAuthorship());

        //Book Section
        fullReference = "Abies alba Mill. in Otto, Sp. Pl. 4(6): 455. 1987";
        INonViralName name2 = parser.parseReferencedName(fullReference + ".", null, rankSpecies);
        assertFullRefNameStandard(name2);
        assertEquals(fullReference, name2.getFullTitleCache());
        assertFalse(name2.hasProblem());
        Reference ref = name2.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, ref.getType());
        IBookSection bookSection = ref;
        IBook inBook = bookSection.getInBook();
        assertNotNull(inBook);
        assertNotNull(inBook.getAuthorship());
        assertEquals("Otto", inBook.getAuthorship().getTitleCache());
        assertEquals("Otto: Sp. Pl. 4(6)", inBook.getTitleCache());
        assertEquals("Sp. Pl.", inBook.getAbbrevTitle());
        assertEquals("4(6)", inBook.getVolume());
        assertTrue("Name author and reference author should be the same", name2.getCombinationAuthorship() == name2.getNomenclaturalReference().getAuthorship());

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
        String parsedYearFormatted = "1987"+SEP+"1989";
        String fullReferenceWithoutYear = "Abies alba Mill. in " + journalTitle + " 4(6): 455.";
        fullReference = fullReferenceWithoutYear + yearPart;
        String fullReferenceWithEnd = fullReference + ".";
        INonViralName name4 = parser.parseReferencedName(fullReferenceWithEnd, null, rankSpecies);
        assertFalse(name4.hasProblem());
        assertFullRefNameStandard(name4);
        assertEquals(fullReferenceWithoutYear + " " + parsedYearFormatted, name4.getFullTitleCache());
        ref = name4.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, ref.getType());
        //article = (Article)ref;
        assertEquals(parsedYearFormatted, ref.getYear());
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

        assertEquals(NomenclaturalCode.ICZN, nameZooRefNotParsabel.getNameType());
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
        assertEquals(strBookSection2NoComma.replace(" ed.", ", ed.").replace("-",SEP), nameBookSection2.getFullTitleCache());
        assertEquals(-1, nameBookSection2.getProblemStarts());
        assertEquals(-1, nameBookSection2.getProblemEnds());
        assertNull((nameBookSection2.getNomenclaturalReference()).getDatePublished().getStart());
        assertEquals("1905"+SEP+"1907", ((IBookSection)nameBookSection2.getNomenclaturalReference()).getInBook().getDatePublished().getYear());

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

        testParsable = "Silene broussonetiana Schott ex Webb & Berthel., Hist. Nat. Iles Canaries 3(2,1): 141. 1840";
        assertTrue("Reference with volume with 2 number in brackets is not parsable", isParsable(testParsable, ICNAFP));
        testParsable = "Silene broussonetiana Schott ex Webb & Berthel., Hist. Nat. Iles Canaries 3(2, 1): 141. 1840";
        assertTrue("Reference with volume with 2 number in brackets is not parsable", isParsable(testParsable, ICNAFP));

    }


    /**
     * Test author with name parts van, von, de, de la, d', da, del.
     * See also https://dev.e-taxonomy.eu/redmine/issues/3373
     */
    @Test
    public final void  testComposedAuthorNames(){

        //van author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        String testParsable = "Aphelocoma unicolor subsp. griscomi van Rossem, 1928";
        assertTrue("Author with 'van' should be parsable", isParsable(testParsable, ICZN));

        //von author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi von Rossem, 1928";
        assertTrue("Author with 'von' should be parsable", isParsable(testParsable, ICZN));

        //de author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi de Rossem, 1928";
        assertTrue("Author with 'de' should be parsable", isParsable(testParsable, ICZN));

        //de la author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi de la Rossem, 1928";
        assertTrue("Author with 'de la' should be parsable", isParsable(testParsable, ICZN));

        //d' author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi d'Rossem, 1928";
        assertTrue("Author with \"'d'\" should be parsable", isParsable(testParsable, ICZN));

        //da author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi da Rossem, 1928";
        assertTrue("Author with 'da' should be parsable", isParsable(testParsable, ICZN));

        //del author (see https://dev.e-taxonomy.eu/redmine/issues/3373)
        testParsable = "Aphelocoma unicolor subsp. griscomi del Rossem, 1928";
        assertTrue("Author with 'del' should be parsable", isParsable(testParsable, ICZN));

        //O' author (see https://dev.e-taxonomy.eu/redmine/issues/4759)
        testParsable = "Aphelocoma unicolor subsp. griscomi O'Connor, 1928";
        assertTrue("Author with 'O'' should be parsable", isParsable(testParsable, ICZN));

        //del author (see https://dev.e-taxonomy.eu/redmine/issues/4759)
        testParsable = "Aphelocoma unicolor subsp. griscomi zur Strassen, 1928";
        assertTrue("Author with 'zur' should be parsable", isParsable(testParsable, ICZN));

    }

    private List<ParserProblem> getProblems(String string, NomenclaturalCode code) {
        List<ParserProblem> result = parser.parseReferencedName(string, code, null).getParsingProblems();
        return result;
    }

    private boolean isParsable(String string, NomenclaturalCode code){
        INonViralName name = parser.parseReferencedName(string, code, null);
        if (name.hasProblem()) {
            return false;
        }else if (name.getNomenclaturalReference() != null) {
            //not really neccessary as "problem" is for fulltitlecache and therefore also
            Reference nomRef = name.getNomenclaturalReference();
            if (nomRef.hasProblem()) {
                return false;
            }else if (nomRef.getInReference() != null) {
                if (nomRef.getInReference().hasProblem()) {
                    return false;
                }
            }
        }
        return true;
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
        Reference ref = name.getNomenclaturalReference();
        assertEquals("1987", ref.getYear());
        assertEquals("Sp. Pl.", ref.getAbbrevTitle());
    }


    @Test
    public void testNeverEndingParsing(){
        //some full titles result in never ending parsing process https://dev.e-taxonomy.eu/redmine/issues/1556

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
    public final void testSeries(){
        String parseStr = "Mazus pumilus (Burm.f.) Steenis in Nova Guinea, n.s., 9: 31. 1958";
        INonViralName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", nomRef.isProtectedTitleCache());

        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals(name.getNomenclaturalMicroReference(), "31");
        assertEquals("Nova Guinea", nomRef.getInJournal().getAbbrevTitle());
        assertEquals("n.s.", nomRef.getSeriesPart());
    }

    @Test
    @Ignore
    public final void testRussian(){
        String parseStr = "Cortusa turkestanica Losinsk. in Тр. Бот. инст. Aкад. наук СССР, сер. 1, 3: 239. 1936";
        INonViralName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", nomRef.isProtectedTitleCache());

        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals(name.getNomenclaturalMicroReference(), "239");
        assertEquals("Тр. Бот. инст. Aкад. наук СССР", nomRef.getInJournal().getAbbrevTitle());
        assertEquals("сер. 1", nomRef.getSeriesPart());
    }

    @Test
    public final void testDetails(){
        //s.p.
        String parseStr = "Xiphion filifolium var. latifolium Baker, Gard. Chron. 1876: s.p.. 1876";
        INonViralName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("s.p.", name.getNomenclaturalMicroReference());

        //roman
        parseStr = "Ophrys lutea subsp. pseudospeculum (DC.) Kergu\u00e9len, Collect. Partim. Nat. 8: xv. 1993";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("xv", name.getNomenclaturalMicroReference());

        //n. 1
        parseStr = "Olea gallica Mill., Gard. Dict. ed. 8: n. 1. 1768";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("n. 1", name.getNomenclaturalMicroReference());

        parseStr = "Lavandula canariensis Mill., Gard. Dict. ed. 8: Lavandula no. 4. 1768";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("Lavandula no. 4", name.getNomenclaturalMicroReference());

        parseStr = "Aceras anthropomorphum (Pers.) Sm. in Rees, Cycl. 39(1): Aceras n. 2. 1818";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals(name.getNomenclaturalMicroReference(), "Aceras n. 2");

        parseStr = "Chlorolepis Nutt. in Trans. Amer. Philos. Soc., n.s., 7: errata. 1841";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("errata", name.getNomenclaturalMicroReference());

        parseStr = "Yermoloffia B\u00e9l., Voy. Indes Or.: t. s.n.. 1846";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("t. s.n.", name.getNomenclaturalMicroReference());

        parseStr = "Gagea mauritanica Durieu, Expl. Sci. Alg\u00e9rie, Atlas: t. 45bis, f. 4. 1850";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("t. 45bis, f. 4", name.getNomenclaturalMicroReference());

        parseStr = "Orchis latifolia f. blyttii Rchb. f. in Reichenbach, Icon. Fl. Germ. Helv. 13-14: 60, t. 59, f. III. 1851";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals("60, t. 59, f. III", name.getNomenclaturalMicroReference());

        parseStr = "Ephedra alata var. decaisnei Stapf in Denkschr. Kaiserl. Akad. Wiss., Wien. Math.-Naturwiss. Kl. 56(2): t. 1/1. 1889";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("t. 1/1", name.getNomenclaturalMicroReference());

        parseStr ="Leptodon smithii (Dicks. ex Hedw.) F. Weber & D. Mohr, Index Mus. Pl. Crypt.: 2 [recto]. 1803";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals(name.getNomenclaturalMicroReference(), "2 [recto]");
    }

    @Test
    public final void testEditionVolumeSeries(){
        //ed. 2, 2(1)
        String parseStr = "Sieberia albida (L.) Spreng., Anleit. Kenntn. Gew., ed. 2, 2(1): 282. 1817";
        INonViralName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("2", nomRef.getEdition());
        assertEquals("2(1)", nomRef.getVolume());

        parseStr = "Gagea glacialis var. joannis (Grossh.) Grossh., Fl. Kavkaza, ed. 2, 2: 105. 1940";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("2", nomRef.getEdition());
        assertEquals("2", nomRef.getVolume());

        //14-15
        parseStr = "Semele gayae (Webb & Berthel.) Svent. & Kunkel, Cuad. Bot. Canaria 14-15: 81. 1972";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("14-15", nomRef.getVolume());

        //35-37(2)
        parseStr = "Lavandula multifida var. heterotricha Sauvage in Bull. Soc. Sci. Nat. Maroc 35-37(2): 392. 1947";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertNull(nomRef.getEdition());
        assertEquals("35-37(2)", nomRef.getVolume());

        //Sér. 7
        parseStr = "Oxynepeta involucrata Bunge, M\u00E9m. Acad. Imp. Sci. Saint P\u00E9tersbourg, S\u00E9r. 7, 21(1): 59. 1878";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertNull(nomRef.getEdition());
        assertEquals("21(1)", nomRef.getVolume());
        //Currently we do not put the series part into the series field, this may change in future
//        assertEquals("Sér. 7", nomRef.getSeriesPart());

        //Suppl. 1
        parseStr = "Dissorhynchium Schauer, Nov. Actorum Acad. Caes. Leop.-Carol. Nat. Cur. 19(Suppl. 1): 434. 1843";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertNull(nomRef.getEdition());
        assertEquals("19(Suppl. 1)", nomRef.getVolume());

        //54*B*
        parseStr = "Thymus chaubardii var. boeoticus (Heinr. Braun) Ronniger in Beih. Bot. Centralbl. 54B: 662. 1936";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertNull(nomRef.getEdition());
        assertEquals("54B", nomRef.getVolume());


        //1, Erg.
        Pattern seriesPattern = Pattern.compile(NonViralNameParserImplRegExBase.volume);
        Matcher matcher = seriesPattern.matcher("12(1, Erg.)");
        Assert.assertTrue("12(1, Erg.) should match", matcher.matches());

        parseStr = "Scilla amethystina Vis. in Flora Abt Awer Ser 12(1, Erg.): 11. 1829";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertNull(nomRef.getEdition());
        assertEquals("12(1, Erg.)", nomRef.getVolume());

        // jubilee ed.
        parseStr = "Orchis sambucina var. bracteata (M. Schulze) Harz, Fl. Deutschl., jubilee ed., 4: 271. 1895";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("jubilee ed.",nomRef.getEdition());
        assertEquals("4", nomRef.getVolume());
        assertEquals(parseStr, name.getFullTitleCache());

        //nouv. ed.
        parseStr = "Fraxinus polemonifolia Poir. in Duhamel du Monceau, Trait\u00E9 Arbr. Arbust., nouv. ed., 4: 66. 1809";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals("nouv. ed.",nomRef.getInReference().getEdition());
        assertEquals("4", nomRef.getInReference().getVolume());
        assertEquals(parseStr, name.getFullTitleCache());

        //ed. 3B
        parseStr = "Juncus supinus var. kochii (F. W. Schultz) Syme in Smith, Engl. Bot., ed. 3B, 10: 33. 1870";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        //maybe we remove ed. for this case in future
        assertEquals("ed. 3B",nomRef.getInReference().getEdition());
        assertEquals("10", nomRef.getInReference().getVolume());
        assertEquals(parseStr, name.getFullTitleCache());

        //ed. 15 bis
        parseStr = "Solanum persicum Willd. ex Roem. & Schult., Syst. Veg., ed. 15 bis, 4: 662. 1819";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        //maybe we remove ed. for this case in future
        assertEquals("ed. 15 bis",nomRef.getEdition());
        assertEquals("4", nomRef.getVolume());
        assertEquals(parseStr, name.getFullTitleCache());


//        Epipactis helleborine subsp. ohwii (Fukuy.) H. J. Su in Fl. Taiwan, ed. 2, 5: 861. 2000
    }

    @Test
    public final void testTitleBrackets(){
        //Bot. Zhurn. (Moscow & Leningrad)
        String parseStr = "Juncus subcompressus Zakirov & Novopokr. in Bot. Zhurn. (Moscow & Leningrad) 36(1): 77. 1951";
        TaxonName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Bot. Zhurn. (Moscow & Leningrad)", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("36(1)", nomRef.getVolume());
    }

    @Test
    public final void testTitleSpecials(){
        //Pt. 2  (currently handled as series part, may change in future
        String parseStr = "Iris pumila subsp. sintenisiiformis Prod\u00E1n in Ann. Sci. Univ. Jassy, Pt. 2, Sci. Nat. 27: 89. 1941";
        TaxonName name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Ann. Sci. Univ. Jassy, Pt. 2, Sci. Nat.", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("27", nomRef.getVolume());

        //same as Pt. 2, "Sect. xx" handled as series part but may change
        parseStr = "Quercus boissieri var. microphylla (A. Camus) Zohary in Bull. Res. Council Israel, Sect. D, Bot. 9: 169. 1961";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Bull. Res. Council Israel, Sect. D, Bot.", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("9", nomRef.getVolume());

        //see above
        parseStr = "Fimbristylis dichotoma var. annua (All.) T. Koyama in J. Fac. Sci. Univ. Tokyo, Sect. 3, Bot. 8: 111. 1961";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("J. Fac. Sci. Univ. Tokyo, Sect. 3, Bot.", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("8", nomRef.getVolume());


        // "- "
        parseStr = "Theresia tulipilfoia (M. Bieb.) Klatt in Hamburger Garten- Blumenzeitung 16: 438. 1860";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Hamburger Garten- Blumenzeitung", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("16", nomRef.getVolume());

        parseStr = "Hyssopus officinalis var. pilifer Pant. in Verh. Vereins Natur- Heilk. Presburg, n.s., 2: 61. 1874";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        //, n.s., is not necessarily part of the title in future
        assertEquals("Verh. Vereins Natur- Heilk. Presburg", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("n.s.", nomRef.getSeriesPart());
        assertEquals("2", nomRef.getVolume());

          //Note: space in E+M, no space in IPNI; is it really a book?
        parseStr = "Amaryllis dubia Houtt., Handl. Pl.- Kruidk. 12: 181. 1780";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("Handl. Pl.- Kruidk.", nomRef.getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("12", nomRef.getVolume());

        // "..."
        parseStr = "Chamaesyce biramensis (Urb.) Alain in Contr. Ocas. Mus. Hist. Nat. Colegio \"De La Salle\" 11: 12. 1952";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Contr. Ocas. Mus. Hist. Nat. Colegio \"De La Salle\"", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("11", nomRef.getVolume());

        //' & '
        parseStr = "Mannaphorus Raf. in Amer. Monthly Mag. & Crit. Rev. 1: 175. 1818";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Amer. Monthly Mag. & Crit. Rev.", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("1", nomRef.getVolume());

        //only for debugging
        boolean  matches;
        matches = "Flo & Amer. Fauna. Ab.".matches (NonViralNameParserImplRegExBase.referenceTitleFirstPart + "*");
        Assert.assertTrue("referenceTitleFirstPart", matches);

        matches = "Fl. Amer. & Fauna. Ab. 101".matches (NonViralNameParserImplRegExBase.pSoftArticleReference);
        Assert.assertTrue("pSoftArticleReference", matches);
        //only for debugging end

        parseStr = "Corallorhiza trifida subsp. virescens (Drejer) L\u00F8jtnant in Fl. & Fauna (Esbjerg) 101: 71. 1996";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Fl. & Fauna (Esbjerg)", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("101", nomRef.getVolume());

        parseStr = "Crocus isauricus Siehe ex Bowles, Handb. Crocus & Colch.: 126. 1924";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("Handb. Crocus & Colch.", nomRef.getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertNull(nomRef.getVolume());

        parseStr = "Ornithogalum bifolium (L.) Neck. in Hist. & Commentat. Acad. Elect. Sci. Theod.-Palat. 2: 461. 1770";
        name = parser.parseReferencedName(parseStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("nom.ref. should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Hist. & Commentat. Acad. Elect. Sci. Theod.-Palat.", nomRef.getInReference().getAbbrevTitle());
        assertNull(nomRef.getEdition());
        assertEquals("2", nomRef.getVolume());

    }

    @Test
    public final void testArticlePattern(){
        Pattern articlePattern = Pattern.compile(NonViralNameParserImplRegExBase.pArticleReference);
        Matcher matcher = articlePattern.matcher("Acta Bot. Hung. 46 (1-2)");
        Assert.assertTrue("", matcher.matches());
        matcher = articlePattern.matcher("Nova Guinea Bla 9");
        Assert.assertTrue("", matcher.matches());
        matcher = articlePattern.matcher("Nova Guinea Bla , n.s., 9");
        Assert.assertTrue("", matcher.matches());
    }


    @Test
    public final void testSeriesPart(){

        Pattern seriesPattern = Pattern.compile(NonViralNameParserImplRegExBase.pSeriesPart);
        Matcher matcher = seriesPattern.matcher(", ser. 2,");
        Assert.assertTrue("", matcher.matches());

        matcher = seriesPattern.matcher("n.s.");
        Assert.assertTrue("", matcher.matches());

//        matcher = seriesPattern.matcher("a.s.");
//        Assert.assertTrue("", matcher.matches());

        //do NOT match edition  //but there are such cases like "Vilm. Blumengärtn. ed. 3" according to TL-2
        matcher = seriesPattern.matcher("ed. 4");
        Assert.assertFalse("", matcher.matches());

        matcher = seriesPattern.matcher("Ser. C");
        Assert.assertTrue("", matcher.matches());

        matcher = seriesPattern.matcher("S\u00E9r. B 1");
        Assert.assertTrue("", matcher.matches());

        matcher = seriesPattern.matcher("Jerusalem Ser.");
        Assert.assertTrue("", matcher.matches());

        matcher = seriesPattern.matcher("nov. Ser.");
        Assert.assertTrue("", matcher.matches());

        //not really series  "Abt. 2"
        String nameStr = "Hydrogonium consanguineum Hilp. in Beih. Bot. Centralbl., Abt. 2, 50(3): 626. 1933";
        TaxonName name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Reference inRef = ref.getInReference();
        //TODO handling of Abt. not fully correct, should better go into title
        Assert.assertEquals("Beih. Bot. Centralbl., Abt. 2", inRef.getAbbrevTitle());
        Assert.assertEquals(null, ref.getSeriesPart());
        Assert.assertEquals("50(3)", ref.getVolume());
        Assert.assertEquals(nameStr, name.getFullTitleCache());
        String detail = name.getNomenclaturalMicroReference();
        Assert.assertEquals("626", detail);
    }

    @Test
    public final void testFullTeams() {
        logger.warn("Not yet implemented"); // TODO
    }

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

    //#6577
    @Test
    public final void testParseSpNov(){
        //Canabio, issue with space
        INonViralName name = parser.parseFullName("Iresine sp. nov. 1");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Assert.assertEquals("sp. nov. 1", name.getSpecificEpithet());

        name = parser.parseFullName("Gomphichis sp. 22");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Assert.assertEquals("sp. 22", name.getSpecificEpithet());

        name = parser.parseFullName("Phleum sp. nov.");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Assert.assertEquals("sp. nov.", name.getSpecificEpithet());

        name = parser.parseFullName("Phleum sp.");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Assert.assertEquals("sp.", name.getSpecificEpithet());

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
        TeamOrPersonBase<?>[] authorArray = new TeamOrPersonBase[4];
        try {
            DateTime start = DateTime.now();
            parser.fullAuthors(authorStr, authorArray, new Integer[]{1800, null, null, null}, NomenclaturalCode.ICNAFP);
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

    @Test
    public final void testParseCultivar() {

        TaxonName name;
        String cultivar;

        //ICN name is not (yet?) converted to ICNCP name
        name = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        name.setGenusOrUninomial("Pinus");
        name.setSpecificEpithet("beta");
        cultivar = "Abies alba 'Albus'";
        parser.parseReferencedName(name, cultivar, null, true);
        Assert.assertEquals("Changing the code automatically is not implemented needs discussion of wanted", NomenclaturalCode.ICNAFP, name.getNameType());
        Assert.assertTrue(name.isProtectedTitleCache());

        //cultivar
        cultivar = "Abies 'Albus'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Albus'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        //same but using ...referencedName
        cultivar = "Abies 'Albus'";
        name = parser.parseReferencedName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Albus'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        //cultivar with author
        cultivar = "Abies 'Albus' Mill.";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Albus'", name.getNameCache());
        Assert.assertNotNull(name.getCombinationAuthorship());
        Assert.assertEquals("Mill.", name.getCombinationAuthorship().getTitleCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        //same with referenced name
        cultivar = "Abies 'Albus' Mill.";
        name = parser.parseReferencedName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Albus'", name.getNameCache());
        Assert.assertNotNull(name.getCombinationAuthorship());
        Assert.assertEquals("Mill.", name.getCombinationAuthorship().getTitleCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        //cultivar with author and incorrect basionym or ex author
        cultivar = "Abies 'Albus' (Basio) Mill.";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertTrue("Cultivar name should not have a basionym author", name.isProtectedTitleCache());
        cultivar = "Abies 'Albus' Mill. ex Meyer";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertTrue("Cultivar name should not have an ex-author", name.isProtectedTitleCache());

        //cultivar with author and nom. ref.
        cultivar = "Abies 'Albus' Mill. in Willdenovia 2: 23. 1983";
        name = parser.parseReferencedName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Albus'", name.getNameCache());
        Assert.assertNotNull(name.getCombinationAuthorship());
        Assert.assertEquals("Mill.", name.getCombinationAuthorship().getTitleCache());
        Assert.assertNotNull(name.getNomenclaturalReference());
        Assert.assertEquals("Mill. 1983: \u2013 Willdenovia 2", name.getNomenclaturalReference().getTitleCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'Beryl, Viscountess Cowdray'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Beryl, Viscountess Cowdray", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Beryl, Viscountess Cowdray'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'Jeanne d\u2019Arc'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Jeanne d\u2019Arc", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Jeanne d\u2019Arc'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'Oh Boy!'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Oh Boy!", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Oh Boy!'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'E.A. Bowles'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("E.A. Bowles", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'E.A. Bowles'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'ENT/100'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("ENT/100", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'ENT/100'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'Go-go  Dancer'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("Go-go Dancer", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'Go-go Dancer'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies 'ENT\\100'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("ENT\\100", name.getCultivarEpithet());
        Assert.assertEquals("Abies 'ENT\\100'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        cultivar = "Abies alba 'Albus'";
        name = (TaxonName)parser.parseFullName(cultivar);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertEquals("alba", name.getSpecificEpithet());
        Assert.assertEquals("Albus", name.getCultivarEpithet());
        Assert.assertEquals("Abies alba 'Albus'", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        //cultivar group
        String group = "Abies Albus Group";
        name = (TaxonName)parser.parseFullName(group);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus Group", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus Group", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivarGroup, name.getRank().getUuid());

        //same but using referenced name
        group = "Abies Albus Group";
        name = parser.parseReferencedName(group);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus Group", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus Group", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivarGroup, name.getRank().getUuid());

        group = "Abies Albus Gp";
        name = (TaxonName)parser.parseFullName(group);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus Gp", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus Gp", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivarGroup, name.getRank().getUuid());

        group = "Abies Gruppo Albus";
        name = (TaxonName)parser.parseFullName(group);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Gruppo Albus", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Gruppo Albus", name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivarGroup, name.getRank().getUuid());

        //grex
        String grex = "Abies Albus grex";
        name = (TaxonName)parser.parseFullName(grex);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus grex", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus grex", name.getNameCache());
        Assert.assertEquals(Rank.uuidGrexICNCP, name.getRank().getUuid());

        //same but using referenced name
        grex = "Abies Albus grex";
        name = parser.parseReferencedName(grex);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus grex", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus grex", name.getNameCache());
        Assert.assertEquals(Rank.uuidGrexICNCP, name.getRank().getUuid());

        grex = "Abies Albus Second grex";
        name = parser.parseReferencedName(grex);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Albus Second grex", name.getCultivarGroupEpithet());
        Assert.assertEquals("Abies Albus Second grex", name.getNameCache());
        Assert.assertEquals(Rank.uuidGrexICNCP, name.getRank().getUuid());

        //combined
        String combined = "Abies (Albus Gruppo) 'Pretty'";
        name = parser.parseReferencedName(combined);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Pretty", name.getCultivarEpithet());
        Assert.assertEquals("Albus Gruppo", name.getCultivarGroupEpithet());
        Assert.assertEquals(combined, name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        combined = "Abies White grex (Albus Gruppo) 'Pretty'";
        name = parser.parseReferencedName(combined);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("Pretty", name.getCultivarEpithet());
        Assert.assertEquals("White grex Albus Gruppo", name.getCultivarGroupEpithet());
        Assert.assertEquals(combined, name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivar, name.getRank().getUuid());

        combined = "Abies White grex Albus Gruppo";
        name = parser.parseReferencedName(combined);
        Assert.assertEquals("Abies", name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertEquals("White grex Albus Gruppo", name.getCultivarGroupEpithet());
        Assert.assertEquals(combined, name.getNameCache());
        Assert.assertEquals(Rank.uuidCultivarGroup, name.getRank().getUuid());

        //incorrect combinations
        combined = "Abies White grex (Albus Gruppo)";
        name = parser.parseReferencedName(combined);
        Assert.assertTrue(name.isProtectedTitleCache());
        Assert.assertNull(name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertNull(name.getCultivarEpithet());
        Assert.assertNull(name.getCultivarGroupEpithet());
        Assert.assertEquals(combined, name.getNameCache());
        Assert.assertNull(name.getRank());

        combined = "Abies Albus Gruppo 'Pretty'";
        name = parser.parseReferencedName(combined);
        Assert.assertTrue(name.isProtectedTitleCache());
        Assert.assertNull(name.getGenusOrUninomial());
        Assert.assertNull(name.getSpecificEpithet());
        Assert.assertNull(name.getCultivarEpithet());
        Assert.assertNull(name.getCultivarGroupEpithet());
        Assert.assertEquals(combined, name.getNameCache());
        Assert.assertNull(name.getRank());
    }

    //#7443
    @Test
    public final void testFungiWithInAuthors() {
        //test in-author and basionym in-author
        String nameStr = "Cora pavonia (Sw. in Weber & Mohr) Fr. in Montagne";
        TaxonName name = (TaxonName)parser.parseFullName(nameStr, FUNGI, Rank.SPECIES());
        assertInAuthors(name, false);
        Assert.assertEquals("(Sw. in Weber & Mohr) Fr. in Montagne", name.getAuthorshipCache());

        //referenced name
        name = parser.parseReferencedName(nameStr, FUNGI, Rank.SPECIES());
        assertInAuthors(name, false);
        Assert.assertEquals("(Sw. in Weber & Mohr) Fr. in Montagne", name.getAuthorshipCache());

        String referencedName = "Cora pavonia (Sw. in Weber & Mohr) Fr. in Montagne, Interesting thoughts about Fungi 2: 66. 1835";
        name = parser.parseReferencedName(referencedName, FUNGI, Rank.SPECIES());
        assertInAuthors(name, true);
        Assert.assertEquals("(Sw. in Weber & Mohr) Fr.", name.getAuthorshipCache());
        Reference inRef = name.getNomenclaturalReference().getInReference();
        Assert.assertEquals("Montagne", inRef.getAuthorship().getTitleCache());
        Assert.assertEquals("Interesting thoughts about Fungi", inRef.getAbbrevTitle());

        //test non-fungi ICNafp name
        name = (TaxonName)parser.parseFullName(nameStr, ICNAFP, Rank.SPECIES());
        Assert.assertTrue("Non-fungi names currently should not support in authors", name.isProtectedTitleCache());

        //... only combination in-author
        nameStr = "Cora pavonia (Sw.) Fr. in Montagne";
        name = (TaxonName)parser.parseFullName(nameStr, ICNAFP, Rank.SPECIES());
        Assert.assertTrue("Non-fungi names currently should not support in authors", name.isProtectedTitleCache());

        //... only basionym in-author
        nameStr = "Cora pavonia (Sw. in Weber & Mohr) Fr.";
        name = (TaxonName)parser.parseFullName(nameStr, ICNAFP, Rank.SPECIES());
        Assert.assertTrue("Non-fungi names currently should not support in authors", name.isProtectedTitleCache());
    }

    //#7443
    @Test
    public final void testZooWithInAuthors() {
        //test in-author and basionym in-author
        String nameStr = "Cora pavonia (Sw. in Weber & Mohr, 1825) Fr. in Montagne, 1830";
        TaxonName name = (TaxonName)parser.parseFullName(nameStr, ICZN, Rank.SPECIES());
        assertInAuthors(name, false);
        Assert.assertEquals("(Sw. in Weber & Mohr, 1825) Fr. in Montagne, 1830", name.getAuthorshipCache());
    }

    private void assertInAuthors(TaxonName name, boolean noInAuthor) {
        Assert.assertFalse(name.isProtectedTitleCache());
        Assert.assertFalse(name.isProtectedNameCache());
        Assert.assertFalse(name.isProtectedAuthorshipCache());
        Assert.assertEquals("Cora", name.getGenusOrUninomial());
        Assert.assertEquals("pavonia", name.getSpecificEpithet());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        Assert.assertEquals("Fr.", combinationAuthor.getTitleCache());
        TeamOrPersonBase<?> combinationInAuthor = name.getInCombinationAuthorship();
        if (noInAuthor) {
            Assert.assertNull(combinationInAuthor);
            Assert.assertNotNull(name.getNomenclaturalReference());
        }else {
            Assert.assertEquals("Montagne", combinationInAuthor.getTitleCache());
            Assert.assertNull(name.getNomenclaturalReference());
        }
        TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
        Assert.assertEquals("Sw.", basionymAuthor.getTitleCache());
        TeamOrPersonBase<?> basionymInAuthor = name.getInBasionymAuthorship();
        Assert.assertEquals("Weber & Mohr", basionymInAuthor.getTitleCache());
        Assert.assertNull(name.getExCombinationAuthorship());
        Assert.assertNull(name.getExBasionymAuthorship());
    }

    @Test
    public final void testNomenclaturalStatus() {
        IBotanicalName name = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY(), "Acanthopale", null, null, null, null, null, null, null);
        name.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
        IBotanicalName name2 = TaxonNameFactory.NewBotanicalInstance(Rank.FAMILY());
        parser.parseReferencedName(name2, name.getFullTitleCache(), name2.getRank(), true);
        parser.parseReferencedName(name2, name.getFullTitleCache(), name2.getRank(), true);
        Assert.assertEquals("Title cache should be same. No duplication of nom. status should take place", name.getFullTitleCache(), name2.getFullTitleCache());

        //desig. inval. #10533
        String str = "Abies alba Mill., desig. inval.";
        TaxonName name3 = parser.parseReferencedName(str);
        Assert.assertEquals(1, name3.getStatus().size());
        Assert.assertEquals(NomenclaturalStatusType.INVALID(), name3.getStatus().iterator().next().getType());

        //pro hybr.+pro sp. #10478
        str = "Abies alba Mill., pro hybr.";
        name3 = parser.parseReferencedName(str);
        Assert.assertEquals(1, name3.getStatus().size());
        Assert.assertEquals(NomenclaturalStatusType.PRO_HYBRID(), name3.getStatus().iterator().next().getType());
        str = "Abies alba Mill., pro sp., nom. illeg.";
        name3 = parser.parseReferencedName(str);
        Assert.assertEquals(2, name3.getStatus().size());
        Set<NomenclaturalStatusType> statusTypes = name3.getStatus().stream().map(s->s.getType()).collect(Collectors.toSet());
        Assert.assertTrue(statusTypes.contains(NomenclaturalStatusType.PRO_SPECIES()));
        Assert.assertTrue(statusTypes.contains(NomenclaturalStatusType.ILLEGITIMATE()));
    }

    @Test
    public final void testSpecificAuthors(){
        //McVaugh
        INonViralName name = parser.parseFullName("Psidium longipes var. orbiculare (O.Berg) McVaugh");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "McVaugh", combinationAuthor.getNomenclaturalTitleCache());
        TeamOrPersonBase<?> basionymAuthor = name.getBasionymAuthorship();
        assertEquals( "O.Berg", basionymAuthor.getNomenclaturalTitleCache());

//      Campanula rhodensis A. DC.

    }

    @Test
    public final void testBookSectionAuthors(){
        INonViralName name;
        Reference nomRef;
        String title;
        String str;

        str = "Pancratium sickenbergeri Asch. & Schweinf. in Barbey-Boissier & Barbey, Herb. Levant: 158. 1882";
        str = "Pancratium sickenbergeri Asch. & Schweinf. in Barbey-Boissier & Barbey, Herb. Levant: 158. 1882";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Asch. & Schweinf.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "Barbey-Boissier & Barbey", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        title = nomRef.getInReference().getAbbrevTitle();
        assertEquals( "Herb. Levant", title);

        name = parser.parseReferencedName("Luzula multiflora subsp. pallescens (Sw.) Reichg. in Van Ooststroom & al., Fl. Neerl. 1: 208. 1964");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "Reichg.", name.getCombinationAuthorship().getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "Van Ooststroom & al.", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        title = nomRef.getInReference().getAbbrevTitle();
        assertEquals( "Fl. Neerl.", title);

        str = "Salvia pratensis var. albiflora T. Durand in De Wildeman & Durand, Prodr. Fl. Belg. 3: 663. 1899";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "T. Durand", name.getCombinationAuthorship().getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "De Wildeman & Durand", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        title = nomRef.getInReference().getAbbrevTitle();
        assertEquals( "Prodr. Fl. Belg.", title);

        str = "Bravoa Lex. in La Llave & Lexarza, Nov. Veg. Desc. 1: 6. 1824";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "Lex.", name.getCombinationAuthorship().getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "La Llave & Lexarza", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        title = nomRef.getInReference().getAbbrevTitle();
        assertEquals( "Nov. Veg. Desc.", title);

        str = "Thymus trachselianus var. vallicola Heinr. Braun in Dalla Torre & Sarnthein, Fl. Tirol 6(3): 204. 1912";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "Dalla Torre & Sarnthein", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        title = nomRef.getInReference().getAbbrevTitle();
        assertEquals( "Fl. Tirol", title);

        //see #openIssues
//        str = "Iris xiphium var. lusitanica (Ker Gawl.) Franco in Amaral Franco & Rocha Afonso, Nova Fl. Portugal 3: 135. 1994";
//        name = parser.parseReferencedName(str);
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
//        nomRef = name.getNomenclaturalReference();
//        assertEquals(ReferenceType.BookSection, nomRef.getType());
//        assertEquals( "Amaral Franco & Rocha Afonso", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
//        title = nomRef.getInReference().getAbbrevTitle();
//        assertEquals( "Nova Fl. Portugal", title);
//
//        str = "Fritillaria mutabilis Kamari in Strid & Kit Tan, Mount. Fl. Greece 2: 679. 1991";
//        name = parser.parseReferencedName(str);
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
//        nomRef = name.getNomenclaturalReference();
//        assertEquals(ReferenceType.BookSection, nomRef.getType());
//        assertEquals( "Strid & Kit Tan", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
//        title = nomRef.getInReference().getAbbrevTitle();
//        assertEquals( "Mount. Fl. Greece", title);

    }

    @Test
    public final void testDatePublished(){

        INonViralName name = parser.parseReferencedName("Calamintha transsilvanica (J\u00e1v.) So\u00f3 in Acta Bot. Acad. Sci. Hung. 23: 382. 1977 publ. 1978");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("1978 [\"1977\"]", nomRef.getDatePublished().toString());

        name = parser.parseReferencedName("Calamintha transsilvanica (J\u00e1v.) So\u00f3 in Acta Bot. Acad. Sci. Hung. 23: 382. 1977 [\"1978\"]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("1977 [\"1978\"]", nomRef.getDatePublished().toString());
        assertEquals("1978", nomRef.getDatePublished().getVerbatimDate());

        name = parser.parseReferencedName("Calamintha transsilvanica (J\u00e1v.) So\u00f3 in Acta Bot. Acad. Sci. Hung. 23: 382. 4 Apr 1977");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4 Apr 1977", nomRef.getDatePublished().toString());
        assertEquals(Integer.valueOf(4), nomRef.getDatePublished().getStartMonth());

        name = parser.parseReferencedName("Calamintha transsilvanica (J\u00e1v.) So\u00f3 in Acta Bot. Acad. Sci. Hung. 23: 382. Feb-Apr 1977");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("Feb"+SEP+"Apr 1977", nomRef.getDatePublished().toString());
        assertEquals(Integer.valueOf(2), nomRef.getDatePublished().getStartMonth());
        assertEquals(Integer.valueOf(4), nomRef.getDatePublished().getEndMonth());
        assertEquals(Integer.valueOf(1977), nomRef.getDatePublished().getStartYear());
        assertEquals(Integer.valueOf(1977), nomRef.getDatePublished().getEndYear());
        assertNull(nomRef.getDatePublished().getStartDay());
        assertNull(nomRef.getDatePublished().getEndDay());
    }


    @Test
    public final void testExistingProblems(){

        INonViralName name;
        String str = "Cerastium nutans var. occidentale Boivin in Canad. Field-Naturalist 65: 5. 1951";
        name = parser.parseReferencedName(str);
        Assert.assertTrue(isParsable(str, NomenclaturalCode.ICNAFP));

        //Canabio, issue with space
        name = parser.parseReferencedName("Machaonia erythrocarpa var. hondurensis (Standl.) Borhidi"
                + " in Acta Bot. Hung. 46 (1-2): 30. 2004");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        TeamOrPersonBase<?> combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi", combinationAuthor.getNomenclaturalTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("46 (1-2)", nomRef.getVolume());

        //Man in 't Veld  #6100
        String nameStr = "Phytophthora multivesiculata Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters",
                name.getCombinationAuthorship().getTitleCache());
        assertEquals("Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters",
                name.getCombinationAuthorship().getNomenclaturalTitleCache());

        //Canabio, detail with fig.
        name = parser.parseReferencedName("Didymaea floribunda Rzed."
                + " in Bol. Soc. Bot. Mex. 44: 72, fig. 1. 1983");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Rzed.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("44", nomRef.getVolume());
        assertEquals("72, fig. 1", name.getNomenclaturalMicroReference());

        //fig with a-c and without dot
        name = parser.parseReferencedName("Deppea guerrerensis Dwyer & Lorence"
                + " in Allertonia 4: 428. fig 4a-c. 1988");  //
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Dwyer & Lorence", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("428. fig 4a-c", name.getNomenclaturalMicroReference());

        //issue with EN_DASH (3–4)
        name = parser.parseReferencedName("Arachnothryx tacanensis (Lundell) Borhidi"
              + " in Acta Bot. Hung. 33 (3" + UTF8.EN_DASH + "4): 303. 1987");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("33 (3" + UTF8.EN_DASH + "4)", nomRef.getVolume());
        assertEquals("303", name.getNomenclaturalMicroReference());

        //fig with f.
        name = parser.parseReferencedName("Stenotis Terrell"
                + " in Sida 19(4): 901" + UTF8.EN_DASH + "911, f. 1" + UTF8.EN_DASH + "2. 2001");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Terrell", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("19(4)", nomRef.getVolume());
        assertEquals("901" + UTF8.EN_DASH + "911, f. 1" + UTF8.EN_DASH + "2", name.getNomenclaturalMicroReference());

        //detail with figs
        name = parser.parseReferencedName("Randia sonorensis Wiggins"
                + " in Contr. Dudley Herb. 3: 75, figs 4-6. 1940");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wiggins", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("75, figs 4-6", name.getNomenclaturalMicroReference());

        //detail with pl. and figs
        name = parser.parseReferencedName("Randia sonorensis Wiggins"
                + " in Contr. Dudley Herb. 3: 75, pl. 19, figs 4-6. 1940");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wiggins", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("75, pl. 19, figs 4-6", name.getNomenclaturalMicroReference());

        //pl
        name = parser.parseReferencedName("Carapichea  Aubl."
                + " in Hist. Pl. Guiane 1: 167, pl. 64. 1775");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Aubl.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("1", nomRef.getVolume());
        assertEquals("167, pl. 64", name.getNomenclaturalMicroReference());

        //fig with ,
        name = parser.parseReferencedName("Hoffmannia ixtlanensis Lorence"
                + " in Novon 4: 121. fig. 2a, b. 1994");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Lorence", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("121. fig. 2a, b", name.getNomenclaturalMicroReference());

        //detail with , to number
        name = parser.parseReferencedName("Deppea martinez-calderonii Lorence"
                + " in Allertonia 4: 399. figs 1e, 2. 1988");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Lorence", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("4", nomRef.getVolume());
        assertEquals("399. figs 1e, 2", name.getNomenclaturalMicroReference());

        //(Suppl.)
        name = parser.parseReferencedName("Manettia costaricensis  Wernham"
                + " in J. Bot. 57(Suppl.): 38. 1919");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Wernham", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("57(Suppl.)", nomRef.getVolume());
        assertEquals("38", name.getNomenclaturalMicroReference());

        //NY.
        name = parser.parseReferencedName("Crusea psyllioides (Kunth) W.R. Anderson"
                + " in Mem. NY. Bot. Gard. 22: 75. 1972");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "W.R. Anderson", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("22", nomRef.getVolume());
        assertEquals("75", name.getNomenclaturalMicroReference());

        //apostroph word in title
        name = parser.parseReferencedName("Sabicea glabrescens Benth."
                + " in Hooker's J. Bot. Kew Gard. Misc. 3: 219. 1841");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Benth.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("3", nomRef.getVolume());
        assertEquals("219", name.getNomenclaturalMicroReference());

        // place published e.g. (Hannover)
        name = parser.parseReferencedName("Pittoniotis trichantha Griseb."
                  + " in Bonplandia (Hannover) 6 (1): 8. 1858");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Griseb.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("6 (1)", nomRef.getVolume());
        assertEquals("8", name.getNomenclaturalMicroReference());

        //komplex / incorrect year without quotation marks
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1\u20134): 16, f. 1\u20132, t. 1-8. 1983 [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1\u20134)", nomRef.getVolume());
        assertEquals("16, f. 1\u20132, t. 1-8", name.getNomenclaturalMicroReference());
        assertEquals("1983 [1984]", nomRef.getDatePublishedString());
//        assertEquals("1984", nomRef.getYear()); //was like this, but is not necessarily correct, see #7429

        //incorrect year with \u201e \u201f  (s. eu.etaxonomy.cdm.common.UTF8.ENGLISH_QUOT_START
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1-4): 16, f. 1-2. \u201e1983\u201f [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1-4)", nomRef.getVolume());
        assertEquals("16, f. 1-2", name.getNomenclaturalMicroReference());
        assertEquals("1984 [\"1983\"]", nomRef.getDatePublishedString());
        assertEquals("1984", nomRef.getYear());

        //incorrect year with "
        name = parser.parseReferencedName("Javorkaea Borhidi & Jarai-Koml."
                + " in Acta Bot. Hung. 29(1-4): 16, f. 1-2. \"1983\" [1984]");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Borhidi & Jarai-Koml.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("29(1-4)", nomRef.getVolume());
        assertEquals("16, f. 1-2", name.getNomenclaturalMicroReference());
        //changed from "1983" [1984] to 1984 ["1983"] after implementing #7429
        assertEquals("1984 [\"1983\"]", nomRef.getDatePublishedString());
        assertEquals("1984", nomRef.getYear());

        //fig. a
        name = parser.parseReferencedName("Psychotria capitata  Ruiz & Pav."
                + " in Fl. Peruv. 2: 59, pl. 206, fig. a. 1799");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Ruiz & Pav.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("2", nomRef.getVolume());
        assertEquals("59, pl. 206, fig. a", name.getNomenclaturalMicroReference());

        //442A.
        name = parser.parseReferencedName("Rogiera elegans Planch."
                + " in Fl. Serres Jard. Eur. 5: 442A. 1849");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Planch.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("5", nomRef.getVolume());
        assertEquals("442A", name.getNomenclaturalMicroReference());

        //f
        name = parser.parseReferencedName("Coussarea imitans L.O. Williams"
                + " in Phytologia 26 (6): 488-489, f. 1973");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "L.O. Williams", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.Article, nomRef.getType());
        assertEquals("26 (6)", nomRef.getVolume());
        assertEquals("488-489, f", name.getNomenclaturalMicroReference());

        //Phys.-Med.
        name = parser.parseReferencedName("Coccocypselum cordifolium Nees & Mart."
                + " in Nova Acta Phys.-Med. Acad. Caes.\u2013Leop. Nat. Cur. 12: 14. 1824");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        combinationAuthor = name.getCombinationAuthorship();
        assertEquals( "Nees & Mart.", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
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
//        assertEquals( "L.", combinationAuthor.getNomenclaturalTitleCache());
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
        assertEquals( "Schinz", combinationAuthor.getNomenclaturalTitleCache());
        nomRef = name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", nomRef.isProtectedTitleCache());
        assertEquals(ReferenceType.Book, nomRef.getType());
        assertEquals("Nat. Pflanzenfam.", nomRef.getAbbrevTitle());
        assertEquals("3(1a)", nomRef.getVolume());
        assertEquals("109", name.getNomenclaturalMicroReference());
        assertEquals("1893", nomRef.getYear());

        //Accent graph in author name #6057
        name = parser.parseReferencedName("Sedum plicatum O`Brian");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "O`Brian", name.getCombinationAuthorship().getNomenclaturalTitleCache());

        //-e-  #6060
        name = parser.parseReferencedName("Thamniopsis stenodictyon (Sehnem) Oliveira-e-Silva & O.Yano");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Team team = (Team)name.getCombinationAuthorship();
        assertEquals( "Oliveira-e-Silva", team.getTeamMembers().get(0).getNomenclaturalTitleCache());

        //Vorabdr.
        name = parser.parseReferencedName("Ophrys hystera  Kreutz & Ruedi Peter in J. Eur. Orchideen 30(Vorabdr.): 128. 1997");
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "30(Vorabdr.)", name.getNomenclaturalReference().getVolume());

        //#6100  jun.
        nameStr = "Swida \u00D7 friedlanderi (W.H.Wagner jun.) Holub";
        name = parser.parseFullName(nameStr, botanicCode, null);  //fails with missing botanicCode, see open issues
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "W.H.Wagner jun.", name.getBasionymAuthorship().getTitleCache());

        //#6100 bis /ter
        nameStr = "Schistidium aquaticum (R.Br.ter) Ochyra";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "R.Br.ter", name.getBasionymAuthorship().getTitleCache());

        nameStr = "Grimmia mitchellii R.Br.bis";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "R.Br.bis", name.getCombinationAuthorship().getTitleCache());

        //forma #6100
        nameStr = "Xerocomus parasiticus forma piperatoides (J. Blum) R. Mazza";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "piperatoides", name.getInfraSpecificEpithet());
        assertEquals( Rank.FORM(), name.getRank());

        //subgen. #6100
        nameStr = "Aliciella subgen. Gilmania (H.Mason & A.D.Grant) J.M.Porter";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "Gilmania", name.getInfraGenericEpithet());
        assertEquals( Rank.SUBGENUS(), name.getRank());

        //subgen. #6100
        nameStr = "Aliciella subgen. Gilmania J.M.Porter";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "Gilmania", name.getInfraGenericEpithet());
        assertEquals( Rank.SUBGENUS(), name.getRank());
        assertEquals( "J.M.Porter", name.getCombinationAuthorship().getTitleCache());

        //la Croix #6100
        nameStr = "Eulophia ovalis var. bainesii (Rolfe) P.J.Cribb & la Croix";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "P.J.Cribb & la Croix", name.getCombinationAuthorship().getTitleCache());

        //I = Yi #6100
        nameStr = "Parasenecio hwangshanicus (P.I Mao) C.I Peng";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("I (=Yi) should be an accepted ending", "C.I Peng", name.getCombinationAuthorship().getTitleCache());
        assertEquals("I (=Yi) should be an accepted ending", "P.I Mao", name.getBasionymAuthorship().getTitleCache());

        //´t Hart #6100
        nameStr = "Sedum decipiens (Baker) Thiede & \u00B4t Hart";   //still does not work with "´", don't know what the difference is, see openIssues()
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("All types of quotation marks should be accepted, though better match it to standard ' afterwards",
                "Thiede & \u00B4t Hart", name.getCombinationAuthorship().getTitleCache());

        //Man in 't Veld  #6100
        nameStr = "Phytophthora multivesiculata Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters",
                name.getCombinationAuthorship().getTitleCache());
        assertEquals("Ilieva, Man in 't Veld, Veenbaas-Rijks & Pieters",
                name.getCombinationAuthorship().getNomenclaturalTitleCache());

        nameStr = "Thymus \u00D7 herberoi De la Torre, Vicedo, Alonso & Paya";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        //TODO may become ... Alonso & al. again in future
        assertEquals("Thymus \u00D7herberoi De la Torre, Vicedo, Alonso & Paya", name.getTitleCache());
        assertEquals("De la Torre, Vicedo, Alonso & Paya",
                name.getCombinationAuthorship().getTitleCache());
        assertEquals("De la Torre, Vicedo, Alonso & Paya",
                name.getCombinationAuthorship().getNomenclaturalTitleCache());

        //Sant'Anna
        nameStr = "Coelosphaerium evidenter-marginatum M.T.P.Azevedo & Sant'Anna";
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("M.T.P.Azevedo & Sant'Anna", name.getCombinationAuthorship().getTitleCache());

        //Heft
        nameStr = "Nepenthes deaniana Macfarl. in Engl., Mein Pflanzenr. IV. 111 (Heft 36): 57. 1908.";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference ref = name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", ref.hasProblem());
        //or even better IV. 111 (Heft 36), but this is currently not implemented
        assertEquals("111 (Heft 36)", ref.getInReference().getVolume());

        //journal with commata at pos 4
        nameStr = "Bufonia kotschyana subsp. densa Chrtek & Krisa in Acta Univ.Carol., Biol. 43(2): 105. 1999";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        String author = name.getAuthorshipCache();
        assertEquals("Chrtek & Krisa", author);
        ref = name.getNomenclaturalReference();
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());

        //Adansonia #9014, #9551
        nameStr = "Casearia annamensis (Gagnep.) Lescot & Sleumer in Adansonia, n.s., 10: 290. 1970";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("Adansonia", ref.getInReference().getAbbrevTitle());

        //, Bot., sér. 4 #9014, #9551
        String ser = "s"+UTF8.SMALL_E_ACUTE+"r";
        nameStr = "Asteropeia amblyocarpa Tul. in Ann. Sci. Nat., Bot., "+ser+". 4, 8: 81. 1857";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("Ann. Sci. Nat., Bot.", ref.getInReference().getAbbrevTitle());
        Assert.assertEquals(ser+". 4", ref.getSeriesPart());

        // Misc. 89   #9014
        nameStr = "Bulbophyllum sordidum Lindl. in Edwards's Bot. Reg. 26: Misc. 89. 1840";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        String detail = name.getNomenclaturalMicroReference();
        Assert.assertEquals("Misc. 89", detail);
        ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("Edwards's Bot. Reg.", ref.getInReference().getAbbrevTitle());

        // #9014 ... in Sitzungsber. Math.-Phys. Cl. Königl. Bayer. Akad. Wiss. München 14: 489. 1884
        nameStr = "Daphnopsis cuneata Radlk. in Sitzungsber. Math.-Phys. Cl. Königl. Bayer. Akad. Wiss. München 14: 489. 1884";
        name = parser.parseReferencedName(nameStr);
        Assert.assertTrue(isParsable(nameStr, ICNAFP));
        Assert.assertEquals("489", name.getNomenclaturalMicroReference());
        ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("Sitzungsber. Math.-Phys. Cl. Königl. Bayer. Akad. Wiss. München", ref.getInReference().getAbbrevTitle());
        Assert.assertEquals("14", ref.getVolume());
    }

    @Test
    public final void explicitJournalTitles(){
        //PhytoKeys #9550
        String nameStr = "Pseudopodospermum baeticum (DC.) Zaika & al. in PhytoKeys 137: 68. 2020";
        TaxonName name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference ref = name.getNomenclaturalReference();
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("PhytoKeys", ref.getInReference().getAbbrevTitle());

        //PLoS ONE #9550 //remaining issue: ", e82692" #9552
//        nameStr = "Pseudopodospermum baeticum (DC.) Zaika & al. in PLoS ONE 8(12), e82692: 17. 2013";
        nameStr = "Pseudopodospermum baeticum (DC.) Zaika & al. in PLoS ONE 8(12): 17. 2013";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        ref = name.getNomenclaturalReference();
        Assert.assertNotNull("Nomenclatural reference should be an article and therefore have an in reference", ref.getInReference());
        Assert.assertEquals(ReferenceType.Journal, ref.getInReference().getType());
        Assert.assertEquals("PLoS ONE", ref.getInReference().getAbbrevTitle());
    }

    @Test
    //#3666
    public final void testOriginalSpelling(){

        String nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"alpa\"]";
        TaxonName name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference ref = name.getNomenclaturalReference();
        Assert.assertFalse("Reference should be parsable", ref.isProtectedTitleCache());
        TaxonName originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertNotNull("An original spelling should exist", originalName);
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa", originalName.getNameCache());
        Assert.assertEquals("Sp. Pl.", name.getNomenclaturalSource().getCitation().getAbbrevTitle());
        Assert.assertEquals("1751", name.getNomenclaturalSource().getCitation().getYear());

        //without ref
        nameStr = "Abies alba Mill [as \"alpa\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa", originalName.getNameCache());

        //without author
        nameStr = "Abies alba [as \"alpa\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa", originalName.getNameCache());

        //with status
        nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"alpa\"], nom. inval.";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa", originalName.getNameCache());
        Assert.assertEquals(1, name.getStatus().size());

        nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"Abies alpa\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa", originalName.getNameCache());

        nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"Apies alpa\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Apies alpa", originalName.getNameCache());

        nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"Apies\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Apies alba", originalName.getNameCache());

        nameStr = "Abies alba subsp. beta Mill, Sp. Pl. 2: 333. 1751 [as \"peta\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alba subsp. peta", originalName.getNameCache());

        nameStr = "Abies alba subsp. beta Mill, Sp. Pl. 2: 333. 1751 [as \"alpa subsp. peta\"]";
        name = parser.parseReferencedName(nameStr);
        originalName = name.getNomenclaturalSource().getNameUsedInSource();
        Assert.assertFalse("Namecache should not be parsable", originalName.isProtectedNameCache());
        Assert.assertEquals("Abies alpa subsp. peta", originalName.getNameCache());

        //unparsable
        nameStr = "Abies alba Mill, Sp. Pl. 2: 333. 1751 [as \"alpa Err\"]";
        Assert.assertTrue("Reference should notbe parsable", parser.parseReferencedName(nameStr).getNomenclaturalReference().isProtectedTitleCache());

    }

    @Test
    public final void testHort(){
        String nameStr = "Epidendrum ciliare var. minor hort. ex Stein";
        TaxonName name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Assert.assertEquals("Epidendrum ciliare var. minor", name.getNameCache());
        Assert.assertEquals("hort.", name.getExCombinationAuthorship().getNomenclaturalTitleCache());
        Assert.assertEquals("Stein", name.getCombinationAuthorship().getNomenclaturalTitleCache());
    }

    @Test
    @Ignore
    public final void openIssues(){
        //#6100  jun.
        String nameStr = "Swida \u00D7 friedlanderi (W.H.Wagner jun.) Holub";
        INonViralName name = parser.parseFullName(nameStr, botanicCode, null);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "W.H.Wagner jun.", name.getBasionymAuthorship().getTitleCache());
        name = parser.parseFullName(nameStr);  //fails for some reasons without botanicCode given, as anyBotanicFullName is not recognized, strange because other very similar names such as Thymus \u00D7 herberoi De la Torre, Vicedo, Alonso & Paya do not fail
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals( "W.H.Wagner jun.", name.getBasionymAuthorship().getTitleCache());

        //´t Hart #6100
        nameStr = "Sedum decipiens (Baker) Thiede & \u00B4t Hart";   //still does not work with "´" if compiled by maven, don't know what the difference is
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("All types of quotation marks should be accepted, though better match it to standard ' afterwards",
                "Thiede & \u00B4t Hart", name.getCombinationAuthorship().getTitleCache());
        nameStr = "Sedum decipiens (Baker) Thiede & ´t Hart";   //does not work if compiled with maven
        name = parser.parseFullName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        assertEquals("All types of quotation marks should be accepted, though better match it to standard ' afterwards",
                "Thiede & ´t Hart", name.getCombinationAuthorship().getTitleCache());

        //should be recognized as book section (see testBookSectionAuthors)
        String str = "Iris xiphium var. lusitanica (Ker Gawl.) Franco in Amaral Franco & Rocha Afonso, Nova Fl. Portugal 3: 135. 1994";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "Amaral Franco & Rocha Afonso", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        assertEquals( "Nova Fl. Portugal", nomRef.getInReference().getAbbrevTitle());

        //same
        str = "Fritillaria mutabilis Kamari in Strid & Kit Tan, Mount. Fl. Greece 2: 679. 1991";
        name = parser.parseReferencedName(str);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        nomRef = name.getNomenclaturalReference();
        assertEquals(ReferenceType.BookSection, nomRef.getType());
        assertEquals( "Strid & Kit Tan", nomRef.getInReference().getAuthorship().getNomenclaturalTitleCache());
        assertEquals( "Mount. Fl. Greece", nomRef.getInReference().getAbbrevTitle());


        //, Ser. B, Div. 2, Bot.   from E+M mosses import
        nameStr = "Schistidium subconfertum (Broth.) Deguchi in J. Sci. Hiroshima Univ., Ser. B, Div. 2, Bot. 16: 240. 1979";
        name = parser.parseReferencedName(nameStr);
        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
        Reference ref = name.getNomenclaturalReference();
        Assert.assertEquals(ReferenceType.Article, ref.getType());
        Reference inRef = ref.getInReference();
        Assert.assertEquals("J. Sci. Hiroshima Univ., Ser. B, Div. 2, Bot.", inRef.getAbbrevTitle());
        Assert.assertEquals(null, ref.getSeriesPart());
        Assert.assertEquals("60", ref.getVolume());
        Assert.assertEquals(nameStr, name.getFullTitleCache());
        String detail = name.getNomenclaturalMicroReference();
        Assert.assertEquals("240", detail);
    }

    //this is a slot for testing new string, once the Strings tested here work move the according test
    //to one of the above methods (or remove it if not needed)
    @Test
    public final void testNew(){
//        String nameStr = "Eclipta humilis Kunth, Nov. Gen. Sp. Pl. (folio ed.) 4. 1820 [1818]";
//
//        TaxonName name = parser.parseReferencedName(nameStr);
//        Assert.assertFalse("Name should be parsable", name.isProtectedTitleCache());
//        Assert.assertFalse("Name should be parsable", name.isProtectedFullTitleCache());
//        Assert.assertEquals("Eclipta humilis", name.getNameCache());
//        Reference nomRef = name.getNomenclaturalReference();
    }

}