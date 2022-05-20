/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
public class PasswordResetRequest extends AbstractRequestToken {

    private String userName;

    private String userEmail;

    /**
     * @param user
     * @param expireInMinutes
     */
    protected PasswordResetRequest(String userName, String userEmail, String token, int expireInMinutes) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
        this.token = token;
        this.setExpiryDate(expireInMinutes);
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}