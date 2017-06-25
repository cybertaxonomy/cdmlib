/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;

import eu.etaxonomy.cdm.common.IoResultBase;


/**
 * @author cmathew
 * @date 31 Jul 2015
 *
 */
public class ExportResult extends IoResultBase implements Serializable {

    private static final long serialVersionUID = 6843406252245776806L;

    private ExportResultState state;

    private ExportDataWrapper<?> data ;  //resulting files


// **************************** FACTORY ****************************************/

    public static ExportResult NewInstance(ExportResultType type){
        return new ExportResult(type);
    }

    public static ExportResult NewNoDataInstance(ExportResultType type){
        ExportResult result = new ExportResult(type);
        result.state = ExportResultState.SUCCESS_BUT_NO_DATA;
        return result;
    }


// *********************** CONSTRUCTOR *****************************************/

    private ExportResult(ExportResultType type) {
        if (type == null){
            type = ExportResultType.LIST_BYTE_ARRAY;
        }
        state = ExportResultState.SUCCESS;
        if (type.equals(ExportResultType.BYTE_ARRAY)){
            data = ExportDataWrapper.NewByteArrayInstance();
        }else if (type.equals(ExportResultType.LIST_BYTE_ARRAY)){
            data = ExportDataWrapper.NewListByteArrayInstance();
        }else if (type.equals(ExportResultType.MAP_BYTE_ARRAY)){
            data = ExportDataWrapper.NewMapByteArrayInstance();
        }
    }

    public enum ExportResultState{
        SUCCESS_BUT_NO_DATA,   //Only if NO data at all is exported, if only 1 class is exported use SUCCESS
        SUCCESS,               //All configured data exported, no warning, no errors
        SUCCESS_WITH_WARNING,   //All data exported but with some warnings
        FINISHED_WITH_ERROR,    //Probably all data exported but with errors
        INCOMPLETE_WITH_ERROR,  //Run to the end, but in the middle there might be "larger" amounts of data missing, e.g. some parts did not run to the end
        CANCELED,              //Export canceled by the user
        ABORTED,                //An handled exception occurred that lead to abort the export
        ;
    }

// **************** GETTER /SETTER *********************/

    public boolean isSuccess(){
        return state == ExportResultState.SUCCESS || state == ExportResultState.SUCCESS_WITH_WARNING;
    }

    public ExportResultState getState() {return state;}
    public void setState(ExportResultState state) {this.state = state;}



    public ExportDataWrapper<?> getExportData() {return data;}
    public void setExportData(ExportDataWrapper<?> data) {this.data = data;}
    public void addExportData(byte[] exportData) {
        data.addExportData(exportData);
    }
    public void putExportData(String tableName, byte[] exportData) {
        data.putExportData(tableName, exportData);
    }


    @Override
    protected void setExceptionState() {
        state = ExportResultState.INCOMPLETE_WITH_ERROR;
    }

    @Override
    public void setAborted() {this.state = ExportResultState.ABORTED;}


    @Override
    public String toString() {
        return state.toString();
    }

    /**
     * @param invoke
     */
    public void merge(ExportResult invoke) {
        // TODO implemented
    }



}
