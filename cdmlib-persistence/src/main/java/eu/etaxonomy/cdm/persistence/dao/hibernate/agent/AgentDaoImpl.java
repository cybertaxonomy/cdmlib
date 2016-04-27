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
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;


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

	@Override
    public List<Institution> getInstitutionByCode(String code) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Criteria crit = getSession().createCriteria(Institution.class);
    		crit.add(Restrictions.eq("code", code));
 	    	return crit.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Institution.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			return query.getResultList();
		}
	}

	@Override
    public int countInstitutionalMemberships(Person person) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(institutionalMembership) from InstitutionalMembership institutionalMembership where institutionalMembership.person = :person");
		    query.setParameter("person", person);
		    return ((Long)query.uniqueResult()).intValue();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(InstitutionalMembership.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("person").eq(person.getId()));
			query.addProjection(AuditEntity.id());
			return ((Long)query.getSingleResult()).intValue();
		}
	}

	@Override
    public int countMembers(Team team) {
		checkNotInPriorView("AgentDaoImpl.countMembers(Team team)");
		Query query = getSession().createQuery("select count(teamMember) from Team team join team.teamMembers teamMember where team = :team");
		query.setParameter("team", team);
		return ((Long)query.uniqueResult()).intValue();
	}

	@Override
    public List<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select institutionalMembership from InstitutionalMembership institutionalMembership left join fetch institutionalMembership.institute where institutionalMembership.person = :person");
		    query.setParameter("person", person);
		    setPagingParameter(query, pageSize, pageNumber);
			return query.list();
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(InstitutionalMembership.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.relatedId("person").eq(person.getId()));
			setPagingParameter(query, pageSize, pageNumber);
			return query.getResultList();
		}
	}

	@Override
    public List<Person> getMembers(Team team, Integer pageSize,	Integer pageNumber) {
		checkNotInPriorView("AgentDaoImpl.getMembers(Team team, Integer pageSize,	Integer pageNumber)");
		Query query = getSession().createQuery("select teamMember from Team team join team.teamMembers teamMember where team = :team order by sortindex");
		query.setParameter("team", team);
		//query.addOrder( Order.asc("sortindex") );
		setPagingParameter(query, pageSize, pageNumber);
		return query.list();
	}

	@Override
    public Integer countAddresses(AgentBase agent) {
		checkNotInPriorView("AgentDaoImpl.countAddresses(AgentBase agent)");
		Query query = getSession().createQuery("select count(address) from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		return ((Long)query.uniqueResult()).intValue();
	}

	@Override
    public List<Address> getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber) {
		checkNotInPriorView("AgentDaoImpl.getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber)");
		Query query = getSession().createQuery("select address from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		setPagingParameter(query, pageSize, pageNumber);
		return query.list();
	}


	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle() {
		List<UuidAndTitleCache<Team>> list = new ArrayList<UuidAndTitleCache<Team>>();
		Session session = getSession();

		Query query = session.createQuery("select uuid, id, nomenclaturalTitle from " + type.getSimpleName() + " where dtype = 'Team'");

		@SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

		for(Object[] object : result){
			list.add(new UuidAndTitleCache<Team>(Team.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
		}

		return list;
	}

	@Override
    public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache() {
		Query query = getSession().createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = 'Person'");
		return getUuidAndTitleCache(query);
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache() {
		Query query = getSession().createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = 'Team'");
		return getUuidAndTitleCache(query);
	}

	@Override
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache() {
		Query query = getSession().createQuery("select uuid, id, titleCache from " + type.getSimpleName() + " where dtype = 'Institution'");
		return getUuidAndTitleCache(query);
	}

	@Override
	public List<Person> getPersonsUsedAsUser(){
	       Query query = getSession().createQuery("SELECT ab.uuid, ob.idInSource FROM Person ab JOIN ab.sources ob WHERE ob.idNamespace LIKE 'User'");
	       List<Person> result = query.list();
	       return result;
	}

}
