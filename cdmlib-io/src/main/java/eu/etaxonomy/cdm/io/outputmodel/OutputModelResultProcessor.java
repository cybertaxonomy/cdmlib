/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.model.common.ICdmBase;


/**
 * @author k.luther
 * @date 16.03.2017
 *
 */
public class OutputModelResultProcessor {

    private static final String HEADER = "HEADER_207dd23a-f877-4c27-b93a-8dbea3234281";

    private Map<OutputModelTable, Map<String,String[]>> result = new HashMap<>();
    private OutputModelExportState state;


    /**
     * @param state
     */
    public OutputModelResultProcessor(OutputModelExportState state) {
        super();
        this.state = state;
    }


    /**
     * @param taxon
     * @param csvLine
     */
    public void put(OutputModelTable table, String id, String[] csvLine) {
        Map<String,String[]> resultMap = result.get(table);
        if (resultMap == null ){
            resultMap = new HashMap<>();
            if (state.getConfig().isHasHeaderLines()){
                resultMap.put(HEADER, table.getColumnNames());
            }
        }
        String[] record = resultMap.get(id);
        if (record == null){
            record = csvLine;
            resultMap.put(id, record);
        }
    }

    public boolean hasRecord(OutputModelTable table, String id){
        Map<String, String[]> resultMap = result.get(table);
        if (resultMap == null){
            return false;
        }else{
            return resultMap.get(id) != null;
        }
    }


    /**
     * @param table
     * @param taxon
     * @param csvLine
     */
    public void put(OutputModelTable table, ICdmBase cdmBase, String[] csvLine) {
       this.put(table, String.valueOf(cdmBase.getId()), csvLine);
    }
}
