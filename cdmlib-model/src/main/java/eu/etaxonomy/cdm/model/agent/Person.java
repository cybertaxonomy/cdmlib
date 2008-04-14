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
 * A representation of a human being, living or dead.
 * It includes name parts, contact details, institutional membership,
 * and other possible information such as life period,
 * taxonomic and/or geographical specialization. For a short name
 * the inherited attribute {@link common.IdentifiableEntity#setTitleCache(String) titleCache} is to be used.
 * For other alternative (string-)names {@link common.OriginalSource OriginalSource} instances must be created.
 * and the inherited attribute {@link common.ReferencedEntityBase#setOriginalNameString(String) originalNameString} must be used.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/Person.rdf">TDWG Ontology</a>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Person extends Agent {
	static Logger logger = Logger.getLogger(Person.class);

	private String prefix;
	private String firstname;
	private String lastname;
	private String suffix;
	private TimePeriod lifespan;
	protected Set<InstitutionalMembership> institutionalMemberships;
	private Contact contact;
	private Set<Keyword> keywords = new HashSet();

	/** 
	 * Class constructor.
	 * 
	 * @see #Person(String, String, String)
	 */
	public Person() {
	}
	/** 
	 * Class constructor using a "forenames" string (including initials),
	 * a surname (family name) and an abbreviated name.
	 *
	 * @param  firstname     the given name of this person
	 * @param  lastname      the hereditary name of this person
	 * @param  abbreviation  a standardised or abbreviated name of this person
	 * @see                  #Person()
	 */
	public Person(String firstname, String lastname, String abbreviation) {
		this.setFirstname(firstname);
		this.setLastname(lastname);
		this.setTitleCache(abbreviation);
	}
	
	
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<InstitutionalMembership> getInstitutionalMemberships(){
		return this.institutionalMemberships;
	}
	protected void setInstitutionalMemberships(Set<InstitutionalMembership> institutionalMemberships){
		this.institutionalMemberships = institutionalMemberships;
	}
	/** 
	 * Adds a new membership of this person in an institution.
	 * This method also creates a new institutional membership instance.
	 *
	 * @param  institution  the institution this person belongs to
	 * @param  period       the time period for which this person has been a member of the institution
	 * @param  department   the string label for the department this person belongs to,
	 * 					    within the institution
	 * @param  role         the string label for the persons's role within the department or institution
	 * @see 			    InstitutionalMembership#InstitutionalMembership(Institution, Person, TimePeriod, String, String)
	 */
	public void addInstitutionalMembership(Institution institution, TimePeriod period, String department, String role){
		InstitutionalMembership ims = new InstitutionalMembership(institution, this, period, department, role); 
	}
	/** 
	 * Removes one element from the set of institutional memberships of this person.
	 *
	 * @param  ims  the institutional membership of this person which should be deleted
	 * @see         #addInstitutionalMembership(Institution, TimePeriod, String, String)
	 */
	public void removeInstitutionalMembership(InstitutionalMembership ims){
		ims.setInstitute(null);
		ims.setPerson(null);
		//this.institutionalMemberships.remove(ims);
	}


	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<Keyword> getKeywords(){
		return this.keywords;
	}
	public void setKeywords(Set<Keyword> keywords){
		this.keywords = keywords;
	}
	/** 
	 * Adds a new keyword from the keyword vocabulary to describe better this person
	 * or circumscribe his activities.
	 *
	 * @param  keyword  any keyword relevant for this person or for his activities
	 * @see 			common.Keyword
	 */
	public void addKeyword(Keyword keyword){
		this.keywords.add(keyword);
	}
	/** 
	 * Removes one element from the set of keywords for this person.
	 *
	 * @param  keyword  the keyword describing this person or his activities which should be deleted
	 * @see             #addKeyword(Keyword)
	 */
	public void removeKeyword(Keyword keyword){
		this.keywords.remove(keyword);
	}



	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Contact getContact(){
		return this.contact;
	}
	/** 
	 * Assigns a {@link Contact contact} to this person.
	 *
	 * @param  contact  the contact which should be assigned to this person
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	
	public String getPrefix(){
		return this.prefix;
	}
	/** 
	 * Assigns a prefix (for instance "Prof.&nbsp;Dr.<!-- -->") to this person's name.
	 *
	 * @param  prefix  the string which should be assigned as a prefix to this person's name
	 */
	public void setPrefix(String prefix){
		this.prefix = prefix;
	}


	public String getFirstname(){
		return this.firstname;
	}
	/** 
	 * Assigns a given name or forename (for instance "John") to this person. 
	 * This is the part of his name which is not shared with other
	 * family members. Actually it may be just initials (for instance "G. Jr."),
	 * all forenames in full or a combination of expanded names and initials. 
	 *
	 * @param  firstname  the string which should be assigned as a given name to this person
	 */
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}

	
	public String getLastname(){
		return this.lastname;
	}
	/** 
	 * Assigns a hereditary name (surname or family name)
	 * to this person (for instance "Smith").
	 * This is the part of his name which is common to (all) other
	 * members of his family, as distinct from the given name or forename. 
	 *
	 * @param  lastname  the string which should be assigned as a hereditary name to this person
	 */
	public void setLastname(String lastname){
		this.lastname = lastname;
	}


	public String getSuffix(){
		return this.suffix;
	}
	/** 
	 * Assigns a suffix (for instance "Junior") to this person's name.
	 *
	 * @param  suffix  the string which should be assigned as a suffix to this person's name
	 */
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}


	public TimePeriod getLifespan(){
		return this.lifespan;
	}
	/**
	 * Assigns to this person a period of time in which he was alive.
	 * The form birthdate - deathdate (XXXX - YYYY; XXXX - or - YYYY as appropriate) is
	 * preferred, but a simple flourished date (fl. XXXX) may be given
	 * if that is all what is known.
	 *
	 * @param lifespan  the time period to be assigned as life time to this person
	 * @see             common.TimePeriod
	 */
	public void setLifespan(TimePeriod lifespan){
		this.lifespan = lifespan;
	}

	@Override
	/**
	 * Generates the "full" name string of this person. The used attributes are:
	 * {@link #prefix prefix}, {@link #firstname firstname}, {@link #lastname lastname} and {@link #suffix suffix}.
	 * This method overrides {@link common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the string with the full name of this person
	 */
	public String generateTitle(){
		return "";
	}

}