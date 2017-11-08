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

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.junit.Assert;
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
import org.unitils.database.annotations.TestDataSource;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.database.PermissionDeniedException;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
import eu.etaxonomy.cdm.persistence.hibernate.permission.ICdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Operation;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.query.MatchMode;


@DataSet
public class SecurityTest extends AbstractSecurityTestBase{


    private static final Logger logger = Logger.getLogger(SecurityTest.class);

    @SpringBeanByType
    private ITaxonService taxonService;

    @SpringBeanByType
    private INameService nameService;

    @SpringBeanByType
    private IReferenceService referenceService;

    @SpringBeanByType
    private ITaxonNodeService taxonNodeService;

    @SpringBeanByType
    private IDescriptionService descriptionService;

    @SpringBeanByType
    private IUserService userService;

    @SpringBeanByType
    private IClassificationService classificationService;

    @SpringBeanByType
    private AuthenticationManager authenticationManager;

    @SpringBeanByType
    private SaltSource saltSource;

    @SpringBeanByType
    private PasswordEncoder passwordEncoder;

    @SpringBean("cdmPermissionEvaluator")
    private ICdmPermissionEvaluator permissionEvaluator;

    @TestDataSource
    protected DataSource dataSource;

    private Authentication authentication;


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

        Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()),null);

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
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", e);
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
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
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

        Taxon taxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()),null);

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

        Taxon expectedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
        expectedTaxon.getName().setTitleCache("Newby admin", true);
        UUID uuid = taxonService.save(expectedTaxon).getUuid();
        commitAndStartNewTransaction(null);
        TaxonBase<?> actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        expectedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);
        expectedTaxon.getName().setTitleCache("Newby taxonEditor", true);
        uuid = taxonService.saveOrUpdate(expectedTaxon);
        commitAndStartNewTransaction(null);
        actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

    }

    @Test
    public final void testSaveNameAllow() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        TaxonName newName = TaxonNameFactory.NewZoologicalInstance(Rank.SPECIES());
        newName.setTitleCache("Newby taxonEditor", true);
        UUID uuid = nameService.saveOrUpdate(newName);
        commitAndStartNewTransaction(null);
        TaxonName savedName = nameService.load(uuid);
        assertEquals(newName, savedName);
    }


    @Test
    @Ignore  //#5829  should be fixed as soon as possible
    public final void testReuseNameAllow() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        TaxonBase<?> taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        TaxonName n_acherontia_thetis = taxon.getName();

        Reference sec = ReferenceFactory.newGeneric();
        sec.setUuid(UUID.fromString("bd7e4a15-6403-49a9-a6df-45b46fa99efd"));
        Taxon newTaxon = Taxon.NewInstance(n_acherontia_thetis, sec);
        Exception exception = null;
        try {
            taxonService.save(newTaxon);
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("must not fail here!", exception);
    }

    @Test
    public final void testMakeTaxonNodeASynonymOfAnotherTaxonNodeAllow_1() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Reference book = referenceService.load(BOOK1_UUID);

        TaxonNode n_acherontia_styx = taxonNodeService.find(ACHERONTIA_STYX_NODE_UUID);
        TaxonNode n_acherontia_lachersis = taxonNodeService.find(ACHERONTIA_LACHESIS_NODE_UUID);

        Exception exception = null;
        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(n_acherontia_styx, n_acherontia_lachersis, SynonymType.HETEROTYPIC_SYNONYM_OF(), book , "33");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        }finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("must not fail here!", exception);
    }

    @Test
    public final void testMakeTaxonNodeASynonymOfAnotherTaxonNodeAllow_2() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Reference book = referenceService.load(BOOK1_UUID);

        TaxonNode n_acherontia_styx = taxonNodeService.find(ACHERONTIA_STYX_NODE_UUID);
        TaxonNode n_acherontia_lachersis = taxonNodeService.find(ACHERONTIA_LACHESIS_NODE_UUID);

        Exception exception = null;
        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(n_acherontia_lachersis, n_acherontia_styx, SynonymType.HOMOTYPIC_SYNONYM_OF(), book , "33");
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        }  finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("must not fail here!", exception);
    }

    @Test
    public final void testUpdateReferenceAllow() throws CdmAuthorityParsingException {


        authentication = authenticationManager.authenticate(tokenForUserManager);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // add REFERENCE[UPDATE] to taxonEditor
        User taxonEditor = userService.load(TAXON_EDITOR_UUID);
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
        grantedAuthorities.addAll(taxonEditor.getGrantedAuthorities());
        GrantedAuthorityImpl referenceUpdate_ga = new CdmAuthority(CdmPermissionClass.REFERENCE, null, EnumSet.of(CRUD.UPDATE), null).asNewGrantedAuthority();
        grantedAuthorities.add(referenceUpdate_ga);
        taxonEditor.setGrantedAuthorities(grantedAuthorities);
        userService.saveOrUpdate(taxonEditor);
        commitAndStartNewTransaction(null);

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Reference book = referenceService.load(BOOK1_UUID);
        book.setTitleCache("Mobydick", true);
        Exception exception = null;
        try {
            referenceService.saveOrUpdate(book);
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("must not fail here!", exception);
        book = referenceService.load(BOOK1_UUID);
        Assert.assertEquals("Mobydick", book.getTitleCache());
    }

    @Test
    public final void testUpateReferenceDeny() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        TaxonBase<?> taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        taxon.getName().getNomenclaturalReference().setTitleCache("Mobydick", true);
        Exception exception = null;
        try {
            taxonService.saveOrUpdate(taxon);
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception  = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNotNull("must fail here!", exception);
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
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
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
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", e);
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

        TaxonBase<?> taxon = taxonService.find(UUID_ACHERONTIA_STYX);
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
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The change must be persisted", taxon.isDoubtful());
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

        TaxonBase<?>  taxon = taxonService.find(UUID_ACHERONTIA_STYX);
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
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
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

        TaxonBase<?> taxon = taxonService.find(UUID_ACHERONTIA_STYX);

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
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        Assert.assertFalse("The change must not be persited", taxon.isDoubtful());
    }

    @Test
    public final void testTaxonPublishAllow_ROLE_ADMIN() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        Taxon taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);

        boolean lastIsPublish = taxon.isPublish();
        taxon.setPublish(!lastIsPublish);
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
        Assert.assertNull("evaluation must not fail since the user has ROLE_ADMIN, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The change must be persisted", taxon.isPublish() != lastIsPublish);
    }


    /**
     * test with Taxonomist account which has the ROLE_PUBLISH
     */
    @Test
    public final void testTaxonPublishAllow_ROLE_PUBLISH() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        Taxon taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);

        boolean lastIsPublish = taxon.isPublish();
        taxon.setPublish(!lastIsPublish);
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
        Assert.assertNull("evaluation must not fail since the user has ROLE_ADMIN, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The change must be persisted", taxon.isPublish() != lastIsPublish);
    }

    /**
     * test with TaxonEditor account which has not the ROLE_PUBLISH
     */
    @Test
    public final void testTaxonPublishDeny() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        Taxon taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);

        boolean lastIsPublish = taxon.isPublish();
        taxon.setPublish(!lastIsPublish);
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
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        Assert.assertTrue("The taxon must be unchanged", taxon.isPublish() == lastIsPublish);
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
        taxonService.delete(taxon);
        commitAndStartNewTransaction(null);

        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
        // reload taxon
        taxon = taxonService.load(UUID_LACTUCA);
        Assert.assertNull("The taxon must be deleted", taxon);
    }

    /**
     * test with admin account - should succeed
     */
    @Test
   public final void testTaxonDeleteAllow_2() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        try{
           // try {
        	DeleteResult result = taxonService.deleteTaxon(taxon.getUuid(), null, taxon.getTaxonNodes().iterator().next().getClassification().getUuid());
            /*} catch (DataChangeNoRollbackException e) {
                Assert.fail();
            }*/
            if (!result.isOk()){
            	Assert.fail();
            }
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

        taxon = (Taxon)taxonService.find(UUID_ACHERONTINII);
        Assert.assertNull("The taxon must be deleted", taxon);
    }


    /**
     * test with tokenForDescriptionEditor account - should fail
     */
    @Test
    public final void testTaxonDeleteDeny() {

        SecurityContext context = SecurityContextHolder.getContext();
//        RuntimeException securityException = null;

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        Taxon taxon = (Taxon)taxonService.load(UUID_LACTUCA);
        try{
        DeleteResult result = taxonService.deleteTaxon(taxon.getUuid(), null, null);
        if(result.isOk()){
            Assert.fail();
        }
        }catch(PermissionDeniedException e){

        }
       endTransaction();
       startNewTransaction();


        //Assert.assertNotNull("evaluation must fail since the user is not permitted", securityException);
        // reload taxon
        taxon = (Taxon)taxonService.load(UUID_LACTUCA);

        Assert.assertNotNull("The change must still exist", taxon);
        Assert.assertNotNull("The name must still exist",taxon.getName());
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
        description.setTitleCache("test", true);
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
    public void testMoveDescriptionElement(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        context.setAuthentication(authentication);

        RuntimeException securityException = null;

        Taxon t_acherontia_lachesis = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);
        Taxon t_acherontia_styx = (Taxon)taxonService.load(UUID_ACHERONTIA_STYX);

        TaxonDescription description_acherontia_styx = t_acherontia_styx.getDescriptions().iterator().next();
        TaxonDescription description_acherontia_lachesis = t_acherontia_lachesis.getDescriptions().iterator().next();

        try {
            descriptionService.moveDescriptionElementsToDescription(description_acherontia_styx.getElements(), description_acherontia_lachesis, false);
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
        /*
         * Expectation:
         */
        Assert.assertNull("evaluation should not fail since the user has sufficient permissions", securityException);

    }

//    @Ignore // FIXME http://dev.e-taxonomy.eu/trac/ticket/4081 : #4081 (TaxonNodeServiceImpl.makeTaxonNodeASynonymOfAnotherTaxonNode() requires TAXONNAME.[UPDATE])
    @Test
    public void testAcceptedTaxonToSynomym(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForPartEditor);
        context.setAuthentication(authentication);

        RuntimeException securityException = null;

        Taxon t_acherontia_lachesis = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);
        UUID name_acherontia_lachesis_uuid = t_acherontia_lachesis.getName().getUuid();
        Taxon t_acherontia_styx = (Taxon)taxonService.load(UUID_ACHERONTIA_STYX);
        int countSynsBefore = t_acherontia_styx.getSynonyms().size();

        TaxonNode n_acherontia_lachesis = t_acherontia_lachesis.getTaxonNodes().iterator().next();
        TaxonNode n_acherontia_styx = t_acherontia_styx.getTaxonNodes().iterator().next();

        int numOfSynonymsBefore_styx = t_acherontia_styx.getSynonyms().size();
        int numOfSynonymsBefore_lachesis = t_acherontia_lachesis.getSynonyms().size();


        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(n_acherontia_lachesis, n_acherontia_styx, SynonymType.SYNONYM_OF(), null, null);
