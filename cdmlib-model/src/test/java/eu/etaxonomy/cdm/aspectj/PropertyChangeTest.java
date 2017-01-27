/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.aspectj;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;

	public class PropertyChangeTest implements PropertyChangeListener {
		static Logger logger = Logger.getLogger(PropertyChangeTest.class);
		private Object lastPropValue;

		@Override
        public void propertyChange(PropertyChangeEvent e){
		logger.debug("Property [" + e.getPropertyName()
				+ "] changed from " + e.getOldValue()
				+ " to " + e.getNewValue());
		lastPropValue = e.getNewValue() == null ? null : e.getNewValue();
		}

		@Test
		public void testPropertyChange() {
			NonViralName<?> b = TaxonNameBase.NewNonViralInstance(Rank.SPECIES());
			b.addPropertyChangeListener(this);
			b.setGenusOrUninomial("Abies");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Picea");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setGenusOrUninomial("Unipicea");
				assertEquals(b.getGenusOrUninomial(), lastPropValue);
			b.setSpecificEpithet("vulgaris");
				assertEquals(b.getSpecificEpithet(), lastPropValue);
			b.setParsingProblem(2);
				assertEquals(b.getParsingProblem(), lastPropValue);
		}

		@Test
		public void testPropertyChangeBoolean() {
			BotanicalName b = TaxonNameBase.NewBotanicalInstance(Rank.SPECIES());
			b.addPropertyChangeListener(this);
			b.setAnamorphic(true);
			assertEquals(b.isAnamorphic(), lastPropValue);
		}

		@Before
		public void updateDebugLevel(){
			logger.setLevel(Level.INFO);
		}

	}

