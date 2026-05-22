/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.filter;

import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.Predicate;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * Factory methods for {@link EntityFilter}s related to {@link CdmBase}.
 *
 * @author muellera
 * @since 20.05.2026
 */
public final class CdmBaseFilters {

    public static final String UUID = "uuid";
    public static final String ID = "id";


    public static <T extends CdmBase> EntityFilter<T> uuidFilter(UUID uuid, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) -> uuid == null ? null :  cb.equal(root.get(UUID), uuid);
    }

    /**
     * Filter for a given set of UUIDs. If the set is null, no filter is applied.
     */
    public static <T extends CdmBase> EntityFilter<T> uuidsFilter(final Set<UUID> uuids, @SuppressWarnings("unused") Class<T> clazzForCasting) {
        return (root, cb) -> uuids == null ? null :  root.get(UUID).in(uuids);
    }

    //TODO move to a more generic filter class
    @SafeVarargs
    public static <T> EntityFilter<T> or(EntityFilter<T>... filters) {
        return (root, cb) -> {
            Predicate result = null;
            for (EntityFilter<T> filter : filters) {
                Predicate predicate = filter.toPredicate(root, cb);
                if (predicate != null) {
                    result = result == null ? predicate : cb.or(result, predicate);
                }
            }
            return result;
        };
    }
}