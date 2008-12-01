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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.common.ExcelUtils;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
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
public class PalmaeProtologueActivator extends AbstractImageImporter {
	private static Logger logger = Logger
			.getLogger(PalmaeProtologueActivator.class);

	private static final File sourceFile = new File("src/main/resources/images/protologue_links_palmae.xls");
	private static final ICdmDataSource cdmDestination = CdmDestinations.cdm_edit_palmae();

	static final UUID secUuid = UUID.fromString("5f32b8af-0c97-48ac-8d33-6099ed68c625");

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
	protected boolean invokeImageImport(IImportConfigurator config) {
		
		ArrayList<HashMap<String, String>> contents =  ExcelUtils.parseXLS(config.getSource().toString());
				
		INameService nameService = config.getCdmAppController().getNameService();
		ICommonService commonService = config.getCdmAppController().getCommonService();
		
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		
		int modCount = 0;
		
		for (HashMap<String, String> row : contents){
			
			String species = row.get(PalmaeProtologueActivator.SPECIES).trim();
			String taxonId = row.get(PalmaeProtologueActivator.TAXONID);
			//String accOrSyn = row.get(PalmaeProtologueActivator.ACC_OR_SYN).trim();
			//String author = row.get(PalmaeProtologueActivator.AUTHOR).trim();			
			//String reference = row.get(PalmaeProtologueActivator.REFERENCE).trim();
			String linkProto= row.get(PalmaeProtologueActivator.LINK_PROTO).trim();
			
			//logger.info(species + ",  " + taxonId + ",  " + accOrSyn + ",  " + reference + ",  " + linkProto);
			
			TaxonNameBase taxonNameBase = (TaxonNameBase)commonService.getSourcedObjectByIdInSource(TaxonNameBase.class, "palm_tn_" + taxonId.replace(".0", ""), "TaxonName");
			
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
				if(modCount % 50 == 0){
					logger.info(modCount + " protologues processed.");
				}
			}
			modCount++;
		}
		
		
		nameService.saveTaxonNameAll(taxonNameStore);
		logger.info(modCount + " protologues imported to CDM store.");
		
		return true;
	}
	
	public static void main (String[] whatever){
		ImageImportConfigurator imageConfigurator = ImageImportConfigurator.NewInstance(sourceFile, cdmDestination);
		imageConfigurator.setSecUuid(secUuid);
		
		AbstractImageImporter imageImporter = new PalmaeProtologueActivator();
		imageImporter.invoke(imageConfigurator, null);
	}
	
}
