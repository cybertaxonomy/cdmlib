// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao;

import java.util.ArrayList;
import java.util.Arrays;

import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;

/**
 * @author a.kohlbecker
 * @date 30.07.2010
 *
 */
public class AnnotationTypeAutoInitializer extends AutoPropertyInitializer<Annotation> {


    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanAutoInitializer#initialize(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public void initialize(Annotation bean) {
        beanInitializer.initialize(bean, Arrays.asList(new String[] {"annotationType"}));
    }

}
