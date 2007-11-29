package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

public class IdentifiableDaoBase<T extends IdentifiableEntity> extends CdmEntityDaoBase<T> implements IIdentifiableDao<T>{


	public IdentifiableDaoBase(Class<T> type) {
		super(type);
	}

	public List<T> findByTitle(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

}
