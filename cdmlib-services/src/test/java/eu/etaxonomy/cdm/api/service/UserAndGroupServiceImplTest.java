/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;


import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;


/**
 * @author a.kohlbecker
 * @since Feb 4, 2014
 *
 */
@DataSet(value="SecurityTest.xml")
public class UserAndGroupServiceImplTest extends AbstractSecurityTestBase {

    protected static final Logger logger = Logger.getLogger(UserAndGroupServiceImplTest.class);

    @SpringBeanByType
    private AuthenticationManager authenticationManager;

    @SpringBeanByType
    private IUserService userService;

    @SpringBeanByType
    private IGroupService groupService;

    @SpringBeanByType
    private IGrantedAuthorityService grantedAuthorityService;

    @SpringBeanByType
    private ITaxonService taxonService;


    private Authentication authentication;


    @Test
    public void testCreateUser() {


        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);


        try{
            userService.createUser(User.NewInstance("new user 1", "00000"));
        }catch(Exception e){
            Assert.fail();
        }

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.createUser(User.NewInstance("new user 2", "00000"));
            Assert.fail();
        }catch(Exception e){
            Assert.assertEquals("Access is denied", e.getMessage());
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
            Assert.fail("the user TaxonEditor should be able to change its own email address");
        }
        */

        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        user.setEmailAddress("user@bgbm.org");

        try{
            userService.updateUser(user);
        }catch (Exception e){
            Assert.fail("the user UserManager should be able to change others email addresses");
        }

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        try{
            userService.updateUser(user);
            Assert.fail("the user PartEditor should NOT be able to change others email addresses");
        }catch (Exception e){
            Assert.assertEquals("Access is denied", e.getMessage());
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
        Assert.assertNotNull("must fail here!", exception);

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
        GrantedAuthorityImpl rolePublishAthotrity = GrantedAuthorityImpl.NewInstance(null);
        rolePublishAthotrity.setAuthority(Role.ROLE_PUBLISH.toString()); // testing if creating a Role from string is working
        authorityList.add(rolePublishAthotrity);

        String publishersGroupName = "publishers";

        groupService.createGroup(publishersGroupName, authorityList);

        commitAndStartNewTransaction(null);

        List<GrantedAuthority> groupAuthorities = groupService.findGroupAuthorities(publishersGroupName);

        Assert.assertEquals(Role.ROLE_PUBLISH.toString(), groupAuthorities.get(0).getAuthority());

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
                Assert.fail("an authority '" + Role.ROLE_PUBLISH + "' must not yet exists");
            }
        }
        for(Group group : user.getGroups()){
            if(group.getName().equals(newGroupName)){
                Assert.fail("the group '" + newGroupName + "' must not yet exists");
            }
        }

        // -----------------------------------------------------------------
        // authenticate as UserManager to be able to add the role ROLE_PUBLISH in various ways
        authentication = authenticationManager.authenticate(tokenForUserManager);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // create an entity of ROLE_PUBLISH and save it to the database
        grantedAuthorityService.save(Role.ROLE_PUBLISH.asNewGrantedAuthority());
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
        Assert.assertTrue("the new authority '" + Role.ROLE_PUBLISH + "' is missing", newAuthorityFound);

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
        Assert.assertTrue("the new authority '" + Role.ROLE_PUBLISH + "' is missing in existing group", newAuthorityFoundInExistingGroup);

        // 3. check new group
        boolean newGroupFound = false;
        for(Group group : user.getGroups()){
            if(group.getName().equals(newGroupName)){
                newGroupFound = true;
                break;
            }
        }
        Assert.assertTrue("the new group '" + newGroupName + "' is missing", newGroupFound);

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
                Assert.fail("The user TaxonEditor should no longer be member of this group");
            }

        }

    }


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
