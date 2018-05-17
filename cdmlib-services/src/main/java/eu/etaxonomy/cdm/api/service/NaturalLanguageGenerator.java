package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.Language;


/**
 * Generator of natural language descriptions from TaxonDescriptions.
 * 
 * @author m.venin
 * @since 13.04.2010
 * @version 1.0
 */
@Component
public class NaturalLanguageGenerator implements INaturalLanguageGenerator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NaturalLanguageGenerator.class);

	private String firstSeparator = ",";
	private String secondSeparator = ".";
	private List<Integer> levels = new ArrayList<Integer>();

	private DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder = new DefaultQuantitativeDescriptionBuilder();
	private DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder = new DefaultCategoricalDescriptionBuilder();

	private TextData previousTextData;
	
	DeltaTextDataProcessor deltaTextDataProcessor = new DeltaTextDataProcessor();

	private Map<String, INaturalLanguageTextDataProcessor> elementProcessors;

	private Set<INaturalLanguageTextDataProcessor> applicableElementProcessors = new HashSet<INaturalLanguageTextDataProcessor>();

	/**
	 * Change the first separator used by generateSingleTextData. By default ",".
	 * 
	 * @param separator
	 */
	public void setFirstSeparator(String separator){
		firstSeparator=separator;
	}

	public String getFirstSeparator(){
		return firstSeparator;
	}

	/**
	 * Change the second separator used by generateSingleTextData. By default ".".
	 * 
	 * @param separator
	 */
	public void setSecondSeparator(String separator){
		secondSeparator=separator;
	}

	public String getSecondSeparator(){
		return secondSeparator;
	}

	/**
	 * @param quantitativeDescriptionBuilder
	 */
	public void setQuantitativeDescriptionBuilder(DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder){
		this.quantitativeDescriptionBuilder = quantitativeDescriptionBuilder;
	}

	/**
	 * @param categoricalDescriptionBuilder
	 */
	public void setCategoricalDescriptionBuilder(DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder){
		this.categoricalDescriptionBuilder = categoricalDescriptionBuilder;
	}

	/**
	 * @return the element processors of this generator
	 */
	public Map<String, INaturalLanguageTextDataProcessor> getElementProcessors() {
		return elementProcessors;
	}

	/**
	 * The keys of the elementProcessors map are regular expressions which are
	 * being used to identify the those Descriptions to which the mapped
	 * NaturalLanguageTextDataProcessor is applicable.
	 * 
	 * @param elementProcessors
	 */
	public void setElementProcessors(
			Map<String, INaturalLanguageTextDataProcessor> elementProcessors) {
		this.elementProcessors = elementProcessors;
	}

	/**
	 * Looks for technical annotations, if one matches a regular expression of the element processors
	 * the associated processor is added to the applicable element processors which will then be applied
	 * when generating the description.
	 * 
	 * @param annotations the set of annotations of the description
	 */
	private void initNaturalLanguageDescriptionElementProcessors(Set<Annotation> annotations) {

		if(annotations != null){
			for(Annotation annotation : annotations){
				if(annotation.getAnnotationType().equals(AnnotationType.TECHNICAL())){
					if (elementProcessors!=null){
						for(String regex : elementProcessors.keySet()){
							if(annotation.getText().matches(regex)){
								applicableElementProcessors.add(elementProcessors.get(regex));
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Applies the list of applicable processors to a TextData.
	 * 
	 * @param textData the TextData to be modified
	 * @param previousTextData the TextData corresponding to the feature of the previous level in the tree
	 */
	private void applyNaturalLanguageDescriptionElementProcessors(TextData textData, TextData previousTextData){
		for(INaturalLanguageTextDataProcessor processor : applicableElementProcessors){
			processor.process(textData, previousTextData);
		}
	}


	/**
	 * The most simple function to generate a description. The language used is the default one.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * 
	 * @return a list of TextData, each one being a basic element of the natural language description
	 */
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description) {
		return generateNaturalLanguageDescription(featureTree,description,Language.DEFAULT());
	}



	/**
	 * Generate a description in a specified language.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * @param language the language in which the description has to be printed
	 * 
	 * @return a list of TextData, each one being a basic element of the natural language description
	 */
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree, TaxonDescription description,	Language language) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		initNaturalLanguageDescriptionElementProcessors(description.getAnnotations());
		return generatePreferredNaturalLanguageDescription(featureTree,description,languages);
	}

	/**
	 * Generate a description with a specified list of preferred languages.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * @param languages the ordered list of languages preferred for printing the description
	 * 
	 * @return a list of TextData, each one being a basic element of the natural language description
	 */
	public List<TextData> generatePreferredNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, List<Language> languages) {
		initNaturalLanguageDescriptionElementProcessors(description.getAnnotations());
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, languages,0);
	}

	/**
	 * Generate a description as a single paragraph in a TextData.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * 
	 * @return a TextData in the default language.
	 */
	public TextData generateSingleTextData(FeatureTree featureTree, TaxonDescription description) {
		return generateSingleTextData(featureTree,description,Language.DEFAULT());
	}

	/**
	 * Generate a description as a single paragraph in a TextData.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * @param language the language in which the description has to be printed
	 * 
	 * @return a TextData in the specified language.
	 */
	public TextData generateSingleTextData(FeatureTree featureTree, TaxonDescription description, Language language) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		return generatePreferredSingleTextData(featureTree,description,languages);
	}

	/**
	 * Generate a description with a specified list of preferred languages.
	 * 
	 * @param featureTree the FeatureTree holding the order in which features and their states must be printed
	 * @param description the TaxonDescription with all the data
	 * @param languages the ordered list of languages preferred for printing the description
	 * 
	 * @return a TextData using the languages (in the given order of preference)
	 */
	public TextData generatePreferredSingleTextData(FeatureTree featureTree, TaxonDescription description, List<Language> languages) {
		levels.clear(); // before the start, the table containing the levels of each node must be cleared
		// Note: this is not the most efficient way to keep track of the levels of the nodes but it allows some flexibility
		List<TextData> texts = generatePreferredNaturalLanguageDescription(featureTree,description, languages);// first get the description as a raw list of TextData

		StringBuilder descriptionStringBuilder = new StringBuilder(); // the StringBuilder used to generate the description
		int i = 0,j,level; // i is used to store the index of the TextData to use
		boolean startSentence = false, firstOne = true;

		for (j=0 ; j<levels.size() ; j++){
			level = levels.get(j);
			if (level==-1){
				if ((j+1)<levels.size() && levels.get(j+1).equals(0)){ // if this node is the direct father of a leaf
					descriptionStringBuilder.append(secondSeparator + " ");
					startSentence=true;
					firstOne=false;
					String asString = texts.get(i).getText(Language.DEFAULT()).toString();
					if (asString.length()>1) descriptionStringBuilder.append(asString.substring(0,1).toUpperCase() + asString.substring(1));
				}
				i++;
			}
			else if (level==0) { // if this node is a leaf
				if (startSentence) descriptionStringBuilder.append(texts.get(i).getText(Language.DEFAULT()));
				else descriptionStringBuilder.append(firstSeparator + texts.get(i).getText(Language.DEFAULT()));
				startSentence=false;
				i++;
			}
			else {
				if (!firstOne && levels.get(j-1).equals(0)){ // if this node corresponds to the states linked to the previous leaf
					if (i<texts.size()) descriptionStringBuilder.append(texts.get(i).getText(Language.DEFAULT()));
					i++;
				}
			}
		}
		descriptionStringBuilder.append(secondSeparator);
		String returnString = descriptionStringBuilder.toString();
		returnString = StringUtils.replace(returnString, "  ", " ");
		returnString = StringUtils.removeStart(returnString, secondSeparator + " ");
		return TextData.NewInstance(returnString,Language.DEFAULT(),TextFormat.NewInstance("", "Text", ""));
	}



	/** recursive function that goes through a tree containing the order in which the description has to be generated,
	 *  if an element of this tree matches one of the TaxonDescription, a DescriptionBuilder is called which returns a TextData with the corresponding description.
	 * 
	 * @param children the children of the feature node considered
	 * @param parent the feature node considered
	 * @param description the TaxonDescription element for which we want a natural language output
	 * @param language The language in which the description has to be written
	 * @param floor integer to keep track of the level in the tree
	 * @return a list of TextData elements containing the part of description corresponding to the feature node considered
	 */
	private List<TextData> buildBranchesDescr(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, List<Language> languages, int floor) {
		List<TextData> listTextData = new ArrayList<TextData>();
		floor++; // counter to know the current level in the tree

		if (!parent.isLeaf()){ // if this node is not a leaf, continue recursively (only the leaves of a FeatureTree contain states)
			levels.add(new Integer(floor)); // the level of the different nodes in the tree are kept, thus it is easier to build a structured text out of the List<TextData>
			Feature feature = parent.getFeature();
			TextData featureName;
			if (feature!=null && feature.getLabel()!=null){ // if a node is associated to a feature
				featureName = categoricalDescriptionBuilder.buildTextDataFeature(feature, languages);
				levels.add(new Integer(-1)); // it is indicated by a '-1' after its level
				listTextData.add(featureName); // the TextData representing the name of the feature is concatenated to the list
			}
			else featureName = new TextData(); // else an empty TextData is created (because we keep track of the features, it is useful to inform when the upper node has no feature attached)

			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				previousTextData = featureName; // this allows to keep track of the name of the feature one level up in the tree
				FeatureNode fn = ifn.next();
				listTextData.addAll(buildBranchesDescr(fn.getChildNodes(),fn,description, languages, floor));
			}
		}
		else { //once a leaf is reached
			Feature feature = parent.getFeature();
			if (feature!=null && (feature.isSupportsQuantitativeData() || feature.isSupportsCategoricalData())) {
				Set<DescriptionElementBase> elements = description.getElements();
				for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){ // iterates over all the descriptions enclosed in the TaxonDescription
					DescriptionElementBase descriptionElement = deb.next();
					if (descriptionElement.getFeature().equals(feature)){ // if one matches the corresponding feature associated to this leaf
						if (descriptionElement instanceof CategoricalData || descriptionElement instanceof QuantitativeData){
							TextData featureTextData;
							TextData statesTextData;
							if (descriptionElement instanceof CategoricalData) { // if this description is a CategoricalData, generate the according TextData
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								statesTextData = categoricalDescriptionBuilder.build(categoricalData, languages);
								featureTextData = categoricalDescriptionBuilder.buildTextDataFeature(feature, languages);
							}
							else { // if this description is a QuantitativeData, generate the according TextData
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								statesTextData = quantitativeDescriptionBuilder.build(quantitativeData, languages);
								featureTextData = quantitativeDescriptionBuilder.buildTextDataFeature(feature, languages);
							}
							applyNaturalLanguageDescriptionElementProcessors(featureTextData, previousTextData);
							levels.add(new Integer(0)); // 0 indicates a feature, which is a leaf of the tree
							listTextData.add(featureTextData);
							levels.add(new Integer(floor)); // this represents the level of the feature and means it is followed by a TextData containing the states of the feature
							listTextData.add(statesTextData);
						}
					}
				}
			}
		}
		return listTextData;
	}	

}
