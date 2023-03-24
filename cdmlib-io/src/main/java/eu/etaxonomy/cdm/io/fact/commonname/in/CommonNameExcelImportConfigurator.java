/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.commonname.in;

import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @since 24.01.2023
 */
public class CommonNameExcelImportConfigurator
        extends FactExcelImportConfiguratorBase<CommonNameExcelFormatAnalyzer>{

    private static final long serialVersionUID = -6403743396163163359L;

    private UUID defaultLanguageUuid;
    private UUID defaultAreaUuid;

    public static CommonNameExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new CommonNameExcelImportConfigurator(uri, destination, null);
    }


    /**
     * How to fill the media title if not explicit value is given.
     */
    public enum MediaTitleEnum{
        NONE,
        NAME_CACHE,  //use name cache
        NAME_TITLE_CACHE,  //use name title cache
        TAXON_TITLE_CACHE,   //use taxon title
        FILE_NAME    //use file name
    }

    private CommonNameExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CommonNameExcelImportState getNewState() {
        return new CommonNameExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                CommonNameExcelImport.class,
        };
    }

    @Override
    public CommonNameExcelFormatAnalyzer getAnalyzer() {
        return new CommonNameExcelFormatAnalyzer(this);
    }

    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setCommonNameColumnLabel(String label) {
        putLabelReplacement(CommonNameExcelImport.COL_COMMON_NAME, label);
    }

    public void setLanguageLabelColumnLabel(String label) {
        putLabelReplacement(CommonNameExcelImport.COL_LANGUAGE_LABEL, label);
    }
    public void setLanguageUuidColumnLabel(String label) {
        putLabelReplacement(CommonNameExcelImport.COL_LANGUAGE_UUID, label);
    }
    public void setAreaLabelColumnLabel(String label) {
        putLabelReplacement(CommonNameExcelImport.COL_AREA_LABEL, label);
    }
    public void setAreaUuidColumnLabel(String label) {
        putLabelReplacement(CommonNameExcelImport.COL_AREA_UUID, label);
    }

    public void setRowToNeglect(int row){

    }

    public UUID getDefaultLanguageUuid() {
        return defaultLanguageUuid;
    }

    public void setDefaultLanguageUuid(UUID defaultLanguageUuid) {
        this.defaultLanguageUuid = defaultLanguageUuid;
    }

    public UUID getDefaultAreaUuid() {
        return defaultAreaUuid;
    }

    public void setDefaultAreaUuid(UUID defaultAreaUuid) {
        this.defaultAreaUuid = defaultAreaUuid;
    }
}
