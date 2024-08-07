/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.IoResultBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 * @since 5 Aug 2015
 */
public class ImportResult extends IoResultBase {

    private static final long serialVersionUID = -7299667532720042100L;

    private List<byte[]> reports = new ArrayList<>();
    //map with simple class name and count
    private Map<String, Integer> newRecords = new HashMap<>();
    private Map<String, Integer> updatedRecords = new HashMap<>();
    private Map<String, Integer> deletedRecords = new HashMap<>();

    private ImportResultState state;

// **************************** FACTORY ****************************************/

    public static ImportResult NewInstance(){
        return new ImportResult();
    }

    public static ImportResult NewNoDataInstance(){
        ImportResult result = new ImportResult();
        result.state = ImportResultState.SUCCESS_BUT_NO_DATA;
        return result;
    }

// *********************** CONSTRUCTOR *****************************************/

    public ImportResult() {
        state = ImportResultState.SUCCESS;
    }

    public enum ImportResultState{
        SUCCESS_BUT_NO_DATA,   //Only if NO data at all is exported, if only 1 class is exported use SUCCESS
        SUCCESS,               //All configured data exported, no warning, no errors
        SUCCESS_WITH_WARNING,   //All data exported but with some warnings
        FINISHED_WITH_ERROR,    //Probably all data exported but with errors
        INCOMPLETE_WITH_ERROR,  //Run to the end, but in the middle there might be "larger" amounts of data missing, e.g. some parts did not run to the end
        CANCELED,              //Export canceled by the user
        ABORTED,                //An handled exception occurred that lead to abort the export
        ;
    }

    @Override
    protected void setExceptionState() {
        state = ImportResultState.INCOMPLETE_WITH_ERROR;
    }

    @Override
    public void setAborted() {this.state = ImportResultState.ABORTED;}

    public boolean isSuccess(){
        return state == ImportResultState.SUCCESS || state == ImportResultState.SUCCESS_WITH_WARNING;
    }

    public void merge(ImportResult otherResult) {
        mergeMap(this.deletedRecords, otherResult.deletedRecords);
        mergeMap(this.updatedRecords, otherResult.updatedRecords);
        mergeMap(this.newRecords, otherResult.newRecords);
        this.reports.addAll(otherResult.reports);
    }

    private void mergeMap(Map<String, Integer> thisMap, Map<String, Integer> otherMap) {
        for (String key: otherMap.keySet()){
            int existing = thisMap.get(key)== null ? 0 : thisMap.get(key);
            thisMap.put(key, existing + thisMap.get(key));
        }
    }

    public List<byte[]> getReports() {
        return reports;
    }
    public void setReports(List<byte[]> reports) {
        this.reports = reports;
    }

    public void addReport(byte[] report) {
        reports.add(report);
    }


    public Map<String, Integer> getNewRecords() {
        return clone(newRecords);
    }
    public Integer getNewRecords(Class<? extends CdmBase> clazz) {
        return clone(newRecords).get(clazz.getSimpleName());
    }

    public Map<String, Integer> getUpdatedRecords() {
        return clone(updatedRecords);
    }

    private Map<String, Integer> clone(Map<String, Integer> records) {
        Map<String, Integer> result = new HashMap<>(records.size());
        for (String clazz : records.keySet()){
            result.put(clazz, records.get(clazz));
        }
        return result;
    }

    public Map<String, Integer> getDeletedRecords() {
        return clone(deletedRecords);
    }

    //new records
    public void addNewRecord(String clazz){
        addNewRecords(clazz, 1);
    }
    public void addNewRecords(String clazz, int count){
        addRecord(newRecords, clazz, count);
    }
    public void addNewRecords(Class<? extends CdmBase> clazz, int count){
        addRecord(newRecords, clazz.getSimpleName(), count);
    }
    public void addNewRecord(CdmBase newRecord) {
        this.addNewRecord(CdmBase.deproxy(newRecord).getClass().getSimpleName());
    }

    //updated records
    public void addUpdatedRecord(String clazz){
        addUpdatedRecords(clazz, 1);
    }
    public void addUpdatedRecords(String clazz, int count){
        addRecord(updatedRecords, clazz, count);
    }
    public void addUpdatedRecord(CdmBase updatedRecord) {
        this.addUpdatedRecord(CdmBase.deproxy(updatedRecord).getClass().getSimpleName());
    }

    //deleted
    public void addDeletedRecord(String clazz){
        addDeletedRecords(clazz, 1);
    }
    public void addDeletedRecords(String clazz, int count){
        addRecord(deletedRecords, clazz, count);
    }

    private void addRecord(Map<String, Integer> records, String clazz, Integer count){
        initClassRecord(records, clazz);
        records.put(clazz, records.get(clazz) + count);
    }

    private void initClassRecord(Map<String, Integer> records, String clazz) {
        if (records.get(clazz) == null){
            records.put(clazz, 0);
        }
    }

    public void addDeletedRecord(CdmBase deletedRecord) {
        this.addDeletedRecord(CdmBase.deproxy(deletedRecord).getClass().getSimpleName());
    }

    @Override
    public StringBuffer createReport() {
        StringBuffer report = super.createReport();
        addEditedReport(report, "New records", this.newRecords);
        addEditedReport(report, "Updated records", this.updatedRecords);
        addEditedReport(report, "Deleted records", this.deletedRecords);
        return report;
    }

    private void addEditedReport(StringBuffer report, String label, Map<String, Integer> records) {
        if (!records.isEmpty()){
            report.append("\n\n" + label + ":\n" + StringUtils.leftPad("", label.length()+1, "="));
            for (String key : records.keySet()){
                report.append("\n" + key + ": " + records.get(key));
            }
        }
    }
}
