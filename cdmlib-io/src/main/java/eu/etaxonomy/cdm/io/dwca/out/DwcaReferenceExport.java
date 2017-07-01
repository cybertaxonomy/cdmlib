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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
public class DwcaReferenceExport extends DwcaDataExportBase {

    private static final long serialVersionUID = -8334741499089219441L;

    private static final Logger logger = Logger.getLogger(DwcaReferenceExport.class);

	protected static final String fileName = "reference.txt";
	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Reference";

	private DwcaMetaDataRecord metaRecord;

	/**
	 * Constructor
	 */
	public DwcaReferenceExport(DwcaTaxExportState state) {
		super();
		this.ioName = this.getClass().getSimpleName();
		metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
        state.addMetaRecord(metaRecord);
        file = DwcaTaxExportFile.REFERENCE;
	}

	@Override
	protected void doInvoke(DwcaTaxExportState state){}

    /**
     * @param state
     * @param node
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    @Override
    protected void handleTaxonNode(DwcaTaxExportState state,
            TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {
        try {
            //sec
            DwcaReferenceRecord record = new DwcaReferenceRecord(metaRecord, state.getConfig());
            Taxon taxon = CdmBase.deproxy(node.getTaxon());
            Reference sec = taxon.getSec();
            if (sec != null && ! state.recordExists(file, sec)){
            	handleReference(state, record, sec, taxon);
            	PrintWriter writer = createPrintWriter(state, file);
                record.write(state, writer);
            	state.addExistingRecord(file, sec);
            }

            //nomRef
            record = new DwcaReferenceRecord(metaRecord, state.getConfig());
            INomenclaturalReference nomRefI = taxon.getName().getNomenclaturalReference();
            Reference nomRef = CdmBase.deproxy(nomRefI, Reference.class);
            if (nomRef != null && ! state.recordExists(file, nomRef)){
            	handleReference(state, record, nomRef, taxon);
            	PrintWriter writer = createPrintWriter(state, file);
                record.write(state, writer);
            	state.addExistingRecord(file, nomRef);
            }

        } catch (Exception e) {
            String message = "Unexpected exception: " + e.getMessage();
            state.getResult().addException(e, message);
        }finally{
            flushWriter(state, file);
        }
    }

	private void handleReference(DwcaTaxExportState state, DwcaReferenceRecord record, Reference reference, Taxon taxon) {

		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());

		record.setISBN_ISSN(StringUtils.isNotBlank(reference.getIsbn())? reference.getIsbn(): reference.getIssn());
		record.setUri(reference.getUri());
		record.setDoi(reference.getDoiString());
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
	public boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoReferences();
	}

}
