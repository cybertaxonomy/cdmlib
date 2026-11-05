/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.permission.PermissionDeniedException;
import eu.etaxonomy.cdm.persistence.permission.Role;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;

/**
 * @author a.kohlbecker
 * @since Feb 4, 2014
 */
@CdmDataSet("SecurityTest.xml")
public class UserAndGroupServiceImplTest extends AbstractSecurityTestBase {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;

    @Autowired
    private IGrantedAuthorityService grantedAuthorityService;

    private Authentication authentication;

    @Test
    public void testCreateUser() {

        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.createUser(User.NewInstance("new_user_1", "00000"));
        }catch(Exception e){
            fail();
        }

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.createUser(User.NewInstance("new_user_2", "00000"));
            fail();
        }catch(Exception e){
            assertEquals("Access is denied", e.getMessage());
        }
    }

    @Test
    public void testUpdateUser(){

        // TaxonEditor should be able to change its own email address
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        User user= userService.find(TAXON_EDITOR_UUID);
        user.setEmailAddress("test@bgbm.org");

        /* FIXME
        try{
            userService.updateUser(user);
        }catch (Exception e){
            fail("the user TaxonEditor should be able to change its own email address");
        }
        */

        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        user.setEmailAddress("user@bgbm.org");

        try{
            userService.updateUser(user);
        }catch (Exception e){
            fail("the user UserManager should be able to change others email addresses");
        }

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.updateUser(user);
            fail("the user PartEditor should NOT be able to change others email addresses");
        }catch (Exception e){
            assertEquals("Access is denied", e.getMessage());
        }
    }

    @Test
    public void testChangePassword(){

        // the user TaxonEditor should be able to change its own password
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        userService.changePasswordForUser(tokenForTaxonEditor.getName(), "newPassword");

        Exception exception = null;
        // the user TaxonEditor should NOT be able to change others passwords
        try{
            userService.changePasswordForUser(tokenForAdmin.getName(), "newPassword");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception  = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNotNull(exception, "must fail here!");

        // the user User manager should be able to change others passwords
        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        userService.changePasswordForUser(tokenForAdmin.getName(), "newPassword");
    }

    @Test
    public void testCreateGroup(){

        authentication = authenticationManager.authenticate(tokenForUserManager);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        GrantedAuthorityImpl roleRemotingAuthority = GrantedAuthorityImpl.NewInstance(null);
        roleRemotingAuthority.setAuthority(Role.ROLE_REMOTING.toString()); // testing if creating a Role from string is working
        authorityList.add(roleRemotingAuthority);

        String remotingGroupName = "publishers";

        groupService.createGroup(remotingGroupName, authorityList);

        commitAndStartNewTransaction(null);

        List<GrantedAuthority> groupAuthorities = groupService.findGroupAuthorities(remotingGroupName);

        assertEquals(Role.ROLE_REMOTING.toString(), groupAuthorities.get(0).getAuthority());
    }

    @Test
    public void testRefreshUser(){

        String newGroupName = "new_publishers";

        // -----------------------------------------------------------------
        // authenticate as TaxonEditor to load the user for the first time and to let cache it in the session
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();

        // check that everything is clean
        for(GrantedAuthority authority : user.getAuthorities()){
            if(authority.equals(Role.ROLE_PUBLISH)){
                fail("an authority '" + Role.ROLE_PUBLISH + "' must not yet exists");
            }
        }
        for(Group group : user.getGroups()){
            if(group.getName().equals(newGroupName)){
                fail("the group '" + newGroupName + "' must not yet exists");
            }
        }

        // -----------------------------------------------------------------
        // authenticate as UserManager to be able to add the role ROLE_PUBLISH in various ways
        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // create an entity of ROLE_PUBLISH and save it to the database
        commitAndStartNewTransaction(null);
        GrantedAuthorityImpl rolePublish = grantedAuthorityService.load(Role.ROLE_PUBLISH.getUuid());

        user = userService.load(TAXON_EDITOR_UUID);

        // 1. add to the users GrantedAuthorities
        // TODO is there any other way to do this?
        Set<GrantedAuthority> grantedAuthorities = user.getGrantedAuthorities();
        grantedAuthorities.add(rolePublish);
        user.setGrantedAuthorities(grantedAuthorities);
        userService.saveOrUpdate(user);

        commitAndStartNewTransaction(null);

        // 2. add to existing group
        Group group_special_editor = groupService.load(GROUP_SPECIAL_EDITOR_UUID);
        String groupSpecialEditor_Name = group_special_editor.getName();
        rolePublish = grantedAuthorityService.load(Role.ROLE_PUBLISH.getUuid());
        group_special_editor.addGrantedAuthority(rolePublish);
        groupService.saveOrUpdate(group_special_editor);

        commitAndStartNewTransaction(null);

        // 3. add in new group
        Group groupNewPublishers = Group.NewInstance(newGroupName);
        rolePublish = grantedAuthorityService.load(Role.ROLE_PUBLISH.getUuid());
        groupNewPublishers.addGrantedAuthority(rolePublish);
        groupService.saveOrUpdate(groupNewPublishers);
        groupService.addUserToGroup(user.getUsername(), newGroupName);

        commitAndStartNewTransaction(null);

        // -----------------------------------------------------------------
        // again authenticate as TaxonEditor
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // and check if everything is updated
        user = (User) authentication.getPrincipal();

        // 1. check users authorities for the role
        boolean newAuthorityFound = false;
        for(GrantedAuthority authority : user.getAuthorities()){
            if(authority.equals(Role.ROLE_PUBLISH)){
                newAuthorityFound = true;
                break;
            }
        }
        assertTrue(newAuthorityFound, "the new authority '" + Role.ROLE_PUBLISH + "' is missing");

        // 2. check for role in existing group
        boolean newAuthorityFoundInExistingGroup = false;
        for(Group group : user.getGroups()){
            if(group.getUuid().equals(GROUP_SPECIAL_EDITOR_UUID)){
                for(GrantedAuthority authority : group.getGrantedAuthorities()){
                    if(authority.equals(Role.ROLE_PUBLISH)){
                        newAuthorityFoundInExistingGroup = true;
                        break;
                    }
                }
                if(newAuthorityFoundInExistingGroup){
                    break;
                }
            }
        }
        assertTrue(newAuthorityFoundInExistingGroup, "the new authority '" + Role.ROLE_PUBLISH + "' is missing in existing group");

        // 3. check new group
        boolean newGroupFound = false;
        for(Group group : user.getGroups()){
            if(group.getName().equals(newGroupName)){
                newGroupFound = true;
                break;
            }
        }
        assertTrue(newGroupFound, "the new group '" + newGroupName + "' is missing");

        // ==================================================================
        // again authenticate as UserManager to be able to add the role ROLE_PUBLISH in various ways
        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        groupService.removeUserFromGroup(user.getUsername(), groupSpecialEditor_Name);

        commitAndStartNewTransaction(null);

        // -----------------------------------------------------------------
        // again authenticate as TaxonEditor
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // and check if the group has been removed from the user
        user = (User) authentication.getPrincipal();

        for(Group group: user.getGroups()){
            if(group.getName().equals(groupSpecialEditor_Name)){
                fail("The user TaxonEditor should no longer be member of this group");
            }
        }
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}
