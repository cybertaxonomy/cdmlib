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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateSystemException;
import org.springframework.security.access.vote.RoleVoter;
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
import org.springframework.test.annotation.ExpectedException;
import org.springframework.transaction.PlatformTransactionManager;


import org.unitils.database.annotations.Transactional;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;


import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.User;


import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;

import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;
import eu.etaxonomy.cdm.test.unitils.CleanSweepInsertLoadStrategy;


@DataSet
public class SecurityTest extends CdmTransactionalIntegrationTestWithSecurity{

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



    private UsernamePasswordAuthenticationToken tokenForAdmin;

    @SpringBeanByType
    private SaltSource saltSource;

    @SpringBeanByType
    private PasswordEncoder passwordEncoder;


    @Before
    public void setUp(){
        tokenForAdmin = new UsernamePasswordAuthenticationToken("admin", "sPePhAz6");
    }

    /**
     * no assertions in this test, since it is only used to create password hashes for test data
     */
    @Test
    public void testEncryptPassword(){

        String password = "sPePhAz6";
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
        //taxonService.getSession().flush();
        TaxonBase<?> actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        tokenForAdmin = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
        authentication = authenticationManager.authenticate(tokenForAdmin);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
        taxonService.saveOrUpdate(actualTaxon);

    }

    @Test
    public void testChangePassword(){

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

    }

    @Test
    public final void testSaveOrUpdateTaxon() {
        authentication = authenticationManager.authenticate(tokenForAdmin);
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        Taxon expectedTaxon = Taxon.NewInstance(null, null);
        UUID uuid = taxonService.save(expectedTaxon);
        TaxonBase<?> actualTaxon = taxonService.load(uuid);
        assertEquals(expectedTaxon, actualTaxon);

        actualTaxon.setName(BotanicalName.NewInstance(Rank.SPECIES()));
        taxonService.saveOrUpdate(actualTaxon);

        tokenForAdmin = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
        authentication = authenticationManager.authenticate(tokenForAdmin);
        context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        actualTaxon = taxonService.load(uuid);

        actualTaxon.setDoubtful(true);
        taxonService.saveOrUpdate(actualTaxon);

    }

    @Test
    public void testCascadingInSpringSecurityAccesDenied(){
        /*authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        */

        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("taxonEditor", "test2"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();

        Taxon taxon =(Taxon) taxonService.load(ACHERONTIA_LACHESIS_UUID);
        taxon.setDoubtful(false);
        assertTrue(permissionEvaluator.hasPermission(authentication, taxon, "UPDATE"));
        taxonService.save(taxon);
        taxon = null;
        commitAndStartNewTransaction(null);

        //during cascading the permissions are not evaluated, but with hibernate listener every database transaction can be interrupted, but how to manage it,
        //when someone has the rights to save descriptions, but not taxa (the editor always saves everything by saving the taxon)
        //taxonService.saveOrUpdate(taxon);


        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
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
        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
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
        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("taxonomist", "test4"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
        taxonService.saveOrUpdate(syn);
    }

    @Test
    @Ignore //FIXME test must not fail !!!!!
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
        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        // test for success
        TaxonNode acherontia_node = taxonNodeService.load(ACHERONTIA_NODE_UUID);
        long numOfChildNodes = acherontia_node.getChildNodes().size();
        TaxonNode childNode = acherontia_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null, null);
        EvaluationFailedException evaluationFailedException = null;
        try{
            taxonNodeService.saveOrUpdate(acherontia_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            evaluationFailedException = findEvaluationFailedExceptionIn(e);
            logger.debug("Unexpected failure of evaluation.", evaluationFailedException);
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + evaluationFailedException.getMessage(), evaluationFailedException);
        Assert.assertEquals("the acherontia_node must now have one more child node ", numOfChildNodes + 1 , acherontia_node.getChildNodes().size());

        // test for denial
        evaluationFailedException = null;
        TaxonNode acherontiini_node = taxonNodeService.load(ACHERONTIINI_NODE_UUID);
        numOfChildNodes = acherontiini_node.getCountChildren();
        acherontiini_node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null, null);
        try{
            taxonNodeService.saveOrUpdate(acherontiini_node);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            evaluationFailedException = findEvaluationFailedExceptionIn(e);
        }
        Assert.assertNotNull("evaluation must fail since the user is not permitted", evaluationFailedException);
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
