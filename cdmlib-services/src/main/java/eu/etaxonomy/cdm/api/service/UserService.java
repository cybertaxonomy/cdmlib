package eu.etaxonomy.cdm.api.service;

import java.util.List;

import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.SaltSource;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;
import org.springframework.security.providers.dao.salt.ReflectionSaltSource;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.security.providers.encoding.PasswordEncoder;
import org.springframework.security.userdetails.GroupManager;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsManager;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.dao.common.IGroupDao;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;

@Service
@Transactional
public class UserService implements UserDetailsManager, GroupManager {

	protected IUserDao userDao;
	
	protected IGroupDao groupDao;
	
	private SaltSource saltSource = new ReflectionSaltSource();
	
	private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	
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

	@Autowired
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}
	
	@Autowired
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	@Autowired
	public void setUserDao(IUserDao userDao) {
		this.userDao = userDao;
	}
	
	@Autowired
	public void setGroupDao(IGroupDao groupDao) {
		this.groupDao = groupDao;
	}
	
	protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
		UserDetails user = loadUserByUsername(currentAuth.getName());
			
		UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
		newAuthentication.setDetails(currentAuth.getDetails());
			
		return newAuthentication;
	}
	
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
			
			userDao.update((User)user);
			SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication, newPassword));
			userCache.removeUserFromCache(user.getUsername());
		} else {
			throw new AccessDeniedException("Can't change password as no Authentication object found in context for current user.");
		}		
	}

	public void createUser(UserDetails user) {
		Assert.isInstanceOf(User.class, user);
		
		String rawPassword = user.getPassword();
		Object salt = this.saltSource.getSalt(user);
		
		String password = passwordEncoder.encodePassword(rawPassword, salt);
		((User)user).setPassword(password);
		
		userDao.save((User)user);
	}

	public void deleteUser(String username) {
		Assert.hasLength(username);
		
		User user = userDao.findUserByUsername(username); 
        if(user != null) {		
		    userDao.delete((User)user);
        }
        
        userCache.removeUserFromCache(username);
	}

	public void updateUser(UserDetails user) {
		Assert.isInstanceOf(User.class, user);
		
		userDao.update((User)user);
		userCache.removeUserFromCache(user.getUsername());
	}

	public boolean userExists(String username) {
		Assert.hasText(username);
		
		User user = userDao.findUserByUsername(username);
		return user != null;
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		Assert.hasText(username);
		try {
		    User user = userDao.findUserByUsername(username);
		    if(user == null) {
				throw new UsernameNotFoundException(username);
			}
		    return user;
		} catch(NonUniqueResultException nure) {
			throw new IncorrectResultSizeDataAccessException("More than one user found with name '" + username + "'", 1);
		}
	}

	public void addGroupAuthority(String groupName, GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = groupDao.findGroupByName(groupName);
		if(group.getGrantedAuthorities().add(authority)) {
			groupDao.update(group);
		}
	}

	public void addUserToGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		User user = userDao.findUserByUsername(username);
		
		if(group.addMember(user)) {
			groupDao.update(group);
			userCache.removeUserFromCache(user.getUsername());
		}		
	}

	public void createGroup(String groupName, GrantedAuthority[] authorities) {
		Assert.hasText(groupName);
		Assert.notNull(authorities);
		
		Group group = new Group();
		group.setName(groupName);
		
		for(GrantedAuthority authority : authorities) {
			group.getGrantedAuthorities().add(authority);
		}
		
		groupDao.save(group);
	}

	public void deleteGroup(String groupName) {
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		groupDao.delete(group);
	}

	public String[] findAllGroups() {
		List<String> names = groupDao.listNames(null,null);
		return names.toArray(new String[names.size()]);
	}

	public GrantedAuthority[] findGroupAuthorities(String groupName) {
		Assert.hasText(groupName);
		Group group = groupDao.findGroupByName(groupName);
		
		return group.getGrantedAuthorities().toArray(new GrantedAuthority[group.getGrantedAuthorities().size()]);
	}

	public String[] findUsersInGroup(String groupName) {
		Assert.hasText(groupName);
		Group group = groupDao.findGroupByName(groupName);
		
		List<String> users = groupDao.listMembers(group, null, null);
		
		return users.toArray(new String[users.size()]);
	}

	public void removeGroupAuthority(String groupName,	GrantedAuthority authority) {
		Assert.hasText(groupName);
		Assert.notNull(authority);
		
		Group group = groupDao.findGroupByName(groupName);
		
		if(group.getGrantedAuthorities().remove(authority)) {
			groupDao.update(group);
		}
		
	}

	public void removeUserFromGroup(String username, String groupName) {
		Assert.hasText(username);
		Assert.hasText(groupName);
		
		Group group = groupDao.findGroupByName(groupName);
		User user = userDao.findUserByUsername(username);
		
		if(group.removeMember(user)) {
			groupDao.update(group);
			userCache.removeUserFromCache(user.getUsername());
		}
	}

	public void renameGroup(String oldName, String newName) {
		Assert.hasText(oldName);
		Assert.hasText(newName);
		
		Group group = groupDao.findGroupByName(oldName);
		
		group.setName(newName);
		groupDao.update(group);
	}

}
