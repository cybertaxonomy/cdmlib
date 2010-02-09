// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

/**
 * @author a.kohlbecker
 * @date 23.06.2009
 *
 */
public class AbstractController {
	
	protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
			"$"
	});
	
	protected Pattern uuidParameterPattern = null;
	
	protected void setUuidParameterPattern(String pattern){
		uuidParameterPattern = Pattern.compile(pattern);
	}
	
	protected UUID readValueUuid(HttpServletRequest request, String pattern) {
		String path = request.getServletPath();
		if(path != null) {
			Matcher uuidMatcher;
			if(pattern != null){
				Pattern suppliedPattern = Pattern.compile(pattern);
				uuidMatcher = suppliedPattern.matcher(path);
			} else {
				uuidMatcher = uuidParameterPattern.matcher(path);				
			}
			if(uuidMatcher.matches() && uuidMatcher.groupCount() > 0){
				try {
					UUID uuid = UUID.fromString(uuidMatcher.group(1));
					return uuid;
				} catch (Exception e) {
					throw new IllegalArgumentException(HttpStatusMessage.UUID_INVALID.toString());
				}
			} else {
				throw new IllegalArgumentException(HttpStatusMessage.UUID_MISSING.toString());
			}
		}
		return null;
	}

}
