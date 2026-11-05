/**
* Copyright (C) 2022 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma incorrect
        team = (Team)parser.parse("CLE Contreras, MR Mariaca  & M."+UTF8.CAPITAL_A_ACUTE+". P"+UTF8.SMALL_E_ACUTE+"rez-Farrera")
                .getEntity();
        assertEquals(3, team.getTeamMembers().size());
        assertNull(team.getTeamMembers().get(0).getFamilyName());
        assertNull(team.getTeamMembers().get(0).getInitials());
        assertEquals("CLE Contreras", team.getTeamMembers().get(0).getTitleCache());

//        //person
//        Person person = (Person)parser.parse("Contreras C.L.E.");
//        assertEquals("Contreras", person.getFamilyName());
//        assertEquals("C.L.E.", person.getInitials());
//        assertEquals("Contreras, C.L.E.", person.getTitleCache());


        //et al.
        team = (Team)parser.parse("C.L.E.Contreras et al").getEntity();
        assertEquals(1, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertTrue(team.isHasMoreMembers());
        assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //& al.
        team = (Team)parser.parse("C.L.E.Contreras & al.").getEntity();
        assertTrue(team.isHasMoreMembers());
        assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //
        team = (Team)parser.parse("M. M. de L.Maldonado, M. J. de A. Velazquez & de la R. M. A. Borja").getEntity();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Velazquez", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M. J. de A.", team.getTeamMembers().get(1).getInitials());
        assertEquals("Borja", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("de la R. M. A.", team.getTeamMembers().get(2).getInitials());

        //initials with hyphen
        team = (Team)parser.parse("B.Moncada, R.-E. Pérez-Pérez & R.Lücking").getEntity();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Moncada", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("B.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Pérez-Pérez", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("R.-E.", team.getTeamMembers().get(1).getInitials());
        assertEquals("Lücking", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("R.", team.getTeamMembers().get(2).getInitials());
    }

    @Test
    public void testParseInitialsBehindComma() {

        Person person = (Person)parser.parse("Schweinfurt,C.").getEntity();
        assertEquals("Schweinfurt", person.getFamilyName());
        assertEquals("C.", person.getInitials());

        Team team = (Team)parser.parse("Contreras,C.L.E., Mariaca, M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera, M. "+UTF8.CAPITAL_A_ACUTE+".")
                .getEntity();
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //FIXME Velazquez, M J de A  is not yet correctly recognized
        team = (Team)parser.parse("Maldonado, M. M. de L., Velazquez, M J de A & Nanez, J. S.").getEntity();
//        System.out.println(team.getCollectorTitleCache());
//        assertEquals(3, team.getTeamMembers().size());
//        assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
//        assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
//        assertEquals("M J de A", team.getTeamMembers().get(1).getInitials());

        //FIXME needs discussion how to handle; the below result is not necessarily the expected one
        //      A solution could be to not return the parsed result but a protectedCollectorCache
        //      Team or Person
        ParserResult<TeamOrPersonBase<?>> parseResult = parser.parse("Miller, John & White, Roger");
        assertTrue(!parseResult.getWarnings().isEmpty());
        team = (Team)parseResult.getEntity();

        assertEquals(4, team.getTeamMembers().size());
        assertEquals("Miller", team.getTeamMembers().get(0).getTitleCache());
        assertEquals("John", team.getTeamMembers().get(1).getTitleCache());
        assertEquals("White", team.getTeamMembers().get(2).getTitleCache());
        assertEquals("Roger", team.getTeamMembers().get(3).getTitleCache());


    }

}
