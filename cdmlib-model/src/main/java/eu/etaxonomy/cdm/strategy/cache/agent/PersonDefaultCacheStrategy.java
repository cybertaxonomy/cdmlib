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

import org.apache.commons.lang.StringUtils;
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
	private static final long serialVersionUID = -6184639515553953112L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PersonDefaultCacheStrategy.class);

	final static UUID uuid = UUID.fromString("9abda0e1-d5cc-480f-be38-40a510a3f253");

	private String initialsSeparator = "";

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
		if (StringUtils.isNotBlank(person.getLastname() ) ){
			result = person.getLastname();
			result = addFirstNamePrefixSuffix(result, person);
			return result;
		}else{
			result = person.getNomenclaturalTitle();
			if (StringUtils.isNotBlank(result)){
				return result;
			}
			result = addFirstNamePrefixSuffix("", person);
			if (StringUtils.isNotBlank(result)){
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


    public String getInitialsFromFirstname(String firstname, boolean forceOnlyFirstLetter) {
        if (firstname == null){
            return null;
        }else if (StringUtils.isBlank(firstname)){
            return "";
        }
        String result = "";
        String[] splits = firstname.split("((?<=\\.)|\\s+|(?=([\\-\u2013])))+"); // [\\-\u2013]? // (?!=\\s) wasn't successful to trim
        for (String split : splits){
            split = split.trim();
            if (StringUtils.isBlank(split)){
                continue;
            }
            if (split.matches("^[\\-\u2013].*")){
                result += split.substring(0, 1);
                split = split.substring(1);
                if (StringUtils.isBlank(split)){
                    continue;
                }
            }
            if (split.matches("[A-Z]{2,3}")){
                split = split.replaceAll("(?<!^)",".");  //insert dots after each letter (each position but not at start)
                result = CdmUtils.concat(initialsSeparator, result, split);
            }else if (forceOnlyFirstLetter){
                result = CdmUtils.concat(initialsSeparator, result, split.substring(0, 1) + ".");
            }else if (split.endsWith(".")){
                result = CdmUtils.concat(initialsSeparator, result, split);
            }else{
                result = CdmUtils.concat(initialsSeparator, result, split.substring(0, 1) + ".");
            }
        }
        return result;
    }

}
