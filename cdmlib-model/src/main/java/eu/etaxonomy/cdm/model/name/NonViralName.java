/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy;
import eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Target;

import javax.persistence.*;

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
@Entity
public class NonViralName<T extends NonViralName> extends TaxonNameBase<NonViralName, INonViralNameCacheStrategy> {
	private static final Logger logger = Logger.getLogger(NonViralName.class);
	
	//The scientific name without author strings and year
	private String nameCache;
	//The suprageneric or the genus name
	private String genusOrUninomial;
	//Genus subdivision epithet
	private String infraGenericEpithet;
	//species epithet
	private String specificEpithet;
	//Species subdivision epithet
	private String infraSpecificEpithet;
	private INomenclaturalAuthor combinationAuthorTeam;
	private INomenclaturalAuthor exCombinationAuthorTeam;
	private INomenclaturalAuthor basionymAuthorTeam;
	private INomenclaturalAuthor exBasionymAuthorTeam;
	//concatenated und formated author teams including basionym and combination authors
	private String authorshipCache;
	//this flag shows if the getAuthorshipCache should return generated value(false) or the given String(true)  
	protected boolean protectedAuthorshipCache;
	protected boolean protectedNameCache;

	protected INonViralNameCacheStrategy cacheStrategy;
	
	// ************* CONSTRUCTORS *************/	
	
