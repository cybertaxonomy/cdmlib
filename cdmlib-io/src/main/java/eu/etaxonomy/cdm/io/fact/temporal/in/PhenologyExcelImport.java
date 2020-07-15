/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon based phenology data.
 *
 * @author a.mueller
 * @since 15.07.2020
 */
@Component
public class PhenologyExcelImport
        extends TemporalDataExcelImport<PhenologyExcelImportState, PhenologyExcelImportConfigurator>{

    private static final long serialVersionUID = 1050528888222978429L;

    public static final String FLOWERING_START = "Flowering start";
    public static final String FLOWERING_END = "Flowering end";
    public static final String FRUITING_START = "Fruiting start";
    public static final String FRUITING_END = "Fruiting end";

    @Override
    protected void doFirstPass(PhenologyExcelImportState state, Taxon taxon,
            String line, String linePure){

        super.doFirstPass(state, taxon, line, linePure);

        if (taxon == null){
//            return;
            taxon = Taxon.NewInstance(null, null);
        }

        Map<String, String> record = state.getOriginalRecord();

        UUID uuidFeatureFruiting = state.getConfig().getFruitingFeatureUuid();
        Feature featureFruiting = (Feature)getTermService().find(uuidFeatureFruiting);
        String colFruitingStart = state.getConfig().getFruitingStartColumnLabel();
        String colFruitingEnd = state.getConfig().getFruitingEndColumnLabel();

        handleFeature(state, taxon, line, linePure, record, featureFruiting, colFruitingStart, colFruitingEnd);
    }
}