//            synonymUuid = synonym.getUuid();
//            taxonService.saveOrUpdate(synonym);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected Exception ", e);
            Assert.fail("Unexpected Exception: " + e.getMessage());
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        /*
         * Expectation:
         */
        Assert.assertNull("evaluation should not fail since the user has sufficient permissions", securityException);

        // reload from db and check assertions
        t_acherontia_styx = (Taxon)taxonService.load(UUID_ACHERONTIA_STYX);
        Assert.assertEquals(numOfSynonymsBefore_styx +1 + numOfSynonymsBefore_lachesis, t_acherontia_styx.getSynonyms().size());

        Assert.assertNotNull(nameService.load(name_acherontia_lachesis_uuid) );
        Assert.assertNull("The old TaxonNode should no longer exist", taxonNodeService.find(n_acherontia_lachesis.getUuid()));
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

        Synonym syn = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
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
        Synonym syn = Synonym.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null);
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
        classificationService.load(UUID.fromString("aeee7448-5298-4991-b724-8d5b75a0a7a9"));
        // test for success
        TaxonNode acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        long numOfChildNodes = acherontia_node.getChildNodes().size();
        TaxonNode acherontia_child_node = acherontia_node.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES()), null), null, null);

        try{
            taxonNodeService.saveOrUpdate(acherontia_child_node);
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
        TaxonNode acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        int numOfChildNodes = acherontiini_node.getCountChildren();
        acherontiini_node.addChildTaxon(Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null), null, null);

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

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }

}
