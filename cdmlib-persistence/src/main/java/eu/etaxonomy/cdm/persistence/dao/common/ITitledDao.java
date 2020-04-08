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

import org.hibernate.criterion.Criterion;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

public interface ITitledDao<T extends CdmBase> {

	public List<T> findByTitle(String queryString);

	public List<T> findByTitle(String queryString, CdmBase sessionObject);

	public long countByTitle(String queryString);

	public long countByTitle(String queryString, CdmBase sessionObject);

	public long countByTitle(String queryString, MatchMode matchMode, List<Criterion> criteria);

	public List<T> findByTitle(String queryString, MatchMode matchMode, int page, int pagesize, List<Criterion> criteria);

}
