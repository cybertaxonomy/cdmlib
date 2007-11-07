/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;


import eu.etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * Taxon name class for all non viral taxa.
 * Parentetical authorship is derived from basionym relationship.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:36:21
 */
@Entity
public class NonViralName extends TaxonNameBase {
	static Logger logger = Logger.getLogger(NonViralName.class);

	//The suprageneric or the genus name
	@Description("The suprageneric or the genus name")
	private String uninomial;
	//Genus subdivision epithet
	@Description("Genus subdivision epithet")
	private String infraGenericEpithet;
	//species epithet
	@Description("species epithet")
	private String specificEpithet;
	//Species subdivision epithet
	@Description("Species subdivision epithet")
	private String infraSpecificEpithet;
	//Full authorship string
	@Description("Full authorship string")
	private String fullAuthorship;
	/**
	 * Author team that published the present combination
	 */
	private Team combinationAuthorTeam;
	/**
	 * Author team that contributed to the publication of the present combination
	 */
	private Team exCombinationAuthorTeam;

	public NonViralName(Rank rank) {
		super(rank);
	}


	
	public Team getCombinationAuthorTeam(){
		return combinationAuthorTeam;
	}

	/**
	 * 
	 * @param combinationAuthorTeam
	 */
	public void setCombinationAuthorTeam(Team combinationAuthorTeam){
		;
	}

	public Team getExCombinationAuthorTeam(){
		return exCombinationAuthorTeam;
	}

	/**
	 * 
	 * @param exCombinationAuthorTeam
	 */
	public void setExCombinationAuthorTeam(Team exCombinationAuthorTeam){
		;
	}

	public String getUninomial(){
		return uninomial;
	}

	/**
	 * 
	 * @param uninomial
	 */
	public void setUninomial(String uninomial){
		;
	}

	public String getInfraGenericEpithet(){
		return infraGenericEpithet;
	}

	/**
	 * 
	 * @param infraGenericEpithet
	 */
	public void setInfraGenericEpithet(String infraGenericEpithet){
		;
	}

	public String getSpecificEpithet(){
		return specificEpithet;
	}

	/**
	 * 
	 * @param specificEpithet
	 */
	public void setSpecificEpithet(String specificEpithet){
		;
	}

	public String getInfraSpecificEpithet(){
		return infraSpecificEpithet;
	}

	/**
	 * 
	 * @param infraSpecificEpithet
	 */
	public void setInfraSpecificEpithet(String infraSpecificEpithet){
		;
	}

	/**
	 * returns concatenated und formated authorteams including basionym and
	 * combination authors 
	 */
	public String getFullAuthorship(){
		return "";
	}
	public void setFullAuthorship(String fullAuthorship){
		this.fullAuthorship=fullAuthorship;
	}

	@Override
	public String generateTitle() {
		// TODO Auto-generated method stub
		return null;
	}

}