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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.ILoadableTerm;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class for individual properties (also designed as character, type or
 * category) of observed phenomena able to be described or measured. It also
 * covers categories of informations on {@link TaxonNameBase taxon names} not
 * taken in account in {@link NomenclaturalCode nomenclature}.<BR>
 * Descriptions require features in order to be structured and disaggregated 
 * in {@link DescriptionElementBase description elements}.<BR>
 * Experts do not use the word feature for the actual description
 * but only for the property itself. Therefore naming this class FeatureType
 * would have leaded to confusion.
 * <P>
 * Since features are {@link DefinedTermBase defined terms} they have a hierarchical
 * structure that allows to specify ("kind of") or generalize
 * ("generalization of") features. "Kind of" / "generalization of" relations
 * are bidirectional (a feature F1 is a "Kind of" a feature F2 if and only
 * if the feature F2 is a "generalization of" the feature F1. This hierarchical
 * structure has nothing in common with {@link FeatureTree feature trees} used for determination.
 * <P> 
 * A standard set of feature instances will be automatically
 * created as the project starts. But this class allows to extend this standard
 * set by creating new instances of additional features if needed.<BR>
 * <P>
 * This class corresponds to DescriptionsSectionType according to the SDD
 * schema.
 * 
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:24
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Feature", factoryMethod="NewInstance", propOrder = {
	    "supportsTextData",
	    "supportsQuantitativeData",
	    "supportsDistribution",
	    "supportsIndividualAssociation",
	    "supportsTaxonInteraction",
	    "supportsCommonTaxonName",
	    "recommendedModifierEnumeration",
	    "recommendedStatisticalMeasures",
	    "supportedCategoricalEnumerations"
})
@XmlRootElement(name = "Feature")
@Entity
public class Feature extends DefinedTermBase {
	static Logger logger = Logger.getLogger(Feature.class);

	@XmlElement(name = "SupportsTextData")
	private boolean supportsTextData;
	
	@XmlElement(name = "SupportsQuantitativeData")
	private boolean supportsQuantitativeData;
	
	@XmlElement(name = "SupportsDistribution")
	private boolean supportsDistribution;
	
	@XmlElement(name = "SupportsIndividualAssociation")
	private boolean supportsIndividualAssociation;
	
	@XmlElement(name = "SupportsTaxonInteraction")
	private boolean supportsTaxonInteraction;
	
	@XmlElement(name = "SupportsCommonTaxonName")
	private boolean supportsCommonTaxonName;
	
	@XmlElementWrapper(name = "RecommendedModifierEnumerations")
	@XmlElement(name = "RecommendedModifierEnumeration")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<TermVocabulary> recommendedModifierEnumeration = new HashSet<TermVocabulary>();
	
	@XmlElementWrapper(name = "RecommendedStatisticalMeasures")
	@XmlElement(name = "RecommendedStatisticalMeasure")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<StatisticalMeasure> recommendedStatisticalMeasures = new HashSet<StatisticalMeasure>();
	
	@XmlElementWrapper(name = "SupportedCategoricalEnumerations")
	@XmlElement(name = "SupportedCategoricalEnumeration")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	private Set<TermVocabulary> supportedCategoricalEnumerations = new HashSet<TermVocabulary>();
	
/* ***************** CONSTRUCTOR AND FACTORY METHODS **********************************/
	

	/** 
	 * Class constructor: creates a new empty feature instance.
	 * 
	 * @see #Feature(String, String, String)
	 */
	public Feature() {
		super();
	}
	
	/** 
	 * Class constructor: creates a new feature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new feature to be created 
	 * @param	label  		 the string identifying the new feature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new feature to be created
	 * @see 				 #Feature()
	 */
	protected Feature(String term, String label, String labelAbbrev) {
		super(term, label, labelAbbrev);
	}

	/** 
	 * Creates a new empty feature instance.
	 * 
	 * @see #NewInstance(String, String, String)
	 */
	public static Feature NewInstance() {
		return new Feature();
	}
	
