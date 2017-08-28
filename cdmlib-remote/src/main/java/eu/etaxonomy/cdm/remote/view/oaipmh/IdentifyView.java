package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.etaxonomy.cdm.remote.dto.oaipmh.DeletedRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Description;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Granularity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Identify;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public class IdentifyView extends OaiPmhResponseView {

	@Override
    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.IDENTIFY);
    	oaiPmh.getRequest().setValue((String)model.get("request"));
        Identify identify = new Identify();
        identify.setRepositoryName((String)model.get("repositoryName"));
        identify.setBaseURL((String)model.get("baseURL"));
        identify.setProtocolVersion((String)model.get("protocolVersion"));
        identify.setDeletedRecord((DeletedRecord) model.get("deletedRecord"));
    	identify.setGranularity((Granularity) model.get("granularity"));
    	identify.setEarliestDatestamp((ZonedDateTime) model.get("earliestDatestamp"));
    	identify.getAdminEmail().add((String) model.get("adminEmail"));
    	if(model.get("description") != null){
    		Description description = new Description();
    		InputSource inputSource = new InputSource(new StringReader((String)model.get("description")));
    		DocumentBuilderFactory documentBuilderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    		documentBuilderFactory.setNamespaceAware(false);
    		Document document = null;
    		DocumentBuilder documentBuilder;
			try {
				documentBuilder = documentBuilderFactory.newDocumentBuilder();
				document = documentBuilder.parse(inputSource);
	    		Element domElem = document.getDocumentElement();

	    		description.setAny(domElem);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	    identify.getDescription().add(description);
    	}

        oaiPmh.setIdentify(identify);
    }
}
