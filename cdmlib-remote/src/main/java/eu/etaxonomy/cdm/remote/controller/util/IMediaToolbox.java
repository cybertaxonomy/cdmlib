/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.util;

import java.io.IOException;
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

    /**
     * Extend the set of media representations in <code>media</code>, filter the resulting representations by the
     * attributes defined via the parameters <code>type</code>, <code>size</code>, <code>height</code>, <code>widthOrDuration</code>
     * and <code>mimeTypes</code> and finally return the list of matching MediaRepresentations ordered by the ranging of the match.
     *
     */
    public List<Media> processAndFilterPreferredMediaRepresentations(Class<? extends MediaRepresentationPart> type,
            String[] mimeTypes, Integer widthOrDuration, Integer height, Integer size, List<Media> taxonGalleryMedia) throws IOException;

    /**
     * Filters the Media  objects and the contained MediaRepresentations by the
     * attributes defined via the parameters <code>type</code>, <code>size</code>, <code>height</code>, <code>widthOrDuration</code>
     * and <code>mimeTypes</code> and finally return the media objects which have at lease one matching representation.
     * The MediaRepresentations are also filtered and ordered by the ranging of the match.
     *
     * @deprecated needs to be replaced, see https://dev.e-taxonomy.eu/redmine/issues/9160
     */
    @Deprecated
    public List<Media> filterPreferredMediaRepresentations(List<Media> mediaList, Class<? extends MediaRepresentationPart> type,
            String[] mimeTypes, Integer widthOrDuration, Integer height, Integer size);

    /**
     * Extend the set of media representations in <code>media</code>, filter the resulting representations by the
     * attributes defined via the parameters <code>type</code>, <code>size</code>, <code>height</code>, <code>widthOrDuration</code>
     * and <code>mimeTypes</code> and finally return the best matching MediaRepresentation.
     *
     * @param media
     * @param type
     * @param size
     * @param height
     * @param widthOrDuration
     * @param mimeTypes
     * @param missingValStrategy Strategies for replacing <code>null</code> values with a numeric value.
     * @return
     * @throws IOException
     */
    public MediaRepresentation processAndFindBestMatchingRepresentation(Media media,
            Class<? extends MediaRepresentationPart> type, Integer size, Integer height, Integer widthOrDuration,
            String[] mimeTypes, MissingValueStrategy missingValStrategy) throws IOException;

}