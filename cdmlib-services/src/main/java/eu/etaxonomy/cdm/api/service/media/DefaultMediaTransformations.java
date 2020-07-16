/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.media;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create default transformations
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

        /*
         * universalViewerThumbnail:
         * crop to fit into a 200 x 147 preview box, the uvfix=1 parameter is used to
         * prevent the universal viewer from corrupting the last query parameter. UV appends a parameter t with
         * question mark character which causes problems for the URI query parser see https://dev.e-taxonomy.eu/redmine/issues/9132#note-8
         */
        String universalViewerThumbnail = "digilib/Scaler/?fn=$1/$2&mo=crop&dw=200&dh=147&uvfix=1";

        MediaUriTransformation tr1 = new MediaUriTransformation();
        tr1.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", dataPortalPreviewImage));
        tr1.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr1.setMimeType("image/jpeg");
        tr1.setWidth(400);
        tr1.setHeight(400);

        MediaUriTransformation tr2 = new MediaUriTransformation();
        tr2.setPathQueryFragment(new SearchReplace("digilib/Scaler/IIIF/([^\\!]+)\\!([^\\/]+)(.*)", universalViewerThumbnail));
        tr2.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr2.setMimeType("image/jpeg");
        tr2.setWidth(200);
        tr2.setHeight(200);

        MediaUriTransformation tr3 = new MediaUriTransformation();
        tr3.setPathQueryFragment(new SearchReplace("digilib/Scaler/\\?fn=([^\\\\/]+)/(\\w+)(.*)", dataPortalPreviewImage));
        tr3.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr3.setMimeType("image/jpeg");
        tr3.setWidth(400);
        tr3.setHeight(400);

        MediaUriTransformation tr4 = new MediaUriTransformation();
        tr4.setPathQueryFragment(new SearchReplace("digilib/Scaler/\\?fn=([^\\\\/]+)/(\\w+)(.*)", universalViewerThumbnail));
        tr4.setHost(new SearchReplace("pictures.bgbm.org", "pictures.bgbm.org")); // host part only used for matching, no replace!
        tr4.setMimeType("image/jpeg");
        tr4.setWidth(200);
        tr4.setHeight(200);

        defaultTransformations.add(tr2);
        defaultTransformations.add(tr1);
        defaultTransformations.add(tr3);
        defaultTransformations.add(tr4);

        return defaultTransformations;
    }



}
