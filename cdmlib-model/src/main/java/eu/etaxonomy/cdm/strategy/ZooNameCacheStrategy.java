package eu.etaxonomy.cdm.strategy;

import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;

public class ZooNameCacheStrategy extends NameCacheStrategyBase implements	INameCacheStrategy {

	public static ZooNameCacheStrategy NewInstance(){
		return new ZooNameCacheStrategy();
	}
	
	private ZooNameCacheStrategy(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getNameCache()
	 */
	// PROTOTYPE dummy implementation
	@Override
	public String getTitleCache(CdmBase object) {
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
