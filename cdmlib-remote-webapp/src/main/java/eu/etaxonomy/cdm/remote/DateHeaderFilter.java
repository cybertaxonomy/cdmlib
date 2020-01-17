/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Adds a "Date" Header to the http response formatted as RFC_1123_DATE_TIME.
 *
 * https://tools.ietf.org/html/rfc2616#section-14.18
 *
 * @author a.kohlbecker
 * @since Jan 17, 2020
 *
 */
public class DateHeaderFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        OffsetDateTime dateTime = OffsetDateTime.now();
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Date", dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        chain.doFilter(request, response);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // Nothing to do
    }

}
