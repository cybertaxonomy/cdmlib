/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.hibernate.envers.RevisionType;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.common.UriUtils;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.model.view.AuditEventRecordImpl;
import eu.etaxonomy.cdm.remote.dto.oaipmh.DeletedRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Granularity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.view.oaipmh.IdentifyView;
import eu.etaxonomy.cdm.remote.view.oaipmh.ListIdentifiersView;
import eu.etaxonomy.cdm.remote.view.oaipmh.ListMetadataFormatsView;
import eu.etaxonomy.cdm.remote.view.oaipmh.ListSetsView;
import eu.etaxonomy.cdm.remote.view.oaipmh.dc.GetRecordView;
import eu.etaxonomy.cdm.remote.view.oaipmh.dc.ListRecordsView;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
@Ignore  //ignore temporarily as the service is currently very slow or not available
public class OaiPmhViewTest extends UnitilsJUnit4 {

    private static final Logger logger = LogManager.getLogger();

    @SpringBeanByName
    private Marshaller marshaller;

    @SpringBeanByType
    private Mapper mapper;

    private Map<String,Object> model;
    private IdentifyView identifyView;
    private GetRecordView getRecordView;
    private ListMetadataFormatsView listMetadataFormatsView;
    private ListSetsView listSetsView;
    private ListIdentifiersView listIdentifiersView;
    private ListRecordsView listRecordsView;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private eu.etaxonomy.cdm.remote.view.oaipmh.rdf.GetRecordView rdfGetRecordView;

    private URI oaiServerURI;
    private static URI dozerXsdServerURI = URI.create("https://dozermapper.github.io/");  //for testing https://dozermapper.github.io/schema/bean-mapping.xsd

    @Before
    public void setUp() throws Exception {

        XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
        XMLUnit.setIgnoreWhitespace(true);

        model = new HashMap<>();
        identifyView = new IdentifyView();
        identifyView.setMarshaller(marshaller);

        getRecordView = new GetRecordView();
        getRecordView.setMarshaller(marshaller);
        getRecordView.setMapper(mapper);

        rdfGetRecordView = new eu.etaxonomy.cdm.remote.view.oaipmh.rdf.GetRecordView();
        rdfGetRecordView.setMarshaller(marshaller);
        rdfGetRecordView.setMapper(mapper);

        listMetadataFormatsView = new ListMetadataFormatsView();
        listMetadataFormatsView.setMarshaller(marshaller);

        listSetsView = new ListSetsView();
        listSetsView.setMarshaller(marshaller);

        listIdentifiersView = new ListIdentifiersView();
        listIdentifiersView.setMarshaller(marshaller);
        listIdentifiersView.setMapper(mapper);

        listRecordsView = new ListRecordsView();
        listRecordsView.setMarshaller(marshaller);
        listRecordsView.setMapper(mapper);

        request = new MockHttpServletRequest();

        oaiServerURI = new URI("http://memory.loc.gov");

        request.setServerName(oaiServerURI.toString());
        request.setServerPort(80);
        response = new MockHttpServletResponse();
    }

    @Test
    public void testIdentifyView() throws Exception {

        if(!oaiServiceIsAvailable()){
            return;
        }

        model.put("repositoryName", "Library of Congress Open Archive Initiative Repository 1");
        model.put("baseURL","http://memory.loc.gov/cgi-bin/oai");
        model.put("protocolVersion","2.0");
        model.put("deletedRecord",DeletedRecord.TRANSIENT);
        model.put("granularity",Granularity.YYYY_MM_DD_THH_MM_SS_Z);

        model.put("earliestDatestamp",ISODateTimeFormat.dateTimeParser().parseDateTime("1990-02-01T12:00:00Z"));
        model.put("adminEmail","somebody@loc.gov");
        model.put("description","<oai-identifier xmlns=\"http://www.openarchives.org/OAI/2.0/oai-identifier\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai-identifier  http://www.openarchives.org/OAI/2.0/oai-identifier.xsd\"><scheme>oai</scheme><repositoryIdentifier>lcoa1.loc.gov</repositoryIdentifier><delimiter>:</delimiter><sampleIdentifier>oai:lcoa1.loc.gov:loc.music/musdi.002</sampleIdentifier></oai-identifier>");

        request.setRequestURI("/cgi-bin/oai?verb=Identify");

        identifyView.render(model, request, response);
//		String resource = "/eu/etaxonomy/cdm/remote/view/oaipmh/IdentifyView.xml";
//		System.out.println(new String(response.getContentAsByteArray()));
//		XMLAssert.assertXMLEqual(new InputStreamReader(this.getClass().getResourceAsStream(resource)),new StringReader(new String(response.getContentAsByteArray())));
    }

    @Test
    public void testGetRecordView() throws Exception {

        if(!oaiServiceIsAvailable() || !dozerXsdIsAvailable()){
            return;
        }

        Taxon taxon = Taxon.NewInstance((TaxonName)null, null);
        taxon.setTitleCache("TitleCache", true);
        taxon.setCreated(new DateTime());
        taxon.setLsid(new LSID("urn:lsid:example.org:taxonconcepts:1"));

        AuditEventRecord<Taxon> auditEventRecord = new AuditEventRecordImpl<Taxon>(new Object[] {taxon, new AuditEvent(),RevisionType.ADD});

        model.put("object", auditEventRecord);

        request.setRequestURI("/cgi-bin/oai?verb=GetRecord&identifier=urn:lsid:example.org:taxonconcepts:1&metadataPrefix=oai_dc");
        getRecordView.render(model, request, response);
//		System.out.println(new String(response.getContentAsByteArray()));
    }

