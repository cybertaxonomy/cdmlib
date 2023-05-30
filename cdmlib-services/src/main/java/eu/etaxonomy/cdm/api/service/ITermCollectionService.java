/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.term.TermCollection;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.persistence.dto.UuidAndTitleCache;
import eu.etaxonomy.cdm.persistence.query.OrderHint;

/**
 * @author a.mueller
 * @date 30.05.2023
 */
public interface ITermCollectionService extends IIdentifiableEntityService<TermCollection> {

    public List<TermCollection> list(TermType termType, Integer limit, Integer start, List<OrderHint> orderHints, List<String> propertyPaths);

    public Pager<TermCollection> page(TermType termType, Integer pageSize, Integer pageIndex, List<OrderHint> orderHints, List<String> propertyPaths);

	public <S extends TermCollection> List<UuidAndTitleCache<S>> getUuidAndTitleCacheByTermType(
	        Class<S> clazz, TermType termType, Integer limit, String pattern);

}
