/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
public class DwcaTaxExportState extends XmlExportState<DwcaTaxExportConfigurator>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaTaxExportState.class);

	private DwcaResultProcessor processor = new DwcaResultProcessor(this);

	private List<DwcaMetaDataRecord> metaRecords = new ArrayList<>();
	private boolean isZip = false;
	private ZipOutputStream zos;
	private List<TaxonNode>  allNodes = new ArrayList<>();
	private Map<DwcaTaxOutputFile, PrintWriter> writerMap = new HashMap<>();

	public DwcaTaxExportState(DwcaTaxExportConfigurator config) {
		super(config);
		File file = config.getDestination();
		if (file != null && ! file.isDirectory()){
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

	public void addMetaRecord(DwcaMetaDataRecord record){
		metaRecords.add(record);
	}

	public List<DwcaMetaDataRecord> getMetaRecords(){
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

    /**
     * @return the allNodes
     */
    public List<TaxonNode> getAllNodes() {
        return allNodes;
    }

    /**
     * @param allNodes the allNodes to set
     */
    public void setAllNodes(List<TaxonNode> allNodes) {
        this.allNodes = allNodes;
    }

    public DwcaResultProcessor getProcessor() {
        return processor;
    }

    public PrintWriter getWriter(DwcaTaxOutputFile file){
        return this.writerMap.get(file);
    }

    public PrintWriter putWriter(DwcaTaxOutputFile file, PrintWriter writer){
        return this.writerMap.put(file, writer);
    }



}
