/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * Writing to zip files is not possible in parallel.
 * This class buffers streams into the zip file in
 * {@link DeferredFileOutputStream}s which store data in
 * memory or, if a certain threshold size is exceeded in
 * temporary files.
 * Calling {@link #close()} will write all data into the
 * given defined zip file.
 * Each entry offers its own {@link OutputStream} to write
 * into in parallel.
 *
 * @author a.mueller
 * @since 29.06.2017
 *
 */
public class ZipWriter {


    public static final int DEFAULT_THRESHOLD = 2000000;

    private int threshold = DEFAULT_THRESHOLD;

    private Map<String, DeferredFileOutputStream> entryMap  = new HashMap<>();

    private File tmpFolder = new File(System.getProperty("java.io.tmpdir"));

    private File targetFile;

    /**
     * {@link ZipWriter} with default values.
     *
     * @param targetFile final zip file to be filled
     */

    public ZipWriter(File targetFile) {
        this(targetFile, null, null);
    }

    /**
     * @param targetFile final zip file to be filled
     * @param threshold the threshold when to start writing to file,
     *         uses default (2MB) if  <code>null</code>
     * @param tmpFolder the folder to write the temporary file to
     */
    public ZipWriter(File targetFile, Integer threshold, File tmpFolder) {
        super();
        this.targetFile = targetFile;
        this.threshold = threshold == null ? DEFAULT_THRESHOLD : threshold;
        this.tmpFolder = tmpFolder == null ? this.tmpFolder : tmpFolder;
    }



    public OutputStream getEntryStream(String entryName){
        DeferredFileOutputStream result = entryMap.get(entryName);
        if (result == null){
            result = new DeferredFileOutputStream(threshold, "tmp", "_zip_buffer", tmpFolder);
            entryMap.put(entryName, result);
        }
        return result;
    }

    public void close() throws IOException{
        FileOutputStream fos = new FileOutputStream(targetFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (String key:  entryMap.keySet()){
            ZipEntry zipEntry = new ZipEntry(key);
            zos.putNextEntry(zipEntry);
            DeferredFileOutputStream os = entryMap.get(key);
            os.close();
//            zos.setComment(comment);
            os.writeTo(zos);
            zos.closeEntry();
        }
        try {
            zos.finish();
            zos.close();
        } catch (IOException e) {
            throw e;
        }
        for (String key:  entryMap.keySet()){
            DeferredFileOutputStream os = entryMap.get(key);
            cleanupFile(os);
        }
    }

    /**
     * @param os
     */
    private boolean cleanupFile(DeferredFileOutputStream os) {
        try {
            boolean deleted = true;
            File file = os.getFile();
            if (file != null){
                deleted = file.delete();
            }
            return deleted;
        } catch (Exception e) {
            //no matter, it should be in temp folder
            e.printStackTrace();
            return false;
        }
    }

}
