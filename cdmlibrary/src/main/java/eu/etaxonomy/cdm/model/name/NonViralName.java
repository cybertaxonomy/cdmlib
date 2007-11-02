/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.name;


import etaxonomy.cdm.model.agent.Team;
import org.apache.log4j.Logger;

/**
 * Taxon name class for all non viral taxa.
 * Parentetical authorship is derived from basionym relationship.
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:15:05
 */
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
	/**
	 * Author team that published the present combination
	 */
	private Team combinationAuthorTeam;
	/**
	 * Author team that contributed to the publication of the present combination
	 */
	private Team exCombinationAuthorTeam;

	public Team getCombinationAuthorTeam(){
		return combinationAuthorTeam;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setCombinationAuthorTeam(Team newVal){
		combinationAuthorTeam = newVal;
	}

	public Team getExCombinationAuthorTeam(){
		return exCombinationAuthorTeam;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setExCombinationAuthorTeam(Team newVal){
		exCombinationAuthorTeam = newVal;
	}

	public String getUninomial(){
		return uninomial;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setUninomial(String newVal){
		uninomial = newVal;
	}

	public String getInfraGenericEpithet(){
		return infraGenericEpithet;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInfraGenericEpithet(String newVal){
		infraGenericEpithet = newVal;
	}

	public String getSpecificEpithet(){
		return specificEpithet;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSpecificEpithet(String newVal){
		specificEpithet = newVal;
	}

	public String getInfraSpecificEpithet(){
		return infraSpecificEpithet;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setInfraSpecificEpithet(String newVal){
		infraSpecificEpithet = newVal;
	}

	/**
	 * returns concatenated und formated authorteams including basionym and
	 * combination authors 
	 */
	@Transient
	public String getFullAuthorship(){
		return "";
	}

}