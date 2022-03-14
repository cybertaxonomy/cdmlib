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
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("Pérez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma
        team = (Team)parser.parse("Contreras C.L.E., Mariaca M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera M. "+UTF8.CAPITAL_A_ACUTE+".");
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("Mariaca", team.getTeamMembers().get(1).getFamilyName());
        Assert.assertEquals("M. R.", team.getTeamMembers().get(1).getInitials());
        Assert.assertEquals("Pérez-Farrera", team.getTeamMembers().get(2).getFamilyName());
        Assert.assertEquals("M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());

        //without comma
        team = (Team)parser.parse("ContrerasC.L.E., Mariaca M. R. & P"+UTF8.SMALL_E_ACUTE+"rez-Farrera M. "+UTF8.CAPITAL_A_ACUTE+".");
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertNull(team.getTeamMembers().get(0).getFamilyName());
        Assert.assertNull(team.getTeamMembers().get(0).getInitials());
        Assert.assertEquals("ContrerasC.L.E.", team.getTeamMembers().get(0).getTitleCache());

        //person
        Person person = (Person)parser.parse("Contreras C.L.E.");
        Assert.assertEquals("Contreras", person.getFamilyName());
        Assert.assertEquals("C.L.E.", person.getInitials());
        Assert.assertEquals("Contreras, C.L.E.", person.getTitleCache());


        //et al.
        team = (Team)parser.parse("Contreras C.L.E. et al");
        Assert.assertEquals(1, team.getTeamMembers().size());
        Assert.assertEquals("Contreras", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("C.L.E.", team.getTeamMembers().get(0).getInitials());
        Assert.assertTrue(team.isHasMoreMembers());
        Assert.assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());

        //et al.
        team = (Team)parser.parse("Contreras C.L.E. & al.");
        Assert.assertTrue(team.isHasMoreMembers());
        Assert.assertEquals("Contreras, C.L.E. & al.", team.getTitleCache());
    }

    @Test
    public void testParseTest() {

        //default
        Team team = (Team)parser.parse("Ramos, M. M., Montoya, G. G. & Vásquez, S. M. Á.");
        Assert.assertEquals(3, team.getTeamMembers().size());
        Assert.assertEquals("Ramos", team.getTeamMembers().get(0).getFamilyName());
        Assert.assertEquals("S. M. "+UTF8.CAPITAL_A_ACUTE+".", team.getTeamMembers().get(2).getInitials());
    }

}
