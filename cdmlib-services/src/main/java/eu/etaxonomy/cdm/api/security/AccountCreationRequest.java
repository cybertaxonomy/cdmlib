
package eu.etaxonomy.cdm.api.security;



/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
public class AccountCreationRequest extends AbstractRequestToken {

    private String userEmail;


    protected AccountCreationRequest(String userEmail, String token, int expireInMinutes) {
        super();
        this.userEmail = userEmail;
        this.token = token;
        this.setExpiryDate(expireInMinutes);
    }

    public String getUserEmail() {
        return userEmail;
    }

}