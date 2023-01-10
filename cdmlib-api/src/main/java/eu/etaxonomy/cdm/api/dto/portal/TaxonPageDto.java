/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.List;

/**
 * A DTO including all data relevant for a CDM Dataportal taxon page.
 *
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDto extends TaxonBaseDto {

    private HomotypicGroupDTO homotypicSynonyms;

    private ContainerDto<TaxonNodeDTO> taxonNodes;

    private ContainerDto<HomotypicGroupDTO> heterotypicSynonyms;

    private ContainerDto<FactualDataDTO> factualData;

    private ContainerDto<KeyDTO> keys;

    private ContainerDto<SpecimenDTO> specimens;

    private ContainerDto<MediaDTO> media;

    private List<MessagesDto> messages;

//******************** subclasses *********************************/

    public class TaxonNodeDTO extends CdmBaseDto {

    }

    public class HomotypicGroupDTO  extends CdmBaseDto{

        ContainerDto<TaxonBaseDto> synonyms;   //Synonym has no relevant extra information therefore no more specific DTOS

        //TODO
        //typification   //see also TypeDesignationWorkingSet implementation

    }

    public class SpecimenDTO extends CdmBaseDto{

    }

    public class FactualDataDTO extends CdmBaseDto{

    }

    public class KeyDTO extends CdmBaseDto{

    }

    public class MediaDTO extends CdmBaseDto{

    }

    public HomotypicGroupDTO getHomotypicSynonyms() {
        return homotypicSynonyms;
    }
    public void setHomotypicSynonyms(HomotypicGroupDTO homotypicSynonyms) {
        this.homotypicSynonyms = homotypicSynonyms;
    }

    public ContainerDto<TaxonNodeDTO> getTaxonNodes() {
        return taxonNodes;
    }
    public void setTaxonNodes(ContainerDto<TaxonNodeDTO> taxonNodes) {
        this.taxonNodes = taxonNodes;
    }

    public ContainerDto<HomotypicGroupDTO> getHeterotypicSynonyms() {
        return heterotypicSynonyms;
    }
    public void setHeterotypicSynonyms(ContainerDto<HomotypicGroupDTO> heterotypicSynonyms) {
        this.heterotypicSynonyms = heterotypicSynonyms;
    }

    public ContainerDto<FactualDataDTO> getFactualData() {
        return factualData;
    }
    public void setFactualData(ContainerDto<FactualDataDTO> factualData) {
        this.factualData = factualData;
    }

    public ContainerDto<KeyDTO> getKeys() {
        return keys;
    }
    public void setKeys(ContainerDto<KeyDTO> keys) {
        this.keys = keys;
    }

    public ContainerDto<SpecimenDTO> getSpecimens() {
        return specimens;
    }
    public void setSpecimens(ContainerDto<SpecimenDTO> specimens) {
        this.specimens = specimens;
    }

    public ContainerDto<MediaDTO> getMedia() {
        return media;
    }
    public void setMedia(ContainerDto<MediaDTO> media) {
        this.media = media;
    }

    public List<MessagesDto> getMessages() {
        return messages;
    }
    public void setMessages(List<MessagesDto> messages) {
        this.messages = messages;
    }





}
