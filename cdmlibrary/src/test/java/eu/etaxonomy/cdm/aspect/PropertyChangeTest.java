package eu.etaxonomy.cdm.aspect;

	import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
	import java.beans.PropertyChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;

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
			TaxonName b = new TaxonName();
			b.addPropertyChangeListener(this);
			b.setGenus("Abies");
			if (lastPropValue != null){
				assertEquals(b.getGenus(), lastPropValue);
			}
			b.setGenus("Picea");
			if (lastPropValue != null){
				assertEquals(b.getGenus(), lastPropValue);
			}
			b.setUninomial("Unipicea");
			if (lastPropValue != null){
				assertEquals(b.getUninomial(), lastPropValue);
			}
			b.setSpecificEpithet("vulgaris");
			if (lastPropValue != null){
				assertEquals(b.getSpecificEpithet(), lastPropValue);
			}
			
		}

		
		@Before
		public void updateDebugLevel(){
			Logger.getRootLogger().setLevel(Level.DEBUG);
		}

	}

