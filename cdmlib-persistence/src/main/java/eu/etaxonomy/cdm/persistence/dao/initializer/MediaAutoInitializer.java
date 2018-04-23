/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;


/**
 * @author a.kohlbecker
 * @since Jan 15, 2013
 *
 */
public class MediaAutoInitializer extends AutoPropertyInitializer<Media> {

    @Override
    public void initialize(Media bean) {
        beanInitializer.initializeInstance(bean.getAllTitles());
        beanInitializer.initializeInstance(bean.getAllDescriptions());
        for (MediaRepresentation r : bean.getRepresentations()){
            beanInitializer.initializeInstance(r.getParts());
        }
    }


    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{
    	return String.format(" LEFT JOIN FETCH %s.representations LEFT JOIN FETCH %s.titles r LEFT JOIN FETCH r.mediaRepresentationParts ", beanAlias);
    }
}
