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
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBean;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

@Ignore //FIXME tests are failing
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-testSecurity.xml")
public class UserServiceImplTest extends CdmIntegrationTest {

	protected static final Logger logger = Logger.getLogger(UserServiceImplTest.class);

	@SpringBeanByType
	private AuthenticationManager authenticationManager;

	@SpringBeanByType
	private IUserService userService;

	@SpringBeanByType
	private IGroupService groupService;

	@SpringBeanByType
	private ITaxonService taxonService;


//	@SpringBeanByType
//    public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
//        this.dataSource=dataSource;
//    }


//
//	@TestDataSource
//	protected DataSource dataSource;

	private Set<GrantedAuthority> expectedRoles;
	private UsernamePasswordAuthenticationToken token;

	private Authentication authentication;

	private PermissionEvaluator permissionEvaluator;
	UUID uuid;

	@Before
	public void setUp() {
		expectedRoles = new HashSet<GrantedAuthority>();

		GrantedAuthorityImpl update = GrantedAuthorityImpl.NewInstance();
		update.setAuthority("USER.Update");
		update.setUuid(UUID.fromString("14788361-1a7e-4eed-b22f-fd90a0b424ac"));
		update.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl annotate = GrantedAuthorityImpl.NewInstance();
		annotate.setAuthority("USER.Create");
		annotate.setUuid(UUID.fromString("fa56073c-0ffd-4384-b459-b2f07e35b689"));
		annotate.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl checkAnnotation = GrantedAuthorityImpl.NewInstance();
		checkAnnotation.setAuthority("USER.Delete");
		checkAnnotation.setUuid(UUID.fromString("e5354c0e-657b-4b4d-bb2f-791612199711"));
		checkAnnotation.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl userAdmin = GrantedAuthorityImpl.NewInstance();
		userAdmin.setAuthority("USER.Admin");

		expectedRoles.add(update);
		expectedRoles.add(annotate);
		expectedRoles.add(checkAnnotation);
		expectedRoles.add(userAdmin);
		String username = "useradmin";
		String password = "password";
		User user = User.NewInstance(username, password);
		user.setAccountNonExpired(true);
		user.setGrantedAuthorities(expectedRoles);
		uuid = userService.save(user);

		User standardUser =  User.NewInstance("standardUser", "pw");
		uuid = userService.save(standardUser);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		permissionEvaluator = new CdmPermissionEvaluator();
	}


	@Test
	@DataSet
	public void testCreateUser() {
		String username = "user2";
		String password = "password";
		User user = User.NewInstance(username, password);

		userService.createUser(user);

		List<User> userList = userService.listByUsername("user2", MatchMode.EXACT, null, null, null, null, null);
		Assert.assertNotNull(userList);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("standardUser", "pw");
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		try{
			userService.createUser(user);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals("Access is denied", e.getMessage());
		}
	}



	@Test
	@DataSet
	public void testUpdateUser(){
		User user= userService.find(uuid);
		user.setEmailAddress("test@bgbm.org");
		try{
		userService.updateUser(user);
		}catch (Exception e){
			Assert.fail();
		}

		try{
			userService.update(user);
			}catch (Exception e){
				Assert.fail();
			}
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("standardUser", "pw");
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		user.setEmailAddress("user@bgbm.org");
		try{
		userService.updateUser(user);
		Assert.fail();
		}catch (Exception e){
			Assert.assertEquals("Access is denied", e.getMessage());
		}

		try{
			userService.saveOrUpdate(user);
			Assert.fail();
		}catch (Exception e){
			Assert.assertEquals("Access is denied", e.getMessage());
		}
		try{
			userService.update(user);
			Assert.fail();
		}catch (Exception e){
			Assert.assertEquals("Access is denied", e.getMessage());
		}
	}

	@Test
	@DataSet
	public void testIfAnyGranted() {
        Object p = authentication.getPrincipal();
		Assert.assertTrue(p instanceof User);
		User principal = (User)p;

		Assert.assertEquals(principal.getUsername(),"useradmin");

		Assert.assertNotNull(expectedRoles);
		Assert.assertEquals(expectedRoles.size(), authentication.getAuthorities().size());
	}


	@Test
	@DataSet
	public void testCreateGroup(){

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
		Assert.assertEquals("UserAdmins", groups.get(0));


		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);

		username = "user4";
		password = "password";
		user = User.NewInstance(username, password);
		try{
			userService.createUser(user);
			Assert.fail();
		}catch(Exception e){
			Assert.assertEquals("Access is denied", e.getMessage());
		}
		groupService.addUserToGroup("user3", "UserAdmins");

	//	System.err.println(context.getAuthentication().getName());
		try{
			userService.createUser(user);
		}catch(Exception e){
			System.err.println(e.getMessage());
			Assert.fail();
		}

	}


	@Test
	@DataSet
	public void testHasRole(){
		String username = "useradmin";
		String newPassword = "password2";
		userService.changePasswordForUser(username, newPassword);
		username = "user4";
		String password = "password";
		User user = User.NewInstance(username, password);
		userService.createUser(user);
		try{
			userService.changePasswordForUser(username, "newPassword");
		}catch (Exception e){
			System.err.println(e.getMessage());
			Assert.fail();
		}
	}


	@Test
	@DataSet
	public void testHasPermission(){
		Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()),null);
		boolean hasPermission = taxonService.hasPermission(authentication, taxon, CdmPermission.UPDATE);
		assertFalse(hasPermission);
		User testUser = User.NewInstance("username123", "1234");
		hasPermission = userService.hasPermission(authentication, testUser, CdmPermission.UPDATE);
		assertTrue(hasPermission);
	}




}
