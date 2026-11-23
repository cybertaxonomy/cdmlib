/**
 * Copyright (C) 2011 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.metadata.SecReferenceHandlingEnum;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Operation;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthorityParsingException;
import eu.etaxonomy.cdm.persistence.permission.ICdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.permission.PermissionDeniedException;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;

@CdmDataSet(cleanSweep = true)
public class SecurityTest extends AbstractSecurityTestBase{

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ITaxonService taxonService;

    @Autowired
    private INameService nameService;

    @Autowired
    private IReferenceService referenceService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    @Autowired
    private IDescriptionService descriptionService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ICdmPermissionEvaluator permissionEvaluator;

    @Autowired
    protected DataSource dataSource;

    private Authentication authentication;

    /**
     * no assertions in this test, since it is only used to create password hashes for test data
     */
    @Test
    public void testEncryptPassword(){

        String password = PASSWORD_ADMIN;
        String passwordEncrypted = passwordEncoder.encode(password);
        logger.info("encrypted password: " + passwordEncrypted );
    }

    @Test
    @CdmDataSet
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
    @CdmDataSet
    public void testListByUsernameAllow(){

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        List<User> userList = userService.listByUsername("Editor", MatchMode.ANYWHERE, null, null, 0, null, null);
        assertTrue(userList.size() > 0, "The user list must have elements");
    }

    @Test
    @CdmDataSet
    public void testLoadUserByUsernameAllow(){

        authentication = authenticationManager.authenticate(tokenForTaxonomist);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        User user = userService.loadUserByUsernameAsUser("taxonEditor");
        assertNotNull(user, "The user list must have elements");
    }

    @Test
    @CdmDataSet
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNotNull(exception, "Must fail here!");
    }

    @Test
    @CdmDataSet
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "Must not fail here!");

    }

    @Test
    @CdmDataSet
    @Disabled // FIXME https://dev.e-taxonomy.eu/redmine/issues/3098
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
        TaxonBase actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        expectedTaxon = Taxon.NewInstance(TaxonNameFactory.NewBotanicalInstance(Rank.GENUS()), null);
        expectedTaxon.getName().setTitleCache("Newby taxonEditor", true);
        uuid = taxonService.save(expectedTaxon).getUuid();
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
        UUID uuid = nameService.save(newName).getUuid();
        commitAndStartNewTransaction(null);
        TaxonName savedName = nameService.load(uuid);
        assertEquals(newName, savedName);
    }

    @Test
    @Disabled  //#5829  should be fixed as soon as possible
    public final void testReuseNameAllow() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        TaxonBase taxon = taxonService.find(UUID_ACHERONTIA_STYX);
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "must not fail here!");
    }

    @Test
    public final void testMakeTaxonNodeASynonymOfAnotherTaxonNodeAllow_1() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Exception exception = null;
        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(
                    ACHERONTIA_STYX_NODE_UUID, ACHERONTIA_LACHESIS_NODE_UUID, SynonymType.HETEROTYPIC_SYNONYM_OF,
                    BOOK1_UUID , "33", SecReferenceHandlingEnum.KeepOrWarn, true);
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        }finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "must not fail here!");
    }

    @Test
    public final void testMakeTaxonNodeASynonymOfAnotherTaxonNodeAllow_2() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Exception exception = null;
        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(
                    ACHERONTIA_LACHESIS_NODE_UUID, ACHERONTIA_STYX_NODE_UUID,
                    SynonymType.HOMOTYPIC_SYNONYM_OF, BOOK1_UUID , "33", null, true);
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        }  finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "must not fail here!");
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
        GrantedAuthorityImpl referenceUpdate_ga = new CdmAuthority(PermissionClass.REFERENCE, null, EnumSet.of(CRUD.UPDATE), null).asNewGrantedAuthority();
        grantedAuthorities.add(referenceUpdate_ga);
        taxonEditor.setGrantedAuthorities(grantedAuthorities);
        commitAndStartNewTransaction(null);

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Reference book = referenceService.load(BOOK1_UUID);
        book.setTitleCache("Mobydick", true);
        Exception exception = null;
        try {
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = e;
        } catch (RuntimeException e){
            logger.error("Unexpected failure of evaluation.", e);
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "must not fail here!");
        book = referenceService.load(BOOK1_UUID);
        assertEquals("Mobydick", book.getTitleCache());
    }

    @Test
    public final void testUpdateReferenceDeny() {

        authentication = authenticationManager.authenticate(tokenForTaxonEditor);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        TaxonBase taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        Reference nomRef = taxon.getName().getNomenclaturalReference();
        UUID refUuid = nomRef.getUuid();
        nomRef.setTitleCache("Mobydick", true);
        Exception exception = null;
        try {
            commitAndStartNewTransaction(null);
        } catch (AccessDeniedException e){
            logger.debug("Expected failure of evaluation.", e);
            exception  = e;
        } catch (RuntimeException e){
            exception = findThrowableOfTypeIn(PermissionDeniedException.class, e);
            logger.debug("Expected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNotNull(exception, "Updating reference should not be allowed, only CREATE and READ! An exception should be thrown");
        Reference updatedRef = referenceService.find(refUuid);
        assertEquals("Lorem ipsum", updatedRef.getTitleCache(), "TitleCache should still be the old titleCache");
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(exception, "must not fail here!");

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
        assertEquals("descriptionEditor", context.getAuthentication().getName());
        Collection<? extends GrantedAuthority> authorities = context.getAuthentication().getAuthorities();
        for(GrantedAuthority authority: authorities){
            // role prefix 'ROLE_' is defined in org.springframework.security.access.vote.RoleVoter !!!
            assertNotSame("ROLE_ADMIN", authority.getAuthority(), "user must not have authority 'ROLE_ADMIN'");
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNotNull(exception, "must fail here!");
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
    public final void testTaxonUpdateAllow_1() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);
        RuntimeException securityException= null;

        TaxonBase taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try{
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        // reload taxon
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        assertTrue(taxon.isDoubtful(), "The change must be persisted");
    }

    /**
     * test with taxonEditor account - should succeed
     */
    @Test
    public final void testTaxonUpdateAllow_2() {

        RuntimeException securityException= null;
        SecurityContext context = SecurityContextHolder.getContext();

         // taxonEditor account - should succeed
        authentication = authenticationManager.authenticate(tokenForTaxonEditor);

        context.setAuthentication(authentication);

        TaxonBase  taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try{
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        // reload taxon
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        assertTrue(taxon.isDoubtful(), "The change must be persisted");
    }

    /**
     * test with tokenForDescriptionEditor account - should fail
     */
    @Test
    public final void testTaxonUpdateDeny_2() {

        SecurityContext context = SecurityContextHolder.getContext();
        RuntimeException securityException = null;

        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonBase taxon = taxonService.find(UUID_ACHERONTIA_STYX);

        assertFalse(taxon.isDoubtful());
        taxon.setDoubtful(true);
        try {
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNotNull(securityException, "evaluation must fail since the user is not permitted");
        // reload taxon
        taxon = taxonService.find(UUID_ACHERONTIA_STYX);
        assertFalse(taxon.isDoubtful(), "The change must not be persisted");
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
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user has ROLE_ADMIN, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        // reload taxon
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        assertTrue(taxon.isPublish() != lastIsPublish, "The change must be persisted");
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
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user has ROLE_ADMIN, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        // reload taxon
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        assertTrue(taxon.isPublish() != lastIsPublish, "The change must be persisted");
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
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNotNull(securityException, "evaluation must fail since the user is not permitted");
        // reload taxon
        taxon = (Taxon) taxonService.find(UUID_ACHERONTIA_STYX);
        assertTrue(taxon.isPublish() == lastIsPublish, "The taxon must be unchanged");
    }

    /**
     * test with admin account - should succeed
     */
    @Test
    public final void testTaxonDeleteAllow_1() {

        SecurityContext context = SecurityContextHolder.getContext();

        authentication = authenticationManager.authenticate(tokenForAdmin);
        context.setAuthentication(authentication);

        TaxonBase taxon = taxonService.load(UUID_LACTUCA);
        try {
            taxonService.delete(taxon);
            commitAndStartNewTransaction(null);
        } catch (Exception e) {
            fail("Evaluation must not fail since the user is permitted, CAUSE :" + e.getMessage());
        }
        // reload taxon
        taxon = taxonService.load(UUID_LACTUCA);
        assertNull(taxon, "The taxon must be deleted");
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
                fail();
            }*/
            if (!result.isOk()){
            	fail();
            }
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException  = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        // reload taxon

        taxon = (Taxon)taxonService.find(UUID_ACHERONTINII);
        assertNull(taxon, "The taxon must be deleted");
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
            fail();
        }
        }catch(PermissionDeniedException e){

        }
       endTransaction();
       startNewTransaction();

        //assertNotNull(securityException, "evaluation must fail since the user is not permitted");
        // reload taxon
        taxon = (Taxon)taxonService.load(UUID_LACTUCA);

        assertNotNull(taxon, "The change must still exist");
        assertNotNull(taxon.getName(), "The name must still exist");
    }

    @Test
    @Disabled //FIXME: adding taxa to a description must be protected at the side of the Description itself!!
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
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        /*
         * Expectation:
         * The user should not be granted to add the Description to a taxon
         */
        assertNotNull(securityException, "evaluation should fail since the user is not permitted to edit Taxa");
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
            descriptionService.moveDescriptionElementsToDescription(getUuidSet(description_acherontia_styx.getElements()), description_acherontia_lachesis, false, true);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        /*
         * Expectation:
         */
        assertNull(securityException, "evaluation should not fail since the user has sufficient permissions");

    }

    /**
     * Convert entity set to uuid set
     */
    private Set<UUID> getUuidSet(Collection<DescriptionElementBase> factSet) {
        return factSet.stream().map(f->f.getUuid()).collect(Collectors.toSet());
    }

//    @Disabled // FIXME https://dev.e-taxonomy.eu/redmine/issues/4081 : #4081 (TaxonNodeServiceImpl.makeTaxonNodeASynonymOfAnotherTaxonNode() requires TAXONNAME.[UPDATE])
    @Test
    public void testAcceptedTaxonToSynomym(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForPartEditor);
        context.setAuthentication(authentication);

        RuntimeException securityException = null;

        Taxon t_acherontia_lachesis = (Taxon)taxonService.load(ACHERONTIA_LACHESIS_UUID);
        UUID name_acherontia_lachesis_uuid = t_acherontia_lachesis.getName().getUuid();
        Taxon t_acherontia_styx = (Taxon)taxonService.load(UUID_ACHERONTIA_STYX);

        TaxonNode n_acherontia_lachesis = t_acherontia_lachesis.getTaxonNodes().iterator().next();
        TaxonNode n_acherontia_styx = t_acherontia_styx.getTaxonNodes().iterator().next();

        int numOfSynonymsBefore_styx = t_acherontia_styx.getSynonyms().size();
        int numOfSynonymsBefore_lachesis = t_acherontia_lachesis.getSynonyms().size();

        try {
            taxonNodeService.makeTaxonNodeASynonymOfAnotherTaxonNode(
                    n_acherontia_lachesis.getUuid(), n_acherontia_styx.getUuid(),
                    SynonymType.SYNONYM_OF, null, null, null, true);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected Exception ", e);
            fail("Unexpected Exception: " + e.getMessage());
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        /*
         * Expectation:
         */
        assertNull(securityException, "evaluation should not fail since the user has sufficient permissions");

        // reload from db and check assertions
        t_acherontia_styx = (Taxon)taxonService.load(UUID_ACHERONTIA_STYX);
        assertEquals(numOfSynonymsBefore_styx +1 + numOfSynonymsBefore_lachesis, t_acherontia_styx.getSynonyms().size());

        assertNotNull(nameService.load(name_acherontia_lachesis_uuid) );
        assertNull(taxonNodeService.find(n_acherontia_lachesis.getUuid()), "The old TaxonNode should no longer exist");
    }

    @Test
    public void testCreateDescriptionWithElement(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        assertTrue(taxon.getDescriptions().size() == 0, "taxon must not yet have descriptions");

        // 1) test for failure - description element but no feature
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase textdataNoFeature = TextData.NewInstance();
        description.addElement(textdataNoFeature);

        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.save(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("RuntimeException caught");
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNotNull(securityException, "evaluation should fail");
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue(descriptions.size() == 0, "taxon must not have any description");

    }

    @Test
    public void testCreateDescriptionWithElementDeny_1(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        assertTrue(taxon.getDescriptions().size() == 0, "taxon must not yet have descriptions");

        // 2) test for failure  - description element but not granted feature
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase descriptionText = TextData.NewInstance(Feature.DESCRIPTION());
        description.addElement(descriptionText);

        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.save(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNotNull(securityException, "evaluation should fail");
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue(descriptions.size() == 0, "taxon must not have any description");

    }

    @Test
    public void testCreateDescriptionWithElementDeny_2(){

        SecurityContext context = SecurityContextHolder.getContext();
        authentication = authenticationManager.authenticate(tokenForDescriptionEditor);
        context.setAuthentication(authentication);

        TaxonDescription description = null;
        RuntimeException securityException = null;
        Taxon taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        assertTrue(taxon.getDescriptions().size() == 0, "taxon must not yet have descriptions");

        // 3) test for failure
        description = TaxonDescription.NewInstance(taxon);
        DescriptionElementBase ecologyText = TextData.NewInstance(Feature.ECOLOGY());
        description.addElement(ecologyText);

        assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
        try{
            descriptionService.save(description);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        taxon = (Taxon)taxonService.load(UUID_ACHERONTINII);
        Set<TaxonDescription> descriptions = taxon.getDescriptions();
        assertTrue(descriptions.size() == 1, "taxon must now have one description");
        assertTrue(descriptions.iterator().next().getElements().size() == 1, "description should have one description element");
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
            taxonService.save(syn);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        assertNotNull(taxonService.find(synUuid), "The new Synonym must be persisted");
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
            taxonService.save(syn);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation: " + securityException.getClass());
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        assertNotNull(securityException, "evaluation must fail since the user is not permitted");
        assertNull(taxonService.find(synUuid), "The Synonym must not be persisted");
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
            taxonNodeService.save(acherontia_child_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
        assertEquals(numOfChildNodes + 1, acherontia_node.getChildNodes().size(), "the acherontia_node must now have one more child node ");
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
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.debug("Expected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        assertNotNull(securityException, "evaluation must fail since the user is not permitted");
        assertEquals(numOfChildNodes, acherontiini_node.getChildNodes().size(), "the number of child nodes must be unchanged ");

    }

    @Test
    @CdmDataSet
    public void testUpgradeMd5Password(){

        int partEditorId = 5;
        User partEditor = userService.find(partEditorId);
        assertEquals("{md5}41af8a6dac9f86b1081aa5840df75a53", partEditor.getPassword());

        Authentication auth = authenticationManager.authenticate(tokenForPartEditor);
        assertTrue(auth.isAuthenticated());

        partEditor = userService.find(partEditorId);
        //not necessarily required, may also become false in future
//        assertEquals("{md5}41af8a6dac9f86b1081aa5840df75a53", partEditor.getPassword(), "Password not yet changed in old session");
//        testIsMd5Password(partEditor, true);

        //new session
        commitAndStartNewTransaction();
        partEditor = userService.find(partEditorId);
        testIsMd5Password(partEditor, false);
        auth = authenticationManager.authenticate(tokenForPartEditor);
        assertTrue(auth.isAuthenticated());
    }

    private void testIsMd5Password(User partEditor, boolean expectedValue) {
        assertEquals(expectedValue, partEditor.getPassword().startsWith("{md5}"));
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}

}