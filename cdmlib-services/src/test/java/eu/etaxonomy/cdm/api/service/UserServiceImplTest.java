/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.api.service;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.test.integration.CdmIntegrationTest;

public class UserServiceImplTest extends CdmIntegrationTest {
	
	@SpringBeanByType
	private AuthenticationManager authenticationManager;
	
	@SpringBeanByType
	private IUserService userService;
	
	private Set<GrantedAuthority> expectedRoles;
	private UsernamePasswordAuthenticationToken token;
	
	@Before
	public void setUp() {
		expectedRoles = new HashSet<GrantedAuthority>();
		GrantedAuthorityImpl publish = new GrantedAuthorityImpl();
		publish.setAuthority("Publish");
		publish.setUuid(UUID.fromString("441a3c40-0c84-11de-8c30-0800200c9a66"));
		publish.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl edit = new GrantedAuthorityImpl();
		edit.setAuthority("Edit");
		edit.setUuid(UUID.fromString("14788361-1a7e-4eed-b22f-fd90a0b424ac"));
		edit.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl annotate = new GrantedAuthorityImpl();
		annotate.setAuthority("Annotate");
		annotate.setUuid(UUID.fromString("fa56073c-0ffd-4384-b459-b2f07e35b689"));
		annotate.setCreated(new DateTime(2009,2,3,17,52,26,0));
		GrantedAuthorityImpl checkAnnotation = new GrantedAuthorityImpl();
		checkAnnotation.setAuthority("CheckAnnotation");
		checkAnnotation.setUuid(UUID.fromString("e5354c0e-657b-4b4d-bb2f-791612199711"));
		checkAnnotation.setCreated(new DateTime(2009,2,3,17,52,26,0));
		expectedRoles.add(publish);
		expectedRoles.add(edit);
		expectedRoles.add(annotate);
		expectedRoles.add(checkAnnotation);
		token = new UsernamePasswordAuthenticationToken("ben","sPePhAz6");
	}
	
	@Test
	@DataSet
	public void testIfAnyGranted() {
        
		Authentication authentication = authenticationManager.authenticate(token);
        Object p = authentication.getPrincipal();
		Assert.assertTrue(p instanceof User);
		User principal = (User)p;
        
		Assert.assertEquals(principal.getUsername(),"ben");
		
		Assert.assertNotNull(expectedRoles);
		Assert.assertEquals(expectedRoles.size(), authentication.getAuthorities().size());
	}
	
	@Test
	public void testAuthentication() {
		String username = "username";
		String password = "password";
		User user = User.NewInstance(username, password);
		
		userService.save(user);
		
//		userService.createUser(user);
		
		
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		
		authenticationManager.authenticate(token);
	}
}
