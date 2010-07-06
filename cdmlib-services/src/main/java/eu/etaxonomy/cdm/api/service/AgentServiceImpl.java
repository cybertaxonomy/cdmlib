// $Id$
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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.dao.agent.IAgentDao;



/**
 * @author a.mueller
 *
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class AgentServiceImpl extends IdentifiableServiceBase<AgentBase,IAgentDao> implements IAgentService {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);
	

	/**
	 * Constructor
	 */
	public AgentServiceImpl(){
		if (logger.isDebugEnabled()) { logger.debug("Load AgentService Bean"); }
	}
	
	public List<Institution> searchInstitutionByCode(String code) {
		return dao.getInstitutionByCode(code);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IIdentifiableEntityService#updateTitleCache()
	 */
	@Override
	public void updateTitleCache() {
		Class<AgentBase> clazz = AgentBase.class;
		super.updateTitleCache(clazz, null, null);
	}

	@Autowired
	protected void setDao(IAgentDao dao) {
		assert dao != null;
		this.dao = dao;
	}

	public Pager<InstitutionalMembership> getInstitutionalMemberships(Person person, Integer pageSize, Integer pageNumber) {
        Integer numberOfResults = dao.countInstitutionalMemberships(person);
		
		List<InstitutionalMembership> results = new ArrayList<InstitutionalMembership>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getInstitutionalMemberships(person, pageSize, pageNumber); 
		}
		
		return new DefaultPagerImpl<InstitutionalMembership>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Person> getMembers(Team team, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countMembers(team);
			
		List<Person> results = new ArrayList<Person>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getMembers(team, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Person>(pageNumber, numberOfResults, pageSize, results);
	}

	public Pager<Address> getAddresses(AgentBase agent, Integer pageSize, Integer pageNumber) {
		Integer numberOfResults = dao.countAddresses(agent);
		
		List<Address> results = new ArrayList<Address>();
		if(numberOfResults > 0) { // no point checking again
			results = dao.getAddresses(agent, pageSize, pageNumber); 
		}
			
		return new DefaultPagerImpl<Address>(pageNumber, numberOfResults, pageSize, results);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.IAgentService#getTeamOrPersonBaseUuidAndNomenclaturalTitle()
	 */
	public List<UuidAndTitleCache<Team>> getTeamUuidAndNomenclaturalTitle() {
		return dao.getTeamUuidAndNomenclaturalTitle();
	}

	@Override
	public List<UuidAndTitleCache<Person>> getPersonUuidAndTitleCache() {
		return dao.getPersonUuidAndTitleCache();
	}

	@Override
	public List<UuidAndTitleCache<Team>> getTeamUuidAndTitleCache() {
		return dao.getTeamUuidAndTitleCache();
	}	
}
