package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.envers.RevisionType;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Header;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ListRecords;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Record;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Status;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public abstract class ListRecordsView extends OaiPmhResponseView {

    @Override
    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.LIST_RECORDS);
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

        ListRecords listRecords = new ListRecords();


        if(model.containsKey("pager")){
			for(AuditEventRecord auditEventRecord : ((Pager<AuditEventRecord>)model.get("pager")).getRecords()) {
	        	Record record = new Record();
	        	Header header = mapper.map(auditEventRecord.getAuditableObject(), Header.class);
		        record.setHeader(header);
		        if(!auditEventRecord.getRevisionType().equals(RevisionType.DEL)) {
		            Metadata metadata = new Metadata();
			        constructMetadata(metadata,(IdentifiableEntity)auditEventRecord.getAuditableObject());
		            record.setMetadata(metadata);
		        } else {
		        	header.setStatus(Status.DELETED);
		        }
		        listRecords.getRecord().add(record);
	        }

			if(model.containsKey("resumptionToken")) {
				listRecords.setResumptionToken((ResumptionToken)model.get("resumptionToken"));
			}

        } else if(model.containsKey("entitylist")){
			for( IdentifiableEntity idetifiableEntity : ((List<IdentifiableEntity>)model.get("entitylist"))) {
	        	Record record = new Record();
	        	Metadata metadata = new Metadata();
		        constructMetadata(metadata, idetifiableEntity);
	            record.setMetadata(metadata);
		        listRecords.getRecord().add(record);
	        }
        }


        oaiPmh.setListRecords(listRecords);
    }

    public abstract void constructMetadata(Metadata metadata,IdentifiableEntity identifiableEntity);
}
