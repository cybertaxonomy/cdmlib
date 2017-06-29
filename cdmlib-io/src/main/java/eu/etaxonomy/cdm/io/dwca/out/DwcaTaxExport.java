/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.out;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaTaxExport extends DwcaDataExportBase {
    private static final long serialVersionUID = -3770976064909193441L;

    private static final Logger logger = Logger.getLogger(DwcaTaxExport.class);


	/**
	 *
	 */
	public DwcaTaxExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
        file = DwcaTaxOutputFile.TAXON;
	}

	/**
	 * Retrieves data from a CDM DB and serializes the CDM to XML.
	 * Starts with root taxa and traverses the classification to retrieve children taxa, synonyms and relationships.
	 * Taxa that are not part of the classification are not found.
	 * <BR>
	 * {@inheritDoc}
	 */
	@Override
	protected void doInvoke(DwcaTaxExportState state){

		TransactionStatus txStatus = startTransaction(true);

		DwcaTaxonExport taxonExport = new DwcaTaxonExport(state);
		DwcaReferenceExport refExport = new DwcaReferenceExport(state);
		DwcaResourceRelationExport relationExport = new DwcaResourceRelationExport(state);
		DwcaTypesExport typesExport = new DwcaTypesExport(state);
		DwcaVernacularExport vernacularExport = new DwcaVernacularExport(state);
		DwcaDescriptionExport descriptionExport = new DwcaDescriptionExport(state);
		DwcaDistributionExport distributionExport = new DwcaDistributionExport(state);
		DwcaImageExport imageExport = new DwcaImageExport(state);

		try {

			List<TaxonNode> allNodes = allNodes(state);

			for (TaxonNode node : allNodes){
			    taxonExport.handleTaxonNode(state, node);
				refExport.handleTaxonNode(state, node);
				relationExport.handleTaxonNode(state, node);
				typesExport.handleTaxonNode(state, node);
				vernacularExport.handleTaxonNode(state, node);
				descriptionExport.handleTaxonNode(state, node);
				distributionExport.handleTaxonNode(state, node);
				imageExport.handleTaxonNode(state, node);
			}
		} catch (Exception e) {
		    String message = "Unexpected exception: " + e.getMessage();
			state.getResult().addException(e, message, "DwcaTaxExport.doInvoke()");
		}
		finally{
		    taxonExport.closeWriter(state);
			refExport.closeWriter(state);
			relationExport.closeWriter(state);
			typesExport.closeWriter(state);
			vernacularExport.closeWriter(state);
			descriptionExport.closeWriter(state);
			distributionExport.closeWriter(state);
			imageExport.closeWriter(state);
		}
		commitTransaction(txStatus);
		return;

	}

    @Override
    protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

    }

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoTaxa();
	}

}
