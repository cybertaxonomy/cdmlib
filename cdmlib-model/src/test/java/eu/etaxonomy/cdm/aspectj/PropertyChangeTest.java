package eu.etaxonomy.cdm.aspectj;

	import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
	import java.beans.PropertyChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;

	public class PropertyChangeTest implements PropertyChangeListener {
		static Logger logger = Logger.getLogger(PropertyChangeTest.class);
		private Object lastPropValue;

		public void propertyChange(PropertyChangeEvent e){
		logger.debug("Property [" + (String)e.getPropertyName() 
				+ "] changed from " + e.getOldValue() 
				+ " to " + e.getNewValue());
		lastPropValue = e.getNewValue() == null ? null : e.getNewValue();
		}

		@Test
		public void testPropertyChange() {
			NonViralName b = NonViralName.NewInstance(Rank.SPECIES());
			b.addPropertyChangeListener(this);
			b.setGenusOrUninomial("Abies");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Picea");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Unipicea");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setSpecificEpithet("vulgaris");
				assertEquals(b.getSpecificEpithet(), lastPropValue);
		}

		@Test
		public void testPropertyChangeBoolean() {
			BotanicalName b = BotanicalName.NewInstance(Rank.SPECIES());
			b.addPropertyChangeListener(this);
			b.setAnamorphic(true);
			assertEquals(b.isAnamorphic(), lastPropValue);
		}
		
		@Before
		public void updateDebugLevel(){
			logger.setLevel(Level.INFO);
		}

	}

