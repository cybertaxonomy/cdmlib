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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.dto.MergeResult;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author n.hoffmann
 * @created Mar 9, 2011
 */
@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
public class GroupServiceImpl extends ServiceBase<Group,IGroupDao> implements IGroupService {

    protected IUserDao userDao;

    protected IGrantedAuthorityDao grantedAuthorityDao;

    @Override
    public List<String> findAllGroups() {
        return dao.listNames(null,null);
    }

    @Override
    public List<String> findUsersInGroup(String groupName) {
        Assert.hasText(groupName);
        Group group = dao.findGroupByName(groupName);

        List<String> users = dao.listMembers(group, null, null);

        return users;
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteGroup(String groupUUID) {
        Assert.notNull(groupUUID);

        Group group = dao.findByUuid(UUID.fromString(groupUUID));
        for (User user : group.getMembers()){
            group.removeMember(user);
        }
        if(group != null){
            dao.delete(group);
        }

    }

    @Override
    @Transactional(readOnly=false)
    public void renameGroup(String oldName, String newName) {
        Assert.hasText(oldName);
        Assert.hasText(newName);

        Group group = dao.findGroupByName(oldName);

        group.setName(newName);
        dao.update(group);
    }

    @Override
    @Transactional(readOnly=false)
    public void addUserToGroup(String username, String groupName) {
        Assert.hasText(username);
        Assert.hasText(groupName);

        Group group = dao.findGroupByName(groupName);
        User user = userDao.findUserByUsername(username);

        if(group != null || user != null){
            if(group.addMember(user)) {
                dao.update(group);
            }
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void removeUserFromGroup(String username, String groupName) {
        Assert.hasText(username);
        Assert.hasText(groupName);

        Group group = dao.findGroupByName(groupName);
        User user = userDao.findUserByUsername(username);

        if(group != null || user != null){
            if(group.removeMember(user)){
                dao.update(group);
            }
        }
    }

    @Override
    public List<GrantedAuthority> findGroupAuthorities(String groupName) {
        Assert.hasText(groupName);
        Group group = dao.findGroupByName(groupName);

        if (group != null){
            return new ArrayList<GrantedAuthority>(group.getGrantedAuthorities());
        }

        return new ArrayList<GrantedAuthority>();
    }

    @Override
    @Transactional(readOnly=false)
    public void addGroupAuthority(String groupName, GrantedAuthority authority) {
        Assert.hasText(groupName);
        Assert.notNull(authority);

        Group group = dao.findGroupByName(groupName);

        if (group != null){
            if(group.getGrantedAuthorities().add(authority)){
                dao.update(group);
            }
        }
    }

    @Override
    @Transactional(readOnly=false)
    public void removeGroupAuthority(String groupName,
            GrantedAuthority authority) {
        Assert.hasText(groupName);
        Assert.notNull(authority);

        Group group = dao.findGroupByName(groupName);

        if(group != null){
            if(group.getGrantedAuthorities().remove(authority)) {
                dao.update(group);
            }
        }
    }

    @Override
    @Autowired
    protected void setDao(IGroupDao dao) {
        this.dao = dao;
    }

    @Autowired
    public void setUserDao(IUserDao userDao){
        this.userDao = userDao;
    }

    @Autowired
    public void setGrantedAuthorityDao(IGrantedAuthorityDao grantedAuthorityDao){
        this.grantedAuthorityDao = grantedAuthorityDao;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Group> listByName(String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
         long numberOfResults = dao.countByName(queryString, matchmode, criteria);

         List<Group> results = new ArrayList<Group>();
         if(numberOfResults > 0) {
                results = dao.findByName(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }
         return results;
    }

    @Override
    @Transactional(readOnly=false)
    public void createGroup(String groupName, List<GrantedAuthority> authorities) {
        Assert.hasText(groupName);
        Assert.notNull(authorities);

        Group newGroup = Group.NewInstance(groupName);
        for (GrantedAuthority grantedAuthority: authorities){
            newGroup.addGrantedAuthority(grantedAuthority);
        }
        saveGroup(newGroup);
    }

    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID saveGroup(Group group) {
        return dao.save(group).getUuid();
    }

    @Override
    @Transactional(readOnly=false)
    public DeleteResult delete(UUID groupUUID ){

       String groupUUIDString = groupUUID.toString();
       //org.springframework.security.provisioning.GroupManager#deleteGroup needs a string argument
        this.deleteGroup(groupUUIDString);
        //there is no feedback from the deleteGroup method...
        return new DeleteResult();
    }

    @Override
    @Transactional(readOnly = false)
    public MergeResult<Group> merge(Group newInstance, boolean returnTransientEntity) {

        Set<GrantedAuthority> newAuthorities = newInstance.getGrantedAuthorities();
        Map<GrantedAuthority, GrantedAuthority> mapOfAlreadyExistingAuthorities = new HashMap<GrantedAuthority, GrantedAuthority>();
        GrantedAuthorityImpl alreadyInDB;
        for (GrantedAuthority authority: newAuthorities){
            if (authority instanceof GrantedAuthorityImpl){
                alreadyInDB = grantedAuthorityDao.findAuthorityString(authority.getAuthority());
                if (alreadyInDB != null){
                    if (alreadyInDB.getId() != ((GrantedAuthorityImpl)authority).getId()){
                        mapOfAlreadyExistingAuthorities.put(authority,alreadyInDB);
                    }
                }
            }
        }
        for (GrantedAuthority authority : mapOfAlreadyExistingAuthorities.keySet()){
            newInstance.removeGrantedAuthority(authority);
            newInstance.addGrantedAuthority(mapOfAlreadyExistingAuthorities.get(authority));
        }

        return dao.merge(newInstance, returnTransientEntity);
    }
}
