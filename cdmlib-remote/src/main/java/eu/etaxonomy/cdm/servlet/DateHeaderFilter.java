/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.servlet;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Adds a "Date" Header to the http response formatted as RFC_1123_DATE_TIME.
 *
 * https://tools.ietf.org/html/rfc2616#section-14.18
 *
 * @author a.kohlbecker
 * @since Jan 17, 2020
 */
public class DateHeaderFilter implements Filter {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Let request exceptions propagate; only guard setting the Date header
        // (may fail if the response is already committed). see #9185
        chain.doFilter(request, response);
        try {
            OffsetDateTime dateTime = OffsetDateTime.now();
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setHeader("Date", dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        } catch (Exception e) {
            logger.debug("Can not add date header: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}