// $Id$
/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.util.HashMap;
import java.util.List;

/**
 * @author k.luther
 * @date 18.07.2016
 *
 */
public class SpecimenDataHolder {

    private String nomenclatureCode;
    private List<HashMap<String, String>> atomisedIdentificationList;



    /**
     * @return the nomenclatureCode
     */
    public String getNomenclatureCode() {
        return nomenclatureCode;
    }



    /**
     * @param nomenclatureCode the nomenclatureCode to set
     */
    public void setNomenclatureCode(String nomenclatureCode) {
        this.nomenclatureCode = nomenclatureCode;
    }



    /**
     * @return the atomisedIdentificationList
     */
    public List<HashMap<String, String>> getAtomisedIdentificationList() {
        return atomisedIdentificationList;
    }



    /**
     * @param atomisedIdentificationList the atomisedIdentificationList to set
     */
    public void setAtomisedIdentificationList(List<HashMap<String, String>> atomisedIdentificationList) {
        this.atomisedIdentificationList = atomisedIdentificationList;
    }
}
