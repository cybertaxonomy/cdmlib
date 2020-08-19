/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.textdata.in;

import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * Configurator for taxon related text based factual data.
 *
 * @author a.mueller
 * @since 06.07.2020
 */
public class TextDataExcelImportConfigurator
        extends FactExcelImportConfiguratorBase<TextDataExcelFormatAnalyzer>{

    private static final long serialVersionUID = 6327798120129619332L;

    private UUID textLanguageUuid;

    public static TextDataExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new TextDataExcelImportConfigurator(uri, destination, null);
    }

    private TextDataExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TextDataExcelImportState getNewState() {
        return new TextDataExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                TextDataExcelImport.class,
        };
    }

    @Override
    public TextDataExcelFormatAnalyzer getAnalyzer() {
        return new TextDataExcelFormatAnalyzer(this);
    }


    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setTextColumnLabel(String label) {
        putLabelReplacement(TextDataExcelImport.COL_TEXT, label);
    }

    public void setRowToNeglect(int row){

    }

    public UUID getTextLanguageUuid() {
        return textLanguageUuid;
    }
    public void setTextLanguageUuid(UUID textLanguageUuid) {
        this.textLanguageUuid = textLanguageUuid;
    }

}
