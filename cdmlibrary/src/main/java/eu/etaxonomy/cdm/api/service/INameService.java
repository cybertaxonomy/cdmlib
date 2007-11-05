package eu.etaxonomy.cdm.api.service;

import java.util.List;

import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.NonViralName;

public interface INameService extends IService {

	/**Returns a new NonViralName object */
	public abstract NonViralName createNonViralName(Rank rank);
	
	public abstract NonViralName getNonViralNameById(Integer id);

	/**
	 * Saves a NonViralName. If the NonViralName is already persisted,
	 * it is updated, otherwise it is saved as a new object.
	 * @param taxonName
	 * @return the NonViralNames Id
	 */
	public abstract int saveNonViralName(NonViralName taxonName);

	public abstract List getAllNames();

	public abstract List getNamesByNameString(String name);

}