/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;


@Repository
public class AgentDaoImpl extends IdentifiableDaoBase<AgentBase> implements IAgentDao{
	
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AgentDaoImpl.class);

	public AgentDaoImpl() {
		super(AgentBase.class);
		indexedClasses = new Class[3];
		indexedClasses[0] = Institution.class;
		indexedClasses[1] = Person.class;
		indexedClasses[2] = Team.class;
	}

	public List<Institution> getInstitutionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria crit = getSession().createCriteria(Institution.class);
    		crit.add(Restrictions.eq("code", code));
 	    	return (List<Institution>)crit.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Institution.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return (List<Institution>)query.getResultList();
		}
	}

	public int countInstitutionalMemberships(Person person) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(institutionalMembership) from InstitutionalMembership institutionalMembership where institutionalMembership.person = :person");
		    query.setParameter("person", person);
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(InstitutionalMembership.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("person").eq(person.getId()));
			query.addProjection(AuditEntity.id().count("id"));
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	public int countMembers(Team team) {
		checkNotInPriorView("AgentDaoImpl.countMembers(Team team)");
		Query query = getSession().createQuery("select count(teamMember) from Team team join team.teamMembers teamMember where team = :team");
		query.setParameter("team", team);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select institutionalMembership from InstitutionalMembership institutionalMembership left join fetch institutionalMembership.institute where institutionalMembership.person = :person");
		    query.setParameter("person", person);
		    setPagingParameter(query, pageSize, pageNumber);
			return (List<InstitutionalMembership>)query.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(InstitutionalMembership.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("person").eq(person.getId()));
			setPagingParameter(query, pageSize, pageNumber);
			return (List<InstitutionalMembership>)query.getResultList();
		}
	}

	public List<Person> getMembers(Team team, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("AgentDaoImpl.getMembers(Team team, Integer pageSize,	Integer pageNumber)");
		Query query = getSession().createQuery("select teamMember from Team team join team.teamMembers teamMember where team = :team order by sortindex");
		query.setParameter("team", team);
		//query.addOrder( Order.asc("sortindex") );
		setPagingParameter(query, pageSize, pageNumber);
		return (List<Person>)query.list();
	}

	public Integer countAddresses(AgentBase agent) {
		checkNotInPriorView("AgentDaoImpl.countAddresses(AgentBase agent)");
		Query query = getSession().createQuery("select count(address) from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		return ((Long)query.uniqueResult()).intValue();
	}

	public List<Address> getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber) {
		checkNotInPriorView("AgentDaoImpl.getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber)");
		Query query = getSession().createQuery("select address from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		setPagingParameter(query, pageSize, pageNumber);
		return (List<Address>)query.list();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao#getTeamOrPersonBaseUuidAndNomenclaturalTitle()
	 */
	public List<UuidAndTitleCache<TeamOrPersonBase>> getTeamOrPersonBaseUuidAndNomenclaturalTitle() {
		List<UuidAndTitleCache<TeamOrPersonBase>> list = new ArrayList<UuidAndTitleCache<TeamOrPersonBase>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, nomenclaturalTitle from " + type.getSimpleName() + " where dtype = 'Person' or dtype = 'Team'");
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<TeamOrPersonBase>(TeamOrPersonBase.class, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao#getTeamUuidAndTitleCache()
	 */
	public List<UuidAndTitleCache<Person>> getPersonUuidAndNomenclaturalTitle() {
		List<UuidAndTitleCache<Person>> list = new ArrayList<UuidAndTitleCache<Person>>();
		Session session = getSession();
		
		Query query = session.createQuery("select uuid, nomenclaturalTitle from " + type.getSimpleName() + " where dtype = 'Person'");
		
		List<Object[]> result = query.list();
		
		for(Object[] object : result){
			list.add(new UuidAndTitleCache<Person>(Person.class, (UUID) object[0], (String) object[1]));
		}
		
		return list;
	}
}
