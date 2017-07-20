/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.transaction.TransactionStatus;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportResult;

/**
 * A base class for <b>simple</b> CSV imports.
 * Simple means less than 10.000 lines and only one file
 * with a flat structure.<BR><BR>
 * See DwC-A import for more complex structures.<BR><BR>
 * Update: as it supports transactions now, also longer files
 * are possible.
 *
 * @author a.mueller
 * @date 08.07.2017
 *
 */
public abstract class CsvImportBase<CONFIG extends CsvImportConfiguratorBase, STATE extends CsvImportState<CONFIG>, T>
        extends CdmImportBase<CONFIG, STATE>{

    private static final long serialVersionUID = 3052198644463797541L;


    @Override
    protected void doInvoke(STATE state) {
        int txNLimit = state.getConfig().getTransactionLineCount();
        ImportResult result = state.getResult();
        try {
            InputStreamReader inputReader = state.getConfig().getSource();
            CSVReader csvReader = new CSVReader(inputReader, state.getConfig().getFieldSeparator());
            String[] headerStr = csvReader.readNext();
            String[] next = csvReader.readNext();

            if (headerStr == null){
                String message = "Import file is empty";
                result.addWarning(message);
            }else if (next == null){
                String message = "No data. Only header line exists";
                result.addWarning(message);
            }else{
                List<String> header = Arrays.asList(headerStr);
                TransactionStatus tx = this.startTransaction();
                int row = 2;
                int txN = 0;
                while (next != null){
                    try {
                        Map<String, String> record = lineToMap(header, next, row, result);
                        state.setCurrentRecord(record);
                        state.setRow(row);
                        handleSingleLine(state);
                        next = csvReader.readNext();
                        row++;
                        txN++;
                        if (txN >= txNLimit && txNLimit > 0){
                            tx = startNewTransaction(state, tx);
                            txN = 0;
                        }
                    } catch (Exception e) {
                        String message = "Exception when handling csv row: " + e.getMessage();
                        state.getResult().addException(e, message, null, state.getLine());
                    }
                }
                this.commitTransaction(tx);

            }
            csvReader.close();

            return ;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private TransactionStatus startNewTransaction(STATE state, TransactionStatus tx) {
        try {
            this.commitTransaction(tx);
        } catch (Exception e) {
            String message = "Exception when commiting transaction: " + e.getMessage();
            state.getResult().addException(e, message, null, state.getLine());
        }
        tx = this.startTransaction();
        try {
            refreshTransactionStatus(state);
        } catch (Exception e) {
            String message = "Exception when refreshing transaction: " + e.getMessage();
            state.getResult().addException(e, message, null, state.getLine());
        }
        return tx;
    }

    /**
     * To be implemented by subclasses if required
     * @param state
     */
    protected void refreshTransactionStatus(STATE state) {}

    /**
     * @param header
     * @param line
     * @return
     */
    private Map<String, String> lineToMap(List<String> header, String[] line, int row, ImportResult importResult) {
        Map<String, String> result = new HashMap<>();
        if (header.size() > line.length){
            String message = "CSV line has less fields than header";
            importResult.addError(message, row);
        }else if (header.size() < line.length){
            String message = "CSV line has more fields than header";
            importResult.addError(message, row);
        }
        for (int i = 0; i<header.size(); i++){
            String value = line.length < i ? null : line[i];
            if (StringUtils.isBlank(value)|| "NULL".equalsIgnoreCase(value)) {
                value = null;
            }
            result.put(header.get(i), value);
        }
        return result;
    }

    /**
     * @param state
     * @param result
     */
    protected abstract void handleSingleLine(STATE state);

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(STATE state) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(STATE state) {
        return false;
    }
}
