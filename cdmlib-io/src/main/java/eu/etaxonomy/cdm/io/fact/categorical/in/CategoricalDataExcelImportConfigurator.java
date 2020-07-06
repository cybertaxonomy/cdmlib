/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.categorical.in;

import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Configurator for taxon based categorical data import.
 *
 * @author a.mueller
 * @since 06.07.2020
 */
public class CategoricalDataExcelImportConfigurator
        extends FactExcelImportConfiguratorBase<CategoricalDataExcelFormatAnalyzer>{

    private static final long serialVersionUID = 5969649644223617705L;

    private UUID stateVocabularyUuid;
    private String stateVocabularyLabel;
    private UUID featureUuid;

    public static CategoricalDataExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new CategoricalDataExcelImportConfigurator(uri, destination, null);
    }

    private CategoricalDataExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CategoricalDataExcelImportState getNewState() {
        return new CategoricalDataExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                StateVocabularyExcelImport.class,
                CategoricalDataExcelImport.class,
        };
    }

    @Override
    public CategoricalDataExcelFormatAnalyzer getAnalyzer() {
        return new CategoricalDataExcelFormatAnalyzer(this);
    }

    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setRowToNeglect(int row){

    }

    public UUID getStateVocabularyUuid() {
        return this.stateVocabularyUuid;
    }
    public void setStateVocabularyUuid(UUID stateVocabularyUuid) {
        this.stateVocabularyUuid = stateVocabularyUuid;
    }

    public String getStateVocabularyLabel() {
        return stateVocabularyLabel;
    }
    public void setStateVocabularyLabel(String stateVocabularyLabel) {
        this.stateVocabularyLabel = stateVocabularyLabel;
    }

    public UUID getFeatureUuid() {
        return featureUuid;
    }
    public void setFeatureUuid(UUID featureUuid) {
        this.featureUuid = featureUuid;
    }

}
