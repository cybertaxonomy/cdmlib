/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import org.junit.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 \* @since 02.08.2011
 *
 */
public class NameTypeParserTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		DefaultTermInitializer initializer = new DefaultTermInitializer();
		initializer.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.strategy.parser.NameTypeParser#makeNameTypeStatus(java.lang.String)}.
	 */
	@Test
	public void testMakeNameTypeStatus() {
		//orig dest
		String typeString = "original designation";
		NameTypeDesignationStatus type;
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be original designation", NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}	
		typeString = "original desig.";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be original designation", NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		typeString = "ORIginAL DESig.";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be original designation", NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}

		//present desig
		typeString = "present designation";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be present designation", NameTypeDesignationStatus.PRESENT_DESIGNATION(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//subsequent desig
		typeString = "subsequent designation";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be subsequent designation", NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//monotypy
		typeString = "monotypy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be 'monotypy'", NameTypeDesignationStatus.MONOTYPY(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//subs. monotypy
		typeString = "subsequent monotypy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be 'subsequent monotypy'", NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}

		//tautonomy
		typeString = "tautonomy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be tautonomy", NameTypeDesignationStatus.TAUTONYMY(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//lectotype
		typeString = "lectotype";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be lectotype", NameTypeDesignationStatus.LECTOTYPE(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//automatic
		typeString = "automatic";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be automatic", NameTypeDesignationStatus.AUTOMATIC(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}
		//automatic
		typeString = "not applicable";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			Assert.assertEquals("Type should be 'not applicable'", NameTypeDesignationStatus.NOT_APPLICABLE(), type);
		} catch (UnknownCdmTypeException e) {
			Assert.fail(typeString + " not recognized");
		}



	}
}
