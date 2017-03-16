/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author k.luther
 * @date 16.03.2017
 *
 */
public class OutputModelResultProcessor {

    Map<OutputModelTable, List<String[]>> result = new HashMap<>();
    OutputModelExportState state;


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
    public void put(OutputModelTable table, String[] csvLine) {
        List<String[]> list = result.get(table);
        if (list == null ){
            list = new ArrayList<>();
            if (state.getConfig().isHasHeaderLines()){
                list.add(table.getColumnNames());
            }
        }
        list.add(csvLine);
    }

}
