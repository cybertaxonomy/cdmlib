/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils;
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

    private List<SourceDTO> sources = new ArrayList<>();

    /**
     * Creates a list of DTOs from the Media entity.
     * For each MediaRepresentationPart a single MediaDTO is being created.
     * TODO this needs to be changed so that it is possible to filter the representations by preferences,
     * see {@link MediaUtils#findBestMatchingRepresentation(Media, Class, Integer, Integer, Integer, String[], eu.etaxonomy.cdm.model.media.MediaUtils.MissingValueStrategy)}
     */
    public static List<MediaDTO> fromEntity(Media entity) {
        List<MediaDTO> dtos = new ArrayList<>();
        entity.getAllTitles(); // initialize all titles!!!
        MediaDTO dto = new MediaDTO(entity.getUuid());
        for (MediaRepresentation rep :entity.getRepresentations()){
            for(MediaRepresentationPart p : rep.getParts()){
                if(p.getUri() != null){
                    dto.setUri(p.getUri().toString());
                    break;
                }
            }
        }
        entity.getSources().stream().forEach(s -> dto.getSources().add(SourceDTO.fromIdentifiableSource(s)));
        if(dto.getUri() != null || !dto.getSources().isEmpty()) {
            dtos.add(dto);
        }
        return dtos;
    }

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

    public List<SourceDTO> getSources() {
        return sources;
    }


}
