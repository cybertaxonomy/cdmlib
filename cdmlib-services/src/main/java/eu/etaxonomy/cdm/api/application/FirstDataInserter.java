/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.IGrantedAuthorityService;
import eu.etaxonomy.cdm.api.service.IGroupService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.common.monitor.NullProgressMonitor;
import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.model.common.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.common.Group;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.persistence.hibernate.permission.Role;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

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
 * <p>
 * The <code>runAsAuthenticationProvider</code> must be set in a security application context, eg:
 * {@code
    <bean id="firstDataInserter" class="eu.etaxonomy.cdm.api.application.FirstDataInserter">
        <property name="runAsAuthenticationProvider" ref="runAsAuthenticationProvider"/>
    </bean>
    }
 *
 *
 *
 * @author a.kohlbecker
 * @date Oct 12, 2012
 *
 */
//@RunAs("ROLE_ADMIN") // seems to be broken in spring see: https://jira.springsource.org/browse/SEC-1671
public class FirstDataInserter implements ApplicationListener<ContextRefreshedEvent> {

    public static final Logger logger = Logger.getLogger(FirstDataInserter.class);

    /**
     * must match the key in eu/etaxonomy/cdm/services_security.xml
     */
    private static final String RUN_AS_KEY = "TtlCx3pgKC4l";

    public static final String[] editorGroupAuthorities = new String[]{
            "REFERENCE.[CREATE,READ]",
            "TAXONNAMEBASE.[CREATE,READ,UPDATE]",
            "TEAMORPERSONBASE.[CREATE,READ]",
            "TAXONBASE.[CREATE,UPDATE,DELETE,READ]",
            "DESCRIPTIONBASE.[CREATE,UPDATE,DELETE,READ]",
            "DESCRIPTIONELEMENTBASE.[CREATE,UPDATE,DELETE,READ]",
    };

    public static final String[] projectManagerGroupAuthorities = new String[]{
            "REFERENCE.[UPDATE,DELETE]",
            "TAXONNAMEBASE.[DELETE]",
            "TEAMORPERSONBASE.[UPDATE,DELETE]",
            Role.ROLE_PROJECT_MANAGER.toString(),
    };

    @Autowired
    private ICommonService commonService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IGroupService groupService;


    @Autowired
    private IGrantedAuthorityService grantedAuthorityService;

    // not to be autowired, since the FirstdataInserter must be usable without security
    private AuthenticationProvider runAsAuthenticationProvider = null;

    protected PlatformTransactionManager transactionManager;

    protected DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();

    private IProgressMonitor progressMonitor = null;

    private boolean firstDataInserted = false;

    private Authentication authentication;

    private ApplicationContext applicationContext;

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
        applicationContext = event.getApplicationContext();

