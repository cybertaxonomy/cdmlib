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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaDescriptionExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaDescriptionExport.class);

	private static final String ROW_TYPE = "http://rs.gbif.org/terms/1.0/Description";
	private static final String fileName = "description.txt";

	/**
	 * Constructor
	 */
	public DwcaDescriptionExport() {
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
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(TextData.class) ){
							Feature feature = el.getFeature();
							if (feature != null &&
									! feature.equals(Feature.IMAGE()) &&
									! config.getFeatureExclusions().contains(feature.getUuid()) &&
									! recordExists(el)){
								DwcaDescriptionRecord record = new DwcaDescriptionRecord(metaRecord, config);
								TextData textData = CdmBase.deproxy(el,TextData.class);
								handleDescription(record, textData, taxon, config);
								record.write(writer);
								addExistingRecord(textData);
							}
						}
					}
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
		} finally {
			closeWriter(writer, state);
		}
		commitTransaction(txStatus);
		return;
	}


	private void handleDescription(DwcaDescriptionRecord record, TextData textData, Taxon taxon, DwcaTaxExportConfigurator config) {
		record.setId(taxon.getId());
		record.setUuid(taxon.getUuid());

		//TODO make this part of the Configuration
		//TODO question: multiple entries for each language??
		List<Language> preferredLanguages = new ArrayList<Language>();
		preferredLanguages.add(Language.DEFAULT());
		LanguageString languageText = textData.getPreferredLanguageString(preferredLanguages);


		if (textData.getFeature() == null){
			String message = "No feature available for text data ("+textData.getId()+"). Feature is required field. Taxon: " + this.getTaxonLogString(taxon);
			logger.warn(message);
		}
		record.setType(textData.getFeature());

		if (languageText == null){
			String message = "No text in default language available for text data ("+textData.getId()+"). Text is required field. Taxon: " + this.getTaxonLogString(taxon);
			logger.warn(message);
		}else{
			record.setDescription(languageText.getText());
			record.setLanguage(languageText.getLanguage());
		}

		//sources
		record.setSource(getSources(textData, config));

		//TODO missing , relationship to credits?
		record.setCreator(null);
		//TODO missing, relationship to credits?
		record.setContributor(null);
		//TODO missing
		record.setAudience(null);
		record.setLicense(textData.getInDescription().getRights());
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
	protected boolean isIgnore(DwcaTaxExportState state) {
		return ! state.getConfig().isDoDescription();
	}

}
