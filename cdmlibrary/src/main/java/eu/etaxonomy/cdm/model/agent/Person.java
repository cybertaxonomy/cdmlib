/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;

/**
 * http://rs.tdwg.org/ontology/voc/Person.rdf
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:43:34
 */
public class Person extends IdentifiableEntity {
	static Logger logger = Logger.getLogger(Person.class);

	//e.g. the title
	@Description("e.g. the title")
	private String prefix;
	//All other names not included in the surname. May be just initials, all forenames in full or a combination of expanded
	//names and initials.
	@Description("All other names not included in the surname. May be just initials, all forenames in full or a combination of expanded names and initials.")
	private String firstname;
	//A hereditary name common to all members of a family, as distinct from a given name or forename
	@Description("A hereditary name common to all members of a family, as distinct from a given name or forename")
	private String lastname;
	//e.g. junior
	@Description("e.g. junior")
	private String suffix;
	//The period for which this person was alive represented as a TimePeriod datatype, i.e. start - end date.
	//Alternative suggestion as a flexible String. the form birthdate - deathdate (XXXX - YYYY; XXXX - or - YYYY as
	//appropriate) is prefered, or as simple flourished date (fl. XXXX) may be given where that is all that is known
	@Description("The period for which this person was alive represented as a TimePeriod datatype, i.e. start - end date.
	Alternative suggestion as a flexible String. the form birthdate - deathdate (XXXX - YYYY; XXXX - or - YYYY as appropriate) is prefered, or as simple flourished date (fl. XXXX) may be given where that is all that is known")
	private TimePeriod lifespan;
	private ArrayList institutionalMemberships;
	private Contact contact;
	private ArrayList personInSources;
	private ArrayList keywords;

	public ArrayList getInstitutionalMemberships(){
		return institutionalMemberships;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInstitutionalMemberships(ArrayList newVal){
		institutionalMemberships = newVal;
	}

	public ArrayList getPersonInSources(){
		return personInSources;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPersonInSources(ArrayList newVal){
		personInSources = newVal;
	}

	public ArrayList getKeywords(){
		return keywords;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setKeywords(ArrayList newVal){
		keywords = newVal;
	}

	public Contact getContact(){
		return contact;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setContact(Contact newVal){
		contact = newVal;
	}

	public String getPrefix(){
		return prefix;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setPrefix(String newVal){
		prefix = newVal;
	}

	public String getFirstname(){
		return firstname;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setFirstname(String newVal){
		firstname = newVal;
	}

	public String getLastname(){
		return lastname;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLastname(String newVal){
		lastname = newVal;
	}

	public String getSuffix(){
		return suffix;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSuffix(String newVal){
		suffix = newVal;
	}

	public TimePeriod getLifespan(){
		return lifespan;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setLifespan(TimePeriod newVal){
		lifespan = newVal;
	}

}