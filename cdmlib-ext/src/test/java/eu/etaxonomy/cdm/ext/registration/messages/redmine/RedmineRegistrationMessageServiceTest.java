/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages.redmine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByType;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

import eu.etaxonomy.cdm.SpringProxyBeanHelper;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.api.service.IRegistrationService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IUserService;
import eu.etaxonomy.cdm.ext.common.ExternalServiceException;
import eu.etaxonomy.cdm.ext.registration.messages.IRegistrationMessageService;
import eu.etaxonomy.cdm.ext.registration.messages.Message;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.Registration;
import eu.etaxonomy.cdm.model.name.RegistrationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.test.integration.CdmTransactionalIntegrationTest;
import unitils.AlternativeUnitilsJUnit4TestClassRunner;

/**
 * @author a.kohlbecker
 * @since Feb 16, 2018
 *
 */
@RunWith(AlternativeUnitilsJUnit4TestClassRunner.class)
public class RedmineRegistrationMessageServiceTest extends CdmTransactionalIntegrationTest {

    private static final String CURATOR = "curator";

    private static final String SUBMITTER = "submitter";

    private static final String EMAIL_DOMAIN = "@localhost.bgbm.fu-berlin.de";


    private static final int REGISTRATION_ID = 5000;

    @SpringBeanByType
    IUserService userService;

    @SpringBeanByType
    ITermService termService;

    @SpringBeanByType
    IReferenceService referenceService;

    @SpringBeanByType
    IRegistrationService registrationService;

    @SpringBeanByType
    INameService nameService;

    private RedmineRegistrationMessageService messageService;

    @SpringBeanByType
    public void setIRegistrationMessageService(IRegistrationMessageService messageService) throws Exception {

        this.messageService = SpringProxyBeanHelper.getTargetObject(messageService, RedmineRegistrationMessageService.class);

    }


    private final String[] includeTableNames_create = new String[]{
            "TAXONNAME", "REFERENCE", "AGENTBASE", "HOMOTYPICALGROUP", "REGISTRATION",
            "USERACCOUNT",
            "HIBERNATE_SEQUENCES"};

    @Before
    public void cleanupRedmine() throws RedmineException{

        nameService.getSession().clear();

        for(com.taskadapter.redmineapi.bean.User user: messageService.redmineManager().getUserManager().getUsers()){
            if(user.getLogin().equals(SUBMITTER) || user.getLogin().equals(CURATOR)){
                messageService.redmineManager().getUserManager().deleteUser(user.getId());
            }
        }
        Map<String,String> params = new HashMap<>();
        params.put("offset", "0");
        params.put("limit", "100");
        params.put("status_id", "*"); // * to get open and closed issues
       while(true){
           ResultsWrapper<Issue> result = messageService.redmineManager().getIssueManager().getIssues(params);
           if(result.getResultsNumber() == 0){
               break;
           }
           for(Issue issue : result.getResults()){
               messageService.redmineManager().getIssueManager().deleteIssue(issue.getId());
           }
       }
    }

    @Test
    @DataSet
    public void testCreateUser() throws RedmineException, ExternalServiceException{

        User submitter = (User) userService.loadUserByUsername(SUBMITTER);

        com.taskadapter.redmineapi.bean.User createdUser = null;

        createdUser = messageService.createUser(submitter);

        assertNotNull(createdUser);
        assertEquals(submitter.getUsername(), createdUser.getLogin());
        assertNotNull(createdUser.getId());

        User submitterReloaded = userService.load(submitter.getUuid(), Arrays.asList("person.extensions.$"));
        assertEquals(1, submitterReloaded.getPerson().getExtensions().size());
        Extension extension = submitterReloaded.getPerson().getExtensions().iterator().next();
        assertEquals(RedmineRegistrationMessageService.EXTTYPE_REGMESG_REDMINEUID_UUID, extension.getType().getUuid());
        assertEquals(createdUser.getId(), Integer.valueOf(extension.getValue()));

    }

    /**
     * For this to work the workflow needs to be
     * configured https://dev.e-taxonomy.eu/redmine/attachments/download/1177/picture68-1.png
     *
     * @throws RedmineException
     * @throws ExternalServiceException
     */
    @Test
    @DataSet
    public void testUpdateRegistrationStatus() throws RedmineException, ExternalServiceException{

        Issue issue = null;

        User submitter = (User) userService.loadUserByUsername(SUBMITTER);

        User curator = (User) userService.loadUserByUsername(CURATOR);

        if(!(submitter.getEmailAddress().equals(SUBMITTER + EMAIL_DOMAIN) &&  curator.getEmailAddress().equals(CURATOR + EMAIL_DOMAIN ))){
            throw new AssertionError("Email adresses must be '" + SUBMITTER + EMAIL_DOMAIN + "' and '" + CURATOR + EMAIL_DOMAIN + "',"
                    + " for futher information please refer to https://dev.e-taxonomy.eu/redmine/issues/7280");
        }


        Registration reg = registrationService.load(REGISTRATION_ID, Arrays.asList("$"));
        issue = messageService.createIssue(reg);
        reg.setStatus(RegistrationStatus.READY);
        messageService.updateIssueStatus(reg);
        issue = messageService.findIssue(reg, false);
        assertEquals("ready", issue.getStatusName());


    }

