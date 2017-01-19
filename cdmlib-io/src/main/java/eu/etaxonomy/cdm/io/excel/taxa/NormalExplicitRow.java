/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.excel.taxa;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class NormalExplicitRow extends ExcelRowBase {


    private String id;
    private String parentId;
    private String rank;
    private String scientificName;
    private String author;
    private String basionymAuthor;
    private String publishingAuthor;
    private String nameStatus;
    private String commonName;
    private String language;
    private String reference;
    private String date;
    private String family;
    private String infraFamily;
    private String genus;
    private String infraGenus;
    private String species;
    private String infraSpecies;

    private String collation;
    private String publicationYear;
    private String remarks;

    private String synonym;
    private String basionym;

    private String version;


	//Sets
	private TreeMap<Integer, String> distributions = new TreeMap<Integer, String>();

	private TreeMap<Integer, String> protologues = new TreeMap<Integer, String>();

	private TreeMap<Integer, String> images = new TreeMap<Integer, String>();

	public NormalExplicitRow() {

	    this.id = "0";
        this.parentId = "0";

		this.rank = "";
		this.scientificName = "";
		this.author =  "";
		this.nameStatus =  "";
		this.commonName =  "";
		this.language =  "";
		this.reference =  "";
		this.setDate("");
		this.setFamily("");
	}

	public NormalExplicitRow(String name, String parentId) {
		this(name, parentId, null);
	}

	public NormalExplicitRow(String scientificName, String parentId, String reference) {
		this.parentId = parentId;
		this.scientificName = scientificName;
		this.reference = reference;
	}

// **************************** GETTER / SETTER *********************************/

	/**
	 * @return the parentId
	 */
	public String getParentId() {
		return parentId;
	}
	/**
	 * @param parentId the parentId to set
	 */
	public void setParentId(String parentId) {
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
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
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

	public void putDistribution(int key, String distribution){
		this.distributions.put(key, distribution);
	}

	public List<String> getDistributions() {
		return getOrdered(distributions);
	}

	public void putProtologue(int key, String protologue){
		this.protologues.put(key, protologue);
	}

	public List<String> getProtologues() {
		return getOrdered(protologues);
	}

	public void putImage(int key, String image){
		this.images.put(key, image);
	}

	public List<String> getImages() {
		return getOrdered(images);
	}


	private List<String> getOrdered(TreeMap<Integer, String> tree) {
		List<String> result = new ArrayList<String>();
		for (String distribution : tree.values()){
			result.add(distribution);
		}
		return result;
	}

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }

    /**
     * @param family the family to set
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * @return the basionymAuthor
     */
    public String getBasionymAuthor() {
        return basionymAuthor;
    }

    /**
     * @param basionymAuthor the basionymAuthor to set
     */
    public void setBasionymAuthor(String basionymAuthor) {
        this.basionymAuthor = basionymAuthor;
    }

    /**
     * @return the infraFamily
     */
    public String getInfraFamily() {
        return infraFamily;
    }

    /**
     * @param infraFamily the infraFamily to set
     */
    public void setInfraFamily(String infraFamily) {
        this.infraFamily = infraFamily;
    }

    /**
     * @return the genus
     */
    public String getGenus() {
        return genus;
    }

    /**
     * @param genus the genus to set
     */
    public void setGenus(String genus) {
        this.genus = genus;
    }

    /**
     * @return the infraGenus
     */
    public String getInfraGenus() {
        return infraGenus;
    }

    /**
     * @param infraGenus the infraGenus to set
     */
    public void setInfraGenus(String infraGenus) {
        this.infraGenus = infraGenus;
    }

    /**
     * @return the species
     */
    public String getSpecies() {
        return species;
    }

    /**
     * @param species the species to set
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * @return the infraSpecies
     */
    public String getInfraSpecies() {
        return infraSpecies;
    }

    /**
     * @param infraSpecies the infraSpecies to set
     */
    public void setInfraSpecies(String infraSpecies) {
        this.infraSpecies = infraSpecies;
    }

    /**
     * @return the collation
     */
    public String getCollation() {
        return collation;
    }

    /**
     * @param collation the collation to set
     */
    public void setCollation(String collation) {
        this.collation = collation;
    }

    /**
     * @return the publicationYear
     */
    public String getPublicationYear() {
        return publicationYear;
    }

    /**
     * @param publicationYear the publicationYear to set
     */
    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return the synonym
     */
    public String getSynonym() {
        return synonym;
    }

    /**
     * @param synonym the synonym to set
     */
    public void setSynonym(String synonym) {
        this.synonym = synonym;
    }

    /**
     * @return the basionym
     */
    public String getBasionym() {
        return basionym;
    }

    /**
     * @param basionym the basionym to set
     */
    public void setBasionym(String basionym) {
        this.basionym = basionym;
    }

    /**
     * @return the publishingAuthor
     */
    public String getPublishingAuthor() {
        return publishingAuthor;
    }

    /**
     * @param publishingAuthor the publishingAuthor to set
     */
    public void setPublishingAuthor(String publishingAuthor) {
        this.publishingAuthor = publishingAuthor;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }




}
