/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.coldp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import eu.etaxonomy.cdm.io.common.ExportType;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * TODO merge with CDM light
 *
 * @author a.mueller
 * @since 2023-07-17
 */
public class ColDpExportResultProcessor {

    private static final String HEADER = "HEADER_207dd23a-f877-4c27-b93a-8dbea3234281";

    private Map<ColDpExportTable, Map<String,String[]>> result = new HashMap<>();
    private ColDpExportState state;

    public ColDpExportResultProcessor(ColDpExportState state) {
        super();
        this.state = state;
        Map<String,String[]> resultMap;
        for (ColDpExportTable table: ColDpExportTable.values()){
            resultMap = new HashMap<>();
            if (state.getConfig().isIncludeHeaderLines()){
                resultMap.put(HEADER, table.getColumnNames());
            }
            result.put(table, resultMap);
        }
    }

    public void put(ColDpExportTable table, String id, String[] csvLine) {
        Map<String,String[]> resultMap = result.get(table);
        if (resultMap == null ){
            resultMap = new HashMap<>();
            if (state.getConfig().isIncludeHeaderLines()){
                resultMap.put(HEADER, table.getColumnNames());
            }
            result.put(table, resultMap);
        }
        String[] record = resultMap.get(id);
        if (record == null){
            record = csvLine;

            String[] oldRecord = resultMap.put(id, record);

            String[] newRecord = resultMap.get(id);

            if (oldRecord != null){
                String message = "Output processor already has a record for id " + id + ". This should not happen.";
                state.getResult().addWarning(message);
            }
        }
    }

    public boolean hasRecord(ColDpExportTable table, String id){
        Map<String, String[]> resultMap = result.get(table);
        if (resultMap == null){
            return false;
        }else{
            return resultMap.get(id) != null;
        }
    }

    public  String[] getRecord(ColDpExportTable table, String id){
        return result.get(table).get(id);
    }

    public void put(ColDpExportTable table, ICdmBase cdmBase, String[] csvLine) {
       this.put(table, cdmBase.getUuid().toString(), csvLine);
    }

    public void createFinalResult(ColDpExportState state) {

        if (!result.isEmpty() ){
            state.setAuthorStore(new HashMap<>());
            state.setHomotypicalGroupStore(new ArrayList<>());
            state.setReferenceStore(new ArrayList<>());
            state.setSpecimenStore(new ArrayList<>());
            state.setNodeChildrenMap(new HashMap<>());
            //Replace quotes by double quotes
            for (ColDpExportTable table: result.keySet()){
                //write each table in an explicite stream ...
                Map<String, String[]> tableData = result.get(table);
                ColDpExportConfigurator config = state.getConfig();
                ByteArrayOutputStream exportStream = new ByteArrayOutputStream();

                try{
                    List<String> data = new ArrayList<>();
                    String[] csvHeaderLine = tableData.get(HEADER);
                    String lineString = createCsvLine(config, csvHeaderLine);
                    lineString = lineString+ "";
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
                    e.printStackTrace();
                    state.getResult().addException(e, e.getMessage());
                }

                state.getResult().putExportData(table.getTableName(), exportStream.toByteArray());
                state.getResult().setExportType(ExportType.CDM_LIGHT);

            }
        }
        result.clear();
    }

    private String createCsvLine(ColDpExportConfigurator config, String[] csvLine) {
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