/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;
import java.sql.Types;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;

/**
 * @author a.mueller
 */
public class ShiftUsertTypeTest {

	private ShiftUserType shiftUserType;

	@Before
	public void setUp() throws Exception {
		shiftUserType = new ShiftUserType();
	}

	@Test
	public void testDeepCopyObject() {
		//TODO
	}

	@Test
	public void testDisassembleObject() {
		Serializable nullValue = this.shiftUserType.disassemble(null);
		Assert.assertNull(nullValue);
		Serializable emptyValue = this.shiftUserType.disassemble(new Shift[0]);
		Assert.assertEquals("", emptyValue);
		Serializable singleValue = this.shiftUserType.disassemble(new Shift[]{new Shift(2, 3)});
		Assert.assertEquals("2,3", singleValue);
		Serializable twoValues = this.shiftUserType.disassemble(new Shift[]{new Shift(2, 3),new Shift(3, -1)});
		Assert.assertEquals("2,3;3,-1", twoValues);
	}

	@Test
	public void testNullSafeGetString() {
		Shift[] emptyResult = shiftUserType.nullSafeGet("");
		Assert.assertEquals(0, emptyResult.length);
		Shift[] singleResult = shiftUserType.nullSafeGet("2,2");
		Assert.assertEquals(1, singleResult.length);
	}

	@Test
	public void testNullSafeSet() {
		//TODO
	}

	@Test
	public void testReturnedClass() {
		Class<?> returnedClass = new ShiftUserType().returnedClass();
		Assert.assertEquals(Shift[].class, returnedClass);
	}

	@Test
	public void testSqlTypes() {
		int[] sqlTypes = new ShiftUserType().sqlTypes();
		Assert.assertArrayEquals(new int[]{Types.CLOB}, sqlTypes);
	}
}