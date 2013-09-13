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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.hibernate.NonUniqueResultException;
import org.hibernate.criterion.Criterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IGrantedAuthorityDao;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * Note: All group related functionality has been refactored into a GroupService. The will be removed in a future version.
 */
@Service
@Transactional(readOnly = true)
// NOTE: no type level @PreAuthorize annotation for this class!
public class UserService extends ServiceBase<User,IUserDao> implements IUserService {

    protected IGroupDao groupDao;

    protected IGrantedAuthorityDao grantedAuthorityDao;

    private SaltSource saltSource; // = new ReflectionSaltSource();

    private PasswordEncoder passwordEncoder; // = new Md5PasswordEncoder();

    private AuthenticationManager authenticationManager;

    private UserCache userCache = new NullUserCache();

    @Autowired(required = false)
    public void setUserCache(UserCache userCache) {
        Assert.notNull(userCache, "userCache cannot be null");
        this.userCache = userCache;
    }

    @Autowired(required = false)
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {

        this.passwordEncoder = passwordEncoder;
    }

    @Autowired(required = false)
    public void setSaltSource(SaltSource saltSource) {
        this.saltSource = saltSource;
    }

    @Autowired(required= false)
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Autowired
    protected void setDao(IUserDao dao) {
        this.dao = dao;
    }

