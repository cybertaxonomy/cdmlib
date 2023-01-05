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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
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
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.model.permission.CRUD;
import eu.etaxonomy.cdm.model.permission.GrantedAuthorityImpl;
import eu.etaxonomy.cdm.model.permission.Group;
import eu.etaxonomy.cdm.model.permission.PermissionClass;
import eu.etaxonomy.cdm.model.permission.User;
import eu.etaxonomy.cdm.persistence.permission.CdmAuthority;
import eu.etaxonomy.cdm.persistence.permission.Role;
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
 * @author a.kohlbecker
 * @since Oct 12, 2012
 */
//@RunAs("ROLE_ADMIN") // seems to be broken in spring see: https://jira.springsource.org/browse/SEC-1671
public class FirstDataInserter extends AbstractDataInserter {

    private static final Logger logger = LogManager.getLogger();

    private static final EnumSet<CRUD> CREATE_READ = EnumSet.of(CRUD.CREATE, CRUD.READ);
    private static final EnumSet<CRUD> UPDATE_DELETE = EnumSet.of(CRUD.UPDATE, CRUD.DELETE);
    private static final EnumSet<CRUD> CREATE_READ_UPDATE = EnumSet.of(CRUD.CREATE, CRUD.READ, CRUD.UPDATE);
    private static final EnumSet<CRUD> CREATE_READ_UPDATE_DELETE = EnumSet.of(CRUD.CREATE, CRUD.READ, CRUD.UPDATE, CRUD.DELETE);

