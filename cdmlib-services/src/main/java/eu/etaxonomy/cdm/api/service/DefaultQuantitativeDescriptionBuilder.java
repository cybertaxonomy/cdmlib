package eu.etaxonomy.cdm.api.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.description.TextFormat;

public class DefaultQuantitativeDescriptionBuilder extends AbstractQuantitativeDescriptionBuilder {
	
	@Override
	protected TextData doBuild(Map<StatisticalMeasure,Float> measures, MeasurementUnit mUnit){
		StringBuilder QuantitativeDescription = new StringBuilder();
		TextData textData = TextData.NewInstance();
		Language language = Language.DEFAULT();
		
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
		
		String unit = mUnit.getLabel();
		
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
		
			if (measures.containsKey(StatisticalMeasure.AVERAGE())) {
				average = true;
				averagevalue = measures.get(StatisticalMeasure.AVERAGE());
			} else if(measures.containsKey(StatisticalMeasure.STANDARD_DEVIATION())) {
				sd = true;
				sdvalue = measures.get(StatisticalMeasure.STANDARD_DEVIATION());
			} else if (measures.containsKey(StatisticalMeasure.MIN())) {
				min = true;
				minvalue = measures.get(StatisticalMeasure.MIN());
			} else if (measures.containsKey(StatisticalMeasure.MAX())) {
				max = true;
				maxvalue = measures.get(StatisticalMeasure.MAX());
			} else if (measures.containsKey(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY())) {
				lowerb = true;
				lowerbvalue = measures.get(StatisticalMeasure.TYPICAL_LOWER_BOUNDARY());
			} else if (measures.containsKey(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY())) {
				upperb = true;
				upperbvalue = measures.get(StatisticalMeasure.TYPICAL_UPPER_BOUNDARY());
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
		textData.setFormat(TextFormat.NewInstance(null, "HTML",null ));
		
		return textData;
	}
}
