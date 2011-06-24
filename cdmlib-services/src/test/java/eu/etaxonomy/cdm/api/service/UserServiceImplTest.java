/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;


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
import org.springframework.transaction.annotation.Transactional;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;

import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.permission.CdmPermission;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.query.MatchMode;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/eu/etaxonomy/cdm/applicationContextSecurity.xml"})
@Transactional
public class UserServiceImplTest {
    protected static final Logger logger = Logger.getLogger(UserServiceImplTest.class);
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
        this.dataSource=dataSource;
    }



    @TestDataSource
    protected DataSource dataSource;

    private Set<GrantedAuthority> expectedRoles;
    private UsernamePasswordAuthenticationToken token;

    private Authentication authentication;

    private PermissionEvaluator permissionEvaluator;

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

        expectedRoles.add(update);
        expectedRoles.add(annotate);
        expectedRoles.add(checkAnnotation);
        String username = "useradmin";
        String password = "password";
        User user = User.NewInstance(username, password);
        user.setAccountNonExpired(true);
        user.setGrantedAuthorities(expectedRoles);
        userService.save(user);
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



        GrantedAuthorityImpl userAdminEdit = GrantedAuthorityImpl.NewInstance();
        userAdminEdit.setAuthority("USER."+CdmPermission.UPDATE);
        GrantedAuthorityImpl userAdminCreate = GrantedAuthorityImpl.NewInstance();
        userAdminCreate.setAuthority("USER."+CdmPermission.CREATE);
        GrantedAuthorityImpl userAdminDelete = GrantedAuthorityImpl.NewInstance();
        userAdminDelete.setAuthority("USER."+CdmPermission.DELETE);
        List<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
        list.add(userAdminEdit);
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
        }catch(Exception e){
            Assert.assertEquals("Access is denied", e.getMessage());
        }
        groupService.addUserToGroup("user3", "UserAdmins");

        logger.debug(context.getAuthentication().getName());
        try{
            userService.createUser(user);
        }catch(Exception e){
            e.printStackTrace();
            Assert.fail(e.getMessage() + " User is member of 'UserAdmins' and thus should be granted creating users");
        }

    }








}
