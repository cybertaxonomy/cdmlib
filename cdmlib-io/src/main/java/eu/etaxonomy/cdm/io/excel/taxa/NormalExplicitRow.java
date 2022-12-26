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
 * @since 13.01.2009
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
    private String infraSpecies_rank;

    private String collation;
    private String page;
    private String publicationYear;
    private String remarks;

    private String synonym;
    private String basionym;
    private String accepted_id;
    private String taxonomicStatus;

    private String version;

    private String ipni_id;
    private String source;
    private String source_Id;
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

	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getScientificName() {
		return scientificName;
	}
	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getReference() {
		return reference;
	}
	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getRank() {
		return rank;
	}
	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

	public String getNameStatus() {
		return nameStatus;
	}
	public void setNameStatus(String nameStatus) {
		this.nameStatus = nameStatus;
	}

	public String getCommonName() {
		return commonName;
	}
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getLanguage() {
		return language;
	}
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

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getFamily() {
        return family;
    }
    public void setFamily(String family) {
        this.family = family;
    }

    public String getBasionymAuthor() {
        return basionymAuthor;
    }
    public void setBasionymAuthor(String basionymAuthor) {
        this.basionymAuthor = basionymAuthor;
    }


    public String getInfraFamily() {
        return infraFamily;
    }
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


    public void setPage(String value) {
        this.page = value;

    }
    public String getPage() {
        return this.page;

    }


    public void setAccepted_id(String value) {
        this.accepted_id = value;
    }

    public String getAccepted_id(){
        return accepted_id;
    }

    public void setInfraSpecies_Rank(String value) {
        this.infraSpecies_rank = value;
    }

    public String getInfraSpecies_Rank() {
        return this.infraSpecies_rank;
    }

    public String getTaxonomicStatus() {
        return this.taxonomicStatus;
    }

    public void setTaxonomicStatus(String value) {
        this.taxonomicStatus = value;
    }

    public String getIpni_id() {
        return ipni_id;
    }

    public void setIpni_id(String ipni_id) {
        this.ipni_id = ipni_id;
    }

   public void setSource(String value) {
      this.source = value;

    }
   public String getSource() {
       return this.source ;

     }
   public void setSource_Id(String value) {
       this.source_Id= value;
   }
   public String getSource_Id() {
       return this.source_Id;
   }


}
