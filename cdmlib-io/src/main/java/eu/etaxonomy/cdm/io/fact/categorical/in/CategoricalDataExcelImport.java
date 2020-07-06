/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.categorical.in;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Import for taxon based categorical data.
 *
 * @author a.mueller
 * @since 06.07.2020
 */
@Component
public class CategoricalDataExcelImport
        extends FactExcelImportBase<CategoricalDataExcelImportState, CategoricalDataExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 8264900898340386516L;

    @Override
    protected String getWorksheetName(CategoricalDataExcelImportConfigurator config) {
        return "Data";
    }

    @Override
    protected void doFirstPass(CategoricalDataExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
//            return;
            taxon = Taxon.NewInstance(null, null);
        }

        Map<String, String> record = state.getOriginalRecord();

        UUID uuidFeature = state.getConfig().getFeatureUuid();
        Feature feature = (Feature)getTermService().find(uuidFeature);

        CategoricalData cd = CategoricalData.NewInstance(feature);
        UUID uuidVoc = state.getConfig().getStateVocabularyUuid();
        @SuppressWarnings("unchecked")
        TermVocabulary<State> voc = getVocabularyService().find(uuidVoc);
        for (State cdState : voc.getTerms()){
            Language language = Language.DEFAULT();
            String label = cdState.getLabel(language);
            String valueStr = record.get(label);
            if(isNotBlank(valueStr)){
                if (valueStr.trim().matches("(1|x|X)")){
                    cd.addStateData(cdState);
                }else{
                    state.getResult().addWarning("Value not recognized", "doFirstPass", line + ":" + label);
                }
            }
        }

        //source
        String id = null;
        String idNamespace = getWorksheetName(state.getConfig());
        Reference reference = getSourceReference(state);

        //description
        if (!cd.getStateData().isEmpty()){
            TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
            taxonDescription.addElement(cd);
            cd.addImportSource(id, idNamespace, reference, linePure);
        }
    }

    @Override
    protected boolean requiresNomenclaturalCode() {
        return false;
    }

    @Override
    protected boolean isIgnore(CategoricalDataExcelImportState state) {
        return false;
    }
}