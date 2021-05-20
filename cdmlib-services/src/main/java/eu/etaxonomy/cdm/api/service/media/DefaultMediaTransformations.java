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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Factory class to create default media transformations
 *
 * @author a.kohlbecker
 * @since Jul 15, 2020
 */
public class DefaultMediaTransformations {

    /**
     * Create default transformations for the diglilib server:
     * <p>
     * Links:
     * <ul>
     * <li>https://robcast.github.io/digilib/scaler-api.html</li>
     * <li>https://robcast.github.io/digilib/iiif-api.html</li>
     * </ul>
     *
     * @return
     */
    static public List<MediaUriTransformation> digilib() {

        List<MediaUriTransformation> defaultTransformations = new ArrayList<>();

        /*
         * dataPortalPreviewImage:
         * image which fits the default preview image size which is
         * for example used in the taxon general page, max extend of the resulting images is 400px
         */
        String dataPortalPreviewImage = "digilib/Scaler/IIIF/$1!$2/full/!400,400/0/default.jpg";
        Point dataPortalPreviewImageSize = new Point(400,400);

        /*
         * universalViewerThumbnail:
         * crop to fit into a 200 x 147 preview box, the uvfix=1 parameter is used to
         * prevent the universal viewer from corrupting the last query parameter. UV appends a parameter t with
         * question mark character which causes problems for the URI query parser see https://dev.e-taxonomy.eu/redmine/issues/9132#note-8
         */
        String universalViewerThumbnail = "digilib/Scaler/?fn=$1/$2&mo=crop&dw=200&dh=147&uvfix=1";
        Point universalViewerThumbnailSize = new Point(200,147);

        MediaUriTransformation tr1 = new MediaUriTransformation();
        tr1.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", dataPortalPreviewImage));
        tr1.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr1.setMimeType("image/jpeg");
        tr1.setWidth(dataPortalPreviewImageSize.x);
        tr1.setHeight(dataPortalPreviewImageSize.y);
        tr1.setMaxExtend(true);

        MediaUriTransformation tr2 = new MediaUriTransformation();
        tr2.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", universalViewerThumbnail));
        tr2.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr2.setMimeType("image/jpeg");
        tr2.setWidth(universalViewerThumbnailSize.x);
        tr2.setHeight(universalViewerThumbnailSize.y);
        tr2.setMaxExtend(false);

        MediaUriTransformation tr3 = new MediaUriTransformation();
        tr3.setPathQueryFragment(new SearchReplace("digilib/Scaler/?\\?fn=([^\\\\/]+)/(\\w+)(.*)", dataPortalPreviewImage));
        tr3.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr3.setMimeType("image/jpeg");
        tr3.setWidth(dataPortalPreviewImageSize.x);
        tr3.setHeight(dataPortalPreviewImageSize.y);
        tr3.setMaxExtend(true);

        MediaUriTransformation tr4 = new MediaUriTransformation();
        tr4.setPathQueryFragment(new SearchReplace("digilib/Scaler/?\\?fn=([^\\\\/]+)/(\\w+)(.*)", universalViewerThumbnail));
        tr4.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr4.setMimeType("image/jpeg");
        tr4.setWidth(universalViewerThumbnailSize.x);
        tr4.setHeight(universalViewerThumbnailSize.y);
        tr4.setMaxExtend(false);

        defaultTransformations.add(tr2);
        defaultTransformations.add(tr1);
        defaultTransformations.add(tr3);
        defaultTransformations.add(tr4);

        return defaultTransformations;
    }

    /**
     * Transforms a BGBM digilib image server URI like
     *
     * https://pictures.bgbm.org/digilib/Scaler?fn=Cyprus/Sisymbrium_aegyptiacum_C1.jpg&mo=file
     *
     * to a <a href="https://github.com/cybertaxonomy/MediaInfoService">MediaInfoService</a> URI:
     *
     * https://image.bgbm.org/metadata/info?file=Cyprus/Sisymbrium_aegyptiacum_C1.jpg
     */
    static public Collection<MediaUriTransformation> bgbmMediaMetadataService() {
        MediaUriTransformation mutScalerAPI = new MediaUriTransformation();
        mutScalerAPI.setHost(new SearchReplace("pictures.bgbm.org", "image.bgbm.org"));
        mutScalerAPI.setPathQueryFragment(new SearchReplace("digilib\\/Scaler\\?fn=([^&]+)&mo=file", "metadata/info?file=$1"));

        // https://pictures.bgbm.org/digilib/Scaler/IIIF/Cichorieae!Lactuca_serriola_Bc_08.jpg/full/full/0/default.jpg
        MediaUriTransformation mutIIIFAPI_1segment = new MediaUriTransformation();
        mutIIIFAPI_1segment.setHost(new SearchReplace("pictures.bgbm.org", "image.bgbm.org"));
        mutIIIFAPI_1segment.setPathQueryFragment(new SearchReplace("digilib\\/Scaler\\/IIIF\\/([^\\/!]+)\\/full/.*", "metadata/info?file=$1"));

        MediaUriTransformation mutIIIFAPI_2segments = new MediaUriTransformation();
        mutIIIFAPI_2segments.setHost(new SearchReplace("pictures.bgbm.org", "image.bgbm.org"));
        mutIIIFAPI_2segments.setPathQueryFragment(new SearchReplace("digilib\\/Scaler\\/IIIF\\/([^\\/!]+)!([^\\/!]+)\\/full/.*", "metadata/info?file=$1/$2"));

        MediaUriTransformation mutIIIFAPI_3segments = new MediaUriTransformation();
        mutIIIFAPI_3segments.setHost(new SearchReplace("pictures.bgbm.org", "image.bgbm.org"));
        mutIIIFAPI_3segments.setPathQueryFragment(new SearchReplace("digilib\\/Scaler\\/IIIF\\/([^\\/!]+)!([^\\/!]+)!([^\\/!]+)\\/full/.*", "metadata/info?file=$1/$2/$3"));
        return Arrays.asList(mutScalerAPI, mutIIIFAPI_1segment, mutIIIFAPI_2segments, mutIIIFAPI_3segments);
    }
}
