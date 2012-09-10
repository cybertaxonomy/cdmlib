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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
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
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
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

import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;


@DataSet
public class SecurityTest extends CdmTransactionalIntegrationTestWithSecurity{

    private static final UUID UUID_ACHERONTIA_STYX = UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331");

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



    @Before
    public void setUp(){
        tokenForAdmin = new UsernamePasswordAuthenticationToken("admin", PASSWORD_ADMIN);
        tokenForTaxonEditor = new UsernamePasswordAuthenticationToken("taxonEditor", PASSWORD_TAXON_EDITOR);
        tokenForDescriptionEditor = new UsernamePasswordAuthenticationToken("descriptionEditor", "test");
        tokenForPartEditor = new UsernamePasswordAuthenticationToken("partEditor", "test4");
        tokenForTaxonomist = new UsernamePasswordAuthenticationToken("taxonomist", "test4");
    }

    /**
     * no assertions in this test, since it is only used to create password hashes for test data
     */
    @Test
    public void testEncryptPassword(){

        String password = PASSWORD_ADMIN;
        User user = User.NewInstance("admin", "");

        Object salt = this.saltSource.getSalt(user);
        String passwordEncrypted = passwordEncoder.encodePassword(password, salt);
        logger.info("encrypted password: " + passwordEncrypted );
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
     */
    @Test
    public final void testSaveTaxon() {
        /*
        Md5PasswordEncoder encoder =new Md5PasswordEncoder();
        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("getUsername");
        User user = User.NewInstance("partEditor", "test4");
        System.err.println(encoder.encodePassword("test4", saltSource.getSalt(user)));

        */
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
    @Ignore //FIXME no need to test this, no access controll needed for userService.changePassword
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
    public void testChangeOthersPassword(){

        SecurityContext context = SecurityContextHolder.getContext();
        // (1) authenticate as admin
        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);

        RuntimeException exception = null;

        try{
            userService.changePasswordForUser("taxonomist", "zuaisd");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(EvaluationFailedException.class, e);
            logger.debug("Unexpected failure of evaluation.", exception);
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

        // (2) authenticate as under privileged user - not an admin !!!
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        // check test preconditions user name and authorities
        Assert.assertEquals("descriptionEditor", context.getAuthentication().getName());
        Collection<GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
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

    @Test
    public final void testSaveOrUpdateTaxon() {
        SecurityContext context = SecurityContextHolder.getContext();

        // 1) test with admin account - should succeed
        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);

        TaxonBase<?> taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        taxon.setDoubtful(true);
        RuntimeException securityException= null;
        try{
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.debug("Unexpected failure of evaluation.", securityException);
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

        // 2) test with taxonEditor account - should succeed
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context.setAuthentication(authentication);

        taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        taxon.setDoubtful(false);
        securityException= null;
        try{
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.debug("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertFalse("The change must be persited", taxon.isDoubtful());

        // 3) test with tokenForDescriptionEditor account - should fail
//        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
//        context.setAuthentication(authentication);
//        taxon = taxonService.load(uuid);
//
//        taxon.setDoubtful(true);
//        taxonService.saveOrUpdate(taxon);
//        commitAndStartNewTransaction(null);

    }

    @Test
    public void testCascadingInSpringSecurityAccesDenied(){
        /*authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        */
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Taxon taxon =(Taxon) taxonService.load(ACHERONTIA_LACHESIS_UUID);
        taxon.setDoubtful(false);
        assertTrue(permissionEvaluator.hasPermission(authentication, taxon, "UPDATE"));
        taxonService.save(taxon);
        commitAndStartNewTransaction(null);
        taxon = null;

        //during cascading the permissions are not evaluated, but with hibernate listener every database transaction can be interrupted, but how to manage it,
        //when someone has the rights to save descriptions, but not taxa (the editor always saves everything by saving the taxon)
        //taxonService.saveOrUpdate(taxon);

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        //taxonService.saveOrUpdate(taxon);

        taxon =(Taxon) taxonService.load(ACHERONTIA_LACHESIS_UUID);

        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.setTitleCache("test");
        descriptionService.saveOrUpdate(description);
        commitAndStartNewTransaction(null);
        taxon = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);
        assertTrue(taxon.getDescriptions().contains(description));
    }

    @Test
    public void testCascadingInSpring(){
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Taxon taxon = (Taxon)taxonService.load(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
        TaxonDescription description = TaxonDescription.NewInstance(taxon);
        description.addElement(Distribution.NewInstance());
        CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));

        descriptionService.saveOrUpdate(description);

        taxon = (Taxon)taxonService.load(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue(descriptions.contains(description));

    }

    @Test
    public void testSaveSynonym(){

        SecurityContext context = SecurityContextHolder.getContext();

        // 1) test for success
        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        context.setAuthentication(authentication);

        RuntimeException securityException = null;
        Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        UUID synUuid = UUID.randomUUID();
        syn.setUuid(synUuid);
        try{
            taxonService.saveOrUpdate(syn);
            logger.debug("will commit ...");
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        Assert.assertNotNull("The new Synonym must be persited", taxonService.find(synUuid));

        // 2) test for denial
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);
        securityException = null;
        syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        synUuid = syn.getUuid();
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
    public void testEditPartOfClassification(){
        /*
         * the user 'partEditor' has the following authorities:
         *
         *  - TAXONNODE.CREATE{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
         *  - TAXONNODE.UPDATE{20c8f083-5870-4cbd-bf56-c5b2b98ab6a7}
         *
         *  that is 'partEditor' is granted to edit the subtree of
         *  which ACHERONTIA_NODE_UUID [20c8f083-5870-4cbd-bf56-c5b2b98ab6a7] is the root node.
         */

        authentication = authenticationManager.authenticate(tokenForPartEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // test for success
        RuntimeException securityException = null;
        TaxonNode acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        long numOfChildNodes = acherontia_node.getChildNodes().size();
        TaxonNode childNode = acherontia_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null, null);

        try{
            taxonNodeService.saveOrUpdate(acherontia_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        Assert.assertEquals("the acherontia_node must now have one more child node ", numOfChildNodes + 1 , acherontia_node.getChildNodes().size());

        // test for denial
        securityException = null;
        TaxonNode acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        numOfChildNodes = acherontiini_node.getCountChildren();
        acherontiini_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null, null);

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

    public static void main(String[] args){
        Md5PasswordEncoder encoder =new Md5PasswordEncoder();

        ReflectionSaltSource saltSource = new ReflectionSaltSource();
        saltSource.setUserPropertyToUse("getUsername");
        User user = User.NewInstance("taxonomist", "test4");
        System.err.println(encoder.encodePassword("test4", saltSource.getSalt(user)));
    }




}
