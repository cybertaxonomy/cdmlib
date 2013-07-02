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
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.app.wp6.palmae.config.PalmaeProtologueImportConfigurator;
import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.DefaultImportState;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
@Component
public class ProtologueImport extends CdmIoBase<DefaultImportState<PalmaeProtologueImportConfigurator>>  {
	private static final Logger logger = Logger.getLogger(ProtologueImport.class);

	private String pluralString = "protologues";
	private static int modCount = 200;

	public ProtologueImport(){
		super();
	}

	public void doInvoke(DefaultImportState<PalmaeProtologueImportConfigurator> state){
		logger.info("start make Protologues from files ...");
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		
		PalmaeProtologueImportConfigurator config = state.getConfig();
		File source = config.getSource();
		TaxonNameBase name;
		TransactionStatus txStatus = startTransaction(false);
		int count = 0;
		if (source.isDirectory()){
			for (File file : source.listFiles() ){
				if (file.isFile()){
					doCount(count++, modCount, pluralString);
					name = importFile(file, state);
					storeName(nameStore, name, state);
				}
			}
		}else{
			if (source.isFile()){
				name = importFile(source, state);
				storeName(nameStore, name, state);
			}
		}
		getNameService().save(nameStore);
		commitTransaction(txStatus);
		logger.info("end make Protologues from files ...");
		return;
	}
	
	private void storeName(Set<TaxonNameBase> nameStore, TaxonNameBase name, DefaultImportState<PalmaeProtologueImportConfigurator> state){
		if (name != null){
			nameStore.add(name);
			return;
		}else{
			state.setUnsuccessfull();
			return;
		}
	}
		
	private TaxonNameBase importFile(File file, DefaultImportState<PalmaeProtologueImportConfigurator> state){
		String originalSourceId = file.getName();
		originalSourceId =originalSourceId.replace("_P.pdf", "");
		originalSourceId =originalSourceId.replace("_tc_", "_tn_");
		String namespace = state.getConfig().getOriginalSourceTaxonNamespace();
		
		
		//for testing only
		TaxonNameBase taxonName = getTaxonName(originalSourceId, namespace);
		if (taxonName == null){
			logger.warn("Name not found for " + originalSourceId);
			return null;
		}
		
//		TaxonNameDescription nameDescription = null;
//		if (taxonName.getDescriptions().size() > 0){
//			nameDescription = (TaxonNameDescription)taxonName.getDescriptions().iterator().next();
//		}else{
//			nameDescription = new TaxonNameDescription();
//		}
		try{
			Media media = getMedia(state, file);
			if (media.getRepresentations().size() > 0){
				TaxonNameDescription description = getNameDescription(taxonName);
				TextData protolog = TextData.NewInstance(Feature.PROTOLOGUE());
				protolog.addMedia(media);
				description.addElement(protolog);
				return taxonName;
			}
			
		}catch(NullPointerException e){
			logger.warn("MediaUrl and/or MediaPath not set. Could not get protologue.");
			return null;
		}
		return null;
		
	}

	private TaxonNameDescription getNameDescription(TaxonNameBase taxonName) {
		TaxonNameDescription result;
		if (taxonName.getDescriptions().size()> 0){
			result = (TaxonNameDescription)taxonName.getDescriptions().iterator().next();
		}else{
			result = TaxonNameDescription.NewInstance();
			taxonName.addDescription(result);
		}
		
		return result;
	}
	
	private Media getMedia(DefaultImportState<PalmaeProtologueImportConfigurator> state, File file){
		try {
			//File file = (File)state.getConfig().getSource();
			String url = file.toURI().toURL().toString();
			String mimeTypePdf = "application/pdf"; 
			String suffixPdf = "pdf"; 
			String urlStringPdf = state.getConfig().getUrlString() + file.getName(); 
			URI uri = CdmUtils.string2Uri(urlStringPdf);
			Integer size = null;
			
			if (file.exists()){  
				Media media = Media.NewInstance();
			    
				MediaRepresentation representationPdf = MediaRepresentation.NewInstance(mimeTypePdf, suffixPdf); 
			    representationPdf.addRepresentationPart(MediaRepresentationPart.NewInstance(uri, size)); 
			    media.addRepresentation(representationPdf); 
			    return media;
			}else{
				return null;
			}
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			return null;
		}
		
 	}
	
	private TaxonNameBase getTaxonName(String originalSourceId, String namespace){
		TaxonNameBase result;
		ICommonService commonService = getCommonService();
		
		result = (TaxonNameBase)commonService.getSourcedObjectByIdInSource(TaxonNameBase.class, originalSourceId , namespace);
		if (result == null){
			logger.warn("Taxon (id: " + originalSourceId + ", namespace: " + namespace + ") could not be found");
		}
		return result;
	}
	
	
	public boolean doCheck(DefaultImportState state){
		boolean result = true;
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(DefaultImportState state){
		return false; // ! state.getConfig();
	}
	
	protected void doCount(int count, int modCount, String pluralString){
		if ((count % modCount ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}


}
