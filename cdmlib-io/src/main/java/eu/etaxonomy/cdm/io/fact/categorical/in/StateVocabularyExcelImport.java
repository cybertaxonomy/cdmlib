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

import eu.etaxonomy.cdm.io.excel.common.ExcelImportBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.State;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.term.Representation;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * Feature vocabulary import for categorical data.
 *
 * @author a.mueller
 * @since 06.07.2020
 */
@Component
public class StateVocabularyExcelImport
        extends ExcelImportBase<CategoricalDataExcelImportState, CategoricalDataExcelImportConfigurator, ExcelRowBase>{

    private static final long serialVersionUID = 8264900898340386516L;

    private static final String COL_TERM_UUID = "termUuid";
    private static final String COL_LABEL_EN = "label_en";
    private static final String COL_LABEL_RU = "label_ru";

    @Override
    protected void firstPass(CategoricalDataExcelImportState importState) {
        String linePure = String.valueOf(importState.getCurrentLine());

        UUID uuidVoc = importState.getConfig().getStateVocabularyUuid();
        String vocLabel = importState.getConfig().getStateVocabularyLabel();

        TermVocabulary<State> voc = getVocabulary(importState, TermType.State, uuidVoc, vocLabel, vocLabel, null, null, true, State.NewInstance());

        String uuidTermStr = getValue(importState, COL_TERM_UUID);
        UUID uuidTerm = uuidTermStr == null ? UUID.randomUUID(): UUID.fromString(uuidTermStr);

        //this is all preliminary for Uzbekistan
        String label_en = getValue(importState, COL_LABEL_EN);
        String label_ru = getValue(importState, COL_LABEL_RU);

        State state = State.NewInstance(label_en, label_en, null);
        state.setUuid(uuidTerm);
        state.addRepresentation(Representation.NewInstance(label_ru, label_ru, null, Language.RUSSIAN()));
        voc.addTerm(state);

        //source
        String id = null;
        String idNamespace = null;
        Reference reference = getSourceReference(importState);

        voc.addImportSource(id, idNamespace, reference, null);
        state.addImportSource(id, idNamespace, reference, linePure);

    }

    @Override
    protected void analyzeRecord(Map<String, String> record, CategoricalDataExcelImportState state) {
        // TODO
    }

    @Override
    protected void secondPass(CategoricalDataExcelImportState state) {
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