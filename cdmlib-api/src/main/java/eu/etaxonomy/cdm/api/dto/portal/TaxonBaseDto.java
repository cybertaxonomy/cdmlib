/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NameRelationDTO;
import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.NomenclaturalStatusDTO;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonBaseDto extends IdentifiableDto {

    public class TaxonNameDto extends IdentifiableDto {

        //nomenclatural code
        private String nameType;

        private ContainerDto<NomenclaturalStatusDTO> status;

        private ContainerDto<NameRelationDTO> relatedNames;

        private List<URI> protologues;

        private ContainerDto<FeatureDto> nameFacts;

        //TODO maybe later this can be combined with taggedLabel (merge taxon and name taggedText)
        private List<TaggedText> taggedName;

        private boolean hasRegistration;

        private boolean isInvalid;

        //*********** GETTER / SETTER *******************/

        public List<TaggedText> getTaggedName() {
            return taggedName;
        }
        public void setTaggedName(List<TaggedText> taggedName) {
            this.taggedName = taggedName;
        }

        public ContainerDto<FeatureDto> getNameFacts() {
            return nameFacts;
        }
        public void setNameFacts(ContainerDto<FeatureDto> nameFacts) {
            this.nameFacts = nameFacts;
        }

        public List<URI> getProtologues() {
            return protologues;
        }
        public void addProtologue(URI uri) {
            if (protologues == null) {
                protologues = new ArrayList<>();
            }
            protologues.add(uri);
        }

        public ContainerDto<NameRelationDTO> getRelatedNames() {
            return relatedNames;
        }
        public void addRelatedName(NameRelationDTO relatedName) {
            if (this.relatedNames == null) {
                this.relatedNames = new ContainerDto<>();
            }
            this.relatedNames.addItem(relatedName);
        }
        //TODO either set or add
        public void setRelatedNames(ContainerDto<NameRelationDTO> relatedNames) {
            this.relatedNames = relatedNames;
        }


        public ContainerDto<NomenclaturalStatusDTO> getStatus(){
            return status;
        }
        public void addStatus(NomenclaturalStatusDTO nomenclaturalStatus) {
            if (this.status == null) {
                this.status = new ContainerDto<>();
            }
            this.status.addItem(nomenclaturalStatus);
        }
        //TODO either set or add
        public void setStatus(ContainerDto<NomenclaturalStatusDTO> nomenclaturalStatuss) {
            this.status = nomenclaturalStatuss;
        }

        public String getType() {
            return nameType;
        }
        public void setType(String nameType) {
            this.nameType = nameType;
        }

        public boolean isInvalid() {
            return isInvalid;
        }
        public void setInvalid(boolean isInvalid) {
            this.isInvalid = isInvalid;
        }

        public boolean isHasRegistration() {
            return hasRegistration;
        }
        public void setHasRegistration(boolean hasRegistration) {
            this.hasRegistration = hasRegistration;
        }
    }

    private TaxonNameDto name;

    //TODO should we distinguish data parts (e.g. on general page we do not need last updates from synonymy)
    //lastUpdated


    //TODO should we keep formatting client side or should we do formatting on server side? Formatting means: filter, italics, order??
//    private List<TypedLabel> typedTaxonLabel;
//    private List<TypedLabel> typedNameLabel;

    private List<TaggedText> taggedLabel;

    public String getNameLabel() {
        return getName().getLabel();
    }
    public void setNameLabel(String nameLabel) {
        getName().setLabel(nameLabel);
    }

    public UUID getNameUuid() {
        return getName().getUuid();
    }
    public void setNameUuid(UUID nameUuid) {
        getName().setUuid(nameUuid);
    }

    public String getNameType() {
        return getName().getType();
    }
    public void setNameType(String nameType) {
        getName().setType(nameType);
    }

    public List<TaggedText> getTaggedLabel() {
        return taggedLabel;
    }
    public void setTaggedLabel(List<TaggedText> taggedLabel) {
        this.taggedLabel = taggedLabel;
    }

    public List<TaggedText> getTaggedName() {
        return getName().getTaggedName();
    }
    public void setTaggedName(List<TaggedText> taggedName) {
        getName().setTaggedName(taggedName);
    }

    public ContainerDto<NameRelationDTO> getRelatedNames() {
        return getName().getRelatedNames();
    }
    public void addRelatedName(NameRelationDTO relatedName) {
        getName().addRelatedName(relatedName);
    }
    //TODO either set or add
    public void setRelatedNames(ContainerDto<NameRelationDTO> relatedNames) {
        getName().setRelatedNames(relatedNames);
    }

    public ContainerDto<NomenclaturalStatusDTO> getNomenclaturalStatus() {
        return getName().getStatus();
    }
    public void addNomenclaturalStatus(NomenclaturalStatusDTO status) {
        getName().addStatus(status);
    }
    //TODO either set or add
    public void setNomenclaturalStatus(ContainerDto<NomenclaturalStatusDTO> status) {
        getName().setStatus(status);
    }

    public ContainerDto<FeatureDto> getNameFacts() {
        return getName().getNameFacts();
    }
    public void setNameFacts(ContainerDto<FeatureDto> nameFacts) {
        getName().setNameFacts(nameFacts);
    }

    //protologues

    public List<URI> getProtologues() {
        return getName().getProtologues();
    }
    public void addProtologue(URI uri) {
        getName().addProtologue(uri);
    }

    public TaxonNameDto getName() {
        if (name == null) {
            name = new TaxonNameDto();
        }
        return name;
    }
    public void setName(TaxonNameDto name) {
        this.name = name;
    }


    //TaxonBase info
    //appendedPhras, useNameCache, doubtful, name, publish
    // => should all be part of the typedLabel

    //secsource  ?? how to handle? part of bibliography

    //TaxonName info
    //rank, nameparts => all in typedLabel

    //types ?? => Teil der homotypischen Gruppe, außer der Fall von Walter (für  name types?)

}
