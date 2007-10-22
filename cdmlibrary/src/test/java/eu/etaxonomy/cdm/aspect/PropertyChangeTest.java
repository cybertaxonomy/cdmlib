package eu.etaxonomy.cdm.aspect;

	import static org.junit.Assert.*;

import java.beans.PropertyChangeEvent;
	import java.beans.PropertyChangeListener;

import org.junit.Test;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.TaxonName;

	public class PropertyChangeTest implements PropertyChangeListener {
		private String lastPropValue;

		public void propertyChange(PropertyChangeEvent e){
		System.out.println("TEST> Property [" + (String)e.getPropertyName() 
				+ "] changed from " + e.getOldValue() 
				+ " to " + e.getNewValue());
		lastPropValue = e.getNewValue() == null ? null : (String) e.getNewValue();
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
	}

