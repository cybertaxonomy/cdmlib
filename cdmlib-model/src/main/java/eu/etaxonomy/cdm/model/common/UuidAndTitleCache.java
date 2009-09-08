// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;

import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * @author n.hoffmann
 * @created Aug 14, 2009
 * @version 1.0
 */
public class UuidAndTitleCache {
	private static final Logger logger = Logger
			.getLogger(UuidAndTitleCache.class);
	
	UUID uuid;
	String titleCache;
	
	public UuidAndTitleCache(UUID uuid, String titleCache) {
		this.uuid = uuid;
		this.titleCache = titleCache;
	}
	
	/**
	 * @return the titleCache
	 */
	public String getTitleCache() {
		return titleCache;
	}
	
	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}
	
}
