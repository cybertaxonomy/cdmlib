/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;


/**
 * @author a.mueller
 *
 */
public class NonViralNameDefaultCacheStrategy extends NameCacheStrategyBase<NonViralName> implements INameCacheStrategy<NonViralName> {
	private static final Logger logger = Logger.getLogger(NonViralNameDefaultCacheStrategy.class);
	
	final static UUID uuid = UUID.fromString("1cdda0d1-d5bc-480f-bf08-40a510a2f223");
	
	public  UUID getUuid(){
		return uuid;
	}

	
	public static NonViralNameDefaultCacheStrategy NewInstance(){
		return new NonViralNameDefaultCacheStrategy();
	}
	
	private NonViralNameDefaultCacheStrategy(){
		super();
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// Test implementation
	@Override
	public String getTitleCache(NonViralName object) {
		String result;
		NonViralName tn = (NonViralName)object;
		result = getNameCache(object);
		INomenclaturalAuthor agent= tn.getCombinationAuthorTeam();
		if (agent != null){
			result += " " + agent.getNomenclaturalTitle();
		}
		return result;
	}

}
