/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import java.util.List;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.media.MediaUtils.MissingValueStrategy;

/**
 * @author a.kohlbecker
 * @since Jul 8, 2020
 */
public interface IMediaToolbox {

    List<Media> processAndFilterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type,
            String[] mimeTypes, Integer widthOrDuration, Integer height, Integer size, List<Media> taxonGalleryMedia);

    List<Media> filterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type, String[] mimeTypes,
            Integer widthOrDuration, Integer height, Integer size, List<Media> taxonGalleryMedia);

    MediaRepresentation processAndFindBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentationPart> type, Integer size, Integer height, Integer widthOrDuration,
            String[] mimeTypes, MissingValueStrategy missingValStrategy);

}