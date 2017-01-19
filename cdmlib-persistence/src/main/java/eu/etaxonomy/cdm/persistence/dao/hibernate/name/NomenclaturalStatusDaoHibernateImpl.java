/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.name;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.ReferencedEntityBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.ReferencedEntityDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;

/**
 * @author a.mueller
 *
 */
@Repository
public class NomenclaturalStatusDaoHibernateImpl 
			extends ReferencedEntityDaoImpl<NomenclaturalStatus> implements INomenclaturalStatusDao {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomenclaturalStatusDaoHibernateImpl.class);

	public NomenclaturalStatusDaoHibernateImpl() {
		super(NomenclaturalStatus.class); 
	}

	public List<ReferencedEntityBase> getAllNomenclaturalStatus(Integer limit, Integer start) {
		Criteria crit = getSession().createCriteria(NomenclaturalStatus.class);
		List<ReferencedEntityBase> results = crit.list();
		return results;
	}

}
