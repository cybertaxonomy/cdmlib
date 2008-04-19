/**
 * 
 */
package eu.etaxonomy.cdm.strategy.cache;

import java.util.UUID;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public class PersonDefaultCacheStrategy extends StrategyBase implements
		INomenclaturalAuthorCacheStrategy<Person> {
	private static final Logger logger = Logger.getLogger(PersonDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("9abda0e1-d5cc-480f-be38-40a510a3f253");

	static public PersonDefaultCacheStrategy NewInstance(){
		return new PersonDefaultCacheStrategy();
	}
	
	/**
	 * 
	 */
	private PersonDefaultCacheStrategy() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.StrategyBase#getUuid()
	 */
	@Override
	protected UUID getUuid() {
		return uuid;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INomenclaturalAuthorCacheStrategy#getNomenclaturalTitle(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getNomenclaturalTitle(Person person) {
		return person.getNomenclaturalTitle();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.strategy.INomenclaturalAuthorCacheStrategy#getTitleCache(eu.etaxonomy.cdm.model.name.TaxonNameBase)
	 */
	public String getTitleCache(Person person) {
		if (! (person.getLastname() == null)  && ! (person.getLastname().trim().equals("")) ){
			String result = "";
			if (person.getFirstname() != null){
				result += person.getFirstname() + " ";
			}
			result += person.getLastname();
			return result;
		}else{
			return person.getNomenclaturalTitle();
		}
	}

}
