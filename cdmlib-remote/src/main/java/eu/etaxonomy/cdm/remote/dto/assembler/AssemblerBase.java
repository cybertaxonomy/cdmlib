package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.UUID;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.BaseSTO;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.IBaseSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;

public abstract class AssemblerBase<STO extends BaseSTO, TO extends BaseTO, C extends CdmBase> {
	
	public String getRandomUUID(){
		return UUID.randomUUID().toString();
	}
	public UUID getUUID(String uuid){
		return UUID.fromString(uuid);
	}
	public void setVersionableEntity(VersionableEntity cdmObj, IBaseSTO sto){		
		sto.setUuid(cdmObj.getUuid().toString());
	}
	public void setVersionableEntity(VersionableEntity identObj, BaseTO to){				
		to.setUuid(identObj.getUuid().toString());
		to.setCreated(identObj.getCreated());
		if(identObj.getCreatedBy()!=null){
			to.setCreatedBy(identObj.getCreatedBy().toString());			
		}
		to.setUpdated(identObj.getUpdated());
		if(identObj.getUpdatedBy()!=null){
			to.setUpdatedBy(identObj.getUpdatedBy().toString());
		}
	}
	
	abstract STO getSTO(C cdmObj);
	abstract TO getTO(C cdmObj);
}
