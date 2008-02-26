package eu.etaxonomy.cdm.persistence.dao.common;

import java.util.List;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface ITitledDao<T extends CdmBase> {

	public List<T> findByTitle(String queryString);
}
