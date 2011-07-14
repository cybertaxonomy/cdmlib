/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;



import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.unitils.UnitilsJUnit4TestClassRunner;

import org.springframework.test.annotation.ExpectedException;
import org.springframework.transaction.annotation.Transactional;


import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;


import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.query.MatchMode;



@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({"/eu/etaxonomy/cdm/applicationContextSecurity.xml"})
@Transactional
@DataSet
//@Ignore
public class UserServiceImplTest {
	protected static final Logger logger = Logger.getLogger(UserServiceImplTest.class);
	
	@SpringBeanByName
	private AuthenticationManager authenticationManager;
	
	@SpringBeanByName
	private IUserService userService;
	
	@SpringBeanByName
	private ITermService termService;
	
	@SpringBeanByName
	private IGroupService groupService;
	
	@TestDataSource
	protected DataSource dataSource;
	
	private Set<GrantedAuthority> expectedRoles;
	private UsernamePasswordAuthenticationToken token;
	
	private Authentication authentication;
	
	
	UUID uuid;
	@Before
	public void setUp(){
		token = new UsernamePasswordAuthenticationToken("ben", "sPePhAz6");
	}
	
	
	@Test

	public void testCreateUser(){
		
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		String username = "standardUser";
		String password = "pw";
		User user = User.NewInstance(username, password);
		
		userService.createUser(user);
		List<User> userList = userService.listByUsername("standardUser", MatchMode.EXACT, null, null, null, null, null);
		Assert.assertTrue(userList.size()>0);
	}
	
	
	
	@Test
	public void testUpdateUser(){
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		String username = "standardUser";
		String password = "pw";
		User user = User.NewInstance(username, password);
		
		userService.createUser(user);
		user.setEmailAddress("test@bgbm.org");
		
		userService.updateUser(user);
		userService.update(user);
	}
	
	@Test

	public void testIfAnyGranted() {
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
	
        Object p = authentication.getPrincipal();
		Assert.assertTrue(p instanceof User);
		User principal = (User)p;
        
		Assert.assertEquals(principal.getUsername(),"ben");
		
	}
	
	

	@Test
	public void testCreateGroup(){
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);	
	
		GrantedAuthorityImpl userAdminUpdate = GrantedAuthorityImpl.NewInstance();
		userAdminUpdate.setAuthority("USER.update");
		GrantedAuthorityImpl userAdminCreate = GrantedAuthorityImpl.NewInstance();
		userAdminCreate.setAuthority("USER.create");
		GrantedAuthorityImpl userAdminDelete = GrantedAuthorityImpl.NewInstance();
		userAdminDelete.setAuthority("USER.delete");
		List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		list.add(userAdminUpdate);
		list.add(userAdminDelete);
		list.add(userAdminCreate);
				
		userService.createGroup("UserAdmins", list);
		String username = "user3";
		String password = "password";
		User user = User.NewInstance(username, password);
		userService.createUser(user);
		List<String> groups = userService.findAllGroups();
		Assert.assertEquals("CopyEditors", groups.get(0));
				
		token = new UsernamePasswordAuthenticationToken(username, password);
		authentication = authenticationManager.authenticate(token);
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		groupService.addUserToGroup("user3", "UserAdmins");	
		username = "user4";
		password = "password";
		user = User.NewInstance(username, password);
		userService.createUser(user);
	}
	
	
	
	
	@Test
	public void testHasRole(){
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		String username = "ben";
		String newPassword = "password2";
		userService.changePasswordForUser(username, newPassword);
		username = "user4";
		String password = "password";
		User user = User.NewInstance(username, password);
		userService.createUser(user);
		
		userService.changePasswordForUser(username, "newPassword");
				
	}
	
	

     
	
}
