/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;


/**
 * @author a.kohlbecker
 * @date Jan 15, 2013
 *
 */
public class MediaAutoInitializer extends AutoPropertyInitializer<Media> {

    @Override
    public void initialize(Media bean) {
        beanInitializer.initializeInstance(bean.getAllTitles());
        for (MediaRepresentation r : bean.getRepresentations()){
            beanInitializer.initializeInstance(r.getParts());
        }
    }
}
