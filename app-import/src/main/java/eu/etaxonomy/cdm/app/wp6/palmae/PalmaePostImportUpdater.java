// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.wp6.palmae;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @created 01.10.2009
 * @version 1.0
 */
public class PalmaePostImportUpdater {
	private static final Logger logger = Logger.getLogger(PalmaePostImportUpdater.class);

	static final ICdmDataSource cdmDestination = CdmDestinations.localH2Palmae();
	
	
	private String relationships = "relationships";
	private String taxonomicAccounts = "taxonomic accounts";
	private String fossilRecord = "fossil record";
	
	public boolean updateMissingFeatures(ICdmDataSource dataSource) {
		try{
			int count = 0;
			UUID featureTreeUuid = PalmaeActivator.featureTreeUuid;
			CdmApplicationController cdmApp = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.VALIDATE);
			
			TransactionStatus tx = cdmApp.startTransaction();
			
			FeatureTree tree = cdmApp.getFeatureTreeService().find(featureTreeUuid);
			FeatureNode root = tree.getRoot();
			
			List<DefinedTermBase> featureList = cdmApp.getTermService().list(Feature.class, null, null, null, null);
			for (DefinedTermBase feature : featureList){
				String label = feature.getLabel();
				if (relationships.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance((Feature)feature);
					root.addChild(newNode);
					count++;
				}else if(taxonomicAccounts.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance((Feature)feature);
					root.addChild(newNode);
					count++;
				}else if(fossilRecord.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance((Feature)feature);
					root.addChild(newNode);
					count++;
				}
			}
			cdmApp.commitTransaction(tx);
			if (count != 3){
				logger.warn("Did not find 3 additional features but " + count);
				return false;
			}
			logger.info("Feature tree updated!");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in feature tree update");
			return false;
		}
		
	}
	
	public boolean updateNameUsage(ICdmDataSource dataSource) {
		try{
			CdmApplicationController cdmApp = CdmApplicationController.NewInstance(dataSource, DbSchemaValidation.VALIDATE);

			TransactionStatus tx = cdmApp.startTransaction();

			int page = 0;
			int count = cdmApp.getTaxonService().count(Taxon.class);
			List<TaxonBase> taxonList = cdmApp.getTaxonService().list(Taxon.class, 100000, page, null, null);
			Taxon taxon;
			for (TaxonBase taxonBase : taxonList){
				if (taxonBase instanceof Taxon){
					taxon = (Taxon)taxonBase;
				if (((Taxon)taxon).getTaxonNodes().size() <1){
					TaxonNameBase name = taxon.getName();
					Set<Taxon> taxa = name.getTaxa();
					for(Taxon taxonCandidate: taxa ){
						if (taxonCandidate.getTaxonNodes().size() >0){
							addNameUsage(taxonCandidate, taxon);
							//delete
						}
					}
				}
				}
				//if nicht in treatment (isNameUsage)
				   //suche treatement taxon
				   
				//Delete from 
				
			}
			cdmApp.commitTransaction(tx);
			logger.info("NameUsage updated!");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in feature tree update");
			return false;
		}
		
	}
	
	
	/**
	 * @param taxonCandidate
	 * @param taxon
	 */
	private boolean addNameUsage(Taxon taxon, Taxon nameUsage) {
		TaxonDescription myDescription = null;
		for (TaxonDescription desc : taxon.getDescriptions()){
			if (! desc.isImageGallery()){
				myDescription = desc;
				break;
			}
		}
		if (myDescription == null){
			return false;
		}
		TextData textData = TextData.NewInstance(Feature.CITATION());
		textData.addSource(null, null, nameUsage.getSec(), null, nameUsage.getName(), nameUsage.getName().getTitleCache());
		myDescription.addElement(textData);
		return true;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PalmaePostImportUpdater updater = new PalmaePostImportUpdater();
		try {
			updater.updateMissingFeatures(cdmDestination);
			updater.updateNameUsage(cdmDestination);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in feature tree update");
		}
	}
}
