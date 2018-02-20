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

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;

import eu.etaxonomy.cdm.api.application.CdmRepository;
import eu.etaxonomy.cdm.ext.common.ExternalServiceException;
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

    private static final String EMAIL_DOMAIN = "@edit-test.bgbm.fu-berlin.de";


    private static final int REGISTRATION_ID = 5000;

    @SpringBeanByName
    private CdmRepository cdmRepository;

    @SpringBeanByType
    private RedmineRegistrationMessageService messageService;


    private final String[] includeTableNames_create = new String[]{
            "TAXONNAME", "REFERENCE", "AGENTBASE", "HOMOTYPICALGROUP", "REGISTRATION",
            "USERACCOUNT",
            "HIBERNATE_SEQUENCES"};


    @Test
    @DataSet
    public void testCreateUser() throws RedmineException, ExternalServiceException{

        User submitter = (User) cdmRepository.getUserService().loadUserByUsername(SUBMITTER);

        com.taskadapter.redmineapi.bean.User createdUser = null;
        try {
            createdUser = messageService.createUser(submitter);

            assertNotNull(createdUser);
            assertEquals(submitter.getUsername(), createdUser.getLogin());
            assertNotNull(createdUser.getId());

            User submitterReloaded = cdmRepository.getUserService().load(submitter.getUuid(), Arrays.asList("person.extensions.$"));
            assertEquals(1, submitterReloaded.getPerson().getExtensions().size());
            Extension extension = submitterReloaded.getPerson().getExtensions().iterator().next();
            assertEquals(RedmineRegistrationMessageService.EXTTYPE_REGMESG_REDMINEUID_UUID, extension.getType().getUuid());
            assertEquals(createdUser.getId(), Integer.valueOf(extension.getValue()));

        } finally {
            if (createdUser != null) {
                messageService.redmineManager().getUserManager().deleteUser(createdUser.getId());
            }
            rollback();
        }
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

        User submitter = (User) cdmRepository.getUserService().loadUserByUsername(SUBMITTER);

        User curator = (User) cdmRepository.getUserService().loadUserByUsername(CURATOR);
        if(!(submitter.getEmailAddress().equals("submitter@localhost") &&  curator.getEmailAddress().equals("curator@localhost"))){
            throw new AssertionError("Email adresses must be '" + SUBMITTER + EMAIL_DOMAIN + "' and '" + CURATOR + EMAIL_DOMAIN + "',"
                    + " for futher information please refer to https://dev.e-taxonomy.eu/redmine/issues/7280");
        }

        try {

            Registration reg = cdmRepository.getRegistrationService().load(REGISTRATION_ID, Arrays.asList("$"));
            issue = messageService.createIssue(reg);
            reg.setStatus(RegistrationStatus.READY);
            messageService.updateIssueStatus(reg);
            issue = messageService.findIssue(reg);
            assertEquals("ready", issue.getStatusName());
        } finally {
            if (issue != null) {
                try {
                    messageService.redmineManager().getIssueManager().deleteIssue(issue.getId());
                } catch (Exception e) { /* IGNORE*/ }
            }
            try {
                com.taskadapter.redmineapi.bean.User redmineSubmitter = messageService.findUser(submitter);
                messageService.redmineManager().getUserManager().deleteUser(redmineSubmitter.getId());

            } catch (Exception e) { /* IGNORE*/ }
        }

    }

    @Test
    @DataSet
    public void testMessageWorkflow() throws RedmineException, ExternalServiceException{

        User submitter = (User) cdmRepository.getUserService().loadUserByUsername(SUBMITTER);

        User curator = (User) cdmRepository.getUserService().loadUserByUsername(CURATOR);

        Registration reg = cdmRepository.getRegistrationService().load(REGISTRATION_ID, Arrays.asList("$"));

        assertNotNull(reg.getIdentifier());
        assertNotNull(reg.getSubmitter());

        Issue issue = null;
        com.taskadapter.redmineapi.bean.User redmineCurator = null;
        com.taskadapter.redmineapi.bean.User redmineSubmitter = null;

        try {
            assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
            assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

            // post a message, this will create an issue and will add a comment
            String messageText1 = "hey submitter how is life in a test environment?";
            messageService.postMessage(reg, messageText1, curator, submitter);
            issue = messageService.findIssue(reg);
            redmineCurator = messageService.findUser(curator);
            redmineSubmitter = messageService.findUser(submitter);
            assertEquals(redmineSubmitter.getId(), issue.getAssigneeId());

            assertEquals(1, messageService.countActiveMessagesFor(reg, submitter));

            // 2. post a message back to curator
            String messageText2 = "pretty boring in here. It is horrible. If you know a way out of here, please help!";
            messageService.postMessage(reg, messageText2, submitter, curator);
            issue = messageService.findIssue(reg);
            assertEquals(redmineCurator.getId(), issue.getAssigneeId());
            assertEquals(1, messageService.countActiveMessagesFor(reg, curator));
            assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

            // 3. post a message back submitter
            String messageText3 = "Dear Submitter, the only solution it to end this test, hold on, just a millisec ...";
            messageService.postMessage(reg, messageText3, curator, submitter);
            issue = messageService.findIssue(reg);
            assertEquals(redmineSubmitter.getId(), issue.getAssigneeId());
            assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
            assertEquals(2, messageService.countActiveMessagesFor(reg, submitter));

            // 4. inactivate messages
            messageService.inactivateMessages(reg);
            issue = messageService.findIssue(reg);
            assertNull(issue.getAssigneeName());
            assertEquals(0, messageService.countActiveMessagesFor(reg, curator));
            assertEquals(0, messageService.countActiveMessagesFor(reg, submitter));

            List<Message> messages = messageService.listMessages(reg);
            assertEquals(3, messages.size());

            // 5. registration becomes rejected
            reg.setStatus(RegistrationStatus.REJECTED);
            messageService.updateIssueStatus(reg);
            issue = messageService.findIssue(reg);
            assertEquals("rejected", issue.getStatusName());

            // 6. check all the messages in this issue
            assertEquals(messageText1, messages.get(0).getText());
            assertEquals(curator, messages.get(0).getFrom());

            assertEquals(messageText2, messages.get(1).getText());
            assertEquals(submitter, messages.get(1).getFrom());

            assertEquals(messageText3, messages.get(2).getText());
            assertEquals(curator, messages.get(2).getFrom());

        } finally {
            try {
                if (issue != null) {
                    messageService.redmineManager().getIssueManager().deleteIssue(issue.getId());
                }
                if(redmineCurator != null){
                    messageService.redmineManager().getUserManager().deleteUser(redmineCurator.getId());
                } else {
                    redmineCurator = messageService.findUser(curator);
                    messageService.redmineManager().getUserManager().deleteUser(redmineCurator.getId());
                }
                if(redmineSubmitter != null){
                    messageService.redmineManager().getUserManager().deleteUser(redmineSubmitter.getId());
                } else {
                    redmineSubmitter = messageService.findUser(submitter);
                    messageService.redmineManager().getUserManager().deleteUser(redmineSubmitter.getId());
                }
            } catch (Exception e) { /* IGNORE*/ }
        }
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

        submitter = cdmRepository.getUserService().save(submitter);
        cdmRepository.getUserService().save(curator);

        Team team = Team.NewTitledInstance("Novis, Braidwood & Kilroy", "Novis, Braidwood & Kilroy");
        Reference nomRef = ReferenceFactory.newArticle();
        nomRef = cdmRepository.getReferenceService().save(nomRef);

        nomRef.setAuthorship(team);
        nomRef.setTitle("P.M. Novis, J. Braidwood & C. Kilroy, Small diatoms (Bacillariophyta) in cultures from the Styx River, New Zealand, including descriptions of three new species in Phytotaxa 64");
        TaxonName name = TaxonName.NewInstance(NomenclaturalCode.ICNAFP, Rank.SPECIES(), "Planothidium", null,  "victori", null, null, nomRef, "11-45", null);
        name = cdmRepository.getNameService().save(name);

        Registration reg = Registration.NewInstance();
        reg.setName(name);
        reg.setSubmitter(submitter);
        reg.setIdentifier("http://phycotest.com/10815");
        reg = cdmRepository.getRegistrationService().save(reg);


        //printDataSetWithNull(System.err, includeTableNames_create);

        commit();

        writeDbUnitDataSetFile(includeTableNames_create);

    }

}
