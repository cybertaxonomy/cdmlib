package eu.etaxonomy.cdm.api.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.impl.xpath.regex.ParseException;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.Modifier;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.common.Language;

@Component
public class NaturalLanguageGenerator implements INaturalLanguageGenerator {

	public List<TextData> generateNaturalLanguageDescription(FeatureTree featureTree,TaxonDescription description) {
		return buildBranchesDescr(featureTree.getRootChildren(), featureTree.getRoot(), description, false);
	}
	
	private List<TextData> buildBranchesDescr(List<FeatureNode> children, FeatureNode parent, TaxonDescription description, boolean leaf) {
		List<TextData> listTextData = new ArrayList<TextData>(); ;
		if (!parent.isLeaf()){
			Feature fref = parent.getFeature();
			for (Iterator<FeatureNode> ifn = children.iterator() ; ifn.hasNext() ;){
				FeatureNode fn = ifn.next();
				listTextData.addAll(buildBranchesDescr(fn.getChildren(),fn,description, leaf));
			}
		}
		else {
			Feature fref = parent.getFeature();
			if (fref!=null) { // needs a better algorithm
				int k=0;
					Set<DescriptionElementBase> elements = description.getElements();
					for (Iterator<DescriptionElementBase> deb = elements.iterator() ; deb.hasNext() ;){
						DescriptionElementBase descriptionElement = deb.next();
						TextData textData = TextData.NewInstance();
						if (descriptionElement.getFeature().equals(fref)){
							if (descriptionElement instanceof CategoricalData) {
								CategoricalData categoricalData = (CategoricalData) descriptionElement;
								textData = buildCategoricalDescr(categoricalData);
								listTextData.add(textData);
							}
							if (descriptionElement instanceof QuantitativeData) {
								QuantitativeData quantitativeData = (QuantitativeData) descriptionElement;
								textData = buildQuantitativeDescr(quantitativeData);
								listTextData.add(textData);
							}
						}
					}
				leaf = true;
			}
		}
		return listTextData;
	}

	//TODO manage different languages ?
	private TextData buildQuantitativeDescr (QuantitativeData quantitativeData) throws ParseException {

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
			QuantitativeDescription.append(" from " + minvalue + " to " + maxvalue + unit);
		}
		else if (min) {
			QuantitativeDescription.append(" from " + minvalue + " " + unit);
		}
		else if (max) {
			QuantitativeDescription.append(" up to " + maxvalue + " " + unit);
		}
		if ((max||min)&&(lowerb||upperb)) {
			QuantitativeDescription.append(",");
		}
		if ((lowerb||upperb)&&(min||max)) {
			QuantitativeDescription.append(" most frequently");
		}
		if (upperb && lowerb) {
			QuantitativeDescription.append(" from " + lowerbvalue + " to " + upperbvalue + unit);
		}
		else if (lowerb) {
			QuantitativeDescription.append(" from " + lowerbvalue + " " + unit);
		}
		else if (upperb) {
			QuantitativeDescription.append(" up to " + upperbvalue + " " + unit);
		}
		if (((max||min)&&(average))||((lowerb||upperb)&&(average))) {
			QuantitativeDescription.append(",");
		}
		if (average) {
			QuantitativeDescription.append(" " + averagevalue + unit + " on average ");
			if (sd) {
				QuantitativeDescription.append("(+/- " + sdvalue + ")");
			}
		}
		textData.putText(QuantitativeDescription.toString(), Language.ENGLISH());
		return textData;
	}
	
	private TextData buildCategoricalDescr(CategoricalData categoricalData) throws ParseException {
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
				CategoricalDescription.append(" " + modifier.getLabel());
			}
			CategoricalDescription.append(" " + s.getLabel());
		}
		textData.putText(CategoricalDescription.toString(), Language.ENGLISH());
		return textData ;
	}
	
}
