/**
d* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaReferenceExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaReferenceExport.class);

	private static final String fileName = "reference.txt";
	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Reference";

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
	protected void doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		TransactionStatus txStatus = startTransaction(true);

		PrintWriter writer = null;
		try {
			writer = createPrintWriter(fileName, state);
			DwcaMetaDataRecord metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
			state.addMetaRecord(metaRecord);

			Set<UUID> classificationUuidSet = config.getClassificationUuids();
            List<Classification> classificationList;
            if (classificationUuidSet.isEmpty()){
                classificationList = getClassificationService().list(Classification.class, null, 0, null, null);
            }else{
                classificationList = getClassificationService().find(classificationUuidSet);
            }

            Set<Classification> classificationSet = new HashSet<Classification>();
            classificationSet.addAll(classificationList);
            List<TaxonNode> allNodes;

            if (state.getAllNodes().isEmpty()){
                getAllNodes(state, classificationSet);
            }
            allNodes = state.getAllNodes();
			for (TaxonNode node : allNodes){
				//sec
				DwcaReferenceRecord record = new DwcaReferenceRecord(metaRecord, config);
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Reference sec = taxon.getSec();
				if (sec != null && ! recordExists(sec)){
					handleReference(record, sec, taxon);
					record.write(writer);
					addExistingRecord(sec);
				}

				//nomRef
				record = new DwcaReferenceRecord(metaRecord, config);
				INomenclaturalReference nomRef = taxon.getName().getNomenclaturalReference();
				if (nomRef != null && ! existingRecordIds.contains(nomRef.getId())){
					handleReference(record, (Reference)nomRef, taxon);
					record.write(writer);
					addExistingRecord((Reference)nomRef);
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
		} finally{
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
	}

	private void handleReference(DwcaReferenceRecord record, Reference reference, Taxon taxon) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());

		record.setISBN_ISSN(StringUtils.isNotBlank(reference.getIsbn())? reference.getIsbn(): reference.getIssn());
		record.setUri(reference.getUri());
		//TODO implementation, DOI is extension type
		record.setDoi(null);
		record.setLsid(reference.getLsid());
		//TODO microreference
		record.setBibliographicCitation(reference.getTitleCache());
		record.setTitle(reference.getTitle());
		record.setCreator(reference.getAuthorship());
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
