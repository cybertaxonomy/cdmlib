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

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * Factory methods for {@link EntityFilter}s related to {@link IdentifiableEntity}.
 *
 * @author muellera
 * @since 20.05.2026
 */
public class IdentifiableEntityFilters {

    public static <T extends IdentifiableEntity<?>> EntityFilter<T> titleCacheFilter(
            @SuppressWarnings("unused") Class<T> type,
            String queryString,
            MatchMode matchMode, boolean ignoreCase) {
        return (root, cb) -> {
            return predicateForMatchMode("titleCache", queryString, matchMode, cb, root, ignoreCase);
        };
    }

    private static <S extends IdentifiableEntity<?>> Predicate predicateForMatchMode(String param,
            String queryString, MatchMode matchMode,
            CriteriaBuilder cb, Path<S> root, boolean ignoreCase) {

        Predicate result;
        if (ignoreCase) {
            String lowerQueryString = queryString == null ? "" : queryString.toLowerCase();
            if (matchMode == null) {
                result = cb.like(cb.lower(root.get(param)), lowerQueryString);
            } else if (matchMode == MatchMode.EXACT) {
                result = cb.equal(cb.lower(root.get(param)), lowerQueryString);
            } else if (matchMode == MatchMode.BEGINNING || matchMode == MatchMode.END
                    || matchMode == MatchMode.ANYWHERE || matchMode == MatchMode.LIKE) {
                result = cb.like(cb.lower(root.get(param)), matchMode.queryStringFrom(lowerQueryString));
            } else {
                throw new RuntimeException("Unsupported MatchMode: " + matchMode.name());
            }
        }else {
            String lowerQueryString = queryString == null ? "" : queryString;
            if (matchMode == null) {
                result = cb.like(root.get(param), lowerQueryString);
            } else if (matchMode == MatchMode.EXACT) {
                result = cb.equal(root.get(param), lowerQueryString);
            } else if (matchMode == MatchMode.BEGINNING || matchMode == MatchMode.END
                    || matchMode == MatchMode.ANYWHERE || matchMode == MatchMode.LIKE) {
                result = cb.like(root.get(param), matchMode.queryStringFrom(lowerQueryString));
            } else {
                throw new RuntimeException("Unsupported MatchMode: " + matchMode.name());
            }
        }
        return result;
    }

}
