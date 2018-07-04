/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.wfo.in;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.io.csv.in.CsvImportState;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 15.11.2017
 *
 */
public class WfoAccessImportState extends CsvImportState<WfoAccessImportConfigurator> {

    private TaxonNode parentNode;

    private Set<String> originalNameOfDoubtful = new HashSet<>();
    private Set<String> existingWfoIDs = new HashSet<>();

    private Map<String, UUID> taxonNodeUuids = new HashMap<>();


    /**
     * @param config
     */
    protected WfoAccessImportState(WfoAccessImportConfigurator config) {
        super(config);
    }

    public void putOriginalNameOfDoubful(String origNameId){
        originalNameOfDoubtful.add(origNameId);
    }
    public boolean isOriginalNameOfDoubful(String origNameId){
        return originalNameOfDoubtful.contains(origNameId);
    }
    public boolean removeOriginalNameOfDoubful(String origNameId){
        return originalNameOfDoubtful.remove(origNameId);
    }

    public void putExistingWfoId(String wfoId){
        existingWfoIDs.add(wfoId);
    }
    public boolean isExistingWfoID(String wfoId){
        return existingWfoIDs.contains(wfoId);
    }
    public boolean removeExistingWfoId(String wfoId){
        return existingWfoIDs.remove(wfoId);
    }

    public UUID getTaxonNodeUuid(String key){
        return taxonNodeUuids.get(key);
    }
    public UUID putTaxonNodeUuid(String key, UUID taxonNodeUuid){
        return taxonNodeUuids.put(key, taxonNodeUuid);
    }


    @Override
    public void resetSession(){
        this.parentNode = null;
        super.resetSession();
    }

    public TaxonNode getParentNode(){
        return this.parentNode;
    }
    public void setParentNode (TaxonNode parentNode){
        this.parentNode = parentNode;
    }





}
