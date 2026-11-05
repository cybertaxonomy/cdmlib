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

/**
 * @author a.mueller
 * @date 05.03.2022
 */
public class BibliographicAuthorParserTest {

    private BibliographicAuthorParser parser = BibliographicAuthorParser.Instance();

    @Test
    public void testParse() {

        //default
        Team team = (Team)parser.parse("Contreras, C.L.E., Mariaca, M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera, M. "+UTF8.CAPITAL_A_ACUTE+".");
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma between name and initials
        team = (Team)parser.parse("Contreras C.L.E., Mariaca M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera M. "+UTF8.CAPITAL_A_ACUTE+".");
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        assertEquals("P"+UTF8.SMALL_E_ACUTE+"rez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma incorrect
        team = (Team)parser.parse("ContrerasC.L.E., Mariaca M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera M. "+UTF8.CAPITAL_A_ACUTE+".");
        assertEquals(3, team.getTeamMembers().size());
        assertNull(team.getTeamMembers().get(0).getFamilyName());
        assertNull(team.getTeamMembers().get(0).getInitials());
        assertEquals("ContrerasC.L.E.", team.getTeamMembers().get(0).getTitleCache());

        //person
        Person person = (Person)parser.parse("Contreras C.L.E.");
        assertEquals("Contreras", person.getFamilyName());
        assertEquals("C.L.E.", person.getInitials());
        assertEquals("Contreras, C.L.E.", person.getTitleCache());


        //et al.
        team = (Team)parser.parse("Contreras C.L.E. et al");
        assertEquals(1, team.getTeamMembers().size());
        assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        assertTrue(team.isHasMoreMembers());
        assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //& al.
        team = (Team)parser.parse("Contreras C.L.E. & al.");
        assertTrue(team.isHasMoreMembers());
        assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //
        team = (Team)parser.parse("Maldonado, M. M. de L., Velazquez, M J de A & Borja, de la R. M. A.");
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Velazquez", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("M J de A", team.getTeamMembers().get(1).getInitials());
        assertEquals("Borja", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("de la R. M. A.", team.getTeamMembers().get(2).getInitials());

        //initials with hyphen
        team = (Team)parser.parse("Moncada, B., Pérez-Pérez, R.-E. & Lücking, R.");
        assertEquals(3, team.getTeamMembers().size());
        assertEquals("Moncada", team.getTeamMembers().get(0).getFamilyName());
        assertEquals("B.", team.getTeamMembers().get(0).getInitials());
        assertEquals("Pérez-Pérez", team.getTeamMembers().get(1).getFamilyName());
        assertEquals("R.-E.", team.getTeamMembers().get(1).getInitials());
        assertEquals("Lücking", team.getTeamMembers().get(2).getFamilyName());
        assertEquals("R.", team.getTeamMembers().get(2).getInitials());
    }

    @Test
    public void testParseTest() {

        //default
//        Team team = (Team)parser.parse("Maldonado, M. M. de L., Velazquez, M J de A & Nanez, J. S.");
//        assertEquals(3, team.getTeamMembers().size());
//        assertEquals("Maldonado", team.getTeamMembers().get(0).getFamilyName());
//        assertEquals("M. M. de L.", team.getTeamMembers().get(0).getInitials());
//        assertEquals("M J de A", team.getTeamMembers().get(1).getInitials());

    }

}
