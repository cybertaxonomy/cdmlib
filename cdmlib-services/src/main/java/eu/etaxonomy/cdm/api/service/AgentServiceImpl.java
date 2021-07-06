/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.config.IIdentifiableEntityServiceConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dto.TeamOrPersonUuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy;
import eu.etaxonomy.cdm.strategy.merge.ConvertMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;
import eu.etaxonomy.cdm.strategy.merge.MergeMode;

/**
 * @author a.mueller
 */
@Service
@Transactional(readOnly = true)
public class AgentServiceImpl
        extends IdentifiableServiceBase<AgentBase,IAgentDao>
        implements IAgentService {

    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);

    @Autowired
    private ICdmGenericDao genericDao;

	@Override
    @Autowired
	protected void setDao(IAgentDao dao) {
		assert dao != null;
		this.dao = dao;
	}

 	/**
	 * Constructor
	 */
	public AgentServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load AgentService Bean"); }
	}

	@Override
	@Transactional(readOnly = false)
    public UpdateResult updateCaches(Class<? extends AgentBase> clazz, Integer stepSize, IIdentifiableEntityCacheStrategy<AgentBase> cacheStrategy, IProgressMonitor monitor) {
		if (clazz == null){
			clazz = AgentBase.class;
		}
		return super.updateCachesImpl(clazz, stepSize, cacheStrategy, monitor);
	}

	@Override
	public List<Institution> searchInstitutionByCode(String code) {
		return dao.getInstitutionByCode(code);
	}

	@Override
	public Pager<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
        long numberOfResults = dao.countInstitutionalMemberships(person);

		List<InstitutionalMembership> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getInstitutionalMemberships(person, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Person> getMembers(Team team, Integer pageSize, Integer pageNumber) {
		long numberOfResults = dao.countMembers(team);

		List<Person> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getMembers(team, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public Pager<Address> getAddresses(AgentBase agent, Integer pageSize, Integer pageNumber) {
		long numberOfResults = dao.countAddresses(agent);

		List<Address> results = new ArrayList<>();
		if(numberOfResults > 0) { // no point checking again //TODO use AbstractPagerImpl.hasResultsInRange(numberOfResults, pageNumber, pageSize)
			results = dao.getAddresses(agent, pageSize, pageNumber);
		}

		return new DefaultPagerImpl<>(pageNumber, numberOfResults, pageSize, results);
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle() {
		return dao.getTeamUuidAndNomenclaturalTitle();
	}

	@Override
	public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache() {
		return dao.getUuidAndTitleCache(Person.class);
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache() {
		return dao.getUuidAndTitleCache(Team.class);
	}

	@Override
	public List<UuidAndTitleCache<Institution>> getInstitutionUuidAndTitleCache(Integer limit, String pattern) {
		return dao.getUuidAndTitleCache(Institution.class, limit, pattern);
	}

	@Override
	@Transactional(readOnly = false)
    public DeleteResult delete(UUID agentUUID){
	    DeleteResult result = new DeleteResult();
	    if (agentUUID == null){
	        result.setAbort();
	        result.addException(new Exception("Can't delete object without UUID."));
	        return result;
	    }
		AgentBase base = dao.load(agentUUID);
		result = isDeletable(agentUUID, null);

    	if (result.isOk()){
			if (base instanceof Team){
				Team baseTeam = (Team) base;
				List<Person> members = baseTeam.getTeamMembers();
				List<Person> temp = new ArrayList<>();
				for (Person member:members){
					temp.add(member);
				}
				for (Person member: temp){
					members.remove(member);
				}
			}
			saveOrUpdate(base);

			dao.delete(base);
			result.addDeletedObject(base);

		}

		return result;
    }

	@Override
    public DeleteResult delete(AgentBase agent){
		return delete(agent.getUuid());
	}

	@Override
	@Transactional(readOnly = false)
	public UpdateResult convertTeam2Person(UUID teamUuid) throws MergeException {
	    Team team = CdmBase.deproxy(dao.load(teamUuid), Team.class);
	    return convertTeam2Person(team);
	}

	@Override
	public UpdateResult convertTeam2Person(Team team) throws MergeException {
        UpdateResult result = new UpdateResult();
        Person newPerson = null;
		team = CdmBase.deproxy(team, Team.class);
		if (team.getTeamMembers().size() > 1){
			throw new IllegalArgumentException("Team must not have more than 1 member to be convertable into a person");
		}else if (team.getTeamMembers().size() == 1){
		    newPerson = team.getTeamMembers().get(0);
			IMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
			strategy.setDefaultCollectionMergeMode(MergeMode.FIRST);
			genericDao.merge(newPerson, team, strategy);
		}else if (team.getTeamMembers().isEmpty()){
		    newPerson = Person.NewInstance();
            genericDao.save(newPerson);
			IMergeStrategy strategy = DefaultMergeStrategy.NewInstance(TeamOrPersonBase.class);
			strategy.setDefaultMergeMode(MergeMode.SECOND);
			strategy.setDefaultCollectionMergeMode(MergeMode.SECOND);
			genericDao.merge(newPerson, team, strategy);
		}else{
			throw new IllegalStateException("Unhandled state of team members collection");
		}
		result.setCdmEntity(newPerson);
		return result;
	}

	@Override
	@Transactional(readOnly = false)
	public UpdateResult convertPerson2Team(UUID personUuid) throws MergeException, IllegalArgumentException {
	    Person person = CdmBase.deproxy(dao.load(personUuid), Person.class);
	    return convertPerson2Team(person);
	}

	@Override
	public UpdateResult convertPerson2Team(Person person) throws MergeException, IllegalArgumentException {
	    if (person == null){
	        throw new IllegalArgumentException("Person does not exist.");
	    }
	    UpdateResult result = new UpdateResult();
        Team team = Team.NewInstance();
        ConvertMergeStrategy strategy = ConvertMergeStrategy.NewInstance(TeamOrPersonBase.class);
        strategy.setDefaultMergeMode(MergeMode.SECOND);
        strategy.setDefaultCollectionMergeMode(MergeMode.SECOND);
        if (person.isProtectedTitleCache() || !person.getTitleCache().startsWith("Person#")){  //the later is for checking if the person is empty, this can be done better
            team.setTitleCache(person.getTitleCache(), true);  //as there are no members we set the titleCache to protected (as long as titleCache does not take the protected nomenclatural title but results in '-empty team-' in this situation); for some reason it is necessary to also set the title cache as well here, otherwise it is recomputed during the merge
        }
        strategy.setMergeMode("protectedTitleCache", MergeMode.FIRST); //as we do not add team members, the titleCache of the new team should be always protected
        strategy.setDeleteSecondObject(true);
        if (StringUtils.isNotBlank(person.getNomenclaturalTitle())){
            team.setNomenclaturalTitleCache(person.getNomenclaturalTitle(), true);  //sets the protected flag always to true, this is necessary as long as person does not have a nomenclatural title cache; maybe setting the protected title itself is not necessary but does no harm
        }
        if (StringUtils.isNotBlank(person.getCollectorTitle())){
            team.setCollectorTitleCache(person.getCollectorTitle(), true);  //sets the protected flag always to true, this is necessary as long as person does not have a collector title cache; maybe setting the protected title itself is not necessary but does no harm
        }

        if (! genericDao.isMergeable(team, person, strategy)){
			throw new MergeException("Person can not be transformed into team.");
		}
		try {
			team = this.save(team);
			genericDao.merge(team, person, strategy);

			//Note: we decided  never add the old person as (first) member of the team as there are not many usecases for this.
			//But we may try to parse the old person into members which may handle the case that teams have been stored as unparsed persons

		} catch (Exception e) {
			throw new MergeException("Unhandled merge exception", e);
		}
		result.setCdmEntity(team);
		result.addUpdatedObject(team);
        return result;
	}

    @Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndAbbrevTitleCache(Class<T> clazz, Integer limit, String pattern) {
        return dao.getUuidAndAbbrevTitleCache(clazz, null, pattern);
    }

    @Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getUuidAndTitleCacheWithCollectorTitleCache(Class<T> clazz, Integer limit, String pattern) {
        return dao.getUuidAndTitleCacheWithCollector(clazz, null, pattern);
    }

    @Override
    public <T extends AgentBase> List<TeamOrPersonUuidAndTitleCache<T>> getTeamOrPersonUuidAndTitleCache(Class<T> clazz, Integer limit, String pattern) {
        return dao.getTeamOrPersonUuidAndTitleCache(clazz, null, pattern);
    }

    @Override
    public <T extends AgentBase<?>> List<T> findByTitleAndAbbrevTitle(IIdentifiableEntityServiceConfigurator<T> config){
        return dao.findByTitleAndAbbrevTitle(config.getClazz(),config.getTitleSearchStringSqlized(), config.getMatchMode(), config.getCriteria(), config.getPageSize(), config.getPageNumber(), config.getOrderHints(), config.getPropertyPaths());
    }
}