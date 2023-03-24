/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.in;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;
import eu.etaxonomy.cdm.io.fact.commonname.in.CommonNameExcelImportState;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public abstract class FactExcelImportBase<STATE extends FactExcelImportStateBase<CONFIG>, CONFIG extends FactExcelImportConfiguratorBase, ROW extends ExcelRowBase>
        extends ExcelImportBase<STATE, CONFIG, ExcelRowBase>{

    private static final long serialVersionUID = 2233954525898978414L;

    protected static final String COL_TAXON_UUID = "taxonUuid";


    @Override
    protected void analyzeRecord(Map<String, String> record, STATE state) {
        // do nothing
    }

    @Override
    protected void firstPass(STATE state) {
        String line = "row " + state.getCurrentLine() + ": ";
        String linePure = "row " + state.getCurrentLine();
//        System.out.println(linePure);
        CONFIG config = state.getConfig();

        //taxon
        Taxon taxon = getTaxonByCdmId(state, COL_TAXON_UUID,
                config.getColNameCache(), config.getColNameTitleCache(),
                config.getColTaxonTitleCache(),
                Taxon.class, linePure);

        if (taxon == null && state.getConfig().isAllowNameMatching()) {
            taxon = getTaxonByNameMatch(state, config.getColTaxonTitleCache(), config.getColNameTitleCache(),
                    config.getColNameCache(), config.getColAuthorship(), config.getTreeIndexFilter(), line);
        }

        doFirstPass(state, taxon, line, linePure);
    }

    protected abstract void doFirstPass(STATE state, Taxon taxon, String line, String linePure);

    @Override
    protected void secondPass(STATE state) {
        //override if necessary
    }


    protected DoubleResult<TaxonName, String> getOriginalName(CommonNameExcelImportState state, Taxon taxon,
            String nameTitleCacheColumnName, String nameCacheColumnName) {
        //TODO treeindex filter

        TaxonName originalName = null;
        String originalNameStr = null;
        Map<String, String> record = state.getOriginalRecord();
        String nameTitleCache = CdmUtils.nullSafeTrim(record.get(nameTitleCacheColumnName));
        String nameCache = record.get(nameCacheColumnName);
        if (isBlank(nameTitleCache) && isBlank(nameCache)) {
            return null;
        }

        //exact match with author
        if (isNotBlank(nameTitleCache)) {
            boolean withAuthor = true;
            originalName = findMatchingInSynonymy(taxon, originalName, nameTitleCache, withAuthor);
            if (originalName == null) {
                originalNameStr = nameTitleCache;
            }
        }
        //namecache match if no exact match with author exits
        if (originalName == null) {
            if (isNotBlank(nameCache)) {
                boolean withAuthor = false;
                originalName = findMatchingInSynonymy(taxon, originalName, nameCache, withAuthor);
                if (originalNameStr == null && originalName == null) {
                    originalNameStr = nameCache;
                }
            }
        }
        //no match in synonymy
        if (originalName == null) {
            List<TaxonName> matchCandiates = new ArrayList<>();
            if (isNotBlank(nameTitleCache)) {
                matchCandiates = getNameService().findNamesByTitleCache(nameTitleCache, MatchMode.EXACT, null);
                if (matchCandiates.isEmpty()) {
                    originalNameStr = nameTitleCache;
                }
            }
            if (matchCandiates.isEmpty()) {
                matchCandiates = getNameService().getNamesByNameCache(nameCache);
                if (matchCandiates.isEmpty() && isBlank(originalNameStr)) {
                    originalNameStr = nameCache;
                }
            }
            if (!matchCandiates.isEmpty()) {
                originalName = matchCandiates.get(0);
            }
        }
        return new DoubleResult<TaxonName, String>(originalName, originalNameStr);
    }

    /**
     * Tries to find a matching name (original name) in the synonymy of the given taxon.
     */
    protected TaxonName findMatchingInSynonymy(Taxon taxon, TaxonName originalName, String nameCache, boolean withAuthor) {
        if (nameMatches(taxon.getName(), nameCache, withAuthor)) {
            originalName = taxon.getName();
        }else {
            for (TaxonName synonym : taxon.getSynonymNames()) {
                if (nameMatches(synonym, nameCache, withAuthor)) {
                    originalName = synonym;
                    break;
                }
            }
        }
        return originalName;
    }

    private boolean nameMatches(TaxonName name, String nameStr, boolean withAuthor) {
        if (name == null) {
            return false;
        }else if (withAuthor){
            return nameStr.equals(name.getTitleCache());
        }else {
            return nameStr.equals(name.getNameCache());
        }
    }
}
