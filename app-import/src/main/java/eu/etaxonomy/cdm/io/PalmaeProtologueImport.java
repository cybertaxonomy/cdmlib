/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io;

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
	private static final Logger logger = Logger
			.getLogger(PalmaeProtologueImport.class);

	public static final String SPECIES = "Species";
	public static final String TAXONID = "TaxonID";
	public static final String ACC_OR_SYN = "AcceptedOrSynonym";
	public static final String AUTHOR = "Author";
	public static final String REFERENCE = "Reference";
	public static final String LINK_PROTO = "Link proto";
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.app.images.AbstractImageImporter#invokeImageImport(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean invokeImageImport(ImageImportConfigurator config) {
		
		ArrayList<HashMap<String, String>> contents;
		try {
			contents = ExcelUtils.parseXLS(config.getSource().toString());
		} catch (/*FileNotFound*/Exception e) {
			logger.error("FileNotFound: " + config.getSource().toString());
			return false;
		}
				
//		ICommonService commonService = getCommonService();
//		INameService nameService = getNameService();
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		
		int count = 0;
		
		for (HashMap<String, String> row : contents){
			count++;
			String species = row.get(PalmaeProtologueImport.SPECIES).trim();
			String taxonId = row.get(PalmaeProtologueImport.TAXONID);
			//String accOrSyn = row.get(PalmaeProtologueImport.ACC_OR_SYN).trim();
			//String author = row.get(PalmaeProtologueImport.AUTHOR).trim();			
			//String reference = row.get(PalmaeProtologueImport.REFERENCE).trim();
			String linkProto= row.get(PalmaeProtologueImport.LINK_PROTO).trim();
			
			//logger.info(species + ",  " + taxonId + ",  " + accOrSyn + ",  " + reference + ",  " + linkProto);
			
			TaxonNameBase taxonNameBase = (TaxonNameBase)getCommonService().getSourcedObjectByIdInSource(TaxonNameBase.class, "palm_tn_" + taxonId.replace(".0", ""), "TaxonName");
			
			if(taxonNameBase == null){
				logger.warn("no taxon with this name found: " + species + ", idInSource: " + taxonId);
			}else{
				
				MediaRepresentationPart representationPart = MediaRepresentationPart.NewInstance(linkProto, 0);
				MediaRepresentation representation = MediaRepresentation.NewInstance("text/html", null);
				representation.addRepresentationPart(representationPart);
				
				Media media = Media.NewInstance();
				media.addRepresentation(representation);
								
				TaxonNameDescription description = TaxonNameDescription.NewInstance();
				TextData protolog = TextData.NewInstance(Feature.PROTOLOG());
				protolog.addMedia(media);
				description.addElement(protolog);
				taxonNameBase.addDescription(description);
				
				taxonNameStore.add(taxonNameBase);
				if(count % 50 == 0){
					logger.info(count + " protologues processed.");
				}
			}
		}
		
		
		getNameService().saveTaxonNameAll(taxonNameStore);
		logger.info(count + " protologues imported to CDM store.");
		
		return true;
	}
	
}
