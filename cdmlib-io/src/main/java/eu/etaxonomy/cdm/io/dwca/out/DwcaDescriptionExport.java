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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 20.04.2011
 */
@Component
public class DwcaDescriptionExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaDescriptionExport.class);

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
	protected boolean doInvoke(DwcaTaxExportState state){
		DwcaTaxExportConfigurator config = state.getConfig();
		String fileName = config.getDestinationNameString();
		TransactionStatus txStatus = startTransaction(true);

		try {
			
			final String coreTaxFileName = "description.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			List<TaxonNode> allNodes =  getAllNodes(null);
			for (TaxonNode node : allNodes){
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<TaxonDescription> descriptions = taxon.getDescriptions();
				for (TaxonDescription description : descriptions){
					for (DescriptionElementBase el : description.getElements()){
						if (el.isInstanceOf(TextData.class)){
							DwcaDescriptionRecord record = new DwcaDescriptionRecord();
							TextData textData = CdmBase.deproxy(el,TextData.class);
							handleDescription(record, textData, taxon);
							record.write(writer);
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
		}
		commitTransaction(txStatus);
		return true;
	}
	



	private void handleDescription(DwcaDescriptionRecord record, TextData textData, Taxon taxon) {
		record.setCoreid(taxon.getId());
		Language.DEFAULT();
		//TODO make this part of the Configuration
		
		
		//TODO question: multiple entries for each language??
		List<Language> preferredLanguages = new ArrayList<Language>();
		preferredLanguages.add(Language.DEFAULT());
		LanguageString languageText = textData.getPreferredLanguageString(preferredLanguages);
		
		
		record.setType(textData.getFeature());
		
		if (languageText == null){
			String message = "No text in default language available for text data ("+textData.getId()+"), Taxon: " + taxon.getTitleCache() + "," + taxon.getId();
			logger.warn(message);
		}else{
			record.setDescription(languageText.getText());
			record.setLanguage(languageText.getLanguage());
		}
		
		//FIXME multiple sources
		record.setSource(null);
		
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
