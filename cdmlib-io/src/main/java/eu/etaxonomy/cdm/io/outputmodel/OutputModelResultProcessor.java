/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.outputmodel;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.io.common.ExportResultType;
import eu.etaxonomy.cdm.model.common.ICdmBase;


/**
 * @author k.luther
 * @date 16.03.2017
 *
 */
public class OutputModelResultProcessor {

    private static final String HEADER = "HEADER_207dd23a-f877-4c27-b93a-8dbea3234281";

    private Map<OutputModelTable, Map<String,String[]>> result = new HashMap<>();
    //private OutputModelExportState state;


    /**
     * @param state
     */
    public OutputModelResultProcessor(OutputModelExportState state) {
        super();
        //this.state = state;
    }


    /**
     * @param taxon
     * @param csvLine
     */
    public void put(OutputModelTable table, String id, String[] csvLine, OutputModelExportState state) {
        Map<String,String[]> resultMap = result.get(table);
        if (resultMap == null ){
            resultMap = new HashMap<>();
            if (state.getConfig().isHasHeaderLines()){
                resultMap.put(HEADER, table.getColumnNames());
            }
            result.put(table, resultMap);
        }
        String[] record = resultMap.get(id);
        if (record == null){
            record = csvLine;

            String[] oldRecord = resultMap.put(id, record);

            if (oldRecord != null){
                System.out.println("This should not happen");
            }
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
    public void put(OutputModelTable table, ICdmBase cdmBase, String[] csvLine, OutputModelExportState state) {
       this.put(table, String.valueOf(cdmBase.getId()), csvLine, state);
    }


    /**
     * @return
     */
    public void createFinalResult(OutputModelExportState state) {
        String strToPrint ="";
        ExportResult finalResult = ExportResult.NewInstance(ExportResultType.MAP_BYTE_ARRAY);

        if (!result.isEmpty() ){
            //Replace quotes by double quotes
            String value ;
            for (OutputModelTable table: result.keySet()){
                //schreibe jede Tabelle in einen Stream...
                Map<String, String[]> tableData = result.get(table);
                OutputModelConfigurator config = state.getConfig();
                ByteArrayOutputStream exportStream = new ByteArrayOutputStream();
                PrintWriter writer = null;
                writer = new PrintWriter(exportStream);
                String[] csvHeaderLine = tableData.get(HEADER);
                String lineString = createCsvLine(config, csvHeaderLine);
                writer.println(lineString);

                for (String key: tableData.keySet()){
                    if (!key.equals(HEADER)){
                        String[] csvLine = tableData.get(key);

                        lineString = createCsvLine(config, csvLine);
                        writer.println(lineString);
                    }
                }
                writer.flush();

                writer.close();
                finalResult.putExportData(table.getTableName(), exportStream.toByteArray());

            }
        }
        result.clear();

        state.setResult(finalResult);
    }


    /**
     * @param config
     * @param csvLine
     * @return
     */
    private String createCsvLine(OutputModelConfigurator config, String[] csvLine) {
        String lineString = "";
        boolean first = true;
        for (String columnEntry: csvLine){
            if (columnEntry == null){
                columnEntry = "";
            }
            columnEntry = columnEntry.replace("\"", "\"\"");
            columnEntry = columnEntry.replace(config.getLinesTerminatedBy(), "\\r");
            //replace all line brakes according to best practices: http://code.google.com/p/gbif-ecat/wiki/BestPractices
            columnEntry = columnEntry.replace("\r\n", "\\r");
            columnEntry = columnEntry.replace("\r", "\\r");
            columnEntry = columnEntry.replace("\n", "\\r");
            if (first){
                lineString += config.getFieldsEnclosedBy() + columnEntry + config.getFieldsEnclosedBy() ;
                first = false;
            }else{
                lineString += config.getFieldsTerminatedBy() + config.getFieldsEnclosedBy() + columnEntry + config.getFieldsEnclosedBy() ;
            }
        }

        return lineString;
    }
}
