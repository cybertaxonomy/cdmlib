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
 * This class bufferes streams into the zip file in
 * {@link DeferredFileOutputStream}s which store data in
 * memory or, if a certain threshold size is exceeded in
 * temporary files.
 * Calling {@link #close()} will write all data into the
 * given defined zip file.
 * Each entry offers its own {@link OutputStream} to write
 * into in parallel.
 *
 * @author a.mueller
 * @date 29.06.2017
 *
 */
public class ZipWriter {


    public static final int DEFAULT_THRESHOLD = 1000000;

    private int threshold = DEFAULT_THRESHOLD;

    private Map<String, DeferredFileOutputStream> entryMap  = new HashMap<>();

    private File tmpFolder = new File(System.getProperty("java.io.tmpdir"));

    private File targetFile;

    public ZipWriter(File targetFile) {
        this(targetFile, null, null);
    }

    /**
     * @param threshold
     * @param tmpFolder
     */
    public ZipWriter(File targetFile, Integer threshold, File tmpFolder) {
        super();
        this.targetFile = targetFile;
        this.threshold = threshold == null ? DEFAULT_THRESHOLD : threshold;
        this.tmpFolder = tmpFolder == null ? this.tmpFolder : tmpFolder;
    }



    public OutputStream getEntryStream(String entry){
        DeferredFileOutputStream result = entryMap.get(entry);
        if (result == null){
            result = new DeferredFileOutputStream(threshold, "tmp", "xxx", tmpFolder);
            entryMap.put(entry, result);
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
            cleanupFile(os);
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
