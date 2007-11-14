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
	private Set<Enumeration> recommendedModifierEnumeration;
	private Set<StatisticalMeasure> recommendedStatisticalMeasures;
	private Set<Enumeration> supportedCategoricalEnumerations;



	
	
	public boolean isSupportsQuantitativeData() {
		return supportsQuantitativeData;
	}

	public void setSupportsQuantitativeData(boolean supportsQuantitativeData) {
		this.supportsQuantitativeData = supportsQuantitativeData;
	}

	public boolean isSupportsTextData() {
		return supportsTextData;
	}

	public void setSupportsTextData(boolean supportsTextData) {
		this.supportsTextData = supportsTextData;
	}

	public boolean isSupportsDistribution() {
		return supportsDistribution;
	}

	public void setSupportsDistribution(boolean supportsDistribution) {
		this.supportsDistribution = supportsDistribution;
	}

	public boolean isSupportsIndividualAssociation() {
		return supportsIndividualAssociation;
	}

	public void setSupportsIndividualAssociation(
			boolean supportsIndividualAssociation) {
		this.supportsIndividualAssociation = supportsIndividualAssociation;
	}

	public boolean isSupportsTaxonInteraction() {
		return supportsTaxonInteraction;
	}

	public void setSupportsTaxonInteraction(boolean supportsTaxonInteraction) {
		this.supportsTaxonInteraction = supportsTaxonInteraction;
	}

	public boolean isSupportsCommonTaxonName() {
		return supportsCommonTaxonName;
	}

	public void setSupportsCommonTaxonName(boolean supportsCommonTaxonName) {
		this.supportsCommonTaxonName = supportsCommonTaxonName;
	}

	public Set<Enumeration> getRecommendedModifierEnumeration() {
		return recommendedModifierEnumeration;
	}

	private void setRecommendedModifierEnumeration(
			Set<Enumeration> recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration = recommendedModifierEnumeration;
	}

	public void addRecommendedModifierEnumeration(
			Enumeration recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.add(recommendedModifierEnumeration);
	}
	public void removeRecommendedModifierEnumeration(
			Enumeration recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.remove(recommendedModifierEnumeration);
	}

	public Set<StatisticalMeasure> getRecommendedStatisticalMeasures() {
		return recommendedStatisticalMeasures;
	}

	private void setRecommendedStatisticalMeasures(
			Set<StatisticalMeasure> recommendedStatisticalMeasures) {
		this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
	}

	public void addRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.add(recommendedStatisticalMeasure);
	}
	public void removeRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.remove(recommendedStatisticalMeasure);
	}

	public Set<Enumeration> getSupportedCategoricalEnumerations() {
		return supportedCategoricalEnumerations;
	}

	private void setSupportedCategoricalEnumerations(
			Set<Enumeration> supportedCategoricalEnumerations) {
		this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
	}
	public void addSupportedCategoricalEnumeration(
			Enumeration supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.add(supportedCategoricalEnumeration);
	}
	public void removeSupportedCategoricalEnumeration(
			Enumeration supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.remove(supportedCategoricalEnumeration);
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