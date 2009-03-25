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

import eu.etaxonomy.cdm.common.MediaMetaData.ImageMetaData;

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
	
	/********************* TESTS ********************/
	
	@Test
	public void readImageInfoFromFile() {
		File imageFile = new File("./src/test/resources/images/OregonScientificDS6639-DSC_0307-small.jpg");
		ImageMetaData imageMetaData = new ImageMetaData();
		imageMetaData = MediaMetaData.readImageMetaData(imageFile, imageMetaData);
		
		assertImageInfo(imageMetaData);		
	}
	
	@Ignore
	public void readImageInfoFromUrl() {
		try {
			
			//TODO make ready for windows
			URL imageUrl = new URL("file://" + new File("").getAbsolutePath()+ "/src/test/resources/images/OregonScientificDS6639-DSC_0307-small.jpg");
			
			ImageMetaData imageMetaData = new ImageMetaData();
			imageMetaData = MediaMetaData.readImageMetaData(imageUrl, imageMetaData);
			
			assertImageInfo(imageMetaData);		
			
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
}
