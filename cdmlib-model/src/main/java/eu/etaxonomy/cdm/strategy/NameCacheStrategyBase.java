/**
 * 
 */
package eu.etaxonomy.cdm.strategy;

import java.util.UUID;

import org.apache.log4j.Logger;


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
	abstract public String getTitleCache(T object);
	

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
			result += " (" + (name.getInfraGenericEpithet() + ")").trim().replace("null", "");
			return result;
		}

		
		protected String getSpeciesNameCache(NonViralName name){
			String result;
			result = name.getGenusOrUninomial();
			result += " " + (name.getSpecificEpithet()).trim().replace("null", "");
			return result;
		}
		
		
		protected String getInfraSpeciesNameCache(NonViralName name){
			String result;
			result = name.getGenusOrUninomial();
			String specis = name.getSpecificEpithet();
			result += " " + (specis.trim()).replace("null", "");
			result += " " + (name.getRank().getAbbreviation()).trim().replace("null", "");
			result += " " + (name.getInfraSpecificEpithet()).trim().replace("null", "");
			return result;
		}
		

}
