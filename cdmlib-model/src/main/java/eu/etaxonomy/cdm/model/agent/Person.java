/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.agent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.springframework.beans.factory.annotation.Configurable;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.search.OrcidBridge;
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
 * For other alternative (string-)names {@link eu.etaxonomy.cdm.model.reference.OriginalSourceBase OriginalSource} instances must be created
 * and the attribute {@link eu.etaxonomy.cdm.model.common.OriginalSourceBase#getOriginalInfo() originalInfo} must be used.
 * <P>
 * This class corresponds to: <ul>
 * <li> Person according to the TDWG ontology
 * <li> AgentName (partially) according to the TCS
 * <li> Person (PersonName partially) according to the ABCD schema
 * </ul>
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:42
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Person", propOrder = {
	    "prefix",
	    "familyName",
	    "givenName",
	    "initials",
	    "suffix",
	    "nomenclaturalTitle",
	    "collectorTitle",
	    "lifespan",
	    "orcid",
	    "institutionalMemberships"
})
@XmlRootElement(name = "Person")
@Entity
//@Indexed disabled to reduce clutter in indexes, since this type is not used by any search
//@Indexed(index = "eu.etaxonomy.cdm.model.agent.AgentBase")
@Audited
@Configurable
public class Person extends TeamOrPersonBase<Person>{

    private static final long serialVersionUID = 4153566493065539763L;
	private static final Logger logger = LogManager.getLogger();

    @XmlElement(name="NomenclaturalTitle")
    @Field(index=Index.YES)
    @NullOrNotEmpty
    @Column(length=255)
    protected String nomenclaturalTitle;

    @XmlElement(name="CollectorTitle")
    @Field(index=Index.YES)
    @NullOrNotEmpty
    @Column(length=255)
    private String collectorTitle;

    @XmlElement(name = "Prefix")
    @Field
  //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	private String prefix;

    @XmlElement(name = "GivenName")
    @Field
  //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	private String givenName;

    @XmlElement(name = "Initials")
    @Field
    @NullOrNotEmpty
    @Column(length=80)
    private String initials;

    @XmlElement(name = "FamilyName")
    @Field
  //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	private String familyName;

    @XmlElement(name = "Suffix")
    @Field
  //TODO Val #3379
//    @NullOrNotEmpty
    @Column(length=255)
	private String suffix;

    @XmlElement(name = "Lifespan")
    @IndexedEmbedded
    @Match(value=MatchMode.EQUAL_OR_ONE_NULL)
  //TODO Val #3379    check carefully what the condition is that lifespan is really null in legacy data
//    @NotNull
	private TimePeriod lifespan = TimePeriod.NewInstance();

    @XmlElement(name = "Orcid")
    @Field
    @FieldBridge(impl = OrcidBridge.class)
    @Type(type="orcidUserType")
    @Column(length=16)
    private ORCID orcid;

    @XmlElementWrapper(name = "InstitutionalMemberships", nillable = true)
    @XmlElement(name = "InstitutionalMembership")
    @OneToMany(fetch=FetchType.LAZY, mappedBy = "person")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	protected Set<InstitutionalMembership> institutionalMemberships;

// *********************** FACTORY **********************************/

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
		result.setTitleCache(titleCache, true);
		return result;
	}

	public static Person NewInstance(String nomRefTitle, String familyName, String initials, String givenName){
        Person result = new Person();
        result.setNomenclaturalTitle(nomRefTitle);
        result.setFamilyName(familyName);
        result.setInitials(initials);
        result.setGivenName(givenName);
        return result;
    }

// *********************** CONSTRUCTOR **********************************/

	/**
	 * Class constructor.
	 *
	 * @see #Person(String, String, String)
	 */
	protected Person() {
		super();
	}

	/**
	 * Class constructor using a "forenames" string (including initials),
	 * a surname (family name) and an abbreviated name as used in nomenclature.
	 * For the abbreviated name the inherited attribute {@link TeamOrPersonBase#getNomenclaturalTitle() nomenclaturalTitle}
	 * is used.
	 *
	 * @param  givenname     		the given name
	 * @param  familyname      		the hereditary name
	 * @param  nomenclaturalTitel 	the abbreviated name
	 * @see                  		#Person()
	 * @see                  		#NewInstance()
	 */
	public Person(String givenname, String familyname, String nomenclaturalTitel) {
		this.setGivenName(givenname);
		this.setFamilyName(familyname);
		logger.debug("before - Set nomenclatural Title");
		this.setNomenclaturalTitle(nomenclaturalTitel);
		logger.debug("after - Set nomenclatural Title");
	}

    @Override
    protected void initDefaultCacheStrategy() {
        this.cacheStrategy = PersonDefaultCacheStrategy.NewInstance();
    }

    @Override
    public void initListener(){
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                if (!ev.getPropertyName().equals("nomenclaturalTitleCache") //not sure if called at all
                        && !ev.getPropertyName().equals("collectorTitleCache") //not sure if called at all
                        && !ev.getPropertyName().equals("cacheStrategy")){
                    if (!ev.getPropertyName().equals("titleCache") && ! isProtectedTitleCache()){
                        titleCache = null;
                    }
                    nomenclaturalTitleCache = null;
                    collectorTitleCache = null;
                }
            }
        };
        addPropertyChangeListener(listener);
    }

