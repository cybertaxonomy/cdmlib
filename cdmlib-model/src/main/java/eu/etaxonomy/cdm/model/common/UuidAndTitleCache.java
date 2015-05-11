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

import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * @author n.hoffmann
 * @created Aug 14, 2009
 * @version 1.0
 */


public class UuidAndTitleCache<T extends ICdmBase> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 3446993458279371682L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger	.getLogger(UuidAndTitleCache.class);

	private Class<T> type;
	private UUID uuid;
	private String titleCache;
	private boolean isOrphaned;

	public UuidAndTitleCache(Class<T> type, UUID uuid, String titleCache) {
		this(uuid, titleCache);
		this.type = type;
		this.isOrphaned = false;
	}

	public UuidAndTitleCache(Class<T> type, UUID uuid, String titleCache, Boolean isOrphaned) {
		this(type, uuid, titleCache);
		this.isOrphaned = isOrphaned;
	}

	/**
	 * @param uuid2
	 * @param string
	 */
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

	public Class<T> getType(){
		return type;
	}

	public boolean getIsOrphaned() {
		return this.isOrphaned;
	}

}
