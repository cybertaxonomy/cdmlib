package eu.etaxonomy.cdm.ext.kml;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import eu.etaxonomy.cdm.ext.geo.kml.KMLDocumentBuilder;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

public class KmlJaxbMarshallerTest {
	
	Kml kml;
	
	Logger logger;
	
	@Before
	public void makeKML() {
		FieldUnit fu = FieldUnit.NewInstance();
		fu.setGatheringEvent(GatheringEvent.NewInstance());
		fu.getGatheringEvent().setExactLocation(Point.NewInstance(-112.292238941097, 36.09520916122063, null, null));
		
		KMLDocumentBuilder builder = new KMLDocumentBuilder();
		builder.addSpecimenOrObservationBase(fu);
		kml = builder.build();
		
		logger = Logger.getLogger(this.getClass());
		logger.setLevel(Level.DEBUG);
	}
	
	
	@Test
	public void marshallTest() throws JAXBException, IOException {

		JAXBContext jaxbContext = JAXBContext.newInstance(Kml.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();
		marshaller.marshal(kml, sw);
		String kml = sw.toString();
		if(logger.isDebugEnabled()) {
			logger.debug("kml:\n" + kml);
			FileUtils.write(new File("KmlJaxbMarshallerTest.kml"), kml);
		}
		assertTrue(kml.contains("<kml:Document>"));
		assertTrue(kml.contains("<kml:Point>"));
		assertTrue(kml.contains("<kml:coordinates>-112.292238941097,36.09520916122063</kml:coordinates>"));
		assertTrue(kml.contains("<kml:altitudeMode>absolute</kml:altitudeMode>"));
	}

}
