package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xerces.impl.xpath.regex.ParseException;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;

@Component
public class NaturalLanguageGenerator implements INaturalLanguageGenerator {

	private DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder = new DefaultQuantitativeDescriptionBuilder();
	private DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder = new DefaultCategoricalDescriptionBuilder();
	
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, Language.DEFAULT(), false);
	}
	
	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description, Language language) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, language, false);
	}
	
	private List<TextData> buildBranchesDescr(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, Language language, boolean leaf) {
		List<TextData> listTextData = new ArrayList<TextData>(); ;
		if (!parent.isLeaf()){
			Feature fref = parent.getFeature();
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				listTextData.addAll(buildBranchesDescr(fn.getChildren(),fn,description, language, leaf));
			}
		}
		else {
			Feature fref = parent.getFeature();
			if (fref!=null) { // needs a better algorithm
				Set<DescriptionElementBase> elements = description.getElements();
				for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
					DescriptionElementBase descriptionElement = deb.next();
					TextData textData = TextData.NewInstance();
					if (descriptionElement.getFeature().equals(fref)){
						if (descriptionElement instanceof CategoricalData) {
							CategoricalData categoricalData = (CategoricalData) descriptionElement;
							//textData = buildCategoricalDescr(categoricalData, language);
							textData = categoricalDescriptionBuilder.build(categoricalData);
							listTextData.add(textData);
						}
						if (descriptionElement instanceof QuantitativeData) {
							QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
							textData = quantitativeDescriptionBuilder.build(quantitativeData);
							listTextData.add(textData);
						}
					}
				}
				leaf = true;
			}
		}
		return listTextData;
	}

	//Deprecated
	private TextData buildQuantitativeDescr (QuantitativeData quantitativeData, Language language) throws ParseException {

		boolean average = false;
		float averagevalue = new Float(0);
		boolean sd = false;
		float sdvalue = new Float(0);
		boolean min = false;
		float minvalue = new Float(0);
		boolean max = false;
		float maxvalue = new Float(0);
		boolean lowerb = false;
		float lowerbvalue = new Float(0);
		boolean upperb = false;
		float upperbvalue = new Float(0);
		
		NaturalLanguageTerm nltFrom = NaturalLanguageTerm.FROM();
		String from = nltFrom.getPreferredRepresentation(language).getLabel();
		NaturalLanguageTerm nltTo = NaturalLanguageTerm.TO();
		String to = nltTo.getPreferredRepresentation(language).getLabel();
		NaturalLanguageTerm nltUp_To = NaturalLanguageTerm.UP_TO();
		String up_To = nltUp_To.getPreferredRepresentation(language).getLabel();
		NaturalLanguageTerm nltMost_Frequently = NaturalLanguageTerm.MOST_FREQUENTLY();
		String most_Frequently = nltMost_Frequently.getPreferredRepresentation(language).getLabel();
		NaturalLanguageTerm nltOn_Average = NaturalLanguageTerm.ON_AVERAGE();
		String on_Average = nltOn_Average.getPreferredRepresentation(language).getLabel();
		NaturalLanguageTerm nltMore_Or_Less = NaturalLanguageTerm.MORE_OR_LESS();
		String more_Or_Less = nltMore_Or_Less.getPreferredRepresentation(language).getLabel();
		String space = " ";
		
		StringBuilder QuantitativeDescription = new StringBuilder();
		Feature feature = quantitativeData.getFeature();
		TextData textData = TextData.NewInstance(feature);
		QuantitativeDescription.append(" "+feature.getLabel());
		String unit = quantitativeData.getUnit().getLabel();
		Set<StatisticalMeasurementValue> statisticalValues = quantitativeData.getStatisticalValues();
		for (Iterator<StatisticalMeasurementValue> smv = statisticalValues.iterator() ; smv.hasNext() ;){
			StatisticalMeasurementValue statisticalValue = smv.next();
			StatisticalMeasure type = statisticalValue.getType();
			if (type.equals(StatisticalMeasure.AVERAGE())) {
				average = true;
				averagevalue = statisticalValue.getValue();
			} else if(type.equals(StatisticalMeasure.STANDARD_DEVIATION())) {
				sd = true;
				sdvalue = statisticalValue.getValue();
			} else if (type.equals(StatisticalMeasure.MIN())) {
				min = true;
				minvalue = statisticalValue.getValue();
			} else if (type.equals(StatisticalMeasure.MAX())) {
				max = true;
				maxvalue = statisticalValue.getValue();
			} else if (type.equals(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY())) {
				lowerb = true;
				lowerbvalue = statisticalValue.getValue();
			} else if (type.equals(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY())) {
				upperb = true;
				upperbvalue = statisticalValue.getValue();
			}
		}
		if (max && min) {
			QuantitativeDescription.append(space + from + space + minvalue + space + to + space + maxvalue + space + unit);
		}
		else if (min) {
			QuantitativeDescription.append(space + from + space + minvalue + space + unit);
		}
		else if (max) {
			QuantitativeDescription.append(space + up_To + space + maxvalue + space + unit);
		}
		if ((max||min)&&(lowerb||upperb)) {
			QuantitativeDescription.append(","); // fusion avec dessous ?
		}
		if ((lowerb||upperb)&&(min||max)) {
			QuantitativeDescription.append(space + most_Frequently + space);
		}
		if (upperb && lowerb) {
			QuantitativeDescription.append(space + from + space + lowerbvalue + space + to + space + upperbvalue + space + unit);
		}
		else if (lowerb) {
			QuantitativeDescription.append(space + from + lowerbvalue + space + unit);
		}
		else if (upperb) {
			QuantitativeDescription.append(space + up_To + space + upperbvalue + space + unit);
		}
		if (((max||min)&&(average))||((lowerb||upperb)&&(average))) {
			QuantitativeDescription.append(",");
		}
		if (average) {
			QuantitativeDescription.append(space + averagevalue + space + unit + space + on_Average);
			if (sd) {
				QuantitativeDescription.append("("+ more_Or_Less + space + sdvalue + ")");
			}
		}
		textData.putText(QuantitativeDescription.toString(), language);
		return textData;
	}
	
	//Deprecated
	private TextData buildCategoricalDescr(CategoricalData categoricalData, Language language) throws ParseException {
		Feature feature = categoricalData.getFeature();
		TextData textData = TextData.NewInstance(feature);
		List<StateData> states = categoricalData.getStates();
		
		StringBuilder CategoricalDescription = new StringBuilder();
		//CategoricalDescription.append(" "+feature.getLabel());

		for (Iterator<StateData> sd = states.iterator() ; sd.hasNext() ;){
			StateData stateData = sd.next();
			State s = stateData.getState();
			Set<Modifier> modifiers = stateData.getModifiers();
			for (Iterator<Modifier> mod = modifiers.iterator() ; mod.hasNext() ;){
				Modifier modifier = mod.next();
				CategoricalDescription.append(" " + modifier.getPreferredRepresentation(language).getLabel());
			}
			CategoricalDescription.append(" " + s.getPreferredRepresentation(language).getLabel());
		}
		textData.putText(CategoricalDescription.toString(), language);
		return textData ;
	}
	
	public void setQuantitativeDescriptionBuilder(DescriptionBuilder<QuantitativeData> quantitativeDescriptionBuilder){
		this.quantitativeDescriptionBuilder = quantitativeDescriptionBuilder;
	}
	
	public void setCategoricalDescriptionBuilder(DescriptionBuilder<CategoricalData> categoricalDescriptionBuilder){
		this.categoricalDescriptionBuilder = categoricalDescriptionBuilder;
	}
}
