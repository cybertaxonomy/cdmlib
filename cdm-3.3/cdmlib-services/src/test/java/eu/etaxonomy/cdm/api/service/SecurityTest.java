/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;


@DataSet
public class SecurityTest extends CdmTransactionalIntegrationTestWithSecurity{

    private static final UUID UUID_ACHERONTINII = UUID.fromString("928a0167-98cd-4555-bf72-52116d067625");

    private static final UUID UUID_ACHERONTIA_STYX = UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331");

    private static final UUID UUID_LACTUCA = UUID.fromString("b2b007a4-9c8c-43a1-8da4-20ed85464cf2");

    private static final UUID PART_EDITOR_UUID = UUID.fromString("38a251bd-0ba4-426f-8fcb-5c09560749a7");

    private static final String PASSWORD_TAXON_EDITOR = "test2";

    private static final String PASSWORD_ADMIN = "sPePhAz6";

    private static final UUID ACHERONTIA_NODE_UUID = UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7");

    private static final UUID ACHERONTIINI_NODE_UUID = UUID.fromString("cecfa77f-f26a-4476-9d87-a8d993cb55d9");

    private static final UUID ACHERONTIA_LACHESIS_UUID = UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783");

    private static final Logger logger = Logger.getLogger(SecurityTest.class);

    /**
     * The transaction manager to use
     */
    @SpringBeanByType
    PlatformTransactionManager transactionManager;

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private IUserService userService;


    @TestDataSource
    protected DataSource dataSource;

    private Authentication authentication;

    @SpringBeanByType
    private AuthenticationManager authenticationManager;

    @SpringBeanByType
    private SaltSource saltSource;

    @SpringBeanByType
    private PasswordEncoder passwordEncoder;

    @SpringBean("cdmPermissionEvaluator")
    private CdmPermissionEvaluator permissionEvaluator;

    private UsernamePasswordAuthenticationToken tokenForAdmin;

    private UsernamePasswordAuthenticationToken tokenForTaxonEditor;

    private UsernamePasswordAuthenticationToken tokenForDescriptionEditor;

    private UsernamePasswordAuthenticationToken tokenForPartEditor;

    private UsernamePasswordAuthenticationToken tokenForTaxonomist;

    private UsernamePasswordAuthenticationToken tokenForUserManager;


    @Before
    public void setUp(){
        /* User 'admin':
            - ROLE_ADMIN
            - TAXONBASE.READ
            - TAXONBASE.CREATE
            - TAXONBASE.DELETE
            - TAXONBASE.UPDATE
        */
        tokenForAdmin = new UsernamePasswordAuthenticationToken(Configuration.adminLogin, PASSWORD_ADMIN);

        /* User 'userManager':
            - ROLE_ADMIN
            - TAXONBASE.READ
            - TAXONBASE.CREATE
            - TAXONBASE.DELETE
            - TAXONBASE.UPDATE
        */
        tokenForUserManager = new UsernamePasswordAuthenticationToken("userManager", PASSWORD_ADMIN);

        /* User 'taxonEditor':
            - TAXONBASE.CREATE
            - TAXONBASE.UPDATE
        */
        tokenForTaxonEditor = new UsernamePasswordAuthenticationToken("taxonEditor", PASSWORD_TAXON_EDITOR);

        /*  User 'descriptionEditor':
            - DESCRIPTIONBASE.CREATE
            - DESCRIPTIONBASE.UPDATE
            - DESCRIPTIONELEMENT(Ecology).CREATE
            - DESCRIPTIONELEMENT(Ecology).UPDATE
         */
        tokenForDescriptionEditor = new UsernamePasswordAuthenticationToken("descriptionEditor", "test");

        /* User 'partEditor':
            - TAXONBASE.ADMIN
            - TAXONNODE.CREATE{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
            - TAXONNODE.UPDATE{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
         */
        tokenForPartEditor = new UsernamePasswordAuthenticationToken("partEditor", "test4");

        /* User 'taxonomist':
            - TAXONBASE.READ
            - TAXONBASE.CREATE
            - TAXONBASE.DELETE
            - TAXONBASE.UPDATE
         */
        tokenForTaxonomist = new UsernamePasswordAuthenticationToken("taxonomist", "test4");
    }


