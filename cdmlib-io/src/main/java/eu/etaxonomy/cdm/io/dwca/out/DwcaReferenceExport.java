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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaReferenceExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaReferenceExport.class);

	/**
	 * Constructor
	 */
	public DwcaReferenceExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
	}

	/** Retrieves data from a CDM DB and serializes them CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 * 
	 * @param exImpConfig
	 * @param dbname
	 * @param filename
	 */
	@Override
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		String dbname = config.getSource() != null ? config.getSource().getName() : "unknown";
    	String fileName = config.getDestinationNameString();
		logger.info("Serializing DB " + dbname + " to file " + fileName);
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			final String coreTaxFileName = "reference.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			List<TaxonNode> allNodes =  getAllNodes(null);
			for (TaxonNode node : allNodes){
				//sec
				DwcaReferenceRecord record = new DwcaReferenceRecord();
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Reference sec = taxon.getSec();
				if (sec == null){
					handleReference(record, sec, taxon);
					record.write(writer);
				}
				
				//nomRef
				record = new DwcaReferenceRecord();
				INomenclaturalReference nomRef = taxon.getName().getNomenclaturalReference();
				if (nomRef != null){
					handleReference(record, (Reference)nomRef, taxon);
					record.write(writer);
				}
				
				writer.flush();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		commitTransaction(txStatus);
		return true;
	}
	



	private void handleReference(DwcaReferenceRecord record, Reference reference, Taxon taxon) {
		record.setCoreid(taxon.getId());
		
		record.setISBN_ISSN(StringUtils.isNotBlank(reference.getIsbn())? reference.getIsbn(): reference.getIssn());
		record.setUri(reference.getUri());
		//TODO implementation, DOI is extension type
		record.setDoi(null);
		record.setLsid(reference.getLsid());
		//TODO microreference
		record.setBibliographicCitation(reference.getTitleCache());
		record.setTitle(reference.getTitle());
		record.setCreator(reference.getAuthorTeam());
		record.setDate(reference.getDatePublished());
		record.setSource(reference.getInReference()==null?null:reference.getInReference().getTitleCache());
		
		//FIXME abstracts, remarks, notes
		record.setDescription(reference.getReferenceAbstract());
		//FIXME
		record.setSubject(null);
		
		//TODO missing, why ISO639-1 better 639-3
		record.setLanguage(null);
		record.setRights(reference.getRights());
		//TODO
		record.setTaxonRemarks(null);
		//TODO
		record.setType(null);
	}
	
	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoReferences();
	}
	
}
