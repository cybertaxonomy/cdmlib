package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.util.Map;

import eu.etaxonomy.cdm.remote.dto.oaipmh.ListMetadataFormats;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataFormat;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public class ListMetadataFormatsView extends OaiPmhResponseView {

    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.LIST_METADATA_FORMATS);
    	oaiPmh.getRequest().setValue((String)model.get("request"));
        
    	ListMetadataFormats listMetadataFormats = new ListMetadataFormats();
    	MetadataFormat oai_dc = new MetadataFormat();
    	oai_dc.setMetadataPrefix(MetadataPrefix.OAI_DC);
    	oai_dc.setSchema("http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
    	oai_dc.setMetadataNamespace("http://www.openarchives.org/OAI/2.0/oai_dc/");
    	listMetadataFormats.getMetadataFormat().add(oai_dc);
    
        oaiPmh.setListMetadataFormats(listMetadataFormats);
    }
}
