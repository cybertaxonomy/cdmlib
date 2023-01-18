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
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;

/**
 * A DTO including all data relevant for a CDM Dataportal taxon page.
 *
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonPageDto extends TaxonBaseDto {

    private HomotypicGroupDTO homotypicSynonyms;

    private ContainerDto<TaxonNodeDTO> taxonNodes;

    private ContainerDto<HomotypicGroupDTO> heterotypicSynonymGroups;

    private ContainerDto<ConceptRelationDTO> conceptRelations;

    private ContainerDto<FeatureDto> factualData;

    private ContainerDto<KeyDTO> keys;

    private ContainerDto<SpecimenDTO> specimens;

    private ContainerDto<MediaDTO> media;

    private List<MessagesDto> messages;

//******************** subclasses *********************************/

    public static class TaxonNodeAgentsRelDTO extends CdmBaseDto{  //only extend to CdmBaseDto to allow usage in ContainerDto
        private String agent;
        private UUID agentUuid;
        //TODO external Link?
        //TODO better use DTOs?
        private String type;
        private UUID typeUuid;

        public String getAgent() {
            return agent;
        }
        public void setAgent(String agent) {
            this.agent = agent;
        }
        public UUID getAgentUuid() {
            return agentUuid;
        }
        public void setAgentUuid(UUID agentUuid) {
            this.agentUuid = agentUuid;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public UUID getTypeUuid() {
            return typeUuid;
        }
        public void setTypeUuid(UUID typeUuid) {
            this.typeUuid = typeUuid;
        }
    }

    public static class TaxonNodeDTO extends CdmBaseDto {
        private UUID classificationUuid;
        private String classificationLabel;
        private String status;
        private String statusNote;
        private ContainerDto<TaxonNodeAgentsRelDTO> agents;


        public UUID getClassificationUuid() {
            return classificationUuid;
        }
        public void setClassificationUuid(UUID classificationUuid) {
            this.classificationUuid = classificationUuid;
        }
        public String getClassificationLabel() {
            return classificationLabel;
        }
        public void setClassificationLabel(String classificationLabel) {
            this.classificationLabel = classificationLabel;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getStatusNote() {
            return statusNote;
        }
        public void setStatusNote(String statusNote) {
            this.statusNote = statusNote;
        }
        public ContainerDto<TaxonNodeAgentsRelDTO> getAgents() {
            return agents;
        }
        public void addAgent(TaxonNodeAgentsRelDTO agent) {
            if (agents == null) {
                agents = new ContainerDto<>();
            }
            agents.addItem(agent);
        }

    }

    public static class HomotypicGroupDTO extends CdmBaseDto{

        private ContainerDto<TaxonBaseDto> synonyms = new ContainerDto<>();   //Synonym has no relevant extra information therefore no more specific DTOS

        private List<TypedLabel> typedTypes;
        private String types;

        public void addSynonym(TaxonBaseDto synonymDto) {
            synonyms.addItem(synonymDto);
        }
        public ContainerDto<TaxonBaseDto> getSynonyms() {
            return synonyms;
        }
        public List<TypedLabel> getTypedTypes() {
            return typedTypes;
        }
        public void setTypedTypes(List<TypedLabel> typedTypes) {
            this.typedTypes = typedTypes;
        }
        public String getTypes() {
            return types;
        }
        public void setTypes(String types) {
            this.types = types;
        }
    }

    public static class ConceptRelationDTO extends TaxonBaseDto{
        //TODO really needed or only in linkedLabel?
        int relTaxonId;
        UUID relTaxonUuid;
        String relTaxonLabel;
        String label;
        List<TypedLabel> typedLabel;
        public int getRelTaxonId() {
            return relTaxonId;
        }
        public void setRelTaxonId(int relTaxonId) {
            this.relTaxonId = relTaxonId;
        }
        public UUID getRelTaxonUuid() {
            return relTaxonUuid;
        }
        public void setRelTaxonUuid(UUID relTaxonUuid) {
            this.relTaxonUuid = relTaxonUuid;
        }
        public String getRelTaxonLabel() {
            return relTaxonLabel;
        }
        public void setRelTaxonLabel(String relTaxonLabel) {
            this.relTaxonLabel = relTaxonLabel;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public List<TypedLabel> getTypedLabel() {
            return typedLabel;
        }
        public void setTypedLabel(List<TypedLabel> typedLabel) {
            this.typedLabel = typedLabel;
        }
    }

    public static class SpecimenDTO extends LabeledEntityDto {

    }

    public class FactualDataDTO extends CdmBaseDto{

    }

    public static class KeyDTO extends CdmBaseDto{

    }

    public static class MediaDTO extends LabeledEntityDto{
        private String description;
        private ContainerDto<MediaRepresentationDTO> representations;

        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public ContainerDto<MediaRepresentationDTO> getRepresentations() {
            return representations;
        }
        public void setRepresentations(ContainerDto<MediaRepresentationDTO> representations) {
            this.representations = representations;
        }
    }

    public static class MediaRepresentationDTO extends CdmBaseDto{

        private Class<? extends MediaRepresentationPart> clazz;
        private String mimeType;
        private String suffix;  //TODO needed?
        private URI uri;
        private Integer size;
        private int height;
        private int width;

        public Class<? extends MediaRepresentationPart> getClazz() {
            return clazz;
        }
        public void setClazz(Class<? extends MediaRepresentationPart> clazz) {
            this.clazz = clazz;
        }
        public String getMimeType() {
            return mimeType;
        }
        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
        public String getSuffix() {
            return suffix;
        }
        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
        public URI getUri() {
            return uri;
        }
        public void setUri(URI uri) {
            this.uri = uri;
        }
        public Integer getSize() {
            return size;
        }
        public void setSize(Integer size) {
            this.size = size;
        }
        public int getHeight() {
            return height;
        }
        public void setHeight(int height) {
            this.height = height;
        }
        public int getWidth() {
            return width;
        }
        public void setWidth(int width) {
            this.width = width;
        }
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

    public ContainerDto<HomotypicGroupDTO> getHeterotypicSynonymGroups() {
        return heterotypicSynonymGroups;
    }
    public void setHeterotypicSynonymGroups(ContainerDto<HomotypicGroupDTO> heterotypicSynonymGroups) {
        this.heterotypicSynonymGroups = heterotypicSynonymGroups;
    }

    public ContainerDto<FeatureDto> getFactualData() {
        return factualData;
    }
    public void setFactualData(ContainerDto<FeatureDto> factualData) {
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

    public ContainerDto<ConceptRelationDTO> getConceptRelations() {
        return conceptRelations;
    }
    public void setConceptRelations(ContainerDto<ConceptRelationDTO> conceptRelations) {
        this.conceptRelations = conceptRelations;
    }
    public List<MessagesDto> getMessages() {
        return messages;
    }
    public void setMessages(List<MessagesDto> messages) {
        this.messages = messages;
    }
}