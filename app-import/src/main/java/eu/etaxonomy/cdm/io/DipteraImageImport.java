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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.common.ImageMetadata.Item;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.babadshanjan
 * @created 27.04.2009
 * @version 1.0
 */
@Component
public class DipteraImageImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(DipteraImageImport.class);

	private static final String URL = "URL";
	private static final String NAME = "NAME";

	/** 
	 * Imports images from an Excel file.
	 */
	protected boolean invokeImageImport (ImageImportConfigurator config){

		ArrayList<HashMap<String, String>> contents;
		try {
			contents = ExcelUtils.parseXLS(config.getSource().toString());
		} catch (FileNotFoundException e1) {
			logger.error("FileNotFound: " + config.getSource().toString());
			return false;
		}

		for (HashMap<String, String> row : contents){

			String taxonName = row.get(DipteraImageImport.NAME).trim();

			List<TaxonBase> taxa = taxonService.searchTaxaByName(taxonName, config.getSourceReference());			

			if(taxa.size() == 0){
				logger.warn("no taxon with this name found: " + taxonName);
				break;
			}else if(taxa.size() > 1){
				logger.warn("multiple taxa with this name found: " + taxonName);
			}
//			}else{
				Taxon taxon = (Taxon) taxa.get(0);

				taxonService.saveTaxon(taxon);

				TextData feature = TextData.NewInstance();

				logger.info("Importing image for taxon: " + taxa);


				ImageMetaData imageMetaData = new ImageMetaData();


				try {
					URL url = new URL(row.get(DipteraImageImport.URL).trim());

					imageMetaData.readFrom(url);

					ImageFile image = ImageFile.NewInstance(url.toString(), null, imageMetaData);

					MediaRepresentation representation = MediaRepresentation.NewInstance(imageMetaData.getMimeType(), null);
					representation.addRepresentationPart(image);

					Media media = Media.NewInstance();
					media.addRepresentation(representation);

					feature.addMedia(media);

					feature.setType(Feature.IMAGE());

					TaxonDescription description = TaxonDescription.NewInstance(taxon);

					description.addElement(feature);

				} catch (MalformedURLException e) {
					logger.error("Malformed URL", e);
				}

			}
//		}
		return true;

	}

}
