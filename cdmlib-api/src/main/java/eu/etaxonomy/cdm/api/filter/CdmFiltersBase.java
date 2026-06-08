/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * A collection of helper methods for filters.
 *
 * @author muellera
 * @since 23.05.2026
 */
public class CdmFiltersBase {

    /**
     * Tests if the object is equal or null. If the object is null, no filter is applied (always true predicate).
     */
    protected static <T extends CdmBase> Predicate predicateEqualIfNotNull(CriteriaBuilder builder,
            Path<T> path, String field, Object obj) {

        if (obj == null){
            return builder.conjunction();  //always true predicate
        }
        return builder.equal(path.get(field), obj);
    }

}
