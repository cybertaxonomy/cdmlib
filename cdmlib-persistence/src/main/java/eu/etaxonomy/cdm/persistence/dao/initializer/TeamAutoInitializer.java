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
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;

/**
 * Initializes the the teamMembers of a Team
 *
 * @author a.kohlbecker
 * @since 30.07.2010
 *
 */
public class TeamAutoInitializer extends AutoPropertyInitializer<TeamOrPersonBase> {


    @Override
    public void initialize(TeamOrPersonBase bean) {
        if(bean instanceof Team) {
            Hibernate.initialize(((Team)bean).getTeamMembers());
        }
    }

    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{

        String result = "";
        // can't distinguish Person and Team here as
        // ((HibernateProxy)propertyValue).getHibernateLazyInitializer().getPersistentClass();
        // always returns TeamOrPersonBase in
        // AdvancedBeanInitializer.preparePropertyValueForBulkLoadOrStore(BeanInitNode node, Object parentBean, String param, Object propertyValue)
        result += String.format(" LEFT JOIN FETCH %s.teamMembers ", beanAlias);

        return result;
    }


}
