/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.api.filter.EntityFilter;
import eu.etaxonomy.cdm.api.filter.MatchMode;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author muellera
 * @since 23.01.2008
 * @param <T>
 */
public interface ITitledDao<T extends CdmBase> {

	public List<T> findByTitle(String queryString);

	public long countByTitle(String queryString);

	public long countByTitle(String queryString, MatchMode matchMode, List<EntityFilter<T>> filter);

	public List<T> findByTitle(String queryString, MatchMode matchMode, List<EntityFilter<T>> filter, int page, int pagesize);

}
