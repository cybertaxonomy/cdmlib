package eu.etaxonomy.cdm.persistence.dao.occurrence;

import java.util.List;

import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.persistence.dao.common.IIdentifiableDao;

public interface ICollectionDao extends IIdentifiableDao<Collection> {
	
	/**
	 * Returns a list of Collection instances matching the code supplied
	 * 
	 * @param code The code 
	 * @return a List of Collection instances
	 */
	public List<Collection> getCollectionByCode(String code);

}
