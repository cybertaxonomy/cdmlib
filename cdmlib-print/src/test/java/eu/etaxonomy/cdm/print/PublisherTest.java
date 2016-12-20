/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.print;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Ignore;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;
import eu.etaxonomy.cdm.print.out.mediawiki.MediawikiOutputModule;
import eu.etaxonomy.cdm.print.out.odf.OdfOutputModule;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
//@Ignore // Implement this test in a more generic way
public class PublisherTest {
	private static final Logger logger = Logger.getLogger(PublisherTest.class);

	private static PublishConfigurator configurator; 
	
	private static Publisher publisher;
	
	private static IXMLEntityFactory factory;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		configurator = PublishConfigurator.NewRemoteInstance();
		
//		configurator.setWebserviceUrl("http://localhost:8080/");
		configurator.setWebserviceUrl("http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/");//central africa production
		//TODO
		//configurator.setWebserviceUrl("http://dev.e-taxonomy.eu/cdmserver/caryophyllales/");					
		//http://160.45.63.201/cdmserver/flora_central_africa
		//http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/

		factory = configurator.getFactory();
		
		//setClassification();
		setTaxonNode();
		
		//TODO: How do we get the uuid for the feature tree from the classification or taxonNode without hardcoding it?
		//configurator.setFeatureTree(UUID.fromString("ac8d4e58-926d-4f81-ac77-cebdd295df7c"));//caryophyllales		
		//configurator.setFeatureTree(UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7"));//Ericaceae
		configurator.setFeatureTree(UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7"));
		//configurator.setFeatureTree(UUID.fromString("ae9615b8-bc60-4ed0-ad96-897f9226d568"));//ae9615b8-bc60-4ed0-ad96-897f9226d568 cichorieae
		
//		Element selectedTaxonNodeElement = new Element("TaxonNode");
//		configurator.addSelectedTaxonNodeElements(selectedTaxonNodeElement);		
		configurator.setExportFolder(new File("/home/sybille/tmp/"));
//		configurator.setExportFolder(new File("/Users/l.morris/Documents")); //TODO: use a relative path
	}
	
	private static void setTaxonNode() {
		
		//http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/taxonNode/a605e87e-113e-4ebd-ad97-f086b734b4da
		//a95f7122-87c9-478b-a1e6-d9199d855356 agarista
		//taxonNodeUuid a605e87e-113e-4ebd-ad97-f086b734b4da FeatureTree UUID 051d35ee-22f1-42d8-be07-9e9bfec5bcf7
		//UUID taxonNodeUuid = UUID.fromString("be6566eb-4661-41fe-8ec2-caf885a12cbd");//5168a18b-c0b1-44cc-80aa-7a5572fefe04
		//UUID taxonNodeUuid = UUID.fromString("a605e87e-113e-4ebd-ad97-f086b734b4da");//Ericaceae
		UUID taxonNodeUuid = UUID.fromString("0044aae4-721b-4726-85ff-752a89cff748");//restionaceae
		//UUID taxonNodeUuid = UUID.fromString("6a7ac1ad-2fd9-4218-8132-12dd463d04b9");// cf74ab02-2385-4818-9d56-902fa3e6fe6b 4f6c68d0-63f2-4235-83c2-5bff14532f90 cichorieae.
		//restionaceae 0044aae4-721b-4726-85ff-752a89cff748
		//UUID taxonNodeUuid = UUID.fromString("9440bd28-b462-4112-8906-a643b7d3f195");//caryophyllales
		Element taxonNodeElement = factory.getTaxonNode(taxonNodeUuid);
		configurator.addSelectedTaxonNodeElements(taxonNodeElement);		
		
	}
	
	/*
	 * Adds all TaxonNodes from a classification to the PublishConfigurator. Only tested with the smaller database Caryophyllales. 
	 * To run the Publisher on a single TaxonNode e.g. family or genus, call setTaxonNode instead.
	 */
	private static void setClassification() {

		List<Element> classifications = configurator.getFactory().getClassifications();

		for(Element child : classifications){
			List<Element> children = child.getChildren();

			List<Element> elements = configurator.getFactory().getChildNodes(child);

			//for(Element child2 : children){
			//logger.warn("The element name is " + child2.getName() + " and value is " + child2.getValue());
			// 1. get the value where the child2.getName is uuid
			//}
			// 2. http://dev.e-taxonomy.eu/cdmserver/caryophyllales/portal/classification/9edc58b5-de3b-43aa-9f31-1ede7c009c2b/childNodes
			// 3. get the taxonNode uuid from the classification
			int count = 0;
			
			for(Element child2 : elements){
				logger.warn("2 The element name is " + child2.getName() + " and value is " + child2.getValue());
				// 1. get the value where the child2.getName is uuid

				logger.warn("3 The uuid is " + child2.getChildText("uuid")); 
				logger.warn("4 The uuid is " + child2.getChild("uuid")); 
				logger.warn("5 The count is " + count); 
				
				//filter out Fabales in FoCE - as it's huge and seems to cause the harvetsing to crash.
				if (child2.getChildText("uuid") == "94bb5507-201d-4e34-9aa7-dce58fcd6a25") {
					break;
				}
				//temporarily filter c15e12c1-6118-4929-aed0-b0cc90f5ab22 as it's causing a lazyInitializationException in caryophyllales
				if (child2.getChildText("uuid") == "c15e12c1-6118-4929-aed0-b0cc90f5ab22") {
					break;
				}

				UUID taxonNodeUuid = UUID.fromString(child2.getChildText("uuid"));
				Element taxonNodeElement = factory.getTaxonNode(taxonNodeUuid);
				configurator.addSelectedTaxonNodeElements(taxonNodeElement);
				count++;
			}
		}
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.Publisher#publish()}.
	 */
//	@Ignore
	@Ignore
	public void testPublishXml() {
		
		List<IPublishOutputModule> modules = new ArrayList();
		//modules.add(new XMLOutputModule());
		modules.add(new MediawikiOutputModule(""));
		configurator.setOutputModules(modules);
		
		//configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new XMLOutputModule()}));
		logger.warn("The number of selected taxon node elements is........ " + configurator.getSelectedTaxonNodeElements().size());
		
		Publisher.publish(configurator);
	}
	
	@Ignore
	public void textPublishOdf() {
		configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new OdfOutputModule()}));
		
		Publisher.publish(configurator);
	}
}
