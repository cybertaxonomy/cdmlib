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
import eu.etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Taxon name class for all non viral taxa. Parentetical authorship is derived
 * from basionym relationship.
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:39
 */
@Entity
public class NonViralName extends TaxonNameBase {
	static Logger logger = Logger.getLogger(NonViralName.class);
	//The suprageneric or the genus name
	private String uninomial;
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
	//concatenated und formated authorteams including basionym and combination authors
	private String authorshipCache;

	public NonViralName(Rank rank) {
		super(rank);
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}  )
	public Agent getCombinationAuthorTeam(){
		return this.combinationAuthorTeam;
	}

	/**
	 * 
	 * @param combinationAuthorTeam    combinationAuthorTeam
	 */
	public void setCombinationAuthorTeam(Agent combinationAuthorTeam){
		this.combinationAuthorTeam = combinationAuthorTeam;
	}

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}  )
	public Agent getExCombinationAuthorTeam(){
		return this.exCombinationAuthorTeam;
	}

	/**
	 * 
	 * @param exCombinationAuthorTeam    exCombinationAuthorTeam
	 */
	public void setExCombinationAuthorTeam(Agent exCombinationAuthorTeam){
		this.exCombinationAuthorTeam = exCombinationAuthorTeam;
	}

	public String getUninomial(){
		return this.uninomial;
	}

	/**
	 * 
	 * @param uninomial    uninomial
	 */
	public void setUninomial(String uninomial){
		this.uninomial = uninomial;
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
		return cacheStrategy.getFullNameCache(this);
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

}