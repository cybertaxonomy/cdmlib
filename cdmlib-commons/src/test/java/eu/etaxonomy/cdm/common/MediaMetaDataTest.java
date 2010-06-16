// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.common;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.common.mediaMetaData.MediaMetaData;
import eu.etaxonomy.cdm.common.mediaMetaData.ImageMetaData;
import eu.etaxonomy.cdm.common.mediaMetaData.MetaDataFactory;
import eu.etaxonomy.cdm.common.mediaMetaData.MimeType;

/**
 * @author n.hoffmann
 * @created 13.11.2008
 * @version 1.0
 */
public class MediaMetaDataTest {
	private static final Logger logger = Logger.getLogger(MediaMetaDataTest.class);

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (logger.isDebugEnabled()){logger.debug("setUpBeforeClass");}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	/********************* TESTS 
	 * @throws IOException ********************/
	
	@Test
	public void readImageInfoFromFile() throws IOException {
		File imageFile = new File("./src/test/resources/images/OregonScientificDS6639-DSC_0307-small.jpg");
		MetaDataFactory metaFactory = MetaDataFactory.getInstance();
		ImageMetaData imageMetaData = (ImageMetaData) metaFactory.readMediaData(imageFile.toURI(), MimeType.JPEG, 0);
		//imageMetaData.readFrom(imageFile);
		
		assertImageInfo(imageMetaData);		
		imageFile = new File("./src/test/resources/images/OregonScientificDS6639-DSC_0307-small.tif");
		
		 imageMetaData = (ImageMetaData) metaFactory.readMediaData(imageFile.toURI(), MimeType.IMAGE, 0);
		 assertTiffInfo(imageMetaData);		
	}
	
	@Test
	public void readImageInfoFromUrl() throws IOException {
		try {
			
			//TODO make ready for windows
			//URL imageUrl = new URL("file://" + new File("").getAbsolutePath()+ "/src/test/resources/images/OregonScientificDS6639-DSC_0307-small.jpg");
			URL imageUrl = new URL("http://wp5.e-taxonomy.eu/media/palmae/photos/palm_tc_100447_6.jpg");
			MetaDataFactory metaFactory = MetaDataFactory.getInstance();
			ImageMetaData imageMetaData = (ImageMetaData) metaFactory.readMediaData(CdmUtils.string2Uri(imageUrl.toString()), MimeType.JPEG, 30000);
			//imageMetaData.readImageMetaData(imageUrl);
			
			Assert.assertNotNull(imageMetaData);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private void assertImageInfo (ImageMetaData imageMetaData){
		Assert.assertEquals(24, imageMetaData.getBitPerPixel());
		Assert.assertEquals("JPEG (Joint Photographic Experts Group) Format", imageMetaData.getFormatName());
		Assert.assertEquals(300, imageMetaData.getWidth());
		Assert.assertEquals(225, imageMetaData.getHeight());
		Assert.assertEquals("image/jpeg", imageMetaData.getMimeType());
		
	}
	
	private void assertTiffInfo (ImageMetaData imageMetaData){
		
		Assert.assertEquals(24, imageMetaData.getBitPerPixel());
		Assert.assertEquals("TIFF Tag-based Image File Format", imageMetaData.getFormatName());
		Assert.assertEquals(300, imageMetaData.getWidth());
		Assert.assertEquals(225, imageMetaData.getHeight());
		Assert.assertEquals("image/tiff", imageMetaData.getMimeType());
	}
}
