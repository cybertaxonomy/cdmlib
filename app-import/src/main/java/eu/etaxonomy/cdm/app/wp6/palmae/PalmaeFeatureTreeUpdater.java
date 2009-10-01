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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * @author a.mueller
 * @created 01.10.2009
 * @version 1.0
 */
public class PalmaeFeatureTreeUpdater {
	private static final Logger logger = Logger.getLogger(PalmaeFeatureTreeUpdater.class);

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
			
			FeatureTree tree = cdmApp.getDescriptionService().getFeatureTreeByUuid(featureTreeUuid);
			FeatureNode root = tree.getRoot();
			
			List<Feature> featureList = cdmApp.getDescriptionService().getFeaturesAll();
			for (Feature feature : featureList){
				String label = feature.getLabel();
				if (relationships.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance(feature);
					root.addChild(newNode);
					count++;
				}else if(taxonomicAccounts.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance(feature);
					root.addChild(newNode);
					count++;
				}else if(fossilRecord.equals(label)){
					FeatureNode newNode = FeatureNode.NewInstance(feature);
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
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PalmaeFeatureTreeUpdater updater = new PalmaeFeatureTreeUpdater();
		try {
			updater.updateMissingFeatures(cdmDestination);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ERROR in feature tree update");
		}
	}
}
