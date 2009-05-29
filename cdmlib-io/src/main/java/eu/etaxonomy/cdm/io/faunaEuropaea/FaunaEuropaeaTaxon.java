/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.faunaEuropaea;

import java.util.UUID;

/**
 * @author a.babadshanjan
 * @created 26.05.2009
 * @version 1.0
 */
public class FaunaEuropaeaTaxon {
	
	private UUID uuid;
	private int id;
	private int parentId;
	private int rankId;
	private String scientificName;
	
	public FaunaEuropaeaTaxon() {
		this.id = 0;
		this.parentId = 0;
		this.rankId = 0;
	}
	
	public FaunaEuropaeaTaxon(String name, int parentId) {
		this(name, parentId, null);
	}
	
	public FaunaEuropaeaTaxon(String scientificName, int parentId, String reference) {
		this.parentId = parentId;
		this.scientificName = scientificName;
	}
	
	/**
	 * @return the parentId
	 */
	public int getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	/**
	 * @return the name
	 */
	public String getScientificName() {
		return scientificName;
	}
	/**
	 * @param name the name to set
	 */
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the rankId
	 */
	public int getRankId() {
		return rankId;
	}

	/**
	 * @param rankId the rankId to set
	 */
	public void setRankId(int rankId) {
		this.rankId = rankId;
	}

	/**
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

}
