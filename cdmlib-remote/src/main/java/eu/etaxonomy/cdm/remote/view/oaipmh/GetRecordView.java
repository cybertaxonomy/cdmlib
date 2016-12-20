package eu.etaxonomy.cdm.remote.view.oaipmh;

import java.util.Map;

import org.hibernate.envers.RevisionType;

import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.GetRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Header;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Metadata;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.OAIPMH;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Record;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Status;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.view.OaiPmhResponseView;

public abstract class GetRecordView extends OaiPmhResponseView {

    protected void constructResponse(OAIPMH oaiPmh,Map<String,Object> model) {
    	oaiPmh.getRequest().setVerb(Verb.GET_RECORD);
    	oaiPmh.getRequest().setValue((String)model.get("request"));
    	oaiPmh.getRequest().setMetadataPrefix((MetadataPrefix)model.get("metadataPrefix"));

    	GetRecord getRecord = new GetRecord();
    	AuditEventRecord<IdentifiableEntity> auditEventRecord = (AuditEventRecord<IdentifiableEntity>)model.get("object");
        Header header = (Header)mapper.map((IdentifiableEntity)auditEventRecord.getAuditableObject(), Header.class);
        Record record = new Record();
        record.setHeader(header);
        if(!auditEventRecord.getRevisionType().equals(RevisionType.DEL)) {
            Metadata metadata = new Metadata();
	        constructMetadata(metadata,(IdentifiableEntity)auditEventRecord.getAuditableObject());
            record.setMetadata(metadata);
        } else {
        	header.setStatus(Status.DELETED);
        }
     
        getRecord.setRecord(record);
	    oaiPmh.setGetRecord(getRecord);
    }

    public abstract void constructMetadata(Metadata metadata,IdentifiableEntity identifiableEntity);
}
