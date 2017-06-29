/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaZipExport extends DwcaExportBase {

    private static final long serialVersionUID = -7674991232867769827L;
    private static final Logger logger = Logger.getLogger(DwcaZipExport.class);

	/**
	 * Constructor
	 */
	public DwcaZipExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves the MetaData for a Darwin Core Archive File.
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected void doInvoke(DwcaTaxExportState state){
		if (state.isZip()){
			try {
				state.closeZip();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
//		DwcaTaxExportConfigurator config = state.getConfig();
//
//		String zipFileName = "dwca.zip";
//	    String filePath = config.getDestinationNameString();
//		String zipFullFileName = filePath + File.separatorChar + zipFileName;
//
//		ZipOutputStream zos  = null;
//		try {
//		  	zos  = new ZipOutputStream( new FileOutputStream(zipFullFileName) ) ;
//
//		    for (DwcaMetaDataRecord record : state.getMetaRecords()){
//		    	try {
//					String fileLocation = record.getFileLocation();
//					File file = new File(filePath + File.separatorChar + fileLocation);
//					ZipEntry newEntry = new ZipEntry(fileLocation);
//					zos.putNextEntry(newEntry);
//
//					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//					while (bis.available() > 0) {
//					    zos.write(bis.read());
//					}
//					zos.closeEntry();
//					bis.close();
//					boolean wasDeleted = file.delete();
//					logger.debug(wasDeleted); //doesn't work on my machine
//				} catch (Exception e) {
//					//TODO finally is not called anymore
//					throw new IOException(e);
//				}
//		   	 }
//
//	        zos.finish();
//	        zos.close();
//
//		} catch (IOException e) {
//			try {
//		       if(zos!=null) zos.close();
//		    } catch(Exception ex){
//
//		    }
//			e.printStackTrace();
//		    //TODO finally is not called anymore
//			throw new RuntimeException(e);
//		} finally {
//		    try {
//		       if(zos!=null) zos.close();
//		    } catch(Exception ex){
//
//		    }
//		 }


		return;
	}



	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.isZip();
	}
}
