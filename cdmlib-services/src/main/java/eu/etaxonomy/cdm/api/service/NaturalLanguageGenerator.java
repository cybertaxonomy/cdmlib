package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	
	
	/**
	 * 
	 */
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, Language.DEFAULT());
	}
	
	
	/**
	 * 
	 */
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, Language language) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, language);
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
	private List<TextData> buildBranchesDescr(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, Language language) {
		List<TextData> listTextData = new ArrayList<TextData>(); ;
		if (!parent.isLeaf()){ // if this node is not a leaf, continue recursively (only the leaves of a FeatureTree contain states)
			Feature fref = parent.getFeature();
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				listTextData.addAll(buildBranchesDescr(fn.getChildren(),fn,description, language));
			}
		}
		else { //once a leaf is reached
			Feature fref = parent.getFeature();
			if (fref!=null) { // needs a better algorithm
				int k=0;
					Set<DescriptionElementBase> elements = description.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){ // iterates over all the descriptions enclosed in the TaxonDescription
						DescriptionElementBase descriptionElement = deb.next();
						TextData textData;// = TextData.NewInstance();
						if (descriptionElement.getFeature().equals(fref)){ // if one matches the corresponding feature associated to this leaf
							if (descriptionElement instanceof CategoricalData) { // if this description is a CategoricalData, generate the according TextData
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								//textData = buildCategoricalDescr(categoricalData, language);
								textData = categoricalDescriptionBuilder.build(categoricalData);
								//textData.putText(fref.getLabel(), Language.DEFAULT());
								TextData featureName = TextData.NewInstance(fref.getLabel(), Language.DEFAULT(), null);
								listTextData.add(featureName); // if you want to print the name of the feature (Should it be an option ?)
								listTextData.add(textData);
							}
							if (descriptionElement instanceof QuantitativeData) { // if this description is a QuantitativeData, generate the according TextData
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								textData = quantitativeDescriptionBuilder.build(quantitativeData);
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
}
