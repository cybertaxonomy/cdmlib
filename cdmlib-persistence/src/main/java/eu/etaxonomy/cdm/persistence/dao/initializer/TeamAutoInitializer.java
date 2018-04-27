/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.agent.Team;

/**
 * Initializes the the teamMembers of a Team
 *
 * @author a.kohlbecker
 * @since 30.07.2010
 *
 */
public class TeamAutoInitializer extends AutoPropertyInitializer<Team> {


    @Override
    public void initialize(Team bean) {
            Hibernate.initialize(bean.getTeamMembers());
    }

    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{

        String result = "";
        if (Team.class.isAssignableFrom(clazz)){
            result += String.format(" LEFT JOIN FETCH %s.teamMembers ", beanAlias);
        }

        return result;
    }


}
