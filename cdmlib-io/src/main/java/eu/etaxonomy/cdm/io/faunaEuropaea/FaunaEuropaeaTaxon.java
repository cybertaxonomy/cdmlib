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
//	private UUID nameUuid;
	private int id;
	private int parentId;
	private int originalGenusId;
	private int rankId;
	private String localName;
	private String originalGenusName = "";
	private int year;
//	private int authorId;
	private String authorName;
	private boolean parenthesis = false;
	private boolean valid = false;
	
	/**
	 * @return the authorName
	 */
	public String getAuthorName() {
		return authorName;
	}

	/**
	 * @param authorName the authorName to set
	 */
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	/**
	 * @return the parenthesis
	 */
	public boolean isParenthesis() {
		return parenthesis;
	}

	/**
	 * @param parenthesis the parenthesis to set
	 */
	public void setParenthesis(boolean parenthesis) {
		this.parenthesis = parenthesis;
	}

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
		this.localName = scientificName;
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
	public String getLocalName() {
		return localName;
	}
	/**
	 * @param name the name to set
	 */
	public void setLocalName(String scientificName) {
		this.localName = scientificName;
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

//	/**
//	 * @return the nameUuid
//	 */
//	public UUID getNameUuid() {
//		return nameUuid;
//	}
//
//	/**
//	 * @param nameUuid the nameUuid to set
//	 */
//	public void setNameUuid(UUID nameUuid) {
//		this.nameUuid = nameUuid;
//	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return authorName;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(String author) {
		this.authorName = author;
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * @return the originalGenusId
	 */
	public int getOriginalGenusId() {
		return originalGenusId;
	}

	/**
	 * @param originalGenusId the originalGenusId to set
	 */
	public void setOriginalGenusId(int originalGenusId) {
		this.originalGenusId = originalGenusId;
	}

	/**
	 * @return the originalGenusName
	 */
	public String getOriginalGenusName() {
		return originalGenusName;
	}

	/**
	 * @param originalGenusName the originalGenusName to set
	 */
	public void setOriginalGenusName(String originalGenusName) {
		this.originalGenusName = originalGenusName;
	}

//	/**
//	 * @return the authorId
//	 */
//	public int getAuthorId() {
//		return authorId;
//	}
//
//	/**
//	 * @param authorId the authorId to set
//	 */
//	public void setAuthorId(int authorId) {
//		this.authorId = authorId;
//	}

}
