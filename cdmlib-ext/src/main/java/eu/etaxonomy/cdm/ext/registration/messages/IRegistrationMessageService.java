/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages;

import java.util.List;

import eu.etaxonomy.cdm.ext.common.ExternalServiceException;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.Registration;

/**
 * @author a.kohlbecker
 * @since Feb 15, 2018
 *
 */
public interface IRegistrationMessageService {

    void activateMessagesFor(Registration registration, User user) throws ExternalServiceException;

    void inactivateMessages(Registration registration) throws ExternalServiceException;

    void updateIssueStatus(Registration registration) throws ExternalServiceException;

    int countActiveMessagesFor(Registration registration, User user) throws ExternalServiceException;

    List<Message> listActiveMessagesFor(Registration registration, User user) throws ExternalServiceException;

    List<Message> listMessages(Registration registration) throws ExternalServiceException;

    void postMessage(Registration registration, String message, User fromUser, User toUser) throws ExternalServiceException;

}
