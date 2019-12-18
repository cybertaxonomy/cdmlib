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
 */
public class SortIndexUpdaterConfigurator implements Serializable{

    private static final long serialVersionUID = 6006504717504371170L;

    private boolean doTaxonNode = true;
    private boolean doTermNode = true;
    private boolean doPolytomousKeyNode = true;
    private IProgressMonitor monitor;

//********************* FACTORY **************************************/

    public static SortIndexUpdaterConfigurator NewInstance() {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator();
        return result;
    }

    public static SortIndexUpdaterConfigurator NewInstance(boolean doTaxonNode,
            boolean doTermNode, boolean doPolytomousKeyNode) {
        SortIndexUpdaterConfigurator result = new SortIndexUpdaterConfigurator();
        result.doTermNode = doTermNode;
        result.doTaxonNode = doTaxonNode;
        result.doPolytomousKeyNode = doPolytomousKeyNode;
        return result;
    }

//*********************** CONSTRUCTOR *****************************/

    private SortIndexUpdaterConfigurator(){}

 // **************** GETTER / SETTER ************************************

    public boolean isDoTaxonNode() {
        return doTaxonNode;
    }
    public void setDoTaxonNode(boolean doTaxonNode) {
        this.doTaxonNode = doTaxonNode;
    }

    public boolean isDoPolytomousKeyNode() {
        return doPolytomousKeyNode;
    }
    public void setDoPolytomousKeyNode(boolean doPolytomousKeyNode) {
        this.doPolytomousKeyNode = doPolytomousKeyNode;
    }

    public boolean isDoTermNode() {
        return doTermNode;
    }
    public void setDoTermNode(boolean doTermNode) {
        this.doTermNode = doTermNode;
    }

    public IProgressMonitor getMonitor() {
        return monitor;
    }
    public void setMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }
}