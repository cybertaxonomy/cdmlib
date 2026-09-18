/**
 * Copyright (C) 2020 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.ext.geo.kml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

public class KmlJaxbMarshallerTest {

    private static final Logger logger = LogManager.getLogger();

    private Kml kml;

	@Before
	public void makeKML() {
		FieldUnit fu = FieldUnit.NewInstance();
		fu.setGatheringEvent(GatheringEvent.NewInstance());
		fu.getGatheringEvent().setExactLocation(Point.NewInstance(-112.292238941097, 36.09520916122063, null, null));

		KmlDocumentBuilder builder = new KmlDocumentBuilder();
		builder.addSpecimenOrObservationBase(fu);
		kml = builder.build();
	}

	@Test
	public void marshallTest() throws IOException {

	    StringWriter sw = new StringWriter();
	    kml.marshal(sw);

		String kml = sw.toString();
		if(logger.isDebugEnabled()) {
			logger.debug("kml:\n" + kml);
			FileUtils.write(new File("KmlJaxbMarshallerTest.kml"), kml, Charset.defaultCharset());
		}
		assertTrue(kml.contains("<Document>"));
		assertTrue(kml.contains("<Point>"));
		assertTrue(kml.contains("<coordinates>-112.292238941097,36.09520916122063</coordinates>"));
		assertTrue(kml.contains("<altitudeMode>absolute</altitudeMode>"));
	}
}