	//needed by hibernate
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @see #NonViralName(Rank, HomotypicalGroup)
	 * @see #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(){
		super();
		setNameCacheStrategy();
	}
	
	/** 
	 * Class constructor: creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank},
	 * its {@link common.HomotypicalGroup homotypical group} and
	 * only containing the {@link eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this taxon name
	 * @param	homotypicalGroup  the homotypical group to which this taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
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
	 * the {@link eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param	rank  the rank to be assigned to this taxon name
	 * @param	genusOrUninomial the string for this taxon name
	 * 			if its rank is genus or above or for the genus component
	 * 			if its rank is lower than genus
	 * @param	specificEpithet  the string for the first epithet of
	 * 			this taxon name if its rank is lower than genus
	 * @param	infraSpecificEpithet  the string for the second epithet of
	 * 			this taxon name if its rank is lower than species
	 * @param	combinationAuthorTeam  the author or the team who published this taxon name
	 * @param	nomenclaturalReference  the nomenclatural reference where this taxon name was published
	 * @param	nomenclMicroRef  the string with the details for precise location within the nomenclatural reference
	 * @param	homotypicalGroup  the homotypical group to which this taxon name belongs
	 * @see 	#NonViralName()
	 * @see		#NonViralName(Rank, HomotypicalGroup)
	 * @see		#NewInstance(Rank, HomotypicalGroup)
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	protected NonViralName(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, TeamOrPersonBase combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
		setGenusOrUninomial(genusOrUninomial);
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
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * 
	 * @param  rank  the rank to be assigned to this non viral taxon name
	 * @see    #NewInstance(Rank, HomotypicalGroup)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
	 */
	public static NonViralName NewInstance(Rank rank){
		return new NonViralName(rank, null);
	}

	/** 
	 * Creates a new non viral taxon name instance
	 * only containing its {@link common.Rank rank} and
	 * its {@link common.HomotypicalGroup homotypical group} and 
 	 * the {@link eu.etaxonomy.cdm.strategy.cache.NonViralNameDefaultCacheStrategy default cache strategy}.
	 * The new non viral taxon name instance will be also added to the set of
	 * taxon names belonging to this homotypical group. If the homotypical 
	 * group does not exist a new instance will be created for it.
	 * 
	 * @param  rank  the rank to be assigned to this non viral taxon name
	 * @param  homotypicalGroup  the homotypical group to which this non viral taxon name belongs
	 * @see    #NewInstance(Rank)
	 * @see    #NonViralName(Rank, HomotypicalGroup)
	 * @see    #NonViralName()
	 * @see    #NonViralName(Rank, String, String, String, TeamOrPersonBase, INomenclaturalReference, String, HomotypicalGroup)
	 * @see    eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see    eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
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
	 * Returns the {@link eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy cache strategy} used to generate
	 * several strings corresponding to this non viral taxon name
	 * (in particular taxon name caches and author strings).
	 * 
	 * @return  the cache strategy used for this non viral taxon name
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy
	 * @see 	eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy
	 * @see     eu.etaxonomy.cdm.strategy.cache.IIdentifiableEntityCacheStrategy
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
	public void setExBasionymAuthorTeam(INomenclaturalAuthor exBasionymAuthorTeam) {
		this.exBasionymAuthorTeam = exBasionymAuthorTeam;
	}

	public String getGenusOrUninomial() {
		return genusOrUninomial;
	}
	
	public void setGenusOrUninomial(String genusOrUninomial) {
		this.genusOrUninomial = genusOrUninomial;
	}

	public String getInfraGenericEpithet(){
		return this.infraGenericEpithet;
	}

	/**
	 * 
	 * @param infraGenericEpithet    infraGenericEpithet
	 */
	public void setInfraGenericEpithet(String infraGenericEpithet){
		this.infraGenericEpithet = infraGenericEpithet;
	}

	public String getSpecificEpithet(){
		return this.specificEpithet;
	}

	/**
	 * 
	 * @param specificEpithet    specificEpithet
	 */
	public void setSpecificEpithet(String specificEpithet){
		this.specificEpithet = specificEpithet;
	}

	public String getInfraSpecificEpithet(){
		return this.infraSpecificEpithet;
	}

	/**
	 * 
	 * @param infraSpecificEpithet    infraSpecificEpithet
	 */
	public void setInfraSpecificEpithet(String infraSpecificEpithet){
		this.infraSpecificEpithet = infraSpecificEpithet;
	}

	/**
	 * Returns the string with the scientific name of this taxon name including
	 * author strings and maybe year. This string may be stored in the
	 * inherited {@link common.IdentifiableEntity#getTitleCache() titleCache} attribute.
	 * This method overrides the generic and inherited
	 * IdentifiableEntity#getTitleCache() method.
	 *
	 * @see  common.IdentifiableEntity#getTitleCache()
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
	 * Generates the composed name string of this taxon name without author
	 * strings or year according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.INonViralNameCacheStrategy INonViralNameCacheStrategy}.
	 * The result might be stored in {@link #getNameCache() nameCache} if the
	 * flag {@link #isProtectedNameCache() protectedNameCache} is not set.
	 * 
	 * @return  the string with the composed name of this taxon name without authors or year
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
	 * without author strings and year) string for this taxon name. If the
	 * {@link #isProtectedNameCache() protectedNameCache} flag is not set (False)
	 * the string will be generated according to a defined strategy,
	 * otherwise the value of the actual nameCache string will be returned.
	 * 
	 * @return  the string which identifies this taxon name (without authors or year)
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
	 * Assigns a nameCache string to this taxon name and protects it from being overwritten.
	 *  
	 * @param  nameCache  the string which identifies this taxon name (without authors or year)
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
	 * string of this taxon name.
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

	
	public String generateAuthorship(){
		if (cacheStrategy == null){
			logger.warn("No CacheStrategy defined for nonViralName: " + this.getUuid());
			return null;
		}else{
			return ((INonViralNameCacheStrategy<T>)cacheStrategy).getAuthorshipCache((T)this);
		}
	}

	/**
	 * returns concatenated und formated authorteams including basionym and
	 * combination authors
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

	public void setAuthorshipCache(String authorshipCache) {
		this.authorshipCache = authorshipCache;
	}

	/**
	 * Returns the boolean value "true" if the components of this taxon name
	 * follow the rules of the corresponding {@link NomenclaturalCode nomenclatural code},
	 * "false" otherwise. The nomenclature code depends on
	 * the concrete name subclass ({@link BacterialName BacterialName},
	 * {@link BotanicalName BotanicalName}, {@link CultivarPlantName CultivarPlantName} ot
	 * {@link ZoologicalName ZoologicalName} to which this taxon name belongs.
	 *  
	 * @return  the boolean value expressing the compliance of this taxon name to the nomenclatural code
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
	@Transient
	@Override
	public NomenclaturalCode getNomeclaturalCode() {
		logger.warn("Non Viral Name has no specific Code defined. Use subclasses");
		return null;
	}

	

}