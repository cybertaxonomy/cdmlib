/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import eu.etaxonomy.cdm.api.dto.portal.TaxonPageDto.MediaDTO;

/**
 * @author a.mueller
 * @date 15.02.2023
 */
public class FactDtoBase extends SourcedDto implements IFactDto {

    private String timeperiod;
    private Integer sortIndex;

    private ContainerDto<MediaDTO> media;

    @Override
    public String getClazz() {
        return getClass().getSimpleName();
    }

    public String getTimeperiod() {
        return timeperiod;
    }
    public void setTimeperiod(String timeperiod) {
        this.timeperiod = timeperiod;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }
    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public ContainerDto<MediaDTO> getMedia() {
        return media;
    }
    public void setMedia(ContainerDto<MediaDTO> mediaContainer) {
        this.media = mediaContainer;
    }
}