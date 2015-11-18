// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

/**
 * @author k.luther
 * @date 05.11.2015
 *
 */
public class FeatureNodeDeletionConfigurator extends NodeDeletionConfigurator {

    public FeatureNodeDeletionConfigurator(){
        deleteElement = false;
    }
}
