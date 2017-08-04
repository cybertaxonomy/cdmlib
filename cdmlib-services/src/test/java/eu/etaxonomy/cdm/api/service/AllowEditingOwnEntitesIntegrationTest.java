/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.io.FileNotFoundException;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.TransactionStatus;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBean;

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.api.conversation.ConversationHolder;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTestWithSecurity;

/**
 * @author a.kohlbecker
 * @since Aug 3, 2017
 *
 */
@DataSet
public class AllowEditingOwnEntitesIntegrationTest extends CdmTransactionalIntegrationTestWithSecurity{

    @SpringBean("cdmRepository")
    private CdmRepository repo;

    @SpringBean("conversationHolder")
    ConversationHolder conversationHolder;

    private Session session;

    private TransactionStatus tx;


    public void startSession(){
        session = repo.getSession();
        tx = repo.startTransaction();
    }

    public void endSession(){
        session.flush();
        repo.commitTransaction(tx);
    }

    @Before
    public void clearSession(){
        repo.getSession().clear();
    }

    @Override
    @After
    public void onTearDown() throws Exception {
        if(conversationHolder.isBound()){
            conversationHolder.unbind();
        }
    }

    public void authenticate(){

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("editor", "sPePhAz6");
        AuthenticationManager authenticationManager = repo.getAuthenticationManager();
        Authentication authentication = authenticationManager.authenticate(token);
    }

    @Test
    @DataSet
    public void longSession_merge_flush(){

       startSession();
       authenticate();
       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       session.merge(article);
       session.flush();
       endSession();
    }

    @Test
    @DataSet
    public void longSession_merge_save_flush(){

       startSession();
       authenticate();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) session.merge(article);
       repo.getReferenceService().save(article);
       session.flush();
       endSession();
    }

    @Test
    @DataSet
    public void longSession_merge_saveOrUpdate_flush(){

       startSession();
       authenticate();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) session.merge(article);
       repo.getReferenceService().saveOrUpdate(article);
       session.flush();
       endSession();
    }

    @Test
    @DataSet
    @Ignore
    public void conversationHolder_merge_flush(){

        authenticate();

        conversationHolder.bind();
       conversationHolder.startTransaction();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) conversationHolder.getSession().merge(article);
       conversationHolder.getSession().flush();
       conversationHolder.commit(true);
    }

    /**
     * fails with org.springframework.security.authentication.BadCredentialsException: Bad credentials
     * during the session.flush() the hibernate
     */
    @Test
    @DataSet
    @Ignore
    public void conversationHolder_merge_save_flush(){

        authenticate();

       conversationHolder.bind();
       conversationHolder.startTransaction();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) conversationHolder.getSession().merge(article);
       repo.getReferenceService().save(article);
       conversationHolder.getSession().flush();
       conversationHolder.commit();
       conversationHolder.unbind();
    }

    @Test
    @DataSet
    @Ignore
    public void conversationHolder_merge_saveOrUpdate_flush(){

       authenticate();

       conversationHolder.bind();
       conversationHolder.startTransaction();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) conversationHolder.getSession().merge(article);
       repo.getReferenceService().saveOrUpdate(article);
       conversationHolder.getSession().flush();
       conversationHolder.commit();
       conversationHolder.unbind();
    }

    @Test
    @DataSet
    @Ignore
    public void conversationHolder_saveOrUpdate_flush(){

       conversationHolder.bind();
       conversationHolder.startTransaction();
       authenticate();

       Reference article = ReferenceFactory.newArticle();
       article.setTitle("Test");
       article = (Reference) conversationHolder.getSession().merge(article);
       repo.getReferenceService().saveOrUpdate(article);
       conversationHolder.getSession().flush();
       conversationHolder.commit();
       conversationHolder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTestDataSet() throws FileNotFoundException {
        // HAND CRAFTED TESTDATA
    }

}
