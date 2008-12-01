/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.app.common;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;

/**
 * This is simple Tool to write FeatureTrees to a CDM Store. 
 * 
 * @author n.hoffmann
 * @created 28.11.2008
 * @version 1.0
 *
 */
public class FeatureTreeCreator {

	private static Logger logger = Logger.getLogger(FeatureTreeCreator.class);
	
	public static void main(String[] yesWeCan) {

		// make BerlinModel Source
		ICdmDataSource destination = CdmDestinations.cdm_edit_palmae();
		UUID featureTreeUuid = UUID.fromString("cf6e34b7-6830-4db7-ae85-3c85df027174");

		TcsImportConfigurator tcsImportConfigurator = TcsImportConfigurator
				.NewInstance(null, destination);
		
		

		// make feature tree
		logger.info("Create new FeatureTree in: " + destination.toString()
				+ " ...");

		CdmApplicationController app = tcsImportConfigurator
				.getCdmAppController();

		Feature[] features = {
				Feature.DIAGNOSIS(),
				Feature.DESCRIPTION(),
				Feature.DISTRIBUTION(),
				Feature.BIOLOGY_ECOLOGY(),
				Feature.CONSERVATION(),
				Feature.ETYMOLOGY(),
				Feature.COMMON_NAME(),
				Feature.USES(),
				Feature.CULTIVATION(),
				Feature.DISCUSSION(),
				Feature.MATERIALS_EXAMINED(),
				Feature.PROTOLOG()
		};
		
		
		FeatureTree tree = getFeatureTree(featureTreeUuid, features);

		app.getDescriptionService().saveFeatureTree(tree);

	}

	private static FeatureTree getFeatureTree(UUID featureTreeUuid, Feature[] features) {

		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		FeatureNode root = result.getRoot();

		for (Feature feature : features){
			root.addChild(FeatureNode.NewInstance(feature));
		}

		return result;

	}

}
