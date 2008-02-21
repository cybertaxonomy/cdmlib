/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.UUID;

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
public class BotanicNameDefaultCacheStrategy extends NameCacheStrategyBase<BotanicalName> implements INameCacheStrategy<BotanicalName> {
	private static final Logger logger = Logger.getLogger(BotanicNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");
	
	public  UUID getUuid(){
		return uuid;
	}

	
	public static BotanicNameDefaultCacheStrategy NewInstance(){
		return new BotanicNameDefaultCacheStrategy();
	}
	
	private BotanicNameDefaultCacheStrategy(){
		super();
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// Test implementation
	@Override
	public String getTitleCache(BotanicalName object) {
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
