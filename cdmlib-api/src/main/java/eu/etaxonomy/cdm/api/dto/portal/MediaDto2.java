/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaRepresentationDTO;

public class MediaDto2 extends IdentifiableDto{

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
    public void addRepresentation(MediaRepresentationDTO representationDTO) {
        if (this.representations == null) {
            this.representations = new ContainerDto<>();
        }
        this.representations.addItem(representationDTO);
    }

}