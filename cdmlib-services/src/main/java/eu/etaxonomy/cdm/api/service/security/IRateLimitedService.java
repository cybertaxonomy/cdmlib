/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.security;

import java.time.Duration;

/**
 * @author a.kohlbecker
 * @since Nov 18, 2021
 */
public interface IRateLimitedService {

    /**
     * Requests to the service methods should be rate limited.
     * This method allows to set the timeout when waiting for a
     * free execution slot. {@link #RATE_LIMTER_TIMEOUT_SECONDS}
     * is the default
     */
    void setRateLimiterTimeout(Duration timeout);


    /**
     * see {@link #setRateLimiterTimeout(Duration)}
     *
     * @return the currently used timeout
     */
    Duration getRateLimiterTimeout();

    /**
     * Requests to the service methods should be rate limited.
     * This method allows to override the default rate
     * {@link #PERMITS_PER_SECOND}
     */
    public void setRate(double rate);

}
