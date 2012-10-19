// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.redlist.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.dwca.out.DwcaMetaDataRecord;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
public class DwcaTaxExportStateRedlist extends XmlExportState<DwcaTaxExportConfiguratorRedlist>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxExportStateRedlist.class);

	private List<DwcaMetaDataRecordRedlist> metaRecords = new ArrayList<DwcaMetaDataRecordRedlist>();
	private boolean isZip = false;
	private ZipOutputStream zos;
	
	public DwcaTaxExportStateRedlist(DwcaTaxExportConfiguratorRedlist config) {
		super(config);
		File file = config.getDestination();
		if (! config.getDestination().isDirectory()){
			try{
				isZip = true;
				if (! file.exists()){
						file.createNewFile();
				}
				
			  	zos  = new ZipOutputStream( new FileOutputStream(file) ) ;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void addMetaRecord(DwcaMetaDataRecordRedlist record){
		metaRecords.add(record);
	}
	
	public List<DwcaMetaDataRecordRedlist> getMetaRecords(){
		return metaRecords;
	}

	/**
	 * @return the isZip
	 */
	public boolean isZip() {
		return isZip;
	}

	public ZipOutputStream getZipStream(String fileName) throws IOException {
		if (isZip){
			zos.putNextEntry(new ZipEntry(fileName));
			return zos;
		}else{
			return null;
		}
	}

	public void closeZip() throws IOException {
		if (zos != null){
			zos.closeEntry();
			zos.finish();
			zos.close();
		}
	}
	


}
