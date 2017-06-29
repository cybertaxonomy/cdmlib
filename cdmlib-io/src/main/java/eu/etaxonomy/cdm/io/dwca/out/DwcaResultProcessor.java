/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import eu.etaxonomy.cdm.io.common.ExportResult;
import eu.etaxonomy.cdm.model.common.ICdmBase;


/**
 * @author a.mueller
 * @date 25.06.2017
 *
 */
public class DwcaResultProcessor {

    private static final String HEADER = "HEADER_207dd23a-f877-4c27-b93a-8dbea3234281";

//    private Map<DwcaTaxOutputTable, Map<String,String[]>> resultAlt = new HashMap<>();
    private DwcaTaxExportState state;
    private Map<DwcaTaxOutputFile, Object> result = new HashMap<>();


    /**
     * @param state
     */
    public DwcaResultProcessor(DwcaTaxExportState state) {
        super();
        this.state = state;
    }

    public void put(DwcaTaxOutputFile table, ByteArrayOutputStream stream) {
        Object tableResult = result.get(table);
        if (tableResult == null){
            result.put(table, stream);
        }else if (tableResult instanceof ByteArrayOutputStream){
            throw new RuntimeException("table stream already exists for table "+  table.getTableName());
        }else {
            throw new RuntimeException("Stream not supported by table " + table.getTableName());
        }
    }



    /**
     * @param taxon
     * @param csvLine
     */
    public void put(DwcaTaxOutputFile table, String id, String[] csvLine) {
        Object tableResult = result.get(table);
        if (tableResult instanceof ByteArrayOutputStream){
            addRecordToStream((ByteArrayOutputStream)tableResult, id, csvLine);
        }else if (tableResult == null || tableResult instanceof Map){
            @SuppressWarnings("unchecked")
            Map<String,String[]> resultMap = (Map<String, String[]>)tableResult;
            if (resultMap == null ){
                resultMap = new HashMap<>();
                if (state.getConfig().isHasHeaderLines()){
                    resultMap.put(HEADER, table.getColumnNamesString());
                }
                result.put(table, resultMap);
            }
            String[] record = resultMap.get(id);
            if (record == null){
                record = csvLine;

                String[] oldRecord = resultMap.put(id, record);

                if (oldRecord != null){
                    String message = "Output processor already has a record for id " + id + ". This should not happen.";
                    state.getResult().addWarning(message);
                }
            }
        }else{
            throw new IllegalStateException("Illegal result for result table. Map or stream expected but was " + tableResult.getClass().getName());
        }
    }


    /**
     * @param tableResult
     * @param id
     * @param csvLine
     */
    private void addRecordToStream(ByteArrayOutputStream tableResult, String id, String[] csvLine) {
        //FIXME
        throw new RuntimeException("add record to stream not yet supported");
    }

    public boolean hasRecord(DwcaTaxOutputFile table, String id){
        Object tableResult = result.get(table);
        if (tableResult == null){
            return false;
        }else if (tableResult instanceof ByteArrayOutputStream){
            throw new RuntimeException("Single records not available for stream results");
        }else if (tableResult instanceof Map){
            @SuppressWarnings("unchecked")
            Map<String,String[]> resultMap = (Map<String, String[]>)tableResult;
            return resultMap.get(id) != null;
        }else{
            throw new IllegalStateException("Illegal result for result table. Map or stream expected but was " + tableResult.getClass().getName());
        }
    }


    /**
     * @param table
     * @param taxon
     * @param csvLine
     */
    public void put(DwcaTaxOutputFile table, ICdmBase cdmBase, String[] csvLine) {
       this.put(table, String.valueOf(cdmBase.getId()), csvLine);
    }


    /**
     * @return
     */
    public void createFinalResult() {
        ExportResult finalResult = state.getResult();

        if (!result.isEmpty() ){
            for (DwcaTaxOutputFile table: result.keySet()){
                //write each table in a stream ...
                Object tableResult = result.get(table);
                ByteArrayOutputStream exportStream;

                if (tableResult instanceof ByteArrayOutputStream){
                    exportStream = (ByteArrayOutputStream)tableResult;
                }else if (tableResult instanceof Map){
                    @SuppressWarnings("unchecked")
                    Map<String, String[]> tableData = (Map<String, String[]>)tableResult;
                    DwcaTaxExportConfigurator config = state.getConfig();
                    exportStream = new ByteArrayOutputStream();

                    try{
                        List<String> data = new ArrayList<>();
                        String[] csvHeaderLine = tableData.get(HEADER);
                        String lineString = createCsvLine(config, csvHeaderLine);
                        lineString = lineString + "";
                        data.add(lineString);
                        for (String key: tableData.keySet()){
                            if (!key.equals(HEADER)){
                                String[] csvLine = tableData.get(key);

                                lineString = createCsvLine(config, csvLine);
                                data.add(lineString);
                            }
                        }
                        IOUtils.writeLines(data,
                                null,exportStream,
                                Charset.forName("UTF-8"));
                    } catch(Exception e){
                        finalResult.addException(e, e.getMessage());
                    }
                }else{
                    throw new IllegalStateException("Illegal result for result table. Map or stream expected but was " + tableResult.getClass().getName());
                }

                finalResult.putExportData(table.getTableName(), exportStream.toByteArray());
            }
        }
        result.clear();

    }


    /**
     * @param config
     * @param csvLine
     * @return
     */
    private String createCsvLine(DwcaTaxExportConfigurator config, String[] csvLine) {
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
