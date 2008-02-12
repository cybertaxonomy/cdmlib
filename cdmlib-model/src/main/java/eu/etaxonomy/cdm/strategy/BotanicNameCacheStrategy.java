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
public class BotanicNameCacheStrategy extends StrategyBase implements INameCacheStrategy {
	private static final Logger logger = Logger.getLogger(BotanicNameCacheStrategy.class);
	
	public static BotanicNameCacheStrategy NewInstance(){
		return new BotanicNameCacheStrategy();
	}
	
	private BotanicNameCacheStrategy(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getFullNameCache()
	 */
	// Test implementation
	public String getNameCache(CdmBase object) {
		String result;
		BotanicalName botanicalName = (BotanicalName)object;
		
		if (botanicalName.getInfraSpecificEpithet() != null){
			result = getInfraSpeciesNameCache(botanicalName);
		}else if (botanicalName.getSpecificEpithet() != null){
			result = getSpeciesNameCache(botanicalName);
		}else if (botanicalName.getInfraGenericEpithet() != null){
			result = getInfraGenusNameCache(botanicalName);
		}else if (botanicalName.getUninomial() != null){
			result = getUninomialNameCache(botanicalName);
		}else{ 
			logger.warn("BotanicalName Strategy for Name (UUID: " + botanicalName.getUuid() +  ") not yet implemented");
			result = "XXX";
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// Test implementation
	public String getFullNameCache(CdmBase object) {
		String result;
		NonViralName tn = (NonViralName)object;
		result = getNameCache(object);
		Agent agent= tn.getCombinationAuthorTeam();
		if (agent != null){
			result += " " + agent.getTitleCache();
		}
		return result;
	}

/************** PRIVATES ****************/
	
	protected String getUninomialNameCache(BotanicalName botanicalName){
		String result;
		result = botanicalName.getUninomial();
		return result;
	}
	
	protected String getInfraGenusNameCache(BotanicalName botanicalName){
		//FIXME
		String result;
		result = botanicalName.getUninomial();
		result += " (" + (botanicalName.getInfraGenericEpithet() + ")").trim().replace("null", "");
		return result;
	}

	
	protected String getSpeciesNameCache(BotanicalName botanicalName){
		String result;
		result = botanicalName.getUninomial();
		result += " " + (botanicalName.getSpecificEpithet()).trim().replace("null", "");
		return result;
	}
	
	
	protected String getInfraSpeciesNameCache(BotanicalName botanicalName){
		String result;
		result = botanicalName.getUninomial();
		String specis = botanicalName.getSpecificEpithet();
		result += " " + (specis.trim()).replace("null", "");
		result += " " + (botanicalName.getRank().getAbbreviation()).trim().replace("null", "");
		result += " " + (botanicalName.getInfraSpecificEpithet()).trim().replace("null", "");
		return result;
	}
	
}
