package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{

	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}

}


