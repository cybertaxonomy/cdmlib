/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.fact.temporal.in;

import java.net.URI;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.description.Feature;

/**
 * Configurator for taxon based phenology import.
 *
 * @author a.mueller
 * @since 15.07.2020
 */
public class PhenologyExcelImportConfigurator
        extends TemporalDataExcelImportConfigurator<PhenologyExcelFormatAnalyzer>{

    private static final long serialVersionUID = 2413575026028295925L;

    private String floweringStartColumnLabel = PhenologyExcelImport.FLOWERING_START;
    private String floweringEndColumnLabel = PhenologyExcelImport.FLOWERING_END;
    private String fruitingStartColumnLabel = PhenologyExcelImport.FRUITING_START;
    private String fruitingEndColumnLabel = PhenologyExcelImport.FRUITING_END;

    public static PhenologyExcelImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new PhenologyExcelImportConfigurator(uri, destination, null);
    }

    private PhenologyExcelImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
        super(uri, destination, transformer);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PhenologyExcelImportState getNewState() {
        return new PhenologyExcelImportState(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                PhenologyExcelImport.class,
        };
    }

    @Override
    public PhenologyExcelFormatAnalyzer getAnalyzer() {
        return new PhenologyExcelFormatAnalyzer(this);
    }

    @Override
    //Used for flowering uuid
    public UUID getFeatureUuid() {
        return Feature.uuidFloweringPeriod;
    }

    public UUID getFruitingFeatureUuid() {
        return Feature.uuidFruitingPeriod;
    }

    @Override
    @Deprecated
    public String getColumnLabelStart() {
        return getFloweringStartColumnLabel();
    }
    @Override
    @Deprecated
    public void setColumnLabelStart(String columnLabelStart) {
        setFloweringStartColumnLabel(columnLabelStart);
    }

    @Override
    @Deprecated
    public String getColumnLabelEnd() {
        return getFloweringEndColumnLabel();
    }
    @Override
    @Deprecated
    public void setColumnLabelEnd(String columnLabelEnd) {
        setFloweringEndColumnLabel(columnLabelEnd);
    }

    public String getFloweringStartColumnLabel() {
        return floweringStartColumnLabel;
    }
    public void setFloweringStartColumnLabel(String floweringStartColumnLabel) {
        this.floweringStartColumnLabel = floweringStartColumnLabel;
    }

    public String getFloweringEndColumnLabel() {
        return floweringEndColumnLabel;
    }
    public void setFloweringEndColumnLabel(String floweringEndColumnLabel) {
        this.floweringEndColumnLabel = floweringEndColumnLabel;
    }

    public String getFruitingStartColumnLabel() {
        return fruitingStartColumnLabel;
    }
    public void setFruitingStartColumnLabel(String fruitingStartColumnLabel) {
        this.fruitingStartColumnLabel = fruitingStartColumnLabel;
    }

    public String getFruitingEndColumnLabel() {
        return fruitingEndColumnLabel;
    }
    public void setFruitingEndColumnLabel(String fruitingEndColumnLabel) {
        this.fruitingEndColumnLabel = fruitingEndColumnLabel;
    }
}
