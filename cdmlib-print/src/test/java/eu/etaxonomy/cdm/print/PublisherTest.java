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

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.print.PublishConfigurator;
import eu.etaxonomy.cdm.print.Publisher;
import eu.etaxonomy.cdm.print.out.IPublishOutputModule;
import eu.etaxonomy.cdm.print.out.odf.OdfOutputModule;
import eu.etaxonomy.cdm.print.out.xml.XMLOutputModule;

/**
 * @author n.hoffmann
 * @created Apr 9, 2010
 * @version 1.0
 */
@Ignore // Implement this test in a more generic way
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
		
		// all nodes e9c61678-8624-48ad-8876-d3aa11a82db5
//		configurator.setSelectedTaxonNodeEntityUuids(Arrays.asList(new UUID[]{UUID.fromString("e9c61678-8624-48ad-8876-d3aa11a82db5")}));
		// lots of nodes
//		configurator.setSelectedTaxonNodeEntityUuids(Arrays.asList(new UUID[]{UUID.fromString("0f108191-4fd0-45fc-bbdb-bd468a82b7a4")}));
		// only a couple of nodes
//		configurator.setSelectedTaxonElements(Arrays.asList(new UUID[]{UUID.fromString("b1fffc62-952a-45eb-9c47-bb4534caf16e")}));
		
		configurator.setExportFolder(new File("/Users/nho/tmp/"));
		
		
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.print.Publisher#publish()}.
	 */
	@Test
	public void testPublishXml() {
		configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new XMLOutputModule()}));
		
		Publisher.publish(configurator);
	}
	
	@Test
	public void textPublishOdf() {
		configurator.setOutputModules(Arrays.asList(new IPublishOutputModule[]{new OdfOutputModule()}));
		
		Publisher.publish(configurator);
	}
}
