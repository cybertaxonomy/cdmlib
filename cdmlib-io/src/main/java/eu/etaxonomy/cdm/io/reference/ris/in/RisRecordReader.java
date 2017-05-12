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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author a.mueller
 * @date 11.05.2017
 *
 */
public class RisRecordReader {

    private BufferedReader lineReader;
    private RisReferenceImportState state;
    private int lineNo = 0;
    private String lineNoStr;

    public static final Map<RisReferenceTag, String>  EOF = new HashMap<>();
    private static final String NL = "\n";


    /**
     * @param baseReader
     */
    public RisRecordReader(RisReferenceImportState state, InputStreamReader inputReader) {
        lineReader = new BufferedReader(inputReader);
        this.state = state;
    }

    public Map<RisReferenceTag, String> readRecord(){
        try {
            Map<RisReferenceTag, String> result = new HashMap<>();
            String line;
            int count = 0;
            boolean started = false;
            RisReferenceTag lastType = null;
            while ((line = lineReader.readLine()) != null) {
                lineNo++;
                if (isBlank(line)){
                   continue;
               }
               RisReferenceTag type;
               if (matchesRisLine(line)){
                   type = RisReferenceTag.TY;
                   if (isTypeLine(line)){
                       type = RisReferenceTag.TY;
                       result.put(type, replaceTag(line));
                       count++;
                   }else if (isErLine(line)){
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
                           //Sollte aber als als extension trotzdem übergeben werden
                           String message = "Unknown reference type %s . Reference attribute could not be added";
                           state.getResult().addWarning(message, lineNo);
                       }else{
                           result.put(type, replaceTag(line));
                       }
                       count++;
                   }
                   if (type != null){
                       lastType = type;
                   }
               }else{
                   if (started){
                       //add to prior
                       String prior = result.get(lastType);
                       result.put(lastType, prior + NL + line);
                   }else{
                       String message = lineNoStr + "RIS record does not start with TY. Can't create record";
                       state.getResult().addError(message, lineNo);
                   }

               }
            }
            if (count>0){
                String message = lineNoStr + "Unexpected end of file. Some records may not have been imported";
                state.getResult().addError(message, lineNo);
            }
            return EOF;

        } catch (IOException e) {
            String message = "Unexpected exception during RIS Reference Import";
            state.getResult().addException(e, message);
            return EOF;
        }
    }


    /**
     * @param line
     * @return
     */
    private String replaceTag(String line) {
        return line.substring(5).trim();
    }


    private static final String risLineReStr = "[A-Z1-9]{2}\\s\\s-\\s.*";
    private static final String typeLineReStr = "TY\\s\\s-\\s.*";
    private static final String erLineReStr = "ER\\s\\s-\\s+";


    /**
     * @param line
     * @return
     */
    private boolean isErLine(String line) {
        return line.matches(erLineReStr);
    }


    /**
     * @param line
     * @return
     */
    private boolean isTypeLine(String line) {
        return line.matches(typeLineReStr);
    }


    /**
     * @param line
     * @return
     */
    private boolean matchesRisLine(String line) {
        return line.matches(risLineReStr);
    }

    private boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
}
