/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.app.cyprus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.app.common.CdmDestinations;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.CdmDefaultImport;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.CHECK;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.io.cyprus.CyprusImportConfigurator;
import eu.etaxonomy.cdm.io.cyprus.CyprusTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.FeatureNode;
import eu.etaxonomy.cdm.model.description.FeatureTree;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 16.12.2010
 * @version 1.0
 */
public class CyprusActivator {
	private static final Logger logger = Logger.getLogger(CyprusActivator.class);
	
	//database validation status (create, update, validate ...)
	static DbSchemaValidation hbm2dll = DbSchemaValidation.CREATE;
//	static final URI source = cyprus_distribution();
	static final URI source = cyprus_local();

	
	static final ICdmDataSource cdmDestination = CdmDestinations.localH2();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_local_postgres_CdmTest();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_test_local_mysql();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_dev();
//	static final ICdmDataSource cdmDestination = CdmDestinations.cdm_cyprus_production();

	
	//feature tree uuid
	public static final UUID featureTreeUuid = UUID.fromString("14d1e912-5ec2-4d10-878b-828788b70a87");
	
	//classification
	static final UUID classificationUuid = UUID.fromString("0c2b5d25-7b15-4401-8b51-dd4be0ee5cab");
	
	//check - import
	static final CHECK check = CHECK.IMPORT_WITHOUT_CHECK;
	
	//taxa
	static final boolean doTaxa = true;
	static final boolean doDeduplicate = false;
	static final boolean doDistribution = false;

	
	private void doImport(ICdmDataSource cdmDestination){
		
		//make Source
		CyprusImportConfigurator config= CyprusImportConfigurator.NewInstance(source, cdmDestination);
		config.setClassificationUuid(classificationUuid);
		config.setCheck(check);
		config.setDoDistribution(doDistribution);
		config.setDoTaxa(doTaxa);
		config.setDbSchemaValidation(hbm2dll);
		
		CdmDefaultImport myImport = new CdmDefaultImport();

		
		//...
		if (true){
			System.out.println("Start import from ("+ source.toString() + ") ...");
			config.setSourceReference(getSourceReference(config.getSourceReferenceTitle()));
			myImport.invoke(config);
			if (doTaxa){
				FeatureTree tree = makeFeatureNodes(myImport.getCdmAppController().getTermService());
				myImport.getCdmAppController().getFeatureTreeService().saveOrUpdate(tree);
			}

			System.out.println("End import from ("+ source.toString() + ")...");
		}
		
		
		
		//deduplicate
		if (doDeduplicate){
			ICdmApplicationConfiguration app = myImport.getCdmAppController();
			int count = app.getAgentService().deduplicate(Person.class, null, null);
			logger.warn("Deduplicated " + count + " persons.");
//			count = app.getAgentService().deduplicate(Team.class, null, null);
//			logger.warn("Deduplicated " + count + " teams.");
			count = app.getReferenceService().deduplicate(Reference.class, null, null);
			logger.warn("Deduplicated " + count + " references.");
		}
		
	}

	private Reference getSourceReference(String string) {
		Reference result = ReferenceFactory.newGeneric();
		result.setTitleCache(string);
		return result;
	}

	private FeatureTree makeFeatureNodes(ITermService service){
		CyprusTransformer transformer = new CyprusTransformer();
		
		FeatureTree result = FeatureTree.NewInstance(featureTreeUuid);
		result.setTitleCache("Cyprus Feature Tree");
		FeatureNode root = result.getRoot();
		FeatureNode newNode;

		newNode = FeatureNode.NewInstance(Feature.STATUS());
		root.addChild(newNode);
		
		newNode = FeatureNode.NewInstance(Feature.DISTRIBUTION());
		root.addChild(newNode);

		newNode = FeatureNode.NewInstance(Feature.SYSTEMATICS());
		root.addChild(newNode);

		//user defined features
		String [] featureList = new String[]{"Red Book", "Endemism"};
		addFeataureNodesByStringList(featureList, root, transformer, service);
		
		return result;
	}
	

	//Cyprus
	public static URI cyprus_local() {
		URI sourceUrl;
		try {
			sourceUrl = new URI("file:/C:/localCopy/Data/zypern/Zypern.xls");
			return sourceUrl;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	//Cyprus distriution
	public static URI cyprus_distribution() {
		URI sourceUrl;
		try {
			sourceUrl = new URI("file:/C:/localCopy/Data/zypern/Zypern_distribution_RH_corr.xls");
			return sourceUrl;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void addFeataureNodesByStringList(String[] featureStringList, FeatureNode root, IInputTransformer transformer, ITermService termService){
		try {
			for (String featureString : featureStringList){
			UUID featureUuid;
			featureUuid = transformer.getFeatureUuid(featureString);
			Feature feature = (Feature)termService.find(featureUuid);
			if (feature != null){
				FeatureNode child = FeatureNode.NewInstance(feature);
				root.addChild(child);	
			}
		}
		} catch (UndefinedTransformerMethodException e) {
			logger.error("getFeatureUuid is not implemented in transformer. Features could not be added");
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CyprusActivator me = new CyprusActivator();
		me.doImport(cdmDestination);
	}
	
}