    /**
     * no assertions in this test, since it is only used to create password hashes for test data
     */
    @Test
    public void testEncryptPassword(){

        String password = PASSWORD_ADMIN;
        User user = User.NewInstance("userManager", "");

        Object salt = this.saltSource.getSalt(user);
        String passwordEncrypted = passwordEncoder.encodePassword(password, salt);
        logger.info("encrypted password: " + passwordEncrypted );
    }

    @Test
    @DataSet
    public void testHasPermission(){

        Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()),null);

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, taxon, Operation.UPDATE);
        assertTrue(hasPermission);

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        hasPermission = permissionEvaluator.hasPermission(authentication, taxon, Operation.UPDATE);
        assertFalse(hasPermission);
    }

    @Test
    @DataSet
    public void testListByUsernameAllow(){

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        List<User> userList = userService.listByUsername("Editor", MatchMode.ANYWHERE, null, null, 0, null, null);
        Assert.assertTrue("The user list must have elements", userList.size() > 0 );
    }

    @Test
    @DataSet
    public void testUserService_CreateDeny(){

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        RuntimeException exception = null;
        try {
            userService.createUser(User.NewInstance("new guy", "alkjdsfalkj"));
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(EvaluationFailedException.class, e);
            logger.debug("Expected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNotNull("Must fail here!", exception);

    }

    @Test
    @DataSet
    public void testUserService_CreateAllow(){

        authentication = authenticationManager.authenticate(tokenForUserManager);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        RuntimeException exception = null;
        try {
            userService.createUser(User.NewInstance("new guy", "alkjdsfalkj"));
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(EvaluationFailedException.class, e);
            logger.error("unexpected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("Must not fail here!", exception);

    }


    @Test
    @DataSet
    @Ignore // FIXME http://dev.e-taxonomy.eu/trac/ticket/3098
    public void testHasPermissions(){

        Taxon taxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()),null);

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        boolean hasPermission = permissionEvaluator.hasPermission(authentication, taxon, Operation.ALL);
        assertTrue(hasPermission);
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public final void testSaveTaxon() {

        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID uuid = taxonService.save(expectedTaxon);
        commitAndStartNewTransaction(null);
        //taxonService.getSession().flush();
        TaxonBase<?> actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        taxonService.saveOrUpdate(actualTaxon);
        commitAndStartNewTransaction(null);

    }

    @Test
    public void testChangeOwnPassword(){

        SecurityContext context = SecurityContextHolder.getContext();
        // authenticate as admin
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context.setAuthentication(authentication);

//        User currentUser =  (User) context.getAuthentication().getPrincipal();

        String newPass = "poiweorijo";
        userService.changePassword(PASSWORD_TAXON_EDITOR, newPass);
        commitAndStartNewTransaction(null);

        // try to re-authenticate user with changed password
        UsernamePasswordAuthenticationToken newTokenForTaxonEditor = new UsernamePasswordAuthenticationToken("taxonEditor", newPass);
        authentication = authenticationManager.authenticate(newTokenForTaxonEditor);
    }

    @Test
    public void testChangeOthersPasswordAllow(){

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException exception = null;

        // (1) authenticate as admin
        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);


        try{
            userService.changePasswordForUser("taxonomist", "zuaisd");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(EvaluationFailedException.class, e);
            logger.error("Unexpected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("must not fail here!", exception);

        // ok, now try authenticating taxonomist with new password
        UsernamePasswordAuthenticationToken newToken = new UsernamePasswordAuthenticationToken("taxonomist", "zuaisd");
        authentication = authenticationManager.authenticate(newToken);
    }

    @Test
    public void testChangeOthersPasswordDeny(){

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException exception = null;

        // (2) authenticate as under privileged user - not an admin !!!
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        // check test preconditions user name and authorities
        Assert.assertEquals("descriptionEditor", context.getAuthentication().getName());
        Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
        for(GrantedAuthority authority: authorities){
            // role prefix 'ROLE_' is defined in org.springframework.security.access.vote.RoleVoter !!!
            Assert.assertNotSame("user must not have authority 'ROLE_ADMIN'", "ROLE_ADMIN", authority.getAuthority());
        }
        // finally perform the test :
        try{
            userService.changePasswordForUser("partEditor", "poiweorijo");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(EvaluationFailedException.class, e);
            logger.debug("Expected failure of evaluation.", exception);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNotNull("must fail here!", exception);
    }

    @Test
    public void testUpdateUser(){

        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        String username = "standardUser";
        String password = "pw";
        User user = User.NewInstance(username, password);

        userService.createUser(user);
        user.setEmailAddress("test@bgbm.org");

        userService.updateUser(user);
        userService.update(user);
        userService.saveOrUpdate(user);
        commitAndStartNewTransaction(null);

    }

    /**
     * test with admin account - should succeed
     */
    @Test
    public final void testTaxonSaveOrUpdateAllow_1() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        TaxonBase<?> taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try{
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The change must be persited", taxon.isDoubtful());
    }

    /**
     * test with taxonEditor account - should succeed
     */
    @Test
    public final void testTaxonSaveOrUpdateAllow_2() {


        RuntimeException securityException= null;
        SecurityContext context = SecurityContextHolder.getContext();

         // taxonEditor account - should succeed
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);

        context.setAuthentication(authentication);

        TaxonBase<?>  taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try{
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The change must be persited", taxon.isDoubtful());
    }

    /**
     * test with tokenForDescriptionEditor account - should fail
     */
    @Test
    public final void testTaxonSaveOrUpdateDeny_2() {

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException securityException = null;

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonBase<?> taxon = taxonService.load(UUID_ACHERONTIA_STYX);

        Assert.assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try {
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNotNull("evaluation must fail since the user is not permitted", securityException);
        // reload taxon
        taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertFalse("The change must not be persited", taxon.isDoubtful());
    }

    /**
     * test with admin account - should succeed
     */
    @Test
    public final void testTaxonDeleteAllow_1() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        TaxonBase<?> taxon = taxonService.load(UUID_LACTUCA);
        try{
            taxonService.delete(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_LACTUCA);
        Assert.assertNull("The taxon must be deleted", taxon);
    }

    /**
     * test with admin account - should succeed
     */
    @Test
    @Ignore
    /*FIXME fails due to org.hibernate.ObjectDeletedException: deleted object would be re-saved by cascade (remove deleted object from associations)
     *       see ticket #3086
     */
    public final void testTaxonDeleteAllow_2() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        TaxonBase<?> taxon = taxonService.load(UUID_ACHERONTINII);
        try{
            taxonService.delete(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_ACHERONTINII);
        Assert.assertNull("The taxon must be deleted", taxon);
    }


    /**
     * test with tokenForDescriptionEditor account - should fail
     */
    @Test
    public final void testTaxonDeleteDeny() {

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException securityException = null;

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonBase<?> taxon = taxonService.load(UUID_LACTUCA);

        try {
            taxonService.delete(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNotNull("evaluation must fail since the user is not permitted", securityException);
        // reload taxon
        taxon = taxonService.load(UUID_LACTUCA);
        Assert.assertNotNull("The change must still exist", taxon);
    }


    @Test
    @Ignore //FIXME: adding taxa to a description must be protected at the side of the Description itself!!
            //        => protecting method TaxonDescription.setTaxon() ?
    public void testAddDescriptionToTaxon(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        RuntimeException securityException = null;

        Taxon taxon = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.setTitleCache("test");
        try {
            descriptionService.saveOrUpdate(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        /*
         * Expectation:
         * The user should not be granted to add the Description to a taxon
         */
        Assert.assertNotNull("evaluation should fail since the user is not permitted to edit Taxa", securityException);
        taxon = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);
        assertTrue(taxon.getDescriptions().contains(description));
    }

    @Test
    public void testCreateDescriptionWithElement(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Assert.assertTrue("taxon must not yet have descriptions", taxon.getDescriptions().size() == 0);


        // 1) test for failure - description element but no feature
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase textdataNoFeature = TextData.NewInstance();
        description.addElement(textdataNoFeature);

        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.saveOrUpdate(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("RuntimeException caught");
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNotNull("evaluation should fail", securityException);
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue("taxon must not have any description", descriptions.size() == 0);

    }

    @Test
    public void testCreateDescriptionWithElementDeny_1(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Assert.assertTrue("taxon must not yet have descriptions", taxon.getDescriptions().size() == 0);

        // 2) test for failure  - description element but not granted feature
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase descriptionText = TextData.NewInstance(Feature.DESCRIPTION());
        description.addElement(descriptionText);

        securityException = null;
        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.saveOrUpdate(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNotNull("evaluation should fail", securityException);
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue("taxon must not have any description", descriptions.size() == 0);

    }

    @Test
    public void testCreateDescriptionWithElementDeny_2(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Assert.assertTrue("taxon must not yet have descriptions", taxon.getDescriptions().size() == 0);

        // 3) test for failure
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase ecologyText = TextData.NewInstance(Feature.ECOLOGY());
        description.addElement(ecologyText);

        securityException = null;
        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.saveOrUpdate(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue("taxon must now have one description", descriptions.size() == 1);
        assertTrue("description should have one description element", descriptions.iterator().next().getElements().size() == 1);
    }

    @Test
    public void testSaveSynonymAllow(){

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException securityException = null;

        // 1) test for success
        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        context.setAuthentication(authentication);

        Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID synUuid = UUID.randomUUID();
        syn.setUuid(synUuid);
        try{
            taxonService.saveOrUpdate(syn);
            logger.debug("will commit ...");
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        Assert.assertNotNull("The new Synonym must be persited", taxonService.find(synUuid));
    }

    @Test
    public void testSaveSynonymDenial(){

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException securityException = null;
        // 2) test for denial
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);
        securityException = null;
        Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID synUuid = syn.getUuid();
        try{
            taxonService.saveOrUpdate(syn);
            logger.debug("will commit ...");
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation: " + securityException.getClass());
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        Assert.assertNotNull("evaluation must fail since the user is not permitted", securityException);
        Assert.assertNull("The Synonym must not be persited", taxonService.find(synUuid));
    }

    @Test
    public void testEditPartOfClassificationAllow(){

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        RuntimeException securityException = null;

        // test for success
        TaxonNode acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        long numOfChildNodes = acherontia_node.getChildNodes().size();
        TaxonNode childNode = acherontia_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null);

        try{
            taxonNodeService.saveOrUpdate(acherontia_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        Assert.assertEquals("the acherontia_node must now have one more child node ", numOfChildNodes + 1 , acherontia_node.getChildNodes().size());
    }

    @Test
    public void testEditPartOfClassificationDeny(){

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        RuntimeException securityException = null;

        // test for denial
        securityException = null;
        TaxonNode acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        int numOfChildNodes = acherontiini_node.getCountChildren();
        acherontiini_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null);

        try{
            logger.debug("==============================");
            taxonNodeService.saveOrUpdate(acherontiini_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        Assert.assertNotNull("evaluation must fail since the user is not permitted", securityException);
        Assert.assertEquals("the number of child nodes must be unchanged ", numOfChildNodes , acherontiini_node.getChildNodes().size());

    }

}
