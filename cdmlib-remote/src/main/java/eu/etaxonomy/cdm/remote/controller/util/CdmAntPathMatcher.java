// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import org.springframework.util.AntPathMatcher;

/**
 * @author a.kohlbecker
 * @date 28.07.2010
 *
 */
public class CdmAntPathMatcher extends AntPathMatcher {

    /**
     * Changes the combination behavior of the overridden method
     * {@link AntPathMatcher#combine(String, String)} only a little but quite
     * useful bit:
     * <p>
     * If the <code>pattern2</code> starts with a double slash it is treated as an absolute
     * path. In this case <code>pattern1</code> will ignored completely, that is
     * the pattern will not be combined and <code>pattern2</code> is returned unmodified.
     *
     * <p>For example (lines in string letters are special to the <code>CdmAntPathMatcher</code>): <table>
     * <tr><th>Pattern 1</th><th>Pattern 2</th><th>Result</th></tr>
     * <tr><td>/hotels</td><td>{@code null}</td><td>/hotels</td></tr>
     * <tr><td>{@code null}</td><td>/hotels</td><td>/hotels</td></tr>
     * <tr><td><b>{@code null}</b></td><td><b>//hotels</b></td><td>/hotels</td></tr>
     * <tr><td>/hotels</td><td><b>//bookings</b></td><td>/bookings</td></tr>
     * <tr><td>/hotels</td><td>bookings</td><td>/hotels/bookings</td></tr>
     * <tr><td>/hotels/*</td><td>bookings</td><td>/hotels/bookings</td></tr>
     * <tr><td>/hotels/*</td><td><b>//bookings</b></td><td>/bookings</td></tr>
     * <tr><td>/hotels/&#42;&#42;</td><td><b>//bookings</b></td><td>/bookings</td></tr>
     * <tr><td>/hotels/&#42;&#42;</td><td>bookings</td><td>/hotels/&#42;&#42;/bookings</td></tr>
     * <tr><td>/hotels</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
     * <tr><td>/hotels/*</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
     * <tr><td>/hotels/**;</td><td>{hotel}</td><td>/hotels/&#42;&#42;/{hotel}</td></tr>
     * <tr><td>/*.html</td><td>hotels.html</td><td>hotels.html</td></tr>
     * <tr><td>/*.html</td><td><b>//hotels.html</b></td><td>/hotels.html</td></tr>
     * <tr><td>/*.html</td><td>/hotels.html</td><td>/hotels.html</td></tr>
     * <tr><td>/*.html</td><td>/hotels</td><td>/hotels</td></tr>
     * <tr><td>/*.html</td><td>hotels</td><td>/hotels</td></tr>
     * <tr><td>/*.html</td><td>hotels</td><td>hotels.html</td></tr>
     * <tr><td>/*.html</td><td><b>//hotels</b></td><td>/hotels</td></tr>
     * <tr><td>/*.html</td><td><b>//*.txt</b></td><td>/*.txt</td></tr> </table>
     *
     *
     * @param pattern1
     * @param pattern2
     */
    @Override
    public String combine(String pattern1, String pattern2) {
        if(pattern2 != null && pattern2.startsWith("//")){
            return pattern2.substring(1);
        } else {
                return super.combine(pattern1, pattern2);

        }
    }



}
