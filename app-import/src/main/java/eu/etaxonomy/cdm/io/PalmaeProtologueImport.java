/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.app.images.AbstractImageImporter;
import eu.etaxonomy.cdm.app.images.ImageImportConfigurator;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

/**
 * @author n.hoffmann
 * @created 19.11.2008
 * @version 1.0
 */
@Component
public class PalmaeProtologueImport extends AbstractImageImporter {
	private static final Logger logger = Logger.getLogger(PalmaeProtologueImport.class);

	public static final String SPECIES = "Species";
	public static final String TAXONID = "Taxon ID";
	public static final String LINK_PROTO = "Link proto";
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.app.images.AbstractImageImporter#invokeImageImport(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean invokeImageImport(ImageImportConfigurator config) {
		
		ArrayList<HashMap<String, String>> contents;
		try {
			contents = ExcelUtils.parseXLS(config.getSource());
		} catch (/*FileNotFound*/Exception e) {
			logger.error("FileNotFound: " + config.getSource().toString());
			return false;
		}
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		
		int count = 0;
		
		for (HashMap<String, String> row : contents){
			count++;
			
			TaxonNameBase taxonNameBase = null;
			String species = null;
			String taxonId = null;
			String linkProto = null;
			try{
				species = row.get(PalmaeProtologueImport.SPECIES).trim();
				taxonId = row.get(PalmaeProtologueImport.TAXONID);
				linkProto= row.get(PalmaeProtologueImport.LINK_PROTO).trim();
				taxonNameBase = (TaxonNameBase)getCommonService().getSourcedObjectByIdInSource(TaxonNameBase.class, "palm_tn_" + taxonId.replace(".0", ""), "TaxonName");
			}catch (Exception e){
				logger.error("The row has errors: rowNumber: " +count + ", content: "  + row, e);
			}
			
				
			
			if(taxonNameBase == null){
				logger.warn("no taxon with this name found: " + species + ", idInSource: " + taxonId);
			}else{
				
				URI uri;
				try {
					uri = new URI(linkProto);
					MediaRepresentationPart representationPart = MediaRepresentationPart.NewInstance(uri, 0);
					MediaRepresentation representation = MediaRepresentation.NewInstance("text/html", null);
					representation.addRepresentationPart(representationPart);
					
					Media media = Media.NewInstance();
					media.addRepresentation(representation);
								
					TaxonNameDescription description = TaxonNameDescription.NewInstance();
					TextData protolog = TextData.NewInstance(Feature.PROTOLOGUE());
					protolog.addMedia(media);
					description.addElement(protolog);
					taxonNameBase.addDescription(description);
				} catch (URISyntaxException e) {
					String message= "URISyntaxException when trying to convert: " + linkProto;
					logger.error(message);
					e.printStackTrace();
				}
				
				taxonNameStore.add(taxonNameBase);
				if(count % 50 == 0){
					logger.info(count + " protologues processed.");
				}
			}
		}
		
		
		getNameService().save(taxonNameStore);
		logger.info(count + " protologues imported to CDM store.");
		
		return true;
	}
	
}