    public static final GrantedAuthority[] EDITOR_GROUP_AUTHORITIES = new GrantedAuthority[]{
            new CdmAuthority(PermissionClass.REFERENCE, CREATE_READ),
            new CdmAuthority(PermissionClass.TAXONNAME, CREATE_READ_UPDATE),
            new CdmAuthority(PermissionClass.TEAMORPERSONBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.TAXONBASE, CREATE_READ_UPDATE_DELETE),
            new CdmAuthority(PermissionClass.DESCRIPTIONBASE, CREATE_READ_UPDATE_DELETE),
            new CdmAuthority(PermissionClass.DESCRIPTIONELEMENTBASE, CREATE_READ_UPDATE_DELETE),
            new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, CREATE_READ_UPDATE_DELETE),
            new CdmAuthority(PermissionClass.COLLECTION, CREATE_READ_UPDATE_DELETE),
    };

    /**
     * This group will in future replace the group Editor, see issue #7150
     */
    public static final CdmAuthority[] EDITOR_GROUP_EXTENDED_CREATE_GROUP_AUTHORITIES = new CdmAuthority[]{
            new CdmAuthority(PermissionClass.REFERENCE, CREATE_READ),
            new CdmAuthority(PermissionClass.TAXONNAME, CREATE_READ),
            new CdmAuthority(PermissionClass.TEAMORPERSONBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.TAXONBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.DESCRIPTIONBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.DESCRIPTIONELEMENTBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.SPECIMENOROBSERVATIONBASE, CREATE_READ),
            new CdmAuthority(PermissionClass.COLLECTION, CREATE_READ),
    };

    public static final GrantedAuthority[] PROJECT_MANAGER_GROUP_AUTHORITIES = new GrantedAuthority[]{
            new CdmAuthority(PermissionClass.REFERENCE, UPDATE_DELETE),
            new CdmAuthority(PermissionClass.TAXONNAME, EnumSet.of(CRUD.DELETE)),
            new CdmAuthority(PermissionClass.TEAMORPERSONBASE, UPDATE_DELETE),
            Role.ROLE_PROJECT_MANAGER,
    };

    public static final CdmAuthority[] EDITOR_REFERENCE_GROUP_AUTHORITIES = new CdmAuthority[]{
            new CdmAuthority(PermissionClass.REFERENCE, UPDATE_DELETE),
            new CdmAuthority(PermissionClass.TEAMORPERSONBASE, UPDATE_DELETE)
    };

    public static final Role[] PUBLISH_GROUP_ROLES = new Role[]{
            Role.ROLE_PUBLISH
    };

    public static final CdmAuthority[] EDIT_ALL_TAXA_GROUP_AUTHORITIES = new CdmAuthority[]{
            new CdmAuthority(PermissionClass.TAXONNODE, CREATE_READ_UPDATE_DELETE)
    };

    public static final Role[] ADMIN_GROUP_ROLES = new Role[]{
            Role.ROLE_ADMIN
    };

    public static final Role[] USER_MANAGER_ROLES = new Role[]{
            Role.ROLE_USER_MANAGER
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
//    private AuthenticationProvider runAsAuthenticationProvider = null;

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
             *      see https://dev.e-taxonomy.eu/redmine/issues/3140 (generic way to obtain work ticks of application startup for monitoring)
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
            assureRole_REMOTING_forEditors();
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
        checkGroup(Group.GROUP_EDITOR_UUID, Group.GROUP_EDITOR_NAME, EDITOR_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_EDITOR_EXTENDED_CREATE_UUID, Group.GROUP_EDITOR_EXTENDED_CREATE_NAME, EDITOR_GROUP_EXTENDED_CREATE_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_PROJECT_MANAGER_UUID, Group.GROUP_PROJECT_MANAGER_NAME, PROJECT_MANAGER_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_ADMIN_UUID, Group.GROUP_ADMIN_NAME, ADMIN_GROUP_ROLES);
        checkGroup(Group.GROUP_EDITOR_REFERENCE_UUID, Group.GROUP_EDITOR_REFERENCE_NAME, EDITOR_REFERENCE_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_ALLOW_ALL_TAXA_UUID, Group.GROUP_ALLOW_ALL_TAXA_NAME, EDIT_ALL_TAXA_GROUP_AUTHORITIES);
        checkGroup(Group.GROUP_PUBLISH_UUID, Group.GROUP_PUBLISH_NAME, PUBLISH_GROUP_ROLES);
        checkGroup(Group.GROUP_USER_MANAGER_UUID, Group.GROUP_USER_MANAGER_NAME, USER_MANAGER_ROLES);
        progressMonitor.worked(1);
    }

    private void checkGroup(UUID groupUuid, String groupName, GrantedAuthority[] requiredAuthorities) {
        Group group = groupService.load(groupUuid);
        if(group == null){
            group = Group.NewInstance();
            group.setUuid(groupUuid);
            logger.info("New Group '" + groupName + "' created");
        }
        group.setName(groupName); // force default name

        Set<GrantedAuthority> grantedAuthorities = group.getGrantedAuthorities();

        for(GrantedAuthority requiredAuthority : requiredAuthorities){
            boolean isMissing = true;
            for(GrantedAuthority ga : grantedAuthorities){
                if(requiredAuthority.getAuthority().equals(ga.getAuthority())){
                    isMissing = false;
                    break;
                }
            }
            if(isMissing){
                addMissingAuthority(groupName, group, requiredAuthority);
            }
        }

        groupService.saveOrUpdate(group);
        logger.info("Check of group  '" + groupName + "' done");
    }

    private void addMissingAuthority(String groupName, Group group, GrantedAuthority requiredAuthority) {
        //NOTE: we still have to do this by string until UUIDs are fixed
        GrantedAuthorityImpl newGa = grantedAuthorityService.findAuthorityString(requiredAuthority.getAuthority());

        if (newGa == null){
            newGa = GrantedAuthorityImpl.NewInstance(requiredAuthority.toString());
            if (requiredAuthority instanceof Role){
                newGa.setUuid(((Role)requiredAuthority).getUuid());
            }
        }

        group.addGrantedAuthority(newGa);
        logger.info("New GrantedAuthority '" + requiredAuthority + "' added  to '" + groupName + "'");
    }

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

    /**
     * Assures the {@link Role#ROLE_REMOTING} exists.
     * <p>
     * If the role is missing in the db it will be created and added to the Groups <code>Editor</code> and <code>EditorExtendedCreate</code>.
     * <p>
     * The role will however not be added to the editor groups in case the role exist but is missing from one of these groups. This allows removal
     * of the role from the editor groups to withdraw the remote editing permission from editors in general for a project.
     * <p>
     * see https://dev.e-taxonomy.eu/redmine/issues/7972
     */
    private void assureRole_REMOTING_forEditors(){

        if(!roleExists(Role.ROLE_REMOTING)){
            GrantedAuthorityImpl roleRemoting = assureRole(Role.ROLE_REMOTING);
            Group groupEditor = groupService.load(Group.GROUP_EDITOR_UUID);
            groupEditor.addGrantedAuthority(roleRemoting);
            groupService.saveOrUpdate(groupEditor);
            Group groupEditorExtendedCreate = groupService.load(Group.GROUP_EDITOR_EXTENDED_CREATE_UUID);
            groupEditorExtendedCreate.addGrantedAuthority(roleRemoting);
            groupService.saveOrUpdate(groupEditorExtendedCreate);
        }
    }

    private void checkAdminRole(User admin) {

        Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) admin.getAuthorities();

        boolean hasRoleAdmin = false;
        for(GrantedAuthority grau : authorities){
            if(grau.getAuthority().contentEquals(Role.ROLE_ADMIN.getAuthority())){
                hasRoleAdmin = true;
                break;
            }
        }

        if(!hasRoleAdmin){
            authorities.add(assureRole(Role.ROLE_ADMIN));
            admin.setGrantedAuthorities(authorities);
            progressMonitor.subTask("Creating Admins Role");
            userService.saveOrUpdate(admin);
            logger.info("Role " + Role.ROLE_ADMIN.getAuthority() + " for user '" + Configuration.adminLogin + "' created and added");
        }
    }

    private GrantedAuthorityImpl assureRole(Role role) {
        GrantedAuthorityImpl roleLoaded = grantedAuthorityService.find(role.getUuid());
        if(roleLoaded == null){
            roleLoaded = grantedAuthorityService.save(role.asNewGrantedAuthority());
        }
        return roleLoaded;
    }

    private boolean roleExists(Role role) {
        GrantedAuthorityImpl roleLoaded = grantedAuthorityService.find(role.getUuid());
        return roleLoaded != null;
    }

    private void createMetadata(){
        List<CdmMetaData> metaData = CdmMetaData.defaultMetaData();
        commonService.saveAllMetaData(metaData);
        logger.info("Metadata created.");
    }
}