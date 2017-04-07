/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.distribution.excelupdate;

import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.excel.common.ExcelImportConfiguratorBase;

/**
 * @author a.mueller
 * @date 06.04.2017
 *
 */
public class ExcelDistributionUpdateConfigurator extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = -2464249013501268222L;

    private UUID areaVocabularyUuid;

    /**
     * @param uri
     * @param destination
     * @param transformer
     */
    protected ExcelDistributionUpdateConfigurator(URI uri, ICdmDataSource destination, UUID areaVocabularyUuid) {
        super(uri, destination, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        // TODO Auto-generated method stub
        return (STATE)new ExcelDistributionUpdateState(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                ExcelDistributionUpdate.class
        };
    }

    public UUID getAreaVocabularyUuid() {
        return this.areaVocabularyUuid;
    }

    public void setAreaVocabularyUuid(UUID areaVocabularyUuid) {
        this.areaVocabularyUuid = areaVocabularyUuid;
    }


}
