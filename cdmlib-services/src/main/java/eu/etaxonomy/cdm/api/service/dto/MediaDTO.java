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

    private static final long serialVersionUID = 1981292478312137355L;

    private String uri;

    private String title_L10n;

    private String mimeType;

    private Integer size;


    /**
     * @param type
     * @param uuid
     */
    public MediaDTO(UUID uuid) {
        super(Media.class, uuid);
    }

    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTitle_l10n() {
        return title_L10n;
    }
    public void setTitle_l10n(String title_l10n) {
        this.title_L10n = title_l10n;
    }

    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }

}
