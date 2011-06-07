// $Id$
/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.common.mediaMetaData;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;

/**
 * 
 * @author n.hoffmann
 * @created 13.11.2008
 * @version 1.0
 */
public abstract class MediaMetaData {
	private static Logger logger = Logger.getLogger(MediaMetaData.class);
	protected String formatName, mimeType;
	HashMap<String, String> metaData;
	
	
	public abstract void readMetaData(URI mediaUri, Integer timeOut) throws IOException, HttpException;
 
	public Map<String, String> getMetaData() {
		
		return metaData;
	}

 
	


		
	
}
