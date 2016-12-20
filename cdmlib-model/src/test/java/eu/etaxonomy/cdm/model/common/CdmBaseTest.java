/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
 
package eu.etaxonomy.cdm.model.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.test.unit.EntityTestBase;


/**
 * @author a.mueller
 *
 */
public class CdmBaseTest extends EntityTestBase{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmBaseTest.class);
	
	static CdmBase cdmBase;

	static public class TestCdmBaseClass extends CdmBase{
		private static final long serialVersionUID = -489812714256694973L;
		public String testString = "testStringXsdfjlksj";
		public float testFloat = 1.43446E-5f;
		public boolean testBoolean = false;
	}
	
	public class PropListener implements PropertyChangeListener {
		private PropertyChangeEvent event;
		public PropListener() {
			event = null;
		}	
		
		public void propertyChange(PropertyChangeEvent evt) {
			this.event = evt;
		}

		public boolean isChanged() {
			return event != null;
		}
		
		public Object getOldValue() {
			return event.getOldValue();
		}
		
		public Object getNewValue() {
			return event.getNewValue();
		}
		
		public String getChangedPropertyName() {
			return event.getPropertyName();
		}
		
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}
	
	private static CdmBase getTestCdmBase(){
		return new TestCdmBaseClass();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		cdmBase = null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		cdmBase = getTestCdmBase();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	private void removeExistingListeners(CdmBase cdmBase){
		Field fieldPropChangeSupport;
		PropertyChangeSupport propertyChangeSupport = null;
		try {
			Class clazz = CdmBase.class;
			fieldPropChangeSupport = clazz.getDeclaredField("propertyChangeSupport");
			fieldPropChangeSupport.setAccessible(true);
			propertyChangeSupport = (PropertyChangeSupport)fieldPropChangeSupport.get(cdmBase);
		} catch (SecurityException e) {
			fail();
		} catch (IllegalArgumentException e) {
			fail();
		} catch (NoSuchFieldException e) {
			fail();
		} catch (IllegalAccessException e) {
			fail();
		}
		PropertyChangeListener[] listeners = propertyChangeSupport.getPropertyChangeListeners();
		for (PropertyChangeListener listener : listeners){
			propertyChangeSupport.removePropertyChangeListener(listener);
		}
		assertFalse(cdmBase.hasListeners("created"));
		assertFalse(cdmBase.hasListeners("createdBy"));
		assertFalse(cdmBase.hasListeners("id"));
		assertFalse(cdmBase.hasListeners("uuid"));
	}
/*************** TESTS **************************************************/
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#CdmBase()}.
	 */
	@Test
	public void testCdmBase() {
		assertTrue(cdmBase != null);
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#addPropertyChangeListener(java.beans.PropertyChangeListener)}.
	 */
	@Test
	public void testAddPropertyChangeListenerPropertyChangeListener() {
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners("uuid"));
		
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(listener);
		UUID uuid = UUID.randomUUID();
		
		assertTrue(cdmBase.hasListeners("created"));
		assertTrue(cdmBase.hasListeners("createdBy"));
		assertTrue(cdmBase.hasListeners("id"));
		assertTrue(cdmBase.hasListeners("uuid"));
		
		cdmBase.setUuid(uuid);
		org.junit.Assert.assertTrue(listener.isChanged());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}.
	 */
	@Test
	public void testAddPropertyChangeListenerStringPropertyChangeListener() {
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners("uuid"));
		
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener("uuid", listener);
		cdmBase.setId(22);
		assertFalse(listener.isChanged());
		assertTrue(cdmBase.hasListeners("uuid"));
		
		UUID uuid = UUID.randomUUID();
		cdmBase.setUuid(uuid);
		assertTrue(listener.isChanged());
	}
	

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#removePropertyChangeListener(java.beans.PropertyChangeListener)}.
	 */
	@Test
	public void testRemovePropertyChangeListenerPropertyChangeListener() {
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners("uuid"));
		
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(listener);
		assertTrue(cdmBase.hasListeners("created"));
		assertTrue(cdmBase.hasListeners("createdBy"));
		assertTrue(cdmBase.hasListeners("id"));
		assertTrue(cdmBase.hasListeners("uuid"));
		
		cdmBase.removePropertyChangeListener(listener);
		assertFalse(cdmBase.hasListeners("created"));
		assertFalse(cdmBase.hasListeners("createdBy"));
		assertFalse(cdmBase.hasListeners("id"));
		assertFalse(cdmBase.hasListeners("uuid"));
		
		UUID uuid = UUID.randomUUID();
		cdmBase.setUuid(uuid);
		assertFalse(listener.isChanged());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)}.
	 */
	@Test
	public void testRemovePropertyChangeListenerStringPropertyChangeListener() {
		removeExistingListeners(cdmBase);
		String strUuid = "uuid";
		assertFalse(cdmBase.hasListeners(strUuid));

		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(strUuid, listener);
		assertTrue(cdmBase.hasListeners(strUuid));
		
		cdmBase.removePropertyChangeListener(strUuid, listener);
		assertFalse(cdmBase.hasListeners(strUuid));
		
		UUID uuid = UUID.randomUUID();
		cdmBase.setUuid(uuid);
		assertFalse(listener.isChanged());	
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#hasListeners(java.lang.String)}.
	 */
	@Test
	public void testHasListeners() {
		String prop = "uuid";
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners(prop));
		
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(prop, listener);
		assertTrue(cdmBase.hasListeners(prop));
		
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners(prop));
		
		cdmBase.addPropertyChangeListener(prop, listener);
		assertTrue(cdmBase.hasListeners(prop));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testFirePropertyChangeStringStringString() {
		//Initialize
		TestCdmBaseClass testCdm;
		testCdm = (TestCdmBaseClass)cdmBase;
		String prop = "testString";
		removeExistingListeners(testCdm);
		assertFalse(testCdm.hasListeners(prop));
		PropListener listener = new PropListener();
		testCdm.addPropertyChangeListener(prop, listener);
		assertTrue(testCdm.hasListeners(prop));
		
		//Test
		String oldValue = testCdm.testString;
		String newValue = "sdfklwekj";
		assertFalse(newValue.equals(oldValue));
		testCdm.firePropertyChange(prop, oldValue, newValue);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.lang.String, int, int)}.
	 */
	@Test
	public void testFirePropertyChangeStringIntInt() {
		//Initialize
		String prop = "id";
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners(prop));
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(prop, listener);
		assertTrue(cdmBase.hasListeners(prop));
		
		//Test
		int oldValue = cdmBase.getId();
		int newValue = 45;
		assertTrue(oldValue != newValue);
		cdmBase.firePropertyChange(prop, oldValue, newValue);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.lang.String, float, float)}.
	 */
	@Test
	public void testFirePropertyChangeStringFloatFloat() {
		//Initialize
		TestCdmBaseClass testCdm;
		testCdm = (TestCdmBaseClass)cdmBase;
		String prop = "testFloat";
		removeExistingListeners(testCdm);
		assertFalse(testCdm.hasListeners(prop));
		PropListener listener = new PropListener();
		testCdm.addPropertyChangeListener(prop, listener);
		assertTrue(testCdm.hasListeners(prop));
		
		//Test
		float oldValue = testCdm.testFloat;
		float newValue = 1.40239846E-4f;
		assertFalse(oldValue == newValue);
		testCdm.firePropertyChange(prop, oldValue, newValue);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.lang.String, boolean, boolean)}.
	 */
	@Test
	public void testFirePropertyChangeStringBooleanBoolean() {
		//Initialize
		TestCdmBaseClass testCdm;
		testCdm = (TestCdmBaseClass)cdmBase;
		String prop = "testBoolean";
		removeExistingListeners(testCdm);
		assertFalse(testCdm.hasListeners(prop));
		PropListener listener = new PropListener();
		testCdm.addPropertyChangeListener(prop, listener);
		assertTrue(testCdm.hasListeners(prop));
		
		//Test
		boolean oldValue = testCdm.testBoolean;
		boolean newValue = ! testCdm.testBoolean;
		assertFalse(oldValue == newValue);
		testCdm.firePropertyChange(prop, oldValue, newValue);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)}.
	 */
	@Test
	public void testFirePropertyChangeStringObjectObject() {
		//Initialize
		String prop = "uuid";
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners(prop));
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(prop, listener);
		assertTrue(cdmBase.hasListeners(prop));
		
		//Test
		UUID oldValue = cdmBase.getUuid();
		UUID newValue = UUID.randomUUID();
		cdmBase.firePropertyChange(prop, oldValue, newValue);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#firePropertyChange(java.beans.PropertyChangeEvent)}.
	 */
	@Test
	public void testFirePropertyChangePropertyChangeEvent() {
		//Initialize
		String prop = "uuid";
		removeExistingListeners(cdmBase);
		assertFalse(cdmBase.hasListeners(prop));
		PropListener listener = new PropListener();
		cdmBase.addPropertyChangeListener(prop, listener);
		assertTrue(cdmBase.hasListeners(prop));
		
		//Test
		UUID oldValue = cdmBase.getUuid();
		UUID newValue = UUID.randomUUID();
		PropertyChangeEvent event = new PropertyChangeEvent(cdmBase , prop, oldValue, newValue);
		cdmBase.firePropertyChange(event);
		assertTrue(listener.isChanged());
		assertEquals(oldValue, listener.getOldValue());
		assertEquals(newValue, listener.getNewValue());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#getId()}.
	 */
	@Test
	public void testGetId() {
		assertEquals(0, cdmBase.getId());
		int id = 22;
		cdmBase.setId(id);
		assertEquals(id, cdmBase.getId());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#setId(int)}.
	 */
	@Test
	public void testSetId() {
		int id = 22;
		cdmBase.setId(id);
		assertEquals(id, cdmBase.getId());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#getUuid()}.
	 */
	@Test
	public void testGetUuid() {
		assertNotNull(cdmBase.getUuid());
		assertFalse(UUID.randomUUID().equals(cdmBase.getUuid()));
		assertFalse("".equals(cdmBase.getUuid()));
		UUID uuid = UUID.randomUUID();
		cdmBase.setUuid(uuid);
		assertEquals(uuid, cdmBase.getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#setUuid(java.util.UUID)}.
	 */
	@Test
	public void testSetUuid() {
		UUID uuid = UUID.randomUUID();
		cdmBase.setUuid(uuid);
		assertEquals(uuid, cdmBase.getUuid());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#getCreated()}.
	 */
	@Test
	public void testGetCreated() {
		assertNotNull(cdmBase.getCreated());
		assertFalse(cdmBase.getCreated().isAfter(new DateTime()));
		DateTime dateTime = new DateTime();
		cdmBase.setCreated(dateTime);
		assertEquals(0, cdmBase.getCreated().getMillisOfSecond());
		dateTime = dateTime.withMillisOfSecond(0);
		assertEquals(dateTime, cdmBase.getCreated());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#setCreated(java.util.Calendar)}.
	 */
	@Test
	public void testSetCreated() {
		DateTime calendar = new DateTime();
		DateTime calendarTrue = calendar.withMillisOfSecond(23);
		DateTime calendarFalse = calendar.withMillisOfSecond(23);
		calendarFalse = calendarFalse.plusMonths(5);
		cdmBase.setCreated(calendar);
		calendar = calendar.withMillisOfSecond(0);
		calendarTrue = calendarTrue.withMillisOfSecond(0);
		calendarFalse = calendarFalse.withMillisOfSecond(0);
		assertEquals(calendar, cdmBase.getCreated());
		assertEquals(calendarTrue, cdmBase.getCreated());
		assertFalse(calendarFalse.equals(calendar));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#getCreatedBy()}.
	 */
	@Test
	public void testGetCreatedBy() {
		assertNull(cdmBase.getCreatedBy());
		User user = new User();
		cdmBase.setCreatedBy(user);
		assertEquals(user, cdmBase.getCreatedBy());
		assertSame(user, cdmBase.getCreatedBy());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#setCreatedBy(eu.etaxonomy.cdm.model.agent.Person)}.
	 */
	@Test
	public void testSetCreatedBy() {
		User person = new User();
		
		User personFalse = new User();
		
		cdmBase.setCreatedBy(person);
		assertEquals(person, cdmBase.getCreatedBy());
		assertSame(person, cdmBase.getCreatedBy());
		assertFalse(personFalse.equals(person));
		//null
		cdmBase.setCreated(null);
		assertNull(cdmBase.getCreated());

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#equals(java.lang.Object)}.
	 */
	@Test
	public void testEqualsObject() {
		ICdmBase cdmBase2 = getTestCdmBase();
		cdmBase2.setUuid(cdmBase.getUuid());
		cdmBase2.setCreated(cdmBase.getCreated());
//		cdmBase2.setCreatedBy(Person.NewInstance());
		assertEquals(cdmBase, cdmBase2);
		//null
		assertFalse(cdmBase.equals(null));
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#toString()}.
	 */
	@Test
	public void testToString() {
		String expected = cdmBase.getClass().getSimpleName()+"#"+cdmBase.getId()+"<"+cdmBase.getUuid()+">";
		assertEquals(expected, cdmBase.toString());
	}
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.common.CdmBase#clone()}.
	 */
	@Test
	public void testClone(){
		try {
			CdmBase clone = (CdmBase) cdmBase.clone();
			assertNotSame(clone, cdmBase);
			assertEquals(clone.getId(), cdmBase.getId());
			assertFalse(clone.getUuid().equals(cdmBase.getUuid()));
			assertNotNull(clone.getCreated());
			assertNull(clone.getCreatedBy());
			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}


}
