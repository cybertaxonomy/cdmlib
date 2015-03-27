/**
 * 
 */
package eu.etaxonomy.cdm.hibernate;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.molecular.SingleReadAlignment.Shift;

/**
 * @author a.mueller
 *
 */
public class ShiftUsertTypeTest {

	private ShiftUserType shiftUserType;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		shiftUserType = new ShiftUserType();
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#deepCopy(java.lang.Object)}.
	 */
	@Test
	public void testDeepCopyObject() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#disassemble(java.lang.Object)}.
	 */
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

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#nullSafeGet(java.sql.ResultSet, java.lang.String[], org.hibernate.engine.spi.SessionImplementor, java.lang.Object)}.
	 */
	@Test
	public void testNullSafeGetString() {
		Shift[] emptyResult = shiftUserType.nullSafeGet("");
		Assert.assertEquals(0, emptyResult.length);
		Shift[] singleResult = shiftUserType.nullSafeGet("2,2");
		Assert.assertEquals(1, singleResult.length);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#nullSafeSet(java.sql.PreparedStatement, java.lang.Object, int, org.hibernate.engine.spi.SessionImplementor)}.
	 */
	@Test
	public void testNullSafeSet() {
		//TODO
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#returnedClass()}.
	 */
	@Test
	public void testReturnedClass() {
		Class<?> returnedClass = new ShiftUserType().returnedClass();
		Assert.assertEquals(Shift[].class, returnedClass);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.hibernate.ShiftUserType#sqlTypes()}.
	 */
	@Test
	public void testSqlTypes() {
		int[] sqlTypes = new ShiftUserType().sqlTypes();
//		Assert.assertEquals(new int[]{ Types.CLOB }, sqlTypes);
	}

}
