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
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.media.IdentifiableMediaEntity;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 * @created 18.04.2011
 */
@Component
public class DwcaImagesExport extends DwcaExportBase {
	private static final Logger logger = Logger.getLogger(DwcaImagesExport.class);

	/**
	 * Constructor
	 */
	public DwcaImagesExport() {
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
			
			final String coreTaxFileName = "images.txt";
			fileName = fileName + File.separatorChar + coreTaxFileName;
			File f = new File(fileName);
			if (!f.exists()){
				f.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(f);
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos, "UTF8"), true);

			
			
			List<TaxonNode> allNodes =  getClassificationService().getAllNodes();
			for (TaxonNode node : allNodes){
				DwcaImagesRecord record = new DwcaImagesRecord();
				Taxon taxon = CdmBase.deproxy(node.getTaxon(), Taxon.class);
				Set<? extends DescriptionBase> descriptions = taxon.getDescriptions();
				for (DescriptionBase description : descriptions){
					for (Object o : description.getElements()){
						DescriptionElementBase el = CdmBase.deproxy(o, DescriptionElementBase.class);
						if (el.getMedia().size() > 0){
							for (Media media: el.getMedia()){
								for (MediaRepresentation repr : media.getRepresentations()){
									for (MediaRepresentationPart part : repr.getParts()){
										handleMedia(record, media, repr, part, taxon);
									}
								}
							}
						}
					}
				}
				
				record.write(writer);
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
	



	private void handleMedia(DwcaImagesRecord record, Media media, MediaRepresentation repr, MediaRepresentationPart part, Taxon taxon) {
		record.setCoreid(taxon.getId());
		record.setIdentifier(part.getUri());
		record.setTitle(media.getTitleCache());
		//TODO description if default language description is not available
		record.setDescription(media.getDescription(Language.DEFAULT()).getText());
		//TODO missing
		record.setSpatial(null);
		//TODO missing
		record.setCoordinates(null);
		record.setFormat(repr.getMimeType());
		//FIXME missing ??
		record.setLicense(media.getRights());
		record.setCreated(media.getMediaCreated());
		record.setCreator(media.getArtist());
		//TODO missing
		record.setContributor(null);
		//TODO missing
		record.setPublisher(null);
		//TODO missing
		record.setAudience(null);
	}
	
	@Override
	protected boolean doCheck(DwcaTaxExportState state) {
		boolean result = true;
		logger.warn("No check implemented for Jaxb export");
		return result;
	}


	@Override
	protected boolean isIgnore(DwcaTaxExportState state) {
		return false;
	}
	
}
