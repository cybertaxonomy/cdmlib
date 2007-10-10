package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.TaxonName;

public class ZooNameCacheStrategy extends StrategyBase implements
		INameCacheStrategy {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getFullNameCache()
	 */
	// PROTOTYPE dummy implementation
	public String getNameCache(VersionableEntity object) {
		String result;
		TaxonName tn = (TaxonName)object;
		if (tn.getUninomial() != null){
			result = tn.getUninomial();
		}else{
			result = tn.getGenus();
			result += (" " + tn.getSpecificEpithet()).trim().replace("null", "");
			result += (" " + tn.getInfraSpecificEpithet()).trim().replace("null", "");
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// PROTOTYPE dummy implementation
	public String getFullNameCache(VersionableEntity object) {
		String result;
		TaxonName tn = (TaxonName)object;
		result = getNameCache(object);
		if (tn.getYear() != null) {
			result = (" " + tn.getYear()).trim();	
		}
		Team team= tn.getAuthorTeam();
		if (team != null){
			if (tn.getYear() != null) {
				result = ",";	
			} 
			result += " " + tn.getAuthorTeam().getShortName();
		}
		return result;
	}

}
