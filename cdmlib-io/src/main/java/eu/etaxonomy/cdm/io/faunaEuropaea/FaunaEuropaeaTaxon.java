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
	private UUID parentUuid;
	private int id;
	private int rankId;
	private int parentId;
	private int parentRankId;
	private int grandParentId;
	private int grandParentRankId;
	private int greatGrandParentId;
	private int greatGrandParentRankId;
	private int greatGreatGrandParentId;
	private int greatGreatGrandParentRankId;
	private int greatGreatGreatGrandParentRankId;
	private int greatGreatGreatGrandParentId;
	private int originalGenusId;
	private Integer year;
	private String localName;
	private String parentName;
	private String grandParentName;
	private String greatGrandParentName;
	private String greatGreatGrandParentName;
	private String greatGreatGreatGrandParentName;
	private String originalGenusName;
	private String authorName;
	private boolean parenthesis = false;
	private boolean valid = false;
//	private int authorId;
//	private UUID nameUuid;
	
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
		this.localName = "";
		this.parentName = "";
		this.grandParentName = "";
		this.greatGrandParentName = "";
		this.originalGenusName = "";
		this.authorName = "";
	}
	
//	public FaunaEuropaeaTaxon(String name, int parentId) {
//		this(name, parentId, null);
//	}
	
//	public FaunaEuropaeaTaxon(String scientificName, int parentId, String reference) {
//		this.parentId = parentId;
//		this.localName = scientificName;
//	}
	
	/**
	 * @return the greatGreatGrandParentRankId
	 */
	protected int getGreatGreatGrandParentRankId() {
		return greatGreatGrandParentRankId;
	}

	/**
	 * @param greatGreatGrandParentRankId the greatGreatGrandParentRankId to set
	 */
	protected void setGreatGreatGrandParentRankId(int greatGreatGrandParentRankId) {
		this.greatGreatGrandParentRankId = greatGreatGrandParentRankId;
	}

	/**
	 * @return the greatGreatGreatGrandParentRankId
	 */
	protected int getGreatGreatGreatGrandParentRankId() {
		return greatGreatGreatGrandParentRankId;
	}

	/**
	 * @param greatGreatGreatGrandParentRankId the greatGreatGreatGrandParentRankId to set
	 */
	protected void setGreatGreatGreatGrandParentRankId(
			int greatGreatGreatGrandParentRankId) {
		this.greatGreatGreatGrandParentRankId = greatGreatGreatGrandParentRankId;
	}

	/**
	 * @return the greatGreatGreatGrandParentId
	 */
	protected int getGreatGreatGreatGrandParentId() {
		return greatGreatGreatGrandParentId;
	}

	/**
	 * @param greatGreatGreatGrandParentId the greatGreatGreatGrandParentId to set
	 */
	protected void setGreatGreatGreatGrandParentId(int greatGreatGreatGrandParentId) {
		this.greatGreatGreatGrandParentId = greatGreatGreatGrandParentId;
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
	public Integer getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(Integer year) {
		if (year != null && year.intValue() == 0) {
			year = null;
		}
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

	/**
	 * @return the parentName
	 */
	public String getParentName() {
		return parentName;
	}

	/**
	 * @param parentName the parentName to set
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	/**
	 * @return the grandParentName
	 */
	public String getGrandParentName() {
		return grandParentName;
	}

	/**
	 * @param grandParentName the grandParentName to set
	 */
	public void setGrandParentName(String grandParentName) {
		this.grandParentName = grandParentName;
	}

	/**
	 * @return the greatGrandParentName
	 */
	public String getGreatGrandParentName() {
		return greatGrandParentName;
	}

	/**
	 * @param greatGrandParentName the greatGrandParentName to set
	 */
	public void setGreatGrandParentName(String greatGrandParentName) {
		this.greatGrandParentName = greatGrandParentName;
	}

	/**
	 * @return the greatGreatGreatGrandParentName
	 */
	protected String getGreatGreatGreatGrandParentName() {
		return greatGreatGreatGrandParentName;
	}

	/**
	 * @param greatGreatGreatGrandParentName the greatGreatGreatGrandParentName to set
	 */
	protected void setGreatGreatGreatGrandParentName(
			String greatGreatGreatGrandParentName) {
		this.greatGreatGreatGrandParentName = greatGreatGreatGrandParentName;
	}

	/**
	 * @return the parentRankId
	 */
	public int getParentRankId() {
		return parentRankId;
	}

	/**
	 * @param parentRankId the parentRankId to set
	 */
	public void setParentRankId(int parentRankId) {
		this.parentRankId = parentRankId;
	}

	/**
	 * @return the grandParentRankId
	 */
	public int getGrandParentRankId() {
		return grandParentRankId;
	}

	/**
	 * @param grandParentRankId the grandParentRankId to set
	 */
	public void setGrandParentRankId(int grandParentRankId) {
		this.grandParentRankId = grandParentRankId;
	}

	/**
	 * @return the greatGrandParentRankId
	 */
	public int getGreatGrandParentRankId() {
		return greatGrandParentRankId;
	}

	/**
	 * @param greatGrandParentRankId the greatGrandParentRankId to set
	 */
	public void setGreatGrandParentRankId(int greatGrandParentRankId) {
		this.greatGrandParentRankId = greatGrandParentRankId;
	}

	/**
	 * @return the greatGreatGrandParentName
	 */
	public String getGreatGreatGrandParentName() {
		return greatGreatGrandParentName;
	}

	/**
	 * @param greatGreatGrandParentName the greatGreatGrandParentName to set
	 */
	public void setGreatGreatGrandParentName(String greatGreatGrandParentName) {
		this.greatGreatGrandParentName = greatGreatGrandParentName;
	}

	/**
	 * @return the grandParentId
	 */
	public int getGrandParentId() {
		return grandParentId;
	}

	/**
	 * @param grandParentId the grandParentId to set
	 */
	public void setGrandParentId(int grandParentId) {
		this.grandParentId = grandParentId;
	}

	/**
	 * @return the greatGrandParentId
	 */
	public int getGreatGrandParentId() {
		return greatGrandParentId;
	}

	/**
	 * @param greatGrandParentId the greatGrandParentId to set
	 */
	public void setGreatGrandParentId(int greatGrandParentId) {
		this.greatGrandParentId = greatGrandParentId;
	}

	/**
	 * @return the greatGreatGrandParentId
	 */
	public int getGreatGreatGrandParentId() {
		return greatGreatGrandParentId;
	}

	/**
	 * @param greatGreatGrandParentId the greatGreatGrandParentId to set
	 */
	public void setGreatGreatGrandParentId(int greatGreatGrandParentId) {
		this.greatGreatGrandParentId = greatGreatGrandParentId;
	}

	/**
	 * @return the parentUuid
	 */
	public UUID getParentUuid() {
		return parentUuid;
	}

	/**
	 * @param parentUuid the parentUuid to set
	 */
	public void setParentUuid(UUID parentUuid) {
		this.parentUuid = parentUuid;
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
