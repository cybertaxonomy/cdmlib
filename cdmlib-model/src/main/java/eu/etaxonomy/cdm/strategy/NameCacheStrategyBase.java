/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

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
	public String getNameCache(T object) {
		String result;
		NonViralName name = (NonViralName)object;
		Rank rank = name.getRank();
		
		if (rank == null){
			return "";
		}else if (rank.isInfraSpecific()){
			result = getInfraSpeciesNameCache(name);
		}else if (rank.isSpecies()){
			result = getSpeciesNameCache(name);
		}else if (rank.isInfraGeneric()){
			result = getInfraGenusNameCache(name);
		}else if (rank.isGenus()){
			result = getGenusOrUninomialNameCache(name);
		}else if (rank.isSupraGeneric()){
			result = getGenusOrUninomialNameCache(name);
		}else{ 
			logger.warn("Name Strategy for Name (UUID: " + name.getUuid() +  ") not yet implemented");
			result = "XXX";
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INameCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public String getTitleCache(T name){
		String result;
		//TODO use authorCache + TODO exAuthors
		Agent agent= name.getCombinationAuthorTeam();
		if (isAutonym(name)){
			result = getSpeciesNameCache(name);
			if (agent != null){
				result += " " + agent.getTitleCache();
			}
			result += " " + (nz(name.getInfraSpecificEpithet())).trim().replace("null", "");
		}else{
			result = getNameCache(name);
			if (agent != null){
				result += " " + agent.getTitleCache();
			}
		}
		return result;
	}

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
		Team at = new Team();
		at.setProtectedTitleCache(true);
		at.setTitleCache(nvn.getAuthorshipCache());
		tags.add(at);			
		tags.add(nvn.getNomenclaturalReference());			
		return tags;
	}


	/************** PRIVATES ****************/
		
		protected String getGenusOrUninomialNameCache(NonViralName name){
			String result;
			result = name.getGenusOrUninomial();
			return result;
		}
		
		protected String getInfraGenusNameCache(NonViralName name){
			//FIXME
			String result;
			result = name.getGenusOrUninomial();
			result += " (" + (nz(name.getInfraGenericEpithet()) + ")").trim().replace("null", "");
			return result;
		}

		
		protected String getSpeciesNameCache(NonViralName name){
			String result;
			result = name.getGenusOrUninomial();
			result += " " + nz(name.getSpecificEpithet()).trim().replace("null", "");
			return result;
		}
		
		
		protected String getInfraSpeciesNameCache(NonViralName name){
			String result;
			result = name.getGenusOrUninomial();
			result += " " + (nz(name.getSpecificEpithet()).trim()).replace("null", "");
			if (! isAutonym(name)){
				result += " " + (name.getRank().getAbbreviation()).trim().replace("null", "");
			}
			result += " " + (nz(name.getInfraSpecificEpithet())).trim().replace("null", "");
			return result;
		}
		
		
		/**
		 * @param name
		 * @return true, if name has Rank, Rank is below species and species epithet equals infraSpeciesEpithtet
		 */
		private boolean isAutonym(NonViralName name){
			if (name.getRank() != null && name.getRank().isInfraSpecific() && name.getSpecificEpithet() != null && name.getSpecificEpithet().equals(name.getInfraSpecificEpithet())){
				return true;
			}else{
				return false;
			}
		}
		
		/* Returns "" if nzString is null, identity function otherwise*/ 
		private String nz(String nzString){
			return (nzString == null)? "" : nzString;
		}

}
