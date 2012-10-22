/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.strategy.cache.agent;

import java.util.UUID;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.strategy.StrategyBase;

/**
 * @author AM
 *
 */
public class PersonDefaultCacheStrategy extends StrategyBase implements
		INomenclaturalAuthorCacheStrategy<Person> {
	@SuppressWarnings("unused")
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
		String result = "";
		if (CdmUtils.isNotEmpty(person.getLastname() ) ){
			result = person.getLastname();
			result = addFirstNamePrefixSuffix(result, person);
			return result;
		}else{
			result = person.getNomenclaturalTitle();
			if (CdmUtils.isNotEmpty(result)){
				return result;
			}
			result = addFirstNamePrefixSuffix("", person);
			if (CdmUtils.isNotEmpty(result)){
				return result;
			}
		}
		return person.toString();
	}

	/**
	 * 
	 */
	private String addFirstNamePrefixSuffix(String oldString, Person person) {
		String result = oldString;
		result = CdmUtils.concat(" ", person.getFirstname(), result); 
		result = CdmUtils.concat(" ", person.getPrefix(), result); 
		result = CdmUtils.concat(" ", result, person.getSuffix()); 
		return result;
	}

}
