/**
* Copyright (C) 2011 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.NaturalLanguageTerm;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * @author m.venin
 */
public class DefaultQuantitativeDescriptionBuilder extends QuantitativeDescriptionBuilderBase {

	private String space = " ";

	@Override
	protected TextData doBuild(Map<StatisticalMeasure,List<BigDecimal>> measures, MeasurementUnit mUnit, List<Language> languages){
		StringBuilder quantitativeDescription = new StringBuilder(); // this StringBuilder is used to concatenate the different words of the description before saving it in the TextData
		TextData textData = TextData.NewInstance(); // TextData that will contain the description and the language corresponding
		// booleans indicating whether a kind of value is present or not and the float that will eventually hold the value

		String unit = "";
		mUnit = HibernateProxyHelper.deproxy(mUnit, MeasurementUnit.class);

		if ((mUnit!=null)&&(mUnit.getIdInVocabulary()!=null)){
			unit = mUnit.getIdInVocabulary();
		}else if ((mUnit!=null)&&(mUnit.getLabel()!=null)){
            unit = mUnit.getIdInVocabulary();
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

		// the booleans and floats are updated according to the presence or absence of values

		Boolean max, min, upperb, lowerb, average, sd, exact;

		String averagevalue = getValues(measures, StatisticalMeasure.AVERAGE());
		if (averagevalue!=null) {
            average=true;
        } else {
            average=false;
        }
		String sdvalue = getValues(measures, StatisticalMeasure.STANDARD_DEVIATION());
		if (sdvalue!=null) {
            sd=true;
        } else {
            sd=false;
        }
		String minvalue = getValues(measures, StatisticalMeasure.MIN());
		if (minvalue!=null) {
            min=true;
        } else {
            min=false;
        }
		String maxvalue = getValues(measures, StatisticalMeasure.MAX());
		if (maxvalue!=null) {
            max=true;
        } else {
            max=false;
        }
		String lowerbvalue = getValues(measures, StatisticalMeasure.TYPICAL_LOWER_BOUNDARY());
		if (lowerbvalue!=null) {
            lowerb=true;
        } else {
            lowerb=false;
        }
		String upperbvalue = getValues(measures, StatisticalMeasure.TYPICAL_UPPER_BOUNDARY());
		if (upperbvalue!=null) {
            upperb=true;
        } else {
            upperb=false;
        }

		String exactValue = getValues(measures, StatisticalMeasure.EXACT_VALUE());
		if (exactValue != null) {
		    exact = true;
		}else {
		    exact = false;
		}


		// depending on the different associations of values, a sentence is built
		if (max && min) {
			quantitativeDescription.append(space + from + space + minvalue + space + to + space + maxvalue + space + unit);
		}
		else if (min) {
			quantitativeDescription.append(space + from + space + minvalue + space + unit);
		}
		else if (max) {
			quantitativeDescription.append(space + up_To + space + maxvalue + space + unit);
		}
		if ((max||min)&&(lowerb||upperb)) {
			quantitativeDescription.append(separator); // merge with below ?
		}
		if ((lowerb||upperb)&&(min||max)) {
			quantitativeDescription.append(space + most_Frequently);
		}
		if (upperb && lowerb) {
			quantitativeDescription.append(space + from + space + lowerbvalue + space + to + space + upperbvalue + space + unit);
		}
		else if (lowerb) {
			quantitativeDescription.append(space + from + space + lowerbvalue + space + unit);
		}
		else if (upperb) {
			quantitativeDescription.append(space + up_To + space + upperbvalue + space + unit);
		}
		if (exact) {
		    quantitativeDescription.append(space + exactValue + space + unit);
		}
		if (((max||min)&&(average))||((lowerb||upperb)&&(average))) {
			quantitativeDescription.append(separator);
		}
		if (average) {
			quantitativeDescription.append(space + averagevalue + space + unit + space + on_Average);
			if (sd) {
				quantitativeDescription.append("("+ more_Or_Less + space + sdvalue + ")");
			}
		}


		textData.putText((languages == null || languages.isEmpty())? Language.DEFAULT() : languages.get(0), quantitativeDescription.toString()); // which language should we put here ?
//		textData.setFormat(TextFormat.NewInstance(null, "Text",null ));

		return textData;
	}

	/**
	 * Returns the value of a given type of measure as a String. If the value is an integer it is printed
	 * as an integer instead of a float.
	 * If no value of this type is present, returns null.
	 *
	 * @param measures the map with the values
	 * @param key the desired measure
	 * @return
	 */
	private String getValues(Map<StatisticalMeasure,List<BigDecimal>> measures, Object key) {
		if(measures.containsKey(key)) {
		    List<BigDecimal> floatValues;
		    String result = "";
			floatValues = measures.get(key);
			for (BigDecimal value: floatValues) {
                result = CdmUtils.concat("; ", result, value.toString());
            }
			return result;
		}
		return null;
	}
}