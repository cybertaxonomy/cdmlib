/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author a.kohlbecker
 \* @since 30.07.2010
 *
 * @TODO only necessary since Point is not subclass of CdmBase
 */
public class GatheringEventLocationAutoInitializer extends AutoPropertyInitializer<GatheringEvent> {

    @Override
    public void initialize(GatheringEvent bean) {
        try {
            beanInitializer.initializeInstance(bean.getExactLocation().getReferenceSystem().getRepresentations());
        } catch (NullPointerException npe){
            /* IGNORE */
        }
    }
    
    @Override
    public String hibernateFetchJoin(Class<?> clazz, String beanAlias) throws Exception{
    	return String.format(" LEFT JOIN FETCH %s.exactLocation l LEFT JOIN l.referenceSystem rs LEFT JOIN rs.representations ", beanAlias); 
    }

}
