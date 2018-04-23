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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @since 06.04.2017
 *
 */
public class ExcelDistributionUpdateConfigurator extends ExcelImportConfiguratorBase{

    private static final long serialVersionUID = -2464249013501268222L;

    private UUID areaVocabularyUuid;

    private boolean createNewDistribution = true;

    public static ExcelDistributionUpdateConfigurator NewInstance(URI uri, ICdmDataSource destination, UUID areaVocabularyUuid){
        ExcelDistributionUpdateConfigurator result = new ExcelDistributionUpdateConfigurator(uri, destination, areaVocabularyUuid);
        return result;
    }

    /**
     * @param uri
     * @param destination
     * @param transformer
     */
    protected ExcelDistributionUpdateConfigurator(URI uri, ICdmDataSource destination, UUID areaVocabularyUuid) {
        super(uri, destination, null);
        this.areaVocabularyUuid = areaVocabularyUuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
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


    @Override
    public boolean isValid(){
        if (this.areaVocabularyUuid != null && (this.getStream() != null || this.getSource() != null)){
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Reference getSourceReference() {
        if (this.sourceReference == null){
            sourceReference = ReferenceFactory.newGeneric();
            if (this.getSource() == null){
                sourceReference.setTitleCache("Excel Distribution Update " + getDateString(), true);
            }else{
                sourceReference.setTitleCache(getSource().toString(), true);
            }
        }
        return sourceReference;
    }

    protected boolean needsNomenclaturalCode() {
        return false;
    }

// ********************** GETTER / SETTER **************************/

    public UUID getAreaVocabularyUuid() {
        return this.areaVocabularyUuid;
    }

    public void setAreaVocabularyUuid(UUID areaVocabularyUuid) {
        this.areaVocabularyUuid = areaVocabularyUuid;
    }

    public boolean isCreateNewDistribution() {
        return createNewDistribution;
    }

    public void setCreateNewDistribution(boolean createNewDistribution) {
        this.createNewDistribution = createNewDistribution;
    }

}
