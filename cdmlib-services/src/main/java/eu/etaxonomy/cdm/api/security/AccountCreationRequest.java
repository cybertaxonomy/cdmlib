
package eu.etaxonomy.cdm.api.security;



/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
public class AccountCreationRequest extends AbstractRequestToken {

    private String userName;

    private String userEmail;

    private String encryptedPassword;

    protected AccountCreationRequest(String userName, String encryptedPassword, String userEmail, String token, int expireInMinutes) {
        super();
        this.userName = userName;
        this.userEmail = userEmail;
        this.encryptedPassword = encryptedPassword;
        this.token = token;
        this.setExpiryDate(expireInMinutes);
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }
}