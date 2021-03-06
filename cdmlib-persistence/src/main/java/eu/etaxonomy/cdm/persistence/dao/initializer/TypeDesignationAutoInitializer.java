/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.util.Optional;

import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * @author a.kohlbecker
 * @since 30.07.2010
 *
 */
public class TypeDesignationAutoInitializer extends AutoPropertyInitializer<TypeDesignationBase> {

    @Override
    public void initialize(TypeDesignationBase bean) {
       beanInitializer.initializeInstance(bean.getTypeStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> hibernateFetchJoin(Class<?> clazz, String beanAlias) {
        return Optional.of(String.format(" LEFT JOIN FETCH %s.typeStatus ", beanAlias));
    }

}
