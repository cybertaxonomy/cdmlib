/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.jaxb;

import java.util.UUID;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.log4j.Logger;

/**
 * @author a.mueller
 * @created 23.07.2008
 * @version 1.0
 */


public class UUIDAdapter extends XmlAdapter<String, String> {
	private static final Logger logger = Logger.getLogger(UUIDAdapter.class);
	
	public static String UUID_URN_PREFIX = "urn-uuid-";

	@Override
	public String marshal(String uuidStr) throws Exception {
		return UUIDAdapter.UUID_URN_PREFIX + uuidStr;
	}

	@Override
	public String unmarshal(String string) throws Exception {
		if(string.startsWith(UUIDAdapter.UUID_URN_PREFIX)) {
			String uuidPart = string.substring(UUIDAdapter.UUID_URN_PREFIX.length());
			return uuidPart;
		} else {
			throw new Exception("uuid attribute should start with " + UUIDAdapter.UUID_URN_PREFIX);
		}
		
	}

}