/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.UUID;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.ref.TypedEntityReference;

/**
 * @author a.kohlbecker
 * @since Aug 3, 2018
 *
 */
public class MediaDTO extends TypedEntityReference<Media> {

    String uri;

    String title_l10n;

    String mimeType;

    private Integer size;


    /**
     * @param type
     * @param uuid
     */
    public MediaDTO(UUID uuid) {
        super(Media.class, uuid);
    }


    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }


    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }


    /**
     * @return the title_l10n
     */
    public String getTitle_l10n() {
        return title_l10n;
    }


    /**
     * @param title_l10n the title_l10n to set
     */
    public void setTitle_l10n(String title_l10n) {
        this.title_l10n = title_l10n;
    }


    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }


    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }


    /**
     * @return the size
     */
    public Integer getSize() {
        return size;
    }


    /**
     * @param size the size to set
     */
    public void setSize(Integer size) {
        this.size = size;
    }



}
