/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;


import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.*;

import javax.persistence.*;

/**
 * http://rs.tdwg.org/ontology/voc/Person.rdf
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Person extends Agent {
	static Logger logger = Logger.getLogger(Person.class);

	//e.g. the title
	private String prefix;
	//All other names not included in the surname. May be just initials, all forenames in full or a combination of expanded
	//names and initials.
	private String firstname;
	//A hereditary name common to all members of a family, as distinct from a given name or forename
	private String lastname;
	//e.g. junior
	private String suffix;
	//The period for which this person was alive represented as a TimePeriod datatype, i.e. start - end date. Alternative
	//suggestion as a flexible String. the form birthdate - deathdate (XXXX - YYYY; XXXX - or - YYYY as appropriate) is
	//preferred, or as simple flourished date (fl. XXXX) may be given where that is all that is known
	private TimePeriod lifespan;
	protected Set<InstitutionalMembership> institutionalMemberships;
	private Contact contact;
	private Set<Keyword> keywords = new HashSet();

	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<InstitutionalMembership> getInstitutionalMemberships(){
		return this.institutionalMemberships;
	}
	protected void setInstitutionalMemberships(Set<InstitutionalMembership> institutionalMemberships){
		this.institutionalMemberships = institutionalMemberships;
	}
	public void addInstitutionalMembership(Institution institution, TimePeriod period, String department, String role){
		InstitutionalMembership ims = new InstitutionalMembership(institution, this, period, department, role); 
	}
	public void removeInstitutionalMembership(InstitutionalMembership ims){
		ims.setInstitute(null);
		ims.setPerson(null);
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<Keyword> getKeywords(){
		return this.keywords;
	}
	public void setKeywords(Set<Keyword> keywords){
		this.keywords = keywords;
	}
	public void addKeyword(Keyword keyword){
		this.keywords.add(keyword);
	}
	public void removeKeyword(Keyword keyword){
		this.keywords.remove(keyword);
	}



	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Contact getContact(){
		return this.contact;
	}
	public void setContact(Contact contact){
		this.contact = contact;
	}

	
	public String getPrefix(){
		return this.prefix;
	}
	public void setPrefix(String prefix){
		this.prefix = prefix;
	}


	public String getFirstname(){
		return this.firstname;
	}
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}

	
	public String getLastname(){
		return this.lastname;
	}
	public void setLastname(String lastname){
		this.lastname = lastname;
	}


	public String getSuffix(){
		return this.suffix;
	}
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}


	public TimePeriod getLifespan(){
		return this.lifespan;
	}

	/**
	 * 
	 * @param lifespan    lifespan
	 */
	public void setLifespan(TimePeriod lifespan){
		this.lifespan = lifespan;
	}

	@Override
	public String generateTitle(){
		return "";
	}

}