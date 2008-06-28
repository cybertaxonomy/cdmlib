/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Target;

import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy;

/**
 * The taxon name class for all non viral taxa. Parentetical authorship is derived
 * from basionym relationship. The scientific name including author strings and
 * maybe year can be stored as a string in the inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
 * The scientific name string without author strings and year can be stored in the {@link #getNameCache() nameCache} attribute.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "nameCache",
    "genusOrUninomial",
    "infraGenericEpithet",
    "specificEpithet",
    "infraSpecificEpithet",
    "combinationAuthorTeam",
    "exCombinationAuthorTeam",
    "basionymAuthorTeam",
    "exBasionymAuthorTeam",
    "authorshipCache",
    "protectedAuthorshipCache",
    "protectedNameCache",
    "cacheStrategy"
})
@XmlRootElement(name = "NonViralName")
@Entity
public class NonViralName<T extends NonViralName> extends TaxonNameBase<NonViralName, INonViralNameCacheStrategy> {
	
	private static final Logger logger = Logger.getLogger(NonViralName.class);
	
	@XmlElement(name = "NameCache")
	private String nameCache;
	
	@XmlElement(name = "GenusOrUninomial")
	private String genusOrUninomial;
	
	@XmlElement(name = "InfraGenericEpithet")
	private String infraGenericEpithet;
	
	@XmlElement(name = "SpecificEpithet")
	private String specificEpithet;
	
	@XmlElement(name = "InfraSpecificEpithet")
	private String infraSpecificEpithet;
	
	@XmlElement(name = "CombinationAuthorTeam")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private INomenclaturalAuthor combinationAuthorTeam;
	
	@XmlElement(name = "ExCombinationAuthorTeam")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private INomenclaturalAuthor exCombinationAuthorTeam;
	
	@XmlElement(name = "BasionymAuthorTeam")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private INomenclaturalAuthor basionymAuthorTeam;
	
	@XmlElement(name = "ExBasionymAuthorTeam")
    //@XmlIDREF
    //@XmlSchemaType(name = "IDREF")
	private INomenclaturalAuthor exBasionymAuthorTeam;
	
	@XmlElement(name = "AuthorshipCache")
	private String authorshipCache;
	
	@XmlElement(name = "ProtectedAuthorshipCache")
	protected boolean protectedAuthorshipCache;
	
	@XmlElement(name = "ProtectedNameCache")
	protected boolean protectedNameCache;

	@XmlElement(name = "CacheStrategy")
	protected INonViralNameCacheStrategy cacheStrategy;
	
	// ************* CONSTRUCTORS *************/	
	
	//needed by hibernate
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #NonViralName(Rank, HomotypicalGroup)
	 * @see #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(){
		super();
		setNameCacheStrategy();
	}
	
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this non viral taxon name
	 * @param	homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, String, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
	}
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group},
	 * its scientific name components, its {@link agent.TeamOrPersonBase author(team)},
	 * its {@link reference.INomenclaturalReference nomenclatural reference} and
	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this non viral taxon name
	 * @param	genusOrUninomial the string for this taxon name
	 * 			if its rank is genus or higher or for the genus part
	 * 			if its rank is lower than genus
	 * @param	infraGenericEpithet  the string for the first epithet of
	 * 			this non viral taxon name if its rank is lower than genus
	 * 			and higher than species aggregate
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this non viral taxon name if its rank is species aggregate or lower
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this non viral taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this non viral taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this non viral taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(Rank rank, String genusOrUninomial, String infraGenericEpithet, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
		setGenusOrUninomial(genusOrUninomial);
		setInfraGenericEpithet (infraGenericEpithet);
		setSpecificEpithet(specificEpithet);
		setInfraSpecificEpithet(infraSpecificEpithet);
		setCombinationAuthorTeam(combinationAuthorTeam);
		setNomenclaturalReference(nomenclaturalReference);
		this.setNomenclaturalMicroReference(nomenclMicroRef);
	}
	
	//********* METHODS **************************************/
	/** 
	 * Creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param  rank  the rank to be assigned to this non viral taxon name
	 * @see    #NewInstance(Rank, HomotypicalGroup)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static NonViralName NewInstance(Rank rank){
		return new NonViralName(rank, null);
	}

	/** 
	 * Creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * its {@link common.HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.name.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new non viral taxon name instance will be also added to the set of
	 * non viral taxon names belonging to this homotypical group. If the homotypical 
	 * group does not exist a new instance will be created for it.
	 * 
	 * @param  rank  the rank to be assigned to this non viral taxon name
	 * @param  homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	public static NonViralName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new NonViralName(rank, homotypicalGroup);
	}
	
	private void setNameCacheStrategy(){
		if (getClass() == NonViralName.class){
			this.cacheStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
		}
		
	}
	
	//TODO for PROTOTYPE
	/**
	 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy cache strategy} used to generate
	 * several strings corresponding to this non viral taxon name
	 * (in particular taxon name caches and author strings).
	 * 
	 * @return  the cache strategy used for this non viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy
	 * @see     eu.etaxonomy.cdm.strategy.cache.common.IIdentifiableEntityCacheStrategy
	 */
	@Transient
	@Override
	public INonViralNameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}
	/**
	 * @see  #getCacheStrategy()
	 */
	@Override
	public void setCacheStrategy(INonViralNameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}
	
	

	/**
	 * Returns the {@link agent.INomenclaturalAuthor author (team)} that published this non viral
	 * taxon name.
	 * 
	 * @return  the nomenclatural author (team) of this non viral taxon name
	 * @see 	agent.INomenclaturalAuthor
	 * @see 	agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getCombinationAuthorTeam(){
		return this.combinationAuthorTeam;
	}
	/**
	 * @see  #getCombinationAuthorTeam()
	 */
	public void setCombinationAuthorTeam(INomenclaturalAuthor combinationAuthorTeam){
		this.combinationAuthorTeam = combinationAuthorTeam;
	}

	/**
	 * Returns the {@link agent.INomenclaturalAuthor author (team)} that contributed to
	 * the publication of this non viral taxon name as generally stated by
	 * the {@link #getCombinationAuthorTeam() combination author (team)} itself.
	 * The presence of an author (team) of this non viral taxon name is a
	 * condition for the existence of an ex author (team) for this same name. 
	 * 
	 * @return  the nomenclatural ex author (team) of this non viral taxon name
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	agent.INomenclaturalAuthor
	 * @see 	agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getExCombinationAuthorTeam(){
		return this.exCombinationAuthorTeam;
	}
	/**
	 * @see  #getExCombinationAuthorTeam()
	 */
	public void setExCombinationAuthorTeam(INomenclaturalAuthor exCombinationAuthorTeam){
		this.exCombinationAuthorTeam = exCombinationAuthorTeam;
	}

	/**
	 * Returns the {@link agent.INomenclaturalAuthor author (team)} that published the original combination
	 * on which this non viral taxon name is nomenclaturally based. Such an
	 * author (team) can only exist if this non viral taxon name is a new
	 * combination due to a taxonomical revision.
	 * 
	 * @return  the nomenclatural basionym author (team) of this non viral taxon name
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	agent.INomenclaturalAuthor
	 * @see 	agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getBasionymAuthorTeam(){
		return basionymAuthorTeam;
	}
	/**
	 * @see  #getBasionymAuthorTeam()
	 */
	public void setBasionymAuthorTeam(INomenclaturalAuthor basionymAuthorTeam) {
		this.basionymAuthorTeam = basionymAuthorTeam;
	}

	/**
	 * Returns the {@link agent.INomenclaturalAuthor author (team)} that contributed to
	 * the publication of the original combination this non viral taxon name is
	 * based on. This should have been generally stated by
	 * the {@link #getCombinationAuthorTeam() basionym author (team)} itself.
	 * The presence of a basionym author (team) of this non viral taxon name is a
	 * condition for the existence of an ex basionym author (team)
	 * for this same name. 
	 * 
	 * @return  the nomenclatural ex basionym author (team) of this non viral taxon name
	 * @see 	#getBasionymAuthorTeam()
	 * @see 	#getCombinationAuthorTeam()
	 * @see 	agent.INomenclaturalAuthor
	 * @see 	agent.TeamOrPersonBase#getNomenclaturalTitle()
	 */
	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getExBasionymAuthorTeam(){
		return exBasionymAuthorTeam;
	}
	/**
	 * @see  #getExBasionymAuthorTeam()
	 */
	public void setExBasionymAuthorTeam(INomenclaturalAuthor exBasionymAuthorTeam) {
		this.exBasionymAuthorTeam = exBasionymAuthorTeam;
	}
	/**
	 * Returns either the scientific name string (without authorship) for this
	 * non viral taxon name if its rank is genus or higher (monomial) or the string for
	 * the genus part of it if its {@link Rank rank} is lower than genus (bi- or trinomial).
	 * Genus or uninomial strings begin with an upper case letter.
	 * 
	 * @return  the string containing the suprageneric name, the genus name or the genus part of this non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getGenusOrUninomial() {
		return genusOrUninomial;
	}
	
	/**
	 * @see  #getGenusOrUninomial()
	 */
	public void setGenusOrUninomial(String genusOrUninomial) {
		this.genusOrUninomial = genusOrUninomial;
	}

	/**
	 * Returns the genus subdivision epithet string (infrageneric part) for
	 * this non viral taxon name if its {@link Rank rank} is infrageneric (lower than genus and
	 * higher than species aggregate). Genus subdivision epithet strings begin
	 * with an upper case letter.
	 * 
	 * @return  the string containing the infrageneric part of this non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getInfraGenericEpithet(){
		return this.infraGenericEpithet;
	}

	/**
	 * @see  #getInfraGenericEpithet()
	 */
	public void setInfraGenericEpithet(String infraGenericEpithet){
		this.infraGenericEpithet = infraGenericEpithet;
	}

	/**
	 * Returns the species epithet string for this non viral taxon name if its {@link Rank rank} is
	 * species aggregate or lower. Species epithet strings begin with a
	 * lower case letter.
	 * 
	 * @return  the string containing the species epithet of this non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getSpecificEpithet(){
		return this.specificEpithet;
	}

	/**
	 * @see  #getSpecificEpithet()
	 */
	public void setSpecificEpithet(String specificEpithet){
		this.specificEpithet = specificEpithet;
	}

	/**
	 * Returns the species subdivision epithet string (infraspecific part) for
	 * this non viral taxon name if its {@link Rank rank} is infraspecific (lower than species).
	 * Species subdivision epithet strings begin with a lower case letter.
	 * 
	 * @return  the string containing the infraspecific part of this non viral taxon name
	 * @see 	#getNameCache()
	 */
	public String getInfraSpecificEpithet(){
		return this.infraSpecificEpithet;
	}

	/**
	 * @see  #getInfraSpecificEpithet()
	 */
	public void setInfraSpecificEpithet(String infraSpecificEpithet){
		this.infraSpecificEpithet = infraSpecificEpithet;
	}

	/**
	 * Generates and returns the string with the scientific name of this
	 * non viral taxon name including author strings and maybe year according to
	 * the strategy defined in
	 *  {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
	 * This string may be stored in the inherited
	 * {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * TaxonNameBase#generateTitle() method.
	 *
	 * @return  the string with the composed name of this non viral taxon name with authorship (and maybe year)
	 * @see  	common.IdentifiableEntity#generateTitle()
	 * @see  	common.IdentifiableEntity#getTitleCache()
	 * @see  	TaxonNameBase#generateTitle()
	 */
	@Override
	public String generateTitle(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return cacheStrategy.getTitleCache(this);
		}
	}
	
	/**
	 * Generates the composed name string of this non viral taxon name without author
	 * strings or year according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
	 * The result might be stored in {@link #getNameCache() nameCache} if the
	 * flag {@link #isProtectedNameCache() protectedNameCache} is not set.
	 * 
	 * @return  the string with the composed name of this non viral taxon name without authors or year
	 * @see 	#getNameCache()
	 */
	protected String generateNameCache(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for taxonName: " + this.toString());
			return null;
		}else{
			return cacheStrategy.getNameCache(this);
		}
	}
	
	/**
	 * Returns or generates the nameCache (scientific name
	 * without author strings and year) string for this non viral taxon name. If the
	 * {@link #isProtectedNameCache() protectedNameCache} flag is not set (False)
	 * the string will be generated according to a defined strategy,
	 * otherwise the value of the actual nameCache string will be returned.
	 * 
	 * @return  the string which identifies this non viral taxon name (without authors or year)
	 * @see 	#generateNameCache()
	 */
	public String getNameCache() {
		if (protectedNameCache){
			return this.nameCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (nameCache == null){
			this.nameCache = generateNameCache();
		}
		return nameCache;
	}

	/**
	 * Assigns a nameCache string to this non viral taxon name and protects it from being overwritten.
	 *  
	 * @param  nameCache  the string which identifies this non viral taxon name (without authors or year)
	 * @see	   #getNameCache()
	 */
	public void setNameCache(String nameCache){
		this.nameCache = nameCache;
		this.setProtectedTitleCache(false);
		this.setProtectedNameCache(true);
	}
	
	/**
	 * Returns the boolean value of the flag intended to protect (true)
	 * or not (false) the {@link #getNameCache() nameCache} (scientific name without author strings and year)
	 * string of this non viral taxon name.
	 *  
	 * @return  the boolean value of the protectedNameCache flag
	 * @see     #getNameCache()
	 */
	public boolean isProtectedNameCache() {
		return protectedNameCache;
	}

	/** 
	 * @see     #isProtectedNameCache()
	 */
	public void setProtectedNameCache(boolean protectedNameCache) {
		this.protectedNameCache = protectedNameCache;
	}

	
	/**
	 * Generates and returns a concatenated and formated authorteams string
	 * including basionym and combination authors of this non viral taxon name
	 * according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName) INonViralNameCacheStrategy}.
	 * 
	 * @return  the string with the concatenated and formated authorteams for this non viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.name.INonViralNameCacheStrategy#getAuthorshipCache(NonViralName)
	 */
	public String generateAuthorship(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return ((INonViralNameCacheStrategy<T>)cacheStrategy).getAuthorshipCache((T)this);
		}
	}

	/**
	 * Returns the concatenated and formated authorteams string including
	 * basionym and combination authors of this non viral taxon name.
	 * If the protectedAuthorshipCache flag is set this method returns the
	 * string stored in the the authorshipCache attribute, otherwise it
	 * generates the complete authorship string, returns it and stores it in
	 * the authorshipCache attribute.
	 * 
	 * @return  the string with the concatenated and formated authorteams for this non viral taxon name
	 * @see 	#generateAuthorship()
	 */
	public String getAuthorshipCache() {
		if (protectedAuthorshipCache){
			return this.authorshipCache;			
		}
		// is title dirty, i.e. equal NULL?
		if (authorshipCache == null){
			this.authorshipCache = generateAuthorship();
		}else{
			//TODO get is Dirty of authors
			this.authorshipCache = generateAuthorship();
		}
		return authorshipCache;
	}

	/**
	 * Assigns an authorshipCache string to this non viral taxon name.
	 *  
	 * @param  authorshipCache  the string which identifies the complete authorship of this non viral taxon name
	 * @see	   #getAuthorshipCache()
	 */
	public void setAuthorshipCache(String authorshipCache) {
		this.authorshipCache = authorshipCache;
	}

	
	
	/**
	 * Returns the boolean value "false" since the components of this taxon name
	 * cannot follow the rules of a corresponding {@link NomenclaturalCode nomenclatural code}
	 * which is not defined for this class. The nomenclature code depends on
	 * the concrete name subclass ({@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} or
	 * {@link ZoologicalName ZoologicalName} to which this non viral taxon name belongs.
	 * This method overrides the isCodeCompliant method from the abstract
	 * {@link TaxonNameBase#isCodeCompliant() TaxonNameBase} class.
	 *  
	 * @return  false
	 * @see	   	TaxonNameBase#isCodeCompliant()
	 */
	@Override
	@Transient
	public boolean isCodeCompliant() {
		//FIXME
		logger.warn("is CodeCompliant not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.name.TaxonNameBase#getNomeclaturalCode()
	 */
	/**
	 * Returns null as {@link NomenclaturalCode nomenclatural code} that governs
	 * the construction of this non viral taxon name since there is no specific
	 * nomenclatural code defined. The real implementention takes place in the
	 * subclasses {@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} and
	 * {@link ZoologicalName ZoologicalName}.
	 * This method overrides the getNomeclaturalCode method from {@link TaxonNameBase#getNomeclaturalCode() TaxonNameBase}.
	 *
	 * @return  null
	 * @see  	#isCodeCompliant()
	 * @see  	TaxonNameBase#getHasProblem()
	 */
	@Transient
	@Override
	public NomenclaturalCode getNomenclaturalCode() {
		//TODO What is the purpose of overriding the inherited method? 
		logger.warn("Non Viral Name has no specific Code defined. Use subclasses");
		return null;
	}

	/**
	 * Returns the boolean value of the flag intended to protect (true)
	 * or not (false) the {@link #getAuthorshipCache() authorshipCache} (complete authorship string)
	 * of this non viral taxon name.
	 *  
	 * @return  the boolean value of the protectedAuthorshipCache flag
	 * @see     #getAuthorshipCache()
	 */
	public boolean isProtectedAuthorshipCache() {
		return protectedAuthorshipCache;
	}

	/** 
	 * @see     #isProtectedAuthorshipCache()
	 * @see     #getAuthorshipCache()
	 */
	public void setProtectedAuthorshipCache(boolean protectedAuthorshipCache) {
		this.protectedAuthorshipCache = protectedAuthorshipCache;
	}

	
	

}