    @Autowired
    public void setGroupDao(IGroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @Autowired
    public void setGrantedAuthorityDao(IGrantedAuthorityDao grantedAuthorityDao) {
        this.grantedAuthorityDao = grantedAuthorityDao;
    }

    @Transactional(readOnly=false)
    protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
        UserDetails user = loadUserByUsername(currentAuth.getName());

        UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        newAuthentication.setDetails(currentAuth.getDetails());

        return newAuthentication;
    }

    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("isAuthenticated()")
    public void changePassword(String oldPassword, String newPassword) {
        Assert.hasText(oldPassword);
        Assert.hasText(newPassword);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() != null && authentication.getPrincipal() instanceof User) {
            User user = (User)authentication.getPrincipal();

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), oldPassword));

            Object salt = this.saltSource.getSalt(user);

            String password = passwordEncoder.encodePassword(newPassword, salt);
            ((User)user).setPassword(password);

            dao.update((User)user);
            SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication, newPassword));
            userCache.removeUserFromCache(user.getUsername());
        } else {
            throw new AccessDeniedException("Can't change password as no Authentication object found in context for current user.");
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IUserService#changePasswordForUser(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("#username == authentication.name or hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void changePasswordForUser(String username, String newPassword) {
        Assert.hasText(username);
        Assert.hasText(newPassword);

        try {
            User user = dao.findUserByUsername(username);
            if(user == null) {
                throw new UsernameNotFoundException(username);
            }

            Object salt = this.saltSource.getSalt(user);

            String password = passwordEncoder.encodePassword(newPassword, salt);
            ((User)user).setPassword(password);

            dao.update((User)user);
            userCache.removeUserFromCache(user.getUsername());
        } catch(NonUniqueResultException nure) {
            throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'", 1);
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.security.provisioning.UserDetailsManager#createUser(org.springframework.security.core.userdetails.UserDetails)
     */
    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void createUser(UserDetails user) {
        Assert.isInstanceOf(User.class, user);

        String rawPassword = user.getPassword();
        Object salt = this.saltSource.getSalt(user);

        String password = passwordEncoder.encodePassword(rawPassword, salt);
        ((User)user).setPassword(password);

        dao.save((User)user);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.provisioning.UserDetailsManager#deleteUser(java.lang.String)
     */
    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void deleteUser(String username) {
        Assert.hasLength(username);

        User user = dao.findUserByUsername(username);
        if(user != null) {
            dao.delete((User)user);
        }

        userCache.removeUserFromCache(username);
    }

    /* (non-Javadoc)
     * @see org.springframework.security.provisioning.UserDetailsManager#updateUser(org.springframework.security.core.userdetails.UserDetails)
     */
    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void updateUser(UserDetails user) {
        Assert.isInstanceOf(User.class, user);

        dao.update((User)user);
        userCache.removeUserFromCache(user.getUsername());
    }

    /* (non-Javadoc)
     * @see org.springframework.security.provisioning.UserDetailsManager#userExists(java.lang.String)
     */
    @Override
    public boolean userExists(String username) {
        Assert.hasText(username);

        User user = dao.findUserByUsername(username);
        return user != null;
    }

    /**
     * <b>DO NOT CALL THIS METHOD IN LONG RUNNING SESSIONS OR CONVERSATIONS
     * A THROWN UsernameNotFoundException WILL RENDER THE CONVERSATION UNUSABLE</b>
     *
     * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
     */
    // NOTE: this method must not be secured since it is being used during the
    //       authentication process
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        Assert.hasText(username);
        try {
            User user = dao.findUserByUsername(username);
            if(user == null) {
                throw new UsernameNotFoundException(username);
            }
            return user;
        } catch(NonUniqueResultException nure) {
            throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'", 1);
        }
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void addGroupAuthority(String groupName, GrantedAuthority authority) {
        Assert.hasText(groupName);
        Assert.notNull(authority);

        Group group = groupDao.findGroupByName(groupName);
        if(group.getGrantedAuthorities().add(authority)) {
            groupDao.update(group);
        }
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void addUserToGroup(String username, String groupName) {
        Assert.hasText(username);
        Assert.hasText(groupName);

        Group group = groupDao.findGroupByName(groupName);
        User user = dao.findUserByUsername(username);

        if(group.addMember(user)) {
            groupDao.update(group);
            userCache.removeUserFromCache(user.getUsername());
        }
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void createGroup(String groupName, List<GrantedAuthority> authorities) {
        Assert.hasText(groupName);
        Assert.notNull(authorities);

        Group group = Group.NewInstance(groupName);

        for(GrantedAuthority authority : authorities) {
            group.getGrantedAuthorities().add(authority);
        }

        groupDao.save(group);
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void deleteGroup(String groupName) {
        Assert.hasText(groupName);

        Group group = groupDao.findGroupByName(groupName);
        groupDao.delete(group);
    }

    @Deprecated // use GroupService instead
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public List<String> findAllGroups() {
        return groupDao.listNames(null,null);
    }

    @Deprecated // use GroupService instead
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public List<GrantedAuthority> findGroupAuthorities(String groupName) {
        Assert.hasText(groupName);
        Group group = groupDao.findGroupByName(groupName);

        return new ArrayList<GrantedAuthority>(group.getGrantedAuthorities());
    }

    @Deprecated // use GroupService instead
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public List<String> findUsersInGroup(String groupName) {
        Assert.hasText(groupName);
        Group group = groupDao.findGroupByName(groupName);

        List<String> users = groupDao.listMembers(group, null, null);

        return users;
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void removeGroupAuthority(String groupName,	GrantedAuthority authority) {
        Assert.hasText(groupName);
        Assert.notNull(authority);

        Group group = groupDao.findGroupByName(groupName);

        if(group.getGrantedAuthorities().remove(authority)) {
            groupDao.update(group);
        }
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void removeUserFromGroup(String username, String groupName) {
        Assert.hasText(username);
        Assert.hasText(groupName);

        Group group = groupDao.findGroupByName(groupName);
        User user = dao.findUserByUsername(username);

        if(group.removeMember(user)) {
            groupDao.update(group);
            userCache.removeUserFromCache(user.getUsername());
        }
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public void renameGroup(String oldName, String newName) {
        Assert.hasText(oldName);
        Assert.hasText(newName);

        Group group = groupDao.findGroupByName(oldName);

        group.setName(newName);
        groupDao.update(group);
    }

    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RUN_AS_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID save(User user) {
        if(user.getId() == 0 || dao.load(user.getUuid()) == null){
            createUser(user);
        }else{
            updateUser(user);
        }
        return user.getUuid();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID update(User user) {
        updateUser(user);
        return user.getUuid();
    }

    @Override
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID saveGrantedAuthority(GrantedAuthority grantedAuthority) {
        return grantedAuthorityDao.save((GrantedAuthorityImpl)grantedAuthority);
    }

    @Deprecated // use GroupService instead
    @Transactional(readOnly=false)
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID saveGroup(Group group) {
        return groupDao.save(group);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.api.service.IUserService#listByUsername(java.lang.String, eu.etaxonomy.cdm.persistence.query.MatchMode, java.util.List, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public List<User> listByUsername(String queryString,MatchMode matchmode, List<Criterion> criteria, Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths) {
         Integer numberOfResults = dao.countByUsername(queryString, matchmode, criteria);

         List<User> results = new ArrayList<User>();
         if(numberOfResults > 0) {
                results = dao.findByUsername(queryString, matchmode, criteria, pageSize, pageNumber, orderHints, propertyPaths);
         }
         return results;
    }

    /* ================================================
     *  overriding methods to secure them
     *  via the type level annotation @PreAuthorize
     * ================================================ */

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID delete(User persistentObject) {
        return super.delete(persistentObject);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public Map<UUID, User> save(Collection<User> newInstances) {
        return super.save(newInstances);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public UUID saveOrUpdate(User transientObject) {
        return super.saveOrUpdate(transientObject);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_MANAGER')")
    public Map<UUID, User> saveOrUpdate(Collection<User> transientInstances) {
        return super.saveOrUpdate(transientInstances);
    }

}
