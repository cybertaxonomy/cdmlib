/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.MeasurementUnit;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon based altitude data as quantitative data.
 *
 * @author a.mueller
 * @since 28.05.2020
 */
@Component
public class AltitudeExcelImport
        extends FactExcelImportBase<AltitudeExcelImportState, AltitudeExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 8264900898340386516L;

    protected static final String COL_ALTITUDE_MIN = "Altitude min";
    protected static final String COL_ALTITUDE_MAX = "Altitude max";

    @Override
    protected void doFirstPass(AltitudeExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
//            return;
            taxon = Taxon.NewInstance(null, null);
        }

        UUID featureUuid = Feature.uuidAltitude;
        String featureLabel = "Altitude";
        Feature feature = getFeature(state, featureUuid);
        if (feature == null){
            feature = getFeature(state, featureUuid, featureLabel, featureLabel, null, null);
        }

        UUID measurementUnitUuid = MeasurementUnit.uuidMeter;
        MeasurementUnit unit = getMeasurementUnit(state, measurementUnitUuid, null, null, null, null);
        //TODO log error if unit is not persistent yet or handle like for feature first part

        String minStr = getValue(state, COL_ALTITUDE_MIN);
        String maxStr = getValue(state, COL_ALTITUDE_MAX);

        BigDecimal min = getBigDecimal(state, minStr);
        BigDecimal max = getBigDecimal(state, maxStr);
        if(min == null && max == null){
            state.addError("No minimum and no maximum exists. Record not imported.");
            return;
        }
        QuantitativeData qd = QuantitativeData.NewMinMaxInstance(feature, unit, min, max);

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(state);

        //description
        TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
        taxonDescription.addElement(qd);
        qd.addImportSource(id, idNamespace, reference, linePure);

    }

    private BigDecimal getBigDecimal(AltitudeExcelImportState state, String numberStr) {
        if(isBlank(numberStr)){
            return null;
        }
        try {
            BigDecimal result = new BigDecimal(numberStr);
            return result;
        } catch (Exception e) {
            state.getResult().addException(e, "Number text not recognized as number (BigDecimal)", getClass().getName()+"getBigDecimal()", "row " + state.getCurrentLine());
            return null;
        }
    }

    @Override
    protected boolean requiresNomenclaturalCode() {
        return false;
    }

    @Override
    protected boolean isIgnore(AltitudeExcelImportState state) {
        return false;
    }

}