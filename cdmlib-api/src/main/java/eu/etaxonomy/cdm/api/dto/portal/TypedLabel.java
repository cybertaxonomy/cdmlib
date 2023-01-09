/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.dto.portal;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @date 07.01.2023
 */
public class TypedLabel {

    UUID uuid;   //or id?
//    to make links possible
    //TODO remove model dependency?
    Class<? extends CdmBase> cdmClass;
    //enum semantics   name_author, name_exauthor, separator ? => not needed if we pass all formatting rules from client to server
    boolean isNamePart;  //is italics??
    String label;

}
