/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.Enumeration;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

/**
 * NEEDS TO BE COMPLEMENTED
 * SPM / TDWG
 * http://rs.tdwg.org/ontology/voc/SpeciesProfileModel
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 19:18:14
 */
public class FeatureType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(FeatureType.class);

	@Description("")
	private boolean supportsQuantitativeData;
	@Description("")
	private boolean supportsTextData;
	@Description("")
	private boolean supportsDistribution;
	@Description("")
	private boolean supportsIndividualAssociation;
	@Description("")
	private boolean supportsTaxonInteraction;
	@Description("")
	private boolean supportsCommonTaxonName;
	private ArrayList recommendedModifierEnumeration;
	private ArrayList recommendedStatisticalMeasures;
	private ArrayList supportedCategoricalEnumerations;

	public ArrayList getRecommendedModifierEnumeration(){
		return recommendedModifierEnumeration;
	}

	/**
	 * 
	 * @param recommendedModifierEnumeration
	 */
	public void setRecommendedModifierEnumeration(ArrayList recommendedModifierEnumeration){
		;
	}

	public ArrayList getRecommendedStatisticalMeasures(){
		return recommendedStatisticalMeasures;
	}

	/**
	 * 
	 * @param recommendedStatisticalMeasures
	 */
	public void setRecommendedStatisticalMeasures(ArrayList recommendedStatisticalMeasures){
		;
	}

	public ArrayList getSupportedCategoricalEnumerations(){
		return supportedCategoricalEnumerations;
	}

	/**
	 * 
	 * @param supportedCategoricalEnumerations
	 */
	public void setSupportedCategoricalEnumerations(ArrayList supportedCategoricalEnumerations){
		;
	}

	public boolean getSupportsQuantitativeData(){
		return supportsQuantitativeData;
	}

	/**
	 * 
	 * @param supportsQuantitativeData
	 */
	public void setSupportsQuantitativeData(boolean supportsQuantitativeData){
		;
	}

	public boolean getSupportsTextData(){
		return supportsTextData;
	}

	/**
	 * 
	 * @param supportsTextData
	 */
	public void setSupportsTextData(boolean supportsTextData){
		;
	}

	public boolean getSupportsDistribution(){
		return supportsDistribution;
	}

	/**
	 * 
	 * @param supportsDistribution
	 */
	public void setSupportsDistribution(boolean supportsDistribution){
		;
	}

	public boolean getSupportsIndividualAssociation(){
		return supportsIndividualAssociation;
	}

	/**
	 * 
	 * @param supportsIndividualAssociation
	 */
	public void setSupportsIndividualAssociation(boolean supportsIndividualAssociation){
		;
	}

	public boolean getSupportsTaxonInteraction(){
		return supportsTaxonInteraction;
	}

	/**
	 * 
	 * @param supportsTaxonInteraction
	 */
	public void setSupportsTaxonInteraction(boolean supportsTaxonInteraction){
		;
	}

	public boolean getSupportsCommonTaxonName(){
		return supportsCommonTaxonName;
	}

	/**
	 * 
	 * @param supportsCommonTaxonName
	 */
	public void setSupportsCommonTaxonName(boolean supportsCommonTaxonName){
		;
	}

	public static final FeatureType DISTRIBUTION(){
		return null;
	}

	/**
	 * special kind of OrganismInteraction
	 */
	public static final FeatureType HYBRID_PARENT(){
		return null;
	}

	public static final FeatureType COMMON_NAME(){
		return null;
	}

}