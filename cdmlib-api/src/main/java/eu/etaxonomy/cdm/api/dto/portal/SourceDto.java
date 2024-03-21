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

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.format.common.TypedLabel;
import eu.etaxonomy.cdm.strategy.cache.TaggedText;

/**
 * @author a.mueller
 * @date 19.01.2023
 */
public class SourceDto extends CdmBaseDto {  //but could be annotatable

    private List<TypedLabel> label = new ArrayList<>();
    private UUID linkedUuid;
    private String linkedClass;
    private String originalInfo;
    private List<TaggedText> nameInSource;
    private UUID nameInSourceUuid;
    private UUID referenceUuid;
    private String doi;
    //TODO maybe we should merge doi and uri
    private URI uri;
    private String type;
    //TODO external links
    private String accessed;

    private List<URI> links;

// ******************** CONSTRUCTOR ****************/

    public SourceDto() {
        super();
    }

    public SourceDto(int id) {
        super(null, id, null);
    }

//************* GETTER/SETTER ***********************/

    public List<TypedLabel> getLabel() {
        return label;
    }

    public void addLabel(TypedLabel label) {
        if (this.label == null) {
            this.label = new ArrayList<>();
        }
        this.label.add(label);
    }

    public UUID getLinkedUuid() {
        return linkedUuid;
    }
    public void setLinkedUuid(UUID linkedUuid) {
        this.linkedUuid = linkedUuid;
    }

    public List<TaggedText> getNameInSource() {
        return nameInSource;
    }
    public void setNameInSource(List<TaggedText> nameInSource) {
        this.nameInSource = nameInSource;
    }

    public UUID getNameInSourceUuid() {
        return nameInSourceUuid;
    }
    public void setNameInSourceUuid(UUID nameInSourceUuid) {
        this.nameInSourceUuid = nameInSourceUuid;
    }

    public String getDoi() {
        return doi;
    }
    public void setDoi(String doi) {
        this.doi = doi;
    }

    public URI getUri() {
        return uri;
    }
    public void setUri(URI uri) {
        this.uri = uri;
    }
    public List<URI> getLinks() {
        return links;
    }
    public void addLink(URI uri) {
        if (links == null) {
            links = new ArrayList<>();
        }
        links.add(uri);
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getLinkedClass() {
        return linkedClass;
    }
    public void setLinkedClass(String linkedClass) {
        this.linkedClass = linkedClass;
    }

    public UUID getReferenceUuid() {
        return referenceUuid;
    }
    public void setReferenceUuid(UUID referenceUuid) {
        this.referenceUuid = referenceUuid;
    }

    public String getOriginalInfo() {
        return originalInfo;
    }
    public void setOriginalInfo(String originalInfo) {
        this.originalInfo = originalInfo;
    }

    public String getAccessed() {
        return accessed;
    }
    public void setAccessed(String accessed) {
        this.accessed = accessed;
    }
}