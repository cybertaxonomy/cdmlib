/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import org.apache.log4j.Logger;

import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlType;

/**
 * Individual property of observed phenomena able to be described or measured.
 * Experts do not use the word feature for the actual description but only for
 * the property itself. Naming this class FeatureType would create confusion.  
 * NEEDS TO BE COMPLEMENTED SPM / TDWG http://rs.tdwg.
 * org/ontology/voc/SpeciesProfileModel
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@XmlType(name="Feature", factoryMethod="NewInstance")
@Entity
public class Feature extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Feature.class);

	private boolean supportsQuantitativeData;
	private boolean supportsTextData;
	private boolean supportsDistribution;
	private boolean supportsIndividualAssociation;
	private boolean supportsTaxonInteraction;
	private boolean supportsCommonTaxonName;
	private Set<TermVocabulary> recommendedModifierEnumeration = new HashSet<TermVocabulary>();
	private Set<StatisticalMeasure> recommendedStatisticalMeasures = new HashSet<StatisticalMeasure>();
	private Set<TermVocabulary> supportedCategoricalEnumerations = new HashSet<TermVocabulary>();
	
/* ***************** CONSTRUCTOR AND FACTORY METHODS **********************************/
	

	public static Feature NewInstance() {
		return new Feature();
	}
	public static Feature NewInstance(String term, String label, String labelAbbrev){
		return new Feature(term, label, labelAbbrev);
	}

	/**
	 * Default Constructor
	 */
	private Feature() {
		super();
	}
	
	protected Feature(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

/* *************************************************************************************/
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

	@OneToMany
	public Set<TermVocabulary> getRecommendedModifierEnumeration() {
		return recommendedModifierEnumeration;
	}

	protected void setRecommendedModifierEnumeration(
			Set<TermVocabulary> recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration = recommendedModifierEnumeration;
	}

	public void addRecommendedModifierEnumeration(
			TermVocabulary recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.add(recommendedModifierEnumeration);
	}
	public void removeRecommendedModifierEnumeration(
			TermVocabulary recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.remove(recommendedModifierEnumeration);
	}

	@OneToMany
	public Set<StatisticalMeasure> getRecommendedStatisticalMeasures() {
		return recommendedStatisticalMeasures;
	}

	protected void setRecommendedStatisticalMeasures(
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

	@OneToMany
	public Set<TermVocabulary> getSupportedCategoricalEnumerations() {
		return supportedCategoricalEnumerations;
	}

	protected void setSupportedCategoricalEnumerations(
			Set<TermVocabulary> supportedCategoricalEnumerations) {
		this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
	}
	public void addSupportedCategoricalEnumeration(
			TermVocabulary supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.add(supportedCategoricalEnumeration);
	}
	public void removeSupportedCategoricalEnumeration(
			TermVocabulary supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.remove(supportedCategoricalEnumeration);
	}

	public static final Feature DISTRIBUTION(){
		//TODO
		logger.warn("DISTRIBUTION not yet implemented");
		return null;
	}

	/**
	 * special kind of OrganismInteraction
	 */
	public static final Feature HYBRID_PARENT(){
		//TODO
		logger.warn("HYBRID_PARENT not yet implemented");
		return null;
	}

	public static final Feature COMMON_NAME(){
		//TODO
		logger.warn("COMMON_NAME not yet implemented");
		return null;
	}

}