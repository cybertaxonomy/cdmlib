/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;

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
	public Feature() {
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

	
	private static final UUID uuidUnknown = UUID.fromString("910307f1-dc3c-452c-a6dd-af5ac7cd365c");
	private static final UUID uuidDescription = UUID.fromString("9087cdcd-8b08-4082-a1de-34c9ba9fb493");
	private static final UUID uuidDistribution = UUID.fromString("9fc9d10c-ba50-49ee-b174-ce83fc3f80c6");
	private static final UUID uuidEcology = UUID.fromString("aa923827-d333-4cf5-9a5f-438ae0a4746b");
	private static final UUID uuidBiologyEcology = UUID.fromString("9832e24f-b670-43b4-ac7c-20a7261a1d8c");
	private static final UUID uuidKey = UUID.fromString("a677f827-22b9-4205-bb37-11cb48dd9106");
	private static final UUID uuidMaterialsExamined = UUID.fromString("7c0c7571-a864-47c1-891d-01f59000dae1");
	private static final UUID uuidMaterialsMethods = UUID.fromString("1e87d9c3-0844-4a03-9686-773e2ccb3ab6");
	private static final UUID uuidEtymology = UUID.fromString("dd653d48-355c-4aec-a4e7-724f6eb29f8d");
	private static final UUID uuidDiagnosis = UUID.fromString("d43d8501-ceab-4caa-9e51-e87138528fac");
	private static final UUID uuidProtolog = UUID.fromString("7f1fd111-fc52-49f0-9e75-d0097f576b2d");
	private static final UUID uuidCommonName = UUID.fromString("fc810911-51f0-4a46-ab97-6562fe263ae5");
	private static final UUID uuidPhenology = UUID.fromString("a7786d3e-7c58-4141-8416-346d4c80c4a2");
	private static final UUID uuidOccurrence = UUID.fromString("5deff505-1a32-4817-9a74-50e6936fd630");
	private static final UUID uuidCitation = UUID.fromString("99b2842f-9aa7-42fa-bd5f-7285311e0101");
	private static final UUID uuidAdditionalPublication = UUID.fromString("cb2eab09-6d9d-4e43-8ad2-873f23400930");
	private static final UUID uuidUses = UUID.fromString("e5374d39-b210-47c7-bec1-bee05b5f1cb6");
	private static final UUID uuidConservation = UUID.fromString("4518fc20-2492-47de-b345-777d2b83c9cf");
	private static final UUID uuidCultivation = UUID.fromString("e28965b2-a367-48c5-b954-8afc8ac2c69b");
	private static final UUID uuidIntroduction = UUID.fromString("e75255ca-8ff4-4905-baad-f842927fe1d3");
	private static final UUID uuidDiscussion = UUID.fromString("d3c4cbb6-0025-4322-886b-cd0156753a25");
	
	
	
//	private static final UUID uuidDistribution = UUID.fromString("");
//	private static final UUID uuidDistribution = UUID.fromString("");
//	private static final UUID uuidDistribution = UUID.fromString("");

//	"86bd920d-f8c5-48b9-af1d-03f63c31de5c",,"Abstract","Abstract"
//	"489bf358-b78a-45e2-a691-f9f3f10446ce",,"Synopsis","Synopsis"
//	"89d3b005-9876-4923-89d9-60eb75b9583b",,"Multiple","Multiple"
//	"555a46bc-211a-476f-a022-c472970d6f8b",,"Acknowledgments","Acknowledgments"
	
	
	public static final Feature getByUuid(UUID uuid){
		return (Feature)findByUuid(uuid);
	}
	
	public static final Feature UNKNOWN(){
		return getByUuid(uuidUnknown);
	}
	
	public static final Feature DESCRIPTION(){
		return getByUuid(uuidDescription);
	}

	public static final Feature DISTRIBUTION(){
		return getByUuid(uuidDistribution);
	}

	public static final Feature DISCUSSION(){
		return getByUuid(uuidDiscussion);
	}
	
	public static final Feature ECOLOGY(){
		return getByUuid(uuidEcology);
	}	
	
	public static final Feature BIOLOGY_ECOLOGY(){
		return getByUuid(uuidBiologyEcology);
	}
	
	public static final Feature KEY(){
		return getByUuid(uuidKey);
	}		
	
	
	public static final Feature MATERIALS_EXAMINED(){
		return getByUuid(uuidMaterialsExamined);
	}
	
	public static final Feature MATERIALS_METHODS(){
		return getByUuid(uuidMaterialsMethods);
	}
	
	public static final Feature ETYMOLOGY(){
		return getByUuid(uuidEtymology);
	}
		
	public static final Feature DIAGNOSIS(){
		return getByUuid(uuidDiagnosis);
	}

	
	public static final Feature INTRODUCTION(){
		return getByUuid(uuidIntroduction);
	}

	public static final Feature PROTOLOG(){
		return getByUuid(uuidProtolog);
	}
	public static final Feature COMMON_NAME(){
		return getByUuid(uuidCommonName);
	}
	
	public static final Feature PHENOLOGY(){
		return getByUuid(uuidPhenology);
	}

	
	public static final Feature OCCURRENCE(){
		return getByUuid(uuidOccurrence);
	}
	
	public static final Feature CITATION(){
		return getByUuid(uuidCitation);
	}
	
	public static final Feature ADDITIONAL_PUBLICATION(){
		return getByUuid(uuidAdditionalPublication);
	}
	
	
	public static final Feature USES(){
		return getByUuid(uuidUses);
	}
	
	
	public static final Feature CONSERVATION(){
		return getByUuid(uuidConservation);
	}
	
	
	public static final Feature CULTIVATION(){
		return getByUuid(uuidCultivation);
	}
	
	
	
	/**
	 * special kind of OrganismInteraction
	 */
	public static final Feature HYBRID_PARENT(){
		//TODO
		logger.warn("HYBRID_PARENT not yet implemented");
		return null;
	}


}