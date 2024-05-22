/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.nameMatching;

import java.util.List;

/**
 * @author andreabee90
 * @since 22.05.2024
 */
public class NameMatchingOutputListObject extends NameMatchingOutputObject{

    private List <NameMatchingOutputObject> outputObjectList;


    public List <NameMatchingOutputObject>  getOutputObject() {
        return outputObjectList;
    }

    public void setOutputObject(List <NameMatchingOutputObject>  outputObject) {
        this.outputObjectList = outputObject;
    }


}
