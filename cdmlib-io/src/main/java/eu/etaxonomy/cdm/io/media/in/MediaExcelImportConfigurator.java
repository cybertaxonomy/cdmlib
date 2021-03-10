/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.media.in;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;

/**
 * @author a.mueller
 * @since 30.10.2017
 */
public class MediaExcelImportConfigurator
        extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = -6403743396163163359L;

    private List<URI> baseUrls = new ArrayList<>();
    private UUID descriptionLanguageUuid;
    private UUID titleLanguageUuid;

    private boolean readMediaData = true;
    private MediaTitleEnum mediaTitle = MediaTitleEnum.NAME_TITLE_CACHE;

    public static MediaExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new MediaExcelImportConfigurator(uri, destination, null);
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

    private MediaExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MediaExcelImportState getNewState() {
        return new MediaExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                MediaExcelImport.class,
        };
    }

    public List<URI> getBaseUrls() {
        return baseUrls ;
    }

    public UUID getDescriptionLanguageUuid() {
        return this.descriptionLanguageUuid;
    }
    public void setDescriptionLanguage(UUID descriptionLanguageUuid) {
        this.descriptionLanguageUuid = descriptionLanguageUuid;
    }

    public UUID getTitleLanguageUuid() {
        return titleLanguageUuid;
    }
    public void setTitleLanguageUuid(UUID titleLanguageUuid) {
        this.titleLanguageUuid = titleLanguageUuid;
    }

    public boolean isReadMediaData() {
        return readMediaData;
    }
    public void setReadMediaData(boolean readMediaData) {
        this.readMediaData = readMediaData;
    }

    public MediaTitleEnum getMediaTitle() {
        return mediaTitle;
    }
    public void setMediaTitle(MediaTitleEnum mediaTitle) {
        this.mediaTitle = mediaTitle;
    }
}