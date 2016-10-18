/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.dbunit.annotation.ExpectedDataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.common.IUserDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.RandomOrder;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;

/**
 * @author a.mueller
 *
 */
public class CdmEntityDaoBaseTest extends CdmTransactionalIntegrationTestWithSecurity {

    private UUID uuid;
    private TaxonBase<?> cdmBase;

    @SpringBeanByType
    private ITaxonDao cdmEntityDaoBase;

    @SpringBeanByType
    private AuthenticationManager authenticationManager;

    @SpringBeanByType
    private IUserDao userDao;

    private TestingAuthenticationToken taxonEditorToken;
    private TestingAuthenticationToken adminToken;
    private TestingAuthenticationToken testerToken;



    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        uuid = UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66");
        cdmBase = Taxon.NewInstance(null, null);
        cdmBase.setUuid(UUID.fromString("e463b270-c76b-11dd-ad8b-0800200c9a66"));

        taxonEditorToken = new TestingAuthenticationToken("taxonEditor", "password",  "TAXONBASE.[CREATE]", "TAXONBASE.[UPDATE]");
        adminToken = new TestingAuthenticationToken("admin", "password",  "ALL.ADMIN");
        testerToken = new TestingAuthenticationToken("tester", "password");


        // Clear the context prior to each test
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(TestingAuthenticationToken token) {
         Authentication authentication = authenticationManager.authenticate(token);

        SecurityContextImpl secureContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(secureContext);
        secureContext.setAuthentication(authentication);
    }

/************ TESTS ********************************/


    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#CdmEntityDaoBase(java.lang.Class)}.
     * @throws Exception
     */
    @Test
    public void testCdmEntityDaoBase() throws Exception {
        assertNotNull("cdmEntityDaoBase should exist",cdmEntityDaoBase);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testSaveOrUpdate() {
        TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.saveOrUpdate(cdmBase);
        commit();
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testSaveOrUpdateWithAuthentication() {

        setAuthentication(taxonEditorToken);
        TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.saveOrUpdate(cdmBase);
        commit();
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#saveOrUpdate(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testSaveOrUpdateNewObjectWithAuthentication() {
//		printDataSet(System.err, new String[]{"TAXONBASE", "HIBERNATE_SEQUENCES"});
        setAuthentication(taxonEditorToken);
        RuntimeException securityException = null;

        // 1) test create
        try{
            cdmEntityDaoBase.saveOrUpdate(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
            Assert.fail();
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        // 1) test update
        cdmBase = cdmEntityDaoBase.findByUuid(cdmBase.getUuid());
        cdmBase.setDoubtful(true);
        try{
            cdmEntityDaoBase.saveOrUpdate(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
            Assert.fail();
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

    }
    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testSave() throws Exception {
        cdmEntityDaoBase.save(cdmBase);
        commit();
    }


    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testSaveWithAuthenticationFailedPermissionEvaluation() throws Exception {
        setAuthentication(testerToken);
        RuntimeException securityException = null;
        try{
            cdmEntityDaoBase.save(cdmBase);
            commitAndStartNewTransaction(null);
            logger.error("Expected failure of evaluation.");
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNotNull("evaluation must fail since the user has no permission", securityException);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#save(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testSaveWithAuthentication() throws Exception {
        setAuthentication(taxonEditorToken);
        RuntimeException securityException = null;
        try {
            cdmEntityDaoBase.save(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException   = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#update(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testUpdate() {
        TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.update(cdmBase);
        commit();
    }

    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testUpdateWithAuthentication() {

        setAuthentication(taxonEditorToken);
        TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        RuntimeException securityException = null;
        try {
        cdmEntityDaoBase.update(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException    = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case saveOrUpdate was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        Assert.assertNull("evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""), securityException);

    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#findById(int)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testFindById() {
        CdmBase cdmBase = cdmEntityDaoBase.findById(1);
        assertNotNull("There should be an entity with an id of 1",cdmBase);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#findByUuid(java.util.UUID)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testFindByUuid() {
        CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        assertNotNull("testFindByUuid() an entity with a uuid of " + uuid.toString(),cdmBase);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#exists(java.util.UUID)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testExists() {
        boolean exists = cdmEntityDaoBase.exists(uuid);
        assertTrue("exists() should return true for uuid " + uuid.toString(), exists);
        boolean existsRandom = cdmEntityDaoBase.exists(UUID.randomUUID());
        assertFalse("exists() should return false for any other uuid", existsRandom);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#list(int, int)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testList() {
        List<TaxonBase> list = cdmEntityDaoBase.list(1000, 0);
        assertNotNull("list() should not return null",list);
        assertEquals("list() should return a list with two entities in it",list.size(),2);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#list(Class, Integer, Integer, List, List)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testRandomOrder() {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new RandomOrder());
        List<TaxonBase> list = cdmEntityDaoBase.list((Class)null, 1000, 0, orderHints, null);
        assertNotNull("list() should not return null", list);
        assertEquals("list() should return a list with two entities in it", list.size(), 2);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#list(java.util.Collection, Integer, Integer, List, List)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testListByUuids() {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new RandomOrder());
        UUID[] uuids = new UUID[]{UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66"), UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46")};
        List<TaxonBase> list = cdmEntityDaoBase.list(Arrays.asList(uuids), 20, 0, orderHints, null);
        assertNotNull("list() should not return null",list);
        assertEquals("list() should return a list with two entities in it",list.size(),2);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#loadList(java.util.Collection, List)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    public void testListByIds() {
        List<OrderHint> orderHints = new ArrayList<OrderHint>();
        orderHints.add(new RandomOrder());
        Integer[] ids = new Integer[]{1, 2};
        List<TaxonBase> list = cdmEntityDaoBase.loadList(Arrays.asList(ids), null);
        assertNotNull("list() should not return null",list);
        assertEquals("list() should return a list with two entities in it",list.size(),2);
    }

    /**
     * Test method for {@link eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmEntityDaoBase#delete(eu.etaxonomy.cdm.model.common.CdmBase)}.
     */
    @Test
    @DataSet("CdmEntityDaoBaseTest.xml")
    @ExpectedDataSet
    public void testDelete() {
        TaxonBase<?> cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        assertNotNull(cdmBase);
        cdmEntityDaoBase.delete(cdmBase);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.test.integration.CdmIntegrationTest#createTestData()
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // TODO Auto-generated method stub

    }
}
