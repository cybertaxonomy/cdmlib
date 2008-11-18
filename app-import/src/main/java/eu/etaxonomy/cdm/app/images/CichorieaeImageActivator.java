/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.images;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author n.hoffmann
 * @created 18.11.2008
 * @version 1.0
 */
public class CichorieaeImageActivator extends AbstractImageImporter {
	private static Logger logger = Logger
			.getLogger(CichorieaeImageActivator.class);
	
	private static final File sourceFile = new File("src/main/resources/cich_images/images_cich.xls");
	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_cichorieae();
	
	static final UUID secUuid = UUID.fromString("6924c75d-e0d0-4a6d-afb7-3dd8c71195ca");

	private static final String URL = "URL";
	private static final String URL_APP = "URL_APP";
	private static final String NAME = "NAME";
	private static final String CODE = "CODE";
	
	protected boolean invokeImageImport (IImportConfigurator config){
		
		ArrayList<HashMap<String, String>> contents =  ExcelUtils.parseXLS(config.getSource().toString());
		
		for (HashMap<String, String> row : contents){
			
			String taxonName = row.get(CichorieaeImageActivator.NAME).trim();
			
			List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			
			
			if(taxa.size() == 0){
				logger.warn("no taxon with this name found: " + taxonName);
			}else if(taxa.size() > 1){
				logger.warn("multiple taxa with this name found: " + taxonName);
			}else{
				Taxon taxon = (Taxon) taxa.get(0);
				
				taxonService.saveTaxon(taxon);
				
				TextData feature = TextData.NewInstance();
				
				logger.info("Importing image for taxon: " + taxa);
				
				
				ImageMetaData imageMetaData = new ImageMetaData();
				
				
				try {
					URL url = new URL(row.get(CichorieaeImageActivator.URL).trim());
					
					imageMetaData.readFrom(url);
					
					ImageFile image = ImageFile.NewInstance(url.toString(), null, imageMetaData);
					
					MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
					representation.addRepresentationPart(image);
					
					Media media = Media.NewInstance();
					media.addRepresentation(representation);
					
					feature.addMedia(media);
					feature.putText(row.get(CichorieaeImageActivator.URL_APP).trim(), Language.ENGLISH());
					
					feature.setType(Feature.IMAGE());
					
					TaxonDescription description = TaxonDescription.NewInstance(taxon);
					
					description.addElement(feature);
					
				} catch (MalformedURLException e) {
					logger.error("Malformed URL", e);
				}
				
			}
		}
		return true;
		
	}
	
	public static void main (String[] cowabunga){
		ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(sourceFile, cdmDestination);
		imageConfigurator.setSecUuid(secUuid);
		
		AbstractImageImporter imageImporter = new CichorieaeImageActivator();
		imageImporter.invoke(imageConfigurator, null);
	}
}
