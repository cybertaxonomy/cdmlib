/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

/**
 * @author a.kohlbecker
 * @since Nov 11, 2021
 */
public class UserAccountEmailTemplates {

    public static final String RESET_REQUEST_EMAIL_SUBJECT_TEMPLATE = "Your password reset request for ${userName}";
    public static final String RESET_REQUEST_EMAIL_BODY_TEMPLATE = "You are receiving this email because a password reset was requested for your account at the ${dataBase}"
            + " data base. If this was not initiated by you, please ignore this message."
            + "\n\nPlease click ${linkUrl} to reset your password";

    public static final String RESET_SUCCESS_EMAIL_SUBJECT_TEMPLATE = "Your password for ${userName} has been changed";
    public static final String RESET_SUCCESS_EMAIL_BODY_TEMPLATE = "The password of your account (${userName}) at the ${dataBase} data base has just been changed."
            + "\n\nIf this was not initiated by you, please contact the administrator (${supportEmailAddress}) as soon as possible.";

    public static final String RESET_FAILED_EMAIL_SUBJECT_TEMPLATE = "Changing your password for ${userName} has failed";
    public static final String RESET_FAILED_EMAIL_BODY_TEMPLATE = "The attempt to change the password of your account at the ${dataBase} data base has failed."
            + "\n\nIf this was not initiated by you, please contact the administrator (${supportEmailAddress}) as soon as possible.";

    public static final String REGISTRATION_REQUEST_EMAIL_SUBJECT_TEMPLATE = "Your requested for new user account at ${dataBase}";
    public static final String REGISTRATION_REQUEST_EMAIL_BODY_TEMPLATE = "You are receiving this email because you requested for a new account at the ${dataBase}"
            + " data base. If this was not initiated by you, please ignore this message."
            + "\n\nPlease click ${linkUrl} to start creating your user account.";

    public static final String REGISTRATION_SUCCESS_EMAIL_SUBJECT_TEMPLATE = "The new user account (${userName}) has been changed";
    public static final String REGISTRATION_SUCCESS_EMAIL_BODY_TEMPLATE = "Your account (${userName}) at the ${dataBase} data base has just been created."
            + "\n\nIf this was not initiated by you, please contact the administrator (${supportEmailAddress}).";

}
