/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;

/**
 * @author pplitzner
 * @since Nov 30, 2015
 *
 */
public class MediaSpecimenFormatter extends DerivedUnitFormatter {

    public MediaSpecimenFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    protected void initFormatKeys(Object object) {
        super.initFormatKeys(object);
        MediaSpecimen mediaSpecimen = (MediaSpecimen)object;
        Media media = mediaSpecimen.getMediaSpecimen();
        if(media!=null){
            if(media.getArtist()!=null){
                formatKeyMap.put(FormatKey.MEDIA_ARTIST, media.getArtist().toString());
            }
            if(media.getTitle()!=null){
                formatKeyMap.put(FormatKey.MEDIA_TITLE, media.getTitle().getText());
            }
            formatKeyMap.put(FormatKey.MEDIA_TITLE_CACHE, media.getTitleCache());
        }
    }

}
