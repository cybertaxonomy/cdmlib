/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import java.io.Serializable;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;

/**
 * @author k.luther
 * @since 08.07.2016
 *
 */
public class SortIndexUpdaterConfigurator implements Serializable{


    private boolean doTaxonNode = true;
    private boolean doFeatureNode = true;
    private boolean doPolytomousKeyNode = true;
    private IProgressMonitor monitor;

    private SortIndexUpdaterConfigurator(){

    }

    /**
     * @param destination
     * @return
     */
    public static SortIndexUpdaterConfigurator NewInstance() {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator();
        return result;
    }

    /**
     * @param destination
     * @return
     */
    public static SortIndexUpdaterConfigurator NewInstance(boolean doTaxonNode, boolean doFeatureNode, boolean doPolytomousKeyNode) {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator();
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

    public IProgressMonitor getMonitor() {
        return monitor;
    }

    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }




}
