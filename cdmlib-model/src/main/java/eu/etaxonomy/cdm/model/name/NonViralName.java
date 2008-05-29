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
 * Taxon name class for all non viral taxa. Parentetical authorship is derived
 * from basionym relationship.
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
	//Author team that published the present combination
	private INomenclaturalAuthor combinationAuthorTeam;
	//Author team that contributed to the publication of the present combination
	private INomenclaturalAuthor exCombinationAuthorTeam;
	//Author team that published the original publication
	private INomenclaturalAuthor basionymAuthorTeam;
	//Author team that contributed to the original publication of the name
	private INomenclaturalAuthor exBasionymAuthorTeam;
	//concatenated und formated authorteams including basionym and combination authors
	private String authorshipCache;
	//this flag shows if the getAuthorshipCache should return generated value(false) or the given String(true)  
	protected boolean protectedAuthorshipCache;
	
	protected INonViralNameCacheStrategy cacheStrategy;
	
	
	
	public static NonViralName NewInstance(Rank rank){
		return new NonViralName(rank, null);
	}

	public static NonViralName NewInstance(Rank rank, HomotypicalGroup homotypicalGroup){
		return new NonViralName(rank, homotypicalGroup);
	}
	
	
	//needed by hibernate
	protected NonViralName(){
		super();
		setNameCacheStrategy();
	}
	
	protected NonViralName(Rank rank, HomotypicalGroup homotypicalGroup) {
		super(rank, homotypicalGroup);
		setNameCacheStrategy();
	}
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
	
	private void setNameCacheStrategy(){
		if (getClass() == NonViralName.class){
			this.cacheStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
		}
		
	}
	
	//TODO for PROTOTYPE
	@Transient
	public INonViralNameCacheStrategy getCacheStrategy() {
		return cacheStrategy;
	}
	public void setCacheStrategy(INonViralNameCacheStrategy cacheStrategy) {
		this.cacheStrategy = cacheStrategy;
	}
	
	

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getCombinationAuthorTeam(){
		return this.combinationAuthorTeam;
	}
	public void setCombinationAuthorTeam(INomenclaturalAuthor combinationAuthorTeam){
		this.combinationAuthorTeam = combinationAuthorTeam;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getExCombinationAuthorTeam(){
		return this.exCombinationAuthorTeam;
	}
	public void setExCombinationAuthorTeam(INomenclaturalAuthor exCombinationAuthorTeam){
		this.exCombinationAuthorTeam = exCombinationAuthorTeam;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	@Target(TeamOrPersonBase.class)
	public INomenclaturalAuthor getBasionymAuthorTeam(){
		return basionymAuthorTeam;
	}
	public void setBasionymAuthorTeam(INomenclaturalAuthor basionymAuthorTeam) {
		this.basionymAuthorTeam = basionymAuthorTeam;
	}

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
	 * Generates the composed name string of this taxon name without authors
	 * or year according to the strategy defined in
	 * {@link eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy INameCacheStrategy}.
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