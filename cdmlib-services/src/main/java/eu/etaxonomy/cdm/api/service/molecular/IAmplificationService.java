// $Id$
/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.molecular;

import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.api.service.IAnnotatableService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.molecular.Amplification;
import eu.etaxonomy.cdm.persistence.dao.initializer.IBeanInitializer;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author pplitzner
 * @date 11.03.2014
 *
 */
public interface IAmplificationService extends IAnnotatableService<Amplification>{

    /**
     * Retrieves the {@link UUID} and the string label of all
     * {@link Amplification}s found in the data base.
     * @return a list of {@link UuidAndTitleCache}
     */
    public List<UuidAndTitleCache<Amplification>> getAmplificationUuidAndLabelCache();

    /**
    * Return a List of {@link Amplification}s matching the given query string, optionally with a particular MatchMode
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
    Pager<Amplification> findByLabelCache(String queryString, MatchMode matchmode, List<Criterion> criteria,
            Integer pageSize, Integer pageNumber, List<OrderHint> orderHints, List<String> propertyPaths);
}