    @Test
    public void testRdfGetRecordView() throws Exception {

        if(!oaiServiceIsAvailable() || !dozerXsdIsAvailable()){
            return;
        }

        Taxon taxon = Taxon.NewInstance(null, null);
        taxon.setTitleCache("TitleCache", true);
        taxon.setCreated(new DateTime());
        taxon.setLsid(new LSID("urn:lsid:example.org:taxonconcepts:1"));

        AuditEventRecord<Taxon> auditEventRecord = new AuditEventRecordImpl<Taxon>(new Object[] {taxon, new AuditEvent(),RevisionType.ADD});

        model.put("object", auditEventRecord);

        request.setRequestURI("/cgi-bin/oai?verb=GetRecord&identifier=urn:lsid:example.org:taxonconcepts:1&metadataPrefix=rdf");
        rdfGetRecordView.render(model, request, response);
        //System.out.println(new String(response.getContentAsByteArray()));
    }

    @Test
    public void testListMetadataFormatsView() throws Exception {

        if(!oaiServiceIsAvailable()){
            return;
        }

        request.setRequestURI("/cgi-bin/pdataprov?verb=ListMetadataFormats&identifier=oai:perseus.tufts.edu:Perseus:text:1999.02.0119");
        listMetadataFormatsView.render(model, request, response);
        //System.out.println(new String(response.getContentAsByteArray()));
    }

    @Test
    public void testListSetsView() throws Exception {

        if(!oaiServiceIsAvailable()){
            return;
        }

        request.setRequestURI("/OAI-script?verb=ListSets");
        Set<SetSpec> sets = new HashSet<SetSpec>();
        sets.add(SetSpec.TAXON);
        sets.add(SetSpec.SYNONYM);
        model.put("sets",sets);
        listSetsView.render(model, request, response);
//		System.out.println(new String(response.getContentAsByteArray()));
    }

    @Test
    public void testListIdentifiersView() throws Exception {

        if(!oaiServiceIsAvailable() || !dozerXsdIsAvailable()){
            return;
        }

        model.put("metadataPrefix", MetadataPrefix.OAI_DC);

        DateTime from = new DateTime(1990,2,1,12,0,0, 0);
        DateTime until = new DateTime();
        model.put("from",from);
        model.put("until", until);

        List<AuditEventRecord<TaxonBase>> r = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            TaxonBase<?> taxon = Taxon.NewInstance(null, null);
            taxon.setTitleCache("TitleCache", true);
            taxon.setCreated(new DateTime());
            taxon.setLsid(new LSID("urn:lsid:example.org:taxonconcepts:"+i));
            if((i % 3) == 0 ) {
                AuditEventRecord<TaxonBase> auditEventRecord = new AuditEventRecordImpl<>(new Object[] {taxon, new AuditEvent(), RevisionType.DEL});
                r.add(auditEventRecord);
            } else {
                AuditEventRecord<TaxonBase> auditEventRecord = new AuditEventRecordImpl<>(new Object[] {taxon, new AuditEvent(), RevisionType.ADD});
                r.add(auditEventRecord);
            }
        }
        Pager<AuditEventRecord<TaxonBase>> results = new DefaultPagerImpl<>(0, Long.valueOf(100), 10, r);
        model.put("pager", results);
        ResumptionToken resumptionToken = new ResumptionToken(results, from, until, MetadataPrefix.OAI_DC, null);
        model.put("resumptionToken",resumptionToken);
        listIdentifiersView.render(model, request, response);
//		System.out.println(new String(response.getContentAsByteArray()));
    }

    @Test
    public void testListRecordsView() throws Exception {

        if(!oaiServiceIsAvailable() || !dozerXsdIsAvailable()){
            return;
        }

        model.put("metadataPrefix", MetadataPrefix.OAI_DC);

        DateTime from = new DateTime(1990,2,1,12,0,0, 0);
        DateTime until = new DateTime();
        model.put("from",from);
        model.put("until", until);

        List<AuditEventRecord<TaxonBase>> r = new ArrayList<>();
        for(int i = 0; i < 10; i++) {
            TaxonBase<?> taxon = Taxon.NewInstance(null, null);
            taxon.setTitleCache("TitleCache", true);
            taxon.setCreated(new DateTime());
            taxon.setLsid(new LSID("urn:lsid:example.org:taxonconcepts:"+i));
            if((i % 3) == 0 ) {
                AuditEventRecord<TaxonBase> auditEventRecord = new AuditEventRecordImpl<>(new Object[] {taxon, new AuditEvent(), RevisionType.DEL});
                r.add(auditEventRecord);
            } else {
                AuditEventRecord<TaxonBase> auditEventRecord = new AuditEventRecordImpl<>(new Object[] {taxon, new AuditEvent(), RevisionType.ADD});
                r.add(auditEventRecord);
            }
        }
        Pager<AuditEventRecord<TaxonBase>> results = new DefaultPagerImpl<>(0, Long.valueOf(100), 10, r);
        model.put("pager", results);
        ResumptionToken resumptionToken = new ResumptionToken(results, from, until, MetadataPrefix.OAI_DC, null);
        model.put("resumptionToken",resumptionToken);
        listRecordsView.render(model, request, response);
        //System.out.println(new String(response.getContentAsByteArray()));
    }

    private boolean oaiServiceIsAvailable() {
        if(!UriUtils.isServiceAvailable(oaiServerURI)) {
            logger.warn("Service " + oaiServerURI.toString() + " unavailable");
            return false;
        } else {
            return true;
        }
    }

    public static boolean dozerXsdIsAvailable() {
        // dozer requires access to dozer.sourceforge.net to test schema (.xsd/.dtd)
        if(!UriUtils.isServiceAvailable(dozerXsdServerURI)) {
            logger.warn("Service " + dozerXsdServerURI.toString() + " unavailable");
            return false;
        } else {
            return true;
        }
    }
}