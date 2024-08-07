/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

/**
 * @author a.mueller
 * @date 15.02.2023
 */
public class FactDtoBase extends SourcedDto implements IFactDto {

    private String timeperiod;
    private Integer sortIndex;

    private ContainerDto<MediaDto2> media;

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

    public ContainerDto<MediaDto2> getMedia() {
        return media;
    }
    public void addMedia(MediaDto2 mediaDto2) {
        if(media == null) {
            media = new ContainerDto<>();
        }
        media.addItem(mediaDto2);
    }
    public void setMedia(ContainerDto<MediaDto2> mediaContainer) {
        this.media = mediaContainer;
    }
}