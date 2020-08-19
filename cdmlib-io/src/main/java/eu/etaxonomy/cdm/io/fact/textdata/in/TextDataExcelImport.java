/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.textdata.in;

import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * Import for taxon retlated text based factual data.
 *
 * @author a.mueller
 * @since 06.07
 */
@Component
public class TextDataExcelImport
        extends FactExcelImportBase<TextDataExcelImportState, TextDataExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = -3679579367658612019L;

    protected static final String COL_TEXT = "Text";

    @Override
    protected void doFirstPass(TextDataExcelImportState state, Taxon taxon,
            String line, String linePure){

        if (taxon == null){
//            return;
            taxon = Taxon.NewInstance(null, null);
        }
        UUID featureUuid = state.getConfig().getFeatureUuid();
        Feature feature = getFeature(state, featureUuid);
        if (feature == null){
            String featureLabel = state.getConfig().getFeatureLabel();
            if (isBlank(featureLabel)){
                String message = "No feature and no label for a new to create feature given. Can't import data.";
                state.getResult().addError(message, null, "doFirstPass", line);
            }
            feature = getFeature(state, featureUuid, featureLabel, featureLabel, null, null);
        }

        String textStr = getValue(state, COL_TEXT);

        Language language;
        UUID languageUuid = state.getConfig().getTextLanguageUuid();
        if (languageUuid != null){
            language = getLanguage(state, languageUuid);
        }else{
            language = Language.DEFAULT();
        }

        if (language == null){
            state.addError("Language for text can not be found. Import not possible.");
            return;
        }

        if (isBlank(textStr)){
            state.getResult().addWarning("Text was empty. No data was imported.", "doFirstPass", linePure);
        }else{
            TextData textData = TextData.NewInstance(feature, textStr, language, null);

            //source
            String id = null;
            String idNamespace = null;
            Reference reference = getSourceReference(state);

            //description
            TaxonDescription taxonDescription = this.getTaxonDescription(taxon, reference, !IMAGE_GALLERY, true);
            taxonDescription.addElement(textData);
            textData.addImportSource(id, idNamespace, reference, linePure);
        }
    }

    @Override
    protected boolean requiresNomenclaturalCode() {
        return false;
    }

    @Override
    protected boolean isIgnore(TextDataExcelImportState state) {
        return false;
    }

}