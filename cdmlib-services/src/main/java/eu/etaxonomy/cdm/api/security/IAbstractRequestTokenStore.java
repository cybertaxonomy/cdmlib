/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.security;

import java.util.Optional;

/**
 * @author a.kohlbecker
 * @since Nov 3, 2021
 */
public interface IAbstractRequestTokenStore<T extends AbstractRequestToken, X extends Object> {

    public static final int TOKEN_LIFETIME_MINUTES_DEFAULT = 60 * 6;

    public T create(String userEmailAddress, X additionalData);

    /**
     * Removes the corresponding <code>AbstractRequestToken</code> from the
     * store
     *
     * @param token
     *            The token string
     * @return true if the token to be remove has existed, otherwise false
     */
    public boolean remove(String token);

    /**
     * Checks is the supplied token exists and has not expired.
     *
     * @param token
     *            The token string
     * @return true if the token is valid
     */
    public boolean isEligibleToken(String token);

    /**
     * Returns the corresponding <code>AbstractRequestToken</code> if it exists
     * and is not expired.
     *
     * @param token
     *            The token string
     * @return the valid <code>AbstractRequestToken</code> or an empty
     *         <code>Optional</code>
     */
    public Optional<T> findRequest(String token);


    public void setTokenLifetimeMinutes(int tokenLifetimeMinutes);

    public T createNewToken(String userEmailAddress, X additionalData, String randomToken, int tokenLifetimeMinutes);

}