    @Test
    @DataSet
    public void testMessageWorkflow() throws RedmineException, ExternalServiceException{

        User submitter = (User) userService.loadUserByUsername(SUBMITTER);

        User curator = (User) userService.loadUserByUsername(CURATOR);

        Registration reg = registrationService.load(REGISTRATION_ID, Arrays.asList("$"));

        assertNotNull(reg.getIdentifier());
        assertNotNull(reg.getSubmitter());

        Issue issue = null;
        com.taskadapter.redmineapi.bean.User redmineCurator = null;
        com.taskadapter.redmineapi.bean.User redmineSubmitter = null;


        assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
        assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

        // post a message, this will create an issue and will add a comment
        String messageText1 = "hey submitter how is life in a test environment?";
        messageService.postMessage(reg, messageText1, curator, submitter);
        issue = messageService.findIssue(reg, false);
        assertTrue(issue.isPrivateIssue());
        redmineCurator = messageService.findUser(curator);
        redmineSubmitter = messageService.findUser(submitter);
        assertEquals(redmineSubmitter.getId(), issue.getAssigneeId());
        assertEquals(redmineCurator.getId(), Integer.valueOf(issue.getCustomFieldByName("Curator").getValue()));

        assertEquals(1, messageService.countActiveMessagesFor(reg, submitter));

        // 2. post a message back to curator
        String messageText2 = "pretty boring in here. It is horrible. If you know a way out of here, please help!";
        messageService.postMessage(reg, messageText2, submitter, curator);
        issue = messageService.findIssue(reg, false);
        assertEquals(redmineCurator.getId(), issue.getAssigneeId());
        assertEquals(1, messageService.countActiveMessagesFor(reg, curator));
        assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

        // 3. post a message back submitter
        String messageText3 = "Dear Submitter, the only solution it to end this test, hold on, just a millisec ...";
        messageService.postMessage(reg, messageText3, curator, submitter);
        issue = messageService.findIssue(reg, false);
        assertEquals(redmineSubmitter.getId(), issue.getAssigneeId());
        assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
        assertEquals(2, messageService.countActiveMessagesFor(reg, submitter));

        // 4. inactivate messages
        messageService.inactivateMessages(reg);
        issue = messageService.findIssue(reg, false);
        assertNull(issue.getAssigneeName());
        assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
        assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

        List<Message> messages = messageService.listMessages(reg);
        assertEquals(3, messages.size());

        // 5. registration becomes rejected
        reg.setStatus(RegistrationStatus.REJECTED);
        messageService.updateIssueStatus(reg);
        issue = messageService.findIssue(reg, false);
        assertEquals("rejected", issue.getStatusName());

        // 6. check all the messages in this issue
        assertEquals(messageText1, messages.get(0).getText());
        assertEquals(curator, messages.get(0).getFrom());

        assertEquals(messageText2, messages.get(1).getText());
        assertEquals(submitter, messages.get(1).getFrom());

        assertEquals(messageText3, messages.get(2).getText());
        assertEquals(curator, messages.get(2).getFrom());


    }



    /**
     * {@inheritDoc}
     */
    @Override
    //@Test
    public void createTestDataSet() throws FileNotFoundException {

        User submitter = User.NewInstance("Submitter", SUBMITTER, "geheim");
        submitter.setEmailAddress(SUBMITTER + EMAIL_DOMAIN);

        User curator = User.NewInstance("Curator", CURATOR, "geheim");
        curator.setEmailAddress(CURATOR + EMAIL_DOMAIN);

        submitter = userService.save(submitter);
        userService.save(curator);

        Team team = Team.NewTitledInstance("Novis, Braidwood & Kilroy", "Novis, Braidwood & Kilroy");
        Reference nomRef = ReferenceFactory.newArticle();
        nomRef = referenceService.save(nomRef);

        nomRef.setAuthorship(team);
        nomRef.setTitle("P.M. Novis, J. Braidwood & C. Kilroy, Small diatoms (Bacillariophyta) in cultures from the Styx River, New Zealand, including descriptions of three new species in Phytotaxa 64");
        TaxonName name = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(), "Planothidium", null,  "victori", null, null, nomRef, "11-45", null);
        name = nameService.save(name);

        Registration reg = Registration.NewInstance();
        reg.setName(name);
        reg.setSubmitter(submitter);
        reg.setIdentifier("http://phycotest.com/10815");
        reg = registrationService.save(reg);


        //printDataSetWithNull(System.err, includeTableNames_create);

        commit();

        writeDbUnitDataSetFile(includeTableNames_create);

    }

}
