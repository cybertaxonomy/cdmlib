package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.common.Language;

@Component
public class NaturalLanguageGenerator implements INaturalLanguageGenerator {

	private DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder = new DefaultQuantitativeDescriptionBuilder();
	private DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder = new DefaultCategoricalDescriptionBuilder();
	
	private String previousFeatureName;
	
	/**
	 * 
	 */
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(Language.DEFAULT());
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, languages);
	}
	
	
	/**
	 * 
	 */
	public List<TextData> generatePreferredNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, List<Language> languages) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, languages);
	}
	
	
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree, TaxonDescription description,	Language language) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, languages);
	}
	
	/** recursive function that goes through a tree containing the order in which the description has to be generated,
	 *  if an element of this tree matches one of the TaxonDescription, a DescriptionBuilder is called which returns a TextData with the corresponding description.
	 * 
	 * @param children
	 * @param parent
	 * @param description
	 * @param language The language in which the description has to be written
	 * @return
	 */
	private List<TextData> buildBranchesDescr(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, List<Language> languages) {
		List<TextData> listTextData = new ArrayList<TextData>(); ;
		if (!parent.isLeaf()){ // if this node is not a leaf, continue recursively (only the leaves of a FeatureTree contain states)
			Feature fref = parent.getFeature();
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				listTextData.addAll(buildBranchesDescr(fn.getChildren(),fn,description, languages));
			}
		}
		else { //once a leaf is reached
			Feature fref = parent.getFeature();
			if (fref!=null) { // needs a better algorithm
				int k=0;
					Set<DescriptionElementBase> elements = description.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){ // iterates over all the descriptions enclosed in the TaxonDescription
						DescriptionElementBase descriptionElement = deb.next();
						TextData textData;
						if (descriptionElement.getFeature().equals(fref)){ // if one matches the corresponding feature associated to this leaf
							if (descriptionElement instanceof CategoricalData) { // if this description is a CategoricalData, generate the according TextData
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								//textData = buildCategoricalDescr(categoricalData, language);
								textData = categoricalDescriptionBuilder.build(categoricalData, languages);
								//textData.putText(fref.getLabel(), Language.DEFAULT());
								TextData featureName = TextData.NewInstance(fref.getLabel(), Language.DEFAULT(), null);
								listTextData.add(featureName); // if you want to print the name of the feature (Should it be an option ?)
								listTextData.add(textData);
							}
							if (descriptionElement instanceof QuantitativeData) { // if this description is a QuantitativeData, generate the according TextData
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								textData = quantitativeDescriptionBuilder.build(quantitativeData, languages);
								TextData featureName = TextData.NewInstance(fref.getLabel(), Language.DEFAULT(), null);
								listTextData.add(featureName); // if you want to print the name of the feature
								listTextData.add(textData);
							}
						}
					}
			}
		}
		return listTextData;
	}

	/**
	 * 
	 * 
	 * @param quantitativeDescriptionBuilder
	 */
	public void setQuantitativeDescriptionBuilder(DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder){
		this.quantitativeDescriptionBuilder = quantitativeDescriptionBuilder;
	}
	
	/**
	 * 
	 * 
	 * @param categoricalDescriptionBuilder
	 */
	public void setCategoricalDescriptionBuilder(DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder){
		this.categoricalDescriptionBuilder = categoricalDescriptionBuilder;
	}
	
	
	/**
	 * @param featureTree
	 * @param description
	 * @param language
	 * @return
	 */
	public String generateStringNaturalLanguageDescription(FeatureTree featureTree, TaxonDescription description,	Language language) {
		List<Language> languages = new ArrayList<Language>();
		languages.add(language);
		return buildString(featureTree.getRootChildren(), featureTree.getRoot(), description, languages).toString();
	}
	
	/**
	 * recursive function that goes through a tree containing the order in which
	 * the description has to be generated, if an element of this tree matches
	 * one of the TaxonDescription, a DescriptionBuilder is called which returns
	 * a TextData with the corresponding description.
	 * <p>
	 * Also applies the folowing formatting rules which are special for data coming from Delta, DeltaAccess, DiversityDescriptions:
	 * 
	 * <ul>
	 * <li><b>(1.A) if( doSkipTextInBrackets) : </b>Label Text in brackets is always skipped the remaining text string I the TEXT_TO_DISPLAY. The text may contain multiple substrings tagged with the brackets. A tagged substring may also occur in the middle of the whole string.</li>
	 * <li><b>(1.B) else : </b>just remove the brackets
	 * <li><b>(2) : </b> If the TEXT_TO_DISPLAY is equal the TEXT_TO_DISPLAY of the previous element output of this text is suppressed.</li>
	 * </ul>
	 * 
	 * @param children
	 * @param parent
	 * @param description
	 * @param language
	 *            The language in which the description has to be written
	 * @return
	 */
	private StringBuilder buildString(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, List<Language> languages) {
		StringBuilder stringbuilder = new StringBuilder();
		if (!parent.isLeaf()){ // if this node is not a leaf, continue recursively (only the leaves of a FeatureTree contain states)
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				StringBuilder tempsb = buildString(fn.getChildren(),fn,description, languages);
				if (tempsb.length()>1) stringbuilder.append(tempsb.deleteCharAt(tempsb.length()-1));
//				if (tempsb.length()>1) stringbuilder.append(tempsb);
			}
			stringbuilder.append('.');
		}
		else { //once a leaf is reached
			Feature fref = parent.getFeature();
			if (fref!=null) { // needs a better algorithm
				int k=0;
					Set<DescriptionElementBase> elements = description.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){ // iterates over all the descriptions enclosed in the TaxonDescription
						DescriptionElementBase descriptionElement = deb.next();
						TextData textData;
						if (descriptionElement.getFeature().equals(fref)){ // if one matches the corresponding feature associated to this leaf
							if (descriptionElement instanceof CategoricalData) { // if this description is a CategoricalData, generate the according TextData
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								//textData = buildCategoricalDescr(categoricalData, language);
								textData = categoricalDescriptionBuilder.build(categoricalData, languages);
								//textData.putText(fref.getLabel(), Language.DEFAULT());
								String featureName = StringUtils.substringBefore(fref.getLabel(), "<");
								if (previousFeatureName==null){
									previousFeatureName = featureName;
									String featureString = categoricalDescriptionBuilder.buildFeature(fref,true);
									stringbuilder.append(featureString.substring(0,1).toUpperCase() + featureString.substring(1));
								}
								else if (!featureName.contains(previousFeatureName)) {
									stringbuilder.append(". ");
									previousFeatureName = featureName;
									String featureString = categoricalDescriptionBuilder.buildFeature(fref,true);
									stringbuilder.append(featureString.substring(0,1).toUpperCase() + featureString.substring(1)); // if you want to print the name of the feature (Should it be an option ?)
								}
								stringbuilder.append(textData.getText(Language.DEFAULT()));
								stringbuilder.append(',');
							}
							if (descriptionElement instanceof QuantitativeData) { // if this description is a QuantitativeData, generate the according TextData
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								textData = quantitativeDescriptionBuilder.build(quantitativeData, languages);
								String featureName = StringUtils.substringBefore(fref.getLabel(), "<");
								if (previousFeatureName==null){
									previousFeatureName = featureName;
									String featureString = quantitativeDescriptionBuilder.buildFeature(fref,true);
									stringbuilder.append(featureString.substring(0,1).toUpperCase() + featureString.substring(1));
								}
								else if (!featureName.contains(previousFeatureName)) {
									stringbuilder.append(". ");
									previousFeatureName = featureName;
									String featureString = quantitativeDescriptionBuilder.buildFeature(fref,true);
									stringbuilder.append(featureString.substring(0,1).toUpperCase() + featureString.substring(1)); // if you want to print the name of the feature (Should it be an option ?)
								}
								stringbuilder.append(textData.getText(Language.DEFAULT()));
								stringbuilder.append(',');
							}
						}
					}
			}
		}
		return stringbuilder;
	}


}
