/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public abstract class NameCacheStrategyBase<T extends NonViralName> extends StrategyBase implements INameCacheStrategy<T> {
	private static final Logger logger = Logger.getLogger(NameCacheStrategyBase.class);

	final static UUID uuid = UUID.fromString("817ae5b5-3ac2-414b-a134-a9ae86cba040");

	/**
	 * 
	 */
	public NameCacheStrategyBase() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getFullNameCache()
	 */
	// Test implementation
	abstract public String getNameCache(T taxonNameBase);
	
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public abstract String getTitleCache(T name);


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTaggedName(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public List<Object> getTaggedName(T nvn) {
		List<Object> tags = new ArrayList<Object>();
		tags.add(nvn.getGenusOrUninomial());
		if (nvn.isSpecies() || nvn.isInfraSpecific()){
			tags.add(nvn.getSpecificEpithet());			
		}
		if (nvn.isInfraSpecific()){
			tags.add(nvn.getRank());			
			tags.add(nvn.getInfraSpecificEpithet());			
		}
		Team at = Team.NewInstance();
		at.setProtectedTitleCache(true);
		at.setTitleCache(nvn.getAuthorshipCache());
		tags.add(at);			
		tags.add(nvn.getNomenclaturalReference());			
		return tags;
	}



}
