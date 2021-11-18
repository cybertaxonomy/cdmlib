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

import eu.etaxonomy.cdm.model.permission.User;

/**
 * @author a.kohlbecker
 * @since Nov 18, 2021
 */
public abstract class AbstractRequestTokenStore<T extends AbstractRequestToken>  implements IPasswordResetTokenStore<T> {

    public static final int TOKEN_LENGTH = 50;
    protected static Logger logger = Logger.getLogger(AbstractRequestTokenStore.class);
    protected Map<String, T> tokenList = new HashMap<>();
    private Integer tokenLifetimeMinutes = null;


    @Override
    public T create(User user) {
        clearExpiredTokens();
        assert user != null;
        assert !user.getEmailAddress().isEmpty();
        String randomToken = generateRandomToken();
        int tokenLifetimeMinutes = getTokenLifetimeMinutes();
        T token = createNewToken(user, randomToken, tokenLifetimeMinutes);
        tokenList.put(token.getToken(), token);
        return token;
    }

    protected String generateRandomToken() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);
        return token;
    }

    @Override
    public Optional<T> findResetRequest(String token) {
        clearExpiredTokens();
        T resetRequest = tokenList.get(token);
        if(isEligibleResetRequest(resetRequest)) {
            return Optional.of(resetRequest);
        }
        return Optional.empty();
    }

    @Override
    public boolean isEligibleToken(String token) {
        clearExpiredTokens();
        T resetRequest = tokenList.get(token);
        return isEligibleResetRequest(resetRequest);
    }

    protected boolean isEligibleResetRequest(T resetRequest) {
        if(resetRequest == null) {
            logger.info("isEligibleToken() : PasswordResetRequest must not be null");
            return false;
        }
        if(resetRequest.getExpiryDate().before(new Date())) {
            tokenList.remove(resetRequest.getToken());
            logger.info("isEligibleToken() : Token is expired, and has been deleted now.");
            return false;
        }
        return true;
    }

    /**
     * To be called periodically to remove expired tokens.
     *
     * @return the number of expired tokens that have been cleared
     */
    protected int clearExpiredTokens() {
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