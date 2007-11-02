/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package etaxonomy.cdm.model.description;


import etaxonomy.cdm.model.common.DefinedTermBase;
import etaxonomy.cdm.model.common.Enumeration;
import org.apache.log4j.Logger;

/**
 * NEEDS TO BE COMPLEMENTED
 * SPM / TDWG
 * http://rs.tdwg.org/ontology/voc/SpeciesProfileModel
 * @author m.doering
 * @version 1.0
 * @created 02-Nov-2007 18:14:49
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
	 * @param newVal
	 */
	public void setRecommendedModifierEnumeration(ArrayList newVal){
		recommendedModifierEnumeration = newVal;
	}

	public ArrayList getRecommendedStatisticalMeasures(){
		return recommendedStatisticalMeasures;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setRecommendedStatisticalMeasures(ArrayList newVal){
		recommendedStatisticalMeasures = newVal;
	}

	public ArrayList getSupportedCategoricalEnumerations(){
		return supportedCategoricalEnumerations;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportedCategoricalEnumerations(ArrayList newVal){
		supportedCategoricalEnumerations = newVal;
	}

	public boolean getSupportsQuantitativeData(){
		return supportsQuantitativeData;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsQuantitativeData(boolean newVal){
		supportsQuantitativeData = newVal;
	}

	public boolean getSupportsTextData(){
		return supportsTextData;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsTextData(boolean newVal){
		supportsTextData = newVal;
	}

	public boolean getSupportsDistribution(){
		return supportsDistribution;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsDistribution(boolean newVal){
		supportsDistribution = newVal;
	}

	public boolean getSupportsIndividualAssociation(){
		return supportsIndividualAssociation;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsIndividualAssociation(boolean newVal){
		supportsIndividualAssociation = newVal;
	}

	public boolean getSupportsTaxonInteraction(){
		return supportsTaxonInteraction;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsTaxonInteraction(boolean newVal){
		supportsTaxonInteraction = newVal;
	}

	public boolean getSupportsCommonTaxonName(){
		return supportsCommonTaxonName;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setSupportsCommonTaxonName(boolean newVal){
		supportsCommonTaxonName = newVal;
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