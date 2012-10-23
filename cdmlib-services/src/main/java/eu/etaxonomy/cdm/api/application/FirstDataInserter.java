// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;

/**
 * The <code>FirstDataInserter</code> is responsible for equipping a new and empty database with
 * the initial set of data need by the cdmlib. It operates not only on empty databases,
 * its methods are executed everytime the ApplicationContext has been started up, that is listens
 * for {@link ContextStartedEvent}s.
 * <p>
 * responsibilities:
 * <ul>
 * <li>User 'admin' and role 'ROLE_ADMIN'</li>
 * <li>cdm metadata</li>
 * <ul>
 *
 * @author a.kohlbecker
 * @date Oct 12, 2012
 *
 */
public class FirstDataInserter implements ApplicationListener<ContextRefreshedEvent> {

    public static final Logger logger = Logger.getLogger(FirstDataInserter.class);

    private static final long serialVersionUID = -4738245032655597608L;

    @Autowired
    private ICommonService commonService;

    @Autowired
    private IUserService userService;

    protected PlatformTransactionManager transactionManager;

    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    private IProgressMonitor progressMonitor = null;

    private boolean firstDataInserted = false;

    @Autowired
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public FirstDataInserter() {
        txDefinition.setName("FirstDataInserter.insertFirstData()");
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext() instanceof MonitoredGenericApplicationContext){
            progressMonitor = ((MonitoredGenericApplicationContext)event.getApplicationContext()).getCurrentMonitor();
            /* TODO set up work amount, currently the amount of work ticks is hard coded
             *      in {@link CdmApplicationControllersetNewDataSource}, but we need some means to register
             *      additional ticks.
             *      see http://dev.e-taxonomy.eu/trac/ticket/3140 (generic way to obtain work ticks of application startup for monitoring)
             *
             */
        } else {
            progressMonitor = new NullProgressMonitor();
        }
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
        insertFirstData();
        transactionManager.commit(txStatus);
    }


    private void insertFirstData() {
        // this ApplicationListener may be called multiple times in nested
        // application contexts like in web applications
        if(!firstDataInserted){
            logger.info("inserting first data");
            checkAdminUser();
            checkMetadata();
            firstDataInserted = true;
        } else {
            logger.debug("insertFirstData() already executed before, skipping this time");
        }
    }


    private void checkMetadata() {
        int metaDataCount = commonService.getCdmMetaData().size();
        if (metaDataCount == 0){
            progressMonitor.subTask("Creating Meta Data");
            createMetadata();
        }
    }

    private void checkAdminUser() {
        // the first user ever created is admin and has the id 10
        User admin = userService.find(10);
        if (admin == null){
            progressMonitor.subTask("Creating Admin User");
            admin = createAdminUser();
        }
        checkAdminRole(admin);
        progressMonitor.worked(1);
    }

    private User createAdminUser(){
        User admin = User.NewInstance("admin", "00000");
        userService.save(admin);
        logger.info("user 'admin' created.");
        return admin;
    }

    private void checkAdminRole(User admin) {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();

        authorities = (Set<GrantedAuthority>) admin.getAuthorities();
        boolean hasRoleAdmin = false;
        for(GrantedAuthority grau : authorities){
            if(grau.getAuthority().contentEquals(Role.ROLE_ADMIN.getAuthority())){
                hasRoleAdmin = true;
                break;
            }
        }

        if(!hasRoleAdmin){
            authorities.add(Role.ROLE_ADMIN.asNewGrantedAuthority());
            admin.setGrantedAuthorities(authorities);
            progressMonitor.subTask("Creating Admins Role");
            userService.saveOrUpdate(admin);
            logger.info("Role " + Role.ROLE_ADMIN.getAuthority() + " for user 'admin' created and added");
        }
    }

    private void createMetadata(){
        List<CdmMetaData> metaData = CdmMetaData.defaultMetaData();
        commonService.saveAllMetaData(metaData);
        logger.info("Metadata created.");
    }
}