// *********************** GETTER SETTER ADDER **********************************/

    /**
	 * Returns the set of {@link InstitutionalMembership institution memberships} corresponding to <i>this</i> person.
	 *
	 * @see     InstitutionalMembership
	 */
	public Set<InstitutionalMembership> getInstitutionalMemberships(){
		if(institutionalMemberships == null) {
			this.institutionalMemberships = new HashSet<>();
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
	public InstitutionalMembership addInstitutionalMembership(Institution institution, TimePeriod period, String department, String role){
		return new InstitutionalMembership(institution, this, period, department, role);
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
		this.prefix = isBlank(prefix) ? null : prefix;
	}

	/**
	 * Returns the string representing the given name or forename
	 * (for instance "John") of <i>this</i> person.
	 * This is the part of his name which is not shared with other
	 * family members. <BR>
	 * Pure initials should be stored in {@link #getInitials() initials}
	 * A combination of expanded names and initials maybe stored here.
	 * <BR> In user interfaces (UI) this field should better be called
	 * "Other/given names" according to {@link https://www.w3.org/International/questions/qa-personal-names.en#fielddesign }.
	 *
	 * @see #getInitials()
	 * @see #getFamilyName()
	 * @see https://www.w3.org/International/questions/qa-personal-names.en#fielddesign
	 */
	public String getGivenName(){
		return this.givenName;
	}
	/**
	 * @see  #getGivenName()
	 */
	public void setGivenName(String givenName){
		this.givenName = isBlank(givenName) ? null : givenName;
	}

	//#4311
    public String getCollectorTitle() {
        return collectorTitle;
    }
    public void setCollectorTitle(String collectorTitle) {
        this.collectorTitle = collectorTitle;
    }

    public String getNomenclaturalTitle() {
        return nomenclaturalTitle;
    }
    /**
     * Sets the nomenclatural title.
     */
    public void setNomenclaturalTitle(String nomenclaturalTitle) {
        this.nomenclaturalTitle = isBlank(nomenclaturalTitle) ? null : nomenclaturalTitle;
    }
    @Override
    public void setNomenclaturalTitleCache(String nomenclaturalTitleCache, boolean protectCache){
        this.nomenclaturalTitleCache = nomenclaturalTitleCache;
        if (protectCache){
            this.setNomenclaturalTitle(nomenclaturalTitleCache);
        }
    }

    /**
     * Returns the initials of this person as used in bibliographic
     * references. Usually these are the first letters of each given name
     * followed by "." per given name. For East Asian names it may
     * be the first 2 letters. Also dashes are kept.
     * @return the initials
     */
    public String getInitials(){
        return this.initials;
    }
    /**
     * @see  #getInitals()
     */
    public void setInitials(String initials){
        this.initials = isBlank(initials) ? null : initials;
    }

	/**
	 * Returns the string representing the hereditary name (surname or family name)
	 * (for instance "Smith") of <i>this</i> person.
	 * This is the part of his name which is common to (all) other
	 * members of his family, as distinct from the given name or forename.
	 *
     * <BR> In user interfaces (UI) this field should better be called
     * "Family name" according to {@link https://www.w3.org/International/questions/qa-personal-names.en#fielddesign }.
     *
     * @see #getInitials()
     * @see #getGivenName()
     * @see https://www.w3.org/International/questions/qa-personal-names.en#fielddesign
	 */
	public String getFamilyName(){
		return this.familyName;
	}
	/**
	 * @see  #getfamilyName()
	 */
	public void setFamilyName(String familyName){
		this.familyName = isBlank(familyName) ? null : familyName;
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
		this.suffix = isBlank(suffix) ? null: suffix;
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
			this.lifespan = TimePeriod.NewInstance();
		}
		return this.lifespan;
	}
	/**
	 * @see  #getLifespan()
	 */
	public void setLifespan(TimePeriod lifespan){
		this.lifespan = lifespan != null? lifespan : TimePeriod.NewInstance();
	}

    /**
     * The {@link ORCID ORCiD} of this person.<BR>
     * See https://orcid.org/ for information on ORCiD.
     * @return the ORCiD
     */
    public ORCID getOrcid() {
        return orcid;
    }
    /**
     * @see #getOrcid()
     */
    public void setOrcid(ORCID orcid) {
        this.orcid = orcid;
    }

    @Override
    public boolean updateCaches(){
        boolean result = false;
        result |= super.updateCaches();
        result |= updateNomenclaturalCache();
        result |= updateCollectorCache();

        return result;
    }

    private boolean updateNomenclaturalCache() {
        //updates the nomenclaturalTitleCache if necessary
        String oldCache = this.nomenclaturalTitleCache;
        String newCache = cacheStrategy().getNomenclaturalTitleCache(this);
        if (!CdmUtils.nullSafeEqual(oldCache, newCache)){
//            this.setNomenclaturalTitleCache(null, false);
            this.getNomenclaturalTitleCache();
            return true;
        }
        return false;
    }

    private boolean updateCollectorCache() {
        //updates the collectorTitleCache if necessary
        String oldCache = this.collectorTitleCache;
        String newCache = cacheStrategy().getCollectorTitleCache(this);
        if (!CdmUtils.nullSafeEqual(oldCache, newCache)){
//            this.setNomenclaturalTitleCache(null, false);
            this.getCollectorTitleCache();
            return true;
        }
        return false;
     }

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> Person. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> Person.
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Person clone() {
		try{
			Person result = (Person)super.clone();
			//no changes to givenname, familyname, lifespan, prefix, suffix
			return result;
		} catch (CloneNotSupportedException e){
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}