        insertFirstData();
    }


    private void insertFirstData() {

        // this ApplicationListener may be called multiple times in nested
        // application contexts like in web applications
        if(!firstDataInserted){

            runAsAuthentication();

            TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

            logger.info("inserting first data");
            checkAdminUser();
            checkDefaultGroups();
            checkMetadata();
            firstDataInserted = true;

            transactionManager.commit(txStatus);

            restoreAuthentication();

        } else {
            logger.debug("insertFirstData() already executed before, skipping this time");
        }
    }

    /**
     * needed to work around the broken @RunAs("ROLE_ADMIN") which
     * seems to be broken in spring see: https://jira.springsource.org/browse/SEC-1671
     */
    private void restoreAuthentication() {
        if(runAsAuthenticationProvider == null){
            logger.debug("no RunAsAuthenticationProvider set, thus nothing to restore");
        }
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);
        logger.debug("last authentication restored: " + (authentication != null ? authentication : "NULL"));
    }

    /**
     *
     * needed to work around the broken @RunAs("ROLE_ADMIN") which seems to be
     * broken in spring see: https://jira.springsource.org/browse/SEC-1671
     */
    private void runAsAuthentication() {
        if(runAsAuthenticationProvider == null){
            logger.debug("no RunAsAuthenticationProvider set, skipping run-as authentication");
            return;
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        authentication = securityContext.getAuthentication();


        Collection<GrantedAuthority> rules = new ArrayList<GrantedAuthority>();
        rules.add(Role.ROLE_ADMIN);
        RunAsUserToken adminToken = new RunAsUserToken(
                RUN_AS_KEY,
                "system-admin",
                null,
                rules,
                (authentication != null ? authentication.getClass() : AnonymousAuthenticationToken.class));

        Authentication runAsAuthentication = runAsAuthenticationProvider.authenticate(adminToken);
        SecurityContextHolder.getContext().setAuthentication(runAsAuthentication);

        logger.debug("switched to run-as authentication: " + runAsAuthentication);
    }


    private void checkMetadata() {
        int metaDataCount = commonService.getCdmMetaData().size();
        if (metaDataCount == 0){
            progressMonitor.subTask("Creating Meta Data");
            createMetadata();
        }
    }

    private void checkAdminUser() {
        User admin = findFirstUser();

        if (admin == null){
            progressMonitor.subTask("Creating Admin User");
            admin = createAdminUser();
        } else {
            logger.info("Assuming first user '" + admin + "' is admin.");
        }

        checkAdminRole(admin);
        progressMonitor.worked(1);
    }

    private void checkDefaultGroups(){

        progressMonitor.subTask("Checking default groups");
        checkGroup(Group.groupEditorUuid, "Editor", editorGroupAuthorities);
        checkGroup(Group.groupProjectManagerUuid, "ProjectManager", projectManagerGroupAuthorities);
        progressMonitor.worked(1);
    }

    /**
     * @param newGroups
     * @param groupName
     * @param requiredAuthorities
     */
    private void checkGroup(UUID groupUuid, String groupName, String[] requiredAuthorities) {
        Group group = groupService.load(groupUuid);
        if(group == null){
            group = Group.NewInstance();
            group.setUuid(groupUuid);
            logger.info("New Group '" + groupName + "' created");
        }
        group.setName(groupName); // force name

        Set<GrantedAuthority> grantedAuthorities = group.getGrantedAuthorities();

        for(String a : requiredAuthorities){
            boolean isMissing = true;
            for(GrantedAuthority ga : grantedAuthorities){
                if(a.equals(ga.getAuthority())){
                    isMissing = false;
                    break;
                }
            }
            if(isMissing){
                GrantedAuthorityImpl newGa = grantedAuthorityService.findAuthorityString(a);

                if (newGa == null){
                    newGa = GrantedAuthorityImpl.NewInstance();
                    newGa.setAuthority(a);
                }

                group.addGrantedAuthority(newGa);
                logger.info("New GrantedAuthority '" + a + "' added  to '" + groupName + "'");
            }
        }

        groupService.merge(group, true);
        logger.info("Check of group  '" + groupName + "' done");
    }

    /**
     * @return
     */
    private User findFirstUser() {
        User firstUser = null;
        List<User> users = userService.list(null, 1, null, Arrays.asList(new OrderHint[]{new OrderHint("id", OrderHint.SortOrder.ASCENDING)}), null);
        if(users.size() > 0){
            firstUser = users.get(0);
        }
        return firstUser;
    }

    private User createAdminUser(){

        User admin = User.NewInstance(Configuration.adminLogin, Configuration.adminPassword);
        userService.save(admin);
        logger.info("user '" + Configuration.adminLogin + "' created.");
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
            authorities.add(getRoleAdmin());
            admin.setGrantedAuthorities(authorities);
            progressMonitor.subTask("Creating Admins Role");
            userService.saveOrUpdate(admin);
            logger.info("Role " + Role.ROLE_ADMIN.getAuthority() + " for user '" + Configuration.adminLogin + "' created and added");
        }
    }

    /**
     * @return
     */
    private GrantedAuthorityImpl getRoleAdmin() {
        GrantedAuthorityImpl role_admin = grantedAuthorityService.find(Role.ROLE_ADMIN.getUuid());
        if(role_admin == null){
            role_admin = Role.ROLE_ADMIN.asNewGrantedAuthority();
        }
        return role_admin;
    }

    private void createMetadata(){
        List<CdmMetaData> metaData = CdmMetaData.defaultMetaData();
        commonService.saveAllMetaData(metaData);
        logger.info("Metadata created.");
    }

    /**
     * @return the runAsAuthenticationProvider
     */
    public AuthenticationProvider getRunAsAuthenticationProvider() {
        return runAsAuthenticationProvider;
    }

    /**
     * @param runAsAuthenticationProvider the runAsAuthenticationProvider to set
     */
    public void setRunAsAuthenticationProvider(AuthenticationProvider runAsAuthenticationProvider) {
        this.runAsAuthenticationProvider = runAsAuthenticationProvider;
    }


}
