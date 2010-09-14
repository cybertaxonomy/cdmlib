package eu.etaxonomy.cdm.api.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;

public class MicroFormatQuantitativeDescriptionBuilder extends AbstractQuantitativeDescriptionBuilder {
	
	private String spanEnd = "</span>";
	
	@Override
	protected TextData doBuild(Map<StatisticalMeasure,Float> measures, MeasurementUnit mUnit, List<Language> languages){
		StringBuilder QuantitativeDescription = new StringBuilder(); // this StringBuilder is used to concatenate the different words of the description before saving it in the TextData
		TextData textData = TextData.NewInstance(); // TextData that will contain the description and the language corresponding

		// booleans indicating whether a kind of value is present or not and the float that will eventually hold the value
		boolean average = false;
		String averagevalue = null;
		boolean sd = false;
		String sdvalue = null;
		boolean min = false;
		String minvalue = null;
		boolean max = false;
		String maxvalue = null;
		boolean lowerb = false;
		String lowerbvalue = null;
		boolean upperb = false;
		String upperbvalue = null;
		
		String unit = "(unknown unit)";
		if ((mUnit!=null)&&(mUnit.getLabel()!=null)){
			unit = spanClass("unit") + mUnit.getLabel() + spanEnd;
		}
		
		// the different linking words are taken from NaturalLanguageTerm.class (should this be changed ?)
		NaturalLanguageTerm nltFrom = NaturalLanguageTerm.FROM();
		String from = nltFrom.getPreferredRepresentation(languages).getLabel();
		NaturalLanguageTerm nltTo = NaturalLanguageTerm.TO();
		String to = nltTo.getPreferredRepresentation(languages).getLabel();
		NaturalLanguageTerm nltUp_To = NaturalLanguageTerm.UP_TO();
		String up_To = nltUp_To.getPreferredRepresentation(languages).getLabel();
		NaturalLanguageTerm nltMost_Frequently = NaturalLanguageTerm.MOST_FREQUENTLY();
		String most_Frequently = nltMost_Frequently.getPreferredRepresentation(languages).getLabel();
		NaturalLanguageTerm nltOn_Average = NaturalLanguageTerm.ON_AVERAGE();
		String on_Average = nltOn_Average.getPreferredRepresentation(languages).getLabel();
		NaturalLanguageTerm nltMore_Or_Less = NaturalLanguageTerm.MORE_OR_LESS();
		String more_Or_Less = nltMore_Or_Less.getPreferredRepresentation(languages).getLabel();
		String space = " "; // should "space" be considered as a linking word and thus be stored in NaturalLanguageTerm.class ?
		
		// the booleans and floats are updated according to the presence or absence of values
			if (measures.containsKey(StatisticalMeasure.AVERAGE())) {
				average = true;
				averagevalue = spanClass("measurement") + measures.get(StatisticalMeasure.AVERAGE()) + spanEnd;
			} else if(measures.containsKey(StatisticalMeasure.STANDARD_DEVIATION())) {
				sd = true;
				sdvalue = spanClass("measurement") + measures.get(StatisticalMeasure.STANDARD_DEVIATION()) + spanEnd;
			} else if (measures.containsKey(StatisticalMeasure.MIN())) {
				min = true;
				minvalue = spanClass("measurement") + measures.get(StatisticalMeasure.MIN()) + spanEnd;
			} else if (measures.containsKey(StatisticalMeasure.MAX())) {
				max = true;
				maxvalue = spanClass("measurement") + measures.get(StatisticalMeasure.MAX()) + spanEnd;
			} else if (measures.containsKey(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY())) {
				lowerb = true;
				lowerbvalue = spanClass("measurement") + measures.get(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY()) + spanEnd;
			} else if (measures.containsKey(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY())) {
				upperb = true;
				upperbvalue = spanClass("measurement") + measures.get(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY()) + spanEnd;
			}
			
			QuantitativeDescription.append(spanClass("state"));
		// depending on the different associations of values, a sentence is built	
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
			QuantitativeDescription.append(","); // merge with below ?
		}
		if ((lowerb||upperb)&&(min||max)) {
			QuantitativeDescription.append(space + most_Frequently + space);
		}
		if (upperb && lowerb) {
			QuantitativeDescription.append(space + from + space + lowerbvalue + space + to + space + upperbvalue + space + unit);
		}
		else if (lowerb) {
			QuantitativeDescription.append(space + from + space + lowerbvalue + space + unit);
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
		QuantitativeDescription.append(spanEnd);
		textData.putText(QuantitativeDescription.toString(), languages.get(0)); // which language should be put here ?
		textData.setFormat(TextFormat.NewInstance(null, "HTML",null )); // the data format is set (not yet real HTML)
		
		return textData;
	}
	
	protected String buildFeature(Feature feature, boolean doItBetter){
		if (feature==null || feature.getLabel()==null) return "";
		else {
			if (doItBetter) {
				String betterString = StringUtils.substringBefore(feature.getLabel(), "<");
				return StringUtils.removeEnd(betterString, " ");
			}
			else	return feature.getLabel();
		}
	}
	
	private String spanClass(String classString){
		return("<span class=\""+classString+"\">");
	}

	
}
