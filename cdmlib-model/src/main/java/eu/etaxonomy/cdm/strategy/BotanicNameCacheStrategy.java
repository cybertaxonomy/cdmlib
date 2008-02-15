/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;

/**
 * @author a.mueller
 *
 */
public class BotanicNameCacheStrategy extends NameCacheStrategyBase implements INameCacheStrategy {
	private static final Logger logger = Logger.getLogger(BotanicNameCacheStrategy.class);
	
	public static BotanicNameCacheStrategy NewInstance(){
		return new BotanicNameCacheStrategy();
	}
	
	private BotanicNameCacheStrategy(){
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// Test implementation
	public String getTitleCache(CdmBase object) {
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
