/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.molecular;

import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.molecular.Primer;
import eu.etaxonomy.cdm.persistence.dao.common.IAnnotatableDao;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @since 11.03.2014
 *
 */
public interface IPrimerDao extends IAnnotatableDao<Primer>{

    /**
     * Retrieves the {@link UUID} and the string representation (title cache) of all
     * {@link Primer}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndTitleCache();


    /**
     * Return a count of {@link Primer}s matching the given query string in the titleCache, optionally with a particular MatchMode
     *
     * @param queryString the query string to filter by
     * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
     * @param criteria extra restrictions to apply
     * @return a count of instances of type T matching the queryString
     */
    public long countByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria);

    /**
    * Return a List of {@link Primer}s matching the given query string, optionally with a particular MatchMode
    *
    * @param queryString the query string to filter by
    * @param matchmode use a particular type of matching (can be null - defaults to exact matching)
    * @param criteria extra restrictions to apply
    * @param pageSize The maximum number of rights returned (can be null for all rights)
    * @param pageNumber The offset (in pageSize chunks) from the start of the result set (0 - based)
    * @param propertyPaths properties to initialize - see {@link IBeanInitializer#initialize(Object, List)}
    * @param orderHints
    *            Supports path like <code>orderHints.propertyNames</code> which
    *            include *-to-one properties like createdBy.username or
    *            authorTeam.persistentTitleCache
    * @return a List of instances of type T matching the queryString
    */
    public List<Primer> findByTitle(String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);


    /**
     * @param limitOfInitialElements
     * @param pattern
     * @return
     */
    public List<UuidAndTitleCache<Primer>> getPrimerUuidAndTitleCache(Integer limitOfInitialElements, String pattern);

}
