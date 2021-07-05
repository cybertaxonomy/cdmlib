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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
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
import eu.etaxonomy.cdm.persistence.dto.TeamOrPersonUuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;


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
 	    	@SuppressWarnings("unchecked")
            List<Institution> result = crit.list();
 	    	return result;
		} else {
			AuditQuery query = getAuditReader().createQuery().forEntitiesAtRevision(Institution.class,auditEvent.getRevisionNumber());
			query.add(AuditEntity.property("code").eq(code));
			@SuppressWarnings("unchecked")
            List<Institution> result = query.getResultList();
			return result;
		}
	}

	@Override
    public long countInstitutionalMemberships(Person person) {
		AuditEvent auditEvent = getAuditEventFromContext();
		if(auditEvent.equals(AuditEvent.CURRENT_VIEW)) {
		    Query query = getSession().createQuery("select count(institutionalMembership) from InstitutionalMembership institutionalMembership where institutionalMembership.person = :person");
		    query.setParameter("person", person);
		    return (Long)query.uniqueResult();
		} else {
			AuditQuery query = makeAuditQuery(InstitutionalMembership.class, auditEvent);
			query.add(AuditEntity.relatedId("person").eq(person.getId()));
			query.addProjection(AuditEntity.id());
			return (Long)query.getSingleResult();
		}
	}

	@Override
    public long countMembers(Team team) {
		checkNotInPriorView("AgentDaoImpl.countMembers(Team team)");
		Query query = getSession().createQuery("select count(teamMember) from Team team join team.teamMembers teamMember where team = :team");
		query.setParameter("team", team);
		return (Long)query.uniqueResult();
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
		@SuppressWarnings("unchecked")
        List<Person> result = query.list();
		return result;
	}

	@Override
    public long countAddresses(AgentBase agent) {
		checkNotInPriorView("AgentDaoImpl.countAddresses(AgentBase agent)");
		Query query = getSession().createQuery("select count(address) from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		return (Long)query.uniqueResult();
	}

	@Override
    public List<Address> getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber) {
		checkNotInPriorView("AgentDaoImpl.getAddresses(AgentBase agent, Integer pageSize,Integer pageNumber)");
		Query query = getSession().createQuery("select address from AgentBase agent join agent.contact.addresses address where agent = :agent");
		query.setParameter("agent", agent);
		setPagingParameter(query, pageSize, pageNumber);
        @SuppressWarnings("unchecked")
        List<Address> result = query.list();
        return result;
	}


	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle() {
		List<UuidAndTitleCache<Team>> list = new ArrayList<>();
		Session session = getSession();

		Query query = session.createQuery("select uuid, id, nomenclaturalTitleCache from " + type.getSimpleName() + " where dtype = 'Team'");

		@SuppressWarnings("unchecked")
        List<Object[]> result = query.list();

		for(Object[] object : result){
			list.add(new UuidAndTitleCache<>(Team.class, (UUID) object[0], (Integer)object[1], (String) object[2]));
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
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache(Integer limit, String pattern) {
		Query query = getSession().createQuery("SELECT uuid, id, titleCache FROM " + Institution.class.getSimpleName());
		return getUuidAndTitleCache(query);
	}

	@Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndTitleCacheWithCollector(Class<T> clazz, Integer limit, String pattern){
	    Session session = getSession();

        clazz = (clazz == null)? (Class)type : clazz;
        String clazzString = " FROM " + clazz.getSimpleName();

        Query query = null;

        if (pattern != null){
            String whereClause = " WHERE collectorTitleCache LIKE :pattern "
                     + " OR titleCache LIKE :pattern "
                     + " OR nomenclaturalTitleCache like :pattern ";

            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString  + whereClause);
            pattern = pattern + "%";
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString);
        }
        if (limit != null){
            query.setMaxResults(limit);
        }

        return getTeamOrPersonUuidAndTitleCache(query);
	}

	@Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getTeamOrPersonUuidAndTitleCache(Class<T> clazz, Integer limit, String pattern){
        Session session = getSession();

        clazz = (clazz == null)? (Class)type : clazz;
        String clazzString = " FROM " + clazz.getSimpleName();

        Query query = null;

        if (pattern != null){
            String whereClause = " WHERE titleCache LIKE :pattern";

            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString  + whereClause);
            pattern = pattern + "%";
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString);
        }
        if (limit != null){
            query.setMaxResults(limit);
        }

        return getTeamOrPersonUuidAndTitleCache(query);
    }


	@Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndAbbrevTitleCache(Class<T> clazz, Integer limit, String pattern){
        Session session = getSession();

        clazz = (clazz == null)? (Class)type : clazz;
        String clazzString = " FROM " + clazz.getSimpleName();

        Query query = null;

        if (pattern != null){
            String whereClause = " WHERE nomenclaturalTitleCache LIKE :pattern";
            if (pattern.startsWith("*")){
                whereClause += " OR titleCache LIKE :pattern";
                whereClause += " OR collectorTitleCache LIKE :pattern";
            }

            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString  + whereClause);
            pattern = pattern + "%";
            pattern = pattern.replace("*", "%");
            pattern = pattern.replace("?", "_");
            query.setParameter("pattern", pattern);
        } else {
            query = session.createQuery("SELECT DISTINCT uuid, id, nomenclaturalTitleCache, titleCache, collectorTitleCache " + clazzString);
        }
        if (limit != null){
            query.setMaxResults(limit);
        }

        return getTeamOrPersonUuidAndTitleCache(query);
    }

	@Override
    public <T extends AgentBase<?>> List<T> findByTitleAndAbbrevTitle(Class<T> clazz, String queryString, MatchMode matchmode, List<Criterion> criterion, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
        Set<String> params = new HashSet<>();
        params.add("titleCache");
        params.add("nomenclaturalTitleCache");
        params.add("collectorTitleCache");

	    return findByParam(clazz, params, queryString, matchmode, criterion, pageSize, pageNumber, orderHints, propertyPaths);
    }

    protected <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getTeamOrPersonUuidAndTitleCache(Query query){
        List<TeamOrPersonUuidAndTitleCache<T>> list = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Object> result = query.list();

        for(Object obj : result){
          Object[] object = (Object[])obj;
          list.add(new TeamOrPersonUuidAndTitleCache((UUID) object[0],(Integer) object[1], (String) object[3], (String) object[2], (String) object[4]));
        }
        return list;
    }
}