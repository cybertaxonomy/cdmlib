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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
public class DwcaDistributionExport extends DwcaDataExportBase {

    private static final long serialVersionUID = -3274468345456407430L;

    private static final Logger logger = Logger.getLogger(DwcaDistributionExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Distribution";
	protected static final String fileName = "distribution.txt";

    private DwcaMetaDataRecord metaRecord;

	/**
	 * Constructor
	 */
	public DwcaDistributionExport(DwcaTaxExportState state) {
		super();
		this.ioName = this.getClass().getSimpleName();
        metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
        state.addMetaRecord(metaRecord);
        file = DwcaTaxOutputFile.DISTRIBUTION;
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
    protected void handleTaxonNode(DwcaTaxExportState state,  TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        try {
            DwcaTaxExportConfigurator config = state.getConfig();

            Taxon taxon = CdmBase.deproxy(node.getTaxon());

            Set<TaxonDescription> descriptions = taxon.getDescriptions();
            for (TaxonDescription description : descriptions){
            	for (DescriptionElementBase el : description.getElements()){
            		if (el.isInstanceOf(Distribution.class) ){
            			if (! state.recordExists(file, el)){
            				DwcaDistributionRecord record = new DwcaDistributionRecord(metaRecord, config);
            				Distribution distribution = CdmBase.deproxy(el, Distribution.class);
            				handleDistribution(state, record, distribution, taxon, config);
            				PrintWriter writer = createPrintWriter(state, file);
            	            record.write(state, writer);
            				state.addExistingRecord(file, distribution);
            			}
            		}else if (el.getFeature().equals(Feature.DISTRIBUTION())){
            		    String message = "Distribution export for TextData not yet implemented";
            			state.getResult().addWarning(message);
            		}
            	}
            }
        } catch (Exception e) {
            String message = "Unexpected exception: " + e.getMessage();
            state.getResult().addException(e, message);
        }finally{
            flushWriter(state, file);
        }
    }




	private void handleDistribution(DwcaTaxExportState state, DwcaDistributionRecord record, Distribution distribution, Taxon taxon, DwcaTaxExportConfigurator config) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());
		handleArea(state, record, distribution.getArea(), taxon, true);
		//TODO missing
		record.setLifeStage(null);
		record.setOccurrenceStatus(distribution.getStatus());
		//TODO missing
		record.setThreadStatus(null);
		record.setEstablishmentMeans(distribution.getStatus());
		//TODO missing
		record.setAppendixCITES(null);
		//TODO missing
		record.setEventDate(null);
		//TODO missing
		record.setSeasonalDate(null);
		//FIXME
		record.setSource(getSources(distribution, config));
		//FIXME
		record.setOccurrenceRemarks(null);

	}

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}

	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoDistributions();
	}
}
