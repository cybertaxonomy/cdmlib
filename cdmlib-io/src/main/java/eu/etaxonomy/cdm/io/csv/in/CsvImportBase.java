/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.csv.in;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.transaction.TransactionStatus;

import au.com.bytecode.opencsv.CSVReader;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportResult;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

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
                int txN = 0; //transaction number
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
                        boolean debug = false;
                        if (debug){
                            e.printStackTrace();
                        }
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
    protected void refreshTransactionStatus(STATE state) {
        state.resetSession();
    }

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
     * Transaction save method to retrieve the source reference
     * if either existent or not in the database (uses check for uuid).
     *
     * @param state
     * @return the source reference
     */
    protected Reference getTransactionalSourceReference(STATE state) {

        //already loaded?
        Reference sourceRef = state.getSourceReference();
        if (sourceRef != null){
            return sourceRef;
        }

        //retrieve uuid
        UUID uuid = state.getConfig().getSourceRefUuid();
        if (uuid == null){
            sourceRef = state.getConfig().getSourceReference();
            if (sourceRef != null){
                uuid = sourceRef.getUuid();
            }
        }
        if (uuid != null){
            Reference existingRef = getReferenceService().find(uuid);
            if (existingRef != null){
                sourceRef = existingRef;
            }
        }

        if (sourceRef == null){
            sourceRef = ReferenceFactory.newGeneric();
            String title = state.getConfig().getSourceNameString();
            sourceRef.setTitle(title);
            state.getConfig().setSourceReference(sourceRef);
            //we do not save here as we expect the reference to be cascaded
        }
        state.setSourceReference(sourceRef);

        return sourceRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean doCheck(STATE state) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isIgnore(STATE state) {
        return false;
    }
}
