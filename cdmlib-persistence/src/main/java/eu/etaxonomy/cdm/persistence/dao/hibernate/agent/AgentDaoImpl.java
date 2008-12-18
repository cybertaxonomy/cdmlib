/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.agent;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;


@Repository
public class AgentDaoImpl extends IdentifiableDaoBase<Agent> implements IAgentDao{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AgentDaoImpl.class);

	public AgentDaoImpl() {
		super(Agent.class); 
	}

	public List<Institution> getInstitutionByCode(String code) {
		Criteria crit = getSession().createCriteria(Institution.class);
		
		crit.add(Restrictions.eq("code", code));
		List<Institution> results = crit.list();
		return results;
	}

	public int countInstitutionalMemberships(Person person) {
		Query query = getSession().createQuery("select count(institutionalMembership) from InstitutionalMembership institutionalMembership where institutionalMembership.person = :person");
		query.setParameter("person", person);
		return ((Long)query.uniqueResult()).intValue();
	}

	public int countMembers(Team team) {
		Query query = getSession().createQuery("select count(teamMember) from Team team join team.teamMembers teamMember where team = :team");
		query.setParameter("team", team);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
		Query query = getSession().createQuery("select institutionalMembership from InstitutionalMembership institutionalMembership left join fetch institutionalMembership.institute where institutionalMembership.person = :person");
		query.setParameter("person", person);
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		
		return (List<InstitutionalMembership>)query.list();
	}

	public List<Person> getMembers(Team team, Integer pageSize,	Integer pageNumber) {
		Query query = getSession().createQuery("select teamMember from Team team join team.teamMembers teamMember where team = :team");
		query.setParameter("team", team);
		
		if(pageSize != null) {
		    query.setMaxResults(pageSize);
		    if(pageNumber != null) {
		        query.setFirstResult(pageNumber * pageSize);
		    } else {
		    	query.setFirstResult(0);
		    }
		}
		return (List<Person>)query.list();
	}
}
