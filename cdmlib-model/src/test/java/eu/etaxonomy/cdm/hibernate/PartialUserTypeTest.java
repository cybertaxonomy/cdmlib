package eu.etaxonomy.cdm.hibernate;

import static org.junit.Assert.assertTrue;

import java.sql.Types;

import org.apache.log4j.Logger;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PartialUserTypeTest {
    private static final Logger logger = Logger.getLogger(PartialUserTypeTest.class);

	private UserType userType;

	private static final DateTimeFieldType YEAR = DateTimeFieldType.year();
    private static final DateTimeFieldType MONTH = DateTimeFieldType.monthOfYear();
    private static final DateTimeFieldType DAY = DateTimeFieldType.dayOfMonth();
    private static final DateTimeFieldType HOUR = DateTimeFieldType.hourOfDay();
    private static final DateTimeFieldType MINUTE = DateTimeFieldType.minuteOfHour();

	@Before
	public void setUp() throws Exception {
		userType = new PartialUserType();
	}

	@Test
	public void testNotMutable() throws Exception {
		Assert.assertFalse(userType.isMutable());
	}

	@Test
	public void testDeepCopyReturnsSelf() throws Exception {
	    Partial partial = new Partial().with(DateTimeFieldType.year(), 1982);
		Assert.assertSame (userType.deepCopy(partial), partial);
	}

	@Test
	public void testReturnedClassIsPartial() throws Exception {
		assertTrue(userType.returnedClass() == Partial.class);
	}

	@Test
	public void testEqualsDelegatesToPartialEquals() throws Exception {
	    Partial y1982 = new Partial().with(YEAR, 1982);
	    Partial copy = new Partial().with(YEAR, 1982);
	    Partial y1983 = new Partial().with(YEAR, 1983);
	    Partial y1982_0 = new Partial().with(YEAR, 1982)
	            .with(HOUR, 00);

	    Assert.assertTrue(userType.equals(y1982, copy));
        Assert.assertFalse(userType.equals(y1982, y1983));
        Assert.assertFalse(userType.equals(y1982, y1982_0));
  	}

	@Test
	public void testAssembleReturnsSelf() throws Exception {
		Partial foo = new Partial().with(YEAR, 1982);
		Assert.assertSame(userType.assemble(foo, null), foo);
	}

	@Test
	public void testDisassembleReturnsSelf() throws Exception {
        Partial foo = new Partial().with(YEAR, 1982);
        Assert.assertSame(userType.disassemble(foo), foo);
        Assert.assertSame(userType.disassemble(null), null);
	}

	@Test
	public void testReplaceReturnsSelf() throws Exception {
	    Partial foo = new Partial().with(YEAR, 1982);
        Assert.assertSame(userType.replace(foo, null, null), foo);
        Assert.assertEquals(userType.replace(null, null, null), null);
	}

	@Test
	public void testSqlTypesAreNumberAndInteger() throws Exception {
		Assert.assertEquals(1, userType.sqlTypes().length);
		Assert.assertEquals(userType.sqlTypes()[0], Types.VARCHAR);
	}

	@Test
	public void testNullSafeGet() throws Exception {
	    logger.warn("Not yet implemented");
	}

	@Test
	public void testNullSafeGetWithNull() throws Exception {
	    logger.warn("Not yet implemented");
	}

	@Test
	public void testNullSafeSet() throws Exception {
	    logger.warn("Not yet implemented");
	}

	@Test
	public void testNullSafeSetWithNull() throws Exception {
	    logger.warn("Not yet implemented");
	}
}
