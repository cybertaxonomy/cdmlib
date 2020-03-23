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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.format.description.CategoricalDataFormatter;
import eu.etaxonomy.cdm.format.description.QuantitativeDataFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.StateData;
import eu.etaxonomy.cdm.model.description.StatisticalMeasurementValue;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @since 20.04.2011
 */
public class DwcaDescriptionExport extends DwcaDataExportBase {

    private static final long serialVersionUID = 4756084824053120718L;

    private static final Logger logger = Logger.getLogger(DwcaDescriptionExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Description";
	protected static final String fileName = "description.txt";

    private DwcaMetaDataRecord metaRecord;

    /**
	 * Constructor
	 */
	public DwcaDescriptionExport(DwcaTaxExportState state) {
		this.ioName = this.getClass().getSimpleName();
	    metaRecord = new DwcaMetaDataRecord(! IS_CORE, fileName, ROW_TYPE);
	    state.addMetaRecord(metaRecord);
	    file = DwcaTaxExportFile.DESCRIPTION;
	}

	@Override
	protected void doInvoke(DwcaTaxExportState state){}

    @Override
    protected void handleTaxonNode(DwcaTaxExportState state, TaxonNode node)
            throws IOException, FileNotFoundException, UnsupportedEncodingException {

        DwcaTaxExportConfigurator config = state.getConfig();
        try {
            Taxon taxon = CdmBase.deproxy(node.getTaxon());
            Set<TaxonDescription> descriptions = node.getTaxon().getDescriptions();
            for (TaxonDescription description : descriptions){
            	for (DescriptionElementBase el : description.getElements()){
            	    el = CdmBase.deproxy(el);  //just in case
            	    Feature feature = el.getFeature();
            	    //TODO there is a lot of redundancy here as it had been implemented as a quick fix
            	    //needs to be cleaned up
            	    if (feature != null &&
                            ! feature.equals(Feature.IMAGE()) &&
                            ! config.getFeatureExclusions().contains(feature.getUuid()) &&
                            ! state.recordExists(file,el)){
            	        if (el.isInstanceOf(TextData.class) ){
            	            DwcaDescriptionRecord record = new DwcaDescriptionRecord(metaRecord, config);
        	                TextData textData = CdmBase.deproxy(el,TextData.class);
        	                handleTextData(state, record, textData, taxon, config);
        	                PrintWriter writer = createPrintWriter(state, DwcaTaxExportFile.DESCRIPTION);
        	                record.write(state, writer);
        	                state.addExistingRecord(file, textData);
            	        }else if (el.isInstanceOf(CategoricalData.class)){
                            DwcaDescriptionRecord record = new DwcaDescriptionRecord(metaRecord, config);
                            CategoricalData cd = CdmBase.deproxy(el,CategoricalData.class);
                            handleCategoricalData(state, record, cd, taxon, config);
                            PrintWriter writer = createPrintWriter(state, DwcaTaxExportFile.DESCRIPTION);
                            record.write(state, writer);
                            state.addExistingRecord(file, cd);
            	        }else if (el.isInstanceOf(QuantitativeData.class)){
                            DwcaDescriptionRecord record = new DwcaDescriptionRecord(metaRecord, config);
                            QuantitativeData qd = CdmBase.deproxy(el, QuantitativeData.class);
                            handleQuantitativeData(state, record, qd, taxon, config);
                            PrintWriter writer = createPrintWriter(state, DwcaTaxExportFile.DESCRIPTION);
                            record.write(state, writer);
                            state.addExistingRecord(file, qd);
            	        }
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

    private void handleCategoricalData(DwcaTaxExportState state, DwcaDescriptionRecord record,
            CategoricalData catData, Taxon taxon, DwcaTaxExportConfigurator config){

        record.setId(taxon.getId());
        record.setUuid(taxon.getUuid());

        if (catData.getFeature() == null){
            String message = "No feature available for text data ("+catData.getId()+"). Feature is required field. Taxon: " + this.getTaxonLogString(taxon);
            state.getResult().addWarning(message);
        }
        record.setType(catData.getFeature());

        List<StateData> stateDatas = catData.getStateData();
        if (stateDatas.isEmpty()){
            String message = "No state data available for categorical data ("+catData.getId()+"). Text is required field. Taxon: " + this.getTaxonLogString(taxon);
            state.getResult().addWarning(message);
        }else{
            List<Language> preferredLanguages = new ArrayList<>();
            preferredLanguages.add(Language.DEFAULT());
            CategoricalDataFormatter formatter = new CategoricalDataFormatter(catData, null);
            formatter.format(catData);
            record.setDescription(formatter.format(catData));
            //TODO
//            record.setLanguage(languageText.getLanguage());
        }

        //sources
        record.setSource(getSources(catData, config));

        //TODO missing , relationship to credits?
        record.setCreator(null);
        //TODO missing, relationship to credits?
        record.setContributor(null);
        //TODO missing
        record.setAudience(null);
        record.setLicense(catData.getInDescription().getRights());
        //TODO missing
        record.setRightsHolder(null);
    }


    private void handleQuantitativeData(DwcaTaxExportState state, DwcaDescriptionRecord record,
            QuantitativeData quantData, Taxon taxon, DwcaTaxExportConfigurator config){

        record.setId(taxon.getId());
        record.setUuid(taxon.getUuid());

        if (quantData.getFeature() == null){
            String message = "No feature available for text data ("+quantData.getId()+"). Feature is required field. Taxon: " + this.getTaxonLogString(taxon);
            state.getResult().addWarning(message);
        }
        record.setType(quantData.getFeature());

        Set<StatisticalMeasurementValue> values = quantData.getStatisticalValues();
        if (values.isEmpty()){
            String message = "No values available for quantitative data ("+quantData.getId()+"). Values are required. Taxon: " + this.getTaxonLogString(taxon);
            state.getResult().addWarning(message);
        }else{
            List<Language> preferredLanguages = new ArrayList<>();
            preferredLanguages.add(Language.DEFAULT());
            QuantitativeDataFormatter formatter = new QuantitativeDataFormatter(quantData, null);
            formatter.format(quantData);
            record.setDescription(formatter.format(quantData));
            //TODO
//            record.setLanguage(languageText.getLanguage());
        }

        handleDescriptionElement(record, quantData, config);
    }

	private void handleTextData(DwcaTaxExportState state, DwcaDescriptionRecord record,
	        TextData textData, Taxon taxon, DwcaTaxExportConfigurator config) {

	    record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());

		//TODO make this part of the Configuration
		//TODO question: multiple entries for each language??
		List<Language> preferredLanguages = new ArrayList<>();
		preferredLanguages.add(Language.DEFAULT());

		if (textData.getFeature() == null){
			String message = "No feature available for text data ("+textData.getId()+"). Feature is required field. Taxon: " + this.getTaxonLogString(taxon);
	        state.getResult().addWarning(message);
		}
		record.setType(textData.getFeature());

		LanguageString languageText = textData.getPreferredLanguageString(preferredLanguages);
		if (languageText == null){
			String message = "No text in default language available for text data ("+textData.getId()+"). Text is required field. Taxon: " + this.getTaxonLogString(taxon);
	        state.getResult().addWarning(message);
		}else{
			record.setDescription(languageText.getText());
			record.setLanguage(languageText.getLanguage());
		}

		handleDescriptionElement(record, textData, config);
	}

    private void handleDescriptionElement(DwcaDescriptionRecord record,
            DescriptionElementBase descriptionElement,
            DwcaTaxExportConfigurator config) {

        //sources
        record.setSource(getSources(descriptionElement, config));

        //TODO missing , relationship to credits?
        record.setCreator(null);
        //TODO missing, relationship to credits?
        record.setContributor(null);
        //TODO missing
        record.setAudience(null);
        record.setLicense(descriptionElement.getInDescription().getRights());
        //TODO missing
        record.setRightsHolder(null);
    }

	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for " + this.ioName);
		return result;
	}


	@Override
	public boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoDescriptions();
	}

}
