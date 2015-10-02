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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author pplitzner
 * @date Mar 26, 2015
 *
 */
public class PreservedSpecimenDTO extends DerivateDTO{

    private static final long serialVersionUID = -8138686023034532991L;

    private String accessionNumber;
    private String uuid;
    private List<String> associatedTaxa;
    private Map<String, List<String>> types;

    public String getAccessionNumber() {
        return accessionNumber;
    }
    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the types
     */
    public Map<String, List<String>> getTypes() {
        return types;
    }
    public void addTypes(String typeStatus, List<String> typedTaxa){
        if(types==null){
            types = new HashMap<String, List<String>>();
        }
        types.put(typeStatus, typedTaxa);
    }

    public List<String> getAssociatedTaxa() {
        return associatedTaxa;
    }
    public void addAssociatedTaxon(String taxonName){
        if(associatedTaxa==null){
            associatedTaxa = new ArrayList<String>();
        }
        associatedTaxa.add(taxonName);
    }

}
