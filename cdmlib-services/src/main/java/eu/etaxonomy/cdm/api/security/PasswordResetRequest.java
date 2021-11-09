
package eu.etaxonomy.cdm.api.security;

import java.util.Calendar;
import java.util.Date;

/**
 * @author a.kohlbecker
 * @since Oct 26, 2021
 */
public class PasswordResetRequest {

    private String token;

    private String userName;

    private String userEmail;

    private Date expiryDate;

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

    public String getToken() {
        return token;
    }

    public String getUserName() {
        return userName;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    protected void setExpiryDate(int minutes){
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes);
        this.expiryDate = now.getTime();
    }

    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }

    public String getUserEmail() {
        return userEmail;
    }
}