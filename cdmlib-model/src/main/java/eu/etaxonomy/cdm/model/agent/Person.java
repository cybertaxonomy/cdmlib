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
import eu.etaxonomy.cdm.strategy.cache.PersonDefaultCacheStrategy;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import java.util.*;
import javax.persistence.*;

/**
 * A representation of a human being, living or dead.
 * It includes name parts, {@link Contact contact} details, {@link InstitutionalMembership institutional membership},
 * and other possible information such as life {@link common.TimePeriod time period},
 * taxonomic and/or geographical {@link common.Keyword specialization}.
 * For a short abbreviated name the inherited attribute {@link TeamOrPersonBase#getNomenclaturalTitle() nomenclaturalTitle}
 * is to be used.
 * For other alternative (string-)names {@link common.OriginalSource OriginalSource} instances must be created
 * and the inherited attribute {@link common.ReferencedEntityBase#getOriginalNameString() originalNameString} must be used.
 * <p>
 * See also the <a href="http://rs.tdwg.org/ontology/voc/Person.rdf">TDWG Ontology</a>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@Entity
public class Person extends TeamOrPersonBase {
	static Logger logger = Logger.getLogger(Person.class);

	private String prefix;
	private String firstname;
	private String lastname;
	private String suffix;
	private TimePeriod lifespan;
	protected Set<InstitutionalMembership> institutionalMemberships;
	private Contact contact;
	private Set<Keyword> keywords = new HashSet<Keyword>();

	/** 
	 * Creates a new empty instance for a person whose existence is all what is known.
	 * This can be a provisional solution until more information about this person
	 * can be gathered, for instance in case a member of a nomenclatural author team
	 * is not explicitly mentioned. It also includes the cache strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.PersonDefaultCacheStrategy PersonDefaultCacheStrategy}.
	 */
	public static Person NewInstance(){
		return new Person();
	}
	
	/** 
	 * Creates a new instance for a person for whom an "identification" string
	 * is all what is known. This string is generally a short or a complete name.
	 * As this string is kept in the {@link common.IdentifiableEntity#getTitleCache() titleCache}
	 * attribute and should not be overwritten by the {@link #generateTitle() generateTitle} method
	 * the {@link common.IdentifiableEntity#isProtectedTitleCache() protectedTitleCache} flag will be turned on. 
	 */
	public static Person NewTitledInstance(String titleCache){
		Person result = new Person();
		result.setTitleCache(titleCache);
		return result;
	}
	
	
	/** 
	 * Class constructor.
	 * 
	 * @see #Person(String, String, String)
	 */
	private Person() {
		super();
		this.cacheStrategy = PersonDefaultCacheStrategy.NewInstance();

	}
	
	/** 
	 * Class constructor using a "forenames" string (including initials),
	 * a surname (family name) and an abbreviated name as used in nomenclature.
	 * For the abbreviated name the inherited attribute {@link TeamOrPersonBase#getNomenclaturalTitle() nomenclaturalTitle}
	 * is used.
	 *
	 * @param  firstname     		the given name
	 * @param  lastname      		the hereditary name
	 * @param  nomenclaturalTitel 	the abbreviated name
	 * @see                  		#Person()
	 * @see                  		#NewInstance()
	 */
	public Person(String firstname, String lastname, String nomenclaturalTitel) {
		this.setFirstname(firstname);
		this.setLastname(lastname);
		this.setNomenclaturalTitle(nomenclaturalTitel);
	}
	
	
	/** 
	 * Returns the set of {@link InstitutionalMembership institution memberships} corresponding to this person. 
	 *
	 * @see     InstitutionalMembership
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	public Set<InstitutionalMembership> getInstitutionalMemberships(){
		return this.institutionalMemberships;
	}
	/** 
	 * @see     #getInstitutionalMemberships()
	 */
	protected void setInstitutionalMemberships(Set<InstitutionalMembership> institutionalMemberships){
		this.institutionalMemberships = institutionalMemberships;
	}
	
	/** 
	 * Adds a new {@link InstitutionalMembership membership} of this person in an {@link Institution institution}
	 * to the set of his institution memberships.
	 * This method also creates a new institutional membership instance.
	 *
	 * @param  institution  the institution this person belongs to
	 * @param  period       the time period for which this person has been a member of the institution
	 * @param  department   the string label for the department this person belongs to,
	 * 					    within the institution
	 * @param  role         the string label for the persons's role within the department or institution
	 * @see 			    #getInstitutionalMemberships()
	 * @see 			    InstitutionalMembership#InstitutionalMembership(Institution, Person, TimePeriod, String, String)
	 */
	public void addInstitutionalMembership(Institution institution, TimePeriod period, String department, String role){
		//TODO to be implemented?
		logger.warn("not yet fully implemented?");
		InstitutionalMembership ims = new InstitutionalMembership(institution, this, period, department, role); 
	}
	
	/** 
	 * Removes one element from the set of institutional memberships of this person.
	 *
	 * @param  ims  the institutional membership of this person which should be deleted
	 * @see     	#getInstitutionalMemberships()
	 */
	public void removeInstitutionalMembership(InstitutionalMembership ims){
		//TODO to be implemented?
		logger.warn("not yet fully implemented?");
		ims.setInstitute(null);
		ims.setPerson(null);
		this.institutionalMemberships.remove(ims);
	}


	/** 
	 * Returns the set of {@link common.Keyword keywords} mostly representing a taxonomic or
	 * a geographical specialization of this person.
	 * Keywords are items of a controlled {@link common.TermVocabulary vocabulary}.
	 *
	 * @see 	common.Keyword
	 */
	@OneToMany
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	public Set<Keyword> getKeywords(){
		return this.keywords;
	}
	/** 
	 * @see     #getKeywords()
	 */
	public void setKeywords(Set<Keyword> keywords){
		this.keywords = keywords;
	}
	/** 
	 * Adds a new keyword from the keyword vocabulary to the set of keywords
	 * describing or circumscribing this person's activities.
	 *
	 * @param  keyword  any keyword 
	 * @see 			#getKeywords()
	 * @see 			common.Keyword
	 */
	public void addKeyword(Keyword keyword){
		this.keywords.add(keyword);
	}
	/** 
	 * Removes one element from the set of keywords for this person.
	 *
	 * @param  keyword  the keyword which should be deleted
	 * @see             #getKeywords()
	 */
	public void removeKeyword(Keyword keyword){
		this.keywords.remove(keyword);
	}



	/** 
	 * Returns the {@link Contact contact} of this person.
	 * The contact contains several ways to approach this person.
	 *
	 * @see 	Contact
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Contact getContact(){
		return this.contact;
	}
	/**
	 * @see  #getContact()
	 */
	public void setContact(Contact contact){
		this.contact = contact;
	}

	
	/**
	 * Returns the string representing the prefix (for instance "Prof.&nbsp;Dr.<!-- -->")
	 * to this person's name.
	 */
	public String getPrefix(){
		return this.prefix;
	}
	/**
	 * @see  #getPrefix()
	 */
	public void setPrefix(String prefix){
		this.prefix = prefix;
	}


	/**
	 * Returns the string representing the given name or forename
	 * (for instance "John") of this person. 
	 * This is the part of his name which is not shared with other
	 * family members. Actually it may be just initials (for instance "G. Jr."),
	 * all forenames in full or a combination of expanded names and initials. 
	 */
	public String getFirstname(){
		return this.firstname;
	}
	/**
	 * @see  #getFirstname()
	 */
	public void setFirstname(String firstname){
		this.firstname = firstname;
	}

	
	/**
	 * Returns the string representing the hereditary name (surname or family name)
	 * (for instance "Smith") of this person. 
	 * This is the part of his name which is common to (all) other
	 * members of his family, as distinct from the given name or forename. 
	 */
	public String getLastname(){
		return this.lastname;
	}
	/**
	 * @see  #getLastname()
	 */
	public void setLastname(String lastname){
		this.lastname = lastname;
	}


	/**
	 * Returns the string representing the suffix (for instance "Junior")
	 * of this person's name.
	 */
	public String getSuffix(){
		return this.suffix;
	}
	/**
	 * @see  #getSuffix()
	 */
	public void setSuffix(String suffix){
		this.suffix = suffix;
	}


	/** 
	 * Returns the {@link common.TimePeriod period of time}
	 * in which this person was alive (life span).
	 * The general form is birth date - death date
	 * (XXXX - YYYY; XXXX - or - YYYY as appropriate),
	 * but a simple flourished date (fl. XXXX) is also possible
	 * if that is all what is known.
	 *
	 * @see  common.TimePeriod
	 */
	public TimePeriod getLifespan(){
		return this.lifespan;
	}
	/**
	 * @see  #getLifespan()
	 */
	public void setLifespan(TimePeriod lifespan){
		this.lifespan = lifespan;
	}

	/**
	 * Generates the "full" name string of this person according to the strategy
	 * defined in {@link eu.etaxonomy.cdm.strategy.cache.PersonDefaultCacheStrategy PersonDefaultCacheStrategy}.
	 * The used attributes are:
	 * {@link #prefix prefix}, {@link #firstname firstname}, {@link #lastname lastname} and {@link #suffix suffix}.
	 * This method overrides {@link common.IdentifiableEntity#generateTitle() generateTitle}.
	 * The result might be kept as {@link common.IdentifiableEntity#setTitleCache(String) titleCache} if the
	 * flag {@link common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
	 * 
	 * @return  the string with the full name of this person
	 */
	@Override
	public String generateTitle(){
		return cacheStrategy.getTitleCache(this);
	}

}