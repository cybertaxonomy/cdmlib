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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.filter.TaxonNodeFilter;
import eu.etaxonomy.cdm.io.common.TaxonNodeOutStreamPartitioner;
import eu.etaxonomy.cdm.io.common.XmlExportState;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaTaxExport extends DwcaExportBase {
    private static final long serialVersionUID = -3770976064909193441L;

    private static final Logger logger = Logger.getLogger(DwcaTaxExport.class);


    @Autowired
    private ITaxonNodeService taxonNodeService;

	public DwcaTaxExport() {
		super();
		this.ioName = this.getClass().getSimpleName();
        file = DwcaTaxExportFile.TAXON;
	}


    /**
     * {@inheritDoc}
     */
    @Override
    public long countSteps(DwcaTaxExportState state) {
        TaxonNodeFilter filter = state.getConfig().getTaxonNodeFilter();
        return taxonNodeService.count(filter);
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

	    IProgressMonitor monitor = state.getCurrentMonitor();

		List<DwcaDataExportBase> exports =  Arrays.asList(new DwcaDataExportBase[]{
	        new DwcaTaxonExport(state),
	        new DwcaReferenceExport(state),
	        new DwcaResourceRelationExport(state),
	        new DwcaTypesExport(state),
	        new DwcaVernacularExport(state),
	        new DwcaDescriptionExport(state),
	        new DwcaDistributionExport(state),
	        new DwcaImageExport(state)
		});

		@SuppressWarnings("unchecked")
	    TaxonNodeOutStreamPartitioner<XmlExportState> partitioner
	      = TaxonNodeOutStreamPartitioner.NewInstance(
                this, state, state.getConfig().getTaxonNodeFilter(),
                (Integer)100, monitor, null);
		try {

		    TaxonNode node = partitioner.next();
			while (node != null){
			    for (DwcaDataExportBase export : exports){
			        handleTaxonNode(export, state, node);
			    }
			    node = partitioner.next();
			}
		} catch (Exception e) {
		    String message = "Unexpected exception: " + e.getMessage();
			state.getResult().addException(e, message, "DwcaTaxExport.doInvoke()");
		}
		finally{
		    if(partitioner != null){
		        partitioner.close();
		    }
		    for (DwcaDataExportBase export : exports){
		        closeWriter(export, state);
            }
		}

		return;

	}

    /**
     * @param taxonExport
     * @param state
     */
    private void closeWriter(DwcaDataExportBase export, DwcaTaxExportState state) {
        if (!export.isIgnore(state)){
            export.closeWriter(state);
        }
    }

    private void handleTaxonNode(DwcaDataExportBase export, DwcaTaxExportState state, TaxonNode node) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (!export.isIgnore(state)){
            export.handleTaxonNode(state, node);
        }
    }


	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	public boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoTaxa();
	}

}
