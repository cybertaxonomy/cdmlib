/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author a.babadshanjan
 * @created 10.09.2009
 * @version 1.0
 */
public class FaunaEuropaeaDistributionTaxon {
	
	private UUID taxonUuid;
	private int taxonId;
	private Set<FaunaEuropaeaDistribution> distributions;	
	
	/**
	 * @param distributions
	 */
	public FaunaEuropaeaDistributionTaxon() {
		this.distributions = new HashSet<FaunaEuropaeaDistribution>();
	}
	/**
	 * @param taxonUuid
	 */
	public FaunaEuropaeaDistributionTaxon(UUID taxonUuid) {
		this();
		this.taxonUuid = taxonUuid;
	}
//	/**
//	 * @return the taxonUuid
//	 */
//	public UUID getTaxonUuid() {
//		return taxonUuid;
//	}
//	/**
//	 * @param taxonUuid the taxonUuid to set
//	 */
//	public void setTaxonUuid(UUID taxonUuid) {
//		this.taxonUuid = taxonUuid;
//	}
//	/**
//	 * @return the taxonId
//	 */
//	public int getTaxonId() {
//		return taxonId;
//	}
//	/**
//	 * @param taxonId the taxonId to set
//	 */
//	public void setTaxonId(int taxonId) {
//		this.taxonId = taxonId;
//	}
	/**
	 * @return the distributions
	 */
	public Set<FaunaEuropaeaDistribution> getDistributions() {
		return distributions;
	}
	/**
	 * @param distributions the distributions to set
	 */
	public void setDistributions(Set<FaunaEuropaeaDistribution> distributions) {
		this.distributions = distributions;
	}
	
	public void addDistribution(FaunaEuropaeaDistribution fauEuDistribution) {
		distributions.add(fauEuDistribution);
	}
}
