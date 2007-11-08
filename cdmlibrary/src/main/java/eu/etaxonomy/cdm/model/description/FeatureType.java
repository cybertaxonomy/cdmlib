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
import eu.etaxonomy.cdm.model.Description;
import java.util.*;
import javax.persistence.*;

/**
 * NEEDS TO BE COMPLEMENTED SPM / TDWG http://rs.tdwg.
 * org/ontology/voc/SpeciesProfileModel
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@Entity
public class FeatureType extends DefinedTermBase {
	static Logger logger = Logger.getLogger(FeatureType.class);
	private boolean supportsQuantitativeData;
	private boolean supportsTextData;
	private boolean supportsDistribution;
	private boolean supportsIndividualAssociation;
	private boolean supportsTaxonInteraction;
	private boolean supportsCommonTaxonName;
	private ArrayList recommendedModifierEnumeration;
	private ArrayList recommendedStatisticalMeasures;
	private ArrayList supportedCategoricalEnumerations;

	public ArrayList getRecommendedModifierEnumeration(){
		return this.recommendedModifierEnumeration;
	}

	/**
	 * 
	 * @param recommendedModifierEnumeration    recommendedModifierEnumeration
	 */
	public void setRecommendedModifierEnumeration(ArrayList recommendedModifierEnumeration){
		this.recommendedModifierEnumeration = recommendedModifierEnumeration;
	}

	public ArrayList getRecommendedStatisticalMeasures(){
		return this.recommendedStatisticalMeasures;
	}

	/**
	 * 
	 * @param recommendedStatisticalMeasures    recommendedStatisticalMeasures
	 */
	public void setRecommendedStatisticalMeasures(ArrayList recommendedStatisticalMeasures){
		this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
	}

	public ArrayList getSupportedCategoricalEnumerations(){
		return this.supportedCategoricalEnumerations;
	}

	/**
	 * 
	 * @param supportedCategoricalEnumerations    supportedCategoricalEnumerations
	 */
	public void setSupportedCategoricalEnumerations(ArrayList supportedCategoricalEnumerations){
		this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
	}

	public boolean getSupportsQuantitativeData(){
		return this.supportsQuantitativeData;
	}

	/**
	 * 
	 * @param supportsQuantitativeData    supportsQuantitativeData
	 */
	public void setSupportsQuantitativeData(boolean supportsQuantitativeData){
		this.supportsQuantitativeData = supportsQuantitativeData;
	}

	public boolean getSupportsTextData(){
		return this.supportsTextData;
	}

	/**
	 * 
	 * @param supportsTextData    supportsTextData
	 */
	public void setSupportsTextData(boolean supportsTextData){
		this.supportsTextData = supportsTextData;
	}

	public boolean getSupportsDistribution(){
		return this.supportsDistribution;
	}

	/**
	 * 
	 * @param supportsDistribution    supportsDistribution
	 */
	public void setSupportsDistribution(boolean supportsDistribution){
		this.supportsDistribution = supportsDistribution;
	}

	public boolean getSupportsIndividualAssociation(){
		return this.supportsIndividualAssociation;
	}

	/**
	 * 
	 * @param supportsIndividualAssociation    supportsIndividualAssociation
	 */
	public void setSupportsIndividualAssociation(boolean supportsIndividualAssociation){
		this.supportsIndividualAssociation = supportsIndividualAssociation;
	}

	public boolean getSupportsTaxonInteraction(){
		return this.supportsTaxonInteraction;
	}

	/**
	 * 
	 * @param supportsTaxonInteraction    supportsTaxonInteraction
	 */
	public void setSupportsTaxonInteraction(boolean supportsTaxonInteraction){
		this.supportsTaxonInteraction = supportsTaxonInteraction;
	}

	public boolean getSupportsCommonTaxonName(){
		return this.supportsCommonTaxonName;
	}

	/**
	 * 
	 * @param supportsCommonTaxonName    supportsCommonTaxonName
	 */
	public void setSupportsCommonTaxonName(boolean supportsCommonTaxonName){
		this.supportsCommonTaxonName = supportsCommonTaxonName;
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