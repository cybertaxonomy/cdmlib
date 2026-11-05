/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.test.TermTestBase;

/**
 * @author a.mueller
 * @since 02.08.2011
 */
public class NameTypeParserTest extends TermTestBase {

    @Test
	public void testMakeNameTypeStatus() {
		//orig dest
		String typeString = "original designation";
		NameTypeDesignationStatus type;
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type, "Type should be original designation");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		typeString = "original desig.";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type, "Type should be original designation");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		typeString = "ORIginAL DESig.";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.ORIGINAL_DESIGNATION(), type, "Type should be original designation");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}

		//present desig
		typeString = "present designation";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.PRESENT_DESIGNATION(), type, "Type should be present designation");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//subsequent desig
		typeString = "subsequent designation";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION(), type, "Type should be subsequent designation");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//monotypy
		typeString = "monotypy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.MONOTYPY(), type, "Type should be 'monotypy'");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//subs. monotypy
		typeString = "subsequent monotypy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY(), type, "Type should be 'subsequent monotypy'");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}

		//tautonomy
		typeString = "tautonomy";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.TAUTONYMY(), type, "Type should be tautonomy");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//lectotype
		typeString = "lectotype";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.LECTOTYPE(), type, "Type should be lectotype");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//automatic
		typeString = "automatic";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.AUTOMATIC(), type, "Type should be automatic");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
		//automatic
		typeString = "not applicable";
		try {
			type = NameTypeParser.parseNameTypeStatus(typeString);
			assertEquals(NameTypeDesignationStatus.NOT_APPLICABLE(), type, "Type should be 'not applicable'");
		} catch (UnknownCdmTypeException e) {
			fail(typeString + " not recognized");
		}
	}
}