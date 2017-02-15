/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import eu.etaxonomy.cdm.model.media.Rights;
import eu.etaxonomy.cdm.persistence.dao.common.IRightsDao;

/**
 * @author k.luther
 * @date 15.02.2017
 *
 */
public class RightsDaoImpl extends  AnnotatableDaoImpl<Rights> implements IRightsDao  {

    /**
     * @param type
     */
    public RightsDaoImpl(Class rightsClass) {
        super(rightsClass);
        // TODO Auto-generated constructor stub
    }

    public RightsDaoImpl(){
        super(Rights.class);
    }




}
