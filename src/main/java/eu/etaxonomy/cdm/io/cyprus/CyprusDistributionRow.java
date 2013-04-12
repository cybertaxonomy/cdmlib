/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.cyprus;

import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * @author a.babadshanjan
 * @created 13.01.2009
 * @version 1.0
 */
public class CyprusDistributionRow {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CyprusDistributionRow.class);

	private String species;
	private String distribution;
	private String reference;
	
	//Sets
	private TreeMap<Integer, String> distributions = new TreeMap<Integer, String>();

	
	
	public CyprusDistributionRow() {
		this.species = "";
		this.setDistribution("");
		this.setReference("");
	}
	
	
// **************************** GETTER / SETTER *********************************/	
	


	public void putDistribution(int key, String distribution){
		this.distributions.put(key, distribution);
	}
	
	public String getSpecies() {
		return species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}


	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}


	public String getDistribution() {
		return distribution;
	}


	public void setReference(String reference) {
		this.reference = reference;
	}


	public String getReference() {
		return reference;
	}

	
}
