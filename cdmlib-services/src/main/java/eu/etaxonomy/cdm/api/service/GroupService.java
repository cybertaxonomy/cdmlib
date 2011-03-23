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

import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @created Mar 9, 2011
 * @version 1.0
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
public class GroupService extends ServiceBase<Group,IGroupDao> implements IGroupService {

	protected IUserDao userDao;
	
	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#findAllGroups()
	 */
	@Override
	public List<String> findAllGroups() {
		return dao.listNames(null,null);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#findUsersInGroup(java.lang.String)
	 */
	@Override
	public List<String> findUsersInGroup(String groupName) {
		Assert.hasText(groupName);
		Group group = dao.findGroupByName(groupName);
		
		List<String> users = dao.listMembers(group, null, null);
		
		return users;
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#createGroup(java.lang.String, java.util.List)
	 */
	@Override
	@Transactional(readOnly=false)
	public void createGroup(String groupName, List<GrantedAuthority> authorities) {
		Assert.hasText(groupName);
		Assert.notNull(authorities);
		
		Group group = Group.NewInstance(groupName);
		
		for(GrantedAuthority authority : authorities) {
			group.getGrantedAuthorities().add(authority);
		}
		
		dao.save(group);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#deleteGroup(java.lang.String)
	 */
	@Override
	@Transactional(readOnly=false)
	public void deleteGroup(String groupName) {
		Assert.hasText(groupName);
		
		Group group = dao.findGroupByName(groupName);
		dao.delete(group);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#renameGroup(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=false)
	public void renameGroup(String oldName, String newName) {
		Assert.hasText(oldName);
		Assert.hasText(newName);
		
		Group group = dao.findGroupByName(oldName);
		
		group.setName(newName);
		dao.update(group);
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#addUserToGroup(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=false)
	public void addUserToGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = dao.findGroupByName(groupName);
		User user = userDao.findUserByUsername(username);
		
		if(group.addMember(user)) {
			dao.update(group);
		}		
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#removeUserFromGroup(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(readOnly=false)
	public void removeUserFromGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = dao.findGroupByName(groupName);
		User user = userDao.findUserByUsername(username);
		
		if(group.removeMember(user)) {
			dao.update(group);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#findGroupAuthorities(java.lang.String)
	 */
	@Override
	public List<GrantedAuthority> findGroupAuthorities(String groupName) {
		Assert.hasText(groupName);
		Group group = dao.findGroupByName(groupName);
		
		return new ArrayList<GrantedAuthority>(group.getGrantedAuthorities());
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#addGroupAuthority(java.lang.String, org.springframework.security.core.GrantedAuthority)
	 */
	@Override
	@Transactional(readOnly=false)
	public void addGroupAuthority(String groupName, GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = dao.findGroupByName(groupName);
		if(group.getGrantedAuthorities().add(authority)) {
			dao.update(group);
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.security.provisioning.GroupManager#removeGroupAuthority(java.lang.String, org.springframework.security.core.GrantedAuthority)
	 */
	@Override
	@Transactional(readOnly=false)
	public void removeGroupAuthority(String groupName,
			GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = dao.findGroupByName(groupName);
		
		if(group.getGrantedAuthorities().remove(authority)) {
			dao.update(group);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ServiceBase#setDao(eu.etaxonomy.cdm.persistence.dao.common.ICdmEntityDao)
	 */
	@Override
	@Autowired
	protected void setDao(IGroupDao dao) {
		this.dao = dao;
	}

	@Autowired
	public void setUserDao(IUserDao userDao){
		this.userDao = userDao;
	}
	
	@Transactional(readOnly = true)
	public List<Group> listByName(String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
		 Integer numberOfResults = dao.countByName(queryString, matchmode, criteria);
			
		 List<Group> results = new ArrayList<Group>();
		 if(numberOfResults > 0) { 
				results = dao.findByName(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths); 
		 }
		 return results;
	}
	
}
