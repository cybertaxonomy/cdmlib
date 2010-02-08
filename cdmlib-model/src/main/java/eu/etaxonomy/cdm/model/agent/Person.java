/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.joda.time.DateTime;
import org.joda.time.Partial;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.validation.annotation.NullOrNotEmpty;

/**
 * This class represents human beings, living or dead.<BR>
 * It includes name parts, {@link Contact contact} details, {@link InstitutionalMembership institutional membership},
 * and other possible information such as life {@link TimePeriod time period},
 * taxonomic and/or geographical {@link Keyword specialization}.
 * For a short abbreviated name the inherited attribute {@link TeamOrPersonBase#getNomenclaturalTitle() nomenclaturalTitle}
 * is to be used.<BR>
 * For other alternative (string-)names {@link eu.etaxonomy.cdm.model.common.OriginalSourceBase OriginalSource} instances must be created
 * and the inherited attribute {@link eu.etaxonomy.cdm.model.common.ReferencedEntityBase#getOriginalNameString() originalNameString} must be used.
 * <P>
 * This class corresponds to: <ul>
 * <li> Person according to the TDWG ontology
 * <li> AgentName (partially) according to the TCS
 * <li> Person (PersonName partially) according to the ABCD schema
 * </ul>
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:42
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Person", propOrder = {
	    "prefix",
	    "firstname",
	    "lastname",
	    "suffix",
	    "lifespan",
	    "institutionalMemberships"
})
@XmlRootElement(name = "Person")
@Entity
@Indexed(index = "eu.etaxonomy.cdm.model.agent.AgentBase")
@Audited
@Configurable
public class Person extends TeamOrPersonBase<Person>{
	private static final long serialVersionUID = 4153566493065539763L;
	public static final Logger logger = Logger.getLogger(Person.class);

    @XmlElement(name = "Prefix")
    @Field(index=Index.TOKENIZED)
    @NullOrNotEmpty
    @Size(max = 255)
	private String prefix;
    
    @XmlElement(name = "FirstName")
    @Field(index=Index.TOKENIZED)
    @NullOrNotEmpty
    @Size(max = 255)
	private String firstname;
	
    @XmlElement(name = "LastName")
    @Field(index=Index.TOKENIZED)
    @NullOrNotEmpty
    @Size(max = 255)
	private String lastname;
	
    @XmlElement(name = "Suffix")
    @Field(index=Index.TOKENIZED)
    @NullOrNotEmpty
    @Size(max = 255)
	private String suffix;
	
    @XmlElement(name = "Lifespan")
    @IndexedEmbedded
    @Match(value=MatchMode.EQUAL_OR_ONE_NULL)
    @NotNull
	private TimePeriod lifespan = TimePeriod.NewInstance();
	
    @XmlElementWrapper(name = "InstitutionalMemberships", nillable = true)
    @XmlElement(name = "InstitutionalMembership")
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "person")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	@NotNull
	protected Set<InstitutionalMembership> institutionalMemberships;

	/** 
	 * Creates a new empty instance for a person whose existence is all what is known.
	 * This can be a provisional solution until more information about <i>this</i> person
	 * can be gathered, for instance in case a member of a nomenclatural author team
	 * is not explicitly mentioned. It also includes the cache strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy PersonDefaultCacheStrategy}.
	 */
	public static Person NewInstance(){
		return new Person();
	}
	
	/** 
	 * Creates a new instance for a person for whom an "identification" string
	 * is all what is known. This string is generally a short or a complete name.
	 * As this string is kept in the {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#getTitleCache() titleCache}
	 * attribute and should not be overwritten by the {@link #generateTitle() generateTitle} method
	 * the {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#isProtectedTitleCache() protectedTitleCache} flag will be turned on. 
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
	protected Person() {
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
		logger.debug("before - Set nomenclatural Title");
		this.setNomenclaturalTitle(nomenclaturalTitel);
		logger.debug("after - Set nomenclatural Title");
	}
	
	
	/** 
	 * Returns the set of {@link InstitutionalMembership institution memberships} corresponding to <i>this</i> person. 
	 *
	 * @see     InstitutionalMembership
	 */
	public Set<InstitutionalMembership> getInstitutionalMemberships(){
		if(institutionalMemberships == null) {
			this.institutionalMemberships = new HashSet<InstitutionalMembership>();
		}
		return this.institutionalMemberships;
	}

	protected void addInstitutionalMembership(InstitutionalMembership ims){
		getInstitutionalMemberships().add(ims);
		if (ims.getPerson() != this){
			logger.warn("Institutional membership's person has to be changed for adding it to person: " + this);
			ims.getPerson().removeInstitutionalMembership(ims);
			ims.setPerson(this);
			
		}
	}
	
	/** 
	 * Adds a new {@link InstitutionalMembership membership} of <i>this</i> person in an {@link Institution institution}
	 * to the set of his institution memberships.
	 * This method also creates a new institutional membership instance.
	 *
	 * @param  institution  the institution <i>this</i> person belongs to
	 * @param  period       the time period for which <i>this</i> person has been a member of the institution
	 * @param  department   the string label for the department <i>this</i> person belongs to,
	 * 					    within the institution
	 * @param  role         the string label for the persons's role within the department or institution
	 * @see 			    #getInstitutionalMemberships()
	 * @see 			    InstitutionalMembership#InstitutionalMembership(Institution, Person, TimePeriod, String, String)
	 */
	public void addInstitutionalMembership(Institution institution, TimePeriod period, String department, String role){
		new InstitutionalMembership(institution, this, period, department, role);
	}
	
	/** 
	 * Removes one element from the set of institutional memberships of <i>this</i> person.
	 * Institute and person attributes of the institutional membership object
	 * will be nullified.
	 *
	 * @param  ims  the institutional membership of <i>this</i> person which should be deleted
	 * @see     	#getInstitutionalMemberships()
	 */
	public void removeInstitutionalMembership(InstitutionalMembership ims){
		ims.setInstitute(null);
		ims.setPerson(null);
		getInstitutionalMemberships().remove(ims);
	}

	/**
	 * Returns the string representing the prefix (for instance "Prof.&nbsp;Dr.<!-- -->")
	 * to <i>this</i> person's name.
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
	 * (for instance "John") of <i>this</i> person. 
	 * This is the part of his name which is not shared with other
	 * family members. Actually it may be just initials (for instance "G.&nbsp;Jr."),
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
	 * (for instance "Smith") of <i>this</i> person. 
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
	 * of <i>this</i> person's name.
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
	 * Returns the {@link eu.etaxonomy.cdm.model.common.TimePeriod period of time}
	 * in which <i>this</i> person was alive (life span).
	 * The general form is birth date - death date
	 * (XXXX - YYYY; XXXX - or - YYYY as appropriate),
	 * but a simple flourished date (fl. XXXX) is also possible
	 * if that is all what is known.
	 *
	 * @see  eu.etaxonomy.cdm.model.common.TimePeriod
	 */
	public TimePeriod getLifespan(){
		if(lifespan == null) {
			this.lifespan = TimePeriod.NewInstance(new Partial(), new Partial());
		}
		return this.lifespan;
	}
	/**
	 * @see  #getLifespan()
	 */
	public void setLifespan(TimePeriod lifespan){
		this.lifespan = lifespan;
	}

//	/**
//	 * Generates the "full" name string of <i>this</i> person according to the strategy
//	 * defined in {@link eu.etaxonomy.cdm.strategy.cache.agent.PersonDefaultCacheStrategy PersonDefaultCacheStrategy}.
//	 * The used attributes are:
//	 * {@link #getPrefix() prefix}, {@link #getFirstname() firstname}, {@link #getLastname() lastname} and {@link #getSuffix() suffix}.
//	 * This method overrides {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle() generateTitle}.
//	 * The result might be kept as {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#setTitleCache(String) titleCache} if the
//	 * flag {@link eu.etaxonomy.cdm.model.common.IdentifiableEntity#protectedTitleCache protectedTitleCache} is not set.
//	 * 
//	 * @return  the string with the full name of <i>this</i> person
//	 */
//	@Override
//	public String generateTitle() {
//		String title = null;
//		if (cacheStrategy != null) {
//		title = cacheStrategy.getTitleCache(this);
//		} 
//        return title;
//	}

}