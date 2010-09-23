/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
@Component
public class CichorieaeImageImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(CichorieaeImageImport.class);
	
	
	/** 
	 * Imports images from a directory.
	 */
	protected boolean invokeImageImport (ImageImportConfigurator config){
		File source = new File(config.getSource());
		UUID treeUuid = config.getTaxonomicTreeUuid();
		TaxonomicTree tree = taxonTreeService.getTaxonomicTreeByUuid(treeUuid);
		ReferenceBase sourceRef = config.getSourceReference();
		
		if (source.isDirectory()){
			for (File file : source.listFiles() ){
				if (file.isFile()){
					String fileName = file.getName();
					String taxonName = getTaxonName(fileName);
					if (taxonName == null){
						continue;
					}
					List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
					if(taxa.size() == 0){
						logger.warn("no taxon with this name found: " + taxonName);
					} else {
						handleTaxa(tree, sourceRef, fileName, taxonName, taxa);
					}
				}else{
					logger.warn("File is not a file (but a directory?): " + file.getName());
				}
			}	
		}else{
			logger.warn("Source is not a directory!" + source.toString());
		}
	
		return true;
		
	}
	
	private String getTaxonName(String fileName){
		String[] fileNameParts = fileName.split("\\.");
		if (fileNameParts.length < 2){
			logger.warn("No file extension found for: " +  fileName);
			return null;
		}
		String extension = fileNameParts[fileNameParts.length - 1];
		if (! "jpg".equalsIgnoreCase(extension)) { 
			logger.warn("Extension not recognized: " + extension);
			// Sometimes occurs here "Thumbs.db"
			return null;
		}
		String firstPart = fileName.substring(0, fileName.length() - extension.length() - 1);
		logger.info(firstPart);
		String[] nameParts = firstPart.split("_");
		if (nameParts.length < 3){
			logger.warn("name string has less than 2 '_'");
			return null;
		}
		
		String featureString = nameParts[nameParts.length-2];
		logger.debug("FeatureString: " +  featureString);
		String detailString = nameParts[nameParts.length-1];
		logger.debug("detailString: " +  detailString);
		
		String taxonName = "";
		for (int i= 0; i < nameParts.length-2; i++){
			taxonName += nameParts[i] + " ";
		}
		taxonName = taxonName.trim();
		logger.info("Taxon name: " +  taxonName);
		
		String _s_ = " s ";
		String subsp = " subsp. ";
		if (taxonName.contains(_s_)) {
			taxonName = taxonName.replace(_s_, subsp);
			logger.info("Taxon name: " +  taxonName);
		}
		return taxonName;
	}


	/**
	 * @param tree
	 * @param sourceRef
	 * @param name
	 * @param taxonName
	 * @param taxa
	 * @param taxon
	 */
	private void handleTaxa(TaxonomicTree tree, ReferenceBase sourceRef, String fileName, String taxonName, List<TaxonBase> taxa) {
		
		Taxon taxon = getTaxon(tree, taxonName, taxa);
		TaxonDescription imageGallery = taxon.getOrCreateImageGallery(sourceRef == null ? null :sourceRef.getTitleCache());
		TextData textData = imageGallery.getOrCreateImageTextData();
		logger.info("Importing image for taxon: " + taxa);
		try {
			Media media = getMedia(fileName, taxonName);
			textData.addMedia(media);
		} catch (MalformedURLException e) {
			logger.error("Malformed URL", e);
		} catch (IOException e) {
			logger.error("IOException when handling image url");
		}
	}


	/**
	 * @param fileName
	 * @param taxonName 
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException 
	 */
	private Media getMedia(String fileName, String taxonName) throws MalformedURLException, IOException {
		String urlPrefix = "http://media.bgbm.org/erez/erez?src=EditWP6/photos/";
		String urlString = urlPrefix + fileName;
		logger.info(urlString);
		URL url = new URL(urlString);
		URI uri = CdmUtils.string2Uri(urlString);
		ImageMetaData imageMetaData =ImageMetaData.newInstance();
		imageMetaData.readImageInfo(uri, 0);
		
		//String uri = url.toString();
		
		String uriString = url.toString();
		String mimeType = imageMetaData.getMimeType();
		String suffix = null;
		int height = imageMetaData.getHeight();
		int width = imageMetaData.getWidth();
		Integer size = null;
		DateTime mediaCreated = null;
		AgentBase artist = null;
		
		 
		ImageFile image = ImageFile.NewInstance(uriString, size, height, width);
		Media media = ImageFile.NewMediaInstance(mediaCreated, artist, uriString, mimeType, suffix, size, height, width);
		media.addTitle(LanguageString.NewInstance(taxonName, Language.LATIN()));
		
		return media;
	}

	/**
	 * @param tree
	 * @param taxonName
	 * @param taxa
	 * @return
	 */
	private Taxon getTaxon(TaxonomicTree tree, String taxonName,
			List<TaxonBase> taxa) {
		Taxon taxon = null;
		if(taxa.size() > 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("multiple taxa with this name found: " + taxonName);
			}
			for (TaxonBase taxonBase : taxa) {
				Taxon tax = (Taxon)taxonBase;
				if (tree.isTaxonInTree(tax)) {
					taxon = tax;
					break;
				}
			}
			if (taxon == null){
				taxon = (Taxon)taxa.get(0);
				logger.warn("Taxon not found in preferred tree. Use " + taxon.getTitleCache() + " instead.");
			}

		} else {
			taxon = (Taxon) taxa.get(0);
		}
		if (taxon != null){
			taxonService.saveOrUpdate(taxon);
		}else{
			logger.warn("Taxon was null. Did not save taxon");
		}
		return taxon;
	}
}
