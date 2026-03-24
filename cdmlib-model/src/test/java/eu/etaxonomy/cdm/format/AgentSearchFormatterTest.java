/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.format.agent.AgentSearchFormatter;
import eu.etaxonomy.cdm.format.agent.AgentSearchFormatter.CacheType;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;


/**
 * Test for {@link AgentSearchFormatter}.
 *
 * @author muellera
 * @since 05.03.2026
 */
public class AgentSearchFormatterTest {

    private Person person1;
    private Person person2;
    private Team team1;
    private AgentSearchFormatter formatter;

    @Before
    public void setUp() throws Exception {
        person1 = Person.NewInstance("Mill.", "A. Miller", "Miller", "A.", "Andrew");
        person1.setId(1);
        person2 = Person.NewInstance("Ball.", "Baller", "Baller", null, null);
        person2.setId(2);
        team1 = Team.NewInstance(person1, person2);
        team1.setId(3);

        formatter = AgentSearchFormatter.INSTANCE();
    }

    @Test
    public void test() {
        Assert.assertEquals("Miller, A. - Mill. - A. Miller [1]", formatter.format(person1));
        Assert.assertEquals("Baller - Ball. [2]", formatter.format(person2));
        Assert.assertEquals("Miller, A. & Baller - Mill. & Ball. - A. Miller & Baller [3]", formatter.format(team1));
    }

    @Test
    public void testWithCacheTypes() {
        Assert.assertEquals("Mill. - Miller, A. - A. Miller [1]", formatter.format(person1,
                AgentSearchFormatter.NOMENCLATURAL_TITLE_FIRST));
        Assert.assertEquals("A. Miller - Miller, A. - Mill. [1]", formatter.format(person1,
                AgentSearchFormatter.COLLECTOR_TITLE_FIRST));
        List<CacheType> cacheTypes = Arrays.asList(new CacheType[] {CacheType.BIBLIOGRAPHIC_TITLE, CacheType.COLLECTOR_TITLE});
        Assert.assertEquals("Miller, A. & Baller - A. Miller & Baller [3]", formatter.format(team1, cacheTypes));
    }
}