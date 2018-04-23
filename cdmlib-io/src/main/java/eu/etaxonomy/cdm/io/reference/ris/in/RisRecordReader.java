/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.reference.ris.in;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author a.mueller
 * @since 11.05.2017
 *
 */
public class RisRecordReader {

    private BufferedReader lineReader;
    private RisReferenceImportState state;
    private int lineNo = 0;
    private String lineNoStr;

    public static final Map<RisReferenceTag, List<RisValue>>  EOF = new HashMap<>();
    private static final String NL = "\n";

    protected class RisValue{
        RisReferenceTag tag;
        String value;
        String location;
        public RisValue(String value, String location, RisReferenceTag tag) {
            this.value = value; this.location = "line " + location; this.tag = tag;
        }
        @Override
        public String toString() {
            return location + ": " + tag + ": " + value;
        }
    }


    /**
     * @param baseReader
     */
    public RisRecordReader(RisReferenceImportState state, InputStreamReader inputReader) {
        lineReader = new BufferedReader(inputReader);
        this.state = state;
    }

    public Map<RisReferenceTag, List<RisValue>> readRecord(){
        try {
            Map<RisReferenceTag, List<RisValue>> result = new HashMap<>();
            String lineOrig;
            int count = 0;
            RisReferenceTag lastType = null;
            boolean startedWithTY = false;
            while ((lineOrig = lineReader.readLine()) != null) {
               String line = lineOrig;
               lineNo++;
               String lineNoStr = "line " + lineNo;
               if (isBlank(line)){
                   continue;
               }else if (count == 0 && line.length() > 1 && isTypeLine(line.substring(1))){
                   line = line.substring(1); //remove BOM cotrol character if encoding is not correctly working
               }
               // OLD BOM remove
               //if (Integer.valueOf(line.toCharArray()[0]).equals(65279)  ){ //remove BOM cotrol character if encoding is not correctly working
               //   line = line.substring(1);
               //}

               RisReferenceTag type;


               if (matchesRisLine(line)){
                   type = RisReferenceTag.TY;
                   if (isTypeLine(line)){
                       type = RisReferenceTag.TY;
                       addTaggedValue(type, result, line, lineNo);
                       count++;
                       startedWithTY = true;
                   }else{
                       if (!startedWithTY){
                           continue;
                       }
                       if (isErLine(line)){
                           addTaggedValue(RisReferenceTag.ER, result, line, lineNo);
                           startedWithTY = false;
                           return result;
                       }else{
                           //TODO
                           try {
                               type = RisReferenceTag.valueOf(line.substring(0, 2));
                           } catch (Exception e) {
                                //type stays null
                           }
                           if (type == null){
                               //TODO
                               //But should be handled as "Extension"
                               String message = "Unknown reference type %s . Reference attribute could not be added. ";
                               state.getResult().addWarning(message, lineNo);
                           }else{
                               addTaggedValue(type, result, line, lineNo);
                           }
                           count++;
                       }
                   }
                   if (type != null){
                       lastType = type;
                   }
               }else{
                   if (startedWithTY){
                       //add to prior
                       List<RisValue> priorList = result.get(lastType);
                       RisValue priorValue = priorList.get(priorList.size()-1);
                       priorValue.value = priorValue + NL + line;
                   }else{
                       String message = "RIS record does not start with TY. Can't create record. Line was: " + lineOrig ;
                       state.getResult().addError(message, lineNoStr);
                   }

               }
            }
            if (count>0){
                String message = "Unexpected end of file. Some records may not have been imported";
                state.getResult().addError(message, lineNoStr);
            }
            return EOF;

        } catch (IOException e) {
            String message = "Unexpected exception during RIS Reference Import";
            state.getResult().addException(e, message);
            return EOF;
        }
    }


    /**
     * @param result
     * @param line
     * @param lineNo2
     * @return
     */
    private void addTaggedValue(RisReferenceTag tag, Map<RisReferenceTag, List<RisValue>> result, String line, int lineNo) {
        String value = replaceTag(line);
        List<RisValue> list = result.get(tag);
        if (list == null){
            list = new ArrayList<>();
            result.put(tag, list);
        }
        RisValue risValue = new RisValue(value, "" + lineNo, tag);
        list.add(risValue);
        return;
    }

    /**
     * @param line
     * @return
     */
    private String replaceTag(String line) {
        return line.substring(5).trim();
    }


    private static final String erLineReStr = "ER\\s\\s-\\s+";
    private boolean isErLine(String line) {
        return line.matches(erLineReStr);
    }


    private static final String typeLineReStr = "TY\\s\\s-\\s.*";
    private boolean isTypeLine(String line) {
        return line.matches(typeLineReStr);
    }

    private static final String risLineReStr = "[A-Z1-9]{2}\\s\\s-\\s.*";
    private boolean matchesRisLine(String line) {
        boolean matches = line.matches(risLineReStr);
        return matches;
    }

    private boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
}
