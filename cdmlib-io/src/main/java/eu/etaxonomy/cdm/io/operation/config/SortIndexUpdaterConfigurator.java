/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.operation.config;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.ImportStateBase;
import eu.etaxonomy.cdm.io.operation.SortIndexUpdaterWrapper;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author k.luther
 * @date 08.07.2016
 *
 */
public class SortIndexUpdaterConfigurator  extends ImportConfiguratorBase<DefaultImportState<SetSecundumForSubtreeConfigurator>, Object> implements IImportConfigurator{


    private boolean doTaxonNode = true;
    private boolean doFeatureNode = true;
    private boolean doPolytomousKeyNode = true;

    private String subTreeIndex = null;

    private SortIndexUpdaterConfigurator(ICdmDataSource destination){
        super(null);
        this.setDestination(destination);
    }

    /**
     * @param destination
     * @return
     */
    public static SortIndexUpdaterConfigurator NewInstance(ICdmDataSource destination) {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator(destination);
        return result;
    }

    /**
     * @param destination
     * @return
     */
    public static SortIndexUpdaterConfigurator NewInstance(ICdmDataSource destination, boolean doTaxonNode, boolean doFeatureNode, boolean doPolytomousKeyNode) {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator(destination);
        result.doFeatureNode = doFeatureNode;
        result.doTaxonNode = doTaxonNode;
        result.doPolytomousKeyNode = doPolytomousKeyNode;
        return result;
    }


 // **************** GETTER / SETTER ************************************


    /**
     * @return the doTaxonNode
     */
    public boolean isDoTaxonNode() {
        return doTaxonNode;
    }
    /**
     * @param doTaxonNode the doTaxonNode to set
     */
    public void setDoTaxonNode(boolean doTaxonNode) {
        this.doTaxonNode = doTaxonNode;
    }
    /**
     * @return the doPolytomousKeyNode
     */
    public boolean isDoPolytomousKeyNode() {
        return doPolytomousKeyNode;
    }
    /**
     * @param doPolytomousKeyNode the doPolytomousKeyNode to set
     */
    public void setDoPolytomousKeyNode(boolean doPolytomousKeyNode) {
        this.doPolytomousKeyNode = doPolytomousKeyNode;
    }
    /**
     * @return the doFeatureNode
     */
    public boolean isDoFeatureNode() {
        return doFeatureNode;
    }
    /**
     * @param doFeatureNode the doFeatureNode to set
     */
    public void setDoFeatureNode(boolean doFeatureNode) {
        this.doFeatureNode = doFeatureNode;
    }

    @Override
    public <STATE extends ImportStateBase> STATE getNewState() {
        return (STATE) new DefaultImportState(this);
    }


    @Override
    protected void makeIoClassList() {
        ioClassList = new Class[]{
                     SortIndexUpdaterWrapper.class
        };
    }


    @Override
    public Reference getSourceReference() {
        return null;
    }


    @Override
    public boolean isValid() {
        //as no source needs to exist
        return true;
    }


}
