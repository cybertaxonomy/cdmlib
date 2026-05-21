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

/**
 * Workaround for Specification pattern that comes with Spring Data which is not yet available.
 *
 * @author muellera
 * @since 19.05.2026
 */
@FunctionalInterface
public interface EntityFilter<T> {

    public Predicate toPredicate(Path<T> path, CriteriaBuilder cb);

}
