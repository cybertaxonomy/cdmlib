/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author k.luther
 * @date 17.03.2017
 *
 */
public class ExportDataWrapper<T> implements Serializable{

    private static final long serialVersionUID = 4500184563547082579L;

    private T exportData;
    private ExportResultType type;

    /**
     * @return the type
     */
    public ExportResultType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ExportResultType type) {
        this.type = type;
    }

    private ExportDataWrapper(){

    }

    public static final ExportDataWrapper<List<byte[]>> NewListByteArrayInstance(){
        ExportDataWrapper<List<byte[]>> result = new ExportDataWrapper<>();
        result.type = ExportResultType.LIST_BYTE_ARRAY;
        result.exportData = new ArrayList<>();
        return result;
    }

    public static final ExportDataWrapper<Map<String,byte[]>> NewMapByteArrayInstance(){
        ExportDataWrapper<Map<String, byte[]>> result = new ExportDataWrapper<>();
        result.type = ExportResultType.MAP_BYTE_ARRAY;
        result.exportData = new HashMap<>();
        return result;
    }

    public static final ExportDataWrapper<byte[]> NewByteArrayInstance(){
        ExportDataWrapper<byte[]> result = new ExportDataWrapper<>();
        result.type = ExportResultType.BYTE_ARRAY;

        return result;
    }

    public void setValue(T value){this.exportData = value;}

    public T getExportData(){return this.exportData;}

    public void addExportData(byte[] data){
        if (type.equals(ExportResultType.BYTE_ARRAY)){
            exportData = (T)data;
        } else if (type.equals(ExportResultType.LIST_BYTE_ARRAY)){
            ((List<byte[]>)exportData).add(data);
        }

    }

    public void addExportData(byte[] data, String key){
        if (type.equals(ExportResultType.MAP_BYTE_ARRAY)){
            ((Map<String, byte[]>)exportData).put(key,data);
        }
    }

}
