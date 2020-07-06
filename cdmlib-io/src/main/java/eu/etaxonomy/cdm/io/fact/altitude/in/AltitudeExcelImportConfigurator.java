/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.altitude.in;

import java.net.URI;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public class AltitudeExcelImportConfigurator
        extends FactExcelImportConfiguratorBase<AltitudeExcelFormatAnalyzer>{

    private static final long serialVersionUID = -6403743396163163359L;

    public static AltitudeExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new AltitudeExcelImportConfigurator(uri, destination, null);
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

    private AltitudeExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AltitudeExcelImportState getNewState() {
        return new AltitudeExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                AltitudeExcelImport.class,
        };
    }

    @Override
    public AltitudeExcelFormatAnalyzer getAnalyzer() {
        return new AltitudeExcelFormatAnalyzer(this);
    }

    @Override
    public NomenclaturalCode getNomenclaturalCode() {
        NomenclaturalCode result = super.getNomenclaturalCode();
        if (result == null){
            result = NomenclaturalCode.ICNAFP;
        }
        return result;
    }

    public void setMinColumnLabel(String label) {
        putLabelReplacement(AltitudeExcelImport.COL_ALTITUDE_MIN, label);
    }

    public void setMaxColumnLabel(String label) {
        putLabelReplacement(AltitudeExcelImport.COL_ALTITUDE_MAX, label);
    }

    public void setRowToNeglect(int row){

    }
}
