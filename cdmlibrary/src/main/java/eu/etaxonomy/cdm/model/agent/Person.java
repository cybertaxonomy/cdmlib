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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/Person.rdf
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:23
 */
@Entity
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
	 * @param institutionalMemberships
	 */
	public void setInstitutionalMemberships(ArrayList institutionalMemberships){
		;
	}

	public ArrayList getPersonInSources(){
		return personInSources;
	}

	/**
	 * 
	 * @param personInSources
	 */
	public void setPersonInSources(ArrayList personInSources){
		;
	}

	public ArrayList getKeywords(){
		return keywords;
	}

	/**
	 * 
	 * @param keywords
	 */
	public void setKeywords(ArrayList keywords){
		;
	}

	public Contact getContact(){
		return contact;
	}

	/**
	 * 
	 * @param contact
	 */
	public void setContact(Contact contact){
		;
	}

	public String getPrefix(){
		return prefix;
	}

	/**
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix){
		;
	}

	public String getFirstname(){
		return firstname;
	}

	/**
	 * 
	 * @param firstname
	 */
	public void setFirstname(String firstname){
		;
	}

	public String getLastname(){
		return lastname;
	}

	/**
	 * 
	 * @param lastname
	 */
	public void setLastname(String lastname){
		;
	}

	public String getSuffix(){
		return suffix;
	}

	/**
	 * 
	 * @param suffix
	 */
	public void setSuffix(String suffix){
		;
	}

	public TimePeriod getLifespan(){
		return lifespan;
	}

	/**
	 * 
	 * @param lifespan
	 */
	public void setLifespan(TimePeriod lifespan){
		;
	}

}