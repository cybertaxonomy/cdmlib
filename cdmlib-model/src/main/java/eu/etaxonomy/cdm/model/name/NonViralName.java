/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.strategy.BotanicNameDefaultCacheStrategy;
import eu.etaxonomy.cdm.strategy.NonViralNameDefaultCacheStrategy;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;

/**
 * Taxon name class for all non viral taxa. Parentetical authorship is derived
 * from basionym relationship.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@Entity
public class NonViralName<T extends NonViralName> extends TaxonNameBase<NonViralName> {
	static Logger logger = Logger.getLogger(NonViralName.class);
	//The suprageneric or the genus name
	private String genusOrUninomial;
	//Genus subdivision epithet
	private String infraGenericEpithet;
	//species epithet
	private String specificEpithet;
	//Species subdivision epithet
	private String infraSpecificEpithet;
	//Author team that published the present combination
	private Agent combinationAuthorTeam;
	//Author team that contributed to the publication of the present combination
	private Agent exCombinationAuthorTeam;
	//Author team that published the original publication
	private Agent basionymAuthorTeam;
	//Author team that contributed to the original publication of the name
	private Agent exBasionymAuthorTeam;
	//concatenated und formated authorteams including basionym and combination authors
	private String authorshipCache;
	
	//needed by hibernate
	protected NonViralName(){
		super();
		setNameCacheStrategy();
	}
	
	private void setNameCacheStrategy(){
		if (getClass() == NonViralName.class){
			this.cacheStrategy = NonViralNameDefaultCacheStrategy.NewInstance();
		}
		
	}
	
	public NonViralName(Rank rank) {
		super(rank);
		setNameCacheStrategy();
	}
	public NonViralName(Rank rank, String genusOrUninomial, String specificEpithet, String infraSpecificEpithet, Agent combinationAuthorTeam, INomenclaturalReference nomenclaturalReference, String nomenclMicroRef) {
		super(rank);
		setNameCacheStrategy();
		setGenusOrUninomial(genusOrUninomial);
		setSpecificEpithet(specificEpithet);
		setInfraSpecificEpithet(infraSpecificEpithet);
		setCombinationAuthorTeam(combinationAuthorTeam);
		setNomenclaturalReference(nomenclaturalReference);
		this.setNomenclaturalMicroReference(nomenclMicroRef);
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getCombinationAuthorTeam(){
		return this.combinationAuthorTeam;
	}
	public void setCombinationAuthorTeam(Agent combinationAuthorTeam){
		this.combinationAuthorTeam = combinationAuthorTeam;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getExCombinationAuthorTeam(){
		return this.exCombinationAuthorTeam;
	}
	public void setExCombinationAuthorTeam(Agent exCombinationAuthorTeam){
		this.exCombinationAuthorTeam = exCombinationAuthorTeam;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getBasionymAuthorTeam(){
		return basionymAuthorTeam;
	}
	public void setBasionymAuthorTeam(Agent basionymAuthorTeam) {
		this.basionymAuthorTeam = basionymAuthorTeam;
	}

	@ManyToOne
	@Cascade({CascadeType.SAVE_UPDATE})
	public Agent getExBasionymAuthorTeam(){
		return exBasionymAuthorTeam;
	}
	public void setExBasionymAuthorTeam(Agent exBasionymAuthorTeam) {
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
	 * returns concatenated und formated authorteams including basionym and
	 * combination authors
	 */
	public String getAuthorshipCache() {
		return authorshipCache;
	}

	public void setAuthorshipCache(String authorshipCache) {
		this.authorshipCache = authorshipCache;
	}

	@Override
	@Transient
	public boolean isCodeCompliant() {
		// TODO Auto-generated method stub
		return false;
	}

}