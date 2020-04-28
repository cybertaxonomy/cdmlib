package eu.etaxonomy.cdm.api.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasure;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TextData;

public abstract class AbstractQuantitativeDescriptionBuilder extends DescriptionBuilder<QuantitativeData>{

	@Override
    public TextData build(QuantitativeData data, List<Language> languages) {
		   Map<StatisticalMeasure,BigDecimal> measures = new HashMap<>();
		   for (StatisticalMeasurementValue smv : data.getStatisticalValues()){
		     measures.put(smv.getType(),smv.getValue());
		   }
		   return doBuild(measures,data.getUnit(), languages);
		 }

	protected abstract TextData doBuild(Map<StatisticalMeasure,BigDecimal> measures,
	        MeasurementUnit unit, List<Language> languages);

}
