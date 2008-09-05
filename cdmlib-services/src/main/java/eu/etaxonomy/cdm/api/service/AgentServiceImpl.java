/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.persistence.dao.common.IAgentDao;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao;



/**
 * @author a.mueller
 *
 */
@Service
@Transactional
public class AgentServiceImpl<T extends Agent> extends IdentifiableServiceBase<T> implements IAgentService<T> {
    private static final Logger logger = Logger.getLogger(AgentServiceImpl.class);

	private IAgentDao agentDao;
	@Autowired
	protected void setDao(IAgentDao dao) {
		this.dao = (ICdmEntityDao)dao;
		this.agentDao = dao;
	}

	public List<T> findAgentsByTitle(String title) {
		return super.findCdmObjectsByTitle(title);
	}

	public T getAgentByUuid(UUID uuid) {
		return super.getCdmObjectByUuid(uuid);
	}

	public UUID saveAgent(T agent) {
		return super.saveCdmObject(agent);
	}
	
	@Transactional(readOnly = false)
	public Map<UUID, T> saveAgentAll(Collection<T> agentCollection){
		return saveCdmObjectAll(agentCollection);
	}

	
	public List<? extends Agent> getAllAgents(int limit, int start){
		return agentDao.list(limit, start);
	}
	
	public List<Agent> getAgentByCode(String code) {
		return agentDao.getAgentByCode(code);
	}

	public void generateTitleCache() {
		// TODO Auto-generated method stub
		
	}
}
