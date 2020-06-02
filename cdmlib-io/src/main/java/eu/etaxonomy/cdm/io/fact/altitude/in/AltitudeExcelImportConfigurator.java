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
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.fact.in.FactExcelImportConfiguratorBase;

/**
 * @author a.mueller
 * @since 28.05.2020
 */
public class AltitudeExcelImportConfigurator
        extends FactExcelImportConfiguratorBase{

    private static final long serialVersionUID = -6403743396163163359L;

    private UUID featureUuid;

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

    public UUID getFeatureUuid() {
        return featureUuid;
    }

    public void setFeatureUuid(UUID featureUuid) {
        this.featureUuid = featureUuid;
    }

}
