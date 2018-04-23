/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.api.validation.batch;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * Probably not required anymore
 * @author ayco_holleman
 * @since 29 jan. 2015
 *
 */
class EntityValidationUnit<T extends ICdmBase, S extends T> {

    private final Class<S> entityClass;
    private final IService<T> entityLoader;

    EntityValidationUnit(Class<S> entityClass, IService<T> entityLoader) {
        this.entityClass = entityClass;
        this.entityLoader = entityLoader;
    }

    /**
     * @return the entityClass
     */
    Class<S> getEntityClass() {
        return entityClass;
    }

    /**
     * @return the entityLoader
     */
    IService<T> getEntityLoader() {
        return entityLoader;
    }

}
