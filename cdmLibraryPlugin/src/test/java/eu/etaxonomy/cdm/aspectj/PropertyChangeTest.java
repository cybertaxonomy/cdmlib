package eu.etaxonomy.cdm.aspectj;

import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;

	public class PropertyChangeTest implements PropertyChangeListener {
		static Logger logger = Logger.getLogger(PropertyChangeTest.class);
		private Object lastPropValue;

		public void propertyChange(PropertyChangeEvent e){
		logger.info("Property [" + (String)e.getPropertyName() 
				+ "] changed from " + e.getOldValue() 
				+ " to " + e.getNewValue());
		lastPropValue = e.getNewValue() == null ? null : e.getNewValue();
		}

		@Test
		public void testPropertyChange() {
			BotanicalName b = new BotanicalName(null);
			logger.debug("startTest");
			b.addPropertyChangeListener(this);
			b.setUninomial("Abies");
			Assert.assertEquals(b.getUninomial(), lastPropValue);
			b.setUninomial("Picea");
			assertEquals(b.getUninomial(), lastPropValue);
			b.setUninomial("Unipicea");
			assertEquals(b.getUninomial(), lastPropValue);
			b.setSpecificEpithet("vulgaris");
			assertEquals(b.getSpecificEpithet(), lastPropValue);
		}

		
		@Before
		public void updateDebugLevel(){
			logger.setLevel(Level.DEBUG);
		}
		
	}

