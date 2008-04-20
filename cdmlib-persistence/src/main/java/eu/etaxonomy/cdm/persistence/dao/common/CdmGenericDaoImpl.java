package eu.etaxonomy.cdm.persistence.dao.common;

import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.CdmBase;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{

	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}

}


