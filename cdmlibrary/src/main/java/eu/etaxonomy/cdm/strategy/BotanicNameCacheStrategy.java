/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.NonViralName;

/**
 * @author a.mueller
 *
 */
public class BotanicNameCacheStrategy extends StrategyBase implements
		INameCacheStrategy {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getFullNameCache()
	 */
	// PROTOTYPE implementation
	public String getNameCache(VersionableEntity object) {
		String result;
		NonViralName tn = (NonViralName)object;
		if (tn.getUninomial() != null){
			result = tn.getUninomial();
		}else{
			result = tn.getUninomial();
			result += (" " + tn.getSpecificEpithet()).trim().replace("null", "");
			result += (" " + tn.getInfraSpecificEpithet()).trim().replace("null", "");
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// PROTOTYPE implementation
	public String getFullNameCache(VersionableEntity object) {
		String result;
		NonViralName tn = (NonViralName)object;
		result = getNameCache(object);
		Agent agent= tn.getCombinationAuthorTeam();
		if (agent != null){
			result += " " + agent.getTitleCache();
		}
		return result;
	}

}
