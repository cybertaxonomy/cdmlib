/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.RandomOrder;
import eu.etaxonomy.cdm.test.integration.spring.CdmDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmExpectedDataSet;
import eu.etaxonomy.cdm.test.integration.spring.CdmTransactionalSpringIntegrationTestWithSecurity;

/**
 * @author a.mueller
 */
public class CdmEntityDaoBaseTest extends CdmTransactionalSpringIntegrationTestWithSecurity {

    private static final Logger logger = LogManager.getLogger();

    private UUID uuid;
    private TaxonBase cdmBase;

    @Autowired
    private ITaxonDao cdmEntityDaoBase;

    @Autowired
    private AuthenticationManager authenticationManager;

    private TestingAuthenticationToken taxonEditorToken;
    private TestingAuthenticationToken adminToken;
    private TestingAuthenticationToken testerToken;

    @BeforeEach
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

    @Test
    public void testCdmEntityDaoBase() throws Exception {
        assertNotNull(cdmEntityDaoBase, "cdmEntityDaoBase should exist");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testSave2() {
        TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.save(cdmBase);
        commit();
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testSave() throws Exception {
        cdmEntityDaoBase.save(cdmBase);
        commit();
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testSaveWithAuthentication2() {

        setAuthentication(taxonEditorToken);
        TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.save(cdmBase);
        commit();
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testSaveNewObjectWithAuthentication() {
//		printDataSet(System.err, new String[]{"TAXONBASE", "HIBERNATE_SEQUENCES"});
        setAuthentication(taxonEditorToken);
        RuntimeException securityException = null;

        // 1) test create
        try{
            cdmEntityDaoBase.save(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
            fail("Unexpected exception: " + securityException.getMessage());
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }

        // 1) test update
        cdmBase = cdmEntityDaoBase.findByUuid(cdmBase.getUuid());
        cdmBase.setDoubtful(true);
        try{
            cdmEntityDaoBase.save(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", e);
            fail("Unexpected exception: " + securityException.getMessage());
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
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
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNotNull(securityException, "evaluation must fail since the user has no permission");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testSaveWithAuthentication() throws Exception {
        setAuthentication(taxonEditorToken);
        RuntimeException securityException = null;
        try {
            cdmEntityDaoBase.save(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException   = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
            fail();
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testUpdate() {
        TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        cdmEntityDaoBase.update(cdmBase);
        commit();
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testUpdateWithAuthentication() {

        setAuthentication(taxonEditorToken);
        TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        cdmBase.setDoubtful(true);
        RuntimeException securityException = null;
        try {
        cdmEntityDaoBase.update(cdmBase);
            commitAndStartNewTransaction(null);
        } catch (RuntimeException e){
            securityException    = findSecurityRuntimeException(e);
            logger.error("Unexpected failure of evaluation.", securityException);
        } finally {
            // needed in case save was interrupted by the RuntimeException
            // commitAndStartNewTransaction() would raise an UnexpectedRollbackException
            endTransaction();
            startNewTransaction();
        }
        assertNull(securityException, "evaluation must not fail since the user is permitted, CAUSE :" + (securityException != null ? securityException.getMessage() : ""));
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testFindById() {
        CdmBase cdmBase = cdmEntityDaoBase.findById(1);
        assertNotNull(cdmBase, "There should be an entity with an id of 1");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testFindByUuid() {
        CdmBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        assertNotNull(cdmBase, "testFindByUuid() an entity with a uuid of " + uuid.toString());
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testExists() {
        boolean exists = cdmEntityDaoBase.exists(uuid);
        assertTrue(exists, "exists() should return true for uuid " + uuid.toString());
        boolean existsRandom = cdmEntityDaoBase.exists(UUID.randomUUID());
        assertFalse(existsRandom, "exists() should return false for any other uuid");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testList() {
        List<TaxonBase> list = cdmEntityDaoBase.list(1000, 0);
        assertNotNull(list, "list() should not return null");
        assertEquals(list.size(), 2, "list() should return a list with two entities in it");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testRandomOrder() {
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new RandomOrder());
        List<TaxonBase> list = cdmEntityDaoBase.list((Class)null, 1000, 0, orderHints, null);
        assertNotNull(list, "list() should not return null");
        assertEquals(list.size(), 2, "list() should return a list with two entities in it");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testListByUuids() {
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new RandomOrder());
        UUID[] uuids = new UUID[]{UUID.fromString("8d77c380-c76a-11dd-ad8b-0800200c9a66"), UUID.fromString("822d98dc-9ef7-44b7-a870-94573a3bcb46")};
        List<TaxonBase> list = cdmEntityDaoBase.list(Arrays.asList(uuids), 20, 0, orderHints, null);
        assertNotNull(list, "list() should not return null");
        assertEquals(list.size(), 2, "list() should return a list with two entities in it");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    public void testListByIds() {
        List<OrderHint> orderHints = new ArrayList<>();
        orderHints.add(new RandomOrder());
        Integer[] ids = new Integer[]{1, 2};
        List<TaxonBase> list = cdmEntityDaoBase.loadList(Arrays.asList(ids), orderHints, null);
        assertNotNull(list, "list() should not return null");
        assertEquals(2, list.size(), "list() should return a list with two entities in it");
    }

    @Test
    @CdmDataSet("CdmEntityDaoBaseTest.xml")
    @CdmExpectedDataSet
    public void testDelete() {
        TaxonBase cdmBase = cdmEntityDaoBase.findByUuid(uuid);
        assertNotNull(cdmBase);
        cdmEntityDaoBase.delete(cdmBase);
    }

    @Override
    public void createTestDataSet() throws FileNotFoundException {}
}
