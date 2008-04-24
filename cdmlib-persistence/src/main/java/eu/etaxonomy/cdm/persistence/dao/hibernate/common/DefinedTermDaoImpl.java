/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Query;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;

@Repository
public class DefinedTermDaoImpl extends CdmEntityDaoBase<DefinedTermBase> implements IDefinedTermDao{
	private static final Logger logger = Logger.getLogger(DefinedTermDaoImpl.class);

	public DefinedTermDaoImpl() {
		super(DefinedTermBase.class);
	}

	public List<DefinedTermBase> findByTitle(String queryString) {
		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation where representation.label = :label");
		query.setParameter("label", queryString);
		return (List<DefinedTermBase>) query.list();
	}
	

//	@Override
//	public List<DefinedTermBase> list(int limit, int start) {
//		Query query = getSession().createQuery("select term from DefinedTermBase term join fetch term.representations representation ");
//		query.setMaxResults(limit);
//		query.setFirstResult(start);
//		return (List<DefinedTermBase>) query.list();
//	}

}
