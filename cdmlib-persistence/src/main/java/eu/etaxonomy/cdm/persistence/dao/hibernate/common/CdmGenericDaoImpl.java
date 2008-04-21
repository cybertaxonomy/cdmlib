package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{

	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}

}


