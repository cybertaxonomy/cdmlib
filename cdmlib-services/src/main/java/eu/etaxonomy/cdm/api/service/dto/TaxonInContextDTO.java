// $Id$
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

/**
 * @author a.mueller
 * @date 21.09.2016
 *
 */
public class TaxonInContextDTO {
    private UUID classificationUuid;
    private UUID taxonNodeUuid;
    private UUID taxonUuid;
    private String taxonLabel;

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

    private List<EntityDTO<Taxon>> children = new ArrayList<>();

    private List<EntityDTO<Synonym>> synonyms = new ArrayList<>();

    private List<MarkedEntityDTO<Taxon>> markedAncestors = new ArrayList<>();



//********************* GETTER / SETTER ****************************/

    /**
     * @return the classificationUuid
     */
    public UUID getClassificationUuid() {
        return classificationUuid;
    }

    /**
     * @param classificationUuid the classificationUuid to set
     */
    public void setClassificationUuid(UUID classificationUuid) {
        this.classificationUuid = classificationUuid;
    }

    /**
     * @param taxonNodeUuid the taxonNodeUuid to set
     */
    public void setTaxonNodeUuid(UUID taxonNodeUuid) {
        this.taxonNodeUuid = taxonNodeUuid;
    }

    /**
     * @param taxonUuid the taxonUuid to set
     */
    public void setTaxonUuid(UUID taxonUuid) {
        this.taxonUuid = taxonUuid;
    }

    /**
     * @param nameUuid the nameUuid to set
     */
    public void setNameUuid(UUID nameUuid) {
        this.nameUuid = nameUuid;
    }

    /**
     * @param rankUuid the rankUuid to set
     */
    public void setRankUuid(UUID rankUuid) {
        this.rankUuid = rankUuid;
    }

    /**
     * @param rankLabel the rankLabel to set
     */
    public void setRankLabel(String rankLabel) {
        this.rankLabel = rankLabel;
    }

    /**
     * @param genusOrUninomial the genusOrUninomial to set
     */
    public void setGenusOrUninomial(String genusOrUninomial) {
        this.genusOrUninomial = genusOrUninomial;
    }

    /**
     * @param infragenericEpithet the infragenericEpithet to set
     */
    public void setInfraGenericEpithet(String infraGenericEpithet) {
        this.infraGenericEpithet = infraGenericEpithet;
    }

    /**
     * @param speciesEpithet the speciesEpithet to set
     */
    public void setSpeciesEpithet(String speciesEpithet) {
        this.speciesEpithet = speciesEpithet;
    }

    /**
     * @param infraspecificEpithet the infraspecificEpithet to set
     */
    public void setInfraSpecificEpithet(String infraSpecificEpithet) {
        this.infraSpecificEpithet = infraSpecificEpithet;
    }

    /**
     * @param authorship the authorship to set
     */
    public void setAuthorship(String authorship) {
        this.authorship = authorship;
    }

    /**
     * @param secundumUuid the secundumUuid to set
     */
    public void setSecundumUuid(UUID secundumUuid) {
        this.secundumUuid = secundumUuid;
    }

    /**
     * @param secundumLabel the secundumLabel to set
     */
    public void setSecundumLabel(String secundumLabel) {
        this.secundumLabel = secundumLabel;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(List<EntityDTO<Taxon>> children) {
        this.children = children;
    }


    /**
     * @param markedAncestors the markedAncestors to set
     */
    public void setMarkedAncestors(List<MarkedEntityDTO<Taxon>> markedAncestors) {
        this.markedAncestors = markedAncestors;
    }

    /**
     * @return the taxonNodeUuid
     */
    public UUID getTaxonNodeUuid() {
        return taxonNodeUuid;
    }

    /**
     * @return the taxonUuid
     */
    public UUID getTaxonUuid() {
        return taxonUuid;
    }

    /**
     * @return the nameUuid
     */
    public UUID getNameUuid() {
        return nameUuid;
    }

    /**
     * @return the rankUuid
     */
    public UUID getRankUuid() {
        return rankUuid;
    }

    /**
     * @return the rankLabel
     */
    public String getRankLabel() {
        return rankLabel;
    }

    /**
     * @return the genusOrUninomial
     */
    public String getGenusOrUninomial() {
        return genusOrUninomial;
    }

    /**
     * @return the infragenericEpithet
     */
    public String getInfraGenericEpithet() {
        return infraGenericEpithet;
    }

    /**
     * @return the speciesEpithet
     */
    public String getSpeciesEpithet() {
        return speciesEpithet;
    }

    /**
     * @return the infraspecificEpithet
     */
    public String getInfraSpecificEpithet() {
        return infraSpecificEpithet;
    }

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
