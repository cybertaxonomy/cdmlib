/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import org.junit.Assert;
import org.junit.Test;

import eu.etaxonomy.cdm.common.UTF8;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * @author a.mueller
 * @date 05.03.2022
 */
public class CollectorParserTest {

    private CollectorParser parser = CollectorParser.Instance();

    @Test
    public void testParse() {

        //default
        Team team = (Team)parser.parse("C.L.E. Contreras, M. R. Mariaca & M. "+UTF8.CAPITAL_A_ACUTE+".P"+UTF8.SMALL_E_ACUTE+"rez-Farrera")
                .getEntity();
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma incorrect
        team = (Team)parser.parse("CLE Contreras, MR Mariaca  & M."+UTF8.CAPITAL_A_ACUTE+". P"+UTF8.SMALL_E_ACUTE+"rez-Farrera")
                .getEntity();
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertNull(team.getTeamMembers().get(0).getFamilyName());
        Assert.assertNull(team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("CLE Contreras", team.getTeamMembers().get(0).getTitleCache());

//        //person
//        Person person = (Person)parser.parse("Contreras C.L.E.");
//        Assert.assertEquals("Contreras", person.getFamilyName());
//        Assert.assertEquals("C.L.E.", person.getInitials());
//        Assert.assertEquals("Contreras, C.L.E.", person.getTitleCache());


        //et al.
        team = (Team)parser.parse("C.L.E.Contreras et al").getEntity();
        Assert.assertEquals(1, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertTrue(team.isHasMoreMembers());
        Assert.assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //& al.
        team = (Team)parser.parse("C.L.E.Contreras & al.").getEntity();
        Assert.assertTrue(team.isHasMoreMembers());
        Assert.assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //
        team = (Team)parser.parse("M. M. de L.Maldonado, M. J. de A. Velazquez & de la R. M. A. Borja").getEntity();
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Velazquez", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("M. J. de A.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("Borja", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("de la R. M. A.", team.getTeamMembers().get(2).getInitials());

        //initials with hyphen
        team = (Team)parser.parse("B.Moncada, R.-E. Pérez-Pérez & R.Lücking").getEntity();
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Moncada", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("B.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Pérez-Pérez", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("R.-E.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("Lücking", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("R.", team.getTeamMembers().get(2).getInitials());
    }

    @Test
    public void testParseInitialsBehindComma() {

        Person person = (Person)parser.parse("Schweinfurt,C.").getEntity();
        Assert.assertEquals("Schweinfurt", person.getFamilyName());
        Assert.assertEquals("C.", person.getInitials());

        Team team = (Team)parser.parse("Contreras,C.L.E., Mariaca, M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera, M. "+UTF8.CAPITAL_A_ACUTE+".")
                .getEntity();
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //FIXME Velazquez, M J de A  is not yet correctly recognized
        team = (Team)parser.parse("Maldonado, M. M. de L., Velazquez, M J de A & Nanez, J. S.").getEntity();
//        System.out.println(team.getCollectorTitleCache());
//        Assert.assertEquals(3, team.getTeamMembers().size());
//        Assert.assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
//        Assert.assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
//        Assert.assertEquals("M J de A", team.getTeamMembers().get(1).getInitials());

        //FIXME needs discussion how to handle; the below result is not necessarily the expected one
        //      A solution could be to not return the parsed result but a protectedCollectorCache
        //      Team or Person
        ParserResult<TeamOrPersonBase<?>> parseResult = parser.parse("Miller, John & White, Roger");
        Assert.assertTrue(!parseResult.getWarnings().isEmpty());
        team = (Team)parseResult.getEntity();

        Assert.assertEquals(4, team.getTeamMembers().size());
        Assert.assertEquals("Miller", team.getTeamMembers().get(0).getTitleCache());
        Assert.assertEquals("John", team.getTeamMembers().get(1).getTitleCache());
        Assert.assertEquals("White", team.getTeamMembers().get(2).getTitleCache());
        Assert.assertEquals("Roger", team.getTeamMembers().get(3).getTitleCache());


    }

}
