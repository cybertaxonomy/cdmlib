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
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TaxonBaseDto extends IdentifiableDto {

    //TODO should we distinguish data parts (e.g. on general page we do not need last updates from synonymy)
    //lastUpdated
    private String nameLabel;
    private UUID nameUuid;

    private ContainerDto<NameRelationDTO> relatedNames;

    private List<URI> protologues;

    //TODO should we keep formatting client side or should we do formatting on server side? Formatting means: filter, italics, order??
//    private List<TypedLabel> typedTaxonLabel;
//    private List<TypedLabel> typedNameLabel;

    private List<TaggedText> taggedLabel;

    public String getNameLabel() {
        return nameLabel;
    }
    public void setNameLabel(String nameLabel) {
        this.nameLabel = nameLabel;
    }

    public UUID getNameUuid() {
        return nameUuid;
    }
    public void setNameUuid(UUID nameUuid) {
        this.nameUuid = nameUuid;
    }

    //    public List<TypedLabel> getTypedTaxonLabel() {
//        return typedTaxonLabel;
//    }
//    public void setTypedTaxonLabel(List<TypedLabel> typedTaxonLabel) {
//        this.typedTaxonLabel = typedTaxonLabel;
//    }
//
//    public List<TypedLabel> getTypedNameLabel() {
//        return typedNameLabel;
//    }
//    public void setTypedNameLabel(List<TypedLabel> typedNameLabel) {
//        this.typedNameLabel = typedNameLabel;
//    }
    public List<TaggedText> getTaggedLabel() {
        return taggedLabel;
    }
    public void setTaggedLabel(List<TaggedText> taggedLabel) {
        this.taggedLabel = taggedLabel;
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

    //protologues

    public List<URI> getProtologues() {
        return protologues;
    }
    public void addProtologue(URI uri) {
        if (protologues == null) {
            protologues = new ArrayList<>();
        }
        protologues.add(uri);
    }



    //TaxonBase info
    //appendedPhras, useNameCache, doubtful, name, publish
    // => should all be part of the typedLabel

    //secsource  ?? how to handle? part of bibliography

    //TaxonName info
    //TODO do we need
    //rank, nameparts => all in typedLabel


    //relatedNames  //as RelatedDTO?

    //types ?? => Teil der homotypischen Gruppe, außer der Fall von Walter (für  name types?)



}
