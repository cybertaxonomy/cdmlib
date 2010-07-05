package eu.etaxonomy.cdm.api.service;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class AbstractQuantitativeDescriptionBuilder extends DescriptionBuilder<QuantitativeData>{
	
	public TextData build(QuantitativeData data) {
		   Map<StatisticalMeasure,Float> measures = new HashMap<StatisticalMeasure,Float>();
		   for (StatisticalMeasurementValue smv : data.getStatisticalValues()){
		     measures.put(smv.getType(),smv.getValue());
		   }
		   TextData textdata = doBuild(measures,data.getUnit());
		   textdata.setFeature(data.getFeature());
		   return textdata;
		 }

	protected abstract TextData doBuild(Map<StatisticalMeasure,Float> measures, MeasurementUnit unit);

}
