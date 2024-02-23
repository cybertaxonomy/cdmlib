/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.SpecimenOrObservationBaseDTO;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

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

    private ContainerDto<FeatureDto> taxonFacts;

    private ContainerDto<KeyDTO> keys;

    //for now this is transient and therefore not visible in the webservice
    private ContainerDto<SpecimenDTO> specimens;

    private List<SpecimenOrObservationBaseDTO> rootSpecimens;

    private ContainerDto<MediaDTO> media;

    private String secTitleCache;

    private List<MessagesDto> messages;

//******************** subclasses *********************************/

    public static class TaxonNodeAgentsRelDTO extends CdmBaseDto{  //only extend to CdmBaseDto to allow usage in ContainerDto

        private String agent;
        private UUID agentUuid;
        //TODO should it be a list?
        private String agentLink;  //preferred external link
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
        public String getAgentLink() {
            return agentLink;
        }
        public void setAgentLink(String agentLink) {
            this.agentLink = agentLink;
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
        private List<TaggedText> taggedTypes;
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
        public List<TaggedText> getTaggedTypes() {
            return taggedTypes;
        }
        public void setTaggedTypes(List<TaggedText> taggedTypes) {
            this.taggedTypes = taggedTypes;
        }
    }

    public static class NameRelationDTO extends SingleSourcedDto {
        private List<TaggedText> nameLabel;
        private UUID nameUuid;
        private String relType;
        private UUID relTypeUuid;
        private boolean inverse;
        private String ruleConsidered;
        //TODO relatedTaxon in this classification

        public List<TaggedText> getNameLabel() {
            return nameLabel;
        }
        public void setNameLabel(List<TaggedText> nameLabel) {
            this.nameLabel = nameLabel;
        }
        public UUID getNameUuid() {
            return nameUuid;
        }
        public void setNameUuid(UUID nameUuid) {
            this.nameUuid = nameUuid;
        }
        public String getRelType() {
            return relType;
        }
        public void setRelType(String relType) {
            this.relType = relType;
        }
        public UUID getRelTypeUuid() {
            return relTypeUuid;
        }
        public void setRelTypeUuid(UUID relTypeUuid) {
            this.relTypeUuid = relTypeUuid;
        }
        public boolean isInverse() {
            return inverse;
        }
        public void setInverse(boolean inverse) {
            this.inverse = inverse;
        }
        public String getRuleConsidered() {
            return ruleConsidered;
        }
        public void setRuleConsidered(String ruleConsidered) {
            this.ruleConsidered = ruleConsidered;
        }
    }

    public static class ConceptRelationDTO extends TaxonBaseDto{

        //TODO really needed or only in linkedLabel?
        private int relTaxonId;
        private UUID relTaxonUuid;
        private String relTaxonLabel;
        private List<TypedLabel> typedLabel;
        private UUID relTypeUuid;
        private Set<UUID> classificationUuids;
        private SourceDto secSource;
        private SourceDto relSource;

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
        public List<TypedLabel> getTypedLabel() {
            return typedLabel;
        }
        public void setTypedLabel(List<TypedLabel> typedLabel) {
            this.typedLabel = typedLabel;
        }
        public UUID getRelTypeUuid() {
            return relTypeUuid;
        }
        public void setRelTypeUuid(UUID relTypeUuid) {
            this.relTypeUuid = relTypeUuid;
        }
        public Set<UUID> getClassificationUuids() {
            return classificationUuids;
        }
        public void addClassificationUuids(UUID classificationUuid) {
            if (this.classificationUuids == null) {
                this.classificationUuids = new HashSet<>();
            }
            this.classificationUuids.add(classificationUuid);
        }
        public SourceDto getSecSource() {
            return secSource;
        }
        public void setSecSource(SourceDto secSource) {
            this.secSource = secSource;
        }
        public SourceDto getRelSource() {
            return relSource;
        }
        public void setRelSource(SourceDto relSource) {
            this.relSource = relSource;
        }
    }

    public static class SpecimenDTO extends IdentifiableDto {

    }

    public static class KeyDTO extends IdentifiableDto{
        private String keyClass;

        public String getKeyClass() {
            return keyClass;
        }
        public void setKeyClass(String keyClass) {
            this.keyClass = keyClass;
        }
    }

    public static class MediaDTO extends IdentifiableDto{

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

    public ContainerDto<FeatureDto> getTaxonFacts() {
        return taxonFacts;
    }
    public void setTaxonFacts(ContainerDto<FeatureDto> taxonFacts) {
        this.taxonFacts = taxonFacts;
    }

    public ContainerDto<KeyDTO> getKeys() {
        return keys;
    }
    public void setKeys(ContainerDto<KeyDTO> keys) {
        this.keys = keys;
    }

    //for now this is transient and therefore not visible in the webservice
    @Transient
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
    public void addMessage(MessagesDto message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    public String getSecTitleCache() {
        return secTitleCache;
    }
    public void setSecTitleCache(String secTitleCache) {
        this.secTitleCache = secTitleCache;
    }

    public List<SpecimenOrObservationBaseDTO> getRootSpecimens() {
        return rootSpecimens;
    }
    public void setRootSpecimens(List<SpecimenOrObservationBaseDTO> rootSpecimens) {
        this.rootSpecimens = rootSpecimens;
    }
}