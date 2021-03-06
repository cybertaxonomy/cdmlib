/*
* Copyright  EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.View;

/**
 * This view will return a simple HTTP file download,
 * please specifiy content type, file name, suffix of
 * file name and the encoding of the file.
 *
 *
 * @author a.oppermann
 * @since 20.03.2014
 *
 */
public class FileDownloadView implements View{

    private String contentType;

    private String fileName;

    private String suffix;

    private String encoding;



    /**
     * This view will render a simple HTTP file download
     *
     * <p>
     *
     * Please make sure you set contentType, fileName, suffix and encoding via setter-methods.
     * Otherwise you'll run into an exception.
     */
    public FileDownloadView(){
      //empty constructor
    }

    /**
     * This view will render a simple HTTP file download with default
     * settings for content type (application/octet-stream) and encoding (no encoding).
     *
     * @param fileName specifiy the name of the file you want to save
     * @param suffix file ending like "txt" or "pdf"
     */
    public FileDownloadView(String fileName, String suffix){
        this.contentType = "application/octet-stream";
        this.fileName = fileName;
        this.suffix = suffix;
        this.encoding = null;
    }


    /**
     * This view will render a simple HTTP file download.
     *
     * @param contentType like "text/csv" or "application/pdf"
     * @param fileName specifiy the name of the file you want to save
     * @param suffix file ending like "txt" or "pdf"
     * @param encoding charset like "utf-8"
     */
    public FileDownloadView(String contentType, String fileName, String suffix, String encoding){
        this.contentType = contentType;
        this.fileName = fileName;
        this.suffix = suffix;
        this.encoding = encoding;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest arg1, HttpServletResponse response) throws Exception {
        response.setContentType(getContentType() + (isBinaryData() ? "" : "; charset="+getEncoding().toLowerCase(Locale.ENGLISH)));
        if(!isBinaryData()){
            response.setCharacterEncoding(getEncoding().toUpperCase(Locale.ENGLISH));
        }
        response.setHeader("Content-Disposition", "attachment; filename=\""+getFileName()+"."+getSuffix()+"\"");

        //set File
        if(model.containsKey("file")){
            Object object = model.get("file");
            if(object instanceof File){
                try{
                    File file = (File) object;
                    FileInputStream fis = new FileInputStream(file);
                    if(!isBinaryData()){
                        InputStreamReader isr = new InputStreamReader(fis, getEncoding().toUpperCase(Locale.ENGLISH));
                        IOUtils.copy(isr, response.getWriter());
                    } else {
                        IOUtils.copy(fis, response.getOutputStream());
                    }
                    response.flushBuffer();
                }catch(IOException e){
                    throw new IOException("IOERROR writing file to outputstream \n" + e);
                }
            }
        }
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType){
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix){
        this.suffix = suffix;
    }

    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    protected boolean isBinaryData(){
        return encoding == null;
    }
}
