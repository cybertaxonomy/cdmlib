/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class TaxonLight {
	
	private int id;
	private int parentId;
	private String rank;
	private String scientificName;
	private String author;
	private String nameStatus;
	private String commonName;
	private String language;
	private String reference;
	
	public TaxonLight() {
		this.id = 0;
		this.parentId = 0;
		this.rank = new String("");
		this.scientificName = new String("");
		this.author = new String("");
		this.nameStatus = new String("");
		this.commonName = new String("");
		this.language = new String("");
		this.reference = new String("");
	}
	
	public TaxonLight(String name, int parentId) {
		this(name, parentId, null);
	}
	
	public TaxonLight(String scientificName, int parentId, String reference) {
		this.parentId = parentId;
		this.scientificName = scientificName;
		this.reference = reference;
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
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}
	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
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
	 * @return the rank
	 */
	public String getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(String rank) {
		this.rank = rank;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return the nameStatus
	 */
	public String getNameStatus() {
		return nameStatus;
	}

	/**
	 * @param nameStatus the nameStatus to set
	 */
	public void setNameStatus(String nameStatus) {
		this.nameStatus = nameStatus;
	}

	/**
	 * @return the commonName
	 */
	public String getCommonName() {
		return commonName;
	}

	/**
	 * @param commonName the commonName to set
	 */
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	
}
