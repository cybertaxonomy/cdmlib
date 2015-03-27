// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.util.ArrayList;
import java.util.List;


/**
 * @author pplitzner
 * @date Mar 26, 2015
 *
 */
public class PreservedSpecimenDTO extends DerivateDTO{

    private String accessionNumber;
    private List<String> types;

    /**
     * @return the accessionNumber
     */
    public String getAccessionNumber() {
        return accessionNumber;
    }
    /**
     * @param accessionNumber the accessionNumber to set
     */
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }
    /**
     * @return the types
     */
    public List<String> getTypes() {
        return types;
    }
    public void addType(String typeStatus){
        if(types==null){
            types = new ArrayList<String>();
        }
        types.add(typeStatus);
    }

}
