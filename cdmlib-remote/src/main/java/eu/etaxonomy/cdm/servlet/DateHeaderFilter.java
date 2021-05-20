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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.util.NestedServletException;

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


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            OffsetDateTime dateTime = OffsetDateTime.now();
            // from chain.doFilter() NestedServletExceptions with NPEs may bubble up from
            // from controller methods which miss proper handling of not found entities or the like
            // (HTTP 404 : uuid not found)
            // These situations are caught below and logged at info level for debugging purposes
            // during development. In production environment we don't want to clutter the logs with
            // this. see #9185
            chain.doFilter(request, response);
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setHeader("Date", dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        } catch ( NullPointerException e) {
            // see above and #9185
            Logger.getLogger(this.getClass()).info("Can not add data header: " + e.getMessage());
        } catch (NestedServletException e) {
            // see above and #9185
            if(e.getCause() != null && e.getCause() instanceof NullPointerException) {
                Logger.getLogger(this.getClass()).info("Can not add data header: " + e.getCause());
            } else {
                // higher level in this case as these are unexpected
                Logger.getLogger(this.getClass()).warn("Can not add data header.", e);
            }
        } catch (Exception e) {
            // higher level in this case as these are unexpected
            Logger.getLogger(this.getClass()).warn("Can not add data header.", e);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
