/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.MalformedLSIDException;
import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.server.LSIDServerException;

import eu.etaxonomy.cdm.api.service.lsid.LSIDMetadataService;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.test.util.LSIDMatchers;

/**
 * @author b.clark
 * @date 2009
 */
public class MetadataControllerTest {

	private LSIDMetadataService metadataService;

    private MetadataController metadataController;

    private LSID lsid;
    private String[] acceptedFormats;

	@BeforeEach
	public void setUp() {
		metadataController = new MetadataController();
		metadataService = EasyMock.createMock(LSIDMetadataService.class);
		metadataController.setLsidMetadataService(metadataService);
		try {
			lsid = new LSID("urn:lsid:fred.org:dagg:1");
		} catch (MalformedLSIDException e) { }
		acceptedFormats = new String[]{MetadataResponse.RDF_FORMAT};
	}

	@Test
    public void testGetMetadata() throws Exception {
		Taxon taxon = Taxon.NewInstance(null,null);
		EasyMock.expect(metadataService.getMetadata(LSIDMatchers.eqLSID(lsid))).andReturn(taxon);
		EasyMock.replay(metadataService);


		ModelAndView modelAndView = metadataController.getMetadata(lsid, "application/xml+rdf");

		EasyMock.verify(metadataService);
		ModelAndViewAssert.assertViewName(modelAndView, "Metadata.rdf");
		assertTrue(modelAndView.getModel().containsValue(taxon));
	}

	@Test
    public void testGetMetadataWithoutAcceptedFormat() throws Exception {
    	acceptedFormats = new String[]{MetadataResponse.N3_FORMAT,MetadataResponse.XMI_FORMAT};
    	EasyMock.replay(metadataService);

		assertThrows(LSIDServerException.class,
				() -> metadataController.getMetadata(lsid, "application/n3,application/xml+xmi"));
	}

	@Test
    public void testGetMetadataWithUnknownLSID() throws Exception {

    	LSIDServerException lse = new LSIDServerException(LSIDException.UNKNOWN_LSID, "Unknown LSID");
    	EasyMock.expect(metadataService.getMetadata(LSIDMatchers.eqLSID(lsid))).andThrow(lse);
    	EasyMock.replay(metadataService);

        assertThrows(LSIDServerException.class,
        		() -> metadataController.getMetadata(lsid, "application/xml+rdf"));
	}
}
