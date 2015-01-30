// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.batch.validation;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * @author ayco_holleman
 * @date 29 jan. 2015
 *
 */
public class EntityValidationUnit {

    private Class<? extends ICdmBase> entityClass;
    private IService<? extends ICdmBase> entityLoader;

    /**
     * @return the entityClass
     */
    public Class<? extends ICdmBase> getEntityClass() {
        return entityClass;
    }

    /**
     * @param entityClass
     *            the entityClass to set
     */
    public void setEntityClass(Class<? extends ICdmBase> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * @return the entityLoader
     */
    public IService<? extends ICdmBase> getEntityLoader() {
        return entityLoader;
    }

    /**
     * @param entityLoader
     *            the entityLoader to set
     */
    public void setEntityLoader(IService<? extends ICdmBase> entityLoader) {
        this.entityLoader = entityLoader;
    }

}
