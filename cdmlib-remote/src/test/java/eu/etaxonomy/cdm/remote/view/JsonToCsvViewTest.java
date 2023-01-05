/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.oxm.Marshaller;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import com.github.dozermapper.core.Mapper;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoExportConfigurator;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoMetaDataRecord;
import eu.etaxonomy.cdm.io.csv.redlist.demo.CsvDemoRecord;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.view.oaipmh.IdentifyView;

@Ignore
@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class JsonToCsvViewTest extends UnitilsJUnit4 {

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByName
    private Marshaller marshaller;

    @SpringBeanByType
    private Mapper mapper;

    private Map<String,Object> model;
    private IdentifyView identifyView;
    private CsvDemoRecord demoRecord;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private URI serverURI;

	private CsvDemoMetaDataRecord metaDataRecord;

	private CsvDemoExportConfigurator config;

    @Before
    public void setUp() throws Exception {

        model = new HashMap<>();
        identifyView = new IdentifyView();
        identifyView.setMarshaller(marshaller);

        metaDataRecord = new CsvDemoMetaDataRecord(true, "/tmp", "");
        config = CsvDemoExportConfigurator.NewInstance(null, null);

        demoRecord = new CsvDemoRecord(metaDataRecord, config);

        request = new MockHttpServletRequest();

        serverURI = new URI("localhost");

        request.setServerName(serverURI.toString());
        request.setServerPort(8090);
        response = new MockHttpServletResponse();
    }


    @Test
    public void testGetRecordView() throws Exception {

        Taxon taxon = Taxon.NewInstance(null, null);
        taxon.setTitleCache("TitleCache", true);
        taxon.setCreated(new DateTime());

        demoRecord.setScientificName(taxon.getTitleCache());
        demoRecord.setAuthorName("Author");
        demoRecord.setDatasetName("Classification");

        ArrayList<CsvDemoRecord> recordList = new ArrayList<>();
        logger.info(recordList.size());
        recordList.add(demoRecord);
        recordList.add(demoRecord);
        recordList.add(demoRecord);
        logger.info(recordList.size());
        logger.info(recordList.get(0).getScientificName());
        model.put("csv", recordList);

        request.setRequestURI("/cgi-bin/oai?verb=GetRecord&identifier=urn:lsid:example.org:taxonconcepts:1&metadataPrefix=oai_dc");
        CsvFileDownloadView jview = new CsvFileDownloadView(new File("test"));
        jview.render(model, request, response);
//		System.out.println(new String(response.getContentAsByteArray()));
    }

}
