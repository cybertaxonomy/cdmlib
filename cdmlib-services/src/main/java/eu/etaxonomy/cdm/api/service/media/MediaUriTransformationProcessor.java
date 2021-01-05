/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import java.awt.Point;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.metadata.PreferencePredicate;

/**
 * Creates new purely volatile {@link MediaRepresentation MediaRepresentations} objects based on
 * a list of {@link MediaUriTransformation} rules. These rules are usually stored in the
 * per data base {@link PreferencePredicate.MediaRepresentationTransformations MediaRepresentationTransformations} property
 * (See also {@link MediaToolbox#readTransformations()}).
 * <p>
 * These volatile {@link MediaRepresentation MediaRepresentations} objects must not be persisted!
 *
 * @author a.kohlbecker
 * @since Jul 8, 2020
 */
public class MediaUriTransformationProcessor {

    private static final Logger logger = Logger.getLogger(MediaUriTransformationProcessor.class);

    private List<MediaUriTransformation> transformations = new ArrayList<>();

    public void add(MediaUriTransformation transformation) {
        transformations.add(transformation);
    }

    public void addAll(Collection<MediaUriTransformation> trans) {
        transformations.addAll(trans);
    }

    public List<URI> applyTo(URI uri) {

        logger.debug("original:    " + uri.toString());
        String pathQueryFragment = buildPathQueryFragment(uri);

        List<URI> newUris = new ArrayList<>();
        for (MediaUriTransformation transformation : transformations) {

            try {
                URI newUri = uriTransformation(uri, pathQueryFragment, transformation);
                newUris.add(newUri);
            } catch (URISyntaxException e) {
                logger.error(e);
            }
        }

        return newUris;
    }

    protected URI uriTransformation(URI uri, String pathQueryFragment, MediaUriTransformation replacement)
            throws URISyntaxException {

        String newScheme = uri.getScheme();
        String newHost = uri.getHost();
        int newPort = uri.getPort();
        String newPathQueryFragment = pathQueryFragment;

        boolean isMatch = true;
        // replace the parts
        if (replacement.getScheme() != null) {
            Matcher m = replacement.getScheme().searchPattern().matcher(newScheme);
            isMatch &= m.find();
            newScheme = m.replaceAll(replacement.getScheme().getReplace());
        }
        if (replacement.getHost() != null) {
            Matcher m = replacement.getHost().searchPattern().matcher(newHost);
            isMatch &= m.find();
            newHost = m.replaceAll(replacement.getHost().getReplace());
        }
        // TODO port

        if (replacement.getPathQueryFragment() != null) {
            Matcher m = replacement.getPathQueryFragment().searchPattern().matcher(newPathQueryFragment);
            isMatch &= m.find();
            newPathQueryFragment = m.replaceAll(replacement.getPathQueryFragment().getReplace());
        }
        if (isMatch) {
            // recombine
            String newURIString = newScheme + "://" + newHost + (newPort > 0 ? ":" + String.valueOf(newPort) : "")
                    + newPathQueryFragment;

            URI newUri = new URI(newURIString);
            logger.debug("transformed: " + newUri.toString());
            return newUri;
        } else {
            return uri;
        }
    }

    protected String buildPathQueryFragment(URI uri) {
        String pathQueryFragment = uri.getPath();
        if (uri.getQuery() != null) {
            pathQueryFragment += "?" + uri.getQuery();
        }
        if (uri.getFragment() != null) {
            pathQueryFragment += "#" + uri.getFragment();
        }
        return pathQueryFragment;
    }

    @Deprecated
    public List<MediaRepresentation> makeNewMediaRepresentationsFor(URI uri) {

        List<MediaRepresentation> repr = new ArrayList<>();

        String pathQueryFragment = buildPathQueryFragment(uri);

        for (MediaUriTransformation transformation : transformations) {

            try {
                URI newUri = uriTransformation(uri, pathQueryFragment, transformation);
                MediaRepresentation mRepresentation = MediaRepresentation.NewInstance(transformation.getMimeType(),
                        null);
                MediaRepresentationPart part;
                if (transformation.getMimeType() != null && transformation.getMimeType().startsWith("image/")) {
                    part = ImageFile.NewInstance(newUri, null, transformation.getHeight(), transformation.getWidth());
                } else {
                    part = MediaRepresentationPart.NewInstance(newUri, null);
                }
                mRepresentation.addRepresentationPart(part);
                repr.add(mRepresentation);

            } catch (URISyntaxException e) {
                logger.error(e);
            }
        }

        return repr;
    }

    public List<MediaRepresentation> makeNewMediaRepresentationsFor(MediaRepresentationPart part) {

        List<MediaRepresentation> repr = new ArrayList<>();

        String pathQueryFragment = buildPathQueryFragment(part.getUri());

        for (MediaUriTransformation transformation : transformations) {

            try {
                URI newUri = uriTransformation(part.getUri(), pathQueryFragment, transformation);
                if (newUri.equals(part.getUri())) {
                    // transformation did not apply
                    continue;
                }
                MediaRepresentation mRepresentation = MediaRepresentation.NewInstance(transformation.getMimeType(),
                        null);
                MediaRepresentationPart newPart;
                if (transformation.getMimeType() != null && transformation.getMimeType().startsWith("image/")) {
                    if (part instanceof ImageFile) {
                        ImageFile originalImageFile = (ImageFile) part;
                        Point newSize = calculateTargetSize(transformation, originalImageFile.getWidth(),
                                originalImageFile.getHeight());
                        newPart = ImageFile.NewInstance(newUri, null, newSize.y, newSize.x);
                    } else {
                        newPart = ImageFile.NewInstance(newUri, null, transformation.getHeight(),
                                transformation.getWidth());
                    }
                } else {
                    newPart = MediaRepresentationPart.NewInstance(newUri, null);
                }
                mRepresentation.addRepresentationPart(newPart);
                repr.add(mRepresentation);

            } catch (URISyntaxException e) {
                logger.error(e);
            }
        }

        return repr;
    }

    /**
     *
     * @param trans
     *   The transfomation
     * @param originalWidth
     * @param originalHeight
     * @param calculateMaxExtend
     * @return
     */
    protected Point calculateTargetSize(MediaUriTransformation trans, Integer originalWidth, Integer originalHeight) {

        if (trans.getWidth() == null && trans.getHeight() == null) {
            return null;
        } else if (originalWidth == null || originalHeight == null) {
            return new Point(trans.getWidth(), trans.getHeight());
        } else {
            if(trans.getHeight() != null && trans.getWidth() != null && !trans.isMaxExtend()) {
                // CROP
                return new Point(trans.getWidth(), trans.getHeight());
            } else {
                // MAX EXTEND
                float originalAspectRatio = ((float) originalWidth / (float) originalHeight);

                boolean widthIsLimiting = trans.getHeight() == null ||
                        trans.getWidth() != null && trans.getHeight() * originalAspectRatio > trans.getWidth();
                if (widthIsLimiting){
                    return new Point(trans.getWidth(), Math.round(trans.getWidth() / originalAspectRatio ));
                } else {
                    return new Point(Math.round(trans.getHeight() * originalAspectRatio), trans.getHeight());
                }
            }
        }
    }
}