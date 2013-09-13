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
	 * If the <code>pattern2</code> is an absolute path, that is it starts with
	 * an slash character, the <code>pattern2</code> overrules pattern1 and no
	 * combined at all is performed. I such case the method just returns
	 * <code>pattern2</code>
	 * 
	 * <p>For example: <table>
	 * <tr><th>Pattern 1</th><th>Pattern 2</th><th>Result</th></tr>
	 * <tr><td>/hotels</td><td>{@code null}</td><td>/hotels</td></tr>
	 * <tr><td>{@code null}</td><td>/hotels</td><td>/hotels</td></tr>
	 * <tr><td>/hotels</td><td>/bookings</td><td>/bookings</td></tr>
	 * <tr><td>/hotels</td><td>bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels/*</td><td>bookings</td><td>/hotels/bookings</td></tr>
	 * <tr><td>/hotels/*</td><td>/bookings</td><td>/bookings</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>/bookings</td><td>/bookings</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>bookings</td><td>/hotels/&#42;&#42;/bookings</td></tr>
	 * <tr><td>/hotels</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
	 * <tr><td>/hotels/*</td><td>{hotel}</td><td>/hotels/{hotel}</td></tr>
	 * <tr><td>/hotels/&#42;&#42;</td><td>{hotel}</td><td>/hotels/&#42;&#42;/{hotel}</td></tr>
	 * <tr><td>/*.html</td><td>hotels.html</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>/hotels.html</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>hotels</td><td>/hotels.html</td></tr>
	 * <tr><td>/*.html</td><td>/hotels</td><td>/hotels</td></tr>
	 * <tr><td>/*.html</td><td>/*.txt</td><td>IllegalArgumentException</td></tr> </table>
	 *
	 * 
	 * @param pattern1
	 * @param pattern2
	 */
	@Override
	public String combine(String pattern1, String pattern2) {
		if(pattern2.startsWith("/")){
			return pattern2;
		} else {
			return super.combine(pattern1, pattern2);			
		}
	}
	
	

}
