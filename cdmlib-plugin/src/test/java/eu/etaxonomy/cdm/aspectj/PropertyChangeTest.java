package eu.etaxonomy.cdm.aspectj;

import static org.junit.Assert.assertEquals;

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
			BotanicalName b = BotanicalName.NewInstance(null);
			logger.debug("startTest");
			b.addPropertyChangeListener(this);
			b.setGenusOrUninomial("Abies");
			Assert.assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Picea");
			assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Unipicea");
			assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setSpecificEpithet("vulgaris");
			assertEquals(b.getSpecificEpithet(), lastPropValue);
		}

		
		@Before
		public void updateDebugLevel(){
			logger.setLevel(Level.DEBUG);
		}
		
	}

