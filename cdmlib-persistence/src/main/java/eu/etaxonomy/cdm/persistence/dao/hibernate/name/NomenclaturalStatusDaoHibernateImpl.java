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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.SingleSourcedEntityDaoImpl;
import eu.etaxonomy.cdm.persistence.dao.name.INomenclaturalStatusDao;

/**
 * @author a.mueller
 */
@Repository
public class NomenclaturalStatusDaoHibernateImpl
			extends SingleSourcedEntityDaoImpl<NomenclaturalStatus>
            implements INomenclaturalStatusDao {

    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NomenclaturalStatusDaoHibernateImpl.class);

	public NomenclaturalStatusDaoHibernateImpl() {
		super(NomenclaturalStatus.class);
	}

}
