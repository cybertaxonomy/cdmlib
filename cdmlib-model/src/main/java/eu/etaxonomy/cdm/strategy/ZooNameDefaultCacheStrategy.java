package eu.etaxonomy.cdm.strategy;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;

public class ZooNameDefaultCacheStrategy extends NameCacheStrategyBase<ZoologicalName> implements	INameCacheStrategy<ZoologicalName> {
	private static final Logger logger = Logger.getLogger(ZooNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("950c4236-8156-4675-b866-785df33bc4d9");

	public UUID getUuid(){
		return uuid;
	}
	
	
	public static ZooNameDefaultCacheStrategy NewInstance(){
		return new ZooNameDefaultCacheStrategy();
	}
	
	private ZooNameDefaultCacheStrategy(){
		super();
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// PROTOTYPE dummy implementation
	@Override
	public String getTitleCache(ZoologicalName object) {
		String result;
		NonViralName tn = (NonViralName)object;
		result = getNameCache(object);
		if (tn.getYear() != null) {
			result = (" " + tn.getYear()).trim();	
		}
		Agent team= tn.getCombinationAuthorTeam();
		if (team != null){
			if (tn.getYear() != null) {
				result = ",";	
			} 
			result += " " + team.getTitleCache();
		}
		return result;
	}

}
