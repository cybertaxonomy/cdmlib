/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dto.TaxonStatus;

/**
 * @author a.mueller
 \* @since 21.09.2016
 *
 */
public class TaxonInContextDTO {

    private UUID classificationUuid;
    private UUID taxonNodeUuid;
    private UUID taxonUuid;
    private String taxonLabel;
    private TaxonStatus taxonStatus;

    //name
    private UUID nameUuid;
    private String nameLabel;

    private String nameWithoutAuthor;
    private UUID rankUuid;
    private String rankLabel;
    private String genusOrUninomial;
    private String infraGenericEpithet;
    private String speciesEpithet;
    private String infraSpecificEpithet;
    private String authorship;

    //sec
    private UUID secundumUuid;
    private String secundumLabel;

    //accepted taxon
    private UUID acceptedTaxonUuid;
    private String acceptedNameLabel;

    private String acceptedTaxonLabel;


    //parent taxon
    private UUID parentTaxonUuid;
    private String parentNameLabel;
    private String parentTaxonLabel;



    private List<EntityDTO<Taxon>> children = new ArrayList<>();

    private List<EntityDTO<Synonym>> synonyms = new ArrayList<>();

    private List<MarkedEntityDTO<Taxon>> markedAncestors = new ArrayList<>();



//********************* GETTER / SETTER ****************************/

    public UUID getClassificationUuid() {return classificationUuid;}
    public void setClassificationUuid(UUID classificationUuid) {this.classificationUuid = classificationUuid;}

    public String getAcceptedTaxonLabel() {return acceptedTaxonLabel;}
    public void setAcceptedTaxonLabel(String acceptedTaxonLabel) {this.acceptedTaxonLabel = acceptedTaxonLabel;}

    public void setTaxonNodeUuid(UUID taxonNodeUuid) {this.taxonNodeUuid = taxonNodeUuid;}
    public void setTaxonUuid(UUID taxonUuid) {this.taxonUuid = taxonUuid;}

    public void setNameUuid(UUID nameUuid) {this.nameUuid = nameUuid;}

    public void setRankUuid(UUID rankUuid) {this.rankUuid = rankUuid;}
    public void setRankLabel(String rankLabel) {this.rankLabel = rankLabel;}

    public void setGenusOrUninomial(String genusOrUninomial) {this.genusOrUninomial = genusOrUninomial;}

    public void setInfraGenericEpithet(String infraGenericEpithet) {this.infraGenericEpithet = infraGenericEpithet;}

    public void setSpeciesEpithet(String speciesEpithet) {this.speciesEpithet = speciesEpithet;}

    public void setInfraSpecificEpithet(String infraSpecificEpithet) {this.infraSpecificEpithet = infraSpecificEpithet;}

    public void setAuthorship(String authorship) {this.authorship = authorship;}

    public void setSecundumUuid(UUID secundumUuid) {this.secundumUuid = secundumUuid;}

    public void setSecundumLabel(String secundumLabel) {this.secundumLabel = secundumLabel;}

    public void setChildren(List<EntityDTO<Taxon>> children) {this.children = children;}

    public void setMarkedAncestors(List<MarkedEntityDTO<Taxon>> markedAncestors) {this.markedAncestors = markedAncestors;}

    public UUID getTaxonNodeUuid() {return taxonNodeUuid;}

    public UUID getTaxonUuid() {return taxonUuid;}

    public UUID getNameUuid() {return nameUuid;}

    public UUID getRankUuid() {return rankUuid;}

    public String getRankLabel() {return rankLabel;}

    public String getGenusOrUninomial() {return genusOrUninomial;}

    public String getInfraGenericEpithet() {return infraGenericEpithet;}
    public String getSpeciesEpithet() {return speciesEpithet;}

    public String getInfraSpecificEpithet() {return infraSpecificEpithet;}

    /**
     * @return the authorship
     */
    public String getAuthorship() {
        return authorship;
    }

    /**
     * @return the secundumUuid
     */
    public UUID getSecundumUuid() {
        return secundumUuid;
    }

    /**
     * @return the secundumLabel
     */
    public String getSecundumLabel() {
        return secundumLabel;
    }


    /**
     * @return the taxonLabel
     */
    public String getTaxonLabel() {
        return taxonLabel;
    }

    /**
     * @param taxonLabel the taxonLabel to set
     */
    public void setTaxonLabel(String taxonLabel) {
        this.taxonLabel = taxonLabel;
    }

    /**
     * @return the nameLabel
     */
    public String getNameLabel() {
        return nameLabel;
    }

    /**
     * @param nameLabel the nameLabel to set
     */
    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    /**
     * @return the nameWithoutAuthor
     */
    public String getNameWithoutAuthor() {
        return nameWithoutAuthor;
    }

    /**
     * @param nameWithoutAuthor the nameWithoutAuthor to set
     */
    public void setNameWithoutAuthor(String nameWithoutAuthor) {
        this.nameWithoutAuthor = nameWithoutAuthor;
    }


    /**
     * @return the children
     */
    public List<EntityDTO<Taxon>> getChildren() {
        return children;
    }

    /**
     * @return the synonyms
     */
    public List<EntityDTO<Synonym>> getSynonyms() {
        return synonyms;
    }
    /**
     * @param synonyms the synonyms to set
     */
    public void setSynonyms(List<EntityDTO<Synonym>> synonyms) {
        this.synonyms = synonyms;
    }

    /**
     * @return the markedAncestors
     */
    public List<MarkedEntityDTO<Taxon>> getMarkedAncestors() {
        return markedAncestors;
    }


    /**
     * @return the acceptedTaxonUuid
     */
    public UUID getAcceptedTaxonUuid() {
        return acceptedTaxonUuid;
    }

    /**
     * @param acceptedTaxonUuid the acceptedTaxonUuid to set
     */
    public void setAcceptedTaxonUuid(UUID acceptedTaxonUuid) {
        this.acceptedTaxonUuid = acceptedTaxonUuid;
    }

    /**
     * @return the acceptedNameLabel
     */
    public String getAcceptedNameLabel() {
        return acceptedNameLabel;
    }

    /**
     * @param acceptedNameLabel the acceptedNameLabel to set
     */
    public void setAcceptedNameLabel(String acceptedNameLabel) {
        this.acceptedNameLabel = acceptedNameLabel;
    }

    /**
     * @return the taxonStatus
     */
    public TaxonStatus getTaxonStatus() {
        return taxonStatus;
    }

    /**
     * @param taxonStatus the taxonStatus to set
     */
    public void setTaxonStatus(TaxonStatus taxonStatus) {
        this.taxonStatus = taxonStatus;
    }

    public UUID getParentTaxonUuid() {
        return parentTaxonUuid;
    }
    public void setParentTaxonUuid(UUID parentTaxonUuid) {
        this.parentTaxonUuid = parentTaxonUuid;
    }

    public String getParentNameLabel() {
        return parentNameLabel;
    }
    public void setParentNameLabel(String parentNameLabel) {
        this.parentNameLabel = parentNameLabel;
    }

    public String getParentTaxonLabel() {
        return parentTaxonLabel;
    }
    public void setParentTaxonLabel(String parentTaxonLabel) {
        this.parentTaxonLabel = parentTaxonLabel;
    }

// *************************** ADDER *******************************/

    public void addChild(EntityDTO<Taxon> childDto){
        this.children.add(childDto);
    }


    public void addSynonym(EntityDTO<Synonym> synonymDto){
        this.synonyms.add(synonymDto);
    }

    public void addMarkedAncestor(MarkedEntityDTO<Taxon> markedAncestor){
        this.markedAncestors.add(markedAncestor);
    }



}
