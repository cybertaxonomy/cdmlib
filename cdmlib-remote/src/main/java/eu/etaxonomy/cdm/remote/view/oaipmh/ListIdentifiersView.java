package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.time.ZonedDateTime;
import java.util.Map;

import org.hibernate.envers.RevisionType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Header;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ListIdentifiers;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Status;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public class ListIdentifiersView extends OaiPmhResponseView {

    @Override
    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.LIST_IDENTIFIERS);
    	oaiPmh.getRequest().setValue((String)model.get("request"));
    	oaiPmh.getRequest().setMetadataPrefix((MetadataPrefix)model.get("metadataPrefix"));

        if(model.containsKey("from")) {
            oaiPmh.getRequest().setFrom((ZonedDateTime)model.get("from"));
        }

        if(model.containsKey("until")) {
            oaiPmh.getRequest().setUntil((ZonedDateTime)model.get("until"));
        }

        if(model.containsKey("set")) {
            oaiPmh.getRequest().setSet((SetSpec)model.get("set"));
        }

        ListIdentifiers listIdentifiers = new ListIdentifiers();

        for(AuditEventRecord auditEventRecord : ((Pager<AuditEventRecord>)model.get("pager")).getRecords()) {
        	Header header = mapper.map(auditEventRecord.getAuditableObject(), Header.class);
        	if(auditEventRecord.getRevisionType().equals(RevisionType.DEL)) {
        		header.setStatus(Status.DELETED);
        	}
        	listIdentifiers.getHeader().add(header);
        }

        if(model.containsKey("resumptionToken")) {
        	listIdentifiers.setResumptionToken((ResumptionToken)model.get("resumptionToken"));
        }

        oaiPmh.setListIdentifiers(listIdentifiers);
    }
}
