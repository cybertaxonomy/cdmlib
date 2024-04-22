/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dto;

import java.io.Serializable;
import java.util.UUID;

import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * @author n.hoffmann
 * @since Aug 14, 2009
 */
public class UuidAndTitleCache<T extends ICdmBase> implements Serializable {

	private static final long serialVersionUID = 3446993458279371682L;

	private Class<? extends T> type;
	final private UUID uuid;
	private Integer id;
	private String titleCache;
	private String abbrevTitleCache;
	private boolean isProtectedTitleCache = true;

    private boolean isOrphaned;

    public UuidAndTitleCache(UUID uuid, Integer id, String titleCache, String abbrevTitleCache) {
        this.uuid = uuid;
        this.setTitleCache(titleCache);
        this.id = id;
        this.abbrevTitleCache = abbrevTitleCache;
    }

    public UuidAndTitleCache(UUID uuid, Integer id, String titleCache) {
        this.uuid = uuid;
        this.setTitleCache(titleCache);
        this.id = id;
        this.abbrevTitleCache = null;
    }

    public UuidAndTitleCache(Class<? extends T> type, UUID uuid, Integer id, String titleCache, String abbrevTitleCache) {
        this(uuid, id, titleCache, abbrevTitleCache);
        this.type = type;
        this.isOrphaned = false;
    }

    public UuidAndTitleCache(Class<? extends T> type, UUID uuid, Integer id, String titleCache) {
        this(uuid, id, titleCache, null);
        this.type = type;
        this.isOrphaned = false;
    }

	public UuidAndTitleCache(Class<? extends T> type, UUID uuid, Integer id, String titleCache, Boolean isOrphaned, String abbrevTitleCache) {
		this(type, uuid, id, titleCache, abbrevTitleCache);
		this.isOrphaned = isOrphaned;
	}

	public UuidAndTitleCache(UUID uuid, String titleCache) {
        this( uuid, null, titleCache);
	}

//  ******************* GETTER / SETTER **********************/

	public String getTitleCache() {
		return titleCache;
	}
	public void setTitleCache(String titleCache) {
        this.titleCache = titleCache;
    }

    public String getAbbrevTitleCache() {
        return abbrevTitleCache;
    }
    public void setAbbrevTitleCache(String abbrevTitleCache) {
        this.abbrevTitleCache = abbrevTitleCache;
    }

    public boolean isProtectedTitleCache() {
        return isProtectedTitleCache;
    }
    public void setProtectedTitleCache(boolean isProtectedTitleCache) {
        this.isProtectedTitleCache = isProtectedTitleCache;
    }

	public UUID getUuid() {
		return uuid;
	}

	public Integer getId() {
	    return id;
    }

	public Class<? extends T> getType(){
		return type;
	}

	public boolean getIsOrphaned() {
		return this.isOrphaned;
	}

    public void setType(Class<T> type) {
        this.type = type;
    }

//************************** toString **********************************/

    @Override
    public String toString() {
        return "UuidAndTitleCache [type= " + type + ", uuid= " + uuid + ", id=" + id + ", titleCache= " + getTitleCache()
                + ", isOrphaned=" + isOrphaned + ", abbrevTitleCache= " + abbrevTitleCache +"]";
    }
}