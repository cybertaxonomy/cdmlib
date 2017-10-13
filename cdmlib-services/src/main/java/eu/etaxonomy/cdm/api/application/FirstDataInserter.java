/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.application;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
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
import eu.etaxonomy.cdm.persistence.hibernate.permission.CRUD;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.hibernate.permission.CdmPermissionClass;
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
public class FirstDataInserter extends AbstractDataInserter {

    /**
     *
     */
    private static final EnumSet<CRUD> CREATE_READ = EnumSet.of(CRUD.CREATE, CRUD.READ);
    private static final EnumSet<CRUD> UPDATE_DELETE = EnumSet.of(CRUD.UPDATE, CRUD.DELETE);
    private static final EnumSet<CRUD> CREATE_READ_UPDATE = EnumSet.of(CRUD.CREATE, CRUD.READ, CRUD.UPDATE);
    private static final EnumSet<CRUD> CREATE_READ_UPDATE_DELETE = EnumSet.of(CRUD.CREATE, CRUD.READ, CRUD.UPDATE, CRUD.DELETE);

    public static final Logger logger = Logger.getLogger(FirstDataInserter.class);

    public static final String[] EDITOR_GROUP_AUTHORITIES = new String[]{
            new CdmAuthority(CdmPermissionClass.REFERENCE, CREATE_READ).toString(),
            new CdmAuthority(CdmPermissionClass.TAXONNAME, CREATE_READ_UPDATE).toString(),
            new CdmAuthority(CdmPermissionClass.TEAMORPERSONBASE, CREATE_READ).toString(),
            new CdmAuthority(CdmPermissionClass.TAXONBASE, CREATE_READ_UPDATE_DELETE).toString(),
            new CdmAuthority(CdmPermissionClass.DESCRIPTIONBASE, CREATE_READ_UPDATE_DELETE).toString(),
            new CdmAuthority(CdmPermissionClass.DESCRIPTIONELEMENTBASE, CREATE_READ_UPDATE_DELETE).toString(),
            new CdmAuthority(CdmPermissionClass.TYPEDESIGNATIONBASE, CREATE_READ_UPDATE_DELETE).toString(),
    };

    public static final String[] PROJECT_MANAGER_GROUP_AUTHORITIES = new String[]{
            new CdmAuthority(CdmPermissionClass.REFERENCE, UPDATE_DELETE).toString(),
            new CdmAuthority(CdmPermissionClass.TAXONNAME, EnumSet.of(CRUD.DELETE)).toString(),
            new CdmAuthority(CdmPermissionClass.TEAMORPERSONBASE, UPDATE_DELETE).toString(),
            Role.ROLE_PROJECT_MANAGER.toString(),
    };

    public static final String[] ADMIN_GROUP_AUTHORITIES = new String[]{
            Role.ROLE_ADMIN.toString()
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

        insertFirstData();
    }


    private void insertFirstData() {

        // this ApplicationListener may be called multiple times in nested
        // application contexts like in web applications
        if(!firstDataInserted){

            runAsAuthentication(Role.ROLE_ADMIN);

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
        checkGroup(Group.GROUP_EDITOR_UUID, "Editor", EDITOR_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_PROJECT_MANAGER_UUID, "ProjectManager", PROJECT_MANAGER_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_ADMIN_UUID, "Admin", ADMIN_GROUP_AUTHORITIES);
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
                    newGa = GrantedAuthorityImpl.NewInstance(a);
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

}
