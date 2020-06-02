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
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 *
 * @author a.mueller
 * @since 28.05.2020
 */
@Component
public class AltitudeExcelImport
        extends FactExcelImportBase<AltitudeExcelImportState, AltitudeExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 8264900898340386516L;

    private static final String COL_ALTITUDE_MIN = "Altitude min";
    private static final String COL_ALTITUDE_MAX = "Altitude max";


    @Override
    protected void doFirstPass(AltitudeExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
            return;
        }

        UUID uuid = Feature.uuidAltitude;
        String featureLabel = "Altitude";
        Feature feature = getFeature(state, uuid, featureLabel, featureLabel, null, null);

        String minStr = getValue(state, COL_ALTITUDE_MIN);
        String maxStr = getValue(state, COL_ALTITUDE_MAX);

        BigDecimal min = new BigDecimal(minStr);
        BigDecimal max = new BigDecimal(maxStr);

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(state);


        QuantitativeData qd = QuantitativeData.NewMinMaxInstance(feature, min, max);

        TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
        taxonDescription.addElement(qd);
        qd.addImportSource(id, idNamespace, reference, linePure);

    }

    @Override
    protected boolean isIgnore(AltitudeExcelImportState state) {
        return false;
    }

}