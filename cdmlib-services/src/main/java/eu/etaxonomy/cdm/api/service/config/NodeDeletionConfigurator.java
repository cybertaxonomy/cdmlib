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
 * @date 03.11.2015
 *
 */
public class NodeDeletionConfigurator extends DeleteConfiguratorBase {

        public enum ChildHandling{
            DELETE,
            MOVE_TO_PARENT
        }


        private ChildHandling childHandling = ChildHandling.DELETE;


        public void setChildHandling(ChildHandling childHandling) {
            this.childHandling = childHandling;
        }


        public ChildHandling getChildHandling() {
            return childHandling;
        }

        public boolean deleteElement = true;


        public boolean isDeleteElement() {
            return deleteElement;
        }


        public void setDeleteElement(boolean deleteElement) {
            this.deleteElement = deleteElement;
        }


}
