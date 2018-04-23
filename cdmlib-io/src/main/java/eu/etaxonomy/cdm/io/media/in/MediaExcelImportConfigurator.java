/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.media.in;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;
import eu.etaxonomy.cdm.model.common.Language;

/**
 * @author a.mueller
 * @since 30.10.2017
 *
 */
public class MediaExcelImportConfigurator
        extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = -6403743396163163359L;

    private List<URI> baseUrls = new ArrayList<>();
    private Language descriptionLanguage;
    private Language titleLanguage;

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

    /**
     * @param uri
     * @param destination
     * @param transformer
     */
    private MediaExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaExcelImportState getNewState() {
        return new MediaExcelImportState(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                MediaExcelImport.class,
        };

    }

    /**
     * @return
     */
    public List<URI> getBaseUrls() {
        return baseUrls ;
    }


    public Language getDescriptionLanguage() {
        return this.descriptionLanguage;
    }
    public void setDescriptionLanguage(Language descriptionLanguage) {
        this.descriptionLanguage = descriptionLanguage;
    }

    public Language getTitleLanguage() {
        return titleLanguage;
    }
    public void setTitleLanguage(Language titleLanguage) {
        this.titleLanguage = titleLanguage;
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