	/** 
	 * Creates a new feature instance with a description (in the {@link Language#DEFAULT() default language}),
	 * a label and a label abbreviation.
	 * 
	 * @param	term  		 the string (in the default language) describing the
	 * 						 new feature to be created 
	 * @param	label  		 the string identifying the new feature to be created
	 * @param	labelAbbrev  the string identifying (in abbreviated form) the
	 * 						 new feature to be created
	 * @see 				 #readCsvLine(List, Language)
	 * @see 				 #NewInstance()
	 */
	public static Feature NewInstance(String term, String label, String labelAbbrev){
		return new Feature(term, label, labelAbbrev);
	}

/* *************************************************************************************/
	
	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link QuantitativeData quantitative data} (true)
	 * or not (false). If this flag is set <i>this</i> feature can only apply to
	 * {@link TaxonDescription taxon descriptions} or {@link SpecimenDescription specimen descriptions}.
	 *  
	 * @return  the boolean value of the supportsQuantitativeData flag
	 */
	public boolean isSupportsQuantitativeData() {
		return supportsQuantitativeData;
	}

	/**
	 * @see	#isSupportsQuantitativeData() 
	 */
	public void setSupportsQuantitativeData(boolean supportsQuantitativeData) {
		this.supportsQuantitativeData = supportsQuantitativeData;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link TextData text data} (true)
	 * or not (false).
	 *  
	 * @return  the boolean value of the supportsTextData flag
	 */
	public boolean isSupportsTextData() {
		return supportsTextData;
	}

	/**
	 * @see	#isSupportsTextData() 
	 */
	public void setSupportsTextData(boolean supportsTextData) {
		this.supportsTextData = supportsTextData;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link Distribution distribution} objects
	 * (true) or not (false). This flag is set if and only if <i>this</i> feature
	 * is the {@link #DISTRIBUTION() distribution feature}.
	 *  
	 * @return  the boolean value of the supportsDistribution flag
	 */
	public boolean isSupportsDistribution() {
		return supportsDistribution;
	}

	/**
	 * @see	#isSupportsDistribution() 
	 */
	public void setSupportsDistribution(boolean supportsDistribution) {
		this.supportsDistribution = supportsDistribution;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link IndividualsAssociation individuals associations}
	 * (true) or not (false).
	 *  
	 * @return  the boolean value of the supportsIndividualAssociation flag
	 */
	public boolean isSupportsIndividualAssociation() {
		return supportsIndividualAssociation;
	}

	/**
	 * @see	#isSupportsIndividualAssociation() 
	 */
	public void setSupportsIndividualAssociation(
			boolean supportsIndividualAssociation) {
		this.supportsIndividualAssociation = supportsIndividualAssociation;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link TaxonInteraction taxon interactions}
	 * (true) or not (false).
	 *  
	 * @return  the boolean value of the supportsTaxonInteraction flag
	 */
	public boolean isSupportsTaxonInteraction() {
		return supportsTaxonInteraction;
	}

	/**
	 * @see	#isSupportsTaxonInteraction() 
	 */
	public void setSupportsTaxonInteraction(boolean supportsTaxonInteraction) {
		this.supportsTaxonInteraction = supportsTaxonInteraction;
	}

	/**
	 * Returns the boolean value of the flag indicating whether <i>this</i>
	 * feature can be described with {@link CommonTaxonName common names}
	 * (true) or not (false). This flag is set if and only if <i>this</i> feature
	 * is the {@link #COMMON_NAME() common name feature}.
	 *  
	 * @return  the boolean value of the supportsCommonTaxonName flag
	 */
	public boolean isSupportsCommonTaxonName() {
		return supportsCommonTaxonName;
	}

	/**
	 * @see	#isSupportsTaxonInteraction() 
	 */
	public void setSupportsCommonTaxonName(boolean supportsCommonTaxonName) {
		this.supportsCommonTaxonName = supportsCommonTaxonName;
	}

	/**
	 * Returns the set of {@link TermVocabulary term vocabularies} containing the
	 * {@link Modifier modifiers} recommended to be used for {@link DescriptionElementBase description elements}
	 * with <i>this</i> feature.
	 */
	@OneToMany
	public Set<TermVocabulary> getRecommendedModifierEnumeration() {
		return recommendedModifierEnumeration;
	}

	/**
	 * @see	#getRecommendedModifierEnumeration() 
	 */
	protected void setRecommendedModifierEnumeration(
			Set<TermVocabulary> recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration = recommendedModifierEnumeration;
	}

	/**
	 * Adds a {@link TermVocabulary term vocabulary} (with {@link Modifier modifiers}) to the set of
	 * {@link #getRecommendedModifierEnumeration() recommended modifier vocabularies} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param recommendedModifierEnumeration	the term vocabulary to be added
	 * @see    	   								#getRecommendedModifierEnumeration()
	 */
	public void addRecommendedModifierEnumeration(
			TermVocabulary recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.add(recommendedModifierEnumeration);
	}
	/** 
	 * Removes one element from the set of {@link #getRecommendedModifierEnumeration() recommended modifier vocabularies}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  recommendedModifierEnumeration	the term vocabulary which should be removed
	 * @see     								#getRecommendedModifierEnumeration()
	 * @see     								#addRecommendedModifierEnumeration(TermVocabulary)
	 */
	public void removeRecommendedModifierEnumeration(
			TermVocabulary recommendedModifierEnumeration) {
		this.recommendedModifierEnumeration.remove(recommendedModifierEnumeration);
	}

	/**
	 * Returns the set of {@link StatisticalMeasure statistical measures} recommended to be used
	 * in case of {@link QuantitativeData quantitative data} with <i>this</i> feature.
	 */
	@ManyToMany
    @JoinTable(
            name="DefinedTermBase_StatisticalMeasure"
        )
	@Cascade({CascadeType.SAVE_UPDATE})
	public Set<StatisticalMeasure> getRecommendedStatisticalMeasures() {
		return recommendedStatisticalMeasures;
	}

	/**
	 * @see	#getRecommendedStatisticalMeasures() 
	 */
	protected void setRecommendedStatisticalMeasures(
			Set<StatisticalMeasure> recommendedStatisticalMeasures) {
		this.recommendedStatisticalMeasures = recommendedStatisticalMeasures;
	}

	/**
	 * Adds a {@link StatisticalMeasure statistical measure} to the set of
	 * {@link #getRecommendedStatisticalMeasures() recommended statistical measures} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param recommendedStatisticalMeasure	the statistical measure to be added
	 * @see    	   							#getRecommendedStatisticalMeasures()
	 */
	public void addRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.add(recommendedStatisticalMeasure);
	}
	/** 
	 * Removes one element from the set of {@link #getRecommendedStatisticalMeasures() recommended statistical measures}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  recommendedStatisticalMeasure	the statistical measure which should be removed
	 * @see     								#getRecommendedStatisticalMeasures()
	 * @see     								#addRecommendedStatisticalMeasure(StatisticalMeasure)
	 */
	public void removeRecommendedStatisticalMeasure(
			StatisticalMeasure recommendedStatisticalMeasure) {
		this.recommendedStatisticalMeasures.remove(recommendedStatisticalMeasure);
	}

	/**
	 * Returns the set of {@link TermVocabulary term vocabularies} containing the list of
	 * possible {@link State states} to be used in {@link CategoricalData categorical data}
	 * with <i>this</i> feature.
	 */
	@OneToMany
	public Set<TermVocabulary> getSupportedCategoricalEnumerations() {
		return supportedCategoricalEnumerations;
	}

	/**
	 * @see	#getSupportedCategoricalEnumerations() 
	 */
	protected void setSupportedCategoricalEnumerations(
			Set<TermVocabulary> supportedCategoricalEnumerations) {
		this.supportedCategoricalEnumerations = supportedCategoricalEnumerations;
	}
	/**
	 * Adds a {@link TermVocabulary term vocabulary} to the set of
	 * {@link #getSupportedCategoricalEnumerations() supported state vocabularies} assigned
	 * to <i>this</i> feature.
	 * 
	 * @param supportedCategoricalEnumeration	the term vocabulary which should be removed
	 * @see    	   								#getSupportedCategoricalEnumerations()
	 */
	public void addSupportedCategoricalEnumeration(
			TermVocabulary supportedCategoricalEnumeration) {
		this.supportedCategoricalEnumerations.add(supportedCategoricalEnumeration);
	}
	/** 
	 * Removes one element from the set of {@link #getSupportedCategoricalEnumerations() supported state vocabularies}
	 * assigned to <i>this</i> feature.
	 *
	 * @param  supportedCategoricalEnumeration	the term vocabulary which should be removed
	 * @see     								#getSupportedCategoricalEnumerations()
	 * @see     								#addSupportedCategoricalEnumeration(TermVocabulary)
	 */
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
	
	
	/** 
	 * Creates and returns a new feature instance on the basis of a given string
	 * list (containing an UUID, an URI, a label and a description) and a given
	 * {@link Language language} to be associated with the description. Furthermore
	 * the flags concerning the supported subclasses of {@link DescriptionElementBase description elements}
	 * are set according to a particular string belonging to the given
	 * string list.<BR>
	 * This method overrides the readCsvLine method from {@link DefinedTermBase#readCsvLine(List, Language) DefinedTermBase}.
	 *
	 * @param  csvLine	the string list with elementary information for attributes
	 * @param  lang		the language in which the description has been formulated
	 * @see     		#NewInstance(String, String, String)
	 */
	@Override
	public ILoadableTerm readCsvLine(List csvLine, Language lang) {
		// TODO Auto-generated method stub
		super.readCsvLine(csvLine, lang);
		String text = (String)csvLine.get(4);
		if (text != null && text.length() >= 6){
			if ("1".equals(text.substring(0, 1))){this.setSupportsTextData(true);};
			if ("1".equals(text.substring(1, 2))){this.setSupportsQuantitativeData(true);};
			if ("1".equals(text.substring(2, 3))){this.setSupportsDistribution(true);};
			if ("1".equals(text.substring(3, 4))){this.setSupportsIndividualAssociation(true);};
			if ("1".equals(text.substring(4, 5))){this.setSupportsTaxonInteraction(true);};
			if ("1".equals(text.substring(5, 6))){this.setSupportsCommonTaxonName(true);};
		}
		return this;
	}

	/**
	 * Returns the feature identified through its immutable universally
	 * unique identifier (UUID).
	 * 
	 * @param	uuid	the universally unique identifier
	 * @return  		the feature corresponding to the given
	 * 					universally unique identifier
	 */
	public static final Feature getByUuid(UUID uuid){
		return (Feature)findByUuid(uuid);
	}
	
	/**
	 * Returns the "unknown" feature. This feature allows to store values of
	 * {@link DescriptionElementBase description elements} even if it is momentarily
	 * not known what they mean.
	 */
	public static final Feature UNKNOWN(){
		return getByUuid(uuidUnknown);
	}
	
	/**
	 * Returns the "description" feature. This feature allows to handle global
	 * {@link DescriptionElementBase description elements} for a global {@link DescriptionBase description}.<BR>
	 * The "description" feature is the highest level feature. 
	 */
	public static final Feature DESCRIPTION(){
		return getByUuid(uuidDescription);
	}

	/**
	 * Returns the "distribution" feature. This feature allows to handle only
	 * {@link Distribution distributions}.
	 * 
	 * @see	#isSupportsDistribution()
	 */
	public static final Feature DISTRIBUTION(){
		return getByUuid(uuidDistribution);
	}

	/**
	 * Returns the "discussion" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature DISCUSSION(){
		return getByUuid(uuidDiscussion);
	}
	
	/**
	 * Returns the "ecology" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "ecology" feature generalizes all other possible features concerning
	 * ecological matters.
	 */
	public static final Feature ECOLOGY(){
		return getByUuid(uuidEcology);
	}	
	
	/**
	 * Returns the "biology_ecology" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "biology_ecology" feature generalizes all possible features concerning
	 * biological aspects of ecological matters.
	 * 
	 * @see #ECOLOGY()
	 */
	public static final Feature BIOLOGY_ECOLOGY(){
		return getByUuid(uuidBiologyEcology);
	}
	
	/**
	 * Returns the "key" feature. This feature is the "upper" feature generalizing
	 * all features being used within an identification key.
	 */
	public static final Feature KEY(){
		return getByUuid(uuidKey);
	}		
	
	
	/**
	 * Returns the "materials_examined" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * mentioning which material has been examined in order to accomplish
	 * the description. This feature applies only to
	 * {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature MATERIALS_EXAMINED(){
		return getByUuid(uuidMaterialsExamined);
	}
	
	/**
	 * Returns the "materials_methods" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * mentioning which methods have been adopted to analyze the material in
	 * order to accomplish the description. This feature applies only to
	 * {@link SpecimenDescription specimen descriptions} or to {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature MATERIALS_METHODS(){
		return getByUuid(uuidMaterialsMethods);
	}
	
	/**
	 * Returns the "etymology" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}
	 * giving some information about the history of the taxon name. This feature applies only to
	 * {@link TaxonNameDescription taxon name descriptions}.
	 */
	public static final Feature ETYMOLOGY(){
		return getByUuid(uuidEtymology);
	}
		
	/**
	 * Returns the "diagnosis" feature. This feature can only be described
	 * with {@link TextData text data} or eventually with {@link CategoricalData categorical data}.
	 * This feature applies only to {@link SpecimenDescription specimen descriptions} or to
	 * {@link TaxonDescription taxon descriptions}.
	 */
	public static final Feature DIAGNOSIS(){
		return getByUuid(uuidDiagnosis);
	}

	
	/**
	 * Returns the "introduction" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature INTRODUCTION(){
		return getByUuid(uuidIntroduction);
	}

	/**
	 * Returns the "protologue" feature. This feature can only be described
	 * with {@link TextData text data} reproducing the content of the protologue 
	 * (or some information about it) of the taxon name. This feature applies only to
	 * {@link TaxonNameDescription taxon name descriptions}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature PROTOLOG(){
		return getByUuid(uuidProtolog);
	}
	/**
	 * Returns the "common_name" feature. This feature allows to handle only
	 * {@link CommonTaxonName common names}.
	 * 
	 * @see	#isSupportsCommonTaxonName()
	 */
	public static final Feature COMMON_NAME(){
		return getByUuid(uuidCommonName);
	}
	
	/**
	 * Returns the "phenology" feature. This feature can only be described
	 * with {@link CategoricalData categorical data} or eventually with {@link TextData text data}
	 * containing information time about recurring natural phenomena.
	 * This feature only applies to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "phenology" feature generalizes all other possible features
	 * concerning time information about particular natural phenomena
	 * (such as "first flight of butterflies").
	 */
	public static final Feature PHENOLOGY(){
		return getByUuid(uuidPhenology);
	}

	
	/**
	 * Returns the "occurrence" feature.
	 */
	public static final Feature OCCURRENCE(){
		return getByUuid(uuidOccurrence);
	}
	
	/**
	 * Returns the "citation" feature. This feature can only be described
	 * with {@link TextData text data}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature CITATION(){
		return getByUuid(uuidCitation);
	}
	
	/**
	 * Returns the "additional_publication" feature. This feature can only be
	 * described with {@link TextData text data} with information about a
	 * publication where a {@link TaxonNameBase taxon name} has also been published
	 * but which is not the {@link TaxonNameBase#getNomenclaturalReference() nomenclatural reference}.
	 * This feature applies only to {@link TaxonNameDescription taxon name descriptions}.
	 * 
	 * @see	#isSupportsTextData()
	 */
	public static final Feature ADDITIONAL_PUBLICATION(){
		return getByUuid(uuidAdditionalPublication);
	}
	
	
	/**
	 * Returns the "uses" feature. This feature only applies
	 * to {@link TaxonDescription taxon descriptions}.<BR>
	 * The "uses" feature generalizes all other possible features concerning
	 * particular uses (for instance "industrial use of seeds").
	 */
	public static final Feature USES(){
		return getByUuid(uuidUses);
	}
	
	
	/**
	 * Returns the "conservation" feature. This feature only applies
	 * to {@link SpecimenDescription specimen descriptions} and generalizes
	 * methods and conditions for the conservation of {@link Specimen specimens}.<BR>
	 */
	public static final Feature CONSERVATION(){
		return getByUuid(uuidConservation);
	}
	
	
	/**
	 * Returns the "cultivation" feature.
	 */
	public static final Feature CULTIVATION(){
		return getByUuid(uuidCultivation);
	}
	
	
	
	/**
	 * special kind of OrganismInteraction
	 */
	/**
	 * Returns the "hybrid_parent" feature. This feature can only be used
	 * by {@link TaxonInteraction taxon interactions}.<BR>
	 * <P>
	 * Note: It must be distinguished between hybrid relationships as
	 * relevant nomenclatural relationships between {@link BotanicalName plant names}
	 * on the one side and the biological relation between two {@link Taxon taxa}
	 * as it is here the case on the other one. 
	 * 
	 * @see	#isSupportsTaxonInteraction()
	 * @see	HybridRelationshipType
	 */
	public static final Feature HYBRID_PARENT(){
		//TODO
		logger.warn("HYBRID_PARENT not yet implemented");
		return null;
	}


}