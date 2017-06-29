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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.io.common.ZipWriter;
import eu.etaxonomy.cdm.model.common.CdmBase;
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
	private ZipWriter zipWriter;
	private List<TaxonNode>  allNodes = new ArrayList<>();
	private Map<DwcaTaxOutputFile, PrintWriter> writerMap = new HashMap<>();


    protected Map<DwcaTaxOutputFile,Set<Integer>> existingRecordIds = new HashMap<>();
    protected Set<UUID> existingRecordUuids = new HashSet<>();

    protected boolean recordExists(DwcaTaxOutputFile file, CdmBase cdmBase) {
        if (existingRecordIds.get(file) == null){
            return false;
        }else{
            return existingRecordIds.get(file).contains(cdmBase.getId());
        }
    }
    protected void addExistingRecord(DwcaTaxOutputFile file, CdmBase cdmBase) {
        Set<Integer> set = existingRecordIds.get(file);
        if (set == null){
            set = new HashSet<>();
            existingRecordIds.put(file, set);
        }
        set.add(cdmBase.getId());
    }
    protected boolean recordExistsUuid(CdmBase cdmBase) {
        return existingRecordUuids.contains(cdmBase.getUuid());
    }
    protected void addExistingRecordUuid(CdmBase cdmBase) {
        existingRecordUuids.add(cdmBase.getUuid());
    }

	public DwcaTaxExportState(DwcaTaxExportConfigurator config) {
		super(config);
		File file = config.getDestination();
		if (file != null && ! file.isDirectory()){
			try{
				isZip = true;
				if (! file.exists()){
					boolean created = file.createNewFile();
				}

				zipWriter = new ZipWriter(file);
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

	public OutputStream getZipStream(String fileName){
		if (isZip){
			OutputStream os = zipWriter.getEntryStream(fileName);
			return os;
		}else{
			return null;
		}
	}

	public void closeZip() throws IOException {
		if (zipWriter != null){
		    zipWriter.close();
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
