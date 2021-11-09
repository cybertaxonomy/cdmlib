/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.kohlbecker
 * @since Nov 3, 2021
 */
@Component
public class PasswordResetTokenStore implements IPasswordResetTokenStore {

    public  static final int TOKEN_LENGTH = 50;

    private static Logger logger = Logger.getLogger(PasswordResetTokenStore.class);

    private Map<String, PasswordResetRequest> tokenList = new HashMap<>();

    private Integer tokenLifetimeMinutes = null;

    @Override
    public PasswordResetRequest create(User user) {
        clearExpiredTokens();
        assert user != null;
        assert !user.getEmailAddress().isEmpty();
        PasswordResetRequest token = new PasswordResetRequest(user.getUsername(), user.getEmailAddress(), generateRandomToken(), getTokenLifetimeMinutes());
        tokenList.put(token.getToken(), token);
        return token;
    }

    private String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);
        return token;
    }

    @Override
    public Optional<PasswordResetRequest> findResetRequest(String token) {
        clearExpiredTokens();
        PasswordResetRequest resetRequest = tokenList.get(token);
        if(isEligibleResetRequest(resetRequest)) {
            return Optional.of(resetRequest);
        }
        return Optional.empty();
    }

    @Override
    public boolean isEligibleToken(String token) {
        clearExpiredTokens();
        PasswordResetRequest resetRequest = tokenList.get(token);
        return isEligibleResetRequest(resetRequest);
    }

    private boolean isEligibleResetRequest(PasswordResetRequest resetRequest) {
        if(resetRequest == null) {
            logger.error("PasswordResetRequest must not be null");
            return false;
        }
        if(resetRequest.getExpiryDate().before(new Date())) {
            tokenList.remove(resetRequest.getToken());
            logger.info("Token is expired, and has been deleted now.");
            return false;
        }
        return true;
    }

    /**
     * To be called periodically to remove expired tokens.
     *
     * @return the number of expired tokens that have been cleared
     */
    private int clearExpiredTokens() {
        Date now = new Date();
        List<String> expiredTokens = tokenList.values().stream().filter(t -> t.getExpiryDate().before(now)).map(t -> t.getToken()).collect(Collectors.toList());
        expiredTokens.stream().forEach(tstr -> tokenList.remove(tstr));
        return expiredTokens.size();
    }

    /**
     * Removes the token from the store
     *
     * @param token
     * @return true if the token to be remove has existed, otherwise false
     */
    @Override
    public boolean remove(String token) {
        clearExpiredTokens();
        return tokenList.remove(token) != null;
    }

    public int getTokenLifetimeMinutes() {
        return this.tokenLifetimeMinutes != null ? this.tokenLifetimeMinutes : TOKEN_LIFETIME_MINUTES_DEFAULT;
    }

    @Override
    public void setTokenLifetimeMinutes(int tokenLifetimeMinutes) {
        this.tokenLifetimeMinutes = tokenLifetimeMinutes;
    }

}
