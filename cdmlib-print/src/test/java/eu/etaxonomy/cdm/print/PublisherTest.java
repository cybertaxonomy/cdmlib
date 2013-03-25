// $Id$
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
import java.util.Arrays;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.print.out.IPublishOutputModule;
import eu.etaxonomy.cdm.print.out.odf.OdfOutputModule;
import eu.etaxonomy.cdm.print.out.xml.XMLOutputModule;

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
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		configurator = PublishConfigurator.NewRemoteInstance();
		
		configurator.setWebserviceUrl("http://localhost:8080/");
		//configurator.setWebserviceUrl("http://160.45.63.201:8080/flora_central_africa/");
		//configurator.setWebserviceUrl("http://160.45.63.201/cdmserver/flora_central_africa/");
		//configurator.setWebserviceUrl("http://160.45.63.201/");
		//configurator.setWebserviceUrl("http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/");
		//http://160.45.63.201/dataportal/d7/flore-afrique-centrale/

		IXMLEntityFactory factory = configurator.getFactory();
		
		//http://160.45.63.201/cdmserver/flora_central_africa/taxonNode/a605e87e-113e-4ebd-ad97-f086b734b4da.json
		//http://dev.e-taxonomy.eu/cdmserver/flora_central_africa/taxonNode/a605e87e-113e-4ebd-ad97-f086b734b4da
		UUID taxonNodeUuid = UUID.fromString("a605e87e-113e-4ebd-ad97-f086b734b4da");//5168a18b-c0b1-44cc-80aa-7a5572fefe04
		Element taxonNodeElement = factory.getTaxonNode(taxonNodeUuid);
		configurator.addSelectedTaxonNodeElements(taxonNodeElement);
		configurator.setFeatureTree(UUID.fromString("051d35ee-22f1-42d8-be07-9e9bfec5bcf7"));
		//051d35ee-22f1-42d8-be07-9e9bfec5bcf7
//		Element selectedTaxonNodeElement = new Element("TaxonNode");
//		
//		configurator.addSelectedTaxonNodeElements(selectedTaxonNodeElement);
		
		//configurator.setExportFolder(new File("/Users/nho/tmp/"));
		configurator.setExportFolder(new File("/Users/l.morris/Documents"));
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.Publisher#publish()}.
	 */
	@Test
	public void testPublishXml() {
		configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new XMLOutputModule()}));
		
		Publisher.publish(configurator);
	}
	
	@Ignore
	public void textPublishOdf() {
		configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new OdfOutputModule()}));
		
		Publisher.publish(configurator);
	}
}
